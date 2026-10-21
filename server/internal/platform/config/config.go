// Package config carga y valida las variables de entorno del servidor.
package config

import (
	"errors"
	"fmt"
	"strconv"
)

// Environment representa el entorno de ejecución.
type Environment string

const (
	// EnvDevelopment es el entorno local de desarrollo.
	EnvDevelopment Environment = "development"
	// EnvProduction es el entorno de producción.
	EnvProduction Environment = "production"
)

const (
	defaultPort        = 8788
	defaultDatabaseURL = "postgres://rosafiesta:rosafiesta_dev@localhost:5433/rosafiesta?sslmode=disable" //nolint:gosec // cadena de conexión local por defecto
)

// Config contiene la configuración validada del servidor.
type Config struct {
	// Env es el entorno de ejecución.
	Env Environment
	// Port es el puerto TCP donde escucha HTTP.
	Port int
	// DatabaseURL es la cadena de conexión a PostgreSQL.
	DatabaseURL string
}

// Load construye y valida [Config] a partir de getenv.
func Load(getenv func(string) string) (Config, error) {
	env := Environment(getenv("ENV"))
	if env == "" {
		env = EnvDevelopment
	}
	if env != EnvDevelopment && env != EnvProduction {
		return Config{}, fmt.Errorf("invalid ENV %q", env)
	}

	port := defaultPort
	if rawPort := getenv("PORT"); rawPort != "" {
		parsed, err := strconv.Atoi(rawPort)
		if err != nil || parsed <= 0 || parsed > 65535 {
			return Config{}, fmt.Errorf("invalid PORT %q", rawPort)
		}
		port = parsed
	}

	dbURL := getenv("DATABASE_URL")
	if dbURL == "" {
		if env == EnvProduction {
			return Config{}, errors.New("DATABASE_URL is required in production")
		}
		dbURL = defaultDatabaseURL
	}

	return Config{
		Env:         env,
		Port:        port,
		DatabaseURL: dbURL,
	}, nil
}
