package models

import (
	"time"

	"github.com/google/uuid"
)

type EventItem struct {
	ID        uuid.UUID `json:"id"`
	EventID   uuid.UUID `json:"event_id"`
	ArticleID uuid.UUID `json:"article_id"`
	Quantity  int       `json:"quantity"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`

	// Optional: Include Article details if joined
	Article *Article `json:"article,omitempty"`
	Price   *float64 `json:"price,omitempty"`
}
