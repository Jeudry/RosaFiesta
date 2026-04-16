package mocks

import (
	"context"
	"database/sql"
	"time"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

type UserStore struct {
	mock.Mock
}

func (m *UserStore) Create(ctx context.Context, tx *sql.Tx, user *models.User) error {
	args := m.Called(ctx, tx, user)
	return args.Error(0)
}

func (m *UserStore) RetrieveById(ctx context.Context, id uuid.UUID) (*models.User, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.User), args.Error(1)
}

func (m *UserStore) CreateAndInvite(ctx context.Context, user *models.User, token string, exp time.Duration) error {
	args := m.Called(ctx, user, token, exp)
	return args.Error(0)
}

func (m *UserStore) Activate(ctx context.Context, token string) error {
	args := m.Called(ctx, token)
	return args.Error(0)
}

func (m *UserStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *UserStore) GetByEmail(ctx context.Context, email string) (*models.User, error) {
	args := m.Called(ctx, email)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.User), args.Error(1)
}

func (m *UserStore) UpdateFCMToken(ctx context.Context, userID uuid.UUID, token string) error {
	args := m.Called(ctx, userID, token)
	return args.Error(0)
}

func (m *UserStore) GetOrganizersFCMTokens(ctx context.Context) ([]string, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]string), args.Error(1)
}

func (m *UserStore) UpdatePhoneNumber(ctx context.Context, userID uuid.UUID, phone string) error {
	args := m.Called(ctx, userID, phone)
	return args.Error(0)
}

func (m *UserStore) CreatePasswordResetToken(ctx context.Context, userID uuid.UUID, token string, exp time.Duration) error {
	args := m.Called(ctx, userID, token, exp)
	return args.Error(0)
}

func (m *UserStore) GetUserByResetToken(ctx context.Context, token string) (*models.User, error) {
	args := m.Called(ctx, token)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.User), args.Error(1)
}

func (m *UserStore) DeletePasswordResetToken(ctx context.Context, userID uuid.UUID) error {
	args := m.Called(ctx, userID)
	return args.Error(0)
}

func (m *UserStore) DeletePasswordResetTokenByToken(ctx context.Context, token string) error {
	args := m.Called(ctx, token)
	return args.Error(0)
}

func (m *UserStore) UpdatePassword(ctx context.Context, userID uuid.UUID, passwordHash []byte) error {
	args := m.Called(ctx, userID, passwordHash)
	return args.Error(0)
}

func (m *UserStore) GetAllClientsForExport(ctx context.Context) ([]models.ClientExport, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.ClientExport), args.Error(1)
}

type ArticlesStore struct {
	mock.Mock
}

func (m *ArticlesStore) Create(ctx context.Context, article *models.Article) error {
	args := m.Called(ctx, article)
	return args.Error(0)
}

func (m *ArticlesStore) GetById(ctx context.Context, id uuid.UUID) (*models.Article, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Article), args.Error(1)
}

func (m *ArticlesStore) GetByCategoryID(ctx context.Context, id uuid.UUID) ([]models.Article, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.Article), args.Error(1)
}

func (m *ArticlesStore) GetAvailability(ctx context.Context, id uuid.UUID, date time.Time) (int, error) {
	args := m.Called(ctx, id, date)
	return args.Int(0), args.Error(1)
}

func (m *ArticlesStore) Update(ctx context.Context, article *models.Article) error {
	args := m.Called(ctx, article)
	return args.Error(0)
}

func (m *ArticlesStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *ArticlesStore) GetAll(ctx context.Context, limit, offset int) ([]models.Article, error) {
	args := m.Called(ctx, limit, offset)
	return args.Get(0).([]models.Article), args.Error(1)
}

func (m *ArticlesStore) GetLowStockCount(ctx context.Context) (int, error) {
	args := m.Called(ctx)
	return args.Int(0), args.Error(1)
}

func (m *ArticlesStore) Search(ctx context.Context, params store.ArticleSearchParams) ([]models.Article, error) {
	args := m.Called(ctx, params)
	return args.Get(0).([]models.Article), args.Error(1)
}

func (m *ArticlesStore) Count(ctx context.Context, search string, categoryID string) (int, error) {
	args := m.Called(ctx, search, categoryID)
	return args.Int(0), args.Error(1)
}

type RoleStore struct {
	mock.Mock
}

func (m *RoleStore) RetrieveByName(ctx context.Context, name string) (*models.Role, error) {
	args := m.Called(ctx, name)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Role), args.Error(1)
}

type CategoryStore struct {
	mock.Mock
}

func (m *CategoryStore) Create(ctx context.Context, category *models.Category) error {
	args := m.Called(ctx, category)
	return args.Error(0)
}

func (m *CategoryStore) GetById(ctx context.Context, id uuid.UUID) (*models.Category, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Category), args.Error(1)
}

func (m *CategoryStore) Update(ctx context.Context, category *models.Category) error {
	args := m.Called(ctx, category)
	return args.Error(0)
}

func (m *CategoryStore) Delete(ctx context.Context, category *models.Category) error {
	args := m.Called(ctx, category)
	return args.Error(0)
}

func (m *CategoryStore) GetAll(ctx context.Context) ([]models.Category, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.Category), args.Error(1)
}

type RefreshTokenStore struct {
	mock.Mock
}

func (m *RefreshTokenStore) Create(ctx context.Context, token *models.RefreshToken) error {
	args := m.Called(ctx, token)
	return args.Error(0)
}

func (m *RefreshTokenStore) GetByToken(ctx context.Context, token string) (*models.RefreshToken, error) {
	args := m.Called(ctx, token)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.RefreshToken), args.Error(1)
}

func (m *RefreshTokenStore) Delete(ctx context.Context, token string) error {
	args := m.Called(ctx, token)
	return args.Error(0)
}

func (m *RefreshTokenStore) DeleteAllForUser(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

type EventStore struct {
	mock.Mock
}

func (m *EventStore) Create(ctx context.Context, event *models.Event) error {
	args := m.Called(ctx, event)
	return args.Error(0)
}

func (m *EventStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Event, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Event), args.Error(1)
}

func (m *EventStore) GetByUserID(ctx context.Context, id uuid.UUID) ([]models.Event, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.Event), args.Error(1)
}

func (m *EventStore) GetPendingByUserID(ctx context.Context, id uuid.UUID) ([]models.Event, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.Event), args.Error(1)
}

func (m *EventStore) GetOrCreateDraft(ctx context.Context, userID uuid.UUID) (*models.Event, error) {
	args := m.Called(ctx, userID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Event), args.Error(1)
}

func (m *EventStore) GetAll(ctx context.Context) ([]models.Event, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.Event), args.Error(1)
}

func (m *EventStore) Update(ctx context.Context, event *models.Event) error {
	args := m.Called(ctx, event)
	return args.Error(0)
}

func (m *EventStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *EventStore) AddItem(ctx context.Context, item *models.EventItem) error {
	args := m.Called(ctx, item)
	return args.Error(0)
}

func (m *EventStore) UpdateItemQuantity(ctx context.Context, itemID uuid.UUID, quantity int) error {
	args := m.Called(ctx, itemID, quantity)
	return args.Error(0)
}

func (m *EventStore) RemoveItem(ctx context.Context, eventID uuid.UUID, itemID uuid.UUID) error {
	args := m.Called(ctx, eventID, itemID)
	return args.Error(0)
}

func (m *EventStore) GetItems(ctx context.Context, eventID uuid.UUID) ([]models.EventItem, error) {
	args := m.Called(ctx, eventID)
	return args.Get(0).([]models.EventItem), args.Error(1)
}

func (m *EventStore) GetDebrief(ctx context.Context, id uuid.UUID) (*models.EventDebrief, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.EventDebrief), args.Error(1)
}

func (m *EventStore) ApproveQuote(ctx context.Context, eventID, userID uuid.UUID) error {
	args := m.Called(ctx, eventID, userID)
	return args.Error(0)
}

func (m *EventStore) RejectQuote(ctx context.Context, eventID, userID uuid.UUID) error {
	args := m.Called(ctx, eventID, userID)
	return args.Error(0)
}

type GuestStore struct {
	mock.Mock
}

func (m *GuestStore) Create(ctx context.Context, guest *models.Guest) error {
	args := m.Called(ctx, guest)
	return args.Error(0)
}

func (m *GuestStore) GetByEventID(ctx context.Context, id uuid.UUID) ([]models.Guest, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.Guest), args.Error(1)
}

func (m *GuestStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Guest, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Guest), args.Error(1)
}

func (m *GuestStore) Update(ctx context.Context, guest *models.Guest) error {
	args := m.Called(ctx, guest)
	return args.Error(0)
}

func (m *GuestStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *GuestStore) Confirm(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *GuestStore) Decline(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

type EventTaskStore struct {
	mock.Mock
}

func (m *EventTaskStore) Create(ctx context.Context, task *models.EventTask) error {
	args := m.Called(ctx, task)
	return args.Error(0)
}

func (m *EventTaskStore) GetByEventID(ctx context.Context, id uuid.UUID) ([]models.EventTask, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.EventTask), args.Error(1)
}

func (m *EventTaskStore) GetByID(ctx context.Context, id uuid.UUID) (*models.EventTask, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.EventTask), args.Error(1)
}

func (m *EventTaskStore) Update(ctx context.Context, task *models.EventTask) error {
	args := m.Called(ctx, task)
	return args.Error(0)
}

func (m *EventTaskStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

type SupplierStore struct {
	mock.Mock
}

func (m *SupplierStore) Create(ctx context.Context, supplier *models.Supplier) error {
	args := m.Called(ctx, supplier)
	return args.Error(0)
}

func (m *SupplierStore) GetByUserID(ctx context.Context, id uuid.UUID) ([]models.Supplier, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.Supplier), args.Error(1)
}

func (m *SupplierStore) GetByID(ctx context.Context, id uuid.UUID) (*models.Supplier, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Supplier), args.Error(1)
}

func (m *SupplierStore) Update(ctx context.Context, supplier *models.Supplier) error {
	args := m.Called(ctx, supplier)
	return args.Error(0)
}

func (m *SupplierStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

type TimelineStore struct {
	mock.Mock
}

func (m *TimelineStore) Create(ctx context.Context, item *models.TimelineItem) error {
	args := m.Called(ctx, item)
	return args.Error(0)
}

func (m *TimelineStore) GetByEventID(ctx context.Context, id uuid.UUID) ([]models.TimelineItem, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.TimelineItem), args.Error(1)
}

func (m *TimelineStore) GetByID(ctx context.Context, id uuid.UUID) (*models.TimelineItem, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.TimelineItem), args.Error(1)
}

func (m *TimelineStore) Update(ctx context.Context, item *models.TimelineItem) error {
	args := m.Called(ctx, item)
	return args.Error(0)
}

func (m *TimelineStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *TimelineStore) GetOverdueCriticalItems(ctx context.Context) ([]models.TimelineItemWithUser, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.TimelineItemWithUser), args.Error(1)
}

type ReviewsStore struct {
	mock.Mock
}

func (m *ReviewsStore) Create(ctx context.Context, review *models.Review) error {
	args := m.Called(ctx, review)
	return args.Error(0)
}

func (m *ReviewsStore) GetByArticleID(ctx context.Context, id uuid.UUID) ([]models.Review, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.Review), args.Error(1)
}

func (m *ReviewsStore) GetSummary(ctx context.Context, id uuid.UUID) (float64, int, error) {
	args := m.Called(ctx, id)
	return args.Get(0).(float64), args.Int(1), args.Error(2)
}

type StatsStore struct {
	mock.Mock
}

func (m *StatsStore) GetSummary(ctx context.Context) (*models.AdminStats, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.AdminStats), args.Error(1)
}

type MessagesStore struct {
	mock.Mock
}

func (m *MessagesStore) Create(ctx context.Context, msg *models.EventMessage) error {
	args := m.Called(ctx, msg)
	return args.Error(0)
}

func (m *MessagesStore) GetByEventID(ctx context.Context, id uuid.UUID) ([]models.EventMessage, error) {
	args := m.Called(ctx, id)
	return args.Get(0).([]models.EventMessage), args.Error(1)
}

type FavoritesStore struct {
	mock.Mock
}

func (m *FavoritesStore) List(ctx context.Context, userID uuid.UUID) ([]models.Article, error) {
	args := m.Called(ctx, userID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.Article), args.Error(1)
}

func (m *FavoritesStore) Add(ctx context.Context, userID, articleID uuid.UUID) error {
	args := m.Called(ctx, userID, articleID)
	return args.Error(0)
}

func (m *FavoritesStore) Remove(ctx context.Context, userID, articleID uuid.UUID) error {
	args := m.Called(ctx, userID, articleID)
	return args.Error(0)
}

func (m *FavoritesStore) IsFavorite(ctx context.Context, userID, articleID uuid.UUID) (bool, error) {
	args := m.Called(ctx, userID, articleID)
	return args.Bool(0), args.Error(1)
}

type CompanyReviewsStore struct {
	mock.Mock
}

func (m *CompanyReviewsStore) Create(ctx context.Context, review *models.CompanyReview) error {
	args := m.Called(ctx, review)
	return args.Error(0)
}

func (m *CompanyReviewsStore) GetAll(ctx context.Context) ([]models.CompanyReview, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.CompanyReview), args.Error(1)
}

func (m *CompanyReviewsStore) GetSummary(ctx context.Context) (float64, int, error) {
	args := m.Called(ctx)
	return args.Get(0).(float64), args.Int(1), args.Error(2)
}

type EventReviewsStore struct {
	mock.Mock
}

func (m *EventReviewsStore) Create(ctx context.Context, review *models.EventReview) error {
	args := m.Called(ctx, review)
	return args.Error(0)
}

func (m *EventReviewsStore) GetByEventID(ctx context.Context, id uuid.UUID) ([]models.EventReview, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.EventReview), args.Error(1)
}

func (m *EventReviewsStore) GetSummary(ctx context.Context, id uuid.UUID) (float64, int, error) {
	args := m.Called(ctx, id)
	return args.Get(0).(float64), args.Int(1), args.Error(2)
}

func (m *EventReviewsStore) AddPhoto(ctx context.Context, reviewID uuid.UUID, photoURL string, caption string, sortOrder int) error {
	args := m.Called(ctx, reviewID, photoURL, caption, sortOrder)
	return args.Error(0)
}

func (m *EventReviewsStore) GetPhotos(ctx context.Context, reviewID uuid.UUID) ([]models.ReviewPhoto, error) {
	args := m.Called(ctx, reviewID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.ReviewPhoto), args.Error(1)
}

type NotificationLogsStore struct {
	mock.Mock
}

func (m *NotificationLogsStore) LogNotification(ctx context.Context, eventID uuid.UUID, notificationType models.NotificationType) error {
	args := m.Called(ctx, eventID, notificationType)
	return args.Error(0)
}

func (m *NotificationLogsStore) HasNotificationBeenSent(ctx context.Context, eventID uuid.UUID, notificationType models.NotificationType) (bool, error) {
	args := m.Called(ctx, eventID, notificationType)
	return args.Bool(0), args.Error(1)
}

type PostsStore struct {
	mock.Mock
}

func (m *PostsStore) Create(ctx context.Context, post *models.Post) error {
	args := m.Called(ctx, post)
	return args.Error(0)
}

func (m *PostsStore) RetrieveById(ctx context.Context, id uuid.UUID) (*models.Post, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.Post), args.Error(1)
}

func (m *PostsStore) Update(ctx context.Context, post *models.Post) error {
	args := m.Called(ctx, post)
	return args.Error(0)
}

func (m *PostsStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *PostsStore) GetUserFeed(ctx context.Context, userID uuid.UUID, fq models.PaginatedFeedQueryModel) ([]models.PostWithMetadata, error) {
	args := m.Called(ctx, userID, fq)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.PostWithMetadata), args.Error(1)
}

type CommentsStore struct {
	mock.Mock
}

func (m *CommentsStore) CreatePostComment(ctx context.Context, comment *models.Comment) error {
	args := m.Called(ctx, comment)
	return args.Error(0)
}

func (m *CommentsStore) RetrieveCommentsByPostId(ctx context.Context, postID uuid.UUID) ([]models.Comment, error) {
	args := m.Called(ctx, postID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.Comment), args.Error(1)
}

type EventPhotosStore struct {
	mock.Mock
}

func (m *EventPhotosStore) Create(ctx context.Context, photo *models.EventPhoto) error {
	args := m.Called(ctx, photo)
	return args.Error(0)
}

func (m *EventPhotosStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.EventPhoto, error) {
	args := m.Called(ctx, eventID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.EventPhoto), args.Error(1)
}

func (m *EventPhotosStore) Delete(ctx context.Context, id uuid.UUID) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

type AuditLogsStore struct {
	mock.Mock
}

func (m *AuditLogsStore) Log(ctx context.Context, log *models.AuditLog) error {
	args := m.Called(ctx, log)
	return args.Error(0)
}

func (m *AuditLogsStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.AuditLogWithUser, error) {
	args := m.Called(ctx, eventID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.AuditLogWithUser), args.Error(1)
}

type InstallmentsStore struct {
	mock.Mock
}

func (m *InstallmentsStore) CreateInstallmentPayment(ctx context.Context, eventID uuid.UUID, amount int, dueDate *time.Time) (*models.InstallmentPayment, error) {
	args := m.Called(ctx, eventID, amount, dueDate)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.InstallmentPayment), args.Error(1)
}

func (m *InstallmentsStore) GetInstallmentByEventID(ctx context.Context, eventID uuid.UUID) ([]models.InstallmentPayment, error) {
	args := m.Called(ctx, eventID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.InstallmentPayment), args.Error(1)
}

func (m *InstallmentsStore) MarkPaid(ctx context.Context, paymentID uuid.UUID, paymentMethod string) error {
	args := m.Called(ctx, paymentID, paymentMethod)
	return args.Error(0)
}

func (m *InstallmentsStore) GetPendingInstallments(ctx context.Context) ([]models.InstallmentPayment, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]models.InstallmentPayment), args.Error(1)
}

func (m *InstallmentsStore) GetByID(ctx context.Context, paymentID uuid.UUID) (*models.InstallmentPayment, error) {
	args := m.Called(ctx, paymentID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.InstallmentPayment), args.Error(1)
}
