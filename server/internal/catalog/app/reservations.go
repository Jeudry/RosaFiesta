// Package app gestiona las reservas de inventario por ventana de fechas y el daño de piezas.
package app

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

// StockConflictError expone el detalle estructurado cuando una solicitud supera el stock libre.
type StockConflictError struct {
	Result domain.AvailabilityCheckResult
}

func (e StockConflictError) Error() string {
	msg := domain.BuildAvailabilityConflictMessage(e.Result.Conflicts)
	if msg == "" {
		return domain.ErrInsufficientStockForDate.Error()
	}
	return msg
}

func (e StockConflictError) Unwrap() error {
	return domain.ErrInsufficientStockForDate
}

// CheckAvailabilityForDate evalúa disponibilidad real para la fecha y ventana +/- 1 día.
func (s *Service) CheckAvailabilityForDate(
	ctx context.Context,
	rawDate string,
	excludeEventID string,
	requirements []domain.StockRequirement,
) (domain.AvailabilityCheckResult, error) {
	s.syncFromStore(ctx)
	eventISO, pickupISO, returnISO := domain.ParseEventDateRange(rawDate, time.Now().UTC())
	s.mu.RLock()
	articles := append([]domain.Article(nil), s.articles...)
	reservations := append([]domain.StockReservation(nil), s.stockReservations...)
	s.mu.RUnlock()

	aggregated := collapseRequirements(articles, requirements)
	conflicts := make([]domain.AvailabilityConflict, 0)
	for _, req := range aggregated {
		article, found := resolveArticleMatch(articles, req.ArticleID, "")
		if !found {
			continue
		}
		free := domain.EffectiveAvailableForDate(article, reservations, pickupISO, returnISO, excludeEventID)
		if req.Quantity > free {
			conflicts = append(conflicts, domain.AvailabilityConflict{
				ArticleID:   article.ID,
				ArticleName: article.Name,
				Requested:   req.Quantity,
				Available:   free,
				DateRange:   fmt.Sprintf("%s a %s", pickupISO, returnISO),
			})
		}
	}
	return domain.AvailabilityCheckResult{
		Available:     len(conflicts) == 0,
		EventDateISO:  eventISO,
		PickupDateISO: pickupISO,
		ReturnDateISO: returnISO,
		Conflicts:     conflicts,
	}, nil
}

// ReserveStockForEvent valida disponibilidad por fecha y guarda reservas 'held' o 'confirmed'.
func (s *Service) ReserveStockForEvent(
	ctx context.Context,
	eventID string,
	quoteID string,
	rawDate string,
	status domain.ReservationStatus,
	requirements []domain.StockRequirement,
) error {
	trimmedEvent := strings.TrimSpace(eventID)
	if trimmedEvent == "" {
		return nil
	}
	check, err := s.CheckAvailabilityForDate(ctx, rawDate, trimmedEvent, requirements)
	if err != nil {
		return err
	}
	if !check.Available {
		return StockConflictError{Result: check}
	}

	now := time.Now().UTC()
	s.mu.RLock()
	articles := append([]domain.Article(nil), s.articles...)
	store := s.store
	s.mu.RUnlock()

	collapsed := collapseRequirements(articles, requirements)
	nextRows := make([]domain.StockReservation, 0, len(collapsed))
	for idx, item := range collapsed {
		nextRows = append(nextRows, domain.StockReservation{
			ID:            fmt.Sprintf("res-%s-%02d-%s", trimmedEvent, idx+1, item.ArticleID),
			ArticleID:     item.ArticleID,
			EventID:       trimmedEvent,
			QuoteID:       strings.TrimSpace(quoteID),
			Quantity:      item.Quantity,
			PickupDateISO: check.PickupDateISO,
			ReturnDateISO: check.ReturnDateISO,
			Status:        status,
			CreatedAt:     now,
			UpdatedAt:     now,
		})
	}
	if ledger, ok := store.(ReservationLedgerStore); ok {
		if isStoreEnabled(store) {
			if err := ledger.ReplaceEventReservations(ctx, trimmedEvent, nextRows); err != nil {
				return err
			}
		}
	}
	s.mu.Lock()
	s.replaceEventReservationsLocked(trimmedEvent, nextRows)
	s.recomputeAvailableStockLocked(ctx, store)
	s.mu.Unlock()
	s.publishCatalogUpdated(trimmedEvent, "stock.reserved")
	return nil
}

// TransitionEventReservations cambia el estado de las reservas asociadas a un evento.
func (s *Service) TransitionEventReservations(
	ctx context.Context,
	eventID string,
	status domain.ReservationStatus,
) error {
	trimmedEvent := strings.TrimSpace(eventID)
	if trimmedEvent == "" {
		return nil
	}
	now := time.Now().UTC()
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if ledger, ok := store.(ReservationLedgerStore); ok {
		if isStoreEnabled(store) {
			if err := ledger.UpdateReservationStatusByEvent(ctx, trimmedEvent, status, now); err != nil {
				return err
			}
		}
	}
	s.mu.Lock()
	for idx := range s.stockReservations {
		if s.stockReservations[idx].EventID == trimmedEvent {
			s.stockReservations[idx].Status = status
			s.stockReservations[idx].UpdatedAt = now
		}
	}
	s.recomputeAvailableStockLocked(ctx, store)
	s.mu.Unlock()
	s.publishCatalogUpdated(trimmedEvent, string(status))
	return nil
}

// AdjustDamagedStock ajusta la cantidad en cuarentena por daño y actualiza el stock disponible.
func (s *Service) AdjustDamagedStock(
	ctx context.Context,
	articleRef string,
	delta int,
) (domain.Article, error) {
	s.syncFromStore(ctx)
	s.mu.Lock()
	targetIdx := -1
	for idx, item := range s.articles {
		if matchesArticleRef(item, articleRef, articleRef) {
			targetIdx = idx
			break
		}
	}
	if targetIdx < 0 {
		s.mu.Unlock()
		return domain.Article{}, domain.ErrArticleNotFound
	}
	updated := s.articles[targetIdx]
	nextDamaged := updated.DamagedStock + delta
	if nextDamaged < 0 {
		nextDamaged = 0
	}
	if nextDamaged > updated.TotalStock {
		nextDamaged = updated.TotalStock
	}
	updated.DamagedStock = nextDamaged
	s.articles[targetIdx] = updated
	store := s.store
	s.recomputeAvailableStockLocked(ctx, store)
	updated = s.articles[targetIdx]
	s.mu.Unlock()

	s.publishCatalogUpdated(updated.ID, "damaged_stock.adjusted")
	return updated, nil
}

type collapsedRequirement struct {
	ArticleID string
	Quantity  int
}

func collapseRequirements(articles []domain.Article, reqs []domain.StockRequirement) []collapsedRequirement {
	totals := make(map[string]int)
	order := make([]string, 0, len(reqs))
	for _, req := range reqs {
		if req.Quantity <= 0 {
			continue
		}
		article, ok := resolveArticleMatch(articles, req.ArticleRef, req.Name)
		if !ok {
			continue
		}
		if _, seen := totals[article.ID]; !seen {
			order = append(order, article.ID)
		}
		totals[article.ID] += req.Quantity
	}
	out := make([]collapsedRequirement, 0, len(order))
	for _, id := range order {
		out = append(out, collapsedRequirement{ArticleID: id, Quantity: totals[id]})
	}
	return out
}

func resolveArticleMatch(articles []domain.Article, ref, name string) (domain.Article, bool) {
	for _, article := range articles {
		if matchesArticleRef(article, ref, name) {
			return article, true
		}
	}
	return domain.Article{}, false
}

func matchesArticleRef(article domain.Article, ref, name string) bool {
	trimmedRef := strings.ToLower(strings.TrimSpace(ref))
	trimmedName := strings.ToLower(strings.TrimSpace(name))
	idLower := strings.ToLower(article.ID)
	nameLower := strings.ToLower(article.Name)
	if trimmedRef != "" {
		if idLower == trimmedRef {
			return true
		}
		if nameLower == trimmedRef {
			return true
		}
	}
	if trimmedName != "" {
		if nameLower == trimmedName {
			return true
		}
		if strings.Contains(nameLower, trimmedName) {
			return true
		}
	}
	return false
}

func (s *Service) replaceEventReservationsLocked(eventID string, nextRows []domain.StockReservation) {
	kept := make([]domain.StockReservation, 0, len(s.stockReservations)+len(nextRows))
	for _, existing := range s.stockReservations {
		if existing.EventID != eventID {
			kept = append(kept, existing)
		}
	}
	kept = append(kept, nextRows...)
	s.stockReservations = kept
}

func (s *Service) recomputeAvailableStockLocked(ctx context.Context, store Store) {
	nowISO := time.Now().UTC().Format("2006-01-02")
	for idx, article := range s.articles {
		peakReserved := computePeakConcurrentDemand(s.stockReservations, article.ID, nowISO)
		nextAvail := article.UsableStock() - peakReserved
		if nextAvail < 0 {
			nextAvail = 0
		}
		s.articles[idx].AvailableStock = nextAvail
		if isStoreEnabled(store) {
			_ = store.UpsertArticle(ctx, s.articles[idx])
		}
	}
}

func computePeakConcurrentDemand(reservations []domain.StockReservation, articleID, minReturnISO string) int {
	active := make([]domain.StockReservation, 0)
	for _, res := range reservations {
		if res.ArticleID != articleID {
			continue
		}
		if !res.Status.IsActive() {
			continue
		}
		if res.ReturnDateISO >= minReturnISO {
			active = append(active, res)
		}
	}
	peak := 0
	for _, anchor := range active {
		overlapQty := 0
		for _, other := range active {
			overlaps := anchor.PickupDateISO <= other.ReturnDateISO && other.PickupDateISO <= anchor.ReturnDateISO
			if overlaps {
				overlapQty += other.Quantity
			}
		}
		if overlapQty > peak {
			peak = overlapQty
		}
	}
	return peak
}

func isStoreEnabled(store Store) bool {
	if store == nil {
		return false
	}
	return store.Enabled()
}

func reservedMapFromLedger(
	reservations []domain.StockReservation,
	articleID string,
	month string,
) map[string]int {
	reservedByDate := make(map[string]int)
	if len(reservations) == 0 {
		return reservedByDate
	}
	startOfMonth, err := time.Parse("2006-01", month)
	if err != nil {
		return reservedByDate
	}
	nextMonth := startOfMonth.AddDate(0, 1, 0)
	daysInMonth := int(nextMonth.Sub(startOfMonth).Hours() / 24)
	for dayNum := 1; dayNum <= daysInMonth; dayNum++ {
		dateStr := fmt.Sprintf("%s-%02d", month, dayNum)
		total := 0
		for _, res := range reservations {
			if res.ArticleID != articleID {
				continue
			}
			if !res.Status.IsActive() {
				continue
			}
			if domain.DatesOverlap(res.PickupDateISO, res.ReturnDateISO, dateStr, dateStr) {
				total += res.Quantity
			}
		}
		if total > 0 {
			reservedByDate[dateStr] = total
		}
	}
	return reservedByDate
}

func (s *Service) publishCatalogUpdated(entityID, reason string) {
	s.mu.RLock()
	pub := s.realtimePub
	s.mu.RUnlock()
	if pub == nil {
		return
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     realtimedomain.EventCatalogUpdated,
		EntityID: entityID,
		Audience: realtimedomain.AudienceAll,
		Summary:  fmt.Sprintf("Inventario actualizado (%s)", reason),
	})
}
