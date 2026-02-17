package main

import (
	"errors"
	"net/http"
)

type updateFCMTokenPayload struct {
	Token string `json:"token"`
}

// updateFCMTokenHandler godoc
//
//	@Summary		Update FCM token
//	@Description	Update the Firebase Cloud Messaging token for the current user
//	@Tags			users
//	@Accept			json
//	@Produce		json
//	@Param			token	body		updateFCMTokenPayload	true	"FCM Token"
//	@Success		200		{object}	string					"Successfully updated token"
//	@Failure		400		{object}	error
//	@Failure		401		{object}	error
//	@Failure		500		{object}	error
//	@Router			/users/fcm-token [put]
func (app *Application) updateFCMTokenHandler(w http.ResponseWriter, r *http.Request) {
	var payload updateFCMTokenPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if payload.Token == "" {
		app.badRequest(w, r, errors.New("token is required"))
		return
	}

	user := GetUserFromCtx(r)
	if err := app.Store.Users.UpdateFCMToken(r.Context(), user.ID, payload.Token); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, map[string]string{"message": "FCM token updated successfully"}); err != nil {
		app.internalServerError(w, r, err)
	}
}
