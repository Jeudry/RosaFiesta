package mailer

import (
	"bytes"
	"errors"
	"gopkg.in/gomail.v2"
	"html/template"
	"log"
)

type GoMailClient struct {
	fromEmail string
	password  string
}

func (m GoMailClient) Send(templateFile, userName, email string, data any, isSandbox bool) (int, error) {
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

	dialer := gomail.NewDialer("smtp.gmail.com", 587, "jeudrypp@gmail.com", m.password)

	if err := dialer.DialAndSend(message); err != nil {
		log.Println(err)
		return -1, err
	}

	return 200, nil
}

func NewGoMailClient(password, fromEmail string) (GoMailClient, error) {
	if fromEmail == "" {
		return GoMailClient{}, errors.New("main key is required")
	}

	return GoMailClient{
		fromEmail: fromEmail,
		password:  password,
	}, nil
}
