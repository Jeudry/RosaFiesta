package integration

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"Backend/internal/api/handlers"
	"Backend/internal/api/middleware"
	"Backend/internal/api/router"
	"Backend/internal/app"
	"Backend/internal/config"
	"Backend/internal/dtos"
	"Backend/internal/services/mocks"
	"Backend/internal/store/models"
	"Backend/internal/utils"

	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"go.uber.org/zap"
)

func TestLoginFlow(t *testing.T) {
	// 1. Setup Controller & Mocks
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockAuthService := mocks.NewMockAuthServicer(ctrl)
	mockUserService := mocks.NewMockUserServicer(ctrl)
	mockPostService := mocks.NewMockPostServicer(ctrl)
	mockArticleService := mocks.NewMockArticleServicer(ctrl)
	mockCategoryService := mocks.NewMockCategoryServicer(ctrl)
	mockFeedService := mocks.NewMockFeedServicer(ctrl)

	// 2. Setup Dependencies
	logger := zap.NewNop().Sugar()
	responder := utils.NewResponder(logger)
	cfg := config.Config{} // Empty config for test
	appInstance := &app.Application{
		Config: cfg,
		Logger: logger,
	}

	h := handlers.NewHandler(
		appInstance,
		mockAuthService,
		mockUserService,
		mockPostService,
		mockArticleService,
		mockCategoryService,
		mockFeedService,
	)

	m := middleware.NewMiddleware(appInstance, responder)
	r := router.NewRouter(appInstance, h, m)

	// 3. Define Test Data
	loginPayload := dtos.CreateUserTokenPayload{
		Email:    "test@example.com",
		Password: "password123",
	}
	expectedToken := &models.UserToken{
		AccessToken:  "access-token",
		RefreshToken: "refresh-token",
	}

	// 4. Expect Service Call
	mockAuthService.EXPECT().
		Login(gomock.Any(), loginPayload.Email, loginPayload.Password).
		Return(expectedToken, nil).
		Times(1)

	// 5. Execute Request
	body, _ := json.Marshal(loginPayload)
	req, _ := http.NewRequest("POST", "/v1/authentication/token", bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")

	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	// 6. Assertions
	assert.Equal(t, http.StatusOK, w.Code)

	var response struct {
		Data models.UserToken `json:"data"`
	}
	err := json.Unmarshal(w.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, expectedToken.AccessToken, response.Data.AccessToken)
}
