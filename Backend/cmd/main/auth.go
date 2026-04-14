package main

import (
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"net/http"
	"time"

	"Backend/cmd/main/view_models/users"
	"Backend/internal/mailer"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// registerUserHandler godoc
//
//	@Summary		Register a new user
//	@Description	Register a new user
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		users.RegisterUserPayload	true	"User credentials"
//	@Success		201		{object}	users.UserWithToken			"User registered"
//	@Failure		400		{object}	error						"Bad request"
//	@Failure		500		{object}	error						"Internal server error"
//	@Router			/authentication/register [post]
func (app *Application) registerUserHandler(w http.ResponseWriter, r *http.Request) {
	var payload users.RegisterUserPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := &models.User{
		UserName: payload.Username,
		Email:    payload.Email,
		Role: models.Role{
			Name: "user",
		},
	}

	// hash the user password
	if err := user.Password.Set(payload.Password); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	ctx := r.Context()

	plainToken := uuid.New().String()

	hash := sha256.Sum256([]byte(plainToken))
	hashToken := hex.EncodeToString(hash[:])

	err := app.Store.Users.CreateAndInvite(ctx, user, hashToken, app.Config.Mail.Exp)
	if err != nil {
		switch err {
		case store.ErrDuplicateEmail:
			app.badRequest(w, r, err)
		case store.ErrDuplicateUserName:
			app.badRequest(w, r, err)
		default:
			app.internalServerError(w, r, err)
		}
		return
	}

	userWithToken := users.UserWithToken{
		User:  user,
		Token: plainToken,
	}

	isProdEnv := app.Config.Env == "production"

	activationURL := fmt.Sprintf("%s/confirm/%s", app.Config.FrontendURL, plainToken)

	vars := struct {
		Username      string
		ActivationURL string
	}{
		Username:      user.UserName,
		ActivationURL: activationURL,
	}

	statusCode, err := app.Mailer.Send(mailer.UserWelcomeTemplate, user.UserName, user.Email, vars, !isProdEnv)
	if err != nil {
		app.Logger.Errorw("error sending welcome email", "error", err)

		if err := app.Store.Users.Delete(ctx, user.ID); err != nil {
			app.Logger.Errorw("error deleting user", "error", err)
		}

		app.internalServerError(w, r, err)
		return
	}

	// Log activation URL in development mode
	if !isProdEnv {
		app.Logger.Infow("🔑 DEVELOPMENT MODE - Activation URL", "url", activationURL, "user", user.UserName, "email", user.Email)
	}

	app.Logger.Infow("Email sent", "status code %v", statusCode)

	if err := app.jsonResponse(w, http.StatusCreated, userWithToken); err != nil {
		app.internalServerError(w, r, err)
	}
}

// refreshTokenHandler godoc
//
//	@Summary		Refresh an access token
//	@Description	Get a new access token using a refresh token
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		users.RefreshTokenRequest	true	"Refresh token"
//	@Success		200		{object}	users.LoginResponse			"New tokens"
//	@Failure		400		{object}	error						"Bad request"
//	@Failure		401		{object}	error						"Unauthorized"
//	@Failure		500		{object}	error						"Internal server error"
//	@Router			/authentication/refresh [post]
func (app *Application) refreshTokenHandler(w http.ResponseWriter, r *http.Request) {
	var payload users.RefreshTokenRequest
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Get refresh token from database
	refreshToken, err := app.Store.RefreshTokens.GetByToken(r.Context(), payload.RefreshToken)
	if err != nil {
		app.unauthorized(w, r, errors.New("invalid refresh token"))
		return
	}

	user, err := app.Store.Users.RetrieveById(r.Context(), refreshToken.UserID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.Store.RefreshTokens.Delete(r.Context(), refreshToken.Token); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	accessTokenExpiration := time.Now().Add(app.Config.Auth.Token.Exp)
	claims := jwt.MapClaims{
		"sub": user.ID,
		"exp": accessTokenExpiration.Unix(),
		"iat": time.Now().Unix(),
		"iss": app.Config.Auth.Token.Iss,
		"nbf": time.Now().Unix(),
		"aud": app.Config.Auth.Token.Aud,
	}

	accessToken, err := app.Auth.GenerateToken(claims)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Generate new refresh token
	newRefreshTokenStr := uuid.New().String()
	refreshTokenExpiration := time.Now().Add(30 * 24 * time.Hour) // 30 days

	newRefreshToken := &models.RefreshToken{
		UserID:    user.ID,
		Token:     newRefreshTokenStr,
		ExpiresAt: refreshTokenExpiration,
	}

	// Store new refresh token in database
	if err := app.Store.RefreshTokens.Create(r.Context(), newRefreshToken); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	response := users.LoginResponse{
		AccessToken:                    accessToken,
		UserId:                         user.ID,
		AccessTokenExpirationTimestamp: accessTokenExpiration.Unix(),
		RefreshToken:                   newRefreshTokenStr,
	}

	if err := app.jsonResponse(w, http.StatusOK, response); err != nil {
		app.internalServerError(w, r, err)
	}
}

// createTokenHandler godoc
//
//	@Summary		Create a new token
//	@Description	Create a new token
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		users.CreateUserTokenPayload	true	"User credentials"
//	@Success		200		{string}	token
//	@Failure		400		{object}	error	"Bad request"
//	@Failure		500		{object}	error	"Internal server error"
//	@Failure		401		{object}	error	"Unauthorized"
//	@Router			/authentication/token [post]
func (app *Application) createTokenHandler(w http.ResponseWriter, r *http.Request) {
	var payload users.CreateUserTokenPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user, err := app.Store.Users.GetByEmail(r.Context(), payload.Email)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.unauthorized(w, r, err)
			return
		}

		app.internalServerError(w, r, err)
		return
	}

	if err := user.Password.Compare(payload.Password); err != nil {
		app.unauthorized(w, r, err)
		return
	}

	if !user.IsActive {
		app.forbidden(w, r, errors.New("please verify your email"))
		return
	}

	// Generate access token
	accessTokenExpiration := time.Now().Add(app.Config.Auth.Token.Exp)
	claims := jwt.MapClaims{
		"sub": user.ID,
		"exp": accessTokenExpiration.Unix(),
		"iat": time.Now().Unix(),
		"iss": app.Config.Auth.Token.Iss,
		"nbf": time.Now().Unix(),
		"aud": app.Config.Auth.Token.Aud,
	}

	accessToken, err := app.Auth.GenerateToken(claims)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Generate refresh token
	refreshTokenStr := uuid.New().String()
	refreshTokenExpiration := time.Now().Add(30 * 24 * time.Hour) // 30 days

	refreshToken := &models.RefreshToken{
		UserID:    user.ID,
		Token:     refreshTokenStr,
		ExpiresAt: refreshTokenExpiration,
	}

	// Store refresh token in database
	if err := app.Store.RefreshTokens.Create(r.Context(), refreshToken); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	response := users.LoginResponse{
		AccessToken:                    accessToken,
		UserId:                         user.ID,
		AccessTokenExpirationTimestamp: accessTokenExpiration.Unix(),
		RefreshToken:                   refreshTokenStr,
	}

	// Fetch pending events for this user
	pendingEvents, err := app.Store.Events.GetPendingByUserID(r.Context(), user.ID)
	if err == nil && len(pendingEvents) > 0 {
		response.PendingEvents = make([]users.PendingEvent, len(pendingEvents))
		for i, ev := range pendingEvents {
			var dateStr *string
			if ev.Date != nil {
				s := ev.Date.Format("2006-01-02")
				dateStr = &s
			}
			response.PendingEvents[i] = users.PendingEvent{
				ID:            ev.ID,
				Name:          ev.Name,
				Date:          dateStr,
				Status:        ev.Status,
				PaymentStatus: ev.PaymentStatus,
			}
		}
	}

	if err := app.jsonResponse(w, http.StatusOK, response); err != nil {
		app.internalServerError(w, r, err)
	}
}

// forgotPasswordHandler godoc
//
//	@Summary		Request a password reset
//	@Description	Request a password reset email for the given email address
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		users.ForgotPasswordRequest	true	"Email address"
//	@Success		200		{string}	string					"Email sent if address exists"
//	@Failure		400		{object}	error					"Bad request"
//	@Failure		500		{object}	error					"Internal server error"
//	@Router			/authentication/forgot-password [post]
func (app *Application) forgotPasswordHandler(w http.ResponseWriter, r *http.Request) {
	var payload users.ForgotPasswordRequest

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()

	// Look up user by email - don't leak whether email exists
	user, err := app.Store.Users.GetByEmail(ctx, payload.Email)
	if err != nil {
		// Still return 200 to prevent email enumeration
		app.jsonResponse(w, http.StatusOK, map[string]string{"message": "If that email exists, a reset link has been sent"})
		return
	}

	// Generate reset token
	plainToken := uuid.New().String()

	// Create password reset token in database
	if err := app.Store.Users.CreatePasswordResetToken(ctx, user.ID, plainToken, app.Config.Mail.Exp); err != nil {
		app.Logger.Errorw("error creating password reset token", "error", err)
		app.internalServerError(w, r, err)
		return
	}

	isProdEnv := app.Config.Env == "production"

	resetURL := fmt.Sprintf("%s/reset-password/%s", app.Config.FrontendURL, plainToken)

	vars := struct {
		Username  string
		ResetURL  string
	}{
		Username:  user.UserName,
		ResetURL:  resetURL,
	}

	statusCode, err := app.Mailer.Send(mailer.PasswordResetTemplate, user.UserName, user.Email, vars, !isProdEnv)
	if err != nil {
		app.Logger.Errorw("error sending password reset email", "error", err)
		app.internalServerError(w, r, err)
		return
	}

	// Log reset URL in development mode
	if !isProdEnv {
		app.Logger.Infow("🔑 DEVELOPMENT MODE - Password Reset URL", "url", resetURL, "user", user.UserName, "email", user.Email)
	}

	app.Logger.Infow("Password reset email sent", "status code %v", statusCode)

	if err := app.jsonResponse(w, http.StatusOK, map[string]string{"message": "If that email exists, a reset link has been sent"}); err != nil {
		app.internalServerError(w, r, err)
	}
}

// resetPasswordHandler godoc
//
//	@Summary		Reset password
//	@Description	Reset password using a valid token
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		users.ResetPasswordRequest	true	"New password"
//	@Success		200		{string}	string					"Password updated"
//	@Failure		400		{object}	error					"Bad request"
//	@Failure		401		{object}	error					"Unauthorized"
//	@Failure		500		{object}	error					"Internal server error"
//	@Router			/authentication/reset-password [post]
func (app *Application) resetPasswordHandler(w http.ResponseWriter, r *http.Request) {
	var payload users.ResetPasswordRequest

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()

	// Get user by reset token
	user, err := app.Store.Users.GetUserByResetToken(ctx, payload.Token)
	if err != nil {
		app.unauthorized(w, r, errors.New("invalid or expired token"))
		return
	}

	// Hash the new password
	if err := user.Password.Set(payload.NewPassword); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Update user password
	if err := app.Store.Users.UpdatePassword(ctx, user.ID, user.Password.Hash); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Delete the reset token (single-use)
	if err := app.Store.Users.DeletePasswordResetTokenByToken(ctx, payload.Token); err != nil {
		app.Logger.Errorw("error deleting password reset token", "error", err)
		// Don't fail the request, password was already updated
	}

	if err := app.jsonResponse(w, http.StatusOK, map[string]string{"message": "Password updated successfully"}); err != nil {
		app.internalServerError(w, r, err)
	}
}
