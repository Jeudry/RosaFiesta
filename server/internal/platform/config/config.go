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
	defaultPort        = 8788
	defaultSMTPPort    = 587
	minJWTSecretLength = 32
)

type Config struct {
	Env                Environment
	Port               int
	SeedMode           string
	DatabaseURL        string
	JWTSecret          string
	CORSAllowedOrigins []string
	SMTPHost           string
	SMTPPort           int
	SMTPFrom           string
	SMTPUsername       string
	SMTPPassword       string
	GoogleClientID     string
	AppleClientID      string
	STTAPIURL          string
	AIProviderAPIKey   string
}

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
	smtpPort, err := parsePort(getenv("SMTP_PORT"), defaultSMTPPort, "SMTP_PORT")
	if err != nil {
		return Config{}, err
	}
	smtpFrom := strings.TrimSpace(getenv("SMTP_FROM"))
	if smtpFrom == "" {
		smtpFrom = "no-reply@rosafiesta.do"
	}
	return Config{
		Env:                env,
		Port:               port,
		SeedMode:           resolveSeedMode(getenv("ROSAFIESTA_SEED_MODE")),
		DatabaseURL:        dbURL,
		JWTSecret:          jwtSecret,
		CORSAllowedOrigins: corsOrigins,
		SMTPHost:           strings.TrimSpace(getenv("SMTP_HOST")),
		SMTPPort:           smtpPort,
		SMTPFrom:           smtpFrom,
		SMTPUsername:       strings.TrimSpace(getenv("SMTP_USERNAME")),
		SMTPPassword:       strings.TrimSpace(getenv("SMTP_PASSWORD")),
		GoogleClientID:     strings.TrimSpace(getenv("GOOGLE_CLIENT_ID")),
		AppleClientID:      strings.TrimSpace(getenv("APPLE_CLIENT_ID")),
		STTAPIURL:          strings.TrimSpace(getenv("STT_API_URL")),
		AIProviderAPIKey:   strings.TrimSpace(getenv("AI_PROVIDER_API_KEY")),
	}, nil
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
