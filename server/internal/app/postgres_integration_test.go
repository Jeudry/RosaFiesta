package app_test

import (
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	catalogpg "github.com/Jeudry/rosafiesta/server/internal/catalog/postgres"
	identitydomain "github.com/Jeudry/rosafiesta/server/internal/identity/domain"
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
		t.Fatalf("expected 6 canonical rental articles in postgres, got %d", len(articles))
	}
	for _, art := range articles {
		if art.AvailableStock <= 0 {
			t.Fatalf("expected available_stock > 0 in postgres for article %s, got %d", art.ID, art.AvailableStock)
		}
	}

	seededTables := []string{
		"users", "sessions", "admin_clients", "staff_members",
		"categories", "articles", "packages", "catalog_trends", "season_banners", "events", "quotes",
		"payments", "refunds", "leads", "suppliers", "reviews",
		"damage_incidents", "offline_queue", "business_settings",
		"delivery_zones", "notifications", "insurance_policies",
		"audit_logs", "bot_faqs", "bot_keywords", "bot_threads",
	}
	for _, table := range seededTables {
		var count int
		if err := pool.QueryRow(ctx, "SELECT count(*) FROM "+table).Scan(&count); err != nil {
			t.Fatalf("count table %s: %v", table, err)
		}
		if count == 0 {
			t.Fatalf("expected seeded rows in table %s, got 0", table)
		}
	}

	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{Pool: pool})...)
	req := httptest.NewRequestWithContext(ctx, http.MethodGet, "/v1/events", nil)
	req.Header.Set("Authorization", "Bearer rf-token-maria")
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 on /v1/events with postgres pool, got %d: %s", rec.Code, rec.Body.String())
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
	dateReq.Header.Set("Authorization", "Bearer rf-token-camila")
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
	if dateLabel != "Vie 30 oct" {
		t.Fatalf("expected postgres event updated to Vie 30 oct, got %q", dateLabel)
	}
	if changesCount != 1 {
		t.Fatalf("expected date_changes_count=1, got %d", changesCount)
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

	mariaHash := identitydomain.HashToken("rf-token-maria")
	var activeView string
	if err := pool.QueryRow(ctx, "SELECT active_view FROM sessions WHERE token_hash = $1", mariaHash).Scan(&activeView); err != nil {
		t.Fatalf("query updated session in postgres: %v", err)
	}
	if activeView != "personal" {
		t.Fatalf("expected postgres session active_view='personal', got %q", activeView)
	}
}

func TestPostgresIntegration_CatalogAndChecklistPersistence(t *testing.T) {
	pool := dbtest.Pool(t)
	ctx := t.Context()

	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{Pool: pool})...)

	for _, path := range []string{"/v1/catalog/articles", "/v1/catalog/categories", "/v1/catalog/packages"} {
		req := httptest.NewRequestWithContext(ctx, http.MethodGet, path, nil)
		rec := httptest.NewRecorder()
		handler.ServeHTTP(rec, req)
		if rec.Code != http.StatusOK {
			t.Fatalf("expected 200 on %s, got %d: %s", path, rec.Code, rec.Body.String())
		}
	}

	chkReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPatch,
		"/v1/operations/events/evt-boda-real/checklist/chk-5",
		strings.NewReader(`{"completed":true,"staffName":"Pedro Santos"}`),
	)
	chkReq.Header.Set("Content-Type", "application/json")
	chkReq.Header.Set("Authorization", "Bearer rf-token-maria")
	chkRec := httptest.NewRecorder()
	handler.ServeHTTP(chkRec, chkReq)
	if chkRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on checklist patch, got %d: %s", chkRec.Code, chkRec.Body.String())
	}

	var chkCompleted bool
	if err := pool.QueryRow(ctx, "SELECT completed FROM event_tasks WHERE id = 'chk-5'").Scan(&chkCompleted); err != nil {
		t.Fatalf("query event_tasks chk-5: %v", err)
	}
	if !chkCompleted {
		t.Fatal("expected event_tasks chk-5 completed=true in postgres")
	}
}

func TestPostgresIntegration_DamagesSeparatedFromReviewsAndOfflineSync(t *testing.T) {
	pool := dbtest.Pool(t)
	ctx := t.Context()

	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{Pool: pool})...)

	var reviewsBefore int
	if err := pool.QueryRow(ctx, "SELECT count(*) FROM reviews").Scan(&reviewsBefore); err != nil {
		t.Fatalf("count reviews before damage report: %v", err)
	}
	var damagesBefore int
	if err := pool.QueryRow(ctx, "SELECT count(*) FROM damage_incidents").Scan(&damagesBefore); err != nil {
		t.Fatalf("count damage_incidents before damage report: %v", err)
	}

	dmgReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/operations/events/evt-boda-real/damages",
		strings.NewReader(`{"articleCode":"RF-001","articleName":"Silla Tiffany dorada","damagedUnits":2,"missingUnits":0,"replacementRD":180,"notes":"Cojín manchado en desmontaje"}`),
	)
	dmgReq.Header.Set("Content-Type", "application/json")
	dmgReq.Header.Set("Authorization", "Bearer rf-token-maria")
	dmgRec := httptest.NewRecorder()
	handler.ServeHTTP(dmgRec, dmgReq)
	if dmgRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on damage report, got %d: %s", dmgRec.Code, dmgRec.Body.String())
	}

	var reviewsAfter int
	if err := pool.QueryRow(ctx, "SELECT count(*) FROM reviews").Scan(&reviewsAfter); err != nil {
		t.Fatalf("count reviews after damage report: %v", err)
	}
	var damagesAfter int
	if err := pool.QueryRow(ctx, "SELECT count(*) FROM damage_incidents").Scan(&damagesAfter); err != nil {
		t.Fatalf("count damage_incidents after damage report: %v", err)
	}
	if reviewsAfter != reviewsBefore {
		t.Fatalf("damage incident polluted reviews table: before=%d, after=%d", reviewsBefore, reviewsAfter)
	}
	if damagesAfter != damagesBefore+1 {
		t.Fatalf("expected damage_incidents count=%d, got %d", damagesBefore+1, damagesAfter)
	}

	syncReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/operations/offline-queue/sync", nil)
	syncReq.Header.Set("Authorization", "Bearer rf-token-maria")
	syncRec := httptest.NewRecorder()
	handler.ServeHTTP(syncRec, syncReq)
	if syncRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on offline-queue sync, got %d: %s", syncRec.Code, syncRec.Body.String())
	}
	var unsyncedCount int
	if err := pool.QueryRow(ctx, "SELECT count(*) FROM offline_queue WHERE synced = FALSE").Scan(&unsyncedCount); err != nil {
		t.Fatalf("count unsynced offline_queue rows: %v", err)
	}
	if unsyncedCount != 0 {
		t.Fatalf("expected 0 unsynced rows in offline_queue after sync, got %d", unsyncedCount)
	}
}

func TestPostgresIntegration_QuotesAndPaymentsPersistence(t *testing.T) {
	pool := dbtest.Pool(t)
	ctx := t.Context()

	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{Pool: pool})...)

	sendReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/quotes/quo-cumple-mateo/send", strings.NewReader(`{}`))
	sendReq.Header.Set("Content-Type", "application/json")
	sendReq.Header.Set("Authorization", "Bearer rf-token-maria")
	sendRec := httptest.NewRecorder()
	handler.ServeHTTP(sendRec, sendReq)
	if sendRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on quote send, got %d: %s", sendRec.Code, sendRec.Body.String())
	}

	signReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/quotes/quo-cumple-mateo/sign",
		strings.NewReader(`{"signedByName":"Laura Peña","signatureSvg":"<svg><path d='M0 0'/></svg>"}`),
	)
	signReq.Header.Set("Content-Type", "application/json")
	signReq.Header.Set("Authorization", "Bearer rf-token-maria")
	signRec := httptest.NewRecorder()
	handler.ServeHTTP(signRec, signReq)
	if signRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on quote sign, got %d: %s", signRec.Code, signRec.Body.String())
	}

	var quoteStatus string
	if err := pool.QueryRow(ctx, "SELECT status FROM quotes WHERE id = 'quo-cumple-mateo'").Scan(&quoteStatus); err != nil {
		t.Fatalf("query quote status in postgres: %v", err)
	}
	if quoteStatus != "signed" {
		t.Fatalf("expected quote status 'signed' in postgres, got %q", quoteStatus)
	}

	payReq := httptest.NewRequestWithContext(
		ctx,
		http.MethodPost,
		"/v1/payments",
		strings.NewReader(`{"eventId":"evt-boda-real","eventTitle":"Boda Real Rosa & Dorado","clientName":"Camila Rodríguez","amountRD":1500,"method":"Banco Popular","kind":"saldo","referenceCode":"POP-2026-900"}`),
	)
	payReq.Header.Set("Content-Type", "application/json")
	payReq.Header.Set("Authorization", "Bearer rf-token-camila")
	payRec := httptest.NewRecorder()
	handler.ServeHTTP(payRec, payReq)
	if payRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on payment register, got %d: %s", payRec.Code, payRec.Body.String())
	}

	var paidAmount int
	var bankName string
	if err := pool.QueryRow(ctx, "SELECT amount_dop, bank_name FROM payments WHERE idempotency_key = 'POP-2026-900'").Scan(&paidAmount, &bankName); err != nil {
		t.Fatalf("query payment in postgres: %v", err)
	}
	if paidAmount != 1500 {
		t.Fatalf("expected payment amount 1500 in postgres, got %d", paidAmount)
	}
	if bankName != "Banco Popular" {
		t.Fatalf("expected bank_name 'Banco Popular' in postgres, got %q", bankName)
	}
}
