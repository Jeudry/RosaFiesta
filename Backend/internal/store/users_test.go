package store

import (
	"context"
	"testing"

	"Backend/internal/store/models"
	"Backend/internal/testutils"

	"github.com/brianvoe/gofakeit/v6"
	"github.com/stretchr/testify/assert"
)

func TestUsersStore(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping integration test")
	}

	tdb := testutils.SetupTestDatabase(t)
	defer tdb.Teardown(t)

	s := &UsersStore{db: tdb.Db}
	ctx := context.Background()

	t.Run("Create and RetrieveById", func(t *testing.T) {
		user := &models.User{
			UserName:  gofakeit.Username(),
			FirstName: gofakeit.FirstName(),
			LastName:  gofakeit.LastName(),
			Email:     gofakeit.Email(),
		}
		user.Password.Set("password123")
		user.Role.Name = "user"

		// Create
		err := s.Create(ctx, nil, user)
		assert.NoError(t, err)
		assert.NotNil(t, user.ID)
		assert.NotZero(t, user.CreatedAt)

		// Retrieve
		retrieved, err := s.RetrieveById(ctx, user.ID)
		assert.NoError(t, err)
		assert.Equal(t, user.ID, retrieved.ID)
		assert.Equal(t, user.Email, retrieved.Email)
		assert.Equal(t, user.UserName, retrieved.UserName)
		assert.Equal(t, "user", retrieved.Role.Name)
	})

	t.Run("GetByEmail", func(t *testing.T) {
		email := gofakeit.Email()
		user := &models.User{
			UserName:  gofakeit.Username(),
			FirstName: gofakeit.FirstName(),
			LastName:  gofakeit.LastName(),
			Email:     email,
			IsActive:  true, // GetByEmail only returns active users
		}
		user.Password.Set("password123")
		user.Role.Name = "user"

		err := s.Create(ctx, nil, user)
		assert.NoError(t, err)

		// GetByEmail only works for activated users.
		// The Create method doesn't set activated=true in DB, it RETURNINGs it? No.
		// Let's check update() to activate.
		// Actually, I'll just use the Store methods to activate.

		// For now, let's manually activate for the test if the Store doesn't have an easy way without invitation.
		_, err = tdb.Db.ExecContext(ctx, "UPDATE users SET activated = true WHERE id = $1", user.ID)
		assert.NoError(t, err)

		retrieved, err := s.GetByEmail(ctx, email)
		assert.NoError(t, err)
		assert.Equal(t, user.ID, retrieved.ID)
		assert.Equal(t, email, retrieved.Email)
	})

	t.Run("Duplicate Email Error", func(t *testing.T) {
		email := gofakeit.Email()
		user1 := &models.User{
			UserName:  gofakeit.Username(),
			FirstName: gofakeit.FirstName(),
			LastName:  gofakeit.LastName(),
			Email:     email,
		}
		user1.Password.Set("password123")

		err := s.Create(ctx, nil, user1)
		assert.NoError(t, err)

		user2 := &models.User{
			UserName:  gofakeit.Username(),
			FirstName: gofakeit.FirstName(),
			LastName:  gofakeit.LastName(),
			Email:     email,
		}
		user2.Password.Set("password123")

		err = s.Create(ctx, nil, user2)
		assert.ErrorIs(t, err, ErrDuplicateEmail)
	})
}
