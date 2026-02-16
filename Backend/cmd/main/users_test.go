package main

import (
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	authMocks "Backend/internal/auth/mocks"
	cacheMocks "Backend/internal/cache/mocks"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

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
		setupMocks     func(*Application)
		expectedCode   int
		expectCacheHit bool
	}{
		{
			name:         "should hit the cache and return 200",
			cacheEnabled: true,
			authHeaderFunc: func(app *Application) string {
				return "Bearer valid-token"
			},
			requestURL: "/v1/users/" + userIDStr,
			setupMocks: func(app *Application) {
				// Mock Auth
				// We need to return a token with the correct claims (sub=userID)
				token := &jwt.Token{
					Claims: jwt.MapClaims{
						"sub": userIDStr,
					},
					Valid: true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				// Mock Cache
				// 1st call (Middleware)
				m := app.CacheStorage.Users.(*cacheMocks.UserCache)
				m.On("Get", mock.Anything, userID).Return(nil, nil).Once()
				// We simulated fail above, so it will check Store.
				// But wait, GetUser calls Store if cache miss.
				// We need to mock Store too!
				// Middleware: GetUser(cache miss) -> Store.RetrieveById -> sets cache

				// Handler calls GetUser again?
				// The handler 'getUserHandler' uses GetUserFromCtx(r).
				// The middleware sets the user in context.
				// So handler doesn't call GetUser again from DB/Cache?
				// Let's check middleware.go.
				// AuthTokenMiddleware: calls app.GetUser(ctx, userID), then sets context.
				// getUserHandler: gets from context.
				// So only 1 call to GetUser (in middleware).

				// app.GetUser calls Cache.Get. If miss, calls Store.RetrieveById, then Cache.Set.

				// So we expect:
				// 1. Cache.Get(userID) -> nil (miss)
				// 2. Store.RetrieveById(userID) -> returns User
				// 3. Cache.Set(user) -> nil

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID}, nil).Once()

				m.On("Set", mock.Anything, mock.Anything).Return(nil).Once()

				// 2nd call (Handler)
				m.On("Get", mock.Anything, userID).Return(&models.User{ID: userID}, nil).Once()
			},
			expectedCode:   http.StatusOK,
			expectCacheHit: true,
		},
		{
			name:         "should not hit cache if disabled",
			cacheEnabled: false,
			authHeaderFunc: func(app *Application) string {
				return "Bearer valid-token"
			},
			requestURL: "/v1/users/" + userIDStr,
			setupMocks: func(app *Application) {
				// Mock Auth
				token := &jwt.Token{
					Claims: jwt.MapClaims{
						"sub": userIDStr,
					},
					Valid: true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				// Cache disabled -> app.GetUser calls Store.RetrieveById directly
				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID}, nil).Times(2)
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
			setupMocks: func(app *Application) {
				// No mocks needed as auth fails early
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

			if tc.setupMocks != nil {
				tc.setupMocks(app)
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
			checkResponseCode(t, tc.expectedCode, rr)

			// Verify Mock Assertions
			mockCacheStore := app.CacheStorage.Users.(*cacheMocks.UserCache)
			storeM := app.Store.Users.(*storeMocks.UserStore)
			authM := app.Auth.(*authMocks.Authenticator)

			if tc.expectCacheHit {
				mockCacheStore.AssertNumberOfCalls(t, "Get", 2)
			} else {
				mockCacheStore.AssertNotCalled(t, "Get")
			}

			// Verify all expectations
			mockCacheStore.AssertExpectations(t)
			storeM.AssertExpectations(t)
			authM.AssertExpectations(t)
		})
	}
}
