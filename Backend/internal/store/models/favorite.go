package models

import (
	"time"

	"github.com/google/uuid"
)

type Favorite struct {
	ID        uuid.UUID `json:"id"`
	UserID    uuid.UUID `json:"user_id"`
	ArticleID uuid.UUID `json:"article_id"`
	Created   time.Time `json:"created"`
}
