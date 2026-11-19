package httpapi_test

import (
	"bytes"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	identityhttp "github.com/Jeudry/rosafiesta/server/internal/identity/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func buildIdentityTestRouter(t *testing.T) http.Handler {
	t.Helper()
	logger := slog.New(slog.DiscardHandler)
	fakeClk := &clock.Fixed{At: time.Date(2026, 11, 15, 11, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(fakeClk, nil, identityapp.Options{
		Env:                   config.EnvDevelopment,
		JWTSecret:             "0123456789abcdef0123456789abcdef0123456789abcdef",
		WebPushVAPIDPublicKey: "BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5Nksh8",
	})
	router, _ := platformhttp.NewWithOptions(
		logger,
		platformhttp.RouterOptions{},
		identityhttp.Module{Service: svc},
	)
	return router
}

func TestIdentityHTTP_RefreshFlow(t *testing.T) {
	router := buildIdentityTestRouter(t)

	loginReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/auth/login",
		bytes.NewBufferString(`{"email":"camila@rosafiesta.do","password":"secret123"}`),
	)
	loginReq.Header.Set("Content-Type", "application/json")
	loginRec := httptest.NewRecorder()
	router.ServeHTTP(loginRec, loginReq)
	if loginRec.Code != http.StatusOK {
		t.Fatalf("expected 200 login, got %d: %s", loginRec.Code, loginRec.Body.String())
	}
	var loginPayload struct {
		AccessToken  string `json:"accessToken"`
		RefreshToken string `json:"refreshToken"`
	}
	if err := json.Unmarshal(loginRec.Body.Bytes(), &loginPayload); err != nil {
		t.Fatalf("unmarshal login payload: %v", err)
	}
	if loginPayload.RefreshToken == "" {
		t.Fatal("expected non-empty refreshToken")
	}

	refreshBody, _ := json.Marshal(map[string]string{"refreshToken": loginPayload.RefreshToken})
	refreshReq := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/auth/refresh", bytes.NewReader(refreshBody))
	refreshReq.Header.Set("Content-Type", "application/json")
	refreshRec := httptest.NewRecorder()
	router.ServeHTTP(refreshRec, refreshReq)
	if refreshRec.Code != http.StatusOK {
		t.Fatalf("expected 200 refresh, got %d: %s", refreshRec.Code, refreshRec.Body.String())
	}
	var refreshedPayload struct {
		RefreshToken string `json:"refreshToken"`
	}
	if err := json.Unmarshal(refreshRec.Body.Bytes(), &refreshedPayload); err != nil {
		t.Fatalf("unmarshal refresh payload: %v", err)
	}
	if refreshedPayload.RefreshToken == loginPayload.RefreshToken {
		t.Fatal("expected rotated refreshToken to change")
	}

	reuseReq := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/auth/refresh", bytes.NewReader(refreshBody))
	reuseReq.Header.Set("Content-Type", "application/json")
	reuseRec := httptest.NewRecorder()
	router.ServeHTTP(reuseRec, reuseReq)
	if reuseRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 on refresh reuse, got %d", reuseRec.Code)
	}
}

func TestIdentityHTTP_CallbackBridges(t *testing.T) {
	router := buildIdentityTestRouter(t)

	cbReq := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/callback?code=authcode99&state=st42", nil)
	cbRec := httptest.NewRecorder()
	router.ServeHTTP(cbRec, cbReq)
	if cbRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /callback, got %d", cbRec.Code)
	}
	if !strings.Contains(cbRec.Body.String(), `"type":"rosafiesta-oauth-callback"`) {
		t.Fatalf("expected serialized oauth callback payload in HTML, got %s", cbRec.Body.String())
	}
	if !strings.Contains(cbRec.Body.String(), `"code":"authcode99"`) {
		t.Fatalf("expected code in /callback HTML payload, got %s", cbRec.Body.String())
	}
	if !strings.Contains(cbRec.Header().Get("Content-Security-Policy"), "nonce-") {
		t.Fatalf("expected script nonce in /callback CSP, got %q", cbRec.Header().Get("Content-Security-Policy"))
	}

	payReq := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/pagos?session_id=cs_test_99&event_id=evt_1", nil)
	payRec := httptest.NewRecorder()
	router.ServeHTTP(payRec, payReq)
	if payRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /pagos, got %d", payRec.Code)
	}
	if !strings.Contains(payRec.Body.String(), `"sessionId":"cs_test_99"`) {
		t.Fatalf("expected payment sessionId payload in /pagos HTML, got %s", payRec.Body.String())
	}
}
