// Package postgres implementa la persistencia PostgreSQL del módulo quotes con consultas generadas por sqlc.
package postgres

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/postgres/sqlcgen"
)

// Repository persiste cotizaciones en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea el repositorio PostgreSQL de cotizaciones.
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

// UpsertQuote inserta o actualiza una cotización en PostgreSQL.
func (r *Repository) UpsertQuote(ctx context.Context, q domain.Quote, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	notes := q.SignedByName
	if q.RejectionReason != "" {
		notes = q.RejectionReason
	}
	return r.queries.UpsertQuote(ctx, sqlcgen.UpsertQuoteParams{
		ID:         q.ID,
		EventID:    q.Code,
		ClientName: q.ClientName,
		EventTitle: q.EventTitle,
		TotalDop:   q.TotalRD,
		DepositDop: q.DepositRD,
		BalanceDop: q.BalanceRD,
		Status:     string(q.Status),
		Version:    1,
		Notes:      notes,
		CreatedAt:  now,
	})
}

// ListQuotes consulta todas las cotizaciones almacenadas en PostgreSQL.
func (r *Repository) ListQuotes(ctx context.Context) ([]domain.Quote, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListQuotes(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Quote, 0, len(rows))
	for _, row := range rows {
		q := domain.Quote{
			ID:             row.ID,
			Code:           row.EventID,
			ClientName:     row.ClientName,
			EventTitle:     row.EventTitle,
			TotalRD:        row.TotalDop,
			DepositPercent: 30,
			DepositRD:      row.DepositDop,
			BalanceRD:      row.BalanceDop,
			Status:         domain.QuoteStatus(row.Status),
		}
		switch q.Status {
		case domain.QuoteStatusSigned:
			q.SignedByName = row.Notes
		case domain.QuoteStatusDeclined:
			q.RejectionReason = row.Notes
		}
		out = append(out, q)
	}
	return out, nil
}
