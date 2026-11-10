package domain_test

import (
	"errors"
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
)

func TestRoleAccessAndPermissions(t *testing.T) {
	maria := domain.User{ID: uuid.New(), FullName: "María", Role: domain.RoleAdmin}
	if !maria.CanAccessView(domain.ViewAdmin) {
		t.Fatal("admin should access admin view")
	}
	if !maria.CanAccessView(domain.ViewPersonal) {
		t.Fatal("admin should access personal view")
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
	if !yuli.CanAccessView(domain.ViewAdmin) {
		t.Fatal("staff should access admin view")
	}
	if yuli.CanAccessView(domain.ViewPersonal) {
		t.Fatal("staff should not access personal view")
	}
	if !yuli.HasPermission("decoracion") {
		t.Fatal("Yuli must have decoracion permission")
	}
	if !yuli.HasPermission("tareas") {
		t.Fatal("Yuli must have tareas permission")
	}
	if yuli.HasPermission("pagos") {
		t.Fatal("Yuli must NOT have pagos permission")
	}

	camila := domain.User{ID: uuid.New(), FullName: "Camila Rodríguez", Role: domain.RoleClient}
	if !camila.CanAccessView(domain.ViewPersonal) {
		t.Fatal("client should access personal view")
	}
	if camila.CanAccessView(domain.ViewAdmin) {
		t.Fatal("client should not access admin view")
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
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if got != "camila@rosafiesta.do" {
		t.Fatalf("unexpected email normalization: %q", got)
	}
	if _, err := domain.NormalizeEmail("invalid"); !errors.Is(err, domain.ErrInvalidInput) {
		t.Fatalf("expected ErrInvalidInput, got %v", err)
	}
}

func TestHashAndVerifyPassword(t *testing.T) {
	hash, err := domain.HashPassword("secret123")
	if err != nil {
		t.Fatalf("hash password: %v", err)
	}
	if !domain.VerifyPassword(hash, "secret123") {
		t.Fatal("expected VerifyPassword to accept matching password")
	}
	if domain.VerifyPassword(hash, "wrong-password") {
		t.Fatal("expected VerifyPassword to reject wrong password")
	}
	if _, err := domain.HashPassword("short"); !errors.Is(err, domain.ErrInvalidInput) {
		t.Fatalf("expected ErrInvalidInput for short password, got %v", err)
	}
}

func TestTokenManagerValidatesSignatureAndExpiration(t *testing.T) {
	now := time.Date(2026, time.October, 20, 12, 0, 0, 0, time.UTC)
	tm := domain.NewTokenManager("test-signing-key-with-at-least-32-bytes!!")
	user := domain.User{
		ID:       uuid.New(),
		Username: "camila",
		FullName: "Camila Rodríguez",
		Email:    "camila@rosafiesta.do",
		Role:     domain.RoleClient,
	}
	token := tm.IssueWithTTL(user, now, time.Hour)
	subject, err := tm.Verify(token, now.Add(30*time.Minute))
	if err != nil {
		t.Fatalf("expected valid token before exp, got %v", err)
	}
	if subject != user.ID.String() {
		t.Fatalf("expected subject %s, got %s", user.ID, subject)
	}

	_, err = tm.Verify(token, now.Add(time.Hour))
	if !errors.Is(err, domain.ErrTokenExpired) {
		t.Fatalf("expected ErrTokenExpired at exact exp boundary, got %v", err)
	}

	otherTM := domain.NewTokenManager("different-key-with-at-least-32-bytes-here")
	if _, err := otherTM.Verify(token, now); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected ErrInvalidCredentials with wrong signing key, got %v", err)
	}
}
