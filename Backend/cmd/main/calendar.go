package main

import (
	"errors"
	"fmt"
	"net/http"
	"time"

	"Backend/internal/store"

	ics "github.com/arran4/golang-ical"
	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// getEventCalendarHandler godoc
//
//	@Summary		Get event calendar subscription
//	@Description	Get a valid .ics file containing the event details
//	@Tags			events
//	@Produce		text/calendar
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{string}	string	"ICS file content"
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/calendar.ics [get]
func (app *Application) getEventCalendarHandler(w http.ResponseWriter, r *http.Request) {
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

	// Create new calendar
	cal := ics.NewCalendar()
	cal.SetMethod(ics.MethodPublish)
	cal.SetProductId("-//RosaFiesta//RosaFiesta Calendar//ES")
	cal.SetCalscale("GREGORIAN")

	// Add event
	e := cal.AddEvent(event.ID.String())
	if created, err := time.Parse(time.RFC3339, event.CreatedAt); err == nil {
		e.SetCreatedTime(created)
	} else {
		e.SetCreatedTime(time.Now())
	}
	e.SetDtStampTime(time.Now())
	if updated, err := time.Parse(time.RFC3339, event.UpdatedAt); err == nil {
		e.SetModifiedAt(updated)
	} else {
		e.SetModifiedAt(time.Now())
	}
	e.SetStartAt(event.Date)
	// As we don't have an explicit end time for events in this schema, let's assume a 4-hour duration
	e.SetEndAt(event.Date.Add(4 * time.Hour))
	e.SetSummary(event.Name)
	e.SetLocation(event.Location)

	description := fmt.Sprintf("Evento: %s\nUbicación: %s\nInvitados: %d\nEstado: %s",
		event.Name, event.Location, event.GuestCount, event.Status)
	e.SetDescription(description)

	// Fetch timeline tasks to add them as sub-events
	timeline, err := app.Store.Timeline.GetByEventID(r.Context(), id)
	if err == nil && timeline != nil {
		for _, tl := range timeline {
			te := cal.AddEvent(tl.ID.String())
			te.SetDtStampTime(time.Now())
			if !tl.StartTime.IsZero() {
				te.SetStartAt(tl.StartTime)

				if !tl.EndTime.IsZero() {
					te.SetEndAt(tl.EndTime)
				} else {
					te.SetEndAt(tl.StartTime.Add(1 * time.Hour))
				}
			} else {
				// Fallback to event date if no start time
				te.SetStartAt(event.Date)
				te.SetEndAt(event.Date.Add(30 * time.Minute))
			}
			te.SetSummary(fmt.Sprintf("%s - %s", event.Name, tl.Title))

			tlDescription := fmt.Sprintf("Actividad: %s", tl.Title)
			if tl.Description != "" {
				tlDescription += fmt.Sprintf("\n%s", tl.Description)
			}
			te.SetDescription(tlDescription)
		}
	}

	w.Header().Set("Content-Type", "text/calendar; charset=utf-8")
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"event_%s.ics\"", event.ID.String()))
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write([]byte(cal.Serialize()))
}
