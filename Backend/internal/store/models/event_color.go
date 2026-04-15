package models

import (
	"github.com/google/uuid"
)

type EventColor struct {
	ID        uuid.UUID `json:"id"`
	EventID   uuid.UUID `json:"event_id"`
	ColorHex  string    `json:"color_hex"`
	SortOrder int       `json:"sort_order"`
}