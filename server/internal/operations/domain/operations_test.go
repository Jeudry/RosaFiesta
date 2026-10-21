package domain_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
)

func TestEventLogistics_CompletionPercent(t *testing.T) {
	t.Parallel()

	logistics := domain.CanonicalLogistics()
	if len(logistics) == 0 {
		t.Fatal("esperaba logística canónica")
	}
	boda := logistics[0]
	if boda.CompletionPercent() != 66 {
		t.Fatalf("esperaba 66%% de avance (4 de 6), obtuvo %d%%", boda.CompletionPercent())
	}
}
