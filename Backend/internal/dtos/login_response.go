package dtos

import "github.com/google/uuid"

type LoginResponse struct {
	AccessToken                    string    `json:"accessToken"`
	UserId                         uuid.UUID `json:"userId"`
	AccessTokenExpirationTimestamp int64     `json:"accessTokenExpirationTimestamp"`
	RefreshToken                   string    `json:"refreshToken"`
}
