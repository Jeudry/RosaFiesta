package app

import (
	"context"
	"embed"
	"fmt"
	"log/slog"
	"net/smtp"
	"strconv"
	"strings"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

//go:embed templates/*.txt
var emailTemplates embed.FS

const (
	otpValidityTTL        = 10 * time.Minute
	resetTokenValidityTTL = 15 * time.Minute
)

type OTPNotifier interface {
	SendOTP(ctx context.Context, recipientEmail, code string, ttl time.Duration) error
}

type OTPRequestResult struct {
	Sent             bool   `json:"sent" doc:"Indicates whether the verification code was dispatched."`
	ExpiresInSeconds int    `json:"expiresInSeconds" doc:"Code expiration window in seconds."`
	DevCode          string `json:"devCode,omitempty" doc:"Verification code exposed strictly in non-production environments."`
}

type OTPVerifyResult struct {
	Verified         bool   `json:"verified" doc:"Indicates whether the verification code matched."`
	ResetToken       string `json:"resetToken" doc:"Single-use token required to complete password reset."`
	ExpiresInSeconds int    `json:"expiresInSeconds" doc:"Reset token expiration window in seconds."`
}

type ResetPasswordCommand struct {
	Identifier  string
	Email       string
	ResetToken  string
	OTPCode     string
	NewPassword string
}

type defaultOTPNotifier struct {
	host     string
	port     int
	from     string
	username string
	password string
}

func newDefaultOTPNotifier(opts Options) OTPNotifier {
	port := opts.SMTPPort
	if port <= 0 {
		port = 587
	}
	from := strings.TrimSpace(opts.SMTPFrom)
	if from == "" {
		from = "no-reply@rosafiesta.do"
	}
	return defaultOTPNotifier{
		host:     strings.TrimSpace(opts.SMTPHost),
		port:     port,
		from:     from,
		username: strings.TrimSpace(opts.SMTPUsername),
		password: opts.SMTPPassword,
	}
}

func (n defaultOTPNotifier) SendOTP(_ context.Context, recipientEmail, code string, ttl time.Duration) error {
	redactedRecipient := redact.Email(recipientEmail)
	if n.host == "" {
		slog.Info("otp delivery simulated without smtp host", slog.String("recipient", redactedRecipient))
		return nil
	}
	rawTemplate, err := emailTemplates.ReadFile("templates/otp_email.txt")
	if err != nil {
		return err
	}
	minutes := int(ttl.Minutes())
	minutesLabel := strconv.Itoa(minutes)
	templateText := string(rawTemplate)
	withCode := strings.ReplaceAll(templateText, "{{CODE}}", code)
	rendered := strings.ReplaceAll(withCode, "{{TTL_MINUTES}}", minutesLabel)
	headerLine := fmt.Sprintf("From: %s\r\nTo: %s\r\n", n.from, recipientEmail)
	messageBytes := []byte(headerLine + rendered)
	addr := fmt.Sprintf("%s:%d", n.host, n.port)
	var smtpAuth smtp.Auth
	if n.username != "" {
		smtpAuth = smtp.PlainAuth("", n.username, n.password, n.host)
	}
	return smtp.SendMail(addr, smtpAuth, n.from, []string{recipientEmail}, messageBytes)
}

func (s *Service) RequestOTP(ctx context.Context, rawEmail string) (OTPRequestResult, error) {
	normalizedEmail, err := domain.NormalizeEmail(rawEmail)
	if err != nil {
		return OTPRequestResult{}, domain.ErrInvalidInput
	}
	if s.requiresConfiguredSMTPBlock() {
		return OTPRequestResult{}, domain.ErrSMTPNotConfigured
	}
	s.syncUsersFromStore(ctx)
	s.mu.RLock()
	_, found := s.findUserLocked(normalizedEmail)
	s.mu.RUnlock()
	if !found {
		return OTPRequestResult{}, domain.ErrUserNotFound
	}
	code, err := domain.GenerateDigitsOTP()
	if err != nil {
		return OTPRequestResult{}, err
	}
	now := s.clock.Now()
	expiresAt := now.Add(otpValidityTTL)
	record := domain.OTPRecord{
		Email:          normalizedEmail,
		CodeHash:       domain.HashToken(code),
		Attempts:       0,
		Verified:       false,
		ResetTokenHash: "",
		Used:           false,
		ExpiresAt:      expiresAt,
		ResetExpiresAt: expiresAt,
		CreatedAt:      now,
	}
	s.saveOTPRecord(ctx, record)
	if dispatchErr := s.dispatchOTPCode(ctx, normalizedEmail, code); dispatchErr != nil {
		return OTPRequestResult{}, dispatchErr
	}
	result := OTPRequestResult{
		Sent:             true,
		ExpiresInSeconds: int(otpValidityTTL.Seconds()),
	}
	if s.opts.Env != config.EnvProduction {
		result.DevCode = code
	}
	return result, nil
}

func (s *Service) requiresConfiguredSMTPBlock() bool {
	if !s.opts.RequireConfiguredSMTP {
		return false
	}
	if strings.TrimSpace(s.opts.SMTPHost) != "" {
		return false
	}
	if s.customNotifierProvided {
		return false
	}
	if s.opts.EmailSender != nil {
		if s.opts.EmailSender.IsConfigured() {
			return false
		}
	}
	return true
}

func (s *Service) dispatchOTPCode(ctx context.Context, recipientEmail, code string) error {
	if s.opts.EmailSender != nil {
		if s.opts.EmailSender.IsConfigured() {
			body, err := renderOTPEmailBody(code, otpValidityTTL)
			if err != nil {
				return err
			}
			if mailErr := s.opts.EmailSender.SendEmail(ctx, recipientEmail, "RosaFiesta Verification Code", body); mailErr != nil {
				return mailErr
			}
			if !s.customNotifierProvided {
				return nil
			}
		}
	}
	return s.notifier.SendOTP(ctx, recipientEmail, code, otpValidityTTL)
}

type OAuthConfigResult struct {
	GoogleEnabled         bool   `json:"googleEnabled" doc:"Indicates whether Google OAuth is configured on the server."`
	AppleEnabled          bool   `json:"appleEnabled" doc:"Indicates whether Apple Sign-In is configured on the server."`
	GoogleClientID        string `json:"googleClientId,omitempty" doc:"Configured Google OAuth client identifier."`
	AppleClientID         string `json:"appleClientId,omitempty" doc:"Configured Apple OAuth client identifier."`
	WebPushVAPIDPublicKey string `json:"webPushVapidPublicKey,omitempty" doc:"Public VAPID key for Web Push subscription."`
}

func (s *Service) OAuthConfig() OAuthConfigResult {
	googleID := strings.TrimSpace(s.opts.GoogleClientID)
	appleID := strings.TrimSpace(s.opts.AppleClientID)
	vapidPublic := strings.TrimSpace(s.opts.WebPushVAPIDPublicKey)
	return OAuthConfigResult{
		GoogleEnabled:         googleID != "",
		AppleEnabled:          appleID != "",
		GoogleClientID:        googleID,
		AppleClientID:         appleID,
		WebPushVAPIDPublicKey: vapidPublic,
	}
}

func (s *Service) RecoverUsername(ctx context.Context, rawIdentifier string) (bool, error) {
	cleaned := strings.TrimSpace(rawIdentifier)
	if cleaned == "" {
		return false, domain.ErrInvalidInput
	}
	if s.requiresConfiguredSMTPBlock() {
		return false, domain.ErrSMTPNotConfigured
	}
	s.syncUsersFromStore(ctx)
	user, found := s.findUserByRecoveryIdentifier(cleaned)
	if !found {
		return true, nil
	}
	if err := s.dispatchUsernameRecoveryEmail(ctx, user); err != nil {
		return false, err
	}
	return true, nil
}

func (s *Service) findUserByRecoveryIdentifier(rawIdentifier string) (domain.User, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if user, ok := s.findUserLocked(rawIdentifier); ok {
		return user, true
	}
	targetDigits := onlyDigits(rawIdentifier)
	for _, user := range s.users {
		if strings.EqualFold(strings.TrimSpace(user.FullName), rawIdentifier) {
			return user, true
		}
		if targetDigits != "" {
			if onlyDigits(user.Phone) == targetDigits {
				return user, true
			}
		}
	}
	return domain.User{}, false
}

func (s *Service) dispatchUsernameRecoveryEmail(ctx context.Context, user domain.User) error {
	body, err := renderUsernameRecoveryEmailBody(user)
	if err != nil {
		return err
	}
	if s.opts.EmailSender != nil {
		if s.opts.EmailSender.IsConfigured() {
			return s.opts.EmailSender.SendEmail(ctx, user.Email, "Recuperación de usuario RosaFiesta", body)
		}
	}
	slog.InfoContext(ctx, "rosafiesta username recovery queued without smtp", slog.String("email", redact.Email(user.Email)))
	return nil
}

func renderUsernameRecoveryEmailBody(user domain.User) (string, error) {
	rawTemplate, err := emailTemplates.ReadFile("templates/username_recovery_email.txt")
	if err != nil {
		return "", err
	}
	res := strings.ReplaceAll(string(rawTemplate), "{{FULL_NAME}}", user.FullName)
	res = strings.ReplaceAll(res, "{{EMAIL}}", user.Email)
	return strings.ReplaceAll(res, "{{USERNAME}}", user.Username), nil
}

func onlyDigits(raw string) string {
	var b strings.Builder
	for _, ch := range raw {
		if ch >= '0' && ch <= '9' {
			b.WriteRune(ch)
		}
	}
	return b.String()
}

func renderOTPEmailBody(code string, ttl time.Duration) (string, error) {
	rawTemplate, err := emailTemplates.ReadFile("templates/otp_email.txt")
	if err != nil {
		return "", err
	}
	minutesLabel := strconv.Itoa(int(ttl.Minutes()))
	withCode := strings.ReplaceAll(string(rawTemplate), "{{CODE}}", code)
	return strings.ReplaceAll(withCode, "{{TTL_MINUTES}}", minutesLabel), nil
}

func (s *Service) VerifyOTP(ctx context.Context, rawEmail, rawCode string) (OTPVerifyResult, error) {
	normalizedEmail, err := domain.NormalizeEmail(rawEmail)
	if err != nil {
		return OTPVerifyResult{}, domain.ErrInvalidInput
	}
	trimmedCode := strings.TrimSpace(rawCode)
	if len(trimmedCode) != 6 {
		return OTPVerifyResult{}, domain.ErrOTPInvalidOrExpired
	}
	record, found := s.loadOTPRecord(ctx, normalizedEmail)
	if !found {
		return OTPVerifyResult{}, domain.ErrOTPInvalidOrExpired
	}
	now := s.clock.Now()
	candidateHash := domain.HashToken(trimmedCode)
	if !record.IsCodeValid(candidateHash, now) {
		s.incrementOTPAttempt(ctx, record)
		return OTPVerifyResult{}, domain.ErrOTPInvalidOrExpired
	}
	resetToken, err := domain.GenerateRandomHex(24)
	if err != nil {
		return OTPVerifyResult{}, err
	}
	resetHash := domain.HashToken(resetToken)
	resetExpires := now.Add(resetTokenValidityTTL)
	record.Verified = true
	record.ResetTokenHash = resetHash
	record.ResetExpiresAt = resetExpires
	s.markOTPVerified(ctx, record)
	return OTPVerifyResult{
		Verified:         true,
		ResetToken:       resetToken,
		ExpiresInSeconds: int(resetTokenValidityTTL.Seconds()),
	}, nil
}

func (s *Service) ResetPassword(ctx context.Context, cmd ResetPasswordCommand) (domain.User, string, error) {
	session, err := s.ResetPasswordWithSession(ctx, cmd)
	if err != nil {
		return domain.User{}, "", err
	}
	return session.User, session.AccessToken, nil
}

func (s *Service) OAuthLogin(ctx context.Context, provider, idToken string) (domain.User, string, error) {
	session, err := s.OAuthLoginWithSession(ctx, provider, idToken)
	if err != nil {
		return domain.User{}, "", err
	}
	return session.User, session.AccessToken, nil
}

func (s *Service) configuredOAuthClientID(provider string) (string, error) {
	if provider == "google" {
		if s.opts.GoogleClientID == "" {
			return "", domain.ErrOAuthProviderNotConfigured
		}
		return s.opts.GoogleClientID, nil
	}
	if provider == "apple" {
		if s.opts.AppleClientID == "" {
			return "", domain.ErrOAuthProviderNotConfigured
		}
		return s.opts.AppleClientID, nil
	}
	return "", domain.ErrInvalidInput
}

func (s *Service) authorizePasswordReset(ctx context.Context, email, rawResetToken, rawOTPCode string) error {
	record, found := s.loadOTPRecord(ctx, email)
	if !found {
		return domain.ErrResetTokenInvalid
	}
	now := s.clock.Now()
	trimmedReset := strings.TrimSpace(rawResetToken)
	if trimmedReset != "" {
		resetHash := domain.HashToken(trimmedReset)
		if !record.IsResetTokenValid(resetHash, now) {
			return domain.ErrResetTokenInvalid
		}
		s.consumeOTPRecord(ctx, record)
		return nil
	}
	trimmedOTP := strings.TrimSpace(rawOTPCode)
	if trimmedOTP != "" {
		otpHash := domain.HashToken(trimmedOTP)
		if !record.IsCodeValid(otpHash, now) {
			s.incrementOTPAttempt(ctx, record)
			return domain.ErrOTPInvalidOrExpired
		}
		s.consumeOTPRecord(ctx, record)
		return nil
	}
	return domain.ErrResetTokenInvalid
}

func (s *Service) saveOTPRecord(ctx context.Context, record domain.OTPRecord) {
	s.mu.Lock()
	s.otps[record.Email] = record
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.UpsertOTP(ctx, record)
		}
	}
}

func (s *Service) loadOTPRecord(ctx context.Context, email string) (domain.OTPRecord, bool) {
	s.mu.RLock()
	store := s.store
	memRec, ok := s.otps[email]
	s.mu.RUnlock()
	if store != nil {
		if store.Enabled() {
			if dbRec, err := store.GetOTP(ctx, email); err == nil {
				s.mu.Lock()
				s.otps[email] = dbRec
				s.mu.Unlock()
				return dbRec, true
			}
		}
	}
	return memRec, ok
}

func (s *Service) incrementOTPAttempt(ctx context.Context, record domain.OTPRecord) {
	record.Attempts++
	s.mu.Lock()
	s.otps[record.Email] = record
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.IncrementOTPAttempts(ctx, record.Email)
		}
	}
}

func (s *Service) markOTPVerified(ctx context.Context, record domain.OTPRecord) {
	s.mu.Lock()
	s.otps[record.Email] = record
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.MarkOTPVerified(ctx, record.Email, record.ResetTokenHash, record.ResetExpiresAt)
		}
	}
}

func (s *Service) consumeOTPRecord(ctx context.Context, record domain.OTPRecord) {
	record.Used = true
	s.mu.Lock()
	s.otps[record.Email] = record
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.MarkOTPUsed(ctx, record.Email)
		}
	}
}
