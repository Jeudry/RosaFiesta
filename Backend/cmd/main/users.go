package main

import (
	"net/http"

	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type userKey string

const UserCtx userKey = "user"

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
func (app *Application) getUserHandler(w http.ResponseWriter, r *http.Request) {
	userId, err := uuid.Parse(chi.URLParam(r, "userId"))
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()
	user, err := app.GetUser(ctx, userId)
	if err != nil {
		app.handleError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, user); err != nil {
		app.internalServerError(w, r, err)
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
func (app *Application) activateUserHandler(w http.ResponseWriter, r *http.Request) {
	token := chi.URLParam(r, "token")

	err := app.Store.Users.Activate(r.Context(), token)
	if err != nil {
		app.handleError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, "User activated successfully"); err != nil {
		app.internalServerError(w, r, err)
	}
}

func GetUserFromCtx(r *http.Request) *models.User {
	user, _ := r.Context().Value(UserCtx).(*models.User)
	return user
}
