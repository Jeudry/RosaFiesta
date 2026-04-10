package main

import (
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// @Summary		List favorites
// @Description	List all articles favorited by the current user
// @Tags			favorites
// @Accept			json
// @Produce		json
// @Security		BearerAuth
// @Success		200	{array}	models.Article	"Favorited articles"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/favorites [get]
func (app *Application) listFavoritesHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	user := GetUserFromCtx(r)

	favorites, err := app.Store.Favorites.List(ctx, user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, favorites); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Add favorite
// @Description	Mark an article as favorite for the current user. Idempotent.
// @Tags			favorites
// @Accept			json
// @Produce		json
// @Param			articleId	path	string	true	"Article ID"
// @Security		BearerAuth
// @Success		201	{object}	map[string]bool	"Favorited"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/favorites/{articleId} [post]
func (app *Application) addFavoriteHandler(w http.ResponseWriter, r *http.Request) {
	articleID, err := uuid.Parse(chi.URLParam(r, "articleId"))
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()
	user := GetUserFromCtx(r)

	if err := app.Store.Favorites.Add(ctx, user.ID, articleID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, map[string]bool{"favorited": true}); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Remove favorite
// @Description	Remove an article from the current user's favorites. No-op if not favorited.
// @Tags			favorites
// @Accept			json
// @Produce		json
// @Param			articleId	path	string	true	"Article ID"
// @Security		BearerAuth
// @Success		204	{object}	nil	"Removed"
// @Failure		400	{object}	error	"Bad request"
// @Failure		500	{object}	error	"Internal server error"
// @Router			/favorites/{articleId} [delete]
func (app *Application) removeFavoriteHandler(w http.ResponseWriter, r *http.Request) {
	articleID, err := uuid.Parse(chi.URLParam(r, "articleId"))
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()
	user := GetUserFromCtx(r)

	if err := app.Store.Favorites.Remove(ctx, user.ID, articleID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
