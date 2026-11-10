//go:build dev

package devtools

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"
)

type Module struct {
	OnReset func(ctx context.Context)
}

type SeedOutput struct {
	Body struct {
		Status   string `json:"status" example:"seeded"`
		Articles int    `json:"articles" example:"6"`
		Events   int    `json:"events" example:"5"`
		Quotes   int    `json:"quotes" example:"4"`
		Staff    int    `json:"staff" example:"4"`
	}
}

func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "dev-seed",
		Method:      http.MethodPost,
		Path:        "/__dev/seed",
		Summary:     "Restore canonical development dataset",
		Tags:        []string{"DevTools"},
	}, m.handleReset)

	huma.Register(api, huma.Operation{
		OperationID: "dev-reset",
		Method:      http.MethodPost,
		Path:        "/__dev/reset",
		Summary:     "Reset in-memory and database development state",
		Tags:        []string{"DevTools"},
	}, m.handleReset)
}

func (m Module) handleReset(ctx context.Context, _ *struct{}) (*SeedOutput, error) {
	if m.OnReset != nil {
		m.OnReset(ctx)
	}
	out := &SeedOutput{}
	out.Body.Status = "seeded"
	out.Body.Articles = 6
	out.Body.Events = 5
	out.Body.Quotes = 4
	out.Body.Staff = 4
	return out, nil
}
