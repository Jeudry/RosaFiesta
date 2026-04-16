package store

import (
	"context"
	"database/sql"
	"errors"
	"strconv"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type RecurringEventsStore struct {
	db *sql.DB
}

func (s *RecurringEventsStore) GetAll(ctx context.Context) ([]models.RecurringEvent, error) {
	query := `
		SELECT id, user_id, name, COALESCE(location, ''), guest_count, budget, frequency,
		       interval_value, days_of_week, start_date, end_date, next_run_date,
		       last_run_event_id, auto_create, is_active, created_at, updated_at
		FROM recurring_events
		WHERE is_active = true
		ORDER BY created_at DESC`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	return s.scanRecurringEvents(rows)
}

func (s *RecurringEventsStore) GetByUserID(ctx context.Context, userID uuid.UUID) ([]models.RecurringEvent, error) {
	query := `
		SELECT id, user_id, name, COALESCE(location, ''), guest_count, budget, frequency,
		       interval_value, days_of_week, start_date, end_date, next_run_date,
		       last_run_event_id, auto_create, is_active, created_at, updated_at
		FROM recurring_events
		WHERE user_id = $1 AND is_active = true
		ORDER BY created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	return s.scanRecurringEvents(rows)
}

func (s *RecurringEventsStore) GetByID(ctx context.Context, id uuid.UUID) (*models.RecurringEvent, error) {
	query := `
		SELECT id, user_id, name, COALESCE(location, ''), guest_count, budget, frequency,
		       interval_value, days_of_week, start_date, end_date, next_run_date,
		       last_run_event_id, auto_create, is_active, created_at, updated_at
		FROM recurring_events WHERE id = $1`

	var r models.RecurringEvent
	var location sql.NullString
	var endDate, lastRunEventID sql.NullString
	var daysOfWeek []byte

	err := s.db.QueryRowContext(ctx, query, id).Scan(
		&r.ID, &r.UserID, &r.Name, &location, &r.GuestCount, &r.Budget, &r.Frequency,
		&r.IntervalValue, &daysOfWeek, &r.StartDate, &endDate, &r.NextRunDate,
		&lastRunEventID, &r.AutoCreate, &r.IsActive, &r.CreatedAt, &r.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}
	r.Location = location.String
	if endDate.Valid {
		t, _ := time.Parse("2006-01-02", endDate.String)
		r.EndDate = &t
	}
	if lastRunEventID.Valid {
		id, _ := uuid.Parse(lastRunEventID.String)
		r.LastRunEventID = &id
	}
	return &r, nil
}

func (s *RecurringEventsStore) Create(ctx context.Context, r *models.RecurringEvent) error {
	query := `
		INSERT INTO recurring_events (id, user_id, name, location, guest_count, budget, frequency,
		                               interval_value, days_of_week, start_date, end_date,
		                               next_run_date, auto_create, is_active)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, true)
		RETURNING created_at, updated_at`

	if r.ID == uuid.Nil {
		r.ID = uuid.New()
	}
	return s.db.QueryRowContext(ctx, query,
		r.ID, r.UserID, r.Name, r.Location, r.GuestCount, r.Budget, r.Frequency,
		r.IntervalValue, "{"+intSliceToString(r.DaysOfWeek)+"}", r.StartDate, r.EndDate,
		r.NextRunDate, r.AutoCreate,
	).Scan(&r.CreatedAt, &r.UpdatedAt)
}

func (s *RecurringEventsStore) Update(ctx context.Context, r *models.RecurringEvent) error {
	query := `
		UPDATE recurring_events
		SET name = $2, location = $3, guest_count = $4, budget = $5, frequency = $6,
		    interval_value = $7, days_of_week = $8, start_date = $9, end_date = $10,
		    next_run_date = $11, auto_create = $12, is_active = $13, updated_at = NOW()
		WHERE id = $1`
	_, err := s.db.ExecContext(ctx, query,
		r.ID, r.Name, r.Location, r.GuestCount, r.Budget, r.Frequency,
		r.IntervalValue, "{"+intSliceToString(r.DaysOfWeek)+"}", r.StartDate, r.EndDate,
		r.NextRunDate, r.AutoCreate, r.IsActive,
	)
	return err
}

func (s *RecurringEventsStore) Delete(ctx context.Context, id uuid.UUID) error {
	_, err := s.db.ExecContext(ctx, `UPDATE recurring_events SET is_active = false WHERE id = $1`, id)
	return err
}

func (s *RecurringEventsStore) GetGeneratedEvents(ctx context.Context, recurringID uuid.UUID) ([]models.Event, error) {
	query := `
		SELECT e.id, e.user_id, e.name, COALESCE(e.location, ''), e.guest_count, e.budget,
		       e.additional_costs, COALESCE(e.admin_notes, ''), e.status, e.payment_status,
		       e.payment_method, e.paid_at, e.quote_approved_at, e.quote_approved_by,
		       e.quote_rejected_at, e.quote_rejected_by, e.deposit_paid, e.deposit_amount,
		       e.deposit_paid_at, e.remaining_amount, e.installment_due_date, e.total_quote,
		       e.created_at, e.updated_at
		FROM events e
		WHERE e.id IN (
			SELECT last_run_event_id FROM recurring_events WHERE id = $1
			UNION
			SELECT UNNEST(string_to_array(replace(last_run_event_id::text, '}', ''), ','))::uuid
			FROM recurring_events WHERE id = $1
		)
		ORDER BY e.date DESC`

	rows, err := s.db.QueryContext(ctx, query, recurringID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var events []models.Event
	for rows.Next() {
		var e models.Event
		var location, adminNotes, paymentMethod sql.NullString
		var paidAt, quoteApprovedAt, quoteRejectedAt, quoteRejectedBy, depositPaidAt, installmentDueDate sql.NullTime
		var quoteApprovedBy, depositPaid sql.NullString
		var additionalCosts sql.NullFloat64

		if err := rows.Scan(
			&e.ID, &e.UserID, &e.Name, &location, &e.GuestCount, &e.Budget,
			&additionalCosts, &adminNotes, &e.Status, &e.PaymentStatus,
			&paymentMethod, &paidAt, &quoteApprovedAt, &quoteApprovedBy,
			&quoteRejectedAt, &quoteRejectedBy, &depositPaid, &e.DepositAmount,
			&depositPaidAt, &e.RemainingAmount, &installmentDueDate, &e.TotalQuote,
			&e.CreatedAt, &e.UpdatedAt,
		); err != nil {
			return nil, err
		}
		e.Location = location.String
		e.AdminNotes = adminNotes.String
		if paymentMethod.Valid {
			e.PaymentMethod = &paymentMethod.String
		}
		events = append(events, e)
	}
	return events, nil
}

func (s *RecurringEventsStore) UpdateLastRun(ctx context.Context, recurringID, eventID uuid.UUID, nextRun time.Time) error {
	_, err := s.db.ExecContext(ctx,
		`UPDATE recurring_events SET last_run_event_id = $2, next_run_date = $3 WHERE id = $1`,
		recurringID, eventID, nextRun,
	)
	return err
}

func (s *RecurringEventsStore) scanRecurringEvents(rows *sql.Rows) ([]models.RecurringEvent, error) {
	var recurring []models.RecurringEvent
	for rows.Next() {
		var r models.RecurringEvent
		var location sql.NullString
		var endDate, lastRunEventID sql.NullString
		var daysOfWeek []byte

		if err := rows.Scan(
			&r.ID, &r.UserID, &r.Name, &location, &r.GuestCount, &r.Budget, &r.Frequency,
			&r.IntervalValue, &daysOfWeek, &r.StartDate, &endDate, &r.NextRunDate,
			&lastRunEventID, &r.AutoCreate, &r.IsActive, &r.CreatedAt, &r.UpdatedAt,
		); err != nil {
			return nil, err
		}
		r.Location = location.String
		if endDate.Valid {
			t, _ := time.Parse("2006-01-02", endDate.String)
			r.EndDate = &t
		}
		if lastRunEventID.Valid {
			id, _ := uuid.Parse(lastRunEventID.String)
			r.LastRunEventID = &id
		}
		recurring = append(recurring, r)
	}
	return recurring, nil
}

func intSliceToString(arr []int) string {
	if len(arr) == 0 {
		return ""
	}
	result := ""
	for i, v := range arr {
		if i > 0 {
			result += ","
		}
		result += strconv.Itoa(v)
	}
	return result
}
