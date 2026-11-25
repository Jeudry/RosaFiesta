package app

import (
	"context"
	"net/http"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
)

const defaultSessionTTL = 30 * 24 * time.Hour

type ActiveEventsChecker interface {
	ActiveEventsForClient(ctx context.Context, clientName string) int
}

type ClientSummary = domain.AdminClient

type StaffMember = domain.StaffRecord

type RegisterCommand struct {
	Username  string
	FullName  string
	Email     string
	Password  string
	BirthDate string
	Phone     string
}

type UpdateProfileCommand struct {
	FullName  *string
	Email     *string
	Phone     *string
	BirthDate *string
	Username  *string
	AvatarKey *string
}

type UpdatePreferencesCommand struct {
	PushNotificationsEnabled *bool
	WhatsAppAlertsEnabled    *bool
	EmailReceiptsEnabled     *bool
	BiometricLockEnabled     *bool
	AutoSyncWifiOnly         *bool
	EventRemindersEnabled    *bool
	PreferredLanguage        *string
	DarkThemeEnabled         *bool
	SavedPaymentMethod       *string
	ProfileZone              *string
}

// Options define las dependencias y políticas de autenticación del servicio de identidad.
type Options struct {
	Env                   config.Environment
	JWTSecret             string
	SMTPHost              string
	SMTPPort              int
	SMTPFrom              string
	SMTPUsername          string
	SMTPPassword          string
	RequireConfiguredSMTP bool
	EmailSender           mailer.Sender
	GoogleClientID        string
	GoogleClientSecret    string
	AppleClientID         string
	AppleClientSecret     string
	AppleTeamID           string
	AppleKeyID            string
	ApplePrivateKey       string
	GoogleJWKSURL         string
	AppleJWKSURL          string
	GoogleTokenURL        string
	AppleTokenURL         string
	WebPushVAPIDPublicKey string
	OAuthHTTPClient       *http.Client
	Notifier              OTPNotifier
}

type Store interface {
	Enabled() bool
	UpsertUser(ctx context.Context, user domain.User, rawSessionToken string, expiresAt time.Time) error
	ListUsers(ctx context.Context) ([]domain.User, error)
	GetUserByEmail(ctx context.Context, email string) (domain.User, error)
	GetUserByID(ctx context.Context, userID uuid.UUID) (domain.User, error)
	UpdatePassword(ctx context.Context, email, passwordHash string) error
	Authenticate(ctx context.Context, rawSessionToken string) (uuid.UUID, error)
	DeleteSession(ctx context.Context, rawSessionToken string) error
	DeleteSessionsByUser(ctx context.Context, userID uuid.UUID) error
	UpdateActiveView(ctx context.Context, userID uuid.UUID, view domain.AppView) error
	UpdateUserPreferences(ctx context.Context, userID uuid.UUID, prefs domain.UserPreferences) error
	DeleteUser(ctx context.Context, userID uuid.UUID) error
	UpsertOTP(ctx context.Context, record domain.OTPRecord) error
	GetOTP(ctx context.Context, email string) (domain.OTPRecord, error)
	IncrementOTPAttempts(ctx context.Context, email string) error
	MarkOTPVerified(ctx context.Context, email, resetTokenHash string, resetExpiresAt time.Time) error
	MarkOTPUsed(ctx context.Context, email string) error
	ConsumeOTPResetToken(ctx context.Context, email, resetTokenHash string) (bool, error)
	ListAdminClients(ctx context.Context) ([]domain.AdminClient, error)
	UpsertAdminClient(ctx context.Context, client domain.AdminClient) error
	ListStaffMembers(ctx context.Context) ([]domain.StaffRecord, error)
	UpsertStaffMember(ctx context.Context, staff domain.StaffRecord) error
	UpsertDevicePushToken(ctx context.Context, record domain.DevicePushToken) error
	DisableDevicePushToken(ctx context.Context, userID uuid.UUID, token string, updatedAt time.Time) error
	DisableAllUserPushTokens(ctx context.Context, userID uuid.UUID, updatedAt time.Time) error
	ListActivePushTokensForUser(ctx context.Context, userID uuid.UUID) ([]domain.DevicePushToken, error)
	ListActiveAdminPushTokens(ctx context.Context) ([]domain.DevicePushToken, error)
	SaveRefreshToken(ctx context.Context, record domain.RefreshTokenRecord) error
	GetRefreshTokenByHash(ctx context.Context, tokenHash string) (domain.RefreshTokenRecord, error)
	RevokeRefreshToken(ctx context.Context, tokenHash string, updatedAt time.Time) error
	RevokeRefreshTokenFamily(ctx context.Context, familyID uuid.UUID, updatedAt time.Time) error
	RevokeRefreshTokensByUser(ctx context.Context, userID uuid.UUID, updatedAt time.Time) error
	DeleteExpiredRefreshTokens(ctx context.Context, expiresBefore, revokedUpdatedBefore time.Time) (int64, error)
	RevokeAccessToken(ctx context.Context, tokenHash string, expiresAt time.Time) error
	IsAccessTokenRevoked(ctx context.Context, tokenHash string) (bool, error)
	DeleteExpiredMaintenanceRows(ctx context.Context, before time.Time) (int64, error)
	DeleteStaffMember(ctx context.Context, staffID string) error
	ListUserFavorites(ctx context.Context, userID uuid.UUID) ([]string, error)
	AddUserFavorite(ctx context.Context, userID uuid.UUID, articleID string) error
	RemoveUserFavorite(ctx context.Context, userID uuid.UUID, articleID string) error
}

type sessionEntry struct {
	UserID    uuid.UUID
	ExpiresAt time.Time
}

type Service struct {
	mu                     sync.RWMutex
	otpMu                  sync.Mutex
	clock                  clock.Clock
	checker                ActiveEventsChecker
	store                  Store
	opts                   Options
	tokenMgr               domain.TokenManager
	notifier               OTPNotifier
	customNotifierProvided bool
	users                  map[uuid.UUID]domain.User
	sessionHashes          map[string]sessionEntry
	revokedHashes          map[string]time.Time
	refreshTokens          map[string]domain.RefreshTokenRecord
	otps                   map[string]domain.OTPRecord
	oauthStates            map[string]oauthStateEntry
	pushTokens             map[string]domain.DevicePushToken
	favorites              map[uuid.UUID]map[string]struct{}
	clients                []domain.AdminClient
	staff                  []domain.StaffRecord
	jwksCache              *jwksKeyCache
}

var (
	seedHashOnce sync.Once
	seedHashVal  string
)

func CanonicalSeedPasswordHash() string {
	return canonicalSeedPasswordHash()
}

func canonicalSeedPasswordHash() string {
	seedHashOnce.Do(func() {
		hashed, _ := domain.HashPassword("secret123")
		seedHashVal = hashed
	})
	return seedHashVal
}

func NewService(clk clock.Clock, checker ActiveEventsChecker) *Service {
	return NewServiceWithOptions(clk, checker, Options{Env: config.EnvDevelopment})
}

func NewServiceWithOptions(clk clock.Clock, checker ActiveEventsChecker, opts Options) *Service {
	resolvedEnv := opts.Env
	if resolvedEnv == "" {
		resolvedEnv = config.EnvDevelopment
	}
	opts.Env = resolvedEnv
	customNotifier := opts.Notifier != nil
	notifier := opts.Notifier
	if notifier == nil {
		notifier = newDefaultOTPNotifier(opts)
	}
	svc := &Service{
		clock:                  clk,
		checker:                checker,
		opts:                   opts,
		tokenMgr:               domain.NewTokenManager(opts.JWTSecret),
		notifier:               notifier,
		customNotifierProvided: customNotifier,
		jwksCache:              newJWKSKeyCache(opts.OAuthHTTPClient),
	}
	svc.Reset(context.Background())
	return svc
}

func (s *Service) Reset(ctx context.Context) {
	if s.opts.Env == config.EnvProduction {
		s.ResetEmpty(ctx)
		return
	}
	now := s.clock.Now()
	users := buildCanonicalUsers(now)
	sessions := buildDevStaticSessions(now)
	s.mu.Lock()
	s.users = users
	s.sessionHashes = sessions
	s.revokedHashes = make(map[string]time.Time)
	s.refreshTokens = make(map[string]domain.RefreshTokenRecord)
	s.otps = make(map[string]domain.OTPRecord)
	s.oauthStates = make(map[string]oauthStateEntry)
	s.pushTokens = make(map[string]domain.DevicePushToken)
	s.favorites = make(map[uuid.UUID]map[string]struct{})
	s.clients = defaultClients(now)
	s.staff = defaultStaff(now)
	s.mu.Unlock()
}

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.users = make(map[uuid.UUID]domain.User)
	s.sessionHashes = make(map[string]sessionEntry)
	s.revokedHashes = make(map[string]time.Time)
	s.refreshTokens = make(map[string]domain.RefreshTokenRecord)
	s.otps = make(map[string]domain.OTPRecord)
	s.oauthStates = make(map[string]oauthStateEntry)
	s.pushTokens = make(map[string]domain.DevicePushToken)
	s.favorites = make(map[uuid.UUID]map[string]struct{})
	s.clients = nil
	s.staff = nil
	s.mu.Unlock()
}

func (s *Service) ResetAdminOnly(ctx context.Context) {
	if s.opts.Env == config.EnvProduction {
		s.ResetEmpty(ctx)
		return
	}
	now := s.clock.Now()
	users := buildAdminSeedUsers(now)
	sessions := buildAdminDevStaticSessions(now)
	s.mu.Lock()
	s.users = users
	s.sessionHashes = sessions
	s.revokedHashes = make(map[string]time.Time)
	s.refreshTokens = make(map[string]domain.RefreshTokenRecord)
	s.otps = make(map[string]domain.OTPRecord)
	s.oauthStates = make(map[string]oauthStateEntry)
	s.pushTokens = make(map[string]domain.DevicePushToken)
	s.favorites = make(map[uuid.UUID]map[string]struct{})
	s.clients = nil
	s.staff = defaultAdminStaff(now)
	s.mu.Unlock()
}

func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.mu.Unlock()
	s.syncUsersFromStore(context.Background())
	s.syncAdminRecordsFromStore(context.Background())
	return s
}

func (s *Service) Authenticate(ctx context.Context, accessToken string) (uuid.UUID, error) {
	cleanToken := strings.TrimSpace(accessToken)
	tokenHash := domain.HashToken(cleanToken)
	if tokenHash == "" {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	now := s.clock.Now()
	s.mu.RLock()
	revokedExp, hasRevokedEntry := s.revokedHashes[tokenHash]
	store := s.store
	entry, inMem := s.sessionHashes[tokenHash]
	userMem, userExists := s.users[entry.UserID]
	s.mu.RUnlock()
	isRevoked := hasRevokedEntry
	if isRevoked {
		isRevoked = now.Before(revokedExp)
	}
	if isRevoked {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	if s.isTokenRevokedInStore(ctx, store, tokenHash) {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	if dbUserID, ok := s.authenticateViaStore(ctx, store, cleanToken); ok {
		return dbUserID, nil
	}
	if parsedUUID, ok := s.verifyStatelessJWT(ctx, cleanToken, now); ok {
		return parsedUUID, nil
	}
	return validateMemSessionEntry(entry, inMem, userMem, userExists, now)
}

func (s *Service) verifyStatelessJWT(ctx context.Context, cleanToken string, now time.Time) (uuid.UUID, bool) {
	subjectID, err := s.tokenMgr.Verify(cleanToken, now)
	if err != nil {
		return uuid.Nil, false
	}
	parsedUUID, parseErr := uuid.Parse(subjectID)
	if parseErr != nil {
		return uuid.Nil, false
	}
	if !s.isUserActiveOrUnknown(ctx, parsedUUID) {
		return uuid.Nil, false
	}
	return parsedUUID, true
}

func validateMemSessionEntry(
	entry sessionEntry,
	inMem bool,
	userMem domain.User,
	userExists bool,
	now time.Time,
) (uuid.UUID, error) {
	if !inMem {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	if !now.Before(entry.ExpiresAt) {
		return uuid.Nil, domain.ErrTokenExpired
	}
	if !userExists {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	if !userMem.Active {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	return entry.UserID, nil
}

func (s *Service) isTokenRevokedInStore(ctx context.Context, store Store, tokenHash string) bool {
	hasStore := store != nil
	if !hasStore {
		return false
	}
	if !store.Enabled() {
		return false
	}
	revoked, err := store.IsAccessTokenRevoked(ctx, tokenHash)
	if err != nil {
		return true
	}
	return revoked
}

func (s *Service) authenticateViaStore(ctx context.Context, store Store, cleanToken string) (uuid.UUID, bool) {
	hasStore := store != nil
	if !hasStore {
		return uuid.Nil, false
	}
	if !store.Enabled() {
		return uuid.Nil, false
	}
	dbUserID, err := store.Authenticate(ctx, cleanToken)
	if err != nil {
		return uuid.Nil, false
	}
	if !s.isUserActiveOrUnknown(ctx, dbUserID) {
		return uuid.Nil, false
	}
	return dbUserID, true
}

func (s *Service) isUserActiveOrUnknown(ctx context.Context, userID uuid.UUID) bool {
	user, err := s.CurrentUser(ctx, userID)
	if err != nil {
		return false
	}
	return user.Active
}

func (s *Service) ResolvePrincipal(ctx context.Context, userID uuid.UUID) (platformhttp.Principal, error) {
	user, err := s.CurrentUser(ctx, userID)
	if err != nil {
		return platformhttp.Principal{}, err
	}
	perms := make([]string, len(user.Permissions))
	copy(perms, user.Permissions)
	return platformhttp.Principal{
		ID:          user.ID,
		Email:       user.Email,
		FullName:    user.FullName,
		Role:        string(user.Role),
		Permissions: perms,
	}, nil
}

func (s *Service) CurrentUser(ctx context.Context, userID uuid.UUID) (domain.User, error) {
	s.mu.RLock()
	store := s.store
	memUser, ok := s.users[userID]
	s.mu.RUnlock()
	if store != nil {
		if store.Enabled() {
			if dbUser, err := store.GetUserByID(ctx, userID); err == nil {
				s.mu.Lock()
				s.users[dbUser.ID] = dbUser
				s.mu.Unlock()
				return dbUser, nil
			}
		}
	}
	if !ok {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	return memUser, nil
}

func (s *Service) ListRegisteredUsers(ctx context.Context) []domain.User {
	s.syncUsersFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()
	result := make([]domain.User, 0, len(s.users))
	for _, user := range s.users {
		result = append(result, user)
	}
	return result
}

func (s *Service) Login(ctx context.Context, identifier, password string) (domain.User, string, error) {
	session, err := s.LoginWithSession(ctx, identifier, password)
	if err != nil {
		return domain.User{}, "", err
	}
	return session.User, session.AccessToken, nil
}

func (s *Service) Register(ctx context.Context, cmd RegisterCommand) (domain.User, string, error) {
	session, err := s.RegisterWithSession(ctx, cmd)
	if err != nil {
		return domain.User{}, "", err
	}
	return session.User, session.AccessToken, nil
}

func (s *Service) Logout(ctx context.Context, rawToken string) error {
	tokenHash := domain.HashToken(rawToken)
	if tokenHash == "" {
		return domain.ErrInvalidCredentials
	}
	expiresAt := s.clock.Now().Add(defaultAccessTokenTTL)
	s.mu.Lock()
	delete(s.sessionHashes, tokenHash)
	s.recordRevokedHashLocked(tokenHash, expiresAt, s.clock.Now())
	store := s.store
	s.mu.Unlock()
	storeEnabled := store != nil
	if storeEnabled {
		storeEnabled = store.Enabled()
	}
	if storeEnabled {
		_ = store.DeleteSession(ctx, rawToken)
		_ = store.RevokeAccessToken(ctx, tokenHash, expiresAt)
	}
	return nil
}

func (s *Service) UpdateProfile(ctx context.Context, userID uuid.UUID, cmd UpdateProfileCommand) (domain.User, error) {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	user, ok := s.users[userID]
	if !ok {
		return domain.User{}, domain.ErrUserNotFound
	}
	updated, err := applyProfilePatch(user, cmd)
	if err != nil {
		return domain.User{}, err
	}
	if err := s.ensureUniqueEmailAndUsernameLocked(userID, updated.Email, updated.Username); err != nil {
		return domain.User{}, err
	}
	s.users[userID] = updated
	s.persistUserOnlyLocked(ctx, updated)
	return updated, nil
}

func (s *Service) UpdatePreferences(
	ctx context.Context,
	userID uuid.UUID,
	cmd UpdatePreferencesCommand,
) (domain.User, error) {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	user, ok := s.users[userID]
	if !ok {
		return domain.User{}, domain.ErrUserNotFound
	}
	user.Preferences = mergeUserPreferences(user.Preferences, cmd)
	s.users[userID] = user
	pushDisabled := !user.Preferences.PushNotificationsEnabled
	if pushDisabled {
		s.disableAllUserPushTokensLocked(userID)
	}
	store := s.store
	if store != nil {
		if store.Enabled() {
			_ = store.UpdateUserPreferences(ctx, userID, user.Preferences)
			if pushDisabled {
				_ = store.DisableAllUserPushTokens(ctx, userID, s.clock.Now())
			}
		}
	}
	return user, nil
}

func (s *Service) PreferencesForAccount(ctx context.Context, userID uuid.UUID, email string) domain.UserPreferences {
	s.syncUsersFromStore(ctx)
	s.mu.RLock()
	defer s.mu.RUnlock()

	if userID != uuid.Nil {
		if user, ok := s.users[userID]; ok {
			return user.Preferences
		}
	}
	cleanEmail := strings.ToLower(strings.TrimSpace(email))
	if cleanEmail != "" {
		if user, found := s.findUserLocked(cleanEmail); found {
			return user.Preferences
		}
	}
	return domain.DefaultUserPreferences()
}

func mergeUserPreferences(
	current domain.UserPreferences,
	cmd UpdatePreferencesCommand,
) domain.UserPreferences {
	updated := mergeNotificationAndSecurityPrefs(current, cmd)
	return mergeLocalizationAndProfilePrefs(updated, cmd)
}

func mergeNotificationAndSecurityPrefs(
	updated domain.UserPreferences,
	cmd UpdatePreferencesCommand,
) domain.UserPreferences {
	if cmd.PushNotificationsEnabled != nil {
		updated.PushNotificationsEnabled = *cmd.PushNotificationsEnabled
	}
	if cmd.WhatsAppAlertsEnabled != nil {
		updated.WhatsAppAlertsEnabled = *cmd.WhatsAppAlertsEnabled
	}
	if cmd.EmailReceiptsEnabled != nil {
		updated.EmailReceiptsEnabled = *cmd.EmailReceiptsEnabled
	}
	if cmd.BiometricLockEnabled != nil {
		updated.BiometricLockEnabled = *cmd.BiometricLockEnabled
	}
	if cmd.AutoSyncWifiOnly != nil {
		updated.AutoSyncWifiOnly = *cmd.AutoSyncWifiOnly
	}
	if cmd.EventRemindersEnabled != nil {
		updated.EventRemindersEnabled = *cmd.EventRemindersEnabled
	}
	return updated
}

func mergeLocalizationAndProfilePrefs(
	updated domain.UserPreferences,
	cmd UpdatePreferencesCommand,
) domain.UserPreferences {
	if cmd.PreferredLanguage != nil {
		lang := strings.ToLower(strings.TrimSpace(*cmd.PreferredLanguage))
		switch lang {
		case "en", "english":
			updated.PreferredLanguage = "en"
		case "":
		default:
			updated.PreferredLanguage = "es"
		}
	}
	if cmd.DarkThemeEnabled != nil {
		updated.DarkThemeEnabled = *cmd.DarkThemeEnabled
	}
	if cmd.SavedPaymentMethod != nil {
		updated.SavedPaymentMethod = strings.TrimSpace(*cmd.SavedPaymentMethod)
	}
	if cmd.ProfileZone != nil {
		updated.ProfileZone = strings.TrimSpace(*cmd.ProfileZone)
	}
	return updated
}

func (s *Service) ChangePassword(ctx context.Context, userID uuid.UUID, currentPassword, newPassword string) error {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	user, ok := s.users[userID]
	if !ok {
		return domain.ErrUserNotFound
	}
	cleanCurrent := strings.TrimSpace(currentPassword)
	matches := domain.VerifyPassword(user.PasswordHash, cleanCurrent)
	if !matches {
		return domain.ErrInvalidCredentials
	}
	newHash, err := domain.HashPassword(newPassword)
	if err != nil {
		return domain.ErrInvalidInput
	}
	user.PasswordHash = newHash
	s.users[userID] = user
	s.updatePasswordInStoreLocked(ctx, user.Email, newHash)
	s.revokeUserAccessLocked(ctx, userID)
	return nil
}

func (s *Service) AuthorizePermission(ctx context.Context, userID uuid.UUID, permission string) error {
	user, err := s.CurrentUser(ctx, userID)
	if err != nil {
		return domain.ErrInvalidCredentials
	}
	if !user.Active {
		return domain.ErrInvalidCredentials
	}
	if !user.HasPermission(permission) {
		return domain.ErrPermissionDenied
	}
	return nil
}

func (s *Service) SwitchView(ctx context.Context, userID uuid.UUID, view domain.AppView) (domain.User, error) {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	user, ok := s.users[userID]
	if !ok {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	if !user.CanAccessView(view) {
		return domain.User{}, domain.ErrForbiddenView
	}
	user.ActiveView = view
	s.users[userID] = user
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpdateActiveView(ctx, userID, view)
		}
	}
	return user, nil
}

func (s *Service) DeleteAccount(ctx context.Context, userID uuid.UUID) error {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	user, ok := s.users[userID]
	if !ok {
		return domain.ErrInvalidCredentials
	}
	activeCount := 0
	if s.checker != nil {
		activeCount = s.checker.ActiveEventsForClient(ctx, user.ID.String())
		if activeCount == 0 {
			activeCount = s.checker.ActiveEventsForClient(ctx, user.Email)
		}
	}
	if err := domain.ValidateAccountDeletion(activeCount); err != nil {
		return err
	}
	delete(s.users, userID)
	s.revokeUserRefreshTokensLocked(ctx, userID, s.clock.Now())
	for hash, entry := range s.sessionHashes {
		if entry.UserID == userID {
			delete(s.sessionHashes, hash)
		}
	}
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.DeleteUser(ctx, userID)
		}
	}
	return nil
}

func applyProfilePatch(user domain.User, cmd UpdateProfileCommand) (domain.User, error) {
	updated := user
	if cmd.FullName != nil {
		trimmed := strings.TrimSpace(*cmd.FullName)
		if trimmed == "" {
			return domain.User{}, domain.ErrInvalidInput
		}
		updated.FullName = trimmed
	}
	if cmd.Email != nil {
		normalized, err := domain.NormalizeEmail(*cmd.Email)
		if err != nil {
			return domain.User{}, domain.ErrInvalidInput
		}
		updated.Email = normalized
	}
	if cmd.Username != nil {
		trimmedUser := strings.TrimSpace(*cmd.Username)
		lowerUser := strings.ToLower(trimmedUser)
		if lowerUser == "" {
			return domain.User{}, domain.ErrInvalidInput
		}
		updated.Username = lowerUser
	}
	if cmd.Phone != nil {
		updated.Phone = strings.TrimSpace(*cmd.Phone)
	}
	if cmd.BirthDate != nil {
		updated.BirthDate = strings.TrimSpace(*cmd.BirthDate)
	}
	if cmd.AvatarKey != nil {
		updated.AvatarKey = strings.TrimSpace(*cmd.AvatarKey)
	}
	return updated, nil
}

func (s *Service) ensureUniqueEmailAndUsernameLocked(excludeID uuid.UUID, email, username string) error {
	for _, existing := range s.users {
		if existing.ID == excludeID {
			continue
		}
		if strings.EqualFold(existing.Email, email) {
			return domain.ErrEmailAlreadyRegistered
		}
		if username != "" {
			if strings.EqualFold(existing.Username, username) {
				return domain.ErrUsernameAlreadyTaken
			}
		}
	}
	return nil
}

func (s *Service) persistUserOnlyLocked(ctx context.Context, user domain.User) {
	if s.store == nil {
		return
	}
	if !s.store.Enabled() {
		return
	}
	expiresAt := s.clock.Now().Add(defaultSessionTTL)
	_ = s.store.UpsertUser(ctx, user, "", expiresAt)
}

func (s *Service) syncUsersFromStore(ctx context.Context) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	dbUsers, err := store.ListUsers(ctx)
	if err != nil {
		return
	}
	nextUsers := make(map[uuid.UUID]domain.User, len(dbUsers))
	for _, dbUser := range dbUsers {
		nextUsers[dbUser.ID] = dbUser
	}
	s.mu.Lock()
	s.users = nextUsers
	s.mu.Unlock()
}

func (s *Service) findUserLocked(cleanIdentifier string) (domain.User, bool) {
	ordered := s.sortedUsersLocked()
	for _, user := range ordered {
		if strings.EqualFold(user.Email, cleanIdentifier) {
			return user, true
		}
	}
	for _, user := range ordered {
		if strings.EqualFold(user.Username, cleanIdentifier) {
			return user, true
		}
	}
	var localMatch domain.User
	localCount := 0
	for _, user := range ordered {
		emailParts := strings.Split(user.Email, "@")
		if strings.EqualFold(emailParts[0], cleanIdentifier) {
			localMatch = user
			localCount++
		}
	}
	if localCount == 1 {
		return localMatch, true
	}
	return domain.User{}, false
}

func (s *Service) sortedUsersLocked() []domain.User {
	ordered := make([]domain.User, 0, len(s.users))
	for _, user := range s.users {
		ordered = append(ordered, user)
	}
	sort.Slice(ordered, func(i, j int) bool {
		return ordered[i].ID.String() < ordered[j].ID.String()
	})
	return ordered
}

// ListFavorites devuelve los IDs de artículos favoritos del usuario autenticado.
func (s *Service) ListFavorites(ctx context.Context, userID uuid.UUID) []string {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	storeEnabled := store != nil
	if storeEnabled {
		storeEnabled = store.Enabled()
	}
	if storeEnabled {
		if rows, err := store.ListUserFavorites(ctx, userID); err == nil {
			s.mu.Lock()
			favMap := make(map[string]struct{}, len(rows))
			for _, id := range rows {
				favMap[id] = struct{}{}
			}
			s.favorites[userID] = favMap
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	favSet := s.favorites[userID]
	out := make([]string, 0, len(favSet))
	for articleID := range favSet {
		out = append(out, articleID)
	}
	return out
}

// AddFavorite agrega un artículo a los favoritos del usuario autenticado.
func (s *Service) AddFavorite(ctx context.Context, userID uuid.UUID, articleID string) ([]string, error) {
	cleanID := strings.TrimSpace(articleID)
	if cleanID == "" {
		return nil, domain.ErrInvalidInput
	}
	s.mu.Lock()
	if s.favorites[userID] == nil {
		s.favorites[userID] = make(map[string]struct{})
	}
	s.favorites[userID][cleanID] = struct{}{}
	store := s.store
	s.mu.Unlock()
	storeEnabled := store != nil
	if storeEnabled {
		storeEnabled = store.Enabled()
	}
	if storeEnabled {
		if err := store.AddUserFavorite(ctx, userID, cleanID); err != nil {
			return nil, err
		}
	}
	return s.ListFavorites(ctx, userID), nil
}

// RemoveFavorite elimina un artículo de los favoritos del usuario autenticado.
func (s *Service) RemoveFavorite(ctx context.Context, userID uuid.UUID, articleID string) ([]string, error) {
	cleanID := strings.TrimSpace(articleID)
	if cleanID == "" {
		return nil, domain.ErrInvalidInput
	}
	s.mu.Lock()
	if favSet := s.favorites[userID]; favSet != nil {
		delete(favSet, cleanID)
	}
	store := s.store
	s.mu.Unlock()
	storeEnabled := store != nil
	if storeEnabled {
		storeEnabled = store.Enabled()
	}
	if storeEnabled {
		if err := store.RemoveUserFavorite(ctx, userID, cleanID); err != nil {
			return nil, err
		}
	}
	return s.ListFavorites(ctx, userID), nil
}
