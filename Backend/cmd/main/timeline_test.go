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

func TestTimelineHandlers(t *testing.T) {
	app := newTestApplication(t, configModels.Config{})
	mux := app.Mount()

	userID := uuid.New()
	user := &models.User{ID: userID, Role: models.Role{Level: 1}}
	eventID := uuid.New()
	event := &models.Event{ID: eventID, UserID: userID}

	// Helper to setup auth mocks
	setupAuth := func(tokenStr string) {
		jwtToken := &jwt.Token{
			Claims: jwt.MapClaims{
				"sub": userID.String(),
			},
			Valid: true,
		}
		app.Auth.(*authMocks.Authenticator).On("ValidateToken", tokenStr).Return(jwtToken, nil)
		app.Store.Users.(*storeMocks.UserStore).On("RetrieveById", mock.Anything, userID).Return(user, nil)
	}

	t.Run("should create timeline item", func(t *testing.T) {
		payload := CreateTimelineItemPayload{
			Title:     "Test Activity",
			StartTime: time.Now().Format(time.RFC3339),
			EndTime:   time.Now().Add(1 * time.Hour).Format(time.RFC3339),
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/events/"+eventID.String()+"/timeline", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Bearer valid-token")

		// Mocks
		setupAuth("valid-token")
		app.Store.Events.(*storeMocks.EventStore).On("GetByID", mock.Anything, eventID).Return(event, nil)
		app.Store.Timeline.(*storeMocks.TimelineStore).On("Create", mock.Anything, mock.Anything).Return(nil).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusCreated, rr)

		app.Store.Timeline.(*storeMocks.TimelineStore).AssertExpectations(t)
	})

	t.Run("should get timeline items", func(t *testing.T) {
		req, _ := http.NewRequest(http.MethodGet, "/v1/events/"+eventID.String()+"/timeline", nil)
		req.Header.Set("Authorization", "Bearer valid-token-2")

		// Mocks
		setupAuth("valid-token-2")
		app.Store.Events.(*storeMocks.EventStore).On("GetByID", mock.Anything, eventID).Return(event, nil)
		app.Store.Timeline.(*storeMocks.TimelineStore).On("GetByEventID", mock.Anything, eventID).Return([]models.TimelineItem{}, nil).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusOK, rr)
	})
}
