// Package app orquesta los casos de uso financieros, verificación de pagos, NCF, pasarela y reembolsos.
package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"sync/atomic"
	"time"

	"github.com/google/uuid"

	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

const (
	canonicalPaymentsSumBaselineRD = 12352
	canonicalRetailBaselineRD      = 98
	canonicalReceivableOffsetRD    = 1669
	defaultReceivableBaselineRD    = 7724
	defaultActiveEventsCount       = 4
	defaultNewQuoteRequests        = 2
	defaultStripeAPIBaseURL        = "https://api.stripe.com"
)

// EventMetricsReader expone una vista de lectura de eventos para calcular cuentas por cobrar.
type EventMetricsReader interface {
	AllEventsSnapshot(ctx context.Context) []eventsdomain.Event
}

// QuoteMetricsReader expone el conteo de cotizaciones pendientes de revisión.
type QuoteMetricsReader interface {
	PendingQuotesCount(ctx context.Context) int
}

// VerifiedPaymentSynchronizer sincroniza otros módulos cuando un pago queda confirmado o verificado.
type VerifiedPaymentSynchronizer interface {
	OnPaymentVerified(ctx context.Context, payment domain.Payment) error
}

// EmailPreferenceChecker verifica si el cliente autoriza el envío de recibos por correo.
type EmailPreferenceChecker interface {
	AllowsEmailReceipts(ctx context.Context, clientID string, email string) bool
}

// DamageExpensesReader expone el total de gastos por daños o reposiciones registradas.
type DamageExpensesReader interface {
	TotalDamageExpensesRD(ctx context.Context) int
}

// TaxRateProvider provee el porcentaje vigente de ITBIS configurado en ajustes del negocio.
type TaxRateProvider interface {
	CurrentITBISPercent(ctx context.Context) int
}

// ApproveRefundOptions encapsula los parámetros adicionales al aprobar un reembolso.
type ApproveRefundOptions struct {
	ApproverName     string
	Method           string
	ReleaseInventory bool
	NotifyWhatsApp   bool
	ClientMessage    string
}

// RefundApprovedHook ejecuta efectos posteriores (liberar inventario y notificar al cliente) al aprobar un reembolso.
type RefundApprovedHook interface {
	OnRefundApproved(ctx context.Context, refund domain.Refund, opts ApproveRefundOptions)
}

// PaymentRejectedHook ejecuta notificaciones al cliente cuando finanzas rechaza un pago pendiente.
type PaymentRejectedHook interface {
	OnPaymentRejected(ctx context.Context, payment domain.Payment, reason string)
}

type ncfSequenceAllocator interface {
	AllocateNextNCF(ctx context.Context, ncfType string) (string, error)
}

// Store abstrae la persistencia durable de pagos y reembolsos.
type Store interface {
	Enabled() bool
	ListPayments(ctx context.Context) ([]domain.Payment, error)
	UpsertPayment(ctx context.Context, payment domain.Payment, now time.Time) error
	ListRefunds(ctx context.Context) ([]domain.Refund, error)
	UpsertRefund(ctx context.Context, refund domain.Refund, now time.Time) error
}

// RegisterPaymentCommand encapsula los datos de entrada para registrar un pago.
type RegisterPaymentCommand struct {
	EventID            string
	EventTitle         string
	ClientEmail        string
	ClientName         string
	AmountRD           int
	Method             string
	Kind               string
	BankName           string
	ReferenceCode      string
	VoucherURL         string
	ReferenceNumber    string
	NCFType            string
	ClientRNC          string
	SecurityDepositDOP int
	ContactPhone       string
}

// CreateCardSessionCommand solicita una sesión de pago electrónico con tarjeta.
type CreateCardSessionCommand struct {
	EventID      string
	EventTitle   string
	AmountRD     int
	Kind         string
	NCFType      string
	ClientRNC    string
	ContactPhone string
}

// Service coordina pagos, emisión de NCF, pasarela de tarjetas, exporte DGII y reembolsos.
type Service struct {
	mu                  sync.RWMutex
	verifyMu            sync.Mutex
	clock               clock.Clock
	store               Store
	eventsReader        EventMetricsReader
	quotesReader        QuoteMetricsReader
	damageReader        DamageExpensesReader
	taxRateProvider     TaxRateProvider
	refundHook          RefundApprovedHook
	rejectedHook        PaymentRejectedHook
	verifiedSync        VerifiedPaymentSynchronizer
	emailPrefChecker    EmailPreferenceChecker
	emailSender         mailer.Sender
	realtimePub         realtimedomain.Publisher
	stripeSecretKey     string
	stripeWebhookSecret string
	publicBaseURL       string
	stripeAPIBaseURL    string
	payments            map[string]domain.Payment
	order               []string
	refunds             map[string]domain.Refund
	refundOrder         []string
	ncfSeqB01           atomic.Int64
	ncfSeqB02           atomic.Int64
	ncfValidUntil       map[string]string
	nextSeq             int
	zeroBaseline        bool
}

// NewService crea una instancia de Service inicializada con el estado base.
func NewService() *Service {
	svc := &Service{
		clock:            clock.System{},
		stripeAPIBaseURL: defaultStripeAPIBaseURL,
	}
	svc.Reset(context.Background())
	return svc
}

// WithClock inyecta un reloj determinístico en el servicio.
func (s *Service) WithClock(clk clock.Clock) *Service {
	if clk != nil {
		s.mu.Lock()
		s.clock = clk
		s.mu.Unlock()
	}
	return s
}

// WithMetricsReaders conecta lectores externos de eventos y cotizaciones para métricas.
func (s *Service) WithMetricsReaders(eventsReader EventMetricsReader, quotesReader QuoteMetricsReader) *Service {
	s.mu.Lock()
	s.eventsReader = eventsReader
	s.quotesReader = quotesReader
	s.mu.Unlock()
	return s
}

// WithStore conecta el repositorio durable de PostgreSQL y sincroniza el estado inicial.
func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.syncFromStoreLocked(context.Background())
	s.mu.Unlock()
	return s
}

// WithVerifiedSynchronizer registra el sincronizador invocado cuando un pago se verifica.
func (s *Service) WithVerifiedSynchronizer(syncer VerifiedPaymentSynchronizer) *Service {
	s.mu.Lock()
	s.verifiedSync = syncer
	s.mu.Unlock()
	return s
}

// WithTaxRateProvider registra el proveedor dinámico del porcentaje de ITBIS.
func (s *Service) WithTaxRateProvider(provider TaxRateProvider) *Service {
	s.mu.Lock()
	s.taxRateProvider = provider
	s.mu.Unlock()
	return s
}

// WithRefundApprovedHook registra el hook posterior a la aprobación de reembolsos.
func (s *Service) WithRefundApprovedHook(hook RefundApprovedHook) *Service {
	s.mu.Lock()
	s.refundHook = hook
	s.mu.Unlock()
	return s
}

// WithPaymentRejectedHook registra el hook posterior al rechazo de comprobantes bancarios.
func (s *Service) WithPaymentRejectedHook(hook PaymentRejectedHook) *Service {
	s.mu.Lock()
	s.rejectedHook = hook
	s.mu.Unlock()
	return s
}

// WithEmailSender registra el cliente SMTP encargado de enviar recibos de pago.
func (s *Service) WithEmailSender(sender mailer.Sender) *Service {
	s.mu.Lock()
	s.emailSender = sender
	s.mu.Unlock()
	return s
}

// WithRealtimePublisher registra el publicador de eventos en tiempo real SSE.
func (s *Service) WithRealtimePublisher(pub realtimedomain.Publisher) *Service {
	s.mu.Lock()
	s.realtimePub = pub
	s.mu.Unlock()
	return s
}

// WithStripeSecretKey configura la clave secreta de Stripe para sesiones con tarjeta.
func (s *Service) WithStripeSecretKey(secretKey string) *Service {
	s.mu.Lock()
	s.stripeSecretKey = strings.TrimSpace(secretKey)
	s.mu.Unlock()
	return s
}

// WithStripeWebhookSecret configura el secreto HMAC para verificar webhooks de Stripe.
func (s *Service) WithStripeWebhookSecret(secret string) *Service {
	s.mu.Lock()
	s.stripeWebhookSecret = strings.TrimSpace(secret)
	s.mu.Unlock()
	return s
}

// WithEmailPreferenceChecker conecta el lector de preferencias de recibos por correo.
func (s *Service) WithEmailPreferenceChecker(checker EmailPreferenceChecker) *Service {
	s.mu.Lock()
	s.emailPrefChecker = checker
	s.mu.Unlock()
	return s
}

// WithDamageExpensesReader conecta el lector de costos de incidencias/reposiciones.
func (s *Service) WithDamageExpensesReader(reader DamageExpensesReader) *Service {
	s.mu.Lock()
	s.damageReader = reader
	s.mu.Unlock()
	return s
}

// WithPublicBaseURL configura la URL pública base usada en retornos de pasarela.
func (s *Service) WithPublicBaseURL(baseURL string) *Service {
	trimmed := strings.TrimRight(strings.TrimSpace(baseURL), "/")
	s.mu.Lock()
	s.publicBaseURL = trimmed
	s.mu.Unlock()
	return s
}

// WithStripeAPIBaseURL permite sobrescribir el endpoint base de Stripe en pruebas unitarias.
func (s *Service) WithStripeAPIBaseURL(apiBaseURL string) *Service {
	trimmed := strings.TrimRight(strings.TrimSpace(apiBaseURL), "/")
	if trimmed == "" {
		return s
	}
	s.mu.Lock()
	s.stripeAPIBaseURL = trimmed
	s.mu.Unlock()
	return s
}

// Reset restablece el estado en memoria con los pagos y reembolsos de demostración.
func (s *Service) Reset(_ context.Context) {
	canonicalPay := domain.CanonicalPayments()
	payments := make(map[string]domain.Payment, len(canonicalPay))
	order := make([]string, 0, len(canonicalPay))
	for _, item := range canonicalPay {
		payments[item.ID] = item
		order = append(order, item.ID)
	}
	canonicalRef := domain.CanonicalRefunds()
	refunds := make(map[string]domain.Refund, len(canonicalRef))
	refundOrder := make([]string, 0, len(canonicalRef))
	for _, ref := range canonicalRef {
		refunds[ref.ID] = ref
		refundOrder = append(refundOrder, ref.ID)
	}
	s.mu.Lock()
	s.payments = payments
	s.order = order
	s.refunds = refunds
	s.refundOrder = refundOrder
	s.nextSeq = 200 + len(canonicalPay)
	s.zeroBaseline = false
	s.mu.Unlock()
}

// ResetEmpty limpia todos los registros en memoria para iniciar en modo vacío.
func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.payments = make(map[string]domain.Payment)
	s.order = nil
	s.refunds = make(map[string]domain.Refund)
	s.refundOrder = nil
	s.nextSeq = 200
	s.zeroBaseline = true
	s.mu.Unlock()
}

func (s *Service) syncFromStoreLocked(ctx context.Context) {
	if s.store == nil {
		return
	}
	if !s.store.Enabled() {
		return
	}
	s.syncPaymentsLocked(ctx)
	s.syncRefundsLocked(ctx)
}

func (s *Service) syncPaymentsLocked(ctx context.Context) {
	dbPayments, err := s.store.ListPayments(ctx)
	if err != nil {
		return
	}
	for _, dbItem := range dbPayments {
		_, exists := s.payments[dbItem.ID]
		s.payments[dbItem.ID] = dbItem
		if !exists {
			s.order = append(s.order, dbItem.ID)
		}
	}
	minNext := 200 + len(s.order)
	if s.nextSeq < minNext {
		s.nextSeq = minNext
	}
}

func (s *Service) syncRefundsLocked(ctx context.Context) {
	dbRefunds, err := s.store.ListRefunds(ctx)
	if err != nil {
		return
	}
	for _, dbRef := range dbRefunds {
		_, exists := s.refunds[dbRef.ID]
		s.refunds[dbRef.ID] = dbRef
		if !exists {
			s.refundOrder = append(s.refundOrder, dbRef.ID)
		}
	}
}

// GetSummary calcula las métricas financieras agregadas para el tablero administrativo.
func (s *Service) GetSummary(ctx context.Context) domain.FinanceSummary {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	paidSum, pendingCount := s.aggregatePaymentsLocked()
	approvedRefunds := s.aggregateApprovedRefundsLocked()
	eventsReader := s.eventsReader
	quotesReader := s.quotesReader
	useZeroBaseline := s.zeroBaseline
	s.mu.Unlock()

	if useZeroBaseline {
		return buildZeroBaselineSummary(ctx, paidSum, approvedRefunds, pendingCount, eventsReader, quotesReader)
	}
	octRevenue := clampNonNegative((paidSum + canonicalRetailBaselineRD) - approvedRefunds)
	extraPaid := paidSum - canonicalPaymentsSumBaselineRD
	activeEvents, receivable := computeReceivableMetrics(ctx, eventsReader, extraPaid)
	newQuotes := defaultNewQuoteRequests
	if quotesReader != nil {
		newQuotes = quotesReader.PendingQuotesCount(ctx)
	}
	return domain.FinanceSummary{
		MonthLabel:          "Octubre 2026",
		OctRevenueRD:        octRevenue,
		AccountsReceivable:  receivable,
		ActiveEventsCount:   activeEvents,
		NewQuoteRequests:    newQuotes,
		PendingVerification: pendingCount,
	}
}

func buildZeroBaselineSummary(
	ctx context.Context,
	paidSum int,
	approvedRefunds int,
	pendingCount int,
	eventsReader EventMetricsReader,
	quotesReader QuoteMetricsReader,
) domain.FinanceSummary {
	octRevenue := clampNonNegative(paidSum - approvedRefunds)
	activeEvents := 0
	receivable := 0
	if eventsReader != nil {
		for _, evt := range eventsReader.AllEventsSnapshot(ctx) {
			if evt.IsActive() {
				activeEvents++
				receivable += evt.BalanceRD()
			}
		}
	}
	newQuotes := 0
	if quotesReader != nil {
		newQuotes = quotesReader.PendingQuotesCount(ctx)
	}
	return domain.FinanceSummary{
		MonthLabel:          "Mes actual",
		OctRevenueRD:        octRevenue,
		AccountsReceivable:  receivable,
		ActiveEventsCount:   activeEvents,
		NewQuoteRequests:    newQuotes,
		PendingVerification: pendingCount,
	}
}

func (s *Service) aggregatePaymentsLocked() (paidSum int, pendingCount int) {
	for _, id := range s.order {
		item := s.payments[id]
		if item.IsPendingVerification() {
			pendingCount++
		}
		if s.shouldCountPaymentInSummary(id, item) {
			paidSum += item.AmountRD
		}
	}
	return paidSum, pendingCount
}

func (s *Service) shouldCountPaymentInSummary(id string, item domain.Payment) bool {
	if s.zeroBaseline {
		return item.IsVerifiedOrConfirmed()
	}
	if isCanonicalSeedPaymentID(id) {
		isExcluded := item.Status == domain.PaymentFailed
		if isExcluded {
			return false
		}
		return item.Status != domain.PaymentRejected
	}
	return item.IsVerifiedOrConfirmed()
}

func isCanonicalSeedPaymentID(id string) bool {
	switch id {
	case "pay-boda-anticipo", "pay-quince-anticipo", "pay-grad-anticipo", "pay-gala-total":
		return true
	default:
		return false
	}
}

func (s *Service) aggregateApprovedRefundsLocked() int {
	total := 0
	for _, id := range s.refundOrder {
		ref := s.refunds[id]
		if ref.Status == domain.RefundStatusApproved {
			total += ref.RefundDop
		}
	}
	return total
}

func computeReceivableMetrics(
	ctx context.Context,
	eventsReader EventMetricsReader,
	extraPaid int,
) (activeCount int, receivableRD int) {
	if eventsReader == nil {
		return defaultActiveEventsCount, clampNonNegative(defaultReceivableBaselineRD - extraPaid)
	}
	unpaidBalance := 0
	for _, evt := range eventsReader.AllEventsSnapshot(ctx) {
		if evt.IsActive() {
			activeCount++
			unpaidBalance += evt.BalanceRD()
		}
	}
	receivable := clampNonNegative((unpaidBalance - canonicalReceivableOffsetRD) - extraPaid)
	return activeCount, receivable
}

func clampNonNegative(value int) int {
	if value < 0 {
		return 0
	}
	return value
}

// ListPayments lista todos los pagos visibles para administración.
func (s *Service) ListPayments(eventID string) []domain.Payment {
	admin := platformhttp.Principal{Role: "admin"}
	return s.ListPaymentsForCaller(context.Background(), eventID, admin, true)
}

// ListPaymentsForCaller lista los pagos visibles según el rol del usuario autenticado.
func (s *Service) ListPaymentsForCaller(
	ctx context.Context,
	eventID string,
	caller platformhttp.Principal,
	authenticated bool,
) []domain.Payment {
	if !authenticated {
		return []domain.Payment{}
	}
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	snapshot := make([]domain.Payment, 0, len(s.order))
	for _, id := range s.order {
		snapshot = append(snapshot, s.payments[id])
	}
	s.mu.Unlock()

	filterEventID := strings.TrimSpace(eventID)
	out := make([]domain.Payment, 0, len(snapshot))
	for _, item := range snapshot {
		if !matchesPaymentFilter(item, filterEventID, caller) {
			continue
		}
		out = append(out, item)
	}
	return out
}

func matchesPaymentFilter(item domain.Payment, eventID string, caller platformhttp.Principal) bool {
	if eventID != "" {
		if item.EventID != eventID {
			return false
		}
	}
	if caller.IsAdminOrStaff() {
		return true
	}
	return item.BelongsToClient(caller.ID.String(), caller.Email, caller.FullName)
}

// RegisterPayment registra un nuevo pago usando credenciales de administrador.
func (s *Service) RegisterPayment(
	eventID string,
	eventTitle string,
	clientName string,
	amountRD int,
	method domain.PaymentMethod,
	kind domain.PaymentKind,
	referenceCode string,
) (domain.Payment, error) {
	cmd := RegisterPaymentCommand{
		EventID:       eventID,
		EventTitle:    eventTitle,
		ClientName:    clientName,
		AmountRD:      amountRD,
		Method:        string(method),
		Kind:          string(kind),
		ReferenceCode: referenceCode,
	}
	admin := platformhttp.Principal{Role: "admin"}
	return s.RegisterPaymentForCaller(context.Background(), cmd, admin)
}

// RegisterPaymentForCaller valida, registra y notifica un pago de transferencia, tarjeta o efectivo.
func (s *Service) RegisterPaymentForCaller(
	ctx context.Context,
	cmd RegisterPaymentCommand,
	caller platformhttp.Principal,
) (domain.Payment, error) {
	itbisRate := s.resolveITBISPercent(ctx)
	prepared, err := validatePaymentRegistrationWithRate(cmd, itbisRate)
	if err != nil {
		return domain.Payment{}, err
	}
	if authErr := validateCallerPaymentMethod(prepared.method, caller); authErr != nil {
		return domain.Payment{}, authErr
	}
	if ownerErr := s.verifyClientOwnsEvent(ctx, cmd.EventID, caller); ownerErr != nil {
		return domain.Payment{}, ownerErr
	}
	now := s.clock.Now()
	effectiveRef := resolveEffectiveReference(cmd.ReferenceCode, cmd.ReferenceNumber)

	if existing, found, dupErr := s.findDuplicatePayment(ctx, effectiveRef, caller); found {
		return existing, dupErr
	}

	ncfNumber := ""
	if prepared.method != domain.MethodTransfer {
		allocated, allocErr := s.allocateNextNCF(ctx, prepared.ncfType)
		if allocErr != nil {
			return domain.Payment{}, allocErr
		}
		ncfNumber = allocated
	}

	payment, store, dupErr := s.insertRegisteredPayment(ctx, cmd, prepared, caller, now, ncfNumber, effectiveRef)
	if dupErr != nil {
		return payment, dupErr
	}
	if err := persistPaymentIfEnabled(ctx, store, payment, now); err != nil {
		return domain.Payment{}, err
	}
	if err := s.dispatchRegistrationSideEffects(ctx, payment); err != nil {
		return domain.Payment{}, err
	}
	return payment, nil
}

func validateCallerPaymentMethod(method domain.PaymentMethod, caller platformhttp.Principal) error {
	if caller.IsClient() {
		if method == domain.MethodCard {
			return domain.ErrCardCheckoutRequired
		}
		if method == domain.MethodCash {
			return domain.ErrClientCashRestricted
		}
		return nil
	}
	isImmediateMethod := method == domain.MethodCash
	if !isImmediateMethod {
		isImmediateMethod = method == domain.MethodCard
	}
	if !isImmediateMethod {
		return nil
	}
	if hasPaymentOrFinanceAccess(caller) {
		return nil
	}
	return domain.ErrPaymentForbidden
}

func hasPaymentOrFinanceAccess(caller platformhttp.Principal) bool {
	if caller.Role == "admin" {
		return true
	}
	if caller.HasPermission("pagos") {
		return true
	}
	return caller.HasPermission("finanzas")
}

func (s *Service) verifyClientOwnsEvent(
	ctx context.Context,
	eventID string,
	caller platformhttp.Principal,
) error {
	if !caller.IsClient() {
		return nil
	}
	s.mu.RLock()
	eventsReader := s.eventsReader
	s.mu.RUnlock()
	if eventsReader == nil {
		return nil
	}
	trimmedEventID := strings.TrimSpace(eventID)
	if trimmedEventID == "" {
		return domain.ErrPaymentForbidden
	}
	snapshot := eventsReader.AllEventsSnapshot(ctx)
	for _, evt := range snapshot {
		if evt.ID != trimmedEventID {
			continue
		}
		if evt.BelongsToClient(caller.ID, caller.Email, caller.FullName) {
			return nil
		}
		return domain.ErrPaymentForbidden
	}
	return domain.ErrPaymentForbidden
}

func (s *Service) resolveITBISPercent(ctx context.Context) int {
	s.mu.RLock()
	provider := s.taxRateProvider
	s.mu.RUnlock()
	if provider == nil {
		return 18
	}
	rate := provider.CurrentITBISPercent(ctx)
	if rate < 0 {
		return 18
	}
	return rate
}

type preparedPaymentData struct {
	method     domain.PaymentMethod
	bankName   string
	kind       domain.PaymentKind
	ncfType    string
	clientRNC  string
	subtotal   int
	itbis      int
	depositDOP int
}

func validatePaymentRegistrationWithRate(cmd RegisterPaymentCommand, itbisPercent int) (preparedPaymentData, error) {
	if cmd.AmountRD <= 0 {
		return preparedPaymentData{}, domain.ErrInvalidPaymentAmount
	}
	normalizedMethod, bankName, err := domain.NormalizePaymentMethod(cmd.Method, cmd.BankName)
	if err != nil {
		return preparedPaymentData{}, err
	}
	ncfType, cleanedRNC, ncfErr := domain.ValidateRNCForNCF(cmd.NCFType, cmd.ClientRNC)
	if ncfErr != nil {
		return preparedPaymentData{}, ncfErr
	}
	deposit := clampSecurityDeposit(cmd.SecurityDepositDOP, cmd.AmountRD)
	subtotal, itbis := domain.ComputeDominicanTaxBreakdownWithRate(cmd.AmountRD, deposit, itbisPercent)
	return preparedPaymentData{
		method:     normalizedMethod,
		bankName:   bankName,
		kind:       domain.NormalizePaymentKind(cmd.Kind),
		ncfType:    ncfType,
		clientRNC:  cleanedRNC,
		subtotal:   subtotal,
		itbis:      itbis,
		depositDOP: deposit,
	}, nil
}

func clampSecurityDeposit(deposit, amount int) int {
	if deposit < 0 {
		return 0
	}
	if deposit > amount {
		return amount
	}
	return deposit
}

func resolveEffectiveReference(refCode, refNumber string) string {
	cleanCode := strings.TrimSpace(refCode)
	if cleanCode != "" {
		return cleanCode
	}
	return strings.TrimSpace(refNumber)
}

func (s *Service) findDuplicatePayment(
	ctx context.Context,
	effectiveRef string,
	caller platformhttp.Principal,
) (domain.Payment, bool, error) {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	existing, found, dupErr := s.findExistingByReferenceLocked(effectiveRef, caller)
	s.mu.Unlock()
	return existing, found, dupErr
}

func (s *Service) insertRegisteredPayment(
	ctx context.Context,
	cmd RegisterPaymentCommand,
	prep preparedPaymentData,
	caller platformhttp.Principal,
	now time.Time,
	ncfNumber string,
	effectiveRef string,
) (domain.Payment, Store, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	if existing, found, dupErr := s.findExistingByReferenceLocked(effectiveRef, caller); found {
		return existing, nil, dupErr
	}
	payment := s.buildNewPaymentLocked(cmd, prep, caller, now, ncfNumber, effectiveRef)
	s.payments[payment.ID] = payment
	s.order = append(s.order, payment.ID)
	return payment, s.store, nil
}

func (s *Service) findExistingByReferenceLocked(
	rawRef string,
	caller platformhttp.Principal,
) (domain.Payment, bool, error) {
	cleanRef := strings.TrimSpace(rawRef)
	if cleanRef == "" {
		return domain.Payment{}, false, nil
	}
	for _, id := range s.order {
		item := s.payments[id]
		if !strings.EqualFold(item.ReferenceCode, cleanRef) {
			continue
		}
		if caller.IsClient() {
			if !item.BelongsToClient(caller.ID.String(), caller.Email, caller.FullName) {
				return domain.Payment{}, true, domain.ErrPaymentForbidden
			}
		}
		return item, true, nil
	}
	return domain.Payment{}, false, nil
}

func (s *Service) buildNewPaymentLocked(
	cmd RegisterPaymentCommand,
	prep preparedPaymentData,
	caller platformhttp.Principal,
	now time.Time,
	ncfNumber string,
	effectiveRef string,
) domain.Payment {
	s.nextSeq++
	suffix := uuid.NewString()[:6]
	id := fmt.Sprintf("pay-%d-%s", s.nextSeq, suffix)
	receipt := fmt.Sprintf("REC-%d-%04d-%s", now.Year(), s.nextSeq, strings.ToUpper(suffix[:4]))
	status, verifiedBy, verifiedAtISO := resolveInitialVerificationState(prep.method, caller, now)
	clientID, clientEmail, clientName := resolvePaymentClient(cmd, caller)
	refCode := effectiveRef
	if refCode == "" {
		refCode = receipt
	}
	return domain.Payment{
		ID:                 id,
		ReceiptNumber:      receipt,
		EventID:            strings.TrimSpace(cmd.EventID),
		EventTitle:         strings.TrimSpace(cmd.EventTitle),
		ClientID:           clientID,
		ClientEmail:        clientEmail,
		ClientName:         clientName,
		AmountRD:           cmd.AmountRD,
		Method:             prep.method,
		Kind:               prep.kind,
		BankName:           prep.bankName,
		PaidDateLabel:      domain.FormatSpanishShortDate(now),
		ReferenceCode:      refCode,
		Status:             status,
		VoucherURL:         strings.TrimSpace(cmd.VoucherURL),
		ReferenceNumber:    strings.TrimSpace(cmd.ReferenceNumber),
		VerifiedBy:         verifiedBy,
		VerifiedAtISO:      verifiedAtISO,
		NCFType:            prep.ncfType,
		NCFNumber:          ncfNumber,
		ClientRNC:          prep.clientRNC,
		SubtotalDOP:        prep.subtotal,
		ITBISDOP:           prep.itbis,
		SecurityDepositDOP: prep.depositDOP,
	}
}

func resolveInitialVerificationState(
	method domain.PaymentMethod,
	caller platformhttp.Principal,
	now time.Time,
) (status domain.PaymentStatus, verifiedBy string, verifiedAtISO string) {
	if method == domain.MethodTransfer {
		return domain.PaymentPendingVerification, "", ""
	}
	verifier := strings.TrimSpace(caller.FullName)
	if verifier == "" {
		verifier = strings.TrimSpace(caller.Email)
	}
	if verifier == "" {
		verifier = "Sistema"
	}
	return domain.PaymentVerified, verifier, now.UTC().Format(time.RFC3339)
}

func resolvePaymentClient(
	cmd RegisterPaymentCommand,
	caller platformhttp.Principal,
) (clientID string, clientEmail string, clientName string) {
	clientEmail = strings.TrimSpace(cmd.ClientEmail)
	clientName = strings.TrimSpace(cmd.ClientName)
	if caller.IsClient() {
		if caller.ID != uuid.Nil {
			clientID = caller.ID.String()
		}
		if clientEmail == "" {
			clientEmail = strings.TrimSpace(caller.Email)
		}
		if clientName == "" {
			clientName = strings.TrimSpace(caller.FullName)
		}
	}
	return clientID, clientEmail, clientName
}

func persistPaymentIfEnabled(ctx context.Context, store Store, payment domain.Payment, now time.Time) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	return store.UpsertPayment(ctx, payment, now)
}

// ListRefunds devuelve todas las solicitudes de reembolso registradas.
func (s *Service) ListRefunds(ctx context.Context) []domain.Refund {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	out := make([]domain.Refund, 0, len(s.refundOrder))
	for _, id := range s.refundOrder {
		out = append(out, s.refunds[id])
	}
	s.mu.Unlock()
	return out
}

// ApproveRefund autoriza una solicitud de reembolso pendiente y ejecuta devolución en Stripe cuando aplica.
func (s *Service) ApproveRefund(
	ctx context.Context,
	refundID string,
	approverName string,
) (domain.Refund, error) {
	return s.ApproveRefundWithOptions(ctx, refundID, ApproveRefundOptions{ApproverName: approverName})
}

// ApproveRefundWithOptions autoriza un reembolso, ejecuta devolución Stripe y dispara liberación/notificación.
func (s *Service) ApproveRefundWithOptions(
	ctx context.Context,
	refundID string,
	opts ApproveRefundOptions,
) (domain.Refund, error) {
	s.verifyMu.Lock()
	defer s.verifyMu.Unlock()

	targetID := strings.TrimSpace(refundID)
	now := s.clock.Now()
	approver := strings.TrimSpace(opts.ApproverName)
	if approver == "" {
		approver = "Administración"
	}

	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	refund, exists := s.refunds[targetID]
	if !exists {
		s.mu.Unlock()
		return domain.Refund{}, domain.ErrRefundNotFound
	}
	if refund.Status == domain.RefundStatusApproved {
		s.mu.Unlock()
		return domain.Refund{}, domain.ErrRefundAlreadyApproved
	}
	linkedPayment, hasLinkedCardPayment := s.findConfirmedStripePaymentLocked(refund.EventID)
	store := s.store
	refundHook := s.refundHook
	s.mu.Unlock()

	gatewayRefundID, gatewayStatus, stripeErr := s.dispatchStripeRefundIfApplicable(ctx, refund, linkedPayment, hasLinkedCardPayment, now)
	if stripeErr != nil {
		return domain.Refund{}, stripeErr
	}

	s.mu.Lock()
	refund = s.refunds[targetID]
	refund.Status = domain.RefundStatusApproved
	refund.ApprovedBy = approver
	refund.ApprovedAtLabel = domain.FormatSpanishShortDate(now)
	if gatewayRefundID != "" {
		refund.GatewayRefundID = gatewayRefundID
		refund.GatewayStatus = gatewayStatus
	}
	s.refunds[targetID] = refund
	s.mu.Unlock()

	if err := persistRefundIfEnabled(ctx, store, refund, now); err != nil {
		return domain.Refund{}, err
	}
	if refundHook != nil {
		refundHook.OnRefundApproved(ctx, refund, opts)
	}
	return refund, nil
}

func (s *Service) findConfirmedStripePaymentLocked(eventID string) (domain.Payment, bool) {
	cleanEventID := strings.TrimSpace(eventID)
	if cleanEventID == "" {
		return domain.Payment{}, false
	}
	for _, id := range s.order {
		pay := s.payments[id]
		if pay.EventID != cleanEventID {
			continue
		}
		if pay.Method != domain.MethodCard {
			continue
		}
		if !pay.IsVerifiedOrConfirmed() {
			continue
		}
		hasStripeRef := pay.GatewayPaymentIntentID != ""
		if !hasStripeRef {
			hasStripeRef = pay.GatewaySessionID != ""
		}
		if hasStripeRef {
			return pay, true
		}
	}
	return domain.Payment{}, false
}

func (s *Service) dispatchStripeRefundIfApplicable(
	ctx context.Context,
	refund domain.Refund,
	linkedPayment domain.Payment,
	paymentFound bool,
	now time.Time,
) (string, string, error) {
	if !paymentFound {
		return "", "", nil
	}
	if refund.Status == domain.RefundStatusApproved {
		return refund.GatewayRefundID, refund.GatewayStatus, nil
	}
	if refund.GatewayRefundID != "" {
		return refund.GatewayRefundID, refund.GatewayStatus, nil
	}
	secretKey, _, apiBase := s.stripeSettingsSnapshot()
	if secretKey == "" {
		return "", "", domain.ErrCardGatewayNotConfigured
	}
	paymentIntentID, resolveErr := s.resolveStripePaymentIntent(ctx, apiBase, secretKey, linkedPayment, now)
	if resolveErr != nil {
		return "", "", resolveErr
	}
	if paymentIntentID == "" {
		return "", "", nil
	}
	stripeRes, refundErr := executeStripeRefundRequest(ctx, apiBase, secretKey, paymentIntentID, refund)
	if refundErr != nil {
		return "", "", refundErr
	}
	return stripeRes.ID, stripeRes.Status, nil
}

func (s *Service) resolveStripePaymentIntent(
	ctx context.Context,
	apiBase string,
	secretKey string,
	payment domain.Payment,
	now time.Time,
) (string, error) {
	if strings.TrimSpace(payment.GatewayPaymentIntentID) != "" {
		return strings.TrimSpace(payment.GatewayPaymentIntentID), nil
	}
	if strings.TrimSpace(payment.GatewaySessionID) == "" {
		return "", nil
	}
	session, err := fetchStripeCheckoutSession(ctx, apiBase, secretKey, payment.GatewaySessionID)
	if err != nil {
		return "", err
	}
	intentID := strings.TrimSpace(session.PaymentIntent)
	if intentID == "" {
		return "", nil
	}
	s.mu.Lock()
	if storedPay, ok := s.payments[payment.ID]; ok {
		storedPay.GatewayPaymentIntentID = intentID
		s.payments[payment.ID] = storedPay
		payment = storedPay
	}
	store := s.store
	s.mu.Unlock()
	_ = persistPaymentIfEnabled(ctx, store, payment, now)
	return intentID, nil
}

// RecordCancellationRefund genera una solicitud de reembolso al cancelar un evento.
func (s *Service) RecordCancellationRefund(
	ctx context.Context,
	eventID string,
	eventTitle string,
	clientName string,
	reason string,
	depositDop int,
	refundDop int,
	policyTier string,
) error {
	now := s.clock.Now()
	percent := 0
	if depositDop > 0 {
		percent = (refundDop * 100) / depositDop
	}
	fullReason := strings.TrimSpace(reason)
	if policyTier != "" {
		fullReason = fmt.Sprintf("%s (%s)", fullReason, policyTier)
	}
	refund := domain.Refund{
		ID:            "ref-" + uuid.NewString()[:8],
		EventID:       strings.TrimSpace(eventID),
		EventTitle:    strings.TrimSpace(eventTitle),
		ClientName:    strings.TrimSpace(clientName),
		PaidRD:        depositDop,
		RefundPercent: percent,
		RefundDop:     refundDop,
		Reason:        fullReason,
		Status:        domain.RefundStatusPending,
	}

	s.mu.Lock()
	s.refunds[refund.ID] = refund
	s.refundOrder = append(s.refundOrder, refund.ID)
	store := s.store
	s.mu.Unlock()

	return persistRefundIfEnabled(ctx, store, refund, now)
}

func persistRefundIfEnabled(ctx context.Context, store Store, refund domain.Refund, now time.Time) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	return store.UpsertRefund(ctx, refund, now)
}
