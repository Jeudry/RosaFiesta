package postgres

import (
	"context"
	"errors"
	"strings"
	"time"

	"github.com/jackc/pgx/v5"
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
	for _, trend := range domain.CanonicalTrends() {
		if err := r.UpsertTrend(ctx, trend); err != nil {
			return err
		}
	}
	return r.UpsertSeasonBanner(ctx, domain.CanonicalSeasonBanner())
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
			row.PhotoUrl,
			row.DailyPriceDop,
			row.TotalStock,
			row.DamagedStock,
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
			row.PhotoUrl,
			row.DailyPriceDop,
			row.TotalStock,
			row.DamagedStock,
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
			row.PhotoUrl,
			row.DailyPriceDop,
			row.TotalStock,
			row.DamagedStock,
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
			PhotoURL:      row.PhotoUrl,
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
	if strings.TrimSpace(article.ID) == "" {
		return errors.New("article id is required")
	}
	tx, err := r.pool.BeginTx(ctx, pgx.TxOptions{})
	if err != nil {
		return err
	}
	defer func() { _ = tx.Rollback(ctx) }()
	qtx := r.queries.WithTx(tx)
	if err := upsertArticleTx(ctx, qtx, article); err != nil {
		return err
	}
	return tx.Commit(ctx)
}

func upsertArticleTx(ctx context.Context, q *sqlcgen.Queries, article domain.Article) error {
	if err := ensureCategoryExistsTx(ctx, q, article.CategoryID); err != nil {
		return err
	}
	variants := article.ColorVariants
	if variants == nil {
		variants = []string{}
	}
	return q.UpsertArticle(ctx, sqlcgen.UpsertArticleParams{
		ID:                article.ID,
		Name:              article.Name,
		CategoryID:        article.CategoryID,
		PhotoKey:          article.PhotoKey,
		PhotoUrl:          article.PhotoURL,
		DailyPriceDop:     article.DailyPriceDop,
		TotalStock:        article.TotalStock,
		DamagedStock:      article.DamagedStock,
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

func (r *Repository) DeleteArticle(ctx context.Context, articleID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteArticle(ctx, articleID)
}

// DeletePackage elimina un paquete temático de PostgreSQL.
func (r *Repository) DeletePackage(ctx context.Context, packageID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeletePackage(ctx, packageID)
}

func (r *Repository) UpsertCategory(ctx context.Context, cat domain.Category) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertCategory(ctx, sqlcgen.UpsertCategoryParams{
		ID:        cat.ID,
		Name:      cat.Name,
		IlloKey:   cat.IlloKey,
		ItemCount: cat.ItemCount,
	})
}

func (r *Repository) DeleteCategory(ctx context.Context, categoryID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteCategory(ctx, categoryID)
}

func (r *Repository) CountArticlesByCategory(ctx context.Context, categoryID string) (int, error) {
	if !r.Enabled() {
		return 0, nil
	}
	count, err := r.queries.CountArticlesByCategory(ctx, categoryID)
	if err != nil {
		return 0, err
	}
	return count, nil
}

func ensureCategoryExistsTx(ctx context.Context, q *sqlcgen.Queries, categoryID string) error {
	existing, err := q.ListCategories(ctx)
	if err == nil {
		for _, row := range existing {
			matchesID := strings.EqualFold(row.ID, categoryID)
			matchesName := strings.EqualFold(row.Name, categoryID)
			if matchesID {
				return nil
			}
			if matchesName {
				return nil
			}
		}
	}
	return q.UpsertCategory(ctx, sqlcgen.UpsertCategoryParams{
		ID:        categoryID,
		Name:      categoryID,
		IlloKey:   "mobiliario",
		ItemCount: 0,
	})
}

func (r *Repository) UpsertPackage(ctx context.Context, pkg domain.PackageBundle) error {
	if !r.Enabled() {
		return nil
	}
	if strings.TrimSpace(pkg.ID) == "" {
		return errors.New("package id is required")
	}
	tx, err := r.pool.BeginTx(ctx, pgx.TxOptions{})
	if err != nil {
		return err
	}
	defer func() { _ = tx.Rollback(ctx) }()
	qtx := r.queries.WithTx(tx)
	if err := upsertPackageTx(ctx, qtx, pkg); err != nil {
		return err
	}
	return tx.Commit(ctx)
}

func upsertPackageTx(ctx context.Context, q *sqlcgen.Queries, pkg domain.PackageBundle) error {
	err := q.UpsertPackage(ctx, sqlcgen.UpsertPackageParams{
		ID:            pkg.ID,
		Name:          pkg.Name,
		EventType:     pkg.EventType,
		PhotoKey:      pkg.PhotoKey,
		PhotoUrl:      pkg.PhotoURL,
		TotalPriceDop: pkg.TotalPriceDop,
		GuestCapacity: pkg.GuestCapacity,
		ItemsSummary:  pkg.ItemsSummary,
		Visible:       pkg.Visible,
	})
	if err != nil {
		return err
	}
	if err := q.DeletePackageItems(ctx, pkg.ID); err != nil {
		return err
	}
	for _, item := range pkg.Items {
		if item.Quantity <= 0 {
			continue
		}
		err := q.UpsertPackageItem(ctx, sqlcgen.UpsertPackageItemParams{
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
	photoURL string,
	dailyPrice int,
	totalStock int,
	damagedStock int,
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
		PhotoURL:          photoURL,
		DailyPriceDop:     dailyPrice,
		TotalStock:        totalStock,
		DamagedStock:      damagedStock,
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

// ListActiveReservations retorna todas las reservas activas ('held' o 'confirmed').
func (r *Repository) ListActiveReservations(ctx context.Context) ([]domain.StockReservation, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListActiveReservations(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.StockReservation, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.StockReservation{
			ID:            row.ID,
			ArticleID:     row.ArticleID,
			EventID:       row.EventID,
			QuoteID:       row.QuoteID,
			Quantity:      row.Quantity,
			PickupDateISO: row.PickupDateIso,
			ReturnDateISO: row.ReturnDateIso,
			Status:        domain.ReservationStatus(row.Status),
			CreatedAt:     row.CreatedAt,
			UpdatedAt:     row.UpdatedAt,
		})
	}
	return out, nil
}

// ReplaceEventReservations reemplaza atómicamente las reservas asociadas a un evento.
func (r *Repository) ReplaceEventReservations(
	ctx context.Context,
	eventID string,
	items []domain.StockReservation,
) error {
	if !r.Enabled() {
		return nil
	}
	tx, err := r.pool.BeginTx(ctx, pgx.TxOptions{})
	if err != nil {
		return err
	}
	defer func() { _ = tx.Rollback(ctx) }()
	qtx := r.queries.WithTx(tx)
	if err := qtx.DeleteReservationsByEvent(ctx, eventID); err != nil {
		return err
	}
	for _, item := range items {
		createdAt := item.CreatedAt
		if createdAt.IsZero() {
			createdAt = time.Now().UTC()
		}
		updatedAt := item.UpdatedAt
		if updatedAt.IsZero() {
			updatedAt = createdAt
		}
		if err := qtx.UpsertStockReservation(ctx, sqlcgen.UpsertStockReservationParams{
			ID:            item.ID,
			ArticleID:     item.ArticleID,
			EventID:       item.EventID,
			QuoteID:       item.QuoteID,
			Quantity:      item.Quantity,
			PickupDateIso: item.PickupDateISO,
			ReturnDateIso: item.ReturnDateISO,
			Status:        string(item.Status),
			CreatedAt:     createdAt,
			UpdatedAt:     updatedAt,
		}); err != nil {
			return err
		}
	}
	return tx.Commit(ctx)
}

// UpdateReservationStatusByEvent actualiza el estado de las reservas de un evento.
func (r *Repository) UpdateReservationStatusByEvent(
	ctx context.Context,
	eventID string,
	status domain.ReservationStatus,
	updatedAt time.Time,
) error {
	if !r.Enabled() {
		return nil
	}
	if updatedAt.IsZero() {
		updatedAt = time.Now().UTC()
	}
	return r.queries.UpdateReservationStatusByEvent(ctx, sqlcgen.UpdateReservationStatusByEventParams{
		EventID:   eventID,
		Status:    string(status),
		UpdatedAt: updatedAt,
	})
}

// ListTrends returns all persisted catalog trends ordered by display priority.
func (r *Repository) ListTrends(ctx context.Context) ([]domain.Trend, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListTrends(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Trend, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.Trend{
			ID:              row.ID,
			Title:           row.Title,
			EventsLabel:     row.EventsLabel,
			PhotoKey:        row.PhotoKey,
			PhotoURL:        row.PhotoUrl,
			PhotoBgHex:      row.PhotoBgHex,
			LinkedCategory:  row.LinkedCategory,
			LinkedPackageID: row.LinkedPackageID,
			SortOrder:       row.SortOrder,
			Active:          row.Active,
		})
	}
	return out, nil
}

// UpsertTrend inserts or updates a catalog trend row.
func (r *Repository) UpsertTrend(ctx context.Context, trend domain.Trend) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertTrend(ctx, sqlcgen.UpsertTrendParams{
		ID:              trend.ID,
		Title:           trend.Title,
		EventsLabel:     trend.EventsLabel,
		PhotoKey:        trend.PhotoKey,
		PhotoUrl:        trend.PhotoURL,
		PhotoBgHex:      trend.PhotoBgHex,
		LinkedCategory:  trend.LinkedCategory,
		LinkedPackageID: trend.LinkedPackageID,
		SortOrder:       trend.SortOrder,
		Active:          trend.Active,
	})
}

// DeleteTrend removes a catalog trend row by identifier.
func (r *Repository) DeleteTrend(ctx context.Context, trendID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteTrend(ctx, trendID)
}

// GetSeasonBanner loads the singleton Personal Home seasonal banner if present.
func (r *Repository) GetSeasonBanner(ctx context.Context) (domain.SeasonBanner, bool, error) {
	if !r.Enabled() {
		return domain.SeasonBanner{}, false, nil
	}
	row, err := r.queries.GetSeasonBanner(ctx)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return domain.SeasonBanner{}, false, nil
		}
		return domain.SeasonBanner{}, false, err
	}
	return domain.SeasonBanner{
		ID:               row.ID,
		BadgeLabel:       row.BadgeLabel,
		Title:            row.Title,
		CtaLabel:         row.CtaLabel,
		TargetScreenCode: row.TargetScreenCode,
		Active:           row.Active,
	}, true, nil
}

// UpsertSeasonBanner saves or updates the singleton Personal Home seasonal banner.
func (r *Repository) UpsertSeasonBanner(ctx context.Context, banner domain.SeasonBanner) error {
	if !r.Enabled() {
		return nil
	}
	id := strings.TrimSpace(banner.ID)
	if id == "" {
		id = "default"
	}
	return r.queries.UpsertSeasonBanner(ctx, sqlcgen.UpsertSeasonBannerParams{
		ID:               id,
		BadgeLabel:       banner.BadgeLabel,
		Title:            banner.Title,
		CtaLabel:         banner.CtaLabel,
		TargetScreenCode: banner.TargetScreenCode,
		Active:           banner.Active,
	})
}
