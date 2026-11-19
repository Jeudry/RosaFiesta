package domain_test

import (
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

func TestParseEventDateRangeAppliesOneDayTurnaround(t *testing.T) {
	eventISO, pickupISO, returnISO := domain.ParseEventDateRange("24 Oct 2026", time.Date(2026, 1, 1, 0, 0, 0, 0, time.UTC))
	if eventISO != "2026-10-24" {
		t.Fatalf("expected 2026-10-24, got %s", eventISO)
	}
	if pickupISO != "2026-10-23" || returnISO != "2026-10-25" {
		t.Fatalf("unexpected turnaround range: %s..%s", pickupISO, returnISO)
	}
}

func TestEffectiveAvailableForDateSubictsDamagedAndOverlappingStock(t *testing.T) {
	article := domain.Article{
		ID:           "silla-tiffany",
		Name:         "Silla Tiffany Dorada",
		TotalStock:   100,
		DamagedStock: 10,
	}
	reservations := []domain.StockReservation{
		{
			ID:            "r1",
			ArticleID:     "silla-tiffany",
			EventID:       "ev-1",
			Quantity:      40,
			PickupDateISO: "2026-11-19",
			ReturnDateISO: "2026-11-21",
			Status:        domain.ReservationHeld,
		},
		{
			ID:            "r2",
			ArticleID:     "silla-tiffany",
			EventID:       "ev-released",
			Quantity:      30,
			PickupDateISO: "2026-11-19",
			ReturnDateISO: "2026-11-21",
			Status:        domain.ReservationReleased,
		},
	}

	freeSameWindow := domain.EffectiveAvailableForDate(article, reservations, "2026-11-20", "2026-11-22", "")
	if freeSameWindow != 50 {
		t.Fatalf("expected 50 available (100 - 10 damaged - 40 overlapping held), got %d", freeSameWindow)
	}

	freeExcluded := domain.EffectiveAvailableForDate(article, reservations, "2026-11-20", "2026-11-22", "ev-1")
	if freeExcluded != 90 {
		t.Fatalf("expected 90 available when ev-1 excluded, got %d", freeExcluded)
	}

	freeNextWeek := domain.EffectiveAvailableForDate(article, reservations, "2026-11-27", "2026-11-29", "")
	if freeNextWeek != 90 {
		t.Fatalf("expected 90 available outside turnaround window, got %d", freeNextWeek)
	}
}
