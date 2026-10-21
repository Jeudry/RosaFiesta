// Package app provee casos de uso de logística de montaje, rutas, incidencias y cola offline.
package app

import (
	"fmt"
	"strings"
	"sync"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
)

// Service administra la logística de montaje, checklist, rutas y sincronización offline.
type Service struct {
	mu           sync.RWMutex
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

// GetEventLogistics devuelve la logística de montaje de un evento (s66, s68, A11).
func (s *Service) GetEventLogistics(eventID string) (domain.EventLogistics, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	l, ok := s.logistics[eventID]
	if !ok {
		return domain.EventLogistics{}, domain.ErrOperationNotFound
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
			found = true
			break
		}
	}
	if !found {
		return domain.EventLogistics{}, domain.ErrChecklistNotFound
	}
	s.logistics[eventID] = l
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
