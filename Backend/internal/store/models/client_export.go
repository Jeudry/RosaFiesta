package models

import (
	"time"

	"github.com/google/uuid"
)

type ClientExport struct {
	ID           uuid.UUID `json:"id"`
	FirstName    string    `json:"first_name"`
	LastName     string    `json:"last_name"`
	Email        string    `json:"email"`
	Phone        string    `json:"phone"`
	CreatedAt    time.Time `json:"created_at"`
	IsActive     bool      `json:"is_active"`
	EventsCount  int       `json:"events_count"`
	TotalSpent   float64   `json:"total_spent"`
}
