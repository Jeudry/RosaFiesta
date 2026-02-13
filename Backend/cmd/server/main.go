package main

import (
	"context"
	"errors"
	"expvar"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"runtime"
	"syscall"
	"time"

	"Backend/docs"
	"Backend/internal/api/handlers"
	"Backend/internal/api/middleware"
	"Backend/internal/api/router"
	"Backend/internal/app"
	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/config"
	"Backend/internal/db"
	"Backend/internal/env"
	"Backend/internal/mailer"
	"Backend/internal/ratelimiter"
	"Backend/internal/services"
	"Backend/internal/store"
	"Backend/internal/utils"

	"github.com/go-redis/redis/v8"
	"github.com/joho/godotenv"
	"go.uber.org/zap"
)

const Version = "1.1.0"

// @title						Swagger Example API
// @description				Api for RosaFiesta a decoration enterprise management system
// @termsOfService				http://swagger.io/terms/
// @contact.name				API Support
// @contact.url				http://www.swagger.io/support
// @contact.email				jeudrypp@gmail.com
// @license.name				Apache 2.0
// @license.url				http://www.apache.org/licenses/LICENSE-2.0.html
//
// @BasePath					/v1
// @securityDefinitions.apikey	ApiKeyAuth
// @in							header
// @name						Authorization
// @description				Provide your API key to access this API
//
// @securityDefinitions.apikey	StaticApiKey
// @in							header
// @name						X-Api-Key
// @description				Provide your Static API key to access this API
func main() {
	err := godotenv.Load()
	if err != nil {
		fmt.Println("Error loading .env file, continuing with environment variables")
	}

	cfg := config.Config{
		Addr:        env.GetString("ADDR", ":3000"),
		ApiURL:      env.GetString("EXTERNAL_URL", "localhost:3000"),
		FrontendURL: env.GetString("FRONTEND_URL", "localhost:4200"),
		Cors: config.CorsConfig{
			AllowedOrigins: env.GetString("CORS_ALLOWED_ORIGINS", "http://localhost:4200"),
		},
		Db: config.DbConfig{
			Addr:         env.GetString("DB_ADDR", "postgres://admin:adminpassword@db.backend.orb.local/rosafiesta?sslmode=disable"),
			MaxOpenConns: env.GetInt("DB_MAX_OPEN_CONNS", 10),
			MaxIdleConns: env.GetInt("DB_MAX_IDLE_CONNS", 10),
			MaxIdleTime:  env.GetString("DB_MAX_IDLE_TIME", "15m"),
		},
		Env: env.GetString("ENV", "dev"),
		Mail: config.MailConfig{
			Exp:       time.Hour * 24 * 3,
			FromEmail: env.GetString("MAIL_FROM_EMAIL", "jeudrypp@gmail.com"),
			Password:  env.GetString("MAIL_PASSWORD", "mavk uwbo nomv zgjh"),
			SendGrid: config.SendGridConfig{
				FromEmail: env.GetString("SENDGRID_FROM_EMAIL", ""),
				ApiKey:    env.GetString("SENDGRID_API_KEY", ""),
			},
			MailTrap: config.MailTrapConfig{
				ApiKey:    env.GetString("MAILTRAP_API_KEY", "a58ed14dbed2d6110f3705cfa0dc7ccd"),
				FromEmail: env.GetString("MAILTRAP_FROM_EMAIL", "jeudrypp@gmail.com"),
			},
		},
		Auth: config.AuthConfig{
			Basic: config.AuthBasicConfig{
				User: env.GetString("BASIC_AUTH_USER", "admin"),
				Pass: env.GetString("BASIC_AUTH_PASS", "admin"),
			},
			Token: config.TokenConfig{
				Secret: env.GetString("JWT_TOKEN_SECRET", "example"),
				Exp:    time.Hour * 24 * 7,
				Aud:    env.GetString("JWT_TOKEN_AUD", "gophersocial"),
				Iss:    env.GetString("JWT_TOKEN_ISS", "gophersocial"),
			},
			ApiKey: config.ApiKeyConfig{
				Header: env.GetString("API_KEY_HEADER", "X-Api-Key"),
				Value:  env.GetString("API_KEY_VALUE", "your-default-key"),
			},
		},
		Redis: config.RedisConfig{
			Addr:    env.GetString("REDIS_ADDR", "xd"),
			Pw:      env.GetString("REDIS_PW", ""),
			Db:      env.GetInt("REDIS_DB", 0),
			Enabled: env.GetBool("REDIS_ENABLED", false),
		},
		RateLimiter: ratelimiter.Config{
			RequestsPerTimeFrame: env.GetInt("RATE_LIMITER_REQUESTS_PER_TIME_FRAME", 100),
			TimeFrame:            time.Second * 5,
			Enabled:              env.GetBool("RATE_LIMITER_ENABLED", true),
		},
	}

	logger := zap.Must(zap.NewProduction()).Sugar()
	defer logger.Sync()

	db, err := db.New(cfg.Db.Addr, cfg.Db.MaxOpenConns, cfg.Db.MaxIdleConns, cfg.Db.MaxIdleTime)
	if err != nil {
		logger.Panic(err)
	}

	defer db.Close()
	logger.Info("connected to database")

	appStore := store.NewStorage(db)

	var rdb *redis.Client
	if cfg.Redis.Enabled {
		rdb = cache.NewRedisClient(cfg.Redis.Addr, cfg.Redis.Pw, cfg.Redis.Db)
		_, err := rdb.Ping(context.Background()).Result()
		if err != nil {
			logger.Panic("failed to connect to Redis:", err)
		}
		logger.Info("redis cache connection established")
	}

	cacheStorage := cache.NewRedisStorage(rdb)

	rateLimiter := ratelimiter.NewFixedWindowRateLimiter(cfg.RateLimiter.RequestsPerTimeFrame, cfg.RateLimiter.TimeFrame)

	mailGo, err := mailer.NewGoMailClient(cfg.Mail.Password, cfg.Mail.FromEmail)
	if err != nil {
		logger.Fatal(err)
	}

	jwtAuthenticator := auth.NewJWTAuthenticator(cfg.Auth.Token.Secret, cfg.Auth.Token.Aud, cfg.Auth.Token.Iss)

	app := &app.Application{
		Config:       cfg,
		Store:        appStore,
		Logger:       logger,
		Mailer:       mailGo,
		Auth:         jwtAuthenticator,
		CacheStorage: cacheStorage,
		RateLimiter:  rateLimiter,
	}

	authService := services.NewAuthService(appStore.Users, appStore.RefreshTokens, cfg, jwtAuthenticator, mailGo, logger)
	userService := services.NewUserService(appStore.Users, logger)
	postService := services.NewPostService(appStore.Posts, appStore.Comments, logger)
	articleService := services.NewArticleService(appStore.Articles, logger)
	categoryService := services.NewCategoryService(appStore.Categories, appStore.Articles, logger)
	feedService := services.NewFeedService(appStore.Posts, logger)

	// responder := utils.NewResponder(logger) // Created inside NewHandler now
	h := handlers.NewHandler(
		app,
		authService,
		userService,
		postService,
		articleService,
		categoryService,
		feedService,
	)
	m := middleware.NewMiddleware(app, utils.NewResponder(logger))

	expvar.NewString("version").Set(Version)
	expvar.Publish("database", expvar.Func(func() interface{} {
		return db.Stats()
	}))
	expvar.Publish("goroutines", expvar.Func(func() interface{} {
		return runtime.NumGoroutine()
	}))

	mux := router.NewRouter(app, h, m)

	logger.Fatal(run(app, mux))
}

func run(app *app.Application, mux http.Handler) error {
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

	// Wait for server context to be stopped
	err := srv.ListenAndServe()
	if !errors.Is(err, http.ErrServerClosed) {
		return err
	}
	err = <-shutdown
	if err != nil {
		return err
	}

	app.Logger.Info("server has stopped", "addr", app.Config.Addr, "env", app.Config.Env)

	return nil
}
