package store

import (
	"context"
	"database/sql"
	"errors"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type EventTypesStore struct {
	db *sql.DB
}

func (s *EventTypesStore) GetAll(ctx context.Context) ([]models.EventType, error) {
	query := `
		SELECT id, name, COALESCE(description, ''), suggested_budget_min, suggested_budget_max,
		       default_guest_count, color, icon, is_active, created_at, updated_at
		FROM event_types
		WHERE is_active = true
		ORDER BY name`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var types []models.EventType
	for rows.Next() {
		var t models.EventType
		var desc sql.NullString
		var budgetMin, budgetMax sql.NullFloat64
		if err := rows.Scan(
			&t.ID, &t.Name, &desc, &budgetMin, &budgetMax,
			&t.DefaultGuestCount, &t.Color, &t.Icon, &t.IsActive, &t.CreatedAt, &t.UpdatedAt,
		); err != nil {
			return nil, err
		}
		t.Description = desc.String
		if budgetMin.Valid {
			t.SuggestedBudgetMin = &budgetMin.Float64
		}
		if budgetMax.Valid {
			t.SuggestedBudgetMax = &budgetMax.Float64
		}
		types = append(types, t)
	}
	return types, nil
}

func (s *EventTypesStore) GetByID(ctx context.Context, id uuid.UUID) (*models.EventType, error) {
	query := `
		SELECT id, name, COALESCE(description, ''), suggested_budget_min, suggested_budget_max,
		       default_guest_count, color, icon, is_active, created_at, updated_at
		FROM event_types WHERE id = $1`

	var t models.EventType
	var desc sql.NullString
	var budgetMin, budgetMax sql.NullFloat64
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&t.ID, &t.Name, &desc, &budgetMin, &budgetMax,
		&t.DefaultGuestCount, &t.Color, &t.Icon, &t.IsActive, &t.CreatedAt, &t.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}
	t.Description = desc.String
	if budgetMin.Valid {
		t.SuggestedBudgetMin = &budgetMin.Float64
	}
	if budgetMax.Valid {
		t.SuggestedBudgetMax = &budgetMax.Float64
	}

	items, _ := s.GetItemsByType(ctx, id)
	t.Items = items
	return &t, nil
}

func (s *EventTypesStore) Create(ctx context.Context, t *models.EventType) error {
	query := `
		INSERT INTO event_types (id, name, description, suggested_budget_min, suggested_budget_max,
		                         default_guest_count, color, icon, is_active)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, true)
		RETURNING created_at, updated_at`

	if t.ID == uuid.Nil {
		t.ID = uuid.New()
	}
	return s.db.QueryRowContext(ctx, query,
		t.ID, t.Name, t.Description, t.SuggestedBudgetMin, t.SuggestedBudgetMax,
		t.DefaultGuestCount, t.Color, t.Icon,
	).Scan(&t.CreatedAt, &t.UpdatedAt)
}

func (s *EventTypesStore) Update(ctx context.Context, t *models.EventType) error {
	query := `
		UPDATE event_types
		SET name = $2, description = $3, suggested_budget_min = $4, suggested_budget_max = $5,
		    default_guest_count = $6, color = $7, icon = $8, updated_at = NOW()
		WHERE id = $1`
	_, err := s.db.ExecContext(ctx, query,
		t.ID, t.Name, t.Description, t.SuggestedBudgetMin, t.SuggestedBudgetMax,
		t.DefaultGuestCount, t.Color, t.Icon,
	)
	return err
}

func (s *EventTypesStore) Delete(ctx context.Context, id uuid.UUID) error {
	_, err := s.db.ExecContext(ctx, `DELETE FROM event_types WHERE id = $1`, id)
	return err
}

func (s *EventTypesStore) GetItemsByType(ctx context.Context, eventTypeID uuid.UUID) ([]models.EventTypeItem, error) {
	query := `
		SELECT eti.id, eti.event_type_id, eti.article_id, eti.category_id, eti.quantity, eti.sort_order,
		       a.name_template
		FROM event_type_items eti
		JOIN articles a ON eti.article_id = a.id
		WHERE eti.event_type_id = $1
		ORDER BY eti.sort_order`

	rows, err := s.db.QueryContext(ctx, query, eventTypeID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []models.EventTypeItem
	for rows.Next() {
		var item models.EventTypeItem
		var catID sql.NullString
		var articleName sql.NullString
		if err := rows.Scan(
			&item.ID, &item.EventTypeID, &item.ArticleID, &catID, &item.Quantity, &item.SortOrder, &articleName,
		); err != nil {
			return nil, err
		}
		if catID.Valid {
			id, _ := uuid.Parse(catID.String)
			item.CategoryID = &id
		}
		items = append(items, item)
	}
	return items, nil
}

func (s *EventTypesStore) SetItems(ctx context.Context, eventTypeID uuid.UUID, items []models.EventTypeItem) error {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.ExecContext(ctx, `DELETE FROM event_type_items WHERE event_type_id = $1`, eventTypeID)
	if err != nil {
		return err
	}

	for _, item := range items {
		if item.ID == uuid.Nil {
			item.ID = uuid.New()
		}
		_, err = tx.ExecContext(ctx,
			`INSERT INTO event_type_items (id, event_type_id, article_id, category_id, quantity, sort_order)
			 VALUES ($1, $2, $3, $4, $5, $6)
			 ON CONFLICT (event_type_id, article_id) DO UPDATE SET quantity = $5, sort_order = $6`,
			item.ID, eventTypeID, item.ArticleID, item.CategoryID, item.Quantity, item.SortOrder,
		)
		if err != nil {
			return err
		}
	}
	return tx.Commit()
}
