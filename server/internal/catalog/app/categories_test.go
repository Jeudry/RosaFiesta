package app

import (
	"context"
	"errors"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

func TestCategoryCRUDCreateAndUpdate(t *testing.T) {
	ctx := context.Background()
	svc := NewService()
	svc.ResetEmpty(ctx)

	if got := len(svc.Categories(ctx)); got != 0 {
		t.Fatalf("expected 0 categories after ResetEmpty, got %d", got)
	}

	created, err := svc.CreateCategory(ctx, SaveCategoryCommand{
		Name:    "Iluminación LED",
		IlloKey: "iluminacion",
	})
	if err != nil {
		t.Fatalf("unexpected create error: %v", err)
	}
	if created.ID != "iluminacion-led" {
		t.Fatalf("expected slug id 'iluminacion-led', got %q", created.ID)
	}

	_, dupErr := svc.CreateCategory(ctx, SaveCategoryCommand{Name: "iluminación led"})
	if !errors.Is(dupErr, domain.ErrCategoryAlreadyExists) {
		t.Fatalf("expected ErrCategoryAlreadyExists, got %v", dupErr)
	}

	updated, err := svc.UpdateCategory(ctx, created.ID, SaveCategoryCommand{
		Name:    "Pistas y luces",
		IlloKey: "iluminacion",
	})
	if err != nil {
		t.Fatalf("unexpected update error: %v", err)
	}
	if updated.Name != "Pistas y luces" {
		t.Fatalf("expected renamed category 'Pistas y luces', got %q", updated.Name)
	}
}

func TestCategoryDeleteProtectionAndCleanup(t *testing.T) {
	ctx := context.Background()
	svc := NewService()
	svc.ResetEmpty(ctx)

	primary, err := svc.CreateCategory(ctx, SaveCategoryCommand{Name: "Pistas y luces"})
	if err != nil {
		t.Fatalf("unexpected create error: %v", err)
	}
	_, artErr := svc.CreateArticle(ctx, CreateArticleCommand{
		Name:          "Foco LED RGB",
		CategoryID:    "Pistas y luces",
		DailyPriceDop: 45,
		TotalStock:    10,
	})
	if artErr != nil {
		t.Fatalf("unexpected create article error: %v", artErr)
	}

	catsWithArticle := svc.Categories(ctx)
	if len(catsWithArticle) != 1 {
		t.Fatalf("expected 1 category, got %d", len(catsWithArticle))
	}
	if catsWithArticle[0].ItemCount != 1 {
		t.Fatalf("expected itemCount=1, got %d", catsWithArticle[0].ItemCount)
	}

	deleteInUseErr := svc.DeleteCategory(ctx, primary.ID)
	if !errors.Is(deleteInUseErr, domain.ErrCategoryInUse) {
		t.Fatalf("expected ErrCategoryInUse, got %v", deleteInUseErr)
	}

	unused, err := svc.CreateCategory(ctx, SaveCategoryCommand{Name: "Letreros neón"})
	if err != nil {
		t.Fatalf("unexpected second create error: %v", err)
	}
	if err := svc.DeleteCategory(ctx, unused.ID); err != nil {
		t.Fatalf("unexpected delete unused category error: %v", err)
	}
	if got := len(svc.Categories(ctx)); got != 1 {
		t.Fatalf("expected 1 remaining category, got %d", got)
	}
}

func TestArticleDeleteProtectionAndCleanup(t *testing.T) {
	ctx := context.Background()
	svc := NewService()
	svc.ResetEmpty(ctx)

	cat, err := svc.CreateCategory(ctx, SaveCategoryCommand{Name: "Mobiliario"})
	if err != nil {
		t.Fatalf("unexpected category create error: %v", err)
	}
	unused, err := svc.CreateArticle(ctx, CreateArticleCommand{
		ID:            "art-rf-001",
		Name:          "Silla plastica",
		CategoryID:    cat.ID,
		DailyPriceDop: 15,
		TotalStock:    100,
	})
	if err != nil {
		t.Fatalf("unexpected article create error: %v", err)
	}
	reserved, err := svc.CreateArticle(ctx, CreateArticleCommand{
		ID:            "art-rf-002",
		Name:          "Silla Tiffany",
		CategoryID:    cat.ID,
		DailyPriceDop: 25,
		TotalStock:    80,
	})
	if err != nil {
		t.Fatalf("unexpected reserved article create error: %v", err)
	}

	reserveErr := svc.ReserveStockForEvent(
		ctx,
		"evt-100",
		"",
		"2026-12-20",
		domain.ReservationConfirmed,
		[]domain.StockRequirement{{ArticleRef: reserved.ID, Quantity: 10}},
	)
	if reserveErr != nil {
		t.Fatalf("unexpected reserve error: %v", reserveErr)
	}

	deleteInUseErr := svc.DeleteArticle(ctx, reserved.ID)
	if !errors.Is(deleteInUseErr, domain.ErrArticleInUse) {
		t.Fatalf("expected ErrArticleInUse, got %v", deleteInUseErr)
	}

	if err := svc.DeleteArticle(ctx, unused.ID); err != nil {
		t.Fatalf("unexpected delete unused article error: %v", err)
	}
	remaining := svc.ArticlesFiltered(ctx, ArticleFilter{Kind: "all", IncludeHidden: true})
	if len(remaining) != 1 {
		t.Fatalf("expected 1 remaining article, got %+v", remaining)
	}
	if remaining[0].ID != reserved.ID {
		t.Fatalf("expected remaining article %q, got %+v", reserved.ID, remaining[0])
	}
}
