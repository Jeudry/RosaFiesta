package main

import (
	"context"
	"net/http"

	"Backend/cmd/main/view_models/categories"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type categoryKey string

const categoryCtx categoryKey = "category"

// @Summary		Creates Category
// @Description	Creates a new category with the provided info
// @Tags			categories
// @Accept			json
// @Produce		json
// @Param			payload	body	categories.CreateCategoryPayload	true	"Category creation payload"
// @Security		ApiKeyAuth
// @Success		201	{object}	models.Category	"Created category"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/categories [post]
func (app *Application) createCategoryHandler(w http.ResponseWriter, r *http.Request) {
	var payload categories.CreateCategoryPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	category := &models.Category{
		BaseModel: models.BaseModel{
			CreatedBy: &user.UserName,
		},
		Name:        payload.Name,
		Description: payload.Description,
		ImageURL:    payload.ImageURL,
		ParentID:    payload.ParentID,
	}

	ctx := r.Context()

	if err := app.Store.Categories.Create(ctx, category); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, category); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Update Category
// @Description	Update a category with the provided info
// @Tags			categories
// @Accept			json
// @Produce		json
// @Param			id		path	string								true	"Category ID"
// @Param			payload	body	categories.UpdateCategoryPayload	true	"Category update payload"
// @Security		ApiKeyAuth
// @Success		200	{object}	models.Category	"Updated category"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/categories/{id} [put]
func (app *Application) updateCategoryHandler(w http.ResponseWriter, r *http.Request) {
	var payload categories.UpdateCategoryPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	category := GetCategoryFromCtx(r)

	category.Name = payload.Name
	category.Description = payload.Description
	category.ImageURL = payload.ImageURL
	category.ParentID = payload.ParentID
	category.UpdatedBy = &user.UserName

	if err := app.Store.Categories.Update(r.Context(), category); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, category); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Delete Category
// @Description	Delete a category by its ID
// @Tags			categories
// @Accept			json
// @Produce		json
// @Security		ApiKeyAuth
// @Param			categoryId	path		string	true	"Category ID"
// @Success		204			{object}	string	"Category deleted successfully"
// @Failure		400			{object}	error	"Bad request"
// @Failure		404			{object}	error	"Category not found"
// @Failure		500			{object}	error	"Internal server error"
// @Router			/categories/{categoryId} [delete]
func (app *Application) deleteCategoryHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	category := GetCategoryFromCtx(r)
	user := GetUserFromCtx(r)

	category.DeletedBy = &user.UserName

	if err := app.Store.Categories.Delete(ctx, category); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// @Summary		Get all Categories
// @Description	Get all categories
// @Tags			categories
// @Accept			json
// @Produce		json
// @Security		StaticApiKey
// @Success		200	{object}	[]models.Category	"List of categories"
// @Failure		500	{object}	error				"Internal server error"
// @Router			/categories [get]
func (app *Application) getAllCategoriesHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	categories, err := app.Store.Categories.GetAll(ctx)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if categories == nil {
		categories = []models.Category{}
	}

	if err := app.jsonResponse(w, http.StatusOK, categories); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Get Category
// @Description	Get a category by its ID
// @Tags			categories
// @Accept			json
// @Produce		json
// @Security		StaticApiKey
// @Param			categoryId	path		string			true	"Category ID"
// @Success		200			{object}	models.Category	"Category"
// @Failure		400			{object}	error			"Bad request"
// @Failure		404			{object}	error			"Category not found"
// @Failure		500			{object}	error			"Internal server error"
// @Router			/categories/{categoryId} [get]
func (app *Application) getCategoryHandler(w http.ResponseWriter, r *http.Request) {
	category := GetCategoryFromCtx(r)

	if err := app.jsonResponse(w, http.StatusOK, category); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) categoriesContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "categoryId")
		idAsUuid, err := uuid.Parse(idParam)
		if err != nil {
			app.badRequest(w, r, err)
			return
		}

		ctx := r.Context()

		category, err := app.Store.Categories.GetById(ctx, idAsUuid)
		if err != nil {
			app.handleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, categoryCtx, category)

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

// @Summary		Get Articles by Category
// @Description	Get all articles for a specific category
// @Tags			categories
// @Accept			json
// @Produce		json
// @Security		StaticApiKey
// @Param			categoryId	path		string				true	"Category ID"
// @Success		200			{object}	[]models.Article	"List of articles"
// @Failure		400			{object}	error				"Bad request"
// @Failure		500			{object}	error				"Internal server error"
// @Router			/categories/{categoryId}/articles [get]
func (app *Application) getArticlesByCategoryHandler(w http.ResponseWriter, r *http.Request) {
	category := GetCategoryFromCtx(r)

	articles, err := app.Store.Articles.GetByCategoryID(r.Context(), category.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if articles == nil {
		articles = []models.Article{}
	}

	if err := app.jsonResponse(w, http.StatusOK, articles); err != nil {
		app.internalServerError(w, r, err)
	}
}

func GetCategoryFromCtx(r *http.Request) *models.Category {
	category, _ := r.Context().Value(categoryCtx).(*models.Category)
	return category
}
