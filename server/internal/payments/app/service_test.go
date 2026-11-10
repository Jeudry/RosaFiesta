package app_test

import (
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type fixedClock struct {
	now time.Time
}

func (f fixedClock) Now() time.Time {
	return f.now
}

func TestService_DynamicSummaryClockAndBankNormalization(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 10, 14, 30, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	initial := svc.GetSummary(ctx)
	if initial.OctRevenueRD != 12450 {
		t.Fatalf("expected canonical OctRevenueRD=12450, got %d", initial.OctRevenueRD)
	}
	if initial.AccountsReceivable != 7724 {
		t.Fatalf("expected canonical AccountsReceivable=7724, got %d", initial.AccountsReceivable)
	}

	admin := platformhttp.Principal{
		ID:       uuid.MustParse("22222222-2222-2222-2222-222222222222"),
		Email:    "maria@rosafiesta.do",
		FullName: "María Rosario",
		Role:     "admin",
	}
	payment, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:       "evt-boda-real",
		EventTitle:    "Boda Real Rosa & Dorado",
		ClientName:    "Camila Rodríguez",
		AmountRD:      1500,
		Method:        "Banco Popular",
		Kind:          "saldo",
		ReferenceCode: "POP-TEST-001",
	}, admin)
	if err != nil {
		t.Fatalf("RegisterPaymentForCaller failed: %v", err)
	}
	if payment.Method != domain.MethodTransfer {
		t.Fatalf("expected normalized method %q, got %q", domain.MethodTransfer, payment.Method)
	}
	if payment.BankName != "Banco Popular" {
		t.Fatalf("expected bank name 'Banco Popular', got %q", payment.BankName)
	}
	if payment.PaidDateLabel != "10 nov" {
		t.Fatalf("expected PaidDateLabel '10 nov' from injected clock, got %q", payment.PaidDateLabel)
	}

	afterPayment := svc.GetSummary(ctx)
	if afterPayment.OctRevenueRD != 13950 {
		t.Fatalf("expected OctRevenueRD=13950 after RD$1500 payment, got %d", afterPayment.OctRevenueRD)
	}
	if afterPayment.AccountsReceivable != 6224 {
		t.Fatalf("expected AccountsReceivable=6224 after RD$1500 payment, got %d", afterPayment.AccountsReceivable)
	}
}

func TestService_RefundApprovalUpdatesSummary(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 10, 14, 30, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	approved, err := svc.ApproveRefund(ctx, "ref-baby-lucia", "María Rosario")
	if err != nil {
		t.Fatalf("ApproveRefund failed: %v", err)
	}
	if approved.Status != domain.RefundStatusApproved {
		t.Fatalf("expected refund status approved, got %q", approved.Status)
	}
	if approved.ApprovedAtLabel != "10 nov" {
		t.Fatalf("expected ApprovedAtLabel '10 nov', got %q", approved.ApprovedAtLabel)
	}

	afterRefund := svc.GetSummary(ctx)
	if afterRefund.OctRevenueRD != 11640 {
		t.Fatalf("expected OctRevenueRD=11640 after RD$810 refund approval, got %d", afterRefund.OctRevenueRD)
	}
}

func TestService_ClientScopedPaymentIsolation(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()

	camila := platformhttp.Principal{
		ID:       uuid.MustParse("11111111-1111-1111-1111-111111111111"),
		Email:    "camila@rosafiesta.do",
		FullName: "Camila Rodríguez",
		Role:     "client",
	}
	camilaPayments := svc.ListPaymentsForCaller(ctx, "", camila, true)
	if len(camilaPayments) != 2 {
		t.Fatalf("expected Camila to see only her 2 payments, got %d", len(camilaPayments))
	}

	admin := platformhttp.Principal{Role: "admin"}
	allPayments := svc.ListPaymentsForCaller(ctx, "", admin, true)
	if len(allPayments) != 4 {
		t.Fatalf("expected admin to see all 4 canonical payments, got %d", len(allPayments))
	}
}
