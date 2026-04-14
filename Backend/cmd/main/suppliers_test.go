package main

import (
	"bytes"
	"encoding/json"
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

func TestGetSuppliers(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return user suppliers",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				supM := app.Store.Suppliers.(*storeMocks.SupplierStore)
				supM.On("GetByUserID", mock.Anything, userID).Return([]models.Supplier{
					{ID: uuid.New(), UserID: userID, Name: "Party Supplies Co"},
					{ID: uuid.New(), UserID: userID, Name: "Flower Shop"},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty list when no suppliers",
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				supM := app.Store.Suppliers.(*storeMocks.SupplierStore)
				supM.On("GetByUserID", mock.Anything, userID).Return([]models.Supplier{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/suppliers", nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestAddSupplier(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()

	tests := []struct {
		name         string
		payload      createSupplierPayload
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should add supplier successfully",
			payload: createSupplierPayload{
				Name:        "Party Supplies Co",
				ContactName: "John Doe",
				Email:       "john@partyco.com",
				Phone:       "+1234567890",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()

				supM := app.Store.Suppliers.(*storeMocks.SupplierStore)
				supM.On("Create", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for missing name",
			payload: createSupplierPayload{
				Name: "", // Missing required name
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name: "should return 400 for invalid email",
			payload: createSupplierPayload{
				Name:  "Party Supplies Co",
				Email: "not-an-email",
			},
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Once()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodPost, "/v1/suppliers", bytes.NewBuffer(body))
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetSupplier(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	supplierID := uuid.New()

	tests := []struct {
		name         string
		supplierID   string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:      "should return supplier by ID",
			supplierID: supplierID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				supM := app.Store.Suppliers.(*storeMocks.SupplierStore)
				supM.On("GetByID", mock.Anything, supplierID).Return(&models.Supplier{
					ID:     supplierID,
					UserID: userID,
					Name:   "Party Supplies Co",
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:      "should return 400 for invalid UUID",
			supplierID: "invalid-uuid",
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/suppliers/"+tc.supplierID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestDeleteSupplier(t *testing.T) {
	userID := uuid.New()
	userIDStr := userID.String()
	supplierID := uuid.New()

	tests := []struct {
		name         string
		supplierID   string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:      "should delete supplier successfully",
			supplierID: supplierID.String(),
			setupMocks: func(app *Application) {
				token := &jwt.Token{
					Claims: jwt.MapClaims{"sub": userIDStr},
					Valid:  true,
				}
				app.Auth.(*authMocks.Authenticator).On("ValidateToken", "valid-token").Return(token, nil).Maybe()

				storeM := app.Store.Users.(*storeMocks.UserStore)
				storeM.On("RetrieveById", mock.Anything, mock.Anything).Return(&models.User{ID: userID, Role: models.Role{Name: "buyer", Level: 1}}, nil).Maybe()

				supM := app.Store.Suppliers.(*storeMocks.SupplierStore)
				supM.On("GetByID", mock.Anything, supplierID).Return(&models.Supplier{
					ID:     supplierID,
					UserID: userID,
					Name:   "Party Supplies Co",
				}, nil).Once()
				supM.On("Delete", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusNoContent,
		},
		{
			name:      "should return 400 for invalid UUID",
			supplierID: "invalid-uuid",
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
			req, _ := http.NewRequest(http.MethodDelete, "/v1/suppliers/"+tc.supplierID, nil)
			req.Header.Set("Authorization", "Bearer valid-token")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}