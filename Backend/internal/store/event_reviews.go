package store

import (
	"context"
	"database/sql"
	"fmt"

	"Backend/internal/store/models"

	"github.com/google/uuid"
)

type EventReviewsStore struct {
	db *sql.DB
}

func (s *EventReviewsStore) Create(ctx context.Context, review *models.EventReview) error {
	query := `
		INSERT INTO event_reviews (user_id, event_id, rating, comment, created_at, updated_at)
		VALUES ($1, $2, $3, $4, NOW(), NOW())
		RETURNING id, created_at, updated_at`

	err := s.db.QueryRowContext(ctx, query,
		review.UserID,
		review.EventID,
		review.Rating,
		review.Comment,
	).Scan(&review.ID, &review.Created, &review.Updated)
	if err != nil {
		return fmt.Errorf("failed to create event review: %w", err)
	}

	return nil
}

func (s *EventReviewsStore) GetByEventID(ctx context.Context, eventID uuid.UUID) ([]models.EventReview, error) {
	query := `
		SELECT r.id, r.user_id, r.event_id, r.rating, r.comment, r.created_at, r.updated_at,
		       u.user_name, u.avatar
		FROM event_reviews r
		JOIN users u ON r.user_id = u.id
		WHERE r.event_id = $1
		ORDER BY r.created_at DESC`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var reviews []models.EventReview
	for rows.Next() {
		var r models.EventReview
		var user models.User
		if err := rows.Scan(
			&r.ID, &r.UserID, &r.EventID, &r.Rating, &r.Comment, &r.Created, &r.Updated,
			&user.UserName, &user.Avatar,
		); err != nil {
			return nil, err
		}
		r.User = &user
		reviews = append(reviews, r)
	}

	// Always return an empty slice instead of nil to avoid null arrays in JSON responses
	if len(reviews) == 0 {
		return []models.EventReview{}, nil
	}
	return reviews, nil
}

func (s *EventReviewsStore) GetSummary(ctx context.Context, eventID uuid.UUID) (float64, int, error) {
	query := `
		SELECT COALESCE(AVG(rating), 0), COUNT(*)
		FROM event_reviews
		WHERE event_id = $1`

	var avg sql.NullFloat64
	var count int
	if err := s.db.QueryRowContext(ctx, query, eventID).Scan(&avg, &count); err != nil {
		return 0, 0, err
	}

	return avg.Float64, count, nil
}

func (s *EventReviewsStore) AddPhoto(ctx context.Context, reviewID uuid.UUID, photoURL string, caption string, sortOrder int) error {
	query := `
		INSERT INTO review_photos (review_id, photo_url, caption, sort_order)
		VALUES ($1, $2, $3, $4)`

	_, err := s.db.ExecContext(ctx, query, reviewID, photoURL, caption, sortOrder)
	return err
}

func (s *EventReviewsStore) GetPhotos(ctx context.Context, reviewID uuid.UUID) ([]models.ReviewPhoto, error) {
	query := `
		SELECT id, review_id, photo_url, caption, sort_order
		FROM review_photos
		WHERE review_id = $1
		ORDER BY sort_order ASC`

	rows, err := s.db.QueryContext(ctx, query, reviewID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var photos []models.ReviewPhoto
	for rows.Next() {
		var p models.ReviewPhoto
		if err := rows.Scan(&p.ID, &p.ReviewID, &p.PhotoURL, &p.Caption, &p.SortOrder); err != nil {
			return nil, err
		}
		photos = append(photos, p)
	}

	if len(photos) == 0 {
		return []models.ReviewPhoto{}, nil
	}
	return photos, nil
}
