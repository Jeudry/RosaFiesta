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

//	@Summary		Creates Post
//	@Description	Creates a new post with the provided title, content, and tags
//	@Tags			posts
//	@Accept			json
//	@Produce		json
//	@Param			payload	body	dtos.CreatePostPayload	true	"Post creation payload"
//	@Security		ApiKeyAuth
//	@Header			Authorization
//	@Success		201	{object}	models.Post	"Created post"
//	@Failure		400	{object}	error		"Bad request"
//	@Failure		500	{object}	error		"Internal server error"
//	@Router			/posts [post]

func (h *Handler) CreatePostHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.CreatePostPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	user := middleware.GetUserFromCtx(r)

	post := &models.Post{
		Title:   payload.Title,
		Content: payload.Content,
		UserID:  user.ID,
		Tags:    payload.Tags,
	}

	ctx := r.Context()

	if err := h.PostService.CreatePost(ctx, post); err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusCreated, post); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Create a new post
// @Description	Create a new post with the provided title, content, and tags
// @Tags			posts
// @Accept			json
// @Produce		json
// @Param			payload	body	dtos.CreatePostPayload	true	"Post creation payload"
//
// @Security		ApiKeyAuth
//
// @Success		201	{object}	models.Post	"Created post"
// @Failure		400	{object}	error		"Bad request"
// @Failure		500	{object}	error		"Internal server error"
// @Router			/posts [post]
func (h *Handler) GetPostHandler(w http.ResponseWriter, r *http.Request) {
	post := middleware.GetPostFromCtx(r)

	comments, err := h.PostService.GetComments(r.Context(), post.ID.String())
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	post.Comments = comments

	if err := utils.JSONResponse(w, http.StatusOK, post); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Create a new comment for a post
// @Description	Create a new comment for a specific post
// @Tags			comments
// @Accept			json
// @Produce		json
// @Param			postId	path	string							true	"Post ID"
// @Param			payload	body	dtos.CreatePostCommentPayload	true	"Comment creation payload"
//
// @Security		ApiKeyAuth
//
// @Success		200	{object}	models.Comment	"Created comment"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/posts/{postId}/comments [post]
func (h *Handler) CreatePostCommentHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "postId")
	postID, err := uuid.Parse(idParam)
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	ctx := r.Context()

	var payload dtos.CreatePostCommentPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	comment := &models.Comment{
		Content: payload.Comment,
		UserID:  middleware.GetUserFromCtx(r).ID,
		PostID:  postID,
	}

	err = h.PostService.CreateComment(ctx, comment)
	if err != nil {
		h.responder.InternalServerError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, comment); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Update an existing post
// @Description	Update the title and/or content of an existing post
// @Tags			posts
// @Accept			json
// @Produce		json
// @Param			postId	path	string					true	"Post ID"
// @Param			payload	body	dtos.UpdatePostPayload	true	"Post update payload"
//
// @Security		ApiKeyAuth
//
// @Success		200	{object}	models.Post	"Updated post"
// @Failure		400	{object}	error		"Bad request"
// @Failure		404	{object}	error		"Post not found"
// @Failure		500	{object}	error		"Internal server error"
// @Router			/posts/{postId} [patch]
func (h *Handler) UpdatePostHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	var payload dtos.UpdatePostPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	postToBeUpdated := middleware.GetPostFromCtx(r)

	if payload.Title != nil {
		postToBeUpdated.Title = *payload.Title
	}

	if payload.Content != nil {
		postToBeUpdated.Content = *payload.Content
	}

	if err := h.PostService.UpdatePost(ctx, postToBeUpdated); err != nil {
		h.responder.HandleError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, postToBeUpdated); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// @Summary		Delete an existing post
// @Description	Delete a post by its ID
// @Tags			posts
// @Accept			json
// @Produce		json
//
// @Security		ApiKeyAuth
//
// @Param			postId	path		string	true	"Post ID"
// @Success		200		{object}	string	"Post deleted successfully"
// @Failure		400		{object}	error	"Bad request"
// @Failure		404		{object}	error	"Post not found"
// @Failure		500		{object}	error	"Internal server error"
// @Router			/posts/{postId} [delete]
func (h *Handler) DeletePostHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "postId")

	ctx := r.Context()

	if err := h.PostService.DeletePost(ctx, idParam); err != nil {
		h.responder.HandleError(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, nil); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

func (h *Handler) PostsContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "postId")

		ctx := r.Context()

		post, err := h.PostService.GetPost(ctx, idParam)
		if err != nil {
			h.responder.HandleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, middleware.PostCtx, post)

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}
