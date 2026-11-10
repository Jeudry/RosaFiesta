package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

const (
	canonicalPaymentsSumBaselineRD = 12352
	canonicalRetailBaselineRD      = 98
	canonicalReceivableOffsetRD    = 1669
	defaultReceivableBaselineRD    = 7724
	defaultActiveEventsCount       = 4
	defaultNewQuoteRequests        = 2
)

type EventMetricsReader interface {
	AllEventsSnapshot(ctx context.Context) []eventsdomain.Event
}

type QuoteMetricsReader interface {
	PendingQuotesCount(ctx context.Context) int
}

type Store interface {
	Enabled() bool
	ListPayments(ctx context.Context) ([]domain.Payment, error)
	UpsertPayment(ctx context.Context, payment domain.Payment, now time.Time) error
	ListRefunds(ctx context.Context) ([]domain.Refund, error)
	UpsertRefund(ctx context.Context, refund domain.Refund, now time.Time) error
}

type RegisterPaymentCommand struct {
	EventID       string
	EventTitle    string
	ClientEmail   string
	ClientName    string
	AmountRD      int
	Method        string
	Kind          string
	BankName      string
	ReferenceCode string
}

type Service struct {
	mu           sync.RWMutex
	clock        clock.Clock
	store        Store
	eventsReader EventMetricsReader
	quotesReader QuoteMetricsReader
	payments     map[string]domain.Payment
	order        []string
	refunds      map[string]domain.Refund
	refundOrder  []string
	nextSeq      int
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

func (s *Service) WithMetricsReaders(eventsReader EventMetricsReader, quotesReader QuoteMetricsReader) *Service {
	s.mu.Lock()
	s.eventsReader = eventsReader
	s.quotesReader = quotesReader
	s.mu.Unlock()
	return s
}

func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.syncFromStoreLocked(context.Background())
	s.mu.Unlock()
	return s
}

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

func (s *Service) GetSummary(ctx context.Context) domain.FinanceSummary {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	paidSum, pendingCount := s.aggregatePaymentsLocked()
	approvedRefunds := s.aggregateApprovedRefundsLocked()
	eventsReader := s.eventsReader
	quotesReader := s.quotesReader
	s.mu.Unlock()

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

func (s *Service) aggregatePaymentsLocked() (paidSum int, pendingCount int) {
	for _, id := range s.order {
		item := s.payments[id]
		if item.Status != domain.PaymentFailed {
			paidSum += item.AmountRD
		}
		if item.Status == domain.PaymentPendingVerification {
			pendingCount++
		}
	}
	return paidSum, pendingCount
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

func (s *Service) ListPayments(eventID string) []domain.Payment {
	admin := platformhttp.Principal{Role: "admin"}
	return s.ListPaymentsForCaller(context.Background(), eventID, admin, true)
}

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

func (s *Service) RegisterPaymentForCaller(
	ctx context.Context,
	cmd RegisterPaymentCommand,
	caller platformhttp.Principal,
) (domain.Payment, error) {
	if cmd.AmountRD <= 0 {
		return domain.Payment{}, domain.ErrInvalidPaymentAmount
	}
	normalizedMethod, bankName, err := domain.NormalizePaymentMethod(cmd.Method, cmd.BankName)
	if err != nil {
		return domain.Payment{}, err
	}
	normalizedKind := domain.NormalizePaymentKind(cmd.Kind)
	now := s.clock.Now()

	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	if existing, found, dupErr := s.findExistingByReferenceLocked(cmd.ReferenceCode, caller); found {
		s.mu.Unlock()
		return existing, dupErr
	}
	payment := s.buildNewPaymentLocked(cmd, normalizedMethod, normalizedKind, bankName, caller, now)
	s.payments[payment.ID] = payment
	s.order = append(s.order, payment.ID)
	store := s.store
	s.mu.Unlock()

	if err := persistPaymentIfEnabled(ctx, store, payment, now); err != nil {
		return domain.Payment{}, err
	}
	return payment, nil
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
	method domain.PaymentMethod,
	kind domain.PaymentKind,
	bankName string,
	caller platformhttp.Principal,
	now time.Time,
) domain.Payment {
	s.nextSeq++
	suffix := uuid.NewString()[:6]
	id := fmt.Sprintf("pay-%d-%s", s.nextSeq, suffix)
	receipt := fmt.Sprintf("REC-%d-%04d-%s", now.Year(), s.nextSeq, strings.ToUpper(suffix[:4]))
	status := domain.PaymentVerified
	if method == domain.MethodTransfer {
		status = domain.PaymentPendingVerification
	}
	clientID, clientEmail, clientName := resolvePaymentClient(cmd, caller)
	refCode := strings.TrimSpace(cmd.ReferenceCode)
	if refCode == "" {
		refCode = receipt
	}
	return domain.Payment{
		ID:            id,
		ReceiptNumber: receipt,
		EventID:       strings.TrimSpace(cmd.EventID),
		EventTitle:    strings.TrimSpace(cmd.EventTitle),
		ClientID:      clientID,
		ClientEmail:   clientEmail,
		ClientName:    clientName,
		AmountRD:      cmd.AmountRD,
		Method:        method,
		Kind:          kind,
		BankName:      bankName,
		PaidDateLabel: domain.FormatSpanishShortDate(now),
		ReferenceCode: refCode,
		Status:        status,
	}
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

func (s *Service) ApproveRefund(
	ctx context.Context,
	refundID string,
	approverName string,
) (domain.Refund, error) {
	targetID := strings.TrimSpace(refundID)
	now := s.clock.Now()
	approver := strings.TrimSpace(approverName)
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
	refund.Status = domain.RefundStatusApproved
	refund.ApprovedBy = approver
	refund.ApprovedAtLabel = domain.FormatSpanishShortDate(now)
	s.refunds[targetID] = refund
	store := s.store
	s.mu.Unlock()

	if err := persistRefundIfEnabled(ctx, store, refund, now); err != nil {
		return domain.Refund{}, err
	}
	return refund, nil
}

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
