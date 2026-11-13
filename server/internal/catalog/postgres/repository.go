package postgres

import (
	"context"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	"github.com/Jeudry/rosafiesta/server/internal/catalog/postgres/sqlcgen"
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

func (r *Repository) Seed(
	ctx context.Context,
	categories []domain.Category,
	articles []domain.Article,
	packages []domain.PackageBundle,
) error {
	if !r.Enabled() {
		return nil
	}
	for _, cat := range categories {
		err := r.queries.UpsertCategory(ctx, sqlcgen.UpsertCategoryParams{
			ID:        cat.ID,
			Name:      cat.Name,
			IlloKey:   cat.IlloKey,
			ItemCount: cat.ItemCount,
		})
		if err != nil {
			return err
		}
	}
	for _, art := range articles {
		if err := r.UpsertArticle(ctx, art); err != nil {
			return err
		}
	}
	for _, balloon := range domain.CanonicalBalloonArticles() {
		if err := r.UpsertArticle(ctx, balloon); err != nil {
			return err
		}
	}
	for _, pkg := range packages {
		if err := r.UpsertPackage(ctx, pkg); err != nil {
			return err
		}
	}
	return nil
}

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
		out = append(out, mapArticleFields(
			row.ID,
			row.Name,
			row.CategoryID,
			row.PhotoKey,
			row.DailyPriceDop,
			row.TotalStock,
			row.AvailableStock,
			row.LowStockThreshold,
			row.Description,
			row.Visible,
			row.Dimensions,
			row.Material,
			row.ColorVariants,
			row.IsDirectSale,
		))
	}
	return out, nil
}

func (r *Repository) ListBalloonArticles(ctx context.Context) ([]domain.Article, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListBalloonArticles(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Article, 0, len(rows))
	for _, row := range rows {
		out = append(out, mapArticleFields(
			row.ID,
			row.Name,
			row.CategoryID,
			row.PhotoKey,
			row.DailyPriceDop,
			row.TotalStock,
			row.AvailableStock,
			row.LowStockThreshold,
			row.Description,
			row.Visible,
			row.Dimensions,
			row.Material,
			row.ColorVariants,
			row.IsDirectSale,
		))
	}
	return out, nil
}

func (r *Repository) ListAllArticles(ctx context.Context) ([]domain.Article, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListAllArticles(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Article, 0, len(rows))
	for _, row := range rows {
		out = append(out, mapArticleFields(
			row.ID,
			row.Name,
			row.CategoryID,
			row.PhotoKey,
			row.DailyPriceDop,
			row.TotalStock,
			row.AvailableStock,
			row.LowStockThreshold,
			row.Description,
			row.Visible,
			row.Dimensions,
			row.Material,
			row.ColorVariants,
			row.IsDirectSale,
		))
	}
	return out, nil
}

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

func (r *Repository) ListPackages(ctx context.Context) ([]domain.PackageBundle, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListPackages(ctx)
	if err != nil {
		return nil, err
	}
	itemRows, err := r.queries.ListPackageItems(ctx)
	if err != nil {
		return nil, err
	}
	itemsByPkg := make(map[string][]domain.PackageItem)
	for _, item := range itemRows {
		itemsByPkg[item.PackageID] = append(itemsByPkg[item.PackageID], domain.PackageItem{
			ArticleID:    item.ArticleID,
			ArticleName:  item.ArticleName,
			Quantity:     item.Quantity,
			UnitPriceDop: item.UnitPriceDop,
		})
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
			Visible:       row.Visible,
			Items:         itemsByPkg[row.ID],
		})
	}
	return out, nil
}

func (r *Repository) UpsertArticle(ctx context.Context, article domain.Article) error {
	if !r.Enabled() {
		return nil
	}
	if err := r.ensureCategoryExists(ctx, article.CategoryID); err != nil {
		return err
	}
	variants := article.ColorVariants
	if variants == nil {
		variants = []string{}
	}
	return r.queries.UpsertArticle(ctx, sqlcgen.UpsertArticleParams{
		ID:                article.ID,
		Name:              article.Name,
		CategoryID:        article.CategoryID,
		PhotoKey:          article.PhotoKey,
		DailyPriceDop:     article.DailyPriceDop,
		TotalStock:        article.TotalStock,
		AvailableStock:    article.AvailableStock,
		LowStockThreshold: article.LowStockThreshold,
		Description:       article.Description,
		Visible:           article.Visible,
		Dimensions:        article.Dimensions,
		Material:          article.Material,
		ColorVariants:     variants,
		IsDirectSale:      article.IsDirectSale,
	})
}

func (r *Repository) ensureCategoryExists(ctx context.Context, categoryID string) error {
	existing, err := r.queries.ListCategories(ctx)
	if err == nil {
		for _, row := range existing {
			if row.ID == categoryID {
				return nil
			}
		}
	}
	return r.queries.UpsertCategory(ctx, sqlcgen.UpsertCategoryParams{
		ID:        categoryID,
		Name:      categoryID,
		IlloKey:   "cat-default",
		ItemCount: 0,
	})
}

func (r *Repository) UpsertPackage(ctx context.Context, pkg domain.PackageBundle) error {
	if !r.Enabled() {
		return nil
	}
	err := r.queries.UpsertPackage(ctx, sqlcgen.UpsertPackageParams{
		ID:            pkg.ID,
		Name:          pkg.Name,
		EventType:     pkg.EventType,
		PhotoKey:      pkg.PhotoKey,
		TotalPriceDop: pkg.TotalPriceDop,
		GuestCapacity: pkg.GuestCapacity,
		ItemsSummary:  pkg.ItemsSummary,
		Visible:       pkg.Visible,
	})
	if err != nil {
		return err
	}
	if err := r.queries.DeletePackageItems(ctx, pkg.ID); err != nil {
		return err
	}
	for _, item := range pkg.Items {
		err := r.queries.UpsertPackageItem(ctx, sqlcgen.UpsertPackageItemParams{
			PackageID:    pkg.ID,
			ArticleID:    item.ArticleID,
			ArticleName:  item.ArticleName,
			Quantity:     item.Quantity,
			UnitPriceDop: item.UnitPriceDop,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

func mapArticleFields(
	id string,
	name string,
	categoryID string,
	photoKey string,
	dailyPrice int,
	totalStock int,
	availableStock int,
	lowStock int,
	description string,
	visible bool,
	dimensions string,
	material string,
	colorVariants []string,
	isDirectSale bool,
) domain.Article {
	return domain.Article{
		ID:                id,
		Name:              name,
		CategoryID:        categoryID,
		PhotoKey:          photoKey,
		DailyPriceDop:     dailyPrice,
		TotalStock:        totalStock,
		AvailableStock:    availableStock,
		LowStockThreshold: lowStock,
		Description:       description,
		Visible:           visible,
		Dimensions:        dimensions,
		Material:          material,
		ColorVariants:     colorVariants,
		IsDirectSale:      isDirectSale,
	}
}
