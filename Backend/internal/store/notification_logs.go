package store

import (
	"context"
	"database/sql"
	"fmt"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type NotificationLogsStore struct {
	db *sql.DB
}

// LogNotification records that a specific notification type was sent for an event
func (s *NotificationLogsStore) LogNotification(ctx context.Context, eventID uuid.UUID, notifType models.NotificationType) error {
	query := `
		INSERT INTO notification_logs (event_id, type)
		VALUES ($1, $2)
		ON CONFLICT (event_id, type) DO NOTHING`

	_, err := s.db.ExecContext(ctx, query, eventID, notifType)
	if err != nil {
		return fmt.Errorf("failed to log notification: %w", err)
	}

	return nil
}

// HasNotificationBeenSent checks if a notification type has already been sent for an event
func (s *NotificationLogsStore) HasNotificationBeenSent(ctx context.Context, eventID uuid.UUID, notifType models.NotificationType) (bool, error) {
	query := `
		SELECT EXISTS(
			SELECT 1 FROM notification_logs 
			WHERE event_id = $1 AND type = $2
		)`

	var exists bool
	if err := s.db.QueryRowContext(ctx, query, eventID, notifType).Scan(&exists); err != nil {
		return false, fmt.Errorf("failed to check notification log: %w", err)
	}

	return exists, nil
}
