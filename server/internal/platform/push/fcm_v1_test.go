package push_test

import (
	"context"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/json"
	"encoding/pem"
	"net/http"
	"net/http/httptest"
	"sync/atomic"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
)

func TestFCMV1SenderExchangesTokenCachesAndSends(t *testing.T) {
	rsaKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatalf("generate rsa key: %v", err)
	}
	pkcs8, err := x509.MarshalPKCS8PrivateKey(rsaKey)
	if err != nil {
		t.Fatalf("marshal rsa pkcs8: %v", err)
	}
	pemText := string(pem.EncodeToMemory(&pem.Block{Type: "PRIVATE KEY", Bytes: pkcs8}))

	var tokenRequests atomic.Int32
	oauthSrv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		tokenRequests.Add(1)
		_ = r.ParseForm()
		if r.FormValue("grant_type") == "" || r.FormValue("assertion") == "" {
			w.WriteHeader(http.StatusBadRequest)
			return
		}
		_ = json.NewEncoder(w).Encode(map[string]any{
			"access_token": "ya29.test-fcm-access-token",
			"expires_in":   3600,
		})
	}))
	defer oauthSrv.Close()

	var capturedAuth string
	var capturedTargetToken string
	fcmSrv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		capturedAuth = r.Header.Get("Authorization")
		var payload struct {
			Message struct {
				Token string `json:"token"`
			} `json:"message"`
		}
		_ = json.NewDecoder(r.Body).Decode(&payload)
		capturedTargetToken = payload.Message.Token
		w.WriteHeader(http.StatusOK)
	}))
	defer fcmSrv.Close()

	saJSON, _ := json.Marshal(map[string]string{
		"project_id":   "rosafiesta-prod",
		"client_email": "fcm-sender@rosafiesta-prod.iam.gserviceaccount.com",
		"private_key":  pemText,
		"token_uri":    oauthSrv.URL,
	})

	sender := push.NewFCMV1Sender(push.FCMV1Config{
		ServiceAccountJSON: string(saJSON),
		EndpointURL:        fcmSrv.URL,
		Clock:              func() time.Time { return time.Unix(1700000000, 0).UTC() },
	})
	if !sender.IsConfigured() {
		t.Fatal("expected FCMV1Sender to be configured")
	}

	msg := push.Message{
		Token:      "fcm-device-reg-token-1",
		Platform:   "android",
		AppVariant: "personal",
		Title:      "Evento confirmado",
		Body:       "Tu montaje quedó agendado.",
		Category:   "eventos",
	}
	if err := sender.SendPush(context.Background(), msg); err != nil {
		t.Fatalf("first SendPush failed: %v", err)
	}
	if err := sender.SendPush(context.Background(), msg); err != nil {
		t.Fatalf("second SendPush failed: %v", err)
	}
	if tokenRequests.Load() != 1 {
		t.Fatalf("expected cached OAuth token (1 exchange), got %d", tokenRequests.Load())
	}
	if capturedAuth != "Bearer ya29.test-fcm-access-token" {
		t.Fatalf("unexpected Authorization header %q", capturedAuth)
	}
	if capturedTargetToken != "fcm-device-reg-token-1" {
		t.Fatalf("unexpected target token %q", capturedTargetToken)
	}
}
