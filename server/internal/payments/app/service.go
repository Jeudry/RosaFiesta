// Package app implementa los casos de uso de pagos, comprobantes y finanzas.
package app

import (
	"fmt"
	"strings"
	"sync"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
)

// Service administra el registro y verificación de pagos y métricas de cobro.
type Service struct {
	mu       sync.RWMutex
	payments map[string]domain.Payment
	order    []string
	summary  domain.FinanceSummary
	nextSeq  int
}

// NewService crea un servicio de pagos con los datos canónicos del mes.
func NewService() *Service {
	canonical := domain.CanonicalPayments()
	payments := make(map[string]domain.Payment, len(canonical))
	order := make([]string, 0, len(canonical))
	for _, p := range canonical {
		payments[p.ID] = p
		order = append(order, p.ID)
	}
	return &Service{
		payments: payments,
		order:    order,
		summary:  domain.CanonicalFinanceSummary(),
		nextSeq:  200,
	}
}

// GetSummary devuelve el resumen financiero del mes (s69, A20).
func (s *Service) GetSummary() domain.FinanceSummary {
	s.mu.RLock()
	defer s.mu.RUnlock()

	return s.summary
}

// ListPayments devuelve los pagos registrados, filtrando opcionalmente por evento.
func (s *Service) ListPayments(eventID string) []domain.Payment {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]domain.Payment, 0, len(s.order))
	for _, id := range s.order {
		p := s.payments[id]
		if eventID != "" && p.EventID != eventID {
			continue
		}
		result = append(result, p)
	}
	return result
}

// RegisterPayment registra un nuevo pago de anticipo o saldo (s30, s31, s38, s70).
func (s *Service) RegisterPayment(
	eventID string,
	eventTitle string,
	clientName string,
	amountRD int,
	method domain.PaymentMethod,
	kind domain.PaymentKind,
	referenceCode string,
) (domain.Payment, error) {
	if amountRD <= 0 {
		return domain.Payment{}, domain.ErrInvalidPaymentAmount
	}
	if err := domain.ValidateMethod(method); err != nil {
		return domain.Payment{}, err
	}
	s.mu.Lock()
	defer s.mu.Unlock()

	s.nextSeq++
	id := fmt.Sprintf("pay-%d", s.nextSeq)
	receipt := fmt.Sprintf("REC-2026-%04d", s.nextSeq)
	status := domain.PaymentVerified
	if method == domain.MethodTransfer {
		status = domain.PaymentPendingVerification
	}
	payment := domain.Payment{
		ID:            id,
		ReceiptNumber: receipt,
		EventID:       eventID,
		EventTitle:    strings.TrimSpace(eventTitle),
		ClientName:    strings.TrimSpace(clientName),
		AmountRD:      amountRD,
		Method:        method,
		Kind:          kind,
		PaidDateLabel: "20 oct",
		ReferenceCode: strings.TrimSpace(referenceCode),
		Status:        status,
	}
	s.payments[id] = payment
	s.order = append(s.order, id)
	s.summary.OctRevenueRD += amountRD
	if s.summary.AccountsReceivable >= amountRD {
		s.summary.AccountsReceivable -= amountRD
	}
	return payment, nil
}
