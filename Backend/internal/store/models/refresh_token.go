package models

import (
	"github.com/google/uuid"
	"time"
)

// RefreshToken represents a refresh token in the system
type RefreshToken struct {
	BaseModel
	UserID    uuid.UUID `json:"user_id"`
	Token     string    `json:"token"`
	ExpiresAt time.Time `json:"expires_at"`
}
