package models

import (
	"time"

	"github.com/google/uuid"
)

type EventInspiration struct {
	ID         uuid.UUID  `json:"id"`
	EventID    uuid.UUID  `json:"event_id"`
	PhotoURL   string     `json:"photo_url"`
	Caption    *string    `json:"caption,omitempty"`
	UploadedBy uuid.UUID  `json:"uploaded_by"`
	UploadedAt time.Time  `json:"uploaded_at"`
}