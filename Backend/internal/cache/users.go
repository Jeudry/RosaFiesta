package cache

import (
	"Backend/internal/store/models"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/go-redis/redis/v8"
	"github.com/google/uuid"
	"log"
)

type UserStore struct {
	rdb *redis.Client
}

const UserExpTime = 60 * 60 * 60

func (u *UserStore) Get(ctx context.Context, userID uuid.UUID) (*models.User, error) {
	cacheKey := fmt.Sprintf("user-%v", userID)

	data, err := u.rdb.Get(ctx, cacheKey).Result()

	if errors.Is(err, redis.Nil) {
		return nil, nil
	} else if err != nil {
		return nil, err
	}

	var user models.User
	if err := json.Unmarshal([]byte(data), &user); err != nil {
		return nil, err
	}

	return &user, nil
}

func (u *UserStore) Set(ctx context.Context, user *models.User) error {
	cacheKey := fmt.Sprintf("user-%v", user.ID)

	data, err := json.Marshal(user)
	if err != nil {
		return err
	}

	err = u.rdb.Set(ctx, cacheKey, data, 0).Err()
	if err != nil {
		return err
	}

	// Verify that the data was set correctly
	_, err = u.rdb.Get(ctx, cacheKey).Result()
	if err != nil {
		log.Printf("Failed to verify set operation: %v", err)
		return err
	}
	return nil
}
