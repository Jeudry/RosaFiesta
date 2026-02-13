package handlers

import (
	"context"
	"net/http"

	"Backend/internal/api/middleware"
	"Backend/internal/dtos"
	"Backend/internal/store/models"
	"Backend/internal/utils"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// @Summary		Creates Article
// @Description	Creates a new article with variants
// @Tags			articles
// @Accept			json
// @Produce		json
// @Param			payload	body	dtos.CreateProductPayload	true	"Article creation payload"
// @Security		StaticApiKey
// @Success		201	{object}	models.Article	"Created article"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/articles [post]
func (h *Handler) CreateArticleHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.CreateProductPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	user := middleware.GetUserFromCtx(r)

	article := &models.Article{
		BaseModel: models.BaseModel{
			CreatedBy: &user.UserName,
		},
		NameTemplate:        payload.NameTemplate,
		DescriptionTemplate: payload.DescriptionTemplate,
		Type:                payload.Type,
		IsActive:            payload.IsActive,
	}

	if payload.CategoryID != nil {
		catID, err := uuid.Parse(*payload.CategoryID)
		if err == nil {
			article.CategoryID = &catID
		}
	}

	// Map Variants
	for _, vPayload := range payload.Variants {
		variant := models.ArticleVariant{
			Sku:             vPayload.Sku,
			Name:            vPayload.Name,
			Description:     vPayload.Description,
			ImageURL:        vPayload.ImageURL,
			IsActive:        vPayload.IsActive,
			Stock:           vPayload.Stock,
			RentalPrice:     vPayload.RentalPrice,
			SalePrice:       vPayload.SalePrice,
			ReplacementCost: vPayload.ReplacementCost,
			Attributes:      vPayload.Attributes,
		}

		// Map Dimensions
		for _, dPayload := range vPayload.Dimensions {
			dim := models.ArticleDimension{
				Height: dPayload.Height,
				Width:  dPayload.Width,
				Depth:  dPayload.Depth,
				Weight: dPayload.Weight,
			}
			variant.Dimensions = append(variant.Dimensions, dim)

		}
		article.Variants = append(article.Variants, variant)
	}

	ctx := r.Context()

	if err := h.ArticleService.CreateArticle(ctx, article); err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusCreated, article); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Update Article
// @Description	Update an article basic info (not variants yet)
// @Tags			articles
// @Accept			json
// @Produce		json
// @Param			id		path	string							true	"Article ID"
// @Param			payload	body	dtos.UpdateProductPayload	true	"Article update payload"
// @Security		StaticApiKey
// @Success		200	{object}	models.Article	"Updated article"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/articles/{id} [put]
func (h *Handler) UpdateArticleHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.UpdateProductPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	user := middleware.GetUserFromCtx(r)
	article := middleware.GetArticleFromCtx(r)

	article.NameTemplate = payload.NameTemplate
	article.DescriptionTemplate = payload.DescriptionTemplate
	article.Type = payload.Type
	article.IsActive = payload.IsActive
	article.UpdatedBy = &user.UserName

	if payload.CategoryID != nil {
		catID, err := uuid.Parse(*payload.CategoryID)
		if err == nil {
			article.CategoryID = &catID
		}
	}

	if err := h.ArticleService.UpdateArticle(r.Context(), article); err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, article); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Delete Article
// @Description	Delete an article by its ID
// @Tags			articles
// @Accept			json
// @Produce		json
// @Security		StaticApiKey
// @Param			articleId	path		string	true	"Article ID"
// @Success		204			{object}	string	"Article deleted successfully"
// @Failure		400			{object}	error	"Bad request"
// @Failure		404			{object}	error	"Article not found"
// @Failure		500			{object}	error	"Internal server error"
// @Router			/articles/{articleId} [delete]
func (h *Handler) DeleteArticleHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	article := middleware.GetArticleFromCtx(r)

	if err := h.ArticleService.DeleteArticle(ctx, article.ID.String()); err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusNoContent, ""); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Get all Articles
// @Description	Get all articles
// @Tags			articles
// @Accept			json
// @Produce		json
// @Security		StaticApiKey
// @Success		200	{object}	[]models.Article	"List of articles"
// @Failure		500	{object}	error				"Internal server error"
// @Router			/articles [get]
func (h *Handler) GetAllArticlesHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	articles, err := h.ArticleService.GetAllArticles(ctx)
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, articles); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Get Article
// @Description	Get an article by its ID with variants
// @Tags			articles
// @Accept			json
// @Produce		json
// @Security		StaticApiKey
// @Param			articleId	path		string			true	"Article ID"
// @Success		200			{object}	models.Article	"Article"
// @Failure		400			{object}	error			"Bad request"
// @Failure		404			{object}	error			"Article not found"
// @Failure		500			{object}	error			"Internal server error"
// @Router			/articles/{articleId} [get]
func (h *Handler) GetArticleHandler(w http.ResponseWriter, r *http.Request) {
	article := middleware.GetArticleFromCtx(r)

	if err := utils.JSONResponse(w, http.StatusOK, article); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

func (h *Handler) ArticlesContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "articleId")

		ctx := r.Context()

		article, err := h.ArticleService.GetArticle(ctx, idParam)
		if err != nil {
			h.responder.HandleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, middleware.ArticleCtx, article)

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}
