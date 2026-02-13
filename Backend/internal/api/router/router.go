package router

import (
	"expvar"
	"fmt"
	"net/http"
	"time"

	"Backend/internal/api/handlers"
	"Backend/internal/api/middleware"
	"Backend/internal/app"

	"github.com/go-chi/chi/v5"
	chiMiddleware "github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
	httpSwagger "github.com/swaggo/http-swagger/v2"
)

func NewRouter(app *app.Application, h *handlers.Handler, m *middleware.Middleware) http.Handler {
	docsUrl := fmt.Sprintf("%s/swagger/doc.json", app.Config.Addr)

	r := chi.NewRouter()

	r.Use(chiMiddleware.RequestID)
	r.Use(chiMiddleware.RealIP)
	r.Use(chiMiddleware.Logger)
	r.Use(chiMiddleware.Recoverer)
	r.Use(m.RequestID)
	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   []string{"*"},
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type", "X-CSRF-AccessToken"},
		ExposedHeaders:   []string{"Link"},
		AllowCredentials: true,
		MaxAge:           300,
	}))

	r.Use(m.RateLimiterMiddleware)

	r.Use(chiMiddleware.Timeout(60 * time.Second))

	r.Route("/v1", func(r chi.Router) {
		r.Get("/health", h.HealthCheckHandler)
		r.With(m.BasicAuthMiddleware()).Get("/debug/vars", expvar.Handler().ServeHTTP)

		r.Get("/swagger/*", httpSwagger.Handler(httpSwagger.URL(docsUrl)))

		r.Route("/posts", func(r chi.Router) {
			r.Use(m.AuthTokenMiddleware())
			r.Post("/", h.CreatePostHandler)

			r.Route("/{postId}", func(r chi.Router) {
				r.Use(h.PostsContextMiddleware)
				r.Get("/", h.GetPostHandler)
				r.Post("/{postId}/comments", h.CreatePostCommentHandler)
				r.Put("/", m.CheckPostOwnerShip("moderator", h.UpdatePostHandler))
				r.Delete("/", m.CheckPostOwnerShip("admin", h.DeletePostHandler))
			})
		})

		r.Route("/articles", func(r chi.Router) {
			r.Use(m.APIKeyMiddleware())
			r.Post("/", h.CreateArticleHandler)
			r.Get("/", h.GetAllArticlesHandler)

			r.Route("/{articleId}", func(r chi.Router) {
				r.Use(h.ArticlesContextMiddleware)
				r.Use(m.RoleMiddleware("moderator"))
				r.Get("/", h.GetArticleHandler)
				r.Put("/", h.UpdateArticleHandler)
				r.Delete("/", h.DeleteArticleHandler)
			})
		})

		r.Route("/categories", func(r chi.Router) {
			// Public/API Key protected endpoints
			r.Group(func(r chi.Router) {
				r.Use(m.APIKeyMiddleware())
				r.Get("/", h.GetAllCategoriesHandler)
				r.With(h.CategoriesContextMiddleware).Get("/{categoryId}/articles", h.GetArticlesByCategoryHandler)
				r.With(h.CategoriesContextMiddleware).Get("/{categoryId}", h.GetCategoryHandler)
			})

			// Protected/Admin endpoints (JWT)
			r.Group(func(r chi.Router) {
				r.Use(m.AuthTokenMiddleware())
				r.Post("/", m.CheckRole("moderator", h.CreateCategoryHandler))

				r.With(h.CategoriesContextMiddleware, m.RoleMiddleware("moderator")).Put("/{categoryId}", h.UpdateCategoryHandler)
				r.With(h.CategoriesContextMiddleware, m.RoleMiddleware("moderator")).Delete("/{categoryId}", h.DeleteCategoryHandler)
			})
		})

		r.Route("/users", func(r chi.Router) {
			r.Put("/active/{token}", h.ActivateUserHandler)

			r.Route("/{userId}", func(r chi.Router) {
				r.Use(m.AuthTokenMiddleware())
				r.Get("/", h.GetUserHandler)
			})

			r.Group(func(r chi.Router) {
				r.Use(m.AuthTokenMiddleware())
				r.Get("/feed", h.GetUserFeedHandler)
			})
		})

		r.Route("/authentication", func(r chi.Router) {
			r.Post("/register", h.RegisterUserHandler)
			r.Post("/token", h.CreateTokenHandler)
			r.Post("/refresh", h.RefreshTokenHandler)
		})
	})

	return r
}
