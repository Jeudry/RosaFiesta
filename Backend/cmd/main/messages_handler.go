package main

import (
	"net/http"
)

func (app *Application) getMessagesHandler(w http.ResponseWriter, r *http.Request) {
	app.jsonResponse(w, http.StatusOK, map[string]string{"status": "not_implemented"})
}

func (app *Application) sendMessageHandler(w http.ResponseWriter, r *http.Request) {
	app.jsonResponse(w, http.StatusCreated, map[string]string{"status": "not_implemented"})
}
