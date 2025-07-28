package store

import (
	"Backend/internal/store/models"
	"context"
	"database/sql"
	"errors"
	"github.com/google/uuid"
	"time"
)

type RefreshTokensStore struct {
	db *sql.DB
}

func (s *RefreshTokensStore) Create(ctx context.Context, token *models.RefreshToken) error {
	query := `INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES ($1, $2, $3) RETURNING id, created_at, updated_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	err := s.db.QueryRowContext(
		ctx, query, token.UserID, token.Token, token.ExpiresAt,
	).Scan(&token.ID, &token.Created, &token.Updated)

	if err != nil {
		return err
	}

	return nil
}

func (s *RefreshTokensStore) GetByToken(ctx context.Context, tokenStr string) (*models.RefreshToken, error) {
	query := `SELECT id, user_id, token, expires_at, created_at, updated_at FROM refresh_tokens WHERE token = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	var token models.RefreshToken
	var created, updated time.Time

	err := s.db.QueryRowContext(ctx, query, tokenStr).Scan(
		&token.ID, &token.UserID, &token.Token, &token.ExpiresAt, &created, &updated,
	)

	if err != nil {
		switch {
		case errors.Is(err, sql.ErrNoRows):
			return nil, ErrNotFound
		default:
			return nil, err
		}
	}

	token.Created = &created
	token.Updated = &updated

	// Check if token is expired
	if token.ExpiresAt.Before(time.Now()) {
		// Delete the expired token
		_ = s.Delete(ctx, tokenStr)
		return nil, errors.New("refresh token expired")
	}

	return &token, nil
}

func (s *RefreshTokensStore) Delete(ctx context.Context, tokenStr string) error {
	query := `DELETE FROM refresh_tokens WHERE token = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, tokenStr)
	return err
}

func (s *RefreshTokensStore) DeleteAllForUser(ctx context.Context, userID uuid.UUID) error {
	query := `DELETE FROM refresh_tokens WHERE user_id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, userID)
	return err
}
