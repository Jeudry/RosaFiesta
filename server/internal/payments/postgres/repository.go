// Package postgres implementa la persistencia PostgreSQL para pagos, NCF y reembolsos.
package postgres

import (
	"context"
	"strings"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	"github.com/Jeudry/rosafiesta/server/internal/payments/postgres/sqlcgen"
)

// Repository persiste y consulta pagos, secuencias NCF y reembolsos en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository crea un repositorio de pagos respaldado por pgxpool.
func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

// Enabled indica si el repositorio tiene una conexión PostgreSQL activa.
func (r *Repository) Enabled() bool {
	if r == nil {
		return false
	}
	return r.pool != nil
}

// AllocateNextNCF reserva y formatea el siguiente comprobante fiscal secuencial DGII.
func (r *Repository) AllocateNextNCF(ctx context.Context, ncfType string) (string, error) {
	if !r.Enabled() {
		return "", nil
	}
	normalized := strings.ToUpper(strings.TrimSpace(ncfType))
	if normalized != "B01" {
		normalized = "B02"
	}
	row, err := r.queries.AllocateNextNCF(ctx, normalized)
	if err != nil {
		return "", err
	}
	return domain.FormatNCFNumber(row.Prefix, row.LastNumber), nil
}

// UpsertPayment inserta o actualiza un registro de pago con su desglose fiscal y estado.
func (r *Repository) UpsertPayment(ctx context.Context, payment domain.Payment, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	idemKey := strings.TrimSpace(payment.ReferenceCode)
	if idemKey == "" {
		idemKey = payment.ID
	}
	receipt := strings.TrimSpace(payment.ReceiptNumber)
	if receipt == "" {
		receipt = payment.ID
	}
	kind := domain.NormalizePaymentKind(string(payment.Kind))
	params := sqlcgen.UpsertPaymentParams{
		ID:                     payment.ID,
		ReceiptNumber:          receipt,
		IdempotencyKey:         idemKey,
		EventID:                payment.EventID,
		EventTitle:             payment.EventTitle,
		ClientID:               payment.ClientID,
		ClientEmail:            payment.ClientEmail,
		ClientName:             payment.ClientName,
		AmountDop:              payment.AmountRD,
		Method:                 string(payment.Method),
		Kind:                   string(kind),
		BankName:               payment.BankName,
		PaidDateLabel:          payment.PaidDateLabel,
		Status:                 string(payment.Status),
		VoucherUrl:             payment.VoucherURL,
		ReferenceNumber:        payment.ReferenceNumber,
		VerifiedBy:             payment.VerifiedBy,
		VerifiedAtIso:          payment.VerifiedAtISO,
		RejectionReason:        payment.RejectionReason,
		NcfType:                payment.NCFType,
		NcfNumber:              payment.NCFNumber,
		ClientRnc:              payment.ClientRNC,
		SubtotalDop:            payment.SubtotalDOP,
		ItbisDop:               payment.ITBISDOP,
		SecurityDepositDop:     payment.SecurityDepositDOP,
		GatewaySessionID:       payment.GatewaySessionID,
		GatewayPaymentIntentID: payment.GatewayPaymentIntentID,
		CreatedAt:              now,
	}
	return r.queries.UpsertPayment(ctx, params)
}

// ListPayments lista todos los pagos almacenados ordenados cronológicamente.
func (r *Repository) ListPayments(ctx context.Context) ([]domain.Payment, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListPayments(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Payment, 0, len(rows))
	for _, row := range rows {
		out = append(out, mapPaymentRow(row))
	}
	return out, nil
}

func mapPaymentRow(row sqlcgen.ListPaymentsRow) domain.Payment {
	receipt := strings.TrimSpace(row.ReceiptNumber)
	if receipt == "" {
		receipt = row.ID
	}
	kind := domain.NormalizePaymentKind(row.Kind)
	return domain.Payment{
		ID:                     row.ID,
		ReceiptNumber:          receipt,
		EventID:                row.EventID,
		EventTitle:             row.EventTitle,
		ClientID:               row.ClientID,
		ClientEmail:            row.ClientEmail,
		ClientName:             row.ClientName,
		AmountRD:               row.AmountDop,
		Method:                 domain.PaymentMethod(row.Method),
		Kind:                   kind,
		BankName:               row.BankName,
		PaidDateLabel:          row.PaidDateLabel,
		ReferenceCode:          row.IdempotencyKey,
		Status:                 domain.PaymentStatus(row.Status),
		VoucherURL:             row.VoucherUrl,
		ReferenceNumber:        row.ReferenceNumber,
		VerifiedBy:             row.VerifiedBy,
		VerifiedAtISO:          row.VerifiedAtIso,
		RejectionReason:        row.RejectionReason,
		NCFType:                row.NcfType,
		NCFNumber:              row.NcfNumber,
		ClientRNC:              row.ClientRnc,
		SubtotalDOP:            row.SubtotalDop,
		ITBISDOP:               row.ItbisDop,
		SecurityDepositDOP:     row.SecurityDepositDop,
		GatewaySessionID:       row.GatewaySessionID,
		GatewayPaymentIntentID: row.GatewayPaymentIntentID,
	}
}

// UpsertRefund inserta o actualiza una solicitud de reembolso.
func (r *Repository) UpsertRefund(ctx context.Context, refund domain.Refund, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	status := string(refund.Status)
	if status == "" {
		status = string(domain.RefundStatusPending)
	}
	params := sqlcgen.UpsertRefundParams{
		ID:              refund.ID,
		IdempotencyKey:  "refund:" + refund.ID,
		EventID:         refund.EventID,
		EventTitle:      refund.EventTitle,
		ClientName:      refund.ClientName,
		PaidDop:         refund.PaidRD,
		RefundPercent:   refund.RefundPercent,
		RefundDop:       refund.RefundDop,
		Reason:          refund.Reason,
		Status:          status,
		ApprovedBy:      refund.ApprovedBy,
		ApprovedAtLabel: refund.ApprovedAtLabel,
		GatewayRefundID: refund.GatewayRefundID,
		GatewayStatus:   refund.GatewayStatus,
		CreatedAt:       now,
	}
	return r.queries.UpsertRefund(ctx, params)
}

// ListRefunds lista todas las solicitudes de reembolso ordenadas de más reciente a más antigua.
func (r *Repository) ListRefunds(ctx context.Context) ([]domain.Refund, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListRefunds(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.Refund, 0, len(rows))
	for _, row := range rows {
		out = append(out, mapRefundRow(row))
	}
	return out, nil
}

func mapRefundRow(row sqlcgen.ListRefundsRow) domain.Refund {
	status := domain.RefundStatus(row.Status)
	if status == "" {
		status = domain.RefundStatusPending
	}
	return domain.Refund{
		ID:              row.ID,
		EventID:         row.EventID,
		EventTitle:      row.EventTitle,
		ClientName:      row.ClientName,
		PaidRD:          row.PaidDop,
		RefundPercent:   row.RefundPercent,
		RefundDop:       row.RefundDop,
		Reason:          row.Reason,
		Status:          status,
		ApprovedBy:      row.ApprovedBy,
		ApprovedAtLabel: row.ApprovedAtLabel,
		GatewayRefundID: row.GatewayRefundID,
		GatewayStatus:   row.GatewayStatus,
	}
}

// SeedCanonical siembra los pagos y reembolsos base en PostgreSQL si está habilitado.
func (r *Repository) SeedCanonical(ctx context.Context, now time.Time) error {
	if !r.Enabled() {
		return nil
	}
	for idx, payment := range domain.CanonicalPayments() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertPayment(ctx, payment, ts); err != nil {
			return err
		}
	}
	for idx, refund := range domain.CanonicalRefunds() {
		ts := now.Add(time.Duration(idx) * time.Second)
		if err := r.UpsertRefund(ctx, refund, ts); err != nil {
			return err
		}
	}
	return nil
}
