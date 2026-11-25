// Package domain define las entidades y reglas de negocio puras del módulo de pagos.
package domain

import (
	"errors"
	"fmt"
	"math"
	"strings"
	"time"
	"unicode"
)

var (
	// ErrPaymentNotFound indica que el pago solicitado no existe.
	ErrPaymentNotFound = errors.New("payment not found")
	// ErrPaymentForbidden indica que el usuario no tiene permiso para operar sobre este pago.
	ErrPaymentForbidden = errors.New("caller is not allowed to access this payment")
	// ErrPaymentNotPending indica que el pago no se encuentra pendiente de verificación.
	ErrPaymentNotPending = errors.New("payment is not pending verification")
	// ErrInvalidFiscalRNC indica que el tipo de NCF o el RNC/Cédula fiscal es inválido.
	ErrInvalidFiscalRNC = errors.New("invalid NCF type or Dominican RNC/Cédula (B01 requires 9 or 11 digits)")
	// ErrCardGatewayNotConfigured indica que la pasarela de pagos con tarjeta no está configurada.
	ErrCardGatewayNotConfigured = errors.New("card payment gateway is not configured")
	// ErrInvalidWebhookSignature indica que la firma Stripe-Signature del webhook no es válida.
	ErrInvalidWebhookSignature = errors.New("invalid stripe webhook signature")
	// ErrCardSessionNotPaid indica que la sesión de Stripe aún no está marcada como pagada.
	ErrCardSessionNotPaid = errors.New("stripe checkout session is not paid")
	// ErrRefundNotFound indica que la solicitud de reembolso no existe.
	ErrRefundNotFound = errors.New("refund not found")
	// ErrRefundAlreadyApproved impide aprobar o reembolsar dos veces la misma solicitud.
	ErrRefundAlreadyApproved = errors.New("refund has already been approved")
	// ErrInvalidPaymentAmount indica que el monto del pago debe ser mayor que cero.
	ErrInvalidPaymentAmount = errors.New("payment amount must be greater than zero")
	// ErrInvalidPaymentMethod indica que el método de pago no es válido.
	ErrInvalidPaymentMethod = errors.New("invalid payment method; use transferencia, tarjeta, or efectivo")
	// ErrCardCheckoutRequired indica que un cliente debe usar la pasarela Stripe para pagar con tarjeta.
	ErrCardCheckoutRequired = errors.New("card payments require Stripe checkout session")
	// ErrClientCashRestricted indica que solo administración puede registrar pagos inmediatos en efectivo.
	ErrClientCashRestricted = errors.New("clients must pay via transfer voucher or card checkout")
	// ErrInvalidPaymentCurrency rechaza sesiones de pago emitidas en una moneda distinta a DOP.
	ErrInvalidPaymentCurrency = errors.New("stripe checkout currency must be DOP")
	// ErrNCFSequenceExpired indica que la secuencia NCF configurada está vencida o agotada.
	ErrNCFSequenceExpired = errors.New("ncf sequence is expired or exhausted")
)

// PaymentMethod representa el medio utilizado para efectuar el pago.
type PaymentMethod string

const (
	// MethodTransfer representa transferencia o depósito bancario.
	MethodTransfer PaymentMethod = "transferencia"
	// MethodCard representa pago electrónico con tarjeta.
	MethodCard PaymentMethod = "tarjeta"
	// MethodCash representa pago en efectivo.
	MethodCash PaymentMethod = "efectivo"
)

// PaymentKind clasifica el concepto del pago recibido.
type PaymentKind string

const (
	// KindDeposit corresponde al anticipo de reserva del evento.
	KindDeposit PaymentKind = "anticipo"
	// KindBalance corresponde al saldo pendiente del evento.
	KindBalance PaymentKind = "saldo"
	// KindDamage corresponde a cargos de reposición o daños.
	KindDamage PaymentKind = "reposicion"
)

// PaymentStatus describe el ciclo de validación de un pago.
type PaymentStatus string

const (
	// PaymentPendingVerification indica que el comprobante espera aprobación de finanzas.
	PaymentPendingVerification PaymentStatus = "pending_verification"
	// PaymentVerified indica que el pago fue verificado inmediatamente o en validación.
	PaymentVerified PaymentStatus = "verified"
	// PaymentConfirmed indica que el pago fue confirmado manualmente con NCF emitido.
	PaymentConfirmed PaymentStatus = "confirmed"
	// PaymentRejected indica que el comprobante bancario fue rechazado.
	PaymentRejected PaymentStatus = "rejected"
	// PaymentFailed indica que la transacción falló.
	PaymentFailed PaymentStatus = "failed"
)

// RefundStatus representa el estado de autorización de un reembolso.
type RefundStatus string

const (
	// RefundStatusPending indica que el reembolso espera aprobación.
	RefundStatusPending RefundStatus = "pending"
	// RefundStatusApproved indica que el reembolso fue aprobado.
	RefundStatusApproved RefundStatus = "approved"
)

// Payment representa un recibo de pago registrado en RosaFiesta con su desglose fiscal.
type Payment struct {
	ID                     string        `json:"id"`
	ReceiptNumber          string        `json:"receiptNumber"`
	EventID                string        `json:"eventId"`
	EventTitle             string        `json:"eventTitle"`
	ClientID               string        `json:"clientId,omitempty"`
	ClientEmail            string        `json:"clientEmail,omitempty"`
	ClientName             string        `json:"clientName"`
	AmountRD               int           `json:"amountRD"`
	Method                 PaymentMethod `json:"method"`
	Kind                   PaymentKind   `json:"kind"`
	BankName               string        `json:"bankName,omitempty"`
	PaidDateLabel          string        `json:"paidDateLabel"`
	ReferenceCode          string        `json:"referenceCode"`
	Status                 PaymentStatus `json:"status"`
	VoucherURL             string        `json:"voucherUrl,omitempty"`
	ReferenceNumber        string        `json:"referenceNumber,omitempty"`
	VerifiedBy             string        `json:"verifiedBy,omitempty"`
	VerifiedAtISO          string        `json:"verifiedAtIso,omitempty"`
	RejectionReason        string        `json:"rejectionReason,omitempty"`
	NCFType                string        `json:"ncfType,omitempty"`
	NCFNumber              string        `json:"ncfNumber,omitempty"`
	ClientRNC              string        `json:"clientRnc,omitempty"`
	SubtotalDOP            int           `json:"subtotalDop,omitempty"`
	ITBISDOP               int           `json:"itbisDop,omitempty"`
	SecurityDepositDOP     int           `json:"securityDepositDop,omitempty"`
	GatewaySessionID       string        `json:"gatewaySessionId,omitempty"`
	GatewayPaymentIntentID string        `json:"gatewayPaymentIntentId,omitempty"`
}

// CardSessionResult representa la sesión de checkout generada en la pasarela de tarjetas.
type CardSessionResult struct {
	SessionID   string `json:"sessionId"`
	CheckoutURL string `json:"checkoutUrl"`
	Provider    string `json:"provider"`
}

// IsVerifiedOrConfirmed indica si el pago ya cuenta con validación contable firme.
func (p Payment) IsVerifiedOrConfirmed() bool {
	switch p.Status {
	case PaymentVerified, PaymentConfirmed:
		return true
	default:
		return false
	}
}

// IsPendingVerification indica si el pago aguarda revisión de un comprobante bancario.
func (p Payment) IsPendingVerification() bool {
	return p.Status == PaymentPendingVerification
}

// BelongsToClient valida si el pago corresponde al cliente autenticado.
func (p Payment) BelongsToClient(userID, email, _ string) bool {
	storedClientID := strings.TrimSpace(p.ClientID)
	if storedClientID != "" {
		trimmedUserID := strings.TrimSpace(userID)
		if trimmedUserID == "" {
			return false
		}
		return storedClientID == trimmedUserID
	}
	trimmedEmail := strings.TrimSpace(email)
	if trimmedEmail == "" {
		return false
	}
	return strings.EqualFold(p.ClientEmail, trimmedEmail)
}

// Refund representa una solicitud de reembolso asociada a la cancelación de un evento.
type Refund struct {
	ID              string       `json:"id"`
	EventID         string       `json:"eventId"`
	EventTitle      string       `json:"eventTitle"`
	ClientName      string       `json:"clientName"`
	PaidRD          int          `json:"paidRD"`
	RefundPercent   int          `json:"refundPercent"`
	RefundDop       int          `json:"refundRD"`
	Reason          string       `json:"reason"`
	Status          RefundStatus `json:"status"`
	ApprovedBy      string       `json:"approvedBy,omitempty"`
	ApprovedAtLabel string       `json:"approvedAtLabel,omitempty"`
	GatewayRefundID string       `json:"gatewayRefundId,omitempty"`
	GatewayStatus   string       `json:"gatewayStatus,omitempty"`
}

// FinanceSummary consolida métricas financieras agregadas para el panel operativo.
type FinanceSummary struct {
	MonthLabel          string `json:"monthLabel"`
	OctRevenueRD        int    `json:"octRevenueRD"`
	AccountsReceivable  int    `json:"accountsReceivableRD"`
	ActiveEventsCount   int    `json:"activeEventsCount"`
	NewQuoteRequests    int    `json:"newQuoteRequests"`
	PendingVerification int    `json:"pendingVerificationCount"`
}

// MonthlyAnalyticsPoint representa la serie mensual real de ingresos, gastos y eventos.
type MonthlyAnalyticsPoint struct {
	MonthKey    string `json:"monthKey"`
	Label       string `json:"label"`
	RevenueRD   int    `json:"revenueRD"`
	ExpensesRD  int    `json:"expensesRD"`
	EventsCount int    `json:"eventsCount"`
}

// CategoryCountPoint representa un desglose agregado por categoría o tipo de evento.
type CategoryCountPoint struct {
	Key   string `json:"key"`
	Label string `json:"label"`
	Count int    `json:"count"`
}

// PaymentsAnalytics expone analítica histórica real filtrada por rango de fechas.
type PaymentsAnalytics struct {
	Range                 string                  `json:"range"`
	MonthlySeries         []MonthlyAnalyticsPoint `json:"monthlySeries"`
	TotalRevenueRD        int                     `json:"totalRevenueRD"`
	TotalExpensesRD       int                     `json:"totalExpensesRD"`
	NetMarginPercent      int                     `json:"netMarginPercent"`
	TotalEventsCount      int                     `json:"totalEventsCount"`
	ConfirmedEventsCount  int                     `json:"confirmedEventsCount"`
	PendingEventsCount    int                     `json:"pendingEventsCount"`
	CompletedEventsCount  int                     `json:"completedEventsCount"`
	CancelledEventsCount  int                     `json:"cancelledEventsCount"`
	EventTypesBreakdown   []CategoryCountPoint    `json:"eventTypesBreakdown"`
	UniqueClientsCount    int                     `json:"uniqueClientsCount"`
	RepeatClientsCount    int                     `json:"repeatClientsCount"`
	CollectionRatePercent int                     `json:"collectionRatePercent"`
}

// ValidateRNCForNCF valida el tipo de comprobante DGII (B01/B02) y el RNC o Cédula.
func ValidateRNCForNCF(ncfType, clientRNC string) (normalizedNCF, cleanedRNC string, err error) {
	normalizedNCF = strings.ToUpper(strings.TrimSpace(ncfType))
	if normalizedNCF == "" {
		normalizedNCF = "B02"
	}
	if normalizedNCF != "B01" {
		if normalizedNCF != "B02" {
			return "", "", ErrInvalidFiscalRNC
		}
	}
	cleanedRNC = extractDigitsOnly(clientRNC)
	if normalizedNCF == "B01" {
		digitCount := len(cleanedRNC)
		isRNC := digitCount == 9
		isCedula := digitCount == 11
		if !isRNC {
			if !isCedula {
				return "", "", ErrInvalidFiscalRNC
			}
		}
	}
	return normalizedNCF, cleanedRNC, nil
}

func extractDigitsOnly(raw string) string {
	var builder strings.Builder
	for _, ch := range strings.TrimSpace(raw) {
		if unicode.IsDigit(ch) {
			builder.WriteRune(ch)
		}
	}
	return builder.String()
}

// ComputeDominicanTaxBreakdown calcula subtotal neto e ITBIS del 18% excluyendo depósito en garantía.
func ComputeDominicanTaxBreakdown(amountDOP, securityDepositDOP int) (subtotal, itbis int) {
	return ComputeDominicanTaxBreakdownWithRate(amountDOP, securityDepositDOP, 18)
}

// ComputeDominicanTaxBreakdownWithRate calcula subtotal neto e ITBIS con una tasa configurable excluyendo depósito en garantía.
func ComputeDominicanTaxBreakdownWithRate(amountDOP, securityDepositDOP, itbisPercent int) (subtotal, itbis int) {
	deposit := securityDepositDOP
	if deposit < 0 {
		deposit = 0
	}
	if deposit > amountDOP {
		deposit = amountDOP
	}
	taxable := amountDOP - deposit
	if taxable <= 0 {
		return 0, 0
	}
	rate := itbisPercent
	if rate < 0 {
		rate = 0
	}
	divisor := 1.0 + (float64(rate) / 100.0)
	subtotal = int(math.Round(float64(taxable) / divisor))
	itbis = taxable - subtotal
	return subtotal, itbis
}

// FormatNCFNumber construye el código NCF secuencial DGII (por ejemplo B0100000001).
func FormatNCFNumber(ncfType string, seq int64) string {
	prefix := strings.ToUpper(strings.TrimSpace(ncfType))
	if prefix != "B01" {
		prefix = "B02"
	}
	return fmt.Sprintf("%s%08d", prefix, seq)
}

// ValidateMethod comprueba que el método de pago normalizado sea permitido.
func ValidateMethod(method PaymentMethod) error {
	switch method {
	case MethodTransfer, MethodCard, MethodCash:
		return nil
	default:
		return ErrInvalidPaymentMethod
	}
}

// NormalizePaymentMethod normaliza un método de pago y deduce el banco en transferencias.
func NormalizePaymentMethod(rawMethod, explicitBank string) (PaymentMethod, string, error) {
	cleanedMethod := strings.TrimSpace(rawMethod)
	lower := strings.ToLower(cleanedMethod)
	bank := strings.TrimSpace(explicitBank)

	if direct, matched := matchNonTransferMethod(lower); matched {
		return direct, bank, nil
	}
	if inferredBank, isTransfer := matchTransferMethod(lower); isTransfer {
		if bank == "" {
			bank = inferredBank
		}
		return MethodTransfer, bank, nil
	}
	return "", "", ErrInvalidPaymentMethod
}

func matchNonTransferMethod(lower string) (PaymentMethod, bool) {
	switch lower {
	case "efectivo", "cash":
		return MethodCash, true
	case "tarjeta", "card", "visa", "mastercard", "credito", "crédito", "debito", "débito":
		return MethodCard, true
	default:
		return "", false
	}
}

func matchTransferMethod(lower string) (string, bool) {
	if strings.Contains(lower, "popular") {
		return "Banco Popular", true
	}
	if strings.Contains(lower, "banreservas") {
		return "Banreservas", true
	}
	if strings.Contains(lower, "reservas") {
		return "Banreservas", true
	}
	if strings.Contains(lower, "bhd") {
		return "BHD León", true
	}
	if strings.Contains(lower, "scotiabank") {
		return "Scotiabank", true
	}
	switch lower {
	case "transferencia", "transfer", "wire", "ach", "lbtr":
		return "", true
	default:
		return "", false
	}
}

// NormalizePaymentKind normaliza la categoría contable de un pago.
func NormalizePaymentKind(rawKind string) PaymentKind {
	lower := strings.ToLower(strings.TrimSpace(rawKind))
	switch lower {
	case "saldo", "balance":
		return KindBalance
	case "reposicion", "reposición", "damage":
		return KindDamage
	default:
		return KindDeposit
	}
}

// FormatSpanishShortDate formatea una fecha al formato breve en español usado en recibos.
func FormatSpanishShortDate(ts time.Time) string {
	months := [12]string{
		"ene", "feb", "mar", "abr", "may", "jun",
		"jul", "ago", "sep", "oct", "nov", "dic",
	}
	idx := int(ts.Month()) - 1
	if idx < 0 {
		idx = 0
	}
	if idx >= len(months) {
		idx = len(months) - 1
	}
	return fmt.Sprintf("%d %s", ts.Day(), months[idx])
}

// CanonicalPayments devuelve los pagos iniciales de referencia del sistema.
func CanonicalPayments() []Payment {
	camila := canonicalCamilaPayments()
	others := canonicalExternalPayments()
	return append(camila, others...)
}

func canonicalCamilaPayments() []Payment {
	camilaID := "11111111-1111-1111-1111-111111111111"
	return []Payment{
		{
			ID:            "pay-boda-anticipo",
			ReceiptNumber: "REC-2026-0184",
			EventID:       "evt-boda-real",
			EventTitle:    "Boda Real Rosa & Dorado",
			ClientID:      camilaID,
			ClientEmail:   "camila@rosafiesta.do",
			ClientName:    "Camila Rodríguez",
			AmountRD:      1350,
			Method:        MethodTransfer,
			Kind:          KindDeposit,
			BankName:      "Banreservas",
			PaidDateLabel: "18 oct",
			ReferenceCode: "BR-9948120",
			Status:        PaymentVerified,
		},
		{
			ID:            "pay-quince-anticipo",
			ReceiptNumber: "REC-2026-0179",
			EventID:       "evt-quince-valentina",
			EventTitle:    "Quinceañera Valentina Crystal",
			ClientID:      camilaID,
			ClientEmail:   "camila@rosafiesta.do",
			ClientName:    "Camila Rodríguez",
			AmountRD:      1860,
			Method:        MethodCash,
			Kind:          KindDeposit,
			PaidDateLabel: "15 oct",
			ReferenceCode: "EFE-CAJA-04",
			Status:        PaymentVerified,
		},
	}
}

func canonicalExternalPayments() []Payment {
	return []Payment{
		{
			ID:            "pay-grad-anticipo",
			ReceiptNumber: "REC-2026-0192",
			EventID:       "evt-grad-promo",
			EventTitle:    "Graduación Promo 2026",
			ClientEmail:   "ana.gomez@correo.do",
			ClientName:    "Ana Gómez",
			AmountRD:      642,
			Method:        MethodTransfer,
			Kind:          KindDeposit,
			BankName:      "Banco Popular",
			PaidDateLabel: "19 oct",
			ReferenceCode: "POP-772104",
			Status:        PaymentPendingVerification,
		},
		{
			ID:            "pay-gala-total",
			ReceiptNumber: "REC-2026-0161",
			EventID:       "evt-gala-caribe",
			EventTitle:    "Gala Corporativa Grupo Caribe",
			ClientEmail:   "eventos@grupocaribe.do",
			ClientName:    "Grupo Caribe",
			AmountRD:      8500,
			Method:        MethodCard,
			Kind:          KindBalance,
			BankName:      "Visa Empresarial",
			PaidDateLabel: "12 oct",
			ReferenceCode: "VISA-4821",
			Status:        PaymentVerified,
		},
	}
}

// CanonicalRefunds devuelve las solicitudes de reembolso iniciales del sistema.
func CanonicalRefunds() []Refund {
	return []Refund{
		{
			ID:            "ref-baby-lucia",
			EventID:       "evt-baby-lucia",
			EventTitle:    "Baby Shower Lucía Pastel",
			ClientName:    "Lucía Fernández",
			PaidRD:        900,
			RefundPercent: 90,
			RefundDop:     810,
			Reason:        "Cancelación con 12 días de anticipación (política 90%)",
			Status:        RefundStatusPending,
		},
	}
}

// CanonicalFinanceSummary devuelve el resumen financiero base de demostración.
func CanonicalFinanceSummary() FinanceSummary {
	return FinanceSummary{
		MonthLabel:          "Octubre 2026",
		OctRevenueRD:        12450,
		AccountsReceivable:  7724,
		ActiveEventsCount:   4,
		NewQuoteRequests:    2,
		PendingVerification: 1,
	}
}
