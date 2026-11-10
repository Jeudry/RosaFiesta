package config_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

func TestLoadDevelopmentDefaults(t *testing.T) {
	cfg, err := config.Load(func(string) string { return "" })
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if cfg.Port != 8788 {
		t.Fatalf("unexpected default port: %d", cfg.Port)
	}
	if cfg.Env != config.EnvDevelopment {
		t.Fatalf("unexpected default env: %s", cfg.Env)
	}
	if len(cfg.JWTSecret) < 32 {
		t.Fatalf("expected ephemeral dev JWT key >= 32 chars, got %d", len(cfg.JWTSecret))
	}
	if len(cfg.CORSAllowedOrigins) != 1 {
		t.Fatalf("expected default dev CORS [*], got %v", cfg.CORSAllowedOrigins)
	}
}

func TestLoadProductionValidation(t *testing.T) {
	_, err := config.Load(func(key string) string {
		if key == "ENV" {
			return "production"
		}
		return ""
	})
	if err == nil {
		t.Fatal("expected error when DATABASE_URL is missing in production")
	}

	missingSecretEnv := map[string]string{
		"APP_ENV":      "production",
		"DATABASE_URL": "postgres://app:pass@db:5432/rf?sslmode=disable",
		"JWT_SECRET":   "too-short",
	}
	_, err = config.Load(func(key string) string { return missingSecretEnv[key] })
	if err == nil {
		t.Fatal("expected error when JWT_SECRET is shorter than 32 chars in production")
	}

	validProdEnv := map[string]string{
		"APP_ENV":              "production",
		"DATABASE_URL":         "postgres://app:pass@db:5432/rf?sslmode=disable",
		"JWT_SECRET":           "0123456789abcdef0123456789abcdef0123456789abcdef",
		"CORS_ALLOWED_ORIGINS": "https://app.rosafiesta.do, https://admin.rosafiesta.do",
		"GOOGLE_CLIENT_ID":     "google-client-123",
	}
	prodCfg, err := config.Load(func(key string) string { return validProdEnv[key] })
	if err != nil {
		t.Fatalf("expected valid production config via APP_ENV fallback, got %v", err)
	}
	if prodCfg.Env != config.EnvProduction {
		t.Fatalf("expected production env, got %s", prodCfg.Env)
	}
	if prodCfg.GoogleClientID != "google-client-123" {
		t.Fatalf("expected GoogleClientID loaded, got %q", prodCfg.GoogleClientID)
	}
}
