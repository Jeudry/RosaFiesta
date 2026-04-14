package main

import (
	"errors"
	"net/http"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type EventReviewPayload struct {
	Rating    int      `json:"rating" validate:"required,min=1,max=5"`
	Comment   string   `json:"comment" validate:"required"`
	PhotoURLs []string `json:"photoURLs"`
}

// createEventReviewHandler godoc
//
//	@Summary		Create an event review
//	@Description	Create an event review
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string				true	"Event ID"
//	@Param			payload	body		EventReviewPayload	true	"Review payload"
//	@Success		201		{object}	models.EventReview
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/reviews [post]
func (app *Application) createEventReviewHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload EventReviewPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify event exists
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	// Check if event is finished
	if event.Status != "completed" && event.Status != "finished" && event.Status != "paid" {
		app.badRequest(w, r, errors.New("reviews can only be submitted for completed events"))
		return
	}

	user := GetUserFromCtx(r)

	review := &models.EventReview{
		UserID:  user.ID,
		EventID: eventID,
		Rating:  payload.Rating,
		Comment: payload.Comment,
	}

	if err := app.Store.EventReviews.Create(r.Context(), review); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Save review photos if any were provided
	for i, photoURL := range payload.PhotoURLs {
		if err := app.Store.EventReviews.AddPhoto(r.Context(), review.ID, photoURL, "", i); err != nil {
			app.internalServerError(w, r, err)
			return
		}
	}

	// Attach user information for response
	review.User = user

	if err := app.jsonResponse(w, http.StatusCreated, review); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getEventReviewsHandler godoc
//
//	@Summary		Get reviews for an event
//	@Description	Get reviews for an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{array}		models.EventReview
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/reviews [get]
func (app *Application) getEventReviewsHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Check if event exists
	_, err = app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	reviews, err := app.Store.EventReviews.GetByEventID(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Fetch photos for each review
	for i := range reviews {
		photos, err := app.Store.EventReviews.GetPhotos(r.Context(), reviews[i].ID)
		if err != nil {
			app.internalServerError(w, r, err)
			return
		}
		reviews[i].Photos = photos
	}

	if err := app.jsonResponse(w, http.StatusOK, reviews); err != nil {
		app.internalServerError(w, r, err)
	}
}
