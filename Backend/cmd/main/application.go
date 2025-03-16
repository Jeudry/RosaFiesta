package main

import (
	"Backend/cmd/main/configModels"
	"Backend/internal/auth"
	"Backend/internal/cache"
	"Backend/internal/mailer"
	"Backend/internal/ratelimiter"
	"Backend/internal/store"
	"go.uber.org/zap"
)

type Application struct {
	Config       configModels.Config
	Store        store.Storage
	Logger       *zap.SugaredLogger
	Mailer       mailer.Client
	Auth         auth.Authenticator
	CacheStorage cache.Storage
	RateLimiter  ratelimiter.RateLimiter
}
