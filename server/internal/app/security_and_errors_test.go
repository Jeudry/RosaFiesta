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
	if apiErr.Code != "AUTH_INVALID_CREDENTIALS" {
		t.Fatalf("unexpected error code: %+v", apiErr)
	}
	if apiErr.Endpoint != "POST /v1/auth/login" {
		t.Fatalf("unexpected error endpoint: %+v", apiErr)
	}
	if apiErr.Policy != "VerifyCredentialsBcrypt" {
		t.Fatalf("unexpected error policy: %+v", apiErr)
	}
	if len(apiErr.Reasons) == 0 {
		t.Fatalf("expected error reasons, got %+v", apiErr.Reasons)
	}
	if !strings.Contains(apiErr.Reasons[0], "c***a@rosafiesta.do") {
		t.Fatalf("expected redacted email in error reasons, got %+v", apiErr.Reasons)
	}
	if strings.Contains(loginRec.Body.String(), "camila@rosafiesta.do") {
		t.Fatalf("raw email leaked in HTTP response: %s", loginRec.Body.String())
	}
}

func TestAnonymousAdminRoutesReturn401AndRBACEnforcement(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{})...)

	for _, adminPath := range []string{"/v1/admin/staff", "/v1/admin/clients", "/v1/payments/summary"} {
		anonReq := httptest.NewRequestWithContext(ctx, http.MethodGet, adminPath, nil)
		anonRec := httptest.NewRecorder()
		handler.ServeHTTP(anonRec, anonReq)
		if anonRec.Code != http.StatusUnauthorized {
			t.Fatalf("expected 401 for anonymous request on %s, got %d: %s", adminPath, anonRec.Code, anonRec.Body.String())
		}
		var missingErr httpapi.APIError
		if err := json.Unmarshal(anonRec.Body.Bytes(), &missingErr); err != nil {
			t.Fatalf("decode 401 APIError on %s: %v", adminPath, err)
		}
		if missingErr.Code != "AUTH_MISSING_TOKEN" {
			t.Fatalf("expected AUTH_MISSING_TOKEN on %s, got %+v", adminPath, missingErr)
		}
	}

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
	if forbiddenErr.Code != "FORBIDDEN_PERMISSION" {
		t.Fatalf("unexpected RBAC error code: %+v", forbiddenErr)
	}
	if forbiddenErr.Policy != "RequirePermission:ajustes" {
		t.Fatalf("unexpected RBAC error policy: %+v", forbiddenErr)
	}

	mariaReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/admin/staff", nil)
	mariaReq.Header.Set("Authorization", "Bearer rf-token-maria")
	mariaRec := httptest.NewRecorder()
	handler.ServeHTTP(mariaRec, mariaReq)
	if mariaRec.Code != http.StatusOK {
		t.Fatalf("expected 200 for Admin Maria on /v1/admin/staff, got %d: %s", mariaRec.Code, mariaRec.Body.String())
	}
}

func TestOTPPasswordResetEndToEndFlow(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{})...)

	otpReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/auth/otp/request",
		strings.NewReader(`{"email":"camila@rosafiesta.do"}`),
	)
	otpReq.Header.Set("Content-Type", "application/json")
	otpRec := httptest.NewRecorder()
	handler.ServeHTTP(otpRec, otpReq)
	if otpRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/auth/otp/request, got %d: %s", otpRec.Code, otpRec.Body.String())
	}
	var otpOut struct {
		Sent    bool   `json:"sent"`
		DevCode string `json:"devCode"`
	}
	if err := json.Unmarshal(otpRec.Body.Bytes(), &otpOut); err != nil {
		t.Fatalf("decode otp request output: %v", err)
	}
	if len(otpOut.DevCode) != 6 {
		t.Fatalf("expected 6-digit devCode in dev environment, got %q", otpOut.DevCode)
	}

	verifyPayload := `{"email":"camila@rosafiesta.do","code":"` + otpOut.DevCode + `"}`
	verifyReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/auth/otp/verify", strings.NewReader(verifyPayload))
	verifyReq.Header.Set("Content-Type", "application/json")
	verifyRec := httptest.NewRecorder()
	handler.ServeHTTP(verifyRec, verifyReq)
	if verifyRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/auth/otp/verify, got %d: %s", verifyRec.Code, verifyRec.Body.String())
	}
	var verifyOut struct {
		Verified   bool   `json:"verified"`
		ResetToken string `json:"resetToken"`
	}
	if err := json.Unmarshal(verifyRec.Body.Bytes(), &verifyOut); err != nil {
		t.Fatalf("decode otp verify output: %v", err)
	}
	if !verifyOut.Verified {
		t.Fatal("expected verified=true")
	}

	resetPayload := `{"email":"camila@rosafiesta.do","resetToken":"` + verifyOut.ResetToken + `","newPassword":"NewSecurePass2026!"}`
	resetReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/auth/reset-password", strings.NewReader(resetPayload))
	resetReq.Header.Set("Content-Type", "application/json")
	resetRec := httptest.NewRecorder()
	handler.ServeHTTP(resetRec, resetReq)
	if resetRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/auth/reset-password, got %d: %s", resetRec.Code, resetRec.Body.String())
	}

	loginReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/auth/login",
		strings.NewReader(`{"email":"camila@rosafiesta.do","password":"NewSecurePass2026!"}`),
	)
	loginReq.Header.Set("Content-Type", "application/json")
	loginRec := httptest.NewRecorder()
	handler.ServeHTTP(loginRec, loginReq)
	if loginRec.Code != http.StatusOK {
		t.Fatalf("expected 200 logging in with reset password, got %d: %s", loginRec.Code, loginRec.Body.String())
	}
}

func TestClientEventIsolationEnforced(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{})...)

	camilaListReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/events", nil)
	camilaListReq.Header.Set("Authorization", "Bearer rf-token-camila")
	camilaListRec := httptest.NewRecorder()
	handler.ServeHTTP(camilaListRec, camilaListReq)
	if camilaListRec.Code != http.StatusOK {
		t.Fatalf("expected 200 for Camila on /v1/events, got %d: %s", camilaListRec.Code, camilaListRec.Body.String())
	}
	var listOut struct {
		Events []struct {
			ID string `json:"id"`
		} `json:"events"`
	}
	if err := json.Unmarshal(camilaListRec.Body.Bytes(), &listOut); err != nil {
		t.Fatalf("decode Camila events: %v", err)
	}
	if len(listOut.Events) != 2 {
		t.Fatalf("expected Camila to see only her 2 events, got %d", len(listOut.Events))
	}

	otherEvtReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/events/evt-grad-promo", nil)
	otherEvtReq.Header.Set("Authorization", "Bearer rf-token-camila")
	otherEvtRec := httptest.NewRecorder()
	handler.ServeHTTP(otherEvtRec, otherEvtReq)
	if otherEvtRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 FORBIDDEN_EVENT_ACCESS for Camila on another client's event, got %d: %s", otherEvtRec.Code, otherEvtRec.Body.String())
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
	if rateErr.Code != "RATE_LIMIT_EXCEEDED" {
		t.Fatalf("unexpected 429 APIError code: %+v", rateErr)
	}
	if rateErr.Policy != "RateLimitAuthLogin" {
		t.Fatalf("unexpected 429 APIError policy: %+v", rateErr)
	}
}

func TestAdminOnlySeedStartsEmptyExceptAdminAndStaff(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{AdminOnlySeed: true})...)

	mariaLoginReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/auth/login",
		strings.NewReader(`{"email":"maria@rosafiesta.do","password":"secret123"}`),
	)
	mariaLoginReq.Header.Set("Content-Type", "application/json")
	mariaLoginRec := httptest.NewRecorder()
	handler.ServeHTTP(mariaLoginRec, mariaLoginReq)
	if mariaLoginRec.Code != http.StatusOK {
		t.Fatalf("expected 200 for maria@rosafiesta.do, got %d: %s", mariaLoginRec.Code, mariaLoginRec.Body.String())
	}

	camilaLoginReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/auth/login",
		strings.NewReader(`{"email":"camila@rosafiesta.do","password":"secret123"}`),
	)
	camilaLoginReq.Header.Set("Content-Type", "application/json")
	camilaLoginRec := httptest.NewRecorder()
	handler.ServeHTTP(camilaLoginRec, camilaLoginReq)
	if camilaLoginRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 for unseeded camila in AdminOnlySeed, got %d", camilaLoginRec.Code)
	}

	assertEmptyAdminSlice := func(path, field string) {
		t.Helper()
		req := httptest.NewRequestWithContext(ctx, http.MethodGet, path, nil)
		req.Header.Set("Authorization", "Bearer rf-token-maria")
		rec := httptest.NewRecorder()
		handler.ServeHTTP(rec, req)
		if rec.Code != http.StatusOK {
			t.Fatalf("GET %s failed with %d: %s", path, rec.Code, rec.Body.String())
		}
		var payload map[string]any
		if err := json.Unmarshal(rec.Body.Bytes(), &payload); err != nil {
			t.Fatalf("decode %s: %v", path, err)
		}
		rawVal, ok := payload[field]
		if !ok {
			return
		}
		if rawVal == nil {
			return
		}
		if items, isSlice := rawVal.([]any); isSlice {
			if len(items) != 0 {
				t.Fatalf("expected empty %s.%s, got %d items", path, field, len(items))
			}
		}
	}

	assertEmptyAdminSlice("/v1/catalog/articles", "articles")
	assertEmptyAdminSlice("/v1/catalog/categories", "categories")
	assertEmptyAdminSlice("/v1/catalog/packages", "packages")
	assertEmptyAdminSlice("/v1/events", "events")
	assertEmptyAdminSlice("/v1/quotes", "quotes")
	assertEmptyAdminSlice("/v1/payments", "payments")
	assertEmptyAdminSlice("/v1/admin/clients", "clients")
	assertEmptyAdminSlice("/v1/notifications", "notifications")

	staffReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/admin/staff", nil)
	staffReq.Header.Set("Authorization", "Bearer rf-token-maria")
	staffRec := httptest.NewRecorder()
	handler.ServeHTTP(staffRec, staffReq)
	var staffOut struct {
		Staff []struct {
			Email string `json:"email"`
		} `json:"staff"`
	}
	if err := json.Unmarshal(staffRec.Body.Bytes(), &staffOut); err != nil {
		t.Fatalf("decode /v1/admin/staff: %v", err)
	}
	if len(staffOut.Staff) != 2 {
		t.Fatalf("expected exactly 2 admin/staff entries (Maria and Yuli), got %d", len(staffOut.Staff))
	}

	summaryReq := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/payments/summary", nil)
	summaryReq.Header.Set("Authorization", "Bearer rf-token-maria")
	summaryRec := httptest.NewRecorder()
	handler.ServeHTTP(summaryRec, summaryReq)
	var summaryOut struct {
		Summary struct {
			OctRevenueRD        int `json:"octRevenueRD"`
			AccountsReceivable  int `json:"accountsReceivable"`
			ActiveEventsCount   int `json:"activeEventsCount"`
			NewQuoteRequests    int `json:"newQuoteRequests"`
			PendingVerification int `json:"pendingVerification"`
		} `json:"summary"`
	}
	if err := json.Unmarshal(summaryRec.Body.Bytes(), &summaryOut); err != nil {
		t.Fatalf("decode summary: %v", err)
	}
	hasNonZeroMetric := summaryOut.Summary.OctRevenueRD != 0
	if summaryOut.Summary.AccountsReceivable != 0 {
		hasNonZeroMetric = true
	}
	if summaryOut.Summary.ActiveEventsCount != 0 {
		hasNonZeroMetric = true
	}
	if summaryOut.Summary.NewQuoteRequests != 0 {
		hasNonZeroMetric = true
	}
	if summaryOut.Summary.PendingVerification != 0 {
		hasNonZeroMetric = true
	}
	if hasNonZeroMetric {
		t.Fatalf("expected zero metrics in AdminOnlySeed summary, got %+v", summaryOut.Summary)
	}
}
