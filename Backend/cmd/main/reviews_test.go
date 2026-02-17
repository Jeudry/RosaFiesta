package main

import (
	"bytes"
	"encoding/json"
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

func TestCreateReview(t *testing.T) {
	articleID := uuid.New()
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		payload      map[string]interface{}
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should create review successfully",
			payload: map[string]interface{}{
				"rating":  5,
				"comment": "Excellent product!",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				articleM := app.Store.Articles.(*storeMocks.ArticlesStore)
				articleM.On("GetById", mock.Anything, articleID).Return(&models.Article{BaseModel: models.BaseModel{ID: articleID}}, nil).Once()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "moderator").Return(&models.Role{Name: "moderator", Level: 1}, nil).Maybe()

				reviewM := app.Store.Reviews.(*storeMocks.ReviewsStore)
				reviewM.On("Create", mock.Anything, mock.MatchedBy(func(r *models.Review) bool {
					return r.ArticleID == articleID && r.UserID == userID && r.Rating == 5 && r.Comment == "Excellent product!"
				})).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for invalid rating",
			payload: map[string]interface{}{
				"rating":  6,
				"comment": "Too high",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				articleM := app.Store.Articles.(*storeMocks.ArticlesStore)
				articleM.On("GetById", mock.Anything, articleID).Return(&models.Article{BaseModel: models.BaseModel{ID: articleID}}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodPost, "/v1/articles/"+articleID.String()+"/reviews", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetArticleReviews(t *testing.T) {
	articleID := uuid.New()

	tests := []struct {
		name         string
		articleID    string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:      "should return reviews for article",
			articleID: articleID.String(),
			setupMocks: func(app *Application) {
				articleM := app.Store.Articles.(*storeMocks.ArticlesStore)
				articleM.On("GetById", mock.Anything, articleID).Return(&models.Article{BaseModel: models.BaseModel{ID: articleID}}, nil).Once()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "moderator").Return(&models.Role{Name: "moderator", Level: 1}, nil).Maybe()

				reviewM := app.Store.Reviews.(*storeMocks.ReviewsStore)
				reviewM.On("GetByArticleID", mock.Anything, articleID).Return([]models.Review{
					{BaseModel: models.BaseModel{ID: uuid.New()}, ArticleID: articleID, Rating: 5, Comment: "Great!"},
				}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/articles/"+tc.articleID+"/reviews", nil)

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}
