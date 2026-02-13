package handlers

import (
	"net/http"

	"Backend/internal/utils"
)

// @Summary		Health Check
// @Description	Returns the health status of the Application
// @Tags			health
// @Accept			json
// @Produce		json
// @Success		200	{object}	map[string]string	"Health status, environment, and version"
// @Failure		500	{object}	error				"Internal server error"
// @Router			/health [get]
func (h *Handler) HealthCheckHandler(w http.ResponseWriter, r *http.Request) {
	data := map[string]string{
		"status":  "Ok",
		"env":     h.app.Config.Env,
		"version": "1.1.0",
	}

	if err := utils.JSONResponse(w, http.StatusOK, data); err != nil {
		utils.WriteJSONError(w, http.StatusInternalServerError, err.Error())
	}
}
