package app

import (
	"context"
	"errors"
	"strings"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
)

var (
	// ErrBootstrapAdminMissing indicates production startup has no existing admin and no bootstrap credentials.
	ErrBootstrapAdminMissing = errors.New("production startup has no active admin user and no BOOTSTRAP_ADMIN_EMAIL/PASSWORD")
	// ErrBootstrapAdminInvalid indicates provided bootstrap admin credentials failed validation.
	ErrBootstrapAdminInvalid = errors.New("invalid bootstrap admin credentials")
)

type adminCounter interface {
	CountAdminUsers(ctx context.Context) (int, error)
}

func defaultAdminPermissions() []string {
	return []string{
		"eventos", "decoracion", "tareas", "inventario",
		"pagos", "reembolsos", "reportes", "ajustes",
	}
}

// EnsureBootstrapAdmin idempotently creates the initial admin user when none exists yet.
// Returns (true, nil) when a new admin was created, (false, nil) when an admin already exists,
// or an error when bootstrap credentials are missing or invalid.
func (s *Service) EnsureBootstrapAdmin(
	ctx context.Context,
	adminEmail string,
	adminPassword string,
	adminName string,
) (bool, error) {
	s.syncUsersFromStore(ctx)
	hasAdmin, err := s.hasActiveAdmin(ctx)
	if err != nil {
		return false, err
	}
	if hasAdmin {
		return false, nil
	}
	cleanEmail := strings.TrimSpace(adminEmail)
	cleanPass := strings.TrimSpace(adminPassword)
	if cleanEmail == "" {
		return false, ErrBootstrapAdminMissing
	}
	if cleanPass == "" {
		return false, ErrBootstrapAdminMissing
	}
	normalizedEmail, err := domain.NormalizeEmail(cleanEmail)
	if err != nil {
		return false, ErrBootstrapAdminInvalid
	}
	if len(cleanPass) < 8 {
		return false, ErrBootstrapAdminInvalid
	}
	hash, err := domain.HashPassword(cleanPass)
	if err != nil {
		return false, err
	}
	displayName := strings.TrimSpace(adminName)
	if displayName == "" {
		displayName = "Administración RosaFiesta"
	}
	now := s.clock.Now().UTC()
	return s.persistBootstrapAdmin(ctx, normalizedEmail, hash, displayName, now)
}

func (s *Service) hasActiveAdmin(ctx context.Context) (bool, error) {
	s.mu.RLock()
	store := s.store
	memHasAdmin := hasActiveAdminInMap(s.users)
	s.mu.RUnlock()
	if memHasAdmin {
		return true, nil
	}
	if !isIdentityStoreEnabled(store) {
		return false, nil
	}
	counter, ok := store.(adminCounter)
	if !ok {
		return false, nil
	}
	count, err := counter.CountAdminUsers(ctx)
	if err != nil {
		return false, err
	}
	return count > 0, nil
}

func hasActiveAdminInMap(users map[uuid.UUID]domain.User) bool {
	for _, existing := range users {
		if existing.Role == domain.RoleAdmin {
			if existing.Active {
				return true
			}
		}
	}
	return false
}

func (s *Service) persistBootstrapAdmin(
	ctx context.Context,
	email string,
	passwordHash string,
	fullName string,
	now time.Time,
) (bool, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if hasActiveAdminInMap(s.users) {
		return false, nil
	}
	userID := uuid.New()
	perms := defaultAdminPermissions()
	user := domain.User{
		ID:           userID,
		FullName:     fullName,
		Email:        email,
		PasswordHash: passwordHash,
		Role:         domain.RoleAdmin,
		ActiveView:   domain.ViewAdmin,
		Permissions:  perms,
		Preferences:  domain.DefaultUserPreferences(),
		Active:       true,
		CreatedAt:    now,
	}
	user.Username = resolveRegisterUsername("", email)
	if s.users == nil {
		s.users = make(map[uuid.UUID]domain.User)
	}
	s.users[userID] = user
	staff := domain.StaffRecord{
		ID:          "staff-" + userID.String()[:8],
		UserID:      userID,
		FullName:    fullName,
		Email:       email,
		RoleLabel:   "Administrador principal",
		Role:        "admin",
		Permissions: perms,
		Active:      true,
		CreatedAt:   now,
	}
	s.staff = append(s.staff, staff)
	if isIdentityStoreEnabled(s.store) {
		if err := s.store.UpsertUser(ctx, user, "", now); err != nil {
			return false, err
		}
		if err := s.store.UpsertStaffMember(ctx, staff); err != nil {
			return false, err
		}
	}
	return true, nil
}

func isIdentityStoreEnabled(store Store) bool {
	if store == nil {
		return false
	}
	return store.Enabled()
}

// ShouldBootstrapAdmin returns true when running in production environment.
func ShouldBootstrapAdmin(env config.Environment) bool {
	return env == config.EnvProduction
}
