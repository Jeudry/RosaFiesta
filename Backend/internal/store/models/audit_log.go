package models

import (
	"time"

	"github.com/google/uuid"
)

// AuditAction represents the type of action that was performed.
type AuditAction string

const (
	AuditActionEventCreate     AuditAction = "event_created"
	AuditActionEventUpdate     AuditAction = "event_updated"
	AuditActionEventDelete     AuditAction = "event_deleted"
	AuditActionEventPay        AuditAction = "event_paid"
	AuditActionEventAdjust     AuditAction = "quote_adjusted"
	AuditActionEventStatus     AuditAction = "status_changed"
	AuditActionEventPhotoAdd   AuditAction = "photo_added"
	AuditActionEventReject     AuditAction = "quote_rejected"
)

// AuditLog represents an entry in the audit trail.
type AuditLog struct {
	ID         uuid.UUID   `json:"id"`
	UserID     *uuid.UUID  `json:"user_id,omitempty"`
	EventID    *uuid.UUID  `json:"event_id,omitempty"`
	Action     AuditAction `json:"action"`
	EntityType string      `json:"entity_type"`
	EntityID   *uuid.UUID  `json:"entity_id,omitempty"`
	OldValue   *string     `json:"old_value,omitempty"`
	NewValue   *string     `json:"new_value,omitempty"`
	CreatedAt  time.Time   `json:"created_at"`
}

// AuditLogWithUser represents an audit log entry with user name for display.
type AuditLogWithUser struct {
	AuditLog
	UserName string `json:"user_name,omitempty"`
}
