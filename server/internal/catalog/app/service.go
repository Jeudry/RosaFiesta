package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

type ReservationReader interface {
	ReservedStockByDateForMonth(ctx context.Context, articleID, month string) map[string]int
}

type Store interface {
	Enabled() bool
	ListArticles(ctx context.Context) ([]domain.Article, error)
	ListBalloonArticles(ctx context.Context) ([]domain.Article, error)
	ListAllArticles(ctx context.Context) ([]domain.Article, error)
	ListCategories(ctx context.Context) ([]domain.Category, error)
	ListPackages(ctx context.Context) ([]domain.PackageBundle, error)
	UpsertArticle(ctx context.Context, article domain.Article) error
	UpsertPackage(ctx context.Context, pkg domain.PackageBundle) error
}

type ArticleFilter struct {
	Query         string
	Kind          string
	IncludeHidden bool
}

type CreateArticleCommand struct {
	ID                string
	Name              string
	CategoryID        string
	PhotoKey          string
	DailyPriceDop     int
	TotalStock        int
	AvailableStock    *int
	LowStockThreshold int
	Description       string
	Visible           *bool
	Dimensions        string
	Material          string
	ColorVariants     []string
	IsDirectSale      bool
}

type UpdateArticleCommand struct {
	Name              *string
	CategoryID        *string
	PhotoKey          *string
	DailyPriceDop     *int
	TotalStock        *int
	AvailableStock    *int
	LowStockThreshold *int
	Description       *string
	Visible           *bool
	Dimensions        *string
	Material          *string
	ColorVariants     *[]string
}

type SavePackageCommand struct {
	ID            string
	Name          string
	EventType     string
	PhotoKey      string
	TotalPriceDop int
	GuestCapacity int
	ItemsSummary  string
	Visible       *bool
	Items         []domain.PackageItem
}

type Service struct {
	mu           sync.RWMutex
	store        Store
	reservations ReservationReader
	articles     []domain.Article
	categories   []domain.Category
	packages     []domain.PackageBundle
}

func NewService() *Service {
	svc := &Service{}
	svc.Reset(context.Background())
	return svc
}

func (s *Service) Reset(_ context.Context) {
	allArticles := domain.CanonicalArticles()
	allArticles = append(allArticles, domain.CanonicalBalloonArticles()...)
	s.mu.Lock()
	s.articles = allArticles
	s.categories = domain.CanonicalCategories()
	s.packages = domain.CanonicalPackages()
	s.mu.Unlock()
}

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.articles = nil
	s.categories = nil
	s.packages = nil
	s.mu.Unlock()
}

func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.mu.Unlock()
	s.syncFromStore(context.Background())
	return s
}

func (s *Service) WithReservationReader(reader ReservationReader) *Service {
	s.mu.Lock()
	s.reservations = reader
	s.mu.Unlock()
	return s
}

func (s *Service) Articles(ctx context.Context, query string) []domain.Article {
	return s.ArticlesFiltered(ctx, ArticleFilter{
		Query:         query,
		Kind:          "rental",
		IncludeHidden: false,
	})
}

func (s *Service) ArticlesFiltered(ctx context.Context, filter ArticleFilter) []domain.Article {
	s.syncFromStore(ctx)
	s.mu.RLock()
	snapshot := make([]domain.Article, len(s.articles))
	copy(snapshot, s.articles)
	s.mu.RUnlock()

	trimmedQuery := strings.TrimSpace(filter.Query)
	lowerQuery := strings.ToLower(trimmedQuery)
	trimmedKind := strings.TrimSpace(filter.Kind)
	lowerKind := strings.ToLower(trimmedKind)
	if lowerKind == "" {
		lowerKind = "rental"
	}

	out := make([]domain.Article, 0, len(snapshot))
	for _, item := range snapshot {
		if !filter.IncludeHidden {
			if !item.Visible {
				continue
			}
		}
		if !matchesKind(item, lowerKind) {
			continue
		}
		if item.MatchesQuery(lowerQuery) {
			out = append(out, item)
		}
	}
	return out
}

func (s *Service) Balloons(ctx context.Context) []domain.Article {
	return s.ArticlesFiltered(ctx, ArticleFilter{
		Kind:          "balloon",
		IncludeHidden: false,
	})
}

func (s *Service) Categories(ctx context.Context) []domain.Category {
	s.syncFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Category, len(s.categories))
	copy(out, s.categories)
	return out
}

func (s *Service) Packages(ctx context.Context) []domain.PackageBundle {
	s.syncFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.PackageBundle, 0, len(s.packages))
	for _, pkg := range s.packages {
		if pkg.Visible {
			out = append(out, pkg)
		}
	}
	return out
}

func (s *Service) Availability(ctx context.Context, articleID, month string) (domain.ArticleAvailability, error) {
	s.syncFromStore(ctx)
	targetID := strings.TrimSpace(articleID)
	if targetID == "" {
		targetID = "silla-tiffany"
	}
	targetMonth := strings.TrimSpace(month)
	if targetMonth == "" {
		targetMonth = "2026-10"
	}
	startOfMonth, err := time.Parse("2006-01", targetMonth)
	if err != nil {
		return domain.ArticleAvailability{}, domain.ErrInvalidCatalog
	}
	article, found := s.findArticleByID(targetID)
	if !found {
		return domain.ArticleAvailability{}, domain.ErrArticleNotFound
	}
	reservedByDate := s.reservedMapForMonth(ctx, article.ID, targetMonth)
	days := buildMonthAvailabilityDays(startOfMonth, article, reservedByDate)
	return domain.ArticleAvailability{
		ArticleID:   article.ID,
		ArticleName: article.Name,
		Month:       targetMonth,
		TotalStock:  article.TotalStock,
		Days:        days,
	}, nil
}

func (s *Service) CreateArticle(ctx context.Context, cmd CreateArticleCommand) (domain.Article, error) {
	name := strings.TrimSpace(cmd.Name)
	if name == "" {
		return domain.Article{}, domain.ErrInvalidCatalog
	}
	if cmd.DailyPriceDop < 0 {
		return domain.Article{}, domain.ErrInvalidCatalog
	}
	if cmd.TotalStock < 0 {
		return domain.Article{}, domain.ErrInvalidCatalog
	}
	article := buildNewArticle(cmd, name)
	s.syncFromStore(ctx)
	s.mu.Lock()
	s.upsertArticleInSliceLocked(article)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertArticle(ctx, article); err != nil {
				return domain.Article{}, err
			}
		}
	}
	return article, nil
}

func (s *Service) UpdateArticle(ctx context.Context, articleID string, cmd UpdateArticleCommand) (domain.Article, error) {
	s.syncFromStore(ctx)
	trimmedID := strings.TrimSpace(articleID)
	s.mu.Lock()
	idx := -1
	for i, existing := range s.articles {
		if existing.ID == trimmedID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.Article{}, domain.ErrArticleNotFound
	}
	updated, err := applyArticlePatch(s.articles[idx], cmd)
	if err != nil {
		s.mu.Unlock()
		return domain.Article{}, err
	}
	s.articles[idx] = updated
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertArticle(ctx, updated); err != nil {
				return domain.Article{}, err
			}
		}
	}
	return updated, nil
}

func (s *Service) CreatePackage(ctx context.Context, cmd SavePackageCommand) (domain.PackageBundle, error) {
	pkg, err := buildPackageBundle(cmd)
	if err != nil {
		return domain.PackageBundle{}, err
	}
	s.syncFromStore(ctx)
	s.mu.Lock()
	s.upsertPackageInSliceLocked(pkg)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertPackage(ctx, pkg); err != nil {
				return domain.PackageBundle{}, err
			}
		}
	}
	return pkg, nil
}

func (s *Service) UpdatePackage(ctx context.Context, packageID string, cmd SavePackageCommand) (domain.PackageBundle, error) {
	s.syncFromStore(ctx)
	trimmedID := strings.TrimSpace(packageID)
	if trimmedID == "" {
		return domain.PackageBundle{}, domain.ErrInvalidCatalog
	}
	cmd.ID = trimmedID
	pkg, err := buildPackageBundle(cmd)
	if err != nil {
		return domain.PackageBundle{}, err
	}
	s.mu.Lock()
	s.upsertPackageInSliceLocked(pkg)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertPackage(ctx, pkg); err != nil {
				return domain.PackageBundle{}, err
			}
		}
	}
	return pkg, nil
}

func (s *Service) findArticleByID(articleID string) (domain.Article, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	for _, item := range s.articles {
		if item.ID == articleID {
			return item, true
		}
	}
	return domain.Article{}, false
}

func (s *Service) reservedMapForMonth(ctx context.Context, articleID, month string) map[string]int {
	s.mu.RLock()
	reader := s.reservations
	s.mu.RUnlock()
	if reader == nil {
		return map[string]int{}
	}
	return reader.ReservedStockByDateForMonth(ctx, articleID, month)
}

func buildMonthAvailabilityDays(
	startOfMonth time.Time,
	article domain.Article,
	reservedByDate map[string]int,
) []domain.AvailabilityDay {
	nextMonth := startOfMonth.AddDate(0, 1, 0)
	monthDuration := nextMonth.Sub(startOfMonth)
	daysInMonth := int(monthDuration.Hours() / 24)
	monthPrefix := startOfMonth.Format("2006-01")
	days := make([]domain.AvailabilityDay, 0, daysInMonth)
	for dayNum := 1; dayNum <= daysInMonth; dayNum++ {
		dateStr := fmt.Sprintf("%s-%02d", monthPrefix, dayNum)
		reserved := reservedByDate[dateStr]
		available := article.TotalStock - reserved
		if available < 0 {
			available = 0
		}
		isLow := available > 0
		if isLow {
			isLow = available <= article.LowStockThreshold
		}
		days = append(days, domain.AvailabilityDay{
			Date:      dateStr,
			Reserved:  reserved,
			Available: available,
			LowStock:  isLow,
		})
	}
	return days
}

func buildNewArticle(cmd CreateArticleCommand, name string) domain.Article {
	id := slugifyCatalogID(cmd.ID, name)
	categoryID := strings.TrimSpace(cmd.CategoryID)
	if categoryID == "" {
		categoryID = "mobiliario"
	}
	photoKey := strings.TrimSpace(cmd.PhotoKey)
	if photoKey == "" {
		photoKey = "silla-tiffany"
	}
	available := cmd.TotalStock
	if cmd.AvailableStock != nil {
		available = *cmd.AvailableStock
	}
	visible := true
	if cmd.Visible != nil {
		visible = *cmd.Visible
	}
	lowStock := cmd.LowStockThreshold
	if lowStock <= 0 {
		lowStock = 5
	}
	return domain.Article{
		ID:                id,
		Name:              name,
		CategoryID:        categoryID,
		PhotoKey:          photoKey,
		DailyPriceDop:     cmd.DailyPriceDop,
		TotalStock:        cmd.TotalStock,
		AvailableStock:    available,
		LowStockThreshold: lowStock,
		Description:       strings.TrimSpace(cmd.Description),
		Visible:           visible,
		Dimensions:        strings.TrimSpace(cmd.Dimensions),
		Material:          strings.TrimSpace(cmd.Material),
		ColorVariants:     cmd.ColorVariants,
		IsDirectSale:      cmd.IsDirectSale,
	}
}

func applyArticlePatch(existing domain.Article, cmd UpdateArticleCommand) (domain.Article, error) {
	updated := existing
	if cmd.Name != nil {
		trimmed := strings.TrimSpace(*cmd.Name)
		if trimmed == "" {
			return domain.Article{}, domain.ErrInvalidCatalog
		}
		updated.Name = trimmed
	}
	if cmd.CategoryID != nil {
		updated.CategoryID = strings.TrimSpace(*cmd.CategoryID)
	}
	if cmd.PhotoKey != nil {
		updated.PhotoKey = strings.TrimSpace(*cmd.PhotoKey)
	}
	var err error
	updated, err = applyArticleStockAndPricePatch(updated, cmd)
	if err != nil {
		return domain.Article{}, err
	}
	return applyArticleSpecsPatch(updated, cmd), nil
}

func applyArticleStockAndPricePatch(updated domain.Article, cmd UpdateArticleCommand) (domain.Article, error) {
	if cmd.DailyPriceDop != nil {
		if *cmd.DailyPriceDop < 0 {
			return domain.Article{}, domain.ErrInvalidCatalog
		}
		updated.DailyPriceDop = *cmd.DailyPriceDop
	}
	if cmd.TotalStock != nil {
		if *cmd.TotalStock < 0 {
			return domain.Article{}, domain.ErrInvalidCatalog
		}
		updated.TotalStock = *cmd.TotalStock
	}
	if cmd.AvailableStock != nil {
		updated.AvailableStock = *cmd.AvailableStock
	}
	if cmd.LowStockThreshold != nil {
		updated.LowStockThreshold = *cmd.LowStockThreshold
	}
	return updated, nil
}

func applyArticleSpecsPatch(updated domain.Article, cmd UpdateArticleCommand) domain.Article {
	if cmd.Description != nil {
		updated.Description = strings.TrimSpace(*cmd.Description)
	}
	if cmd.Visible != nil {
		updated.Visible = *cmd.Visible
	}
	if cmd.Dimensions != nil {
		updated.Dimensions = strings.TrimSpace(*cmd.Dimensions)
	}
	if cmd.Material != nil {
		updated.Material = strings.TrimSpace(*cmd.Material)
	}
	if cmd.ColorVariants != nil {
		updated.ColorVariants = *cmd.ColorVariants
	}
	return updated
}

func buildPackageBundle(cmd SavePackageCommand) (domain.PackageBundle, error) {
	name := strings.TrimSpace(cmd.Name)
	if name == "" {
		return domain.PackageBundle{}, domain.ErrInvalidCatalog
	}
	if cmd.TotalPriceDop < 0 {
		return domain.PackageBundle{}, domain.ErrInvalidCatalog
	}
	id := slugifyCatalogID(cmd.ID, "pkg-"+name)
	eventType := strings.TrimSpace(cmd.EventType)
	if eventType == "" {
		eventType = "Evento"
	}
	photoKey := strings.TrimSpace(cmd.PhotoKey)
	if photoKey == "" {
		photoKey = "boda"
	}
	visible := true
	if cmd.Visible != nil {
		visible = *cmd.Visible
	}
	summary := strings.TrimSpace(cmd.ItemsSummary)
	if summary == "" {
		summary = summarizePackageItems(cmd.Items)
	}
	return domain.PackageBundle{
		ID:            id,
		Name:          name,
		EventType:     eventType,
		PhotoKey:      photoKey,
		TotalPriceDop: cmd.TotalPriceDop,
		GuestCapacity: cmd.GuestCapacity,
		ItemsSummary:  summary,
		Visible:       visible,
		Items:         cmd.Items,
	}, nil
}

func summarizePackageItems(items []domain.PackageItem) string {
	if len(items) == 0 {
		return "Custom package configuration"
	}
	parts := make([]string, 0, len(items))
	for _, item := range items {
		label := fmt.Sprintf("%d %s", item.Quantity, item.ArticleName)
		parts = append(parts, label)
	}
	return strings.Join(parts, " · ")
}

func slugifyCatalogID(rawID, fallbackName string) string {
	trimmed := strings.TrimSpace(rawID)
	if trimmed != "" {
		return strings.ToLower(trimmed)
	}
	lower := strings.ToLower(fallbackName)
	replaced := strings.ReplaceAll(lower, " ", "-")
	return replaced
}

func matchesKind(item domain.Article, kind string) bool {
	if kind == "all" {
		return true
	}
	if kind == "balloon" {
		return item.IsDirectSale
	}
	return !item.IsDirectSale
}

func (s *Service) upsertArticleInSliceLocked(article domain.Article) {
	for i, existing := range s.articles {
		if existing.ID == article.ID {
			s.articles[i] = article
			return
		}
	}
	s.articles = append(s.articles, article)
}

func (s *Service) upsertPackageInSliceLocked(pkg domain.PackageBundle) {
	for i, existing := range s.packages {
		if existing.ID == pkg.ID {
			s.packages[i] = pkg
			return
		}
	}
	s.packages = append(s.packages, pkg)
}

func (s *Service) syncFromStore(ctx context.Context) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	dbArticles, artErr := store.ListAllArticles(ctx)
	dbCats, catErr := store.ListCategories(ctx)
	dbPkgs, pkgErr := store.ListPackages(ctx)
	s.mu.Lock()
	if artErr == nil {
		if len(dbArticles) > 0 {
			s.articles = dbArticles
		}
	}
	if catErr == nil {
		if len(dbCats) > 0 {
			s.categories = dbCats
		}
	}
	if pkgErr == nil {
		if len(dbPkgs) > 0 {
			s.packages = dbPkgs
		}
	}
	s.mu.Unlock()
}
