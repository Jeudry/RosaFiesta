package domain

import (
	"strings"

	"golang.org/x/crypto/bcrypt"
)

// HashPassword genera un hash bcrypt seguro para almacenar contraseñas sin exponer texto plano.
func HashPassword(plain string) (string, error) {
	trimmed := strings.TrimSpace(plain)
	if len(trimmed) < 6 {
		return "", ErrInvalidInput
	}
	bytes, err := bcrypt.GenerateFromPassword([]byte(trimmed), bcrypt.MinCost)
	if err != nil {
		return "", err
	}
	return string(bytes), nil
}

// VerifyPassword comprueba en tiempo constante si plain coincide con el hash bcrypt almacenado.
func VerifyPassword(passwordHash, plain string) bool {
	trimmedPlain := strings.TrimSpace(plain)
	trimmedHash := strings.TrimSpace(passwordHash)
	if trimmedPlain == "" || trimmedHash == "" {
		return false
	}
	return bcrypt.CompareHashAndPassword([]byte(trimmedHash), []byte(trimmedPlain)) == nil
}
