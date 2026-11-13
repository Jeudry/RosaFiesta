package app

import (
	"context"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
)

type Store interface {
	Enabled() bool
	UpsertChecklistItem(ctx context.Context, eventID string, item domain.ChecklistItem) error
	ListChecklistItems(ctx context.Context, eventID string) (map[string]domain.ChecklistItem, error)
	RecordDamageIncident(ctx context.Context, report domain.DamageReport, now time.Time) error
	ListDamageIncidents(ctx context.Context, eventID string) ([]domain.DamageReport, error)
	UpsertOfflineMutation(ctx context.Context, mutation domain.OfflineMutation, now time.Time) error
	ListOfflineMutations(ctx context.Context) ([]domain.OfflineMutation, error)
	MarkAllOfflineMutationsSynced(ctx context.Context) error
	UpsertLead(ctx context.Context, lead domain.Lead, now time.Time) error
	ListLeads(ctx context.Context) ([]domain.Lead, error)
	UpsertSupplier(ctx context.Context, sup domain.Supplier) error
	ListSuppliers(ctx context.Context) ([]domain.Supplier, error)
	UpsertReview(ctx context.Context, rev domain.Review, now time.Time) error
	ListReviews(ctx context.Context) ([]domain.Review, error)
	UpsertBusinessSettings(ctx context.Context, settings domain.BusinessSettings, now time.Time) error
	GetBusinessSettings(ctx context.Context) (domain.BusinessSettings, error)
	UpsertDeliveryZone(ctx context.Context, zone domain.DeliveryZone) error
	ListDeliveryZones(ctx context.Context) ([]domain.DeliveryZone, error)
	UpsertNotification(ctx context.Context, ntf domain.Notification, now time.Time) error
	ListNotifications(ctx context.Context) ([]domain.Notification, error)
	MarkNotificationRead(ctx context.Context, id string) error
	ListInsurancePolicies(ctx context.Context) ([]domain.InsurancePolicy, error)
	UpsertAuditLog(ctx context.Context, aud domain.AuditLog, now time.Time) error
	ListAuditLogs(ctx context.Context) ([]domain.AuditLog, error)
	UpsertBotFAQ(ctx context.Context, faq domain.BotFAQ) error
	ListBotFAQs(ctx context.Context) ([]domain.BotFAQ, error)
	UpsertBotKeyword(ctx context.Context, kw domain.BotKeyword) error
	ListBotKeywords(ctx context.Context) ([]domain.BotKeyword, error)
	UpsertBotThread(ctx context.Context, th domain.BotThread) error
	ListBotThreads(ctx context.Context) ([]domain.BotThread, error)
}

type Service struct {
	mu            sync.RWMutex
	clock         clock.Clock
	store         Store
	octRevenueFn  func(ctx context.Context) int
	logistics     map[string]domain.EventLogistics
	routeStops    []domain.RouteStop
	offlineQueue  []domain.OfflineMutation
	reviews       []domain.Review
	suppliers     []domain.Supplier
	leads         []domain.Lead
	settings      domain.BusinessSettings
	deliveryZones []domain.DeliveryZone
	notifications []domain.Notification
	policies      []domain.InsurancePolicy
	auditLogs     []domain.AuditLog
	botFAQs       []domain.BotFAQ
	botKeywords   []domain.BotKeyword
	botThreads    []domain.BotThread
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

func (s *Service) WithRevenueProvider(fn func(ctx context.Context) int) *Service {
	s.mu.Lock()
	s.octRevenueFn = fn
	s.mu.Unlock()
	return s
}

func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.mu.Unlock()
	return s
}

func (s *Service) Reset(_ context.Context) {
	canonical := domain.CanonicalLogistics()
	logistics := make(map[string]domain.EventLogistics, len(canonical))
	for _, item := range canonical {
		logistics[item.EventID] = cloneEventLogistics(item)
	}
	s.mu.Lock()
	s.logistics = logistics
	s.routeStops = domain.CanonicalRouteStops()
	s.offlineQueue = domain.CanonicalOfflineQueue()
	s.reviews = domain.CanonicalReviews()
	s.suppliers = domain.CanonicalSuppliers()
	s.leads = domain.CanonicalLeads()
	s.settings = domain.CanonicalBusinessSettings()
	s.deliveryZones = domain.CanonicalDeliveryZones()
	s.notifications = domain.CanonicalNotifications()
	s.policies = domain.CanonicalInsurancePolicies()
	s.auditLogs = domain.CanonicalAuditLogs()
	s.botFAQs = domain.CanonicalBotFAQs()
	s.botKeywords = domain.CanonicalBotKeywords()
	s.botThreads = domain.CanonicalBotThreads()
	s.mu.Unlock()
}

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.logistics = make(map[string]domain.EventLogistics)
	s.routeStops = nil
	s.offlineQueue = nil
	s.reviews = nil
	s.suppliers = nil
	s.leads = nil
	s.settings = domain.BusinessSettings{
		ID:          "default",
		CompanyName: "Rosa Fiesta",
	}
	s.deliveryZones = nil
	s.notifications = nil
	s.policies = nil
	s.auditLogs = nil
	s.botFAQs = nil
	s.botKeywords = nil
	s.botThreads = nil
	s.mu.Unlock()
}

func cloneEventLogistics(src domain.EventLogistics) domain.EventLogistics {
	crew := make([]string, len(src.AssignedCrew))
	copy(crew, src.AssignedCrew)
	checklist := make([]domain.ChecklistItem, len(src.Checklist))
	copy(checklist, src.Checklist)
	damages := make([]domain.DamageReport, len(src.DamageReports))
	copy(damages, src.DamageReports)
	src.AssignedCrew = crew
	src.Checklist = checklist
	src.DamageReports = damages
	return src
}

func (s *Service) storeEnabled() (Store, bool) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return nil, false
	}
	return store, store.Enabled()
}

func (s *Service) GetEventLogistics(ctx context.Context, eventID string) (domain.EventLogistics, error) {
	s.mu.Lock()
	logi, ok := s.logistics[eventID]
	store := s.store
	s.mu.Unlock()
	if !ok {
		return domain.EventLogistics{}, domain.ErrOperationNotFound
	}
	logi = cloneEventLogistics(logi)
	if store != nil {
		if store.Enabled() {
			logi = mergePersistedLogistics(ctx, store, eventID, logi)
			s.mu.Lock()
			s.logistics[eventID] = logi
			s.mu.Unlock()
		}
	}
	return logi, nil
}

func mergePersistedLogistics(
	ctx context.Context,
	store Store,
	eventID string,
	logi domain.EventLogistics,
) domain.EventLogistics {
	dbItems, err := store.ListChecklistItems(ctx, eventID)
	if err == nil {
		if len(dbItems) > 0 {
			mergeChecklistSlice(logi.Checklist, dbItems)
		}
	}
	dbDamages, dmgErr := store.ListDamageIncidents(ctx, eventID)
	if dmgErr == nil {
		if len(dbDamages) > 0 {
			logi.DamageReports = dbDamages
		}
	}
	return logi
}

func mergeChecklistSlice(checklist []domain.ChecklistItem, persisted map[string]domain.ChecklistItem) {
	for idx := range checklist {
		dbItem, exists := persisted[checklist[idx].ID]
		if !exists {
			continue
		}
		checklist[idx].Completed = dbItem.Completed
		checklist[idx].CompletedBy = dbItem.CompletedBy
		if dbItem.Completed {
			if dbItem.CompletedAt != "" {
				checklist[idx].CompletedAt = dbItem.CompletedAt
			}
		} else {
			checklist[idx].CompletedAt = ""
		}
	}
}

func (s *Service) ToggleChecklistItem(
	ctx context.Context,
	eventID string,
	itemID string,
	completed bool,
	staffName string,
) (domain.EventLogistics, error) {
	completedLabel := s.clock.Now().Format("03:04 PM")

	s.mu.Lock()
	logi, ok := s.logistics[eventID]
	if !ok {
		s.mu.Unlock()
		return domain.EventLogistics{}, domain.ErrOperationNotFound
	}
	logi = cloneEventLogistics(logi)
	updatedItem, found := applyChecklistToggle(logi.Checklist, itemID, completed, staffName, completedLabel)
	if !found {
		s.mu.Unlock()
		return domain.EventLogistics{}, domain.ErrChecklistNotFound
	}
	s.logistics[eventID] = logi
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			if err := store.UpsertChecklistItem(ctx, eventID, updatedItem); err != nil {
				return domain.EventLogistics{}, err
			}
		}
	}
	return logi, nil
}

func applyChecklistToggle(
	checklist []domain.ChecklistItem,
	itemID string,
	completed bool,
	staffName string,
	completedAtLabel string,
) (domain.ChecklistItem, bool) {
	for idx := range checklist {
		if checklist[idx].ID != itemID {
			continue
		}
		checklist[idx].Completed = completed
		if completed {
			checklist[idx].CompletedBy = strings.TrimSpace(staffName)
			checklist[idx].CompletedAt = completedAtLabel
		} else {
			checklist[idx].CompletedBy = ""
			checklist[idx].CompletedAt = ""
		}
		return checklist[idx], true
	}
	return domain.ChecklistItem{}, false
}

func (s *Service) ListRouteStops() []domain.RouteStop {
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.RouteStop, len(s.routeStops))
	copy(out, s.routeStops)
	return out
}

func (s *Service) ReportDamage(
	ctx context.Context,
	eventID string,
	articleCode string,
	articleName string,
	damagedUnits int,
	missingUnits int,
	replacementRD int,
	notes string,
) (domain.DamageReport, error) {
	if replacementRD < 0 {
		return domain.DamageReport{}, domain.ErrInvalidReplacement
	}
	now := s.clock.Now()
	report := domain.DamageReport{
		ID:            "dmg-" + uuid.NewString()[:8],
		EventID:       strings.TrimSpace(eventID),
		ArticleCode:   strings.TrimSpace(articleCode),
		ArticleName:   strings.TrimSpace(articleName),
		DamagedUnits:  damagedUnits,
		MissingUnits:  missingUnits,
		ReplacementRD: replacementRD,
		Notes:         strings.TrimSpace(notes),
	}

	s.mu.Lock()
	logi, ok := s.logistics[eventID]
	if !ok {
		s.mu.Unlock()
		return domain.DamageReport{}, domain.ErrOperationNotFound
	}
	logi = cloneEventLogistics(logi)
	logi.DamageReports = append(logi.DamageReports, report)
	s.logistics[eventID] = logi
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			if err := store.RecordDamageIncident(ctx, report, now); err != nil {
				return domain.DamageReport{}, err
			}
		}
	}
	return report, nil
}

func (s *Service) ListDamages(ctx context.Context, eventID string) []domain.DamageReport {
	if store, enabled := s.storeEnabled(); enabled {
		if dbList, err := store.ListDamageIncidents(ctx, eventID); err == nil {
			if len(dbList) > 0 {
				return dbList
			}
		}
	}
	filterEvent := strings.TrimSpace(eventID)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.DamageReport, 0)
	for _, logi := range s.logistics {
		if filterEvent != "" {
			if logi.EventID != filterEvent {
				continue
			}
		}
		out = append(out, logi.DamageReports...)
	}
	return out
}

func (s *Service) ListOfflineQueue(ctx context.Context) []domain.OfflineMutation {
	if store, enabled := s.storeEnabled(); enabled {
		if dbQueue, err := store.ListOfflineMutations(ctx); err == nil {
			if len(dbQueue) > 0 {
				s.mu.Lock()
				s.offlineQueue = dbQueue
				s.mu.Unlock()
				return dbQueue
			}
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.OfflineMutation, len(s.offlineQueue))
	copy(out, s.offlineQueue)
	return out
}

func (s *Service) SyncOfflineQueue(ctx context.Context) []domain.OfflineMutation {
	store, enabled := s.storeEnabled()
	if enabled {
		_ = store.MarkAllOfflineMutationsSynced(ctx)
		if dbQueue, err := store.ListOfflineMutations(ctx); err == nil {
			if len(dbQueue) > 0 {
				s.mu.Lock()
				s.offlineQueue = dbQueue
				s.mu.Unlock()
				return dbQueue
			}
		}
	}
	s.mu.Lock()
	for idx := range s.offlineQueue {
		s.offlineQueue[idx].Synced = true
	}
	out := make([]domain.OfflineMutation, len(s.offlineQueue))
	copy(out, s.offlineQueue)
	s.mu.Unlock()
	return out
}
