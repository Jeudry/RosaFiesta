package main

import (
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	authMocks "Backend/internal/auth/mocks"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func TestListFavorites(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return list of favorites",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				favM := app.Store.Favorites.(*storeMocks.FavoritesStore)
				favM.On("List", mock.Anything, userID).Return([]models.Article{
					{BaseModel: models.BaseModel{ID: uuid.New()}, NameTemplate: "Table 1"},
					{BaseModel: models.BaseModel{ID: uuid.New()}, NameTemplate: "Chair 1"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty list when no favorites",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				favM := app.Store.Favorites.(*storeMocks.FavoritesStore)
				favM.On("List", mock.Anything, userID).Return([]models.Article{}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			req, _ := http.NewRequest(http.MethodGet, "/v1/favorites", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestAddFavorite(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	articleID := uuid.New()

	tests := []struct {
		name         string
		articleID    string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:      "should add favorite successfully",
			articleID: articleID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				favM := app.Store.Favorites.(*storeMocks.FavoritesStore)
				favM.On("Add", mock.Anything, userID, articleID).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name:      "should return 400 for invalid article ID",
			articleID: "invalid-uuid",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()
			},
			expectedCode: http.StatusBadRequest,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			req, _ := http.NewRequest(http.MethodPost, "/v1/favorites/"+tc.articleID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestRemoveFavorite(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	articleID := uuid.New()

	tests := []struct {
		name         string
		articleID    string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:      "should remove favorite successfully",
			articleID: articleID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				favM := app.Store.Favorites.(*storeMocks.FavoritesStore)
				favM.On("Remove", mock.Anything, userID, articleID).Return(nil).Once()
			},
			expectedCode: http.StatusNoContent,
		},
		{
			name:      "should return 400 for invalid article ID",
			articleID: "invalid-uuid",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()
			},
			expectedCode: http.StatusBadRequest,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			req, _ := http.NewRequest(http.MethodDelete, "/v1/favorites/"+tc.articleID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}