package products

type CreateProductPayload struct {
	Name        string   `json:"name" validate:"required,min=3,max=256"`
	Description *string  `json:"description,omitempty" validate:"omitempty,min=5,max=20000"`
	Price       float64  `json:"price" validate:"gt=5,lt=10000000"`
	RentalPrice *float64 `json:"rental_price,omitempty" validate:"omitempty,gt=5,lt=10000000"`
	Color       uint64   `json:"color" validate:"gte=0,lte=4294967295"`
	Size        float64  `json:"size" validate:"lte=99999"`
	ImageURL    *string  `json:"image_url,omitempty" validate:"omitempty,max=3000"`
	Stock       int      `json:"stock" validate:"max=100000000"`
}
