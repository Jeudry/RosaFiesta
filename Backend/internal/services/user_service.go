package services

import (
	"context"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type UserService struct {
	users  store.UserRepository
	logger *zap.SugaredLogger
}

func NewUserService(users store.UserRepository, logger *zap.SugaredLogger) *UserService {
	return &UserService{
		users:  users,
		logger: logger,
	}
}

func (s *UserService) GetUser(ctx context.Context, userID string) (*models.User, error) {
	id, err := uuid.Parse(userID)
	if err != nil {
		return nil, err
	}
	// We might want to use the cached version if available (from app.GetUser logic),
	// but strictly speaking the service should probably use the cache service if we had one.
	// For now, let's just use the store directly or reproduce the logic from Application.GetUser.
	// The original Application.GetUser logic was:
	// 1. Check cache
	// 2. If missing, check store
	// 3. Set cache
	// Since we are moving logic to Service, this logic belongs here.
	// However, the current prompt hasn't emphasized caching in Service Layer yet.
	// Given the interface `GetUser(ctx, userID string)`, we should use the Store.
	// Wait, the interface for UserServicer uses string ID, but Store uses UUID.

	return s.users.RetrieveById(ctx, id)
}

func (s *UserService) ActivateUser(ctx context.Context, token string) error {
	return s.users.Activate(ctx, token)
}
