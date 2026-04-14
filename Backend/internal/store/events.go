package store

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type EventStore struct {
	db *sql.DB
}

func (s *EventStore) Create(ctx context.Context, event *models.Event) error {
	if event.Status == "" {
		event.Status = models.EventStatusDraft
	}

	query := `
		INSERT INTO events (user_id, name, date, location, guest_count, budget, status, additional_costs, admin_notes, payment_status, payment_method, paid_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
		RETURNING id, created_at, updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		event.UserID,
		event.Name,
		event.Date,
		event.Location,
		event.GuestCount,
		event.Budget,
		event.Status,
		event.AdditionalCosts,
		event.AdminNotes,
		event.PaymentStatus,
		event.PaymentMethod,
		event.PaidAt,
	).Scan(
		&event.ID,
		&event.CreatedAt,
		&event.UpdatedAt,
	)
	if err != nil {
		return err
	}

	return nil
}

// GetOrCreateDraft returns the user's current draft event. If they don't
// have one yet, an empty draft is created and returned.
//
// A user is guaranteed to have at most one draft at a time thanks to the
// `events_user_active_draft` partial unique index. The race between two
// concurrent requests is resolved by ON CONFLICT — both end up returning
// the same row.
func (s *EventStore) GetOrCreateDraft(ctx context.Context, userID uuid.UUID) (*models.Event, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	// Try to find an existing draft first.
	const selectQuery = `
		SELECT id, user_id, name, date, location, guest_count, budget, status,
		       additional_costs, admin_notes, payment_status, payment_method,
		       paid_at,
		       quote_approved_at, quote_approved_by, quote_rejected_at, quote_rejected_by,
		       created_at, updated_at
		FROM events
		WHERE user_id = $1 AND status = 'draft'
		LIMIT 1
	`

	scan := func(row *sql.Row, ev *models.Event) error {
		return row.Scan(
			&ev.ID, &ev.UserID, &ev.Name, &ev.Date, &ev.Location,
			&ev.GuestCount, &ev.Budget, &ev.Status,
			&ev.AdditionalCosts, &ev.AdminNotes,
			&ev.PaymentStatus, &ev.PaymentMethod, &ev.PaidAt,
			&ev.QuoteApprovedAt, &ev.QuoteApprovedBy, &ev.QuoteRejectedAt, &ev.QuoteRejectedBy,
			&ev.CreatedAt, &ev.UpdatedAt,
		)
	}

	var event models.Event
	err := scan(s.db.QueryRowContext(ctx, selectQuery, userID), &event)
	if err == nil {
		return &event, nil
	}
	if !errors.Is(err, sql.ErrNoRows) {
		return nil, err
	}

	// No draft yet — create one. The partial unique index ensures that if
	// a concurrent request beats us to it we just read whatever ended up
	// being inserted. Empty defaults are explicit so the model's non-nullable
	// string fields scan cleanly when we re-read the row.
	const insertQuery = `
		INSERT INTO events (user_id, name, location, status, admin_notes, payment_status)
		VALUES ($1, '', '', 'draft', '', '')
		ON CONFLICT (user_id) WHERE status = 'draft' DO NOTHING
	`
	if _, err := s.db.ExecContext(ctx, insertQuery, userID); err != nil {
		return nil, err
	}

	// Re-read whatever draft now exists for this user.
	if err := scan(s.db.QueryRowContext(ctx, selectQuery, userID), &event); err != nil {
		return nil, err
	}
	return &event, nil
}

func (s *EventStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Event, error) {
	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, additional_costs, admin_notes,
		       payment_status, payment_method, paid_at,
		       quote_approved_at, quote_approved_by, quote_rejected_at, quote_rejected_by,
		       created_at, updated_at
		FROM events
		WHERE id = $1
	`

	var event models.Event
	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&event.ID,
		&event.UserID,
		&event.Name,
		&event.Date,
		&event.Location,
		&event.GuestCount,
		&event.Budget,
		&event.Status,
		&event.AdditionalCosts,
		&event.AdminNotes,
		&event.PaymentStatus,
		&event.PaymentMethod,
		&event.PaidAt,
		&event.QuoteApprovedAt,
		&event.QuoteApprovedBy,
		&event.QuoteRejectedAt,
		&event.QuoteRejectedBy,
		&event.CreatedAt,
		&event.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	return &event, nil
}

func (s *EventStore) GetByUserID(ctx context.Context, userID uuid.UUID) ([]models.Event, error) {
	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, additional_costs, admin_notes,
		       payment_status, payment_method, paid_at,
		       quote_approved_at, quote_approved_by, quote_rejected_at, quote_rejected_by,
		       created_at, updated_at
		FROM events
		WHERE user_id = $1
		ORDER BY date ASC
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var events []models.Event
	for rows.Next() {
		var event models.Event
		err := rows.Scan(
			&event.ID,
			&event.UserID,
			&event.Name,
			&event.Date,
			&event.Location,
			&event.GuestCount,
			&event.Budget,
			&event.Status,
			&event.AdditionalCosts,
			&event.AdminNotes,
			&event.PaymentStatus,
			&event.PaymentMethod,
			&event.PaidAt,
			&event.QuoteApprovedAt,
			&event.QuoteApprovedBy,
			&event.QuoteRejectedAt,
			&event.QuoteRejectedBy,
			&event.CreatedAt,
			&event.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		events = append(events, event)
	}

	return events, nil
}

// GetPendingByUserID returns events that are not completed or cancelled.
func (s *EventStore) GetPendingByUserID(ctx context.Context, userID uuid.UUID) ([]models.Event, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, additional_costs, admin_notes,
		       payment_status, payment_method, paid_at,
		       quote_approved_at, quote_approved_by, quote_rejected_at, quote_rejected_by,
		       created_at, updated_at
		FROM events
		WHERE user_id = $1 AND status NOT IN ('completed', 'cancelled')
		ORDER BY date ASC
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var events []models.Event
	for rows.Next() {
		var event models.Event
		err := rows.Scan(
			&event.ID,
			&event.UserID,
			&event.Name,
			&event.Date,
			&event.Location,
			&event.GuestCount,
			&event.Budget,
			&event.Status,
			&event.AdditionalCosts,
			&event.AdminNotes,
			&event.PaymentStatus,
			&event.PaymentMethod,
			&event.PaidAt,
			&event.QuoteApprovedAt,
			&event.QuoteApprovedBy,
			&event.QuoteRejectedAt,
			&event.QuoteRejectedBy,
			&event.CreatedAt,
			&event.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		events = append(events, event)
	}

	return events, nil
}

func (s *EventStore) GetAll(ctx context.Context) ([]models.Event, error) {
	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, additional_costs, admin_notes,
		       payment_status, payment_method, paid_at,
		       quote_approved_at, quote_approved_by, quote_rejected_at, quote_rejected_by,
		       created_at, updated_at
		FROM events
		ORDER BY date ASC
	`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var events []models.Event
	for rows.Next() {
		var event models.Event
		err := rows.Scan(
			&event.ID,
			&event.UserID,
			&event.Name,
			&event.Date,
			&event.Location,
			&event.GuestCount,
			&event.Budget,
			&event.Status,
			&event.AdditionalCosts,
			&event.AdminNotes,
			&event.PaymentStatus,
			&event.PaymentMethod,
			&event.PaidAt,
			&event.QuoteApprovedAt,
			&event.QuoteApprovedBy,
			&event.QuoteRejectedAt,
			&event.QuoteRejectedBy,
			&event.CreatedAt,
			&event.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}
		events = append(events, event)
	}

	return events, nil
}

func (s *EventStore) Update(ctx context.Context, event *models.Event) error {
	query := `
		UPDATE events
		SET name = $1, date = $2, location = $3, guest_count = $4, budget = $5, status = $6, 
		    additional_costs = $7, admin_notes = $8, payment_status = $9, payment_method = $10, paid_at = $11, updated_at = NOW()
		WHERE id = $12
		RETURNING updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		event.Name,
		event.Date,
		event.Location,
		event.GuestCount,
		event.Budget,
		event.Status,
		event.AdditionalCosts,
		event.AdminNotes,
		event.PaymentStatus,
		event.PaymentMethod,
		event.PaidAt,
		event.ID,
	).Scan(&event.UpdatedAt)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return ErrNotFound
		}
		return err
	}

	return nil
}

func (s *EventStore) Delete(ctx context.Context, id uuid.UUID) error {
	query := `DELETE FROM events WHERE id = $1`

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

// AddItem upserts an event item. If a line for the same (event, article,
// variant) tuple already exists, the quantity is incremented; otherwise
// a new row is inserted with the captured price snapshot.
func (s *EventStore) AddItem(ctx context.Context, item *models.EventItem) error {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	// Try to find an existing line for this (event, article, variant).
	const checkQuery = `
		SELECT id, quantity FROM event_items
		WHERE event_id = $1
		  AND article_id = $2
		  AND ((variant_id IS NULL AND $3::UUID IS NULL) OR variant_id = $3::UUID)
	`
	var existingID uuid.UUID
	var existingQty int
	err := s.db.QueryRowContext(ctx, checkQuery, item.EventID, item.ArticleID, item.VariantID).
		Scan(&existingID, &existingQty)

	if err == nil {
		// Increment the existing line's quantity.
		const updateQuery = `
			UPDATE event_items
			SET quantity = quantity + $1, updated_at = NOW()
			WHERE id = $2
			RETURNING id, quantity, created_at, updated_at
		`
		return s.db.QueryRowContext(ctx, updateQuery, item.Quantity, existingID).
			Scan(&item.ID, &item.Quantity, &item.CreatedAt, &item.UpdatedAt)
	}
	if !errors.Is(err, sql.ErrNoRows) {
		return err
	}

	// New line — insert.
	const insertQuery = `
		INSERT INTO event_items (event_id, article_id, variant_id, quantity, price_snapshot)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id, created_at, updated_at
	`
	return s.db.QueryRowContext(
		ctx,
		insertQuery,
		item.EventID,
		item.ArticleID,
		item.VariantID,
		item.Quantity,
		item.PriceSnapshot,
	).Scan(&item.ID, &item.CreatedAt, &item.UpdatedAt)
}

// UpdateItemQuantity sets the absolute quantity for a given event item.
func (s *EventStore) UpdateItemQuantity(ctx context.Context, itemID uuid.UUID, quantity int) error {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	const query = `
		UPDATE event_items
		SET quantity = $1, updated_at = NOW()
		WHERE id = $2
	`
	res, err := s.db.ExecContext(ctx, query, quantity, itemID)
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

func (s *EventStore) RemoveItem(ctx context.Context, eventID, startID uuid.UUID) error {
	query := `DELETE FROM event_items WHERE id = $1 AND event_id = $2`

	res, err := s.db.ExecContext(ctx, query, startID, eventID)
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

func (s *EventStore) GetItems(ctx context.Context, eventID uuid.UUID) ([]models.EventItem, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	const query = `
		SELECT ei.id, ei.event_id, ei.article_id, ei.variant_id, ei.quantity,
		       ei.price_snapshot, ei.created_at, ei.updated_at,
		       a.id, a.name_template, a.description_template, a.category_id,
		       a.is_active, COALESCE(a.type, ''),
		       v.id, v.sku, v.name, v.image_url, v.rental_price, v.sale_price, v.stock,
		       COALESCE(
		           ei.price_snapshot,
		           v.rental_price,
		           (SELECT v2.rental_price FROM article_variants v2
		            WHERE v2.article_id = a.id ORDER BY v2.created_at ASC LIMIT 1)
		       ) AS effective_price
		FROM event_items ei
		JOIN articles a ON ei.article_id = a.id
		LEFT JOIN article_variants v ON ei.variant_id = v.id
		WHERE ei.event_id = $1
		ORDER BY ei.created_at DESC
	`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]models.EventItem, 0)
	for rows.Next() {
		var item models.EventItem
		item.Article = &models.Article{}

		var (
			variantID        sql.NullString
			variantSku       sql.NullString
			variantName      sql.NullString
			variantImage     sql.NullString
			variantRental    sql.NullFloat64
			variantSale      sql.NullFloat64
			variantStock     sql.NullInt64
			effectivePrice   sql.NullFloat64
		)

		if err := rows.Scan(
			&item.ID,
			&item.EventID,
			&item.ArticleID,
			&item.VariantID,
			&item.Quantity,
			&item.PriceSnapshot,
			&item.CreatedAt,
			&item.UpdatedAt,
			&item.Article.ID,
			&item.Article.NameTemplate,
			&item.Article.DescriptionTemplate,
			&item.Article.CategoryID,
			&item.Article.IsActive,
			&item.Article.Type,
			&variantID, &variantSku, &variantName, &variantImage,
			&variantRental, &variantSale, &variantStock,
			&effectivePrice,
		); err != nil {
			return nil, err
		}

		if variantID.Valid {
			vid, _ := uuid.Parse(variantID.String)
			variant := models.ArticleVariant{
				ID:        vid,
				ArticleID: item.ArticleID,
				Sku:       variantSku.String,
				Name:      variantName.String,
			}
			if variantImage.Valid {
				img := variantImage.String
				variant.ImageURL = &img
			}
			if variantRental.Valid {
				variant.RentalPrice = variantRental.Float64
			}
			if variantSale.Valid {
				sp := variantSale.Float64
				variant.SalePrice = &sp
			}
			if variantStock.Valid {
				variant.Stock = int(variantStock.Int64)
			}
			item.Variant = &variant
		}

		if effectivePrice.Valid {
			price := effectivePrice.Float64
			item.Price = &price
		}

		items = append(items, item)
	}

	return items, nil
}

func (s *EventStore) GetDebrief(ctx context.Context, id uuid.UUID) (*models.EventDebrief, error) {
	// 1. Get Event and Budget
	event, err := s.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	items, err := s.GetItems(ctx, id)
	if err != nil {
		return nil, err
	}

	var totalSpent float64
	for _, item := range items {
		if item.Price != nil {
			totalSpent += *item.Price * float64(item.Quantity)
		}
	}

	// 2. Completion Stats and Punctuality
	var totalTasks, completedTasks int
	err = s.db.QueryRowContext(ctx, "SELECT COUNT(*), COUNT(*) FILTER (WHERE is_completed) FROM event_tasks WHERE event_id = $1", id).Scan(&totalTasks, &completedTasks)
	if err != nil {
		return nil, err
	}

	var totalTimeline, completedTimeline int
	err = s.db.QueryRowContext(ctx, "SELECT COUNT(*), COUNT(*) FILTER (WHERE is_completed) FROM event_timeline_items WHERE event_id = $1", id).Scan(&totalTimeline, &completedTimeline)
	if err != nil {
		return nil, err
	}

	// 3. Delayed Critical Items
	query := `
		SELECT title, start_time, completed_at
		FROM event_timeline_items
		WHERE event_id = $1 AND is_critical = TRUE AND is_completed = TRUE AND completed_at > start_time
	`
	rows, err := s.db.QueryContext(ctx, query, id)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var delayedItems []models.DelayedItemInfo
	var totalDelaysSeconds int64
	var criticalCount float64

	for rows.Next() {
		var info models.DelayedItemInfo
		var start, completed time.Time
		if err := rows.Scan(&info.Title, &start, &completed); err != nil {
			return nil, err
		}
		info.ExpectedTime = start
		info.ActualTime = completed
		info.Delay = completed.Sub(start)
		delayedItems = append(delayedItems, info)

		totalDelaysSeconds += int64(info.Delay.Seconds())
	}

	// Calculate a simple punctuality score (0-100)
	// Base 100, subtract 5 points per delayed critical item (example)
	err = s.db.QueryRowContext(ctx, "SELECT COUNT(*) FROM event_timeline_items WHERE event_id = $1 AND is_critical = TRUE", id).Scan(&criticalCount)
	punctualityScore := 100.0
	if criticalCount > 0 {
		punctualityScore = 100.0 - (float64(len(delayedItems)) / criticalCount * 100.0)
	}

	return &models.EventDebrief{
		PunctualityScore: punctualityScore,
		DelayedCritical:  delayedItems,
		BudgetAnalysis: models.BudgetSummary{
			EstimatedBudget: event.Budget,
			ActualSpent:     totalSpent,
			Difference:      event.Budget - totalSpent,
			IsOverBudget:    totalSpent > event.Budget,
		},
		CompletionStats: models.CompletionStats{
			TotalTasks:        totalTasks,
			CompletedTasks:    completedTasks,
			TotalTimeline:     totalTimeline,
			CompletedTimeline: completedTimeline,
		},
	}, nil
}

func (s *EventStore) ApproveQuote(ctx context.Context, eventID, userID uuid.UUID) error {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		UPDATE events
		SET status = $1, quote_approved_at = NOW(), quote_approved_by = $2, updated_at = NOW()
		WHERE id = $3
	`
	res, err := s.db.ExecContext(ctx, query, models.EventStatusPaid, userID, eventID)
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

func (s *EventStore) RejectQuote(ctx context.Context, eventID, userID uuid.UUID) error {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		UPDATE events
		SET status = $1, quote_rejected_at = NOW(), quote_rejected_by = $2, updated_at = NOW()
		WHERE id = $3
	`
	res, err := s.db.ExecContext(ctx, query, models.EventStatusRejected, userID, eventID)
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
