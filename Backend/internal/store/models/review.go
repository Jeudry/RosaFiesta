package models

import "github.com/google/uuid"

type Review struct {
	BaseModel
	UserID    uuid.UUID `json:"user_id"`
	ArticleID uuid.UUID `json:"article_id"`
	Rating    int       `json:"rating"`
	Comment   string    `json:"comment"`

	// Relationships
	User *User `json:"user,omitempty"`
}

// CompanyReview is a review for the RosaFiesta company as a whole.
// It is not tied to any specific article.
type CompanyReview struct {
	BaseModel
	UserID   uuid.UUID `json:"user_id"`
	Rating   int       `json:"rating"`
	Comment  string    `json:"comment"`
	Source   string    `json:"source"` // "google", "facebook", "direct", etc.

	// Relationships
	User *User `json:"user,omitempty"`
}
