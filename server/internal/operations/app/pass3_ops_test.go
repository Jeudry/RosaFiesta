package app_test

import (
	"context"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type spyOpsStore struct {
	app.Store
	zones        []domain.DeliveryZone
	replaceCalls int
}

func (s *spyOpsStore) Enabled() bool { return true }

func (s *spyOpsStore) ListReviews(_ context.Context) ([]domain.Review, error) {
	return domain.CanonicalReviews(), nil
}

func (s *spyOpsStore) UpsertReview(_ context.Context, _ domain.Review, _ time.Time) error {
	return nil
}

func (s *spyOpsStore) UpsertBusinessSettings(_ context.Context, _ domain.BusinessSettings, _ time.Time) error {
	return nil
}

func (s *spyOpsStore) GetBusinessSettings(_ context.Context) (domain.BusinessSettings, error) {
	return domain.CanonicalBusinessSettings(), nil
}

func (s *spyOpsStore) ListDeliveryZones(_ context.Context) ([]domain.DeliveryZone, error) {
	return s.zones, nil
}

func (s *spyOpsStore) UpsertDeliveryZone(_ context.Context, zone domain.DeliveryZone) error {
	s.zones = append(s.zones, zone)
	return nil
}

func (s *spyOpsStore) ReplaceDeliveryZones(_ context.Context, zones []domain.DeliveryZone) error {
	s.replaceCalls++
	s.zones = make([]domain.DeliveryZone, len(zones))
	copy(s.zones, zones)
	return nil
}

func (s *spyOpsStore) UpsertAuditLog(_ context.Context, _ domain.AuditLog, _ time.Time) error {
	return nil
}

func TestOperationsZeroITBISWhatsAppToggleReviewVerifiedAndZoneDeletion(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	store := &spyOpsStore{}
	svc := app.NewService().WithStore(store)
	admin := platformhttp.Principal{FullName: "María", Role: "admin"}

	zero := 0
	autoWA := true
	remainingZones := []domain.DeliveryZone{{ID: "zone-piantini", Name: "Piantini", FeeDop: 350, EtaMinutes: 30, Active: true}}
	updatedSettings, zones, err := svc.UpdateSettings(ctx, app.UpdateSettingsCommand{
		ItbisPercent:              &zero,
		WhatsAppAutoAlertsEnabled: &autoWA,
		DeliveryZones:             remainingZones,
		HasDeliveryZones:          true,
	}, admin)
	if err != nil {
		t.Fatalf("UpdateSettings failed: %v", err)
	}
	if updatedSettings.ItbisPercent != 0 {
		t.Fatalf("expected 0%% ITBIS persisted, got %d", updatedSettings.ItbisPercent)
	}
	if !updatedSettings.WhatsAppAutoAlertsEnabled {
		t.Fatal("expected WhatsAppAutoAlertsEnabled=true persisted")
	}
	if store.replaceCalls != 1 {
		t.Fatalf("expected ReplaceDeliveryZones called once, got %d", store.replaceCalls)
	}
	if len(zones) != 1 {
		t.Fatalf("expected 1 delivery zone after replacement, got %d", len(zones))
	}

	// Empty zone slice deletes all zones cleanly.
	_, clearedZones, clearErr := svc.UpdateSettings(ctx, app.UpdateSettingsCommand{
		DeliveryZones:    []domain.DeliveryZone{},
		HasDeliveryZones: true,
	}, admin)
	if clearErr != nil {
		t.Fatalf("clearing delivery zones failed: %v", clearErr)
	}
	if len(clearedZones) != 0 {
		t.Fatalf("expected 0 zones after clearing, got %d", len(clearedZones))
	}

	// Verify review reply toggle sets verified=true and verified=false.
	reviews := svc.ListReviews(ctx, "")
	if len(reviews) == 0 {
		t.Fatal("expected canonical reviews present")
	}
	verifyFalse := false
	replied, repErr := svc.ReplyReviewWithVerification(ctx, reviews[0].ID, "Gracias por preferirnos", &verifyFalse, admin)
	if repErr != nil {
		t.Fatalf("ReplyReviewWithVerification failed: %v", repErr)
	}
	if replied.Verified {
		t.Fatal("expected review verified=false when toggled false")
	}
	verifyTrue := true
	repliedTrue, _ := svc.ReplyReviewWithVerification(ctx, reviews[0].ID, "Verificada ahora", &verifyTrue, admin)
	if !repliedTrue.Verified {
		t.Fatal("expected review verified=true when toggled true")
	}
}

func TestBuildReportExportUsesTaxInclusiveITBISExtraction(t *testing.T) {
	t.Parallel()
	now := time.Date(2026, time.November, 24, 12, 0, 0, 0, time.UTC)
	snap := domain.ReportSnapshot{
		ItbisPercent: 18,
		Months: []domain.ReportMonthMetric{
			{Year: 2026, Month: time.November, IncomeRD: 11800, EventsCount: 2},
		},
	}
	rep, err := domain.BuildReportExportFromSnapshot("income", "6m", snap, now)
	if err != nil {
		t.Fatalf("BuildReportExportFromSnapshot failed: %v", err)
	}
	if rep.ItbisRD != 1800 {
		t.Fatalf("expected tax-inclusive ITBIS=1800 from 11800 DOP gross at 18%%, got %d", rep.ItbisRD)
	}

	snap.ItbisPercent = 0
	repZero, errZero := domain.BuildReportExportFromSnapshot("income", "6m", snap, now)
	if errZero != nil {
		t.Fatalf("BuildReportExportFromSnapshot 0%% failed: %v", errZero)
	}
	if repZero.ItbisRD != 0 {
		t.Fatalf("expected 0 ITBIS when ItbisPercent=0, got %d", repZero.ItbisRD)
	}
}
