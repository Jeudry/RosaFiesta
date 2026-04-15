package store

import (
	"context"
	"database/sql"

	"github.com/google/uuid"
)

type EventColorsStore struct {
	db *sql.DB
}

// SetColors replaces all colors for an event with the provided list.
// It uses a delete-then-insert pattern (upsert).
func (s *EventColorsStore) SetColors(ctx context.Context, eventID uuid.UUID, colors []string) error {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	// Delete existing colors for this event
	_, err = tx.ExecContext(ctx, "DELETE FROM event_colors WHERE event_id = $1", eventID)
	if err != nil {
		return err
	}

	// Insert new colors
	for i, hex := range colors {
		_, err = tx.ExecContext(ctx,
			"INSERT INTO event_colors (event_id, color_hex, sort_order) VALUES ($1, $2, $3)",
			eventID, hex, i,
		)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

// GetByEventID returns all color hex strings for a given event.
func (s *EventColorsStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]string, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx,
		"SELECT color_hex FROM event_colors WHERE event_id = $1 ORDER BY sort_order ASC",
		eventID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var colors []string
	for rows.Next() {
		var hex string
		if err := rows.Scan(&hex); err != nil {
			return nil, err
		}
		colors = append(colors, hex)
	}

	if colors == nil {
		colors = []string{}
	}

	return colors, rows.Err()
}