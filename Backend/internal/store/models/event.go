package models

import (
	"time"

	"github.com/google/uuid"
)

type Event struct {
	ID         uuid.UUID `json:"id"`
	UserID     uuid.UUID `json:"user_id"`
	Name       string    `json:"name"`
	Date       time.Time `json:"date"`
	Location   string    `json:"location"`
	GuestCount int       `json:"guest_count"`
	Budget     float64   `json:"budget"`
	Status     string    `json:"status"` // planning, confirmed, completed
	CreatedAt  string    `json:"created_at"`
	UpdatedAt  string    `json:"updated_at"`
}

type CreateEventPayload struct {
	Name       string  `json:"name" validate:"required,max=255"`
	Date       string  `json:"date" validate:"required"` // ISO8601 string
	Location   string  `json:"location" validate:"max=255"`
	GuestCount int     `json:"guest_count" validate:"min=0"`
	Budget     float64 `json:"budget" validate:"min=0"`
}

type UpdateEventPayload struct {
	Name       *string  `json:"name" validate:"omitempty,max=255"`
	Date       *string  `json:"date" validate:"omitempty"`
	Location   *string  `json:"location" validate:"omitempty,max=255"`
	GuestCount *int     `json:"guest_count" validate:"omitempty,min=0"`
	Budget     *float64 `json:"budget" validate:"omitempty,min=0"`
	Status     *string  `json:"status" validate:"omitempty,oneof=planning confirmed completed"`
}
