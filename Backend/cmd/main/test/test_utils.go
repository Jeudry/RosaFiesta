package test

import (
	"Backend/cmd/main/configModels"
	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/store"
	"net/http"
	"net/http/httptest"
	"testing"

	"go.uber.org/zap"
)

func newTestApplication(t *testing.T, cfg configModels.Config) *main.Application {
	t.Helper()

	logger := zap.NewNop().Sugar()
	// Uncomment to enable logs
	// logger := zap.Must(zap.NewProduction()).Sugar()
	mockStore := store.NewMockStore()
	mockCacheStore := cache.NewMockStore()

	testAuth := &auth.TestAuthenticator{}

	return &main.Application{
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
