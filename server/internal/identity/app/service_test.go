package app_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"github.com/google/uuid"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
)

type stubChecker struct{ active int }

func (s stubChecker) ActiveEventsForClient(_ context.Context, _ string) int { return s.active }

func TestServiceLoginSwitchViewAndDeleteAccount(t *testing.T) {
	clk := &clock.Fixed{At: time.Date(2026, time.October, 20, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewService(clk, stubChecker{active: 2})

	maria, token, err := svc.Login(context.Background(), "maria@rosafiesta.do", "secret123")
	if err != nil || token == "" || maria.Role != domain.RoleAdmin {
		t.Fatalf("unexpected login result: %+v, token=%q, err=%v", maria, token, err)
	}
	if _, _, err := svc.Login(context.Background(), "maria@rosafiesta.do", "wrong-password"); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected ErrInvalidCredentials for wrong password, got %v", err)
	}

	switched, err := svc.SwitchView(context.Background(), maria.ID, domain.ViewPersonal)
	if err != nil || switched.ActiveView != domain.ViewPersonal {
		t.Fatalf("admin should switch to personal view (A30): %v", err)
	}

	camilaID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	if _, err := svc.SwitchView(context.Background(), camilaID, domain.ViewAdmin); !errors.Is(err, domain.ErrForbiddenView) {
		t.Fatalf("client must not switch to admin view: %v", err)
	}

	if err := svc.DeleteAccount(context.Background(), camilaID); !errors.Is(err, domain.ErrActiveEventsPreventDeletion) {
		t.Fatalf("expected ErrActiveEventsPreventDeletion (s88), got %v", err)
	}
}
