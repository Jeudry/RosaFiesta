package main

import (
	"encoding/json"
	"log"
	"net/http"

	"github.com/go-playground/validator/v10"
)

func init() {
	Validate = validator.New(validator.WithRequiredStructEnabled())
}

var Validate *validator.Validate

func writeJson(w http.ResponseWriter, status int, data any) error {
	w.Header().Set("Content-Type", "Application/json")

	w.WriteHeader(status)

	return json.NewEncoder(w).Encode(data)
}

func readJson(w http.ResponseWriter, r *http.Request, data any) error {
	maxBytes := 1_048_576

	r.Body = http.MaxBytesReader(w, r.Body, int64(maxBytes))

	decoder := json.NewDecoder(r.Body)
	decoder.DisallowUnknownFields()

	return decoder.Decode(data)
}

func writeJsonError(w http.ResponseWriter, status int, message string) {
	type envelope struct {
		Data    any    `json:"data"`
		Status  int    `json:"status"`
		Error   string `json:"error"`
		Message string `json:"message"`
		Code    int    `json:"code"`
	}

	err := writeJson(w, status, envelope{
		Data:    status, // Send numeric status to satisfy int expectation for 'data' in error cases
		Status:  status,
		Error:   message,
		Message: message,
		Code:    status,
	})
	if err != nil {
		log.Printf("error writing json error: %s", err)
	}
}

func (app *Application) jsonResponse(w http.ResponseWriter, status int, data any) error {
	return writeJson(w, status, map[string]any{
		"data": data,
	})
}
