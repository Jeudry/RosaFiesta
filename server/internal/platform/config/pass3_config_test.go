package config_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

func TestLoadProductionSecurityAndStorageValidation(t *testing.T) {
	t.Parallel()

	weakSecretEnv := map[string]string{
		"ENV":          "production",
		"DATABASE_URL": "postgres://app:pass@db:5432/rf?sslmode=disable",
		"JWT_SECRET":   "changeme-0123456789abcdef0123456789abcdef",
	}
	if _, err := config.Load(func(key string) string { return weakSecretEnv[key] }); err == nil {
		t.Fatal("expected error when JWT_SECRET uses placeholder pattern in production")
	}

	missingS3Env := map[string]string{
		"ENV":                   "production",
		"DATABASE_URL":          "postgres://app:pass@db:5432/rf?sslmode=disable",
		"JWT_SECRET":            "9f8e7d6c5b4a392817161514131211109f8e7d6c5b4a3928",
		"MEDIA_STORAGE_BACKEND": "s3",
	}
	if _, err := config.Load(func(key string) string { return missingS3Env[key] }); err == nil {
		t.Fatal("expected error when S3 settings are missing with MEDIA_STORAGE_BACKEND=s3 in production")
	}

	validS3Env := map[string]string{
		"ENV":                      "production",
		"DATABASE_URL":             "postgres://app:pass@db:5432/rf?sslmode=disable",
		"JWT_SECRET":               "9f8e7d6c5b4a392817161514131211109f8e7d6c5b4a3928",
		"MEDIA_STORAGE_BACKEND":    "s3",
		"S3_ENDPOINT":              "https://s3.us-east-1.amazonaws.com",
		"S3_BUCKET":                "rf-media",
		"S3_ACCESS_KEY_ID":         "AKIA123",
		"S3_SECRET_ACCESS_KEY":     "secret123",
		"TRUSTED_PROXIES":          "172.16.0.0/12, 127.0.0.1",
		"BOOTSTRAP_ADMIN_EMAIL":    "owner@rosafiesta.do",
		"BOOTSTRAP_ADMIN_PASSWORD": "StrongAdminPassword1!",
		"BOOTSTRAP_ADMIN_NAME":     "María Propietaria",
	}
	cfg, err := config.Load(func(key string) string { return validS3Env[key] })
	if err != nil {
		t.Fatalf("expected valid prod config with S3 and trusted proxies, got %v", err)
	}
	if cfg.MediaMaxBytes != 16<<20 {
		t.Fatalf("expected default MediaMaxBytes 16MiB, got %d", cfg.MediaMaxBytes)
	}
	if len(cfg.TrustedProxies) != 2 {
		t.Fatalf("expected 2 trusted proxies, got %v", cfg.TrustedProxies)
	}
	if cfg.BootstrapAdminEmail != "owner@rosafiesta.do" {
		t.Fatalf("expected bootstrap email loaded, got %q", cfg.BootstrapAdminEmail)
	}

	invalidProxyEnv := map[string]string{
		"TRUSTED_PROXIES": "not-an-ip",
	}
	if _, err := config.Load(func(key string) string { return invalidProxyEnv[key] }); err == nil {
		t.Fatal("expected error for invalid TRUSTED_PROXIES entry")
	}
}
