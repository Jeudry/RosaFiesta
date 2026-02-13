package middleware

import (
	"net/http"

	"Backend/internal/store/models"
)

type contextKey string

const (
	UserCtx     contextKey = "user"
	PostCtx     contextKey = "post"
	ArticleCtx  contextKey = "article"
	CategoryCtx contextKey = "category"
)

func GetUserFromCtx(r *http.Request) *models.User {
	user, _ := r.Context().Value(UserCtx).(*models.User)
	return user
}

func GetPostFromCtx(r *http.Request) *models.Post {
	post, _ := r.Context().Value(PostCtx).(*models.Post)
	return post
}

func GetArticleFromCtx(r *http.Request) *models.Article {
	article, _ := r.Context().Value(ArticleCtx).(*models.Article)
	return article
}

func GetCategoryFromCtx(r *http.Request) *models.Category {
	category, _ := r.Context().Value(CategoryCtx).(*models.Category)
	return category
}
