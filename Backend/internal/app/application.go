package app

import (
	"context"

	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/config"
	"Backend/internal/mailer"
	"Backend/internal/ratelimiter"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type Application struct {
	Config       config.Config
	Store        store.Storage
	Logger       *zap.SugaredLogger
	Mailer       mailer.Client
	Auth         auth.Authenticator
	CacheStorage cache.Storage
	RateLimiter  ratelimiter.RateLimiter
}

func (app *Application) GetUser(ctx context.Context, userID uuid.UUID) (*models.User, error) {
	if !app.Config.Redis.Enabled {
		user, err := app.Store.Users.RetrieveById(ctx, userID)
		if err != nil {
			return nil, err
		}
		return user, nil
	}

	user, err := app.CacheStorage.Users.Get(ctx, userID)
	if err != nil {
		return nil, err
	}

	if user == nil {
		user, err = app.Store.Users.RetrieveById(ctx, userID)
		if err != nil {
			return nil, err
		}

		err = app.CacheStorage.Users.Set(ctx, user)
		if err != nil {
			return nil, err
		}
	}

	return user, nil
}
