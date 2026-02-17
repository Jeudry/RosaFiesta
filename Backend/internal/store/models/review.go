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
