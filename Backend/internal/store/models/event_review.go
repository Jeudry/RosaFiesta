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
	User   *User         `json:"user,omitempty"`
	Photos []ReviewPhoto `json:"photos,omitempty"`
}

type ReviewPhoto struct {
	ID        uuid.UUID `json:"id"`
	ReviewID  uuid.UUID `json:"review_id"`
	PhotoURL  string    `json:"photo_url"`
	Caption   string    `json:"caption,omitempty"`
	SortOrder int       `json:"sort_order"`
}
