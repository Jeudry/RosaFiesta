package models

import (
	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

type User struct {
	ID          uuid.UUID  `json:"id"`
	UserName    string     `json:"userName"`
	FirstName   string     `json:"firstName"`
	LastName    string     `json:"lastName"`
	Email       string     `json:"email"`
	PhoneNumber string     `json:"phone_number"`
	Password    password   `json:"-"`
	Avatar      string     `json:"avatar"`
	BornDate    string     `json:"born_date"`
	CreatedAt   string     `json:"created_at"`
	UpdatedAt   string     `json:"updated_at"`
	IsActive    bool       `json:"is_active"`
	RoleID      uuid.UUID  `json:"role_id"`
	Role        Role       `json:"role"`
	Params      UserParams `json:"-"`
}

type UserParams struct {
	FirstName string `json:"firstName"`
	LastName  string `json:"lastName"`
	Username  string `json:"username"`
	Email     string `json:"email"`
	Password  string `json:"password"`
}

type password struct {
	text *string
	Hash []byte
}

func (p *password) Set(password string) error {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return err
	}

	p.text = &password
	p.Hash = hash

	return nil
}

func (p *password) Compare(password string) error {
	return bcrypt.CompareHashAndPassword(p.Hash, []byte(password))
}

type UserWithToken struct {
	*User
	Token string `json:"token"`
}

type UserToken struct {
	AccessToken                    string `json:"access_token"`
	RefreshToken                   string `json:"refresh_token"`
	AccessTokenExpirationTimestamp int64  `json:"access_token_expiration_timestamp"`
	UserID                         string `json:"user_id"`
}
