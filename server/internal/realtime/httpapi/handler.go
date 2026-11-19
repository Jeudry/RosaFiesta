// Package httpapi expone el endpoint SSE GET /v1/stream y estado de difusión.
package httpapi

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/danielgtaylor/huma/v2"
	"github.com/go-chi/chi/v5"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/realtime/app"
	"github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

const heartbeatInterval = 20 * time.Second

// Module registra el estado OpenAPI y el flujo SSE nativo sobre chi.
type Module struct {
	Broker *app.Broker
	Auth   platformhttp.Authenticator
}

type streamHealthOutput struct {
	Body struct {
		Status string `json:"status"`
		Path   string `json:"path"`
	}
}

// Register documenta el punto de estado del canal en tiempo real.
func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "get-stream-status",
		Method:      http.MethodGet,
		Path:        "/v1/stream/status",
		Summary:     "Estado del canal de eventos en tiempo real (SSE)",
		Tags:        []string{"Realtime"},
		Security:    []map[string][]string{{platformhttp.SecurityScheme: {}}},
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)},
	}, func(_ context.Context, _ *struct{}) (*streamHealthOutput, error) {
		out := &streamHealthOutput{}
		out.Body.Status = "ready"
		out.Body.Path = "/v1/stream"
		return out, nil
	})
}

// RegisterRoutes registra el flujo SSE continuo en GET /v1/stream.
func (m Module) RegisterRoutes(router chi.Router) {
	router.Get("/v1/stream", m.handleSSE)
}

func (m Module) handleSSE(w http.ResponseWriter, r *http.Request) {
	principal, ok := m.authenticateRequest(r)
	if !ok {
		http.Error(w, "authentication required", http.StatusUnauthorized)
		return
	}
	sinceSeq := parseSinceSeq(r)
	filter := app.SubscriberFilter{
		PrincipalID:   principal.ID.String(),
		PrincipalRole: principal.Role,
		TopicPrefix:   strings.TrimSpace(r.URL.Query().Get("topic")),
		EventID:       strings.TrimSpace(r.URL.Query().Get("event_id")),
	}
	m.streamEvents(w, r, filter, sinceSeq)
}

func (m Module) streamEvents(
	w http.ResponseWriter,
	r *http.Request,
	filter app.SubscriberFilter,
	sinceSeq int64,
) {
	headers := w.Header()
	headers.Set("Content-Type", "text/event-stream; charset=utf-8")
	headers.Set("Cache-Control", "no-cache, no-transform")
	headers.Set("Connection", "keep-alive")
	headers.Set("X-Accel-Buffering", "no")

	rc := http.NewResponseController(w)
	ch, cancel := m.Broker.Subscribe(r.Context(), filter, sinceSeq)
	defer cancel()

	_, _ = fmt.Fprintf(w, ": connected\n\n")
	_ = rc.Flush()

	ticker := time.NewTicker(heartbeatInterval)
	defer ticker.Stop()

	for {
		select {
		case <-r.Context().Done():
			return
		case event, open := <-ch:
			if !open {
				return
			}
			writeSSEFrame(w, rc, event)
		case <-ticker.C:
			_, _ = fmt.Fprintf(w, ": keepalive\n\n")
			_ = rc.Flush()
		}
	}
}

func (m Module) authenticateRequest(r *http.Request) (platformhttp.Principal, bool) {
	if m.Auth == nil {
		return platformhttp.Principal{}, false
	}
	token := extractToken(r)
	if token == "" {
		return platformhttp.Principal{}, false
	}
	userID, err := m.Auth.Authenticate(r.Context(), token)
	if err != nil {
		return platformhttp.Principal{}, false
	}
	if resolver, ok := m.Auth.(platformhttp.PrincipalResolver); ok {
		principal, resolveErr := resolver.ResolvePrincipal(r.Context(), userID)
		if resolveErr == nil {
			return principal, true
		}
	}
	return platformhttp.Principal{ID: userID, Role: "client"}, true
}

func extractToken(r *http.Request) string {
	authHeader := strings.TrimSpace(r.Header.Get("Authorization"))
	if strings.HasPrefix(strings.ToLower(authHeader), "bearer ") {
		return strings.TrimSpace(authHeader[7:])
	}
	return strings.TrimSpace(r.URL.Query().Get("access_token"))
}

func parseSinceSeq(r *http.Request) int64 {
	raw := strings.TrimSpace(r.Header.Get("Last-Event-ID"))
	if raw == "" {
		raw = strings.TrimSpace(r.URL.Query().Get("since_seq"))
	}
	if raw == "" {
		return 0
	}
	parsed, err := strconv.ParseInt(raw, 10, 64)
	if err != nil {
		return 0
	}
	if parsed < 0 {
		return 0
	}
	return parsed
}

func writeSSEFrame(w http.ResponseWriter, rc *http.ResponseController, event domain.RealtimeEvent) {
	payload, err := json.Marshal(event)
	if err != nil {
		return
	}
	_, _ = fmt.Fprintf(w, "id: %d\nevent: %s\ndata: %s\n\n", event.Seq, event.Type, payload)
	_ = rc.Flush()
}
