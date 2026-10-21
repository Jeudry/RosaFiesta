package domain_test

import (
	"errors"
	"testing"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
)

func TestRoleAccessAndPermissions(t *testing.T) {
	maria := domain.User{ID: uuid.New(), FullName: "María", Role: domain.RoleAdmin}
	if !maria.CanAccessView(domain.ViewAdmin) || !maria.CanAccessView(domain.ViewPersonal) {
		t.Fatal("admin María should access both Admin and Personal views (A30)")
	}
	if !maria.HasPermission("payments") {
		t.Fatal("admin should have all permissions")
	}

	yuli := domain.User{
		ID:          uuid.New(),
		FullName:    "Yuli",
		Role:        domain.RoleStaff,
		Permissions: []string{"decoracion", "tareas"},
	}
	if !yuli.CanAccessView(domain.ViewAdmin) || yuli.CanAccessView(domain.ViewPersonal) {
		t.Fatal("staff Yuli should only access Admin view")
	}
	if !yuli.HasPermission("decoracion") || !yuli.HasPermission("tareas") {
		t.Fatal("Yuli must have decoracion and tareas permissions (A28)")
	}
	if yuli.HasPermission("pagos") {
		t.Fatal("Yuli must NOT have pagos permission (A28)")
	}

	camila := domain.User{ID: uuid.New(), FullName: "Camila Rodríguez", Role: domain.RoleClient}
	if !camila.CanAccessView(domain.ViewPersonal) || camila.CanAccessView(domain.ViewAdmin) {
		t.Fatal("client Camila should only access Personal view")
	}
}

func TestValidateAccountDeletion(t *testing.T) {
	if err := domain.ValidateAccountDeletion(2); !errors.Is(err, domain.ErrActiveEventsPreventDeletion) {
		t.Fatalf("expected ErrActiveEventsPreventDeletion, got %v", err)
	}
	if err := domain.ValidateAccountDeletion(0); err != nil {
		t.Fatalf("expected nil when no active events, got %v", err)
	}
}

func TestNormalizeEmail(t *testing.T) {
	got, err := domain.NormalizeEmail("  Camila@RosaFiesta.do ")
	if err != nil || got != "camila@rosafiesta.do" {
		t.Fatalf("unexpected email normalization: %q, %v", got, err)
	}
	if _, err := domain.NormalizeEmail("invalid"); !errors.Is(err, domain.ErrInvalidInput) {
		t.Fatalf("expected ErrInvalidInput, got %v", err)
	}
}
