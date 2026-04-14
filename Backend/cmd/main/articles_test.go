package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"testing"

	"Backend/cmd/main/configModels"
	"Backend/cmd/main/view_models/products"
	storeMocks "Backend/internal/store/mocks"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

func ptrFloat64(f float64) *float64 {
	return &f
}

func TestCreateArticle(t *testing.T) {
	cfg := configModels.Config{
		Auth: configModels.AuthConfig{
			ApiKey: configModels.ApiKeyConfig{
				Header: "X-Api-Key",
				Value:  "test-api-key",
			},
		},
	}

	tests := []struct {
		name         string
		payload      products.CreateProductPayload
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should create article successfully",
			payload: products.CreateProductPayload{
				NameTemplate:        "Round Table",
				DescriptionTemplate: ptrStr("A classic round table"),
				Type:                models.ArticleTypeRental,
				IsActive:            true,
				Variants: []products.CreateArticleVariantPayload{
					{
						Sku:         "RT-001",
						Name:        "Round Table Variant 1",
						Description: ptrStr("Classic wood"),
						IsActive:    true,
						Stock:       10,
						RentalPrice: 25.00,
					},
				},
			},
			setupMocks: func(app *Application) {
				artM := app.Store.Articles.(*storeMocks.ArticlesStore)
				artM.On("Create", mock.Anything, mock.Anything).Return(nil).Once()
			},
			expectedCode: http.StatusCreated,
		},
		{
			name: "should return 400 for invalid payload - missing name",
			payload: products.CreateProductPayload{
				NameTemplate: "", // Missing name
				Type:         models.ArticleTypeRental,
				Variants: []products.CreateArticleVariantPayload{
					{
						Sku:         "RT-001",
						Name:        "Round Table Variant 1",
						IsActive:    true,
						Stock:       10,
						RentalPrice: 25.00,
					},
				},
			},
			setupMocks: func(app *Application) {
				// No mocks needed for validation failure
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name: "should return 400 for invalid payload - missing variants",
			payload: products.CreateProductPayload{
				NameTemplate: "Round Table",
				Type:         models.ArticleTypeRental,
				Variants:     []products.CreateArticleVariantPayload{}, // Empty variants
			},
			setupMocks: func(app *Application) {
				// No mocks needed for validation failure
			},
			expectedCode: http.StatusBadRequest,
		},
		{
			name: "should return 400 for invalid payload - missing type",
			payload: products.CreateProductPayload{
				NameTemplate: "Round Table",
				Variants: []products.CreateArticleVariantPayload{
					{
						Sku:         "RT-001",
						Name:        "Round Table Variant 1",
						IsActive:    true,
						Stock:       10,
						RentalPrice: 25.00,
					},
				},
			},
			setupMocks: func(app *Application) {
				// No mocks needed for validation failure
			},
			expectedCode: http.StatusBadRequest,
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			app := newTestApplication(t, cfg)
			if tc.setupMocks != nil {
				tc.setupMocks(app)
			}

			mux := app.Mount()
			body, _ := json.Marshal(tc.payload)
			req, _ := http.NewRequest(http.MethodPost, "/v1/articles", bytes.NewBuffer(body))
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetAllArticles(t *testing.T) {
	tests := []struct {
		name         string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name: "should return all articles",
			setupMocks: func(app *Application) {
				artM := app.Store.Articles.(*storeMocks.ArticlesStore)
				artM.On("GetAll", mock.Anything, 10, 0).Return([]models.Article{
					{BaseModel: models.BaseModel{ID: uuid.New()}, NameTemplate: "Round Table", Type: models.ArticleTypeRental},
					{BaseModel: models.BaseModel{ID: uuid.New()}, NameTemplate: "Chairs", Type: models.ArticleTypeRental},
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name: "should return empty list when no articles",
			setupMocks: func(app *Application) {
				artM := app.Store.Articles.(*storeMocks.ArticlesStore)
				artM.On("GetAll", mock.Anything, 10, 0).Return([]models.Article{}, nil).Once()
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/articles", nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func TestGetArticle(t *testing.T) {
	articleID := uuid.New()

	tests := []struct {
		name         string
		articleID    string
		setupMocks   func(*Application)
		expectedCode int
	}{
		{
			name:      "should return article by ID",
			articleID: articleID.String(),
			setupMocks: func(app *Application) {
				artM := app.Store.Articles.(*storeMocks.ArticlesStore)
				artM.On("GetById", mock.Anything, articleID).Return(&models.Article{
					BaseModel:    models.BaseModel{ID: articleID},
					NameTemplate: "Round Table",
					Type:        models.ArticleTypeRental,
				}, nil).Once()
			},
			expectedCode: http.StatusOK,
		},
		{
			name:      "should return 400 for invalid UUID",
			articleID: "invalid-uuid",
			setupMocks: func(app *Application) {},
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
			req, _ := http.NewRequest(http.MethodGet, "/v1/articles/"+tc.articleID, nil)
			req.Header.Set("X-Api-Key", "test-api-key")

			rr := executeRequest(req, mux)
			checkResponseCode(t, tc.expectedCode, rr)
		})
	}
}

func ptrBool(b bool) *bool {
	return &b
}