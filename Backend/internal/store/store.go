package store

import (
	"Backend/internal/store/models"
	"context"
	"database/sql"
	"errors"
	"time"
)

var (
	ErrNotFound          = errors.New("resource not found")
	QueryTimeoutDuration = 5 * time.Second
	ErrConflict          = errors.New("resource conflict")
)

type Storage struct {
	Products interface {
		Create(context.Context, *models.Product) error
		GetById(context.Context, int64) (*models.Product, error)
		Update(context.Context, *models.Product) error
		Delete(context.Context, *models.Product) error
		GetAll(context.Context) ([]models.Product, error)
	}
	Posts interface {
		Create(context.Context, *models.Post) error
		RetrieveById(context.Context, int64) (*models.Post, error)
		Update(context.Context, *models.Post) error
		Delete(context.Context, int64) error
		GetUserFeed(context.Context, int64, models.PaginatedFeedQueryModel) ([]models.PostWithMetadata, error)
	}
	Users interface {
		Create(context.Context, *sql.Tx, *models.User) error
		RetrieveById(context.Context, int64) (*models.User, error)
		CreateAndInvite(context.Context, *models.User, string, time.Duration) error
		Activate(context.Context, string) error
		Delete(context.Context, int64) error
		GetByEmail(context.Context, string) (*models.User, error)
	}
	Roles interface {
		RetrieveByName(context.Context, string) (*models.Role, error)
	}
	Comments interface {
		CreatePostComment(context.Context, *models.Comment) error
		RetrieveCommentsByPostId(context.Context, int64) ([]models.Comment, error)
	}
}

func NewStorage(db *sql.DB) Storage {
	return Storage{
		Products: &ProductsStore{db: db},
		Posts:    &PostsStore{db: db},
		Users:    &UsersStore{db: db},
		Comments: &CommentsStore{db: db},
		Roles:    &RolesStore{db: db},
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
