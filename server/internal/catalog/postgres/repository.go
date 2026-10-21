// Package postgres implementa la persistencia PostgreSQL del módulo catalog con consultas generadas por sqlc.
package postgres

import (
	"context"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	"github.com/Jeudry/rosafiesta/server/internal/catalog/postgres/sqlcgen"
)

// Repository persiste categorías, artículos y paquetes en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea el repositorio PostgreSQL de catálogo.
func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

// Enabled indica si hay un pool PostgreSQL activo.
func (r *Repository) Enabled() bool {
	return r != nil && r.pool != nil
}

// Seed inserta de forma idempotente las categorías, artículos y paquetes canónicos.
func (r *Repository) Seed(
	ctx context.Context,
	categories []domain.Category,
	articles []domain.Article,
	packages []domain.PackageBundle,
) error {
	if !r.Enabled() {
		return nil
	}
	for _, c := range categories {
		if err := r.queries.UpsertCategory(ctx, sqlcgen.UpsertCategoryParams{
			ID:        c.ID,
			Name:      c.Name,
			IlloKey:   c.IlloKey,
			ItemCount: c.ItemCount,
		}); err != nil {
			return err
		}
	}
	for _, a := range articles {
		if err := r.queries.UpsertArticle(ctx, sqlcgen.UpsertArticleParams{
			ID:                a.ID,
			Name:              a.Name,
			CategoryID:        a.CategoryID,
			PhotoKey:          a.PhotoKey,
			DailyPriceDop:     a.DailyPriceDop,
			TotalStock:        a.TotalStock,
			LowStockThreshold: a.LowStockThreshold,
			Description:       a.Description,
		}); err != nil {
			return err
		}
	}
	for _, p := range packages {
		if err := r.queries.UpsertPackage(ctx, sqlcgen.UpsertPackageParams{
			ID:            p.ID,
			Name:          p.Name,
			EventType:     p.EventType,
			PhotoKey:      p.PhotoKey,
			TotalPriceDop: p.TotalPriceDop,
			GuestCapacity: p.GuestCapacity,
			ItemsSummary:  p.ItemsSummary,
		}); err != nil {
			return err
		}
	}
	return nil
}

// ListArticles consulta los artículos almacenados en PostgreSQL.
func (r *Repository) ListArticles(ctx context.Context) ([]domain.Article, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListArticles(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Article, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Article{
			ID:                row.ID,
			Name:              row.Name,
			CategoryID:        row.CategoryID,
			PhotoKey:          row.PhotoKey,
			DailyPriceDop:     row.DailyPriceDop,
			TotalStock:        row.TotalStock,
			LowStockThreshold: row.LowStockThreshold,
			Description:       row.Description,
		})
	}
	return out, nil
}

// ListCategories consulta las categorías almacenadas en PostgreSQL.
func (r *Repository) ListCategories(ctx context.Context) ([]domain.Category, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListCategories(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Category, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Category{
			ID:        row.ID,
			Name:      row.Name,
			IlloKey:   row.IlloKey,
			ItemCount: row.ItemCount,
		})
	}
	return out, nil
}

// ListPackages consulta los paquetes almacenados en PostgreSQL.
func (r *Repository) ListPackages(ctx context.Context) ([]domain.PackageBundle, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListPackages(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.PackageBundle, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.PackageBundle{
			ID:            row.ID,
			Name:          row.Name,
			EventType:     row.EventType,
			PhotoKey:      row.PhotoKey,
			TotalPriceDop: row.TotalPriceDop,
			GuestCapacity: row.GuestCapacity,
			ItemsSummary:  row.ItemsSummary,
		})
	}
	return out, nil
}

// UpsertArticle inserta o actualiza un artículo en PostgreSQL.
func (r *Repository) UpsertArticle(ctx context.Context, a domain.Article) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertArticle(ctx, sqlcgen.UpsertArticleParams{
		ID:                a.ID,
		Name:              a.Name,
		CategoryID:        a.CategoryID,
		PhotoKey:          a.PhotoKey,
		DailyPriceDop:     a.DailyPriceDop,
		TotalStock:        a.TotalStock,
		LowStockThreshold: a.LowStockThreshold,
		Description:       a.Description,
	})
}
