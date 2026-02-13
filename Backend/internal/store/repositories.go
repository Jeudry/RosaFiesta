package store

import (
	"context"
	"database/sql"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

// ArticleRepository defines storage operations for articles.
type ArticleRepository interface {
	Create(context.Context, *models.Article) error
	GetById(context.Context, uuid.UUID) (*models.Article, error)
	GetByCategoryID(context.Context, uuid.UUID) ([]models.Article, error)
	Update(context.Context, *models.Article) error
	Delete(context.Context, uuid.UUID) error
	GetAll(context.Context) ([]models.Article, error)
}

// CategoryRepository defines storage operations for categories.
type CategoryRepository interface {
	Create(context.Context, *models.Category) error
	GetById(context.Context, uuid.UUID) (*models.Category, error)
	Update(context.Context, *models.Category) error
	Delete(context.Context, *models.Category) error
	GetAll(context.Context) ([]models.Category, error)
}

// PostRepository defines storage operations for posts.
type PostRepository interface {
	Create(context.Context, *models.Post) error
	RetrieveById(context.Context, uuid.UUID) (*models.Post, error)
	Update(context.Context, *models.Post) error
	Delete(context.Context, uuid.UUID) error
	GetUserFeed(context.Context, uuid.UUID, models.PaginatedFeedQueryModel) ([]models.PostWithMetadata, error)
}

// UserRepository defines storage operations for users.
type UserRepository interface {
	Create(context.Context, *sql.Tx, *models.User) error
	RetrieveById(context.Context, uuid.UUID) (*models.User, error)
	CreateAndInvite(context.Context, *models.User, string, time.Duration) error
	Activate(context.Context, string) error
	Delete(context.Context, uuid.UUID) error
	GetByEmail(context.Context, string) (*models.User, error)
}

// RoleRepository defines storage operations for user roles.
type RoleRepository interface {
	RetrieveByName(context.Context, string) (*models.Role, error)
}

// CommentRepository defines storage operations for comments.
type CommentRepository interface {
	CreatePostComment(context.Context, *models.Comment) error
	RetrieveCommentsByPostId(context.Context, uuid.UUID) ([]models.Comment, error)
}

// RefreshTokenRepository defines storage operations for refresh tokens.
type RefreshTokenRepository interface {
	Create(context.Context, *models.RefreshToken) error
	GetByToken(context.Context, string) (*models.RefreshToken, error)
	Delete(context.Context, string) error
	DeleteAllForUser(context.Context, uuid.UUID) error
}
