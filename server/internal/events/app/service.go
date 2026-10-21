// Package app provee casos de uso para la gestión de eventos, cambios de fecha y cancelación.
package app

import (
	"context"
	"strings"
	"sync"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
)

// Service administra el ciclo de vida de los eventos en RosaFiesta.
type Service struct {
	mu        sync.RWMutex
	templates []domain.EventTemplate
	events    map[string]domain.Event
	order     []string
}

// NewService crea un servicio de eventos inicializado con los eventos canónicos.
func NewService() *Service {
	canonical := domain.CanonicalEvents()
	events := make(map[string]domain.Event, len(canonical))
	order := make([]string, 0, len(canonical))
	for _, e := range canonical {
		events[e.ID] = e
		order = append(order, e.ID)
	}
	return &Service{
		templates: domain.CanonicalTemplates(),
		events:    events,
		order:     order,
	}
}

// ActiveEventsForClient cuenta los eventos activos de un cliente para validar s88 (eliminación de cuenta).
func (s *Service) ActiveEventsForClient(_ context.Context, clientName string) int {
	s.mu.RLock()
	defer s.mu.RUnlock()

	target := strings.TrimSpace(strings.ToLower(clientName))
	count := 0
	for _, id := range s.order {
		evt := s.events[id]
		if strings.ToLower(evt.ClientName) == target && evt.IsActive() {
			count++
		}
	}
	return count
}

// ListTemplates devuelve las 6 plantillas de eventos.
func (s *Service) ListTemplates() []domain.EventTemplate {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]domain.EventTemplate, len(s.templates))
	copy(result, s.templates)
	return result
}

// ListEvents devuelve los eventos filtrados opcionalmente por estado.
func (s *Service) ListEvents(status string) []domain.Event {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]domain.Event, 0, len(s.order))
	for _, id := range s.order {
		evt := s.events[id]
		if status != "" && string(evt.Status) != status {
			continue
		}
		result = append(result, evt)
	}
	return result
}

// GetEvent obtiene el detalle de un evento por ID.
func (s *Service) GetEvent(id string) (domain.Event, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	evt, ok := s.events[id]
	if !ok {
		return domain.Event{}, domain.ErrEventNotFound
	}
	return evt, nil
}

// RequestDateChange ejecuta un cambio de fecha respetando la política (s36).
func (s *Service) RequestDateChange(id string, newDateLabel string, newDaysUntilEvent int) (domain.Event, domain.DateChangeEvaluation, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	evt, ok := s.events[id]
	if !ok {
		return domain.Event{}, domain.DateChangeEvaluation{}, domain.ErrEventNotFound
	}
	eval, err := domain.EvaluateDateChange(evt)
	if err != nil {
		return domain.Event{}, domain.DateChangeEvaluation{}, err
	}
	evt.DateLabel = newDateLabel
	evt.DaysUntilEvent = newDaysUntilEvent
	evt.DateChangesUsed = eval.NextCount
	evt.TotalRD += eval.FeeRD
	s.events[id] = evt
	return evt, eval, nil
}

// CancelEvent cancela un evento y devuelve el desglose de reembolso según política (s37).
func (s *Service) CancelEvent(id string) (domain.Event, domain.RefundPolicyResult, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	evt, ok := s.events[id]
	if !ok {
		return domain.Event{}, domain.RefundPolicyResult{}, domain.ErrEventNotFound
	}
	if evt.Status == domain.StatusCompleted {
		return domain.Event{}, domain.RefundPolicyResult{}, domain.ErrCannotCancelCompleted
	}
	refund := domain.CalculateCancellationRefund(evt.PaidDepositRD, evt.DaysUntilEvent)
	evt.Status = domain.StatusCancelled
	s.events[id] = evt
	return evt, refund, nil
}
