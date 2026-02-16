package main

import (
	"net/http"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type createGuestPayload struct {
	Name                string  `json:"name" validate:"required,max=255"`
	Email               *string `json:"email" validate:"omitempty,email"`
	Phone               *string `json:"phone" validate:"omitempty,max=50"`
	RSVPStatus          string  `json:"rsvp_status" validate:"required,oneof=pending confirmed declined"`
	PlusOne             bool    `json:"plus_one"`
	DietaryRestrictions *string `json:"dietary_restrictions"`
}

func (app *Application) addGuestHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload createGuestPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	guest := &models.Guest{
		EventID:             eventID,
		Name:                payload.Name,
		Email:               payload.Email,
		Phone:               payload.Phone,
		RSVPStatus:          payload.RSVPStatus,
		PlusOne:             payload.PlusOne,
		DietaryRestrictions: payload.DietaryRestrictions,
	}

	if err := app.Store.Guests.Create(r.Context(), guest); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, guest); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) getGuestsHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	guests, err := app.Store.Guests.GetByEventID(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, guests); err != nil {
		app.internalServerError(w, r, err)
	}
}

type updateGuestPayload struct {
	Name                *string `json:"name" validate:"omitempty,max=255"`
	Email               *string `json:"email" validate:"omitempty,email"`
	Phone               *string `json:"phone" validate:"omitempty,max=50"`
	RSVPStatus          *string `json:"rsvp_status" validate:"omitempty,oneof=pending confirmed declined"`
	PlusOne             *bool   `json:"plus_one"`
	DietaryRestrictions *string `json:"dietary_restrictions"`
}

func (app *Application) updateGuestHandler(w http.ResponseWriter, r *http.Request) {
	guestIDStr := chi.URLParam(r, "guestId")
	guestID, err := uuid.Parse(guestIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload updateGuestPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	guest, err := app.Store.Guests.GetByID(r.Context(), guestID)
	if err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	if payload.Name != nil {
		guest.Name = *payload.Name
	}
	if payload.Email != nil {
		guest.Email = payload.Email
	}
	if payload.Phone != nil {
		guest.Phone = payload.Phone
	}
	if payload.RSVPStatus != nil {
		guest.RSVPStatus = *payload.RSVPStatus
	}
	if payload.PlusOne != nil {
		guest.PlusOne = *payload.PlusOne
	}
	if payload.DietaryRestrictions != nil {
		guest.DietaryRestrictions = payload.DietaryRestrictions
	}

	if err := app.Store.Guests.Update(r.Context(), guest); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, guest); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) deleteGuestHandler(w http.ResponseWriter, r *http.Request) {
	guestIDStr := chi.URLParam(r, "guestId")
	guestID, err := uuid.Parse(guestIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := app.Store.Guests.Delete(r.Context(), guestID); err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
