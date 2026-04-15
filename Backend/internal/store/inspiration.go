package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type InspirationStore struct {
	db *sql.DB
}

func (s *InspirationStore) Upload(ctx context.Context, eventID uuid.UUID, photoURL string, caption string, uploadedBy uuid.UUID) error {
	query := `
		INSERT INTO event_inspiration (event_id, photo_url, caption, uploaded_by)
		VALUES ($1, $2, $3, $4)
		RETURNING id, uploaded_at`

	var id uuid.UUID
	var uploadedAt sql.NullTime

	err := s.db.QueryRowContext(ctx, query,
		eventID,
		photoURL,
		nullableString(caption),
		uploadedBy,
	).Scan(&id, &uploadedAt)

	if err != nil {
		return err
	}

	return nil
}

func (s *InspirationStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.EventInspiration, error) {
	query := `
		SELECT id, event_id, photo_url, caption, uploaded_by, uploaded_at
		FROM event_inspiration
		WHERE event_id = $1
		ORDER BY uploaded_at DESC`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	photos := []models.EventInspiration{}
	for rows.Next() {
		var p models.EventInspiration
		var caption sql.NullString
		var uploadedBy sql.NullString

		if err := rows.Scan(&p.ID, &p.EventID, &p.PhotoURL, &caption, &uploadedBy, &p.UploadedAt); err != nil {
			return nil, err
		}
		if caption.Valid {
			p.Caption = &caption.String
		}
		if uploadedBy.Valid {
			if uid, err := uuid.Parse(uploadedBy.String); err == nil {
				p.UploadedBy = uid
			}
		}
		photos = append(photos, p)
	}
	return photos, nil
}

func (s *InspirationStore) Delete(ctx context.Context, inspirationID uuid.UUID) error {
	query := `DELETE FROM event_inspiration WHERE id = $1`
	_, err := s.db.ExecContext(ctx, query, inspirationID)
	return err
}

func nullableString(s string) sql.NullString {
	if s == "" {
		return sql.NullString{}
	}
	return sql.NullString{String: s, Valid: true}
}