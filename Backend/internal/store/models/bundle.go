package models

import "github.com/google/uuid"

type Bundle struct {
	ID              uuid.UUID   `json:"id"`
	Name            string      `json:"name"`
	Description     string      `json:"description,omitempty"`
	DiscountPercent float64     `json:"discount_percent"`
	ImageURL        string      `json:"image_url,omitempty"`
	IsActive        bool        `json:"is_active"`
	MinPrice        float64     `json:"min_price"`
	Items           []BundleItem `json:"items,omitempty"`
	CreatedAt       string      `json:"created_at,omitempty"`
}

type BundleItem struct {
	ID         uuid.UUID      `json:"id"`
	BundleID   uuid.UUID      `json:"bundle_id"`
	ArticleID  uuid.UUID      `json:"article_id"`
	Quantity   int            `json:"quantity"`
	IsOptional bool           `json:"is_optional"`
	Article    *Article       `json:"article,omitempty"`
}
