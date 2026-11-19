package app

import (
	"context"
	"strings"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
)

const (
	defaultAccessTokenTTL         = 7 * 24 * time.Hour
	defaultRefreshTokenTTL        = 30 * 24 * time.Hour
	revokedTokenRetentionDuration = 7 * 24 * time.Hour
)

type AuthSession struct {
	User         domain.User
	AccessToken  string
	RefreshToken string
	ExpiresIn    int
}

func (s *Service) LoginWithSession(ctx context.Context, identifier, password string) (AuthSession, error) {
	cleanPass := strings.TrimSpace(password)
	trimmedIdentifier := strings.TrimSpace(identifier)
	cleanIdentifier := strings.ToLower(trimmedIdentifier)
	if cleanIdentifier == "" {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	if cleanPass == "" {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	s.syncUsersFromStore(ctx)

	s.mu.Lock()
	defer s.mu.Unlock()

	user, found := s.findUserLocked(cleanIdentifier)
	if !found {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	validPassword := domain.VerifyPassword(user.PasswordHash, cleanPass)
	if !validPassword {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	return s.issueSessionBundleLocked(ctx, user, uuid.Nil), nil
}

func (s *Service) RegisterWithSession(ctx context.Context, cmd RegisterCommand) (AuthSession, error) {
	normalizedEmail, err := domain.NormalizeEmail(cmd.Email)
	if err != nil {
		return AuthSession{}, domain.ErrInvalidInput
	}
	fullName := strings.TrimSpace(cmd.FullName)
	if fullName == "" {
		return AuthSession{}, domain.ErrInvalidInput
	}
	passwordHash, err := domain.HashPassword(cmd.Password)
	if err != nil {
		return AuthSession{}, domain.ErrInvalidInput
	}
	username := resolveRegisterUsername(cmd.Username, normalizedEmail)

	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	if err := s.ensureUniqueEmailAndUsernameLocked(uuid.Nil, normalizedEmail, username); err != nil {
		return AuthSession{}, err
	}
	newUser := buildRegisteredUser(normalizedEmail, fullName, username, passwordHash, cmd, s.clock.Now())
	s.users[newUser.ID] = newUser
	return s.issueSessionBundleLocked(ctx, newUser, uuid.Nil), nil
}

func (s *Service) ResetPasswordWithSession(ctx context.Context, cmd ResetPasswordCommand) (AuthSession, error) {
	passwordHash, err := domain.HashPassword(cmd.NewPassword)
	if err != nil {
		return AuthSession{}, domain.ErrInvalidInput
	}
	lowerLookup := resolveResetLookup(cmd)
	if lowerLookup == "" {
		return AuthSession{}, domain.ErrInvalidInput
	}
	s.syncUsersFromStore(ctx)
	s.mu.RLock()
	user, found := s.findUserLocked(lowerLookup)
	s.mu.RUnlock()
	if !found {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	if err := s.authorizePasswordReset(ctx, user.Email, cmd.ResetToken, cmd.OTPCode); err != nil {
		return AuthSession{}, err
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	user.PasswordHash = passwordHash
	s.users[user.ID] = user
	s.updatePasswordInStoreLocked(ctx, user.Email, passwordHash)
	s.revokeUserRefreshTokensLocked(ctx, user.ID, s.clock.Now())
	return s.issueSessionBundleLocked(ctx, user, uuid.Nil), nil
}

func (s *Service) OAuthLoginWithSession(ctx context.Context, provider, idToken string) (AuthSession, error) {
	trimmedProvider := strings.TrimSpace(provider)
	normalizedProvider := strings.ToLower(trimmedProvider)
	clientID, err := s.configuredOAuthClientID(normalizedProvider)
	if err != nil {
		return AuthSession{}, err
	}
	claims, err := s.verifyOAuthIDToken(ctx, normalizedProvider, idToken, clientID)
	if err != nil {
		return AuthSession{}, err
	}
	return s.upsertOAuthUserWithSession(ctx, claims.Email, claims.Name), nil
}

func (s *Service) ExchangeOAuthCodeWithSession(
	ctx context.Context,
	provider string,
	code string,
	codeVerifier string,
	redirectURI string,
) (AuthSession, error) {
	normalizedProvider := strings.ToLower(strings.TrimSpace(provider))
	clientID, err := s.configuredOAuthClientID(normalizedProvider)
	if err != nil {
		return AuthSession{}, err
	}
	cleanCode := strings.TrimSpace(code)
	cleanVerifier := strings.TrimSpace(codeVerifier)
	if cleanCode == "" {
		return AuthSession{}, domain.ErrInvalidInput
	}
	if len(cleanVerifier) < minPKCEVerifierLen {
		return AuthSession{}, domain.ErrInvalidInput
	}
	cleanRedirect, err := validateOAuthRedirectURI(redirectURI)
	if err != nil {
		return AuthSession{}, err
	}
	idToken, err := s.requestOAuthIDToken(ctx, normalizedProvider, clientID, cleanCode, cleanVerifier, cleanRedirect)
	if err != nil {
		return AuthSession{}, err
	}
	claims, err := s.verifyOAuthIDToken(ctx, normalizedProvider, idToken, clientID)
	if err != nil {
		return AuthSession{}, err
	}
	return s.upsertOAuthUserWithSession(ctx, claims.Email, claims.Name), nil
}

func (s *Service) RefreshSession(ctx context.Context, rawRefreshToken string) (AuthSession, error) {
	cleanToken := strings.TrimSpace(rawRefreshToken)
	tokenHash := domain.HashToken(cleanToken)
	if tokenHash == "" {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	s.syncUsersFromStore(ctx)
	record, found := s.loadRefreshToken(ctx, tokenHash)
	if !found {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	now := s.clock.Now()
	s.mu.Lock()
	defer s.mu.Unlock()
	if record.Revoked {
		s.revokeRefreshFamilyLocked(ctx, record.FamilyID, now)
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	if !record.IsUsable(now) {
		s.revokeSingleRefreshTokenLocked(ctx, tokenHash, now)
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	user, exists := s.users[record.UserID]
	if !exists {
		return AuthSession{}, domain.ErrInvalidCredentials
	}
	s.revokeSingleRefreshTokenLocked(ctx, tokenHash, now)
	return s.issueSessionBundleLocked(ctx, user, record.FamilyID), nil
}

func (s *Service) LogoutWithRefresh(ctx context.Context, rawAccessToken, rawRefreshToken string) error {
	if err := s.Logout(ctx, rawAccessToken); err != nil {
		return err
	}
	cleanRefresh := strings.TrimSpace(rawRefreshToken)
	refreshHash := domain.HashToken(cleanRefresh)
	if refreshHash == "" {
		return nil
	}
	record, found := s.loadRefreshToken(ctx, refreshHash)
	now := s.clock.Now()
	s.mu.Lock()
	defer s.mu.Unlock()
	if found {
		s.revokeRefreshFamilyLocked(ctx, record.FamilyID, now)
		return nil
	}
	s.revokeSingleRefreshTokenLocked(ctx, refreshHash, now)
	return nil
}

func (s *Service) issueSessionBundleLocked(
	ctx context.Context,
	user domain.User,
	familyID uuid.UUID,
) AuthSession {
	now := s.clock.Now()
	expiresAt := now.Add(defaultAccessTokenTTL)
	jwtToken := s.tokenMgr.IssueWithTTL(user, now, defaultAccessTokenTTL)
	tokenHash := domain.HashToken(jwtToken)
	s.sessionHashes[tokenHash] = sessionEntry{
		UserID:    user.ID,
		ExpiresAt: expiresAt,
	}
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpsertUser(ctx, user, jwtToken, expiresAt)
		}
	}
	rawRefresh, err := domain.GenerateRefreshToken()
	if err != nil {
		rawRefresh = ""
	}
	refreshHash := domain.HashToken(rawRefresh)
	if refreshHash != "" {
		record := domain.NewRefreshTokenRecord(user.ID, familyID, refreshHash, now, defaultRefreshTokenTTL)
		s.refreshTokens[refreshHash] = record
		if s.store != nil {
			if s.store.Enabled() {
				_ = s.store.SaveRefreshToken(ctx, record)
			}
		}
	}
	return AuthSession{
		User:         user,
		AccessToken:  jwtToken,
		RefreshToken: rawRefresh,
		ExpiresIn:    int(defaultAccessTokenTTL.Seconds()),
	}
}

func (s *Service) loadRefreshToken(ctx context.Context, tokenHash string) (domain.RefreshTokenRecord, bool) {
	s.mu.RLock()
	record, inMem := s.refreshTokens[tokenHash]
	store := s.store
	s.mu.RUnlock()
	if inMem {
		return record, true
	}
	if store == nil {
		return domain.RefreshTokenRecord{}, false
	}
	if !store.Enabled() {
		return domain.RefreshTokenRecord{}, false
	}
	dbRec, err := store.GetRefreshTokenByHash(ctx, tokenHash)
	if err != nil {
		return domain.RefreshTokenRecord{}, false
	}
	s.mu.Lock()
	s.refreshTokens[tokenHash] = dbRec
	s.mu.Unlock()
	return dbRec, true
}

func (s *Service) revokeSingleRefreshTokenLocked(ctx context.Context, tokenHash string, now time.Time) {
	if existing, ok := s.refreshTokens[tokenHash]; ok {
		existing.Revoked = true
		existing.UpdatedAt = now
		s.refreshTokens[tokenHash] = existing
	}
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.RevokeRefreshToken(ctx, tokenHash, now)
		}
	}
}

func (s *Service) revokeRefreshFamilyLocked(ctx context.Context, familyID uuid.UUID, now time.Time) {
	if familyID == uuid.Nil {
		return
	}
	for hash, rec := range s.refreshTokens {
		if rec.FamilyID == familyID {
			rec.Revoked = true
			rec.UpdatedAt = now
			s.refreshTokens[hash] = rec
		}
	}
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.RevokeRefreshTokenFamily(ctx, familyID, now)
		}
	}
}

func (s *Service) CleanupExpiredRefreshTokens(ctx context.Context) (int64, error) {
	now := s.clock.Now()
	revokedCutoff := now.Add(-revokedTokenRetentionDuration)
	removedMemory := int64(0)

	s.mu.Lock()
	for hash, rec := range s.refreshTokens {
		if shouldEvictRefreshToken(rec, now, revokedCutoff) {
			delete(s.refreshTokens, hash)
			removedMemory++
		}
	}
	store := s.store
	s.mu.Unlock()

	if store == nil {
		return removedMemory, nil
	}
	if !store.Enabled() {
		return removedMemory, nil
	}
	removedDB, err := store.DeleteExpiredRefreshTokens(ctx, now, revokedCutoff)
	if err != nil {
		return removedMemory, err
	}
	if removedDB > removedMemory {
		return removedDB, nil
	}
	return removedMemory, nil
}

func (s *Service) StartMaintenanceLoop(ctx context.Context, interval time.Duration) {
	if ctx == nil {
		return
	}
	if interval <= 0 {
		interval = time.Hour
	}
	go func() {
		ticker := time.NewTicker(interval)
		defer ticker.Stop()
		for {
			select {
			case <-ctx.Done():
				return
			case <-ticker.C:
				_, _ = s.CleanupExpiredRefreshTokens(ctx)
			}
		}
	}()
}

func shouldEvictRefreshToken(rec domain.RefreshTokenRecord, now, revokedCutoff time.Time) bool {
	if rec.ExpiresAt.Before(now) {
		return true
	}
	if rec.Revoked {
		return rec.UpdatedAt.Before(revokedCutoff)
	}
	return false
}

func (s *Service) revokeUserRefreshTokensLocked(ctx context.Context, userID uuid.UUID, now time.Time) {
	for hash, rec := range s.refreshTokens {
		if rec.UserID == userID {
			rec.Revoked = true
			rec.UpdatedAt = now
			s.refreshTokens[hash] = rec
		}
	}
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.RevokeRefreshTokensByUser(ctx, userID, now)
		}
	}
}

func (s *Service) updatePasswordInStoreLocked(ctx context.Context, email, passwordHash string) {
	if s.store == nil {
		return
	}
	if !s.store.Enabled() {
		return
	}
	_ = s.store.UpdatePassword(ctx, email, passwordHash)
}

func (s *Service) upsertOAuthUserWithSession(ctx context.Context, email, displayName string) AuthSession {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()
	existing, found := s.findUserLocked(email)
	if found {
		return s.issueSessionBundleLocked(ctx, existing, uuid.Nil)
	}
	created := buildNewOAuthUser(email, displayName, s.clock.Now())
	s.users[created.ID] = created
	return s.issueSessionBundleLocked(ctx, created, uuid.Nil)
}

func resolveRegisterUsername(rawUsername, normalizedEmail string) string {
	trimmed := strings.TrimSpace(rawUsername)
	lower := strings.ToLower(trimmed)
	if lower != "" {
		return lower
	}
	emailParts := strings.Split(normalizedEmail, "@")
	return emailParts[0]
}

func resolveResetLookup(cmd ResetPasswordCommand) string {
	lookup := strings.TrimSpace(cmd.Email)
	if lookup == "" {
		lookup = strings.TrimSpace(cmd.Identifier)
	}
	return strings.ToLower(lookup)
}

func buildRegisteredUser(
	normalizedEmail string,
	fullName string,
	username string,
	passwordHash string,
	cmd RegisterCommand,
	now time.Time,
) domain.User {
	return domain.User{
		ID:           uuid.New(),
		Username:     username,
		FullName:     fullName,
		Email:        normalizedEmail,
		Phone:        strings.TrimSpace(cmd.Phone),
		BirthDate:    strings.TrimSpace(cmd.BirthDate),
		Role:         domain.RoleClient,
		Permissions:  []string{},
		ActiveView:   domain.ViewPersonal,
		Preferences:  domain.DefaultUserPreferences(),
		CreatedAt:    now,
		PasswordHash: passwordHash,
	}
}

func buildNewOAuthUser(email, displayName string, now time.Time) domain.User {
	fullName := strings.TrimSpace(displayName)
	emailParts := strings.Split(email, "@")
	if fullName == "" {
		fullName = emailParts[0]
	}
	randomPass, _ := domain.GenerateRandomHex(16)
	passHash, _ := domain.HashPassword(randomPass)
	return domain.User{
		ID:           uuid.New(),
		Username:     emailParts[0],
		FullName:     fullName,
		Email:        email,
		Role:         domain.RoleClient,
		Permissions:  []string{},
		ActiveView:   domain.ViewPersonal,
		Preferences:  domain.DefaultUserPreferences(),
		CreatedAt:    now,
		PasswordHash: passHash,
	}
}
