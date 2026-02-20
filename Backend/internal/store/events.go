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

func (s *EventStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Event, error) {
	query := `
		SELECT id, user_id, name, date, location, guest_count, budget, status, additional_costs, admin_notes, 
		       payment_status, payment_method, paid_at, created_at, updated_at
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
		       payment_status, payment_method, paid_at, created_at, updated_at
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
		       payment_status, payment_method, paid_at, created_at, updated_at
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

func (s *EventStore) AddItem(ctx context.Context, item *models.EventItem) error {
	query := `
		INSERT INTO event_items (event_id, article_id, quantity)
		VALUES ($1, $2, $3)
		RETURNING id, created_at, updated_at
	`

	err := s.db.QueryRowContext(
		ctx,
		query,
		item.EventID,
		item.ArticleID,
		item.Quantity,
	).Scan(
		&item.ID,
		&item.CreatedAt,
		&item.UpdatedAt,
	)
	if err != nil {
		return err
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
	query := `
		SELECT ei.id, ei.event_id, ei.article_id, ei.quantity, ei.created_at, ei.updated_at,
		       a.id, a.name_template, a.description_template, a.category_id, a.is_active, a.type,
               (SELECT v.rental_price FROM article_variants v WHERE v.article_id = a.id LIMIT 1) as price
		FROM event_items ei
		JOIN articles a ON ei.article_id = a.id
		WHERE ei.event_id = $1
		ORDER BY ei.created_at DESC
	`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []models.EventItem
	for rows.Next() {
		var item models.EventItem
		item.Article = &models.Article{}
		err := rows.Scan(
			&item.ID,
			&item.EventID,
			&item.ArticleID,
			&item.Quantity,
			&item.CreatedAt,
			&item.UpdatedAt,
			&item.Article.ID,
			&item.Article.NameTemplate,
			&item.Article.DescriptionTemplate,
			&item.Article.CategoryID,
			&item.Article.IsActive,
			&item.Article.Type,
			&item.Price,
		)
		if err != nil {
			return nil, err
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
