package services

import (
	"context"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

type PostService struct {
	posts    store.PostRepository
	comments store.CommentRepository
	logger   *zap.SugaredLogger
}

func NewPostService(posts store.PostRepository, comments store.CommentRepository, logger *zap.SugaredLogger) *PostService {
	return &PostService{
		posts:    posts,
		comments: comments,
		logger:   logger,
	}
}

func (s *PostService) CreatePost(ctx context.Context, post *models.Post) error {
	return s.posts.Create(ctx, post)
}

func (s *PostService) UpdatePost(ctx context.Context, post *models.Post) error {
	return s.posts.Update(ctx, post)
}

func (s *PostService) DeletePost(ctx context.Context, postID string) error {
	id, err := uuid.Parse(postID)
	if err != nil {
		return err
	}
	return s.posts.Delete(ctx, id)
}

func (s *PostService) CreateComment(ctx context.Context, comment *models.Comment) error {
	return s.comments.CreatePostComment(ctx, comment)
}

func (s *PostService) GetComments(ctx context.Context, postID string) ([]models.Comment, error) {
	id, err := uuid.Parse(postID)
	if err != nil {
		return nil, err
	}
	return s.comments.RetrieveCommentsByPostId(ctx, id)
}

func (s *PostService) GetPost(ctx context.Context, postID string) (*models.Post, error) {
	id, err := uuid.Parse(postID)
	if err != nil {
		return nil, err
	}
	return s.posts.RetrieveById(ctx, id)
}
