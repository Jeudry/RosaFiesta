package store

import (
	"context"
	"database/sql"
	"errors"
	"fmt"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type ArticlesStore struct {
	db *sql.DB
}

// Transaction helper
func (s *ArticlesStore) execTx(ctx context.Context, fn func(*sql.Tx) error) error {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	if err := fn(tx); err != nil {
		return err
	}

	return tx.Commit()
}

func (s *ArticlesStore) Create(ctx context.Context, article *models.Article) error {
	return s.execTx(ctx, func(tx *sql.Tx) error {
		// 1. Insert Article
		query := `
			INSERT INTO articles (
				name_template, description_template, type, category_id, is_active, created_by, updated_by
			) VALUES (
				$1, $2, $3, $4, $5, $6, $6
			) RETURNING id, created, updated`

		if err := tx.QueryRowContext(ctx, query,
			article.NameTemplate,
			article.DescriptionTemplate,
			article.Type,
			article.CategoryID,
			article.IsActive,
			article.CreatedBy,
		).Scan(&article.ID, &article.Created, &article.Updated); err != nil {
			return fmt.Errorf("failed to insert article: %w", err)
		}

		// 2. Insert Variants
		for i, variant := range article.Variants {
			var variantId uuid.UUID
			queryVariant := `
				INSERT INTO article_variants (
					article_id, sku, name, description, image_url, is_active, stock, rental_price, sale_price, replacement_cost
				) VALUES (
					$1, $2, $3, $4, $5, $6, $7, $8, $9, $10
				) RETURNING id`

			if err := tx.QueryRowContext(ctx, queryVariant,
				article.ID,
				variant.Sku,
				variant.Name,
				variant.Description,
				variant.ImageURL,
				variant.IsActive,
				variant.Stock,
				variant.RentalPrice,
				variant.SalePrice,
				variant.ReplacementCost,
			).Scan(&variantId); err != nil {
				return fmt.Errorf("failed to insert variant %s: %w", variant.Sku, err)
			}
			article.Variants[i].ID = variantId
			article.Variants[i].ArticleID = article.ID

			// 3. Insert Attributes
			if len(variant.Attributes) > 0 {
				queryAttr := `INSERT INTO article_variant_attributes (variant_id, key, value) VALUES ($1, $2, $3)`
				stmtAttr, err := tx.PrepareContext(ctx, queryAttr)
				if err != nil {
					return err
				}
				defer stmtAttr.Close()

				for k, v := range variant.Attributes {
					if _, err := stmtAttr.ExecContext(ctx, variantId, k, v); err != nil {
						return fmt.Errorf("failed to insert attribute %s for variant %s: %w", k, variant.Sku, err)
					}
				}
			}

			// 4. Insert Dimensions
			if len(variant.Dimensions) > 0 {
				queryDim := `
					INSERT INTO article_variant_dimensions (
						variant_id, height, width, depth, weight
					) VALUES ($1, $2, $3, $4, $5) RETURNING id`
				stmtDim, err := tx.PrepareContext(ctx, queryDim)
				if err != nil {
					return err
				}
				defer stmtDim.Close()

				for j, dim := range variant.Dimensions {
					var dimId uuid.UUID
					if err := stmtDim.QueryRowContext(ctx, variantId, dim.Height, dim.Width, dim.Depth, dim.Weight).Scan(&dimId); err != nil {
						return fmt.Errorf("failed to insert dimension for variant %s: %w", variant.Sku, err)
					}
					article.Variants[i].Dimensions[j].ID = dimId
					article.Variants[i].Dimensions[j].VariantID = variantId
				}
			}
		}

		return nil
	})
}

func (s *ArticlesStore) GetById(ctx context.Context, id uuid.UUID) (*models.Article, error) {
	article := &models.Article{}

	// 1. Get Article
	query := `
		SELECT id, name_template, description_template, COALESCE(type, ''), category_id, is_active, created, updated, created_by, updated_by
		FROM articles
		WHERE id = $1`

	if err := s.db.QueryRowContext(ctx, query, id).Scan(
		&article.ID,
		&article.NameTemplate,
		&article.DescriptionTemplate,
		&article.Type,
		&article.CategoryID,
		&article.IsActive,
		&article.Created,
		&article.Updated,
		&article.CreatedBy,
		&article.UpdatedBy,
	); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	// 2. Get Variants
	// Note: N+1 problem minimized by fetching all variants for this article
	// Attributes and Dimensions still need care. For MVP/Speed, simple iterative fetching or JOINs.
	// Let's use two queries: one for variants, one for attributes/dimensions joined or separate.
	// For simplicity in this iteration: Fetch variants, then loop (optimize later if perf hit).

	queryVariants := `
		SELECT id, sku, name, description, image_url, is_active, stock, rental_price, sale_price, replacement_cost
		FROM article_variants
		WHERE article_id = $1`

	rows, err := s.db.QueryContext(ctx, queryVariants, article.ID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	for rows.Next() {
		var v models.ArticleVariant
		v.ArticleID = article.ID // explicit set
		if err := rows.Scan(
			&v.ID, &v.Sku, &v.Name, &v.Description, &v.ImageURL, &v.IsActive, &v.Stock,
			&v.RentalPrice, &v.SalePrice, &v.ReplacementCost,
		); err != nil {
			return nil, err
		}

		// Fetch Attributes
		attrRows, err := s.db.QueryContext(ctx, `SELECT key, value FROM article_variant_attributes WHERE variant_id = $1`, v.ID)
		if err == nil {
			v.Attributes = make(map[string]string)
			for attrRows.Next() {
				var k, val string
				if err := attrRows.Scan(&k, &val); err == nil {
					v.Attributes[k] = val
				}
			}
			attrRows.Close()
		}

		// Fetch Dimensions
		dimRows, err := s.db.QueryContext(ctx, `SELECT id, height, width, depth, weight FROM article_variant_dimensions WHERE variant_id = $1`, v.ID)
		if err == nil {
			for dimRows.Next() {
				var d models.ArticleDimension
				d.VariantID = v.ID
				if err := dimRows.Scan(&d.ID, &d.Height, &d.Width, &d.Depth, &d.Weight); err == nil {
					v.Dimensions = append(v.Dimensions, d)
				}
			}
			dimRows.Close()
		}

		article.Variants = append(article.Variants, v)
	}

	return article, nil
}

func (s *ArticlesStore) GetAll(ctx context.Context) ([]models.Article, error) {
	// Basic implementation: fetch articles only, no deep nested variants for list view to avoid massive payload/query
	query := `
		SELECT id, name_template, description_template, COALESCE(type, ''), category_id, is_active, created, updated, created_by, updated_by
		FROM articles`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var articles []models.Article
	for rows.Next() {
		var article models.Article
		if err := rows.Scan(
			&article.ID, &article.NameTemplate, &article.DescriptionTemplate, &article.Type,
			&article.CategoryID, &article.IsActive, &article.Created, &article.Updated,
			&article.CreatedBy, &article.UpdatedBy,
		); err != nil {
			return nil, err
		}
		articles = append(articles, article)
	}

	return articles, nil
}

func (s *ArticlesStore) GetByCategoryID(ctx context.Context, categoryID uuid.UUID) ([]models.Article, error) {
	query := `
		SELECT id, name_template, description_template, COALESCE(type, ''), category_id, is_active, created, updated, created_by, updated_by
		FROM articles
		WHERE category_id = $1`

	rows, err := s.db.QueryContext(ctx, query, categoryID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var articles []models.Article
	for rows.Next() {
		var article models.Article
		if err := rows.Scan(
			&article.ID, &article.NameTemplate, &article.DescriptionTemplate, &article.Type,
			&article.CategoryID, &article.IsActive, &article.Created, &article.Updated,
			&article.CreatedBy, &article.UpdatedBy,
		); err != nil {
			return nil, err
		}
		articles = append(articles, article)
	}

	return articles, nil
}

func (s *ArticlesStore) Update(ctx context.Context, article *models.Article) error {
	// Not fully implemented deep update for variants yet in this snippet requirement,
	// focusing on Article fields for now or full graph replacement?
	// User asked for "adjust endpoints... to be completely articles".
	// Implementing basic article update. Full variant sync is complex.

	query := `
		UPDATE articles 
		SET name_template = $1, description_template = $2, type = $3, category_id = $4, is_active = $5, updated = NOW(), updated_by = $6
		WHERE id = $7
		RETURNING updated`

	err := s.db.QueryRowContext(ctx, query,
		article.NameTemplate, article.DescriptionTemplate, article.Type, article.CategoryID, article.IsActive, article.UpdatedBy, article.ID,
	).Scan(&article.Updated)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *ArticlesStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM articles WHERE id = $1`
	res, err := s.db.ExecContext(ctx, query, id)
	if err != nil {
		return err
	}

	rows, _ := res.RowsAffected()
	if rows == 0 {
		return ErrNotFound
	}

	return nil
}
