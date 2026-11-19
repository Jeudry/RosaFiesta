package push_test

import (
	"context"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"math/big"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
)

func TestAPNSSenderSignsES256JWTAndRoutesIOS(t *testing.T) {
	privateKey, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		t.Fatalf("generate p256 key: %v", err)
	}
	pkcs8Bytes, err := x509.MarshalPKCS8PrivateKey(privateKey)
	if err != nil {
		t.Fatalf("marshal pkcs8 key: %v", err)
	}
	pemText := string(pem.EncodeToMemory(&pem.Block{Type: "PRIVATE KEY", Bytes: pkcs8Bytes}))

	var capturedAuth string
	var capturedTopic string
	var capturedPath string
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		capturedAuth = r.Header.Get("Authorization")
		capturedTopic = r.Header.Get("apns-topic")
		capturedPath = r.URL.Path
		w.WriteHeader(http.StatusOK)
	}))
	defer server.Close()

	apns := push.NewAPNSSender(push.APNSConfig{
		KeyID:         "KEY123",
		TeamID:        "TEAM456",
		BundleID:      "com.jeudry.rosafiesta.admin",
		PrivateKeyPEM: pemText,
		UseSandbox:    true,
		EndpointURL:   server.URL,
		Clock:         func() time.Time { return time.Unix(1700000000, 0).UTC() },
	})
	multi := push.NewMultiSender(nil, apns)
	if !multi.IsConfigured() {
		t.Fatal("expected multi sender with apns to report configured")
	}

	err = multi.SendPush(context.Background(), push.Message{
		Token:      "device-apns-token-hex",
		Platform:   "ios",
		AppVariant: "admin",
		Title:      "Nueva cotización",
		Body:       "Revisar solicitud de Camila",
		Category:   "quote_created",
	})
	if err != nil {
		t.Fatalf("expected nil send error, got %v", err)
	}
	if capturedPath != "/3/device/device-apns-token-hex" {
		t.Fatalf("unexpected APNs path %q", capturedPath)
	}
	if capturedTopic != "com.jeudry.rosafiesta.admin" {
		t.Fatalf("unexpected apns-topic %q", capturedTopic)
	}
	verifyAPNSES256JWT(t, capturedAuth, &privateKey.PublicKey)
}

func verifyAPNSES256JWT(t *testing.T, authHeader string, pubKey *ecdsa.PublicKey) {
	t.Helper()
	rawJWT := strings.TrimPrefix(authHeader, "bearer ")
	parts := strings.Split(rawJWT, ".")
	if len(parts) != 3 {
		t.Fatalf("expected 3 JWT segments, got %q", authHeader)
	}
	headerRaw, _ := base64.RawURLEncoding.DecodeString(parts[0])
	var header map[string]string
	_ = json.Unmarshal(headerRaw, &header)
	if header["alg"] != "ES256" {
		t.Fatalf("expected ES256 header, got %+v", header)
	}
	if header["kid"] != "KEY123" {
		t.Fatalf("expected kid KEY123, got %+v", header)
	}
	sigRaw, err := base64.RawURLEncoding.DecodeString(parts[2])
	if err != nil {
		t.Fatalf("invalid raw ES256 signature encoding: %v", err)
	}
	if len(sigRaw) != 64 {
		t.Fatalf("invalid raw ES256 signature len=%d", len(sigRaw))
	}
	digest := sha256.Sum256([]byte(parts[0] + "." + parts[1]))
	rVal := new(big.Int).SetBytes(sigRaw[:32])
	sVal := new(big.Int).SetBytes(sigRaw[32:])
	if !ecdsa.Verify(pubKey, digest[:], rVal, sVal) {
		t.Fatal("failed to verify APNs JWT ES256 signature")
	}
}
