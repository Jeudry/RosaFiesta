package apperrors

import (
	"errors"
	"net/http"
)

// AppError is the interface for our custom errors
type AppError interface {
	error
	Status() int
}

// NotFoundError represents a resource not found
type NotFoundError struct {
	Err error
}

func (e *NotFoundError) Error() string {
	return e.Err.Error()
}

func (e *NotFoundError) Status() int {
	return http.StatusNotFound
}

// ValidationError represents a bad request due to validation
type ValidationError struct {
	Err error
}

func (e *ValidationError) Error() string {
	return e.Err.Error()
}

func (e *ValidationError) Status() int {
	return http.StatusBadRequest
}

// ConflictError represents a conflict state (e.g. duplicate email)
type ConflictError struct {
	Err error
}

func (e *ConflictError) Error() string {
	return e.Err.Error()
}

func (e *ConflictError) Status() int {
	return http.StatusConflict
}

// InternalError represents an unexpected server error
type InternalError struct {
	Err error
}

func (e *InternalError) Error() string {
	return e.Err.Error()
}

func (e *InternalError) Status() int {
	return http.StatusInternalServerError
}

// unauthorizedError represents an unauthorized request
type UnauthorizedError struct {
	Err error
}

func (e *UnauthorizedError) Error() string {
	return e.Err.Error()
}

func (e *UnauthorizedError) Status() int {
	return http.StatusUnauthorized
}

// ForbiddenError represents a forbidden request
type ForbiddenError struct {
	Err error
}

func (e *ForbiddenError) Error() string {
	return e.Err.Error()
}

func (e *ForbiddenError) Status() int {
	return http.StatusForbidden
}

// MapError maps a Go error to an HTTP Status Code and a JSON-friendly response
func MapError(err error) (int, interface{}) {
	var appErr AppError
	if errors.As(err, &appErr) {
		return appErr.Status(), map[string]string{"error": appErr.Error()}
	}

	// Default to 500
	return http.StatusInternalServerError, map[string]string{"error": "internal server error"}
}

// Helper constructors
func NewNotFound(msg string) *NotFoundError {
	return &NotFoundError{Err: errors.New(msg)}
}

func NewValidation(msg string) *ValidationError {
	return &ValidationError{Err: errors.New(msg)}
}

func NewConflict(msg string) *ConflictError {
	return &ConflictError{Err: errors.New(msg)}
}

func NewInternal(msg string) *InternalError {
	return &InternalError{Err: errors.New(msg)}
}

func NewUnauthorized(msg string) *UnauthorizedError {
	return &UnauthorizedError{Err: errors.New(msg)}
}

func NewForbidden(msg string) *ForbiddenError {
	return &ForbiddenError{Err: errors.New(msg)}
}
