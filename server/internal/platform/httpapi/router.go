// Package httpapi construye el enrutador HTTP y la descripción OpenAPI.
//
// Lo comparten el servidor real y el generador de la especificación, así el contrato publicado siempre sale del mismo
// código que atiende las peticiones.
package httpapi

import (
	"log/slog"
	"net/http"
	"time"

	"github.com/danielgtaylor/huma/v2"
	"github.com/danielgtaylor/huma/v2/adapters/humachi"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
)

const (
	apiTitle   = "RosaFiesta API"
	apiVersion = "1.0.0"
)

// Module es un módulo del monolito que publica rutas en la API.
type Module interface {
	// Register agrega las rutas del módulo a api.
	Register(api huma.API)
}

// New arma el enrutador con sus middlewares y registra los módulos.
func New(logger *slog.Logger, modules ...Module) (http.Handler, huma.API) {
	router := chi.NewRouter()
	router.Use(corsMiddleware, middleware.RequestID, requestLogger(logger), middleware.Recoverer)

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

// requestLogger registra cada petición con su método, ruta, estado, duración e identificador.
func requestLogger(logger *slog.Logger) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			start := time.Now()
			wrapped := middleware.NewWrapResponseWriter(w, r.ProtoMajor)

			next.ServeHTTP(wrapped, r)

			logger.InfoContext(r.Context(), "http request",
				slog.String("method", r.Method),
				slog.String("path", r.URL.Path),
				slog.Int("status", wrapped.Status()),
				slog.Duration("duration", time.Since(start)),
				slog.String("request_id", middleware.GetReqID(r.Context())),
			)
		})
	}
}

// corsMiddleware habilita Cross-Origin Resource Sharing para clientes web y preflight OPTIONS.
func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
		w.Header().Set("Access-Control-Allow-Headers", "Accept, Authorization, Content-Type, Idempotency-Key, X-Request-ID")
		w.Header().Set("Access-Control-Expose-Headers", "Link")
		w.Header().Set("Access-Control-Max-Age", "300")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}

		next.ServeHTTP(w, r)
	})
}
