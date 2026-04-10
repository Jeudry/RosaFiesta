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
