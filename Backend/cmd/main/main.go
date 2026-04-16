package main

import (
	"context"
	"expvar"
	"fmt"
	"runtime"
	"time"

	"Backend/cmd/main/configModels"
	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/db"
	"Backend/internal/env"
	"Backend/internal/mailer"
	"Backend/internal/notifications"
	"Backend/internal/ratelimiter"
	"Backend/internal/store"
	"Backend/internal/whatsapp"
	"Backend/internal/worker"

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

	cfg := configModels.Config{
		Addr:        env.GetString("ADDR", ":3000"),
		ApiURL:      env.GetString("EXTERNAL_URL", "localhost:3000"),
		FrontendURL: env.GetString("FRONTEND_URL", "http://127.0.0.1:8081/#"),
		Cors: configModels.CorsConfig{
			AllowedOrigins: env.GetString("CORS_ALLOWED_ORIGINS", "http://127.0.0.1:8081"),
		},
		Db: configModels.DbConfig{
			Addr:         env.GetString("DB_ADDR", "postgres://admin:adminpassword@db.backend.orb.local/rosafiesta?sslmode=disable"),
			MaxOpenConns: env.GetInt("DB_MAX_OPEN_CONNS", 10),
			MaxIdleConns: env.GetInt("DB_MAX_IDLE_CONNS", 10),
			MaxIdleTime:  env.GetString("DB_MAX_IDLE_TIME", "15m"),
		},
		Env: env.GetString("ENV", "dev"),
		Mail: configModels.MailConfig{
			Exp:       time.Hour * 24 * 3,
			FromEmail: env.GetString("MAIL_FROM_EMAIL", "jeudrypp@gmail.com"),
			Password:  env.GetString("MAIL_PASSWORD", "mavk uwbo nomv zgjh"),
			SendGrid: configModels.SendGridConfig{
				FromEmail: env.GetString("SENDGRID_FROM_EMAIL", ""),
				ApiKey:    env.GetString("SENDGRID_API_KEY", ""),
			},
			MailTrap: configModels.MailTrapConfig{
				ApiKey:    env.GetString("MAILTRAP_API_KEY", "a58ed14dbed2d6110f3705cfa0dc7ccd"),
				FromEmail: env.GetString("MAILTRAP_FROM_EMAIL", "jeudrypp@gmail.com"),
			},
		},
		Auth: configModels.AuthConfig{
			Basic: configModels.AuthBasicConfig{
				User: env.GetString("BASIC_AUTH_USER", "admin"),
				Pass: env.GetString("BASIC_AUTH_PASS", "admin"),
			},
			Token: configModels.TokenConfig{
				Secret: env.GetString("JWT_TOKEN_SECRET", "example"),
				Exp:    time.Hour * 24 * 7,
				Aud:    env.GetString("JWT_TOKEN_AUD", "gophersocial"),
				Iss:    env.GetString("JWT_TOKEN_ISS", "gophersocial"),
			},
			ApiKey: configModels.ApiKeyConfig{
				Header: env.GetString("API_KEY_HEADER", "X-Api-Key"),
				Value:  env.GetString("API_KEY_VALUE", "your-default-key"),
			},
		},
		Redis: configModels.RedisConfig{
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
		R2: configModels.R2Config{
			AccountID: env.GetString("R2_ACCOUNT_ID", ""),
			AccessKey: env.GetString("R2_ACCESS_KEY", ""),
			SecretKey: env.GetString("R2_SECRET_KEY", ""),
			Bucket:    env.GetString("R2_BUCKET", "rosafiesta"),
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

	/*mailTrap, err := mailer.NewMailTrapClient(cfg.Mail.MailTrap.ApiKey, cfg.Mail.MailTrap.FromEmail)
	if err != nil {
		logger.Fatal(err)
	}*/

	mailGo, err := mailer.NewGoMailClient(cfg.Mail.Password, cfg.Mail.FromEmail)

	jwtAuthenticator := auth.NewJWTAuthenticator(cfg.Auth.Token.Secret, cfg.Auth.Token.Aud, cfg.Auth.Token.Iss)
	notificationService, _ := notifications.NewNotificationService()

	chatHub := newHub()
	go chatHub.run()

	delayChecker := worker.NewDelayChecker(appStore, logger, notificationService)
	go delayChecker.Start(context.Background())

	emailSender := worker.NewEmailSender(appStore, logger, mailGo)
	go emailSender.Start(context.Background(), 30*time.Minute)

	if notificationService != nil {
		notificationSender := worker.NewNotificationSender(appStore, logger, notificationService)
		go notificationSender.Start(context.Background(), 30*time.Minute)
	}

	var r2Client *store.R2Client
	if cfg.R2.AccountID != "" && cfg.R2.AccessKey != "" {
		r2Client, err = store.NewR2Client(store.R2Config{
			AccountID: cfg.R2.AccountID,
			AccessKey: cfg.R2.AccessKey,
			SecretKey: cfg.R2.SecretKey,
			Bucket:    cfg.R2.Bucket,
		})
		if err != nil {
			logger.Warnf("failed to initialize R2 client: %v", err)
		} else {
			logger.Info("R2 client initialized")
		}
	}

	var whatsappClient *whatsapp.Client
	if cfg.WhatsApp.AccessToken != "" && cfg.WhatsApp.PhoneNumberID != "" {
		whatsappClient = whatsapp.NewClient(whatsapp.Config{
			PhoneNumberID: cfg.WhatsApp.PhoneNumberID,
			AccessToken:   cfg.WhatsApp.AccessToken,
			FromName:     cfg.WhatsApp.FromName,
		})
		logger.Info("WhatsApp client initialized")
	}

	app := &Application{
		Config:        cfg,
		Store:         appStore,
		Logger:        logger,
		Mailer:        mailGo,
		Auth:          jwtAuthenticator,
		CacheStorage:  cacheStorage,
		RateLimiter:   rateLimiter,
		Notifications: notificationService,
		ChatHub:       chatHub,
		R2:            r2Client,
		WhatsApp:      whatsappClient,
		Redis:         rdb,
	}

	expvar.NewString("version").Set(Version)
	expvar.Publish("database", expvar.Func(func() interface{} {
		return db.Stats()
	}))
	expvar.Publish("goroutines", expvar.Func(func() interface{} {
		return runtime.NumGoroutine()
	}))

	mux := app.Mount()

	logger.Fatal(app.run(mux))
}
