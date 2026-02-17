package store

import (
	"context"
	"database/sql"
	"fmt"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type ReviewsStore struct {
	db *sql.DB
}

func (s *ReviewsStore) Create(ctx context.Context, review *models.Review) error {
	query := `
		INSERT INTO reviews (user_id, article_id, rating, comment, created, updated)
		VALUES ($1, $2, $3, $4, NOW(), NOW())
		RETURNING id, created, updated`

	err := s.db.QueryRowContext(ctx, query,
		review.UserID,
		review.ArticleID,
		review.Rating,
		review.Comment,
	).Scan(&review.ID, &review.Created, &review.Updated)
	if err != nil {
		return fmt.Errorf("failed to create review: %w", err)
	}

	return nil
}

func (s *ReviewsStore) GetByArticleID(ctx context.Context, articleID uuid.UUID) ([]models.Review, error) {
	query := `
		SELECT r.id, r.user_id, r.article_id, r.rating, r.comment, r.created, r.updated,
		       u.user_name, u.avatar
		FROM reviews r
		JOIN users u ON r.user_id = u.id
		WHERE r.article_id = $1
		ORDER BY r.created DESC`

	rows, err := s.db.QueryContext(ctx, query, articleID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var reviews []models.Review
	for rows.Next() {
		var r models.Review
		var user models.User
		if err := rows.Scan(
			&r.ID, &r.UserID, &r.ArticleID, &r.Rating, &r.Comment, &r.Created, &r.Updated,
			&user.UserName, &user.Avatar,
		); err != nil {
			return nil, err
		}
		r.User = &user
		reviews = append(reviews, r)
	}

	return reviews, nil
}

func (s *ReviewsStore) GetSummary(ctx context.Context, articleID uuid.UUID) (float64, int, error) {
	query := `
		SELECT COALESCE(AVG(rating), 0), COUNT(*)
		FROM reviews
		WHERE article_id = $1`

	var avg sql.NullFloat64
	var count int
	if err := s.db.QueryRowContext(ctx, query, articleID).Scan(&avg, &count); err != nil {
		return 0, 0, err
	}

	return avg.Float64, count, nil
}
