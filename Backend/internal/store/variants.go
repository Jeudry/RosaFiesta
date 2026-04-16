package store

import (
	"context"
	"database/sql"
	"errors"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type VariantsStore struct {
	db *sql.DB
}

func (s *VariantsStore) Create(ctx context.Context, variant *models.ArticleVariant) error {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	query := `
		INSERT INTO article_variants (id, article_id, sku, name, description, image_url, is_active, stock, rental_price, sale_price, replacement_cost)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)`

	if variant.ID == uuid.Nil {
		variant.ID = uuid.New()
	}
	if variant.Sku == "" {
		variant.Sku = variant.ID.String()
	}

	_, err = tx.ExecContext(ctx, query,
		variant.ID, variant.ArticleID, variant.Sku, variant.Name, variant.Description,
		variant.ImageURL, variant.IsActive, variant.Stock, variant.RentalPrice,
		variant.SalePrice, variant.ReplacementCost,
	)
	if err != nil {
		return err
	}

	// Attributes
	for key, val := range variant.Attributes {
		_, err = tx.ExecContext(ctx,
			`INSERT INTO article_variant_attributes (variant_id, key, value) VALUES ($1, $2, $3)`,
			variant.ID, key, val,
		)
		if err != nil {
			return err
		}
	}

	// Dimensions
	for _, dim := range variant.Dimensions {
		dimID := dim.ID
		if dimID == uuid.Nil {
			dimID = uuid.New()
		}
		dim.VariantID = variant.ID
		_, err = tx.ExecContext(ctx,
			`INSERT INTO article_variant_dimensions (id, variant_id, height, width, depth, weight)
			 VALUES ($1, $2, $3, $4, $5, $6)`,
			dimID, variant.ID, dim.Height, dim.Width, dim.Depth, dim.Weight,
		)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

func (s *VariantsStore) GetByArticleID(ctx context.Context, articleID uuid.UUID) ([]models.ArticleVariant, error) {
	query := `
		SELECT id, article_id, sku, name, COALESCE(description, ''), COALESCE(image_url, ''),
		       is_active, stock, rental_price, COALESCE(sale_price, 0), COALESCE(replacement_cost, 0)
		FROM article_variants
		WHERE article_id = $1
		ORDER BY created_at ASC`

	rows, err := s.db.QueryContext(ctx, query, articleID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var variants []models.ArticleVariant
	for rows.Next() {
		var v models.ArticleVariant
		if err := rows.Scan(
			&v.ID, &v.ArticleID, &v.Sku, &v.Name, &v.Description, &v.ImageURL,
			&v.IsActive, &v.Stock, &v.RentalPrice, &v.SalePrice, &v.ReplacementCost,
		); err != nil {
			return nil, err
		}

		// Attributes
		attrRows, err := s.db.QueryContext(ctx,
			`SELECT key, value FROM article_variant_attributes WHERE variant_id = $1`, v.ID)
		if err == nil {
			v.Attributes = make(map[string]string)
			for attrRows.Next() {
				var k, val string
				if attrRows.Scan(&k, &val) == nil {
					v.Attributes[k] = val
				}
			}
			attrRows.Close()
		}

		// Dimensions
		dimRows, err := s.db.QueryContext(ctx,
			`SELECT id, height, width, depth, weight FROM article_variant_dimensions WHERE variant_id = $1`, v.ID)
		if err == nil {
			for dimRows.Next() {
				var d models.ArticleDimension
				d.VariantID = v.ID
				if dimRows.Scan(&d.ID, &d.Height, &d.Width, &d.Depth, &d.Weight) == nil {
					v.Dimensions = append(v.Dimensions, d)
				}
			}
			dimRows.Close()
		}

		variants = append(variants, v)
	}

	return variants, nil
}

func (s *VariantsStore) GetByID(ctx context.Context, id uuid.UUID) (*models.ArticleVariant, error) {
	query := `
		SELECT id, article_id, sku, name, COALESCE(description, ''), COALESCE(image_url, ''),
		       is_active, stock, rental_price, COALESCE(sale_price, 0), COALESCE(replacement_cost, 0)
		FROM article_variants
		WHERE id = $1`

	v := &models.ArticleVariant{}
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&v.ID, &v.ArticleID, &v.Sku, &v.Name, &v.Description, &v.ImageURL,
		&v.IsActive, &v.Stock, &v.RentalPrice, &v.SalePrice, &v.ReplacementCost,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	// Attributes
	attrRows, err := s.db.QueryContext(ctx,
		`SELECT key, value FROM article_variant_attributes WHERE variant_id = $1`, v.ID)
	if err == nil {
		v.Attributes = make(map[string]string)
		for attrRows.Next() {
			var k, val string
			if attrRows.Scan(&k, &val) == nil {
				v.Attributes[k] = val
			}
		}
		attrRows.Close()
	}

	// Dimensions
	dimRows, err := s.db.QueryContext(ctx,
		`SELECT id, height, width, depth, weight FROM article_variant_dimensions WHERE variant_id = $1`, v.ID)
	if err == nil {
		for dimRows.Next() {
			var d models.ArticleDimension
			d.VariantID = v.ID
			if dimRows.Scan(&d.ID, &d.Height, &d.Width, &d.Depth, &d.Weight) == nil {
				v.Dimensions = append(v.Dimensions, d)
			}
		}
		dimRows.Close()
	}

	return v, nil
}

func (s *VariantsStore) Update(ctx context.Context, variant *models.ArticleVariant) error {
	query := `
		UPDATE article_variants SET
			sku = $2, name = $3, description = $4, image_url = $5,
			is_active = $6, stock = $7, rental_price = $8, sale_price = $9, replacement_cost = $10
		WHERE id = $1`

	_, err := s.db.ExecContext(ctx, query,
		variant.ID, variant.Sku, variant.Name, variant.Description, variant.ImageURL,
		variant.IsActive, variant.Stock, variant.RentalPrice, variant.SalePrice, variant.ReplacementCost,
	)
	return err
}

func (s *VariantsStore) Delete(ctx context.Context, id uuid.UUID) error {
	_, err := s.db.ExecContext(ctx, `DELETE FROM article_variants WHERE id = $1`, id)
	return err
}
