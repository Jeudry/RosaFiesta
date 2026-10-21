// Package app provee casos de uso de logística de montaje, rutas, incidencias y cola offline.
package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
)

// Store persiste y consulta operaciones de montaje, checklist e incidencias en PostgreSQL.
type Store interface {
	// Enabled indica si hay conexión PostgreSQL activa.
	Enabled() bool
	// UpsertChecklistItem persiste el estado de un ítem del checklist en PostgreSQL.
	UpsertChecklistItem(ctx context.Context, eventID string, item domain.ChecklistItem) error
	// ListChecklistItems devuelve los ítems de checklist persistidos para un evento en PostgreSQL.
	ListChecklistItems(ctx context.Context, eventID string) (map[string]domain.ChecklistItem, error)
	// RecordDamageIncident persiste un reporte de daño post-evento en PostgreSQL.
	RecordDamageIncident(ctx context.Context, report domain.DamageReport, now time.Time) error
}

// Service administra la logística de montaje, checklist, rutas y sincronización offline.
type Service struct {
	mu           sync.RWMutex
	store        Store
	logistics    map[string]domain.EventLogistics
	routeStops   []domain.RouteStop
	offlineQueue []domain.OfflineMutation
	nextDamage   int
}

// NewService crea un servicio de operaciones con la logística canónica.
func NewService() *Service {
	canonical := domain.CanonicalLogistics()
	logistics := make(map[string]domain.EventLogistics, len(canonical))
	for _, l := range canonical {
		logistics[l.EventID] = l
	}
	return &Service{
		logistics:    logistics,
		routeStops:   domain.CanonicalRouteStops(),
		offlineQueue: domain.CanonicalOfflineQueue(),
		nextDamage:   10,
	}
}

// WithStore conecta un repositorio PostgreSQL opcional al servicio de operaciones.
func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.store = store
	return s
}

// GetEventLogistics devuelve la logística de montaje de un evento (s66, s68, A11).
func (s *Service) GetEventLogistics(eventID string) (domain.EventLogistics, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	l, ok := s.logistics[eventID]
	if !ok {
		return domain.EventLogistics{}, domain.ErrOperationNotFound
	}
	if s.store != nil && s.store.Enabled() {
		if dbItems, err := s.store.ListChecklistItems(context.Background(), eventID); err == nil && len(dbItems) > 0 {
			for i := range l.Checklist {
				if persisted, exists := dbItems[l.Checklist[i].ID]; exists {
					l.Checklist[i].Completed = persisted.Completed
					l.Checklist[i].CompletedBy = persisted.CompletedBy
					if persisted.Completed {
						if l.Checklist[i].CompletedAt == "" {
							l.Checklist[i].CompletedAt = persisted.CompletedAt
						}
					} else {
						l.Checklist[i].CompletedAt = ""
					}
				}
			}
			s.logistics[eventID] = l
		}
	}
	return l, nil
}

// ToggleChecklistItem marca o desmarca un paso del checklist de montaje (s68, A11).
func (s *Service) ToggleChecklistItem(eventID string, itemID string, completed bool, staffName string) (domain.EventLogistics, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	l, ok := s.logistics[eventID]
	if !ok {
		return domain.EventLogistics{}, domain.ErrOperationNotFound
	}
	var updatedItem domain.ChecklistItem
	found := false
	for i := range l.Checklist {
		if l.Checklist[i].ID == itemID {
			l.Checklist[i].Completed = completed
			if completed {
				l.Checklist[i].CompletedBy = strings.TrimSpace(staffName)
				l.Checklist[i].CompletedAt = "11:15 AM"
			} else {
				l.Checklist[i].CompletedBy = ""
				l.Checklist[i].CompletedAt = ""
			}
			updatedItem = l.Checklist[i]
			found = true
			break
		}
	}
	if !found {
		return domain.EventLogistics{}, domain.ErrChecklistNotFound
	}
	s.logistics[eventID] = l
	if s.store != nil && s.store.Enabled() {
		_ = s.store.UpsertChecklistItem(context.Background(), eventID, updatedItem)
	}
	return l, nil
}

// ListRouteStops devuelve la hoja de ruta del día (A13).
func (s *Service) ListRouteStops() []domain.RouteStop {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]domain.RouteStop, len(s.routeStops))
	copy(result, s.routeStops)
	return result
}

// ReportDamage registra un daño o pérdida post-evento (A14).
func (s *Service) ReportDamage(
	eventID string,
	articleCode string,
	articleName string,
	damagedUnits int,
	missingUnits int,
	replacementRD int,
	notes string,
) (domain.DamageReport, error) {
	if replacementRD < 0 {
		return domain.DamageReport{}, domain.ErrInvalidReplacement
	}
	s.mu.Lock()
	defer s.mu.Unlock()

	l, ok := s.logistics[eventID]
	if !ok {
		return domain.DamageReport{}, domain.ErrOperationNotFound
	}
	s.nextDamage++
	report := domain.DamageReport{
		ID:            fmt.Sprintf("dmg-%d", s.nextDamage),
		EventID:       eventID,
		ArticleCode:   articleCode,
		ArticleName:   articleName,
		DamagedUnits:  damagedUnits,
		MissingUnits:  missingUnits,
		ReplacementRD: replacementRD,
		Notes:         strings.TrimSpace(notes),
	}
	l.DamageReports = append(l.DamageReports, report)
	s.logistics[eventID] = l
	if s.store != nil && s.store.Enabled() {
		_ = s.store.RecordDamageIncident(context.Background(), report, time.Now().UTC())
	}
	return report, nil
}

// ListOfflineQueue devuelve la cola de cambios pendientes en modo sin conexión (A37).
func (s *Service) ListOfflineQueue() []domain.OfflineMutation {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]domain.OfflineMutation, len(s.offlineQueue))
	copy(result, s.offlineQueue)
	return result
}

// SyncOfflineQueue sincroniza todos los cambios pendientes en cola (A37).
func (s *Service) SyncOfflineQueue() []domain.OfflineMutation {
	s.mu.Lock()
	defer s.mu.Unlock()

	for i := range s.offlineQueue {
		s.offlineQueue[i].Synced = true
	}
	result := make([]domain.OfflineMutation, len(s.offlineQueue))
	copy(result, s.offlineQueue)
	return result
}
