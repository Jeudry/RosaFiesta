package app

import (
	"context"
	"strings"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

// SaveTrendCommand encapsulates validated fields for creating or updating a catalog trend.
type SaveTrendCommand struct {
	ID              string
	Title           string
	EventsLabel     string
	PhotoKey        string
	PhotoURL        string
	PhotoBgHex      string
	LinkedCategory  string
	LinkedPackageID string
	SortOrder       int
	Active          *bool
}

// UpdateSeasonBannerCommand encapsulates fields for updating the Personal Home seasonal hero banner.
type UpdateSeasonBannerCommand struct {
	BadgeLabel       string
	Title            string
	CtaLabel         string
	TargetScreenCode string
	Active           bool
}

// Trends returns persisted catalog trends, optionally excluding inactive entries.
func (s *Service) Trends(ctx context.Context, includeInactive bool) []domain.Trend {
	s.syncFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Trend, 0, len(s.trends))
	for _, trend := range s.trends {
		if !includeInactive {
			if !trend.Active {
				continue
			}
		}
		out = append(out, trend)
	}
	return out
}

// CreateTrend validates, creates, and persists a new catalog trend.
func (s *Service) CreateTrend(ctx context.Context, cmd SaveTrendCommand) (domain.Trend, error) {
	cleanTitle := strings.TrimSpace(cmd.Title)
	if !isValidTrendTitle(cleanTitle) {
		return domain.Trend{}, domain.ErrInvalidCatalog
	}
	trendID := buildTrendID(cmd.ID, cleanTitle)
	active := true
	if cmd.Active != nil {
		active = *cmd.Active
	}
	s.syncFromStore(ctx)
	s.mu.Lock()
	if hasDuplicateTrendLocked(s.trends, trendID, cleanTitle, "") {
		s.mu.Unlock()
		return domain.Trend{}, domain.ErrTrendAlreadyExists
	}
	created := domain.Trend{
		ID:              trendID,
		Title:           cleanTitle,
		EventsLabel:     strings.TrimSpace(cmd.EventsLabel),
		PhotoKey:        normalizeTrendPhotoKey(cmd.PhotoKey),
		PhotoURL:        strings.TrimSpace(cmd.PhotoURL),
		PhotoBgHex:      normalizeHexBg(cmd.PhotoBgHex),
		LinkedCategory:  strings.TrimSpace(cmd.LinkedCategory),
		LinkedPackageID: strings.TrimSpace(cmd.LinkedPackageID),
		SortOrder:       cmd.SortOrder,
		Active:          active,
	}
	s.trends = append(s.trends, created)
	s.hasDbTrends = true
	store := s.store
	s.mu.Unlock()
	if err := persistTrendUpsert(ctx, store, created); err != nil {
		return domain.Trend{}, err
	}
	s.publishCatalogUpdated(created.ID, "trend.created")
	return created, nil
}

// UpdateTrend updates an existing catalog trend while keeping its identifier stable.
func (s *Service) UpdateTrend(
	ctx context.Context,
	trendID string,
	cmd SaveTrendCommand,
) (domain.Trend, error) {
	trimmedID := strings.TrimSpace(trendID)
	cleanTitle := strings.TrimSpace(cmd.Title)
	if trimmedID == "" {
		return domain.Trend{}, domain.ErrInvalidCatalog
	}
	if !isValidTrendTitle(cleanTitle) {
		return domain.Trend{}, domain.ErrInvalidCatalog
	}
	s.syncFromStore(ctx)
	s.mu.Lock()
	targetIdx := findTrendIndexLocked(s.trends, trimmedID)
	if targetIdx < 0 {
		s.mu.Unlock()
		return domain.Trend{}, domain.ErrTrendNotFound
	}
	existing := s.trends[targetIdx]
	if hasDuplicateTrendLocked(s.trends, "", cleanTitle, existing.ID) {
		s.mu.Unlock()
		return domain.Trend{}, domain.ErrTrendAlreadyExists
	}
	active := existing.Active
	if cmd.Active != nil {
		active = *cmd.Active
	}
	updated := domain.Trend{
		ID:              existing.ID,
		Title:           cleanTitle,
		EventsLabel:     strings.TrimSpace(cmd.EventsLabel),
		PhotoKey:        normalizeTrendPhotoKey(cmd.PhotoKey),
		PhotoURL:        strings.TrimSpace(cmd.PhotoURL),
		PhotoBgHex:      normalizeHexBg(cmd.PhotoBgHex),
		LinkedCategory:  strings.TrimSpace(cmd.LinkedCategory),
		LinkedPackageID: strings.TrimSpace(cmd.LinkedPackageID),
		SortOrder:       cmd.SortOrder,
		Active:          active,
	}
	s.trends[targetIdx] = updated
	s.hasDbTrends = true
	store := s.store
	s.mu.Unlock()
	if err := persistTrendUpsert(ctx, store, updated); err != nil {
		return domain.Trend{}, err
	}
	s.publishCatalogUpdated(updated.ID, "trend.updated")
	return updated, nil
}

// DeleteTrend deletes a catalog trend by identifier.
func (s *Service) DeleteTrend(ctx context.Context, trendID string) error {
	trimmedID := strings.TrimSpace(trendID)
	if trimmedID == "" {
		return domain.ErrTrendNotFound
	}
	s.syncFromStore(ctx)
	s.mu.Lock()
	targetIdx := findTrendIndexLocked(s.trends, trimmedID)
	if targetIdx < 0 {
		s.mu.Unlock()
		return domain.ErrTrendNotFound
	}
	removed := s.trends[targetIdx]
	remaining := make([]domain.Trend, 0, len(s.trends)-1)
	for idx, trend := range s.trends {
		if idx != targetIdx {
			remaining = append(remaining, trend)
		}
	}
	s.trends = remaining
	s.hasDbTrends = true
	store := s.store
	s.mu.Unlock()
	if err := persistTrendDelete(ctx, store, removed.ID); err != nil {
		return err
	}
	s.publishCatalogUpdated(removed.ID, "trend.deleted")
	return nil
}

// SeasonBanner returns the current Personal Home seasonal hero banner configuration.
func (s *Service) SeasonBanner(ctx context.Context) domain.SeasonBanner {
	s.syncFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.seasonBanner
}

// UpdateSeasonBanner validates and persists the Personal Home seasonal hero banner.
func (s *Service) UpdateSeasonBanner(
	ctx context.Context,
	cmd UpdateSeasonBannerCommand,
) (domain.SeasonBanner, error) {
	cleanBadge := strings.TrimSpace(cmd.BadgeLabel)
	cleanTitle := strings.TrimSpace(cmd.Title)
	cleanCta := strings.TrimSpace(cmd.CtaLabel)
	cleanTarget := strings.TrimSpace(cmd.TargetScreenCode)
	if cleanTarget == "" {
		cleanTarget = "s21"
	}
	if cmd.Active {
		if len([]rune(cleanTitle)) < 2 {
			return domain.SeasonBanner{}, domain.ErrInvalidSeasonBanner
		}
	}
	if len([]rune(cleanTitle)) > 80 {
		return domain.SeasonBanner{}, domain.ErrInvalidSeasonBanner
	}
	if len([]rune(cleanBadge)) > 48 {
		return domain.SeasonBanner{}, domain.ErrInvalidSeasonBanner
	}
	updated := domain.SeasonBanner{
		ID:               "default",
		BadgeLabel:       cleanBadge,
		Title:            cleanTitle,
		CtaLabel:         cleanCta,
		TargetScreenCode: cleanTarget,
		Active:           cmd.Active,
	}
	s.mu.Lock()
	s.seasonBanner = updated
	s.hasDbBanner = true
	store := s.store
	s.mu.Unlock()
	if err := persistSeasonBannerUpsert(ctx, store, updated); err != nil {
		return domain.SeasonBanner{}, err
	}
	s.publishCatalogUpdated(updated.ID, "season_banner.updated")
	return updated, nil
}

func (s *Service) syncTrendsAndBannerFromStore(ctx context.Context, store Store) {
	tbStore, ok := store.(TrendAndBannerStore)
	if !ok {
		return
	}
	dbTrends, trendErr := tbStore.ListTrends(ctx)
	dbBanner, hasBannerRow, bannerErr := tbStore.GetSeasonBanner(ctx)
	s.mu.Lock()
	if trendErr == nil {
		s.applySyncedTrendsLocked(dbTrends)
	}
	if bannerErr == nil {
		s.applySyncedSeasonBannerLocked(dbBanner, hasBannerRow)
	}
	s.mu.Unlock()
}

func (s *Service) applySyncedTrendsLocked(dbTrends []domain.Trend) {
	if len(dbTrends) > 0 {
		s.trends = dbTrends
		s.hasDbTrends = true
		return
	}
	if s.emptyMode {
		s.trends = dbTrends
		return
	}
	if s.hasDbTrends {
		s.trends = dbTrends
	}
}

func (s *Service) applySyncedSeasonBannerLocked(dbBanner domain.SeasonBanner, hasRow bool) {
	if hasRow {
		s.seasonBanner = dbBanner
		s.hasDbBanner = true
		return
	}
	if s.emptyMode {
		s.seasonBanner = domain.SeasonBanner{ID: "default", TargetScreenCode: "s21", Active: false}
		return
	}
	if s.hasDbBanner {
		s.seasonBanner = domain.SeasonBanner{ID: "default", TargetScreenCode: "s21", Active: false}
	}
}

func isValidTrendTitle(cleanTitle string) bool {
	if len([]rune(cleanTitle)) < 2 {
		return false
	}
	return len([]rune(cleanTitle)) <= 50
}

func buildTrendID(rawID, cleanTitle string) string {
	trimmed := strings.TrimSpace(rawID)
	if trimmed != "" {
		return slugifyCatalogID(trimmed, cleanTitle)
	}
	return "trend-" + buildSlugTail(cleanTitle)
}

func buildSlugTail(cleanText string) string {
	lower := strings.ToLower(cleanText)
	var b strings.Builder
	prevDash := false
	for _, r := range lower {
		if isAsciiSlugRune(r) {
			b.WriteRune(r)
			prevDash = false
		} else if !prevDash {
			b.WriteByte('-')
			prevDash = true
		}
	}
	out := strings.Trim(b.String(), "-")
	if out == "" {
		return "item"
	}
	return out
}

func isAsciiSlugRune(r rune) bool {
	if r >= 'a' {
		if r <= 'z' {
			return true
		}
	}
	if r >= '0' {
		return r <= '9'
	}
	return false
}

func normalizeTrendPhotoKey(rawKey string) string {
	trimmed := strings.TrimSpace(rawKey)
	if trimmed == "" {
		return "boda"
	}
	return strings.ToLower(trimmed)
}

func normalizeHexBg(rawHex string) string {
	trimmed := strings.TrimSpace(rawHex)
	if trimmed == "" {
		return "#F3EEF6"
	}
	if !strings.HasPrefix(trimmed, "#") {
		return "#F3EEF6"
	}
	if len(trimmed) != 7 {
		return "#F3EEF6"
	}
	return strings.ToUpper(trimmed)
}

func findTrendIndexLocked(trends []domain.Trend, targetID string) int {
	for idx, trend := range trends {
		if strings.EqualFold(strings.TrimSpace(trend.ID), targetID) {
			return idx
		}
	}
	return -1
}

func hasDuplicateTrendLocked(
	trends []domain.Trend,
	candidateID string,
	candidateTitle string,
	excludeID string,
) bool {
	for _, trend := range trends {
		if excludeID != "" {
			if strings.EqualFold(trend.ID, excludeID) {
				continue
			}
		}
		if candidateID != "" {
			if strings.EqualFold(trend.ID, candidateID) {
				return true
			}
		}
		if strings.EqualFold(trend.Title, candidateTitle) {
			return true
		}
	}
	return false
}

func persistTrendUpsert(ctx context.Context, store Store, trend domain.Trend) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	tbStore, ok := store.(TrendAndBannerStore)
	if !ok {
		return nil
	}
	return tbStore.UpsertTrend(ctx, trend)
}

func persistTrendDelete(ctx context.Context, store Store, trendID string) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	tbStore, ok := store.(TrendAndBannerStore)
	if !ok {
		return nil
	}
	return tbStore.DeleteTrend(ctx, trendID)
}

func persistSeasonBannerUpsert(ctx context.Context, store Store, banner domain.SeasonBanner) error {
	if store == nil {
		return nil
	}
	if !store.Enabled() {
		return nil
	}
	tbStore, ok := store.(TrendAndBannerStore)
	if !ok {
		return nil
	}
	return tbStore.UpsertSeasonBanner(ctx, banner)
}
