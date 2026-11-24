package app

import (
	"context"
	"fmt"
	"log/slog"
	"strings"
	"time"
	_ "time/tzdata"

	"github.com/google/uuid"

	eventsapp "github.com/Jeudry/rosafiesta/server/internal/events/app"
	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	identitydomain "github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	operationsapp "github.com/Jeudry/rosafiesta/server/internal/operations/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
)

const (
	reminderKindPreEvent  = "pre_event"
	reminderKindPostEvent = "post_event"
	preEventWindowHours   = 48 * time.Hour
	postEventWindowHours  = 48 * time.Hour
	completedWindowDays   = 7 * 24 * time.Hour
)

// EventReminderWorker evalúa eventos próximos y recién finalizados para enviar recordatorios idempotentes.
type EventReminderWorker struct {
	events      *eventsapp.Service
	identity    *identityapp.Service
	operations  *operationsapp.Service
	emailSender mailer.Sender
	pushSender  push.Sender
	clock       clock.Clock
}

// NewEventReminderWorker construye un worker de recordatorios automáticos pre y post evento.
func NewEventReminderWorker(
	events *eventsapp.Service,
	identity *identityapp.Service,
	operations *operationsapp.Service,
	emailSender mailer.Sender,
	pushSender push.Sender,
	clk clock.Clock,
) *EventReminderWorker {
	if clk == nil {
		clk = clock.System{}
	}
	return &EventReminderWorker{
		events:      events,
		identity:    identity,
		operations:  operations,
		emailSender: emailSender,
		pushSender:  pushSender,
		clock:       clk,
	}
}

// Start inicia la ejecución periódica en segundo plano hasta cancelar el contexto.
func (w *EventReminderWorker) Start(ctx context.Context, interval time.Duration) {
	if ctx == nil {
		return
	}
	if interval <= 0 {
		interval = 15 * time.Minute
	}
	go func() {
		w.RunOnce(ctx)
		ticker := time.NewTicker(interval)
		defer ticker.Stop()
		for {
			select {
			case <-ctx.Done():
				return
			case <-ticker.C:
				w.RunOnce(ctx)
			}
		}
	}()
}

func santoDomingoLocation() *time.Location {
	loc, err := time.LoadLocation("America/Santo_Domingo")
	if err == nil {
		return loc
	}
	return time.FixedZone("America/Santo_Domingo", -4*3600)
}

// RunOnce evalúa los eventos activos y despacha recordatorios pendientes retornando el total enviado.
func (w *EventReminderWorker) RunOnce(ctx context.Context) int {
	hasRequiredDeps := w.events != nil
	if hasRequiredDeps {
		hasRequiredDeps = w.identity != nil
	}
	if hasRequiredDeps {
		hasRequiredDeps = w.operations != nil
	}
	if !hasRequiredDeps {
		return 0
	}
	now := w.clock.Now().In(santoDomingoLocation())
	snapshot := w.events.AllEventsSnapshot(ctx)
	dispatchedCount := 0
	for _, evt := range snapshot {
		if w.processEventReminders(ctx, evt, now) {
			dispatchedCount++
		}
	}
	return dispatchedCount
}

func (w *EventReminderWorker) processEventReminders(
	ctx context.Context,
	evt eventsdomain.Event,
	now time.Time,
) bool {
	kind, title, body, eligible := classifyEventReminderWindow(evt, now)
	if !eligible {
		return false
	}
	prefs := w.identity.PreferencesForAccount(ctx, evt.ClientID, evt.ClientEmail)
	if !prefs.EventRemindersEnabled {
		return false
	}
	alreadySent, checkErr := w.operations.HasSentEventReminder(ctx, evt.ID, evt.ClientID, kind)
	if checkErr != nil {
		slog.Warn("reminder_check_failed", slog.String("event_id", evt.ID), slog.String("error", checkErr.Error()))
		return false
	}
	if alreadySent {
		return false
	}
	w.deliverReminderChannels(ctx, evt, prefs, title, body)
	if markErr := w.operations.MarkEventReminderSent(ctx, evt.ID, evt.ClientID, kind); markErr != nil {
		slog.Warn("reminder_mark_failed", slog.String("event_id", evt.ID), slog.String("error", markErr.Error()))
	}
	return true
}

func (w *EventReminderWorker) deliverReminderChannels(
	ctx context.Context,
	evt eventsdomain.Event,
	prefs identitydomain.UserPreferences,
	title string,
	body string,
) {
	clientID := ""
	if evt.ClientID != uuid.Nil {
		clientID = evt.ClientID.String()
	}
	w.operations.CreateNotification(ctx, title, body, "recordatorios", "client", clientID)
	if prefs.PushNotificationsEnabled {
		if evt.ClientID != uuid.Nil {
			tokens := w.identity.ActivePushTokensForUser(ctx, evt.ClientID)
			dispatchRemotePush(ctx, w.pushSender, tokens, title, body, "recordatorios")
		}
	}
	if prefs.EmailReceiptsEnabled {
		w.sendReminderEmailIfConfigured(ctx, evt.ClientEmail, title, body)
	}
	if prefs.WhatsAppAlertsEnabled {
		w.sendReminderWhatsAppIfPossible(ctx, evt, body)
	}
}

func (w *EventReminderWorker) sendReminderEmailIfConfigured(
	ctx context.Context,
	email string,
	subject string,
	body string,
) {
	if w.emailSender == nil {
		return
	}
	if !w.emailSender.IsConfigured() {
		return
	}
	trimmedEmail := strings.TrimSpace(email)
	if trimmedEmail == "" {
		return
	}
	if err := w.emailSender.SendEmail(ctx, trimmedEmail, subject, body); err != nil {
		slog.Warn("reminder_email_failed", slog.String("error", err.Error()))
	}
}

func (w *EventReminderWorker) sendReminderWhatsAppIfPossible(
	ctx context.Context,
	evt eventsdomain.Event,
	body string,
) {
	adapter := userPreferencesAdapter{identity: w.identity, operations: w.operations}
	phone := adapter.lookupClientPhone(ctx, evt)
	if phone == "" {
		return
	}
	_, _ = w.operations.SendWhatsAppBotReply(ctx, "", phone, body)
}

func classifyEventReminderWindow(
	evt eventsdomain.Event,
	now time.Time,
) (kind, title, body string, eligible bool) {
	if evt.Status == eventsdomain.StatusCancelled {
		return "", "", "", false
	}
	eventDate, parsedOK := parseReminderEventDate(evt.EventDateISO)
	if !parsedOK {
		return "", "", "", false
	}
	todayStart := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, time.UTC)
	eventStart := time.Date(eventDate.Year(), eventDate.Month(), eventDate.Day(), 0, 0, 0, 0, time.UTC)
	untilEvent := eventStart.Sub(todayStart)

	if isPreEventCandidate(evt.Status, untilEvent) {
		title = "Recordatorio de evento próximo · " + evt.Title
		body = fmt.Sprintf(
			"Hola %s, tu evento \"%s\" en %s es pronto (%s). Verifica inventario y saldo pendiente.",
			evt.ClientName, evt.Title, evt.Venue, evt.DateLabel,
		)
		return reminderKindPreEvent, title, body, true
	}
	if isPostEventCandidate(evt.Status, todayStart.Sub(eventStart)) {
		title = "Gracias por celebrar con RosaFiesta · " + evt.Title
		body = fmt.Sprintf(
			"Hola %s, esperamos que hayas disfrutado \"%s\". Cuéntanos tu experiencia dejando una reseña en la app.",
			evt.ClientName, evt.Title,
		)
		return reminderKindPostEvent, title, body, true
	}
	return "", "", "", false
}

func isPreEventCandidate(status eventsdomain.EventStatus, untilEvent time.Duration) bool {
	if status == eventsdomain.StatusCompleted {
		return false
	}
	if untilEvent < 0 {
		return false
	}
	return untilEvent <= preEventWindowHours
}

func isPostEventCandidate(status eventsdomain.EventStatus, sinceEvent time.Duration) bool {
	if sinceEvent <= 0 {
		return false
	}
	if status == eventsdomain.StatusCompleted {
		return sinceEvent <= completedWindowDays
	}
	return sinceEvent <= postEventWindowHours
}

func parseReminderEventDate(raw string) (time.Time, bool) {
	cleaned := strings.TrimSpace(raw)
	if cleaned == "" {
		return time.Time{}, false
	}
	parsed, err := time.Parse("2006-01-02", cleaned)
	if err != nil {
		return time.Time{}, false
	}
	return parsed, true
}
