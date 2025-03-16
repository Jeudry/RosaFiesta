package store

import (
	"Backend/internal/store/models"
	"context"
	"crypto/sha256"
	"database/sql"
	"encoding/hex"
	"errors"
	"time"
)

var (
	ErrDuplicateEmail    = errors.New("an user with that email already exists")
	ErrDuplicateUserName = errors.New("an user with that username already exists")
)

type UsersStore struct {
	db *sql.DB
}

func (s *UsersStore) Create(ctx context.Context, tx *sql.Tx, user *models.User) error {
	query := `INSERT INTO users (user_name, first_name, last_name, email, password, role_id) VALUES ($1, $2, $3, $4, $5, (select id FROM roles WHERE name = $6)) RETURNING id, created_at`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	role := user.Role.Name
	if role == "" {
		role = "user"
	}

	err := s.db.QueryRowContext(
		ctx, query, user.UserName, user.FirstName, user.LastName, user.Email, user.Password.Hash, role,
	).Scan(&user.ID, &user.CreatedAt)

	if err != nil {
		switch {
		case err.Error() == `pq: duplicate key value violates unique constraint "users_email_key"`:
			return ErrDuplicateEmail
		case err.Error() == `pq: duplicate key value violates unique constraint "users_userName_key"`:
			return ErrDuplicateUserName
		default:
			return err
		}
	}

	return err
}

func (s *UsersStore) RetrieveById(ctx context.Context, id int64) (*models.User, error) {
	query := `SELECT users.id, user_name, first_name, last_name, email, password, created_at, roles.* FROM users JOIN roles ON (users.role_id = roles.id) WHERE users.id = $1`

	var user models.User

	err := s.db.QueryRowContext(ctx, query, id).Scan(&user.ID, &user.UserName, &user.FirstName, &user.LastName, &user.Email, &user.Password.Hash, &user.CreatedAt, &user.Role.ID, &user.Role.Name, &user.Role.Level, &user.Role.Description)

	if err != nil {
		switch {
		case errors.Is(err, sql.ErrNoRows):
			return nil, ErrNotFound
		default:
			return nil, err
		}
	}

	return &user, nil
}

func (s *UsersStore) CreateAndInvite(ctx context.Context, user *models.User, token string, invitationExp time.Duration) error {
	return withTx(s.db, ctx, func(tx *sql.Tx) error {
		if err := s.Create(ctx, tx, user); err != nil {
			return err
		}

		if err := s.createUserInvitation(ctx, tx, token, invitationExp, user.ID); err != nil {
			return err
		}

		return nil
	})
}

func (s *UsersStore) Delete(ctx context.Context, id int64) error {
	return withTx(s.db, ctx, func(tx *sql.Tx) error {
		if err := s.delete(ctx, tx, id); err != nil {
			return err
		}

		if err := s.deleteUserInvitations(ctx, tx, id); err != nil {
			return err
		}

		return nil
	})
}

func (s *UsersStore) delete(ctx context.Context, tx *sql.Tx, id int64) error {
	query := `DELETE FROM users WHERE id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)

	defer cancel()

	_, err := tx.ExecContext(ctx, query, id)

	if err != nil {
		return err
	}

	return nil
}

func (s *UsersStore) createUserInvitation(ctx context.Context, tx *sql.Tx, token string, invitationExp time.Duration, userID int64) error {
	query := `INSERT INTO user_invitations (token, user_id, expiry) VALUES ($1, $2, $3)`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)

	defer cancel()

	_, err := tx.ExecContext(ctx, query, token, userID, time.Now().Add(invitationExp))

	if err != nil {
		return err
	}

	return nil
}

func (s *UsersStore) Activate(ctx context.Context, token string) error {
	return withTx(s.db, ctx, func(tx *sql.Tx) error {
		user, err := s.getUserFromInvitation(ctx, tx, token)
		if err != nil {
			return err
		}

		user.IsActive = true

		if err := s.update(ctx, tx, user); err != nil {
			return err
		}

		if err := s.deleteUserInvitations(ctx, tx, user.ID); err != nil {
			return err
		}

		return nil
	})
}

func (s *UsersStore) getUserFromInvitation(ctx context.Context, tx *sql.Tx, token string) (*models.User, error) {
	query := `SELECT u.id, u.user_name, u.first_name, u.last_name, u.email, u.password, u.created_at, u.activated FROM users u 
	JOIN user_invitations ui on u.ID = ui.user_id
	WHERE ui.token = $1 AND ui.expiry > $2`

	hash := sha256.Sum256([]byte(token))
	hashToken := hex.EncodeToString(hash[:])

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)

	defer cancel()

	user := &models.User{}
	err := tx.QueryRowContext(ctx, query, hashToken, time.Now()).Scan(
		&user.ID, &user.UserName, &user.FirstName, &user.LastName, &user.Email, &user.Password.Hash, &user.CreatedAt, &user.IsActive,
	)

	if err != nil {
		switch err {
		case sql.ErrNoRows:
			return nil, ErrNotFound
		default:
			return nil, err
		}
	}

	return user, nil
}

func (s *UsersStore) update(ctx context.Context, tx *sql.Tx, user *models.User) error {
	query := `UPDATE users SET user_name = $1, email = $2, activated=$3 WHERE id = $4`

	_, err := tx.ExecContext(ctx, query, user.UserName, user.Email, user.IsActive, user.ID)

	if err != nil {
		return err
	}

	return nil
}

func (s *UsersStore) deleteUserInvitations(ctx context.Context, tx *sql.Tx, id int64) error {
	query := `DELETE FROM user_invitations WHERE user_id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)

	defer cancel()

	_, err := tx.ExecContext(ctx, query, id)

	if err != nil {
		return err
	}

	return nil
}

func (s *UsersStore) GetByEmail(ctx context.Context, email string) (*models.User, error) {
	query := `SELECT id, user_name, first_name, last_name, email, password, created_at FROM users WHERE email = $1 AND activated = true`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	user := &models.User{}

	err := s.db.QueryRowContext(ctx, query, email).Scan(&user.ID, &user.UserName, &user.FirstName, &user.LastName, &user.Email, &user.Password.Hash, &user.CreatedAt)

	if err != nil {
		switch {
		case errors.Is(err, sql.ErrNoRows):
			return nil, ErrNotFound
		default:
			return nil, err
		}
	}

	return user, nil
}
