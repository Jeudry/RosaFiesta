package store

import (
	"context"
	"database/sql"
	"fmt"

	"Backend/internal/store/models"
)

type CompanyReviewsStore struct {
	db *sql.DB
}

func (s *CompanyReviewsStore) Create(ctx context.Context, review *models.CompanyReview) error {
	query := `
		INSERT INTO company_reviews (user_id, rating, comment, source, created, updated)
		VALUES ($1, $2, $3, $4, NOW(), NOW())
		RETURNING id, created, updated`

	err := s.db.QueryRowContext(ctx, query,
		review.UserID,
		review.Rating,
		review.Comment,
		review.Source,
	).Scan(&review.ID, &review.Created, &review.Updated)
	if err != nil {
		return fmt.Errorf("failed to create company review: %w", err)
	}
	return nil
}

func (s *CompanyReviewsStore) GetAll(ctx context.Context) ([]models.CompanyReview, error) {
	query := `
		SELECT cr.id, cr.user_id, cr.rating, cr.comment, cr.source, cr.created, cr.updated,
		       u.user_name, u.avatar
		FROM company_reviews cr
		JOIN users u ON cr.user_id = u.id
		ORDER BY cr.created DESC`

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var reviews []models.CompanyReview
	for rows.Next() {
		var r models.CompanyReview
		var user models.User
		if err := rows.Scan(
			&r.ID, &r.UserID, &r.Rating, &r.Comment, &r.Source, &r.Created, &r.Updated,
			&user.UserName, &user.Avatar,
		); err != nil {
			return nil, err
		}
		r.User = &user
		reviews = append(reviews, r)
	}
	return reviews, nil
}

func (s *CompanyReviewsStore) GetSummary(ctx context.Context) (float64, int, error) {
	query := `
		SELECT COALESCE(AVG(rating), 0)::float, COUNT(*)
		FROM company_reviews`

	var avg sql.NullFloat64
	var count int
	if err := s.db.QueryRowContext(ctx, query).Scan(&avg, &count); err != nil {
		return 0, 0, err
	}
	return avg.Float64, count, nil
}
