package models

import (
	"time"

	"github.com/google/uuid"
)

// Event statuses. `EventStatusDraft` is the new default — a user always has
// at most one draft event acting as their "active event" while they browse
// the catalog.
const (
	EventStatusDraft     = "draft"
	EventStatusPlanning  = "planning" // legacy, kept for backwards compat
	EventStatusRequested = "requested"
	EventStatusAdjusted  = "adjusted"
	EventStatusConfirmed = "confirmed"
	EventStatusPaid      = "paid"
	EventStatusCompleted = "completed"
	EventStatusCancelled = "cancelled"
	EventStatusRejected  = "rejected"
)

type Event struct {
	ID                 uuid.UUID  `json:"id"`
	UserID             uuid.UUID  `json:"user_id"`
	Name               string     `json:"name"`
	Date               *time.Time `json:"date,omitempty"`
	Location           string     `json:"location"`
	GuestCount         int        `json:"guest_count"`
	Budget             float64    `json:"budget"`
	AdditionalCosts    float64    `json:"additional_costs"`
	AdminNotes         string     `json:"admin_notes"`
	Status             string     `json:"status"`
	PaymentStatus      string     `json:"payment_status"`
	PaymentMethod      *string    `json:"payment_method"`
	PaidAt             *time.Time `json:"paid_at"`
	QuoteApprovedAt    *time.Time `json:"quote_approved_at,omitempty"`
	QuoteApprovedBy    *uuid.UUID `json:"quote_approved_by,omitempty"`
	QuoteRejectedAt    *time.Time `json:"quote_rejected_at,omitempty"`
	QuoteRejectedBy    *uuid.UUID `json:"quote_rejected_by,omitempty"`
	DepositPaid        bool       `json:"depositPaid"`
	DepositAmount      int        `json:"depositAmount"`
	DepositPaidAt      *time.Time `json:"depositPaidAt,omitempty"`
	RemainingAmount    int        `json:"remainingAmount"`
	InstallmentDueDate *time.Time `json:"installmentDueDate,omitempty"`
	TotalQuote         int        `json:"totalQuote"`
	CreatedAt          string     `json:"created_at"`
	UpdatedAt          string     `json:"updated_at"`
}

// CreateEventPayload still requires name and date because the explicit
// "create event" flow is for users who already know what they want.
// The implicit draft creation goes through GetOrCreateDraft instead.
type CreateEventPayload struct {
	Name       string  `json:"name" validate:"required,max=255"`
	Date       string  `json:"date" validate:"required"` // ISO8601 string
	Location   string  `json:"location" validate:"max=255"`
	GuestCount int     `json:"guest_count" validate:"min=0"`
	Budget     float64 `json:"budget" validate:"min=0"`
}

type UpdateEventPayload struct {
	Name            *string  `json:"name" validate:"omitempty,max=255"`
	Date            *string  `json:"date" validate:"omitempty"`
	Location        *string  `json:"location" validate:"omitempty,max=255"`
	GuestCount      *int     `json:"guest_count" validate:"omitempty,min=0"`
	Budget          *float64 `json:"budget" validate:"omitempty,min=0"`
	AdditionalCosts *float64 `json:"additional_costs" validate:"omitempty,min=0"`
	AdminNotes      *string  `json:"admin_notes" validate:"omitempty"`
	Status          *string  `json:"status" validate:"omitempty,oneof=draft planning requested adjusted confirmed paid completed cancelled rejected"`
	PaymentStatus   *string  `json:"payment_status" validate:"omitempty"`
	PaymentMethod   *string  `json:"payment_method" validate:"omitempty"`
}
