package models

type Category struct {
	BaseModel
	Name        string  `json:"name"`
	Description *string `json:"description,omitempty"`
	ImageURL    *string `json:"image_url,omitempty"`
	ParentID    *string `json:"parent_id,omitempty"`
}
