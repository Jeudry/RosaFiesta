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

func TestCreateCompanyReview(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		payload      map[string]interface{}
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should create company review successfully",
			payload: map[string]interface{}{
				"rating":  5,
				"comment": "Excellent service!",
				"source":  "google",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				reviewM := app.Store.CompanyReviews.(*storeMocks.CompanyReviewsStore)
				reviewM.On("Create", mock.Anything, mock.MatchedBy(func(r *models.CompanyReview) bool {
					return r.UserID == userID && r.Rating == 5 && r.Comment == "Excellent service!" && r.Source == "google"
				})).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for invalid rating (too low)",
			payload: map[string]interface{}{
				"rating":  0,
				"comment": "Bad service",
			},
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
		{
			name: "should return 400 for invalid rating (too high)",
			payload: map[string]interface{}{
				"rating":  6,
				"comment": "Too good",
			},
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
		{
			name: "should return 400 for missing comment",
			payload: map[string]interface{}{
				"rating": 5,
			},
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
		{
			name: "should default source to direct when not provided",
			payload: map[string]interface{}{
				"rating":  4,
				"comment": "Good service",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				reviewM := app.Store.CompanyReviews.(*storeMocks.CompanyReviewsStore)
				reviewM.On("Create", mock.Anything, mock.MatchedBy(func(r *models.CompanyReview) bool {
					return r.Source == "direct"
				})).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
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
			req, _ := http.NewRequest(http.MethodPost, "/v1/company/reviews", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetCompanyReviews(t *testing.T) {
	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return all company reviews",
			setupMocks: func(app *Application) {
				reviewM := app.Store.CompanyReviews.(*storeMocks.CompanyReviewsStore)
				reviewM.On("GetAll", mock.Anything).Return([]models.CompanyReview{
					{BaseModel: models.BaseModel{ID: uuid.New()}, Rating: 5, Comment: "Great!"},
					{BaseModel: models.BaseModel{ID: uuid.New()}, Rating: 4, Comment: "Good"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty list when no reviews",
			setupMocks: func(app *Application) {
				reviewM := app.Store.CompanyReviews.(*storeMocks.CompanyReviewsStore)
				reviewM.On("GetAll", mock.Anything).Return([]models.CompanyReview{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/company/reviews", nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetCompanyReviewsSummary(t *testing.T) {
	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return reviews summary",
			setupMocks: func(app *Application) {
				reviewM := app.Store.CompanyReviews.(*storeMocks.CompanyReviewsStore)
				reviewM.On("GetSummary", mock.Anything).Return(4.5, 10, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return zero average when no reviews",
			setupMocks: func(app *Application) {
				reviewM := app.Store.CompanyReviews.(*storeMocks.CompanyReviewsStore)
				reviewM.On("GetSummary", mock.Anything).Return(float64(0), 0, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/company/reviews/summary", nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}