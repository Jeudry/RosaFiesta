package categories

type CreateCategoryPayload struct {
	Name        string  `json:"name" validate:"required,max=100"`
	Description *string `json:"description,omitempty" validate:"omitempty,max=500"`
	ImageURL    *string `json:"image_url,omitempty" validate:"omitempty,url"`
	ParentID    *string `json:"parent_id,omitempty" validate:"omitempty,uuid"`
}

type UpdateCategoryPayload struct {
	Name        string  `json:"name" validate:"required,max=100"`
	Description *string `json:"description,omitempty" validate:"omitempty,max=500"`
	ImageURL    *string `json:"image_url,omitempty" validate:"omitempty,url"`
	ParentID    *string `json:"parent_id,omitempty" validate:"omitempty,uuid"`
}
