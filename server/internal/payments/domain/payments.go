// Package domain define las entidades y reglas de pagos, comprobantes y finanzas de RosaFiesta.
package domain

import "errors"

// Errores del dominio de pagos.
var (
	ErrPaymentNotFound      = errors.New("pago no encontrado")
	ErrInvalidPaymentAmount = errors.New("el monto del pago debe ser mayor a cero")
	ErrInvalidPaymentMethod = errors.New("método de pago inválido; use transferencia, tarjeta o efectivo")
)

// PaymentMethod representa un canal de pago permitido en RosaFiesta.
type PaymentMethod string

// Métodos de pago soportados.
const (
	MethodTransfer PaymentMethod = "transferencia"
	MethodCard     PaymentMethod = "tarjeta"
	MethodCash     PaymentMethod = "efectivo"
)

// PaymentKind indica si el pago corresponde a anticipo, saldo o cargo adicional.
type PaymentKind string

// Tipos de pago.
const (
	KindDeposit PaymentKind = "anticipo"
	KindBalance PaymentKind = "saldo"
	KindDamage  PaymentKind = "reposicion"
)

// PaymentStatus representa el estado de verificación del pago.
type PaymentStatus string

// Estados de pago.
const (
	PaymentPendingVerification PaymentStatus = "pending_verification"
	PaymentVerified            PaymentStatus = "verified"
	PaymentFailed              PaymentStatus = "failed"
)

// Payment representa un registro de pago o abono asociado a un evento.
type Payment struct {
	ID            string        `json:"id"`
	ReceiptNumber string        `json:"receiptNumber"`
	EventID       string        `json:"eventId"`
	EventTitle    string        `json:"eventTitle"`
	ClientName    string        `json:"clientName"`
	AmountRD      int           `json:"amountRD"`
	Method        PaymentMethod `json:"method"`
	Kind          PaymentKind   `json:"kind"`
	PaidDateLabel string        `json:"paidDateLabel"`
	ReferenceCode string        `json:"referenceCode"`
	Status        PaymentStatus `json:"status"`
}

// FinanceSummary resume las métricas financieras del mes (s69, A20).
type FinanceSummary struct {
	MonthLabel          string `json:"monthLabel"`
	OctRevenueRD        int    `json:"octRevenueRD"`
	AccountsReceivable  int    `json:"accountsReceivableRD"`
	ActiveEventsCount   int    `json:"activeEventsCount"`
	NewQuoteRequests    int    `json:"newQuoteRequests"`
	PendingVerification int    `json:"pendingVerificationCount"`
}

// ValidateMethod comprueba que el método de pago sea uno de los 3 permitidos.
func ValidateMethod(m PaymentMethod) error {
	switch m {
	case MethodTransfer, MethodCard, MethodCash:
		return nil
	default:
		return ErrInvalidPaymentMethod
	}
}

// CanonicalPayments devuelve los pagos canónicos del sistema de diseño (Sección 6).
func CanonicalPayments() []Payment {
	return []Payment{
		{
			ID:            "pay-boda-anticipo",
			ReceiptNumber: "REC-2026-0184",
			EventID:       "evt-boda-real",
			EventTitle:    "Boda Real Rosa & Dorado",
			ClientName:    "Camila Rodríguez",
			AmountRD:      1350,
			Method:        MethodTransfer,
			Kind:          KindDeposit,
			PaidDateLabel: "18 oct",
			ReferenceCode: "BR-9948120",
			Status:        PaymentVerified,
		},
		{
			ID:            "pay-quince-anticipo",
			ReceiptNumber: "REC-2026-0179",
			EventID:       "evt-quince-valentina",
			EventTitle:    "Quinceañera Valentina Crystal",
			ClientName:    "Camila Rodríguez",
			AmountRD:      1860,
			Method:        MethodCash,
			Kind:          KindDeposit,
			PaidDateLabel: "15 oct",
			ReferenceCode: "EFE-CAJA-04",
			Status:        PaymentVerified,
		},
		{
			ID:            "pay-grad-anticipo",
			ReceiptNumber: "REC-2026-0192",
			EventID:       "evt-grad-promo",
			EventTitle:    "Graduación Promo 2026",
			ClientName:    "Ana Gómez",
			AmountRD:      642,
			Method:        MethodTransfer,
			Kind:          KindDeposit,
			PaidDateLabel: "19 oct",
			ReferenceCode: "POP-772104",
			Status:        PaymentPendingVerification,
		},
		{
			ID:            "pay-gala-total",
			ReceiptNumber: "REC-2026-0161",
			EventID:       "evt-gala-caribe",
			EventTitle:    "Gala Corporativa Grupo Caribe",
			ClientName:    "Grupo Caribe",
			AmountRD:      8500,
			Method:        MethodCard,
			Kind:          KindBalance,
			PaidDateLabel: "12 oct",
			ReferenceCode: "VISA-4821",
			Status:        PaymentVerified,
		},
	}
}

// CanonicalFinanceSummary devuelve los indicadores financieros canónicos (Sección 6: RD$ 12,450 ingresos, RD$ 7,724 por cobrar).
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
