package main

import (
	"errors"
	"net/http"
	"time"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// createEventHandler godoc
//
//	@Summary		Create a new event
//	@Description	Create a new event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		models.CreateEventPayload	true	"Event payload"
//	@Success		201		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events [post]
func (app *Application) createEventHandler(w http.ResponseWriter, r *http.Request) {
	var payload models.CreateEventPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	// Parse date
	date, err := time.Parse(time.RFC3339, payload.Date)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	event := &models.Event{
		UserID:     user.ID,
		Name:       payload.Name,
		Date:       date,
		Location:   payload.Location,
		GuestCount: payload.GuestCount,
		Budget:     payload.Budget,
		Status:     "planning",
	}

	if err := app.Store.Events.Create(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getEventHandler godoc
//
//	@Summary		Get event by ID
//	@Description	Get event by ID
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{object}	models.Event
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id} [get]
func (app *Application) getEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	// Authorization check: ensure user owns the event
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getUserEventsHandler godoc
//
//	@Summary		Get all events for current user
//	@Description	Get all events for current user
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Success		200	{array}		models.Event
//	@Failure		500	{object}	error
//	@Router			/events [get]
func (app *Application) getUserEventsHandler(w http.ResponseWriter, r *http.Request) {
	user := GetUserFromCtx(r)

	events, err := app.Store.Events.GetByUserID(r.Context(), user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, events); err != nil {
		app.internalServerError(w, r, err)
	}
}

// updateEventHandler godoc
//
//	@Summary		Update an event
//	@Description	Update an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string							true	"Event ID"
//	@Param			payload	body		models.UpdateEventPayload	true	"Event payload"
//	@Success		200		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id} [put]
func (app *Application) updateEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload models.UpdateEventPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	// Authorization check
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to update this event"))
		return
	}

	if payload.Name != nil {
		event.Name = *payload.Name
	}
	if payload.Date != nil {
		date, err := time.Parse(time.RFC3339, *payload.Date)
		if err != nil {
			app.badRequest(w, r, err)
			return
		}
		event.Date = date
	}
	if payload.Location != nil {
		event.Location = *payload.Location
	}
	if payload.GuestCount != nil {
		event.GuestCount = *payload.GuestCount
	}
	if payload.Budget != nil {
		event.Budget = *payload.Budget
	}
	if payload.Status != nil {
		event.Status = *payload.Status
	}

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// deleteEventHandler godoc
//
//	@Summary		Delete an event
//	@Description	Delete an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		204	{object}	nil
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id} [delete]
func (app *Application) deleteEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Fetch first to check ownership
	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to delete this event"))
		return
	}

	if err := app.Store.Events.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
