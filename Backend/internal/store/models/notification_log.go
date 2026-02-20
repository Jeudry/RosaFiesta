package models

import (
	"time"

	"github.com/google/uuid"
)

type NotificationType string

const (
	PreEventReminder NotificationType = "pre-event-reminder"
	PostEventReview  NotificationType = "post-event-review"
)

type NotificationLog struct {
	ID      uuid.UUID        `json:"id"`
	EventID uuid.UUID        `json:"event_id"`
	Type    NotificationType `json:"type"`
	SentAt  time.Time        `json:"sent_at"`
}
