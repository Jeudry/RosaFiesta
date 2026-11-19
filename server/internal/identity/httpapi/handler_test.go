package httpapi_test

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/danielgtaylor/huma/v2"
	"github.com/danielgtaylor/huma/v2/adapters/humachi"
	"github.com/go-chi/chi/v5"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	identityhttp "github.com/Jeudry/rosafiesta/server/internal/identity/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

func TestIdentityHTTP_StrictSMTP503_OAuth503_And_StaffInviteLogin(t *testing.T) {
	t.Parallel()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, nil, identityapp.Options{
		Env:                   config.EnvDevelopment,
		JWTSecret:             "0123456789abcdef0123456789abcdef0123456789abcdef",
		RequireConfiguredSMTP: true,
	})
	mux := chi.NewMux()
	api := humachi.New(mux, huma.DefaultConfig("Identity Test", "1.0.0"))
	identityhttp.Module{Service: svc}.Register(api)

	otpReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/auth/otp/request",
		bytes.NewBufferString(`{"email":"camila@rosafiesta.do"}`),
	)
	otpReq.Header.Set("Content-Type", "application/json")
	otpRec := httptest.NewRecorder()
	mux.ServeHTTP(otpRec, otpReq)
	if otpRec.Code != http.StatusServiceUnavailable {
		t.Fatalf("expected 503 on unconfigured strict SMTP, got %d: %s", otpRec.Code, otpRec.Body.String())
	}
	if !strings.Contains(otpRec.Body.String(), "SMTP_PROVIDER_NOT_CONFIGURED") {
		t.Fatalf("expected SMTP_PROVIDER_NOT_CONFIGURED, got %s", otpRec.Body.String())
	}

	oauthReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/auth/oauth",
		bytes.NewBufferString(`{"provider":"google","idToken":"abc.def.ghi"}`),
	)
	oauthReq.Header.Set("Content-Type", "application/json")
	oauthRec := httptest.NewRecorder()
	mux.ServeHTTP(oauthRec, oauthReq)
	if oauthRec.Code != http.StatusServiceUnavailable {
		t.Fatalf("expected 503 on unconfigured OAuth provider, got %d: %s", oauthRec.Code, oauthRec.Body.String())
	}
	if !strings.Contains(oauthRec.Body.String(), "OAUTH_PROVIDER_NOT_CONFIGURED") {
		t.Fatalf("expected OAUTH_PROVIDER_NOT_CONFIGURED, got %s", oauthRec.Body.String())
	}

	inviteReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/admin/staff",
		bytes.NewBufferString(`{"fullName":"Carlos Finanzas","email":"carlos.fin@rosafiesta.do","permissions":["finanzas"],"initialPassword":"MiClaveSegura2026"}`),
	)
	inviteReq.Header.Set("Authorization", "Bearer rf-token-maria")
	inviteReq.Header.Set("Content-Type", "application/json")
	inviteRec := httptest.NewRecorder()
	mux.ServeHTTP(inviteRec, inviteReq)
	if inviteRec.Code != http.StatusOK {
		t.Fatalf("expected 200 inviting staff, got %d: %s", inviteRec.Code, inviteRec.Body.String())
	}

	loginReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/auth/login",
		bytes.NewBufferString(`{"email":"carlos.fin@rosafiesta.do","password":"MiClaveSegura2026"}`),
	)
	loginReq.Header.Set("Content-Type", "application/json")
	loginRec := httptest.NewRecorder()
	mux.ServeHTTP(loginRec, loginReq)
	if loginRec.Code != http.StatusOK {
		t.Fatalf("expected invited staff login 200, got %d: %s", loginRec.Code, loginRec.Body.String())
	}

	pushReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/devices/push-token",
		bytes.NewBufferString(`{"platform":"web","token":"web-sub-123","appVariant":"personal"}`),
	)
	pushReq.Header.Set("Authorization", "Bearer rf-token-camila")
	pushReq.Header.Set("Content-Type", "application/json")
	pushRec := httptest.NewRecorder()
	mux.ServeHTTP(pushRec, pushReq)
	if pushRec.Code != http.StatusOK {
		t.Fatalf("expected 200 registering push token, got %d: %s", pushRec.Code, pushRec.Body.String())
	}

	delPushReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodDelete,
		"/v1/devices/push-token?token=web-sub-123",
		nil,
	)
	delPushReq.Header.Set("Authorization", "Bearer rf-token-camila")
	delPushRec := httptest.NewRecorder()
	mux.ServeHTTP(delPushRec, delPushReq)
	if delPushRec.Code != http.StatusOK {
		t.Fatalf("expected 200 deleting push token, got %d: %s", delPushRec.Code, delPushRec.Body.String())
	}
}

func TestIdentityHTTP_OAuthStartEndpoint(t *testing.T) {
	t.Parallel()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, nil, identityapp.Options{
		Env:            config.EnvDevelopment,
		JWTSecret:      "0123456789abcdef0123456789abcdef0123456789abcdef",
		GoogleClientID: "google-client-http",
	})
	mux := chi.NewMux()
	api := humachi.New(mux, huma.DefaultConfig("Identity OAuth Test", "1.0.0"))
	identityhttp.Module{Service: svc}.Register(api)

	challenge := strings.Repeat("a", 43)
	startReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodGet,
		"/v1/auth/oauth/google/start?redirectUri=http%3A%2F%2F127.0.0.1%3A9999%2Fcb&codeChallenge="+challenge+"&state=st1",
		nil,
	)
	startRec := httptest.NewRecorder()
	mux.ServeHTTP(startRec, startReq)
	if startRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on oauth start, got %d: %s", startRec.Code, startRec.Body.String())
	}
	if !strings.Contains(startRec.Body.String(), "accounts.google.com") {
		t.Fatalf("expected Google authorize URL, got %s", startRec.Body.String())
	}
}

func TestCallbacksBridge_HandlesAppleFormPostCallback(t *testing.T) {
	t.Parallel()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, nil, identityapp.Options{
		Env:       config.EnvDevelopment,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})
	mux := chi.NewMux()
	identityhttp.Module{
		Service:           svc,
		AllowedWebOrigins: []string{"https://app.rosafiesta.do"},
	}.RegisterRoutes(mux)

	postReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/callback",
		strings.NewReader("code=apple-code-99&state=apple-state-77"),
	)
	postReq.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	postRec := httptest.NewRecorder()
	mux.ServeHTTP(postRec, postReq)
	if postRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on POST /callback, got %d: %s", postRec.Code, postRec.Body.String())
	}
	body := postRec.Body.String()
	if !strings.Contains(body, "apple-code-99") {
		t.Fatalf("expected code in rendered POST callback HTML, got %s", body)
	}
	if !strings.Contains(body, "apple-state-77") {
		t.Fatalf("expected state in rendered POST callback HTML, got %s", body)
	}
	if !strings.Contains(body, "https://app.rosafiesta.do") {
		t.Fatalf("expected configured web origin in callback HTML, got %s", body)
	}

	payReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodGet,
		"/pagos?session_id=cs_live_123&event_id=evt-1",
		nil,
	)
	payRec := httptest.NewRecorder()
	mux.ServeHTTP(payRec, payReq)
	if payRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on GET /pagos, got %d: %s", payRec.Code, payRec.Body.String())
	}
	if !strings.Contains(payRec.Body.String(), "https://app.rosafiesta.do") {
		t.Fatalf("expected configured web origin in payment callback HTML, got %s", payRec.Body.String())
	}
}
