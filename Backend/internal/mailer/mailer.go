package mailer

import "embed"

const (
	FromName                   = "AdventistaSc"
	MaxRetries                 = 3
	UserWelcomeTemplate        = "user_invitation.tmpl"
	EventReminder7dTemplate    = "event_reminder_7_days.tmpl"
	EventReminder24hTemplate   = "event_reminder_24h.tmpl"
	EventThankYouTemplate      = "event_thank_you.tmpl"
	PasswordResetTemplate      = "password_reset.tmpl"
)

//go:embed "templates"
var FS embed.FS

type Client interface {
	Send(templateFile, userName, email string, data any, isSandbox bool) (int, error)
}
