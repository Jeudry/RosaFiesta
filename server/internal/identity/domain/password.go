package domain

import (
	"strings"

	"golang.org/x/crypto/bcrypt"
)

func HashPassword(plain string) (string, error) {
	trimmed := strings.TrimSpace(plain)
	if len(trimmed) < 6 {
		return "", ErrInvalidInput
	}
	bytes, err := bcrypt.GenerateFromPassword([]byte(trimmed), bcrypt.DefaultCost)
	if err != nil {
		return "", err
	}
	return string(bytes), nil
}

func VerifyPassword(passwordHash, plain string) bool {
	trimmedPlain := strings.TrimSpace(plain)
	trimmedHash := strings.TrimSpace(passwordHash)
	if trimmedPlain == "" {
		return false
	}
	if trimmedHash == "" {
		return false
	}
	err := bcrypt.CompareHashAndPassword([]byte(trimmedHash), []byte(trimmedPlain))
	return err == nil
}
