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

// addEventItemHandler godoc
//
//	@Summary		Add item to event
//	@Description	Add item to event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string					true	"Event ID"
//	@Param			payload	body		object					true	"Item payload"
//	@Success		201		{object}	models.EventItem
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/items [post]
func (app *Application) addEventItemHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		ArticleID uuid.UUID `json:"article_id"`
		Quantity  int       `json:"quantity"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
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
		app.forbidden(w, r, errors.New("you do not have permission to modify this event"))
		return
	}

	item := &models.EventItem{
		EventID:   eventID,
		ArticleID: payload.ArticleID,
		Quantity:  payload.Quantity,
	}
	if item.Quantity <= 0 {
		item.Quantity = 1
	}

	// Phase 17: Availability & Inventory Check
	availability, err := app.Store.Articles.GetAvailability(r.Context(), payload.ArticleID, event.Date)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	if availability < item.Quantity {
		app.badRequest(w, r, errors.New("insufficient stock for this date"))
		return
	}

	if err := app.Store.Events.AddItem(r.Context(), item); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, item); err != nil {
		app.internalServerError(w, r, err)
	}
}

// removeEventItemHandler godoc
//
//	@Summary		Remove item from event
//	@Description	Remove item from event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			itemId	path		string	true	"Item ID"
//	@Success		204		{object}	nil
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/items/{itemId} [delete]
func (app *Application) removeEventItemHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	itemIDParam := chi.URLParam(r, "itemId")
	itemID, err := uuid.Parse(itemIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
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
		app.forbidden(w, r, errors.New("you do not have permission to modify this event"))
		return
	}

	if err := app.Store.Events.RemoveItem(r.Context(), eventID, itemID); err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// getEventItemsHandler godoc
//
//	@Summary		Get items for an event
//	@Description	Get items for an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{array}		models.EventItem
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/items [get]
func (app *Application) getEventItemsHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
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
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	items, err := app.Store.Events.GetItems(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, items); err != nil {
		app.internalServerError(w, r, err)
	}
}

// payEventHandler godoc
//
//	@Summary		Simulate event payment
//	@Description	Simulate event payment and update status to 'paid'
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			payload	body		object	true	"Payment payload"
//	@Success		200		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/pay [post]
func (app *Application) payEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		PaymentMethod string `json:"payment_method" validate:"required"`
	}
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
		app.forbidden(w, r, errors.New("you do not have permission to pay for this event"))
		return
	}

	if event.Status != "confirmed" {
		app.badRequest(w, r, errors.New("only confirmed events can be paid"))
		return
	}

	// Update payment fields
	now := time.Now()
	event.PaymentStatus = "completed"
	event.PaymentMethod = &payload.PaymentMethod
	event.PaidAt = &now
	event.Status = "paid"

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// adjustQuoteHandler godoc
//
//	@Summary		Adjust event quote (Admin only)
//	@Description	Adjust event quote with additional costs and notes, and set status to 'adjusted'
//	@Tags			admin, events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			payload	body		object	true	"Adjustment payload"
//	@Success		200		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/admin/events/{id}/adjust [patch]
func (app *Application) adjustQuoteHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		AdditionalCosts float64 `json:"additional_costs" validate:"min=0"`
		AdminNotes      string  `json:"admin_notes"`
	}
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

	// Update quotation fields
	event.AdditionalCosts = payload.AdditionalCosts
	event.AdminNotes = payload.AdminNotes
	event.Status = "adjusted"

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}
