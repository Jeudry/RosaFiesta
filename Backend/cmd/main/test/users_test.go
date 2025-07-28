package test

import (
	"Backend/cmd/main/configModels"
	"Backend/internal/cache"
	"github.com/stretchr/testify/mock"
	"net/http"
	"testing"
)

func TestGetUser(t *testing.T) {
	withRedis := configModels.Config{
		Redis: configModels.RedisConfig{
			Enabled: true,
		},
	}
	app := newTestApplication(t, withRedis)
	mux := app.Mount()

	testToken, err := app.Auth.GenerateToken(nil)

	if err != nil {
		t.Fatal(err)
	}

	t.Run("should hit the cache first and if not exists it sets the user on the cache", func(t *testing.T) {
		mockCacheStore := app.CacheStorage.Users.(*cache.MockUserStore)

		mockCacheStore.On("Get", uuid.UUID(1)).Return(nil, nil)
		mockCacheStore.On("Get", int64(42)).Return(nil, nil)
		mockCacheStore.On("Set", mock.Anything, mock.Anything).Return(nil)

		req, err := http.NewRequest(http.MethodGet, "/v1/users/1", nil)
		if err != nil {
			t.Fatal(err)
		}

		req.Header.Set("Authorization", "Bearer "+testToken)

		rr := executeRequest(req, mux)

		checkResponseCode(t, http.StatusOK, rr.Code)

		mockCacheStore.AssertNumberOfCalls(t, "Get", 2)

		mockCacheStore.Calls = nil
	})

	t.Run("should not hit the cache if is not enabled", func(t *testing.T) {
		withRedis := configModels.Config{
			Redis: configModels.RedisConfig{
				Enabled: false,
			},
		}

		app := newTestApplication(t, withRedis)
		mux := app.Mount()

		mockCacheStore := app.CacheStorage.Users.(*cache.MockUserStore)

		req, err := http.NewRequest(http.MethodGet, "/v1/users/1", nil)

		if err != nil {
			t.Fatal(err)
		}

		req.Header.Set("Authorization", "Bearer "+testToken)

		rr := executeRequest(req, mux)

		checkResponseCode(t, http.StatusOK, rr.Code)

		mockCacheStore.AssertNotCalled(t, "Get")

		mockCacheStore.Calls = nil
	})

	t.Run("should not allow unauthorized access", func(t *testing.T) {
		req, err := http.NewRequest("GET", "/v1/users/1", nil)

		if err != nil {
			t.Fatal(err)
		}

		rr := executeRequest(req, mux)

		checkResponseCode(t, http.StatusUnauthorized, rr.Code)
	})

	t.Run("should allow authenticated requests", func(t *testing.T) {
		req, err := http.NewRequest(http.MethodGet, "/v1/users/1", nil)

		if err != nil {
			t.Fatal(err)
		}

		req.Header.Set("Authorization", "Bearer "+testToken)

		rr := executeRequest(req, mux)

		checkResponseCode(t, http.StatusOK, rr.Code)
	})
}
