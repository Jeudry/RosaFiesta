package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

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
	DaysUntilEvent *int
	Guests         *int
	Status         *string
	AssignedLead   *string
	SketchJSON     *string
	Items          *[]domain.EventItem
}

type Service struct {
	mu             sync.RWMutex
	clock          clock.Clock
	store          Store
	refundRecorder RefundRecorder
	templates      []domain.EventTemplate
	events         map[string]domain.Event
	order          []string
	guests         map[string][]domain.EventGuest
	schedules      map[string][]domain.ScheduleBlock
	messages       []domain.ChatMessage
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
	s.schedules = schedules
	s.messages = domain.CanonicalChatMessages(now)
	s.mu.Unlock()
}

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.templates = nil
	s.events = make(map[string]domain.Event)
	s.order = nil
	s.guests = make(map[string][]domain.EventGuest)
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
		result = append(result, evt)
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
	return evt, nil
}

func (s *Service) CreateEvent(
	ctx context.Context,
	cmd CreateEventCommand,
	caller platformhttp.Principal,
) (domain.Event, error) {
	title := strings.TrimSpace(cmd.Title)
	if title == "" {
		return domain.Event{}, domain.ErrInvalidEventInput
	}
	if cmd.Guests <= 0 {
		return domain.Event{}, domain.ErrInvalidGuestCount
	}
	now := s.clock.Now()
	newEvt := buildCreatedEvent(cmd, title, caller, now)
	s.mu.Lock()
	s.events[newEvt.ID] = newEvt
	s.order = append(s.order, newEvt.ID)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertEvent(ctx, newEvt, now); err != nil {
				return domain.Event{}, err
			}
		}
	}
	return newEvt, nil
}

func (s *Service) UpdateEvent(
	ctx context.Context,
	id string,
	cmd UpdateEventCommand,
	caller platformhttp.Principal,
) (domain.Event, error) {
	evt, err := s.GetEventForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Event{}, err
	}
	updated, err := applyEventPatch(evt, cmd)
	if err != nil {
		return domain.Event{}, err
	}
	now := s.clock.Now()
	s.mu.Lock()
	s.events[id] = updated
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertEvent(ctx, updated, now); err != nil {
				return domain.Event{}, err
			}
		}
	}
	return updated, nil
}

func (s *Service) DuplicateEvent(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
) (domain.Event, error) {
	source, err := s.GetEventForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Event{}, err
	}
	now := s.clock.Now()
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
	duplicated.Items = itemsCopy

	s.mu.Lock()
	s.events[duplicated.ID] = duplicated
	s.order = append(s.order, duplicated.ID)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertEvent(ctx, duplicated, now); err != nil {
				return domain.Event{}, err
			}
		}
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
	evt.DaysUntilEvent = newDaysUntilEvent
	evt.DateChangesUsed = eval.NextCount
	evt.TotalRD += eval.FeeRD
	s.mu.Lock()
	s.events[id] = evt
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.UpdateDate(ctx, evt)
		}
	}
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
	cleanReason := strings.TrimSpace(reason)
	if cleanReason == "" {
		cleanReason = "Cancellation requested by client"
	}
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
	if recorder != nil {
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
	return evt, refund, nil
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
	return domain.Event{
		ID:             "evt-" + suffix,
		Code:           code,
		ClientID:       clientID,
		ClientName:     clientName,
		ClientEmail:    clientEmail,
		Title:          title,
		EventType:      eventType,
		DateLabel:      strings.TrimSpace(cmd.DateLabel),
		DaysUntilEvent: daysUntil,
		Venue:          strings.TrimSpace(cmd.Venue),
		Zone:           strings.TrimSpace(cmd.Zone),
		Guests:         cmd.Guests,
		ArticleCount:   articlesCount,
		TotalRD:        totalRD,
		PaidDepositRD:  cmd.PaidDepositRD,
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

func applyEventPatch(existing domain.Event, cmd UpdateEventCommand) (domain.Event, error) {
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
	if cmd.DaysUntilEvent != nil {
		updated.DaysUntilEvent = *cmd.DaysUntilEvent
	}
	return applyEventDetailsPatch(updated, cmd)
}

func applyEventDetailsPatch(updated domain.Event, cmd UpdateEventCommand) (domain.Event, error) {
	if cmd.Guests != nil {
		if *cmd.Guests <= 0 {
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
		updated.Status = candidate
	}
	if cmd.AssignedLead != nil {
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
	if len(merged.Items) == 0 {
		merged.Items = existing.Items
	}
	return merged
}
