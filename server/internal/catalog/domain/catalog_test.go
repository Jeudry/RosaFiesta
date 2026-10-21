package domain_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

func TestCanonicalDailyPricesMatchDesignSpec(t *testing.T) {
	wantPrices := map[string]int{
		"silla-tiffany": 4,
		"mesa-plegable": 20,
		"mesa-redonda":  25,
		"centro-floral": 35,
		"arco-rosa":     95,
		"mesa-helados":  150,
	}
	articles := domain.CanonicalArticles()
	if len(articles) != len(wantPrices) {
		t.Fatalf("expected %d canonical articles, got %d", len(wantPrices), len(articles))
	}
	for _, item := range articles {
		if wantPrices[item.ID] != item.DailyPriceDop {
			t.Fatalf("article %s expected daily price RD$ %d, got %d", item.ID, wantPrices[item.ID], item.DailyPriceDop)
		}
	}
}
