package app

import (
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"

	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type stripeSessionPayload struct {
	ID            string            `json:"id"`
	PaymentIntent string            `json:"payment_intent"`
	PaymentStatus string            `json:"payment_status"`
	AmountTotal   int64             `json:"amount_total"`
	CustomerEmail string            `json:"customer_email"`
	Metadata      map[string]string `json:"metadata"`
}

type stripeRefundObject struct {
	ID            string            `json:"id"`
	Status        string            `json:"status"`
	PaymentIntent string            `json:"payment_intent"`
	Metadata      map[string]string `json:"metadata"`
}

type stripeChargeWebhookObject struct {
	ID            string `json:"id"`
	PaymentIntent string `json:"payment_intent"`
	Refunds       struct {
		Data []stripeRefundObject `json:"data"`
	} `json:"refunds"`
}

// ConfirmCardSession verifica una sesión de Stripe Checkout y confirma idempotente el pago asociado.
func (s *Service) ConfirmCardSession(
	ctx context.Context,
	sessionID string,
	caller platformhttp.Principal,
) (domain.Payment, error) {
	cleanID := strings.TrimSpace(sessionID)
	if cleanID == "" {
		return domain.Payment{}, domain.ErrPaymentNotFound
	}
	if existing, found := s.findVerifiedSessionPayment(ctx, cleanID); found {
		return existing, nil
	}
	secretKey, _, apiBase := s.stripeSettingsSnapshot()
	if secretKey == "" {
		return domain.Payment{}, domain.ErrCardGatewayNotConfigured
	}
	session, err := fetchStripeCheckoutSession(ctx, apiBase, secretKey, cleanID)
	if err != nil {
		return domain.Payment{}, err
	}
	if !strings.EqualFold(session.PaymentStatus, "paid") {
		return domain.Payment{}, domain.ErrCardSessionNotPaid
	}
	return s.recordVerifiedStripeSession(ctx, session, caller)
}

// HandleStripeWebhook valida Stripe-Signature HMAC-SHA256 y procesa eventos checkout y refund idempotentemente.
func (s *Service) HandleStripeWebhook(
	ctx context.Context,
	rawBody []byte,
	signatureHeader string,
) (domain.Payment, error) {
	s.mu.RLock()
	webhookSecret := s.stripeWebhookSecret
	s.mu.RUnlock()
	if webhookSecret == "" {
		return domain.Payment{}, domain.ErrCardGatewayNotConfigured
	}
	if !verifyStripeWebhookSignature(rawBody, signatureHeader, webhookSecret, s.clock.Now()) {
		return domain.Payment{}, domain.ErrInvalidWebhookSignature
	}
	var event struct {
		Type string          `json:"type"`
		Data json.RawMessage `json:"data"`
	}
	if err := json.Unmarshal(rawBody, &event); err != nil {
		return domain.Payment{}, err
	}
	switch event.Type {
	case "checkout.session.completed":
		return s.handleStripeCheckoutCompletedWebhook(ctx, event.Data)
	case "charge.refunded":
		return domain.Payment{}, s.handleStripeChargeRefundedWebhook(ctx, event.Data)
	case "refund.updated":
		return domain.Payment{}, s.handleStripeRefundUpdatedWebhook(ctx, event.Data)
	default:
		return domain.Payment{}, nil
	}
}

func (s *Service) handleStripeCheckoutCompletedWebhook(ctx context.Context, rawData json.RawMessage) (domain.Payment, error) {
	var wrapper struct {
		Object stripeSessionPayload `json:"object"`
	}
	if err := json.Unmarshal(rawData, &wrapper); err != nil {
		return domain.Payment{}, err
	}
	session := wrapper.Object
	if session.ID == "" {
		return domain.Payment{}, domain.ErrPaymentNotFound
	}
	if existing, found := s.findVerifiedSessionPayment(ctx, session.ID); found {
		return existing, nil
	}
	return s.recordVerifiedStripeSession(ctx, session, platformhttp.Principal{Role: "admin", FullName: "Stripe Webhook"})
}

func (s *Service) handleStripeChargeRefundedWebhook(ctx context.Context, rawData json.RawMessage) error {
	var wrapper struct {
		Object stripeChargeWebhookObject `json:"object"`
	}
	if err := json.Unmarshal(rawData, &wrapper); err != nil {
		return err
	}
	charge := wrapper.Object
	refundObj := stripeRefundObject{
		PaymentIntent: strings.TrimSpace(charge.PaymentIntent),
		Status:        "succeeded",
	}
	if len(charge.Refunds.Data) > 0 {
		refundObj = charge.Refunds.Data[0]
		if strings.TrimSpace(refundObj.PaymentIntent) == "" {
			refundObj.PaymentIntent = strings.TrimSpace(charge.PaymentIntent)
		}
	}
	return s.applyStripeRefundWebhookSync(ctx, refundObj)
}

func (s *Service) handleStripeRefundUpdatedWebhook(ctx context.Context, rawData json.RawMessage) error {
	var wrapper struct {
		Object stripeRefundObject `json:"object"`
	}
	if err := json.Unmarshal(rawData, &wrapper); err != nil {
		return err
	}
	return s.applyStripeRefundWebhookSync(ctx, wrapper.Object)
}

func (s *Service) applyStripeRefundWebhookSync(ctx context.Context, stripeRef stripeRefundObject) error {
	now := s.clock.Now()
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	targetID := s.findRefundIDForStripeEventLocked(stripeRef)
	if targetID == "" {
		s.mu.Unlock()
		return nil
	}
	ref := s.refunds[targetID]
	if strings.TrimSpace(stripeRef.ID) != "" {
		ref.GatewayRefundID = strings.TrimSpace(stripeRef.ID)
	}
	if strings.TrimSpace(stripeRef.Status) != "" {
		ref.GatewayStatus = strings.TrimSpace(stripeRef.Status)
	}
	if ref.Status != domain.RefundStatusApproved {
		ref.Status = domain.RefundStatusApproved
		if strings.TrimSpace(ref.ApprovedBy) == "" {
			ref.ApprovedBy = "Pasarela Stripe"
		}
		if strings.TrimSpace(ref.ApprovedAtLabel) == "" {
			ref.ApprovedAtLabel = domain.FormatSpanishShortDate(now)
		}
	}
	s.refunds[targetID] = ref
	store := s.store
	s.mu.Unlock()
	return persistRefundIfEnabled(ctx, store, ref, now)
}

func (s *Service) findRefundIDForStripeEventLocked(stripeRef stripeRefundObject) string {
	explicitRefundID := strings.TrimSpace(stripeRef.Metadata["refund_id"])
	if explicitRefundID != "" {
		if _, exists := s.refunds[explicitRefundID]; exists {
			return explicitRefundID
		}
	}
	cleanStripeRefID := strings.TrimSpace(stripeRef.ID)
	if cleanStripeRefID != "" {
		for _, id := range s.refundOrder {
			if s.refunds[id].GatewayRefundID == cleanStripeRefID {
				return id
			}
		}
	}
	intentID := strings.TrimSpace(stripeRef.PaymentIntent)
	if intentID == "" {
		return ""
	}
	for _, payID := range s.order {
		pay := s.payments[payID]
		if pay.GatewayPaymentIntentID == intentID {
			return s.findFirstRefundByEventLocked(pay.EventID)
		}
	}
	return ""
}

func (s *Service) findFirstRefundByEventLocked(eventID string) string {
	for _, id := range s.refundOrder {
		if s.refunds[id].EventID == eventID {
			return id
		}
	}
	return ""
}

func (s *Service) findVerifiedSessionPayment(ctx context.Context, sessionID string) (domain.Payment, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	for _, id := range s.order {
		item := s.payments[id]
		if item.GatewaySessionID == sessionID {
			return item, true
		}
	}
	return domain.Payment{}, false
}

func (s *Service) recordVerifiedStripeSession(
	ctx context.Context,
	session stripeSessionPayload,
	caller platformhttp.Principal,
) (domain.Payment, error) {
	amountRD := int(session.AmountTotal / 100)
	if amountRD <= 0 {
		return domain.Payment{}, domain.ErrInvalidPaymentAmount
	}
	eventID := strings.TrimSpace(session.Metadata["event_id"])
	ncfType, cleanedRNC, ncfErr := domain.ValidateRNCForNCF(session.Metadata["ncf_type"], session.Metadata["client_rnc"])
	if ncfErr != nil {
		ncfType = "B02"
		cleanedRNC = ""
	}
	ncfNumber, allocErr := s.allocateNextNCF(ctx, ncfType)
	if allocErr != nil {
		return domain.Payment{}, allocErr
	}
	now := s.clock.Now()
	payment := s.buildStripeConfirmedPayment(ctx, session, eventID, amountRD, ncfType, cleanedRNC, ncfNumber, caller, now)

	s.mu.Lock()
	s.payments[payment.ID] = payment
	s.order = append(s.order, payment.ID)
	store := s.store
	s.mu.Unlock()

	if err := persistPaymentIfEnabled(ctx, store, payment, now); err != nil {
		return domain.Payment{}, err
	}
	if err := s.dispatchRegistrationSideEffects(ctx, payment); err != nil {
		return domain.Payment{}, err
	}
	s.sendVerificationReceiptIfConfigured(ctx, payment)
	return payment, nil
}

func (s *Service) buildStripeConfirmedPayment(
	ctx context.Context,
	session stripeSessionPayload,
	eventID string,
	amountRD int,
	ncfType string,
	cleanedRNC string,
	ncfNumber string,
	caller platformhttp.Principal,
	now time.Time,
) domain.Payment {
	eventTitle, clientID, clientName, clientEmail := s.resolveStripeEventMetadata(ctx, eventID, session, caller)
	subtotal, itbis := domain.ComputeDominicanTaxBreakdown(amountRD, 0)
	s.mu.Lock()
	s.nextSeq++
	seq := s.nextSeq
	s.mu.Unlock()

	return domain.Payment{
		ID:                     "pay-" + uuid.NewString()[:8],
		ReceiptNumber:          fmt.Sprintf("REC-%d-%04d", now.Year(), seq),
		EventID:                eventID,
		EventTitle:             eventTitle,
		ClientID:               clientID,
		ClientEmail:            clientEmail,
		ClientName:             clientName,
		AmountRD:               amountRD,
		Method:                 domain.MethodCard,
		Kind:                   domain.NormalizePaymentKind(session.Metadata["kind"]),
		BankName:               "Stripe Checkout",
		PaidDateLabel:          domain.FormatSpanishShortDate(now),
		ReferenceCode:          "STR-" + strings.ToUpper(session.ID[:minInt(len(session.ID), 12)]),
		Status:                 domain.PaymentConfirmed,
		ReferenceNumber:        session.ID,
		VerifiedBy:             "Pasarela Stripe",
		VerifiedAtISO:          now.UTC().Format(time.RFC3339),
		NCFType:                ncfType,
		NCFNumber:              ncfNumber,
		ClientRNC:              cleanedRNC,
		SubtotalDOP:            subtotal,
		ITBISDOP:               itbis,
		GatewaySessionID:       session.ID,
		GatewayPaymentIntentID: strings.TrimSpace(session.PaymentIntent),
	}
}

func (s *Service) resolveStripeEventMetadata(
	ctx context.Context,
	eventID string,
	session stripeSessionPayload,
	caller platformhttp.Principal,
) (eventTitle, clientID, clientName, clientEmail string) {
	eventTitle = "Reserva RosaFiesta"
	clientEmail = strings.TrimSpace(session.CustomerEmail)
	if clientEmail == "" {
		clientEmail = strings.TrimSpace(caller.Email)
	}
	clientName = strings.TrimSpace(caller.FullName)
	if caller.ID != uuid.Nil {
		clientID = caller.ID.String()
	}
	s.mu.RLock()
	eventsReader := s.eventsReader
	s.mu.RUnlock()
	if eventsReader == nil {
		return eventTitle, clientID, nonEmptyOr(clientName, "Cliente RosaFiesta"), clientEmail
	}
	for _, evt := range eventsReader.AllEventsSnapshot(ctx) {
		if evt.ID == eventID {
			eventTitle = nonEmptyOr(evt.Title, eventTitle)
			if evt.ClientID != uuid.Nil {
				clientID = evt.ClientID.String()
			}
			clientName = nonEmptyOr(evt.ClientName, clientName)
			clientEmail = nonEmptyOr(evt.ClientEmail, clientEmail)
			break
		}
	}
	return eventTitle, clientID, nonEmptyOr(clientName, "Cliente RosaFiesta"), clientEmail
}

func fetchStripeCheckoutSession(
	ctx context.Context,
	apiBase string,
	secretKey string,
	sessionID string,
) (stripeSessionPayload, error) {
	endpoint := fmt.Sprintf("%s/v1/checkout/sessions/%s", strings.TrimRight(apiBase, "/"), sessionID)
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, endpoint, nil)
	if err != nil {
		return stripeSessionPayload{}, err
	}
	req.Header.Set("Authorization", "Bearer "+secretKey)
	client := &http.Client{Timeout: 15 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return stripeSessionPayload{}, err
	}
	defer func() { _ = resp.Body.Close() }()

	rawBody, readErr := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
	if readErr != nil {
		return stripeSessionPayload{}, readErr
	}
	isSuccessStatus := resp.StatusCode >= 200
	if isSuccessStatus {
		isSuccessStatus = resp.StatusCode < 300
	}
	if !isSuccessStatus {
		return stripeSessionPayload{}, fmt.Errorf("stripe session fetch status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawBody)))
	}
	var payload stripeSessionPayload
	if unmarshalErr := json.Unmarshal(rawBody, &payload); unmarshalErr != nil {
		return stripeSessionPayload{}, unmarshalErr
	}
	return payload, nil
}

func executeStripeRefundRequest(
	ctx context.Context,
	apiBase string,
	secretKey string,
	paymentIntentID string,
	refund domain.Refund,
) (stripeRefundObject, error) {
	form := url.Values{}
	form.Set("payment_intent", strings.TrimSpace(paymentIntentID))
	if refund.RefundDop > 0 {
		form.Set("amount", strconv.Itoa(refund.RefundDop*100))
	}
	form.Set("reason", "requested_by_customer")
	form.Set("metadata[refund_id]", refund.ID)
	form.Set("metadata[event_id]", refund.EventID)

	endpoint := strings.TrimRight(apiBase, "/") + "/v1/refunds"
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, strings.NewReader(form.Encode()))
	if err != nil {
		return stripeRefundObject{}, err
	}
	req.Header.Set("Authorization", "Bearer "+secretKey)
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Idempotency-Key", "refund-"+refund.ID)

	client := &http.Client{Timeout: 15 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return stripeRefundObject{}, err
	}
	defer func() { _ = resp.Body.Close() }()

	rawBody, readErr := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
	if readErr != nil {
		return stripeRefundObject{}, readErr
	}
	isSuccess := resp.StatusCode >= 200
	if isSuccess {
		isSuccess = resp.StatusCode < 300
	}
	if !isSuccess {
		return stripeRefundObject{}, fmt.Errorf("stripe refund returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawBody)))
	}
	var parsed stripeRefundObject
	if unmarshalErr := json.Unmarshal(rawBody, &parsed); unmarshalErr != nil {
		return stripeRefundObject{}, unmarshalErr
	}
	return parsed, nil
}

func verifyStripeWebhookSignature(rawBody []byte, header string, secret string, now time.Time) bool {
	timestamp, signatures := parseStripeSignatureHeader(header)
	if timestamp == "" {
		return false
	}
	if len(signatures) == 0 {
		return false
	}
	unixSec, err := strconv.ParseInt(timestamp, 10, 64)
	if err != nil {
		return false
	}
	delta := now.Unix() - unixSec
	if delta < 0 {
		delta = -delta
	}
	if delta > 300 {
		return false
	}
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write([]byte(timestamp + "."))
	mac.Write(rawBody)
	expectedHex := hex.EncodeToString(mac.Sum(nil))
	for _, sig := range signatures {
		if hmac.Equal([]byte(sig), []byte(expectedHex)) {
			return true
		}
	}
	return false
}

func parseStripeSignatureHeader(header string) (string, []string) {
	var timestamp string
	var signatures []string
	for _, part := range strings.Split(header, ",") {
		trimmed := strings.TrimSpace(part)
		kv := strings.SplitN(trimmed, "=", 2)
		if len(kv) != 2 {
			continue
		}
		if kv[0] == "t" {
			timestamp = strings.TrimSpace(kv[1])
		}
		if kv[0] == "v1" {
			signatures = append(signatures, strings.TrimSpace(kv[1]))
		}
	}
	return timestamp, signatures
}

// ComputeAnalytics calcula analítica histórica y de período usando exclusivamente pagos, reembolsos, daños y eventos almacenados.
func (s *Service) ComputeAnalytics(ctx context.Context, rawRange string) domain.PaymentsAnalytics {
	rangeKey := normalizeAnalyticsRange(rawRange)
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	allPayments := s.copyPaymentsLocked()
	allRefunds := s.copyRefundsLocked()
	eventsReader := s.eventsReader
	damageReader := s.damageReader
	s.mu.Unlock()

	var allEvents []eventsdomain.Event
	if eventsReader != nil {
		allEvents = eventsReader.AllEventsSnapshot(ctx)
	}
	damageExpense := 0
	if damageReader != nil {
		damageExpense = damageReader.TotalDamageExpensesRD(ctx)
	}

	now := s.clock.Now()
	cutoff, hasCutoff := analyticsRangeCutoff(now, rangeKey)
	monthCount := 6
	isAnnualView := rangeKey == "365d"
	if !isAnnualView {
		isAnnualView = rangeKey == "all"
	}
	if isAnnualView {
		monthCount = 12
	}

	series := buildMonthlyAnalyticsSeries(now, monthCount, allPayments, allRefunds, allEvents)
	filteredPayments := filterPaymentsByCutoff(allPayments, now, hasCutoff, cutoff)
	filteredEvents := filterEventsByCutoff(allEvents, now, hasCutoff, cutoff)
	filteredRefunds := filterRefundsByCutoff(allRefunds, now, hasCutoff, cutoff)
	totalRev := sumVerifiedPayments(filteredPayments)
	approvedRefunds := sumApprovedRefunds(filteredRefunds)
	totalExp := approvedRefunds + damageExpense
	netMargin := computeNetMarginPercent(totalRev, totalExp)
	confirmed, pending, completed, cancelled, typesBreakdown, uniqueClients, repeatClients, collectionPct := aggregateEventAnalytics(filteredEvents)

	return domain.PaymentsAnalytics{
		Range:                 rangeKey,
		MonthlySeries:         series,
		TotalRevenueRD:        totalRev,
		TotalExpensesRD:       totalExp,
		NetMarginPercent:      netMargin,
		TotalEventsCount:      len(filteredEvents),
		ConfirmedEventsCount:  confirmed,
		PendingEventsCount:    pending,
		CompletedEventsCount:  completed,
		CancelledEventsCount:  cancelled,
		EventTypesBreakdown:   typesBreakdown,
		UniqueClientsCount:    uniqueClients,
		RepeatClientsCount:    repeatClients,
		CollectionRatePercent: collectionPct,
	}
}

func normalizeAnalyticsRange(raw string) string {
	switch strings.ToLower(strings.TrimSpace(raw)) {
	case "7d", "30d", "90d", "365d", "all":
		return strings.ToLower(strings.TrimSpace(raw))
	default:
		return "30d"
	}
}

func analyticsRangeCutoff(now time.Time, rangeKey string) (time.Time, bool) {
	switch rangeKey {
	case "7d":
		return now.AddDate(0, 0, -7), true
	case "30d":
		return now.AddDate(0, 0, -30), true
	case "90d":
		return now.AddDate(0, 0, -90), true
	case "365d":
		return now.AddDate(-1, 0, 0), true
	default:
		return time.Time{}, false
	}
}

func (s *Service) copyPaymentsLocked() []domain.Payment {
	out := make([]domain.Payment, 0, len(s.order))
	for _, id := range s.order {
		out = append(out, s.payments[id])
	}
	return out
}

func (s *Service) copyRefundsLocked() []domain.Refund {
	out := make([]domain.Refund, 0, len(s.refundOrder))
	for _, id := range s.refundOrder {
		out = append(out, s.refunds[id])
	}
	return out
}

var spanishShortMonths = []string{"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"}

func buildMonthlyAnalyticsSeries(
	now time.Time,
	monthCount int,
	payments []domain.Payment,
	refunds []domain.Refund,
	events []eventsdomain.Event,
) []domain.MonthlyAnalyticsPoint {
	points := make([]domain.MonthlyAnalyticsPoint, monthCount)
	indexByKey := make(map[string]int, monthCount)
	for offset := monthCount - 1; offset >= 0; offset-- {
		target := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, now.Location()).AddDate(0, -offset, 0)
		idx := (monthCount - 1) - offset
		key := target.Format("2006-01")
		indexByKey[key] = idx
		points[idx] = domain.MonthlyAnalyticsPoint{
			MonthKey: key,
			Label:    spanishShortMonths[int(target.Month())-1],
		}
	}
	for _, pay := range payments {
		if !pay.IsVerifiedOrConfirmed() {
			continue
		}
		dt := resolvePaymentDate(pay, now)
		if idx, ok := indexByKey[dt.Format("2006-01")]; ok {
			points[idx].RevenueRD += pay.AmountRD
		}
	}
	for _, ref := range refunds {
		if ref.Status != domain.RefundStatusApproved {
			continue
		}
		dt := parseSpanishShortDate(ref.ApprovedAtLabel, now)
		if idx, ok := indexByKey[dt.Format("2006-01")]; ok {
			points[idx].ExpensesRD += ref.RefundDop
		}
	}
	for _, evt := range events {
		dt := resolveEventDate(evt, now)
		if idx, ok := indexByKey[dt.Format("2006-01")]; ok {
			points[idx].EventsCount++
		}
	}
	return points
}

func resolvePaymentDate(pay domain.Payment, fallback time.Time) time.Time {
	if trimmed := strings.TrimSpace(pay.VerifiedAtISO); trimmed != "" {
		if parsed, err := time.Parse(time.RFC3339, trimmed); err == nil {
			return parsed
		}
	}
	return parseSpanishShortDate(pay.PaidDateLabel, fallback)
}

func resolveEventDate(evt eventsdomain.Event, fallback time.Time) time.Time {
	if trimmed := strings.TrimSpace(evt.EventDateISO); trimmed != "" {
		if parsed, err := time.Parse("2006-01-02", trimmed); err == nil {
			return parsed
		}
	}
	return parseSpanishShortDate(evt.DateLabel, fallback)
}

func parseSpanishShortDate(raw string, fallback time.Time) time.Time {
	clean := strings.ToLower(strings.TrimSpace(raw))
	if clean == "" {
		return fallback
	}
	parts := strings.Fields(clean)
	if len(parts) >= 2 {
		day, dayErr := strconv.Atoi(parts[0])
		month := spanishMonthIndex(parts[1])
		if dayErr == nil && month > 0 {
			return time.Date(fallback.Year(), time.Month(month), maxInt(1, minInt(day, 28)), 0, 0, 0, 0, fallback.Location())
		}
	}
	return fallback
}

func spanishMonthIndex(token string) int {
	for idx, label := range spanishShortMonths {
		if strings.HasPrefix(token, label) {
			return idx + 1
		}
	}
	return 0
}

func filterPaymentsByCutoff(items []domain.Payment, now time.Time, hasCutoff bool, cutoff time.Time) []domain.Payment {
	if !hasCutoff {
		return items
	}
	out := make([]domain.Payment, 0, len(items))
	for _, item := range items {
		dt := resolvePaymentDate(item, now)
		if !dt.Before(cutoff) {
			out = append(out, item)
		}
	}
	return out
}

func filterEventsByCutoff(items []eventsdomain.Event, now time.Time, hasCutoff bool, cutoff time.Time) []eventsdomain.Event {
	if !hasCutoff {
		return items
	}
	out := make([]eventsdomain.Event, 0, len(items))
	for _, item := range items {
		dt := resolveEventDate(item, now)
		if !dt.Before(cutoff) {
			out = append(out, item)
		}
	}
	return out
}

func sumVerifiedPayments(items []domain.Payment) int {
	total := 0
	for _, item := range items {
		if item.IsVerifiedOrConfirmed() {
			total += item.AmountRD
		}
	}
	return total
}

func sumApprovedRefunds(items []domain.Refund) int {
	total := 0
	for _, item := range items {
		if item.Status == domain.RefundStatusApproved {
			total += item.RefundDop
		}
	}
	return total
}

func filterRefundsByCutoff(items []domain.Refund, now time.Time, hasCutoff bool, cutoff time.Time) []domain.Refund {
	if !hasCutoff {
		return items
	}
	out := make([]domain.Refund, 0, len(items))
	for _, item := range items {
		dt := parseSpanishShortDate(item.ApprovedAtLabel, now)
		if !dt.Before(cutoff) {
			out = append(out, item)
		}
	}
	return out
}

func computeNetMarginPercent(revenue, expenses int) int {
	if revenue <= 0 {
		return 0
	}
	pct := ((revenue - expenses) * 100) / revenue
	if pct < 0 {
		return 0
	}
	return pct
}

func aggregateEventAnalytics(events []eventsdomain.Event) (
	confirmed, pending, completed, cancelled int,
	types []domain.CategoryCountPoint,
	uniqueClients, repeatClients, collectionPct int,
) {
	typeCounts := make(map[string]int)
	clientCounts := make(map[string]int)
	totalValue := 0
	totalCollected := 0
	for _, evt := range events {
		switch evt.Status {
		case eventsdomain.StatusConfirmed, eventsdomain.StatusInProgress:
			confirmed++
		case eventsdomain.StatusPending, eventsdomain.StatusDraft:
			pending++
		case eventsdomain.StatusCompleted:
			completed++
		case eventsdomain.StatusCancelled:
			cancelled++
		}
		typeLabel := nonEmptyOr(evt.EventType, "Otro")
		typeCounts[typeLabel]++
		clientKey := strings.ToLower(nonEmptyOr(evt.ClientEmail, evt.ClientName))
		if clientKey != "" {
			clientCounts[clientKey]++
		}
		totalValue += evt.TotalRD
		totalCollected += evt.PaidDepositRD
	}
	types = sortedCategoryCounts(typeCounts)
	uniqueClients = len(clientCounts)
	for _, count := range clientCounts {
		if count > 1 {
			repeatClients++
		}
	}
	if totalValue > 0 {
		collectionPct = minInt(100, (totalCollected*100)/totalValue)
	}
	return confirmed, pending, completed, cancelled, types, uniqueClients, repeatClients, collectionPct
}

func sortedCategoryCounts(counts map[string]int) []domain.CategoryCountPoint {
	out := make([]domain.CategoryCountPoint, 0, len(counts))
	for label, count := range counts {
		out = append(out, domain.CategoryCountPoint{
			Key:   strings.ToLower(label),
			Label: label,
			Count: count,
		})
	}
	sort.Slice(out, func(i, j int) bool {
		if out[i].Count != out[j].Count {
			return out[i].Count > out[j].Count
		}
		return out[i].Label < out[j].Label
	})
	return out
}

func nonEmptyOr(value, fallback string) string {
	trimmed := strings.TrimSpace(value)
	if trimmed != "" {
		return trimmed
	}
	return fallback
}

func minInt(a, b int) int {
	if a < b {
		return a
	}
	return b
}

func maxInt(a, b int) int {
	if a > b {
		return a
	}
	return b
}
