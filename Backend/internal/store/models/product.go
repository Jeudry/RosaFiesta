package models

type Product struct {
	BaseModel
	Name        string   `json:"name"`
	Description *string  `json:"description,omitempty"`
	Price       float64  `json:"price"`
	RentalPrice *float64 `json:"rental_price,omitempty"`
	Color       uint32   `json:"color"`
	Size        float64  `json:"size"`
	ImageURL    *string  `json:"image_url,omitempty"`
	Stock       int      `json:"stock"`
}
