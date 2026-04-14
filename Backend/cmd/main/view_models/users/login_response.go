package users

import "github.com/google/uuid"

type LoginResponse struct {
	AccessToken                    string           `json:"accessToken"`
	UserId                         uuid.UUID        `json:"userId"`
	AccessTokenExpirationTimestamp int64            `json:"accessTokenExpirationTimestamp"`
	RefreshToken                   string           `json:"refreshToken"`
	PendingEvents                  []PendingEvent   `json:"pendingEvents,omitempty"`
}

type PendingEvent struct {
	ID            uuid.UUID  `json:"id"`
	Name          string     `json:"name"`
	Date          *string    `json:"date,omitempty"`
	Status        string     `json:"status"`
	PaymentStatus string     `json:"paymentStatus"`
}
