package config

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"strconv"
	"strings"
)

type Environment string

const (
	EnvDevelopment Environment = "development"
	EnvProduction  Environment = "production"
)

const (
	SeedModeAdminOnly = "admin_only"
	SeedModeFull      = "full"
)

const (
	defaultPort            = 8788
	defaultSMTPPort        = 587
	defaultMediaMaxBytes   = 10 << 20
	minJWTSecretLength     = 32
	defaultPublicBaseURL   = "http://localhost:8788"
	defaultMediaStorageDir = "./var/media"
	defaultAIProvider      = "gemini"
	defaultGeminiModel     = "gemini-2.5-flash-lite"
	defaultGroqAIModel     = "llama-3.3-70b-versatile"
	defaultOpenAIModel     = "gpt-4o-mini"
	defaultGroqSTTModel    = "whisper-large-v3-turbo"
	defaultOpenAISTTModel  = "gpt-4o-mini-transcribe"
)

// Config centraliza la configuración cargada desde variables de entorno.
type Config struct {
	Env                    Environment
	Port                   int
	SeedMode               string
	DatabaseURL            string
	JWTSecret              string
	CORSAllowedOrigins     []string
	PublicAppBaseURL       string
	MediaStorageBackend    string
	MediaStorageDir        string
	MediaMaxBytes          int64
	S3Endpoint             string
	S3Bucket               string
	S3Region               string
	S3AccessKeyID          string
	S3SecretAccessKey      string
	S3PublicBaseURL        string
	SMTPHost               string
	SMTPPort               int
	SMTPFrom               string
	SMTPUsername           string
	SMTPPassword           string
	RequireConfiguredSMTP  bool
	GoogleClientID         string
	GoogleClientSecret     string
	AppleClientID          string
	AppleClientSecret      string
	AppleTeamID            string
	AppleKeyID             string
	ApplePrivateKey        string
	FCMServerURL           string
	FCMServerKey           string
	FCMProjectID           string
	FCMServiceAccountJSON  string
	APNSKeyID              string
	APNSTeamID             string
	APNSBundleID           string
	APNSPrivateKey         string
	APNSUseSandbox         bool
	WebPushVAPIDPublicKey  string
	WebPushVAPIDPrivateKey string
	WebPushVAPIDSubject    string
	AIProvider             string
	AIModel                string
	AIProviderAPIKey       string
	STTProvider            string
	STTModel               string
	STTAPIURL              string
	STTAPIKey              string
	StripeSecretKey        string
	StripeWebhookSecret    string
	WhatsAppPhoneNumberID  string
	WhatsAppAccessToken    string
	WhatsAppVerifyToken    string
	WhatsAppAppSecret      string
}

// Load lee y valida la configuración del servidor a partir de getenv.
func Load(getenv func(string) string) (Config, error) {
	env, err := parseEnvironment(getenv)
	if err != nil {
		return Config{}, err
	}
	port, err := parsePort(getenv("PORT"), defaultPort, "PORT")
	if err != nil {
		return Config{}, err
	}
	dbURL, err := resolveDatabaseURL(getenv, env)
	if err != nil {
		return Config{}, err
	}
	jwtSecret, err := resolveJWTSecret(getenv("JWT_SECRET"), env)
	if err != nil {
		return Config{}, err
	}
	corsOrigins, err := resolveCORSOrigins(getenv("CORS_ALLOWED_ORIGINS"), env)
	if err != nil {
		return Config{}, err
	}
	cfg := Config{
		Env:                 env,
		Port:                port,
		SeedMode:            resolveSeedMode(getenv("ROSAFIESTA_SEED_MODE")),
		DatabaseURL:         dbURL,
		JWTSecret:           jwtSecret,
		CORSAllowedOrigins:  corsOrigins,
		PublicAppBaseURL:    fallbackString(getenv("PUBLIC_APP_BASE_URL"), defaultPublicBaseURL),
		MediaStorageBackend: fallbackString(getenv("MEDIA_STORAGE_BACKEND"), "local"),
		MediaStorageDir:     fallbackString(getenv("MEDIA_STORAGE_DIR"), defaultMediaStorageDir),
	}
	if err := populateIntegrations(getenv, &cfg); err != nil {
		return Config{}, err
	}
	return cfg, nil
}

func populateIntegrations(getenv func(string) string, cfg *Config) error {
	smtpPort, err := parsePort(getenv("SMTP_PORT"), defaultSMTPPort, "SMTP_PORT")
	if err != nil {
		return err
	}
	mediaMaxBytes, err := parsePositiveInt64(getenv("MEDIA_MAX_BYTES"), defaultMediaMaxBytes, "MEDIA_MAX_BYTES")
	if err != nil {
		return err
	}
	aiProvider := resolveProviderName(getenv("AI_PROVIDER"), defaultAIProvider)
	sttProvider := resolveProviderName(getenv("STT_PROVIDER"), aiProvider)
	aiAPIKey := strings.TrimSpace(getenv("AI_PROVIDER_API_KEY"))

	cfg.MediaMaxBytes = mediaMaxBytes
	populateStorageAndPushIntegrations(getenv, cfg)
	cfg.SMTPHost = strings.TrimSpace(getenv("SMTP_HOST"))
	cfg.SMTPPort = smtpPort
	cfg.SMTPFrom = fallbackString(getenv("SMTP_FROM"), "no-reply@rosafiesta.do")
	cfg.SMTPUsername = strings.TrimSpace(getenv("SMTP_USERNAME"))
	cfg.SMTPPassword = strings.TrimSpace(getenv("SMTP_PASSWORD"))
	cfg.RequireConfiguredSMTP = !parseBool(getenv("ALLOW_SIMULATED_OTP"))
	cfg.GoogleClientID = strings.TrimSpace(getenv("GOOGLE_CLIENT_ID"))
	cfg.GoogleClientSecret = strings.TrimSpace(getenv("GOOGLE_CLIENT_SECRET"))
	cfg.AppleClientID = strings.TrimSpace(getenv("APPLE_CLIENT_ID"))
	cfg.AppleClientSecret = strings.TrimSpace(getenv("APPLE_CLIENT_SECRET"))
	cfg.AppleTeamID = strings.TrimSpace(getenv("APPLE_TEAM_ID"))
	cfg.AppleKeyID = strings.TrimSpace(getenv("APPLE_KEY_ID"))
	cfg.ApplePrivateKey = strings.TrimSpace(getenv("APPLE_PRIVATE_KEY"))
	cfg.AIProvider = aiProvider
	cfg.AIModel = resolveAIModel(aiProvider, getenv("AI_MODEL"))
	cfg.AIProviderAPIKey = aiAPIKey
	cfg.STTProvider = sttProvider
	cfg.STTModel = resolveSTTModel(sttProvider, getenv("STT_MODEL"))
	cfg.STTAPIURL = strings.TrimSpace(getenv("STT_API_URL"))
	cfg.STTAPIKey = resolveSTTAPIKey(getenv("STT_API_KEY"), aiAPIKey, sttProvider, aiProvider)
	cfg.StripeSecretKey = strings.TrimSpace(getenv("STRIPE_SECRET_KEY"))
	cfg.StripeWebhookSecret = strings.TrimSpace(getenv("STRIPE_WEBHOOK_SECRET"))
	cfg.WhatsAppPhoneNumberID = strings.TrimSpace(getenv("WHATSAPP_PHONE_NUMBER_ID"))
	cfg.WhatsAppAccessToken = strings.TrimSpace(getenv("WHATSAPP_ACCESS_TOKEN"))
	cfg.WhatsAppVerifyToken = strings.TrimSpace(getenv("WHATSAPP_VERIFY_TOKEN"))
	cfg.WhatsAppAppSecret = strings.TrimSpace(getenv("WHATSAPP_APP_SECRET"))
	return nil
}

func populateStorageAndPushIntegrations(getenv func(string) string, cfg *Config) {
	cfg.S3Endpoint = strings.TrimSpace(getenv("S3_ENDPOINT"))
	cfg.S3Bucket = strings.TrimSpace(getenv("S3_BUCKET"))
	cfg.S3Region = fallbackString(getenv("S3_REGION"), "auto")
	cfg.S3AccessKeyID = strings.TrimSpace(getenv("S3_ACCESS_KEY_ID"))
	cfg.S3SecretAccessKey = strings.TrimSpace(getenv("S3_SECRET_ACCESS_KEY"))
	cfg.S3PublicBaseURL = strings.TrimSpace(getenv("S3_PUBLIC_BASE_URL"))
	cfg.FCMServerURL = strings.TrimSpace(getenv("FCM_SERVER_URL"))
	cfg.FCMServerKey = strings.TrimSpace(getenv("FCM_SERVER_KEY"))
	cfg.FCMProjectID = strings.TrimSpace(getenv("FCM_PROJECT_ID"))
	cfg.FCMServiceAccountJSON = strings.TrimSpace(getenv("FCM_SERVICE_ACCOUNT_JSON"))
	cfg.APNSKeyID = strings.TrimSpace(getenv("APNS_KEY_ID"))
	cfg.APNSTeamID = strings.TrimSpace(getenv("APNS_TEAM_ID"))
	cfg.APNSBundleID = fallbackString(getenv("APNS_BUNDLE_ID"), "com.jeudry.rosafiesta")
	cfg.APNSPrivateKey = strings.TrimSpace(getenv("APNS_PRIVATE_KEY"))
	cfg.APNSUseSandbox = parseBool(getenv("APNS_USE_SANDBOX"))
	cfg.WebPushVAPIDPublicKey = strings.TrimSpace(getenv("WEB_PUSH_VAPID_PUBLIC_KEY"))
	cfg.WebPushVAPIDPrivateKey = strings.TrimSpace(getenv("WEB_PUSH_VAPID_PRIVATE_KEY"))
	cfg.WebPushVAPIDSubject = fallbackString(getenv("WEB_PUSH_VAPID_SUBJECT"), "mailto:hola@rosafiesta.do")
}

func parseEnvironment(getenv func(string) string) (Environment, error) {
	raw := strings.TrimSpace(getenv("ENV"))
	if raw == "" {
		raw = strings.TrimSpace(getenv("APP_ENV"))
	}
	if raw == "" {
		return EnvDevelopment, nil
	}
	env := Environment(raw)
	isValidDev := env == EnvDevelopment
	isValidProd := env == EnvProduction
	if isValidDev {
		return env, nil
	}
	if isValidProd {
		return env, nil
	}
	return "", fmt.Errorf("invalid ENV %q", env)
}

func parsePort(raw string, fallback int, fieldName string) (int, error) {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return fallback, nil
	}
	parsed, err := strconv.Atoi(trimmed)
	if err != nil {
		return 0, fmt.Errorf("invalid %s %q", fieldName, raw)
	}
	isOutOfBounds := parsed <= 0
	if !isOutOfBounds {
		isOutOfBounds = parsed > 65535
	}
	if isOutOfBounds {
		return 0, fmt.Errorf("invalid %s %q", fieldName, raw)
	}
	return parsed, nil
}

func resolveDatabaseURL(getenv func(string) string, env Environment) (string, error) {
	dbURL := strings.TrimSpace(getenv("DATABASE_URL"))
	if dbURL != "" {
		return dbURL, nil
	}
	if env == EnvProduction {
		return "", errors.New("DATABASE_URL is required in production")
	}
	dbUser := fallbackString(getenv("POSTGRES_USER"), "rosafiesta")
	dbPass := fallbackString(getenv("POSTGRES_PASSWORD"), "rosafiesta_dev")
	dbHost := fallbackString(getenv("POSTGRES_HOST"), "localhost:5433")
	dbName := fallbackString(getenv("POSTGRES_DB"), "rosafiesta")
	built := fmt.Sprintf("postgres://%s:%s@%s/%s?sslmode=disable", dbUser, dbPass, dbHost, dbName)
	return built, nil
}

func resolveJWTSecret(raw string, env Environment) (string, error) {
	trimmed := strings.TrimSpace(raw)
	if env == EnvProduction {
		if len(trimmed) < minJWTSecretLength {
			return "", errors.New("JWT_SECRET with at least 32 characters is required in production")
		}
		return trimmed, nil
	}
	if trimmed != "" {
		return trimmed, nil
	}
	buf := make([]byte, minJWTSecretLength)
	if _, err := rand.Read(buf); err != nil {
		return "", fmt.Errorf("generate ephemeral dev jwt key: %w", err)
	}
	return hex.EncodeToString(buf), nil
}

func resolveCORSOrigins(raw string, env Environment) ([]string, error) {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		if env == EnvProduction {
			return []string{"https://app.rosafiesta.do", "https://admin.rosafiesta.do"}, nil
		}
		return []string{"*"}, nil
	}
	parts := strings.Split(trimmed, ",")
	origins := make([]string, 0, len(parts))
	for _, part := range parts {
		candidate := strings.TrimSpace(part)
		if candidate == "" {
			continue
		}
		isProdWildcard := env == EnvProduction
		if isProdWildcard {
			isProdWildcard = candidate == "*"
		}
		if isProdWildcard {
			return nil, errors.New("wildcard CORS_ALLOWED_ORIGINS is forbidden in production")
		}
		origins = append(origins, candidate)
	}
	if len(origins) == 0 {
		return []string{"*"}, nil
	}
	return origins, nil
}

func resolveSeedMode(raw string) string {
	normalized := strings.ToLower(strings.TrimSpace(raw))
	if normalized == SeedModeFull {
		return SeedModeFull
	}
	return SeedModeAdminOnly
}

func fallbackString(raw, fallback string) string {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return fallback
	}
	return trimmed
}

func parsePositiveInt64(raw string, fallback int64, fieldName string) (int64, error) {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return fallback, nil
	}
	parsed, err := strconv.ParseInt(trimmed, 10, 64)
	if err != nil {
		return 0, fmt.Errorf("invalid %s %q", fieldName, raw)
	}
	if parsed <= 0 {
		return 0, fmt.Errorf("invalid %s %q", fieldName, raw)
	}
	return parsed, nil
}

func parseBool(raw string) bool {
	switch strings.ToLower(strings.TrimSpace(raw)) {
	case "true", "1", "yes":
		return true
	default:
		return false
	}
}

func resolveProviderName(raw, fallback string) string {
	normalized := strings.ToLower(strings.TrimSpace(raw))
	switch normalized {
	case "gemini", "groq", "openai":
		return normalized
	default:
		return fallback
	}
}

func resolveAIModel(provider, raw string) string {
	trimmed := strings.TrimSpace(raw)
	if trimmed != "" {
		return trimmed
	}
	switch provider {
	case "groq":
		return defaultGroqAIModel
	case "openai":
		return defaultOpenAIModel
	default:
		return defaultGeminiModel
	}
}

func resolveSTTModel(provider, raw string) string {
	trimmed := strings.TrimSpace(raw)
	if trimmed != "" {
		return trimmed
	}
	switch provider {
	case "groq":
		return defaultGroqSTTModel
	case "openai":
		return defaultOpenAISTTModel
	default:
		return defaultGeminiModel
	}
}

func resolveSTTAPIKey(explicitSTTKey, aiKey, sttProvider, aiProvider string) string {
	trimmedSTT := strings.TrimSpace(explicitSTTKey)
	if trimmedSTT != "" {
		return trimmedSTT
	}
	if sttProvider == aiProvider {
		return aiKey
	}
	return ""
}
