package view_models

type CreateProductPayload struct {
	Name        string   `json:"name" validate:"required,max=128"`
	Description *string  `json:"description,omitempty" validate:"omitempty,max=3000"`
	Price       float64  `json:"price" validate:"gt=0,lt=10000000"`
	RentalPrice *float64 `json:"rental_price,omitempty" validate:"omitempty,gt=0,lt=10000000"`
	Color       uint32   `json:"color" validate:"gte=0,lte=4294967295"`
	Size        float64  `json:"size"`
	ImageURL    *string  `json:"image_url,omitempty" validate:"omitempty,max=3000"`
	Stock       int      `json:"stock" validate:"max=100000000"`
}
