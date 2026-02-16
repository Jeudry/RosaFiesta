package store

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

var (
	ErrNotFound          = errors.New("resource not found")
	QueryTimeoutDuration = 5 * time.Second
	ErrConflict          = errors.New("resource conflict")
)

type Storage struct {
	Articles interface {
		Create(context.Context, *models.Article) error
		GetById(context.Context, uuid.UUID) (*models.Article, error)
		GetByCategoryID(context.Context, uuid.UUID) ([]models.Article, error)
		Update(context.Context, *models.Article) error
		Delete(context.Context, uuid.UUID) error
		GetAll(context.Context) ([]models.Article, error)
	}
	Categories interface {
		Create(context.Context, *models.Category) error
		GetById(context.Context, uuid.UUID) (*models.Category, error)
		Update(context.Context, *models.Category) error
		Delete(context.Context, *models.Category) error
		GetAll(context.Context) ([]models.Category, error)
	}
	Posts interface {
		Create(context.Context, *models.Post) error
		RetrieveById(context.Context, uuid.UUID) (*models.Post, error)
		Update(context.Context, *models.Post) error
		Delete(context.Context, uuid.UUID) error
		GetUserFeed(context.Context, uuid.UUID, models.PaginatedFeedQueryModel) ([]models.PostWithMetadata, error)
	}
	Users interface {
		Create(context.Context, *sql.Tx, *models.User) error
		RetrieveById(context.Context, uuid.UUID) (*models.User, error)
		CreateAndInvite(context.Context, *models.User, string, time.Duration) error
		Activate(context.Context, string) error
		Delete(context.Context, uuid.UUID) error
		GetByEmail(context.Context, string) (*models.User, error)
	}
	Roles interface {
		RetrieveByName(context.Context, string) (*models.Role, error)
	}
	Comments interface {
		CreatePostComment(context.Context, *models.Comment) error
		RetrieveCommentsByPostId(context.Context, uuid.UUID) ([]models.Comment, error)
	}
	RefreshTokens interface {
		Create(context.Context, *models.RefreshToken) error
		GetByToken(context.Context, string) (*models.RefreshToken, error)
		Delete(context.Context, string) error
		DeleteAllForUser(context.Context, uuid.UUID) error
	}
	Carts interface {
		Create(context.Context, *models.Cart) error
		GetByUserID(context.Context, uuid.UUID) (*models.Cart, error)
		AddItem(context.Context, *models.CartItem) error
		UpdateItemQuantity(context.Context, uuid.UUID, int) error
		RemoveItem(context.Context, uuid.UUID) error
		ClearCart(context.Context, uuid.UUID) error
	}
	Events interface {
		Create(context.Context, *models.Event) error
		GetByID(context.Context, uuid.UUID) (*models.Event, error)
		GetByUserID(context.Context, uuid.UUID) ([]models.Event, error)
		Update(context.Context, *models.Event) error

		Delete(context.Context, uuid.UUID) error
		AddItem(context.Context, *models.EventItem) error
		RemoveItem(context.Context, uuid.UUID, uuid.UUID) error
		GetItems(context.Context, uuid.UUID) ([]models.EventItem, error)
	}
}

func NewStorage(db *sql.DB) Storage {
	return Storage{
		Articles:      &ArticlesStore{db: db},
		Categories:    &CategoriesStore{db: db},
		Posts:         &PostsStore{db: db},
		Users:         &UsersStore{db: db},
		Comments:      &CommentsStore{db: db},
		Roles:         &RolesStore{db: db},
		RefreshTokens: &RefreshTokensStore{db: db},
		Carts:         &CartsStore{db: db},
		Events:        &EventStore{db: db},
	}
}

func withTx(db *sql.DB, ctx context.Context, fn func(*sql.Tx) error) error {
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}

	if err := fn(tx); err != nil {
		_ = tx.Rollback()
		return err
	}

	return tx.Commit()
}
