package models

import (
	"time"

	"github.com/google/uuid"
)

type Cart struct {
	ID        uuid.UUID  `json:"id"`
	UserID    uuid.UUID  `json:"user_id"`
	Items     []CartItem `json:"items"`
	CreatedAt time.Time  `json:"created_at"`
	UpdatedAt time.Time  `json:"updated_at"`
}

type CartItem struct {
	ID        uuid.UUID       `json:"id"`
	CartID    uuid.UUID       `json:"cart_id"`
	ArticleID uuid.UUID       `json:"article_id"`
	Article   Article         `json:"article"` // Eager loaded
	VariantID *uuid.UUID      `json:"variant_id,omitempty"`
	Variant   *ArticleVariant `json:"variant,omitempty"` // Eager loaded, if applicable
	Quantity  int             `json:"quantity"`
	CreatedAt time.Time       `json:"created_at"`
	UpdatedAt time.Time       `json:"updated_at"`
}
