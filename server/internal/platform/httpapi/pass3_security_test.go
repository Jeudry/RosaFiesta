package httpapi_test

import (
	"log/slog"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestTrustedProxiesIsolateForwardedClientBuckets(t *testing.T) {
	t.Parallel()

	limiter := platformhttp.NewRateLimiter(1, time.Minute).WithTrustedProxies([]string{"172.18.0.0/16"})
	handler := limiter.Middleware(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))

	reqOne := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/v1/catalog/categories", nil)
	reqOne.RemoteAddr = "172.18.0.5:43120"
	reqOne.Header.Set("X-Forwarded-For", "198.51.100.10")
	recOne := httptest.NewRecorder()
	handler.ServeHTTP(recOne, reqOne)
	if recOne.Code != http.StatusOK {
		t.Fatalf("expected first proxied client allowed, got %d", recOne.Code)
	}

	// Second distinct client behind the same Caddy proxy IP must not share the rate-limit bucket.
	reqTwo := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/v1/catalog/categories", nil)
	reqTwo.RemoteAddr = "172.18.0.5:43121"
	reqTwo.Header.Set("X-Forwarded-For", "198.51.100.11")
	recTwo := httptest.NewRecorder()
	handler.ServeHTTP(recTwo, reqTwo)
	if recTwo.Code != http.StatusOK {
		t.Fatalf("expected second proxied client isolated, got %d", recTwo.Code)
	}

	// Untrusted direct caller cannot bypass its bucket by spoofing X-Forwarded-For.
	untrustedA := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/v1/catalog/categories", nil)
	untrustedA.RemoteAddr = "203.0.113.50:9000"
	untrustedA.Header.Set("X-Forwarded-For", "198.51.100.99")
	recA := httptest.NewRecorder()
	handler.ServeHTTP(recA, untrustedA)
	if recA.Code != http.StatusOK {
		t.Fatalf("expected first untrusted request allowed, got %d", recA.Code)
	}

	untrustedB := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/v1/catalog/categories", nil)
	untrustedB.RemoteAddr = "203.0.113.50:9001"
	untrustedB.Header.Set("X-Forwarded-For", "198.51.100.100")
	recB := httptest.NewRecorder()
	handler.ServeHTTP(recB, untrustedB)
	if recB.Code != http.StatusTooManyRequests {
		t.Fatalf("expected spoofed header from untrusted remote blocked with 429, got %d", recB.Code)
	}
}

func TestRouterOptionsAcceptTrustedProxies(t *testing.T) {
	t.Parallel()
	handler, _ := platformhttp.NewWithOptions(
		slog.New(slog.DiscardHandler),
		platformhttp.RouterOptions{TrustedProxies: []string{"127.0.0.1/32"}},
	)
	if handler == nil {
		t.Fatal("expected non-nil router handler")
	}
}
