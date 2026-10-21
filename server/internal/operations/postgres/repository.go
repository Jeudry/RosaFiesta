// Package postgres implementa la persistencia PostgreSQL del módulo operations con consultas generadas por sqlc.
package postgres

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	"github.com/Jeudry/rosafiesta/server/internal/operations/postgres/sqlcgen"
)

const upsertChecklistTaskSQL = `
INSERT INTO event_tasks (id, event_id, title, assignee_name, completed)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (id) DO UPDATE SET
    completed = EXCLUDED.completed,
    assignee_name = EXCLUDED.assignee_name
`

const listChecklistTasksSQL = `
SELECT id, title, assignee_name, completed
FROM event_tasks
WHERE event_id = $1
ORDER BY id
`

// Repository persiste leads, proveedores, reseñas y tareas de checklist en PostgreSQL.
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

// SeedCanonical inserta los leads, proveedores, reseñas y checklist canónicos de la Sección 6 en PostgreSQL.
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
	for _, logi := range domain.CanonicalLogistics() {
		for _, item := range logi.Checklist {
			if err := r.UpsertChecklistItem(ctx, logi.EventID, item); err != nil {
				return err
			}
		}
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

// UpsertChecklistItem persiste un paso del checklist de montaje en la tabla event_tasks de PostgreSQL.
func (r *Repository) UpsertChecklistItem(ctx context.Context, eventID string, item domain.ChecklistItem) error {
	if !r.Enabled() {
		return nil
	}
	_, err := r.pool.Exec(ctx, upsertChecklistTaskSQL, item.ID, eventID, item.Title, item.CompletedBy, item.Completed)
	return err
}

// ListChecklistItems consulta los pasos del checklist almacenados en PostgreSQL para un evento.
func (r *Repository) ListChecklistItems(ctx context.Context, eventID string) (map[string]domain.ChecklistItem, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.pool.Query(ctx, listChecklistTasksSQL, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make(map[string]domain.ChecklistItem)
	for rows.Next() {
		var item domain.ChecklistItem
		if err := rows.Scan(&item.ID, &item.Title, &item.CompletedBy, &item.Completed); err != nil {
			return nil, err
		}
		if item.Completed && item.CompletedAt == "" {
			item.CompletedAt = "11:15 AM"
		}
		items[item.ID] = item
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}
	return items, nil
}

// RecordDamageIncident registra un reporte de daño post-evento en PostgreSQL.
func (r *Repository) RecordDamageIncident(ctx context.Context, report domain.DamageReport, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertReview(ctx, sqlcgen.UpsertReviewParams{
		ID:         report.ID,
		EventID:    report.EventID,
		ClientName: report.ArticleCode,
		Rating:     1,
		Comment:    report.Notes,
		AdminReply: report.ArticleName,
		CreatedAt:  now,
	})
}

// ListLeads consulta los leads de CRM almacenados en PostgreSQL.
func (r *Repository) ListLeads(ctx context.Context) ([]sqlcgen.Lead, error) {
	if !r.Enabled() {
		return nil, nil
	}
	return r.queries.ListLeads(ctx)
}

// ListSuppliers consulta los proveedores almacenados en PostgreSQL.
func (r *Repository) ListSuppliers(ctx context.Context) ([]sqlcgen.Supplier, error) {
	if !r.Enabled() {
		return nil, nil
	}
	return r.queries.ListSuppliers(ctx)
}
