package store

import (
	"context"
	"database/sql"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type FavoritesStore struct {
	db *sql.DB
}

// List returns all articles favorited by the given user, joined with their
// primary variant so the favorites screen can render cards without N+1.
func (s *FavoritesStore) List(ctx context.Context, userID uuid.UUID) ([]models.Article, error) {
	query := `
		SELECT
			a.id, a.name_template, a.description_template, COALESCE(a.type, ''),
			a.category_id, a.is_active, a.stock_quantity,
			a.created, a.updated, a.created_by, a.updated_by,
			v.id, v.sku, v.name, v.description, v.image_url, v.is_active,
			v.stock, v.rental_price, v.sale_price, v.replacement_cost
		FROM favorites f
		JOIN articles a ON a.id = f.article_id
		LEFT JOIN LATERAL (
			SELECT id, sku, name, description, image_url, is_active,
			       stock, rental_price, sale_price, replacement_cost
			FROM article_variants
			WHERE article_id = a.id
			ORDER BY created_at ASC
			LIMIT 1
		) v ON true
		WHERE f.user_id = $1
		ORDER BY f.created DESC`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	articles := make([]models.Article, 0)
	for rows.Next() {
		var article models.Article
		var (
			vID              sql.NullString
			vSku             sql.NullString
			vName            sql.NullString
			vDescription     sql.NullString
			vImageURL        sql.NullString
			vIsActive        sql.NullBool
			vStock           sql.NullInt32
			vRentalPrice     sql.NullFloat64
			vSalePrice       sql.NullFloat64
			vReplacementCost sql.NullFloat64
		)
		if err := rows.Scan(
			&article.ID, &article.NameTemplate, &article.DescriptionTemplate, &article.Type,
			&article.CategoryID, &article.IsActive, &article.StockQuantity,
			&article.Created, &article.Updated, &article.CreatedBy, &article.UpdatedBy,
			&vID, &vSku, &vName, &vDescription, &vImageURL, &vIsActive,
			&vStock, &vRentalPrice, &vSalePrice, &vReplacementCost,
		); err != nil {
			return nil, err
		}

		if vID.Valid {
			variantID, _ := uuid.Parse(vID.String)
			variant := models.ArticleVariant{
				ID:          variantID,
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

		articles = append(articles, article)
	}

	return articles, nil
}

// Add inserts a favorite. Idempotent: duplicates are silently ignored thanks
// to the (user_id, article_id) UNIQUE constraint.
func (s *FavoritesStore) Add(ctx context.Context, userID, articleID uuid.UUID) error {
	query := `
		INSERT INTO favorites (user_id, article_id)
		VALUES ($1, $2)
		ON CONFLICT (user_id, article_id) DO NOTHING`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, userID, articleID)
	return err
}

// Remove deletes a favorite. No-op if it doesn't exist.
func (s *FavoritesStore) Remove(ctx context.Context, userID, articleID uuid.UUID) error {
	query := `DELETE FROM favorites WHERE user_id = $1 AND article_id = $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, userID, articleID)
	return err
}

// IsFavorite reports whether the given article is favorited by the user.
func (s *FavoritesStore) IsFavorite(ctx context.Context, userID, articleID uuid.UUID) (bool, error) {
	query := `SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = $1 AND article_id = $2)`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var exists bool
	if err := s.db.QueryRowContext(ctx, query, userID, articleID).Scan(&exists); err != nil {
		return false, err
	}
	return exists, nil
}
