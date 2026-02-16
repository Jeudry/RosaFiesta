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
