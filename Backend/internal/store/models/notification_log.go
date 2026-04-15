package models

import (
	"time"

	"github.com/google/uuid"
)

type NotificationType string

const (
	PreEventReminder      NotificationType = "pre-event-reminder"
	PostEventReview       NotificationType = "post-event-review"
	EmailEventReminder7d NotificationType = "email-event-reminder-7d"
	EmailEventReminder24h NotificationType = "email-event-reminder-24h"
	EmailEventThankYou    NotificationType = "email-event-thank-you"
	EmailReminder7d      NotificationType = "email_reminder_7d"
	EmailReminder24h     NotificationType = "email_reminder_24h"
	EmailThankYou         NotificationType = "email_thank_you"
	QuoteAdjusted         NotificationType = "quote_adjusted"
	QuoteApproved        NotificationType = "quote_approved"
	QuoteRejected        NotificationType = "quote_rejected"
	AutoReminder7d       NotificationType = "auto_reminder_7d"
)

type NotificationLog struct {
	ID      uuid.UUID        `json:"id"`
	EventID uuid.UUID        `json:"event_id"`
	Type    NotificationType `json:"type"`
	SentAt  time.Time        `json:"sent_at"`
}
