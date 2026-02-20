package models

import (
	"time"

	"github.com/google/uuid"
)

type TimelineItem struct {
	ID          uuid.UUID  `json:"id"`
	EventID     uuid.UUID  `json:"event_id"`
	Title       string     `json:"title"`
	Description string     `json:"description"`
	StartTime   time.Time  `json:"start_time"`
	EndTime     time.Time  `json:"end_time"`
	IsCompleted bool       `json:"is_completed"`
	IsCritical  bool       `json:"is_critical"`
	CompletedAt *time.Time `json:"completed_at,omitempty"`
	CreatedAt   time.Time  `json:"created_at"`
	UpdatedAt   time.Time  `json:"updated_at"`
}

type TimelineItemWithUser struct {
	TimelineItem
	UserFCMToken string `json:"user_fcm_token"`
}
