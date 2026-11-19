package push

import (
	"bytes"
	"context"
	"crypto/ecdsa"
	"crypto/rand"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const (
	apnsProductionBaseURL = "https://api.push.apple.com"
	apnsSandboxBaseURL    = "https://api.sandbox.push.apple.com"
	apnsTimeout           = 8 * time.Second
	apnsCoordinateBytes   = 32
)

// APNSConfig configures direct Apple Push Notification service delivery via HTTP/2 JWT auth.
type APNSConfig struct {
	KeyID         string
	TeamID        string
	BundleID      string
	PrivateKeyPEM string
	UseSandbox    bool
	EndpointURL   string
	HTTPClient    *http.Client
	Clock         func() time.Time
}

// APNSSender delivers alert notifications directly to Apple Push Notification service.
type APNSSender struct {
	cfg        APNSConfig
	privateKey *ecdsa.PrivateKey
	client     *http.Client
	clock      func() time.Time
}

// NewAPNSSender constructs an APNs push sender using an ES256 P-256 signing key.
func NewAPNSSender(cfg APNSConfig) *APNSSender {
	client := cfg.HTTPClient
	if client == nil {
		client = &http.Client{Timeout: apnsTimeout}
	}
	nowFn := cfg.Clock
	if nowFn == nil {
		nowFn = time.Now
	}
	parsedKey, _ := parseECDSAPrivateKey(cfg.PrivateKeyPEM)
	return &APNSSender{
		cfg:        cfg,
		privateKey: parsedKey,
		client:     client,
		clock:      nowFn,
	}
}

// IsConfigured reports whether APNs credentials and P-256 key are ready.
func (s *APNSSender) IsConfigured() bool {
	if s == nil {
		return false
	}
	if s.privateKey == nil {
		return false
	}
	if strings.TrimSpace(s.cfg.KeyID) == "" {
		return false
	}
	if strings.TrimSpace(s.cfg.TeamID) == "" {
		return false
	}
	return strings.TrimSpace(s.cfg.BundleID) != ""
}

// SendPush delivers an iOS notification through APNs HTTP/2 token-based authentication.
func (s *APNSSender) SendPush(ctx context.Context, msg Message) error {
	if !s.IsConfigured() {
		return ErrNotConfigured
	}
	cleanToken := strings.TrimSpace(msg.Token)
	if cleanToken == "" {
		return errors.New("apns device token is required")
	}
	bearerJWT, err := s.signProviderToken()
	if err != nil {
		return err
	}
	payloadBytes, err := buildAPNsPayload(msg)
	if err != nil {
		return err
	}
	targetURL := s.resolveDeviceURL(cleanToken)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, targetURL, bytes.NewReader(payloadBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "bearer "+bearerJWT)
	req.Header.Set("apns-topic", strings.TrimSpace(s.cfg.BundleID))
	req.Header.Set("apns-push-type", "alert")
	resp, err := s.client.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		rawErr, _ := io.ReadAll(io.LimitReader(resp.Body, 512))
		return fmt.Errorf("apns returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawErr)))
	}
	return nil
}

func (s *APNSSender) resolveDeviceURL(deviceToken string) string {
	override := strings.TrimSpace(s.cfg.EndpointURL)
	base := apnsProductionBaseURL
	if s.cfg.UseSandbox {
		base = apnsSandboxBaseURL
	}
	if override != "" {
		base = strings.TrimRight(override, "/")
	}
	return base + "/3/device/" + deviceToken
}

func (s *APNSSender) signProviderToken() (string, error) {
	headerBytes, err := json.Marshal(map[string]string{
		"alg": "ES256",
		"kid": strings.TrimSpace(s.cfg.KeyID),
	})
	if err != nil {
		return "", err
	}
	claimsBytes, err := json.Marshal(map[string]any{
		"iss": strings.TrimSpace(s.cfg.TeamID),
		"iat": s.clock().UTC().Unix(),
	})
	if err != nil {
		return "", err
	}
	signingInput := base64.RawURLEncoding.EncodeToString(headerBytes) + "." + base64.RawURLEncoding.EncodeToString(claimsBytes)
	digest := sha256.Sum256([]byte(signingInput))
	rVal, sVal, err := ecdsa.Sign(rand.Reader, s.privateKey, digest[:])
	if err != nil {
		return "", err
	}
	sig := make([]byte, apnsCoordinateBytes*2)
	rVal.FillBytes(sig[:apnsCoordinateBytes])
	sVal.FillBytes(sig[apnsCoordinateBytes:])
	return signingInput + "." + base64.RawURLEncoding.EncodeToString(sig), nil
}

func buildAPNsPayload(msg Message) ([]byte, error) {
	payload := map[string]any{
		"aps": map[string]any{
			"alert": map[string]string{
				"title": strings.TrimSpace(msg.Title),
				"body":  strings.TrimSpace(msg.Body),
			},
			"sound":    "default",
			"category": strings.TrimSpace(msg.Category),
		},
		"platform":   strings.TrimSpace(msg.Platform),
		"appVariant": strings.TrimSpace(msg.AppVariant),
	}
	return json.Marshal(payload)
}

func parseECDSAPrivateKey(rawPEM string) (*ecdsa.PrivateKey, error) {
	normalized := strings.ReplaceAll(strings.TrimSpace(rawPEM), `\n`, "\n")
	if normalized == "" {
		return nil, errors.New("empty apns private key")
	}
	block, _ := pem.Decode([]byte(normalized))
	if block == nil {
		return nil, errors.New("invalid apns pem block")
	}
	if keyAny, err := x509.ParsePKCS8PrivateKey(block.Bytes); err == nil {
		if ecKey, ok := keyAny.(*ecdsa.PrivateKey); ok {
			return ecKey, nil
		}
		return nil, errors.New("pkcs8 key is not ecdsa")
	}
	return x509.ParseECPrivateKey(block.Bytes)
}

// MultiSender routes iOS notifications to APNs, web subscriptions to Web Push, and mobile/Android tokens to FCM.
type MultiSender struct {
	fcm     Sender
	apns    Sender
	webPush Sender
}

// NewMultiSender builds a platform-aware push dispatcher combining FCM and APNs.
func NewMultiSender(fcm, apns Sender) *MultiSender {
	return &MultiSender{fcm: fcm, apns: apns}
}

// WithWebPush configures a VAPID + RFC 8291 Web Push sender for browser subscriptions.
func (m *MultiSender) WithWebPush(sender Sender) *MultiSender {
	if m == nil {
		return nil
	}
	m.webPush = sender
	return m
}

// IsConfigured reports true when at least one underlying transport is configured.
func (m *MultiSender) IsConfigured() bool {
	if m == nil {
		return false
	}
	if isSenderReady(m.apns) {
		return true
	}
	if isSenderReady(m.webPush) {
		return true
	}
	return isSenderReady(m.fcm)
}

// SendPush routes iOS to APNs, Web to Web Push, and other platforms to FCM.
func (m *MultiSender) SendPush(ctx context.Context, msg Message) error {
	if m == nil {
		return ErrNotConfigured
	}
	platform := strings.ToLower(strings.TrimSpace(msg.Platform))
	if platform == "ios" {
		if isSenderReady(m.apns) {
			return m.apns.SendPush(ctx, msg)
		}
	}
	if platform == "web" {
		if isSenderReady(m.webPush) {
			return m.webPush.SendPush(ctx, msg)
		}
		return ErrNotConfigured
	}
	if isSenderReady(m.fcm) {
		return m.fcm.SendPush(ctx, msg)
	}
	return ErrNotConfigured
}

func isSenderReady(sender Sender) bool {
	if sender == nil {
		return false
	}
	return sender.IsConfigured()
}
