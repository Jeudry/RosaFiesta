package models

import "time"

type BaseModel struct {
	ID        *string    `json:"id,omitempty"`
	Created   *time.Time `json:"created"`
	CreatedBy *string    `json:"created_by,omitempty"`
	Updated   *time.Time `json:"updated,omitempty"`
	UpdatedBy *string    `json:"updated_by,omitempty"`
	Deleted   *time.Time `json:"deleted,omitempty"`
	DeletedBy *string    `json:"deleted_by,omitempty"`
}
