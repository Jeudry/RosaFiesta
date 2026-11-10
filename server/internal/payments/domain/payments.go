package domain

import (
	"errors"
	"fmt"
	"strings"
	"time"
)

var (
	ErrPaymentNotFound      = errors.New("payment not found")
	ErrPaymentForbidden     = errors.New("caller is not allowed to access this payment")
	ErrRefundNotFound       = errors.New("refund not found")
	ErrInvalidPaymentAmount = errors.New("payment amount must be greater than zero")
	ErrInvalidPaymentMethod = errors.New("invalid payment method; use transferencia, tarjeta, or efectivo")
)

type PaymentMethod string

const (
	MethodTransfer PaymentMethod = "transferencia"
	MethodCard     PaymentMethod = "tarjeta"
	MethodCash     PaymentMethod = "efectivo"
)

type PaymentKind string

const (
	KindDeposit PaymentKind = "anticipo"
	KindBalance PaymentKind = "saldo"
	KindDamage  PaymentKind = "reposicion"
)

type PaymentStatus string

const (
	PaymentPendingVerification PaymentStatus = "pending_verification"
	PaymentVerified            PaymentStatus = "verified"
	PaymentFailed              PaymentStatus = "failed"
)

type RefundStatus string

const (
	RefundStatusPending  RefundStatus = "pending"
	RefundStatusApproved RefundStatus = "approved"
)

type Payment struct {
	ID            string        `json:"id"`
	ReceiptNumber string        `json:"receiptNumber"`
	EventID       string        `json:"eventId"`
	EventTitle    string        `json:"eventTitle"`
	ClientID      string        `json:"clientId,omitempty"`
	ClientEmail   string        `json:"clientEmail,omitempty"`
	ClientName    string        `json:"clientName"`
	AmountRD      int           `json:"amountRD"`
	Method        PaymentMethod `json:"method"`
	Kind          PaymentKind   `json:"kind"`
	BankName      string        `json:"bankName,omitempty"`
	PaidDateLabel string        `json:"paidDateLabel"`
	ReferenceCode string        `json:"referenceCode"`
	Status        PaymentStatus `json:"status"`
}

func (p Payment) BelongsToClient(userID, email, fullName string) bool {
	trimmedUserID := strings.TrimSpace(userID)
	if trimmedUserID != "" {
		if p.ClientID == trimmedUserID {
			return true
		}
	}
	trimmedEmail := strings.TrimSpace(email)
	if trimmedEmail != "" {
		if strings.EqualFold(p.ClientEmail, trimmedEmail) {
			return true
		}
	}
	trimmedName := strings.TrimSpace(fullName)
	if trimmedName != "" {
		if strings.EqualFold(p.ClientName, trimmedName) {
			return true
		}
	}
	return false
}

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
}

type FinanceSummary struct {
	MonthLabel          string `json:"monthLabel"`
	OctRevenueRD        int    `json:"octRevenueRD"`
	AccountsReceivable  int    `json:"accountsReceivableRD"`
	ActiveEventsCount   int    `json:"activeEventsCount"`
	NewQuoteRequests    int    `json:"newQuoteRequests"`
	PendingVerification int    `json:"pendingVerificationCount"`
}

func ValidateMethod(method PaymentMethod) error {
	switch method {
	case MethodTransfer, MethodCard, MethodCash:
		return nil
	default:
		return ErrInvalidPaymentMethod
	}
}

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
