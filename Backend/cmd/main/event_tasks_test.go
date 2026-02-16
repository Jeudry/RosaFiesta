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

func TestAddEventTaskHandler(t *testing.T) {
	cfg := configModels.Config{}
	app := newTestApplication(t, cfg)
	mux := app.Mount()

	taskID := uuid.New()
	eventID := uuid.New()

	payload := createEventTaskPayload{
		Title: "Test Task",
	}

	userID := uuid.New()
	user := &models.User{ID: userID, UserName: "testuser"}
	app.Store.Users.(*storeMocks.UserStore).On("RetrieveById", mock.Anything, mock.Anything).Return(user, nil)

	token := &jwt.Token{
		Claims: jwt.MapClaims{"sub": userID.String()},
		Valid:  true,
	}
	app.Auth.(*authMocks.Authenticator).On("ValidateToken", "mock-token").Return(token, nil)

	app.Store.EventTasks.(*storeMocks.EventTaskStore).On("Create", mock.Anything, mock.AnythingOfType("*models.EventTask")).Return(nil).Run(func(args mock.Arguments) {
		task := args.Get(1).(*models.EventTask)
		task.ID = taskID
	})

	body, _ := json.Marshal(payload)
	req, _ := http.NewRequest(http.MethodPost, fmt.Sprintf("/v1/events/%s/tasks", eventID.String()), bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer mock-token")

	rr := executeRequest(req, mux)

	checkResponseCode(t, http.StatusCreated, rr)
}

func TestGetEventTasksHandler(t *testing.T) {
	cfg := configModels.Config{}
	app := newTestApplication(t, cfg)
	mux := app.Mount()

	eventID := uuid.New()

	tasks := []models.EventTask{
		{ID: uuid.New(), EventID: eventID, Title: "Task 1", IsCompleted: false},
	}

	app.Store.EventTasks.(*storeMocks.EventTaskStore).On("GetByEventID", mock.Anything, eventID).Return(tasks, nil)

	userID := uuid.New()
	user := &models.User{ID: userID, UserName: "testuser"}
	app.Store.Users.(*storeMocks.UserStore).On("RetrieveById", mock.Anything, mock.Anything).Return(user, nil)

	token := &jwt.Token{
		Claims: jwt.MapClaims{"sub": userID.String()},
		Valid:  true,
	}
	app.Auth.(*authMocks.Authenticator).On("ValidateToken", "mock-token").Return(token, nil)

	req, _ := http.NewRequest(http.MethodGet, fmt.Sprintf("/v1/events/%s/tasks", eventID.String()), nil)
	req.Header.Set("Authorization", "Bearer mock-token")
	rr := executeRequest(req, mux)

	checkResponseCode(t, http.StatusOK, rr)
}
