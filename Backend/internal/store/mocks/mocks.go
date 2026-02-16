package mocks

import (
	"context"
	"database/sql"
	"time"

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

func (m *EventStore) RemoveItem(ctx context.Context, eventID uuid.UUID, itemID uuid.UUID) error {
	args := m.Called(ctx, eventID, itemID)
	return args.Error(0)
}

func (m *EventStore) GetItems(ctx context.Context, eventID uuid.UUID) ([]models.EventItem, error) {
	args := m.Called(ctx, eventID)
	return args.Get(0).([]models.EventItem), args.Error(1)
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
