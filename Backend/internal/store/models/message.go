package models

import (
	"time"

	"github.com/google/uuid"
)

// EventMessage represents a chat message within an event context.
type EventMessage struct {
	ID        uuid.UUID `json:"id"`
	EventID   uuid.UUID `json:"event_id"`
	SenderID  uuid.UUID `json:"sender_id"`
	Content   string    `json:"content"`
	CreatedAt time.Time `json:"created_at"`

	// Optional: Include sender name for the UI
	SenderName string `json:"sender_name,omitempty"`
}

// CreateMessagePayload defines the data needed to send a new message.
type CreateMessagePayload struct {
	Content string `json:"content" validate:"required"`
}
