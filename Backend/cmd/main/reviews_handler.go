package main

import (
	"errors"
	"net/http"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type createReviewPayload struct {
	Rating  int    `json:"rating" validate:"required,min=1,max=5"`
	Comment string `json:"comment" validate:"required"`
}

// @Summary		Create Review
// @Description	Add a review to an article
// @Tags			reviews
// @Accept			json
// @Produce		json
// @Param			articleId	path		string				true	"Article ID"
// @Param			payload		body		createReviewPayload	true	"Review payload"
// @Security		StaticApiKey
// @Success		201			{object}	models.Review		"Created review"
// @Failure		400			{object}	error				"Bad request"
// @Failure		500			{object}	error				"Internal server error"
// @Router			/articles/{articleId}/reviews [post]
func (app *Application) createReviewHandler(w http.ResponseWriter, r *http.Request) {
	var payload createReviewPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	articleIDParam := chi.URLParam(r, "articleId")
	articleID, err := uuid.Parse(articleIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	review := &models.Review{
		UserID:    user.ID,
		ArticleID: articleID,
		Rating:    payload.Rating,
		Comment:   payload.Comment,
	}

	if err := app.Store.Reviews.Create(r.Context(), review); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, review); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Get Article Reviews
// @Description	Get all reviews for an article
// @Tags			reviews
// @Accept			json
// @Produce		json
// @Param			articleId	path		string			true	"Article ID"
// @Success		200			{object}	[]models.Review	"List of reviews"
// @Failure		400			{object}	error			"Bad request"
// @Failure		500			{object}	error			"Internal server error"
// @Router			/articles/{articleId}/reviews [get]
func (app *Application) getArticleReviewsHandler(w http.ResponseWriter, r *http.Request) {
	articleIDParam := chi.URLParam(r, "articleId")
	articleID, err := uuid.Parse(articleIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	reviews, err := app.Store.Reviews.GetByArticleID(r.Context(), articleID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, reviews); err != nil {
		app.internalServerError(w, r, err)
	}
}

// ── Company Reviews ─────────────────────────────────────────────────────────────

type createCompanyReviewPayload struct {
	Rating  int    `json:"rating" validate:"required,min=1,max=5"`
	Comment string `json:"comment" validate:"required"`
	Source  string `json:"source"`
}

// @Summary		Create Company Review
// @Description	Add a company-wide review for RosaFiesta
// @Tags			company-reviews
// @Accept			json
// @Produce		json
// @Success		201	{object}	models.CompanyReview	"Created review"
// @Failure		400	{object}	error					"Bad request"
// @Failure		500	{object}	error					"Internal server error"
// @Router		/company/reviews [post]
func (app *Application) createCompanyReviewHandler(w http.ResponseWriter, r *http.Request) {
	var payload createCompanyReviewPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	if payload.Source == "" {
		payload.Source = "direct"
	}

	user := GetUserFromCtx(r)
	review := &models.CompanyReview{
		UserID:   user.ID,
		Rating:   payload.Rating,
		Comment:  payload.Comment,
		Source:    payload.Source,
	}
	if err := app.Store.CompanyReviews.Create(r.Context(), review); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	if err := app.jsonResponse(w, http.StatusCreated, review); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Get Company Reviews
// @Description	Get all company-wide reviews
// @Tags			company-reviews
// @Produce		json
// @Success		200	{object}	[]models.CompanyReview	"List of company reviews"
// @Failure		500	{object}	error					"Internal server error"
// @Router		/company/reviews [get]
func (app *Application) getCompanyReviewsHandler(w http.ResponseWriter, r *http.Request) {
	reviews, err := app.Store.CompanyReviews.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	if err := app.jsonResponse(w, http.StatusOK, reviews); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Get Company Reviews Summary
// @Description	Get average rating and count for RosaFiesta
// @Tags			company-reviews
// @Produce		json
// @Success		200	{object}	map[string]interface{}	"rating summary"
// @Failure		500	{object}	error					"Internal server error"
// @Router		/company/reviews/summary [get]
func (app *Application) getCompanyReviewsSummaryHandler(w http.ResponseWriter, r *http.Request) {
	avg, count, err := app.Store.CompanyReviews.GetSummary(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	if err := app.jsonResponse(w, http.StatusOK, map[string]interface{}{
		"average_rating": avg,
		"review_count":   count,
	}); err != nil {
		app.internalServerError(w, r, err)
	}
}
