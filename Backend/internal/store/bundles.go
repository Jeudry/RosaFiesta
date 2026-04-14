package store

import (
	"context"
	"database/sql"
	"errors"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type BundlesStore struct {
	db *sql.DB
}

// GetAll returns all active bundles with their items and article details.
func (s *BundlesStore) GetAll(ctx context.Context) ([]models.Bundle, error) {
	query := `
		SELECT id, name, COALESCE(description, ''), discount_percent, COALESCE(image_url, ''), is_active, min_price, created_at
		FROM bundles
		WHERE is_active = true
		ORDER BY created_at DESC`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var bundles []models.Bundle
	for rows.Next() {
		var b models.Bundle
		if err := rows.Scan(&b.ID, &b.Name, &b.Description, &b.DiscountPercent, &b.ImageURL, &b.IsActive, &b.MinPrice, &b.CreatedAt); err != nil {
			return nil, err
		}
		bundles = append(bundles, b)
	}

	return bundles, nil
}

// GetByID returns a single bundle with its items and article details.
func (s *BundlesStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Bundle, error) {
	bundle := &models.Bundle{}

	query := `
		SELECT id, name, COALESCE(description, ''), discount_percent, COALESCE(image_url, ''), is_active, min_price, created_at
		FROM bundles
		WHERE id = $1 AND is_active = true`

	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&bundle.ID, &bundle.Name, &bundle.Description, &bundle.DiscountPercent,
		&bundle.ImageURL, &bundle.IsActive, &bundle.MinPrice, &bundle.CreatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	// Get bundle items with article details
	itemsQuery := `
		SELECT
			bi.id, bi.bundle_id, bi.article_id, bi.quantity, bi.is_optional,
			a.id, a.name_template, COALESCE(a.description_template, ''), a.type,
			a.stock_quantity, a.low_stock_threshold,
			v.id, v.sku, v.name, COALESCE(v.description, ''), COALESCE(v.image_url, ''), v.is_active,
			v.stock, v.rental_price, v.sale_price, v.replacement_cost
		FROM bundle_items bi
		JOIN articles a ON bi.article_id = a.id
		LEFT JOIN LATERAL (
			SELECT id, sku, name, description, image_url, is_active, stock, rental_price, sale_price, replacement_cost
			FROM article_variants
			WHERE article_id = a.id
			ORDER BY created_at ASC
			LIMIT 1
		) v ON true
		WHERE bi.bundle_id = $1
		ORDER BY bi.sort_order ASC`

	itemRows, err := s.db.QueryContext(ctx, itemsQuery, id)
	if err != nil {
		return nil, err
	}
	defer itemRows.Close()

	for itemRows.Next() {
		var item models.BundleItem
		var article models.Article
		var vID, vSku, vName, vDescription, vImageURL sql.NullString
		var vSalePrice, vReplacementCost sql.NullFloat64
		var vIsActive sql.NullBool
		var vStock sql.NullInt32
		var vRentalPrice sql.NullFloat64

		if err := itemRows.Scan(
			&item.ID, &item.BundleID, &item.ArticleID, &item.Quantity, &item.IsOptional,
			&article.ID, &article.NameTemplate, &article.DescriptionTemplate, &article.Type,
			&article.StockQuantity, &article.LowStockThreshold,
			&vID, &vSku, &vName, &vDescription, &vImageURL, &vIsActive,
			&vStock, &vRentalPrice, &vSalePrice, &vReplacementCost,
		); err != nil {
			return nil, err
		}

		// Build article variant
		if vID.Valid {
			variant := models.ArticleVariant{
				ID:          parseNullStringUUID(vID),
				ArticleID:   article.ID,
				Sku:         vSku.String,
				Name:        vName.String,
				IsActive:    vIsActive.Bool,
				Stock:       int(vStock.Int32),
				RentalPrice: vRentalPrice.Float64,
			}
			if vDescription.Valid {
				desc := vDescription.String
				variant.Description = &desc
			}
			if vImageURL.Valid {
				img := vImageURL.String
				variant.ImageURL = &img
			}
			if vSalePrice.Valid {
				sp := vSalePrice.Float64
				variant.SalePrice = &sp
			}
			if vReplacementCost.Valid {
				rc := vReplacementCost.Float64
				variant.ReplacementCost = &rc
			}
			article.Variants = []models.ArticleVariant{variant}
		}

		item.Article = &article
		bundle.Items = append(bundle.Items, item)
	}

	return bundle, nil
}

// GetByCategory returns bundles that contain items from a specific category.
func (s *BundlesStore) GetByCategory(ctx context.Context, categoryID uuid.UUID) ([]models.Bundle, error) {
	query := `
		SELECT DISTINCT b.id, b.name, COALESCE(b.description, ''), b.discount_percent, COALESCE(b.image_url, ''), b.is_active, b.min_price, b.created_at
		FROM bundles b
		JOIN bundle_items bi ON b.id = bi.bundle_id
		WHERE b.is_active = true AND bi.article_id IN (
			SELECT id FROM articles WHERE category_id = $1
		)
		ORDER BY b.created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, categoryID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var bundles []models.Bundle
	for rows.Next() {
		var b models.Bundle
		if err := rows.Scan(&b.ID, &b.Name, &b.Description, &b.DiscountPercent, &b.ImageURL, &b.IsActive, &b.MinPrice, &b.CreatedAt); err != nil {
			return nil, err
		}
		bundles = append(bundles, b)
	}

	return bundles, nil
}

// parseNullStringUUID safely parses a UUID from sql.NullString.
func parseNullStringUUID(ns sql.NullString) uuid.UUID {
	if ns.Valid {
		id, _ := uuid.Parse(ns.String)
		return id
	}
	return uuid.Nil
}
