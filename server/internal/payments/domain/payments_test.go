package domain_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
)

func TestCanonicalFinanceSummary_MatchesDesignMetrics(t *testing.T) {
	t.Parallel()

	summary := domain.CanonicalFinanceSummary()
	if summary.OctRevenueRD != 12450 {
		t.Fatalf("unexpected OctRevenueRD: %d", summary.OctRevenueRD)
	}
	if summary.AccountsReceivable != 7724 {
		t.Fatalf("unexpected AccountsReceivable: %d", summary.AccountsReceivable)
	}
	if summary.ActiveEventsCount != 4 {
		t.Fatalf("unexpected ActiveEventsCount: %d", summary.ActiveEventsCount)
	}
	if summary.NewQuoteRequests != 2 {
		t.Fatalf("unexpected NewQuoteRequests: %d", summary.NewQuoteRequests)
	}
}

func TestValidateMethod_RejectsUnknownMethod(t *testing.T) {
	t.Parallel()

	if err := domain.ValidateMethod(domain.MethodTransfer); err != nil {
		t.Fatalf("unexpected error on transfer method: %v", err)
	}
	if err := domain.ValidateMethod("cripto"); err == nil {
		t.Fatal("expected error on unsupported payment method")
	}
}

func TestNormalizePaymentMethod_HandlesBanksAndCaseInsensitiveInputs(t *testing.T) {
	t.Parallel()

	cases := []struct {
		rawMethod  string
		explicitBk string
		wantMethod domain.PaymentMethod
		wantBank   string
	}{
		{"Banco Popular", "", domain.MethodTransfer, "Banco Popular"},
		{"Transferencia Banreservas", "", domain.MethodTransfer, "Banreservas"},
		{"BHD León", "", domain.MethodTransfer, "BHD León"},
		{"TRANSFERENCIA", "Asociación Cibao", domain.MethodTransfer, "Asociación Cibao"},
		{"Tarjeta", "", domain.MethodCard, ""},
		{"EFECTIVO", "", domain.MethodCash, ""},
	}

	for _, tc := range cases {
		method, bank, err := domain.NormalizePaymentMethod(tc.rawMethod, tc.explicitBk)
		if err != nil {
			t.Fatalf("NormalizePaymentMethod(%q): unexpected error %v", tc.rawMethod, err)
		}
		if method != tc.wantMethod {
			t.Fatalf("NormalizePaymentMethod(%q): got method %q, want %q", tc.rawMethod, method, tc.wantMethod)
		}
		if bank != tc.wantBank {
			t.Fatalf("NormalizePaymentMethod(%q): got bank %q, want %q", tc.rawMethod, bank, tc.wantBank)
		}
	}

	if _, _, err := domain.NormalizePaymentMethod("bitcoin", ""); err == nil {
		t.Fatal("expected error for unsupported method")
	}
}
