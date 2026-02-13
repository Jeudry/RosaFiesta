package main

import (
	"context"
	"net/http"

	"Backend/cmd/main/view_models/products"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type articleKey string

const articleCtx articleKey = "article"

// @Summary		Creates Article
// @Description	Creates a new article with variants
// @Tags			articles
// @Accept			json
// @Produce		json
// @Param			payload	body	products.CreateProductPayload	true	"Article creation payload"
// @Security		StaticApiKey
// @Success		201	{object}	models.Article	"Created article"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/articles [post]
func (app *Application) createArticleHandler(w http.ResponseWriter, r *http.Request) {
	var payload products.CreateProductPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

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

	if err := app.Store.Articles.Create(ctx, article); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, article); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Update Article
// @Description	Update an article basic info (not variants yet)
// @Tags			articles
// @Accept			json
// @Produce		json
// @Param			id		path	string							true	"Article ID"
// @Param			payload	body	products.UpdateProductPayload	true	"Article update payload"
// @Security		StaticApiKey
// @Success		200	{object}	models.Article	"Updated article"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/articles/{id} [put]
func (app *Application) updateArticleHandler(w http.ResponseWriter, r *http.Request) {
	var payload products.UpdateProductPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	article := GetArticleFromCtx(r)

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

	if err := app.Store.Articles.Update(r.Context(), article); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, article); err != nil {
		app.internalServerError(w, r, err)
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
func (app *Application) deleteArticleHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	article := GetArticleFromCtx(r)

	if err := app.Store.Articles.Delete(ctx, article.ID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusNoContent, ""); err != nil {
		app.internalServerError(w, r, err)
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
func (app *Application) getAllArticlesHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	articles, err := app.Store.Articles.GetAll(ctx)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, articles); err != nil {
		app.internalServerError(w, r, err)
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
func (app *Application) getArticleHandler(w http.ResponseWriter, r *http.Request) {
	article := GetArticleFromCtx(r)

	if err := app.jsonResponse(w, http.StatusOK, article); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) articlesContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "articleId")
		idAsUUID, err := uuid.Parse(idParam)
		if err != nil {
			app.badRequest(w, r, err)
			return
		}

		ctx := r.Context()

		article, err := app.Store.Articles.GetById(ctx, idAsUUID)
		if err != nil {
			app.handleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, articleCtx, article)

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func GetArticleFromCtx(r *http.Request) *models.Article {
	article, _ := r.Context().Value(articleCtx).(*models.Article)
	return article
}
