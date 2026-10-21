package app_test

import (
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	catalogpg "github.com/Jeudry/rosafiesta/server/internal/catalog/postgres"
	identitypg "github.com/Jeudry/rosafiesta/server/internal/identity/postgres"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/dbtest"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestPostgresIntegration_SeedsAndQueriesAllModules(t *testing.T) {
	pool := dbtest.Pool(t)
	ctx := t.Context()

	if err := app.SeedDatabase(ctx, pool, clock.System{}); err != nil {
		t.Fatalf("seed database: %v", err)
	}

	idRepo := identitypg.NewRepository(pool)
	userID, err := idRepo.Authenticate(ctx, "rf-token-maria")
	if err != nil {
		t.Fatalf("authenticate maria token in postgres: %v", err)
	}
	if userID.String() != "22222222-2222-2222-2222-222222222222" {
		t.Fatalf("unexpected user id: %s", userID)
	}

	catRepo := catalogpg.NewRepository(pool)
	articles, err := catRepo.ListArticles(ctx)
	if err != nil {
		t.Fatalf("list articles from postgres: %v", err)
	}
	if len(articles) != 6 {
		t.Fatalf("expected 6 canonical articles in postgres, got %d", len(articles))
	}

	var count int
	for _, table := range []string{"users", "sessions", "categories", "articles", "packages", "events", "quotes", "payments", "leads", "suppliers", "reviews"} {
		if err := pool.QueryRow(ctx, "SELECT count(*) FROM "+table).Scan(&count); err != nil {
			t.Fatalf("count table %s: %v", table, err)
		}
		if count == 0 {
			t.Fatalf("expected seeded rows in table %s, got 0", table)
		}
	}

	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{Pool: pool})...)
	req := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/events", nil)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/events with postgres pool, got %d", rec.Code)
	}
}

func TestPostgresIntegration_PersistsHttpMutations(t *testing.T) {
	pool := dbtest.Pool(t)
	ctx := t.Context()

	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{Pool: pool})...)

	dateReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/events/evt-boda-real/date-change",
		strings.NewReader(`{"newDateLabel":"Vie 30 oct","newDaysUntilEvent":12}`),
	)
	dateReq.Header.Set("Content-Type", "application/json")
	dateRec := httptest.NewRecorder()
	handler.ServeHTTP(dateRec, dateReq)
	if dateRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on date-change, got %d: %s", dateRec.Code, dateRec.Body.String())
	}

	var dateLabel string
	var changesCount int
	if err := pool.QueryRow(ctx, "SELECT event_date_label, date_changes_count FROM events WHERE id = 'evt-boda-real'").Scan(&dateLabel, &changesCount); err != nil {
		t.Fatalf("query updated event in postgres: %v", err)
	}
	if dateLabel != "Vie 30 oct" || changesCount != 1 {
		t.Fatalf("expected postgres event updated to Vie 30 oct (count=1), got %q (count=%d)", dateLabel, changesCount)
	}

	switchReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/auth/switch-view",
		strings.NewReader(`{"view":"personal"}`),
	)
	switchReq.Header.Set("Content-Type", "application/json")
	switchReq.Header.Set("Authorization", "Bearer rf-token-maria")
	switchRec := httptest.NewRecorder()
	handler.ServeHTTP(switchRec, switchReq)
	if switchRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on switch-view, got %d: %s", switchRec.Code, switchRec.Body.String())
	}

	var activeView string
	if err := pool.QueryRow(ctx, "SELECT active_view FROM sessions WHERE token_hash = 'rf-token-maria'").Scan(&activeView); err != nil {
		t.Fatalf("query updated session in postgres: %v", err)
	}
	if activeView != "personal" {
		t.Fatalf("expected postgres session active_view='personal', got %q", activeView)
	}
}
