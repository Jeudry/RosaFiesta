package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type MessagesStore struct {
	db *sql.DB
}

// Create inserts a new message into the database.
func (s *MessagesStore) Create(ctx context.Context, msg *models.EventMessage) error {
	query := `
		INSERT INTO event_messages (event_id, sender_id, content)
		VALUES ($1, $2, $3)
		RETURNING id, created_at
	`

	return s.db.QueryRowContext(ctx, query,
		msg.EventID,
		msg.SenderID,
		msg.Content,
	).Scan(&msg.ID, &msg.CreatedAt)
}

// GetByEventID retrieves all messages for a specific event, ordered by date.
func (s *MessagesStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.EventMessage, error) {
	query := `
		SELECT m.id, m.event_id, m.sender_id, m.content, m.created_at, u.username
		FROM event_messages m
		JOIN users u ON m.sender_id = u.id
		WHERE m.event_id = $1
		ORDER BY m.created_at ASC
	`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var messages []models.EventMessage
	for rows.Next() {
		var msg models.EventMessage
		if err := rows.Scan(
			&msg.ID,
			&msg.EventID,
			&msg.SenderID,
			&msg.Content,
			&msg.CreatedAt,
			&msg.SenderName,
		); err != nil {
			return nil, err
		}
		messages = append(messages, msg)
	}

	return messages, nil
}
