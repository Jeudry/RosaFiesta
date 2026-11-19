package httpapi_test

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestRateLimiterEnforcesStrictAuthBucketAndPreservesGeneralBucket(t *testing.T) {
	limiter := platformhttp.NewRateLimiterWithAuth(10, 2, time.Minute)
	handler := limiter.Middleware(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))

	// Two allowed requests to /v1/auth/refresh from 203.0.113.5, third must hit 429.
	for attempt := 0; attempt < 2; attempt++ {
		req := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/auth/refresh", nil)
		req.RemoteAddr = "203.0.113.5:50000"
		rec := httptest.NewRecorder()
		handler.ServeHTTP(rec, req)
		if rec.Code != http.StatusOK {
			t.Fatalf("attempt %d expected 200, got %d", attempt+1, rec.Code)
		}
	}

	blockedReq := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/auth/otp/request", nil)
	blockedReq.RemoteAddr = "203.0.113.5:50001"
	blockedRec := httptest.NewRecorder()
	handler.ServeHTTP(blockedRec, blockedReq)
	if blockedRec.Code != http.StatusTooManyRequests {
		t.Fatalf("expected 429 on 3rd sensitive auth request, got %d", blockedRec.Code)
	}

	// Same IP hitting general route still has its separate 10-req general quota.
	generalReq := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/v1/catalog/categories", nil)
	generalReq.RemoteAddr = "203.0.113.5:50002"
	generalRec := httptest.NewRecorder()
	handler.ServeHTTP(generalRec, generalReq)
	if generalRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on general route after auth throttle, got %d", generalRec.Code)
	}
}

func TestRateLimiterEvictsExpiredBucketsAfterWindow(t *testing.T) {
	limiter := platformhttp.NewRateLimiter(1, time.Minute)
	start := time.Date(2026, time.November, 15, 10, 0, 0, 0, time.UTC)

	if !limiter.Allow("198.51.100.1", start) {
		t.Fatal("expected first call allowed")
	}
	if limiter.Allow("198.51.100.1", start.Add(10*time.Second)) {
		t.Fatal("expected second call in same window blocked")
	}
	later := start.Add(2 * time.Minute)
	if !limiter.Allow("198.51.100.99", later) {
		t.Fatal("expected sweep on new window allowed")
	}
	if !limiter.Allow("198.51.100.1", later) {
		t.Fatal("expected evicted key allowed in fresh window")
	}
}
