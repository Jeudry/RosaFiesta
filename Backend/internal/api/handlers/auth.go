package handlers

import (
	"net/http"

	"Backend/internal/dtos"
	"Backend/internal/store"
	"Backend/internal/utils"
)

// registerUserHandler godoc
//
//	@Summary		Register a new user
//	@Description	Register a new user
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		dtos.RegisterUserPayload	true	"User credentials"
//	@Success		201		{object}	dtos.UserWithToken			"User registered"
//	@Failure		400		{object}	error						"Bad request"
//	@Failure		500		{object}	error						"Internal server error"
//	@Router			/authentication/register [post]
func (h *Handler) RegisterUserHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.RegisterUserPayload

	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	userWithToken, err := h.AuthService.RegisterUser(r.Context(), payload)
	if err != nil {
		switch err {
		case store.ErrDuplicateEmail, store.ErrDuplicateUserName:
			h.responder.BadRequest(w, r, err)
		default:
			h.responder.InternalServerError(w, r, err)
		}
		return
	}

	if err := utils.JSONResponse(w, http.StatusCreated, userWithToken); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// refreshTokenHandler godoc
//
//	@Summary		Refresh an access token
//	@Description	Get a new access token using a refresh token
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		dtos.RefreshTokenRequest	true	"Refresh token"
//	@Success		200		{object}	dtos.LoginResponse			"New tokens"
//	@Failure		400		{object}	error						"Bad request"
//	@Failure		401		{object}	error						"Unauthorized"
//	@Failure		500		{object}	error						"Internal server error"
//	@Router			/authentication/refresh [post]
func (h *Handler) RefreshTokenHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.RefreshTokenRequest
	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	userToken, err := h.AuthService.RefreshToken(r.Context(), payload.RefreshToken)
	if err != nil {
		h.responder.Unauthorized(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, userToken); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}

// createTokenHandler godoc
//
//	@Summary		Create a new token
//	@Description	Create a new token
//	@Tags			authentication
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		dtos.CreateUserTokenPayload	true	"User credentials"
//	@Success		200		{string}	token
//	@Failure		400		{object}	error	"Bad request"
//	@Failure		500		{object}	error	"Internal server error"
//	@Failure		401		{object}	error	"Unauthorized"
//	@Router			/authentication/token [post]
func (h *Handler) CreateTokenHandler(w http.ResponseWriter, r *http.Request) {
	var payload dtos.CreateUserTokenPayload
	if err := utils.ReadJSON(w, r, &payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	if err := utils.Validate.Struct(payload); err != nil {
		h.responder.BadRequest(w, r, err)
		return
	}

	userToken, err := h.AuthService.Login(r.Context(), payload.Email, payload.Password)
	if err != nil {
		h.responder.Unauthorized(w, r, err)
		return
	}

	if err := utils.JSONResponse(w, http.StatusOK, userToken); err != nil {
		h.responder.InternalServerError(w, r, err)
	}
}
