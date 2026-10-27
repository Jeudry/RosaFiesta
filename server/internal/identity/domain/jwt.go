package domain

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"strings"
	"time"
)

const (
	jwtSigningSecret   = "rf-2026-jwt-hs256-signing-secret-key-do-santo-domingo" //nolint:gosec // clave de firma local por defecto
	defaultJwtTTLHours = 24 * 7
)

type jwtClaimsPayload struct {
	SubjectID string `json:"sub"`
	Username  string `json:"username"`
	Email     string `json:"email"`
	FullName  string `json:"name"`
	Role      string `json:"role"`
	BirthDate string `json:"birthdate"`
	IssuedAt  int64  `json:"iat"`
	ExpiresAt int64  `json:"exp"`
}

// IssueAccessToken emits a signed RFC 7519 HS256 JWT token for the given user.
func IssueAccessToken(user User, issuedAt time.Time) string {
	iat := issuedAt.Unix()
	exp := issuedAt.Add(defaultJwtTTLHours * time.Hour).Unix()
	username := strings.TrimSpace(user.Username)
	if username == "" {
		username = strings.Split(user.Email, "@")[0]
	}
	payload := jwtClaimsPayload{
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
	signature := computeHmacSha256(signingInput)
	return signingInput + "." + signature
}

// VerifyAccessToken validates the HS256 signature of a JWT token and returns the subject user ID.
func VerifyAccessToken(token string) (string, bool) {
	parts := strings.Split(strings.TrimSpace(token), ".")
	if len(parts) != 3 {
		return "", false
	}
	signingInput := parts[0] + "." + parts[1]
	expectedSig := computeHmacSha256(signingInput)
	if !hmac.Equal([]byte(parts[2]), []byte(expectedSig)) {
		return "", false
	}
	payloadBytes, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return "", false
	}
	var payload jwtClaimsPayload
	if err := json.Unmarshal(payloadBytes, &payload); err != nil {
		return "", false
	}
	if strings.TrimSpace(payload.SubjectID) == "" {
		return "", false
	}
	return payload.SubjectID, true
}

func computeHmacSha256(message string) string {
	mac := hmac.New(sha256.New, []byte(jwtSigningSecret))
	_, _ = mac.Write([]byte(message))
	return base64.RawURLEncoding.EncodeToString(mac.Sum(nil))
}
