// Package httpapi construye el enrutador HTTP y la descripción OpenAPI.
//
// Lo comparten el servidor real y el generador de la especificación, así el contrato publicado siempre sale del mismo
// código que atiende las peticiones.
package httpapi

import (
	"log/slog"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/danielgtaylor/huma/v2"
	"github.com/danielgtaylor/huma/v2/adapters/humachi"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"

	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

const (
	apiTitle             = "RosaFiesta API"
	apiVersion           = "1.0.0"
	defaultRateLimitReqs = 120
)

// Module es un módulo del monolito que publica rutas en la API.
type Module interface {
	// Register agrega las rutas del módulo a api.
	Register(api huma.API)
}

// New arma el enrutador con sus middlewares de seguridad, límite de tasa, logs censurados y registra los módulos.
func New(logger *slog.Logger, modules ...Module) (http.Handler, huma.API) {
	router := chi.NewRouter()
	limiter := NewRateLimiter(defaultRateLimitReqs, time.Minute)
	router.Use(
		SecurityHeadersMiddleware,
		corsMiddleware,
		middleware.RequestID,
		limiter.Middleware,
		requestLogger(logger),
		middleware.Recoverer,
	)

	config := huma.DefaultConfig(apiTitle, apiVersion)
	config.Info.Description = "API de RosaFiesta (App Personal y App Administrativa). Documentación interactiva en /docs."
	config.Components.SecuritySchemes = map[string]*huma.SecurityScheme{
		SecurityScheme: {Type: "http", Scheme: "bearer", Description: "Token de acceso entregado por /v1/auth/login."},
	}
	api := humachi.New(router, config)

	for _, module := range modules {
		module.Register(api)
	}
	return router, api
}

// requestLogger registra cada petición con su método, ruta censurada, estado, duración e identificador.
func requestLogger(logger *slog.Logger) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			start := time.Now()
			wrapped := middleware.NewWrapResponseWriter(w, r.ProtoMajor)

			next.ServeHTTP(wrapped, r)

			logger.InfoContext(r.Context(), "http request",
				slog.String("method", r.Method),
				slog.String("path", redact.Text(r.URL.Path)),
				slog.Int("status", wrapped.Status()),
				slog.Duration("duration", time.Since(start)),
				slog.String("request_id", middleware.GetReqID(r.Context())),
			)
		})
	}
}

// corsMiddleware habilita Cross-Origin Resource Sharing con lista blanca configurable por CORS_ALLOWED_ORIGINS.
func corsMiddleware(next http.Handler) http.Handler {
	allowedRaw := strings.TrimSpace(os.Getenv("CORS_ALLOWED_ORIGINS"))
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		origin := strings.TrimSpace(r.Header.Get("Origin"))
		allowOrigin := resolveAllowedOrigin(origin, allowedRaw)
		w.Header().Set("Access-Control-Allow-Origin", allowOrigin)
		w.Header().Set("Vary", "Origin")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
		w.Header().Set("Access-Control-Allow-Headers", "Accept, Authorization, Content-Type, Idempotency-Key, X-Request-ID")
		w.Header().Set("Access-Control-Expose-Headers", "Link, X-Request-ID")
		w.Header().Set("Access-Control-Max-Age", "300")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}

		next.ServeHTTP(w, r)
	})
}

func resolveAllowedOrigin(requestOrigin, allowedEnv string) string {
	if allowedEnv == "" {
		if requestOrigin != "" {
			return requestOrigin
		}
		return "*"
	}
	for _, candidate := range strings.Split(allowedEnv, ",") {
		trimmed := strings.TrimSpace(candidate)
		if trimmed == "*" || strings.EqualFold(trimmed, requestOrigin) {
			return requestOrigin
		}
	}
	return "https://app.rosafiesta.do"
}
