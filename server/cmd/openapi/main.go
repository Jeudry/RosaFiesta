// Command openapi escribe la especificación OpenAPI en un archivo (por defecto ../api/openapi.json).
package main

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/Jeudry/rosafiesta/server/internal/app"
)

func main() {
	if err := run(); err != nil {
		fmt.Fprintln(os.Stderr, "openapi:", err)
		os.Exit(1)
	}
}

func run() error {
	path := filepath.Join("..", "api", "openapi.json")
	if len(os.Args) > 1 {
		path = os.Args[1]
	}

	spec, err := app.OpenAPISpec()
	if err != nil {
		return err
	}
	if err := os.MkdirAll(filepath.Dir(path), 0o750); err != nil { //nolint:gosec // herramienta de desarrollo local
		return fmt.Errorf("create output directory: %w", err)
	}
	if err := os.WriteFile(path, spec, 0o600); err != nil { //nolint:gosec // herramienta de desarrollo local
		return fmt.Errorf("write %s: %w", path, err)
	}
	return nil
}
