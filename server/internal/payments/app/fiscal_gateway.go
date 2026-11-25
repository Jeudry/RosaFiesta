package app

import (
	"bytes"
	"context"
	"encoding/csv"
	"encoding/json"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"
	_ "time/tzdata"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

const defaultStripePublicReturnBase = "https://app.rosafiesta.do"

// VerifyPaymentForCaller aprueba una transferencia pendiente, asigna NCF secuencial y sincroniza módulos.
func (s *Service) VerifyPaymentForCaller(
	ctx context.Context,
	paymentID string,
	notesOrReason string,
	caller platformhttp.Principal,
) (domain.Payment, error) {
	if !hasFinanceAccess(caller) {
		return domain.Payment{}, domain.ErrPaymentForbidden
	}
	s.verifyMu.Lock()
	defer s.verifyMu.Unlock()

	targetID := strings.TrimSpace(paymentID)
	ncfType, err := s.ensurePendingPaymentNCFType(ctx, targetID)
	if err != nil {
		return domain.Payment{}, err
	}
	ncfNumber, allocErr := s.allocateNextNCF(ctx, ncfType)
	if allocErr != nil {
		return domain.Payment{}, allocErr
	}

	now := s.clock.Now()
	itbisRate := s.resolveITBISPercent(ctx)
	verified, store, lockErr := s.markPaymentConfirmedLocked(ctx, targetID, ncfType, ncfNumber, notesOrReason, itbisRate, caller, now)
	if lockErr != nil {
		return domain.Payment{}, lockErr
	}
	if persistErr := persistPaymentIfEnabled(ctx, store, verified, now); persistErr != nil {
		return domain.Payment{}, persistErr
	}
	if syncErr := s.invokeVerifiedSynchronizer(ctx, verified); syncErr != nil {
		return domain.Payment{}, syncErr
	}
	s.sendVerificationReceiptIfConfigured(ctx, verified)
	s.publishPaymentEvent(realtimedomain.EventPaymentVerified, resolvePaymentAudience(verified), verified, "Pago verificado y comprobante NCF emitido")
	return verified, nil
}

// RejectPaymentForCaller rechaza un comprobante bancario pendiente y publica el evento en tiempo real.
func (s *Service) RejectPaymentForCaller(
	ctx context.Context,
	paymentID string,
	reason string,
	caller platformhttp.Principal,
) (domain.Payment, error) {
	if !hasFinanceAccess(caller) {
		return domain.Payment{}, domain.ErrPaymentForbidden
	}
	targetID := strings.TrimSpace(paymentID)
	cleanReason := strings.TrimSpace(reason)
	if cleanReason == "" {
		cleanReason = "Comprobante rechazado por finanzas"
	}

	now := s.clock.Now()
	rejected, store, lockErr := s.markPaymentRejectedLocked(ctx, targetID, cleanReason, caller, now)
	if lockErr != nil {
		return domain.Payment{}, lockErr
	}
	if persistErr := persistPaymentIfEnabled(ctx, store, rejected, now); persistErr != nil {
		return domain.Payment{}, persistErr
	}
	s.invokeRejectedHook(ctx, rejected, cleanReason)
	s.publishPaymentEvent(realtimedomain.EventPaymentRejected, resolvePaymentAudience(rejected), rejected, cleanReason)
	return rejected, nil
}

func (s *Service) invokeRejectedHook(ctx context.Context, payment domain.Payment, reason string) {
	s.mu.RLock()
	hook := s.rejectedHook
	s.mu.RUnlock()
	if hook != nil {
		hook.OnPaymentRejected(ctx, payment, reason)
	}
}

// CreateCardSession genera una sesión de checkout en Stripe o devuelve ErrCardGatewayNotConfigured.
func (s *Service) CreateCardSession(
	ctx context.Context,
	cmd CreateCardSessionCommand,
	caller platformhttp.Principal,
) (domain.CardSessionResult, error) {
	if cmd.AmountRD <= 0 {
		return domain.CardSessionResult{}, domain.ErrInvalidPaymentAmount
	}
	if ownerErr := s.verifyClientOwnsEvent(ctx, cmd.EventID, caller); ownerErr != nil {
		return domain.CardSessionResult{}, ownerErr
	}
	ncfType, cleanedRNC, ncfErr := domain.ValidateRNCForNCF(cmd.NCFType, cmd.ClientRNC)
	if ncfErr != nil {
		return domain.CardSessionResult{}, ncfErr
	}
	secretKey, publicBase, apiBase := s.stripeSettingsSnapshot()
	if secretKey == "" {
		return domain.CardSessionResult{}, domain.ErrCardGatewayNotConfigured
	}
	formValues := buildStripeSessionForm(cmd, ncfType, cleanedRNC, publicBase, caller.Email)
	return executeStripeCheckoutRequest(ctx, apiBase, secretKey, formValues)
}

// ExportDGII607CSV genera el archivo CSV de formato DGII 607 con los pagos verificados que tienen NCF.
func (s *Service) ExportDGII607CSV(ctx context.Context, period string) (string, error) {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	snapshot := make([]domain.Payment, 0, len(s.order))
	for _, id := range s.order {
		snapshot = append(snapshot, s.payments[id])
	}
	s.mu.Unlock()

	var buf bytes.Buffer
	writer := csv.NewWriter(&buf)
	header := []string{
		"RNC_Cedula",
		"Tipo_NCF",
		"NCF",
		"Fecha_Pago",
		"Evento_ID",
		"Cliente",
		"Subtotal_DOP",
		"ITBIS_DOP",
		"Deposito_Garantia_DOP",
		"Total_DOP",
		"Metodo_Pago",
	}
	if err := writer.Write(header); err != nil {
		return "", err
	}
	trimmedPeriod := strings.TrimSpace(period)
	for _, item := range snapshot {
		if !shouldIncludeInDGII607(item, trimmedPeriod) {
			continue
		}
		if err := writer.Write(buildDGII607Row(item)); err != nil {
			return "", err
		}
	}
	writer.Flush()
	if err := writer.Error(); err != nil {
		return "", err
	}
	return buf.String(), nil
}

func hasFinanceAccess(caller platformhttp.Principal) bool {
	if !caller.IsAdminOrStaff() {
		return false
	}
	if caller.Role == "admin" {
		return true
	}
	return caller.HasPermission("finanzas")
}

func (s *Service) ensurePendingPaymentNCFType(ctx context.Context, paymentID string) (string, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	item, exists := s.payments[paymentID]
	if !exists {
		return "", domain.ErrPaymentNotFound
	}
	if !item.IsPendingVerification() {
		return "", domain.ErrPaymentNotPending
	}
	ncfType := strings.ToUpper(strings.TrimSpace(item.NCFType))
	if ncfType != "B01" {
		return "B02", nil
	}
	return ncfType, nil
}

func (s *Service) allocateNextNCF(ctx context.Context, ncfType string) (string, error) {
	normalized := normalizeNCFPrefix(ncfType)
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()

	if store != nil {
		if store.Enabled() {
			if allocator, ok := store.(ncfSequenceAllocator); ok {
				return allocator.AllocateNextNCF(ctx, normalized)
			}
		}
	}
	if normalized == "B01" {
		if s.isNCFExpired("B01", s.clock.Now()) {
			return "", domain.ErrNCFSequenceExpired
		}
		seq := s.ncfSeqB01.Add(1)
		return domain.FormatNCFNumber("B01", seq), nil
	}
	if s.isNCFExpired("B02", s.clock.Now()) {
		return "", domain.ErrNCFSequenceExpired
	}
	seq := s.ncfSeqB02.Add(1)
	return domain.FormatNCFNumber("B02", seq), nil
}

// SetNCFValidUntil configura la fecha límite de vigencia (YYYY-MM-DD) de una secuencia NCF en memoria.
func (s *Service) SetNCFValidUntil(ncfType, validUntilISO string) *Service {
	normalized := normalizeNCFPrefix(ncfType)
	s.mu.Lock()
	if s.ncfValidUntil == nil {
		s.ncfValidUntil = make(map[string]string)
	}
	s.ncfValidUntil[normalized] = strings.TrimSpace(validUntilISO)
	s.mu.Unlock()
	return s
}

func (s *Service) isNCFExpired(ncfType string, now time.Time) bool {
	s.mu.RLock()
	validUntil := ""
	if s.ncfValidUntil != nil {
		validUntil = strings.TrimSpace(s.ncfValidUntil[ncfType])
	}
	s.mu.RUnlock()
	if validUntil == "" {
		return false
	}
	todayAST := now.In(santoDomingoLocation()).Format("2006-01-02")
	return todayAST > validUntil
}

func santoDomingoLocation() *time.Location {
	loc, err := time.LoadLocation("America/Santo_Domingo")
	if err == nil {
		return loc
	}
	return time.FixedZone("America/Santo_Domingo", -4*3600)
}

func normalizeNCFPrefix(ncfType string) string {
	upper := strings.ToUpper(strings.TrimSpace(ncfType))
	if upper == "B01" {
		return "B01"
	}
	return "B02"
}

func (s *Service) markPaymentConfirmedLocked(
	ctx context.Context,
	paymentID string,
	ncfType string,
	ncfNumber string,
	notesOrReason string,
	itbisPercent int,
	caller platformhttp.Principal,
	now time.Time,
) (domain.Payment, Store, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)

	item, exists := s.payments[paymentID]
	if !exists {
		return domain.Payment{}, nil, domain.ErrPaymentNotFound
	}
	if !item.IsPendingVerification() {
		return domain.Payment{}, nil, domain.ErrPaymentNotPending
	}
	applyConfirmedDetails(&item, ncfType, ncfNumber, notesOrReason, itbisPercent, caller, now)
	s.payments[paymentID] = item
	return item, s.store, nil
}

func applyConfirmedDetails(
	item *domain.Payment,
	ncfType string,
	ncfNumber string,
	notesOrReason string,
	itbisPercent int,
	caller platformhttp.Principal,
	now time.Time,
) {
	verifier := resolveVerifierName(caller)
	item.Status = domain.PaymentConfirmed
	item.VerifiedBy = verifier
	item.VerifiedAtISO = now.UTC().Format(time.RFC3339)
	item.RejectionReason = ""
	item.NCFType = ncfType
	item.NCFNumber = ncfNumber
	if item.SubtotalDOP == 0 {
		if item.ITBISDOP == 0 {
			subtotal, itbis := domain.ComputeDominicanTaxBreakdownWithRate(item.AmountRD, item.SecurityDepositDOP, itbisPercent)
			item.SubtotalDOP = subtotal
			item.ITBISDOP = itbis
		}
	}
	trimmedNotes := strings.TrimSpace(notesOrReason)
	if trimmedNotes != "" {
		item.ReferenceNumber = resolveReferenceNote(item.ReferenceNumber, trimmedNotes)
	}
}

func resolveReferenceNote(existing, note string) string {
	if strings.TrimSpace(existing) != "" {
		return existing
	}
	return note
}

func resolveVerifierName(caller platformhttp.Principal) string {
	fullName := strings.TrimSpace(caller.FullName)
	if fullName != "" {
		return fullName
	}
	email := strings.TrimSpace(caller.Email)
	if email != "" {
		return email
	}
	return "Finanzas RosaFiesta"
}

func (s *Service) markPaymentRejectedLocked(
	ctx context.Context,
	paymentID string,
	reason string,
	caller platformhttp.Principal,
	now time.Time,
) (domain.Payment, Store, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)

	item, exists := s.payments[paymentID]
	if !exists {
		return domain.Payment{}, nil, domain.ErrPaymentNotFound
	}
	if !item.IsPendingVerification() {
		return domain.Payment{}, nil, domain.ErrPaymentNotPending
	}
	item.Status = domain.PaymentRejected
	item.RejectionReason = reason
	item.VerifiedBy = resolveVerifierName(caller)
	item.VerifiedAtISO = now.UTC().Format(time.RFC3339)
	s.payments[paymentID] = item
	return item, s.store, nil
}

func (s *Service) dispatchRegistrationSideEffects(ctx context.Context, payment domain.Payment) error {
	if payment.IsPendingVerification() {
		s.publishPaymentEvent(
			realtimedomain.EventPaymentPending,
			realtimedomain.AudienceAdmin,
			payment,
			"Transferencia bancaria pendiente de verificación",
		)
		return nil
	}
	if !payment.IsVerifiedOrConfirmed() {
		return nil
	}
	if err := s.invokeVerifiedSynchronizer(ctx, payment); err != nil {
		return err
	}
	s.sendVerificationReceiptIfConfigured(ctx, payment)
	s.publishPaymentEvent(realtimedomain.EventPaymentVerified, resolvePaymentAudience(payment), payment, "Pago confirmado con NCF emitido")
	return nil
}

func resolvePaymentAudience(payment domain.Payment) realtimedomain.Audience {
	if strings.TrimSpace(payment.ClientID) != "" {
		return realtimedomain.AudienceClient
	}
	return realtimedomain.AudienceAdmin
}

func (s *Service) invokeVerifiedSynchronizer(ctx context.Context, payment domain.Payment) error {
	s.mu.RLock()
	syncer := s.verifiedSync
	s.mu.RUnlock()
	if syncer == nil {
		return nil
	}
	return syncer.OnPaymentVerified(ctx, payment)
}

func (s *Service) sendVerificationReceiptIfConfigured(ctx context.Context, payment domain.Payment) {
	recipient := strings.TrimSpace(payment.ClientEmail)
	if recipient == "" {
		return
	}
	if !s.allowsReceiptEmail(ctx, payment.ClientID, recipient) {
		return
	}
	sender := s.emailSenderSnapshot()
	if sender == nil {
		return
	}
	if !sender.IsConfigured() {
		return
	}
	itbisPercent := s.resolveITBISPercent(ctx)
	subject := fmt.Sprintf("Recibo confirmado %s - %s", payment.ReceiptNumber, payment.EventTitle)
	body := fmt.Sprintf(
		"Hola %s,\n\nTu pago #%s por RD$%d para %s ha sido confirmado.\nNCF (%s): %s\nSubtotal: RD$%d | ITBIS (%d%%): RD$%d\nVerificado por: %s\n\nGracias por elegir RosaFiesta.",
		payment.ClientName,
		payment.ReceiptNumber,
		payment.AmountRD,
		payment.EventTitle,
		payment.NCFType,
		payment.NCFNumber,
		payment.SubtotalDOP,
		itbisPercent,
		payment.ITBISDOP,
		payment.VerifiedBy,
	)
	if mailErr := sender.SendEmail(ctx, recipient, subject, body); mailErr != nil {
		slog.WarnContext(ctx, "payment receipt email failed", slog.String("payment_id", payment.ID), slog.Any("error", mailErr))
	}
}

func (s *Service) allowsReceiptEmail(ctx context.Context, clientID, email string) bool {
	s.mu.RLock()
	checker := s.emailPrefChecker
	s.mu.RUnlock()
	if checker == nil {
		return true
	}
	return checker.AllowsEmailReceipts(ctx, clientID, email)
}

func (s *Service) emailSenderSnapshot() mailer.Sender {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.emailSender
}

func (s *Service) publishPaymentEvent(
	eventType realtimedomain.EventType,
	audience realtimedomain.Audience,
	payment domain.Payment,
	summary string,
) {
	s.mu.RLock()
	pub := s.realtimePub
	s.mu.RUnlock()
	if pub == nil {
		return
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     eventType,
		Audience: audience,
		ClientID: payment.ClientID,
		EntityID: payment.ID,
		Summary:  summary,
		Payload: map[string]any{
			"paymentId":  payment.ID,
			"eventId":    payment.EventID,
			"status":     payment.Status,
			"ncfNumber":  payment.NCFNumber,
			"amountRD":   payment.AmountRD,
			"eventTitle": payment.EventTitle,
		},
	})
}

func (s *Service) stripeSettingsSnapshot() (secretKey, publicBase, apiBase string) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	publicBase = s.publicBaseURL
	if publicBase == "" {
		publicBase = defaultStripePublicReturnBase
	}
	apiBase = s.stripeAPIBaseURL
	if apiBase == "" {
		apiBase = defaultStripeAPIBaseURL
	}
	return s.stripeSecretKey, publicBase, apiBase
}

func buildStripeSessionForm(
	cmd CreateCardSessionCommand,
	ncfType string,
	cleanedRNC string,
	publicBase string,
	callerEmail string,
) url.Values {
	eventID := strings.TrimSpace(cmd.EventID)
	eventTitle := strings.TrimSpace(cmd.EventTitle)
	if eventTitle == "" {
		eventTitle = "Reserva RosaFiesta"
	}
	form := url.Values{}
	form.Set("mode", "payment")
	form.Set("success_url", publicBase+"/pagos?session_id={CHECKOUT_SESSION_ID}&event_id="+url.QueryEscape(eventID))
	form.Set("cancel_url", publicBase+"/pagos?cancelled=1&event_id="+url.QueryEscape(eventID))
	form.Set("line_items[0][quantity]", "1")
	form.Set("line_items[0][price_data][currency]", "dop")
	form.Set("line_items[0][price_data][unit_amount]", strconv.Itoa(cmd.AmountRD*100))
	form.Set("line_items[0][price_data][product_data][name]", "RosaFiesta - "+eventTitle)
	form.Set("metadata[event_id]", eventID)
	form.Set("metadata[kind]", strings.TrimSpace(cmd.Kind))
	form.Set("metadata[ncf_type]", ncfType)
	form.Set("metadata[client_rnc]", cleanedRNC)
	if trimmedPhone := strings.TrimSpace(cmd.ContactPhone); trimmedPhone != "" {
		form.Set("metadata[contact_phone]", trimmedPhone)
	}
	if trimmedEmail := strings.TrimSpace(callerEmail); trimmedEmail != "" {
		form.Set("customer_email", trimmedEmail)
	}
	return form
}

func executeStripeCheckoutRequest(
	ctx context.Context,
	apiBase string,
	secretKey string,
	form url.Values,
) (domain.CardSessionResult, error) {
	endpoint := strings.TrimRight(apiBase, "/") + "/v1/checkout/sessions"
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, strings.NewReader(form.Encode()))
	if err != nil {
		return domain.CardSessionResult{}, err
	}
	req.Header.Set("Authorization", "Bearer "+secretKey)
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	client := &http.Client{Timeout: 15 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return domain.CardSessionResult{}, err
	}
	defer func() { _ = resp.Body.Close() }()

	rawBody, readErr := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
	if readErr != nil {
		return domain.CardSessionResult{}, readErr
	}
	if resp.StatusCode < 200 {
		return domain.CardSessionResult{}, fmt.Errorf("stripe checkout returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawBody)))
	}
	if resp.StatusCode >= 300 {
		return domain.CardSessionResult{}, fmt.Errorf("stripe checkout returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawBody)))
	}
	var payload struct {
		ID  string `json:"id"`
		URL string `json:"url"`
	}
	if unmarshalErr := json.Unmarshal(rawBody, &payload); unmarshalErr != nil {
		return domain.CardSessionResult{}, unmarshalErr
	}
	return domain.CardSessionResult{
		SessionID:   payload.ID,
		CheckoutURL: payload.URL,
		Provider:    "stripe",
	}, nil
}

func shouldIncludeInDGII607(item domain.Payment, period string) bool {
	if !item.IsVerifiedOrConfirmed() {
		return false
	}
	if strings.TrimSpace(item.NCFNumber) == "" {
		return false
	}
	return matchesDGIIPeriod(item, period)
}

func matchesDGIIPeriod(item domain.Payment, period string) bool {
	if period == "" {
		return true
	}
	compactPeriod := strings.ReplaceAll(strings.TrimSpace(period), "-", "")
	if parsed, err := time.Parse(time.RFC3339, strings.TrimSpace(item.VerifiedAtISO)); err == nil {
		astMonth := parsed.In(santoDomingoLocation()).Format("200601")
		if len(compactPeriod) == 6 {
			return astMonth == compactPeriod
		}
		if len(compactPeriod) == 4 {
			return strings.HasPrefix(astMonth, compactPeriod)
		}
	}
	compactVerified := strings.ReplaceAll(item.VerifiedAtISO, "-", "")
	if strings.Contains(compactVerified, compactPeriod) {
		return true
	}
	return strings.Contains(strings.ToLower(item.PaidDateLabel), strings.ToLower(period))
}

func buildDGII607Row(item domain.Payment) []string {
	subtotal := item.SubtotalDOP
	itbis := item.ITBISDOP
	if subtotal == 0 {
		if itbis == 0 {
			subtotal, itbis = domain.ComputeDominicanTaxBreakdown(item.AmountRD, item.SecurityDepositDOP)
		}
	}
	return []string{
		sanitizeCSVCell(item.ClientRNC),
		sanitizeCSVCell(item.NCFType),
		sanitizeCSVCell(item.NCFNumber),
		sanitizeCSVCell(item.PaidDateLabel),
		sanitizeCSVCell(item.EventID),
		sanitizeCSVCell(item.ClientName),
		strconv.Itoa(subtotal),
		strconv.Itoa(itbis),
		strconv.Itoa(item.SecurityDepositDOP),
		strconv.Itoa(item.AmountRD),
		sanitizeCSVCell(string(item.Method)),
	}
}

func sanitizeCSVCell(raw string) string {
	if raw == "" {
		return ""
	}
	switch raw[0] {
	case '=', '+', '-', '@', '\t', '\r':
		return "'" + raw
	default:
		return raw
	}
}
