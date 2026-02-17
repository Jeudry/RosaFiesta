package main

import (
	"net/http"

	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

func (app *Application) getMessagesHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	messages, err := app.Store.Messages.GetByEventID(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, messages)
}

func (app *Application) sendMessageHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload models.CreateMessagePayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	message := &models.EventMessage{
		EventID:  eventID,
		SenderID: user.ID,
		Content:  payload.Content,
	}

	if err := app.Store.Messages.Create(r.Context(), message); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Broadcast to WebSocket hub
	app.ChatHub.broadcast <- &BroadcastMessage{
		EventID: eventID,
		Payload: message,
	}

	app.jsonResponse(w, http.StatusCreated, message)
}

func (app *Application) wsHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Upgrade the connection
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		app.Logger.Errorf("failed to upgrade connection: %v", err)
		return
	}

	// Authenticate user via token in query param
	tokenStr := r.URL.Query().Get("token")
	if tokenStr == "" {
		conn.WriteJSON(map[string]string{"error": "authorization token required"})
		conn.Close()
		return
	}

	token, err := app.Auth.ValidateToken(tokenStr)
	if err != nil {
		conn.WriteJSON(map[string]string{"error": "invalid token"})
		conn.Close()
		return
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		conn.WriteJSON(map[string]string{"error": "failed to parse claims"})
		conn.Close()
		return
	}
	userIDStr := claims["sub"].(string)
	userID, _ := uuid.Parse(userIDStr)

	client := &Client{
		hub:     app.ChatHub,
		conn:    conn,
		send:    make(chan interface{}, 256),
		eventID: eventID,
		userID:  userID,
	}
	client.hub.register <- client

	// Start read/write pumps
	go client.writePump()
	go client.readPump()
}
