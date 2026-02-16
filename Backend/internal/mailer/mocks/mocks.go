package mocks

import (
	"github.com/stretchr/testify/mock"
)

type Mailer struct {
	mock.Mock
}

func (m *Mailer) Send(templateFile, userName, email string, data any, isSandbox bool) (int, error) {
	args := m.Called(templateFile, userName, email, data, isSandbox)
	return args.Int(0), args.Error(1)
}
