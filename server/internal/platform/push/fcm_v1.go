package push

import (
	"bytes"
	"context"
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"
)

const (
	defaultGoogleOAuthEndpoint = "https://oauth2.googleapis.com/token"
	fcmV1Scope                 = "https://www.googleapis.com/auth/firebase.messaging"
	fcmV1Timeout               = 10 * time.Second
	tokenRefreshMargin         = 60 * time.Second
)

// FCMV1Config configures Firebase Cloud Messaging HTTP v1 using Google Service Account credentials.
type FCMV1Config struct {
	ServiceAccountJSON string
	ProjectID          string
	EndpointURL        string
	TokenURL           string
	HTTPClient         *http.Client
	Clock              func() time.Time
}

// FCMV1Sender delivers Android and mobile notifications via the FCM HTTP v1 API.
type FCMV1Sender struct {
	projectID   string
	clientEmail string
	privateKey  *rsa.PrivateKey
	endpointURL string
	tokenURL    string
	client      *http.Client
	clock       func() time.Time
	mu          sync.Mutex
	cachedToken string
	tokenExpiry time.Time
}

type serviceAccountPayload struct {
	ProjectID   string `json:"project_id"`
	ClientEmail string `json:"client_email"`
	PrivateKey  string `json:"private_key"`
	TokenURI    string `json:"token_uri"`
}

// NewFCMV1Sender parses the Service Account JSON (raw or base64) and creates an FCM HTTP v1 sender.
func NewFCMV1Sender(cfg FCMV1Config) *FCMV1Sender {
	client := cfg.HTTPClient
	if client == nil {
		client = &http.Client{Timeout: fcmV1Timeout}
	}
	nowFn := cfg.Clock
	if nowFn == nil {
		nowFn = time.Now
	}
	sa, privKey, ok := parseServiceAccount(cfg.ServiceAccountJSON)
	if !ok {
		return &FCMV1Sender{client: client, clock: nowFn}
	}
	projectID := strings.TrimSpace(cfg.ProjectID)
	if projectID == "" {
		projectID = strings.TrimSpace(sa.ProjectID)
	}
	tokenURL := strings.TrimSpace(cfg.TokenURL)
	if tokenURL == "" {
		tokenURL = strings.TrimSpace(sa.TokenURI)
	}
	if tokenURL == "" {
		tokenURL = defaultGoogleOAuthEndpoint
	}
	endpointURL := strings.TrimSpace(cfg.EndpointURL)
	if endpointURL == "" && projectID != "" {
		endpointURL = fmt.Sprintf("https://fcm.googleapis.com/v1/projects/%s/messages:send", projectID)
	}
	return &FCMV1Sender{
		projectID:   projectID,
		clientEmail: strings.TrimSpace(sa.ClientEmail),
		privateKey:  privKey,
		endpointURL: endpointURL,
		tokenURL:    tokenURL,
		client:      client,
		clock:       nowFn,
	}
}

// IsConfigured reports whether Service Account credentials and target endpoint are ready.
func (s *FCMV1Sender) IsConfigured() bool {
	if s == nil {
		return false
	}
	if s.privateKey == nil {
		return false
	}
	if s.projectID == "" {
		return false
	}
	if s.clientEmail == "" {
		return false
	}
	return s.endpointURL != ""
}

// SendPush obtains a scoped Google OAuth2 bearer token and dispatches an FCM v1 message.
func (s *FCMV1Sender) SendPush(ctx context.Context, msg Message) error {
	if !s.IsConfigured() {
		return ErrNotConfigured
	}
	cleanToken := strings.TrimSpace(msg.Token)
	if cleanToken == "" {
		return errors.New("device token is required")
	}
	bearer, err := s.resolveAccessToken(ctx)
	if err != nil {
		return err
	}
	body, err := marshalFCMV1Payload(cleanToken, msg)
	if err != nil {
		return err
	}
	return s.postMessage(ctx, bearer, body)
}

func marshalFCMV1Payload(token string, msg Message) ([]byte, error) {
	payload := map[string]any{
		"message": map[string]any{
			"token": token,
			"notification": map[string]string{
				"title": strings.TrimSpace(msg.Title),
				"body":  strings.TrimSpace(msg.Body),
			},
			"data": map[string]string{
				"platform":   strings.TrimSpace(msg.Platform),
				"appVariant": strings.TrimSpace(msg.AppVariant),
				"category":   strings.TrimSpace(msg.Category),
			},
		},
	}
	return json.Marshal(payload)
}

func (s *FCMV1Sender) postMessage(ctx context.Context, bearer string, body []byte) error {
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.endpointURL, bytes.NewReader(body))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+bearer)
	resp, err := s.client.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		rawErr, _ := io.ReadAll(io.LimitReader(resp.Body, 512))
		return fmt.Errorf("fcm v1 returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawErr)))
	}
	return nil
}

func (s *FCMV1Sender) resolveAccessToken(ctx context.Context) (string, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	now := s.clock().UTC()
	hasValidToken := s.cachedToken != "" && now.Add(tokenRefreshMargin).Before(s.tokenExpiry)
	if hasValidToken {
		return s.cachedToken, nil
	}
	assertion, err := s.signServiceAccountJWT(now)
	if err != nil {
		return "", err
	}
	accessToken, expiresIn, err := s.exchangeAssertionForToken(ctx, assertion)
	if err != nil {
		return "", err
	}
	s.cachedToken = accessToken
	s.tokenExpiry = now.Add(time.Duration(expiresIn) * time.Second)
	return accessToken, nil
}

func (s *FCMV1Sender) signServiceAccountJWT(now time.Time) (string, error) {
	headerBytes, _ := json.Marshal(map[string]string{"alg": "RS256", "typ": "JWT"})
	claimsBytes, _ := json.Marshal(map[string]any{
		"iss":   s.clientEmail,
		"scope": fcmV1Scope,
		"aud":   s.tokenURL,
		"iat":   now.Unix(),
		"exp":   now.Add(time.Hour).Unix(),
	})
	signingInput := base64.RawURLEncoding.EncodeToString(headerBytes) + "." + base64.RawURLEncoding.EncodeToString(claimsBytes)
	digest := sha256.Sum256([]byte(signingInput))
	sigBytes, err := rsa.SignPKCS1v15(rand.Reader, s.privateKey, crypto.SHA256, digest[:])
	if err != nil {
		return "", err
	}
	return signingInput + "." + base64.RawURLEncoding.EncodeToString(sigBytes), nil
}

func (s *FCMV1Sender) exchangeAssertionForToken(ctx context.Context, assertion string) (string, int64, error) {
	form := url.Values{}
	form.Set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
	form.Set("assertion", assertion)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.tokenURL, strings.NewReader(form.Encode()))
	if err != nil {
		return "", 0, err
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	resp, err := s.client.Do(req)
	if err != nil {
		return "", 0, err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		rawErr, _ := io.ReadAll(io.LimitReader(resp.Body, 512))
		return "", 0, fmt.Errorf("oauth token exchange returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawErr)))
	}
	var tokenResp struct {
		AccessToken string `json:"access_token"`
		ExpiresIn   int64  `json:"expires_in"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&tokenResp); err != nil {
		return "", 0, err
	}
	if strings.TrimSpace(tokenResp.AccessToken) == "" {
		return "", 0, errors.New("empty oauth access token from google")
	}
	expiresIn := tokenResp.ExpiresIn
	if expiresIn <= 0 {
		expiresIn = 3600
	}
	return tokenResp.AccessToken, expiresIn, nil
}

func parseServiceAccount(raw string) (serviceAccountPayload, *rsa.PrivateKey, bool) {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return serviceAccountPayload{}, nil, false
	}
	jsonBytes := []byte(trimmed)
	if !strings.HasPrefix(trimmed, "{") {
		decoded, err := base64.StdEncoding.DecodeString(trimmed)
		if err != nil {
			return serviceAccountPayload{}, nil, false
		}
		jsonBytes = decoded
	}
	var sa serviceAccountPayload
	if err := json.Unmarshal(jsonBytes, &sa); err != nil {
		return serviceAccountPayload{}, nil, false
	}
	privKey, err := parseRSAPrivateKey(sa.PrivateKey)
	if err != nil {
		return serviceAccountPayload{}, nil, false
	}
	return sa, privKey, true
}

func parseRSAPrivateKey(rawPEM string) (*rsa.PrivateKey, error) {
	normalized := strings.ReplaceAll(strings.TrimSpace(rawPEM), `\n`, "\n")
	block, _ := pem.Decode([]byte(normalized))
	if block == nil {
		return nil, errors.New("invalid rsa pem key")
	}
	if keyAny, err := x509.ParsePKCS8PrivateKey(block.Bytes); err == nil {
		if rsaKey, ok := keyAny.(*rsa.PrivateKey); ok {
			return rsaKey, nil
		}
		return nil, errors.New("pkcs8 key is not rsa")
	}
	return x509.ParsePKCS1PrivateKey(block.Bytes)
}
