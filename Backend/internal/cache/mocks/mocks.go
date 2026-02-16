package mocks

import (
	"context"

	"Backend/internal/store/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/mock"
)

type UserCache struct {
	mock.Mock
}

func (m *UserCache) Get(ctx context.Context, id uuid.UUID) (*models.User, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.User), args.Error(1)
}

func (m *UserCache) Set(ctx context.Context, user *models.User) error {
	args := m.Called(ctx, user)
	return args.Error(0)
}
