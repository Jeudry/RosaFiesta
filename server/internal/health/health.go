package health

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type ReadinessChecker interface {
	Ping(ctx context.Context) error
}

type Module struct {
	Readiness ReadinessChecker
}

type Output struct {
	Body struct {
		Status  string `json:"status" doc:"Service status."`
		Product string `json:"product" doc:"System name."`
	}
}

func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "get-health",
		Method:      http.MethodGet,
		Path:        "/healthz",
		Summary:     "Check service liveness",
		Tags:        []string{"Health"},
	}, m.handleLiveness)

	huma.Register(api, huma.Operation{
		OperationID: "get-readiness",
		Method:      http.MethodGet,
		Path:        "/readyz",
		Summary:     "Check service and database readiness",
		Tags:        []string{"Health"},
	}, m.handleReadiness)
}

func (m Module) handleLiveness(_ context.Context, _ *struct{}) (*Output, error) {
	out := &Output{}
	out.Body.Status = "ok"
	out.Body.Product = "RosaFiesta"
	return out, nil
}

func (m Module) handleReadiness(ctx context.Context, _ *struct{}) (*Output, error) {
	if m.Readiness != nil {
		if err := m.Readiness.Ping(ctx); err != nil {
			msg := err.Error()
			return nil, platformhttp.NewAPIError(
				http.StatusServiceUnavailable,
				"SERVICE_NOT_READY",
				"database readiness check failed",
				"GET /readyz",
				"DatabasePingRequired",
				msg,
			)
		}
	}
	out := &Output{}
	out.Body.Status = "ready"
	out.Body.Product = "RosaFiesta"
	return out, nil
}
