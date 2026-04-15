package store

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type InstallmentStore struct {
	db *sql.DB
}

func (s *InstallmentStore) CreateInstallmentPayment(ctx context.Context, eventID uuid.UUID, amount int, dueDate *time.Time) (*models.InstallmentPayment, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		INSERT INTO installment_payments (event_id, amount, due_date, payment_status)
		VALUES ($1, $2, $3, 'pending')
		RETURNING id, event_id, amount, payment_method, payment_status, due_date, paid_at, created_at
	`

	var payment models.InstallmentPayment
	var paymentMethod sql.NullString
	var dueDateVal sql.NullTime
	var paidAt sql.NullTime

	err := s.db.QueryRowContext(ctx, query, eventID, amount, dueDate).Scan(
		&payment.ID,
		&payment.EventID,
		&payment.Amount,
		&paymentMethod,
		&payment.PaymentStatus,
		&dueDateVal,
		&paidAt,
		&payment.CreatedAt,
	)
	if err != nil {
		return nil, err
	}

	if paymentMethod.Valid {
		payment.PaymentMethod = &paymentMethod.String
	}
	if dueDateVal.Valid {
		payment.DueDate = &dueDateVal.Time
	}
	if paidAt.Valid {
		payment.PaidAt = &paidAt.Time
	}

	return &payment, nil
}

func (s *InstallmentStore) GetInstallmentByEventID(ctx context.Context, eventID uuid.UUID) ([]models.InstallmentPayment, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		SELECT id, event_id, amount, payment_method, payment_status, due_date, paid_at, created_at
		FROM installment_payments
		WHERE event_id = $1
		ORDER BY created_at ASC
	`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var payments []models.InstallmentPayment
	for rows.Next() {
		var payment models.InstallmentPayment
		var paymentMethod sql.NullString
		var dueDate sql.NullTime
		var paidAt sql.NullTime

		if err := rows.Scan(
			&payment.ID,
			&payment.EventID,
			&payment.Amount,
			&paymentMethod,
			&payment.PaymentStatus,
			&dueDate,
			&paidAt,
			&payment.CreatedAt,
		); err != nil {
			return nil, err
		}

		if paymentMethod.Valid {
			payment.PaymentMethod = &paymentMethod.String
		}
		if dueDate.Valid {
			payment.DueDate = &dueDate.Time
		}
		if paidAt.Valid {
			payment.PaidAt = &paidAt.Time
		}

		payments = append(payments, payment)
	}

	return payments, nil
}

func (s *InstallmentStore) MarkPaid(ctx context.Context, paymentID uuid.UUID, paymentMethod string) error {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		UPDATE installment_payments
		SET payment_status = 'paid', payment_method = $1, paid_at = NOW()
		WHERE id = $2
	`

	res, err := s.db.ExecContext(ctx, query, paymentMethod, paymentID)
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

func (s *InstallmentStore) GetPendingInstallments(ctx context.Context) ([]models.InstallmentPayment, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		SELECT ip.id, ip.event_id, ip.amount, ip.payment_method, ip.payment_status, ip.due_date, ip.paid_at, ip.created_at
		FROM installment_payments ip
		JOIN events e ON ip.event_id = e.id
		WHERE ip.payment_status = 'pending'
		  AND ip.due_date IS NOT NULL
		  AND ip.due_date <= NOW() + INTERVAL '7 days'
		  AND e.status NOT IN ('completed', 'cancelled', 'paid')
		ORDER BY ip.due_date ASC
	`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var payments []models.InstallmentPayment
	for rows.Next() {
		var payment models.InstallmentPayment
		var paymentMethod sql.NullString
		var dueDate sql.NullTime
		var paidAt sql.NullTime

		if err := rows.Scan(
			&payment.ID,
			&payment.EventID,
			&payment.Amount,
			&paymentMethod,
			&payment.PaymentStatus,
			&dueDate,
			&paidAt,
			&payment.CreatedAt,
		); err != nil {
			return nil, err
		}

		if paymentMethod.Valid {
			payment.PaymentMethod = &paymentMethod.String
		}
		if dueDate.Valid {
			payment.DueDate = &dueDate.Time
		}
		if paidAt.Valid {
			payment.PaidAt = &paidAt.Time
		}

		payments = append(payments, payment)
	}

	return payments, nil
}

func (s *InstallmentStore) GetByID(ctx context.Context, paymentID uuid.UUID) (*models.InstallmentPayment, error) {
	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	query := `
		SELECT id, event_id, amount, payment_method, payment_status, due_date, paid_at, created_at
		FROM installment_payments
		WHERE id = $1
	`

	var payment models.InstallmentPayment
	var paymentMethod sql.NullString
	var dueDate sql.NullTime
	var paidAt sql.NullTime

	err := s.db.QueryRowContext(ctx, query, paymentID).Scan(
		&payment.ID,
		&payment.EventID,
		&payment.Amount,
		&paymentMethod,
		&payment.PaymentStatus,
		&dueDate,
		&paidAt,
		&payment.CreatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, ErrNotFound
		}
		return nil, err
	}

	if paymentMethod.Valid {
		payment.PaymentMethod = &paymentMethod.String
	}
	if dueDate.Valid {
		payment.DueDate = &dueDate.Time
	}
	if paidAt.Valid {
		payment.PaidAt = &paidAt.Time
	}

	return &payment, nil
}
