// Package postgres implementa la persistencia PostgreSQL del módulo operations con consultas generadas por sqlc.
package postgres

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/operations/postgres/sqlcgen"
)

// Repository persiste leads, proveedores y reseñas en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea el repositorio PostgreSQL de operaciones.
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

// SeedCanonical inserta los leads, proveedores y reseñas canónicos de la Sección 6 en PostgreSQL.
func (r *Repository) SeedCanonical(ctx context.Context, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	if err := r.queries.UpsertLead(ctx, sqlcgen.UpsertLeadParams{
		ID:           "lead-mateo",
		FullName:     "Laura Peña",
		Phone:        "809-555-0188",
		EventType:    "Cumpleaños de Mateo",
		EstimatedDop: 405,
		Stage:        "cotizado",
		CreatedAt:    now,
	}); err != nil {
		return err
	}
	if err := r.queries.UpsertSupplier(ctx, sqlcgen.UpsertSupplierParams{
		ID:        "sup-flores-caribe",
		Name:      "Flores del Caribe SRL",
		Specialty: "Rosas y hortensias frescas",
		Phone:     "809-555-0301",
		Rating:    "4.9",
	}); err != nil {
		return err
	}
	return r.queries.UpsertReview(ctx, sqlcgen.UpsertReviewParams{
		ID:         "rev-gala-caribe",
		EventID:    "evt-gala-caribe",
		ClientName: "Grupo Caribe",
		Rating:     5,
		Comment:    "Montaje impecable y puntual en toda la gala corporativa.",
		AdminReply: "¡Gracias por confiar en RosaFiesta!",
		CreatedAt:  now,
	})
}
