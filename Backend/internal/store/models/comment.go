package models

import "github.com/google/uuid"

type Comment struct {
	ID        uuid.UUID `json:"id"`
	Content   string    `json:"content"`
	PostID    uuid.UUID `json:"post_id"`
	UserID    uuid.UUID `json:"user_id"`
	User      User      `json:"user"`
	CreatedAt string    `json:"created_at"`
	UpdatedAt string    `json:"updated_at"`
}
