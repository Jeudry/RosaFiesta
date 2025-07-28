package main

import (
	"Backend/cmd/main/view_models/users"
	"Backend/internal/mailer"
	"Backend/internal/store"
	"Backend/internal/store/models"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"net/http"
	"time"
)

// registerUserHandler godoc
//
//	@Summary		Register a new user
//	@Description	Register a new user
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		view_models.RegisterUserPayload	true	"User credentials"
//	@Success		201		{object}	view_models.UserWithToken		"User registered"
//	@Failure		400		{object}	error							"Bad request"
//	@Failure		500		{object}	error							"Internal server error"
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

	//hash the user password
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

	activationURL := fmt.Sprintf("%s/confirm/%s", app.Config.FrontendURL, hashToken)

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
//	@Param			payload	body		view_models.RefreshTokenRequest	true	"Refresh token"
//	@Success		200		{object}	view_models.LoginResponse		"New tokens"
//	@Failure		400		{object}	error							"Bad request"
//	@Failure		401		{object}	error							"Unauthorized"
//	@Failure		500		{object}	error							"Internal server error"
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
//	@Param			payload	body	view_models.CreateUserTokenPayload	true	"User credentials"
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

	if err := app.jsonResponse(w, http.StatusOK, response); err != nil {
		app.internalServerError(w, r, err)
	}
}
