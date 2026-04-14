package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type EventPhotosStore struct {
	db *sql.DB
}

func (s *EventPhotosStore) Create(ctx context.Context, photo *models.EventPhoto) error {
	query := `
		INSERT INTO event_photos (event_id, url, caption)
		VALUES ($1, $2, $3)
		RETURNING id, uploaded_at`

	return s.db.QueryRowContext(ctx, query,
		photo.EventID,
		photo.URL,
		photo.Caption,
	).Scan(&photo.ID, &photo.UploadedAt)
}

func (s *EventPhotosStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.EventPhoto, error) {
	query := `
		SELECT id, event_id, url, caption, uploaded_at
		FROM event_photos
		WHERE event_id = $1
		ORDER BY uploaded_at DESC`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	photos := []models.EventPhoto{}
	for rows.Next() {
		var p models.EventPhoto
		if err := rows.Scan(&p.ID, &p.EventID, &p.URL, &p.Caption, &p.UploadedAt); err != nil {
			return nil, err
		}
		photos = append(photos, p)
	}
	return photos, nil
}

func (s *EventPhotosStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM event_photos WHERE id = $1`
	_, err := s.db.ExecContext(ctx, query, id)
	return err
}
