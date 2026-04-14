package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type AuditLogsStore struct {
	db *sql.DB
}

// Log records an audit trail entry.
func (s *AuditLogsStore) Log(ctx context.Context, log *models.AuditLog) error {
	query := `
		INSERT INTO audit_logs (user_id, event_id, action, entity_type, entity_id, old_value, new_value)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at`

	return s.db.QueryRowContext(ctx, query,
		log.UserID,
		log.EventID,
		log.Action,
		log.EntityType,
		log.EntityID,
		log.OldValue,
		log.NewValue,
	).Scan(&log.ID, &log.CreatedAt)
}

// GetByEventID retrieves all audit logs for a specific event.
func (s *AuditLogsStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.AuditLogWithUser, error) {
	query := `
		SELECT al.id, al.user_id, al.event_id, al.action, al.entity_type, al.entity_id,
		       al.old_value, al.new_value, al.created_at,
		       COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.email) as user_name
		FROM audit_logs al
		LEFT JOIN users u ON al.user_id = u.id
		WHERE al.event_id = $1
		ORDER BY al.created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logs []models.AuditLogWithUser
	for rows.Next() {
		var l models.AuditLogWithUser
		if err := rows.Scan(
			&l.ID, &l.UserID, &l.EventID, &l.Action, &l.EntityType, &l.EntityID,
			&l.OldValue, &l.NewValue, &l.CreatedAt, &l.UserName,
		); err != nil {
			return nil, err
		}
		logs = append(logs, l)
	}
	return logs, nil
}
