// Package domain define los contratos puros de eventos en tiempo real (SSE).
package domain

import (
	"strings"
	"time"
)

// EventType clasifica los eventos difundidos por el flujo SSE.
type EventType string

const (
	EventQuoteCreated        EventType = "quote.created"
	EventQuoteSent           EventType = "quote.sent"
	EventQuoteSigned         EventType = "quote.signed"
	EventEventUpdated        EventType = "event.updated"
	EventPaymentPending      EventType = "payment.pending_verification"
	EventPaymentVerified     EventType = "payment.verified"
	EventPaymentRejected     EventType = "payment.rejected"
	EventChatMessage         EventType = "chat.message"
	EventLogisticsUpdated    EventType = "logistics.updated"
	EventDamageReported      EventType = "damage.reported"
	EventCatalogUpdated      EventType = "catalog.updated"
	EventNotificationCreated EventType = "notification.created"
)

// Audience define qué perfiles reciben el evento en tiempo real.
type Audience string

const (
	AudienceAll    Audience = "all"
	AudienceAdmin  Audience = "admin"
	AudienceClient Audience = "client"
)

// RealtimeEvent representa un mensaje estructurado emitido por el servidor hacia los clientes conectados.
type RealtimeEvent struct {
	Seq        int64          `json:"seq"`
	ID         string         `json:"id"`
	Type       EventType      `json:"type"`
	EntityID   string         `json:"entity_id"`
	ClientID   string         `json:"client_id,omitempty"`
	Audience   Audience       `json:"audience"`
	Summary    string         `json:"summary"`
	Payload    map[string]any `json:"payload,omitempty"`
	OccurredAt time.Time      `json:"occurred_at"`
}

// Publisher permite a cualquier módulo publicar eventos sin conocer el transporte HTTP.
type Publisher interface {
	Publish(event RealtimeEvent) RealtimeEvent
}

// CanReceive evalúa si un usuario autenticado está autorizado para recibir el evento.
func (e RealtimeEvent) CanReceive(principalRole, principalID string) bool {
	normalizedRole := strings.ToLower(strings.TrimSpace(principalRole))
	isStaffOrAdmin := isPrivilegedRole(normalizedRole)
	if isStaffOrAdmin {
		return true
	}
	if e.Audience == AudienceAdmin {
		return false
	}
	if e.Audience == AudienceAll {
		return true
	}
	targetClient := strings.TrimSpace(e.ClientID)
	if targetClient == "" {
		return true
	}
	return targetClient == strings.TrimSpace(principalID)
}

func isPrivilegedRole(role string) bool {
	switch role {
	case "admin", "staff", "manager", "supervisor":
		return true
	default:
		return false
	}
}
