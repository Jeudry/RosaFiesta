package products

import "Backend/internal/store/models"

type CreateProductPayload struct {
	NameTemplate        string                        `json:"name_template" validate:"required,max=100"`
	DescriptionTemplate *string                       `json:"description_template" validate:"max=255"`
	Type                models.ArticleType            `json:"type" validate:"required,oneof=Rental Sale"`
	CategoryID          *string                       `json:"category_id"` // string to parsing uuid later
	IsActive            bool                          `json:"is_active"`
	Variants            []CreateArticleVariantPayload `json:"variants" validate:"required,min=1"`
}

type CreateArticleVariantPayload struct {
	Sku             string                          `json:"sku" validate:"required,max=255"`
	Name            string                          `json:"name" validate:"required,max=255"`
	Description     *string                         `json:"description"`
	ImageURL        *string                         `json:"image_url"`
	IsActive        bool                            `json:"is_active"`
	Stock           int                             `json:"stock" validate:"min=0"`
	RentalPrice     float64                         `json:"rental_price" validate:"required,min=0"`
	SalePrice       *float64                        `json:"sale_price" validate:"min=0"`
	ReplacementCost *float64                        `json:"replacement_cost" validate:"min=0"`
	Attributes      map[string]string               `json:"attributes"`
	Dimensions      []CreateArticleDimensionPayload `json:"dimensions"`
}

type CreateArticleDimensionPayload struct {
	Height *float64 `json:"height"`
	Width  *float64 `json:"width"`
	Depth  *float64 `json:"depth"`
	Weight *float64 `json:"weight"`
}
