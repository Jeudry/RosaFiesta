// Package app implementa la orquestación de eventos, invitados, cronogramas y reservas asociadas.
package app

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

// StockReservationManager administra reservas temporales o confirmadas de artículos por evento.
type StockReservationManager interface {
	ReserveEventStock(ctx context.Context, eventID, quoteID, dateOrISO, status string, items []domain.EventItem) error
	TransitionEventStock(ctx context.Context, eventID, status string) error
}

// PaymentReminderDispatcher envía recordatorios de pago por los canales habilitados y registra notificación.
type PaymentReminderDispatcher interface {
	DispatchPaymentReminder(ctx context.Context, event domain.Event, channel, message string) (bool, error)
}

type RefundRecorder interface {
	RecordCancellationRefund(
		ctx context.Context,
		eventID string,
		eventTitle string,
		clientName string,
		reason string,
		depositDop int,
		refundDop int,
		policyTier string,
	) error
}

type Store interface {
	Enabled() bool
	ListEvents(ctx context.Context) ([]domain.Event, error)
	UpsertEvent(ctx context.Context, event domain.Event, now time.Time) error
	UpdateDate(ctx context.Context, event domain.Event) error
	UpdateStatusAndReason(ctx context.Context, id string, status domain.EventStatus, reason string) error
	ListGuests(ctx context.Context, eventID string) ([]domain.EventGuest, error)
	UpsertGuest(ctx context.Context, guest domain.EventGuest) error
	DeleteGuest(ctx context.Context, eventID, guestID string) error
	ListTeamTasks(ctx context.Context, eventID string) ([]domain.EventTask, error)
	UpsertTeamTask(ctx context.Context, task domain.EventTask) error
	DeleteTeamTask(ctx context.Context, eventID, taskID string) error
	ListScheduleBlocks(ctx context.Context, eventID string) ([]domain.ScheduleBlock, error)
	UpsertScheduleBlock(ctx context.Context, block domain.ScheduleBlock) error
	DeleteScheduleBlock(ctx context.Context, eventID, blockID string) error
	ListAllChatMessages(ctx context.Context) ([]domain.ChatMessage, error)
	ListChatMessagesByThread(ctx context.Context, threadID string) ([]domain.ChatMessage, error)
	UpsertChatMessage(ctx context.Context, msg domain.ChatMessage) error
}

type CreateEventCommand struct {
	Title          string
	EventType      string
	ClientName     string
	ClientEmail    string
	DateLabel      string
	EventDateISO   string
	QuoteID        string
	DaysUntilEvent int
	Venue          string
	Zone           string
	Guests         int
	TotalRD        int
	PaidDepositRD  int
	BalanceDueDate string
	AssignedLead   string
	SketchJSON     string
	Items          []domain.EventItem
}

type UpdateEventCommand struct {
	Title          *string
	Venue          *string
	Zone           *string
	DateLabel      *string
	EventDateISO   *string
	DaysUntilEvent *int
	Guests         *int
	Status         *string
	AssignedLead   *string
	SketchJSON     *string
	Items          *[]domain.EventItem
}

// ProvisionFromQuoteCommand contiene los datos de una cotización firmada para crear o enlazar su evento.
type ProvisionFromQuoteCommand struct {
	EventID      string
	QuoteID      string
	ClientID     uuid.UUID
	ClientEmail  string
	ClientName   string
	Title        string
	EventType    string
	DateLabel    string
	EventDateISO string
	Venue        string
	Guests       int
	TotalRD      int
	Items        []domain.EventItem
}

type Service struct {
	mu                 sync.RWMutex
	clock              clock.Clock
	store              Store
	refundRecorder     RefundRecorder
	stockManager       StockReservationManager
	reminderDispatcher PaymentReminderDispatcher
	realtimePub        realtimedomain.Publisher
	templates          []domain.EventTemplate
	events             map[string]domain.Event
	order              []string
	guests             map[string][]domain.EventGuest
	tasks              map[string][]domain.EventTask
	schedules          map[string][]domain.ScheduleBlock
	messages           []domain.ChatMessage
}

func NewService() *Service {
	svc := &Service{clock: clock.System{}}
	svc.Reset(context.Background())
	return svc
}

func (s *Service) WithClock(clk clock.Clock) *Service {
	if clk != nil {
		s.mu.Lock()
		s.clock = clk
		s.mu.Unlock()
	}
	return s
}

func (s *Service) WithRefundRecorder(recorder RefundRecorder) *Service {
	s.mu.Lock()
	s.refundRecorder = recorder
	s.mu.Unlock()
	return s
}

// WithStockManager configura el gestor de reservas de inventario del servicio.
func (s *Service) WithStockManager(mgr StockReservationManager) *Service {
	s.mu.Lock()
	s.stockManager = mgr
	s.mu.Unlock()
	return s
}

// WithRealtimePublisher configura el publicador SSE para notificaciones de eventos y chat.
func (s *Service) WithRealtimePublisher(pub realtimedomain.Publisher) *Service {
	s.mu.Lock()
	s.realtimePub = pub
	s.mu.Unlock()
	return s
}

// WithPaymentReminderDispatcher configura el despachador multicanal de recordatorios de pago.
func (s *Service) WithPaymentReminderDispatcher(dispatcher PaymentReminderDispatcher) *Service {
	s.mu.Lock()
	s.reminderDispatcher = dispatcher
	s.mu.Unlock()
	return s
}

func (s *Service) Reset(_ context.Context) {
	now := s.clock.Now()
	canonical := domain.CanonicalEvents()
	events := make(map[string]domain.Event, len(canonical))
	order := make([]string, 0, len(canonical))
	for _, evt := range canonical {
		events[evt.ID] = evt
		order = append(order, evt.ID)
	}
	guests := make(map[string][]domain.EventGuest)
	for _, guest := range domain.CanonicalGuests(now) {
		guests[guest.EventID] = append(guests[guest.EventID], guest)
	}
	schedules := make(map[string][]domain.ScheduleBlock)
	for _, block := range domain.CanonicalScheduleBlocks() {
		schedules[block.EventID] = append(schedules[block.EventID], block)
	}
	s.mu.Lock()
	s.templates = domain.CanonicalTemplates()
	s.events = events
	s.order = order
	s.guests = guests
	s.tasks = make(map[string][]domain.EventTask)
	s.schedules = schedules
	s.messages = domain.CanonicalChatMessages(now)
	s.mu.Unlock()
}

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.templates = domain.CanonicalTemplates()
	s.events = make(map[string]domain.Event)
	s.order = nil
	s.guests = make(map[string][]domain.EventGuest)
	s.tasks = make(map[string][]domain.EventTask)
	s.schedules = make(map[string][]domain.ScheduleBlock)
	s.messages = nil
	s.mu.Unlock()
}

func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.syncFromStoreLocked(context.Background())
	s.mu.Unlock()
	return s
}

func (s *Service) ActiveEventsForClient(ctx context.Context, clientIdentifier string) int {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)

	trimmed := strings.TrimSpace(clientIdentifier)
	target := strings.ToLower(trimmed)
	count := 0
	for _, id := range s.order {
		evt := s.events[id]
		if !evt.IsActive() {
			continue
		}
		matchesName := strings.EqualFold(evt.ClientName, target)
		matchesEmail := strings.EqualFold(evt.ClientEmail, target)
		if matchesName {
			count++
			continue
		}
		if matchesEmail {
			count++
		}
	}
	return count
}

func (s *Service) AllEventsSnapshot(ctx context.Context) []domain.Event {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	out := make([]domain.Event, 0, len(s.order))
	for _, id := range s.order {
		out = append(out, s.events[id])
	}
	return out
}

func (s *Service) ReservedStockByDateForMonth(ctx context.Context, articleID, month string) map[string]int {
	events := s.AllEventsSnapshot(ctx)
	year := parseMonthYear(month)
	reserved := make(map[string]int)
	for _, evt := range events {
		if !evt.IsActive() {
			continue
		}
		isoDate := domain.EventISODate(evt.DateLabel, year)
		if !strings.HasPrefix(isoDate, month) {
			continue
		}
		for _, item := range evt.Items {
			if domain.MatchesArticleID(item.ArticleCode, articleID) {
				reserved[isoDate] += item.Quantity
			}
		}
	}
	return reserved
}

func (s *Service) ListTemplates() []domain.EventTemplate {
	s.mu.RLock()
	defer s.mu.RUnlock()
	result := make([]domain.EventTemplate, len(s.templates))
	copy(result, s.templates)
	return result
}

func (s *Service) ListEvents(status string) []domain.Event {
	return s.ListEventsForCaller(context.Background(), status, platformhttp.Principal{Role: "admin"}, true)
}

func (s *Service) ListEventsForCaller(
	ctx context.Context,
	status string,
	caller platformhttp.Principal,
	authenticated bool,
) []domain.Event {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	snapshot := make([]domain.Event, 0, len(s.order))
	for _, id := range s.order {
		snapshot = append(snapshot, s.events[id])
	}
	s.mu.Unlock()

	trimmedStatus := strings.TrimSpace(status)
	result := make([]domain.Event, 0, len(snapshot))
	for _, evt := range snapshot {
		if trimmedStatus != "" {
			if string(evt.Status) != trimmedStatus {
				continue
			}
		}
		if authenticated {
			if caller.IsClient() {
				if !evt.BelongsToClient(caller.ID, caller.Email, caller.FullName) {
					continue
				}
			}
		}
		result = append(result, s.withComputedDaysUntil(evt))
	}
	return result
}

func (s *Service) GetEventForCaller(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
	authenticated bool,
) (domain.Event, error) {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	evt, ok := s.events[id]
	s.mu.Unlock()
	if !ok {
		return domain.Event{}, domain.ErrEventNotFound
	}
	if authenticated {
		if caller.IsClient() {
			if !evt.BelongsToClient(caller.ID, caller.Email, caller.FullName) {
				return domain.Event{}, domain.ErrEventForbidden
			}
		}
	}
	return s.withComputedDaysUntil(evt), nil
}

func (s *Service) withComputedDaysUntil(evt domain.Event) domain.Event {
	if strings.TrimSpace(evt.EventDateISO) != "" {
		evt.DaysUntilEvent = domain.ComputeDaysUntilEvent(evt.EventDateISO, s.clock.Now(), evt.DaysUntilEvent)
	}
	return evt
}

func (s *Service) CreateEvent(
	ctx context.Context,
	cmd CreateEventCommand,
	caller platformhttp.Principal,
) (domain.Event, error) {
	return s.CreateEventForCaller(ctx, cmd, caller)
}

// CreateEventForCaller crea un nuevo evento tras reservar inventario para la fecha solicitada.
func (s *Service) CreateEventForCaller(
	ctx context.Context,
	cmd CreateEventCommand,
	caller platformhttp.Principal,
) (domain.Event, error) {
	title := strings.TrimSpace(cmd.Title)
	if title == "" {
		return domain.Event{}, domain.ErrInvalidEventInput
	}
	if cmd.Guests < 0 {
		return domain.Event{}, domain.ErrInvalidGuestCount
	}
	now := s.clock.Now()
	newEvt := buildCreatedEvent(cmd, title, caller, now)
	applyEventDateRange(&newEvt, now.Year())
	if strings.TrimSpace(newEvt.EventDateISO) != "" {
		newEvt.DaysUntilEvent = domain.ComputeDaysUntilEvent(newEvt.EventDateISO, now, newEvt.DaysUntilEvent)
	}
	if err := s.reserveStockForEvent(ctx, newEvt, reservationStatusForEvent(newEvt.Status)); err != nil {
		return domain.Event{}, err
	}
	if err := s.saveAndPublishEvent(ctx, newEvt, now, true); err != nil {
		return domain.Event{}, err
	}
	return newEvt, nil
}

func (s *Service) UpdateEvent(
	ctx context.Context,
	id string,
	cmd UpdateEventCommand,
	caller platformhttp.Principal,
) (domain.Event, error) {
	return s.UpdateEventForCaller(ctx, id, cmd, caller)
}

// UpdateEventForCaller actualiza campos del evento y sincroniza reservas cuando cambian fecha o artículos.
func (s *Service) UpdateEventForCaller(
	ctx context.Context,
	id string,
	cmd UpdateEventCommand,
	caller platformhttp.Principal,
) (domain.Event, error) {
	evt, err := s.GetEventForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Event{}, err
	}
	updated, err := applyEventPatchForCaller(evt, cmd, caller)
	if err != nil {
		return domain.Event{}, err
	}
	now := s.clock.Now()
	if patchChangesDate(cmd) {
		applyEventDateRange(&updated, now.Year())
		if strings.TrimSpace(updated.EventDateISO) != "" {
			updated.DaysUntilEvent = domain.ComputeDaysUntilEvent(updated.EventDateISO, now, updated.DaysUntilEvent)
		}
	}
	if patchRequiresStockSync(cmd) {
		if err := s.reserveStockForEvent(ctx, updated, reservationStatusForEvent(updated.Status)); err != nil {
			return domain.Event{}, err
		}
	}
	if err := s.saveAndPublishEvent(ctx, updated, now, false); err != nil {
		return domain.Event{}, err
	}
	return updated, nil
}

func (s *Service) DuplicateEvent(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
) (domain.Event, error) {
	return s.DuplicateEventForCaller(ctx, id, caller)
}

// DuplicateEventForCaller clona un evento existente como borrador validando disponibilidad de inventario.
func (s *Service) DuplicateEventForCaller(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
) (domain.Event, error) {
	source, err := s.GetEventForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Event{}, err
	}
	now := s.clock.Now()
	duplicated := cloneEventAsDraft(source, now)
	if err := s.reserveStockForEvent(ctx, duplicated, "held"); err != nil {
		return domain.Event{}, err
	}
	if err := s.saveAndPublishEvent(ctx, duplicated, now, true); err != nil {
		return domain.Event{}, err
	}
	return duplicated, nil
}

func (s *Service) RequestDateChangeForCaller(
	ctx context.Context,
	id string,
	newDateLabel string,
	newDaysUntilEvent int,
	caller platformhttp.Principal,
) (domain.Event, domain.DateChangeEvaluation, error) {
	evt, err := s.GetEventForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Event{}, domain.DateChangeEvaluation{}, err
	}
	eval, err := domain.EvaluateDateChange(evt)
	if err != nil {
		return domain.Event{}, domain.DateChangeEvaluation{}, err
	}
	evt.DateLabel = strings.TrimSpace(newDateLabel)
	evt.EventDateISO = ""
	applyEventDateRange(&evt, s.clock.Now().Year())
	computedDays := domain.ComputeDaysUntilEvent(evt.EventDateISO, s.clock.Now(), newDaysUntilEvent)
	canUseAdminDays := !caller.IsClient()
	if canUseAdminDays {
		canUseAdminDays = newDaysUntilEvent > 0
	}
	if canUseAdminDays {
		computedDays = newDaysUntilEvent
	}
	evt.DaysUntilEvent = computedDays
	evt.DateChangesUsed = eval.NextCount
	evt.TotalRD += eval.FeeRD
	if err := s.reserveStockForEvent(ctx, evt, reservationStatusForEvent(evt.Status)); err != nil {
		return domain.Event{}, domain.DateChangeEvaluation{}, err
	}
	if err := s.persistDateChange(ctx, evt); err != nil {
		return domain.Event{}, domain.DateChangeEvaluation{}, err
	}
	s.publishEventUpdated(evt)
	return evt, eval, nil
}

func (s *Service) CancelEventForCaller(
	ctx context.Context,
	id string,
	reason string,
	caller platformhttp.Principal,
) (domain.Event, domain.RefundPolicyResult, error) {
	evt, err := s.GetEventForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Event{}, domain.RefundPolicyResult{}, err
	}
	if evt.Status == domain.StatusCompleted {
		return domain.Event{}, domain.RefundPolicyResult{}, domain.ErrCannotCancelCompleted
	}
	if evt.Status == domain.StatusCancelled {
		return domain.Event{}, domain.RefundPolicyResult{}, domain.ErrAlreadyCancelled
	}
	cleanReason := defaultTrimmed(reason, "Cancellation requested by client")
	refund := domain.CalculateCancellationRefund(evt.PaidDepositRD, evt.DaysUntilEvent)
	evt.Status = domain.StatusCancelled
	evt.CancellationReason = cleanReason

	s.mu.Lock()
	s.events[id] = evt
	store := s.store
	recorder := s.refundRecorder
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			_ = store.UpdateStatusAndReason(ctx, id, evt.Status, evt.CancellationReason)
		}
	}
	_ = s.transitionStockForEvent(ctx, id, "cancelled")
	s.recordCancellationRefund(ctx, recorder, evt, refund)
	s.publishEventUpdated(evt)
	return evt, refund, nil
}

// ProvisionFromSignedQuote crea o actualiza un evento enlazado a una cotización firmada y retiene su stock.
func (s *Service) ProvisionFromSignedQuote(
	ctx context.Context,
	cmd ProvisionFromQuoteCommand,
) (domain.Event, error) {
	now := s.clock.Now()
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	existing, found := s.findExistingEventForQuoteLocked(cmd.EventID, cmd.QuoteID)
	s.mu.Unlock()

	var candidate domain.Event
	if found {
		candidate = mergeProvisionedEvent(existing, cmd)
	} else {
		candidate = buildProvisionedEvent(cmd, now)
	}
	applyEventDateRange(&candidate, now.Year())
	if err := s.reserveStockForEvent(ctx, candidate, "held"); err != nil {
		return domain.Event{}, err
	}
	if err := s.saveAndPublishEvent(ctx, candidate, now, !found); err != nil {
		return domain.Event{}, err
	}
	return candidate, nil
}

// ApplyVerifiedPayment incrementa el anticipo pagado y confirma evento y stock al alcanzar el mínimo.
func (s *Service) ApplyVerifiedPayment(
	ctx context.Context,
	eventID string,
	verifiedAmountRD int,
) (domain.Event, bool, error) {
	evt, found := s.findEventByID(ctx, strings.TrimSpace(eventID))
	if !found {
		return domain.Event{}, false, domain.ErrEventNotFound
	}
	transitioned := evt.ApplyPaidDeposit(verifiedAmountRD)
	if transitioned {
		if err := s.transitionStockForEvent(ctx, evt.ID, "confirmed"); err != nil {
			return domain.Event{}, false, err
		}
	}
	if err := s.saveAndPublishEvent(ctx, evt, s.clock.Now(), false); err != nil {
		return domain.Event{}, false, err
	}
	return evt, transitioned, nil
}

// CompleteCloseoutForCaller ejecuta el cierre del evento por personal autorizado y libera el inventario.
func (s *Service) CompleteCloseoutForCaller(
	ctx context.Context,
	eventID string,
	notes string,
	caller platformhttp.Principal,
) (domain.Event, error) {
	if !caller.IsAdminOrStaff() {
		return domain.Event{}, domain.ErrEventForbidden
	}
	evt, found := s.findEventByID(ctx, strings.TrimSpace(eventID))
	if !found {
		return domain.Event{}, domain.ErrEventNotFound
	}
	if err := evt.CompleteCloseout(notes); err != nil {
		return domain.Event{}, err
	}
	if err := s.transitionStockForEvent(ctx, evt.ID, "released"); err != nil {
		return domain.Event{}, err
	}
	if err := s.saveAndPublishEvent(ctx, evt, s.clock.Now(), false); err != nil {
		return domain.Event{}, err
	}
	return evt, nil
}

// SendPaymentReminderForCaller registra y envía un recordatorio de saldo pendiente al cliente.
func (s *Service) SendPaymentReminderForCaller(
	ctx context.Context,
	eventID string,
	channel string,
	customMessage string,
	caller platformhttp.Principal,
) (domain.Event, bool, error) {
	if !caller.IsAdminOrStaff() {
		return domain.Event{}, false, domain.ErrEventForbidden
	}
	evt, found := s.findEventByID(ctx, strings.TrimSpace(eventID))
	if !found {
		return domain.Event{}, false, domain.ErrEventNotFound
	}
	cleanChannel := strings.ToLower(strings.TrimSpace(channel))
	if cleanChannel == "" {
		cleanChannel = "email"
	}
	now := s.clock.Now()
	s.mu.RLock()
	dispatcher := s.reminderDispatcher
	s.mu.RUnlock()
	dispatched := false
	if dispatcher != nil {
		ok, err := dispatcher.DispatchPaymentReminder(ctx, evt, cleanChannel, strings.TrimSpace(customMessage))
		if err != nil {
			return domain.Event{}, false, err
		}
		dispatched = ok
	}
	evt.LastPaymentReminderAt = now.UTC().Format(time.RFC3339)
	evt.LastPaymentReminderChannel = cleanChannel
	if err := s.saveAndPublishEvent(ctx, evt, now, false); err != nil {
		return domain.Event{}, false, err
	}
	return evt, dispatched, nil
}

func buildCreatedEvent(
	cmd CreateEventCommand,
	title string,
	caller platformhttp.Principal,
	now time.Time,
) domain.Event {
	suffix := uuid.NewString()[:8]
	clientID, clientName, clientEmail := resolveCreatedEventClient(cmd, caller)
	items := cmd.Items
	if items == nil {
		items = []domain.EventItem{}
	}
	articlesCount, computedTotal := sumEventItems(items)
	totalRD := cmd.TotalRD
	if totalRD <= 0 {
		totalRD = computedTotal
	}
	sketch := defaultTrimmed(cmd.SketchJSON, "{}")
	eventType := defaultTrimmed(cmd.EventType, "Evento")
	daysUntil := cmd.DaysUntilEvent
	if daysUntil == 0 {
		daysUntil = 14
	}
	upperSuffix := strings.ToUpper(suffix[:4])
	code := fmt.Sprintf("RF-%d-%s", now.Year(), upperSuffix)
	paidDeposit := cmd.PaidDepositRD
	if caller.IsClient() {
		paidDeposit = 0
	}
	return domain.Event{
		ID:             "evt-" + suffix,
		Code:           code,
		ClientID:       clientID,
		ClientName:     clientName,
		ClientEmail:    clientEmail,
		Title:          title,
		EventType:      eventType,
		DateLabel:      strings.TrimSpace(cmd.DateLabel),
		EventDateISO:   strings.TrimSpace(cmd.EventDateISO),
		QuoteID:        strings.TrimSpace(cmd.QuoteID),
		DaysUntilEvent: daysUntil,
		Venue:          strings.TrimSpace(cmd.Venue),
		Zone:           strings.TrimSpace(cmd.Zone),
		Guests:         cmd.Guests,
		ArticleCount:   articlesCount,
		TotalRD:        totalRD,
		PaidDepositRD:  paidDeposit,
		BalanceDueDate: strings.TrimSpace(cmd.BalanceDueDate),
		Status:         domain.StatusPending,
		AssignedLead:   strings.TrimSpace(cmd.AssignedLead),
		SketchJSON:     sketch,
		Items:          items,
	}
}

func resolveCreatedEventClient(
	cmd CreateEventCommand,
	caller platformhttp.Principal,
) (uuid.UUID, string, string) {
	clientID := uuid.Nil
	clientName := strings.TrimSpace(cmd.ClientName)
	clientEmail := strings.TrimSpace(cmd.ClientEmail)
	if caller.IsClient() {
		clientID = caller.ID
		clientEmail = caller.Email
	}
	if clientName == "" {
		clientName = caller.FullName
	}
	return clientID, clientName, clientEmail
}

func defaultTrimmed(raw, fallback string) string {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return fallback
	}
	return trimmed
}

func applyEventPatchForCaller(existing domain.Event, cmd UpdateEventCommand, caller platformhttp.Principal) (domain.Event, error) {
	updated := existing
	if cmd.Title != nil {
		trimmed := strings.TrimSpace(*cmd.Title)
		if trimmed == "" {
			return domain.Event{}, domain.ErrInvalidEventInput
		}
		updated.Title = trimmed
	}
	if cmd.Venue != nil {
		updated.Venue = strings.TrimSpace(*cmd.Venue)
	}
	if cmd.Zone != nil {
		updated.Zone = strings.TrimSpace(*cmd.Zone)
	}
	if cmd.DateLabel != nil {
		updated.DateLabel = strings.TrimSpace(*cmd.DateLabel)
	}
	if cmd.EventDateISO != nil {
		updated.EventDateISO = strings.TrimSpace(*cmd.EventDateISO)
	}
	canOverrideDays := !caller.IsClient()
	if canOverrideDays {
		canOverrideDays = cmd.DaysUntilEvent != nil
	}
	if canOverrideDays {
		updated.DaysUntilEvent = *cmd.DaysUntilEvent
	}
	return applyEventDetailsPatch(updated, cmd, caller)
}

func applyEventDetailsPatch(updated domain.Event, cmd UpdateEventCommand, caller platformhttp.Principal) (domain.Event, error) {
	if cmd.Guests != nil {
		if *cmd.Guests < 0 {
			return domain.Event{}, domain.ErrInvalidGuestCount
		}
		updated.Guests = *cmd.Guests
	}
	if cmd.Status != nil {
		trimmedStatus := strings.TrimSpace(*cmd.Status)
		candidate := domain.EventStatus(trimmedStatus)
		if !domain.IsValidEventStatus(candidate) {
			return domain.Event{}, domain.ErrInvalidEventStatus
		}
		clientDenied := caller.IsClient()
		if clientDenied {
			clientDenied = !isClientAllowedStatus(candidate)
		}
		if clientDenied {
			return domain.Event{}, domain.ErrEventForbidden
		}
		updated.Status = candidate
	}
	canAssignLead := !caller.IsClient()
	if canAssignLead {
		canAssignLead = cmd.AssignedLead != nil
	}
	if canAssignLead {
		updated.AssignedLead = strings.TrimSpace(*cmd.AssignedLead)
	}
	if cmd.SketchJSON != nil {
		updated.SketchJSON = strings.TrimSpace(*cmd.SketchJSON)
	}
	if cmd.Items != nil {
		items := *cmd.Items
		count, total := sumEventItems(items)
		updated.Items = items
		updated.ArticleCount = count
		if total > 0 {
			updated.TotalRD = total
		}
	}
	return updated, nil
}

func isClientAllowedStatus(status domain.EventStatus) bool {
	if status == domain.StatusDraft {
		return true
	}
	return status == domain.StatusPending
}

func sumEventItems(items []domain.EventItem) (int, int) {
	totalCount := 0
	totalPrice := 0
	for _, item := range items {
		totalCount += item.Quantity
		totalPrice += item.SubtotalRD()
	}
	return totalCount, totalPrice
}

func parseMonthYear(month string) int {
	parsed, err := time.Parse("2006-01", month)
	if err != nil {
		return 2026
	}
	return parsed.Year()
}

func (s *Service) syncFromStoreLocked(ctx context.Context) {
	if s.store == nil {
		return
	}
	if !s.store.Enabled() {
		return
	}
	dbEvents, err := s.store.ListEvents(ctx)
	if err != nil {
		return
	}
	for _, dbEvt := range dbEvents {
		existing, ok := s.events[dbEvt.ID]
		if ok {
			merged := mergeEventFromDB(existing, dbEvt)
			s.events[dbEvt.ID] = merged
		} else {
			s.events[dbEvt.ID] = dbEvt
			s.order = append(s.order, dbEvt.ID)
		}
	}
}

func mergeEventFromDB(existing, dbEvt domain.Event) domain.Event {
	merged := dbEvt
	if merged.Code == "" {
		merged.Code = existing.Code
	}
	if merged.Zone == "" {
		merged.Zone = existing.Zone
	}
	if merged.AssignedLead == "" {
		merged.AssignedLead = existing.AssignedLead
	}
	if merged.EventDateISO == "" {
		merged.EventDateISO = existing.EventDateISO
		merged.PickupDateISO = existing.PickupDateISO
		merged.ReturnDateISO = existing.ReturnDateISO
	}
	if merged.QuoteID == "" {
		merged.QuoteID = existing.QuoteID
	}
	if len(merged.Items) == 0 {
		merged.Items = existing.Items
	}
	return merged
}

func cloneEventAsDraft(source domain.Event, now time.Time) domain.Event {
	suffix := uuid.NewString()[:8]
	itemsCopy := make([]domain.EventItem, len(source.Items))
	copy(itemsCopy, source.Items)
	duplicated := source
	duplicated.ID = "evt-" + suffix
	duplicated.Code = "RF-DUP-" + strings.ToUpper(suffix[:4])
	duplicated.Title = "Copia de " + source.Title
	duplicated.PaidDepositRD = 0
	duplicated.Status = domain.StatusDraft
	duplicated.DateChangesUsed = 0
	duplicated.CancellationReason = ""
	duplicated.QuoteID = ""
	duplicated.CloseoutCompleted = false
	duplicated.CloseoutNotes = ""
	duplicated.Items = itemsCopy
	applyEventDateRange(&duplicated, now.Year())
	return duplicated
}

func patchChangesDate(cmd UpdateEventCommand) bool {
	if cmd.DateLabel != nil {
		return true
	}
	return cmd.EventDateISO != nil
}

func patchRequiresStockSync(cmd UpdateEventCommand) bool {
	if cmd.Items != nil {
		return true
	}
	return patchChangesDate(cmd)
}

func applyEventDateRange(evt *domain.Event, referenceYear int) {
	eventISO, pickupISO, returnISO := domain.ComputeEventDateRange(evt.EventDateISO, evt.DateLabel, referenceYear)
	if eventISO == "" {
		return
	}
	evt.EventDateISO = eventISO
	evt.PickupDateISO = pickupISO
	evt.ReturnDateISO = returnISO
}

func reservationStatusForEvent(status domain.EventStatus) string {
	if status == domain.StatusConfirmed {
		return "confirmed"
	}
	if status == domain.StatusInProgress {
		return "confirmed"
	}
	return "held"
}

func (s *Service) reserveStockForEvent(ctx context.Context, evt domain.Event, status string) error {
	s.mu.RLock()
	mgr := s.stockManager
	s.mu.RUnlock()
	if mgr == nil {
		return nil
	}
	dateTarget := evt.EventDateISO
	if dateTarget == "" {
		dateTarget = evt.DateLabel
	}
	if err := mgr.ReserveEventStock(ctx, evt.ID, evt.QuoteID, dateTarget, status, evt.Items); err != nil {
		if errors.Is(err, domain.ErrInsufficientStock) {
			return err
		}
		return fmt.Errorf("%w: %w", domain.ErrInsufficientStock, err)
	}
	return nil
}

func (s *Service) transitionStockForEvent(ctx context.Context, eventID, status string) error {
	s.mu.RLock()
	mgr := s.stockManager
	s.mu.RUnlock()
	if mgr == nil {
		return nil
	}
	return mgr.TransitionEventStock(ctx, eventID, status)
}

func (s *Service) saveAndPublishEvent(ctx context.Context, evt domain.Event, now time.Time, appendOrder bool) error {
	s.mu.Lock()
	s.events[evt.ID] = evt
	if appendOrder {
		s.order = append(s.order, evt.ID)
	}
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertEvent(ctx, evt, now); err != nil {
				return err
			}
		}
	}
	s.publishEventUpdated(evt)
	return nil
}

func (s *Service) persistDateChange(ctx context.Context, evt domain.Event) error {
	s.mu.Lock()
	s.events[evt.ID] = evt
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			return store.UpdateDate(ctx, evt)
		}
	}
	return nil
}

func (s *Service) findEventByID(ctx context.Context, eventID string) (domain.Event, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	evt, ok := s.events[eventID]
	return evt, ok
}

func (s *Service) findExistingEventForQuoteLocked(eventID, quoteID string) (domain.Event, bool) {
	trimmedEvent := strings.TrimSpace(eventID)
	if trimmedEvent != "" {
		if evt, ok := s.events[trimmedEvent]; ok {
			return evt, true
		}
	}
	trimmedQuote := strings.TrimSpace(quoteID)
	if trimmedQuote == "" {
		return domain.Event{}, false
	}
	for _, id := range s.order {
		candidate := s.events[id]
		if candidate.QuoteID == trimmedQuote {
			return candidate, true
		}
	}
	return domain.Event{}, false
}

func buildProvisionedEvent(cmd ProvisionFromQuoteCommand, now time.Time) domain.Event {
	suffix := uuid.NewString()[:8]
	items := make([]domain.EventItem, len(cmd.Items))
	copy(items, cmd.Items)
	articleCount, computedTotal := sumEventItems(items)
	totalRD := cmd.TotalRD
	if totalRD <= 0 {
		totalRD = computedTotal
	}
	guests := cmd.Guests
	if guests <= 0 {
		guests = 1
	}
	upperSuffix := strings.ToUpper(suffix[:4])
	return domain.Event{
		ID:             "evt-" + suffix,
		Code:           fmt.Sprintf("RF-%d-%s", now.Year(), upperSuffix),
		ClientID:       cmd.ClientID,
		ClientEmail:    strings.TrimSpace(cmd.ClientEmail),
		ClientName:     strings.TrimSpace(cmd.ClientName),
		Title:          defaultTrimmed(cmd.Title, "Evento RosaFiesta"),
		EventType:      defaultTrimmed(cmd.EventType, "Evento"),
		DateLabel:      strings.TrimSpace(cmd.DateLabel),
		EventDateISO:   strings.TrimSpace(cmd.EventDateISO),
		QuoteID:        strings.TrimSpace(cmd.QuoteID),
		DaysUntilEvent: 14,
		Venue:          strings.TrimSpace(cmd.Venue),
		Guests:         guests,
		ArticleCount:   articleCount,
		TotalRD:        totalRD,
		Status:         domain.StatusPending,
		SketchJSON:     "{}",
		Items:          items,
	}
}

func mergeProvisionedEvent(existing domain.Event, cmd ProvisionFromQuoteCommand) domain.Event {
	merged := existing
	merged.QuoteID = defaultTrimmed(cmd.QuoteID, existing.QuoteID)
	merged.Title = defaultTrimmed(cmd.Title, existing.Title)
	merged.EventType = defaultTrimmed(cmd.EventType, existing.EventType)
	merged.DateLabel = defaultTrimmed(cmd.DateLabel, existing.DateLabel)
	merged.EventDateISO = defaultTrimmed(cmd.EventDateISO, existing.EventDateISO)
	merged.Venue = defaultTrimmed(cmd.Venue, existing.Venue)
	if cmd.Guests > 0 {
		merged.Guests = cmd.Guests
	}
	if len(cmd.Items) > 0 {
		items := make([]domain.EventItem, len(cmd.Items))
		copy(items, cmd.Items)
		merged.Items = items
		merged.ArticleCount, _ = sumEventItems(items)
	}
	if cmd.TotalRD > 0 {
		merged.TotalRD = cmd.TotalRD
	}
	if merged.ClientID == uuid.Nil {
		merged.ClientID = cmd.ClientID
	}
	merged.ClientEmail = defaultTrimmed(existing.ClientEmail, cmd.ClientEmail)
	merged.ClientName = defaultTrimmed(existing.ClientName, cmd.ClientName)
	return merged
}

func (s *Service) recordCancellationRefund(
	ctx context.Context,
	recorder RefundRecorder,
	evt domain.Event,
	refund domain.RefundPolicyResult,
) {
	if recorder == nil {
		return
	}
	_ = recorder.RecordCancellationRefund(
		ctx,
		evt.ID,
		evt.Title,
		evt.ClientName,
		evt.CancellationReason,
		evt.PaidDepositRD,
		refund.RefundRD,
		refund.PolicyTier,
	)
}

func (s *Service) publishEventUpdated(evt domain.Event) {
	s.mu.RLock()
	pub := s.realtimePub
	s.mu.RUnlock()
	if pub == nil {
		return
	}
	clientID := ""
	audience := realtimedomain.AudienceAdmin
	if evt.ClientID != uuid.Nil {
		clientID = evt.ClientID.String()
		audience = realtimedomain.AudienceClient
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     realtimedomain.EventEventUpdated,
		Audience: audience,
		ClientID: clientID,
		EntityID: evt.ID,
		Payload: map[string]any{
			"eventId":           evt.ID,
			"status":            string(evt.Status),
			"closeoutCompleted": evt.CloseoutCompleted,
			"paidDepositRD":     evt.PaidDepositRD,
		},
	})
}
