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

func TestGetStats(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return admin stats",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, userID).Return(&models.User{ID: userID, Role: models.Role{Name: "admin", Level: 5}}, nil).Maybe()

				roleM := app.Store.Roles.(*storeMocks.RoleStore)
				roleM.On("RetrieveByName", mock.Anything, "admin").Return(&models.Role{Name: "admin", Level: 5}, nil).Maybe()

				statsM := app.Store.Stats.(*storeMocks.StatsStore)
				statsM.On("GetSummary", mock.Anything).Return(&models.AdminStats{
					TotalRevenue:   50000.0,
					TotalEvents:    50,
					RevenueByMonth: map[string]float64{"2026-01": 10000.0},
					EventsByStatus: map[string]int{"completed": 30},
				}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/admin/stats", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}