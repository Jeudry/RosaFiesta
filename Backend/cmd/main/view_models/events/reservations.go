package events

import (
	"github.com/google/uuid"
)

type ReservationSummary struct {
	ID            uuid.UUID `json:"id"`
	Name          string    `json:"name"`
	Date          *string   `json:"date,omitempty"`
	Status        string    `json:"status"`
	PaymentStatus string    `json:"payment_status"`
	TotalQuote    int       `json:"total_quote"`
	DepositPaid   int       `json:"deposit_paid"`
	Remaining     int       `json:"remaining"`
	ContractReady bool      `json:"contract_ready"`
	ReceiptReady  bool      `json:"receipt_ready"`
	HasPhotos     bool      `json:"has_photos"`
	GuestCount    int       `json:"guest_count"`
	ReviewGiven   bool      `json:"review_given"`
	Location      string    `json:"location"`
}