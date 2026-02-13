package handlers

import (
	"context"
	"net/http"

	"Backend/internal/api/middleware"
	"Backend/internal/dtos"
	"Backend/internal/store/models"
	"Backend/internal/utils"

	"github.com/go-chi/chi/v5"
)

// @Summary		Creates Category
// @Description	Creates a new category with the provided info
// @Tags			categories
// @Accept			json
// @Produce		json
// @Param			payload	body	dtos.CreateCategoryPayload	true	"Category creation payload"
// @Security		ApiKeyAuth
// @Success		201	{object}	models.Category	"Created category"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/categories [post]
func (h *Handler) CreateCategoryHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.CreateCategoryPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	user := middleware.GetUserFromCtx(r)

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

	if err := h.CategoryService.CreateCategory(ctx, category); err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusCreated, category); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Update Category
// @Description	Update a category with the provided info
// @Tags			categories
// @Accept			json
// @Produce		json
// @Param			id		path	string								true	"Category ID"
// @Param			payload	body	dtos.UpdateCategoryPayload	true	"Category update payload"
// @Security		ApiKeyAuth
// @Success		200	{object}	models.Category	"Updated category"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/categories/{id} [put]
func (h *Handler) UpdateCategoryHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.UpdateCategoryPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	user := middleware.GetUserFromCtx(r)
	category := middleware.GetCategoryFromCtx(r)

	category.Name = payload.Name
	category.Description = payload.Description
	category.ImageURL = payload.ImageURL
	category.ParentID = payload.ParentID
	category.UpdatedBy = &user.UserName

	if err := h.CategoryService.UpdateCategory(r.Context(), category); err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, category); err != nil {
		h.responder.InternalServerError(w, r, err)
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
func (h *Handler) DeleteCategoryHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	category := middleware.GetCategoryFromCtx(r)
	user := middleware.GetUserFromCtx(r)

	category.DeletedBy = &user.UserName

	if err := h.CategoryService.DeleteCategory(ctx, category.ID.String()); err != nil {
		h.responder.InternalServerError(w, r, err)
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
func (h *Handler) GetAllCategoriesHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	categories, err := h.CategoryService.GetAllCategories(ctx)
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, categories); err != nil {
		h.responder.InternalServerError(w, r, err)
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
func (h *Handler) GetCategoryHandler(w http.ResponseWriter, r *http.Request) {
	category := middleware.GetCategoryFromCtx(r)

	if err := utils.JSONResponse(w, http.StatusOK, category); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

func (h *Handler) CategoriesContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "categoryId")

		ctx := r.Context()

		category, err := h.CategoryService.GetCategory(ctx, idParam)
		if err != nil {
			h.responder.HandleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, middleware.CategoryCtx, category)

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
func (h *Handler) GetArticlesByCategoryHandler(w http.ResponseWriter, r *http.Request) {
	category := middleware.GetCategoryFromCtx(r)

	articles, err := h.CategoryService.GetArticlesByCategory(r.Context(), category.ID.String())
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, articles); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}
