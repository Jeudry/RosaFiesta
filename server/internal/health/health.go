// Package health expone el estado de salud del servicio.
package health

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"
)

// Module registra la ruta `/healthz` en la API.
type Module struct{}

// Output es la respuesta de `/healthz`.
type Output struct {
	Body struct {
		Status  string `json:"status" doc:"Estado del servicio."`
		Product string `json:"product" doc:"Nombre del sistema."`
	}
}

// Register agrega `/healthz` a api.
func (Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "get-health",
		Method:      http.MethodGet,
		Path:        "/healthz",
		Summary:     "Verifica que el servidor esté vivo",
		Tags:        []string{"Health"},
	}, func(_ context.Context, _ *struct{}) (*Output, error) {
		out := &Output{}
		out.Body.Status = "ok"
		out.Body.Product = "RosaFiesta"
		return out, nil
	})
}
