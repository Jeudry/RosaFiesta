package dtos

import "Backend/internal/store/models"

type UserWithToken struct {
	*models.User
	Token string `json:"token"`
}
