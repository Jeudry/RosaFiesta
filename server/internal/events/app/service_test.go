package app_test

import (
	"context"
	"errors"
	"testing"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/events/app"
	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

type fakeStockManager struct {
	reserveErr      error
	transitionErr   error
	reservedCalls   []reserveCall
	transitionCalls []transitionCall
}

type reserveCall struct {
	eventID string
	quoteID string
	dateISO string
	status  string
	items   []domain.EventItem
}

type transitionCall struct {
	eventID string
	status  string
}

func (f *fakeStockManager) ReserveEventStock(
	_ context.Context,
	eventID, quoteID, dateOrISO, status string,
	items []domain.EventItem,
) error {
	f.reservedCalls = append(f.reservedCalls, reserveCall{
		eventID: eventID,
		quoteID: quoteID,
		dateISO: dateOrISO,
		status:  status,
		items:   items,
	})
	return f.reserveErr
}

func (f *fakeStockManager) TransitionEventStock(_ context.Context, eventID, status string) error {
	f.transitionCalls = append(f.transitionCalls, transitionCall{eventID: eventID, status: status})
	return f.transitionErr
}

type fakeRealtimePublisher struct {
	events []realtimedomain.RealtimeEvent
}

func (f *fakeRealtimePublisher) Publish(event realtimedomain.RealtimeEvent) realtimedomain.RealtimeEvent {
	f.events = append(f.events, event)
	return event
}

func TestCreateEventForCallerRejectsOverbookedStockBeforeSaving(t *testing.T) {
	stock := &fakeStockManager{reserveErr: errors.New("solo hay 10 sillas disponibles")}
	svc := app.NewService().WithStockManager(stock)
	svc.ResetEmpty(context.Background())

	caller := platformhttp.Principal{
		ID:       uuid.New(),
		Email:    "cliente@rosafiesta.do",
		FullName: "Cliente Prueba",
		Role:     "client",
	}
	cmd := app.CreateEventCommand{
		Title:        "Boda Sobrecomprada",
		DateLabel:    "2026-12-10",
		EventDateISO: "2026-12-10",
		Venue:        "Jardín Central",
		Guests:       80,
		Items: []domain.EventItem{
			{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 150, UnitPriceRD: 4},
		},
	}

	_, err := svc.CreateEventForCaller(context.Background(), cmd, caller)
	if !errors.Is(err, domain.ErrInsufficientStock) {
		t.Fatalf("expected ErrInsufficientStock, got %v", err)
	}
	if len(stock.reservedCalls) != 1 {
		t.Fatalf("expected 1 reserve call, got %d", len(stock.reservedCalls))
	}
	if stock.reservedCalls[0].status != "held" {
		t.Fatalf("expected held reservation status, got %q", stock.reservedCalls[0].status)
	}
	snapshot := svc.AllEventsSnapshot(context.Background())
	if len(snapshot) != 0 {
		t.Fatalf("expected overbooked event not to be persisted, got %d events", len(snapshot))
	}
}

func TestProvisionFromSignedQuoteCreatesAndReusesEvent(t *testing.T) {
	stock := &fakeStockManager{}
	pub := &fakeRealtimePublisher{}
	svc := app.NewService().WithStockManager(stock).WithRealtimePublisher(pub)
	svc.ResetEmpty(context.Background())

	clientID := uuid.New()
	cmd := app.ProvisionFromQuoteCommand{
		QuoteID:      "quo-1001",
		ClientID:     clientID,
		ClientEmail:  "camila@rosafiesta.do",
		ClientName:   "Camila Rodríguez",
		Title:        "Boda Primavera",
		EventType:    "Boda",
		DateLabel:    "20 Nov 2026",
		EventDateISO: "2026-11-20",
		Venue:        "Salón Real",
		Guests:       100,
		TotalRD:      5000,
		Items: []domain.EventItem{
			{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 100, UnitPriceRD: 4},
		},
	}

	created, err := svc.ProvisionFromSignedQuote(context.Background(), cmd)
	if err != nil {
		t.Fatalf("unexpected provision error: %v", err)
	}
	if created.Status != domain.StatusPending {
		t.Fatalf("expected pending status, got %q", created.Status)
	}
	if created.PickupDateISO != "2026-11-19" {
		t.Fatalf("unexpected pickup date: %s", created.PickupDateISO)
	}
	if created.ReturnDateISO != "2026-11-21" {
		t.Fatalf("unexpected return date: %s", created.ReturnDateISO)
	}
	if len(stock.reservedCalls) != 1 {
		t.Fatalf("expected 1 reserve call, got %d", len(stock.reservedCalls))
	}
	if stock.reservedCalls[0].quoteID != "quo-1001" {
		t.Fatalf("expected held stock reservation linked to quo-1001, got %+v", stock.reservedCalls)
	}
	if len(pub.events) != 1 {
		t.Fatalf("expected 1 SSE event, got %d", len(pub.events))
	}
	if pub.events[0].Type != realtimedomain.EventEventUpdated {
		t.Fatalf("expected event.updated SSE, got %+v", pub.events)
	}

	cmd.Venue = "Salón Actualizado"
	cmd.EventID = ""
	reused, err := svc.ProvisionFromSignedQuote(context.Background(), cmd)
	if err != nil {
		t.Fatalf("unexpected reprovision error: %v", err)
	}
	if reused.ID != created.ID {
		t.Fatalf("expected reuse of event %s, got %s", created.ID, reused.ID)
	}
	if reused.Venue != "Salón Actualizado" {
		t.Fatalf("expected updated venue, got %q", reused.Venue)
	}
}

func TestApplyVerifiedPaymentConfirmsEventAtDepositThreshold(t *testing.T) {
	stock := &fakeStockManager{}
	pub := &fakeRealtimePublisher{}
	svc := app.NewService().WithStockManager(stock).WithRealtimePublisher(pub)
	svc.ResetEmpty(context.Background())

	created, err := svc.ProvisionFromSignedQuote(context.Background(), app.ProvisionFromQuoteCommand{
		QuoteID:      "quo-pay-1",
		ClientID:     uuid.New(),
		ClientEmail:  "pay@rosafiesta.do",
		ClientName:   "Carlos Pérez",
		Title:        "Aniversario Oro",
		DateLabel:    "2026-12-01",
		EventDateISO: "2026-12-01",
		Venue:        "Club Naco",
		Guests:       50,
		TotalRD:      10000,
	})
	if err != nil {
		t.Fatalf("provision failed: %v", err)
	}

	partialEvt, confirmedPartial, err := svc.ApplyVerifiedPayment(context.Background(), created.ID, 1500)
	if err != nil {
		t.Fatalf("partial payment failed: %v", err)
	}
	if confirmedPartial {
		t.Fatalf("expected partial payment below 30%% not to confirm")
	}
	if partialEvt.Status != domain.StatusPending {
		t.Fatalf("expected partial payment below 30%% to stay pending, got status=%s", partialEvt.Status)
	}

	confirmedEvt, transitioned, err := svc.ApplyVerifiedPayment(context.Background(), created.ID, 1500)
	if err != nil {
		t.Fatalf("confirming payment failed: %v", err)
	}
	if !transitioned {
		t.Fatalf("expected confirmation transition to be true")
	}
	if confirmedEvt.Status != domain.StatusConfirmed {
		t.Fatalf("expected confirmed status, got %s", confirmedEvt.Status)
	}
	if len(stock.transitionCalls) != 1 {
		t.Fatalf("expected 1 stock transition call, got %d", len(stock.transitionCalls))
	}
	if stock.transitionCalls[0].status != "confirmed" {
		t.Fatalf("expected confirmed stock transition, got %+v", stock.transitionCalls)
	}
}

func TestCompleteCloseoutEnforcesPermissionsPreventsRepeatAndReleasesStock(t *testing.T) {
	stock := &fakeStockManager{}
	pub := &fakeRealtimePublisher{}
	svc := app.NewService().WithStockManager(stock).WithRealtimePublisher(pub)

	client := platformhttp.Principal{ID: uuid.New(), Role: "client"}
	if _, err := svc.CompleteCloseoutForCaller(context.Background(), "evt-boda-real", "todo ok", client); !errors.Is(err, domain.ErrEventForbidden) {
		t.Fatalf("expected client closeout to fail with ErrEventForbidden, got %v", err)
	}

	staff := platformhttp.Principal{ID: uuid.New(), Role: "staff", FullName: "Pedro"}
	closed, err := svc.CompleteCloseoutForCaller(context.Background(), "evt-boda-real", "Inventario devuelto completo", staff)
	if err != nil {
		t.Fatalf("expected closeout to succeed, got %v", err)
	}
	if !closed.CloseoutCompleted {
		t.Fatalf("expected closeout completed flag true")
	}
	if closed.Status != domain.StatusCompleted {
		t.Fatalf("expected completed status, got %s", closed.Status)
	}
	if len(stock.transitionCalls) != 1 {
		t.Fatalf("expected 1 stock transition call, got %d", len(stock.transitionCalls))
	}
	if stock.transitionCalls[0].status != "released" {
		t.Fatalf("expected stock released call, got %+v", stock.transitionCalls)
	}

	if _, err := svc.CompleteCloseoutForCaller(context.Background(), "evt-boda-real", "repetido", staff); !errors.Is(err, domain.ErrAlreadyClosedOut) {
		t.Fatalf("expected ErrAlreadyClosedOut on repeat closeout, got %v", err)
	}
}

func TestPostChatMessageStoresAttachmentURLAndEmitsRealtimeEvent(t *testing.T) {
	pub := &fakeRealtimePublisher{}
	svc := app.NewService().WithRealtimePublisher(pub)
	client := platformhttp.Principal{ID: uuid.New(), Role: "staff", FullName: "María"}

	msg, err := svc.PostChatMessage(context.Background(), "thr-evt-boda-real", app.PostChatMessageCommand{
		EventID:       "evt-boda-real",
		EventTitle:    "Boda Real",
		Body:          "Adjunto plano final",
		AttachmentURL: "/v1/media/plano-final.pdf",
	}, client)
	if err != nil {
		t.Fatalf("expected chat post to succeed, got %v", err)
	}
	if msg.AttachmentURL != "/v1/media/plano-final.pdf" {
		t.Fatalf("expected attachmentUrl to persist, got %q", msg.AttachmentURL)
	}
	if len(pub.events) != 1 {
		t.Fatalf("expected 1 SSE event, got %d", len(pub.events))
	}
	if pub.events[0].Type != realtimedomain.EventChatMessage {
		t.Fatalf("expected chat.message SSE event, got %+v", pub.events)
	}
}

type fakeReminderDispatcher struct {
	calls       int
	lastChannel string
}

func (f *fakeReminderDispatcher) DispatchPaymentReminder(_ context.Context, _ domain.Event, channel, _ string) (bool, error) {
	f.calls++
	f.lastChannel = channel
	return true, nil
}

func TestSendPaymentReminderUpdatesTimestampAndDispatches(t *testing.T) {
	disp := &fakeReminderDispatcher{}
	svc := app.NewService().WithPaymentReminderDispatcher(disp)
	staff := platformhttp.Principal{ID: uuid.New(), Role: "staff", FullName: "María"}

	evt, sent, err := svc.SendPaymentReminderForCaller(context.Background(), "evt-boda-real", "whatsapp", "Favor abonar saldo", staff)
	if err != nil {
		t.Fatalf("expected SendPaymentReminderForCaller to succeed, got %v", err)
	}
	if !sent {
		t.Fatalf("expected sent=true")
	}
	if disp.calls != 1 {
		t.Fatalf("expected 1 dispatcher call, got %d", disp.calls)
	}
	if disp.lastChannel != "whatsapp" {
		t.Fatalf("expected whatsapp channel, got %s", disp.lastChannel)
	}
	if evt.LastPaymentReminderAt == "" {
		t.Fatalf("expected LastPaymentReminderAt populated")
	}
	if evt.LastPaymentReminderChannel != "whatsapp" {
		t.Fatalf("expected LastPaymentReminderChannel whatsapp, got %q", evt.LastPaymentReminderChannel)
	}
}
