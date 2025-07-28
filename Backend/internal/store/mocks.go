package store

import (
	"Backend/internal/store/models"
	"context"
	"database/sql"
	"github.com/google/uuid"
	"time"
)

func NewMockStore() Storage {
	return Storage{
		Users: &MockUserStore{},
	}
}

type MockUserStore struct{}

func (m *MockUserStore) Create(ctx context.Context, tx *sql.Tx, u *models.User) error {
	return nil
}

func (m *MockUserStore) RetrieveById(ctx context.Context, userID uuid.UUID) (*models.User, error) {
	return &models.User{ID: userID}, nil
}

func (m *MockUserStore) GetByEmail(context.Context, string) (*models.User, error) {
	return &models.User{}, nil
}

func (m *MockUserStore) CreateAndInvite(ctx context.Context, user *models.User, token string, exp time.Duration) error {
	return nil
}

func (m *MockUserStore) Activate(ctx context.Context, t string) error {
	return nil
}

func (m *MockUserStore) Delete(ctx context.Context, id uuid.UUID) error {
	return nil
}
