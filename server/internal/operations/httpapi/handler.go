// Package httpapi expone los endpoints HTTP de logística, checklist, rutas, daños y sincronización offline.
package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
)

// Module conecta el servicio de operaciones al enrutador Huma.
type Module struct {
	Service *app.Service
}

// EventLogisticsInput recibe el ID del evento para consultar su logística.
type EventLogisticsInput struct {
	EventID string `path:"eventId" doc:"ID del evento"`
}

// EventLogisticsOutput devuelve la logística y el avance del checklist.
type EventLogisticsOutput struct {
	Body struct {
		Logistics         domain.EventLogistics `json:"logistics"`
		CompletionPercent int                   `json:"completionPercent"`
	}
}

// ToggleChecklistInput recibe el cambio de estado de un paso del checklist (s68, A11).
type ToggleChecklistInput struct {
	EventID string `path:"eventId" doc:"ID del evento"`
	ItemID  string `path:"itemId" doc:"ID del paso del checklist"`
	Body    struct {
		Completed bool   `json:"completed"`
		StaffName string `json:"staffName" minLength:"2"`
	}
}

// RouteStopsOutput devuelve las paradas de la hoja de ruta del día (A13).
type RouteStopsOutput struct {
	Body struct {
		Stops []domain.RouteStop `json:"stops"`
	}
}

// ReportDamageInput recibe un reporte de daño o faltante post-evento (A14).
type ReportDamageInput struct {
	EventID string `path:"eventId" doc:"ID del evento"`
	Body    struct {
		ArticleCode   string `json:"articleCode" minLength:"2"`
		ArticleName   string `json:"articleName" minLength:"2"`
		DamagedUnits  int    `json:"damagedUnits" minimum:"0"`
		MissingUnits  int    `json:"missingUnits" minimum:"0"`
		ReplacementRD int    `json:"replacementRD" minimum:"0"`
		Notes         string `json:"notes"`
	}
}

// ReportDamageOutput envuelve el reporte de daño registrado.
type ReportDamageOutput struct {
	Body struct {
		Report domain.DamageReport `json:"report"`
	}
}

// OfflineQueueOutput devuelve la cola de mutaciones offline (A37).
type OfflineQueueOutput struct {
	Body struct {
		Queue []domain.OfflineMutation `json:"queue"`
	}
}

// Register registra las rutas HTTP del módulo de operaciones.
func (m Module) Register(api huma.API) {
	m.registerLogisticsRoutes(api)
	m.registerRouteAndOfflineHandlers(api)
}

func (m Module) registerLogisticsRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "operations-get-event-logistics",
		Method:      http.MethodGet,
		Path:        "/v1/operations/events/{eventId}",
		Summary:     "Obtener logística, equipo y checklist de montaje de un evento (s66, s68, A11)",
		Tags:        []string{"Operations"},
	}, func(_ context.Context, in *EventLogisticsInput) (*EventLogisticsOutput, error) {
		l, err := m.Service.GetEventLogistics(in.EventID)
		if err != nil {
			return nil, huma.Error404NotFound(err.Error())
		}
		out := &EventLogisticsOutput{}
		out.Body.Logistics = l
		out.Body.CompletionPercent = l.CompletionPercent()
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "operations-toggle-checklist-item",
		Method:      http.MethodPatch,
		Path:        "/v1/operations/events/{eventId}/checklist/{itemId}",
		Summary:     "Marcar o desmarcar un paso del checklist de montaje (s68, A11)",
		Tags:        []string{"Operations"},
	}, func(_ context.Context, in *ToggleChecklistInput) (*EventLogisticsOutput, error) {
		l, err := m.Service.ToggleChecklistItem(in.EventID, in.ItemID, in.Body.Completed, in.Body.StaffName)
		if err != nil {
			return nil, huma.Error404NotFound(err.Error())
		}
		out := &EventLogisticsOutput{}
		out.Body.Logistics = l
		out.Body.CompletionPercent = l.CompletionPercent()
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "operations-report-damage",
		Method:      http.MethodPost,
		Path:        "/v1/operations/events/{eventId}/damages",
		Summary:     "Registrar incidencia de daño o faltante post-evento (A14)",
		Tags:        []string{"Operations"},
	}, func(_ context.Context, in *ReportDamageInput) (*ReportDamageOutput, error) {
		r, err := m.Service.ReportDamage(
			in.EventID,
			in.Body.ArticleCode,
			in.Body.ArticleName,
			in.Body.DamagedUnits,
			in.Body.MissingUnits,
			in.Body.ReplacementRD,
			in.Body.Notes,
		)
		if err != nil {
			return nil, huma.Error400BadRequest(err.Error())
		}
		out := &ReportDamageOutput{}
		out.Body.Report = r
		return out, nil
	})
}

func (m Module) registerRouteAndOfflineHandlers(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "operations-list-route-stops",
		Method:      http.MethodGet,
		Path:        "/v1/operations/routes/today",
		Summary:     "Listar paradas de la ruta de entrega del día (A13)",
		Tags:        []string{"Operations"},
	}, func(_ context.Context, _ *struct{}) (*RouteStopsOutput, error) {
		out := &RouteStopsOutput{}
		out.Body.Stops = m.Service.ListRouteStops()
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-offline-queue",
		Method:      http.MethodGet,
		Path:        "/v1/operations/offline-queue",
		Summary:     "Listar cambios pendientes de sincronizar en modo sin conexión (A37)",
		Tags:        []string{"Operations"},
	}, func(_ context.Context, _ *struct{}) (*OfflineQueueOutput, error) {
		out := &OfflineQueueOutput{}
		out.Body.Queue = m.Service.ListOfflineQueue()
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "operations-sync-offline-queue",
		Method:      http.MethodPost,
		Path:        "/v1/operations/offline-queue/sync",
		Summary:     "Sincronizar todos los cambios pendientes en cola offline (A37)",
		Tags:        []string{"Operations"},
	}, func(_ context.Context, _ *struct{}) (*OfflineQueueOutput, error) {
		out := &OfflineQueueOutput{}
		out.Body.Queue = m.Service.SyncOfflineQueue()
		return out, nil
	})
}
