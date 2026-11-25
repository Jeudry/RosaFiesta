package domain_test

import (
	"fmt"
	"strings"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

func TestBuildQuoteContractPDF_WinAnsiAndMultipagePagination(t *testing.T) {
	t.Parallel()

	lines := make([]domain.QuoteLine, 0, 55)
	for idx := 1; idx <= 55; idx++ {
		lines = append(lines, domain.QuoteLine{
			ArticleCode: fmt.Sprintf("RF-%03d", idx),
			Name:        fmt.Sprintf("Decoración española ñandú #%d", idx),
			Quantity:    2,
			UnitPriceRD: 100,
		})
	}
	quote := domain.Quote{
		Code:           "COT-2026-99",
		Status:         domain.QuoteStatusSigned,
		EventTitle:     "Celebración Ágape Peña",
		ClientName:     "María José Núñez",
		ClientEmail:    "maria@rosafiesta.do",
		EventDateLabel: "Sáb 15 dic",
		Venue:          "Salón Piantini",
		Guests:         120,
		Lines:          lines,
		TotalRD:        11000,
		DepositPercent: 30,
		DepositRD:      3300,
		BalanceRD:      7700,
		SignedByName:   "María José Núñez",
	}

	pdfText := string(domain.BuildQuoteContractPDF(quote))
	if !strings.Contains(pdfText, "/Encoding /WinAnsiEncoding") {
		t.Fatal("expected WinAnsiEncoding declared in PDF font object")
	}
	if !strings.Contains(pdfText, "\\361") {
		t.Fatal("expected WinAnsi octal escape \\361 for 'ñ'")
	}
	if !strings.Contains(pdfText, "\\363") {
		t.Fatal("expected WinAnsi octal escape \\363 for 'ó'")
	}
	if !strings.Contains(pdfText, "/Count 2") {
		t.Fatalf("expected 2-page PDF for 55+ lines, got: %s", pdfText[:min(400, len(pdfText))])
	}
}
