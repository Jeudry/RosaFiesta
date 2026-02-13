package services

import (
	"context"

	"Backend/internal/dtos"
	"Backend/internal/store/models"
)

// AuthServicer defines the interface for authentication-related operations.
type AuthServicer interface {
	// RegisterUser registers a new user with the given payload.
	RegisterUser(ctx context.Context, payload dtos.RegisterUserPayload) (*models.UserWithToken, error)
	// Login authenticates a user by email and password.
	Login(ctx context.Context, email, password string) (*models.UserToken, error)
	// RefreshToken generates a new access token using a valid refresh token.
	RefreshToken(ctx context.Context, token string) (*models.UserToken, error)
}

// UserServicer defines the interface for user profile operations.
type UserServicer interface {
	// GetUser retrieves a user by their ID.
	GetUser(ctx context.Context, userID string) (*models.User, error)
	// ActivateUser activates a user account using a token.
	ActivateUser(ctx context.Context, token string) error
}

// PostServicer defines the interface for blog post operations.
type PostServicer interface {
	// CreatePost creates a new post.
	CreatePost(ctx context.Context, post *models.Post) error
	// UpdatePost updates an existing post.
	UpdatePost(ctx context.Context, post *models.Post) error
	// DeletePost deletes a post by ID.
	DeletePost(ctx context.Context, postID string) error
	// CreateComment adds a comment to a post.
	CreateComment(ctx context.Context, comment *models.Comment) error
	// GetComments retrieves all comments for a post.
	GetComments(ctx context.Context, postID string) ([]models.Comment, error)
	// GetPost retrieves a single post by ID.
	GetPost(ctx context.Context, postID string) (*models.Post, error)
}

// ArticleServicer defines the interface for article management in the catalog.
type ArticleServicer interface {
	// CreateArticle creates a new article.
	CreateArticle(ctx context.Context, article *models.Article) error
	// UpdateArticle updates an existing article.
	UpdateArticle(ctx context.Context, article *models.Article) error
	// DeleteArticle deletes an article by ID.
	DeleteArticle(ctx context.Context, articleID string) error
	// GetArticle retrieves a single article by ID.
	GetArticle(ctx context.Context, articleID string) (*models.Article, error)
	// GetAllArticles retrieves all articles.
	GetAllArticles(ctx context.Context) ([]models.Article, error)
}

// CategoryServicer defines the interface for category management.
type CategoryServicer interface {
	// CreateCategory creates a new category.
	CreateCategory(ctx context.Context, category *models.Category) error
	// UpdateCategory updates an existing category.
	UpdateCategory(ctx context.Context, category *models.Category) error
	// DeleteCategory deletes a category by ID.
	DeleteCategory(ctx context.Context, categoryID string) error
	// GetCategory retrieves a single category by ID.
	GetCategory(ctx context.Context, categoryID string) (*models.Category, error)
	// GetAllCategories retrieves all categories.
	GetAllCategories(ctx context.Context) ([]models.Category, error)
	// GetArticlesByCategory retrieves all articles belonging to a specific category.
	GetArticlesByCategory(ctx context.Context, categoryID string) ([]models.Article, error)
}

// FeedServicer defines the interface for retrieving user feeds.
type FeedServicer interface {
	// GetUserFeed retrieves a paginated feed of posts for a specific user.
	GetUserFeed(ctx context.Context, userID string, p models.PaginatedFeedQueryModel) ([]models.PostWithMetadata, error)
}
