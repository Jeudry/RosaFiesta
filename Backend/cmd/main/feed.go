package main

import (
	"net/http"

	"Backend/internal/store/models"
)

// @Summary		Get User Feed
// @Description	Retrieves the feed for a user with pagination and sorting options
// @Tags			feed
// @Accept			json
// @Produce		json
// @Param			limit	path		int			true	"User ID"
// @Param			offset	path		int			true	"User ID"
// @Param			sort	path		string		true	"User ID"
// @Success		200		{array}		models.Post	"List of posts in the user's feed"
// @Failure		400		{object}	error
// @Failure		500		{object}	error
//
// @Router			/users/feed [get]
func (app *Application) getUserFeedHandler(w http.ResponseWriter, r *http.Request) {
	fq := models.PaginatedFeedQueryModel{
		Limit:  20,
		Offset: 0,
		Sort:   "desc",
	}

	fq, err := fq.Parse(r)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(fq); err != nil {
		app.badRequest(w, r, err)
	}

	user := GetUserFromCtx(r)
	ctx := r.Context()

	feed, err := app.Store.Posts.GetUserFeed(ctx, user.ID, fq)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, feed); err != nil {
		app.internalServerError(w, r, err)
	}
}
