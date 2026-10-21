package app

import (
	"encoding/json"
	"fmt"
	"log/slog"

	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// OpenAPISpec devuelve la especificación OpenAPI de la API, en JSON con sangría y salto de línea final.
//
// Se guarda en `api/openapi.json` y el CI verifica que coincida con el código.
func OpenAPISpec() ([]byte, error) {
	_, api := httpapi.New(slog.New(slog.DiscardHandler), Modules(Dependencies{})...)
	spec, err := json.MarshalIndent(api.OpenAPI(), "", "  ")
	if err != nil {
		return nil, fmt.Errorf("marshal openapi spec: %w", err)
	}
	return append(spec, '\n'), nil
}
