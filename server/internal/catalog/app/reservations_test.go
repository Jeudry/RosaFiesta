package app_test

import (
	"context"
	"errors"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

func TestServiceBlocksOverbookingAndTracksDamageQuarantine(t *testing.T) {
	ctx := context.Background()
	svc := app.NewService()

	err := svc.ReserveStockForEvent(
		ctx,
		"ev-alpha",
		"q-alpha",
		"2026-12-10",
		domain.ReservationHeld,
		[]domain.StockRequirement{{ArticleRef: "arco-rosa", Quantity: 8}},
	)
	if err != nil {
		t.Fatalf("expected initial reservation to fit total stock, got %v", err)
	}

	err = svc.ReserveStockForEvent(
		ctx,
		"ev-beta",
		"q-beta",
		"2026-12-11",
		domain.ReservationHeld,
		[]domain.StockRequirement{{ArticleRef: "arco-rosa", Quantity: 4}},
	)
	if !errors.Is(err, domain.ErrInsufficientStockForDate) {
		t.Fatalf("expected ErrInsufficientStockForDate during overlapping turnaround window, got %v", err)
	}

	if err := svc.TransitionEventReservations(ctx, "ev-alpha", domain.ReservationReleased); err != nil {
		t.Fatalf("release error: %v", err)
	}

	err = svc.ReserveStockForEvent(
		ctx,
		"ev-beta",
		"q-beta",
		"2026-12-11",
		domain.ReservationHeld,
		[]domain.StockRequirement{{ArticleRef: "arco-rosa", Quantity: 3}},
	)
	if err != nil {
		t.Fatalf("expected reservation to succeed once ev-alpha released, got %v", err)
	}

	updated, err := svc.AdjustDamagedStock(ctx, "arco-rosa", 1)
	if err != nil {
		t.Fatalf("damage adjust error: %v", err)
	}
	if updated.DamagedStock != 1 {
		t.Fatalf("expected DamagedStock=1, got %d", updated.DamagedStock)
	}
}
