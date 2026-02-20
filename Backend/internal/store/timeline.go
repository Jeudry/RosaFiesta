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
	GetOverdueCriticalItems(context.Context) ([]models.TimelineItemWithUser, error)
}

type timelineStore struct {
	db *sql.DB
}

func (s *timelineStore) Create(ctx context.Context, item *models.TimelineItem) error {
	query := `
		INSERT INTO event_timeline_items (event_id, title, description, start_time, end_time, is_completed, is_critical)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at, updated_at, completed_at
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
		item.IsCompleted,
		item.IsCritical,
	).Scan(
		&item.ID,
		&item.CreatedAt,
		&item.UpdatedAt,
		&item.CompletedAt,
	)

	return err
}

func (s *timelineStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.TimelineItem, error) {
	query := `
		SELECT id, event_id, title, description, start_time, end_time, is_completed, is_critical, created_at, updated_at
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
			&i.IsCompleted,
			&i.IsCritical,
			&i.CompletedAt,
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
		SELECT id, event_id, title, description, start_time, end_time, is_completed, is_critical, created_at, updated_at
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
		&i.IsCompleted,
		&i.IsCritical,
		&i.CompletedAt,
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
		SET title = $1, 
		    description = $2, 
		    start_time = $3, 
		    end_time = $4, 
		    is_completed = $5, 
		    is_critical = $6, 
		    updated_at = NOW(),
		    completed_at = CASE 
		        WHEN $5 = TRUE AND completed_at IS NULL THEN NOW()
		        WHEN $5 = FALSE THEN NULL
		        ELSE completed_at
		    END
		WHERE id = $7
		RETURNING updated_at, completed_at
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
		item.IsCompleted,
		item.IsCritical,
		item.ID,
	).Scan(&item.UpdatedAt, &item.CompletedAt)
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

func (s *timelineStore) GetOverdueCriticalItems(ctx context.Context) ([]models.TimelineItemWithUser, error) {
	// Items that are critical, not completed, and start_time was more than 15 minutes ago
	// Joining with events and users to get the FCM token of the organizer
	query := `
		SELECT 
			t.id, t.event_id, t.title, t.description, t.start_time, t.end_time, t.is_completed, t.is_critical, t.completed_at, t.created_at, t.updated_at,
			u.fcm_token
		FROM event_timeline_items t
		JOIN events e ON t.event_id = e.id
		JOIN users u ON e.user_id = u.id
		WHERE t.is_critical = TRUE 
		  AND t.is_completed = FALSE 
		  AND e.status IN ('confirmed', 'paid')
		  AND t.start_time < (NOW() - INTERVAL '15 minutes')
		ORDER BY t.start_time ASC
	`

	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []models.TimelineItemWithUser
	for rows.Next() {
		var i models.TimelineItemWithUser
		err := rows.Scan(
			&i.ID,
			&i.EventID,
			&i.Title,
			&i.Description,
			&i.StartTime,
			&i.EndTime,
			&i.IsCompleted,
			&i.IsCritical,
			&i.CompletedAt,
			&i.CreatedAt,
			&i.UpdatedAt,
			&i.UserFCMToken,
		)
		if err != nil {
			return nil, err
		}
		items = append(items, i)
	}

	if items == nil {
		items = []models.TimelineItemWithUser{}
	}

	return items, nil
}
