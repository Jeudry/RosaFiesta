package app_test

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/health"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimeapp "github.com/Jeudry/rosafiesta/server/internal/realtime/app"
)

func TestMediaUploadAndTranscribeBodyLimits(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	tempDir := t.TempDir()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{
		Config: config.Config{
			MediaStorageDir: filepath.Join(tempDir, "media"),
			MediaMaxBytes:   16 << 20,
		},
	})...)

	// Valid PNG signature + 1.2 MiB payload (> Huma default 1 MiB, < 16 MiB).
	payload := make([]byte, 1250*1024)
	copy(payload, []byte{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})
	encoded := base64.StdEncoding.EncodeToString(payload)
	reqBody := `{"filename":"montaje.png","purpose":"setup","data_base64":"` + encoded + `"}`

	req := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/media", strings.NewReader(reqBody))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer rf-token-maria")
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201 for 1.2 MiB upload, got %d: %s", rec.Code, rec.Body.String())
	}

	// Audio transcribe payload > 1 MiB must not fail with 413 Request Entity Too Large.
	audioPayload := base64.StdEncoding.EncodeToString(bytes.Repeat([]byte{0x1A}, 1150*1024))
	transReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/rosa/transcribe", strings.NewReader(`{"audioBase64":"`+audioPayload+`","mimeType":"audio/mp4","locale":"es-DO"}`))
	transReq.Header.Set("Content-Type", "application/json")
	transReq.Header.Set("Authorization", "Bearer rf-token-maria")
	transRec := httptest.NewRecorder()
	handler.ServeHTTP(transRec, transReq)
	if transRec.Code == http.StatusRequestEntityTooLarge {
		t.Fatalf("expected /v1/rosa/transcribe >1 MiB not to return 413, got 413: %s", transRec.Body.String())
	}

	// Body over 16 MiB must be rejected cleanly with 413.
	tooBig := bytes.Repeat([]byte("A"), (16<<20)+1024)
	bigReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/media", bytes.NewReader(tooBig))
	bigReq.Header.Set("Content-Type", "application/json")
	bigReq.Header.Set("Authorization", "Bearer rf-token-maria")
	bigRec := httptest.NewRecorder()
	handler.ServeHTTP(bigRec, bigReq)
	if bigRec.Code != http.StatusRequestEntityTooLarge {
		t.Fatalf("expected 413 for >16 MiB payload, got %d", bigRec.Code)
	}
}

func TestReviewIDORAndClientChecklistOwnership(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(app.Dependencies{
		Config: config.Config{MediaStorageDir: filepath.Join(t.TempDir(), "media")},
	})...)

	// Register a second distinct client who does NOT own evt-boda-real (owned by Camila).
	regReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/auth/register", strings.NewReader(`{
		"username":"otrocliente",
		"fullName":"Otro Cliente",
		"email":"otro@rosafiesta.do",
		"password":"PasswordSeguro123!",
		"birthDate":"1994-03-10",
		"phone":"809-555-0888"
	}`))
	regReq.Header.Set("Content-Type", "application/json")
	regRec := httptest.NewRecorder()
	handler.ServeHTTP(regRec, regReq)
	if regRec.Code != http.StatusOK {
		t.Fatalf("register other client failed: %d: %s", regRec.Code, regRec.Body.String())
	}
	otherToken := extractJSONField(t, regRec.Body.Bytes(), "accessToken")

	// Other client cannot post a review on Camila's event (Review IDOR protection).
	revReq := httptest.NewRequestWithContext(ctx, http.MethodPost, "/v1/reviews", strings.NewReader(`{
		"eventId":"evt-boda-real",
		"rating":5,
		"comment":"Intentando reseñar evento ajeno"
	}`))
	revReq.Header.Set("Content-Type", "application/json")
	revReq.Header.Set("Authorization", "Bearer "+otherToken)
	revRec := httptest.NewRecorder()
	handler.ServeHTTP(revRec, revReq)
	if revRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 on review IDOR attempt, got %d: %s", revRec.Code, revRec.Body.String())
	}

	// Owner client (Camila) CAN toggle checklist step on her own event.
	ownerPatch := httptest.NewRequestWithContext(ctx, http.MethodPatch, "/v1/operations/events/evt-boda-real/checklist/chk-5", strings.NewReader(`{"completed":true}`))
	ownerPatch.Header.Set("Content-Type", "application/json")
	ownerPatch.Header.Set("Authorization", "Bearer rf-token-camila")
	ownerRec := httptest.NewRecorder()
	handler.ServeHTTP(ownerRec, ownerPatch)
	if ownerRec.Code != http.StatusOK {
		t.Fatalf("expected 200 when event owner toggles checklist item, got %d: %s", ownerRec.Code, ownerRec.Body.String())
	}

	// Non-owner client is blocked with 403 on checklist PATCH.
	otherPatch := httptest.NewRequestWithContext(ctx, http.MethodPatch, "/v1/operations/events/evt-boda-real/checklist/chk-5", strings.NewReader(`{"completed":false}`))
	otherPatch.Header.Set("Content-Type", "application/json")
	otherPatch.Header.Set("Authorization", "Bearer "+otherToken)
	otherPatchRec := httptest.NewRecorder()
	handler.ServeHTTP(otherPatchRec, otherPatch)
	if otherPatchRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 when non-owner toggles checklist item, got %d: %s", otherPatchRec.Code, otherPatchRec.Body.String())
	}
}

func TestReadyzChecksStorageFailureAndProdRejectsBrokenS3(t *testing.T) {
	t.Parallel()
	ctx := t.Context()

	unhealthyRouter, _ := httpapi.New(slog.New(slog.DiscardHandler), health.Module{
		Checks: []health.NamedCheck{
			{Name: "storage", Check: func(context.Context) error { return context.DeadlineExceeded }},
		},
	})
	req := httptest.NewRequestWithContext(ctx, http.MethodGet, "/readyz", nil)
	rec := httptest.NewRecorder()
	unhealthyRouter.ServeHTTP(rec, req)
	if rec.Code != http.StatusServiceUnavailable {
		t.Fatalf("expected 503 when readiness dependency check fails, got %d", rec.Code)
	}

	_, err := app.BuildModules(app.Dependencies{
		Config: config.Config{
			Env:                 config.EnvProduction,
			MediaStorageBackend: "s3",
		},
	})
	if err == nil {
		t.Fatal("expected BuildModules to fail fast in production when S3 init fails")
	}
}

func TestRealtimeBrokerWaitStreamsDrainsCleanly(t *testing.T) {
	t.Parallel()
	broker := realtimeapp.NewBroker(time.Now)
	release := broker.TrackStream()

	waitCtx, cancelWait := context.WithTimeout(t.Context(), 30*time.Millisecond)
	defer cancelWait()
	if broker.WaitStreams(waitCtx) {
		t.Fatal("expected WaitStreams to block while stream is active")
	}

	release()
	drainCtx, cancelDrain := context.WithTimeout(t.Context(), time.Second)
	defer cancelDrain()
	if !broker.WaitStreams(drainCtx) {
		t.Fatal("expected WaitStreams to return true once stream releases")
	}
}

func extractJSONField(t *testing.T, raw []byte, field string) string {
	t.Helper()
	var parsed map[string]any
	if err := json.Unmarshal(raw, &parsed); err != nil {
		t.Fatalf("unmarshal json response: %v", err)
	}
	val, _ := parsed[field].(string)
	if val == "" {
		t.Fatalf("missing json field %q in %s", field, string(raw))
	}
	return val
}
