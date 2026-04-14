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

func TestGetMessages(t *testing.T) {
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
			name:    "should return messages for event",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "admin", Level: 5}}, nil).Maybe()

				msgM := app.Store.Messages.(*storeMocks.MessagesStore)
				msgM.On("GetByEventID", mock.Anything, eventID).Return([]models.EventMessage{
					{ID: uuid.New(), EventID: eventID, Content: "Hello"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:    "should return empty list when no messages",
			eventID: eventID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "admin", Level: 5}}, nil).Maybe()

				msgM := app.Store.Messages.(*storeMocks.MessagesStore)
				msgM.On("GetByEventID", mock.Anything, eventID).Return([]models.EventMessage{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/events/"+tc.eventID+"/messages", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}