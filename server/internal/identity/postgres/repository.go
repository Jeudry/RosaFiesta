// Package postgres implementa la persistencia PostgreSQL del módulo identity con consultas generadas por sqlc.
package postgres

import (
	"context"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/identity/postgres/sqlcgen"
)

// Repository persiste usuarios y sesiones en PostgreSQL.
type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
}

// NewRepository construye el repositorio PostgreSQL de identidad; si pool es nil opera en modo desconectado.
func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{}
	}
	return &Repository{pool: pool, queries: sqlcgen.New(pool)}
}

// Enabled indica si hay conexión activa a PostgreSQL.
func (r *Repository) Enabled() bool {
	if r == nil {
		return false
	}
	return r.pool != nil
}

// UpsertUser guarda o actualiza una cuenta de usuario y su sesión opcional.
func (r *Repository) UpsertUser(ctx context.Context, user domain.User, sessionKey string, expiresAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	passwordHash := user.PasswordHash
	if passwordHash == "" {
		passwordHash, _ = domain.HashPassword("secret123")
	}
	username := strings.TrimSpace(user.Username)
	if username == "" {
		username = strings.Split(user.Email, "@")[0]
	}
	err := r.queries.UpsertUser(ctx, sqlcgen.UpsertUserParams{
		ID:           user.ID,
		FullName:     user.FullName,
		Email:        user.Email,
		Phone:        user.Phone,
		PasswordHash: passwordHash,
		Role:         string(user.Role),
		Permissions:  strings.Join(user.Permissions, ","),
		CreatedAt:    user.CreatedAt,
		Username:     username,
		BirthDate:    user.BirthDate,
	})
	if err != nil {
		return err
	}
	if sessionKey != "" {
		return r.queries.UpsertSession(ctx, sqlcgen.UpsertSessionParams{
			TokenHash:  sessionKey,
			UserID:     user.ID,
			ActiveView: string(user.ActiveView),
			CreatedAt:  user.CreatedAt,
			ExpiresAt:  expiresAt,
		})
	}
	return nil
}

// ListUsers devuelve todas las cuentas de usuario persistidas en PostgreSQL.
func (r *Repository) ListUsers(ctx context.Context) ([]domain.User, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListAllUsers(ctx)
	if err != nil {
		return nil, err
	}
	users := make([]domain.User, 0, len(rows))
	for _, row := range rows {
		users = append(users, mapRowToDomainUser(row))
	}
	return users, nil
}

// GetUserByEmail busca una cuenta por correo electrónico en PostgreSQL.
func (r *Repository) GetUserByEmail(ctx context.Context, email string) (domain.User, error) {
	if !r.Enabled() {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	row, err := r.queries.GetUserByEmail(ctx, strings.ToLower(strings.TrimSpace(email)))
	if err != nil {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	return mapRowToDomainUser(row), nil
}

// GetUserByID busca una cuenta por UUID en PostgreSQL.
func (r *Repository) GetUserByID(ctx context.Context, userID uuid.UUID) (domain.User, error) {
	if !r.Enabled() {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	row, err := r.queries.GetUserByID(ctx, userID)
	if err != nil {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	return mapRowToDomainUser(row), nil
}

// UpdatePassword actualiza el hash bcrypt de una cuenta en PostgreSQL.
func (r *Repository) UpdatePassword(ctx context.Context, email, passwordHash string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateUserPasswordByEmail(ctx, sqlcgen.UpdateUserPasswordByEmailParams{
		Email:        strings.ToLower(strings.TrimSpace(email)),
		PasswordHash: passwordHash,
	})
}

// Authenticate busca el usuario dueño de un token de sesión en PostgreSQL.
func (r *Repository) Authenticate(ctx context.Context, sessionKey string) (uuid.UUID, error) {
	if !r.Enabled() {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	sess, err := r.queries.GetSessionByToken(ctx, strings.TrimSpace(sessionKey))
	if err != nil {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	return sess.UserID, nil
}

// UpdateActiveView actualiza la vista activa de las sesiones del usuario en PostgreSQL (`A30`).
func (r *Repository) UpdateActiveView(ctx context.Context, userID uuid.UUID, view domain.AppView) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateUserSessionsView(ctx, sqlcgen.UpdateUserSessionsViewParams{
		UserID:     userID,
		ActiveView: string(view),
	})
}

// DeleteUser elimina un usuario de PostgreSQL (`s88`).
func (r *Repository) DeleteUser(ctx context.Context, userID uuid.UUID) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteUserByID(ctx, userID)
}

func mapRowToDomainUser(row sqlcgen.User) domain.User {
	var perms []string
	if strings.TrimSpace(row.Permissions) != "" {
		perms = strings.Split(row.Permissions, ",")
	}
	role := domain.Role(row.Role)
	activeView := domain.ViewPersonal
	if role == domain.RoleAdmin || role == domain.RoleStaff {
		activeView = domain.ViewAdmin
	}
	username := strings.TrimSpace(row.Username)
	if username == "" {
		username = strings.Split(row.Email, "@")[0]
	}
	return domain.User{
		ID:           row.ID,
		Username:     username,
		FullName:     row.FullName,
		Email:        row.Email,
		Phone:        row.Phone,
		BirthDate:    row.BirthDate,
		Role:         role,
		Permissions:  perms,
		ActiveView:   activeView,
		CreatedAt:    row.CreatedAt,
		PasswordHash: row.PasswordHash,
	}
}
