// Package mailer envía correos transaccionales reales vía SMTP configurado por entorno.
package mailer

import (
	"context"
	"crypto/tls"
	"errors"
	"fmt"
	"net"
	"net/smtp"
	"strings"
	"time"
)

// ErrNotConfigured se retorna cuando no hay un servidor SMTP externo configurado.
var ErrNotConfigured = errors.New("smtp provider is not configured")

// SMTPConfig define los parámetros de conexión al servidor SMTP externo.
type SMTPConfig struct {
	Host     string
	Port     int
	From     string
	Username string
	Password string
	Timeout  time.Duration
}

// Sender envía correos electrónicos transaccionales.
type Sender interface {
	IsConfigured() bool
	SendEmail(ctx context.Context, to, subject, textBody string) error
}

// SMTPSender implementa Sender usando net/smtp sobre TLS/STARTTLS.
type SMTPSender struct {
	cfg SMTPConfig
}

// NewSMTPSender construye un remitente SMTP real a partir de la configuración.
func NewSMTPSender(cfg SMTPConfig) *SMTPSender {
	if cfg.Timeout <= 0 {
		cfg.Timeout = 12 * time.Second
	}
	if strings.TrimSpace(cfg.From) == "" {
		cfg.From = "no-reply@rosafiesta.do"
	}
	if cfg.Port <= 0 {
		cfg.Port = 587
	}
	return &SMTPSender{cfg: cfg}
}

// IsConfigured indica si existe un host SMTP externo válido.
func (s *SMTPSender) IsConfigured() bool {
	if s == nil {
		return false
	}
	return strings.TrimSpace(s.cfg.Host) != ""
}

// SendEmail envía un correo en texto plano al destinatario usando el servidor SMTP configurado.
func (s *SMTPSender) SendEmail(ctx context.Context, to, subject, textBody string) error {
	if !s.IsConfigured() {
		return ErrNotConfigured
	}
	recipient := strings.TrimSpace(to)
	if recipient == "" {
		return errors.New("recipient email is required")
	}
	addr := net.JoinHostPort(strings.TrimSpace(s.cfg.Host), fmt.Sprintf("%d", s.cfg.Port))
	msg := buildRFC822Message(s.cfg.From, recipient, subject, textBody)
	return s.deliver(ctx, addr, recipient, msg)
}

func (s *SMTPSender) deliver(ctx context.Context, addr, recipient string, msg []byte) error {
	dialer := net.Dialer{Timeout: s.cfg.Timeout}
	conn, err := dialer.DialContext(ctx, "tcp", addr)
	if err != nil {
		return fmt.Errorf("smtp dial %s: %w", addr, err)
	}
	client, err := s.openClient(conn)
	if err != nil {
		_ = conn.Close()
		return err
	}
	defer func() { _ = client.Close() }()

	if err := s.authenticate(client); err != nil {
		return err
	}
	if err := client.Mail(s.cfg.From); err != nil {
		return fmt.Errorf("smtp MAIL FROM: %w", err)
	}
	if err := client.Rcpt(recipient); err != nil {
		return fmt.Errorf("smtp RCPT TO: %w", err)
	}
	writer, err := client.Data()
	if err != nil {
		return fmt.Errorf("smtp DATA: %w", err)
	}
	if _, err := writer.Write(msg); err != nil {
		return fmt.Errorf("smtp write body: %w", err)
	}
	if err := writer.Close(); err != nil {
		return fmt.Errorf("smtp commit body: %w", err)
	}
	return client.Quit()
}

func (s *SMTPSender) openClient(conn net.Conn) (*smtp.Client, error) {
	host := strings.TrimSpace(s.cfg.Host)
	if s.cfg.Port == 465 {
		tlsConn := tls.Client(conn, &tls.Config{ServerName: host, MinVersion: tls.VersionTLS12})
		client, err := smtp.NewClient(tlsConn, host)
		if err != nil {
			return nil, fmt.Errorf("smtp implicit tls client: %w", err)
		}
		return client, nil
	}
	client, err := smtp.NewClient(conn, host)
	if err != nil {
		return nil, fmt.Errorf("smtp client: %w", err)
	}
	if ok, _ := client.Extension("STARTTLS"); ok {
		if err := client.StartTLS(&tls.Config{ServerName: host, MinVersion: tls.VersionTLS12}); err != nil {
			_ = client.Close()
			return nil, fmt.Errorf("smtp starttls: %w", err)
		}
	}
	return client, nil
}

func (s *SMTPSender) authenticate(client *smtp.Client) error {
	username := strings.TrimSpace(s.cfg.Username)
	if username == "" {
		return nil
	}
	auth := smtp.PlainAuth("", username, s.cfg.Password, strings.TrimSpace(s.cfg.Host))
	if err := client.Auth(auth); err != nil {
		return fmt.Errorf("smtp auth: %w", err)
	}
	return nil
}

func buildRFC822Message(from, to, subject, body string) []byte {
	cleanSubject := strings.NewReplacer("\r", "", "\n", "").Replace(subject)
	headers := []string{
		fmt.Sprintf("From: RosaFiesta <%s>", from),
		fmt.Sprintf("To: %s", to),
		fmt.Sprintf("Subject: %s", cleanSubject),
		"MIME-Version: 1.0",
		"Content-Type: text/plain; charset=UTF-8",
		"",
		body,
	}
	return []byte(strings.Join(headers, "\r\n"))
}
