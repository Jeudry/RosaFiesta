package app_test

import (
	"bytes"
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	eventsapp "github.com/Jeudry/rosafiesta/server/internal/events/app"
	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	operationsapp "github.com/Jeudry/rosafiesta/server/internal/operations/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
)

type recordingPushSender struct {
	messages []push.Message
}

func (r *recordingPushSender) IsConfigured() bool { return true }

func (r *recordingPushSender) SendPush(_ context.Context, msg push.Message) error {
	r.messages = append(r.messages, msg)
	return nil
}

func TestEventReminderWorker_PrePostOptOutAndIdempotency(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	now := time.Date(2026, 11, 24, 10, 0, 0, 0, time.UTC)
	clk := &clock.Fixed{At: now}

	idSvc := identityapp.NewService(clk, nil)
	evtSvc := eventsapp.NewService().WithClock(clk)
	opsSvc := operationsapp.NewService().WithClock(clk)

	userPre, _, err := idSvc.Register(ctx, identityapp.RegisterCommand{
		Username: "ana_pre",
		FullName: "Ana Pre",
		Email:    "ana@example.com",
		Phone:    "809-555-1111",
		Password: "Password123!",
	})
	if err != nil {
		t.Fatalf("register userPre: %v", err)
	}
	userOptOut, _, err := idSvc.Register(ctx, identityapp.RegisterCommand{
		Username: "luis_off",
		FullName: "Luis Off",
		Email:    "luis@example.com",
		Phone:    "809-555-2222",
		Password: "Password123!",
	})
	if err != nil {
		t.Fatalf("register userOptOut: %v", err)
	}
	disabled := false
	if _, err := idSvc.UpdatePreferences(ctx, userOptOut.ID, identityapp.UpdatePreferencesCommand{
		EventRemindersEnabled: &disabled,
	}); err != nil {
		t.Fatalf("disable reminders: %v", err)
	}

	_, err = evtSvc.ProvisionFromSignedQuote(ctx, eventsapp.ProvisionFromQuoteCommand{
		EventID:      "evt-pre-soon",
		QuoteID:      "quo-pre",
		ClientID:     userPre.ID,
		ClientEmail:  userPre.Email,
		ClientName:   userPre.FullName,
		Title:        "Boda Mañana",
		DateLabel:    "Mié 25 nov",
		EventDateISO: "2026-11-25",
		Venue:        "Piantini",
		Guests:       40,
		TotalRD:      15000,
	})
	if err != nil {
		t.Fatalf("provision evt-pre-soon: %v", err)
	}
	_, err = evtSvc.ProvisionFromSignedQuote(ctx, eventsapp.ProvisionFromQuoteCommand{
		EventID:      "evt-post-done",
		QuoteID:      "quo-post",
		ClientID:     userPre.ID,
		ClientEmail:  userPre.Email,
		ClientName:   userPre.FullName,
		Title:        "Evento Ayer",
		DateLabel:    "Lun 23 nov",
		EventDateISO: "2026-11-23",
		Venue:        "Naco",
		Guests:       30,
		TotalRD:      9000,
	})
	if err != nil {
		t.Fatalf("provision evt-post-done: %v", err)
	}
	_, err = evtSvc.ProvisionFromSignedQuote(ctx, eventsapp.ProvisionFromQuoteCommand{
		EventID:      "evt-opt-out",
		QuoteID:      "quo-opt-out",
		ClientID:     userOptOut.ID,
		ClientEmail:  userOptOut.Email,
		ClientName:   userOptOut.FullName,
		Title:        "Evento Silenciado",
		DateLabel:    "Mié 25 nov",
		EventDateISO: "2026-11-25",
		Venue:        "Gazcue",
		Guests:       20,
		TotalRD:      6000,
	})
	if err != nil {
		t.Fatalf("provision evt-opt-out: %v", err)
	}

	pushSpy := &recordingPushSender{}
	_, _ = idSvc.RegisterPushToken(ctx, userPre.ID, "ios", "tok-ana", "personal")
	worker := app.NewEventReminderWorker(evtSvc, idSvc, opsSvc, nil, pushSpy, clk)

	sentFirst := worker.RunOnce(ctx)
	if sentFirst < 2 {
		t.Fatalf("expected at least 2 reminders sent on first pass, got %d", sentFirst)
	}
	sentSecond := worker.RunOnce(ctx)
	if sentSecond != 0 {
		t.Fatalf("expected 0 reminders on idempotent second pass, got %d", sentSecond)
	}
	for _, msg := range pushSpy.messages {
		if strings.Contains(msg.Body, "Evento Silenciado") {
			t.Fatalf("did not expect reminder for opted-out user, got %+v", msg)
		}
	}
}

func TestAuditRemediation_EndToEndSecurityAndFeatures(t *testing.T) {
	t.Parallel()
	handler := newAuditHTTPTestHandler(t)
	clientToken := registerClientForTest(t, handler, "raul_audit", "raul.audit@example.com")

	verifyPaymentCardAndCashRestriction(t, handler, clientToken)
	verifyEventSpoofingAndTasksAndGuests(t, handler, clientToken)
	verifyContractPDFAndDeclineGuardAndPackages(t, handler, clientToken)
	verifyStaffDeactivationAndSwitchViewAndFavorites(t, handler, clientToken)
	verifyWhatsAppHMACAndBotAutoReplyAndReports(t, handler)
}

func newAuditHTTPTestHandler(t *testing.T) http.Handler {
	t.Helper()
	deps := app.Dependencies{
		Context:       t.Context(),
		Clock:         &clock.Fixed{At: time.Date(2026, 11, 24, 12, 0, 0, 0, time.UTC)},
		AdminOnlySeed: true,
		Config: config.Config{
			Env:               config.EnvDevelopment,
			JWTSecret:         "test-jwt-secret-must-be-at-least-32-chars",
			StripeSecretKey:   "",
			WhatsAppAppSecret: "meta-webhook-secret",
		},
	}
	handler, _ := platformhttp.New(slog.New(slog.DiscardHandler), app.Modules(deps)...)
	return handler
}

func verifyPaymentCardAndCashRestriction(t *testing.T, handler http.Handler, clientToken string) {
	t.Helper()
	cardRes := doJSONRequest(t, handler, http.MethodPost, "/v1/payments", clientToken, `{
		"eventId":"evt-1","eventTitle":"Boda","clientName":"Raúl","amountRD":5000,"method":"tarjeta","kind":"anticipo","referenceCode":"CARD-1"
	}`)
	if cardRes.Code != http.StatusForbidden {
		t.Fatalf("expected 403 when client posts tarjeta payment manually, got %d: %s", cardRes.Code, cardRes.Body.String())
	}
	cashRes := doJSONRequest(t, handler, http.MethodPost, "/v1/payments", clientToken, `{
		"eventId":"evt-1","eventTitle":"Boda","clientName":"Raúl","amountRD":5000,"method":"efectivo","kind":"anticipo","referenceCode":"CASH-1"
	}`)
	if cashRes.Code != http.StatusForbidden {
		t.Fatalf("expected 403 when client registers efectivo payment, got %d: %s", cashRes.Code, cashRes.Body.String())
	}
}

func verifyEventSpoofingAndTasksAndGuests(t *testing.T, handler http.Handler, clientToken string) {
	t.Helper()
	spoofRes := doJSONRequest(t, handler, http.MethodPost, "/v1/events", clientToken, `{
		"title":"Evento Audit","eventType":"Boda","dateLabel":"Vie 27 nov","eventDateIso":"2026-11-27",
		"venue":"Naco","guests":40,"status":"confirmed","paidDepositRD":99999,"daysUntilEvent":1
	}`)
	if spoofRes.Code != http.StatusUnprocessableEntity {
		t.Fatalf("expected 422 rejecting client spoofed event fields, got %d: %s", spoofRes.Code, spoofRes.Body.String())
	}
	createRes := doJSONRequest(t, handler, http.MethodPost, "/v1/events", clientToken, `{
		"title":"Evento Audit","eventType":"Boda","dateLabel":"Vie 27 nov","eventDateIso":"2026-11-27",
		"venue":"Naco","guests":40
	}`)
	if createRes.Code != http.StatusOK {
		t.Fatalf("create event failed %d: %s", createRes.Code, createRes.Body.String())
	}
	var evOut struct {
		Event eventsdomain.Event `json:"event"`
	}
	_ = json.Unmarshal(createRes.Body.Bytes(), &evOut)
	if evOut.Event.Status != eventsdomain.StatusPending {
		t.Fatalf("client spoofed event status -> %s", evOut.Event.Status)
	}
	if evOut.Event.PaidDepositRD != 0 {
		t.Fatalf("client spoofed paidDepositRD -> %d", evOut.Event.PaidDepositRD)
	}

	taskRes := doJSONRequest(t, handler, http.MethodPost, "/v1/events/"+evOut.Event.ID+"/tasks", clientToken, `{
		"title":"Confirmar DJ","assigneeName":"Raúl"
	}`)
	if taskRes.Code != http.StatusOK {
		t.Fatalf("create task failed %d: %s", taskRes.Code, taskRes.Body.String())
	}
	var tOut struct {
		Task struct {
			ID string `json:"id"`
		} `json:"task"`
	}
	_ = json.Unmarshal(taskRes.Body.Bytes(), &tOut)

	patchTask := doJSONRequest(t, handler, http.MethodPatch, "/v1/events/"+evOut.Event.ID+"/tasks/"+tOut.Task.ID, clientToken, `{"done":true}`)
	if patchTask.Code != http.StatusOK {
		t.Fatalf("patch task failed %d", patchTask.Code)
	}
	delTask := doJSONRequest(t, handler, http.MethodDelete, "/v1/events/"+evOut.Event.ID+"/tasks/"+tOut.Task.ID, clientToken, "")
	if delTask.Code != http.StatusOK {
		t.Fatalf("delete task failed %d", delTask.Code)
	}

	guestRes := doJSONRequest(t, handler, http.MethodPost, "/v1/events/"+evOut.Event.ID+"/guests", clientToken, `{
		"fullName":"Invitado Uno","rsvpStatus":"pending","tableLabel":"Mesa 1"
	}`)
	if guestRes.Code != http.StatusOK {
		t.Fatalf("add guest failed %d: %s", guestRes.Code, guestRes.Body.String())
	}
	var gOut struct {
		Guest struct {
			ID string `json:"id"`
		} `json:"guest"`
	}
	_ = json.Unmarshal(guestRes.Body.Bytes(), &gOut)
	delGuest := doJSONRequest(t, handler, http.MethodDelete, "/v1/events/"+evOut.Event.ID+"/guests/"+gOut.Guest.ID, clientToken, "")
	if delGuest.Code != http.StatusOK {
		t.Fatalf("delete guest failed %d: %s", delGuest.Code, delGuest.Body.String())
	}

	cancel1 := doJSONRequest(t, handler, http.MethodPost, "/v1/events/"+evOut.Event.ID+"/cancel", clientToken, `{"reason":"Cambio de planes"}`)
	if cancel1.Code != http.StatusOK {
		t.Fatalf("first cancel failed %d: %s", cancel1.Code, cancel1.Body.String())
	}
	cancel2 := doJSONRequest(t, handler, http.MethodPost, "/v1/events/"+evOut.Event.ID+"/cancel", clientToken, `{"reason":"Duplicado"}`)
	if cancel2.Code != http.StatusConflict {
		t.Fatalf("expected 409 on duplicate cancel, got %d: %s", cancel2.Code, cancel2.Body.String())
	}
}

func verifyStaffDeactivationAndSwitchViewAndFavorites(t *testing.T, handler http.Handler, clientToken string) {
	t.Helper()
	favPut := doJSONRequest(t, handler, http.MethodPut, "/v1/auth/me/favorites/art-arco", clientToken, "")
	if favPut.Code != http.StatusOK {
		t.Fatalf("put favorite failed %d: %s", favPut.Code, favPut.Body.String())
	}
	favList := doJSONRequest(t, handler, http.MethodGet, "/v1/auth/me/favorites", clientToken, "")
	if !strings.Contains(favList.Body.String(), "art-arco") {
		t.Fatalf("expected art-arco in favorites, got %s", favList.Body.String())
	}

	yuliMeBefore := doJSONRequest(t, handler, http.MethodGet, "/v1/auth/me", "rf-token-yuli", "")
	if yuliMeBefore.Code != http.StatusOK {
		t.Fatalf("yuli auth/me failed before deactivation: %d", yuliMeBefore.Code)
	}
	deactRes := doJSONRequest(t, handler, http.MethodPatch, "/v1/admin/staff/staff-yuli", "rf-token-maria", `{"active":false}`)
	if deactRes.Code != http.StatusOK {
		t.Fatalf("deactivate yuli failed %d: %s", deactRes.Code, deactRes.Body.String())
	}
	yuliMeAfter := doJSONRequest(t, handler, http.MethodGet, "/v1/auth/me", "rf-token-yuli", "")
	if yuliMeAfter.Code != http.StatusUnauthorized {
		t.Fatalf("expected deactivated staff token to return 401, got %d", yuliMeAfter.Code)
	}
	delStaff := doJSONRequest(t, handler, http.MethodDelete, "/v1/admin/staff/staff-yuli", "rf-token-maria", "")
	if delStaff.Code != http.StatusOK {
		t.Fatalf("delete staff failed %d: %s", delStaff.Code, delStaff.Body.String())
	}
	switchRes := doJSONRequest(t, handler, http.MethodPost, "/v1/auth/switch-view", "rf-token-maria", `{"view":"personal"}`)
	if switchRes.Code != http.StatusOK {
		t.Fatalf("switch-view failed %d: %s", switchRes.Code, switchRes.Body.String())
	}
	if !strings.Contains(switchRes.Body.String(), `"personal"`) {
		t.Fatalf("expected personal view in response: %s", switchRes.Body.String())
	}
	logoutRes := doJSONRequest(t, handler, http.MethodPost, "/v1/auth/logout", clientToken, "")
	if logoutRes.Code != http.StatusOK {
		t.Fatalf("logout failed %d", logoutRes.Code)
	}
	afterLogout := doJSONRequest(t, handler, http.MethodGet, "/v1/auth/me", clientToken, "")
	if afterLogout.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 after logout, got %d", afterLogout.Code)
	}
}

func verifyContractPDFAndDeclineGuardAndPackages(t *testing.T, handler http.Handler, clientToken string) {
	t.Helper()
	createCatalogArticleForTest(t, handler, "art-pdf-test", 5)
	pkgRes := doJSONRequest(t, handler, http.MethodPost, "/v1/catalog/packages", "rf-token-maria", `{
		"id":"pkg-del-test","name":"Paquete Prueba","eventType":"Cumpleaños","totalPriceDop":5000,"guestCapacity":25,"visible":false
	}`)
	if pkgRes.Code != http.StatusOK {
		t.Fatalf("create package failed %d: %s", pkgRes.Code, pkgRes.Body.String())
	}
	pubPkgs := doJSONRequest(t, handler, http.MethodGet, "/v1/catalog/packages", "", "")
	if strings.Contains(pubPkgs.Body.String(), "pkg-del-test") {
		t.Fatalf("did not expect hidden package in public list: %s", pubPkgs.Body.String())
	}
	pubHiddenPkgs := doJSONRequest(t, handler, http.MethodGet, "/v1/catalog/packages?includeHidden=true", "", "")
	if strings.Contains(pubHiddenPkgs.Body.String(), "pkg-del-test") {
		t.Fatalf("did not expect hidden package for unauthenticated includeHidden=true: %s", pubHiddenPkgs.Body.String())
	}
	allPkgs := doJSONRequest(t, handler, http.MethodGet, "/v1/catalog/packages?includeHidden=true", "rf-token-maria", "")
	if !strings.Contains(allPkgs.Body.String(), "pkg-del-test") {
		t.Fatalf("expected hidden package when admin calls includeHidden=true: %s", allPkgs.Body.String())
	}
	delPkg := doJSONRequest(t, handler, http.MethodDelete, "/v1/catalog/packages/pkg-del-test", "rf-token-maria", "")
	if delPkg.Code != http.StatusOK {
		t.Fatalf("delete package failed %d", delPkg.Code)
	}

	qRes := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes", clientToken, `{
		"eventTitle":"Boda PDF","clientName":"Raúl","eventDateLabel":"Sáb 28 nov","eventDateIso":"2026-11-28",
		"venue":"Naco","guests":30,"deliveryFeeRD":500,"discountRD":0,"lines":[{"articleCode":"art-pdf-test","name":"Arco","quantity":1,"unitPriceRD":3000}]
	}`)
	if qRes.Code != http.StatusOK {
		t.Fatalf("create quote failed %d: %s", qRes.Code, qRes.Body.String())
	}
	var qOut struct {
		Quote struct {
			ID string `json:"id"`
		} `json:"quote"`
	}
	_ = json.Unmarshal(qRes.Body.Bytes(), &qOut)

	sendRes := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes/"+qOut.Quote.ID+"/send", "rf-token-maria", `{}`)
	if sendRes.Code != http.StatusOK {
		t.Fatalf("send quote failed %d: %s", sendRes.Code, sendRes.Body.String())
	}
	signRes := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes/"+qOut.Quote.ID+"/sign", clientToken, `{"signedByName":"Raúl","signatureSvg":"<svg/>"}`)
	if signRes.Code != http.StatusOK {
		t.Fatalf("sign quote failed %d", signRes.Code)
	}
	declineRes := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes/"+qOut.Quote.ID+"/decline", clientToken, `{"reason":"Ya firmada"}`)
	if declineRes.Code != http.StatusConflict {
		t.Fatalf("expected 409 declining signed quote, got %d: %s", declineRes.Code, declineRes.Body.String())
	}
	pdfRes := doJSONRequest(t, handler, http.MethodGet, "/v1/quotes/"+qOut.Quote.ID+"/contract", clientToken, "")
	if pdfRes.Code != http.StatusOK {
		t.Fatalf("contract pdf failed %d: %s", pdfRes.Code, pdfRes.Body.String())
	}
	if !strings.HasPrefix(pdfRes.Body.String(), "%PDF-1.4") {
		t.Fatalf("expected valid PDF header, got %q", pdfRes.Body.String()[:min(len(pdfRes.Body.String()), 20)])
	}
}

func verifyWhatsAppHMACAndBotAutoReplyAndReports(t *testing.T, handler http.Handler) {
	t.Helper()
	payload := []byte(`{"from":"8095559999","name":"Cliente Nuevo","message":"Quiero cotizar mi boda"}`)
	badReq := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/webhooks/whatsapp", bytes.NewReader(payload))
	badReq.Header.Set("Content-Type", "application/json")
	badReq.Header.Set("X-Hub-Signature-256", "sha256=deadbeef")
	badRec := httptest.NewRecorder()
	handler.ServeHTTP(badRec, badReq)
	if badRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 for bad WhatsApp HMAC signature, got %d", badRec.Code)
	}

	mac := hmac.New(sha256.New, []byte("meta-webhook-secret"))
	_, _ = mac.Write(payload)
	sig := "sha256=" + hex.EncodeToString(mac.Sum(nil))
	goodReq := httptest.NewRequestWithContext(t.Context(), http.MethodPost, "/v1/webhooks/whatsapp", bytes.NewReader(payload))
	goodReq.Header.Set("Content-Type", "application/json")
	goodReq.Header.Set("X-Hub-Signature-256", sig)
	goodRec := httptest.NewRecorder()
	handler.ServeHTTP(goodRec, goodReq)
	if goodRec.Code != http.StatusOK {
		t.Fatalf("expected 200 for valid WhatsApp HMAC signature, got %d: %s", goodRec.Code, goodRec.Body.String())
	}

	repRes := doJSONRequest(t, handler, http.MethodGet, "/v1/reports/export?type=income&period=6m", "rf-token-maria", "")
	if repRes.Code != http.StatusOK {
		t.Fatalf("report export failed %d: %s", repRes.Code, repRes.Body.String())
	}
	if strings.Contains(repRes.Body.String(), "Nov 2025") {
		t.Fatalf("report still contains old hardcoded 2025 months: %s", repRes.Body.String())
	}
	_ = uuid.Nil
}
