package main

import (
	"sync"

	"github.com/google/uuid"
)

// Hub maintains the set of active clients and broadcasts messages to them.
type Hub struct {
	// Registered clients grouped by EventID.
	clients map[uuid.UUID]map[*Client]bool

	// Inbound messages from the clients.
	broadcast chan *BroadcastMessage

	// Register requests from the clients.
	register chan *Client

	// Unregister requests from clients.
	unregister chan *Client

	mu sync.RWMutex
}

// BroadcastMessage encapsulates a message and the event it belongs to.
type BroadcastMessage struct {
	EventID uuid.UUID
	Payload interface{}
}

func newHub() *Hub {
	return &Hub{
		broadcast:  make(chan *BroadcastMessage),
		register:   make(chan *Client),
		unregister: make(chan *Client),
		clients:    make(map[uuid.UUID]map[*Client]bool),
	}
}

func (h *Hub) run() {
	for {
		select {
		case client := <-h.register:
			h.mu.Lock()
			if h.clients[client.eventID] == nil {
				h.clients[client.eventID] = make(map[*Client]bool)
			}
			h.clients[client.eventID][client] = true
			h.mu.Unlock()

		case client := <-h.unregister:
			h.mu.Lock()
			if clients, ok := h.clients[client.eventID]; ok {
				if _, ok := clients[client]; ok {
					delete(clients, client)
					close(client.send)
					if len(clients) == 0 {
						delete(h.clients, client.eventID)
					}
				}
			}
			h.mu.Unlock()

		case message := <-h.broadcast:
			h.mu.RLock()
			clients := h.clients[message.EventID]
			for client := range clients {
				select {
				case client.send <- message.Payload:
				default:
					// If the send buffer is full, unregister the client.
					// This is handled in the next loop iteration or via unregister channel.
				}
			}
			h.mu.RUnlock()
		}
	}
}
