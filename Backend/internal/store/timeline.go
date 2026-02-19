package store

import (
	"context"
	"database/sql"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type TimelineStore interface {
	Create(context.Context, *models.TimelineItem) error
	GetByEventID(context.Context, uuid.UUID) ([]models.TimelineItem, error)
	GetByID(context.Context, uuid.UUID) (*models.TimelineItem, error)
	Update(context.Context, *models.TimelineItem) error
	Delete(context.Context, uuid.UUID) error
}

type timelineStore struct {
	db *sql.DB
}

func (s *timelineStore) Create(ctx context.Context, item *models.TimelineItem) error {
	query := `
		INSERT INTO event_timeline_items (event_id, title, description, start_time, end_time)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id, created_at, updated_at
	`

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	err := s.db.QueryRowContext(
		ctx,
		query,
		item.EventID,
		item.Title,
		item.Description,
		item.StartTime,
		item.EndTime,
	).Scan(
		&item.ID,
		&item.CreatedAt,
		&item.UpdatedAt,
	)

	return err
}

func (s *timelineStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.TimelineItem, error) {
	query := `
		SELECT id, event_id, title, description, start_time, end_time, created_at, updated_at
		FROM event_timeline_items
		WHERE event_id = $1
		ORDER BY start_time ASC
	`

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []models.TimelineItem
	for rows.Next() {
		var i models.TimelineItem
		err := rows.Scan(
			&i.ID,
			&i.EventID,
			&i.Title,
			&i.Description,
			&i.StartTime,
			&i.EndTime,
			&i.CreatedAt,
			&i.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		items = append(items, i)
	}

	if items == nil {
		items = []models.TimelineItem{}
	}

	return items, nil
}

func (s *timelineStore) GetByID(ctx context.Context, id uuid.UUID) (*models.TimelineItem, error) {
	query := `
		SELECT id, event_id, title, description, start_time, end_time, created_at, updated_at
		FROM event_timeline_items
		WHERE id = $1
	`

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	var i models.TimelineItem
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&i.ID,
		&i.EventID,
		&i.Title,
		&i.Description,
		&i.StartTime,
		&i.EndTime,
		&i.CreatedAt,
		&i.UpdatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &i, nil
}

func (s *timelineStore) Update(ctx context.Context, item *models.TimelineItem) error {
	query := `
		UPDATE event_timeline_items
		SET title = $1, description = $2, start_time = $3, end_time = $4, updated_at = NOW()
		WHERE id = $5
		RETURNING updated_at
	`

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	err := s.db.QueryRowContext(
		ctx,
		query,
		item.Title,
		item.Description,
		item.StartTime,
		item.EndTime,
		item.ID,
	).Scan(&item.UpdatedAt)
	if err != nil {
		if err == sql.ErrNoRows {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *timelineStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM event_timeline_items WHERE id = $1`

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

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
