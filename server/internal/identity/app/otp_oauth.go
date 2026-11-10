package app

import (
	"context"
	"embed"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/smtp"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

//go:embed templates/otp_email.txt
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
	if err := s.notifier.SendOTP(ctx, normalizedEmail, code, otpValidityTTL); err != nil {
		return OTPRequestResult{}, err
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
	passwordHash, err := domain.HashPassword(cmd.NewPassword)
	if err != nil {
		return domain.User{}, "", domain.ErrInvalidInput
	}
	lookup := strings.TrimSpace(cmd.Email)
	if lookup == "" {
		lookup = strings.TrimSpace(cmd.Identifier)
	}
	lowerLookup := strings.ToLower(lookup)
	if lowerLookup == "" {
		return domain.User{}, "", domain.ErrInvalidInput
	}
	s.syncUsersFromStore(ctx)
	s.mu.RLock()
	user, found := s.findUserLocked(lowerLookup)
	s.mu.RUnlock()
	if !found {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	if err := s.authorizePasswordReset(ctx, user.Email, cmd.ResetToken, cmd.OTPCode); err != nil {
		return domain.User{}, "", err
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	user.PasswordHash = passwordHash
	s.users[user.ID] = user
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpdatePassword(ctx, user.Email, passwordHash)
		}
	}
	token := s.issueAndPersistSessionLocked(ctx, user)
	return user, token, nil
}

func (s *Service) OAuthLogin(ctx context.Context, provider, idToken string) (domain.User, string, error) {
	trimmedProvider := strings.TrimSpace(provider)
	normalizedProvider := strings.ToLower(trimmedProvider)
	clientID, err := s.configuredOAuthClientID(normalizedProvider)
	if err != nil {
		return domain.User{}, "", err
	}
	now := s.clock.Now()
	claims, err := parseVerifiedOAuthClaims(idToken, clientID, now)
	if err != nil {
		return domain.User{}, "", err
	}
	return s.upsertOAuthUser(ctx, claims.Email, claims.Name)
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

type oauthTokenClaims struct {
	Audience  string `json:"aud"`
	Email     string `json:"email"`
	Name      string `json:"name"`
	ExpiresAt int64  `json:"exp"`
}

func parseVerifiedOAuthClaims(idToken, expectedAudience string, now time.Time) (oauthTokenClaims, error) {
	trimmed := strings.TrimSpace(idToken)
	parts := strings.Split(trimmed, ".")
	if len(parts) != 3 {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if strings.TrimSpace(parts[2]) == "" {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	payloadBytes, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	var claims oauthTokenClaims
	if err := json.Unmarshal(payloadBytes, &claims); err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if claims.Audience != expectedAudience {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	if now.Unix() >= claims.ExpiresAt {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	normalizedEmail, err := domain.NormalizeEmail(claims.Email)
	if err != nil {
		return oauthTokenClaims{}, domain.ErrOAuthInvalidToken
	}
	claims.Email = normalizedEmail
	return claims, nil
}

func (s *Service) upsertOAuthUser(ctx context.Context, email, displayName string) (domain.User, string, error) {
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()
	existing, found := s.findUserLocked(email)
	if found {
		token := s.issueAndPersistSessionLocked(ctx, existing)
		return existing, token, nil
	}
	fullName := strings.TrimSpace(displayName)
	if fullName == "" {
		emailParts := strings.Split(email, "@")
		fullName = emailParts[0]
	}
	emailParts := strings.Split(email, "@")
	randomPass, _ := domain.GenerateRandomHex(16)
	passHash, _ := domain.HashPassword(randomPass)
	now := s.clock.Now()
	created := domain.User{
		ID:           uuid.New(),
		Username:     emailParts[0],
		FullName:     fullName,
		Email:        email,
		Role:         domain.RoleClient,
		Permissions:  []string{},
		ActiveView:   domain.ViewPersonal,
		CreatedAt:    now,
		PasswordHash: passHash,
	}
	s.users[created.ID] = created
	token := s.issueAndPersistSessionLocked(ctx, created)
	return created, token, nil
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
