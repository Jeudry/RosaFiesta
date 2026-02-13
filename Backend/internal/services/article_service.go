package services

import (
	"context"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type ArticleService struct {
	articles store.ArticleRepository
	logger   *zap.SugaredLogger
}

func NewArticleService(articles store.ArticleRepository, logger *zap.SugaredLogger) *ArticleService {
	return &ArticleService{
		articles: articles,
		logger:   logger,
	}
}

func (s *ArticleService) CreateArticle(ctx context.Context, article *models.Article) error {
	return s.articles.Create(ctx, article)
}

func (s *ArticleService) UpdateArticle(ctx context.Context, article *models.Article) error {
	return s.articles.Update(ctx, article)
}

func (s *ArticleService) DeleteArticle(ctx context.Context, articleID string) error {
	id, err := uuid.Parse(articleID)
	if err != nil {
		return err
	}
	return s.articles.Delete(ctx, id)
}

func (s *ArticleService) GetArticle(ctx context.Context, articleID string) (*models.Article, error) {
	id, err := uuid.Parse(articleID)
	if err != nil {
		return nil, err
	}
	return s.articles.GetById(ctx, id)
}

func (s *ArticleService) GetAllArticles(ctx context.Context) ([]models.Article, error) {
	return s.articles.GetAll(ctx)
}
