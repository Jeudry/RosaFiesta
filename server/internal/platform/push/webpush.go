package push

import (
	"bytes"
	"context"
	"crypto/aes"
	"crypto/cipher"
	"crypto/ecdh"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/hmac"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"
)

const (
	defaultVAPIDSubject   = "mailto:hola@rosafiesta.do"
	defaultWebPushTTL     = 86400
	webPushRecordSize     = 4096
	webPushTimeout        = 10 * time.Second
	vapidTokenLifetime    = 12 * time.Hour
	aes128KeyLength       = 16
	aesGCMNonceLength     = 12
	p256PublicKeyByteSize = 65
)

// WebPushConfig configures VAPID and RFC 8291 aes128gcm delivery to browser PushManager subscriptions.
type WebPushConfig struct {
	VAPIDPublicKey        string
	VAPIDPrivateKey       string
	Subject               string
	TTLSeconds            int
	AllowPrivateEndpoints bool
	HTTPClient            *http.Client
	Clock                 func() time.Time
}

// WebPushSender delivers encrypted Web Push notifications to browser PushSubscription tokens.
type WebPushSender struct {
	cfg           WebPushConfig
	privateKey    *ecdsa.PrivateKey
	vapidPubBytes []byte
	vapidPubB64   string
	subject       string
	ttlSeconds    int
	client        *http.Client
	clock         func() time.Time
}

type webPushSubscription struct {
	Endpoint string              `json:"endpoint"`
	Keys     webPushKeyMaterials `json:"keys"`
}

type webPushKeyMaterials struct {
	P256DH string `json:"p256dh"`
	Auth   string `json:"auth"`
}

// NewWebPushSender builds a VAPID + RFC 8291 Web Push sender from raw base64url or PEM VAPID keys.
func NewWebPushSender(cfg WebPushConfig) *WebPushSender {
	client := cfg.HTTPClient
	if client == nil {
		client = &http.Client{
			Timeout: webPushTimeout,
			CheckRedirect: func(*http.Request, []*http.Request) error {
				return http.ErrUseLastResponse
			},
		}
	}
	nowFn := cfg.Clock
	if nowFn == nil {
		nowFn = time.Now
	}
	subject := strings.TrimSpace(cfg.Subject)
	if subject == "" {
		subject = defaultVAPIDSubject
	}
	ttl := cfg.TTLSeconds
	if ttl <= 0 {
		ttl = defaultWebPushTTL
	}
	privKey, pubBytes, pubB64 := parseVAPIDKeyPair(cfg.VAPIDPrivateKey, cfg.VAPIDPublicKey)
	return &WebPushSender{
		cfg:           cfg,
		privateKey:    privKey,
		vapidPubBytes: pubBytes,
		vapidPubB64:   pubB64,
		subject:       subject,
		ttlSeconds:    ttl,
		client:        client,
		clock:         nowFn,
	}
}

// IsConfigured reports whether valid VAPID public/private keys are loaded.
func (s *WebPushSender) IsConfigured() bool {
	if s == nil {
		return false
	}
	if s.privateKey == nil {
		return false
	}
	return len(s.vapidPubBytes) == p256PublicKeyByteSize
}

// SendPush encrypts the notification payload per RFC 8291 and posts it with VAPID authentication.
func (s *WebPushSender) SendPush(ctx context.Context, msg Message) error {
	if !s.IsConfigured() {
		return ErrNotConfigured
	}
	sub, uaPub, authSecret, err := parseWebPushSubscription(ctx, msg.Token, s.cfg.AllowPrivateEndpoints)
	if err != nil {
		return err
	}
	payloadBytes, err := marshalWebPushPayload(msg)
	if err != nil {
		return err
	}
	body, err := encryptAES128GCM(payloadBytes, uaPub, authSecret)
	if err != nil {
		return err
	}
	jwtToken, err := s.signVAPIDJWT(sub.Endpoint)
	if err != nil {
		return err
	}
	return s.dispatchWebPushRequest(ctx, sub.Endpoint, jwtToken, body)
}

func marshalWebPushPayload(msg Message) ([]byte, error) {
	payload := map[string]string{
		"title":      strings.TrimSpace(msg.Title),
		"body":       strings.TrimSpace(msg.Body),
		"category":   strings.TrimSpace(msg.Category),
		"appVariant": strings.TrimSpace(msg.AppVariant),
	}
	return json.Marshal(payload)
}

func (s *WebPushSender) dispatchWebPushRequest(
	ctx context.Context,
	endpoint string,
	jwtToken string,
	encryptedBody []byte,
) error {
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(encryptedBody))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/octet-stream")
	req.Header.Set("Content-Encoding", "aes128gcm")
	req.Header.Set("TTL", strconv.Itoa(s.ttlSeconds))
	req.Header.Set("Urgency", "normal")
	req.Header.Set("Authorization", fmt.Sprintf("vapid t=%s, k=%s", jwtToken, s.vapidPubB64))
	resp, err := s.client.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		rawErr, _ := io.ReadAll(io.LimitReader(resp.Body, 512))
		return fmt.Errorf("web push returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawErr)))
	}
	return nil
}

func (s *WebPushSender) signVAPIDJWT(rawEndpoint string) (string, error) {
	parsedURL, err := url.Parse(rawEndpoint)
	if err != nil {
		return "", err
	}
	audience := parsedURL.Scheme + "://" + parsedURL.Host
	now := s.clock().UTC()
	headerBytes, _ := json.Marshal(map[string]string{"typ": "JWT", "alg": "ES256"})
	claimsBytes, _ := json.Marshal(map[string]any{
		"aud": audience,
		"exp": now.Add(vapidTokenLifetime).Unix(),
		"sub": s.subject,
	})
	signingInput := base64.RawURLEncoding.EncodeToString(headerBytes) + "." + base64.RawURLEncoding.EncodeToString(claimsBytes)
	digest := sha256.Sum256([]byte(signingInput))
	rVal, sVal, err := ecdsa.Sign(rand.Reader, s.privateKey, digest[:])
	if err != nil {
		return "", err
	}
	sig := make([]byte, 64)
	rVal.FillBytes(sig[:32])
	sVal.FillBytes(sig[32:])
	return signingInput + "." + base64.RawURLEncoding.EncodeToString(sig), nil
}

func parseWebPushSubscription(ctx context.Context, rawToken string, allowPrivate bool) (webPushSubscription, *ecdh.PublicKey, []byte, error) {
	var sub webPushSubscription
	if err := json.Unmarshal([]byte(strings.TrimSpace(rawToken)), &sub); err != nil {
		return webPushSubscription{}, nil, nil, fmt.Errorf("invalid web push subscription json: %w", err)
	}
	endpoint := strings.TrimSpace(sub.Endpoint)
	if endpoint == "" {
		return webPushSubscription{}, nil, nil, errors.New("web push subscription endpoint is required")
	}
	parsedEndpoint, err := url.Parse(endpoint)
	if err != nil {
		return webPushSubscription{}, nil, nil, fmt.Errorf("invalid web push endpoint url: %w", err)
	}
	if err := validateWebPushEndpoint(ctx, parsedEndpoint, allowPrivate); err != nil {
		return webPushSubscription{}, nil, nil, err
	}
	sub.Endpoint = endpoint
	uaPubBytes, err := decodeBase64URLFlexible(sub.Keys.P256DH)
	if err != nil {
		return webPushSubscription{}, nil, nil, fmt.Errorf("invalid p256dh key: %w", err)
	}
	uaPub, err := ecdh.P256().NewPublicKey(uaPubBytes)
	if err != nil {
		return webPushSubscription{}, nil, nil, fmt.Errorf("invalid p256dh curve point: %w", err)
	}
	authSecret, err := decodeBase64URLFlexible(sub.Keys.Auth)
	if err != nil {
		return webPushSubscription{}, nil, nil, errors.New("invalid web push auth secret")
	}
	if len(authSecret) < 16 {
		return webPushSubscription{}, nil, nil, errors.New("invalid web push auth secret")
	}
	return sub, uaPub, authSecret, nil
}

func validateWebPushEndpoint(ctx context.Context, parsed *url.URL, allowPrivate bool) error {
	scheme := strings.ToLower(strings.TrimSpace(parsed.Scheme))
	host := strings.TrimSpace(parsed.Hostname())
	if host == "" {
		return errors.New("web push endpoint host is required")
	}
	if allowPrivate {
		if scheme == "http" {
			return nil
		}
		if scheme == "https" {
			return nil
		}
		return errors.New("web push endpoint must use http or https")
	}
	if scheme != "https" {
		return errors.New("web push endpoint must use https")
	}
	return rejectPrivateEndpointHost(ctx, host)
}

func rejectPrivateEndpointHost(ctx context.Context, host string) error {
	lowerHost := strings.ToLower(strings.TrimSpace(host))
	if lowerHost == "localhost" {
		return errors.New("web push loopback endpoint is forbidden")
	}
	if strings.HasSuffix(lowerHost, ".localhost") {
		return errors.New("web push loopback endpoint is forbidden")
	}
	if ip := net.ParseIP(lowerHost); ip != nil {
		if isBlockedPushIP(ip) {
			return errors.New("web push private or loopback ip is forbidden")
		}
		return nil
	}
	ips, err := (&net.Resolver{}).LookupIPAddr(ctx, lowerHost)
	if err != nil {
		return fmt.Errorf("resolve web push host: %w", err)
	}
	for _, resolved := range ips {
		if isBlockedPushIP(resolved.IP) {
			return errors.New("web push private or loopback ip is forbidden")
		}
	}
	return nil
}

func isBlockedPushIP(ip net.IP) bool {
	if ip.IsLoopback() {
		return true
	}
	if ip.IsPrivate() {
		return true
	}
	if ip.IsLinkLocalUnicast() {
		return true
	}
	if ip.IsLinkLocalMulticast() {
		return true
	}
	if ip.IsMulticast() {
		return true
	}
	return ip.IsUnspecified()
}

func encryptAES128GCM(plaintext []byte, uaPub *ecdh.PublicKey, authSecret []byte) ([]byte, error) {
	salt := make([]byte, aes128KeyLength)
	if _, err := io.ReadFull(rand.Reader, salt); err != nil {
		return nil, err
	}
	asPriv, err := ecdh.P256().GenerateKey(rand.Reader)
	if err != nil {
		return nil, err
	}
	asPubBytes := asPriv.PublicKey().Bytes()
	sharedSecret, err := asPriv.ECDH(uaPub)
	if err != nil {
		return nil, err
	}
	cek, nonce := deriveRFC8291Keys(sharedSecret, authSecret, uaPub.Bytes(), asPubBytes, salt)
	block, err := aes.NewCipher(cek)
	if err != nil {
		return nil, err
	}
	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}
	paddedRecord := make([]byte, len(plaintext)+1)
	copy(paddedRecord, plaintext)
	paddedRecord[len(plaintext)] = 0x02
	sealed := gcm.Seal(nil, nonce, paddedRecord, nil)

	if len(asPubBytes) != p256PublicKeyByteSize {
		return nil, errors.New("unexpected p256 ephemeral public key length")
	}
	header := make([]byte, 16+4+1+len(asPubBytes))
	copy(header[:16], salt)
	binary.BigEndian.PutUint32(header[16:20], webPushRecordSize)
	header[20] = p256PublicKeyByteSize
	copy(header[21:], asPubBytes)
	return append(header, sealed...), nil
}

func deriveRFC8291Keys(sharedSecret, authSecret, uaPubBytes, asPubBytes, salt []byte) ([]byte, []byte) {
	ikmInfo := make([]byte, 0, len("WebPush: info\x00")+len(uaPubBytes)+len(asPubBytes))
	ikmInfo = append(ikmInfo, []byte("WebPush: info\x00")...)
	ikmInfo = append(ikmInfo, uaPubBytes...)
	ikmInfo = append(ikmInfo, asPubBytes...)

	prkCombine := hkdfExtractSHA256(authSecret, sharedSecret)
	ikm := hkdfExpandSHA256(prkCombine, ikmInfo, 32)
	prkContent := hkdfExtractSHA256(salt, ikm)
	cek := hkdfExpandSHA256(prkContent, []byte("Content-Encoding: aes128gcm\x00"), aes128KeyLength)
	nonce := hkdfExpandSHA256(prkContent, []byte("Content-Encoding: nonce\x00"), aesGCMNonceLength)
	return cek, nonce
}

func hkdfExtractSHA256(salt, ikm []byte) []byte {
	mac := hmac.New(sha256.New, salt)
	mac.Write(ikm)
	return mac.Sum(nil)
}

func hkdfExpandSHA256(prk, info []byte, length int) []byte {
	var out []byte
	var prev []byte
	counter := byte(1)
	for len(out) < length {
		mac := hmac.New(sha256.New, prk)
		mac.Write(prev)
		mac.Write(info)
		mac.Write([]byte{counter})
		prev = mac.Sum(nil)
		out = append(out, prev...)
		counter++
	}
	return out[:length]
}

func parseVAPIDKeyPair(rawPriv, rawPub string) (*ecdsa.PrivateKey, []byte, string) {
	cleanPriv := strings.TrimSpace(rawPriv)
	if cleanPriv == "" {
		return nil, nil, ""
	}
	privKey, err := parseVAPIDPrivateKey(cleanPriv)
	if err != nil {
		return nil, nil, ""
	}
	ecdhPriv, err := privKey.ECDH()
	if err != nil {
		return nil, nil, ""
	}
	derivedPub := ecdhPriv.PublicKey().Bytes()
	cleanPub := strings.TrimSpace(rawPub)
	if cleanPub == "" {
		return privKey, derivedPub, base64.RawURLEncoding.EncodeToString(derivedPub)
	}
	decodedPub, err := decodeBase64URLFlexible(cleanPub)
	if err != nil {
		return nil, nil, ""
	}
	if len(decodedPub) != p256PublicKeyByteSize {
		return nil, nil, ""
	}
	return privKey, decodedPub, base64.RawURLEncoding.EncodeToString(decodedPub)
}

func parseVAPIDPrivateKey(rawPriv string) (*ecdsa.PrivateKey, error) {
	if strings.Contains(rawPriv, "BEGIN ") {
		return parseECDSAPrivateKey(rawPriv)
	}
	scalar, err := decodeBase64URLFlexible(rawPriv)
	if err != nil {
		return nil, errors.New("invalid raw vapid private key")
	}
	if len(scalar) != 32 {
		return nil, errors.New("invalid raw vapid private key")
	}
	return ecdsa.ParseRawPrivateKey(elliptic.P256(), scalar)
}

func decodeBase64URLFlexible(raw string) ([]byte, error) {
	clean := strings.TrimSpace(raw)
	if clean == "" {
		return nil, errors.New("empty base64 string")
	}
	if decoded, err := base64.RawURLEncoding.DecodeString(clean); err == nil {
		return decoded, nil
	}
	if decoded, err := base64.URLEncoding.DecodeString(clean); err == nil {
		return decoded, nil
	}
	if decoded, err := base64.RawStdEncoding.DecodeString(clean); err == nil {
		return decoded, nil
	}
	return base64.StdEncoding.DecodeString(clean)
}
