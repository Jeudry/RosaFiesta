package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"
	"time"

	"Backend/cmd/main/configModels"
	authMocks "Backend/internal/auth/mocks"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func TestGetUserEvents(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return user events",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				evtM := app.Store.Events.(*storeMocks.EventStore)
				evtM.On("GetByUserID", mock.Anything, userID).Return([]models.Event{
					{ID: uuid.New(), UserID: userID, Name: "Wedding"},
					{ID: uuid.New(), UserID: userID, Name: "Birthday Party"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty list when no events",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				evtM := app.Store.Events.(*storeMocks.EventStore)
				evtM.On("GetByUserID", mock.Anything, userID).Return([]models.Event{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/events", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetEvent(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	eventID := uuid.New()

	tests := []struct {
		name         string
		eventID      string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should return event by ID",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				evtM := app.Store.Events.(*storeMocks.EventStore)
				evtM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					UserID: userID,
					Name:   "Wedding",
					Status: models.EventStatusPlanning,
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:    "should return 400 for invalid UUID",
			eventID: "invalid-uuid",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/events/"+tc.eventID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestCreateEvent(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	eventID := uuid.New()

	tests := []struct {
		name         string
		payload      models.CreateEventPayload
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should create event successfully",
			payload: models.CreateEventPayload{
				Name:       "Wedding Reception",
				Date:       "2026-06-15T14:00:00Z",
				Location:   "Beach Club",
				GuestCount: 100,
				Budget:     5000.00,
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				evtM := app.Store.Events.(*storeMocks.EventStore)
				evtM.On("Create", mock.Anything, mock.Anything).Run(func(args mock.Arguments) {
					evt := args.Get(1).(*models.Event)
					evt.ID = eventID
				}).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for invalid payload - missing name",
			payload: models.CreateEventPayload{
				Name: "", // Missing name
				Date: "2026-06-15T14:00:00Z",
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
			name: "should return 400 for invalid date format",
			payload: models.CreateEventPayload{
				Name: "Wedding",
				Date: "invalid-date",
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
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, configModels.Config{})
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			body, _ := json.Marshal(tc.payload)
			req, _ := http.NewRequest(http.MethodPost, "/v1/events", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestUpdateEvent(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	eventID := uuid.New()
	date := time.Now().Add(24 * time.Hour)

	tests := []struct {
		name         string
		eventID      string
		payload      map[string]interface{}
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should update event successfully",
			eventID: eventID.String(),
			payload: map[string]interface{}{
				"name":        "Updated Wedding",
				"guest_count": 150,
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				evtM := app.Store.Events.(*storeMocks.EventStore)
				evtM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					UserID: userID,
					Name:   "Wedding",
					Date:   &date,
					Status: models.EventStatusPlanning,
				}, nil).Once()
				evtM.On("Update", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:    "should return 400 for invalid UUID",
			eventID: "invalid-uuid",
			payload: map[string]interface{}{
				"name": "Updated",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()
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
			req, _ := http.NewRequest(http.MethodPut, "/v1/events/"+tc.eventID, bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestDeleteEvent(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	eventID := uuid.New()

	tests := []struct {
		name         string
		eventID      string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should delete event successfully",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				evtM := app.Store.Events.(*storeMocks.EventStore)
				evtM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					UserID: userID,
					Status: models.EventStatusPlanning,
				}, nil).Once()
				evtM.On("Delete", mock.Anything, eventID).Return(nil).Once()
			},
			expectedCode: http.StatusNoContent,
		},
		{
			name:    "should return 400 for invalid UUID",
			eventID: "invalid-uuid",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()
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
			req, _ := http.NewRequest(http.MethodDelete, "/v1/events/"+tc.eventID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}