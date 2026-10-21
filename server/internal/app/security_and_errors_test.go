package app_test

import (
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestSecurityHeadersAndStructuredRedactedErrors(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{})...)

	loginReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/auth/login",
		strings.NewReader(`{"email":"camila@rosafiesta.do","password":"wrong-password"}`),
	)
	loginReq.Header.Set("Content-Type", "application/json")
	loginRec := httptest.NewRecorder()
	handler.ServeHTTP(loginRec, loginReq)

	if loginRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 on wrong password, got %d: %s", loginRec.Code, loginRec.Body.String())
	}
	if loginRec.Header().Get("X-Content-Type-Options") != "nosniff" {
		t.Fatalf("missing X-Content-Type-Options header: %v", loginRec.Header())
	}
	if loginRec.Header().Get("X-Frame-Options") != "DENY" {
		t.Fatalf("missing X-Frame-Options header: %v", loginRec.Header())
	}

	var apiErr httpapi.APIError
	if err := json.Unmarshal(loginRec.Body.Bytes(), &apiErr); err != nil {
		t.Fatalf("decode structured APIError: %v", err)
	}
	if apiErr.Code != "AUTH_INVALID_CREDENTIALS" || apiErr.Endpoint != "POST /v1/auth/login" || apiErr.Policy != "VerifyCredentialsBcrypt" {
		t.Fatalf("unexpected structured error payload: %+v", apiErr)
	}
	if len(apiErr.Reasons) == 0 || !strings.Contains(apiErr.Reasons[0], "c***a@rosafiesta.do") {
		t.Fatalf("expected redacted email in error reasons, got %+v", apiErr.Reasons)
	}
	if strings.Contains(loginRec.Body.String(), "camila@rosafiesta.do") {
		t.Fatalf("raw email leaked in HTTP response: %s", loginRec.Body.String())
	}
}

func TestRBACPermissionEnforcementOnAdminRoutes(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{})...)

	yuliReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/admin/staff", nil)
	yuliReq.Header.Set("Authorization", "Bearer rf-token-yuli")
	yuliRec := httptest.NewRecorder()
	handler.ServeHTTP(yuliRec, yuliReq)
	if yuliRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 for Yuli on /v1/admin/staff (lacks 'ajustes'), got %d: %s", yuliRec.Code, yuliRec.Body.String())
	}

	var forbiddenErr httpapi.APIError
	if err := json.Unmarshal(yuliRec.Body.Bytes(), &forbiddenErr); err != nil {
		t.Fatalf("decode forbidden APIError: %v", err)
	}
	if forbiddenErr.Code != "FORBIDDEN_PERMISSION" || forbiddenErr.Policy != "RequirePermission:ajustes" {
		t.Fatalf("unexpected RBAC error payload: %+v", forbiddenErr)
	}

	mariaReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/admin/staff", nil)
	mariaReq.Header.Set("Authorization", "Bearer rf-token-maria")
	mariaRec := httptest.NewRecorder()
	handler.ServeHTTP(mariaRec, mariaReq)
	if mariaRec.Code != http.StatusOK {
		t.Fatalf("expected 200 for Admin María on /v1/admin/staff, got %d: %s", mariaRec.Code, mariaRec.Body.String())
	}
}

func TestRateLimiterBlocksExcessTrafficWithStructured429(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	limiter := httpapi.NewRateLimiter(2, time.Minute)
	protected := limiter.Middleware(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))

	for i := range 2 {
		req := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/auth/login", nil)
		req.RemoteAddr = "203.0.113.10:44321"
		rec := httptest.NewRecorder()
		protected.ServeHTTP(rec, req)
		if rec.Code != http.StatusOK {
			t.Fatalf("request %d should pass, got %d", i+1, rec.Code)
		}
	}

	blockedReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/auth/login", nil)
	blockedReq.RemoteAddr = "203.0.113.10:44321"
	blockedRec := httptest.NewRecorder()
	protected.ServeHTTP(blockedRec, blockedReq)
	if blockedRec.Code != http.StatusTooManyRequests {
		t.Fatalf("expected 429 on 3rd request, got %d", blockedRec.Code)
	}
	var rateErr httpapi.APIError
	if err := json.Unmarshal(blockedRec.Body.Bytes(), &rateErr); err != nil {
		t.Fatalf("decode 429 APIError: %v", err)
	}
	if rateErr.Code != "RATE_LIMIT_EXCEEDED" || rateErr.Policy != "RateLimitAuthLogin" {
		t.Fatalf("unexpected 429 APIError payload: %+v", rateErr)
	}
}
