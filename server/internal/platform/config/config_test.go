package config_test

import (
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

func TestLoadDefaultsAndValidation(t *testing.T) {
	cfg, err := config.Load(func(string) string { return "" })
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if cfg.Port != 8788 || cfg.Env != config.EnvDevelopment {
		t.Fatalf("unexpected default config: %+v", cfg)
	}

	_, err = config.Load(func(key string) string {
		if key == "ENV" {
			return "production"
		}
		return ""
	})
	if err == nil {
		t.Fatal("expected error when DATABASE_URL is missing in production")
	}
}
