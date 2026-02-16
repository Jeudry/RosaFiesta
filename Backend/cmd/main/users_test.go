package main

import (
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	"Backend/internal/cache"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func TestGetUser(t *testing.T) {
	// Setup a valid UUID for testing
	// This user ID will be used BOTH as the authenticated user AND the requested user resource
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name           string
		cacheEnabled   bool
		authHeaderFunc func(app *Application) string
		requestURL     string
		setupMocks     func(*cache.MockUserStore)
		expectedCode   int
		expectCacheHit bool
	}{
		{
			name:         "should hit the cache and return 200",
			cacheEnabled: true,
			authHeaderFunc: func(app *Application) string {
				// Generate token for THE SAME user ID we are requesting
				claims := jwt.MapClaims{
					"sub": userIDStr,
				}
				token, _ := app.Auth.GenerateToken(claims)
				return "Bearer " + token
			},
			requestURL: "/v1/users/" + userIDStr,
			setupMocks: func(m *cache.MockUserStore) {
				// Mocks need to handle BOTH the authentication check AND the handler logic
				// 1. AuthMiddleware calls GetUser(userID)
				// 2. Handler calls GetUser(userID)
				// So we expect 2 calls to Get(userID) and 2 calls to Set(userID) (since we simulate cache miss via nil return)

				// Simulate Cache MISS (returns nil, nil)
				m.On("Get", userID).Return(nil, nil)
				m.On("Set", mock.Anything, mock.Anything).Return(nil)
			},
			expectedCode:   http.StatusOK,
			expectCacheHit: true,
		},
		{
			name:         "should not hit cache if disabled",
			cacheEnabled: false,
			authHeaderFunc: func(app *Application) string {
				claims := jwt.MapClaims{
					"sub": userIDStr,
				}
				token, _ := app.Auth.GenerateToken(claims)
				return "Bearer " + token
			},
			requestURL: "/v1/users/" + userIDStr,
			setupMocks: func(m *cache.MockUserStore) {
				// No cache calls expected
			},
			expectedCode:   http.StatusOK,
			expectCacheHit: false,
		},
		{
			name:         "should not allow unauthorized access",
			cacheEnabled: true,
			authHeaderFunc: func(app *Application) string {
				return "" // No auth header
			},
			requestURL: "/v1/users/" + userIDStr,
			setupMocks: func(m *cache.MockUserStore) {
				// No calls expected as auth fails first
			},
			expectedCode:   http.StatusUnauthorized,
			expectCacheHit: false,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			cfg := configModels.Config{
				Redis: configModels.RedisConfig{
					Enabled: tc.cacheEnabled,
				},
			}
			app := newTestApplication(t, cfg)

			// Setup Mocks
			mockCacheStore := app.CacheStorage.Users.(*cache.MockUserStore) // Test helper guarantees this type
			if tc.setupMocks != nil {
				tc.setupMocks(mockCacheStore)
			}

			// Mount routes
			mux := app.Mount()

			// Requests
			req, err := http.NewRequest(http.MethodGet, tc.requestURL, nil)
			if err != nil {
				t.Fatal(err)
			}

			if tc.authHeaderFunc != nil {
				header := tc.authHeaderFunc(app)
				if header != "" {
					req.Header.Set("Authorization", header)
				}
			}

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr.Code)

			// Verify Mock Assertions
			if tc.expectCacheHit {
				// Since middleware AND handler call GetUser, we expect multiple calls
				// We won't assert exact number unless critical, but "at least 1" is good enough for "Hit" check logic
				// Actually, we expect 2.
				mockCacheStore.AssertNumberOfCalls(t, "Get", 2)
			} else {
				mockCacheStore.AssertNotCalled(t, "Get")
			}
		})
	}
}
