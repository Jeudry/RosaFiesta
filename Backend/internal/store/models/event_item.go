package models

import (
	"time"

	"github.com/google/uuid"
)

type EventItem struct {
	ID            uuid.UUID  `json:"id"`
	EventID       uuid.UUID  `json:"event_id"`
	ArticleID     uuid.UUID  `json:"article_id"`
	VariantID     *uuid.UUID `json:"variant_id,omitempty"`
	Quantity      int        `json:"quantity"`
	PriceSnapshot *float64   `json:"price_snapshot,omitempty"`
	CreatedAt     time.Time  `json:"created_at"`
	UpdatedAt     time.Time  `json:"updated_at"`

	// Optional: Include Article + Variant details if joined
	Article *Article        `json:"article,omitempty"`
	Variant *ArticleVariant `json:"variant,omitempty"`
	Price   *float64        `json:"price,omitempty"`
}

// UnitPrice returns the effective unit price, falling back through PriceSnapshot → Variant.RentalPrice → Price.
func (e *EventItem) UnitPrice() float64 {
	if e.PriceSnapshot != nil {
		return *e.PriceSnapshot
	}
	if e.Variant != nil {
		return e.Variant.RentalPrice
	}
	if e.Price != nil {
		return *e.Price
	}
	return 0
}

// LineTotal returns the total price for this line item.
func (e *EventItem) LineTotal() float64 {
	return e.UnitPrice() * float64(e.Quantity)
}
