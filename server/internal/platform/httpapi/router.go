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
	apiTitle                 = "RosaFiesta API"
	apiVersion               = "1.0.0"
	defaultRateLimitReqs     = 240
	defaultAuthRateLimitReqs = 20
)

type Module interface {
	Register(api huma.API)
}

// RawRouterModule permite registrar rutas HTTP en streaming o binarias directamente en chi.
type RawRouterModule interface {
	RegisterRoutes(router chi.Router)
}

type RouterOptions struct {
	AllowedOrigins []string
	DisableDocs    bool
	TrustedProxies []string
}

func New(logger *slog.Logger, modules ...Module) (http.Handler, huma.API) {
	rawOrigins := strings.TrimSpace(os.Getenv("CORS_ALLOWED_ORIGINS"))
	var origins []string
	if rawOrigins != "" {
		origins = strings.Split(rawOrigins, ",")
	}
	opts := RouterOptions{AllowedOrigins: origins}
	return NewWithOptions(logger, opts, modules...)
}

func NewWithOptions(logger *slog.Logger, opts RouterOptions, modules ...Module) (http.Handler, huma.API) {
	router := chi.NewRouter()
	limiter := NewRateLimiterWithAuth(defaultRateLimitReqs, defaultAuthRateLimitReqs, time.Minute).
		WithTrustedProxies(opts.TrustedProxies)
	corsHandler := buildCORSMiddleware(opts.AllowedOrigins)
	logHandler := requestLogger(logger)
	router.Use(
		SecurityHeadersMiddleware,
		corsHandler,
		middleware.RequestID,
		limiter.Middleware,
		logHandler,
		middleware.Recoverer,
	)

	apiCfg := huma.DefaultConfig(apiTitle, apiVersion)
	apiCfg.Info.Description = "API de RosaFiesta (App Personal y App Administrativa). Documentación interactiva en /docs."
	if opts.DisableDocs {
		apiCfg.DocsPath = ""
		apiCfg.OpenAPIPath = ""
	}
	apiCfg.Components.SecuritySchemes = map[string]*huma.SecurityScheme{
		SecurityScheme: {
			Type:        "http",
			Scheme:      "bearer",
			Description: "Token de acceso entregado por /v1/auth/login.",
		},
	}
	api := humachi.New(router, apiCfg)

	for _, module := range modules {
		module.Register(api)
		if rawModule, ok := module.(RawRouterModule); ok {
			rawModule.RegisterRoutes(router)
		}
	}
	return router, api
}

func requestLogger(logger *slog.Logger) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			start := time.Now()
			wrapped := middleware.NewWrapResponseWriter(w, r.ProtoMajor)

			next.ServeHTTP(wrapped, r)

			safePath := redact.Text(r.URL.Path)
			status := wrapped.Status()
			duration := time.Since(start)
			reqCtx := r.Context()
			reqID := middleware.GetReqID(reqCtx)
			methodAttr := slog.String("method", r.Method)
			pathAttr := slog.String("path", safePath)
			statusAttr := slog.Int("status", status)
			durationAttr := slog.Duration("duration", duration)
			reqIDAttr := slog.String("request_id", reqID)
			logger.InfoContext(reqCtx, "http request", methodAttr, pathAttr, statusAttr, durationAttr, reqIDAttr)
		})
	}
}

func buildCORSMiddleware(allowedOrigins []string) func(http.Handler) http.Handler {
	normalized := normalizeOriginList(allowedOrigins)
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			requestOrigin := strings.TrimSpace(r.Header.Get("Origin"))
			allowOrigin := resolveAllowedOrigin(requestOrigin, normalized)
			headers := w.Header()
			if allowOrigin != "" {
				headers.Set("Access-Control-Allow-Origin", allowOrigin)
			}
			headers.Set("Vary", "Origin")
			headers.Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
			headers.Set("Access-Control-Allow-Headers", "Accept, Authorization, Content-Type, Idempotency-Key, X-Request-ID")
			headers.Set("Access-Control-Expose-Headers", "Link, X-Request-ID")
			headers.Set("Access-Control-Max-Age", "300")

			if r.Method == http.MethodOptions {
				w.WriteHeader(http.StatusNoContent)
				return
			}
			next.ServeHTTP(w, r)
		})
	}
}

func normalizeOriginList(raw []string) []string {
	out := make([]string, 0, len(raw))
	for _, item := range raw {
		trimmed := strings.TrimSpace(item)
		if trimmed != "" {
			out = append(out, trimmed)
		}
	}
	return out
}

func resolveAllowedOrigin(requestOrigin string, allowed []string) string {
	if len(allowed) == 0 {
		if requestOrigin != "" {
			return requestOrigin
		}
		return "*"
	}
	for _, candidate := range allowed {
		if candidate == "*" {
			if requestOrigin != "" {
				return requestOrigin
			}
			return "*"
		}
		if strings.EqualFold(candidate, requestOrigin) {
			return requestOrigin
		}
	}
	return ""
}
