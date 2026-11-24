package app

import (
	"context"
	"crypto"
	"crypto/ecdsa"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"io"
	"math/big"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

const (
	defaultGoogleJWKSURL    = "https://www.googleapis.com/oauth2/v3/certs"
	defaultAppleJWKSURL     = "https://appleid.apple.com/auth/keys"
	googleOAuthExchangeURL  = "https://oauth2.googleapis.com/token"
	appleOAuthExchangeURL   = "https://appleid.apple.com/auth/token"
	googleAuthorizeURL      = "https://accounts.google.com/o/oauth2/v2/auth"
	appleAuthorizeURL       = "https://appleid.apple.com/auth/authorize"
	appleAudienceURL        = "https://appleid.apple.com"
	jwksCacheTTL            = 5 * time.Minute
	appleClientSecretTTL    = 5 * time.Minute
	es256CoordinateByteSize = 32
	minPKCEVerifierLen      = 43
	minPKCEChallengeLen     = 43
)

type OAuthStartResult struct {
	Provider     string `json:"provider"`
	AuthorizeURL string `json:"authorizeUrl"`
	State        string `json:"state"`
}

type oauthTokenHeader struct {
	Algorithm string `json:"alg"`
	KeyID     string `json:"kid"`
}

type oauthTokenClaims struct {
	Issuer        string        `json:"iss"`
	Audience      string        `json:"aud"`
	Email         string        `json:"email"`
	EmailVerified *flexibleBool `json:"email_verified"`
	Name          string        `json:"name"`
	ExpiresAt     int64         `json:"exp"`
}

type flexibleBool bool

func (b *flexibleBool) UnmarshalJSON(raw []byte) error {
	trimmed := strings.TrimSpace(string(raw))
	if trimmed == "true" {
		*b = true
		return nil
	}
	if trimmed == `"true"` {
		*b = true
		return nil
	}
	if trimmed == "false" {
		*b = false
		return nil
	}
	if trimmed == `"false"` {
		*b = false
		return nil
	}
	return domain.ErrOAuthInvalidToken
}

type jwkDocument struct {
	Keys []jwkKeyEntry `json:"keys"`
}

type jwkKeyEntry struct {
	KeyType   string `json:"kty"`
	KeyID     string `json:"kid"`
	Algorithm string `json:"alg"`
	Modulus   string `json:"n"`
	Exponent  string `json:"e"`
}

type cachedJWKS struct {
	keys      map[string]*rsa.PublicKey
	fetchedAt time.Time
}

type jwksKeyCache struct {
	mu      sync.RWMutex
	client  *http.Client
	entries map[string]cachedJWKS
}

func newJWKSKeyCache(client *http.Client) *jwksKeyCache {
	resolvedClient := client
	if resolvedClient == nil {
		resolvedClient = &http.Client{Timeout: 10 * time.Second}
	}
	return &jwksKeyCache{
		client:  resolvedClient,
		entries: make(map[string]cachedJWKS),
	}
}

func (c *jwksKeyCache) publicKey(ctx context.Context, jwksURL, keyID string, now time.Time) (*rsa.PublicKey, error) {
	cleanKID := strings.TrimSpace(keyID)
	if cleanKID == "" {
		return nil, domain.ErrOAuthInvalidToken
	}
	if key, found := c.lookupCachedKey(jwksURL, cleanKID, now); found {
		return key, nil
	}
	keys, err := c.fetchJWKS(ctx, jwksURL, now)
	if err != nil {
		return nil, err
	}
	key, ok := keys[cleanKID]
	if !ok {
		return nil, domain.ErrOAuthInvalidToken
	}
	return key, nil
}

func (c *jwksKeyCache) lookupCachedKey(jwksURL, keyID string, now time.Time) (*rsa.PublicKey, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()
	cached, exists := c.entries[jwksURL]
	if !exists {
		return nil, false
	}
	if now.Sub(cached.fetchedAt) >= jwksCacheTTL {
		return nil, false
	}
	key, ok := cached.keys[keyID]
	return key, ok
}

func (c *jwksKeyCache) fetchJWKS(ctx context.Context, jwksURL string, now time.Time) (map[string]*rsa.PublicKey, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, jwksURL, nil)
	if err != nil {
		return nil, domain.ErrOAuthInvalidToken
	}
	resp, err := c.client.Do(req)
	if err != nil {
		return nil, domain.ErrOAuthInvalidToken
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return nil, domain.ErrOAuthInvalidToken
	}
	body, err := io.ReadAll(io.LimitReader(resp.Body, 512*1024))
	if err != nil {
		return nil, domain.ErrOAuthInvalidToken
	}
	keys, err := decodeJWKSKeys(body)
	if err != nil {
		return nil, err
	}
	c.mu.Lock()
	c.entries[jwksURL] = cachedJWKS{keys: keys, fetchedAt: now}
	c.mu.Unlock()
	return keys, nil
}

func decodeJWKSKeys(raw []byte) (map[string]*rsa.PublicKey, error) {
	var doc jwkDocument
	if err := json.Unmarshal(raw, &doc); err != nil {
		return nil, domain.ErrOAuthInvalidToken
	}
	keys := make(map[string]*rsa.PublicKey, len(doc.Keys))
	for _, entry := range doc.Keys {
		pubKey, ok := parseRSAPublicJWK(entry)
		if ok {
			keys[entry.KeyID] = pubKey
		}
	}
	if len(keys) == 0 {
		return nil, domain.ErrOAuthInvalidToken
	}
	return keys, nil
}

func parseRSAPublicJWK(entry jwkKeyEntry) (*rsa.PublicKey, bool) {
	if entry.KeyType != "RSA" {
		return nil, false
	}
	if strings.TrimSpace(entry.KeyID) == "" {
		return nil, false
	}
	modBytes, err := base64.RawURLEncoding.DecodeString(entry.Modulus)
	if err != nil {
		return nil, false
	}
	expBytes, err := base64.RawURLEncoding.DecodeString(entry.Exponent)
	if err != nil {
		return nil, false
	}
	expInt := new(big.Int).SetBytes(expBytes)
	if !expInt.IsInt64() {
		return nil, false
	}
	exponent := int(expInt.Int64())
	if exponent <= 1 {
		return nil, false
	}
	return &rsa.PublicKey{
		N: new(big.Int).SetBytes(modBytes),
		E: exponent,
	}, true
}

func (s *Service) verifyOAuthIDToken(
	ctx context.Context,
	provider string,
	idToken string,
	expectedAudience string,
) (oauthTokenClaims, error) {
	trimmed := strings.TrimSpace(idToken)
	parts := strings.Split(trimmed, ".")
	if len(parts) != 3 {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	header, err := parseOAuthHeader(parts[0])
	if err != nil {
		return oauthTokenClaims{}, err
	}
	claims, err := parseOAuthClaims(parts[1], provider, expectedAudience, s.clock.Now())
	if err != nil {
		return oauthTokenClaims{}, err
	}
	signature, err := base64.RawURLEncoding.DecodeString(parts[2])
	if err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	jwksURL := s.resolveJWKSURL(provider)
	pubKey, err := s.jwksCache.publicKey(ctx, jwksURL, header.KeyID, s.clock.Now())
	if err != nil {
		return oauthTokenClaims{}, err
	}
	signingInput := parts[0] + "." + parts[1]
	digest := sha256.Sum256([]byte(signingInput))
	if err := rsa.VerifyPKCS1v15(pubKey, crypto.SHA256, digest[:], signature); err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	return claims, nil
}

func parseOAuthHeader(encodedHeader string) (oauthTokenHeader, error) {
	raw, err := base64.RawURLEncoding.DecodeString(encodedHeader)
	if err != nil {
		return oauthTokenHeader{}, domain.ErrOAuthInvalidToken
	}
	var header oauthTokenHeader
	if err := json.Unmarshal(raw, &header); err != nil {
		return oauthTokenHeader{}, domain.ErrOAuthInvalidToken
	}
	if header.Algorithm != "RS256" {
		return oauthTokenHeader{}, domain.ErrOAuthInvalidToken
	}
	return header, nil
}

func parseOAuthClaims(
	encodedPayload string,
	provider string,
	expectedAudience string,
	now time.Time,
) (oauthTokenClaims, error) {
	raw, err := base64.RawURLEncoding.DecodeString(encodedPayload)
	if err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	var claims oauthTokenClaims
	if err := json.Unmarshal(raw, &claims); err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if !isAllowedOAuthIssuer(provider, claims.Issuer) {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if claims.Audience != expectedAudience {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if now.Unix() >= claims.ExpiresAt {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if claims.EmailVerified == nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if !bool(*claims.EmailVerified) {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	normalizedEmail, err := domain.NormalizeEmail(claims.Email)
	if err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	claims.Email = normalizedEmail
	return claims, nil
}

func isAllowedOAuthIssuer(provider, issuer string) bool {
	cleanIssuer := strings.TrimSpace(issuer)
	if provider == "google" {
		if cleanIssuer == "https://accounts.google.com" {
			return true
		}
		return cleanIssuer == "accounts.google.com"
	}
	if provider == "apple" {
		return cleanIssuer == "https://appleid.apple.com"
	}
	return false
}

func (s *Service) resolveJWKSURL(provider string) string {
	if provider == "google" {
		if strings.TrimSpace(s.opts.GoogleJWKSURL) != "" {
			return strings.TrimSpace(s.opts.GoogleJWKSURL)
		}
		return defaultGoogleJWKSURL
	}
	if strings.TrimSpace(s.opts.AppleJWKSURL) != "" {
		return strings.TrimSpace(s.opts.AppleJWKSURL)
	}
	return defaultAppleJWKSURL
}

func (s *Service) resolveExchangeEndpoint(provider string) string {
	if provider == "google" {
		if strings.TrimSpace(s.opts.GoogleTokenURL) != "" {
			return strings.TrimSpace(s.opts.GoogleTokenURL)
		}
		return googleOAuthExchangeURL
	}
	if strings.TrimSpace(s.opts.AppleTokenURL) != "" {
		return strings.TrimSpace(s.opts.AppleTokenURL)
	}
	return appleOAuthExchangeURL
}

func (s *Service) configuredOAuthClientSecret(provider, clientID string) string {
	if provider == "google" {
		return strings.TrimSpace(s.opts.GoogleClientSecret)
	}
	if provider == "apple" {
		if signed, ok := signAppleClientSecretJWT(s.opts, clientID, s.clock.Now()); ok {
			return signed
		}
		return strings.TrimSpace(s.opts.AppleClientSecret)
	}
	return ""
}

func signAppleClientSecretJWT(opts Options, clientID string, now time.Time) (string, bool) {
	teamID := strings.TrimSpace(opts.AppleTeamID)
	keyID := strings.TrimSpace(opts.AppleKeyID)
	trimmedClient := strings.TrimSpace(clientID)
	privKey, err := parseApplePrivateKey(opts.ApplePrivateKey)
	if err != nil {
		return "", false
	}
	if teamID == "" {
		return "", false
	}
	if keyID == "" {
		return "", false
	}
	if trimmedClient == "" {
		return "", false
	}
	headerBytes, err := json.Marshal(map[string]string{"alg": "ES256", "kid": keyID, "typ": "JWT"})
	if err != nil {
		return "", false
	}
	issuedAt := now.UTC().Unix()
	claimsBytes, err := json.Marshal(map[string]any{
		"iss": teamID,
		"iat": issuedAt,
		"exp": issuedAt + int64(appleClientSecretTTL.Seconds()),
		"aud": appleAudienceURL,
		"sub": trimmedClient,
	})
	if err != nil {
		return "", false
	}
	signingInput := base64.RawURLEncoding.EncodeToString(headerBytes) + "." + base64.RawURLEncoding.EncodeToString(claimsBytes)
	digest := sha256.Sum256([]byte(signingInput))
	rVal, sVal, err := ecdsa.Sign(rand.Reader, privKey, digest[:])
	if err != nil {
		return "", false
	}
	sig := make([]byte, es256CoordinateByteSize*2)
	rVal.FillBytes(sig[:es256CoordinateByteSize])
	sVal.FillBytes(sig[es256CoordinateByteSize:])
	return signingInput + "." + base64.RawURLEncoding.EncodeToString(sig), true
}

func parseApplePrivateKey(rawPEM string) (*ecdsa.PrivateKey, error) {
	normalized := strings.ReplaceAll(strings.TrimSpace(rawPEM), `\n`, "\n")
	if normalized == "" {
		return nil, domain.ErrOAuthProviderNotConfigured
	}
	block, _ := pem.Decode([]byte(normalized))
	if block == nil {
		return nil, domain.ErrOAuthProviderNotConfigured
	}
	if keyAny, err := x509.ParsePKCS8PrivateKey(block.Bytes); err == nil {
		if ecKey, ok := keyAny.(*ecdsa.PrivateKey); ok {
			return ecKey, nil
		}
		return nil, domain.ErrOAuthProviderNotConfigured
	}
	return x509.ParseECPrivateKey(block.Bytes)
}

func (s *Service) StartOAuthAuthorization(
	provider string,
	redirectURI string,
	codeChallenge string,
	state string,
) (OAuthStartResult, error) {
	normalizedProvider := strings.ToLower(strings.TrimSpace(provider))
	clientID, err := s.configuredOAuthClientID(normalizedProvider)
	if err != nil {
		return OAuthStartResult{}, err
	}
	cleanRedirect, err := validateOAuthRedirectURI(redirectURI, s.opts.Env)
	if err != nil {
		return OAuthStartResult{}, err
	}
	cleanChallenge := strings.TrimSpace(codeChallenge)
	if len(cleanChallenge) < minPKCEChallengeLen {
		return OAuthStartResult{}, domain.ErrInvalidInput
	}
	resolvedState := strings.TrimSpace(state)
	if resolvedState == "" {
		resolvedState, err = domain.GenerateRandomHex(16)
		if err != nil {
			return OAuthStartResult{}, err
		}
	}
	s.storeOAuthState(resolvedState, normalizedProvider, cleanRedirect)
	authURL := buildOAuthAuthorizeURL(normalizedProvider, clientID, cleanRedirect, cleanChallenge, resolvedState)
	return OAuthStartResult{
		Provider:     normalizedProvider,
		AuthorizeURL: authURL,
		State:        resolvedState,
	}, nil
}

const oauthStateTTL = 15 * time.Minute

type oauthStateEntry struct {
	Provider    string
	RedirectURI string
	ExpiresAt   time.Time
}

func (s *Service) storeOAuthState(state, provider, redirectURI string) {
	now := s.clock.Now()
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.oauthStates == nil {
		s.oauthStates = make(map[string]oauthStateEntry)
	}
	for k, existing := range s.oauthStates {
		if !now.Before(existing.ExpiresAt) {
			delete(s.oauthStates, k)
		}
	}
	s.oauthStates[state] = oauthStateEntry{
		Provider:    provider,
		RedirectURI: redirectURI,
		ExpiresAt:   now.Add(oauthStateTTL),
	}
}

func (s *Service) consumeOAuthState(state, provider, redirectURI string) error {
	cleanState := strings.TrimSpace(state)
	if cleanState == "" {
		return domain.ErrInvalidInput
	}
	now := s.clock.Now()
	s.mu.Lock()
	defer s.mu.Unlock()
	entry, exists := s.oauthStates[cleanState]
	if !exists {
		return domain.ErrInvalidInput
	}
	delete(s.oauthStates, cleanState)
	if !now.Before(entry.ExpiresAt) {
		return domain.ErrInvalidInput
	}
	if entry.Provider != provider {
		return domain.ErrInvalidInput
	}
	if entry.RedirectURI != redirectURI {
		return domain.ErrInvalidInput
	}
	return nil
}

func buildOAuthAuthorizeURL(provider, clientID, redirectURI, codeChallenge, state string) string {
	params := url.Values{}
	params.Set("client_id", clientID)
	params.Set("redirect_uri", redirectURI)
	params.Set("response_type", "code")
	params.Set("code_challenge", codeChallenge)
	params.Set("code_challenge_method", "S256")
	params.Set("state", state)
	if provider == "apple" {
		params.Set("scope", "name email")
		params.Set("response_mode", "form_post")
		return appleAuthorizeURL + "?" + params.Encode()
	}
	params.Set("scope", "openid email profile")
	params.Set("access_type", "offline")
	params.Set("prompt", "select_account")
	return googleAuthorizeURL + "?" + params.Encode()
}

func validateOAuthRedirectURI(raw string, env config.Environment) (string, error) {
	trimmed := strings.TrimSpace(raw)
	parsed, err := url.Parse(trimmed)
	if err != nil {
		return "", domain.ErrInvalidInput
	}
	scheme := strings.ToLower(parsed.Scheme)
	if scheme == "rosafiesta" {
		return trimmed, nil
	}
	host := strings.ToLower(strings.TrimSpace(parsed.Hostname()))
	if scheme == "https" {
		if isAllowedHTTPSOAuthHost(host) {
			return trimmed, nil
		}
		return "", domain.ErrInvalidInput
	}
	if env != config.EnvProduction {
		if scheme == "http" {
			if isLoopbackOAuthHost(host) {
				return trimmed, nil
			}
		}
	}
	return "", domain.ErrInvalidInput
}

func isAllowedHTTPSOAuthHost(host string) bool {
	switch host {
	case "rosafiesta.do", "app.rosafiesta.do", "admin.rosafiesta.do", "api.rosafiesta.do":
		return true
	default:
		return false
	}
}

func isLoopbackOAuthHost(host string) bool {
	if host == "localhost" {
		return true
	}
	return host == "127.0.0.1"
}

type oauthTokenEndpointResponse struct {
	IDToken string `json:"id_token"`
}

func (s *Service) ExchangeOAuthCode(
	ctx context.Context,
	provider string,
	code string,
	codeVerifier string,
	redirectURI string,
	state string,
) (domain.User, string, error) {
	session, err := s.ExchangeOAuthCodeWithSession(ctx, provider, code, codeVerifier, redirectURI, state)
	if err != nil {
		return domain.User{}, "", err
	}
	return session.User, session.AccessToken, nil
}

func (s *Service) requestOAuthIDToken(
	ctx context.Context,
	provider string,
	clientID string,
	code string,
	codeVerifier string,
	redirectURI string,
) (string, error) {
	form := url.Values{}
	form.Set("grant_type", "authorization_code")
	form.Set("code", code)
	form.Set("client_id", clientID)
	form.Set("redirect_uri", redirectURI)
	form.Set("code_verifier", codeVerifier)
	secret := s.configuredOAuthClientSecret(provider, clientID)
	if secret != "" {
		form.Set("client_secret", secret)
	}
	tokenURL := s.resolveExchangeEndpoint(provider)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, tokenURL, strings.NewReader(form.Encode()))
	if err != nil {
		return "", domain.ErrOAuthInvalidToken
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	resp, err := s.jwksCache.client.Do(req)
	if err != nil {
		return "", domain.ErrOAuthInvalidToken
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return "", domain.ErrOAuthInvalidToken
	}
	body, err := io.ReadAll(io.LimitReader(resp.Body, 512*1024))
	if err != nil {
		return "", domain.ErrOAuthInvalidToken
	}
	var parsed oauthTokenEndpointResponse
	if err := json.Unmarshal(body, &parsed); err != nil {
		return "", domain.ErrOAuthInvalidToken
	}
	if strings.TrimSpace(parsed.IDToken) == "" {
		return "", domain.ErrOAuthInvalidToken
	}
	return parsed.IDToken, nil
}
