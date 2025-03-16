package main

import (
	"Backend/cmd/main/view_models"
	"Backend/internal/store/models"
	"context"
	"github.com/go-chi/chi/v5"
	"net/http"
	"strconv"
)

type postKey string

const postCtx postKey = "post"

//	@Summary		Creates Post
//	@Description	Creates a new post with the provided title, content, and tags
//	@Tags			posts
//	@Accept			json
//	@Produce		json
//	@Param			payload	body	view_models.CreatePostPayload	true	"Post creation payload"
//	@Security		ApiKeyAuth
//	@Header			Authorization
//	@Success		201	{object}	models.Post	"Created post"
//	@Failure		400	{object}	error		"Bad request"
//	@Failure		500	{object}	error		"Internal server error"
//	@Router			/posts [post]

func (app *Application) createPostHandler(w http.ResponseWriter, r *http.Request) {
	var payload view_models.CreatePostPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	post := &models.Post{
		Title:   payload.Title,
		Content: payload.Content,
		UserID:  user.ID,
		Tags:    payload.Tags,
	}

	ctx := r.Context()

	if err := app.Store.Posts.Create(ctx, post); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, post); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Create a new post
// @Description	Create a new post with the provided title, content, and tags
// @Tags			posts
// @Accept			json
// @Produce		json
// @Param			payload	body	view_models.CreatePostPayload	true	"Post creation payload"
//
// @Security		ApiKeyAuth
//
// @Success		201	{object}	models.Post	"Created post"
// @Failure		400	{object}	error		"Bad request"
// @Failure		500	{object}	error		"Internal server error"
// @Router			/posts [post]
func (app *Application) getPostHandler(w http.ResponseWriter, r *http.Request) {
	post := GetPostFromCtx(r)

	comments, err := app.Store.Comments.RetrieveCommentsByPostId(r.Context(), post.ID)

	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	post.Comments = comments

	if err := app.jsonResponse(w, http.StatusOK, post); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Create a new comment for a post
// @Description	Create a new comment for a specific post
// @Tags			comments
// @Accept			json
// @Produce		json
// @Param			postId	path	int										true	"Post ID"
// @Param			payload	body	view_models.CreatePostCommentPayload	true	"Comment creation payload"
//
// @Security		ApiKeyAuth
//
// @Success		200	{object}	models.Comment	"Created comment"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/posts/{postId}/comments [post]
func (app *Application) createPostCommentHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "postId")
	idAsInt, err := strconv.ParseInt(idParam, 10, 64)

	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	ctx := r.Context()

	var payload view_models.CreatePostCommentPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	comment := &models.Comment{
		Content: payload.Comment,
		UserID:  1,
		PostID:  idAsInt,
	}

	err = app.Store.Comments.CreatePostComment(ctx, comment)

	if err != nil {
		app.internalServerError(w, r, err)
	}

	if err := app.jsonResponse(w, http.StatusOK, comment); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Update an existing post
// @Description	Update the title and/or content of an existing post
// @Tags			posts
// @Accept			json
// @Produce		json
// @Param			postId	path	int								true	"Post ID"
// @Param			payload	body	view_models.UpdatePostPayload	true	"Post update payload"
//
// @Security		ApiKeyAuth
//
// @Success		200	{object}	models.Post	"Updated post"
// @Failure		400	{object}	error		"Bad request"
// @Failure		404	{object}	error		"Post not found"
// @Failure		500	{object}	error		"Internal server error"
// @Router			/posts/{postId} [patch]
func (app *Application) updatePostHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	var payload view_models.UpdatePostPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	postToBeUpdated := GetPostFromCtx(r)

	if payload.Title != nil {
		postToBeUpdated.Title = *payload.Title
	}

	if payload.Content != nil {
		postToBeUpdated.Content = *payload.Content
	}

	if err := app.Store.Posts.Update(ctx, postToBeUpdated); err != nil {
		app.handleError(w, r, err)
	}

	if err := app.jsonResponse(w, http.StatusOK, postToBeUpdated); err != nil {
		app.internalServerError(w, r, err)
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
// @Param			postId	path		int		true	"Post ID"
// @Success		200		{object}	string	"Post deleted successfully"
// @Failure		400		{object}	error	"Bad request"
// @Failure		404		{object}	error	"Post not found"
// @Failure		500		{object}	error	"Internal server error"
// @Router			/posts/{postId} [delete]
func (app *Application) deletePostHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "postId")
	idAsInt, err := strconv.ParseInt(idParam, 10, 64)

	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	ctx := r.Context()

	if err := app.Store.Posts.Delete(ctx, idAsInt); err != nil {
		app.handleError(w, r, err)
	}

	if err := app.jsonResponse(w, http.StatusOK, nil); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) postsContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "postId")
		idAsInt, err := strconv.ParseInt(idParam, 10, 64)

		if err != nil {
			app.internalServerError(w, r, err)
			return
		}

		ctx := r.Context()

		post, err := app.Store.Posts.RetrieveById(ctx, idAsInt)

		if err != nil {
			app.handleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, postCtx, post)

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func GetPostFromCtx(r *http.Request) *models.Post {
	post, _ := r.Context().Value(postCtx).(*models.Post)
	return post
}
