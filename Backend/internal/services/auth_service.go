package services

import (
	"context"
	"errors"
	"fmt"
	"time"

	"Backend/internal/auth"
	"Backend/internal/config"
	"Backend/internal/dtos"
	"Backend/internal/mailer"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"go.uber.org/zap"
)

type AuthService struct {
	users         store.UserRepository
	refreshTokens store.RefreshTokenRepository
	config        config.Config
	auth          auth.Authenticator
	mailer        mailer.Client
	logger        *zap.SugaredLogger
}

func NewAuthService(users store.UserRepository, refreshTokens store.RefreshTokenRepository, cfg config.Config, auth auth.Authenticator, mailer mailer.Client, logger *zap.SugaredLogger) *AuthService {
	return &AuthService{
		users:         users,
		refreshTokens: refreshTokens,
		config:        cfg,
		auth:          auth,
		mailer:        mailer,
		logger:        logger,
	}
}

func (s *AuthService) RegisterUser(ctx context.Context, payload dtos.RegisterUserPayload) (*models.UserWithToken, error) {
	if payload.Password != payload.ConfirmPassword {
		return nil, fmt.Errorf("passwords do not match")
	}

	user := &models.User{
		Params: models.UserParams{
			FirstName: payload.FirstName,
			LastName:  payload.LastName,
			Username:  payload.Username,
			Email:     payload.Email,
			Password:  payload.Password,
		},
		Role: models.Role{
			Name: "user",
		},
	}

	plainToken := uuid.New().String()
	hash := s.auth.HashToken(plainToken)

	if err := s.users.CreateAndInvite(ctx, user, hash, s.config.Mail.Exp); err != nil {
		return nil, err
	}

	// Send Email
	isProdEnv := s.config.Env == "production"
	vars := struct {
		Username      string
		ActivationURL string
	}{
		Username:      user.UserName,
		ActivationURL: fmt.Sprintf("%s/confirm/%s", s.config.FrontendURL, plainToken),
	}

	status, err := s.mailer.Send(mailer.UserWelcomeTemplate, user.UserName, user.Email, vars, !isProdEnv)
	if err != nil {
		s.logger.Errorw("error sending welcome email", "error", err)

		// Rollback user creation if email fails (optional, but good practice)
		if err := s.users.Delete(ctx, user.ID); err != nil {
			s.logger.Errorw("error deleting user", "error", err)
		}

		return nil, err
	}

	s.logger.Infow("email sent", "status", status)

	// Create Token
	claims := jwt.MapClaims{
		"sub": user.ID,
		"exp": time.Now().Add(s.config.Auth.Token.Exp).Unix(),
		"iat": time.Now().Unix(),
		"nbf": time.Now().Unix(),
		"iss": s.config.Auth.Token.Iss,
		"aud": s.config.Auth.Token.Aud,
	}

	token, err := s.auth.GenerateToken(claims)
	if err != nil {
		return nil, err
	}

	return &models.UserWithToken{
		User:  user,
		Token: token,
	}, nil
}

func (s *AuthService) Login(ctx context.Context, email, password string) (*models.UserToken, error) {
	user, err := s.users.GetByEmail(ctx, email)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			return nil, errors.New("invalid credentials")
		}
		return nil, err
	}

	if err := user.Password.Compare(password); err != nil {
		return nil, errors.New("invalid credentials")
	}

	return s.generateUserToken(ctx, user)
}

func (s *AuthService) RefreshToken(ctx context.Context, token string) (*models.UserToken, error) {
	refreshToken, err := s.refreshTokens.GetByToken(ctx, token)
	if err != nil {
		return nil, errors.New("invalid refresh token")
	}

	user, err := s.users.RetrieveById(ctx, refreshToken.UserID)
	if err != nil {
		return nil, err
	}

	if err := s.refreshTokens.Delete(ctx, refreshToken.Token); err != nil {
		return nil, err
	}

	return s.generateUserToken(ctx, user)
}

func (s *AuthService) generateUserToken(ctx context.Context, user *models.User) (*models.UserToken, error) {
	// Generate access token
	accessTokenExpiration := time.Now().Add(s.config.Auth.Token.Exp)
	claims := jwt.MapClaims{
		"sub": user.ID,
		"exp": accessTokenExpiration.Unix(),
		"iat": time.Now().Unix(),
		"iss": s.config.Auth.Token.Iss,
		"nbf": time.Now().Unix(),
		"aud": s.config.Auth.Token.Aud,
	}

	accessToken, err := s.auth.GenerateToken(claims)
	if err != nil {
		return nil, err
	}

	// Generate refresh token
	refreshTokenStr := uuid.New().String()
	refreshTokenExpiration := time.Now().Add(30 * 24 * time.Hour) // 30 days

	refreshToken := &models.RefreshToken{
		UserID:    user.ID,
		Token:     refreshTokenStr,
		ExpiresAt: refreshTokenExpiration,
	}

	if err := s.refreshTokens.Create(ctx, refreshToken); err != nil {
		return nil, err
	}

	return &models.UserToken{
		AccessToken:                    accessToken,
		RefreshToken:                   refreshTokenStr,
		AccessTokenExpirationTimestamp: accessTokenExpiration.Unix(),
		UserID:                         user.ID.String(),
	}, nil
}
