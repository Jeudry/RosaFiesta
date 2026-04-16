package store

import (
	"context"
	"crypto/sha256"
	"database/sql"
	"encoding/hex"
	"errors"
	"time"

	"Backend/internal/store/models"

	"github.com/google/uuid"
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

func (s *UsersStore) RetrieveById(ctx context.Context, id uuid.UUID) (*models.User, error) {
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

func (s *UsersStore) Delete(ctx context.Context, id uuid.UUID) error {
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

func (s *UsersStore) delete(ctx context.Context, tx *sql.Tx, id uuid.UUID) error {
	query := `DELETE FROM users WHERE id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)

	defer cancel()

	_, err := tx.ExecContext(ctx, query, id)
	if err != nil {
		return err
	}

	return nil
}

func (s *UsersStore) createUserInvitation(ctx context.Context, tx *sql.Tx, token string, invitationExp time.Duration, userID uuid.UUID) error {
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

func (s *UsersStore) deleteUserInvitations(ctx context.Context, tx *sql.Tx, id uuid.UUID) error {
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
	query := `SELECT id, user_name, first_name, last_name, email, password, created_at, activated FROM users WHERE email = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	user := &models.User{}

	err := s.db.QueryRowContext(ctx, query, email).Scan(&user.ID, &user.UserName, &user.FirstName, &user.LastName, &user.Email, &user.Password.Hash, &user.CreatedAt, &user.IsActive)
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

// UpdateFCMToken updates the FCM token for a specific user.
func (s *UsersStore) UpdateFCMToken(ctx context.Context, userID uuid.UUID, token string) error {
	query := `UPDATE users SET fcm_token = $1 WHERE id = $2`
	_, err := s.db.ExecContext(ctx, query, token, userID)
	return err
}

// GetOrganizersFCMTokens fetches FCM tokens for all users with admin or moderator roles.
func (s *UsersStore) GetOrganizersFCMTokens(ctx context.Context) ([]string, error) {
	query := `
		SELECT fcm_token
		FROM users u
		JOIN roles r ON u.role_id = r.id
		WHERE r.name IN ('admin', 'moderator')
		  AND u.fcm_token IS NOT NULL
		  AND u.fcm_token <> ''
	`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var tokens []string
	for rows.Next() {
		var token string
		if err := rows.Scan(&token); err != nil {
			return nil, err
		}
		tokens = append(tokens, token)
	}

	return tokens, nil
}

// UpdatePhoneNumber updates the phone number for a specific user.
func (s *UsersStore) UpdatePhoneNumber(ctx context.Context, userID uuid.UUID, phone string) error {
	query := `UPDATE users SET phonenumber = $1 WHERE id = $2`
	_, err := s.db.ExecContext(ctx, query, phone, userID)
	return err
}

// CreatePasswordResetToken creates a password reset token for the given user.
func (s *UsersStore) CreatePasswordResetToken(ctx context.Context, userID uuid.UUID, token string, exp time.Duration) error {
	query := `INSERT INTO password_reset_tokens (token_hash, user_id, expiry) VALUES ($1, $2, $3)`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	hash := sha256.Sum256([]byte(token))
	hashToken := hex.EncodeToString(hash[:])

	_, err := s.db.ExecContext(ctx, query, hashToken, userID, time.Now().Add(exp))
	return err
}

// GetUserByResetToken looks up a user by a valid (non-expired) password reset token.
func (s *UsersStore) GetUserByResetToken(ctx context.Context, token string) (*models.User, error) {
	query := `SELECT u.id, u.user_name, u.first_name, u.last_name, u.email, u.password, u.created_at, u.activated
		FROM users u
		JOIN password_reset_tokens prt ON u.id = prt.user_id
		WHERE prt.token_hash = $1 AND prt.expiry > $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	hash := sha256.Sum256([]byte(token))
	hashToken := hex.EncodeToString(hash[:])

	user := &models.User{}
	err := s.db.QueryRowContext(ctx, query, hashToken, time.Now()).Scan(
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

// DeletePasswordResetToken deletes all password reset tokens for a given user.
func (s *UsersStore) DeletePasswordResetToken(ctx context.Context, userID uuid.UUID) error {
	query := `DELETE FROM password_reset_tokens WHERE user_id = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, userID)
	return err
}

// DeletePasswordResetTokenByToken deletes a password reset token by its token value.
func (s *UsersStore) DeletePasswordResetTokenByToken(ctx context.Context, token string) error {
	query := `DELETE FROM password_reset_tokens WHERE token_hash = $1`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	hash := sha256.Sum256([]byte(token))
	hashToken := hex.EncodeToString(hash[:])

	_, err := s.db.ExecContext(ctx, query, hashToken)
	return err
}

// UpdatePassword updates the password for a given user. The password should already be bcrypt hashed.
func (s *UsersStore) UpdatePassword(ctx context.Context, userID uuid.UUID, passwordHash []byte) error {
	query := `UPDATE users SET password = $1 WHERE id = $2`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	_, err := s.db.ExecContext(ctx, query, passwordHash, userID)
	return err
}

// GetAllClientsForExport returns all users with client role for export
func (s *UsersStore) GetAllClientsForExport(ctx context.Context) ([]models.ClientExport, error) {
	query := `
		SELECT u.id, u.first_name, u.last_name, u.email, COALESCE(u.phone_number, ''),
		       COALESCE(u.created_at, NOW()), u.is_active,
		       COUNT(e.id) as events_count,
		       COALESCE(SUM(e.total_quote), 0) as total_spent
		FROM users u
		LEFT JOIN events e ON e.user_id = u.id AND e.status = 'paid'
		WHERE u.role_id = (SELECT id FROM roles WHERE name = 'client')
		GROUP BY u.id
		ORDER BY u.created_at DESC`

	ctx, cancel := context.WithTimeout(ctx, QueryTimeoutDuration)
	defer cancel()

	rows, err := s.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var clients []models.ClientExport
	for rows.Next() {
		var c models.ClientExport
		if err := rows.Scan(&c.ID, &c.FirstName, &c.LastName, &c.Email, &c.Phone, &c.CreatedAt, &c.IsActive, &c.EventsCount, &c.TotalSpent); err != nil {
			return nil, err
		}
		clients = append(clients, c)
	}
	return clients, nil
}
