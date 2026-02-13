package config

import "time"

type MailConfig struct {
	Exp       time.Duration
	SendGrid  SendGridConfig
	FromEmail string
	MailTrap  MailTrapConfig
	Password  string
}
