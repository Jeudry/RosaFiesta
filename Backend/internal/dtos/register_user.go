package dtos

type RegisterUserPayload struct {
	Username        string `json:"username" validate:"required,max=100"`
	FirstName       string `json:"first_name" validate:"required,max=100"`
	LastName        string `json:"last_name" validate:"required,max=100"`
	Email           string `json:"email" validate:"required,email,max=255"`
	Password        string `json:"password" validate:"required,min=3,max=72"`
	ConfirmPassword string `json:"confirm_password" validate:"required,eqfield=Password"`
}
