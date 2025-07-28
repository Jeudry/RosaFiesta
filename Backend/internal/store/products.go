package store

import (
	"Backend/internal/store/models"
	"context"
	"database/sql"
	"errors"
	"fmt"
	"github.com/google/uuid"
)

type ProductsStore struct {
	db *sql.DB
}

func (s *ProductsStore) GetAll(ctx context.Context) ([]models.Product, error) {
	query := `
		SELECT id, created_by, name, description, price, rental_price, color, size, image_url, stock, created, updated, updated_by
		FROM products`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var products []models.Product
	for rows.Next() {
		var product models.Product
		if err := rows.Scan(
			&product.ID,
			&product.CreatedBy,
			&product.Name,
			&product.Description,
			&product.Price,
			&product.RentalPrice,
			&product.Color,
			&product.Size,
			&product.ImageURL,
			&product.Stock,
			&product.Created,
			&product.Updated,
			&product.UpdatedBy,
		); err != nil {
			return nil, err
		}
		products = append(products, product)
	}

	if err := rows.Err(); err != nil {
		return nil, err
	}

	return products, nil
}

func (s *ProductsStore) Create(ctx context.Context, product *models.Product) error {
	query := `
		INSERT INTO products (
			created_by, name, description, price, rental_price, color, size, image_url, stock
		) VALUES (
			$1, $2, $3, $4, $5, $6, $7, $8, $9
		) RETURNING id`

	err := s.db.QueryRowContext(ctx, query,
		product.CreatedBy,
		product.Name,
		product.Description,
		product.Price,
		product.RentalPrice,
		product.Color,
		product.Size,
		product.ImageURL,
		product.Stock,
	).Scan(&product.ID)

	if err != nil {
		return err
	}

	return nil
}

func (s *ProductsStore) GetById(ctx context.Context, id uuid.UUID) (*models.Product, error) {
	query := `
	  SELECT id, created_by, name, description, price, rental_price, color, size, image_url, stock, created, updated
	  FROM products
	  WHERE id = $1`

	var product models.Product

	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&product.ID,
		&product.CreatedBy,
		&product.Name,
		&product.Description,
		&product.Price,
		&product.RentalPrice,
		&product.Color,
		&product.Size,
		&product.ImageURL,
		&product.Stock,
		&product.Created,
		&product.Updated,
	)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, fmt.Errorf("product not found")
		}
		return nil, err
	}

	return &product, nil
}

func (s *ProductsStore) Update(ctx context.Context, product *models.Product) error {
	query := `
  UPDATE products
  SET name = $1, description = $2, price = $3, rental_price = $4, color = $5, size = $6, image_url = $7, stock = $8, updated = NOW(), updated_by = $9
  WHERE id = $10
  RETURNING updated`

	err := s.db.QueryRowContext(ctx, query,
		product.Name,
		product.Description,
		product.Price,
		product.RentalPrice,
		product.Color,
		product.Size,
		product.ImageURL,
		product.Stock,
		product.UpdatedBy,
		product.ID,
	).Scan(&product.Updated)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return fmt.Errorf("product not found")
		}
		return err
	}

	return nil
}

func (s *ProductsStore) Delete(ctx context.Context, product *models.Product) error {
	query := `
		UPDATE products
		SET deleted = NOW(), deleted_by = $1
		WHERE id = $2
		RETURNING deleted`

	err := s.db.QueryRowContext(ctx, query, product.DeletedBy, product.ID).Scan(&product.Deleted)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return fmt.Errorf("product not found")
		}
		return err
	}

	return nil
}
