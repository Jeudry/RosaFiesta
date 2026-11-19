package app

import (
	"context"
	"strings"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
)

func pushTokenMapKey(userID uuid.UUID, token string) string {
	return userID.String() + "|" + strings.TrimSpace(token)
}

func (s *Service) RegisterPushToken(
	ctx context.Context,
	userID uuid.UUID,
	platform string,
	token string,
	appVariant string,
) (domain.DevicePushToken, error) {
	normalizedPlatform, err := domain.NormalizePushPlatform(platform)
	if err != nil {
		return domain.DevicePushToken{}, err
	}
	cleanToken := strings.TrimSpace(token)
	if cleanToken == "" {
		return domain.DevicePushToken{}, domain.ErrInvalidInput
	}
	normalizedVariant, err := domain.NormalizeAppVariant(appVariant)
	if err != nil {
		return domain.DevicePushToken{}, err
	}
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()
	if _, exists := s.users[userID]; !exists {
		return domain.DevicePushToken{}, domain.ErrUserNotFound
	}
	record := domain.DevicePushToken{
		ID:         uuid.New(),
		UserID:     userID,
		Platform:   normalizedPlatform,
		Token:      cleanToken,
		AppVariant: normalizedVariant,
		Enabled:    true,
		UpdatedAt:  s.clock.Now(),
	}
	if s.pushTokens == nil {
		s.pushTokens = make(map[string]domain.DevicePushToken)
	}
	s.pushTokens[pushTokenMapKey(userID, cleanToken)] = record
	s.persistPushToken(ctx, record)
	return record, nil
}

func (s *Service) UnregisterPushToken(ctx context.Context, userID uuid.UUID, token string) error {
	now := s.clock.Now()
	cleanToken := strings.TrimSpace(token)
	s.mu.Lock()
	defer s.mu.Unlock()
	if cleanToken == "" {
		s.disableAllUserPushTokensLocked(userID)
		if s.hasEnabledStoreLocked() {
			return s.store.DisableAllUserPushTokens(ctx, userID, now)
		}
		return nil
	}
	key := pushTokenMapKey(userID, cleanToken)
	if existing, ok := s.pushTokens[key]; ok {
		existing.Enabled = false
		existing.UpdatedAt = now
		s.pushTokens[key] = existing
	}
	if s.hasEnabledStoreLocked() {
		return s.store.DisableDevicePushToken(ctx, userID, cleanToken, now)
	}
	return nil
}

func (s *Service) ActivePushTokensForUser(ctx context.Context, userID uuid.UUID) []domain.DevicePushToken {
	if userID == uuid.Nil {
		return nil
	}
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store != nil {
		if store.Enabled() {
			if rows, err := store.ListActivePushTokensForUser(ctx, userID); err == nil {
				return rows
			}
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	var out []domain.DevicePushToken
	for _, entry := range s.pushTokens {
		matchesUser := entry.UserID == userID
		isActive := entry.Enabled
		if matchesUser {
			if isActive {
				out = append(out, entry)
			}
		}
	}
	return out
}

func (s *Service) ActiveAdminPushTokens(ctx context.Context) []domain.DevicePushToken {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store != nil {
		if store.Enabled() {
			if rows, err := store.ListActiveAdminPushTokens(ctx); err == nil {
				return rows
			}
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	var out []domain.DevicePushToken
	for _, entry := range s.pushTokens {
		if !entry.Enabled {
			continue
		}
		user, ok := s.users[entry.UserID]
		if !ok {
			continue
		}
		if user.CanAccessView(domain.ViewAdmin) {
			out = append(out, entry)
		}
	}
	return out
}

func (s *Service) disableAllUserPushTokensLocked(userID uuid.UUID) {
	now := s.clock.Now()
	for key, entry := range s.pushTokens {
		if entry.UserID == userID {
			entry.Enabled = false
			entry.UpdatedAt = now
			s.pushTokens[key] = entry
		}
	}
}

func (s *Service) persistPushToken(ctx context.Context, record domain.DevicePushToken) {
	if s.hasEnabledStoreLocked() {
		_ = s.store.UpsertDevicePushToken(ctx, record)
	}
}

func (s *Service) hasEnabledStoreLocked() bool {
	if s.store == nil {
		return false
	}
	return s.store.Enabled()
}
