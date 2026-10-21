package domain_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
)

func TestCanonicalFinanceSummary_MatchesDesignMetrics(t *testing.T) {
	t.Parallel()

	summary := domain.CanonicalFinanceSummary()
	if summary.OctRevenueRD != 12450 || summary.AccountsReceivable != 7724 {
		t.Fatalf("métricas financieras no coinciden con Sección 6: %+v", summary)
	}
	if summary.ActiveEventsCount != 4 || summary.NewQuoteRequests != 2 {
		t.Fatalf("conteos de negocio incorrectos: %+v", summary)
	}
}

func TestValidateMethod_RejectsUnknownMethod(t *testing.T) {
	t.Parallel()

	if err := domain.ValidateMethod(domain.MethodTransfer); err != nil {
		t.Fatalf("no esperaba error en transferencia: %v", err)
	}
	if err := domain.ValidateMethod("cripto"); err == nil {
		t.Fatal("esperaba error en método desconocido")
	}
}
