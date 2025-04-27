package main

import (
	"Backend/internal/store"
	"errors"
	"fmt"
	"net/http"
	"runtime"
)

func (app *Application) internalServerError(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Errorf("internal server error: %s, path: %s error: %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusInternalServerError, "The server encountered a problem and could not process your request.")
}

func (app *Application) badRequest(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Errorf("bad request error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusBadRequest, err.Error())
}

func (app *Application) notFoundResponse(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Errorf("not found error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusNotFound, "The requested resource could not be found.")
}

func (app *Application) conflictResponse(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Errorf("conflict error: %s, path: %s error %s", r.Method, r.URL.Path, err.Error())

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

func printFormattedError(err error) {
	buf := make([]byte, 1024)
	n := runtime.Stack(buf, false)
	fmt.Printf("\nError: %s\n\nFormatted Stack trace:\n%s\n", err.Error(), buf[:n])
}

func (app *Application) unauthorized(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Errorf("unauthorized error: %s, path: %s error %q", r.Method, r.URL.Path, err.Error())
	writeJsonError(w, http.StatusUnauthorized, "Unauthorized")
}

func (app *Application) basicUnauthorized(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Errorf("unauthorized basic error: %s, path: %s error %q", r.Method, r.URL.Path, err.Error())

	w.Header().Set("WWW-Authenticate", `Basic realm="restricted", charset="UTF-8"`)

	writeJsonError(w, http.StatusUnauthorized, "Unauthorized")
}

func (app *Application) forbidden(w http.ResponseWriter, r *http.Request, err error) {
	printFormattedError(err)
	app.Logger.Warnf("forbidden error: %s, path: %s error %q", r.Method, r.URL.Path, err.Error())

	writeJsonError(w, http.StatusForbidden, "Forbidden")
}

func (app *Application) rateLimitExceededResponse(w http.ResponseWriter, r *http.Request, retryAfter string) {
	app.Logger.Warnf("rate limit exceeded error: %s, path: %s", r.Method, r.URL.Path)

	w.Header().Set("Retry-After", retryAfter)
	writeJsonError(w, http.StatusTooManyRequests, "Rate limit exceeded")
}
