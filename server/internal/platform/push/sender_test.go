package push_test

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
)

func TestHTTPSender_UnconfiguredAndConfiguredDelivery(t *testing.T) {
	unconfigured := push.NewHTTPSender(push.Config{})
	if unconfigured.IsConfigured() {
		t.Fatal("expected IsConfigured=false without ServerKey")
	}
	err := unconfigured.SendPush(t.Context(), push.Message{Token: "dev-tok", Title: "Hola", Body: "Mundo"})
	if !errors.Is(err, push.ErrNotConfigured) {
		t.Fatalf("expected ErrNotConfigured, got %v", err)
	}

	var capturedAuth string
	var capturedTo string
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		capturedAuth = r.Header.Get("Authorization")
		var body struct {
			To string `json:"to"`
		}
		_ = json.NewDecoder(r.Body).Decode(&body)
		capturedTo = body.To
		w.WriteHeader(http.StatusOK)
	}))
	defer srv.Close()

	configured := push.NewHTTPSender(push.Config{
		EndpointURL: srv.URL,
		ServerKey:   "fcm-test-key",
	})
	if !configured.IsConfigured() {
		t.Fatal("expected IsConfigured=true with ServerKey")
	}
	if sendErr := configured.SendPush(t.Context(), push.Message{
		Token:      "token-device-1",
		Platform:   "android",
		AppVariant: "personal",
		Title:      "Pago verificado",
		Body:       "Tu abono fue confirmado.",
		Category:   "pagos",
	}); sendErr != nil {
		t.Fatalf("SendPush failed: %v", sendErr)
	}
	if capturedAuth != "key=fcm-test-key" {
		t.Fatalf("expected key=fcm-test-key header, got %q", capturedAuth)
	}
	if capturedTo != "token-device-1" {
		t.Fatalf("expected token-device-1 payload, got %q", capturedTo)
	}
}
