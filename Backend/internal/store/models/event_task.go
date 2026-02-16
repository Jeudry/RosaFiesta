package models

import (
	"time"

	"github.com/google/uuid"
)

type EventTask struct {
	ID          uuid.UUID  `json:"id"`
	EventID     uuid.UUID  `json:"event_id"`
	Title       string     `json:"title"`
	Description *string    `json:"description,omitempty"`
	IsCompleted bool       `json:"is_completed"`
	DueDate     *time.Time `json:"due_date,omitempty"`
	CreatedAt   time.Time  `json:"created_at"`
	UpdatedAt   time.Time  `json:"updated_at"`
}
