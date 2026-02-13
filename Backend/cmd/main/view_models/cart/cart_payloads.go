package cart

import "github.com/google/uuid"

type AddItemPayload struct {
	ArticleID uuid.UUID  `json:"article_id" validate:"required"`
	VariantID *uuid.UUID `json:"variant_id"`
	Quantity  int        `json:"quantity" validate:"required,min=1"`
}

type UpdateItemPayload struct {
	Quantity int `json:"quantity" validate:"required,min=1"`
}
