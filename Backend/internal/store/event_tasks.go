package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type EventTaskStore struct {
	db *sql.DB
}

func (s *EventTaskStore) Create(ctx context.Context, task *models.EventTask) error {
	query := `
		INSERT INTO event_tasks (event_id, title, description, is_completed, due_date)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id, created_at, updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		task.EventID,
		task.Title,
		task.Description,
		task.IsCompleted,
		task.DueDate,
	).Scan(
		&task.ID,
		&task.CreatedAt,
		&task.UpdatedAt,
	)
	if err != nil {
		return err
	}

	return nil
}

func (s *EventTaskStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.EventTask, error) {
	query := `
		SELECT id, event_id, title, description, is_completed, due_date, created_at, updated_at
		FROM event_tasks
		WHERE event_id = $1
		ORDER BY created_at ASC
	`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var tasks []models.EventTask
	for rows.Next() {
		var task models.EventTask
		err := rows.Scan(
			&task.ID,
			&task.EventID,
			&task.Title,
			&task.Description,
			&task.IsCompleted,
			&task.DueDate,
			&task.CreatedAt,
			&task.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		tasks = append(tasks, task)
	}

	return tasks, nil
}

func (s *EventTaskStore) GetByID(ctx context.Context, id uuid.UUID) (*models.EventTask, error) {
	query := `
		SELECT id, event_id, title, description, is_completed, due_date, created_at, updated_at
		FROM event_tasks
		WHERE id = $1
	`

	var task models.EventTask
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&task.ID,
		&task.EventID,
		&task.Title,
		&task.Description,
		&task.IsCompleted,
		&task.DueDate,
		&task.CreatedAt,
		&task.UpdatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &task, nil
}

func (s *EventTaskStore) Update(ctx context.Context, task *models.EventTask) error {
	query := `
		UPDATE event_tasks
		SET title = $1, description = $2, is_completed = $3, due_date = $4, updated_at = NOW()
		WHERE id = $5
		RETURNING updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		task.Title,
		task.Description,
		task.IsCompleted,
		task.DueDate,
		task.ID,
	).Scan(&task.UpdatedAt)
	if err != nil {
		if err == sql.ErrNoRows {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *EventTaskStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM event_tasks WHERE id = $1`

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
