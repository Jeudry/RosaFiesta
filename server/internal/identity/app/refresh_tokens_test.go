package app_test

import (
	"context"
	"errors"
	"testing"
	"time"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

func TestService_RefreshTokenRotationAndReuseRevocation(t *testing.T) {
	fakeClk := &clock.Fixed{At: time.Date(2026, 11, 15, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(fakeClk, nil, identityapp.Options{
		Env:                   config.EnvDevelopment,
		JWTSecret:             "0123456789abcdef0123456789abcdef0123456789abcdef",
		WebPushVAPIDPublicKey: "vapid-test-key",
	})
	ctx := context.Background()

	initial, err := svc.LoginWithSession(ctx, "camila@rosafiesta.do", "secret123")
	if err != nil {
		t.Fatalf("LoginWithSession failed: %v", err)
	}
	if initial.RefreshToken == "" {
		t.Fatal("expected non-empty refresh token")
	}
	if initial.ExpiresIn <= 0 {
		t.Fatalf("expected positive expiresIn, got %d", initial.ExpiresIn)
	}

	fakeClk.Advance(24 * time.Hour)
	rotated, err := svc.RefreshSession(ctx, initial.RefreshToken)
	if err != nil {
		t.Fatalf("RefreshSession failed: %v", err)
	}
	if rotated.RefreshToken == "" {
		t.Fatal("expected rotated refresh token")
	}
	if rotated.RefreshToken == initial.RefreshToken {
		t.Fatal("expected rotated refresh token to differ from initial token")
	}
	if rotated.User.ID != initial.User.ID {
		t.Fatalf("expected same user ID %s, got %s", initial.User.ID, rotated.User.ID)
	}

	// Reusing an already rotated refresh token must revoke the whole family.
	if _, err := svc.RefreshSession(ctx, initial.RefreshToken); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected ErrInvalidCredentials on reused refresh token, got %v", err)
	}
	if _, err := svc.RefreshSession(ctx, rotated.RefreshToken); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected family revocation of rotated token, got %v", err)
	}

	// Verify VAPID public key surfaced in OAuthConfig.
	cfg := svc.OAuthConfig()
	if cfg.WebPushVAPIDPublicKey != "vapid-test-key" {
		t.Fatalf("expected vapid-test-key, got %q", cfg.WebPushVAPIDPublicKey)
	}
}

func TestService_LogoutWithRefreshRevokesSessionAndFamily(t *testing.T) {
	fakeClk := &clock.Fixed{At: time.Date(2026, 11, 15, 10, 0, 0, 0, time.UTC)}
	svc := identityapp.NewServiceWithOptions(fakeClk, nil, identityapp.Options{
		Env:       config.EnvDevelopment,
		JWTSecret: "0123456789abcdef0123456789abcdef0123456789abcdef",
	})
	ctx := context.Background()

	session, err := svc.LoginWithSession(ctx, "camila@rosafiesta.do", "secret123")
	if err != nil {
		t.Fatalf("LoginWithSession failed: %v", err)
	}
	if err := svc.LogoutWithRefresh(ctx, session.AccessToken, session.RefreshToken); err != nil {
		t.Fatalf("LogoutWithRefresh failed: %v", err)
	}
	if _, err := svc.Authenticate(ctx, session.AccessToken); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected revoked access token, got %v", err)
	}
	if _, err := svc.RefreshSession(ctx, session.RefreshToken); !errors.Is(err, domain.ErrInvalidCredentials) {
		t.Fatalf("expected revoked refresh token, got %v", err)
	}
}
