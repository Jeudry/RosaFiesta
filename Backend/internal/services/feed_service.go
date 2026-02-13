package services

import (
	"context"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type FeedService struct {
	posts  store.PostRepository
	logger *zap.SugaredLogger
}

func NewFeedService(posts store.PostRepository, logger *zap.SugaredLogger) *FeedService {
	return &FeedService{
		posts:  posts,
		logger: logger,
	}
}

func (s *FeedService) GetUserFeed(ctx context.Context, userID string, p models.PaginatedFeedQueryModel) ([]models.PostWithMetadata, error) {
	id, err := uuid.Parse(userID)
	if err != nil {
		return nil, err
	}
	return s.posts.GetUserFeed(ctx, id, p)
}
