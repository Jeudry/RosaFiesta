package postgres

import (
	"context"
	"strings"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	"github.com/Jeudry/rosafiesta/server/internal/payments/postgres/sqlcgen"
)

type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

func (r *Repository) Enabled() bool {
	if r == nil {
		return false
	}
	return r.pool != nil
}

func (r *Repository) UpsertPayment(ctx context.Context, payment domain.Payment, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	idemKey := strings.TrimSpace(payment.ReferenceCode)
	if idemKey == "" {
		idemKey = payment.ID
	}
	receipt := strings.TrimSpace(payment.ReceiptNumber)
	if receipt == "" {
		receipt = payment.ID
	}
	kind := domain.NormalizePaymentKind(string(payment.Kind))
	params := sqlcgen.UpsertPaymentParams{
		ID:             payment.ID,
		ReceiptNumber:  receipt,
		IdempotencyKey: idemKey,
		EventID:        payment.EventID,
		EventTitle:     payment.EventTitle,
		ClientID:       payment.ClientID,
		ClientEmail:    payment.ClientEmail,
		ClientName:     payment.ClientName,
		AmountDop:      payment.AmountRD,
		Method:         string(payment.Method),
		Kind:           string(kind),
		BankName:       payment.BankName,
		PaidDateLabel:  payment.PaidDateLabel,
		Status:         string(payment.Status),
		CreatedAt:      now,
	}
	return r.queries.UpsertPayment(ctx, params)
}

func (r *Repository) ListPayments(ctx context.Context) ([]domain.Payment, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListPayments(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Payment, 0, len(rows))
	for _, row := range rows {
		out = append(out, mapPaymentRow(row))
	}
	return out, nil
}

func mapPaymentRow(row sqlcgen.ListPaymentsRow) domain.Payment {
	receipt := strings.TrimSpace(row.ReceiptNumber)
	if receipt == "" {
		receipt = row.ID
	}
	kind := domain.NormalizePaymentKind(row.Kind)
	return domain.Payment{
		ID:            row.ID,
		ReceiptNumber: receipt,
		EventID:       row.EventID,
		EventTitle:    row.EventTitle,
		ClientID:      row.ClientID,
		ClientEmail:   row.ClientEmail,
		ClientName:    row.ClientName,
		AmountRD:      row.AmountDop,
		Method:        domain.PaymentMethod(row.Method),
		Kind:          kind,
		BankName:      row.BankName,
		PaidDateLabel: row.PaidDateLabel,
		ReferenceCode: row.IdempotencyKey,
		Status:        domain.PaymentStatus(row.Status),
	}
}

func (r *Repository) UpsertRefund(ctx context.Context, refund domain.Refund, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	status := string(refund.Status)
	if status == "" {
		status = string(domain.RefundStatusPending)
	}
	params := sqlcgen.UpsertRefundParams{
		ID:              refund.ID,
		IdempotencyKey:  "refund:" + refund.ID,
		EventID:         refund.EventID,
		EventTitle:      refund.EventTitle,
		ClientName:      refund.ClientName,
		PaidDop:         refund.PaidRD,
		RefundPercent:   refund.RefundPercent,
		RefundDop:       refund.RefundDop,
		Reason:          refund.Reason,
		Status:          status,
		ApprovedBy:      refund.ApprovedBy,
		ApprovedAtLabel: refund.ApprovedAtLabel,
		CreatedAt:       now,
	}
	return r.queries.UpsertRefund(ctx, params)
}

func (r *Repository) ListRefunds(ctx context.Context) ([]domain.Refund, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListRefunds(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Refund, 0, len(rows))
	for _, row := range rows {
		out = append(out, mapRefundRow(row))
	}
	return out, nil
}

func mapRefundRow(row sqlcgen.ListRefundsRow) domain.Refund {
	status := domain.RefundStatus(row.Status)
	if status == "" {
		status = domain.RefundStatusPending
	}
	return domain.Refund{
		ID:              row.ID,
		EventID:         row.EventID,
		EventTitle:      row.EventTitle,
		ClientName:      row.ClientName,
		PaidRD:          row.PaidDop,
		RefundPercent:   row.RefundPercent,
		RefundDop:       row.RefundDop,
		Reason:          row.Reason,
		Status:          status,
		ApprovedBy:      row.ApprovedBy,
		ApprovedAtLabel: row.ApprovedAtLabel,
	}
}

func (r *Repository) SeedCanonical(ctx context.Context, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	for idx, payment := range domain.CanonicalPayments() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertPayment(ctx, payment, ts); err != nil {
			return err
		}
	}
	for idx, refund := range domain.CanonicalRefunds() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertRefund(ctx, refund, ts); err != nil {
			return err
		}
	}
	return nil
}
