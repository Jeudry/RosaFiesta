package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	"Backend/cmd/main/view_models/categories"
	authMocks "Backend/internal/auth/mocks"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func ptrStr(s string) *string {
	return &s
}

func TestCreateCategory(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		payload      categories.CreateCategoryPayload
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should create category successfully",
			payload: categories.CreateCategoryPayload{
				Name:        "Decorations",
				Description: ptrStr("Party decorations"),
				ImageURL:    ptrStr("https://example.com/cat.jpg"),
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "moderator", Level: 3}}, nil).Once()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "moderator").Return(&models.Role{Name: "moderator", Level: 3}, nil).Once()

				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("Create", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for invalid payload",
			payload: categories.CreateCategoryPayload{
				Name: "", // Missing name
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "moderator", Level: 3}}, nil).Once()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "moderator").Return(&models.Role{Name: "moderator", Level: 3}, nil).Maybe()
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
			body, _ := json.Marshal(tc.payload)
			req, _ := http.NewRequest(http.MethodPost, "/v1/categories", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetAllCategories(t *testing.T) {
	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return all categories",
			setupMocks: func(app *Application) {
				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("GetAll", mock.Anything).Return([]models.Category{
					{BaseModel: models.BaseModel{ID: uuid.New()}, Name: "Tables"},
					{BaseModel: models.BaseModel{ID: uuid.New()}, Name: "Chairs"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty list when no categories",
			setupMocks: func(app *Application) {
				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("GetAll", mock.Anything).Return([]models.Category{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/categories", nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetCategory(t *testing.T) {
	categoryID := uuid.New()

	tests := []struct {
		name         string
		categoryID   string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:       "should return category by ID",
			categoryID: categoryID.String(),
			setupMocks: func(app *Application) {
				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("GetById", mock.Anything, categoryID).Return(&models.Category{
					BaseModel: models.BaseModel{ID: categoryID},
					Name:      "Tables",
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:       "should return 400 for invalid UUID",
			categoryID: "invalid-uuid",
			setupMocks: func(app *Application) {},
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/categories/"+tc.categoryID, nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestDeleteCategory(t *testing.T) {
	categoryID := uuid.New()
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		categoryID   string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:       "should delete category successfully",
			categoryID: categoryID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "moderator", Level: 3}}, nil).Maybe()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "moderator").Return(&models.Role{Name: "moderator", Level: 3}, nil).Maybe()

				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("GetById", mock.Anything, categoryID).Return(&models.Category{BaseModel: models.BaseModel{ID: categoryID}}, nil).Once()
				catM.On("Delete", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusNoContent,
		},
		{
			name:       "should return 400 for invalid UUID",
			categoryID: "invalid-uuid",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "moderator", Level: 3}}, nil).Maybe()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "moderator").Return(&models.Role{Name: "moderator", Level: 3}, nil).Maybe()
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
			req, _ := http.NewRequest(http.MethodDelete, "/v1/categories/"+tc.categoryID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetArticlesByCategory(t *testing.T) {
	categoryID := uuid.New()
	articleID := uuid.New()

	tests := []struct {
		name         string
		categoryID   string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:       "should return articles for category",
			categoryID: categoryID.String(),
			setupMocks: func(app *Application) {
				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("GetById", mock.Anything, categoryID).Return(&models.Category{BaseModel: models.BaseModel{ID: categoryID}}, nil).Once()

				artM := app.Store.Articles.(*storeMocks.ArticlesStore)
				artM.On("GetByCategoryID", mock.Anything, categoryID).Return([]models.Article{
					{BaseModel: models.BaseModel{ID: articleID}, NameTemplate: "Round Table"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:       "should return empty list when no articles",
			categoryID: categoryID.String(),
			setupMocks: func(app *Application) {
				catM := app.Store.Categories.(*storeMocks.CategoryStore)
				catM.On("GetById", mock.Anything, categoryID).Return(&models.Category{BaseModel: models.BaseModel{ID: categoryID}}, nil).Once()

				artM := app.Store.Articles.(*storeMocks.ArticlesStore)
				artM.On("GetByCategoryID", mock.Anything, categoryID).Return([]models.Article{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/categories/"+tc.categoryID+"/articles", nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}