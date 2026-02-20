package models

import "github.com/google/uuid"

// / <summary>
// / Represents a review left by a user for an event
// / </summary>
type EventReview struct {
	BaseModel
	UserID  uuid.UUID `json:"user_id"`
	EventID uuid.UUID `json:"event_id"`
	Rating  int       `json:"rating"`
	Comment string    `json:"comment"`

	// Relationships
	User *User `json:"user,omitempty"`
}
