package models

import "github.com/google/uuid"

type ArticleType string

const (
	ArticleTypeRental ArticleType = "Rental"
	ArticleTypeSale   ArticleType = "Sale"
)

type Article struct {
	BaseModel
	NameTemplate        string      `json:"name_template"`
	DescriptionTemplate *string     `json:"description_template,omitempty"`
	IsActive            bool        `json:"is_active"`
	Type                ArticleType `json:"type"`
	StockQuantity       int         `json:"stock_quantity"`
	CategoryID          *uuid.UUID  `json:"category_id,omitempty"`
	Category            *Category   `json:"category,omitempty"` // For eager loading

	Variants []ArticleVariant `json:"variants,omitempty"`

	AverageRating float64 `json:"average_rating"`
	ReviewCount   int     `json:"review_count"`
}

type ArticleVariant struct {
	ID              uuid.UUID `json:"id"`
	ArticleID       uuid.UUID `json:"article_id"`
	Sku             string    `json:"sku"`
	Name            string    `json:"name"`
	Description     *string   `json:"description,omitempty"`
	ImageURL        *string   `json:"image_url,omitempty"`
	IsActive        bool      `json:"is_active"`
	Stock           int       `json:"stock"`
	RentalPrice     float64   `json:"rental_price"`
	SalePrice       *float64  `json:"sale_price,omitempty"`
	ReplacementCost *float64  `json:"replacement_cost,omitempty"`

	// Relations
	Attributes map[string]string  `json:"attributes,omitempty"`
	Dimensions []ArticleDimension `json:"dimensions,omitempty"`
}

type ArticleDimension struct {
	ID        uuid.UUID `json:"id"`
	VariantID uuid.UUID `json:"variant_id"`
	Height    *float64  `json:"height,omitempty"`
	Width     *float64  `json:"width,omitempty"`
	Depth     *float64  `json:"depth,omitempty"`
	Weight    *float64  `json:"weight,omitempty"`
}
