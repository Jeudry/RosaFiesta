package app_test

import (
	"bufio"
	"context"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"path/filepath"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestCrossAppPublicItbisChecklistProvisioningAndSSE(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{
		Config: config.Config{MediaStorageDir: filepath.Join(t.TempDir(), "media")},
	})...)

	srv := httptest.NewServer(handler)
	defer srv.Close()

	streamCtx, cancelStream := context.WithCancel(ctx)
	seenMu := &sync.Mutex{}
	seenTypes := make(map[string]bool)
	readyCh := make(chan struct{})
	doneCh := make(chan struct{})
	go readSSEStreamTypes(streamCtx, srv.URL+"/v1/stream", "rf-token-maria", seenMu, seenTypes, readyCh, doneCh)
	defer func() {
		cancelStream()
		<-doneCh
	}()
	waitForSSEReady(t, readyCh)

	// 1. Admin sets 0% ITBIS -> public settings must expose itbisPercent: 0 and broadcast catalog.updated SSE.
	mustJSONRequest(t, handler, http.MethodPut, "/v1/operations/settings", "rf-token-maria", `{"itbisPercent":0}`, http.StatusOK)
	pubBody := mustJSONRequest(t, handler, http.MethodGet, "/v1/operations/settings/public", "", "", http.StatusOK)
	if !strings.Contains(string(pubBody), `"itbisPercent":0`) {
		t.Fatalf("expected public settings to include itbisPercent:0, got %s", string(pubBody))
	}

	// 2. Client creates a brand-new event -> draft logistics + checklist must be immediately available (no 404).
	evtBody := mustJSONRequest(t, handler, http.MethodPost, "/v1/events", "rf-token-camila", `{
		"title":"Evento Recien Creado",
		"eventType":"Boda",
		"dateLabel":"28 Dic",
		"eventDateIso":"2026-12-28",
		"venue":"Salon Central",
		"guests":50,
		"items":[]
	}`, http.StatusOK)
	newEventID := extractNestedJSONField(t, evtBody, "event", "id")

	mustJSONRequest(t, handler, http.MethodGet, "/v1/operations/events/"+newEventID, "rf-token-camila", "", http.StatusOK)
	firstChkID := "chk-" + newEventID + "-1"
	mustJSONRequest(t, handler, http.MethodPatch, "/v1/operations/events/"+newEventID+"/checklist/"+firstChkID, "rf-token-camila", `{"completed":true}`, http.StatusOK)

	// 3. Client requests a quote and then declines it -> Admin must observe quote.created and quote.declined SSE.
	qBody := mustJSONRequest(t, handler, http.MethodPost, "/v1/quotes", "rf-token-camila", `{
		"eventId":"`+newEventID+`",
		"eventTitle":"Evento Recien Creado",
		"clientName":"Camila",
		"eventDateLabel":"28 Dic",
		"venue":"Salon Central",
		"guests":50,
		"deliveryFeeRD":1200,
		"discountRD":0,
		"lines":[{"articleCode":"RF-001","name":"Arco Floral","quantity":1,"unitPriceRD":2000}]
	}`, http.StatusOK)
	newQuoteID := extractNestedJSONField(t, qBody, "quote", "id")
	mustJSONRequest(t, handler, http.MethodPost, "/v1/quotes/"+newQuoteID+"/decline", "rf-token-camila", `{"reason":"Ajustar flores"}`, http.StatusOK)

	// 4. Rejecting a transfer payment with blank reason must fail with 400 INVALID_REJECTION_REASON.
	payBody := mustJSONRequest(t, handler, http.MethodPost, "/v1/payments", "rf-token-camila", `{
		"eventId":"`+newEventID+`",
		"eventTitle":"Evento Recien Creado",
		"clientName":"Camila",
		"amountRD":1500,
		"method":"transferencia",
		"kind":"anticipo",
		"ncfType":"B02",
		"referenceCode":"REF-BLANK-CHECK",
		"voucherUrl":"/uploads/check.png"
	}`, http.StatusOK)
	payID := extractNestedJSONField(t, payBody, "payment", "id")
	mustJSONRequest(t, handler, http.MethodPost, "/v1/payments/"+payID+"/reject", "rf-token-maria", `{"reason":"   "}`, http.StatusBadRequest)

	requiredTypes := []string{"catalog.updated", "logistics.updated", "quote.created", "quote.declined"}
	waitForRequiredSSE(t, seenMu, seenTypes, requiredTypes, 3*time.Second)
}

func TestPaymentReminderRespectsEventRemindersPreference(t *testing.T) {
	t.Parallel()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{
		Config: config.Config{MediaStorageDir: filepath.Join(t.TempDir(), "media")},
	})...)

	mustJSONRequest(t, handler, http.MethodPatch, "/v1/auth/me/preferences", "rf-token-camila", `{"pushNotificationsEnabled":true,"eventRemindersEnabled":false}`, http.StatusOK)
	beforeBody := mustJSONRequest(t, handler, http.MethodGet, "/v1/notifications", "rf-token-camila", "", http.StatusOK)
	beforeCount := strings.Count(string(beforeBody), `"id":"ntf-`)

	mustJSONRequest(t, handler, http.MethodPost, "/v1/events/evt-boda-real/payment-reminder", "rf-token-maria", `{"channel":"push"}`, http.StatusOK)
	afterBody := mustJSONRequest(t, handler, http.MethodGet, "/v1/notifications", "rf-token-camila", "", http.StatusOK)
	afterCount := strings.Count(string(afterBody), `"id":"ntf-`)
	if afterCount != beforeCount {
		t.Fatalf("expected no reminder notification when eventRemindersEnabled=false (before=%d after=%d)", beforeCount, afterCount)
	}
}

func mustJSONRequest(
	t *testing.T,
	handler http.Handler,
	method string,
	path string,
	token string,
	bodyJSON string,
	expectedStatus int,
) []byte {
	t.Helper()
	var bodyReader *strings.Reader
	if bodyJSON != "" {
		bodyReader = strings.NewReader(bodyJSON)
	} else {
		bodyReader = strings.NewReader("")
	}
	req := httptest.NewRequestWithContext(t.Context(), method, path, bodyReader)
	if bodyJSON != "" {
		req.Header.Set("Content-Type", "application/json")
	}
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != expectedStatus {
		t.Fatalf("%s %s expected %d, got %d: %s", method, path, expectedStatus, rec.Code, rec.Body.String())
	}
	return rec.Body.Bytes()
}

func waitForSSEReady(t *testing.T, readyCh <-chan struct{}) {
	t.Helper()
	select {
	case <-readyCh:
	case <-time.After(3 * time.Second):
		t.Fatal("SSE test stream did not become ready")
	}
}

func extractNestedJSONField(t *testing.T, raw []byte, outerKey, innerKey string) string {
	t.Helper()
	var top map[string]any
	if err := json.Unmarshal(raw, &top); err != nil {
		t.Fatalf("unmarshal nested json (%s.%s): %v", outerKey, innerKey, err)
	}
	nested, ok := top[outerKey].(map[string]any)
	if !ok {
		t.Fatalf("missing object key %q in %s", outerKey, string(raw))
	}
	val, exists := nested[innerKey]
	if !exists {
		t.Fatalf("missing inner key %q in %s", innerKey, string(raw))
	}
	strVal, ok := val.(string)
	if !ok {
		t.Fatalf("expected string for %s.%s, got %T", outerKey, innerKey, val)
	}
	return strVal
}

func readSSEStreamTypes(
	ctx context.Context,
	streamURL string,
	token string,
	seenMu *sync.Mutex,
	seenTypes map[string]bool,
	readyCh chan<- struct{},
	doneCh chan<- struct{},
) {
	defer close(doneCh)
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, streamURL, nil)
	if err != nil {
		close(readyCh)
		return
	}
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("Accept", "text/event-stream")
	resp, err := (&http.Client{}).Do(req)
	close(readyCh)
	if err != nil {
		return
	}
	defer func() { _ = resp.Body.Close() }()
	scanner := bufio.NewScanner(resp.Body)
	for scanner.Scan() {
		line := scanner.Text()
		if !strings.HasPrefix(line, "data:") {
			continue
		}
		rawJSON := strings.TrimSpace(strings.TrimPrefix(line, "data:"))
		var payload struct {
			Type string `json:"type"`
		}
		if unmarshalErr := json.Unmarshal([]byte(rawJSON), &payload); unmarshalErr == nil {
			if payload.Type != "" {
				seenMu.Lock()
				seenTypes[payload.Type] = true
				seenMu.Unlock()
			}
		}
	}
}

func waitForRequiredSSE(
	t *testing.T,
	seenMu *sync.Mutex,
	seenTypes map[string]bool,
	required []string,
	timeout time.Duration,
) {
	t.Helper()
	deadline := time.Now().Add(timeout)
	for time.Now().Before(deadline) {
		seenMu.Lock()
		allFound := true
		for _, eventType := range required {
			if !seenTypes[eventType] {
				allFound = false
				break
			}
		}
		seenMu.Unlock()
		if allFound {
			return
		}
		time.Sleep(25 * time.Millisecond)
	}
	seenMu.Lock()
	defer seenMu.Unlock()
	t.Fatalf("missing required SSE event types %v; seen=%v", required, seenTypes)
}
