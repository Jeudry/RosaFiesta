package main

import (
	"encoding/json"
	"net/http"
	"time"

	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type CreateTimelineItemPayload struct {
	Title       string    `json:"title" validate:"required,max=255"`
	Description string    `json:"description"`
	StartTime   time.Time `json:"start_time" validate:"required"`
	EndTime     time.Time `json:"end_time" validate:"required"`
}

func (app *Application) createTimelineItemHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload CreateTimelineItemPayload
	if err := json.NewDecoder(r.Body).Decode(&payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify event ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		app.notFoundResponse(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	item := &models.TimelineItem{
		EventID:     eventID,
		Title:       payload.Title,
		Description: payload.Description,
		StartTime:   payload.StartTime,
		EndTime:     payload.EndTime,
	}

	if err := app.Store.Timeline.Create(r.Context(), item); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, item); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) getTimelineItemsHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify event ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		app.notFoundResponse(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	items, err := app.Store.Timeline.GetByEventID(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, items); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) updateTimelineItemHandler(w http.ResponseWriter, r *http.Request) {
	itemIDStr := chi.URLParam(r, "itemId")
	itemID, err := uuid.Parse(itemIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload CreateTimelineItemPayload
	if err := json.NewDecoder(r.Body).Decode(&payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	item, err := app.Store.Timeline.GetByID(r.Context(), itemID)
	if err != nil {
		app.notFoundResponse(w, r, err)
		return
	}

	// Verify event ownership
	event, err := app.Store.Events.GetByID(r.Context(), item.EventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	item.Title = payload.Title
	item.Description = payload.Description
	item.StartTime = payload.StartTime
	item.EndTime = payload.EndTime

	if err := app.Store.Timeline.Update(r.Context(), item); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, item); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) deleteTimelineItemHandler(w http.ResponseWriter, r *http.Request) {
	itemIDStr := chi.URLParam(r, "itemId")
	itemID, err := uuid.Parse(itemIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	item, err := app.Store.Timeline.GetByID(r.Context(), itemID)
	if err != nil {
		app.notFoundResponse(w, r, err)
		return
	}

	// Verify event ownership
	event, err := app.Store.Events.GetByID(r.Context(), item.EventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	if err := app.Store.Timeline.Delete(r.Context(), itemID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
