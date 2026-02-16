package models

import (
	"time"

	"github.com/google/uuid"
)

type Guest struct {
	ID                  uuid.UUID `json:"id"`
	EventID             uuid.UUID `json:"event_id"`
	Name                string    `json:"name"`
	Email               *string   `json:"email,omitempty"`
	Phone               *string   `json:"phone,omitempty"`
	RSVPStatus          string    `json:"rsvp_status"` // pending, confirmed, declined
	PlusOne             bool      `json:"plus_one"`
	DietaryRestrictions *string   `json:"dietary_restrictions,omitempty"`
	CreatedAt           time.Time `json:"created_at"`
	UpdatedAt           time.Time `json:"updated_at"`
}
