package main

import (
	"net/http"
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
