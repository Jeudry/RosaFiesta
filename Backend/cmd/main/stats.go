package main

import (
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// getStatsHandler godoc
//
//	@Summary		Get administrative stats
//	@Description	Get summary metrics for the admin dashboard
//	@Tags			admin
//	@Produce		json
//	@Success		200	{object}	models.AdminStats
//	@Failure		500	{object}	error
//	@Router			/admin/stats [get]
func (app *Application) getStatsHandler(w http.ResponseWriter, r *http.Request) {
	stats, err := app.Store.Stats.GetSummary(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, stats); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getEventAuditLogHandler godoc
//
//	@Summary		Get audit log for an event
//	@Description	Get the audit trail for a specific event
//	@Tags			admin
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{object}	[]models.AuditLogWithUser
//	@Failure		400	{object}	error
//	@Failure		500	{object}	error
//	@Router			/admin/events/{id}/audit [get]
func (app *Application) getEventAuditLogHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	logs, err := app.Store.AuditLogs.GetByEventID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, logs); err != nil {
		app.internalServerError(w, r, err)
	}
}
