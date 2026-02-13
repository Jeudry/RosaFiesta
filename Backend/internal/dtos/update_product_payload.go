package dtos

import "Backend/internal/store/models"

type UpdateProductPayload struct {
	NameTemplate        string             `json:"name_template" validate:"required,max=100"`
	DescriptionTemplate *string            `json:"description_template" validate:"max=255"`
	Type                models.ArticleType `json:"type" validate:"required,oneof=Rental Sale"`
	CategoryID          *string            `json:"category_id"`
	IsActive            bool               `json:"is_active"`
}
