package test

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"Backend/internal/app"
	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/config"
	"Backend/internal/store"

	"go.uber.org/zap"
)

func newTestApplication(t *testing.T, cfg config.Config) *app.Application {
	t.Helper()

	logger := zap.NewNop().Sugar()
	// Uncomment to enable logs
	// logger := zap.Must(zap.NewProduction()).Sugar()
	mockStore := store.NewMockStore()
	mockCacheStore := cache.NewMockStore()

	testAuth := &auth.TestAuthenticator{}

	return &app.Application{
		Logger:       logger,
		Store:        mockStore,
		CacheStorage: mockCacheStore,
		Auth:         testAuth,
		Config:       cfg,
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
