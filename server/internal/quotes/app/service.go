// Package app implementa los casos de uso de cotizaciones, contratos y asistente Rosa IA.
package app

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

// Store persiste cotizaciones y firmas en almacenamiento duradero (PostgreSQL).
type Store interface {
	// Enabled indica si hay conexión PostgreSQL activa.
	Enabled() bool
	// ListQuotes consulta las cotizaciones almacenadas en PostgreSQL.
	ListQuotes(ctx context.Context) ([]domain.Quote, error)
	// UpsertQuote inserta o actualiza una cotización en PostgreSQL.
	UpsertQuote(ctx context.Context, q domain.Quote, now time.Time) error
}

// Service administra las cotizaciones y el flujo conversacional de Rosa IA.
type Service struct {
	mu      sync.RWMutex
	store   Store
	quotes  map[string]domain.Quote
	order   []string
	nextSeq int
}

// NewService crea un servicio de cotizaciones con las cotizaciones canónicas.
func NewService() *Service {
	canonical := domain.CanonicalQuotes()
	quotes := make(map[string]domain.Quote, len(canonical))
	order := make([]string, 0, len(canonical))
	for _, q := range canonical {
		quotes[q.ID] = q
		order = append(order, q.ID)
	}
	return &Service{
		quotes:  quotes,
		order:   order,
		nextSeq: 1200,
	}
}

// WithStore conecta un repositorio PostgreSQL opcional al servicio de cotizaciones.
func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.store = store
	s.syncFromStoreLocked(context.Background())
	return s
}

func (s *Service) syncFromStoreLocked(ctx context.Context) {
	if s.store == nil || !s.store.Enabled() {
		return
	}
	dbQuotes, err := s.store.ListQuotes(ctx)
	if err != nil {
		return
	}
	for _, dbQ := range dbQuotes {
		if existing, ok := s.quotes[dbQ.ID]; ok {
			existing.Status = dbQ.Status
			existing.TotalRD = dbQ.TotalRD
			existing.DepositRD = dbQ.DepositRD
			existing.BalanceRD = dbQ.BalanceRD
			if dbQ.SignedByName != "" {
				existing.SignedByName = dbQ.SignedByName
			}
			if dbQ.RejectionReason != "" {
				existing.RejectionReason = dbQ.RejectionReason
			}
			s.quotes[dbQ.ID] = existing
		} else {
			s.quotes[dbQ.ID] = dbQ
			s.order = append(s.order, dbQ.ID)
		}
	}
}

// ListQuotes devuelve todas las cotizaciones, filtradas opcionalmente por estado.
func (s *Service) ListQuotes(status string) []domain.Quote {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(context.Background())

	result := make([]domain.Quote, 0, len(s.order))
	for _, id := range s.order {
		q := s.quotes[id]
		if status != "" && string(q.Status) != status {
			continue
		}
		result = append(result, q)
	}
	return result
}

// GetQuote devuelve una cotización por su ID.
func (s *Service) GetQuote(id string) (domain.Quote, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(context.Background())

	q, ok := s.quotes[id]
	if !ok {
		return domain.Quote{}, domain.ErrQuoteNotFound
	}
	return q, nil
}

// CreateQuote registra una nueva cotización con cálculo automático de anticipo (30%).
func (s *Service) CreateQuote(
	eventTitle string,
	clientName string,
	eventDateLabel string,
	venue string,
	guests int,
	lines []domain.QuoteLine,
	deliveryFeeRD int,
	discountRD int,
) (domain.Quote, error) {
	total, deposit, balance, err := domain.CalculateQuoteTotals(lines, deliveryFeeRD, discountRD)
	if err != nil {
		return domain.Quote{}, err
	}
	s.mu.Lock()
	defer s.mu.Unlock()

	s.nextSeq++
	id := fmt.Sprintf("quo-%d", s.nextSeq)
	code := fmt.Sprintf("COT-2026-%d", s.nextSeq)
	quote := domain.Quote{
		ID:             id,
		Code:           code,
		EventTitle:     strings.TrimSpace(eventTitle),
		ClientName:     strings.TrimSpace(clientName),
		EventDateLabel: strings.TrimSpace(eventDateLabel),
		Venue:          strings.TrimSpace(venue),
		Guests:         guests,
		Lines:          lines,
		DeliveryFeeRD:  deliveryFeeRD,
		DiscountRD:     discountRD,
		TotalRD:        total,
		DepositPercent: 30,
		DepositRD:      deposit,
		BalanceRD:      balance,
		Status:         domain.QuoteStatusPending,
	}
	s.quotes[id] = quote
	s.order = append(s.order, id)
	if s.store != nil {
		_ = s.store.UpsertQuote(context.Background(), quote, time.Now().UTC())
	}
	return quote, nil
}

// SignContract registra la firma digital del cliente sobre una cotización (s28).
func (s *Service) SignContract(id string, signedByName string, signatureSVG string) (domain.Quote, error) {
	if strings.TrimSpace(signatureSVG) == "" || strings.TrimSpace(signedByName) == "" {
		return domain.Quote{}, domain.ErrInvalidSignature
	}
	s.mu.Lock()
	defer s.mu.Unlock()

	q, ok := s.quotes[id]
	if !ok {
		return domain.Quote{}, domain.ErrQuoteNotFound
	}
	q.Status = domain.QuoteStatusSigned
	q.SignedByName = strings.TrimSpace(signedByName)
	q.SignatureSVG = signatureSVG
	s.quotes[id] = q
	if s.store != nil {
		_ = s.store.UpsertQuote(context.Background(), q, time.Now().UTC())
	}
	return q, nil
}

// DeclineQuote rechaza una solicitud de cotización con motivo (A08).
func (s *Service) DeclineQuote(id string, reason string) (domain.Quote, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	q, ok := s.quotes[id]
	if !ok {
		return domain.Quote{}, domain.ErrQuoteNotFound
	}
	q.Status = domain.QuoteStatusDeclined
	q.RejectionReason = strings.TrimSpace(reason)
	s.quotes[id] = q
	if s.store != nil {
		_ = s.store.UpsertQuote(context.Background(), q, time.Now().UTC())
	}
	return q, nil
}

// GenerateRosaProposal ejecuta el asistente Rosa IA para armar propuesta y moodboard (s40-s48, A16-A18).
func (s *Service) GenerateRosaProposal(eventType string, guests int, budgetRD int) domain.RosaMoodboardProposal {
	return domain.BuildRosaProposal(eventType, guests, budgetRD)
}
