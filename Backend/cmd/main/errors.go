package main

import (
	"Backend/internal/store"
	"errors"
	"net/http"
)

func (app *Application) internalServerError(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Errorw("internal server error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusInternalServerError, "The server encountered a problem and could not process your request.")
}

func (app *Application) badRequest(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Errorw("bad request error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusBadRequest, err.Error())
}

func (app *Application) notFoundResponse(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Errorw("not found error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusNotFound, "The requested resource could not be found.")
}

func (app *Application) conflictResponse(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Errorw("conflict error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusConflict, "The requested resource could not be created due to a conflict.")
}

func (app *Application) handleError(w http.ResponseWriter, r *http.Request, err error) {
	switch {
	case errors.Is(err, store.ErrNotFound):
		app.notFoundResponse(w, r, err)
	case errors.Is(err, store.ErrConflict):
		app.conflictResponse(w, r, err)
	default:
		app.internalServerError(w, r, err)
	}
}

func (app *Application) unauthorized(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Errorw("unauthorized error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusUnauthorized, "Unauthorized")
}

func (app *Application) basicUnauthorized(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Errorw("unauthorized basic error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	w.Header().Set("WWW-Authenticate", `Basic realm="restricted", charset="UTF-8"`)

	writeJsonError(w, http.StatusUnauthorized, "Unauthorized")
}

func (app *Application) forbidden(w http.ResponseWriter, r *http.Request, err error) {
	app.Logger.Warnw("forbidden error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusForbidden, "Forbidden")
}

func (app *Application) rateLimitExceededResponse(w http.ResponseWriter, r *http.Request, retryAfter string) {
	app.Logger.Warnw("rate limit exceeded error: %s, path: %s", r.Method, r.URL.Path)

	w.Header().Set("Retry-After", retryAfter)
	writeJsonError(w, http.StatusTooManyRequests, "Rate limit exceeded")
}
