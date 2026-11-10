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
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

type stubChecker struct{ active int }

func (s stubChecker) ActiveEventsForClient(_ context.Context, _ string) int { return s.active }

func TestServiceLoginSwitchViewAndDeleteAccount(t *testing.T) {
	clk := &clock.Fixed{At: time.Date(2026, time.October, 20, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewService(clk, stubChecker{active: 2})

	maria, token, err := svc.Login(context.Background(), "maria@rosafiesta.do", "secret123")
	if err != nil {
		t.Fatalf("unexpected login error: %v", err)
	}
	if token == "" {
		t.Fatal("expected non-empty access token")
	}
	if maria.Role != domain.RoleAdmin {
		t.Fatalf("expected admin role, got %s", maria.Role)
	}
	if _, _, err := svc.Login(context.Background(), "maria@rosafiesta.do", "wrong-password"); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected ErrInvalidCredentials for wrong password, got %v", err)
	}
	if _, _, err := svc.Login(context.Background(), "maria@rosafiesta.do", "rosa2026"); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected backdoor password rosa2026 to be rejected, got %v", err)
	}

	switched, err := svc.SwitchView(context.Background(), maria.ID, domain.ViewPersonal)
	if err != nil {
		t.Fatalf("admin should switch to personal view: %v", err)
	}
	if switched.ActiveView != domain.ViewPersonal {
		t.Fatalf("expected personal view, got %s", switched.ActiveView)
	}

	camilaID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	if _, err := svc.SwitchView(context.Background(), camilaID, domain.ViewAdmin); !errors.Is(err, domain.ErrForbiddenView) {
		t.Fatalf("client must not switch to admin view: %v", err)
	}

	if err := svc.DeleteAccount(context.Background(), camilaID); !errors.Is(err, domain.ErrActiveEventsPreventDeletion) {
		t.Fatalf("expected ErrActiveEventsPreventDeletion, got %v", err)
	}
}

func TestServiceOTPPasswordResetLogoutAndOAuth(t *testing.T) {
	ctx := context.Background()
	clk := &clock.Fixed{At: time.Date(2026, time.October, 20, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:       config.EnvDevelopment,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})

	_, _, unauthErr := svc.ResetPassword(ctx, identityapp.ResetPasswordCommand{
		Identifier:  "camila@rosafiesta.do",
		NewPassword: "NewStrongPassword123",
	})
	if !errors.Is(unauthErr, domain.ErrResetTokenInvalid) {
		t.Fatalf("expected unauthenticated reset password to fail with ErrResetTokenInvalid, got %v", unauthErr)
	}

	otpRes, err := svc.RequestOTP(ctx, "camila@rosafiesta.do")
	if err != nil {
		t.Fatalf("RequestOTP failed: %v", err)
	}
	if len(otpRes.DevCode) != 6 {
		t.Fatalf("expected 6-digit devCode in development, got %q", otpRes.DevCode)
	}

	verifyRes, err := svc.VerifyOTP(ctx, "camila@rosafiesta.do", otpRes.DevCode)
	if err != nil {
		t.Fatalf("VerifyOTP failed: %v", err)
	}
	if verifyRes.ResetToken == "" {
		t.Fatal("expected single-use resetToken")
	}

	_, resetJwt, err := svc.ResetPassword(ctx, identityapp.ResetPasswordCommand{
		Email:       "camila@rosafiesta.do",
		ResetToken:  verifyRes.ResetToken,
		NewPassword: "UpdatedPassword2026",
	})
	if err != nil {
		t.Fatalf("ResetPassword with valid resetToken failed: %v", err)
	}

	_, _, reuseErr := svc.ResetPassword(ctx, identityapp.ResetPasswordCommand{
		Email:       "camila@rosafiesta.do",
		ResetToken:  verifyRes.ResetToken,
		NewPassword: "AnotherPassword2026",
	})
	if !errors.Is(reuseErr, domain.ErrResetTokenInvalid) {
		t.Fatalf("expected reused resetToken to fail, got %v", reuseErr)
	}

	if err := svc.Logout(ctx, resetJwt); err != nil {
		t.Fatalf("Logout failed: %v", err)
	}
	if _, authErr := svc.Authenticate(ctx, resetJwt); authErr == nil {
		t.Fatal("expected logged-out JWT to be rejected")
	}

	_, _, oauthErr := svc.OAuthLogin(ctx, "google", "dummy.token.sig")
	if !errors.Is(oauthErr, domain.ErrOAuthProviderNotConfigured) {
		t.Fatalf("expected ErrOAuthProviderNotConfigured, got %v", oauthErr)
	}
}

func TestServiceProductionDisablesStaticTokensAndDevOTP(t *testing.T) {
	ctx := context.Background()
	clk := &clock.Fixed{At: time.Date(2026, time.October, 20, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:       config.EnvProduction,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})

	if _, err := svc.Authenticate(ctx, "rf-token-maria"); err == nil {
		t.Fatal("static demo tokens must not be active in EnvProduction")
	}
	otpRes, err := svc.RequestOTP(ctx, "maria@rosafiesta.do")
	if err != nil {
		t.Fatalf("RequestOTP failed: %v", err)
	}
	if otpRes.DevCode != "" {
		t.Fatalf("devCode must never be exposed in EnvProduction, got %q", otpRes.DevCode)
	}
}
