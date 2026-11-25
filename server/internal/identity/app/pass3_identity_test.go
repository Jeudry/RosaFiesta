package app

import (
	"errors"
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

type stubClock struct {
	now time.Time
}

func (s stubClock) Now() time.Time {
	return s.now
}

func TestEnsureBootstrapAdminIdempotentAndLoginable(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	now := time.Date(2026, time.November, 24, 12, 0, 0, 0, time.UTC)
	svc := NewServiceWithOptions(stubClock{now: now}, nil, Options{
		Env:       config.EnvProduction,
		JWTSecret: "9f8e7d6c5b4a392817161514131211109f8e7d6c5b4a3928",
	})
	svc.ResetEmpty(ctx)

	_, err := svc.EnsureBootstrapAdmin(ctx, "", "", "")
	if !errors.Is(err, ErrBootstrapAdminMissing) {
		t.Fatalf("expected ErrBootstrapAdminMissing when empty, got %v", err)
	}

	_, invalidErr := svc.EnsureBootstrapAdmin(ctx, "invalid-email", "short", "")
	if !errors.Is(invalidErr, ErrBootstrapAdminInvalid) {
		t.Fatalf("expected ErrBootstrapAdminInvalid for invalid email/short password, got %v", invalidErr)
	}

	created, err := svc.EnsureBootstrapAdmin(ctx, "admin@rosafiesta.do", "BootstrapPass2026!", "Admin Inicial")
	if err != nil {
		t.Fatalf("expected bootstrap admin creation to succeed, got %v", err)
	}
	if !created {
		t.Fatal("expected created=true on first bootstrap run")
	}

	createdAgain, errAgain := svc.EnsureBootstrapAdmin(ctx, "other@rosafiesta.do", "OtherPass2026!", "Otro")
	if errAgain != nil {
		t.Fatalf("expected idempotent no-op without error, got %v", errAgain)
	}
	if createdAgain {
		t.Fatal("expected created=false when an admin already exists")
	}

	session, loginErr := svc.LoginWithSession(ctx, "admin@rosafiesta.do", "BootstrapPass2026!")
	if loginErr != nil {
		t.Fatalf("expected bootstrapped admin login to succeed, got %v", loginErr)
	}
	if session.User.Role != domain.RoleAdmin {
		t.Fatalf("expected admin role, got %s", session.User.Role)
	}
}

func TestCleanupEvictsExpiredInMemorySecurityState(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	base := time.Date(2026, time.November, 24, 12, 0, 0, 0, time.UTC)
	svc := NewServiceWithOptions(stubClock{now: base.Add(2 * time.Hour)}, nil, Options{})
	svc.ResetEmpty(ctx)

	svc.mu.Lock()
	svc.revokedHashes["expired-revoked"] = base.Add(time.Minute)
	svc.revokedHashes["active-revoked"] = base.Add(3 * time.Hour)
	svc.sessionHashes["expired-sess"] = sessionEntry{UserID: uuid.New(), ExpiresAt: base.Add(time.Minute)}
	svc.sessionHashes["active-sess"] = sessionEntry{UserID: uuid.New(), ExpiresAt: base.Add(3 * time.Hour)}
	svc.otps["expired@rosafiesta.do"] = domain.OTPRecord{Email: "expired@rosafiesta.do", ExpiresAt: base.Add(time.Minute)}
	svc.otps["active@rosafiesta.do"] = domain.OTPRecord{Email: "active@rosafiesta.do", ExpiresAt: base.Add(3 * time.Hour)}
	svc.oauthStates["expired-state"] = oauthStateEntry{ExpiresAt: base.Add(time.Minute)}
	svc.oauthStates["active-state"] = oauthStateEntry{ExpiresAt: base.Add(3 * time.Hour)}
	svc.mu.Unlock()

	if _, err := svc.CleanupExpiredRefreshTokens(ctx); err != nil {
		t.Fatalf("cleanup failed: %v", err)
	}

	svc.mu.RLock()
	defer svc.mu.RUnlock()
	if _, ok := svc.revokedHashes["expired-revoked"]; ok {
		t.Fatal("expected expired revoked hash evicted")
	}
	if _, ok := svc.revokedHashes["active-revoked"]; !ok {
		t.Fatal("expected active revoked hash retained")
	}
	if _, ok := svc.sessionHashes["expired-sess"]; ok {
		t.Fatal("expected expired session evicted")
	}
	if _, ok := svc.sessionHashes["active-sess"]; !ok {
		t.Fatal("expected active session retained")
	}
	if _, ok := svc.otps["expired@rosafiesta.do"]; ok {
		t.Fatal("expected expired otp evicted")
	}
	if _, ok := svc.otps["active@rosafiesta.do"]; !ok {
		t.Fatal("expected active otp retained")
	}
	if _, ok := svc.oauthStates["expired-state"]; ok {
		t.Fatal("expected expired oauth state evicted")
	}
	if _, ok := svc.oauthStates["active-state"]; !ok {
		t.Fatal("expected active oauth state retained")
	}
}

func TestChangePasswordAndImmediateReloginWithinSameSecondSucceeds(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	base := time.Date(2026, time.November, 25, 16, 9, 26, 0, time.UTC)
	svc := NewServiceWithOptions(stubClock{now: base}, nil, Options{
		Env:       config.EnvProduction,
		JWTSecret: "9f8e7d6c5b4a392817161514131211109f8e7d6c5b4a3928",
	})
	svc.ResetEmpty(ctx)

	registeredUser, initialToken, regErr := svc.Register(ctx, RegisterCommand{
		Username: "same_sec_client",
		FullName: "Same Second Client",
		Email:    "samesec@rosafiesta.do",
		Password: "Password123!",
		Phone:    "809-555-0101",
	})
	if regErr != nil {
		t.Fatalf("Register failed: %v", regErr)
	}

	if err := svc.ChangePassword(ctx, registeredUser.ID, "Password123!", "Password999!"); err != nil {
		t.Fatalf("ChangePassword failed: %v", err)
	}
	if _, authOldErr := svc.Authenticate(ctx, initialToken); authOldErr == nil {
		t.Fatal("expected initial access token to be revoked immediately after password change")
	}

	_, newToken, loginErr := svc.Login(ctx, "samesec@rosafiesta.do", "Password999!")
	if loginErr != nil {
		t.Fatalf("Login after password change failed: %v", loginErr)
	}
	if newToken == initialToken {
		t.Fatal("expected new login within the same Unix second to issue a distinct JWT")
	}

	authenticatedUserID, authNewErr := svc.Authenticate(ctx, newToken)
	if authNewErr != nil {
		t.Fatalf("expected relogin token within same second to authenticate, got %v", authNewErr)
	}
	if authenticatedUserID != registeredUser.ID {
		t.Fatalf("expected user ID %s, got %s", registeredUser.ID, authenticatedUserID)
	}
}
