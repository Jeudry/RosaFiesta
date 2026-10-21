//go:build dev

// Command seed verifica e imprime los datos canónicos de desarrollo de RosaFiesta (`make seed`). Solo se compila con `-tags dev`.
package main

import (
	"fmt"
	"os"

	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
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
	fmt.Println("RosaFiesta — Datos canónicos cargados (Sección 6):")
	fmt.Println("  - Eventos: Boda Real Rosa & Dorado (RD$ 4,500), Quinceañera Valentina Crystal (RD$ 6,200), Cumpleaños de Mateo (RD$ 405), Graduación Promo 2026 (RD$ 2,140), Gala Corporativa Grupo Caribe (RD$ 8,500)")
	fmt.Println("  - Catálogo: 6 artículos principales + paquetes")
	fmt.Println("  - Finanzas: Ingresos octubre RD$ 12,450 · Por cobrar RD$ 7,724")
	fmt.Println("  - Equipo: María (admin), Pedro, Yuli, Andrés")
	return nil
}
