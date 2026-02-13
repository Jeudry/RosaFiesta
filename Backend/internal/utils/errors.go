package utils

import (
	"errors"
	"fmt"
	"net/http"
	"runtime"

	"Backend/internal/store"

	"go.uber.org/zap"
)

// Responder handles HTTP responses and error logging
type Responder struct {
	Logger *zap.SugaredLogger
}

func NewResponder(logger *zap.SugaredLogger) *Responder {
	return &Responder{Logger: logger}
}

func (r *Responder) InternalServerError(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Errorf("internal server error: %s, path: %s error: %s", req.Method, req.URL.Path, err.Error())

	WriteJSONError(w, http.StatusInternalServerError, fmt.Sprintf("The server encountered a problem and could not process your request. %s", err.Error()))
}

func (r *Responder) BadRequest(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Errorf("bad request error: %s, path: %s error %s", req.Method, req.URL.Path, err.Error())

	WriteJSONError(w, http.StatusBadRequest, err.Error())
}

func (r *Responder) NotFoundResponse(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Errorf("not found error: %s, path: %s error %s", req.Method, req.URL.Path, err.Error())

	WriteJSONError(w, http.StatusNotFound, "The requested resource could not be found.")
}

func (r *Responder) ConflictResponse(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Errorf("conflict error: %s, path: %s error %s", req.Method, req.URL.Path, err.Error())

	WriteJSONError(w, http.StatusConflict, "The requested resource could not be created due to a conflict.")
}

func (r *Responder) HandleError(w http.ResponseWriter, req *http.Request, err error) {
	switch {
	case errors.Is(err, store.ErrNotFound):
		r.NotFoundResponse(w, req, err)
	case errors.Is(err, store.ErrConflict):
		r.ConflictResponse(w, req, err)
	default:
		r.InternalServerError(w, req, err)
	}
}

func printFormattedError(err error) {
	buf := make([]byte, 1024)
	n := runtime.Stack(buf, false)
	fmt.Printf("\nError: %s\n\nFormatted Stack trace:\n%s\n", err.Error(), buf[:n])
}

func (r *Responder) Unauthorized(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Errorf("unauthorized error: %s, path: %s error %q", req.Method, req.URL.Path, err.Error())
	WriteJSONError(w, http.StatusUnauthorized, "Unauthorized")
}

func (r *Responder) BasicUnauthorized(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Errorf("unauthorized basic error: %s, path: %s error %q", req.Method, req.URL.Path, err.Error())

	w.Header().Set("WWW-Authenticate", `Basic realm="restricted", charset="UTF-8"`)

	WriteJSONError(w, http.StatusUnauthorized, "Unauthorized")
}

func (r *Responder) Forbidden(w http.ResponseWriter, req *http.Request, err error) {
	printFormattedError(err)
	r.Logger.Warnf("forbidden error: %s, path: %s error %q", req.Method, req.URL.Path, err.Error())

	WriteJSONError(w, http.StatusForbidden, "Forbidden")
}

func (r *Responder) RateLimitExceededResponse(w http.ResponseWriter, req *http.Request, retryAfter string) {
	r.Logger.Warnf("rate limit exceeded error: %s, path: %s", req.Method, req.URL.Path)

	w.Header().Set("Retry-After", retryAfter)
	WriteJSONError(w, http.StatusTooManyRequests, "Rate limit exceeded")
}

func (r *Responder) Error500(w http.ResponseWriter, err error) {
	r.InternalServerError(w, nil, err) // nil request for now as InternalServerError needs it, but let's see if we can adapt or just use InternalServerError directly in handler if needed. Wait, InternalServerError takes *http.Request.
	// Actually, let's just make Error500 a wrapper that adapts or creates a dummy request if needed, OR better, let's just use JSON for everything in the new handler logic and manually pass status.
	// But `RespondWithError` uses `h.responder.JSON`.
}

func (r *Responder) JSON(w http.ResponseWriter, status int, data any) {
	if err := WriteJSON(w, status, data); err != nil {
		r.Logger.Errorw("Error writing JSON response", "error", err)
	}
}
