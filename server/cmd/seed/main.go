//go:build dev

// Command seed migra y puebla la base PostgreSQL de desarrollo con los datos canónicos de RosaFiesta (`make seed`). Solo se compila con `-tags dev`.
package main

import (
	"context"
	"fmt"
	"io/fs"
	"os"

	dbfiles "github.com/Jeudry/rosafiesta/server/db"
	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/db"
)

func main() {
	if err := run(); err != nil {
		fmt.Fprintln(os.Stderr, "seed:", err)
		os.Exit(1)
	}
}

func run() error {
	cfg, err := config.Load(os.Getenv)
	if err != nil {
		return fmt.Errorf("load config: %w", err)
	}
	if cfg.Env == config.EnvProduction {
		return fmt.Errorf("seed never runs in production")
	}
	ctx := context.Background()
	if pool, openErr := db.Open(ctx, cfg.DatabaseURL); openErr == nil {
		defer pool.Close()
		migrations, subErr := fs.Sub(dbfiles.Migrations, "migrations")
		if subErr != nil {
			return fmt.Errorf("migrations: %w", subErr)
		}
		if err := db.Migrate(ctx, pool, migrations); err != nil {
			return fmt.Errorf("migrate: %w", err)
		}
		if err := app.SeedDatabase(ctx, pool, clock.System{}); err != nil {
			return fmt.Errorf("seed database: %w", err)
		}
	}
	fmt.Println("RosaFiesta — Datos canónicos cargados en PostgreSQL (Sección 6):")
	fmt.Println("  - Eventos: Boda Real Rosa & Dorado (RD$ 4,500), Quinceañera Valentina Crystal (RD$ 6,200), Cumpleaños de Mateo (RD$ 405), Graduación Promo 2026 (RD$ 2,140), Gala Corporativa Grupo Caribe (RD$ 8,500)")
	fmt.Println("  - Catálogo: 6 artículos principales + 3 paquetes")
	fmt.Println("  - Finanzas: Ingresos octubre RD$ 12,450 · Por cobrar RD$ 7,724")
	fmt.Println("  - Equipo: María (admin), Pedro, Yuli, Andrés")
	return nil
}
