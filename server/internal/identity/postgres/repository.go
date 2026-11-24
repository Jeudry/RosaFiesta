package postgres

import (
	"context"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/identity/postgres/sqlcgen"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
)

type Repository struct {
	pool    *pgxpool.Pool
	queries *sqlcgen.Queries
	clock   clock.Clock
}

func NewRepository(pool *pgxpool.Pool) *Repository {
	if pool == nil {
		return &Repository{clock: clock.System{}}
	}
	return &Repository{
		pool:    pool,
		queries: sqlcgen.New(pool),
		clock:   clock.System{},
	}
}

func (r *Repository) WithClock(clk clock.Clock) *Repository {
	if clk != nil {
		r.clock = clk
	}
	return r
}

func (r *Repository) Enabled() bool {
	if r == nil {
		return false
	}
	return r.pool != nil
}

func (r *Repository) UpsertUser(ctx context.Context, user domain.User, rawSessionToken string, expiresAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	passwordHash := strings.TrimSpace(user.PasswordHash)
	if passwordHash == "" {
		return domain.ErrInvalidCredentials
	}
	username := strings.TrimSpace(user.Username)
	if username == "" {
		emailParts := strings.Split(user.Email, "@")
		username = emailParts[0]
	}
	joinedPermissions := strings.Join(user.Permissions, ",")
	err := r.queries.UpsertUser(ctx, sqlcgen.UpsertUserParams{
		ID:                       user.ID,
		FullName:                 user.FullName,
		Email:                    user.Email,
		Phone:                    user.Phone,
		PasswordHash:             passwordHash,
		Role:                     string(user.Role),
		Permissions:              joinedPermissions,
		CreatedAt:                user.CreatedAt,
		Username:                 username,
		BirthDate:                user.BirthDate,
		AvatarKey:                user.AvatarKey,
		Active:                   user.Active,
		PushNotificationsEnabled: user.Preferences.PushNotificationsEnabled,
		WhatsappAlertsEnabled:    user.Preferences.WhatsAppAlertsEnabled,
		EmailReceiptsEnabled:     user.Preferences.EmailReceiptsEnabled,
		BiometricLockEnabled:     user.Preferences.BiometricLockEnabled,
		AutoSyncWifiOnly:         user.Preferences.AutoSyncWifiOnly,
		EventRemindersEnabled:    user.Preferences.EventRemindersEnabled,
		PreferredLanguage:        user.Preferences.PreferredLanguage,
		DarkThemeEnabled:         user.Preferences.DarkThemeEnabled,
		SavedPaymentMethod:       user.Preferences.SavedPaymentMethod,
		ProfileZone:              user.Preferences.ProfileZone,
	})
	if err != nil {
		return err
	}
	tokenHash := domain.HashToken(rawSessionToken)
	if tokenHash != "" {
		return r.queries.UpsertSession(ctx, sqlcgen.UpsertSessionParams{
			TokenHash:  tokenHash,
			UserID:     user.ID,
			ActiveView: string(user.ActiveView),
			CreatedAt:  user.CreatedAt,
			ExpiresAt:  expiresAt,
		})
	}
	return nil
}

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
		mapped := mapRowToDomainUser(row)
		users = append(users, mapped)
	}
	return users, nil
}

func (r *Repository) GetUserByEmail(ctx context.Context, email string) (domain.User, error) {
	if !r.Enabled() {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	row, err := r.queries.GetUserByEmail(ctx, normalized)
	if err != nil {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	return mapRowToDomainUser(row), nil
}

func (r *Repository) GetUserByID(ctx context.Context, userID uuid.UUID) (domain.User, error) {
	if !r.Enabled() {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	row, err := r.queries.GetUserByID(ctx, userID)
	if err != nil {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	mapped := mapRowToDomainUser(row)
	viewStr, viewErr := r.queries.GetLatestActiveViewByUserID(ctx, sqlcgen.GetLatestActiveViewByUserIDParams{
		UserID:    userID,
		ExpiresAt: r.clock.Now(),
	})
	if viewErr == nil {
		candidate := domain.AppView(viewStr)
		if mapped.CanAccessView(candidate) {
			mapped.ActiveView = candidate
		}
	}
	return mapped, nil
}

func (r *Repository) UpdatePassword(ctx context.Context, email, passwordHash string) error {
	if !r.Enabled() {
		return nil
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	return r.queries.UpdateUserPasswordByEmail(ctx, sqlcgen.UpdateUserPasswordByEmailParams{
		Email:        normalized,
		PasswordHash: passwordHash,
	})
}

func (r *Repository) Authenticate(ctx context.Context, rawSessionToken string) (uuid.UUID, error) {
	if !r.Enabled() {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	tokenHash := domain.HashToken(rawSessionToken)
	if tokenHash == "" {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	now := r.clock.Now()
	params := sqlcgen.GetSessionByTokenHashParams{
		TokenHash: tokenHash,
		ExpiresAt: now,
	}
	sess, err := r.queries.GetSessionByTokenHash(ctx, params)
	if err != nil {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	return sess.UserID, nil
}

func (r *Repository) DeleteSession(ctx context.Context, rawSessionToken string) error {
	if !r.Enabled() {
		return nil
	}
	tokenHash := domain.HashToken(rawSessionToken)
	if tokenHash == "" {
		return nil
	}
	return r.queries.DeleteSessionByTokenHash(ctx, tokenHash)
}

func (r *Repository) DeleteSessionsByUser(ctx context.Context, userID uuid.UUID) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteSessionsByUserID(ctx, userID)
}

func (r *Repository) UpdateActiveView(ctx context.Context, userID uuid.UUID, view domain.AppView) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateUserSessionsView(ctx, sqlcgen.UpdateUserSessionsViewParams{
		UserID:     userID,
		ActiveView: string(view),
	})
}

func (r *Repository) DeleteUser(ctx context.Context, userID uuid.UUID) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteUserByID(ctx, userID)
}

func (r *Repository) UpsertOTP(ctx context.Context, record domain.OTPRecord) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertOTPCode(ctx, sqlcgen.UpsertOTPCodeParams{
		Email:          record.Email,
		CodeHash:       record.CodeHash,
		Attempts:       record.Attempts,
		Verified:       record.Verified,
		ResetTokenHash: record.ResetTokenHash,
		Used:           record.Used,
		ExpiresAt:      record.ExpiresAt,
		ResetExpiresAt: record.ResetExpiresAt,
		CreatedAt:      record.CreatedAt,
	})
}

func (r *Repository) GetOTP(ctx context.Context, email string) (domain.OTPRecord, error) {
	if !r.Enabled() {
		return domain.OTPRecord{}, domain.ErrOTPInvalidOrExpired
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	row, err := r.queries.GetOTPCodeByEmail(ctx, normalized)
	if err != nil {
		return domain.OTPRecord{}, domain.ErrOTPInvalidOrExpired
	}
	return domain.OTPRecord{
		Email:          row.Email,
		CodeHash:       row.CodeHash,
		Attempts:       row.Attempts,
		Verified:       row.Verified,
		ResetTokenHash: row.ResetTokenHash,
		Used:           row.Used,
		ExpiresAt:      row.ExpiresAt,
		ResetExpiresAt: row.ResetExpiresAt,
		CreatedAt:      row.CreatedAt,
	}, nil
}

func (r *Repository) IncrementOTPAttempts(ctx context.Context, email string) error {
	if !r.Enabled() {
		return nil
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	return r.queries.IncrementOTPAttempts(ctx, normalized)
}

func (r *Repository) MarkOTPVerified(ctx context.Context, email, resetTokenHash string, resetExpiresAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	return r.queries.MarkOTPVerified(ctx, sqlcgen.MarkOTPVerifiedParams{
		Email:          normalized,
		ResetTokenHash: resetTokenHash,
		ResetExpiresAt: resetExpiresAt,
	})
}

func (r *Repository) MarkOTPUsed(ctx context.Context, email string) error {
	if !r.Enabled() {
		return nil
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	return r.queries.MarkOTPUsed(ctx, normalized)
}

func (r *Repository) ConsumeOTPResetToken(ctx context.Context, email, resetTokenHash string) (bool, error) {
	if !r.Enabled() {
		return true, nil
	}
	trimmed := strings.TrimSpace(email)
	normalized := strings.ToLower(trimmed)
	rows, err := r.queries.ConsumeOTPResetToken(ctx, sqlcgen.ConsumeOTPResetTokenParams{
		Email:          normalized,
		ResetTokenHash: resetTokenHash,
	})
	if err != nil {
		return false, err
	}
	return rows > 0, nil
}

func (r *Repository) ListAdminClients(ctx context.Context) ([]domain.AdminClient, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListAdminClients(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.AdminClient, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.AdminClient{
			ID:          row.ID,
			FullName:    row.FullName,
			Email:       row.Email,
			Phone:       row.Phone,
			Notes:       row.Notes,
			EventsCount: row.EventsCount,
			TotalSpent:  row.TotalSpentDop,
			BalanceDop:  row.BalanceDop,
			UpdatedAt:   row.UpdatedAt,
		})
	}
	return out, nil
}

func (r *Repository) UpsertAdminClient(ctx context.Context, client domain.AdminClient) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertAdminClient(ctx, sqlcgen.UpsertAdminClientParams{
		ID:            client.ID,
		FullName:      client.FullName,
		Email:         client.Email,
		Phone:         client.Phone,
		Notes:         client.Notes,
		EventsCount:   client.EventsCount,
		TotalSpentDop: client.TotalSpent,
		BalanceDop:    client.BalanceDop,
		UpdatedAt:     client.UpdatedAt,
	})
}

func (r *Repository) ListStaffMembers(ctx context.Context) ([]domain.StaffRecord, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListStaffMembers(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]domain.StaffRecord, 0, len(rows))
	for _, row := range rows {
		perms := splitCommaPermissions(row.Permissions)
		out = append(out, domain.StaffRecord{
			ID:          row.ID,
			UserID:      row.UserID,
			FullName:    row.FullName,
			Email:       row.Email,
			Phone:       row.Phone,
			RoleLabel:   row.RoleLabel,
			Role:        row.Role,
			Permissions: perms,
			Active:      row.Active,
			CreatedAt:   row.CreatedAt,
		})
	}
	return out, nil
}

func (r *Repository) UpsertStaffMember(ctx context.Context, staff domain.StaffRecord) error {
	if !r.Enabled() {
		return nil
	}
	joined := strings.Join(staff.Permissions, ",")
	return r.queries.UpsertStaffMember(ctx, sqlcgen.UpsertStaffMemberParams{
		ID:          staff.ID,
		UserID:      staff.UserID,
		FullName:    staff.FullName,
		Email:       staff.Email,
		Phone:       staff.Phone,
		RoleLabel:   staff.RoleLabel,
		Role:        staff.Role,
		Permissions: joined,
		Active:      staff.Active,
		CreatedAt:   staff.CreatedAt,
	})
}

func (r *Repository) UpdateUserPreferences(ctx context.Context, userID uuid.UUID, prefs domain.UserPreferences) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpdateUserPreferencesByID(ctx, sqlcgen.UpdateUserPreferencesByIDParams{
		ID:                       userID,
		PushNotificationsEnabled: prefs.PushNotificationsEnabled,
		WhatsappAlertsEnabled:    prefs.WhatsAppAlertsEnabled,
		EmailReceiptsEnabled:     prefs.EmailReceiptsEnabled,
		BiometricLockEnabled:     prefs.BiometricLockEnabled,
		AutoSyncWifiOnly:         prefs.AutoSyncWifiOnly,
		EventRemindersEnabled:    prefs.EventRemindersEnabled,
		PreferredLanguage:        prefs.PreferredLanguage,
		DarkThemeEnabled:         prefs.DarkThemeEnabled,
		SavedPaymentMethod:       prefs.SavedPaymentMethod,
		ProfileZone:              prefs.ProfileZone,
	})
}

func (r *Repository) RevokeAccessToken(ctx context.Context, tokenHash string, expiresAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	if strings.TrimSpace(tokenHash) == "" {
		return nil
	}
	return r.queries.UpsertRevokedToken(ctx, sqlcgen.UpsertRevokedTokenParams{
		TokenHash: tokenHash,
		ExpiresAt: expiresAt,
		CreatedAt: r.clock.Now(),
	})
}

func (r *Repository) IsAccessTokenRevoked(ctx context.Context, tokenHash string) (bool, error) {
	if !r.Enabled() {
		return false, nil
	}
	if strings.TrimSpace(tokenHash) == "" {
		return false, nil
	}
	return r.queries.IsTokenRevoked(ctx, sqlcgen.IsTokenRevokedParams{
		TokenHash: tokenHash,
		ExpiresAt: r.clock.Now(),
	})
}

func (r *Repository) DeleteExpiredMaintenanceRows(ctx context.Context, before time.Time) (int64, error) {
	if !r.Enabled() {
		return 0, nil
	}
	revokedCount, err := r.queries.DeleteExpiredRevokedTokens(ctx, before)
	if err != nil {
		return 0, err
	}
	sessCount, err := r.queries.DeleteExpiredSessions(ctx, before)
	if err != nil {
		return revokedCount, err
	}
	otpCount, err := r.queries.DeleteExpiredOTPCodes(ctx, before)
	if err != nil {
		return revokedCount + sessCount, err
	}
	return revokedCount + sessCount + otpCount, nil
}

func (r *Repository) DeleteStaffMember(ctx context.Context, staffID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DeleteStaffMemberByID(ctx, staffID)
}

func (r *Repository) ListUserFavorites(ctx context.Context, userID uuid.UUID) ([]string, error) {
	if !r.Enabled() {
		return nil, nil
	}
	return r.queries.ListUserFavorites(ctx, userID)
}

func (r *Repository) AddUserFavorite(ctx context.Context, userID uuid.UUID, articleID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.AddUserFavorite(ctx, sqlcgen.AddUserFavoriteParams{
		UserID:    userID,
		ArticleID: articleID,
		CreatedAt: r.clock.Now(),
	})
}

func (r *Repository) RemoveUserFavorite(ctx context.Context, userID uuid.UUID, articleID string) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.RemoveUserFavorite(ctx, sqlcgen.RemoveUserFavoriteParams{
		UserID:    userID,
		ArticleID: articleID,
	})
}

func (r *Repository) UpsertDevicePushToken(ctx context.Context, record domain.DevicePushToken) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.UpsertDevicePushToken(ctx, sqlcgen.UpsertDevicePushTokenParams{
		ID:         record.ID,
		UserID:     record.UserID,
		Platform:   record.Platform,
		Token:      record.Token,
		AppVariant: record.AppVariant,
		UpdatedAt:  record.UpdatedAt,
	})
}

func (r *Repository) DisableDevicePushToken(ctx context.Context, userID uuid.UUID, token string, updatedAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DisableDevicePushToken(ctx, sqlcgen.DisableDevicePushTokenParams{
		UserID:    userID,
		Token:     token,
		UpdatedAt: updatedAt,
	})
}

func (r *Repository) DisableAllUserPushTokens(ctx context.Context, userID uuid.UUID, updatedAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.DisableAllUserPushTokens(ctx, sqlcgen.DisableAllUserPushTokensParams{
		UserID:    userID,
		UpdatedAt: updatedAt,
	})
}

func (r *Repository) ListActivePushTokensForUser(ctx context.Context, userID uuid.UUID) ([]domain.DevicePushToken, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListActivePushTokensByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	return mapPushTokenRows(rows), nil
}

func (r *Repository) ListActiveAdminPushTokens(ctx context.Context) ([]domain.DevicePushToken, error) {
	if !r.Enabled() {
		return nil, nil
	}
	rows, err := r.queries.ListActiveAdminPushTokens(ctx)
	if err != nil {
		return nil, err
	}
	return mapPushTokenRows(rows), nil
}

func (r *Repository) SaveRefreshToken(ctx context.Context, record domain.RefreshTokenRecord) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.InsertRefreshToken(ctx, sqlcgen.InsertRefreshTokenParams{
		ID:        record.ID,
		UserID:    record.UserID,
		FamilyID:  record.FamilyID,
		TokenHash: record.TokenHash,
		Revoked:   record.Revoked,
		ExpiresAt: record.ExpiresAt,
		CreatedAt: record.CreatedAt,
		UpdatedAt: record.UpdatedAt,
	})
}

func (r *Repository) GetRefreshTokenByHash(ctx context.Context, tokenHash string) (domain.RefreshTokenRecord, error) {
	if !r.Enabled() {
		return domain.RefreshTokenRecord{}, domain.ErrInvalidCredentials
	}
	row, err := r.queries.GetRefreshTokenByHash(ctx, tokenHash)
	if err != nil {
		return domain.RefreshTokenRecord{}, domain.ErrInvalidCredentials
	}
	return domain.RefreshTokenRecord{
		ID:        row.ID,
		UserID:    row.UserID,
		FamilyID:  row.FamilyID,
		TokenHash: row.TokenHash,
		Revoked:   row.Revoked,
		ExpiresAt: row.ExpiresAt,
		CreatedAt: row.CreatedAt,
		UpdatedAt: row.UpdatedAt,
	}, nil
}

func (r *Repository) RevokeRefreshToken(ctx context.Context, tokenHash string, updatedAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.RevokeRefreshTokenByHash(ctx, sqlcgen.RevokeRefreshTokenByHashParams{
		TokenHash: tokenHash,
		UpdatedAt: updatedAt,
	})
}

func (r *Repository) RevokeRefreshTokenFamily(ctx context.Context, familyID uuid.UUID, updatedAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.RevokeRefreshTokenFamily(ctx, sqlcgen.RevokeRefreshTokenFamilyParams{
		FamilyID:  familyID,
		UpdatedAt: updatedAt,
	})
}

func (r *Repository) RevokeRefreshTokensByUser(ctx context.Context, userID uuid.UUID, updatedAt time.Time) error {
	if !r.Enabled() {
		return nil
	}
	return r.queries.RevokeRefreshTokensByUser(ctx, sqlcgen.RevokeRefreshTokensByUserParams{
		UserID:    userID,
		UpdatedAt: updatedAt,
	})
}

func (r *Repository) DeleteExpiredRefreshTokens(
	ctx context.Context,
	expiresBefore time.Time,
	revokedUpdatedBefore time.Time,
) (int64, error) {
	if !r.Enabled() {
		return 0, nil
	}
	return r.queries.DeleteExpiredOrRevokedRefreshTokens(ctx, sqlcgen.DeleteExpiredOrRevokedRefreshTokensParams{
		ExpiresAt: expiresBefore,
		UpdatedAt: revokedUpdatedBefore,
	})
}

func mapPushTokenRows(rows []sqlcgen.DevicePushToken) []domain.DevicePushToken {
	out := make([]domain.DevicePushToken, 0, len(rows))
	for _, row := range rows {
		out = append(out, domain.DevicePushToken{
			ID:         row.ID,
			UserID:     row.UserID,
			Platform:   row.Platform,
			Token:      row.Token,
			AppVariant: row.AppVariant,
			Enabled:    row.Enabled,
			UpdatedAt:  row.UpdatedAt,
		})
	}
	return out
}

func mapRowToDomainUser(row sqlcgen.User) domain.User {
	perms := splitCommaPermissions(row.Permissions)
	role := domain.Role(row.Role)
	activeView := defaultViewForRole(role)
	username := strings.TrimSpace(row.Username)
	if username == "" {
		emailParts := strings.Split(row.Email, "@")
		username = emailParts[0]
	}
	return domain.User{
		ID:          row.ID,
		Username:    username,
		FullName:    row.FullName,
		Email:       row.Email,
		Phone:       row.Phone,
		BirthDate:   row.BirthDate,
		AvatarKey:   row.AvatarKey,
		Active:      row.Active,
		Role:        role,
		Permissions: perms,
		ActiveView:  activeView,
		Preferences: domain.UserPreferences{
			PushNotificationsEnabled: row.PushNotificationsEnabled,
			WhatsAppAlertsEnabled:    row.WhatsappAlertsEnabled,
			EmailReceiptsEnabled:     row.EmailReceiptsEnabled,
			BiometricLockEnabled:     row.BiometricLockEnabled,
			AutoSyncWifiOnly:         row.AutoSyncWifiOnly,
			EventRemindersEnabled:    row.EventRemindersEnabled,
			PreferredLanguage:        row.PreferredLanguage,
			DarkThemeEnabled:         row.DarkThemeEnabled,
			SavedPaymentMethod:       row.SavedPaymentMethod,
			ProfileZone:              row.ProfileZone,
		},
		CreatedAt:    row.CreatedAt,
		PasswordHash: row.PasswordHash,
	}
}

func defaultViewForRole(role domain.Role) domain.AppView {
	if role == domain.RoleAdmin {
		return domain.ViewAdmin
	}
	if role == domain.RoleStaff {
		return domain.ViewAdmin
	}
	return domain.ViewPersonal
}

func splitCommaPermissions(raw string) []string {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return []string{}
	}
	return strings.Split(trimmed, ",")
}
