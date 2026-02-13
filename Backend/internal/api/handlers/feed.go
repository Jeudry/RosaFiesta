package handlers

import (
	"fmt"
	"net/http"

	"Backend/internal/api/middleware"
	"Backend/internal/store/models"
	"Backend/internal/utils"
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
func (h *Handler) GetUserFeedHandler(w http.ResponseWriter, r *http.Request) {
	fq := models.PaginatedFeedQueryModel{
		Limit:  20,
		Offset: 0,
		Sort:   "desc",
	}

	fq, err := fq.Parse(r)
	if err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(fq); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	user := middleware.GetUserFromCtx(r)
	if user == nil {
		h.responder.Unauthorized(w, r, fmt.Errorf("user not found in context"))
		return
	}

	ctx := r.Context()

	feed, err := h.FeedService.GetUserFeed(ctx, user.ID.String(), fq)
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, feed); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}
