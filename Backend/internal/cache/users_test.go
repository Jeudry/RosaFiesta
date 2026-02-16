package cache

import (
	"context"
	"testing"

	"Backend/internal/store/models"
	"Backend/internal/testutils"

	"github.com/brianvoe/gofakeit/v6"
	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
)

func TestUserCache(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping integration test")
	}

	tr := testutils.SetupTestRedis(t)
	defer tr.Teardown(t)

	cache := &UserStore{rdb: tr.Client}
	ctx := context.Background()

	t.Run("Set and Get", func(t *testing.T) {
		user := &models.User{
			ID:       uuid.New(),
			UserName: gofakeit.Username(),
			Email:    gofakeit.Email(),
		}

		// Set
		err := cache.Set(ctx, user)
		assert.NoError(t, err)

		// Get
		retrieved, err := cache.Get(ctx, user.ID)
		assert.NoError(t, err)
		assert.NotNil(t, retrieved)
		assert.Equal(t, user.ID, retrieved.ID)
		assert.Equal(t, user.Email, retrieved.Email)
	})

	t.Run("Get Non-Existent", func(t *testing.T) {
		retrieved, err := cache.Get(ctx, uuid.New())
		assert.NoError(t, err)
		assert.Nil(t, retrieved)
	})
}
