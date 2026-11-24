package app_test

import (
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

type fixedClock struct {
	now time.Time
}

func (f fixedClock) Now() time.Time {
	return f.now
}

func TestService_ChecklistTimestampAndDamageSeparation(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.October, 23, 14, 45, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	reviewsBefore := len(svc.ListReviews(ctx, ""))
	logi, err := svc.ToggleChecklistItem(ctx, "evt-boda-real", "chk-5", true, "Pedro Santos")
	if err != nil {
		t.Fatalf("ToggleChecklistItem failed: %v", err)
	}
	foundChk5 := false
	for _, item := range logi.Checklist {
		if item.ID == "chk-5" {
			foundChk5 = true
			if item.CompletedAt != "02:45 PM" {
				t.Fatalf("expected CompletedAt '02:45 PM' from injected clock, got %q", item.CompletedAt)
			}
		}
	}
	if !foundChk5 {
		t.Fatal("expected chk-5 in checklist")
	}

	dmg, err := svc.ReportDamage(ctx, "evt-boda-real", "RF-001", "Silla Tiffany dorada", 2, 0, 80, "Respaldo rayado")
	if err != nil {
		t.Fatalf("ReportDamage failed: %v", err)
	}
	if !strings.HasPrefix(dmg.ID, "dmg-") {
		t.Fatalf("unexpected damage id: %s", dmg.ID)
	}
	if len(svc.ListReviews(ctx, "")) != reviewsBefore {
		t.Fatalf("expected reviews count unchanged (%d), got %d", reviewsBefore, len(svc.ListReviews(ctx, "")))
	}
	if len(svc.ListDamages(ctx, "evt-boda-real")) != 2 {
		t.Fatalf("expected 2 damage incidents for evt-boda-real, got %d", len(svc.ListDamages(ctx, "evt-boda-real")))
	}
}

func TestService_PublicSettingsAndReportExports(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()

	pub := svc.GetPublicSettings(ctx)
	if pub.Address != "Av. Winston Churchill 1099, Piantini, Santo Domingo" {
		t.Fatalf("unexpected public address: %q", pub.Address)
	}
	if pub.RNC != "1-31-84920-2" {
		t.Fatalf("unexpected RNC: %q", pub.RNC)
	}

	report6m, err := svc.ExportReport(ctx, "income", "6m")
	if err != nil {
		t.Fatalf("ExportReport 6m failed: %v", err)
	}
	if len(report6m.Series) != 6 {
		t.Fatalf("expected 6 points for 6m, got %d", len(report6m.Series))
	}

	report12m, err := svc.ExportReport(ctx, "dgii", "12m")
	if err != nil {
		t.Fatalf("ExportReport dgii 12m failed: %v", err)
	}
	if len(report12m.Series) != 12 {
		t.Fatalf("expected 12 points for 12m, got %d", len(report12m.Series))
	}
	if !strings.Contains(report12m.CSV, "ITBIS_18_DOP") {
		t.Fatalf("expected CSV header with ITBIS_18_DOP, got %q", report12m.CSV)
	}
}

type fakeStockAdjuster struct {
	deltas map[string]int
}

func (f *fakeStockAdjuster) AdjustDamagedStock(_ context.Context, articleRef string, delta int) error {
	if f.deltas == nil {
		f.deltas = make(map[string]int)
	}
	f.deltas[articleRef] += delta
	return nil
}

type fakeChargeRecorder struct {
	charges []domain.DamageReport
}

func (f *fakeChargeRecorder) RecordDamageCharge(_ context.Context, report domain.DamageReport) (string, error) {
	f.charges = append(f.charges, report)
	return "pay-dmg-001", nil
}

type fakeOfflineDispatcher struct {
	dispatched []string
}

func (f *fakeOfflineDispatcher) DispatchMutation(_ context.Context, opType, _ string, _ platformhttp.Principal) error {
	f.dispatched = append(f.dispatched, opType)
	return nil
}

type fakeOpsPublisher struct {
	events []realtimedomain.RealtimeEvent
}

func (f *fakeOpsPublisher) Publish(event realtimedomain.RealtimeEvent) realtimedomain.RealtimeEvent {
	f.events = append(f.events, event)
	return event
}

func TestService_ConfirmedEventLogisticsAndDamageLifecycle(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	stock := &fakeStockAdjuster{}
	charges := &fakeChargeRecorder{}
	pub := &fakeOpsPublisher{}
	svc := app.NewService().
		WithDamagedStockAdjuster(stock).
		WithReplacementChargeRecorder(charges).
		WithRealtimePublisher(pub)
	svc.ResetEmpty(ctx)

	if err := svc.EnsureLogisticsForConfirmedEvent(ctx, "evt-new", "Boda Nueva", "Salón Azul", "Piantini", "15 dic"); err != nil {
		t.Fatalf("EnsureLogisticsForConfirmedEvent failed: %v", err)
	}
	if err := svc.EnsureLogisticsForConfirmedEvent(ctx, "evt-new", "Boda Nueva", "Salón Azul", "Piantini", "15 dic"); err != nil {
		t.Fatalf("idempotent EnsureLogisticsForConfirmedEvent failed: %v", err)
	}
	if len(svc.ListRouteStops()) != 2 {
		t.Fatalf("expected exactly 2 route stops after idempotent call, got %d", len(svc.ListRouteStops()))
	}

	report, err := svc.ReportDamageWithDetails(ctx, app.ReportDamageCommand{
		EventID:       "evt-new",
		ArticleCode:   "RF-010",
		ArticleName:   "Copa Cristal",
		DamagedUnits:  3,
		MissingUnits:  1,
		ReplacementRD: 1200,
		Notes:         "Rotura en recogida",
		PhotoURL:      "https://cdn.rosafiesta.do/dmg.jpg",
	})
	if err != nil {
		t.Fatalf("ReportDamageWithDetails failed: %v", err)
	}
	if stock.deltas["RF-010"] != 4 {
		t.Fatalf("expected stock delta 4, got %d", stock.deltas["RF-010"])
	}
	if report.ChargePaymentID != "pay-dmg-001" {
		t.Fatalf("expected charge pay-dmg-001, got %q", report.ChargePaymentID)
	}

	resolved, resErr := svc.ResolveDamage(ctx, report.ID, "Reparado en taller")
	if resErr != nil {
		t.Fatalf("ResolveDamage failed: %v", resErr)
	}
	if resolved.Status != "resuelto" {
		t.Fatalf("expected resuelto, got %q", resolved.Status)
	}
	if stock.deltas["RF-010"] != 1 {
		t.Fatalf("expected net damaged stock 1 (missing only), got %d", stock.deltas["RF-010"])
	}
}

func TestService_OfflineIdempotencyDispatchAndWhatsApp(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	dispatcher := &fakeOfflineDispatcher{}
	pub := &fakeOpsPublisher{}
	svc := app.NewService().WithOfflineDispatcher(dispatcher).WithRealtimePublisher(pub)
	svc.ResetEmpty(ctx)

	cmd := app.EnqueueOfflineMutationCommand{
		ActionLabel:    "Checklist montaje",
		EntityCode:     "evt-1",
		OperationType:  "toggle_checklist",
		PayloadJSON:    `{"itemId":"chk-1"}`,
		IdempotencyKey: "idem-offline-1",
	}
	_, _ = svc.EnqueueOfflineMutation(ctx, cmd)
	_, _ = svc.EnqueueOfflineMutation(ctx, cmd)
	if len(svc.ListOfflineQueue(ctx)) != 1 {
		t.Fatalf("expected idempotent offline enqueue size 1, got %d", len(svc.ListOfflineQueue(ctx)))
	}
	synced, err := svc.SyncOfflineMutations(ctx, nil, platformhttp.Principal{Role: "admin"})
	if err != nil {
		t.Fatalf("SyncOfflineMutations failed: %v", err)
	}
	if len(dispatcher.dispatched) != 1 {
		t.Fatalf("expected 1 dispatched mutation, got %d", len(dispatcher.dispatched))
	}
	if !synced[0].Synced {
		t.Fatalf("expected mutation marked synced")
	}

	if _, waErr := svc.SendWhatsAppBotReply(ctx, "", "+18095550199", "Hola"); !errors.Is(waErr, domain.ErrWhatsAppNotConfigured) {
		t.Fatalf("expected ErrWhatsAppNotConfigured, got %v", waErr)
	}

	var gotBearer string
	mockWA := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotBearer = r.Header.Get("Authorization")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"messages":[{"id":"wamid.1"}]}`))
	}))
	defer mockWA.Close()

	svc.WithWhatsAppConfig("123456789", "wa-secret-tok", "verify-tok").WithWhatsAppAPIBaseURL(mockWA.URL)
	th, sendErr := svc.SendWhatsAppBotReply(ctx, "", "+18095550199", "Confirmamos entrega")
	if sendErr != nil {
		t.Fatalf("SendWhatsAppBotReply failed: %v", sendErr)
	}
	if gotBearer != "Bearer wa-secret-tok" {
		t.Fatalf("expected Bearer token header, got %q", gotBearer)
	}
	if th.LastMessage != "Confirmamos entrega" {
		t.Fatalf("unexpected LastMessage: %q", th.LastMessage)
	}
	if err := svc.IngestWhatsAppWebhook(ctx, []byte(`{"entry":[{"changes":[{"value":{"contacts":[{"wa_id":"18095550199","profile":{"name":"Laura"}}],"messages":[{"from":"18095550199","text":{"body":"Perfecto, gracias"}}]}}]}]}`)); err != nil {
		t.Fatalf("IngestWhatsAppWebhook failed: %v", err)
	}
}

func TestService_WhatsAppBotAutoReplyAndNotificationsOwnership(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()

	var outboundCount int
	mockWA := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		outboundCount++
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"messages":[{"id":"wamid.auto"}]}`))
	}))
	defer mockWA.Close()

	svc.WithWhatsAppConfig("123456", "wa-token", "verify-tok").WithWhatsAppAPIBaseURL(mockWA.URL)
	if err := svc.IngestWhatsAppWebhook(ctx, []byte(`{"from":"8095554321","name":"Sofía","message":"Quiero cotizar un cumple"}`)); err != nil {
		t.Fatalf("auto-reply webhook failed: %v", err)
	}
	if outboundCount != 1 {
		t.Fatalf("expected 1 automatic WhatsApp reply for keyword 'cotizar', got %d", outboundCount)
	}

	otherCaller := svc.CreateNotification(ctx, "Pago confirmado", "RD$5000", "pagos", "client", "11111111-1111-1111-1111-111111111111")
	if _, err := svc.MarkNotificationReadForCaller(ctx, otherCaller.ID, platformhttpPrincipalForTest()); !errors.Is(err, domain.ErrNotificationForbidden) {
		t.Fatalf("expected ErrNotificationForbidden when another client marks notification read, got %v", err)
	}
}

func platformhttpPrincipalForTest() platformhttp.Principal {
	return platformhttp.Principal{ID: [16]byte{9}, Role: "client"}
}
