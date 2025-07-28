package main

import (
	"Backend/docs"
	"context"
	"errors"
	"expvar"
	"fmt"
	"github.com/go-chi/cors"
	"os"
	"os/signal"
	"syscall"

	"net/http"
	"time"

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

		r.Route("/products", func(r chi.Router) {
			r.Use(app.AuthTokenMiddleware())
			r.Post("/", app.CheckRole("moderator", app.createProductHandler))
			r.Get("/", app.getAllProductsHandler)

			r.Route("/{productId}", func(r chi.Router) {
				r.Use(app.productsContextMiddleware)
				r.Use(app.RoleMiddleware("moderator"))
				r.Get("/", app.getProductHandler)
				r.Put("/", app.updateProductHandler)
				r.Delete("/", app.deleteProductHandler)
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
