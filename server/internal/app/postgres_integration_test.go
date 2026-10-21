package app_test

import (
	"log/slog"
	"net/http"
	"net/http/httptest"
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
