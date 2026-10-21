package domain_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
)

func TestCalculateCancellationRefund_RespectsThreeTierPolicy(t *testing.T) {
	t.Parallel()

	moreThanSeven := domain.CalculateCancellationRefund(1350, 10)
	if moreThanSeven.RefundPercent != 90 || moreThanSeven.RefundRD != 1215 || moreThanSeven.RetainedRD != 135 {
		t.Fatalf("esperaba 90%% (RD$ 1215), obtuvo %+v", moreThanSeven)
	}

	betweenThreeAndSeven := domain.CalculateCancellationRefund(1350, 5)
	if betweenThreeAndSeven.RefundPercent != 50 || betweenThreeAndSeven.RefundRD != 675 || betweenThreeAndSeven.RetainedRD != 675 {
		t.Fatalf("esperaba 50%% (RD$ 675), obtuvo %+v", betweenThreeAndSeven)
	}

	lessThanThree := domain.CalculateCancellationRefund(1350, 2)
	if lessThanThree.RefundPercent != 0 || lessThanThree.RefundRD != 0 || lessThanThree.RetainedRD != 1350 {
		t.Fatalf("esperaba 0%% (RD$ 0), obtuvo %+v", lessThanThree)
	}
}

func TestEvaluateDateChange_FirstChangeFreeAndSubsequentCharged(t *testing.T) {
	t.Parallel()

	evt := domain.CanonicalEvents()[0]
	evt.DaysUntilEvent = 5
	evt.DateChangesUsed = 0

	first, err := domain.EvaluateDateChange(evt)
	if err != nil {
		t.Fatalf("no esperaba error en primer cambio: %v", err)
	}
	if !first.IsFree || first.FeeRD != 0 {
		t.Fatalf("esperaba primer cambio gratis, obtuvo %+v", first)
	}

	evt.DateChangesUsed = 1
	second, err := domain.EvaluateDateChange(evt)
	if err != nil {
		t.Fatalf("no esperaba error en segundo cambio: %v", err)
	}
	if second.IsFree || second.FeeRD <= 0 {
		t.Fatalf("esperaba recargo en segundo cambio, obtuvo %+v", second)
	}
}

func TestCanonicalEvents_MatchSampleData(t *testing.T) {
	t.Parallel()

	events := domain.CanonicalEvents()
	if len(events) < 5 {
		t.Fatalf("esperaba al menos 5 eventos canónicos, obtuvo %d", len(events))
	}

	boda := events[0]
	if boda.Title != "Boda Real Rosa & Dorado" || boda.TotalRD != 4500 || boda.PaidDepositRD != 1350 || boda.BalanceRD() != 3150 {
		t.Fatalf("datos incoherentes en Boda Real: %+v (balance %d)", boda, boda.BalanceRD())
	}

	quince := events[1]
	if quince.Title != "Quinceañera Valentina Crystal" || quince.TotalRD != 6200 || quince.PaidDepositRD != 1860 || quince.BalanceRD() != 4340 {
		t.Fatalf("datos incoherentes en Quinceañera Valentina: %+v (balance %d)", quince, quince.BalanceRD())
	}
}
