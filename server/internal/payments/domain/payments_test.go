package domain_test

import (
	"errors"
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

func TestValidateRNCForNCF_ValidatesB01AndB02(t *testing.T) {
	t.Parallel()

	ncf, rnc, err := domain.ValidateRNCForNCF("b01", "1-31-45678-9")
	if err != nil {
		t.Fatalf("expected valid 9-digit RNC, got %v", err)
	}
	if ncf != "B01" {
		t.Fatalf("expected normalized NCF B01, got %q", ncf)
	}
	if rnc != "131456789" {
		t.Fatalf("expected cleaned RNC 131456789, got %q", rnc)
	}

	_, cedula, cedErr := domain.ValidateRNCForNCF("B01", "001-1234567-8")
	if cedErr != nil {
		t.Fatalf("expected valid 11-digit cedula, got %v", cedErr)
	}
	if cedula != "00112345678" {
		t.Fatalf("expected cleaned cedula 00112345678, got %q", cedula)
	}

	if _, _, badErr := domain.ValidateRNCForNCF("B01", "12345"); !errors.Is(badErr, domain.ErrInvalidFiscalRNC) {
		t.Fatalf("expected ErrInvalidFiscalRNC for short B01 RNC, got %v", badErr)
	}
	if _, _, typeErr := domain.ValidateRNCForNCF("B99", ""); !errors.Is(typeErr, domain.ErrInvalidFiscalRNC) {
		t.Fatalf("expected ErrInvalidFiscalRNC for unsupported NCF type, got %v", typeErr)
	}
	defaultNCF, _, defErr := domain.ValidateRNCForNCF("", "")
	if defErr != nil {
		t.Fatalf("expected default B02 to pass, got %v", defErr)
	}
	if defaultNCF != "B02" {
		t.Fatalf("expected default NCF B02, got %q", defaultNCF)
	}
}

func TestComputeDominicanTaxBreakdownAndFormatNCF(t *testing.T) {
	t.Parallel()

	subtotal, itbis := domain.ComputeDominicanTaxBreakdown(2180, 1000)
	if subtotal != 1000 {
		t.Fatalf("expected subtotal 1000 excluding 1000 deposit, got %d", subtotal)
	}
	if itbis != 180 {
		t.Fatalf("expected 18%% ITBIS 180, got %d", itbis)
	}
	if formatted := domain.FormatNCFNumber("B01", 42); formatted != "B0100000042" {
		t.Fatalf("expected B0100000042, got %q", formatted)
	}
}
