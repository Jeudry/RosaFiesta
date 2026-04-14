package main

import (
	"net/http"

	"Backend/internal/whatsapp"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type sendWhatsAppPayload struct {
	To   string `json:"to" validate:"required"`
	Body string `json:"body" validate:"required"`
}

// sendWhatsAppHandler godoc
//
//	@Summary		Send WhatsApp message for an event
//	@Description	Sends a WhatsApp message to the client's phone number associated with the event
//	@Tags			events
//	@Accept		json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Param			payload	body		sendWhatsAppPayload	true	"Message details"
//	@Success		200	{object}	map[string]string
//	@Failure		400	{object}	error
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/whatsapp [post]
func (app *Application) sendWhatsAppHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		app.notFoundResponse(w, r, err)
		return
	}

	_, err = app.Store.Users.RetrieveById(r.Context(), event.UserID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	var payload sendWhatsAppPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if payload.To == "" || payload.Body == "" {
		app.badRequest(w, r, err)
		return
	}

	msg := whatsapp.Message{
		To:   payload.To,
		Body: payload.Body,
	}

	if err := app.WhatsApp.SendTextMessage(r.Context(), msg); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, map[string]string{
		"status":  "sent",
		"to":      payload.To,
		"message": payload.Body,
	}); err != nil {
		app.internalServerError(w, r, err)
	}
}
