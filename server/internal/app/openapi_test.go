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

	req := httptest.NewRequestWithContext(ctx, http.MethodGet, "/healthz", nil)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /healthz, got %d", rec.Code)
	}

	req = httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/catalog/articles", nil)
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/catalog/articles, got %d: %s", rec.Code, rec.Body.String())
	}

	req = httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/events", nil)
	req.Header.Set("Authorization", "Bearer rf-token-maria")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/events, got %d", rec.Code)
	}

	body := `{"eventType":"Boda","guests":120,"budgetRD":4500}`
	req = httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/quotes/rosa-proposal", strings.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/quotes/rosa-proposal, got %d: %s", rec.Code, rec.Body.String())
	}

	req = httptest.NewRequestWithContext(ctx, http.MethodDelete, "/v1/account", nil)
	req.Header.Set("Authorization", "Bearer rf-token-camila")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusConflict {
		t.Fatalf("expected 409 Conflict deleting account with active events, got %d: %s", rec.Code, rec.Body.String())
	}

	req = httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/payments/summary", nil)
	req.Header.Set("Authorization", "Bearer rf-token-maria")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/payments/summary, got %d", rec.Code)
	}
	var financeResp struct {
		Summary struct {
			OctRevenueRD       int `json:"octRevenueRD"`
			AccountsReceivable int `json:"accountsReceivableRD"`
		} `json:"summary"`
	}
	if err := json.Unmarshal(rec.Body.Bytes(), &financeResp); err != nil {
		t.Fatalf("decode finance summary: %v", err)
	}
	if financeResp.Summary.OctRevenueRD != 12450 {
		t.Fatalf("expected OctRevenueRD=12450, got %+v", financeResp.Summary)
	}
	if financeResp.Summary.AccountsReceivable != 7724 {
		t.Fatalf("expected AccountsReceivable=7724, got %+v", financeResp.Summary)
	}
}
