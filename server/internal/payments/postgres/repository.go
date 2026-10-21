// Package postgres implementa la persistencia PostgreSQL del módulo payments con consultas generadas por sqlc.
package postgres

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	"github.com/Jeudry/rosafiesta/server/internal/payments/postgres/sqlcgen"
)

// Repository persiste pagos y reembolsos con idempotencia en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea el repositorio PostgreSQL de pagos.
func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

// Enabled indica si hay conexión PostgreSQL activa.
func (r *Repository) Enabled() bool {
	return r != nil && r.pool != nil
}

// UpsertPayment inserta un pago garantizando idempotencia por clave.
func (r *Repository) UpsertPayment(ctx context.Context, p domain.Payment, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	idemKey := p.ReferenceCode
	if idemKey == "" {
		idemKey = p.ID
	}
	return r.queries.UpsertPayment(ctx, sqlcgen.UpsertPaymentParams{
		ID:             p.ID,
		IdempotencyKey: idemKey,
		EventID:        p.EventID,
		ClientName:     p.ClientName,
		AmountDop:      p.AmountRD,
		Method:         string(p.Method),
		PaidDateLabel:  p.PaidDateLabel,
		Status:         string(p.Status),
		CreatedAt:      now,
	})
}

// ListPayments consulta todos los pagos almacenados en PostgreSQL.
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
		out = append(out, domain.Payment{
			ID:            row.ID,
			ReceiptNumber: row.ID,
			EventID:       row.EventID,
			ClientName:    row.ClientName,
			AmountRD:      row.AmountDop,
			Method:        domain.PaymentMethod(row.Method),
			PaidDateLabel: row.PaidDateLabel,
			ReferenceCode: row.IdempotencyKey,
			Status:        domain.PaymentStatus(row.Status),
		})
	}
	return out, nil
}
