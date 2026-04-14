package main

import (
	"net/http"

	"Backend/internal/store"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// @Summary		Get all bundles
// @Description	Returns all active bundles with their items and article details
// @Tags			bundles
// @Produce		json
// @Security		StaticApiKey
// @Success		200	{object}	[]models.Bundle	"List of bundles"
// @Failure		500	{object}	error	"Internal server error"
// @Router			/bundles [get]
func (app *Application) getBundlesHandler(w http.ResponseWriter, r *http.Request) {
	bundles, err := app.Store.Bundles.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, bundles)
}

// @Summary		Get bundle by ID
// @Description	Returns a single bundle with its items and article details
// @Tags			bundles
// @Produce		json
// @Security		StaticApiKey
// @Param			id	path		string	true	"Bundle ID"
// @Success		200	{object}	models.Bundle	"Bundle with items"
// @Failure		404	{object}	error	"Bundle not found"
// @Failure		500	{object}	error	"Internal server error"
// @Router			/bundles/{id} [get]
func (app *Application) getBundleHandler(w http.ResponseWriter, r *http.Request) {
	idStr := chi.URLParam(r, "id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	bundle, err := app.Store.Bundles.GetByID(r.Context(), id)
	if err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, bundle)
}

// @Summary		Get bundles by category
// @Description	Returns bundles that contain items from a specific category
// @Tags			bundles
// @Produce		json
// @Security		StaticApiKey
// @Param			categoryId	path		string	true	"Category ID"
// @Success		200	{object}	[]models.Bundle	"List of bundles for category"
// @Failure		500	{object}	error	"Internal server error"
// @Router		/bundles/category/{categoryId} [get]
func (app *Application) getBundlesByCategoryHandler(w http.ResponseWriter, r *http.Request) {
	categoryIdStr := chi.URLParam(r, "categoryId")
	categoryID, err := uuid.Parse(categoryIdStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	bundles, err := app.Store.Bundles.GetByCategory(r.Context(), categoryID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, bundles)
}
