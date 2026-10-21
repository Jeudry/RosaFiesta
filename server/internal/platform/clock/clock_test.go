package clock_test

import (
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
)

func TestFixedClockAdvance(t *testing.T) {
	base := time.Date(2026, time.October, 20, 12, 0, 0, 0, time.UTC)
	fixed := &clock.Fixed{At: base}
	if !fixed.Now().Equal(base) {
		t.Fatalf("expected %v, got %v", base, fixed.Now())
	}
	fixed.Advance(2 * time.Hour)
	want := base.Add(2 * time.Hour)
	if !fixed.Now().Equal(want) {
		t.Fatalf("expected %v, got %v", want, fixed.Now())
	}
}
