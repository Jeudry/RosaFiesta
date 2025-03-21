package mailer

import (
	"bytes"
	"errors"
	gomail "gopkg.in/mail.v2"
	"html/template"
	"log"
)

type MailTrapClient struct {
	fromEmail string
	apiKey    string
}

func NewMailTrapClient(apiKey, fromEmail string) (MailTrapClient, error) {
	if apiKey == "" {
		return MailTrapClient{}, errors.New("main key is required")
	}

	return MailTrapClient{
		fromEmail: fromEmail,
		apiKey:    apiKey,
	}, nil
}

func (m MailTrapClient) Send(templateFile, username, email string, data any, isSandbox bool) (int, error) {
	tmpl, err := template.ParseFS(FS, "templates/"+templateFile)
	if err != nil {
		return -1, err
	}

	subject := new(bytes.Buffer)
	err = tmpl.ExecuteTemplate(subject, "subject", data)
	if err != nil {
		return -1, err
	}

	body := new(bytes.Buffer)
	err = tmpl.ExecuteTemplate(body, "body", data)
	if err != nil {
		return -1, err
	}

	message := gomail.NewMessage()
	message.SetHeader("From", m.fromEmail)
	message.SetHeader("To", email)
	message.SetHeader("Subject", subject.String())

	message.AddAlternative("text/html", body.String())

	dialer := gomail.NewDialer("live.smtp.mailtrap.io", 587, "main", m.apiKey)

	if err := dialer.DialAndSend(message); err != nil {
		log.Println(err)
		return -1, err
	}

	return 200, nil
}
