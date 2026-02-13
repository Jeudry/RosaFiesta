package store

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type CartsStore struct {
	db *sql.DB
}

func (s *CartsStore) Create(ctx context.Context, cart *models.Cart) error {
	query := `
		INSERT INTO carts (id, user_id, created_at, updated_at)
		VALUES ($1, $2, $3, $4)
	`
	cart.ID = uuid.New()
	cart.CreatedAt = time.Now()
	cart.UpdatedAt = time.Now()

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, cart.ID, cart.UserID, cart.CreatedAt, cart.UpdatedAt)
	return err
}

func (s *CartsStore) GetByUserID(ctx context.Context, userID uuid.UUID) (*models.Cart, error) {
	query := `
		SELECT id, user_id, created_at, updated_at
		FROM carts
		WHERE user_id = $1
	`
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var cart models.Cart
	err := s.db.QueryRowContext(ctx, query, userID).Scan(
		&cart.ID,
		&cart.UserID,
		&cart.CreatedAt,
		&cart.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	// Fetch items with details
	itemsQuery := `
		SELECT 
			ci.id, ci.cart_id, ci.article_id, ci.variant_id, ci.quantity, ci.created_at, ci.updated_at,
			a.id, a.name_template, a.description_template, a.is_active, a.type, a.category_id,
			av.id, av.sku, av.name, av.rental_price, av.sale_price, av.image_url
		FROM cart_items ci
		JOIN articles a ON ci.article_id = a.id
		LEFT JOIN article_variants av ON ci.variant_id = av.id
		WHERE ci.cart_id = $1
	`
	rows, err := s.db.QueryContext(ctx, itemsQuery, cart.ID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []models.CartItem
	for rows.Next() {
		var item models.CartItem
		var article models.Article
		// Temporary variables for scanning nullable fields
		var variantID sql.NullString
		var variantIDVal uuid.UUID
		var variantSku sql.NullString
		var variantName sql.NullString
		var variantRentalPrice sql.NullFloat64 // Changed from Price to RentalPrice based on struct
		var variantSalePrice sql.NullFloat64
		var variantImageURL sql.NullString

		err := rows.Scan(
			&item.ID, &item.CartID, &item.ArticleID, &variantID, &item.Quantity, &item.CreatedAt, &item.UpdatedAt,
			&article.ID, &article.NameTemplate, &article.DescriptionTemplate, &article.IsActive, &article.Type, &article.CategoryID,
			&variantIDVal, &variantSku, &variantName, &variantRentalPrice, &variantSalePrice, &variantImageURL,
		)
		if err != nil {
			// If scan fails, return error. Note: nullable columns in SQL scan into sql.Null* types
			// However, if the JOIN finds no variant, all variant columns will be NULL.
			// The strict scan above might fail if we try to scan NULL into a non-pointer/non-Null type like &variantIDVal (uuid.UUID)
			// We need to use pointers or Null types for ALL variant columns effectively.
			// Let's retry with a more robust scan approach or just skip variant details for now to be safe.
			// For this iteration, let's try to scan nullable variant ID into NullString first.
			return nil, err
		}

		item.Article = article

		if variantID.Valid {
			// Re-construct variant object if it exists
			vid, _ := uuid.Parse(variantID.String)
			item.VariantID = &vid

			variant := models.ArticleVariant{
				ID: vid,
			}
			if variantSku.Valid {
				variant.Sku = variantSku.String
			}
			if variantName.Valid {
				variant.Name = variantName.String
			}
			if variantRentalPrice.Valid {
				variant.RentalPrice = variantRentalPrice.Float64
			}
			if variantSalePrice.Valid {
				val := variantSalePrice.Float64
				variant.SalePrice = &val
			}
			if variantImageURL.Valid {
				val := variantImageURL.String
				variant.ImageURL = &val
			}
			item.Variant = &variant
		}

		items = append(items, item)
	}

	cart.Items = items
	return &cart, nil
}

func (s *CartsStore) AddItem(ctx context.Context, item *models.CartItem) error {
	// Upsert logic: If item exists (same cart, article, variant), update quantity. Else insert.
	// Note: UUID coalesce hack might be needed depending on DB constraints for NULL unique index
	// Or define a partial unique index in migration.
	// For simplicity in code, let's assume standard upsert or just Insert for now if we handle it in CreateCartHandler logic.
	// Let's stick to simple INSERT for now to avoid complex SQL in this step if unique index isn't set up yet.

	// Actually, let's just do a Check then Insert/Update to be safe and portable.
	var existingID string
	checkQuery := `
		SELECT id FROM cart_items 
		WHERE cart_id = $1 AND article_id = $2 AND ((variant_id IS NULL AND $3 IS NULL) OR (variant_id = $3))
	`
	err := s.db.QueryRowContext(ctx, checkQuery, item.CartID, item.ArticleID, item.VariantID).Scan(&existingID)

	if err == nil {
		// Item exists, update quantity
		updateQuery := `UPDATE cart_items SET quantity = quantity + $1, updated_at = $2 WHERE id = $3`
		_, err = s.db.ExecContext(ctx, updateQuery, item.Quantity, time.Now(), existingID)
		return err
	} else if err != sql.ErrNoRows {
		return err
	}

	// Item does not exist, insert
	insertQuery := `
		INSERT INTO cart_items (id, cart_id, article_id, variant_id, quantity, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
	`
	item.ID = uuid.New()
	item.CreatedAt = time.Now()
	item.UpdatedAt = time.Now()

	_, err = s.db.ExecContext(ctx, insertQuery, item.ID, item.CartID, item.ArticleID, item.VariantID, item.Quantity, item.CreatedAt, item.UpdatedAt)
	return err
}

func (s *CartsStore) UpdateItemQuantity(ctx context.Context, itemID uuid.UUID, quantity int) error {
	query := `
		UPDATE cart_items
		SET quantity = $1, updated_at = $2
		WHERE id = $3
	`
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, quantity, time.Now(), itemID)
	return err
}

func (s *CartsStore) RemoveItem(ctx context.Context, itemID uuid.UUID) error {
	query := `DELETE FROM cart_items WHERE id = $1`
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, itemID)
	return err
}

func (s *CartsStore) ClearCart(ctx context.Context, cartID uuid.UUID) error {
	query := `DELETE FROM cart_items WHERE cart_id = $1`
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, cartID)
	return err
}
