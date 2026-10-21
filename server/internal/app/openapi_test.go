package app_test

import (
	"bytes"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// TestOpenAPISpecIsUpToDate falla si api/openapi.json no coincide con el código.
// El cliente de la app se genera desde ese archivo, así que nunca debe quedar desactualizado.
func TestOpenAPISpecIsUpToDate(t *testing.T) {
	want, err := app.OpenAPISpec()
	if err != nil {
		t.Fatal(err)
	}

	path := filepath.Join("..", "..", "..", "api", "openapi.json")
	got, err := os.ReadFile(path)
	if err != nil {
		t.Fatalf("read %s: %v (run `make openapi`)", path, err)
	}

	if !bytes.Equal(got, want) {
		t.Fatalf("%s is out of date; run `make openapi`", path)
	}
}

func TestModularMonolithEndpoints_EndToEnd(t *testing.T) {
	t.Parallel()

	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{})...)

	// 1. Health check
	req := httptest.NewRequestWithContext(ctx, http.MethodGet, "/healthz", nil)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("esperaba 200 en /healthz, obtuvo %d", rec.Code)
	}

	// 2. Catalog articles
	req = httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/catalog/articles", nil)
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("esperaba 200 en /v1/catalog/articles, obtuvo %d: %s", rec.Code, rec.Body.String())
	}

	// 3. Events list
	req = httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/events", nil)
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("esperaba 200 en /v1/events, obtuvo %d", rec.Code)
	}

	// 4. Rosa IA proposal
	body := `{"eventType":"Boda","guests":120,"budgetRD":4500}`
	req = httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/quotes/rosa-proposal", strings.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("esperaba 200 en /v1/quotes/rosa-proposal, obtuvo %d: %s", rec.Code, rec.Body.String())
	}

	// 5. Account deletion blocked when active events exist (s88)
	req = httptest.NewRequestWithContext(ctx, http.MethodDelete, "/v1/account", nil)
	req.Header.Set("Authorization", "Bearer rf-token-camila")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusConflict {
		t.Fatalf("esperaba 409 Conflict al borrar cuenta con eventos activos (s88), obtuvo %d: %s", rec.Code, rec.Body.String())
	}

	// 6. Finance summary matches Section 6
	req = httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/payments/summary", nil)
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("esperaba 200 en /v1/payments/summary, obtuvo %d", rec.Code)
	}
	var financeResp struct {
		Summary struct {
			OctRevenueRD       int `json:"octRevenueRD"`
			AccountsReceivable int `json:"accountsReceivableRD"`
		} `json:"summary"`
	}
	if err := json.Unmarshal(rec.Body.Bytes(), &financeResp); err != nil {
		t.Fatalf("error decodificando resumen financiero: %v", err)
	}
	if financeResp.Summary.OctRevenueRD != 12450 || financeResp.Summary.AccountsReceivable != 7724 {
		t.Fatalf("resumen financiero inesperado: %+v", financeResp.Summary)
	}
}
