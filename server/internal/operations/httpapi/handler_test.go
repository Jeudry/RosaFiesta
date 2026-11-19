package httpapi_test

import (
	"bytes"
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/danielgtaylor/huma/v2"
	"github.com/danielgtaylor/huma/v2/adapters/humachi"
	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
	opshttp "github.com/Jeudry/rosafiesta/server/internal/operations/httpapi"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type stubOpsAuth struct {
	principal platformhttp.Principal
}

func (s stubOpsAuth) Authenticate(_ context.Context, accessToken string) (uuid.UUID, error) {
	if accessToken != "admin-token" {
		return uuid.Nil, errors.New("unauthorized")
	}
	return s.principal.ID, nil
}

func (s stubOpsAuth) AuthorizePermission(_ context.Context, userID uuid.UUID, permission string) error {
	if userID == s.principal.ID {
		if s.principal.HasPermission(permission) {
			return nil
		}
	}
	return errors.New("forbidden")
}

func (s stubOpsAuth) ResolvePrincipal(_ context.Context, userID uuid.UUID) (platformhttp.Principal, error) {
	if userID == s.principal.ID {
		return s.principal, nil
	}
	return platformhttp.Principal{}, errors.New("not found")
}

func newOpsTestRouter() (http.Handler, *app.Service) {
	svc := app.NewService()
	auth := stubOpsAuth{
		principal: platformhttp.Principal{
			ID:       uuid.New(),
			Email:    "maria@rosafiesta.do",
			FullName: "María",
			Role:     "admin",
		},
	}
	mux := chi.NewMux()
	api := humachi.New(mux, huma.DefaultConfig("Operations Test", "1.0.0"))
	opshttp.Module{Service: svc, Auth: auth}.Register(api)
	return mux, svc
}

func TestOperationsHTTP_DamageResolve_And_WhatsApp503AndWebhook(t *testing.T) {
	t.Parallel()
	router, svc := newOpsTestRouter()

	sendReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/bot/whatsapp/send",
		bytes.NewBufferString(`{"recipientPhone":"+18095550101","message":"Hola"}`),
	)
	sendReq.Header.Set("Authorization", "Bearer admin-token")
	sendReq.Header.Set("Content-Type", "application/json")
	sendRec := httptest.NewRecorder()
	router.ServeHTTP(sendRec, sendReq)
	if sendRec.Code != http.StatusServiceUnavailable {
		t.Fatalf("expected 503 WHATSAPP_PROVIDER_NOT_CONFIGURED, got %d: %s", sendRec.Code, sendRec.Body.String())
	}
	if !strings.Contains(sendRec.Body.String(), "WHATSAPP_PROVIDER_NOT_CONFIGURED") {
		t.Fatalf("expected WHATSAPP_PROVIDER_NOT_CONFIGURED error code, got %s", sendRec.Body.String())
	}

	svc.WithWhatsAppConfig("123", "tok", "my-verify-token")
	verifyReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodGet,
		"/v1/webhooks/whatsapp?hub.mode=subscribe&hub.verify_token=my-verify-token&hub.challenge=CHALLENGE_99",
		nil,
	)
	verifyRec := httptest.NewRecorder()
	router.ServeHTTP(verifyRec, verifyReq)
	if verifyRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on webhook verify, got %d", verifyRec.Code)
	}
	if verifyRec.Body.String() != "CHALLENGE_99" {
		t.Fatalf("expected CHALLENGE_99, got %q", verifyRec.Body.String())
	}

	resolveReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/operations/damage/dmg-1/resolve",
		bytes.NewBufferString(`{"notes":"Repuesto instalado"}`),
	)
	resolveReq.Header.Set("Authorization", "Bearer admin-token")
	resolveRec := httptest.NewRecorder()
	router.ServeHTTP(resolveRec, resolveReq)
	if resolveRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on damage resolve, got %d: %s", resolveRec.Code, resolveRec.Body.String())
	}
	if !strings.Contains(resolveRec.Body.String(), `"status":"resuelto"`) {
		t.Fatalf("expected status resuelto in response, got %s", resolveRec.Body.String())
	}
}
