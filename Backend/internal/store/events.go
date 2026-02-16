package store

import (
	"context"
	"database/sql"
	"errors"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type EventStore struct {
	db *sql.DB
}

func (s *EventStore) Create(ctx context.Context, event *models.Event) error {
	query := `
		INSERT INTO events (user_id, name, date, location, guest_count, budget, status)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at, updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		event.UserID,
		event.Name,
		event.Date,
		event.Location,
		event.GuestCount,
		event.Budget,
		event.Status,
	).Scan(
		&event.ID,
		&event.CreatedAt,
		&event.UpdatedAt,
	)
	if err != nil {
		return err
	}

	return nil
}

func (s *EventStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Event, error) {
	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, created_at, updated_at
		FROM events
		WHERE id = $1
	`

	var event models.Event
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&event.ID,
		&event.UserID,
		&event.Name,
		&event.Date,
		&event.Location,
		&event.GuestCount,
		&event.Budget,
		&event.Status,
		&event.CreatedAt,
		&event.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &event, nil
}

func (s *EventStore) GetByUserID(ctx context.Context, userID uuid.UUID) ([]models.Event, error) {
	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, created_at, updated_at
		FROM events
		WHERE user_id = $1
		ORDER BY date ASC
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var events []models.Event
	for rows.Next() {
		var event models.Event
		err := rows.Scan(
			&event.ID,
			&event.UserID,
			&event.Name,
			&event.Date,
			&event.Location,
			&event.GuestCount,
			&event.Budget,
			&event.Status,
			&event.CreatedAt,
			&event.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		events = append(events, event)
	}

	return events, nil
}

func (s *EventStore) Update(ctx context.Context, event *models.Event) error {
	query := `
		UPDATE events
		SET name = $1, date = $2, location = $3, guest_count = $4, budget = $5, status = $6, updated_at = NOW()
		WHERE id = $7
		RETURNING updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		event.Name,
		event.Date,
		event.Location,
		event.GuestCount,
		event.Budget,
		event.Status,
		event.ID,
	).Scan(&event.UpdatedAt)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *EventStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM events WHERE id = $1`

	res, err := s.db.ExecContext(ctx, query, id)
	if err != nil {
		return err
	}

	rows, err := res.RowsAffected()
	if err != nil {
		return err
	}

	if rows == 0 {
		return ErrNotFound
	}

	return nil
}
