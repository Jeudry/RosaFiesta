package app_test

import (
	"crypto"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"math/big"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/google/uuid"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

func generateTestRSAKeyPair(t *testing.T) (*rsa.PrivateKey, string) {
	t.Helper()
	privKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatalf("generate rsa key: %v", err)
	}
	nEncoded := base64.RawURLEncoding.EncodeToString(privKey.N.Bytes())
	eBytes := big.NewInt(int64(privKey.E)).Bytes()
	eEncoded := base64.RawURLEncoding.EncodeToString(eBytes)
	jwksJSON, err := json.Marshal(map[string]any{
		"keys": []map[string]string{
			{
				"kty": "RSA",
				"kid": "test-kid-1",
				"alg": "RS256",
				"n":   nEncoded,
				"e":   eEncoded,
			},
		},
	})
	if err != nil {
		t.Fatalf("marshal jwks: %v", err)
	}
	return privKey, string(jwksJSON)
}

func signRS256Token(t *testing.T, privKey *rsa.PrivateKey, header map[string]string, claims map[string]any) string {
	t.Helper()
	headerBytes, err := json.Marshal(header)
	if err != nil {
		t.Fatalf("marshal header: %v", err)
	}
	claimsBytes, err := json.Marshal(claims)
	if err != nil {
		t.Fatalf("marshal claims: %v", err)
	}
	signingInput := base64.RawURLEncoding.EncodeToString(headerBytes) + "." + base64.RawURLEncoding.EncodeToString(claimsBytes)
	digest := sha256.Sum256([]byte(signingInput))
	sig, err := rsa.SignPKCS1v15(rand.Reader, privKey, crypto.SHA256, digest[:])
	if err != nil {
		t.Fatalf("sign rs256: %v", err)
	}
	return signingInput + "." + base64.RawURLEncoding.EncodeToString(sig)
}

func TestService_OAuthRS256JWKSAndPKCEExchange(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	now := time.Date(2026, time.November, 15, 12, 0, 0, 0, time.UTC)
	clk := &clock.Fixed{At: now}
	privKey, jwksBody := generateTestRSAKeyPair(t)

	validIDToken := signRS256Token(t, privKey, map[string]string{
		"alg": "RS256",
		"kid": "test-kid-1",
	}, map[string]any{
		"iss":            "https://accounts.google.com",
		"aud":            "google-client-test",
		"email":          "sofia.oauth@rosafiesta.do",
		"email_verified": true,
		"name":           "Sofia OAuth",
		"exp":            now.Add(time.Hour).Unix(),
	})

	jwksServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, _ *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(jwksBody))
	}))
	defer jwksServer.Close()

	tokenServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		if err := req.ParseForm(); err != nil {
			http.Error(writer, "bad form", http.StatusBadRequest)
			return
		}
		if req.Form.Get("code") != "auth-code-123" {
			http.Error(writer, "invalid code", http.StatusUnauthorized)
			return
		}
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(`{"id_token":"` + validIDToken + `"}`))
	}))
	defer tokenServer.Close()

	svc := identityapp.NewServiceWithOptions(clk, stubChecker{}, identityapp.Options{
		Env:            config.EnvDevelopment,
		JWTSecret:      "0123456789abcdef0123456789abcdef0123456789abcdef",
		GoogleClientID: "google-client-test",
		GoogleJWKSURL:  jwksServer.URL,
		GoogleTokenURL: tokenServer.URL,
	})

	verifier := strings.Repeat("v", 48)
	challengeSum := sha256.Sum256([]byte(verifier))
	challenge := base64.RawURLEncoding.EncodeToString(challengeSum[:])
	startRes, err := svc.StartOAuthAuthorization("google", "http://127.0.0.1:43210/callback", challenge, "state-xyz")
	if err != nil {
		t.Fatalf("StartOAuthAuthorization failed: %v", err)
	}
	if !strings.Contains(startRes.AuthorizeURL, "code_challenge_method=S256") {
		t.Fatalf("expected PKCE S256 in authorize URL, got %s", startRes.AuthorizeURL)
	}

	user, jwtToken, err := svc.ExchangeOAuthCode(ctx, "google", "auth-code-123", verifier, "http://127.0.0.1:43210/callback")
	if err != nil {
		t.Fatalf("ExchangeOAuthCode failed: %v", err)
	}
	if user.Email != "sofia.oauth@rosafiesta.do" {
		t.Fatalf("unexpected oauth email: %q", user.Email)
	}
	if jwtToken == "" {
		t.Fatal("expected JWT session token after OAuth exchange")
	}

	tamperedToken := validIDToken[:len(validIDToken)-4] + "aaaa"
	if _, _, err := svc.OAuthLogin(ctx, "google", tamperedToken); !errors.Is(err, domain.ErrOAuthInvalidToken) {
		t.Fatalf("expected ErrOAuthInvalidToken on tampered signature, got %v", err)
	}
}

func TestService_PushTokenLifecycleAndPreferences(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 13, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{}, identityapp.Options{
		Env:       config.EnvDevelopment,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})

	camilaID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	mariaID := uuid.MustParse("22222222-2222-2222-2222-222222222222")

	rec, err := svc.RegisterPushToken(ctx, camilaID, "android", "fcm-camila-token-1", "personal")
	if err != nil {
		t.Fatalf("RegisterPushToken client failed: %v", err)
	}
	if rec.Platform != "android" {
		t.Fatalf("expected android platform, got %q", rec.Platform)
	}
	if _, err := svc.RegisterPushToken(ctx, mariaID, "ios", "apns-maria-token-1", "admin"); err != nil {
		t.Fatalf("RegisterPushToken admin failed: %v", err)
	}

	if got := len(svc.ActivePushTokensForUser(ctx, camilaID)); got != 1 {
		t.Fatalf("expected 1 active camila token, got %d", got)
	}
	if got := len(svc.ActiveAdminPushTokens(ctx)); got != 1 {
		t.Fatalf("expected 1 active admin token, got %d", got)
	}

	pushOff := false
	if _, err := svc.UpdatePreferences(ctx, camilaID, identityapp.UpdatePreferencesCommand{
		PushNotificationsEnabled: &pushOff,
	}); err != nil {
		t.Fatalf("UpdatePreferences failed: %v", err)
	}
	if got := len(svc.ActivePushTokensForUser(ctx, camilaID)); got != 0 {
		t.Fatalf("expected camila push tokens disabled when pref turned off, got %d", got)
	}

	if err := svc.UnregisterPushToken(ctx, mariaID, "apns-maria-token-1"); err != nil {
		t.Fatalf("UnregisterPushToken failed: %v", err)
	}
	if got := len(svc.ActiveAdminPushTokens(ctx)); got != 0 {
		t.Fatalf("expected 0 admin tokens after unregister, got %d", got)
	}
}

func TestService_AppleOAuthFormPostAndES256ClientSecret(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	now := time.Date(2026, time.November, 15, 14, 0, 0, 0, time.UTC)
	clk := &clock.Fixed{At: now}

	applePrivKey, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		t.Fatalf("generate ecdsa key: %v", err)
	}
	pkcs8Bytes, err := x509.MarshalPKCS8PrivateKey(applePrivKey)
	if err != nil {
		t.Fatalf("marshal pkcs8: %v", err)
	}
	applePEM := string(pem.EncodeToMemory(&pem.Block{Type: "PRIVATE KEY", Bytes: pkcs8Bytes}))

	rsaKey, jwksBody := generateTestRSAKeyPair(t)
	appleIDToken := signRS256Token(t, rsaKey, map[string]string{
		"alg": "RS256",
		"kid": "test-kid-1",
	}, map[string]any{
		"iss":            "https://appleid.apple.com",
		"aud":            "do.rosafiesta.signin",
		"email":          "ana.apple@rosafiesta.do",
		"email_verified": "true",
		"name":           "Ana Apple",
		"exp":            now.Add(time.Hour).Unix(),
	})

	jwksServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, _ *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(jwksBody))
	}))
	defer jwksServer.Close()

	var capturedSecret string
	tokenServer := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		if parseErr := req.ParseForm(); parseErr != nil {
			http.Error(writer, "bad form", http.StatusBadRequest)
			return
		}
		capturedSecret = req.Form.Get("client_secret")
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(`{"id_token":"` + appleIDToken + `"}`))
	}))
	defer tokenServer.Close()

	svc := identityapp.NewServiceWithOptions(clk, stubChecker{}, identityapp.Options{
		Env:             config.EnvDevelopment,
		JWTSecret:       "0123456789abcdef0123456789abcdef0123456789abcdef",
		AppleClientID:   "do.rosafiesta.signin",
		AppleTeamID:     "TEAM123456",
		AppleKeyID:      "KEY1234567",
		ApplePrivateKey: applePEM,
		AppleJWKSURL:    jwksServer.URL,
		AppleTokenURL:   tokenServer.URL,
	})

	challenge := base64.RawURLEncoding.EncodeToString(make([]byte, 32))
	startRes, startErr := svc.StartOAuthAuthorization("apple", "https://api.rosafiesta.do/callback", challenge, "st-apple")
	if startErr != nil {
		t.Fatalf("StartOAuthAuthorization apple failed: %v", startErr)
	}
	if !strings.Contains(startRes.AuthorizeURL, "response_mode=form_post") {
		t.Fatalf("expected Apple response_mode=form_post, got %s", startRes.AuthorizeURL)
	}

	user, _, exchangeErr := svc.ExchangeOAuthCode(ctx, "apple", "apple-code-1", strings.Repeat("v", 48), "https://api.rosafiesta.do/callback")
	if exchangeErr != nil {
		t.Fatalf("ExchangeOAuthCode apple failed: %v", exchangeErr)
	}
	if user.Email != "ana.apple@rosafiesta.do" {
		t.Fatalf("expected Apple user email, got %q", user.Email)
	}
	parts := strings.Split(capturedSecret, ".")
	if len(parts) != 3 {
		t.Fatalf("expected signed ES256 client_secret JWT, got %q", capturedSecret)
	}
	sigBytes, decErr := base64.RawURLEncoding.DecodeString(parts[2])
	if decErr != nil {
		t.Fatalf("decode ES256 sig failed: %v", decErr)
	}
	if len(sigBytes) != 64 {
		t.Fatalf("expected 64-byte IEEE P1363 sig, got %d", len(sigBytes))
	}
	digest := sha256.Sum256([]byte(parts[0] + "." + parts[1]))
	rBig := new(big.Int).SetBytes(sigBytes[:32])
	sBig := new(big.Int).SetBytes(sigBytes[32:])
	if !ecdsa.Verify(&applePrivKey.PublicKey, digest[:], rBig, sBig) {
		t.Fatal("expected valid ES256 signature on Apple client_secret JWT")
	}
}

func TestService_CleanupExpiredRefreshTokens(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 14, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{}, identityapp.Options{
		Env:       config.EnvDevelopment,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})
	sess, err := svc.LoginWithSession(ctx, "camila", "secret123")
	if err != nil {
		t.Fatalf("LoginWithSession failed: %v", err)
	}
	clk.At = clk.At.Add(31 * 24 * time.Hour)
	removed, cleanErr := svc.CleanupExpiredRefreshTokens(ctx)
	if cleanErr != nil {
		t.Fatalf("CleanupExpiredRefreshTokens failed: %v", cleanErr)
	}
	if removed != 1 {
		t.Fatalf("expected 1 expired refresh token removed, got %d", removed)
	}
	if _, refreshErr := svc.RefreshSession(ctx, sess.RefreshToken); refreshErr == nil {
		t.Fatal("expected error refreshing cleaned-up expired token")
	}
}
