package main

import (
	"net/http"
	"testing"
	"time"

	"Backend/cmd/main/configModels"
	authMocks "Backend/internal/auth/mocks"
	"Backend/internal/store"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func TestGetEventCalendar(t *testing.T) {
	eventID := uuid.New()
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		eventID     string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:    "should return calendar for valid event",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{Claims: jwt.MapClaims{"sub": userIDStr}, Valid: true}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				eventsM := app.Store.Events.(*storeMocks.EventStore)
				futureDate := time.Now().Add(7 * 24 * time.Hour)
				eventsM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:     eventID,
					UserID: userID,
					Name:   "Test Event",
					Date:   &futureDate,
				}, nil).Once()

				timelineM := app.Store.Timeline.(*storeMocks.TimelineStore)
				timelineM.On("GetByEventID", mock.Anything, eventID).Return([]models.TimelineItem{}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:    "should return 400 for invalid event UUID",
			eventID: "invalid-uuid",
			setupMocks: func(app *Application) {
				token := &jwt.Token{Claims: jwt.MapClaims{"sub": userIDStr}, Valid: true}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name:    "should return 404 for non-existent event",
			eventID: uuid.New().String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{Claims: jwt.MapClaims{"sub": userIDStr}, Valid: true}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				eventsM := app.Store.Events.(*storeMocks.EventStore)
				eventsM.On("GetByID", mock.Anything, mock.Anything).Return(nil, store.ErrNotFound).Maybe()
			},
			expectedCode: http.StatusNotFound,
		},
		{
			name:    "should return 400 when event has no date",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{Claims: jwt.MapClaims{"sub": userIDStr}, Valid: true}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				eventsM := app.Store.Events.(*storeMocks.EventStore)
				eventsM.On("GetByID", mock.Anything, eventID).Return(&models.Event{
					ID:   eventID,
					Name: "Draft Event",
					Date: nil,
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/events/"+tc.eventID+"/calendar.ics", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}
