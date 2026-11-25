package domain

import (
	"crypto/hmac"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"strings"
	"time"
)

const (
	defaultJWTTTLHours   = 24 * 7
	ephemeralKeyByteSize = 32
	jwtIDByteSize        = 16
)

type jwtClaimsPayload struct {
	TokenID   string `json:"jti,omitempty"`
	SubjectID string `json:"sub"`
	Username  string `json:"username"`
	Email     string `json:"email"`
	FullName  string `json:"name"`
	Role      string `json:"role"`
	BirthDate string `json:"birthdate"`
	IssuedAt  int64  `json:"iat"`
	ExpiresAt int64  `json:"exp"`
}

type TokenManager struct {
	secret []byte
}

func NewTokenManager(secret string) TokenManager {
	trimmed := strings.TrimSpace(secret)
	if trimmed != "" {
		return TokenManager{secret: []byte(trimmed)}
	}
	buf := make([]byte, ephemeralKeyByteSize)
	_, _ = rand.Read(buf)
	return TokenManager{secret: buf}
}

func (tm TokenManager) Issue(user User, issuedAt time.Time) string {
	return tm.IssueWithTTL(user, issuedAt, defaultJWTTTLHours*time.Hour)
}

func (tm TokenManager) IssueWithTTL(user User, issuedAt time.Time, ttl time.Duration) string {
	iat := issuedAt.Unix()
	exp := issuedAt.Add(ttl).Unix()
	username := strings.TrimSpace(user.Username)
	if username == "" {
		parts := strings.Split(user.Email, "@")
		username = parts[0]
	}
	payload := jwtClaimsPayload{
		TokenID:   newTokenID(),
		SubjectID: user.ID.String(),
		Username:  username,
		Email:     user.Email,
		FullName:  user.FullName,
		Role:      string(user.Role),
		BirthDate: user.BirthDate,
		IssuedAt:  iat,
		ExpiresAt: exp,
	}
	headerBytes := []byte(`{"alg":"HS256","typ":"JWT"}`)
	payloadBytes, err := json.Marshal(payload)
	if err != nil {
		return ""
	}
	encodedHeader := base64.RawURLEncoding.EncodeToString(headerBytes)
	encodedPayload := base64.RawURLEncoding.EncodeToString(payloadBytes)
	signingInput := encodedHeader + "." + encodedPayload
	signature := tm.computeHMAC(signingInput)
	return signingInput + "." + signature
}

func (tm TokenManager) Verify(token string, now time.Time) (string, error) {
	trimmed := strings.TrimSpace(token)
	parts := strings.Split(trimmed, ".")
	if len(parts) != 3 {
		return "", ErrInvalidCredentials
	}
	signingInput := parts[0] + "." + parts[1]
	expectedSig := tm.computeHMAC(signingInput)
	sigMatches := hmac.Equal([]byte(parts[2]), []byte(expectedSig))
	if !sigMatches {
		return "", ErrInvalidCredentials
	}
	payloadBytes, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return "", ErrInvalidCredentials
	}
	var payload jwtClaimsPayload
	if err := json.Unmarshal(payloadBytes, &payload); err != nil {
		return "", ErrInvalidCredentials
	}
	subjectID := strings.TrimSpace(payload.SubjectID)
	if subjectID == "" {
		return "", ErrInvalidCredentials
	}
	if payload.ExpiresAt <= 0 {
		return "", ErrTokenExpired
	}
	if now.Unix() >= payload.ExpiresAt {
		return "", ErrTokenExpired
	}
	return subjectID, nil
}

func (tm TokenManager) computeHMAC(message string) string {
	mac := hmac.New(sha256.New, tm.secret)
	_, _ = mac.Write([]byte(message))
	sum := mac.Sum(nil)
	return base64.RawURLEncoding.EncodeToString(sum)
}

func newTokenID() string {
	buf := make([]byte, jwtIDByteSize)
	_, _ = rand.Read(buf)
	return base64.RawURLEncoding.EncodeToString(buf)
}
