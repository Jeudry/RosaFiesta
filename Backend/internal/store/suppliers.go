package store

import (
	"context"
	"database/sql"
	"errors"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type SupplierStore struct {
	db *sql.DB
}

func (s *SupplierStore) Create(ctx context.Context, supplier *models.Supplier) error {
	query := `
		INSERT INTO suppliers (user_id, name, contact_name, email, phone, website, notes)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id, created_at, updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		supplier.UserID,
		supplier.Name,
		supplier.ContactName,
		supplier.Email,
		supplier.Phone,
		supplier.Website,
		supplier.Notes,
	).Scan(
		&supplier.ID,
		&supplier.CreatedAt,
		&supplier.UpdatedAt,
	)

	return err
}

func (s *SupplierStore) GetByUserID(ctx context.Context, userID uuid.UUID) ([]models.Supplier, error) {
	query := `
		SELECT id, user_id, name, contact_name, email, phone, website, notes, created_at, updated_at
		FROM suppliers
		WHERE user_id = $1
		ORDER BY created_at DESC
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var suppliers []models.Supplier
	for rows.Next() {
		var s models.Supplier
		err := rows.Scan(
			&s.ID,
			&s.UserID,
			&s.Name,
			&s.ContactName,
			&s.Email,
			&s.Phone,
			&s.Website,
			&s.Notes,
			&s.CreatedAt,
			&s.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		suppliers = append(suppliers, s)
	}

	return suppliers, nil
}

func (s *SupplierStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Supplier, error) {
	query := `
		SELECT id, user_id, name, contact_name, email, phone, website, notes, created_at, updated_at
		FROM suppliers
		WHERE id = $1
	`

	var supplier models.Supplier
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&supplier.ID,
		&supplier.UserID,
		&supplier.Name,
		&supplier.ContactName,
		&supplier.Email,
		&supplier.Phone,
		&supplier.Website,
		&supplier.Notes,
		&supplier.CreatedAt,
		&supplier.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &supplier, nil
}

func (s *SupplierStore) Update(ctx context.Context, supplier *models.Supplier) error {
	query := `
		UPDATE suppliers
		SET name = $1, contact_name = $2, email = $3, phone = $4, website = $5, notes = $6, updated_at = CURRENT_TIMESTAMP
		WHERE id = $7
		RETURNING updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		supplier.Name,
		supplier.ContactName,
		supplier.Email,
		supplier.Phone,
		supplier.Website,
		supplier.Notes,
		supplier.ID,
	).Scan(&supplier.UpdatedAt)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *SupplierStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM suppliers WHERE id = $1`

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
