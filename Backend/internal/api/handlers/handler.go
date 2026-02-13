package handlers

import (
	"net/http"

	"Backend/internal/app"
	"Backend/internal/services"
	"Backend/internal/utils"
	"Backend/internal/utils/apperrors"
)

type Handler struct {
	app             *app.Application
	responder       *utils.Responder
	AuthService     services.AuthServicer
	UserService     services.UserServicer
	PostService     services.PostServicer
	ArticleService  services.ArticleServicer
	CategoryService services.CategoryServicer
	FeedService     services.FeedServicer
}

func NewHandler(
	app *app.Application,
	authService services.AuthServicer,
	userService services.UserServicer,
	postService services.PostServicer,
	articleService services.ArticleServicer,
	categoryService services.CategoryServicer,
	feedService services.FeedServicer,
) *Handler {
	return &Handler{
		app:             app,
		responder:       utils.NewResponder(app.Logger),
		AuthService:     authService,
		UserService:     userService,
		PostService:     postService,
		ArticleService:  articleService,
		CategoryService: categoryService,
		FeedService:     feedService,
	}
}

func (h *Handler) RespondWithError(w http.ResponseWriter, err error) {
	status, payload := apperrors.MapError(err)
	h.responder.JSON(w, status, payload)
}
