package main

import (
	"net/http"
	"time"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type createEventTaskPayload struct {
	Title       string     `json:"title" validate:"required,max=255"`
	Description *string    `json:"description" validate:"omitempty"`
	DueDate     *time.Time `json:"due_date" validate:"omitempty"`
}

func (app *Application) addEventTaskHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload createEventTaskPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	task := &models.EventTask{
		EventID:     eventID,
		Title:       payload.Title,
		Description: payload.Description,
		DueDate:     payload.DueDate,
		IsCompleted: false,
	}

	if err := app.Store.EventTasks.Create(r.Context(), task); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, task); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) getEventTasksHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	tasks, err := app.Store.EventTasks.GetByEventID(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, tasks); err != nil {
		app.internalServerError(w, r, err)
	}
}

type updateEventTaskPayload struct {
	Title       *string    `json:"title" validate:"omitempty,max=255"`
	Description *string    `json:"description" validate:"omitempty"`
	IsCompleted *bool      `json:"is_completed" validate:"omitempty"`
	DueDate     *time.Time `json:"due_date" validate:"omitempty"`
}

func (app *Application) updateEventTaskHandler(w http.ResponseWriter, r *http.Request) {
	taskIDStr := chi.URLParam(r, "taskId")
	taskID, err := uuid.Parse(taskIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload updateEventTaskPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	task, err := app.Store.EventTasks.GetByID(r.Context(), taskID)
	if err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	if payload.Title != nil {
		task.Title = *payload.Title
	}
	if payload.Description != nil {
		task.Description = payload.Description
	}
	if payload.IsCompleted != nil {
		task.IsCompleted = *payload.IsCompleted
	}
	if payload.DueDate != nil {
		task.DueDate = payload.DueDate
	}

	if err := app.Store.EventTasks.Update(r.Context(), task); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, task); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) deleteEventTaskHandler(w http.ResponseWriter, r *http.Request) {
	taskIDStr := chi.URLParam(r, "taskId")
	taskID, err := uuid.Parse(taskIDStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := app.Store.EventTasks.Delete(r.Context(), taskID); err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
