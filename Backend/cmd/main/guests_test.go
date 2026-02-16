package main

import (
	"bytes"
	"encoding/json"
	"fmt"
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

func TestAddGuestHandler(t *testing.T) {
	cfg := configModels.Config{}
	app := newTestApplication(t, cfg)
	mux := app.Mount()

	guestID := uuid.New()
	eventID := uuid.New()

	payload := createGuestPayload{
		Name:       "Test Guest",
		RSVPStatus: "pending",
	}

	app.Store.Guests.(*storeMocks.GuestStore).On("Create", mock.Anything, mock.AnythingOfType("*models.Guest")).Return(nil).Run(func(args mock.Arguments) {
		guest := args.Get(1).(*models.Guest)
		guest.ID = guestID
	})

	userID := uuid.New()
	user := &models.User{ID: userID, UserName: "testuser"}
	app.Store.Users.(*storeMocks.UserStore).On("RetrieveById", mock.Anything, mock.Anything).Return(user, nil)

	token := &jwt.Token{
		Claims: jwt.MapClaims{"sub": userID.String()},
		Valid:  true,
	}
	app.Auth.(*authMocks.Authenticator).On("ValidateToken", "mock-token").Return(token, nil)

	body, _ := json.Marshal(payload)
	req, _ := http.NewRequest(http.MethodPost, fmt.Sprintf("/v1/events/%s/guests", eventID.String()), bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer mock-token")

	rr := executeRequest(req, mux)

	checkResponseCode(t, http.StatusCreated, rr)
}

func TestGetGuestsHandler(t *testing.T) {
	cfg := configModels.Config{}
	app := newTestApplication(t, cfg)
	mux := app.Mount()

	eventID := uuid.New()

	guests := []models.Guest{
		{ID: uuid.New(), EventID: eventID, Name: "Guest 1"},
	}

	app.Store.Guests.(*storeMocks.GuestStore).On("GetByEventID", mock.Anything, eventID).Return(guests, nil)

	userID := uuid.New()
	user := &models.User{ID: userID, UserName: "testuser"}
	app.Store.Users.(*storeMocks.UserStore).On("RetrieveById", mock.Anything, mock.Anything).Return(user, nil)

	token := &jwt.Token{
		Claims: jwt.MapClaims{"sub": userID.String()},
		Valid:  true,
	}
	app.Auth.(*authMocks.Authenticator).On("ValidateToken", "mock-token").Return(token, nil)

	req, _ := http.NewRequest(http.MethodGet, fmt.Sprintf("/v1/events/%s/guests", eventID.String()), nil)
	req.Header.Set("Authorization", "Bearer mock-token")
	rr := executeRequest(req, mux)

	checkResponseCode(t, http.StatusOK, rr)
}
