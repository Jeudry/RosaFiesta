package app

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"

	catalogapp "github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	catalogdomain "github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	eventsapp "github.com/Jeudry/rosafiesta/server/internal/events/app"
	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	identitydomain "github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	operationsapp "github.com/Jeudry/rosafiesta/server/internal/operations/app"
	operationsdomain "github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	paymentsapp "github.com/Jeudry/rosafiesta/server/internal/payments/app"
	paymentsdomain "github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
	quotesdomain "github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

type catalogStockAdapter struct {
	catalog *catalogapp.Service
}

func (a catalogStockAdapter) ReserveEventStock(
	ctx context.Context,
	eventID string,
	quoteID string,
	dateOrISO string,
	status string,
	items []eventsdomain.EventItem,
) error {
	reqs := mapEventItemsToRequirements(items)
	resStatus := parseReservationStatus(status)
	if err := a.catalog.ReserveStockForEvent(ctx, eventID, quoteID, dateOrISO, resStatus, reqs); err != nil {
		if errors.Is(err, catalogdomain.ErrInsufficientStockForDate) {
			return fmt.Errorf("%w: %w", eventsdomain.ErrInsufficientStock, err)
		}
		return err
	}
	return nil
}

func (a catalogStockAdapter) TransitionEventStock(ctx context.Context, eventID, status string) error {
	return a.catalog.TransitionEventReservations(ctx, eventID, parseReservationStatus(status))
}

func (a catalogStockAdapter) CheckQuoteAvailability(
	ctx context.Context,
	rawDate string,
	excludeEventID string,
	lines []quotesdomain.QuoteLine,
) error {
	reqs := mapQuoteLinesToRequirements(lines)
	check, err := a.catalog.CheckAvailabilityForDate(ctx, rawDate, excludeEventID, reqs)
	if err != nil {
		return err
	}
	if !check.Available {
		msg := catalogdomain.BuildAvailabilityConflictMessage(check.Conflicts)
		return fmt.Errorf("%w: %s", quotesdomain.ErrInsufficientStock, msg)
	}
	return nil
}

func (a catalogStockAdapter) AdjustDamagedStock(ctx context.Context, articleRef string, delta int) error {
	trimmed := strings.TrimSpace(articleRef)
	if trimmed == "" {
		return nil
	}
	_, err := a.catalog.AdjustDamagedStock(ctx, trimmed, delta)
	if errors.Is(err, catalogdomain.ErrArticleNotFound) {
		return nil
	}
	return err
}

func mapEventItemsToRequirements(items []eventsdomain.EventItem) []catalogdomain.StockRequirement {
	out := make([]catalogdomain.StockRequirement, 0, len(items))
	for _, item := range items {
		out = append(out, catalogdomain.StockRequirement{
			ArticleRef: item.ArticleCode,
			Name:       item.Name,
			Quantity:   item.Quantity,
		})
	}
	return out
}

func mapQuoteLinesToRequirements(lines []quotesdomain.QuoteLine) []catalogdomain.StockRequirement {
	out := make([]catalogdomain.StockRequirement, 0, len(lines))
	for _, line := range lines {
		out = append(out, catalogdomain.StockRequirement{
			ArticleRef: line.ArticleCode,
			Name:       line.Name,
			Quantity:   line.Quantity,
		})
	}
	return out
}

func parseReservationStatus(raw string) catalogdomain.ReservationStatus {
	normalized := strings.ToLower(strings.TrimSpace(raw))
	switch catalogdomain.ReservationStatus(normalized) {
	case catalogdomain.ReservationConfirmed:
		return catalogdomain.ReservationConfirmed
	case catalogdomain.ReservationReleased:
		return catalogdomain.ReservationReleased
	case catalogdomain.ReservationCancelled:
		return catalogdomain.ReservationCancelled
	default:
		return catalogdomain.ReservationHeld
	}
}

type quoteLifecycleAdapter struct {
	events     *eventsapp.Service
	operations *operationsapp.Service
	identity   *identityapp.Service
	pushSender push.Sender
}

func (a quoteLifecycleAdapter) ProvisionEventFromQuote(ctx context.Context, quote quotesdomain.Quote) (string, error) {
	clientUUID := parseOptionalUUID(quote.ClientID)
	items := make([]eventsdomain.EventItem, 0, len(quote.Lines))
	for _, line := range quote.Lines {
		items = append(items, eventsdomain.EventItem{
			ArticleCode: line.ArticleCode,
			Name:        line.Name,
			Quantity:    line.Quantity,
			UnitPriceRD: line.UnitPriceRD,
		})
	}
	evt, err := a.events.ProvisionFromSignedQuote(ctx, eventsapp.ProvisionFromQuoteCommand{
		EventID:      quote.EventID,
		QuoteID:      quote.ID,
		ClientID:     clientUUID,
		ClientEmail:  quote.ClientEmail,
		ClientName:   quote.ClientName,
		Title:        quote.EventTitle,
		DateLabel:    quote.EventDateLabel,
		EventDateISO: quote.EventDateISO,
		Venue:        quote.Venue,
		Guests:       quote.Guests,
		TotalRD:      quote.TotalRD,
		Items:        items,
	})
	if err != nil {
		if errors.Is(err, eventsdomain.ErrInsufficientStock) {
			return "", fmt.Errorf("%w: %w", quotesdomain.ErrInsufficientStock, err)
		}
		return "", err
	}
	return evt.ID, nil
}

func (a quoteLifecycleAdapter) NotifyQuoteSigned(ctx context.Context, quote quotesdomain.Quote) error {
	title := "Contrato firmado: " + quote.EventTitle
	body := fmt.Sprintf("%s firmó el contrato %s por RD$%d.", quote.ClientName, quote.Code, quote.TotalRD)
	a.operations.CreateNotification(
		ctx,
		title,
		body,
		"cotizaciones",
		"admin",
		"",
	)
	if a.identity != nil {
		dispatchRemotePush(ctx, a.pushSender, a.identity.ActiveAdminPushTokens(ctx), title, body, "cotizaciones")
	}
	return nil
}

func (a quoteLifecycleAdapter) NotifyQuoteSent(ctx context.Context, quote quotesdomain.Quote) {
	if a.operations == nil {
		return
	}
	clientID := strings.TrimSpace(quote.ClientID)
	title := "Cotización lista: " + quote.EventTitle
	body := fmt.Sprintf("Recibiste la cotización %s por RD$%d. Revísala y fírmala en la app.", quote.Code, quote.TotalRD)
	a.operations.CreateNotification(ctx, title, body, "cotizaciones", "client", clientID)
	clientUUID := parseOptionalUUID(clientID)
	canPushClient := a.identity != nil
	if canPushClient {
		canPushClient = clientUUID != uuid.Nil
	}
	if !canPushClient {
		return
	}
	prefs := a.identity.PreferencesForAccount(ctx, clientUUID, quote.ClientEmail)
	if !prefs.PushNotificationsEnabled {
		return
	}
	tokens := a.identity.ActivePushTokensForUser(ctx, clientUUID)
	dispatchRemotePush(ctx, a.pushSender, tokens, title, body, "cotizaciones")
}

func parseOptionalUUID(raw string) uuid.UUID {
	parsed, err := uuid.Parse(strings.TrimSpace(raw))
	if err != nil {
		return uuid.Nil
	}
	return parsed
}

type paymentVerifiedAdapter struct {
	events     *eventsapp.Service
	operations *operationsapp.Service
	identity   *identityapp.Service
	pushSender push.Sender
}

func (a paymentVerifiedAdapter) OnPaymentVerified(ctx context.Context, payment paymentsdomain.Payment) error {
	eventID := strings.TrimSpace(payment.EventID)
	if eventID != "" {
		evt, _, err := a.events.ApplyVerifiedPayment(ctx, eventID, payment.AmountRD)
		isUnexpectedErr := err != nil
		if isUnexpectedErr {
			isUnexpectedErr = !errors.Is(err, eventsdomain.ErrEventNotFound)
		}
		if isUnexpectedErr {
			return err
		}
		isConfirmed := err == nil
		if isConfirmed {
			isConfirmed = evt.Status == eventsdomain.StatusConfirmed
		}
		if isConfirmed {
			if logiErr := a.operations.EnsureLogisticsForConfirmedEvent(
				ctx,
				evt.ID,
				evt.Title,
				evt.Venue,
				evt.Zone,
				evt.DateLabel,
			); logiErr != nil {
				return logiErr
			}
		}
	}
	a.notifyPaymentClient(
		ctx,
		payment,
		"Pago confirmado · "+payment.ReceiptNumber,
		fmt.Sprintf("Tu pago de RD$%d para %s fue verificado.", payment.AmountRD, payment.EventTitle),
	)
	return nil
}

func (a paymentVerifiedAdapter) OnPaymentRejected(ctx context.Context, payment paymentsdomain.Payment, reason string) {
	body := fmt.Sprintf("Tu comprobante de RD$%d para %s fue rechazado: %s", payment.AmountRD, payment.EventTitle, reason)
	a.notifyPaymentClient(ctx, payment, "Comprobante rechazado · "+payment.ReceiptNumber, body)
}

func (a paymentVerifiedAdapter) notifyPaymentClient(
	ctx context.Context,
	payment paymentsdomain.Payment,
	title string,
	body string,
) {
	if a.operations == nil {
		return
	}
	clientID := strings.TrimSpace(payment.ClientID)
	a.operations.CreateNotification(ctx, title, body, "pagos", "client", clientID)
	clientUUID := parseOptionalUUID(clientID)
	canPushClient := a.identity != nil
	if canPushClient {
		canPushClient = clientUUID != uuid.Nil
	}
	if !canPushClient {
		return
	}
	prefs := a.identity.PreferencesForAccount(ctx, clientUUID, payment.ClientEmail)
	if !prefs.PushNotificationsEnabled {
		return
	}
	tokens := a.identity.ActivePushTokensForUser(ctx, clientUUID)
	dispatchRemotePush(ctx, a.pushSender, tokens, title, body, "pagos")
}

type damageChargeAdapter struct {
	payments *paymentsapp.Service
}

func (a damageChargeAdapter) RecordDamageCharge(ctx context.Context, report operationsdomain.DamageReport) (string, error) {
	if report.ReplacementRD <= 0 {
		return "", nil
	}
	adminCaller := platformhttp.Principal{Role: "admin", FullName: "Operaciones RosaFiesta"}
	payment, err := a.payments.RegisterPaymentForCaller(ctx, paymentsapp.RegisterPaymentCommand{
		EventID:       report.EventID,
		EventTitle:    "Reposición · " + report.ArticleName,
		AmountRD:      report.ReplacementRD,
		Method:        string(paymentsdomain.MethodCash),
		Kind:          string(paymentsdomain.KindDamage),
		ReferenceCode: "DMG-" + strings.ToUpper(report.ID),
		NCFType:       "B02",
	}, adminCaller)
	if err != nil {
		return "", err
	}
	return payment.ID, nil
}

type offlineDispatcherAdapter struct {
	events     *eventsapp.Service
	operations *operationsapp.Service
	payments   *paymentsapp.Service
}

func (a offlineDispatcherAdapter) DispatchMutation(
	ctx context.Context,
	opType string,
	payloadJSON string,
	caller platformhttp.Principal,
) error {
	trimmedType := strings.ToLower(strings.TrimSpace(opType))
	if trimmedType == "" {
		return nil
	}
	switch trimmedType {
	case "checklist.toggle":
		return a.dispatchChecklistToggle(ctx, payloadJSON, caller)
	case "damage.report":
		return a.dispatchDamageReport(ctx, payloadJSON, caller)
	case "chat.send":
		return a.dispatchChatMessage(ctx, payloadJSON, caller)
	case "guest.add":
		return a.dispatchGuestAdd(ctx, payloadJSON, caller)
	case "payment.register":
		return a.dispatchPaymentRegister(ctx, payloadJSON, caller)
	default:
		return nil
	}
}

func callerHasAnyOfflinePermission(caller platformhttp.Principal, permissions ...string) bool {
	if !caller.IsAdminOrStaff() {
		return false
	}
	for _, perm := range permissions {
		if caller.HasPermission(perm) {
			return true
		}
	}
	return false
}

func (a offlineDispatcherAdapter) dispatchChecklistToggle(
	ctx context.Context,
	payloadJSON string,
	caller platformhttp.Principal,
) error {
	if !callerHasAnyOfflinePermission(caller, "tareas", "eventos", "decoracion") {
		return operationsdomain.ErrOfflineActionForbidden
	}
	var body struct {
		EventID   string `json:"eventId"`
		ItemID    string `json:"itemId"`
		Completed bool   `json:"completed"`
		StaffName string `json:"staffName"`
	}
	if err := json.Unmarshal([]byte(payloadJSON), &body); err != nil {
		return fmt.Errorf("decode checklist offline payload: %w", err)
	}
	if strings.TrimSpace(body.EventID) == "" {
		return nil
	}
	_, err := a.operations.ToggleChecklistItem(ctx, body.EventID, body.ItemID, body.Completed, body.StaffName)
	return err
}

func (a offlineDispatcherAdapter) dispatchDamageReport(
	ctx context.Context,
	payloadJSON string,
	caller platformhttp.Principal,
) error {
	if !callerHasAnyOfflinePermission(caller, "inventario", "eventos", "decoracion") {
		return operationsdomain.ErrOfflineActionForbidden
	}
	var cmd operationsapp.ReportDamageCommand
	if err := json.Unmarshal([]byte(payloadJSON), &cmd); err != nil {
		return fmt.Errorf("decode damage offline payload: %w", err)
	}
	if strings.TrimSpace(cmd.EventID) == "" {
		return nil
	}
	_, err := a.operations.ReportDamageWithDetails(ctx, cmd)
	return err
}

func (a offlineDispatcherAdapter) dispatchChatMessage(
	ctx context.Context,
	payloadJSON string,
	caller platformhttp.Principal,
) error {
	var body struct {
		ThreadID       string `json:"threadId"`
		EventID        string `json:"eventId"`
		EventTitle     string `json:"eventTitle"`
		RecipientLabel string `json:"recipientLabel"`
		Body           string `json:"body"`
		AttachmentName string `json:"attachmentName"`
		AttachmentURL  string `json:"attachmentUrl"`
	}
	if err := json.Unmarshal([]byte(payloadJSON), &body); err != nil {
		return fmt.Errorf("decode chat offline payload: %w", err)
	}
	if strings.TrimSpace(body.Body) == "" {
		return nil
	}
	_, err := a.events.PostChatMessage(ctx, body.ThreadID, eventsapp.PostChatMessageCommand{
		EventID:        body.EventID,
		EventTitle:     body.EventTitle,
		RecipientLabel: body.RecipientLabel,
		Body:           body.Body,
		AttachmentName: body.AttachmentName,
		AttachmentURL:  body.AttachmentURL,
	}, caller)
	return err
}

func (a offlineDispatcherAdapter) dispatchGuestAdd(
	ctx context.Context,
	payloadJSON string,
	caller platformhttp.Principal,
) error {
	var body struct {
		EventID      string `json:"eventId"`
		FullName     string `json:"fullName"`
		RSVPStatus   string `json:"rsvpStatus"`
		TableLabel   string `json:"tableLabel"`
		DietaryNotes string `json:"dietaryNotes"`
	}
	if err := json.Unmarshal([]byte(payloadJSON), &body); err != nil {
		return fmt.Errorf("decode guest offline payload: %w", err)
	}
	if strings.TrimSpace(body.EventID) == "" {
		return nil
	}
	_, err := a.events.AddGuest(ctx, body.EventID, eventsapp.AddGuestCommand{
		FullName:     body.FullName,
		RSVPStatus:   body.RSVPStatus,
		TableLabel:   body.TableLabel,
		DietaryNotes: body.DietaryNotes,
	}, caller)
	return err
}

func (a offlineDispatcherAdapter) dispatchPaymentRegister(
	ctx context.Context,
	payloadJSON string,
	caller platformhttp.Principal,
) error {
	var cmd paymentsapp.RegisterPaymentCommand
	if err := json.Unmarshal([]byte(payloadJSON), &cmd); err != nil {
		return fmt.Errorf("decode payment offline payload: %w", err)
	}
	if cmd.AmountRD <= 0 {
		return nil
	}
	_, err := a.payments.RegisterPaymentForCaller(ctx, cmd, caller)
	return err
}

type damageExpensesAdapter struct {
	operations *operationsapp.Service
}

func (a damageExpensesAdapter) TotalDamageExpensesRD(ctx context.Context) int {
	if a.operations == nil {
		return 0
	}
	total := 0
	for _, dmg := range a.operations.ListDamages(ctx, "") {
		total += dmg.ReplacementRD
	}
	return total
}

type userPreferencesAdapter struct {
	identity    *identityapp.Service
	operations  *operationsapp.Service
	emailSender mailer.Sender
	pushSender  push.Sender
}

func (a userPreferencesAdapter) AllowsEmailReceipts(ctx context.Context, clientID string, email string) bool {
	if a.identity == nil {
		return true
	}
	parsedID := parseOptionalUUID(clientID)
	return a.identity.PreferencesForAccount(ctx, parsedID, email).EmailReceiptsEnabled
}

func (a userPreferencesAdapter) DispatchPaymentReminder(
	ctx context.Context,
	event eventsdomain.Event,
	channel string,
	customMessage string,
) (bool, error) {
	balance := event.BalanceRD()
	bodyText := strings.TrimSpace(customMessage)
	if bodyText == "" {
		bodyText = fmt.Sprintf(
			"Hola %s, te recordamos que el evento \"%s\" tiene un saldo pendiente de RD$%d.",
			event.ClientName, event.Title, balance,
		)
	}
	prefs := a.identity.PreferencesForAccount(ctx, event.ClientID, event.ClientEmail)
	dispatched := a.sendReminderByChannel(ctx, event, channel, bodyText, prefs.EmailReceiptsEnabled, prefs.WhatsAppAlertsEnabled)
	if a.operations != nil {
		if prefs.PushNotificationsEnabled {
			title := "Recordatorio de saldo · " + event.Title
			clientID := ""
			if event.ClientID != uuid.Nil {
				clientID = event.ClientID.String()
			}
			a.operations.CreateNotification(
				ctx,
				title,
				bodyText,
				"pagos",
				"client",
				clientID,
			)
			if a.identity != nil {
				tokens := a.identity.ActivePushTokensForUser(ctx, event.ClientID)
				if dispatchRemotePush(ctx, a.pushSender, tokens, title, bodyText, "pagos") {
					dispatched = true
				}
			}
		}
	}
	return dispatched, nil
}

func (a userPreferencesAdapter) sendReminderByChannel(
	ctx context.Context,
	event eventsdomain.Event,
	channel string,
	bodyText string,
	allowEmail bool,
	allowWhatsApp bool,
) bool {
	sent := false
	cleanChannel := strings.ToLower(strings.TrimSpace(channel))
	if cleanChannel == "whatsapp" {
		if allowWhatsApp {
			if a.operations != nil {
				if phone := a.lookupClientPhone(ctx, event); phone != "" {
					if _, err := a.operations.SendWhatsAppBotReply(ctx, "", phone, bodyText); err == nil {
						sent = true
					}
				}
			}
		}
		return sent
	}
	if allowEmail {
		if a.emailSender != nil {
			if a.emailSender.IsConfigured() {
				if strings.TrimSpace(event.ClientEmail) != "" {
					if err := a.emailSender.SendEmail(ctx, event.ClientEmail, "Recordatorio de pago · "+event.Title, bodyText); err == nil {
						sent = true
					}
				}
			}
		}
	}
	return sent
}

func (a userPreferencesAdapter) lookupClientPhone(ctx context.Context, event eventsdomain.Event) string {
	if a.identity == nil {
		return ""
	}
	for _, client := range a.identity.Clients(ctx) {
		if strings.EqualFold(client.Email, event.ClientEmail) {
			return strings.TrimSpace(client.Phone)
		}
		if strings.EqualFold(client.FullName, event.ClientName) {
			return strings.TrimSpace(client.Phone)
		}
	}
	for _, usr := range a.identity.ListRegisteredUsers(ctx) {
		if event.ClientID != uuid.Nil {
			if usr.ID == event.ClientID {
				return strings.TrimSpace(usr.Phone)
			}
		}
		if strings.EqualFold(usr.Email, event.ClientEmail) {
			return strings.TrimSpace(usr.Phone)
		}
		if strings.EqualFold(usr.FullName, event.ClientName) {
			return strings.TrimSpace(usr.Phone)
		}
	}
	return ""
}

type businessSettingsAdapter struct {
	operations *operationsapp.Service
}

func (a businessSettingsAdapter) CurrentITBISPercent(ctx context.Context) int {
	if a.operations == nil {
		return 18
	}
	settings, _ := a.operations.GetSettings(ctx)
	if settings.ItbisPercent < 0 {
		return 18
	}
	return settings.ItbisPercent
}

func (a businessSettingsAdapter) CurrentDepositPercent(ctx context.Context) int {
	if a.operations == nil {
		return 50
	}
	settings, _ := a.operations.GetSettings(ctx)
	if settings.DepositPercent > 0 {
		return settings.DepositPercent
	}
	return 50
}

type refundApprovedAdapter struct {
	events     *eventsapp.Service
	catalog    *catalogapp.Service
	operations *operationsapp.Service
	identity   *identityapp.Service
	pushSender push.Sender
}

func (a refundApprovedAdapter) OnRefundApproved(
	ctx context.Context,
	refund paymentsdomain.Refund,
	opts paymentsapp.ApproveRefundOptions,
) {
	shouldRelease := opts.ReleaseInventory
	if shouldRelease {
		shouldRelease = strings.TrimSpace(refund.EventID) != ""
	}
	if shouldRelease {
		if a.catalog != nil {
			_ = a.catalog.TransitionEventReservations(ctx, refund.EventID, catalogdomain.ReservationReleased)
		}
	}
	event, hasEvent := a.lookupEvent(ctx, refund.EventID)
	clientID := ""
	clientEmail := ""
	clientName := refund.ClientName
	if hasEvent {
		if event.ClientID != uuid.Nil {
			clientID = event.ClientID.String()
		}
		clientEmail = event.ClientEmail
		if strings.TrimSpace(event.ClientName) != "" {
			clientName = event.ClientName
		}
	}
	body := strings.TrimSpace(opts.ClientMessage)
	if body == "" {
		body = fmt.Sprintf("Hola %s, tu reembolso %s por RD$%d fue aprobado.", clientName, refund.ID, refund.RefundDop)
	}
	if a.operations != nil {
		a.operations.CreateNotification(ctx, "Reembolso aprobado · "+refund.ID, body, "pagos", "client", clientID)
	}
	a.dispatchRefundAlertChannels(ctx, hasEvent, event, clientID, clientEmail, body, opts.NotifyWhatsApp)
}

func (a refundApprovedAdapter) lookupEvent(ctx context.Context, eventID string) (eventsdomain.Event, bool) {
	if a.events == nil {
		return eventsdomain.Event{}, false
	}
	if strings.TrimSpace(eventID) == "" {
		return eventsdomain.Event{}, false
	}
	for _, evt := range a.events.AllEventsSnapshot(ctx) {
		if evt.ID == eventID {
			return evt, true
		}
	}
	return eventsdomain.Event{}, false
}

func (a refundApprovedAdapter) dispatchRefundAlertChannels(
	ctx context.Context,
	hasEvent bool,
	event eventsdomain.Event,
	clientID string,
	clientEmail string,
	body string,
	notifyWhatsApp bool,
) {
	if a.identity == nil {
		return
	}
	clientUUID := parseOptionalUUID(clientID)
	prefs := a.identity.PreferencesForAccount(ctx, clientUUID, clientEmail)
	canPush := prefs.PushNotificationsEnabled
	if canPush {
		canPush = clientUUID != uuid.Nil
	}
	if canPush {
		tokens := a.identity.ActivePushTokensForUser(ctx, clientUUID)
		dispatchRemotePush(ctx, a.pushSender, tokens, "Reembolso aprobado", body, "pagos")
	}
	canSendWhatsApp := notifyWhatsApp
	if canSendWhatsApp {
		canSendWhatsApp = prefs.WhatsAppAlertsEnabled
	}
	if canSendWhatsApp {
		canSendWhatsApp = a.operations != nil
	}
	if canSendWhatsApp {
		canSendWhatsApp = hasEvent
	}
	if canSendWhatsApp {
		phone := userPreferencesAdapter{identity: a.identity}.lookupClientPhone(ctx, event)
		if phone != "" {
			_, _ = a.operations.SendWhatsAppBotReply(ctx, "", phone, body)
		}
	}
}

type reportSnapshotAdapter struct {
	payments *paymentsapp.Service
	events   *eventsapp.Service
	catalog  *catalogapp.Service
	identity *identityapp.Service
}

func (a reportSnapshotAdapter) BuildReportSnapshot(
	ctx context.Context,
	months int,
	now time.Time,
) (operationsdomain.ReportSnapshot, error) {
	byKey := make(map[string]*operationsdomain.ReportMonthMetric, months)
	anchor := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
	for offset := 0; offset < months; offset++ {
		ts := anchor.AddDate(0, -offset, 0)
		key := fmt.Sprintf("%04d-%02d", ts.Year(), int(ts.Month()))
		byKey[key] = &operationsdomain.ReportMonthMetric{Year: ts.Year(), Month: ts.Month()}
	}
	eventDateByID := a.aggregateEventsIntoSnapshot(ctx, byKey)
	a.aggregatePaymentsIntoSnapshot(byKey, eventDateByID, anchor)
	a.applyTotalsToSnapshot(ctx, byKey)

	out := make([]operationsdomain.ReportMonthMetric, 0, len(byKey))
	for _, metric := range byKey {
		out = append(out, *metric)
	}
	return operationsdomain.ReportSnapshot{Months: out}, nil
}

func (a reportSnapshotAdapter) aggregateEventsIntoSnapshot(
	ctx context.Context,
	byKey map[string]*operationsdomain.ReportMonthMetric,
) map[string]time.Time {
	datesByEvent := make(map[string]time.Time)
	if a.events == nil {
		return datesByEvent
	}
	for _, evt := range a.events.AllEventsSnapshot(ctx) {
		parsed, ok := parseReminderEventDate(evt.EventDateISO)
		if !ok {
			continue
		}
		datesByEvent[evt.ID] = parsed
		if evt.Status == eventsdomain.StatusCancelled {
			continue
		}
		key := fmt.Sprintf("%04d-%02d", parsed.Year(), int(parsed.Month()))
		if metric, exists := byKey[key]; exists {
			metric.EventsCount++
		}
	}
	return datesByEvent
}

func (a reportSnapshotAdapter) aggregatePaymentsIntoSnapshot(
	byKey map[string]*operationsdomain.ReportMonthMetric,
	datesByEvent map[string]time.Time,
	fallback time.Time,
) {
	if a.payments == nil {
		return
	}
	for _, pay := range a.payments.ListPayments("") {
		if !pay.IsVerifiedOrConfirmed() {
			continue
		}
		ts := resolvePaymentReportTime(pay, datesByEvent, fallback)
		key := fmt.Sprintf("%04d-%02d", ts.Year(), int(ts.Month()))
		if metric, exists := byKey[key]; exists {
			metric.IncomeRD += pay.AmountRD
		}
	}
}

func (a reportSnapshotAdapter) applyTotalsToSnapshot(
	ctx context.Context,
	byKey map[string]*operationsdomain.ReportMonthMetric,
) {
	totalStock := 0
	if a.catalog != nil {
		for _, article := range a.catalog.Articles(ctx, "") {
			totalStock += article.TotalStock
		}
	}
	totalClients := 0
	if a.identity != nil {
		totalClients = len(a.identity.Clients(ctx))
	}
	for _, metric := range byKey {
		metric.InventoryCount = totalStock
		metric.ClientsCount = totalClients
	}
}

func resolvePaymentReportTime(
	pay paymentsdomain.Payment,
	datesByEvent map[string]time.Time,
	fallback time.Time,
) time.Time {
	trimmedISO := strings.TrimSpace(pay.VerifiedAtISO)
	if trimmedISO != "" {
		if parsed, err := time.Parse(time.RFC3339, trimmedISO); err == nil {
			return parsed
		}
		if parsed, ok := parseReminderEventDate(trimmedISO); ok {
			return parsed
		}
	}
	if evtDate, ok := datesByEvent[pay.EventID]; ok {
		return evtDate
	}
	return fallback
}

type reviewEventAdapter struct {
	events *eventsapp.Service
}

func (a reviewEventAdapter) VerifyEventOwnership(
	ctx context.Context,
	eventID string,
	caller platformhttp.Principal,
) error {
	_, err := a.events.GetEventForCaller(ctx, eventID, caller, true)
	if errors.Is(err, eventsdomain.ErrEventForbidden) {
		return operationsdomain.ErrReviewForbidden
	}
	if errors.Is(err, eventsdomain.ErrEventNotFound) {
		return operationsdomain.ErrReviewEventNotFound
	}
	return err
}

func dispatchRemotePush(
	ctx context.Context,
	sender push.Sender,
	tokens []identitydomain.DevicePushToken,
	title string,
	body string,
	category string,
) bool {
	if sender == nil {
		return false
	}
	if !sender.IsConfigured() {
		return false
	}
	delivered := false
	for _, device := range tokens {
		err := sender.SendPush(ctx, push.Message{
			Token:      device.Token,
			Platform:   device.Platform,
			AppVariant: device.AppVariant,
			Title:      title,
			Body:       body,
			Category:   category,
		})
		if err == nil {
			delivered = true
		}
	}
	return delivered
}
