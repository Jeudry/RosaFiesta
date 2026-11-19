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

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	paymentshttp "github.com/Jeudry/rosafiesta/server/internal/payments/httpapi"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type stubPrincipalResolver struct {
	principals map[string]platformhttp.Principal
}

func (s stubPrincipalResolver) Authenticate(_ context.Context, accessToken string) (uuid.UUID, error) {
	principal, ok := s.principals[accessToken]
	if !ok {
		return uuid.Nil, errors.New("unauthorized")
	}
	return principal.ID, nil
}

func (s stubPrincipalResolver) AuthorizePermission(_ context.Context, userID uuid.UUID, permission string) error {
	for _, principal := range s.principals {
		if principal.ID == userID {
			if principal.HasPermission(permission) {
				return nil
			}
			return errors.New("forbidden")
		}
	}
	return errors.New("forbidden")
}

func (s stubPrincipalResolver) ResolvePrincipal(_ context.Context, userID uuid.UUID) (platformhttp.Principal, error) {
	for _, principal := range s.principals {
		if principal.ID == userID {
			return principal, nil
		}
	}
	return platformhttp.Principal{}, errors.New("not found")
}

func newPaymentsTestRouter() (http.Handler, *app.Service) {
	svc := app.NewService()
	finanzasID := uuid.New()
	decorID := uuid.New()
	auth := stubPrincipalResolver{
		principals: map[string]platformhttp.Principal{
			"finanzas-token": {
				ID:          finanzasID,
				Email:       "finanzas@rosafiesta.do",
				FullName:    "Rosa Finanzas",
				Role:        "staff",
				Permissions: []string{"finanzas"},
			},
			"decor-token": {
				ID:          decorID,
				Email:       "decor@rosafiesta.do",
				FullName:    "Yuli Decor",
				Role:        "staff",
				Permissions: []string{"decoracion"},
			},
		},
	}
	mux := chi.NewMux()
	api := humachi.New(mux, huma.DefaultConfig("Payments Test", "1.0.0"))
	paymentshttp.Module{Service: svc, Auth: auth}.Register(api)
	return mux, svc
}

func TestPaymentsHTTP_InvalidRNC_And_CardGateway503_And_VerifyPermissions(t *testing.T) {
	t.Parallel()
	router, _ := newPaymentsTestRouter()

	badRNCReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/payments",
		bytes.NewBufferString(`{"eventId":"evt-1","eventTitle":"Boda Test","clientName":"Empresa","amountRD":1180,"method":"efectivo","kind":"anticipo","referenceCode":"REF-B01-BAD","ncfType":"B01","clientRnc":"999"}`),
	)
	badRNCReq.Header.Set("Authorization", "Bearer finanzas-token")
	badRNCReq.Header.Set("Content-Type", "application/json")
	badRNCRec := httptest.NewRecorder()
	router.ServeHTTP(badRNCRec, badRNCReq)
	if badRNCRec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400 on invalid B01 RNC, got %d: %s", badRNCRec.Code, badRNCRec.Body.String())
	}
	if !strings.Contains(badRNCRec.Body.String(), "INVALID_FISCAL_RNC") {
		t.Fatalf("expected INVALID_FISCAL_RNC code, got %s", badRNCRec.Body.String())
	}

	cardReq := httptest.NewRequestWithContext(
		t.Context(),
		http.MethodPost,
		"/v1/payments/card-session",
		bytes.NewBufferString(`{"eventId":"evt-1","eventTitle":"Boda Test","amountRD":1500}`),
	)
	cardReq.Header.Set("Authorization", "Bearer finanzas-token")
	cardReq.Header.Set("Content-Type", "application/json")
	cardRec := httptest.NewRecorder()
	router.ServeHTTP(cardRec, cardReq)
	if cardRec.Code != http.StatusServiceUnavailable {
		t.Fatalf("expected 503 CARD_GATEWAY_NOT_CONFIGURED, got %d: %s", cardRec.Code, cardRec.Body.String())
	}
	if !strings.Contains(cardRec.Body.String(), "CARD_GATEWAY_NOT_CONFIGURED") {
		t.Fatalf("expected CARD_GATEWAY_NOT_CONFIGURED error code, got %s", cardRec.Body.String())
	}

	forbiddenVerify := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/payments/pay-grad-anticipo/verify", bytes.NewBufferString(`{}`))
	forbiddenVerify.Header.Set("Authorization", "Bearer decor-token")
	forbiddenRec := httptest.NewRecorder()
	router.ServeHTTP(forbiddenRec, forbiddenVerify)
	if forbiddenRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 for decoracion staff verifying payment, got %d", forbiddenRec.Code)
	}

	verifyReq := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/payments/pay-grad-anticipo/verify", bytes.NewBufferString(`{"notes":"Verificado OK"}`))
	verifyReq.Header.Set("Authorization", "Bearer finanzas-token")
	verifyRec := httptest.NewRecorder()
	router.ServeHTTP(verifyRec, verifyReq)
	if verifyRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on verify with finanzas permission, got %d: %s", verifyRec.Code, verifyRec.Body.String())
	}
	if !strings.Contains(verifyRec.Body.String(), "B0200000001") {
		t.Fatalf("expected sequential NCF B0200000001 in response, got %s", verifyRec.Body.String())
	}

	dgiiReq := httptest.NewRequestWithContext(t.Context(), http.MethodGet, "/v1/payments/dgii-607", nil)
	dgiiReq.Header.Set("Authorization", "Bearer finanzas-token")
	dgiiRec := httptest.NewRecorder()
	router.ServeHTTP(dgiiRec, dgiiReq)
	if dgiiRec.Code != http.StatusOK {
		t.Fatalf("expected 200 on DGII 607 export, got %d", dgiiRec.Code)
	}
	if !strings.Contains(dgiiRec.Body.String(), "B0200000001") {
		t.Fatalf("expected verified payment NCF in DGII 607 CSV, got %s", dgiiRec.Body.String())
	}
}
