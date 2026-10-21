// Command api arranca el servidor HTTP de RosaFiesta.
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

// migrationFiles devuelve las migraciones empaquetadas en el binario.
func migrationFiles() fs.FS {
	sub, err := fs.Sub(dbfiles.Migrations, "migrations")
	if err != nil {
		panic(err)
	}
	return sub
}

func main() {
	if len(os.Args) > 1 && os.Args[1] == "healthcheck" {
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

	if pool, openErr := db.Open(ctx, cfg.DatabaseURL); openErr == nil {
		defer pool.Close()
		if migErr := db.Migrate(ctx, pool, migrationFiles()); migErr != nil {
			logger.Warn("database migration skipped", slog.String("err", migErr.Error()))
		}
	}

	deps := app.Dependencies{Clock: clock.System{}}
	handler, _ := httpapi.New(logger, app.Modules(deps)...)
	server := &http.Server{
		Addr:              fmt.Sprintf(":%d", cfg.Port),
		Handler:           handler,
		ReadHeaderTimeout: readHeaderTimeout,
	}

	serverErr := make(chan error, 1)
	go func() {
		logger.Info("server listening", slog.String("addr", server.Addr), slog.String("env", string(cfg.Env)))
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
