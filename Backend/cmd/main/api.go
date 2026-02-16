package main

import (
	"context"
	"errors"
	"expvar"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"Backend/docs"

	"github.com/go-chi/cors"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	httpSwagger "github.com/swaggo/http-swagger/v2"
)

func (app *Application) Mount() http.Handler {
	docsUrl := fmt.Sprintf("%s/swagger/doc.json", app.Config.Addr)

	r := chi.NewRouter()

	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   []string{"*"},
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type", "X-CSRF-AccessToken"},
		ExposedHeaders:   []string{"Link"},
		AllowCredentials: true,
		MaxAge:           300,
	}))

	r.Use(app.RateLimiterMiddleware)

	r.Use(middleware.Timeout(60 * time.Second))

	r.Route("/v1", func(r chi.Router) {
		r.Get("/health", app.healthCheckHandler)
		r.With(app.BasicAuthMiddleware()).Get("/debug/vars", expvar.Handler().ServeHTTP)

		r.Get("/swagger/*", httpSwagger.Handler(httpSwagger.URL(docsUrl)))

		r.Route("/posts", func(r chi.Router) {
			r.Use(app.AuthTokenMiddleware())
			r.Post("/", app.createPostHandler)

			r.Route("/{postId}", func(r chi.Router) {
				r.Use(app.postsContextMiddleware)
				r.Get("/", app.getPostHandler)
				r.Post("/{postId}/comments", app.createPostCommentHandler)
				r.Put("/", app.CheckPostOwnerShip("moderator", app.updatePostHandler))
				r.Delete("/", app.CheckPostOwnerShip("admin", app.deletePostHandler))
			})
		})

		r.Route("/articles", func(r chi.Router) {
			r.Use(app.APIKeyMiddleware())
			r.Post("/", app.createArticleHandler)
			r.Get("/", app.getAllArticlesHandler)

			r.Route("/{articleId}", func(r chi.Router) {
				r.Use(app.articlesContextMiddleware)
				r.Use(app.RoleMiddleware("moderator"))
				r.Get("/", app.getArticleHandler)
				r.Put("/", app.updateArticleHandler)
				r.Delete("/", app.deleteArticleHandler)
			})
		})

		r.Route("/categories", func(r chi.Router) {
			// Public/API Key protected endpoints
			r.Group(func(r chi.Router) {
				r.Use(app.APIKeyMiddleware())
				r.Get("/", app.getAllCategoriesHandler)
				r.With(app.categoriesContextMiddleware).Get("/{categoryId}/articles", app.getArticlesByCategoryHandler)
				r.With(app.categoriesContextMiddleware).Get("/{categoryId}", app.getCategoryHandler)
			})

			// Protected/Admin endpoints (JWT)
			r.Group(func(r chi.Router) {
				r.Use(app.AuthTokenMiddleware())
				r.Post("/", app.CheckRole("moderator", app.createCategoryHandler))

				r.With(app.categoriesContextMiddleware, app.RoleMiddleware("moderator")).Put("/{categoryId}", app.updateCategoryHandler)
				r.With(app.categoriesContextMiddleware, app.RoleMiddleware("moderator")).Delete("/{categoryId}", app.deleteCategoryHandler)
			})
		})

		r.Route("/users", func(r chi.Router) {
			r.Put("/active/{token}", app.activateUserHandler)

			r.Route("/{userId}", func(r chi.Router) {
				r.Use(app.AuthTokenMiddleware())
				r.Get("/", app.getUserHandler)
			})

			r.Group(func(r chi.Router) {
				r.Use(app.AuthTokenMiddleware())
				r.Get("/feed", app.getUserFeedHandler)
			})
		})

		r.Route("/authentication", func(r chi.Router) {
			r.Post("/register", app.registerUserHandler)
			r.Post("/token", app.createTokenHandler)
			r.Post("/refresh", app.refreshTokenHandler)
		})

		r.Route("/cart", func(r chi.Router) {
			r.Use(app.AuthTokenMiddleware())
			r.Get("/", app.getCartHandler)
			r.Delete("/", app.clearCartHandler)

			r.Route("/items", func(r chi.Router) {
				r.Post("/", app.addItemToCartHandler)
				r.Route("/{itemId}", func(r chi.Router) {
					r.Patch("/", app.updateCartItemHandler)
					r.Delete("/", app.removeCartItemHandler)
				})
			})
		})

		r.Route("/events", func(r chi.Router) {
			r.Use(app.AuthTokenMiddleware())
			r.Post("/", app.createEventHandler)
			r.Get("/", app.getUserEventsHandler)
			r.Get("/{id}", app.getEventHandler)
			r.Put("/{id}", app.updateEventHandler)

			r.Delete("/{id}", app.deleteEventHandler)

			r.Route("/{id}/items", func(r chi.Router) {
				r.Post("/", app.addEventItemHandler)
				r.Get("/", app.getEventItemsHandler)
				r.Delete("/{itemId}", app.removeEventItemHandler)
			})

			r.Route("/{id}/guests", func(r chi.Router) {
				r.Post("/", app.addGuestHandler)
				r.Get("/", app.getGuestsHandler)
			})

			r.Route("/{id}/tasks", func(r chi.Router) {
				r.Post("/", app.addEventTaskHandler)
				r.Get("/", app.getEventTasksHandler)
			})

			r.Route("/{id}/timeline", func(r chi.Router) {
				r.Post("/", app.createTimelineItemHandler)
				r.Get("/", app.getTimelineItemsHandler)
			})

			r.Group(func(r chi.Router) {
				r.Use(app.RoleMiddleware("admin"))
				r.Patch("/{id}/adjust", app.adjustQuoteHandler)
			})
		})

		r.Route("/timeline/{itemId}", func(r chi.Router) {
			r.Use(app.AuthTokenMiddleware())
			r.Put("/", app.updateTimelineItemHandler)
			r.Delete("/", app.deleteTimelineItemHandler)
		})

		r.Route("/guests/{guestId}", func(r chi.Router) {
			r.Put("/", app.updateGuestHandler)
			r.Delete("/", app.deleteGuestHandler)
		})

		r.Route("/tasks/{taskId}", func(r chi.Router) {
			r.Put("/", app.updateEventTaskHandler)
			r.Delete("/", app.deleteEventTaskHandler)
		})

		r.Route("/suppliers", func(r chi.Router) {
			r.Use(app.AuthTokenMiddleware())
			r.Post("/", app.addSupplierHandler)
			r.Get("/", app.getSuppliersHandler)
			r.Route("/{id}", func(r chi.Router) {
				r.Get("/", app.getSupplierHandler)
				r.Patch("/", app.updateSupplierHandler)
				r.Delete("/", app.deleteSupplierHandler)
			})
		})
	})

	return r
}

func (app *Application) run(mux http.Handler) error {
	docs.SwaggerInfo.Version = Version
	docs.SwaggerInfo.Host = app.Config.ApiURL
	docs.SwaggerInfo.BasePath = "/v1"

	srv := &http.Server{
		Addr:         app.Config.Addr,
		Handler:      mux,
		WriteTimeout: time.Second * 30,
		ReadTimeout:  time.Second * 10,
		IdleTimeout:  time.Minute,
	}

	shutdown := make(chan error)

	go func() {
		quit := make(chan os.Signal, 1)

		signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)

		s := <-quit

		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)

		defer cancel()

		app.Logger.Infow("shutting down server", "signal", s.String())

		shutdown <- srv.Shutdown(ctx)
	}()

	app.Logger.Infow("server has started at", " addr", app.Config.Addr, "env", app.Config.Env)

	err := srv.ListenAndServe()
	if !errors.Is(err, http.ErrServerClosed) {
		return err
	}
	err = <-shutdown
	if err != nil {
		return err
	}

	app.Logger.Info("server has stopped", "addr", app.Config.Addr, "env", app.Config.Env)

	return srv.ListenAndServe()
}
