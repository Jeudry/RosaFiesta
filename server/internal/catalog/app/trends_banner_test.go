package app_test

import (
	"errors"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

func TestTrendsCRUDAndEmptyMode(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()
	svc.ResetEmpty(ctx)

	if len(svc.Trends(ctx, true)) != 0 {
		t.Fatalf("expected 0 hardcoded trends in empty mode")
	}
	activeTrue := true
	created, err := svc.CreateTrend(ctx, app.SaveTrendCommand{
		Title:       "Navidad Corporativa",
		EventsLabel: "32 reservas",
		PhotoKey:    "decoracion",
		PhotoURL:    "/v1/media/med-navidad-1",
		PhotoBgHex:  "#FDE6F2",
		SortOrder:   1,
		Active:      &activeTrue,
	})
	if err != nil {
		t.Fatalf("CreateTrend failed: %v", err)
	}
	if created.ID != "trend-navidad-corporativa" {
		t.Fatalf("unexpected generated trend id: %s", created.ID)
	}
	if _, dupErr := svc.CreateTrend(ctx, app.SaveTrendCommand{Title: "navidad corporativa"}); !errors.Is(dupErr, domain.ErrTrendAlreadyExists) {
		t.Fatalf("expected ErrTrendAlreadyExists on duplicate, got %v", dupErr)
	}
	activeFalse := false
	updated, err := svc.UpdateTrend(ctx, created.ID, app.SaveTrendCommand{
		Title:       "Navidad VIP",
		EventsLabel: "50 eventos",
		PhotoURL:    "/v1/media/med-navidad-2",
		Active:      &activeFalse,
	})
	if err != nil {
		t.Fatalf("UpdateTrend failed: %v", err)
	}
	if updated.Title != "Navidad VIP" {
		t.Fatalf("expected updated title, got %s", updated.Title)
	}
	if len(svc.Trends(ctx, false)) != 0 {
		t.Fatalf("expected inactive trend hidden when includeInactive=false")
	}
	if delErr := svc.DeleteTrend(ctx, created.ID); delErr != nil {
		t.Fatalf("DeleteTrend failed: %v", delErr)
	}
	if len(svc.Trends(ctx, true)) != 0 {
		t.Fatalf("expected 0 trends after deletion")
	}
}

func TestSeasonBannerUpdateAndEmptyMode(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()
	svc.ResetEmpty(ctx)

	if svc.SeasonBanner(ctx).Active {
		t.Fatalf("expected season banner inactive in empty mode")
	}
	banner, err := svc.UpdateSeasonBanner(ctx, app.UpdateSeasonBannerCommand{
		BadgeLabel:       "TEMPORADA NAVIDAD 2026",
		Title:            "Haz brillar tu fiesta de fin de año",
		CtaLabel:         "Ver catálogo",
		TargetScreenCode: "s18",
		Active:           true,
	})
	if err != nil {
		t.Fatalf("UpdateSeasonBanner failed: %v", err)
	}
	if !banner.Active {
		t.Fatalf("expected active season banner")
	}
	if banner.Title != "Haz brillar tu fiesta de fin de año" {
		t.Fatalf("unexpected saved season banner title: %+v", banner)
	}
}
