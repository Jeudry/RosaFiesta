package services

import (
	"context"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type CategoryService struct {
	categories store.CategoryRepository
	articles   store.ArticleRepository
	logger     *zap.SugaredLogger
}

func NewCategoryService(categories store.CategoryRepository, articles store.ArticleRepository, logger *zap.SugaredLogger) *CategoryService {
	return &CategoryService{
		categories: categories,
		articles:   articles,
		logger:     logger,
	}
}

func (s *CategoryService) CreateCategory(ctx context.Context, category *models.Category) error {
	return s.categories.Create(ctx, category)
}

func (s *CategoryService) UpdateCategory(ctx context.Context, category *models.Category) error {
	return s.categories.Update(ctx, category)
}

func (s *CategoryService) DeleteCategory(ctx context.Context, categoryID string) error {
	// Category deletion in logic needs to fetch the category first to pass the model,
	// because the store interface Delete takes *models.Category?
	// Let's check `store.go`.
	// Delete(context.Context, *models.Category) error
	// Yes, so we need to fetch it first.
	// But `DeleteCategory` in interface takes `categoryID string`.

	id, err := uuid.Parse(categoryID)
	if err != nil {
		return err
	}

	category, err := s.categories.GetById(ctx, id)
	if err != nil {
		return err
	}

	return s.categories.Delete(ctx, category)
}

func (s *CategoryService) GetCategory(ctx context.Context, categoryID string) (*models.Category, error) {
	id, err := uuid.Parse(categoryID)
	if err != nil {
		return nil, err
	}
	return s.categories.GetById(ctx, id)
}

func (s *CategoryService) GetAllCategories(ctx context.Context) ([]models.Category, error) {
	return s.categories.GetAll(ctx)
}

func (s *CategoryService) GetArticlesByCategory(ctx context.Context, categoryID string) ([]models.Article, error) {
	id, err := uuid.Parse(categoryID)
	if err != nil {
		return nil, err
	}
	return s.articles.GetByCategoryID(ctx, id)
}
