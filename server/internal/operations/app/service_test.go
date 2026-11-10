package app_test

import (
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
)

type fixedClock struct {
	now time.Time
}

func (f fixedClock) Now() time.Time {
	return f.now
}

func TestService_ChecklistTimestampAndDamageSeparation(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.October, 23, 14, 45, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	reviewsBefore := len(svc.ListReviews(ctx, ""))
	logi, err := svc.ToggleChecklistItem(ctx, "evt-boda-real", "chk-5", true, "Pedro Santos")
	if err != nil {
		t.Fatalf("ToggleChecklistItem failed: %v", err)
	}
	foundChk5 := false
	for _, item := range logi.Checklist {
		if item.ID == "chk-5" {
			foundChk5 = true
			if item.CompletedAt != "02:45 PM" {
				t.Fatalf("expected CompletedAt '02:45 PM' from injected clock, got %q", item.CompletedAt)
			}
		}
	}
	if !foundChk5 {
		t.Fatal("expected chk-5 in checklist")
	}

	dmg, err := svc.ReportDamage(ctx, "evt-boda-real", "RF-001", "Silla Tiffany dorada", 2, 0, 80, "Respaldo rayado")
	if err != nil {
		t.Fatalf("ReportDamage failed: %v", err)
	}
	if !strings.HasPrefix(dmg.ID, "dmg-") {
		t.Fatalf("unexpected damage id: %s", dmg.ID)
	}
	if len(svc.ListReviews(ctx, "")) != reviewsBefore {
		t.Fatalf("expected reviews count unchanged (%d), got %d", reviewsBefore, len(svc.ListReviews(ctx, "")))
	}
	if len(svc.ListDamages(ctx, "evt-boda-real")) != 2 {
		t.Fatalf("expected 2 damage incidents for evt-boda-real, got %d", len(svc.ListDamages(ctx, "evt-boda-real")))
	}
}

func TestService_PublicSettingsAndReportExports(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()

	pub := svc.GetPublicSettings(ctx)
	if pub.Address != "Av. Winston Churchill 1099, Piantini, Santo Domingo" {
		t.Fatalf("unexpected public address: %q", pub.Address)
	}
	if pub.RNC != "1-31-84920-2" {
		t.Fatalf("unexpected RNC: %q", pub.RNC)
	}

	report6m, err := svc.ExportReport(ctx, "income", "6m")
	if err != nil {
		t.Fatalf("ExportReport 6m failed: %v", err)
	}
	if len(report6m.Series) != 6 {
		t.Fatalf("expected 6 points for 6m, got %d", len(report6m.Series))
	}

	report12m, err := svc.ExportReport(ctx, "dgii", "12m")
	if err != nil {
		t.Fatalf("ExportReport dgii 12m failed: %v", err)
	}
	if len(report12m.Series) != 12 {
		t.Fatalf("expected 12 points for 12m, got %d", len(report12m.Series))
	}
	if !strings.Contains(report12m.CSV, "ITBIS_18_DOP") {
		t.Fatalf("expected CSV header with ITBIS_18_DOP, got %q", report12m.CSV)
	}
}
