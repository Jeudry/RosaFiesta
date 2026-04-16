package models

import (
	"time"

	"github.com/google/uuid"
)

type EventType struct {
	ID                 uuid.UUID  `json:"id"`
	Name               string     `json:"name"`
	Description        string     `json:"description,omitempty"`
	SuggestedBudgetMin *float64   `json:"suggested_budget_min,omitempty"`
	SuggestedBudgetMax *float64   `json:"suggested_budget_max,omitempty"`
	DefaultGuestCount  int        `json:"default_guest_count"`
	Color              string     `json:"color"`
	Icon               string     `json:"icon"`
	IsActive           bool       `json:"is_active"`
	CreatedAt          time.Time  `json:"created_at"`
	UpdatedAt          time.Time  `json:"updated_at"`
	Items              []EventTypeItem `json:"items,omitempty"`
}

type EventTypeItem struct {
	ID          uuid.UUID  `json:"id"`
	EventTypeID uuid.UUID  `json:"event_type_id"`
	ArticleID   uuid.UUID  `json:"article_id"`
	CategoryID  *uuid.UUID `json:"category_id,omitempty"`
	Quantity    int        `json:"quantity"`
	SortOrder   int        `json:"sort_order"`
	Article     *Article   `json:"article,omitempty"`
}
