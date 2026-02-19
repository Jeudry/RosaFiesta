package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type GuestStore struct {
	db *sql.DB
}

func (s *GuestStore) Create(ctx context.Context, guest *models.Guest) error {
	query := `
		INSERT INTO guests (event_id, name, email, phone, rsvp_status, plus_one, dietary_restrictions)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at, updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		guest.EventID,
		guest.Name,
		guest.Email,
		guest.Phone,
		guest.RSVPStatus,
		guest.PlusOne,
		guest.DietaryRestrictions,
	).Scan(
		&guest.ID,
		&guest.CreatedAt,
		&guest.UpdatedAt,
	)
	if err != nil {
		return err
	}

	return nil
}

func (s *GuestStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Guest, error) {
	query := `
		SELECT id, event_id, name, email, phone, rsvp_status, plus_one, dietary_restrictions, created_at, updated_at
		FROM guests
		WHERE id = $1
	`

	var guest models.Guest
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&guest.ID,
		&guest.EventID,
		&guest.Name,
		&guest.Email,
		&guest.Phone,
		&guest.RSVPStatus,
		&guest.PlusOne,
		&guest.DietaryRestrictions,
		&guest.CreatedAt,
		&guest.UpdatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &guest, nil
}

func (s *GuestStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.Guest, error) {
	query := `
		SELECT id, event_id, name, email, phone, rsvp_status, plus_one, dietary_restrictions, created_at, updated_at
		FROM guests
		WHERE event_id = $1
		ORDER BY name ASC
	`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var guests []models.Guest
	for rows.Next() {
		var guest models.Guest
		err := rows.Scan(
			&guest.ID,
			&guest.EventID,
			&guest.Name,
			&guest.Email,
			&guest.Phone,
			&guest.RSVPStatus,
			&guest.PlusOne,
			&guest.DietaryRestrictions,
			&guest.CreatedAt,
			&guest.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		guests = append(guests, guest)
	}

	if guests == nil {
		guests = []models.Guest{}
	}

	return guests, nil
}

func (s *GuestStore) Update(ctx context.Context, guest *models.Guest) error {
	query := `
		UPDATE guests
		SET name = $1, email = $2, phone = $3, rsvp_status = $4, plus_one = $5, dietary_restrictions = $6, updated_at = NOW()
		WHERE id = $7
		RETURNING updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		guest.Name,
		guest.Email,
		guest.Phone,
		guest.RSVPStatus,
		guest.PlusOne,
		guest.DietaryRestrictions,
		guest.ID,
	).Scan(&guest.UpdatedAt)
	if err != nil {
		if err == sql.ErrNoRows {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *GuestStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM guests WHERE id = $1`

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
