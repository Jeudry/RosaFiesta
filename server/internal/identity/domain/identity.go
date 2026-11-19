package domain

import (
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"math/big"
	"strings"
	"time"

	"github.com/google/uuid"
)

type Role string

const (
	RoleClient Role = "client"
	RoleAdmin  Role = "admin"
	RoleStaff  Role = "staff"
)

type AppView string

const (
	ViewPersonal AppView = "personal"
	ViewAdmin    AppView = "admin"
)

var (
	ErrInvalidCredentials          = errors.New("invalid email or password")
	ErrTokenExpired                = errors.New("access token has expired")
	ErrEmailAlreadyRegistered      = errors.New("email already registered")
	ErrUsernameAlreadyTaken        = errors.New("username already taken")
	ErrInvalidInput                = errors.New("invalid identity input")
	ErrActiveEventsPreventDeletion = errors.New("cannot delete account with active events")
	ErrForbiddenView               = errors.New("role is not allowed to enter requested app view")
	ErrPermissionDenied            = errors.New("staff member lacks required permission")
	ErrUserNotFound                = errors.New("account not found")
	ErrOTPInvalidOrExpired         = errors.New("verification code is invalid or expired")
	ErrResetTokenInvalid           = errors.New("password reset token is missing, invalid, or expired")
	ErrSMTPNotConfigured           = errors.New("smtp email provider is not configured on server")
	ErrOAuthProviderNotConfigured  = errors.New("oauth provider is not configured on server")
	ErrOAuthInvalidToken           = errors.New("oauth identity token is invalid or expired")
)

var canonicalStaffPermissions = map[string]struct{}{
	"eventos":    {},
	"decoracion": {},
	"tareas":     {},
	"inventario": {},
	"pagos":      {},
	"finanzas":   {},
	"reembolsos": {},
	"reportes":   {},
	"ajustes":    {},
}

type UserPreferences struct {
	PushNotificationsEnabled bool `json:"pushNotificationsEnabled"`
	WhatsAppAlertsEnabled    bool `json:"whatsappAlertsEnabled"`
	EmailReceiptsEnabled     bool `json:"emailReceiptsEnabled"`
	BiometricLockEnabled     bool `json:"biometricLockEnabled"`
	AutoSyncWifiOnly         bool `json:"autoSyncWifiOnly"`
}

func DefaultUserPreferences() UserPreferences {
	return UserPreferences{
		PushNotificationsEnabled: true,
		WhatsAppAlertsEnabled:    true,
		EmailReceiptsEnabled:     true,
		BiometricLockEnabled:     false,
		AutoSyncWifiOnly:         false,
	}
}

type User struct {
	ID           uuid.UUID       `json:"id"`
	Username     string          `json:"username"`
	FullName     string          `json:"fullName"`
	Email        string          `json:"email"`
	Phone        string          `json:"phone"`
	BirthDate    string          `json:"birthDate"`
	AvatarKey    string          `json:"avatarKey,omitempty"`
	Role         Role            `json:"role"`
	Permissions  []string        `json:"permissions"`
	ActiveView   AppView         `json:"activeView"`
	Preferences  UserPreferences `json:"preferences"`
	CreatedAt    time.Time       `json:"createdAt"`
	PasswordHash string          `json:"-"`
}

type DevicePushToken struct {
	ID         uuid.UUID `json:"id"`
	UserID     uuid.UUID `json:"userId"`
	Platform   string    `json:"platform"`
	Token      string    `json:"token"`
	AppVariant string    `json:"appVariant"`
	Enabled    bool      `json:"enabled"`
	UpdatedAt  time.Time `json:"updatedAt"`
}

var allowedPushPlatforms = map[string]struct{}{
	"android": {},
	"ios":     {},
	"web":     {},
	"desktop": {},
}

func NormalizePushPlatform(raw string) (string, error) {
	clean := strings.ToLower(strings.TrimSpace(raw))
	if _, ok := allowedPushPlatforms[clean]; ok {
		return clean, nil
	}
	return "", ErrInvalidInput
}

func NormalizeAppVariant(raw string) (string, error) {
	clean := strings.ToLower(strings.TrimSpace(raw))
	if clean == "" {
		return "personal", nil
	}
	if clean == "personal" {
		return "personal", nil
	}
	if clean == "admin" {
		return "admin", nil
	}
	return "", ErrInvalidInput
}

func (u User) CanAccessView(view AppView) bool {
	if view == ViewPersonal {
		if u.Role == RoleClient {
			return true
		}
		return u.Role == RoleAdmin
	}
	if view == ViewAdmin {
		if u.Role == RoleAdmin {
			return true
		}
		return u.Role == RoleStaff
	}
	return false
}

func (u User) HasPermission(permission string) bool {
	if u.Role == RoleAdmin {
		return true
	}
	if u.Role != RoleStaff {
		return false
	}
	trimmedTarget := strings.TrimSpace(permission)
	target := strings.ToLower(trimmedTarget)
	for _, item := range u.Permissions {
		trimmedItem := strings.TrimSpace(item)
		if strings.ToLower(trimmedItem) == target {
			return true
		}
	}
	return false
}

func ValidateAccountDeletion(activeEventsCount int) error {
	if activeEventsCount > 0 {
		return ErrActiveEventsPreventDeletion
	}
	return nil
}

func NormalizeEmail(raw string) (string, error) {
	trimmed := strings.TrimSpace(raw)
	cleaned := strings.ToLower(trimmed)
	if len(cleaned) < 5 {
		return "", ErrInvalidInput
	}
	if !strings.Contains(cleaned, "@") {
		return "", ErrInvalidInput
	}
	atIdx := strings.IndexByte(cleaned, '@')
	domainPart := cleaned[atIdx+1:]
	if !strings.Contains(domainPart, ".") {
		return "", ErrInvalidInput
	}
	return cleaned, nil
}

func NormalizeStaffPermissions(raw []string) ([]string, error) {
	seen := make(map[string]struct{}, len(raw))
	out := make([]string, 0, len(raw))
	for _, item := range raw {
		trimmed := strings.TrimSpace(item)
		lower := strings.ToLower(trimmed)
		if lower == "" {
			continue
		}
		_, allowed := canonicalStaffPermissions[lower]
		if !allowed {
			return nil, ErrInvalidInput
		}
		_, duplicate := seen[lower]
		if !duplicate {
			seen[lower] = struct{}{}
			out = append(out, lower)
		}
	}
	return out, nil
}

func HashToken(raw string) string {
	trimmed := strings.TrimSpace(raw)
	if trimmed == "" {
		return ""
	}
	sum := sha256.Sum256([]byte(trimmed))
	return hex.EncodeToString(sum[:])
}

func GenerateDigitsOTP() (string, error) {
	maxVal := big.NewInt(1_000_000)
	num, err := rand.Int(rand.Reader, maxVal)
	if err != nil {
		return "", err
	}
	return fmt.Sprintf("%06d", num.Int64()), nil
}

func GenerateRandomHex(numBytes int) (string, error) {
	buf := make([]byte, numBytes)
	if _, err := rand.Read(buf); err != nil {
		return "", err
	}
	return hex.EncodeToString(buf), nil
}
