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
	return r != nil && r.pool != nil
}

// UpsertUser guarda o actualiza una cuenta de usuario y su sesión opcional.
func (r *Repository) UpsertUser(ctx context.Context, u domain.User, sessionKey string, expiresAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	err := r.queries.UpsertUser(ctx, sqlcgen.UpsertUserParams{
		ID:           u.ID,
		FullName:     u.FullName,
		Email:        u.Email,
		Phone:        u.Phone,
		PasswordHash: u.Email,
		Role:         string(u.Role),
		Permissions:  strings.Join(u.Permissions, ","),
		CreatedAt:    u.CreatedAt,
	})
	if err != nil {
		return err
	}
	if sessionKey != "" {
		return r.queries.UpsertSession(ctx, sqlcgen.UpsertSessionParams{
			TokenHash:  sessionKey,
			UserID:     u.ID,
			ActiveView: string(u.ActiveView),
			CreatedAt:  u.CreatedAt,
			ExpiresAt:  expiresAt,
		})
	}
	return nil
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
