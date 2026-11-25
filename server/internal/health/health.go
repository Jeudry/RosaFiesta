package health

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

type ReadinessChecker interface {
	Ping(ctx context.Context) error
}

// NamedCheck representa una verificación nominada de disponibilidad para /readyz.
type NamedCheck struct {
	Name  string
	Check func(context.Context) error
}

type Module struct {
	Readiness ReadinessChecker
	Checks    []NamedCheck
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
		Summary:     "Check service, database, and storage readiness",
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
	if err := m.runChecks(ctx); err != nil {
		return nil, err
	}
	out := &Output{}
	out.Body.Status = "ready"
	out.Body.Product = "RosaFiesta"
	return out, nil
}

func (m Module) runChecks(ctx context.Context) error {
	if m.Readiness != nil {
		if err := m.Readiness.Ping(ctx); err != nil {
			return readinessError("database", err)
		}
	}
	for _, entry := range m.Checks {
		if entry.Check == nil {
			continue
		}
		if err := entry.Check(ctx); err != nil {
			return readinessError(entry.Name, err)
		}
	}
	return nil
}

func readinessError(dependency string, err error) error {
	return platformhttp.NewAPIError(
		http.StatusServiceUnavailable,
		"SERVICE_NOT_READY",
		dependency+" readiness check failed",
		"GET /readyz",
		"DependencyReadinessRequired",
		redact.Text(err.Error()),
	)
}
