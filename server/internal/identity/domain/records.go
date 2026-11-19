package domain

import (
	"crypto/rand"
	"crypto/subtle"
	"encoding/base64"
	"strings"
	"time"

	"github.com/google/uuid"
)

const MaxOTPVerificationAttempts = 5

type OTPRecord struct {
	Email          string
	CodeHash       string
	Attempts       int
	Verified       bool
	ResetTokenHash string
	Used           bool
	ExpiresAt      time.Time
	ResetExpiresAt time.Time
	CreatedAt      time.Time
}

func (r OTPRecord) IsCodeValid(candidateHash string, now time.Time) bool {
	if r.Used {
		return false
	}
	if r.Attempts >= MaxOTPVerificationAttempts {
		return false
	}
	if !now.Before(r.ExpiresAt) {
		return false
	}
	if r.CodeHash == "" {
		return false
	}
	equal := subtle.ConstantTimeCompare([]byte(r.CodeHash), []byte(candidateHash))
	return equal == 1
}

func (r OTPRecord) IsResetTokenValid(candidateResetHash string, now time.Time) bool {
	if r.Used {
		return false
	}
	if !r.Verified {
		return false
	}
	if r.ResetTokenHash == "" {
		return false
	}
	if !now.Before(r.ResetExpiresAt) {
		return false
	}
	equal := subtle.ConstantTimeCompare([]byte(r.ResetTokenHash), []byte(candidateResetHash))
	return equal == 1
}

type AdminClient struct {
	ID          string    `json:"id" doc:"Client identifier."`
	FullName    string    `json:"fullName" doc:"Client full name."`
	Email       string    `json:"email" doc:"Client email address."`
	Phone       string    `json:"phone" doc:"Client phone number."`
	Notes       string    `json:"notes,omitempty" doc:"Internal operational notes."`
	EventsCount int       `json:"eventsCount" doc:"Total events contracted."`
	TotalSpent  int       `json:"totalSpentDop" doc:"Total contracted amount in DOP."`
	BalanceDop  int       `json:"balanceDop" doc:"Outstanding balance in DOP."`
	UpdatedAt   time.Time `json:"-"`
}

func (c AdminClient) ApplyPatch(fullName, email, phone, notes *string, now time.Time) (AdminClient, error) {
	updated := c
	if fullName != nil {
		trimmed := strings.TrimSpace(*fullName)
		if trimmed == "" {
			return AdminClient{}, ErrInvalidInput
		}
		updated.FullName = trimmed
	}
	if email != nil {
		normalized, err := NormalizeEmail(*email)
		if err != nil {
			return AdminClient{}, err
		}
		updated.Email = normalized
	}
	if phone != nil {
		updated.Phone = strings.TrimSpace(*phone)
	}
	if notes != nil {
		updated.Notes = strings.TrimSpace(*notes)
	}
	updated.UpdatedAt = now
	return updated, nil
}

type StaffRecord struct {
	ID          string    `json:"id" doc:"Staff member identifier."`
	UserID      uuid.UUID `json:"-"`
	FullName    string    `json:"fullName" doc:"Staff member full name."`
	Email       string    `json:"email,omitempty" doc:"Staff member email address."`
	Phone       string    `json:"phone,omitempty" doc:"Staff member phone number."`
	RoleLabel   string    `json:"roleLabel" doc:"Display role title."`
	Role        string    `json:"role" doc:"Technical role (admin or staff)."`
	Permissions []string  `json:"permissions" doc:"Assigned operational permissions."`
	Active      bool      `json:"active" doc:"Whether the staff member is active."`
	CreatedAt   time.Time `json:"-"`
}

func (s StaffRecord) ApplyPatch(
	fullName *string,
	roleLabel *string,
	role *string,
	permissions *[]string,
	active *bool,
) (StaffRecord, error) {
	updated := s
	if fullName != nil {
		trimmed := strings.TrimSpace(*fullName)
		if trimmed == "" {
			return StaffRecord{}, ErrInvalidInput
		}
		updated.FullName = trimmed
	}
	if roleLabel != nil {
		updated.RoleLabel = strings.TrimSpace(*roleLabel)
	}
	if role != nil {
		trimmedRole := strings.TrimSpace(*role)
		normalizedRole := strings.ToLower(trimmedRole)
		validRole := isStaffOrAdminRole(normalizedRole)
		if !validRole {
			return StaffRecord{}, ErrInvalidInput
		}
		updated.Role = normalizedRole
	}
	if permissions != nil {
		normalizedPerms, err := NormalizeStaffPermissions(*permissions)
		if err != nil {
			return StaffRecord{}, err
		}
		updated.Permissions = normalizedPerms
	}
	if active != nil {
		updated.Active = *active
	}
	return updated, nil
}

func isStaffOrAdminRole(role string) bool {
	if role == string(RoleAdmin) {
		return true
	}
	return role == string(RoleStaff)
}

const refreshTokenByteLength = 32

type RefreshTokenRecord struct {
	ID        uuid.UUID
	UserID    uuid.UUID
	FamilyID  uuid.UUID
	TokenHash string
	Revoked   bool
	ExpiresAt time.Time
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (r RefreshTokenRecord) IsUsable(now time.Time) bool {
	if r.Revoked {
		return false
	}
	if r.TokenHash == "" {
		return false
	}
	return now.Before(r.ExpiresAt)
}

func NewRefreshTokenRecord(
	userID uuid.UUID,
	familyID uuid.UUID,
	tokenHash string,
	now time.Time,
	ttl time.Duration,
) RefreshTokenRecord {
	resolvedFamily := familyID
	if resolvedFamily == uuid.Nil {
		resolvedFamily = uuid.New()
	}
	return RefreshTokenRecord{
		ID:        uuid.New(),
		UserID:    userID,
		FamilyID:  resolvedFamily,
		TokenHash: tokenHash,
		Revoked:   false,
		ExpiresAt: now.Add(ttl),
		CreatedAt: now,
		UpdatedAt: now,
	}
}

func GenerateRefreshToken() (string, error) {
	buf := make([]byte, refreshTokenByteLength)
	if _, err := rand.Read(buf); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(buf), nil
}
