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
		"GOOGLE_CLIENT_SECRET": "google-secret-123",
		"FCM_SERVER_KEY":       "fcm-key-123",
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
	if prodCfg.GoogleClientSecret != "google-secret-123" {
		t.Fatalf("expected GoogleClientSecret loaded, got %q", prodCfg.GoogleClientSecret)
	}
	if prodCfg.FCMServerKey != "fcm-key-123" {
		t.Fatalf("expected FCMServerKey loaded, got %q", prodCfg.FCMServerKey)
	}
	if prodCfg.AIProvider != "gemini" {
		t.Fatalf("expected default AIProvider gemini, got %s", prodCfg.AIProvider)
	}
	if prodCfg.AIModel != "gemini-2.5-flash-lite" {
		t.Fatalf("expected default AIModel gemini-2.5-flash-lite, got %s", prodCfg.AIModel)
	}
	if !prodCfg.RequireConfiguredSMTP {
		t.Fatal("expected RequireConfiguredSMTP true by default when ALLOW_SIMULATED_OTP is unset")
	}
}

func TestLoadProviderAndSMTPConfig(t *testing.T) {
	groqEnv := map[string]string{
		"AI_PROVIDER":         "groq",
		"AI_PROVIDER_API_KEY": "gsk_test",
		"STT_PROVIDER":        "groq",
		"ALLOW_SIMULATED_OTP": "true",
		"MEDIA_MAX_BYTES":     "2097152",
	}
	groqCfg, err := config.Load(func(key string) string { return groqEnv[key] })
	if err != nil {
		t.Fatalf("unexpected error loading groq env: %v", err)
	}
	if groqCfg.STTAPIKey != "gsk_test" {
		t.Fatalf("expected STTAPIKey fallback from matching AI provider, got %q", groqCfg.STTAPIKey)
	}
	if groqCfg.STTModel != "whisper-large-v3-turbo" {
		t.Fatalf("expected default groq STT model, got %s", groqCfg.STTModel)
	}
	if groqCfg.RequireConfiguredSMTP {
		t.Fatal("expected RequireConfiguredSMTP false when ALLOW_SIMULATED_OTP=true")
	}
}

func TestLoadInfraPushAndStorageConfig(t *testing.T) {
	infraEnv := map[string]string{
		"MEDIA_STORAGE_BACKEND":      "s3",
		"S3_ENDPOINT":                "https://s3.us-east-1.amazonaws.com",
		"S3_BUCKET":                  "rosafiesta-media",
		"S3_REGION":                  "us-east-1",
		"S3_ACCESS_KEY_ID":           "test-key",
		"S3_SECRET_ACCESS_KEY":       "test-secret",
		"S3_PUBLIC_BASE_URL":         "https://cdn.rosafiesta.do",
		"APNS_KEY_ID":                "ABC123DEFG",
		"APNS_TEAM_ID":               "TEAM123456",
		"APNS_BUNDLE_ID":             "com.jeudry.rosafiesta",
		"APNS_PRIVATE_KEY":           "pem-key-placeholder",
		"APNS_USE_SANDBOX":           "true",
		"WEB_PUSH_VAPID_PUBLIC_KEY":  "vapid-public-key-value",
		"WEB_PUSH_VAPID_PRIVATE_KEY": "vapid-private-key-value",
		"WEB_PUSH_VAPID_SUBJECT":     "mailto:push@rosafiesta.do",
		"FCM_PROJECT_ID":             "rosafiesta-fcm",
		"FCM_SERVICE_ACCOUNT_JSON":   "{\"project_id\":\"rosafiesta-fcm\"}",
	}
	infraCfg, err := config.Load(func(key string) string { return infraEnv[key] })
	if err != nil {
		t.Fatalf("unexpected error loading infra env: %v", err)
	}
	if infraCfg.MediaStorageBackend != "s3" {
		t.Fatalf("expected s3 backend, got %q", infraCfg.MediaStorageBackend)
	}
	if infraCfg.S3Bucket != "rosafiesta-media" {
		t.Fatalf("expected s3 bucket rosafiesta-media, got %q", infraCfg.S3Bucket)
	}
	if infraCfg.APNSKeyID != "ABC123DEFG" {
		t.Fatalf("expected apns keyId ABC123DEFG, got %q", infraCfg.APNSKeyID)
	}
	if !infraCfg.APNSUseSandbox {
		t.Fatal("expected APNSUseSandbox true")
	}
	if infraCfg.WebPushVAPIDPublicKey != "vapid-public-key-value" {
		t.Fatalf("expected WebPushVAPIDPublicKey loaded, got %q", infraCfg.WebPushVAPIDPublicKey)
	}
	if infraCfg.WebPushVAPIDPrivateKey != "vapid-private-key-value" {
		t.Fatalf("expected WebPushVAPIDPrivateKey loaded, got %q", infraCfg.WebPushVAPIDPrivateKey)
	}
	if infraCfg.FCMProjectID != "rosafiesta-fcm" {
		t.Fatalf("expected FCMProjectID loaded, got %q", infraCfg.FCMProjectID)
	}
}
