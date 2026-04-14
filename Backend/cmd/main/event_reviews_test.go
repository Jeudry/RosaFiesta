package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	"Backend/internal/store"
	authMocks "Backend/internal/auth/mocks"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func TestCreateEventReview(t *testing.T) {
	eventID := uuid.New()
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		eventID      string
		payload      map[string]interface{}
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should create event review successfully",
			eventID: eventID.String(),
			payload: map[string]interface{}{
				"rating":  5,
				"comment": "Amazing event!",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					Status: "completed",
				}, nil).Once()

				reviewM := app.Store.EventReviews.(*storeMocks.EventReviewsStore)
				reviewM.On("Create", mock.Anything, mock.MatchedBy(func(r *models.EventReview) bool {
					return r.EventID == eventID && r.UserID == userID && r.Rating == 5
				})).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name:    "should return 404 for non-existent event",
			eventID: eventID.String(),
			payload: map[string]interface{}{
				"rating":  5,
				"comment": "Great!",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(nil, store.ErrNotFound).Once()
			},
			expectedCode: http.StatusNotFound,
		},
		{
			name:    "should return 400 for incomplete event",
			eventID: eventID.String(),
			payload: map[string]interface{}{
				"rating":  5,
				"comment": "Great!",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					Status: "in_progress",
				}, nil).Once()
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name:    "should return 400 for invalid rating",
			eventID: eventID.String(),
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

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					Status: "completed",
				}, nil).Once()
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name:    "should return 400 for missing comment",
			eventID: eventID.String(),
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

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					Status: "completed",
				}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodPost, "/v1/events/"+tc.eventID+"/reviews", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetEventReviews(t *testing.T) {
	eventID := uuid.New()
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		eventID      string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should return reviews for completed event",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					Status: "completed",
				}, nil).Once()

				reviewM := app.Store.EventReviews.(*storeMocks.EventReviewsStore)
				reviewM.On("GetByEventID", mock.Anything, eventID).Return([]models.EventReview{
					{BaseModel: models.BaseModel{ID: uuid.New()}, EventID: eventID, Rating: 5, Comment: "Great!"},
				}, nil).Once()
				reviewM.On("GetPhotos", mock.Anything, mock.Anything).Return([]models.ReviewPhoto{}, nil).Maybe()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:    "should return 404 for non-existent event",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(nil, store.ErrNotFound).Once()
			},
			expectedCode: http.StatusNotFound,
		},
		{
			name:    "should return empty list for event with no reviews",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				eventM := app.Store.Events.(*storeMocks.EventStore)
				eventM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					Status: "completed",
				}, nil).Once()

				reviewM := app.Store.EventReviews.(*storeMocks.EventReviewsStore)
				reviewM.On("GetByEventID", mock.Anything, eventID).Return([]models.EventReview{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/events/"+tc.eventID+"/reviews", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}