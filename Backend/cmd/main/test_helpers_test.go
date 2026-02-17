package main

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"Backend/cmd/main/configModels"
	authMocks "Backend/internal/auth/mocks"
	"Backend/internal/cache"
	cacheMocks "Backend/internal/cache/mocks"
	mailerMocks "Backend/internal/mailer/mocks"
	"Backend/internal/ratelimiter"
	"Backend/internal/store"
	storeMocks "Backend/internal/store/mocks"

	"go.uber.org/zap"
)

func newTestApplication(t *testing.T, cfg configModels.Config) *Application {
	t.Helper()

	logger := zap.NewNop().Sugar()
	// Uncomment to enable logs
	// logger := zap.Must(zap.NewProduction()).Sugar()

	mockStore := store.Storage{
		Articles:      &storeMocks.ArticlesStore{},
		Users:         &storeMocks.UserStore{},
		Roles:         &storeMocks.RoleStore{},
		RefreshTokens: &storeMocks.RefreshTokenStore{},
		Events:        &storeMocks.EventStore{},
		Guests:        &storeMocks.GuestStore{},
		EventTasks:    &storeMocks.EventTaskStore{},
		Suppliers:     &storeMocks.SupplierStore{},
		Timeline:      &storeMocks.TimelineStore{},
		Reviews:       &storeMocks.ReviewsStore{},
		Stats:         &storeMocks.StatsStore{},
		Messages:      &storeMocks.MessagesStore{},
	}

	mockCacheStore := cache.Storage{
		Users: &cacheMocks.UserCache{},
	}

	testAuth := &authMocks.Authenticator{} // Use mock authenticator
	testMailer := &mailerMocks.Mailer{}

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
		Mailer:       testMailer,
		Config:       cfg,
		RateLimiter:  rl,
	}
}

func executeRequest(req *http.Request, mux http.Handler) *httptest.ResponseRecorder {
	rr := httptest.NewRecorder()
	mux.ServeHTTP(rr, req)

	return rr
}

func checkResponseCode(t *testing.T, expected int, rr *httptest.ResponseRecorder) {
	if expected != rr.Code {
		t.Errorf("Expected response code %d. Got %d. Body: %s", expected, rr.Code, rr.Body.String())
	}
}
