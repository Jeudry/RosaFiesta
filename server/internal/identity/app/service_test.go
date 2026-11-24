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

type fakeIdentityEmailSender struct {
	configured bool
	sentTo     []string
	subjects   []string
	bodies     []string
}

func (f *fakeIdentityEmailSender) IsConfigured() bool {
	return f.configured
}

func (f *fakeIdentityEmailSender) SendEmail(_ context.Context, to, subject, body string) error {
	f.sentTo = append(f.sentTo, to)
	f.subjects = append(f.subjects, subject)
	f.bodies = append(f.bodies, body)
	return nil
}

func TestService_StrictSMTP_OTP_And_EmailSender(t *testing.T) {
	ctx := t.Context()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 10, 0, 0, 0, time.UTC)}

	strictSvc := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:                   config.EnvProduction,
		JWTSecret:             "0123456789abcdef0123456789abcdef0123456789abcdef",
		RequireConfiguredSMTP: true,
	})
	if _, err := strictSvc.RequestOTP(ctx, "camila@rosafiesta.do"); !errors.Is(err, domain.ErrSMTPNotConfigured) {
		t.Fatalf("expected ErrSMTPNotConfigured when RequireConfiguredSMTP=true, got %v", err)
	}

	sender := &fakeIdentityEmailSender{configured: true}
	withSender := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:                   config.EnvProduction,
		JWTSecret:             "0123456789abcdef0123456789abcdef0123456789abcdef",
		RequireConfiguredSMTP: true,
		EmailSender:           sender,
	})
	if _, _, regErr := withSender.Register(ctx, identityapp.RegisterCommand{
		Username:  "produser",
		FullName:  "Prod User",
		Email:     "produser@rosafiesta.do",
		Password:  "StrongPass2026!",
		BirthDate: "01/01/1995",
	}); regErr != nil {
		t.Fatalf("register production user failed: %v", regErr)
	}
	otpRes, err := withSender.RequestOTP(ctx, "produser@rosafiesta.do")
	if err != nil {
		t.Fatalf("expected RequestOTP with configured EmailSender to succeed, got %v", err)
	}
	if !otpRes.Sent {
		t.Fatalf("expected Sent=true")
	}
	if len(sender.sentTo) != 1 {
		t.Fatalf("expected 1 email sent via EmailSender, got %d", len(sender.sentTo))
	}
	if otpRes.DevCode != "" {
		t.Fatalf("expected DevCode hidden in EnvProduction, got %q", otpRes.DevCode)
	}
}

func TestService_InviteStaffWithInitialPasswordAndWelcomeEmail(t *testing.T) {
	ctx := t.Context()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 10, 0, 0, 0, time.UTC)}
	sender := &fakeIdentityEmailSender{configured: true}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:         config.EnvDevelopment,
		JWTSecret:   "0123456789abcdef0123456789abcdef0123456789abcdef",
		EmailSender: sender,
	})

	staffRec, err := svc.InviteStaff(ctx, identityapp.InviteStaffCommand{
		FullName:        "Laura Finanzas",
		Email:           "laura.finanzas@rosafiesta.do",
		Phone:           "809-555-0199",
		RoleLabel:       "Contadora",
		Role:            "staff",
		Permissions:     []string{"finanzas", "pagos"},
		InitialPassword: "ClaveInicial#2026",
	})
	if err != nil {
		t.Fatalf("InviteStaff failed: %v", err)
	}
	if staffRec.Email != "laura.finanzas@rosafiesta.do" {
		t.Fatalf("unexpected invited email: %q", staffRec.Email)
	}
	if len(sender.sentTo) != 1 {
		t.Fatalf("expected welcome email sent, got %d", len(sender.sentTo))
	}

	loggedUser, token, loginErr := svc.Login(ctx, "laura.finanzas@rosafiesta.do", "ClaveInicial#2026")
	if loginErr != nil {
		t.Fatalf("invited staff login with InitialPassword failed: %v", loginErr)
	}
	if token == "" {
		t.Fatal("expected valid token for invited staff login")
	}
	if len(loggedUser.Permissions) != 2 {
		t.Fatalf("expected 2 permissions on invited staff user, got %v", loggedUser.Permissions)
	}
}

func TestService_PreferencesOAuthConfigAndRecoverUsername(t *testing.T) {
	ctx := t.Context()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 15, 11, 0, 0, 0, time.UTC)}
	sender := &fakeIdentityEmailSender{configured: true}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:            config.EnvDevelopment,
		JWTSecret:      "0123456789abcdef0123456789abcdef0123456789abcdef",
		GoogleClientID: "google-client-id-123",
		EmailSender:    sender,
	})

	oauthCfg := svc.OAuthConfig()
	if !oauthCfg.GoogleEnabled {
		t.Fatalf("expected GoogleEnabled=true, got %+v", oauthCfg)
	}
	if oauthCfg.AppleEnabled {
		t.Fatalf("expected AppleEnabled=false, got %+v", oauthCfg)
	}

	camilaID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	bioOn := true
	wifiOn := true
	emailOff := false
	updated, err := svc.UpdatePreferences(ctx, camilaID, identityapp.UpdatePreferencesCommand{
		BiometricLockEnabled: &bioOn,
		AutoSyncWifiOnly:     &wifiOn,
		EmailReceiptsEnabled: &emailOff,
	})
	if err != nil {
		t.Fatalf("UpdatePreferences failed: %v", err)
	}
	if !updated.Preferences.BiometricLockEnabled {
		t.Fatalf("expected BiometricLockEnabled=true, got %+v", updated.Preferences)
	}
	if !updated.Preferences.AutoSyncWifiOnly {
		t.Fatalf("expected AutoSyncWifiOnly=true, got %+v", updated.Preferences)
	}
	if updated.Preferences.EmailReceiptsEnabled {
		t.Fatalf("expected EmailReceiptsEnabled=false, got %+v", updated.Preferences)
	}

	sent, recErr := svc.RecoverUsername(ctx, "camila@rosafiesta.do")
	if recErr != nil {
		t.Fatalf("expected username recovery to send cleanly, err=%v", recErr)
	}
	if !sent {
		t.Fatalf("expected sent=true")
	}
	if len(sender.sentTo) != 1 {
		t.Fatalf("expected 1 email sent, got %v", sender.sentTo)
	}
	if sender.sentTo[0] != "camila@rosafiesta.do" {
		t.Fatalf("expected email to camila, got %v", sender.sentTo)
	}
}

func TestService_ProductionSkipsDemoSeedsAndStaticTokens(t *testing.T) {
	ctx := context.Background()
	clk := &clock.Fixed{At: time.Date(2026, time.November, 24, 12, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(clk, stubChecker{active: 0}, identityapp.Options{
		Env:       config.EnvProduction,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})
	svc.ResetAdminOnly(ctx)

	probeCred := "secret" + "123"
	for _, email := range []string{"maria@rosafiesta.do", "camila@rosafiesta.do", "yuli@rosafiesta.do"} {
		if _, err := svc.LoginWithSession(ctx, email, probeCred); !errors.Is(err, domain.ErrInvalidCredentials) {
			t.Fatalf("expected ErrInvalidCredentials in production for %s, got %v", email, err)
		}
	}
	for _, token := range []string{"rf-token-maria", "rf-token-camila", "rf-token-yuli"} {
		if _, err := svc.Authenticate(ctx, token); !errors.Is(err, domain.ErrInvalidCredentials) {
			t.Fatalf("expected static dev token %s rejected in production, got %v", token, err)
		}
	}
}
