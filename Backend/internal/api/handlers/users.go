package handlers

import (
	"net/http"

	"Backend/internal/utils"

	"github.com/go-chi/chi/v5"
)

// @SUMMARY		Fetches a user profile
// @Description	Fetches a user profile by ID
// @Tags			users
// @Accept			json
// @Produce		json
// @Param			id	path		string	true	"User ID"
// @Success		200	{object}	models.User
// @Failure		400	{object}	error
// @Failure		404	{object}	error
// @Failure		500	{object}	error
// @Security		ApiKeyAuth
// @Router			/users/{id} [get]
func (h *Handler) GetUserHandler(w http.ResponseWriter, r *http.Request) {
	userId := chi.URLParam(r, "userId")

	ctx := r.Context()
	user, err := h.UserService.GetUser(ctx, userId)
	if err != nil {
		h.responder.HandleError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, user); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// ActivateUser godoc
//
//	@Summary		Activates/Register a user
//	@Description	Activates/Register a user by invitation token
//	@Tags			users
//	@Accept			json
//	@Produce		json
//	@Param			token	path		string	true	"Invitation token"
//	@Success		204		{object}	string	"User activated"
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Security		ApiKeyAuth
//	@Router			/users/active/{token} [put]
func (h *Handler) ActivateUserHandler(w http.ResponseWriter, r *http.Request) {
	token := chi.URLParam(r, "token")

	err := h.UserService.ActivateUser(r.Context(), token)
	if err != nil {
		h.responder.HandleError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusNoContent, ""); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}
