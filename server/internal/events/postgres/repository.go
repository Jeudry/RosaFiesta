// Package postgres implementa la persistencia PostgreSQL del módulo events con consultas generadas por sqlc.
package postgres

import (
	"context"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	"github.com/Jeudry/rosafiesta/server/internal/events/postgres/sqlcgen"
)

// Repository persiste eventos y tareas en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea el repositorio PostgreSQL de eventos.
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

// UpsertEvent guarda o actualiza un evento en PostgreSQL.
func (r *Repository) UpsertEvent(ctx context.Context, e domain.Event, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertEvent(ctx, sqlcgen.UpsertEventParams{
		ID:               e.ID,
		ClientID:         uuid.MustParse("11111111-1111-1111-1111-111111111111"),
		ClientName:       e.ClientName,
		Title:            e.Title,
		EventType:        e.EventType,
		PhotoKey:         e.EventType,
		Venue:            e.Venue,
		EventDateLabel:   e.DateLabel,
		DaysUntil:        e.DaysUntilEvent,
		GuestsCount:      e.Guests,
		ArticlesCount:    e.ArticleCount,
		TotalDop:         e.TotalRD,
		DepositDop:       e.PaidDepositRD,
		PaidDop:          e.PaidDepositRD,
		BalanceDop:       e.BalanceRD(),
		BalanceDueLabel:  e.BalanceDueDate,
		Status:           string(e.Status),
		DateChangesCount: e.DateChangesUsed,
		CreatedAt:        now,
	})
}

// UpdateDate actualiza la fecha y el contador de cambios de un evento en PostgreSQL (`s84`).
func (r *Repository) UpdateDate(ctx context.Context, e domain.Event) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateEventDate(ctx, sqlcgen.UpdateEventDateParams{
		ID:               e.ID,
		EventDateLabel:   e.DateLabel,
		DaysUntil:        e.DaysUntilEvent,
		DateChangesCount: e.DateChangesUsed,
		TotalDop:         e.TotalRD,
	})
}

// UpdateStatus actualiza el estado del evento en PostgreSQL (`s85`).
func (r *Repository) UpdateStatus(ctx context.Context, id string, status domain.EventStatus) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateEventStatus(ctx, sqlcgen.UpdateEventStatusParams{
		ID:     id,
		Status: string(status),
	})
}
