package models

import (
	"time"

	"github.com/google/uuid"
)

type InstallmentPayment struct {
	ID            uuid.UUID  `json:"id"`
	EventID       uuid.UUID  `json:"event_id"`
	Amount        int        `json:"amount"`
	PaymentMethod *string    `json:"payment_method"`
	PaymentStatus string     `json:"payment_status"`
	DueDate       *time.Time `json:"due_date,omitempty"`
	PaidAt        *time.Time `json:"paid_at,omitempty"`
	CreatedAt     time.Time  `json:"created_at"`
}

type PaymentSchedule struct {
	DepositPaid        bool       `json:"depositPaid"`
	DepositAmount      int        `json:"depositAmount"`
	DepositPaidAt      *time.Time `json:"depositPaidAt,omitempty"`
	RemainingAmount    int        `json:"remainingAmount"`
	InstallmentDueDate *time.Time `json:"installmentDueDate,omitempty"`
	TotalQuote         int        `json:"totalQuote"`
	PendingPayments    []InstallmentPayment `json:"pendingPayments,omitempty"`
}
