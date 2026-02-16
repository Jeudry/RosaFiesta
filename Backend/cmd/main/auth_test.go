package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"
	"time"

	"Backend/cmd/main/configModels"
	"Backend/cmd/main/view_models/users"
	authMocks "Backend/internal/auth/mocks"
	mailerMocks "Backend/internal/mailer/mocks"
	"Backend/internal/store"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestRegisterUser(t *testing.T) {
	app := newTestApplication(t, configModels.Config{})
	mux := app.Mount()

	t.Run("should register a user successfully", func(t *testing.T) {
		payload := users.RegisterUserPayload{
			Username: "testuser",
			Email:    "test@example.com",
			Password: "password123",
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/authentication/register", bytes.NewBuffer(body))

		// Mocks
		app.Store.Users.(*storeMocks.UserStore).On("CreateAndInvite", mock.Anything, mock.Anything, mock.Anything, mock.Anything).Return(nil).Once()
		app.Mailer.(*mailerMocks.Mailer).On("Send", mock.Anything, "testuser", "test@example.com", mock.Anything, mock.Anything).Return(200, nil).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusCreated, rr)

		app.Store.Users.(*storeMocks.UserStore).AssertExpectations(t)
		app.Mailer.(*mailerMocks.Mailer).AssertExpectations(t)
	})

	t.Run("should fail on invalid payload", func(t *testing.T) {
		payload := users.RegisterUserPayload{
			Username: "", // Missing username
			Email:    "invalid-email",
			Password: "123", // Too short
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/authentication/register", bytes.NewBuffer(body))

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusBadRequest, rr)
	})
}

func TestRefreshToken(t *testing.T) {
	t.Run("should refresh token successfully", func(t *testing.T) {
		app := newTestApplication(t, configModels.Config{})
		mux := app.Mount()

		payload := users.RefreshTokenRequest{
			RefreshToken: "valid-refresh-token",
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/authentication/refresh", bytes.NewBuffer(body))

		userID := uuid.New()
		refreshToken := &models.RefreshToken{
			UserID:    userID,
			Token:     "valid-refresh-token",
			ExpiresAt: time.Now().Add(time.Hour),
		}

		user := &models.User{
			ID: userID,
		}

		// Mocks
		app.Store.RefreshTokens.(*storeMocks.RefreshTokenStore).On("GetByToken", mock.Anything, "valid-refresh-token").Return(refreshToken, nil).Once()
		app.Store.Users.(*storeMocks.UserStore).On("RetrieveById", mock.Anything, userID).Return(user, nil).Once()
		app.Auth.(*authMocks.Authenticator).On("GenerateToken", mock.Anything).Return("new-access-token", nil).Once()
		app.Store.RefreshTokens.(*storeMocks.RefreshTokenStore).On("Delete", mock.Anything, "valid-refresh-token").Return(nil).Once()
		app.Store.RefreshTokens.(*storeMocks.RefreshTokenStore).On("Create", mock.Anything, mock.Anything).Return(nil).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusOK, rr)

		var resp struct {
			Data users.LoginResponse `json:"data"`
		}
		json.Unmarshal(rr.Body.Bytes(), &resp)
		assert.Equal(t, "new-access-token", resp.Data.AccessToken)
		assert.NotEmpty(t, resp.Data.RefreshToken)
	})

	t.Run("should fail on invalid refresh token", func(t *testing.T) {
		app := newTestApplication(t, configModels.Config{})
		mux := app.Mount()

		payload := users.RefreshTokenRequest{
			RefreshToken: "invalid-token",
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/authentication/refresh", bytes.NewBuffer(body))

		app.Store.RefreshTokens.(*storeMocks.RefreshTokenStore).On("GetByToken", mock.Anything, "invalid-token").Return(nil, store.ErrNotFound).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusUnauthorized, rr)
	})
}

func TestCreateToken(t *testing.T) {
	t.Run("should create token successfully", func(t *testing.T) {
		app := newTestApplication(t, configModels.Config{})
		mux := app.Mount()

		payload := users.CreateUserTokenPayload{
			Email:    "test@example.com",
			Password: "password123",
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/authentication/token", bytes.NewBuffer(body))

		userID := uuid.New()
		user := &models.User{
			ID:    userID,
			Email: "test@example.com",
		}
		user.Password.Set("password123")

		// Mocks
		app.Store.Users.(*storeMocks.UserStore).On("GetByEmail", mock.Anything, "test@example.com").Return(user, nil).Once()
		app.Auth.(*authMocks.Authenticator).On("GenerateToken", mock.Anything).Return("access-token", nil).Once()
		app.Store.RefreshTokens.(*storeMocks.RefreshTokenStore).On("Create", mock.Anything, mock.Anything).Return(nil).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusOK, rr)

		var resp struct {
			Data users.LoginResponse `json:"data"`
		}
		json.Unmarshal(rr.Body.Bytes(), &resp)
		assert.Equal(t, "access-token", resp.Data.AccessToken)
		assert.NotEmpty(t, resp.Data.RefreshToken)

		app.Store.Users.(*storeMocks.UserStore).AssertExpectations(t)
		app.Store.RefreshTokens.(*storeMocks.RefreshTokenStore).AssertExpectations(t)
	})

	t.Run("should fail on invalid credentials", func(t *testing.T) {
		app := newTestApplication(t, configModels.Config{})
		mux := app.Mount()

		payload := users.CreateUserTokenPayload{
			Email:    "test@example.com",
			Password: "wrong-password",
		}

		body, _ := json.Marshal(payload)
		req, _ := http.NewRequest(http.MethodPost, "/v1/authentication/token", bytes.NewBuffer(body))

		user := &models.User{
			Email: "test@example.com",
		}
		user.Password.Set("password123")

		app.Store.Users.(*storeMocks.UserStore).On("GetByEmail", mock.Anything, "test@example.com").Return(user, nil).Once()

		rr := executeRequest(req, mux)
		checkResponseCode(t, http.StatusUnauthorized, rr)
	})
}
