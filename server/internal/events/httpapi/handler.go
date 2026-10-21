// Package httpapi expone los endpoints HTTP de eventos y plantillas.
package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/events/app"
	"github.com/Jeudry/rosafiesta/server/internal/events/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// Module conecta el servicio de eventos al enrutador Huma.
type Module struct {
	Service *app.Service
}

// TemplatesOutput envuelve la lista de plantillas de eventos.
type TemplatesOutput struct {
	Body struct {
		Templates []domain.EventTemplate `json:"templates"`
	}
}

// EventsListInput filtra el listado de eventos.
type EventsListInput struct {
	Status string `query:"status" doc:"Estado opcional (confirmed, pending, completed, cancelled)"`
}

// EventsListOutput devuelve el listado de eventos.
type EventsListOutput struct {
	Body struct {
		Events []domain.Event `json:"events"`
	}
}

// EventByIDInput recibe el ID del evento.
type EventByIDInput struct {
	ID string `path:"id" doc:"ID del evento"`
}

// EventDetailOutput devuelve un evento individual y su saldo calculado.
type EventDetailOutput struct {
	Body struct {
		Event     domain.Event `json:"event"`
		BalanceRD int          `json:"balanceRD"`
	}
}

// DateChangeInput recibe la solicitud de cambio de fecha (s36).
type DateChangeInput struct {
	ID   string `path:"id" doc:"ID del evento"`
	Body struct {
		NewDateLabel      string `json:"newDateLabel" doc:"Nueva fecha legible (ej. Sáb 31 oct)"`
		NewDaysUntilEvent int    `json:"newDaysUntilEvent" minimum:"2"`
	}
}

// DateChangeOutput devuelve el evento actualizado y la evaluación del cambio.
type DateChangeOutput struct {
	Body struct {
		Event      domain.Event                `json:"event"`
		Evaluation domain.DateChangeEvaluation `json:"evaluation"`
	}
}

// CancelEventOutput devuelve el evento cancelado y el cálculo de reembolso (s37).
type CancelEventOutput struct {
	Body struct {
		Event  domain.Event              `json:"event"`
		Refund domain.RefundPolicyResult `json:"refund"`
	}
}

// Register registra las rutas HTTP del módulo de eventos.
func (m Module) Register(api huma.API) {
	m.registerReadRoutes(api)
	m.registerMutationRoutes(api)
}

func (m Module) registerReadRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "events-list-templates",
		Method:      http.MethodGet,
		Path:        "/v1/events/templates",
		Summary:     "Listar plantillas de tipos de evento (Bodas, Quinceañeras, Cumpleaños, etc.)",
		Tags:        []string{"Events"},
	}, func(_ context.Context, _ *struct{}) (*TemplatesOutput, error) {
		out := &TemplatesOutput{}
		out.Body.Templates = m.Service.ListTemplates()
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "events-list",
		Method:      http.MethodGet,
		Path:        "/v1/events",
		Summary:     "Listar eventos de clientes y operación",
		Tags:        []string{"Events"},
	}, func(_ context.Context, in *EventsListInput) (*EventsListOutput, error) {
		out := &EventsListOutput{}
		out.Body.Events = m.Service.ListEvents(in.Status)
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "events-get-by-id",
		Method:      http.MethodGet,
		Path:        "/v1/events/{id}",
		Summary:     "Obtener detalle de un evento y saldo pendiente",
		Tags:        []string{"Events"},
	}, func(_ context.Context, in *EventByIDInput) (*EventDetailOutput, error) {
		evt, err := m.Service.GetEvent(in.ID)
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusNotFound,
				"EVENT_NOT_FOUND",
				err.Error(),
				"GET /v1/events/"+in.ID,
				"EventMustExist",
				err.Error(),
			)
		}
		out := &EventDetailOutput{}
		out.Body.Event = evt
		out.Body.BalanceRD = evt.BalanceRD()
		return out, nil
	})
}

func (m Module) registerMutationRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "events-request-date-change",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/date-change",
		Summary:     "Solicitar cambio de fecha (primer cambio gratis)",
		Tags:        []string{"Events"},
	}, func(_ context.Context, in *DateChangeInput) (*DateChangeOutput, error) {
		evt, eval, err := m.Service.RequestDateChange(in.ID, in.Body.NewDateLabel, in.Body.NewDaysUntilEvent)
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusUnprocessableEntity,
				"EVENT_DATE_CHANGE_REJECTED",
				err.Error(),
				"POST /v1/events/"+in.ID+"/date-change",
				"DateChangePolicy",
				err.Error(),
			)
		}
		out := &DateChangeOutput{}
		out.Body.Event = evt
		out.Body.Evaluation = eval
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "events-cancel",
		Method:      http.MethodPost,
		Path:        "/v1/events/{id}/cancel",
		Summary:     "Cancelar evento aplicando política de reembolso 90%/50%/0%",
		Tags:        []string{"Events"},
	}, func(_ context.Context, in *EventByIDInput) (*CancelEventOutput, error) {
		evt, refund, err := m.Service.CancelEvent(in.ID)
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusUnprocessableEntity,
				"EVENT_CANCEL_REJECTED",
				err.Error(),
				"POST /v1/events/"+in.ID+"/cancel",
				"RefundCancellationPolicy",
				err.Error(),
			)
		}
		out := &CancelEventOutput{}
		out.Body.Event = evt
		out.Body.Refund = refund
		return out, nil
	})
}
