package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"
	"unicode"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

// CategoryStore persiste operaciones de creación y eliminación de categorías en PostgreSQL.
type CategoryStore interface {
	UpsertCategory(ctx context.Context, category domain.Category) error
	DeleteCategory(ctx context.Context, categoryID string) error
}

// ArticleDeleteStore persists article deletion operations in PostgreSQL.
type ArticleDeleteStore interface {
	DeleteArticle(ctx context.Context, articleID string) error
}

// PackageDeleteStore persists package deletion operations in PostgreSQL.
type PackageDeleteStore interface {
	DeletePackage(ctx context.Context, packageID string) error
}

// TrendAndBannerStore persists catalog trends and the seasonal home banner in PostgreSQL.
type TrendAndBannerStore interface {
	ListTrends(ctx context.Context) ([]domain.Trend, error)
	UpsertTrend(ctx context.Context, trend domain.Trend) error
	DeleteTrend(ctx context.Context, trendID string) error
	GetSeasonBanner(ctx context.Context) (domain.SeasonBanner, bool, error)
	UpsertSeasonBanner(ctx context.Context, banner domain.SeasonBanner) error
}

// ReservationLedgerStore persiste las reservas por rango de fechas en base de datos.
type ReservationLedgerStore interface {
	ListActiveReservations(ctx context.Context) ([]domain.StockReservation, error)
	ReplaceEventReservations(ctx context.Context, eventID string, items []domain.StockReservation) error
	UpdateReservationStatusByEvent(ctx context.Context, eventID string, status domain.ReservationStatus, updatedAt time.Time) error
}

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
	PhotoURL          string
	DailyPriceDop     int
	TotalStock        int
	DamagedStock      int
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
	PhotoURL          *string
	DailyPriceDop     *int
	TotalStock        *int
	DamagedStock      *int
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
	PhotoURL      string
	TotalPriceDop int
	GuestCapacity int
	ItemsSummary  string
	Visible       *bool
	Items         []domain.PackageItem
}

type SaveCategoryCommand struct {
	ID      string
	Name    string
	IlloKey string
}

type Service struct {
	mu                sync.RWMutex
	reserveMu         sync.Mutex
	store             Store
	reservations      ReservationReader
	realtimePub       realtimedomain.Publisher
	articles          []domain.Article
	categories        []domain.Category
	packages          []domain.PackageBundle
	trends            []domain.Trend
	seasonBanner      domain.SeasonBanner
	stockReservations []domain.StockReservation
	emptyMode         bool
	hasDbCategories   bool
	hasDbArticles     bool
	hasDbTrends       bool
	hasDbBanner       bool
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
	s.trends = domain.CanonicalTrends()
	s.seasonBanner = domain.CanonicalSeasonBanner()
	s.stockReservations = nil
	s.emptyMode = false
	s.hasDbCategories = false
	s.hasDbArticles = false
	s.hasDbTrends = false
	s.hasDbBanner = false
	s.mu.Unlock()
}

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.articles = nil
	s.categories = nil
	s.packages = nil
	s.trends = nil
	s.seasonBanner = domain.SeasonBanner{ID: "default", TargetScreenCode: "s21", Active: false}
	s.stockReservations = nil
	s.emptyMode = true
	s.hasDbCategories = false
	s.hasDbArticles = false
	s.hasDbTrends = false
	s.hasDbBanner = false
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

// WithRealtimePublisher registra el publicador SSE para notificaciones del catálogo.
func (s *Service) WithRealtimePublisher(pub realtimedomain.Publisher) *Service {
	s.mu.Lock()
	s.realtimePub = pub
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
	out := make([]domain.Category, 0, len(s.categories))
	for _, cat := range s.categories {
		enriched := cat
		enriched.ItemCount = countArticlesForCategory(s.articles, cat)
		out = append(out, enriched)
	}
	return out
}

// CreateCategory valida, crea y persiste una nueva categoría del catálogo administrativo.
func (s *Service) CreateCategory(ctx context.Context, cmd SaveCategoryCommand) (domain.Category, error) {
	cleanName := strings.TrimSpace(cmd.Name)
	if !isValidCategoryName(cleanName) {
		return domain.Category{}, domain.ErrInvalidCatalog
	}
	s.syncFromStore(ctx)
	categoryID := buildCategoryID(cmd.ID, cleanName)
	illoKey := normalizeCategoryIlloKey(cmd.IlloKey)

	s.mu.Lock()
	if hasDuplicateCategoryLocked(s.categories, categoryID, cleanName, "") {
		s.mu.Unlock()
		return domain.Category{}, domain.ErrCategoryAlreadyExists
	}
	created := domain.Category{
		ID:        categoryID,
		Name:      cleanName,
		IlloKey:   illoKey,
		ItemCount: countArticlesForCategory(s.articles, domain.Category{ID: categoryID, Name: cleanName}),
	}
	s.categories = append(s.categories, created)
	s.hasDbCategories = true
	store := s.store
	s.mu.Unlock()

	if err := persistCategoryUpsert(ctx, store, created); err != nil {
		return domain.Category{}, err
	}
	s.publishCatalogUpdated(created.ID, "category.created")
	return created, nil
}

// UpdateCategory actualiza el nombre o ícono de una categoría existente manteniendo su identificador estable.
func (s *Service) UpdateCategory(
	ctx context.Context,
	categoryID string,
	cmd SaveCategoryCommand,
) (domain.Category, error) {
	s.syncFromStore(ctx)
	trimmedID := strings.TrimSpace(categoryID)
	cleanName := strings.TrimSpace(cmd.Name)
	if trimmedID == "" {
		return domain.Category{}, domain.ErrInvalidCatalog
	}
	if !isValidCategoryName(cleanName) {
		return domain.Category{}, domain.ErrInvalidCatalog
	}

	s.mu.Lock()
	targetIndex := findCategoryIndexLocked(s.categories, trimmedID)
	if targetIndex < 0 {
		s.mu.Unlock()
		return domain.Category{}, domain.ErrCategoryNotFound
	}
	existing := s.categories[targetIndex]
	if hasDuplicateCategoryLocked(s.categories, "", cleanName, existing.ID) {
		s.mu.Unlock()
		return domain.Category{}, domain.ErrCategoryAlreadyExists
	}
	resolvedIllo := existing.IlloKey
	if strings.TrimSpace(cmd.IlloKey) != "" {
		resolvedIllo = normalizeCategoryIlloKey(cmd.IlloKey)
	}
	updated := domain.Category{
		ID:        existing.ID,
		Name:      cleanName,
		IlloKey:   resolvedIllo,
		ItemCount: countArticlesForCategory(s.articles, domain.Category{ID: existing.ID, Name: cleanName}),
	}
	s.categories[targetIndex] = updated
	relinkedArticles := relinkArticlesCategoryLocked(s.articles, existing.Name, updated.ID)
	s.hasDbCategories = true
	store := s.store
	s.mu.Unlock()

	if err := persistCategoryUpsert(ctx, store, updated); err != nil {
		return domain.Category{}, err
	}
	if err := persistRelinkedArticles(ctx, store, relinkedArticles); err != nil {
		return domain.Category{}, err
	}
	s.publishCatalogUpdated(updated.ID, "category.updated")
	return updated, nil
}

// DeleteCategory elimina una categoría del catálogo cuando no tiene artículos vinculados.
func (s *Service) DeleteCategory(ctx context.Context, categoryID string) error {
	s.syncFromStore(ctx)
	trimmedID := strings.TrimSpace(categoryID)
	if trimmedID == "" {
		return domain.ErrInvalidCatalog
	}

	s.mu.Lock()
	targetIndex := findCategoryIndexLocked(s.categories, trimmedID)
	if targetIndex < 0 {
		s.mu.Unlock()
		return domain.ErrCategoryNotFound
	}
	targetCategory := s.categories[targetIndex]
	if countArticlesForCategory(s.articles, targetCategory) > 0 {
		s.mu.Unlock()
		return domain.ErrCategoryInUse
	}
	s.categories = append(s.categories[:targetIndex], s.categories[targetIndex+1:]...)
	s.hasDbCategories = true
	store := s.store
	s.mu.Unlock()

	if err := persistCategoryDelete(ctx, store, targetCategory.ID); err != nil {
		return err
	}
	s.publishCatalogUpdated(targetCategory.ID, "category.deleted")
	return nil
}

func (s *Service) Packages(ctx context.Context) []domain.PackageBundle {
	return s.PackagesFiltered(ctx, false)
}

// PackagesFiltered devuelve los paquetes del catálogo, opcionalmente incluyendo los ocultos.
func (s *Service) PackagesFiltered(ctx context.Context, includeHidden bool) []domain.PackageBundle {
	s.syncFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.PackageBundle, 0, len(s.packages))
	for _, pkg := range s.packages {
		isVisible := includeHidden
		if !isVisible {
			isVisible = pkg.Visible
		}
		if isVisible {
			out = append(out, pkg)
		}
	}
	return out
}

// DeletePackage elimina un paquete temático por identificador.
func (s *Service) DeletePackage(ctx context.Context, packageID string) error {
	trimmedID := strings.TrimSpace(packageID)
	if trimmedID == "" {
		return domain.ErrInvalidCatalog
	}
	s.syncFromStore(ctx)
	s.mu.Lock()
	idx := -1
	for i, existing := range s.packages {
		if existing.ID == trimmedID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.ErrArticleNotFound
	}
	s.packages = append(s.packages[:idx], s.packages[idx+1:]...)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if delStore, ok := store.(PackageDeleteStore); ok {
				if err := delStore.DeletePackage(ctx, trimmedID); err != nil {
					return err
				}
			}
		}
	}
	s.publishCatalogUpdated(trimmedID, "package.deleted")
	return nil
}

func (s *Service) Availability(ctx context.Context, articleID, month string) (domain.ArticleAvailability, error) {
	s.syncFromStore(ctx)
	targetID := strings.TrimSpace(articleID)
	if targetID == "" {
		targetID = "silla-tiffany"
	}
	targetMonth := strings.TrimSpace(month)
	if targetMonth == "" {
		targetMonth = time.Now().UTC().Format("2006-01")
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
	article.CategoryID = resolveCategoryIDLocked(s.categories, article.CategoryID)
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
	s.publishCatalogUpdated(article.ID, "article.created")
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
	updated.CategoryID = resolveCategoryIDLocked(s.categories, updated.CategoryID)
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
	s.publishCatalogUpdated(updated.ID, "article.updated")
	return updated, nil
}

// DeleteArticle removes an unused catalog article when it has no active reservations.
func (s *Service) DeleteArticle(ctx context.Context, articleID string) error {
	s.syncFromStore(ctx)
	trimmedID := strings.TrimSpace(articleID)
	if trimmedID == "" {
		return domain.ErrInvalidCatalog
	}

	s.mu.Lock()
	targetIndex := findArticleIndexLocked(s.articles, trimmedID)
	if targetIndex < 0 {
		s.mu.Unlock()
		return domain.ErrArticleNotFound
	}
	targetArticle := s.articles[targetIndex]
	if hasActiveReservationsForArticleLocked(s.stockReservations, targetArticle.ID) {
		s.mu.Unlock()
		return domain.ErrArticleInUse
	}
	s.articles = append(s.articles[:targetIndex], s.articles[targetIndex+1:]...)
	s.hasDbArticles = true
	store := s.store
	s.mu.Unlock()

	if err := persistArticleDelete(ctx, store, targetArticle.ID); err != nil {
		return err
	}
	s.publishCatalogUpdated(targetArticle.ID, "article.deleted")
	return nil
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
	reservations := append([]domain.StockReservation(nil), s.stockReservations...)
	s.mu.RUnlock()

	merged := make(map[string]int)
	if reader != nil {
		for dateISO, qty := range reader.ReservedStockByDateForMonth(ctx, articleID, month) {
			merged[dateISO] = qty
		}
	}
	ledgerByDate := reservedMapFromLedger(reservations, articleID, month)
	for dateISO, ledgerQty := range ledgerByDate {
		if ledgerQty > merged[dateISO] {
			merged[dateISO] = ledgerQty
		}
	}
	return merged
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
	usable := article.UsableStock()
	days := make([]domain.AvailabilityDay, 0, daysInMonth)
	for dayNum := 1; dayNum <= daysInMonth; dayNum++ {
		dateStr := fmt.Sprintf("%s-%02d", monthPrefix, dayNum)
		reserved := reservedByDate[dateStr]
		available := usable - reserved
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
	damaged := cmd.DamagedStock
	if damaged < 0 {
		damaged = 0
	}
	if damaged > cmd.TotalStock {
		damaged = cmd.TotalStock
	}
	usable := cmd.TotalStock - damaged
	available := usable
	if cmd.AvailableStock != nil {
		available = *cmd.AvailableStock
		if available > usable {
			available = usable
		}
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
		PhotoURL:          strings.TrimSpace(cmd.PhotoURL),
		DailyPriceDop:     cmd.DailyPriceDop,
		TotalStock:        cmd.TotalStock,
		DamagedStock:      damaged,
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
	if cmd.PhotoURL != nil {
		updated.PhotoURL = strings.TrimSpace(*cmd.PhotoURL)
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
	if cmd.DamagedStock != nil {
		if *cmd.DamagedStock < 0 {
			return domain.Article{}, domain.ErrInvalidCatalog
		}
		updated.DamagedStock = *cmd.DamagedStock
		if updated.DamagedStock > updated.TotalStock {
			updated.DamagedStock = updated.TotalStock
		}
		usable := updated.UsableStock()
		if updated.AvailableStock > usable {
			updated.AvailableStock = usable
		}
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
		PhotoURL:      strings.TrimSpace(cmd.PhotoURL),
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
	var dbReservations []domain.StockReservation
	var resErr error
	if ledger, ok := store.(ReservationLedgerStore); ok {
		dbReservations, resErr = ledger.ListActiveReservations(ctx)
	}
	s.mu.Lock()
	if artErr == nil {
		s.applySyncedArticlesLocked(dbArticles)
	}
	if catErr == nil {
		s.applySyncedCategoriesLocked(dbCats)
	}
	if pkgErr == nil {
		if len(dbPkgs) > 0 {
			s.packages = dbPkgs
		}
	}
	if resErr == nil {
		s.stockReservations = dbReservations
	}
	s.mu.Unlock()
	s.syncTrendsAndBannerFromStore(ctx, store)
}

func (s *Service) applySyncedArticlesLocked(dbArticles []domain.Article) {
	if len(dbArticles) > 0 {
		s.articles = dbArticles
		s.hasDbArticles = true
		return
	}
	if s.emptyMode {
		s.articles = dbArticles
		return
	}
	if s.hasDbArticles {
		s.articles = dbArticles
	}
}

func (s *Service) applySyncedCategoriesLocked(dbCats []domain.Category) {
	if len(dbCats) > 0 {
		s.categories = dbCats
		s.hasDbCategories = true
		return
	}
	if s.emptyMode {
		s.categories = dbCats
		return
	}
	if s.hasDbCategories {
		s.categories = dbCats
	}
}

func isValidCategoryName(cleanName string) bool {
	if len([]rune(cleanName)) < 2 {
		return false
	}
	return len([]rune(cleanName)) <= 40
}

func normalizeCategoryIlloKey(rawKey string) string {
	trimmed := strings.TrimSpace(rawKey)
	if trimmed == "" {
		return "cat-default"
	}
	lower := strings.ToLower(trimmed)
	isMediaURL := strings.HasPrefix(lower, "/v1/media/")
	if isMediaURL {
		return trimmed
	}
	isHTTP := strings.HasPrefix(lower, "http://")
	if isHTTP {
		return trimmed
	}
	isHTTPS := strings.HasPrefix(lower, "https://")
	if isHTTPS {
		return trimmed
	}
	return lower
}

func buildCategoryID(rawID, cleanName string) string {
	trimmedID := strings.TrimSpace(rawID)
	if trimmedID != "" {
		return slugifyCategoryAscii(trimmedID)
	}
	return slugifyCategoryAscii(cleanName)
}

func slugifyCategoryAscii(input string) string {
	replacer := strings.NewReplacer(
		"á", "a", "é", "e", "í", "i", "ó", "o", "ú", "u", "ü", "u", "ñ", "n",
		"Á", "a", "É", "e", "Í", "i", "Ó", "o", "Ú", "u", "Ü", "u", "Ñ", "n",
	)
	normalized := strings.ToLower(replacer.Replace(strings.TrimSpace(input)))
	var builder strings.Builder
	lastDash := false
	for _, r := range normalized {
		isAlphaNum := unicode.IsLetter(r) || unicode.IsDigit(r)
		if isAlphaNum {
			builder.WriteRune(r)
			lastDash = false
		} else if !lastDash {
			if builder.Len() > 0 {
				builder.WriteRune('-')
				lastDash = true
			}
		}
	}
	slug := strings.Trim(builder.String(), "-")
	if slug == "" {
		return "categoria"
	}
	return slug
}

func findCategoryIndexLocked(categories []domain.Category, targetIDOrName string) int {
	for idx, cat := range categories {
		if strings.EqualFold(cat.ID, targetIDOrName) {
			return idx
		}
		if strings.EqualFold(cat.Name, targetIDOrName) {
			return idx
		}
	}
	return -1
}

func hasDuplicateCategoryLocked(
	categories []domain.Category,
	candidateID string,
	candidateName string,
	excludeID string,
) bool {
	for _, cat := range categories {
		if excludeID != "" {
			if strings.EqualFold(cat.ID, excludeID) {
				continue
			}
		}
		if candidateID != "" {
			if strings.EqualFold(cat.ID, candidateID) {
				return true
			}
		}
		if strings.EqualFold(cat.Name, candidateName) {
			return true
		}
	}
	return false
}

func countArticlesForCategory(articles []domain.Article, cat domain.Category) int {
	count := 0
	for _, art := range articles {
		if articleMatchesCategory(art, cat) {
			count++
		}
	}
	return count
}

func articleMatchesCategory(art domain.Article, cat domain.Category) bool {
	if strings.EqualFold(strings.TrimSpace(art.CategoryID), strings.TrimSpace(cat.ID)) {
		return true
	}
	return strings.EqualFold(strings.TrimSpace(art.CategoryID), strings.TrimSpace(cat.Name))
}

func resolveCategoryIDLocked(categories []domain.Category, rawCategory string) string {
	trimmed := strings.TrimSpace(rawCategory)
	if trimmed == "" {
		if len(categories) > 0 {
			return categories[0].ID
		}
		return "mobiliario"
	}
	for _, cat := range categories {
		if strings.EqualFold(cat.ID, trimmed) {
			return cat.ID
		}
		if strings.EqualFold(cat.Name, trimmed) {
			return cat.ID
		}
	}
	return trimmed
}

func relinkArticlesCategoryLocked(
	articles []domain.Article,
	oldName string,
	targetID string,
) []domain.Article {
	var updated []domain.Article
	for idx, art := range articles {
		isLegacyNamed := strings.EqualFold(strings.TrimSpace(art.CategoryID), strings.TrimSpace(oldName))
		differsFromID := !strings.EqualFold(strings.TrimSpace(art.CategoryID), strings.TrimSpace(targetID))
		if isLegacyNamed {
			if differsFromID {
				articles[idx].CategoryID = targetID
				updated = append(updated, articles[idx])
			}
		}
	}
	return updated
}

func persistCategoryUpsert(ctx context.Context, store Store, cat domain.Category) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	catStore, ok := store.(CategoryStore)
	if !ok {
		return nil
	}
	return catStore.UpsertCategory(ctx, cat)
}

func persistCategoryDelete(ctx context.Context, store Store, categoryID string) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	catStore, ok := store.(CategoryStore)
	if !ok {
		return nil
	}
	return catStore.DeleteCategory(ctx, categoryID)
}

func findArticleIndexLocked(articles []domain.Article, targetID string) int {
	for idx, art := range articles {
		if strings.EqualFold(strings.TrimSpace(art.ID), targetID) {
			return idx
		}
	}
	return -1
}

func hasActiveReservationsForArticleLocked(
	reservations []domain.StockReservation,
	articleID string,
) bool {
	trimmed := strings.TrimSpace(articleID)
	for _, res := range reservations {
		if !res.Status.IsActive() {
			continue
		}
		if strings.EqualFold(strings.TrimSpace(res.ArticleID), trimmed) {
			return true
		}
	}
	return false
}

func persistArticleDelete(ctx context.Context, store Store, articleID string) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	delStore, ok := store.(ArticleDeleteStore)
	if !ok {
		return nil
	}
	return delStore.DeleteArticle(ctx, articleID)
}

func persistRelinkedArticles(ctx context.Context, store Store, articles []domain.Article) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	for _, art := range articles {
		if err := store.UpsertArticle(ctx, art); err != nil {
			return err
		}
	}
	return nil
}
