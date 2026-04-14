package models

import (
	"time"

	"github.com/google/uuid"
)

type EventPhoto struct {
	ID        uuid.UUID  `json:"id"`
	EventID   uuid.UUID  `json:"event_id"`
	URL       string     `json:"url"`
	Caption   *string    `json:"caption,omitempty"`
	UploadedAt time.Time `json:"uploaded_at"`
}
