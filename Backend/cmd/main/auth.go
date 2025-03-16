package main

import (
	"Backend/cmd/main/view_models"
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
//	@Router			/authentication/user [post]
func (app *Application) registerUserHandler(w http.ResponseWriter, r *http.Request) {
	var payload view_models.RegisterUserPayload

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

	userWithToken := view_models.UserWithToken{
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

// createTokenHandler godoc
//
//	@Summary		Create a new token
//	@Description	Create a new token
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		view_models.CreateUserTokenPayload	true	"User credentials"
//	@Success		200		{string}	token
//	@Failure		400		{object}	error	"Bad request"
//	@Failure		500		{object}	error	"Internal server error"
//	@Failure		401		{object}	error	"Unauthorized"
//	@Router			/authentication/token [post]
func (app *Application) createTokenHandler(w http.ResponseWriter, r *http.Request) {
	var payload view_models.CreateUserTokenPayload
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

	claims := jwt.MapClaims{
		"sub": user.ID,
		"exp": time.Now().Add(app.Config.Auth.Token.Exp).Unix(),
		"iat": time.Now().Unix(),
		"iss": app.Config.Auth.Token.Iss,
		"nbf": time.Now().Unix(),
		"aud": app.Config.Auth.Token.Aud,
	}

	token, err := app.Auth.GenerateToken(claims)

	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, token); err != nil {
		app.internalServerError(w, r, err)
	}
}
