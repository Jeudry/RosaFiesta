package main

import (
	"context"
	"errors"
	"fmt"
	"io/fs"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	dbfiles "github.com/Jeudry/rosafiesta/server/db"
	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/db"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

const (
	readHeaderTimeout = 5 * time.Second
	shutdownTimeout   = 10 * time.Second
)

func migrationFiles() fs.FS {
	sub, err := fs.Sub(dbfiles.Migrations, "migrations")
	if err != nil {
		panic(err)
	}
	return sub
}

func isHealthcheckInvocation(args []string) bool {
	if len(args) <= 1 {
		return false
	}
	return args[1] == "healthcheck"
}

func main() {
	if isHealthcheckInvocation(os.Args) {
		if err := healthcheck(os.Getenv); err != nil {
			fmt.Fprintln(os.Stderr, "healthcheck:", err)
			os.Exit(1)
		}
		return
	}
	if err := run(); err != nil {
		fmt.Fprintln(os.Stderr, "api:", err)
		os.Exit(1)
	}
}

func run() error {
	cfg, err := config.Load(os.Getenv)
	if err != nil {
		return fmt.Errorf("load config: %w", err)
	}

	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	pool, dbErr := openAndMigrateDatabase(ctx, cfg, logger)
	if dbErr != nil {
		return dbErr
	}
	if pool != nil {
		defer pool.Close()
	}

	deps := app.Dependencies{
		Context:       ctx,
		Pool:          pool,
		Clock:         clock.System{},
		Config:        cfg,
		AdminOnlySeed: cfg.SeedMode != config.SeedModeFull,
	}
	routerOpts := httpapi.RouterOptions{
		AllowedOrigins: cfg.CORSAllowedOrigins,
		DisableDocs:    cfg.Env == config.EnvProduction,
	}
	modules := app.Modules(deps)
	handler, _ := httpapi.NewWithOptions(logger, routerOpts, modules...)
	server := &http.Server{
		Addr:              fmt.Sprintf(":%d", cfg.Port),
		Handler:           handler,
		ReadHeaderTimeout: readHeaderTimeout,
		IdleTimeout:       60 * time.Second,
	}
	return serveWithGracefulShutdown(ctx, server, logger, cfg.Env)
}

func openAndMigrateDatabase(
	ctx context.Context,
	cfg config.Config,
	logger *slog.Logger,
) (*pgxpool.Pool, error) {
	pool, openErr := db.Open(ctx, cfg.DatabaseURL)
	if openErr != nil {
		if cfg.Env == config.EnvProduction {
			return nil, fmt.Errorf("open production database: %w", openErr)
		}
		logger.Warn("database connection unavailable; using in-memory dev store", slog.String("err", openErr.Error()))
		return nil, nil
	}
	if migErr := db.Migrate(ctx, pool, migrationFiles()); migErr != nil {
		pool.Close()
		if cfg.Env == config.EnvProduction {
			return nil, fmt.Errorf("migrate production database: %w", migErr)
		}
		logger.Warn("database migration skipped", slog.String("err", migErr.Error()))
		return nil, nil
	}
	return pool, nil
}

func serveWithGracefulShutdown(
	ctx context.Context,
	server *http.Server,
	logger *slog.Logger,
	env config.Environment,
) error {
	serverErr := make(chan error, 1)
	go func() {
		logger.Info("server listening", slog.String("addr", server.Addr), slog.String("env", string(env)))
		serverErr <- server.ListenAndServe()
	}()

	select {
	case err := <-serverErr:
		if !errors.Is(err, http.ErrServerClosed) {
			return fmt.Errorf("serve: %w", err)
		}
		return nil
	case <-ctx.Done():
		logger.Info("shutting down")
		shutdownCtx, cancel := context.WithTimeout(context.Background(), shutdownTimeout)
		defer cancel()
		if err := server.Shutdown(shutdownCtx); err != nil {
			return fmt.Errorf("shutdown: %w", err)
		}
		return nil
	}
}

func healthcheck(getenv func(string) string) error {
	port := getenv("PORT")
	if port == "" {
		port = "8788"
	}
	ctx, cancel := context.WithTimeout(context.Background(), readHeaderTimeout)
	defer cancel()
	request, err := http.NewRequestWithContext(ctx, http.MethodGet, "http://127.0.0.1:"+port+"/healthz", nil)
	if err != nil {
		return fmt.Errorf("build request: %w", err)
	}
	response, err := http.DefaultClient.Do(request)
	if err != nil {
		return fmt.Errorf("request: %w", err)
	}
	defer func() { _ = response.Body.Close() }()
	if response.StatusCode != http.StatusOK {
		return fmt.Errorf("unexpected status %d", response.StatusCode)
	}
	return nil
}
