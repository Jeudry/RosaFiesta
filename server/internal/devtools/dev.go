//go:build dev

// Package devtools expone endpoints exclusivos de desarrollo (compilados solo con -tags dev).
package devtools

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"
)

// Module registra las rutas /__dev/* cuando se compila con -tags dev.
type Module struct {
	OnReset func()
}

// SeedOutput resume el resultado de poblar la base de datos con los datos canónicos.
type SeedOutput struct {
	Body struct {
		Status   string `json:"status" example:"seeded"`
		Articles int    `json:"articles" example:"6"`
		Events   int    `json:"events" example:"5"`
		Quotes   int    `json:"quotes" example:"4"`
		Staff    int    `json:"staff" example:"4"`
	}
}

// Register añade las rutas /__dev/* al router Huma cuando se compila con -tags dev.
func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "dev-seed",
		Method:      http.MethodPost,
		Path:        "/__dev/seed",
		Summary:     "Restaurar los datos canónicos de RosaFiesta (Sección 6)",
		Tags:        []string{"DevTools"},
	}, func(_ context.Context, _ *struct{}) (*SeedOutput, error) {
		if m.OnReset != nil {
			m.OnReset()
		}
		out := &SeedOutput{}
		out.Body.Status = "seeded"
		out.Body.Articles = 6
		out.Body.Events = 5
		out.Body.Quotes = 4
		out.Body.Staff = 4
		return out, nil
	})
}
