package main

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"Backend/cmd/main/configModels"
	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/ratelimiter"
	"Backend/internal/store"

	"go.uber.org/zap"
)

func newTestApplication(t *testing.T, cfg configModels.Config) *Application {
	t.Helper()

	logger := zap.NewNop().Sugar()
	// Uncomment to enable logs
	// logger := zap.Must(zap.NewProduction()).Sugar()
	mockStore := store.NewMockStore()
	mockCacheStore := cache.NewMockStore()

	testAuth := &auth.TestAuthenticator{}

	// Initialize RateLimiter
	// If config enables it, we must provide a valid limiter.
	// Even if disabled, initializing it is safer if code relies on it, though middleware prevents use if disabled.
	var rl ratelimiter.RateLimiter
	if cfg.RateLimiter.Enabled {
		rl = ratelimiter.NewFixedWindowRateLimiter(
			cfg.RateLimiter.RequestsPerTimeFrame,
			cfg.RateLimiter.TimeFrame,
		)
	} else {
		// Provide a dummy or default implementation if needed to avoid nil pointer dereferences
		// if accessed elsewhere.
		// For now, let's assume NewFixedWindowRateLimiter handles zero values gracefully or we pass default.
		rl = ratelimiter.NewFixedWindowRateLimiter(100, 1) // Default
	}

	return &Application{
		Logger:       logger,
		Store:        mockStore,
		CacheStorage: mockCacheStore,
		Auth:         testAuth,
		Config:       cfg,
		RateLimiter:  rl,
	}
}

func executeRequest(req *http.Request, mux http.Handler) *httptest.ResponseRecorder {
	rr := httptest.NewRecorder()
	mux.ServeHTTP(rr, req)

	return rr
}

func checkResponseCode(t *testing.T, expected, actual int) {
	if expected != actual {
		t.Errorf("Expected response code %d. Got %d", expected, actual)
	}
}
