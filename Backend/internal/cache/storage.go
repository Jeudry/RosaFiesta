package cache

import (
	"Backend/internal/store/models"
	"context"
	"github.com/go-redis/redis/v8"
	"github.com/google/uuid"
)

type Storage struct {
	Users interface {
		Get(context.Context, uuid.UUID) (*models.User, error)
		Set(context.Context, *models.User) error
	}
}

func NewRedisStorage(rdb *redis.Client) Storage {
	return Storage{
		Users: &UserStore{rdb: rdb},
	}
}
