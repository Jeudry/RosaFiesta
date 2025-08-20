package store

import (
	"Backend/internal/store/models"
	"context"
	"database/sql"
	"errors"
	"fmt"
	"github.com/google/uuid"
)

type CategoriesStore struct {
	db *sql.DB
}

func (s *CategoriesStore) GetAll(ctx context.Context) ([]models.Category, error) {
	query := `
		SELECT id, created_by, name, description, image_url, parent_id, created, updated, updated_by
		FROM categories
		WHERE deleted IS NULL`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var categories []models.Category
	for rows.Next() {
		var category models.Category
		if err := rows.Scan(
			&category.ID,
			&category.CreatedBy,
			&category.Name,
			&category.Description,
			&category.ImageURL,
			&category.ParentID,
			&category.Created,
			&category.Updated,
			&category.UpdatedBy,
		); err != nil {
			return nil, err
		}
		categories = append(categories, category)
	}

	if err := rows.Err(); err != nil {
		return nil, err
	}

	return categories, nil
}

func (s *CategoriesStore) Create(ctx context.Context, category *models.Category) error {
	query := `
		INSERT INTO categories (
			created_by, name, description, image_url, parent_id
		) VALUES (
			$1, $2, $3, $4, $5
		) RETURNING id, created`

	err := s.db.QueryRowContext(ctx, query,
		category.CreatedBy,
		category.Name,
		category.Description,
		category.ImageURL,
		category.ParentID,
	).Scan(&category.ID, &category.Created)

	if err != nil {
		return err
	}

	return nil
}

func (s *CategoriesStore) GetById(ctx context.Context, id uuid.UUID) (*models.Category, error) {
	query := `
		SELECT id, created_by, name, description, image_url, parent_id, created, updated, updated_by
		FROM categories
		WHERE id = $1 AND deleted IS NULL`

	var category models.Category

	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&category.ID,
		&category.CreatedBy,
		&category.Name,
		&category.Description,
		&category.ImageURL,
		&category.ParentID,
		&category.Created,
		&category.Updated,
		&category.UpdatedBy,
	)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, fmt.Errorf("category not found")
		}
		return nil, err
	}

	return &category, nil
}

func (s *CategoriesStore) Update(ctx context.Context, category *models.Category) error {
	query := `
		UPDATE categories
		SET name = $1, description = $2, image_url = $3, parent_id = $4, updated = NOW(), updated_by = $5
		WHERE id = $6 AND deleted IS NULL
		RETURNING updated`

	err := s.db.QueryRowContext(ctx, query,
		category.Name,
		category.Description,
		category.ImageURL,
		category.ParentID,
		category.UpdatedBy,
		category.ID,
	).Scan(&category.Updated)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return fmt.Errorf("category not found")
		}
		return err
	}

	return nil
}

func (s *CategoriesStore) Delete(ctx context.Context, category *models.Category) error {
	query := `
		UPDATE categories
		SET deleted = NOW(), deleted_by = $1
		WHERE id = $2 AND deleted IS NULL
		RETURNING deleted`

	err := s.db.QueryRowContext(ctx, query, category.DeletedBy, category.ID).Scan(&category.Deleted)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return fmt.Errorf("category not found")
		}
		return err
	}

	return nil
}
