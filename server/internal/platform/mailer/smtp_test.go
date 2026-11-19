package mailer_test

import (
	"context"
	"errors"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
)

func TestSMTPSenderReturnsNotConfiguredWhenHostMissing(t *testing.T) {
	sender := mailer.NewSMTPSender(mailer.SMTPConfig{})
	if sender.IsConfigured() {
		t.Fatal("expected IsConfigured false without host")
	}
	err := sender.SendEmail(context.Background(), "cliente@rosafiesta.do", "Prueba", "Hola")
	if !errors.Is(err, mailer.ErrNotConfigured) {
		t.Fatalf("expected ErrNotConfigured, got %v", err)
	}
}
