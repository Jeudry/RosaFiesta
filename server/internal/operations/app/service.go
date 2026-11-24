// Package app orquesta la logística operativa, incidencias, cola sin conexión, CRM y bot WhatsApp.
package app

import (
	"context"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

const defaultWhatsAppAPIBaseURL = "https://graph.facebook.com/v21.0"

// DamagedStockAdjuster ajusta las unidades dañadas o restauradas en inventario.
type DamagedStockAdjuster interface {
	AdjustDamagedStock(ctx context.Context, articleRef string, delta int) error
}

// ReplacementChargeRecorder registra cargos por reposición en el módulo de pagos.
type ReplacementChargeRecorder interface {
	RecordDamageCharge(ctx context.Context, report domain.DamageReport) (string, error)
}

// OfflineMutationDispatcher procesa mutaciones encoladas cuando la aplicación vuelve a estar en línea.
type OfflineMutationDispatcher interface {
	DispatchMutation(ctx context.Context, opType string, payloadJSON string, caller platformhttp.Principal) error
}

// ReportSnapshotProvider calcula métricas reales mensuales desde módulos vecinos para reportes.
type ReportSnapshotProvider interface {
	BuildReportSnapshot(ctx context.Context, months int, now time.Time) (domain.ReportSnapshot, error)
}

// DeletableStore abstrae eliminaciones persistentes de proveedores y reglas de bot.
type DeletableStore interface {
	DeleteSupplier(ctx context.Context, id string) error
	DeleteBotFAQ(ctx context.Context, id string) error
	DeleteBotKeyword(ctx context.Context, id string) error
}

// ReminderStore abstrae el registro idempotente de recordatorios enviados.
type ReminderStore interface {
	HasSentEventReminder(ctx context.Context, eventID, reminderKind string) (bool, error)
	InsertSentEventReminder(ctx context.Context, eventID, reminderKind string, sentAt time.Time) error
}

// Store abstrae la persistencia PostgreSQL del módulo de operaciones.
type Store interface {
	Enabled() bool
	UpsertEventLogistics(ctx context.Context, logi domain.EventLogistics, zone, status, updatedAtLabel string) error
	ListEventLogistics(ctx context.Context) ([]domain.EventLogistics, error)
	ReplaceDeliveryStops(ctx context.Context, eventID string, stops []domain.RouteStop) error
	ListDeliveryStops(ctx context.Context) ([]domain.RouteStop, error)
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

// ReportDamageCommand encapsula los datos para registrar una incidencia de daño o extravío.
type ReportDamageCommand struct {
	EventID       string
	ArticleCode   string
	ArticleName   string
	DamagedUnits  int
	MissingUnits  int
	ReplacementRD int
	Notes         string
	PhotoURL      string
}

// EnqueueOfflineMutationCommand encapsula una mutación generada sin conexión.
type EnqueueOfflineMutationCommand struct {
	ActionLabel    string
	EntityCode     string
	OperationType  string
	PayloadJSON    string
	IdempotencyKey string
}

// Service coordina logística de campo, incidencias, cola sin conexión, CRM y bot WhatsApp.
type Service struct {
	mu                  sync.RWMutex
	clock               clock.Clock
	store               Store
	octRevenueFn        func(ctx context.Context) int
	reportSnapshotFn    ReportSnapshotProvider
	stockAdjuster       DamagedStockAdjuster
	chargeRecorder      ReplacementChargeRecorder
	offlineDispatcher   OfflineMutationDispatcher
	realtimePub         realtimedomain.Publisher
	whatsAppPhoneID     string
	whatsAppAccessToken string
	whatsAppVerifyToken string
	whatsAppAppSecret   string
	whatsAppAPIBaseURL  string
	logistics           map[string]domain.EventLogistics
	routeStops          []domain.RouteStop
	offlineQueue        []domain.OfflineMutation
	reviews             []domain.Review
	suppliers           []domain.Supplier
	leads               []domain.Lead
	settings            domain.BusinessSettings
	deliveryZones       []domain.DeliveryZone
	notifications       []domain.Notification
	policies            []domain.InsurancePolicy
	auditLogs           []domain.AuditLog
	botFAQs             []domain.BotFAQ
	botKeywords         []domain.BotKeyword
	botThreads          []domain.BotThread
	sentReminders       map[string]bool
}

// NewService inicializa el servicio operativo con los datos de demostración.
func NewService() *Service {
	svc := &Service{
		clock:              clock.System{},
		whatsAppAPIBaseURL: defaultWhatsAppAPIBaseURL,
	}
	svc.Reset(context.Background())
	return svc
}

// WithClock inyecta un reloj determinístico para marcas temporales operativas.
func (s *Service) WithClock(clk clock.Clock) *Service {
	if clk != nil {
		s.mu.Lock()
		s.clock = clk
		s.mu.Unlock()
	}
	return s
}

// WithRevenueProvider conecta la lectura en vivo de ingresos mensuales para reportes.
func (s *Service) WithRevenueProvider(fn func(ctx context.Context) int) *Service {
	s.mu.Lock()
	s.octRevenueFn = fn
	s.mu.Unlock()
	return s
}

// WithReportSnapshotProvider conecta agregaciones mensuales reales para exportación de reportes.
func (s *Service) WithReportSnapshotProvider(provider ReportSnapshotProvider) *Service {
	s.mu.Lock()
	s.reportSnapshotFn = provider
	s.mu.Unlock()
	return s
}

// WithStore conecta el repositorio PostgreSQL de operaciones.
func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.mu.Unlock()
	return s
}

// WithDamagedStockAdjuster registra el puerto para descontar/restaurar inventario dañado.
func (s *Service) WithDamagedStockAdjuster(adjuster DamagedStockAdjuster) *Service {
	s.mu.Lock()
	s.stockAdjuster = adjuster
	s.mu.Unlock()
	return s
}

// WithReplacementChargeRecorder registra el puerto para facturar cargos por reposición.
func (s *Service) WithReplacementChargeRecorder(recorder ReplacementChargeRecorder) *Service {
	s.mu.Lock()
	s.chargeRecorder = recorder
	s.mu.Unlock()
	return s
}

// WithOfflineDispatcher registra el puerto encargado de ejecutar mutaciones diferidas.
func (s *Service) WithOfflineDispatcher(dispatcher OfflineMutationDispatcher) *Service {
	s.mu.Lock()
	s.offlineDispatcher = dispatcher
	s.mu.Unlock()
	return s
}

// WithRealtimePublisher conecta el bus de eventos SSE para notificaciones en tiempo real.
func (s *Service) WithRealtimePublisher(pub realtimedomain.Publisher) *Service {
	s.mu.Lock()
	s.realtimePub = pub
	s.mu.Unlock()
	return s
}

// WithWhatsAppConfig configura las credenciales oficiales de Meta WhatsApp Cloud API.
func (s *Service) WithWhatsAppConfig(phoneNumberID, accessToken, verifyToken string) *Service {
	s.mu.Lock()
	s.whatsAppPhoneID = strings.TrimSpace(phoneNumberID)
	s.whatsAppAccessToken = strings.TrimSpace(accessToken)
	s.whatsAppVerifyToken = strings.TrimSpace(verifyToken)
	s.mu.Unlock()
	return s
}

// WithWhatsAppAppSecret configura el secreto de aplicación de Meta para verificar HMAC en webhooks.
func (s *Service) WithWhatsAppAppSecret(secret string) *Service {
	s.mu.Lock()
	s.whatsAppAppSecret = strings.TrimSpace(secret)
	s.mu.Unlock()
	return s
}

// WithWhatsAppAPIBaseURL permite sustituir el host de Graph API en pruebas unitarias.
func (s *Service) WithWhatsAppAPIBaseURL(baseURL string) *Service {
	trimmed := strings.TrimRight(strings.TrimSpace(baseURL), "/")
	if trimmed == "" {
		return s
	}
	s.mu.Lock()
	s.whatsAppAPIBaseURL = trimmed
	s.mu.Unlock()
	return s
}

// Reset restablece el estado operativo con los datos de demostración.
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

// ResetEmpty vacía las colecciones operativas en memoria.
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
	s.sentReminders = make(map[string]bool)
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

// GetEventLogistics recupera la ficha logística, checklist e incidencias de un evento.
func (s *Service) GetEventLogistics(ctx context.Context, eventID string) (domain.EventLogistics, error) {
	s.mu.Lock()
	logi, ok := s.logistics[eventID]
	store := s.store
	s.mu.Unlock()
	if !ok {
		loaded, found := s.loadLogisticsFromStore(ctx, store, eventID)
		if !found {
			return domain.EventLogistics{}, domain.ErrOperationNotFound
		}
		logi = loaded
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

func (s *Service) loadLogisticsFromStore(ctx context.Context, store Store, eventID string) (domain.EventLogistics, bool) {
	if store == nil {
		return domain.EventLogistics{}, false
	}
	if !store.Enabled() {
		return domain.EventLogistics{}, false
	}
	rows, err := store.ListEventLogistics(ctx)
	if err != nil {
		return domain.EventLogistics{}, false
	}
	for _, item := range rows {
		if item.EventID == eventID {
			item.Checklist = buildDefaultEventChecklist(eventID)
			return item, true
		}
	}
	return domain.EventLogistics{}, false
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

// ToggleChecklistItem actualiza el estado de una tarea del checklist y lo persiste.
func (s *Service) ToggleChecklistItem(
	ctx context.Context,
	eventID string,
	itemID string,
	completed bool,
	staffName string,
) (domain.EventLogistics, error) {
	if _, err := s.GetEventLogistics(ctx, eventID); err != nil {
		return domain.EventLogistics{}, err
	}
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

// ListRouteStops devuelve las paradas de entrega y recogida del itinerario diario.
func (s *Service) ListRouteStops() []domain.RouteStop {
	if store, enabled := s.storeEnabled(); enabled {
		if dbStops, err := store.ListDeliveryStops(context.Background()); err == nil {
			if len(dbStops) > 0 {
				return dbStops
			}
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.RouteStop, len(s.routeStops))
	copy(out, s.routeStops)
	return out
}

// ReportDamage registra un daño o faltante post-evento usando parámetros posicionales.
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
	return s.ReportDamageWithDetails(ctx, ReportDamageCommand{
		EventID:       eventID,
		ArticleCode:   articleCode,
		ArticleName:   articleName,
		DamagedUnits:  damagedUnits,
		MissingUnits:  missingUnits,
		ReplacementRD: replacementRD,
		Notes:         notes,
	})
}

// ReportDamageWithDetails registra daño/pérdida con foto de evidencia, ajusta stock y crea cargo.
func (s *Service) ReportDamageWithDetails(
	ctx context.Context,
	cmd ReportDamageCommand,
) (domain.DamageReport, error) {
	if cmd.ReplacementRD < 0 {
		return domain.DamageReport{}, domain.ErrInvalidReplacement
	}
	eventID := strings.TrimSpace(cmd.EventID)
	if _, err := s.GetEventLogistics(ctx, eventID); err != nil {
		return domain.DamageReport{}, err
	}

	now := s.clock.Now()
	report := domain.DamageReport{
		ID:            "dmg-" + uuid.NewString()[:8],
		EventID:       eventID,
		ArticleCode:   strings.TrimSpace(cmd.ArticleCode),
		ArticleName:   strings.TrimSpace(cmd.ArticleName),
		DamagedUnits:  cmd.DamagedUnits,
		MissingUnits:  cmd.MissingUnits,
		ReplacementRD: cmd.ReplacementRD,
		Notes:         strings.TrimSpace(cmd.Notes),
		PhotoURL:      strings.TrimSpace(cmd.PhotoURL),
		Status:        "open",
	}

	if err := s.applyDamageSideEffects(ctx, &report); err != nil {
		return domain.DamageReport{}, err
	}
	if err := s.storeAndSaveDamageReport(ctx, eventID, report, now); err != nil {
		return domain.DamageReport{}, err
	}
	s.publishOpsEvent(realtimedomain.EventDamageReported, report.ID, "Incidencia de daño registrada en "+report.ArticleName, map[string]any{
		"incidentId": report.ID,
		"eventId":    report.EventID,
		"article":    report.ArticleCode,
	})
	return report, nil
}

// ListDamages lista las incidencias registradas opcionalmente filtradas por evento.
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

// ListOfflineQueue lista las mutaciones de campo pendientes o ya sincronizadas.
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

// SyncOfflineQueue sincroniza las mutaciones fuera de línea pendientes en la cola actual.
func (s *Service) SyncOfflineQueue(ctx context.Context) []domain.OfflineMutation {
	synced, _ := s.SyncOfflineMutations(ctx, nil, platformhttp.Principal{Role: "admin"})
	return synced
}
