package domain_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

func TestCalculateQuoteTotals_Computes30PercentDepositAnd70PercentBalance(t *testing.T) {
	t.Parallel()

	lines := []domain.QuoteLine{
		{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 100, UnitPriceRD: 4},
		{ArticleCode: "RF-005", Name: "Arco orgánico rosa", Quantity: 1, UnitPriceRD: 95},
	}
	total, deposit, balance, err := domain.CalculateQuoteTotals(lines, 5, 0)
	if err != nil {
		t.Fatalf("no esperaba error: %v", err)
	}
	if total != 500 || deposit != 150 || balance != 350 {
		t.Fatalf("totales incorrectos: total=%d deposit=%d balance=%d", total, deposit, balance)
	}
}

func TestBuildRosaProposal_RespectsGuestCountAndCalculatesTotals(t *testing.T) {
	t.Parallel()

	proposal := domain.BuildRosaProposal("Boda", 120, 4500)
	if proposal.Guests != 120 {
		t.Fatalf("esperaba 120 invitados, obtuvo %d", proposal.Guests)
	}
	if len(proposal.SuggestedLines) < 4 {
		t.Fatalf("esperaba al menos 4 líneas sugeridas, obtuvo %d", len(proposal.SuggestedLines))
	}
	if proposal.EstimatedTotalRD <= 0 || proposal.DepositRequiredRD <= 0 {
		t.Fatalf("esperaba totales positivos en propuesta Rosa IA: %+v", proposal)
	}
}
