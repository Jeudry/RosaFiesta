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
		GetAvailability(context.Context, uuid.UUID, time.Time) (int, error)
		Update(context.Context, *models.Article) error
		Delete(context.Context, uuid.UUID) error
		GetAll(context.Context, int, int) ([]models.Article, error)
		GetLowStockCount(context.Context) (int, error)
		Search(context.Context, ArticleSearchParams) ([]models.Article, error)
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
		UpdateFCMToken(context.Context, uuid.UUID, string) error
		GetOrganizersFCMTokens(context.Context) ([]string, error)
		UpdatePhoneNumber(context.Context, uuid.UUID, string) error
		CreatePasswordResetToken(context.Context, uuid.UUID, string, time.Duration) error
		GetUserByResetToken(context.Context, string) (*models.User, error)
		DeletePasswordResetToken(context.Context, uuid.UUID) error
		DeletePasswordResetTokenByToken(context.Context, string) error
		UpdatePassword(context.Context, uuid.UUID, []byte) error
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
	Events interface {
		Create(context.Context, *models.Event) error
		GetByID(context.Context, uuid.UUID) (*models.Event, error)
		GetByUserID(context.Context, uuid.UUID) ([]models.Event, error)
		GetPendingByUserID(context.Context, uuid.UUID) ([]models.Event, error)
		// GetOrCreateDraft returns the user's current draft event,
		// creating an empty one if none exists. This is the entry point
		// for the catalog "+" button — every user always has exactly
		// one draft acting as their active event.
		GetOrCreateDraft(context.Context, uuid.UUID) (*models.Event, error)
		Update(context.Context, *models.Event) error
		Delete(context.Context, uuid.UUID) error
		AddItem(context.Context, *models.EventItem) error
		UpdateItemQuantity(context.Context, uuid.UUID, int) error
		RemoveItem(context.Context, uuid.UUID, uuid.UUID) error
		GetItems(context.Context, uuid.UUID) ([]models.EventItem, error)
		GetDebrief(context.Context, uuid.UUID) (*models.EventDebrief, error)
		GetAll(context.Context) ([]models.Event, error)
		ApproveQuote(context.Context, uuid.UUID, uuid.UUID) error
		RejectQuote(context.Context, uuid.UUID, uuid.UUID) error
	}
	Guests interface {
		Create(context.Context, *models.Guest) error
		GetByEventID(context.Context, uuid.UUID) ([]models.Guest, error)
		GetByID(context.Context, uuid.UUID) (*models.Guest, error)
		Update(context.Context, *models.Guest) error
		Delete(context.Context, uuid.UUID) error
		Confirm(context.Context, uuid.UUID) error
		Decline(context.Context, uuid.UUID) error
	}
	EventTasks interface {
		Create(context.Context, *models.EventTask) error
		GetByEventID(context.Context, uuid.UUID) ([]models.EventTask, error)
		GetByID(context.Context, uuid.UUID) (*models.EventTask, error)
		Update(context.Context, *models.EventTask) error
		Delete(context.Context, uuid.UUID) error
	}
	Suppliers interface {
		Create(context.Context, *models.Supplier) error
		GetByUserID(context.Context, uuid.UUID) ([]models.Supplier, error)
		GetByID(context.Context, uuid.UUID) (*models.Supplier, error)
		Update(context.Context, *models.Supplier) error
		Delete(context.Context, uuid.UUID) error
	}
	Timeline interface {
		Create(context.Context, *models.TimelineItem) error
		GetByEventID(context.Context, uuid.UUID) ([]models.TimelineItem, error)
		GetByID(context.Context, uuid.UUID) (*models.TimelineItem, error)
		Update(context.Context, *models.TimelineItem) error
		Delete(context.Context, uuid.UUID) error
		GetOverdueCriticalItems(context.Context) ([]models.TimelineItemWithUser, error)
	}
	Messages interface {
		Create(context.Context, *models.EventMessage) error
		GetByEventID(context.Context, uuid.UUID) ([]models.EventMessage, error)
	}
	Stats interface {
		GetSummary(context.Context) (*models.AdminStats, error)
	}
	Reviews interface {
		Create(context.Context, *models.Review) error
		GetByArticleID(context.Context, uuid.UUID) ([]models.Review, error)
		GetSummary(context.Context, uuid.UUID) (float64, int, error)
	}
	EventReviews interface {
		Create(context.Context, *models.EventReview) error
		GetByEventID(context.Context, uuid.UUID) ([]models.EventReview, error)
		GetSummary(context.Context, uuid.UUID) (float64, int, error)
		AddPhoto(ctx context.Context, reviewID uuid.UUID, photoURL string, caption string, sortOrder int) error
		GetPhotos(ctx context.Context, reviewID uuid.UUID) ([]models.ReviewPhoto, error)
	}
	CompanyReviews interface {
		Create(context.Context, *models.CompanyReview) error
		GetAll(context.Context) ([]models.CompanyReview, error)
		GetSummary(context.Context) (float64, int, error)
	}
	NotificationLogs interface {
		LogNotification(context.Context, uuid.UUID, models.NotificationType) error
		HasNotificationBeenSent(context.Context, uuid.UUID, models.NotificationType) (bool, error)
	}
	Favorites interface {
		List(context.Context, uuid.UUID) ([]models.Article, error)
		Add(context.Context, uuid.UUID, uuid.UUID) error
		Remove(context.Context, uuid.UUID, uuid.UUID) error
		IsFavorite(context.Context, uuid.UUID, uuid.UUID) (bool, error)
	}
	EventPhotos interface {
		Create(context.Context, *models.EventPhoto) error
		GetByEventID(context.Context, uuid.UUID) ([]models.EventPhoto, error)
		Delete(context.Context, uuid.UUID) error
	}
	AuditLogs interface {
		Log(context.Context, *models.AuditLog) error
		GetByEventID(context.Context, uuid.UUID) ([]models.AuditLogWithUser, error)
	}
	DeliveryZones interface {
		GetAll(context.Context) ([]models.DeliveryZone, error)
		GetByID(context.Context, uuid.UUID) (*models.DeliveryZone, error)
		CalculateFee(context.Context, string) (*models.DeliveryFeeResponse, error)
	}
	Bundles interface {
		GetAll(context.Context) ([]models.Bundle, error)
		GetByID(context.Context, uuid.UUID) (*models.Bundle, error)
		GetByCategory(context.Context, uuid.UUID) ([]models.Bundle, error)
	}
}

func NewStorage(db *sql.DB) Storage {
	return Storage{
		Articles:         &ArticlesStore{db: db},
		Categories:       &CategoriesStore{db: db},
		Posts:            &PostsStore{db: db},
		Users:            &UsersStore{db: db},
		Comments:         &CommentsStore{db: db},
		Roles:            &RolesStore{db: db},
		RefreshTokens:    &RefreshTokensStore{db: db},
		Events:           &EventStore{db: db},
		Guests:           &GuestStore{db: db},
		EventTasks:       &EventTaskStore{db: db},
		Suppliers:        &SupplierStore{db: db},
		Timeline:         &timelineStore{db: db},
		Messages:         &MessagesStore{db: db},
		Stats:            &StatsStore{db: db},
		Reviews:          &ReviewsStore{db: db},
		EventReviews:     &EventReviewsStore{db: db},
		CompanyReviews:   &CompanyReviewsStore{db: db},
		NotificationLogs: &NotificationLogsStore{db: db},
		Favorites:        &FavoritesStore{db: db},
		EventPhotos:      &EventPhotosStore{db: db},
		AuditLogs:        &AuditLogsStore{db: db},
		DeliveryZones:    &DeliveryZonesStore{db: db},
		Bundles:          &BundlesStore{db: db},
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
