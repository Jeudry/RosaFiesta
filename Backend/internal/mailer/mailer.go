package mailer

import "embed"

const (
	FromName            = "AdventistaSc"
	MaxRetries          = 3
	UserWelcomeTemplate = "user_invitation.tmpl"
)

//go:embed "templates"
var FS embed.FS

type Client interface {
	Send(templateFile, userName, email string, data any, isSandbox bool) (int, error)
}
