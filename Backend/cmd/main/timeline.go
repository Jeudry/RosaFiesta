package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type CreateTimelineItemPayload struct {
	Title       string `json:"title" validate:"required,max=255"`
	Description string `json:"description"`
	StartTime   string `json:"start_time" validate:"required"`
	EndTime     string `json:"end_time" validate:"required"`
}

func parseTimelineTime(tStr string) (time.Time, error) {
	// Try RFC3339 first (standard server format)
	if t, err := time.Parse(time.RFC3339, tStr); err == nil {
		return t, nil
	}
	// Try common ISO8601 format from Flutter (without Z or offset)
	// Example: 2026-02-18T22:00:00.000
	layouts := []string{
		"2006-01-02T15:04:05.000",
		"2006-01-02T15:04:05",
	}

	for _, layout := range layouts {
		if t, err := time.Parse(layout, tStr); err == nil {
			return t, nil
		}
	}

	return time.Time{}, fmt.Errorf("could not parse time: %s", tStr)
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

	startTime, err := parseTimelineTime(payload.StartTime)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	endTime, err := parseTimelineTime(payload.EndTime)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	item := &models.TimelineItem{
		EventID:     eventID,
		Title:       payload.Title,
		Description: payload.Description,
		StartTime:   startTime,
		EndTime:     endTime,
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

	if items == nil {
		items = []models.TimelineItem{}
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

	startTime, err := parseTimelineTime(payload.StartTime)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	endTime, err := parseTimelineTime(payload.EndTime)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	item.Title = payload.Title
	item.Description = payload.Description
	item.StartTime = startTime
	item.EndTime = endTime

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
