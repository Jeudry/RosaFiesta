package app_test

import (
	"bufio"
	"context"
	"encoding/base64"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/app"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func newLifecycleTestHandler(t *testing.T, adminOnly, requireSMTP bool) http.Handler {
	t.Helper()
	deps := app.Dependencies{
		AdminOnlySeed: adminOnly,
		Config: config.Config{
			MediaStorageDir:       t.TempDir(),
			MediaMaxBytes:         2 << 20,
			RequireConfiguredSMTP: requireSMTP,
		},
	}
	handler, _ := httpapi.New(slog.New(slog.DiscardHandler), app.Modules(deps)...)
	return handler
}

func doJSONRequest(
	t *testing.T,
	handler http.Handler,
	method string,
	path string,
	token string,
	payload string,
) *httptest.ResponseRecorder {
	t.Helper()
	var bodyReader *strings.Reader
	if payload != "" {
		bodyReader = strings.NewReader(payload)
	} else {
		bodyReader = strings.NewReader("")
	}
	req := httptest.NewRequestWithContext(t.Context(), method, path, bodyReader)
	if payload != "" {
		req.Header.Set("Content-Type", "application/json")
	}
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	return rec
}

func registerClientForTest(t *testing.T, handler http.Handler, username, email string) string {
	t.Helper()
	body := `{"username":"` + username + `","fullName":"Laura Cliente","email":"` + email + `","password":"Password123!","birthDate":"15/08/1995","phone":"8095550199"}`
	rec := doJSONRequest(t, handler, http.MethodPost, "/v1/auth/register", "", body)
	if rec.Code != http.StatusCreated {
		if rec.Code != http.StatusOK {
			t.Fatalf("register client failed with %d: %s", rec.Code, rec.Body.String())
		}
	}
	var out struct {
		AccessToken string `json:"accessToken"`
	}
	if err := json.Unmarshal(rec.Body.Bytes(), &out); err != nil {
		t.Fatalf("decode register output: %v", err)
	}
	if out.AccessToken == "" {
		t.Fatalf("missing access token in register output: %s", rec.Body.String())
	}
	return out.AccessToken
}

func createCatalogArticleForTest(t *testing.T, handler http.Handler, articleID string, stock int) {
	t.Helper()
	payload := `{"id":"` + articleID + `","name":"Silla Ghost Transparente","categoryId":"cat-sillas","dailyPriceDop":250,"totalStock":` + jsonInt(stock) + `}`
	rec := doJSONRequest(t, handler, http.MethodPost, "/v1/catalog/articles", "rf-token-maria", payload)
	if rec.Code != http.StatusOK {
		if rec.Code != http.StatusCreated {
			t.Fatalf("create article failed with %d: %s", rec.Code, rec.Body.String())
		}
	}
}

func jsonInt(value int) string {
	raw, _ := json.Marshal(value)
	return string(raw)
}

func TestFunctionalLifecycle_TurnaroundOverbookingAndRelease(t *testing.T) {
	t.Parallel()
	handler := newLifecycleTestHandler(t, true, false)
	clientToken := registerClientForTest(t, handler, "laura_overbook", "laura.over@example.com")
	createCatalogArticleForTest(t, handler, "art-ghost-test", 5)

	createQuote1 := `{"eventTitle":"Boda Turno 1","clientName":"Laura Cliente","eventDateLabel":"Vie 20 nov","eventDateIso":"2026-11-20","venue":"Salon Real","guests":50,"lines":[{"articleCode":"art-ghost-test","name":"Silla Ghost","quantity":4,"unitPriceRD":250}],"deliveryFeeRD":500,"discountRD":0}`
	q1Rec := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes", clientToken, createQuote1)
	if q1Rec.Code != http.StatusOK {
		t.Fatalf("create quote 1 failed %d: %s", q1Rec.Code, q1Rec.Body.String())
	}
	var q1Out struct {
		Quote struct {
			ID string `json:"id"`
		} `json:"quote"`
	}
	_ = json.Unmarshal(q1Rec.Body.Bytes(), &q1Out)

	sign1Rec := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes/"+q1Out.Quote.ID+"/sign", clientToken, `{"signedByName":"Laura Cliente","signatureSvg":"<svg><path d='M0,0'/></svg>"}`)
	if sign1Rec.Code != http.StatusOK {
		t.Fatalf("sign quote 1 failed %d: %s", sign1Rec.Code, sign1Rec.Body.String())
	}
	var signed1 struct {
		Quote struct {
			EventID string `json:"eventId"`
		} `json:"quote"`
	}
	_ = json.Unmarshal(sign1Rec.Body.Bytes(), &signed1)
	if signed1.Quote.EventID == "" {
		t.Fatalf("expected signed quote to provision eventId")
	}

	createQuote2 := `{"eventTitle":"Boda Turno 2","clientName":"Laura Cliente","eventDateLabel":"Sab 21 nov","eventDateIso":"2026-11-21","venue":"Jardin Norte","guests":30,"lines":[{"articleCode":"art-ghost-test","name":"Silla Ghost","quantity":2,"unitPriceRD":250}],"deliveryFeeRD":0,"discountRD":0}`
	q2Rec := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes", clientToken, createQuote2)
	if q2Rec.Code != http.StatusConflict {
		t.Fatalf("expected 409 STOCK_INSUFFICIENT_FOR_DATE on overlapping turnaround day, got %d: %s", q2Rec.Code, q2Rec.Body.String())
	}
	if !strings.Contains(q2Rec.Body.String(), "STOCK_INSUFFICIENT_FOR_DATE") {
		t.Fatalf("expected STOCK_INSUFFICIENT_FOR_DATE error code, got %s", q2Rec.Body.String())
	}

	closeoutRec := doJSONRequest(t, handler, http.MethodPost, "/v1/events/"+signed1.Quote.EventID+"/closeout", "rf-token-maria", `{"notes":"Todo retornado sin danos"}`)
	if closeoutRec.Code != http.StatusOK {
		t.Fatalf("closeout failed %d: %s", closeoutRec.Code, closeoutRec.Body.String())
	}

	retryRec := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes", clientToken, createQuote2)
	if retryRec.Code != http.StatusOK {
		t.Fatalf("expected quote 2 to succeed after closeout stock release, got %d: %s", retryRec.Code, retryRec.Body.String())
	}
}

func uploadTestPNGVoucher(t *testing.T, handler http.Handler, token string) string {
	t.Helper()
	pngBytes := []byte{
		0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
		0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
		0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
		0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
		0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
		0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
		0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
		0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
		0x42, 0x60, 0x82,
	}
	uploadBody := `{"filename":"comprobante.png","purpose":"voucher","data_base64":"` + base64.StdEncoding.EncodeToString(pngBytes) + `"}`
	mediaRec := doJSONRequest(t, handler, http.MethodPost, "/v1/media", token, uploadBody)
	if mediaRec.Code != http.StatusCreated {
		t.Fatalf("upload media failed %d: %s", mediaRec.Code, mediaRec.Body.String())
	}
	var mediaOut struct {
		URL string `json:"url"`
	}
	_ = json.Unmarshal(mediaRec.Body.Bytes(), &mediaOut)
	if !strings.HasPrefix(mediaOut.URL, "/v1/media/") {
		t.Fatalf("unexpected media url: %+v (%s)", mediaOut, mediaRec.Body.String())
	}
	unauthMediaRec := doJSONRequest(t, handler, http.MethodGet, mediaOut.URL, "", "")
	if unauthMediaRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 for unauthenticated private voucher GET, got %d", unauthMediaRec.Code)
	}
	getMediaRec := doJSONRequest(t, handler, http.MethodGet, mediaOut.URL, token, "")
	if getMediaRec.Code != http.StatusOK {
		t.Fatalf("GET media failed %d", getMediaRec.Code)
	}
	if getMediaRec.Header().Get("Content-Type") != "image/png" {
		t.Fatalf("GET media unexpected contentType=%s", getMediaRec.Header().Get("Content-Type"))
	}
	return mediaOut.URL
}

func createAndSignLifecycleQuote(t *testing.T, handler http.Handler, clientToken string) (string, int) {
	t.Helper()
	quoteBody := `{"eventTitle":"Boda NCF Completa","clientName":"Laura Cliente","eventDateLabel":"Jue 26 nov","eventDateIso":"2026-11-26","venue":"Casa de Campo","guests":60,"lines":[{"articleCode":"art-arco-flow","name":"Arco Floral","quantity":2,"unitPriceRD":2000}],"deliveryFeeRD":500,"discountRD":0}`
	qRec := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes", clientToken, quoteBody)
	if qRec.Code != http.StatusOK {
		t.Fatalf("create quote failed %d: %s", qRec.Code, qRec.Body.String())
	}
	var qOut struct {
		Quote struct {
			ID string `json:"id"`
		} `json:"quote"`
	}
	_ = json.Unmarshal(qRec.Body.Bytes(), &qOut)

	signRec := doJSONRequest(t, handler, http.MethodPost, "/v1/quotes/"+qOut.Quote.ID+"/sign", clientToken, `{"signedByName":"Laura Cliente","signatureSvg":"<svg><path d='M0,0 L10,10'/></svg>"}`)
	if signRec.Code != http.StatusOK {
		t.Fatalf("sign quote failed %d: %s", signRec.Code, signRec.Body.String())
	}
	var signedOut struct {
		Quote struct {
			EventID   string `json:"eventId"`
			DepositRD int    `json:"depositRD"`
		} `json:"quote"`
	}
	_ = json.Unmarshal(signRec.Body.Bytes(), &signedOut)
	return signedOut.Quote.EventID, signedOut.Quote.DepositRD
}

func TestFunctionalLifecycle_MediaVoucherPaymentVerifyNCFLogisticsAndDGII(t *testing.T) {
	t.Parallel()
	handler := newLifecycleTestHandler(t, true, false)
	clientToken := registerClientForTest(t, handler, "camila_flow", "camila.flow@example.com")
	createCatalogArticleForTest(t, handler, "art-arco-flow", 6)

	voucherURL := uploadTestPNGVoucher(t, handler, clientToken)
	eventID, depositRD := createAndSignLifecycleQuote(t, handler, clientToken)

	payBody := `{"eventId":"` + eventID + `","eventTitle":"Boda NCF Completa","clientName":"Laura Cliente","amountRD":` + jsonInt(depositRD) + `,"method":"transferencia","kind":"anticipo","bankName":"Banreservas","referenceCode":"TRF-998811","voucherUrl":"` + voucherURL + `","ncfType":"B01","clientRnc":"131849202"}`
	payRec := doJSONRequest(t, handler, http.MethodPost, "/v1/payments", clientToken, payBody)
	if payRec.Code != http.StatusOK {
		t.Fatalf("register transfer payment failed %d: %s", payRec.Code, payRec.Body.String())
	}
	var payOut struct {
		Payment struct {
			ID     string `json:"id"`
			Status string `json:"status"`
		} `json:"payment"`
	}
	_ = json.Unmarshal(payRec.Body.Bytes(), &payOut)
	if payOut.Payment.Status != "pending_verification" {
		t.Fatalf("expected transfer payment pending_verification, got %q", payOut.Payment.Status)
	}

	verifyRec := doJSONRequest(t, handler, http.MethodPost, "/v1/payments/"+payOut.Payment.ID+"/verify", "rf-token-maria", `{"notes":"Validado en Banreservas"}`)
	if verifyRec.Code != http.StatusOK {
		t.Fatalf("verify payment failed %d: %s", verifyRec.Code, verifyRec.Body.String())
	}
	var verifiedOut struct {
		Payment struct {
			Status    string `json:"status"`
			NCFNumber string `json:"ncfNumber"`
			ITBISDOP  int    `json:"itbisDop"`
		} `json:"payment"`
	}
	_ = json.Unmarshal(verifyRec.Body.Bytes(), &verifiedOut)
	assertVerifiedPaymentFields(t, verifiedOut.Payment.Status, verifiedOut.Payment.NCFNumber, verifiedOut.Payment.ITBISDOP)

	routesRec := doJSONRequest(t, handler, http.MethodGet, "/v1/operations/routes/today", "rf-token-maria", "")
	if routesRec.Code != http.StatusOK {
		t.Fatalf("GET routes failed status=%d body=%s", routesRec.Code, routesRec.Body.String())
	}
	if !strings.Contains(routesRec.Body.String(), eventID) {
		t.Fatalf("expected confirmed event %s route stops, body=%s", eventID, routesRec.Body.String())
	}
	dgiiRec := doJSONRequest(t, handler, http.MethodGet, "/v1/payments/dgii-607", "rf-token-maria", "")
	if dgiiRec.Code != http.StatusOK {
		t.Fatalf("GET dgii-607 failed status=%d body=%s", dgiiRec.Code, dgiiRec.Body.String())
	}
	if !strings.Contains(dgiiRec.Body.String(), "B0100000001") {
		t.Fatalf("expected B0100000001 in DGII 607 CSV, body=%s", dgiiRec.Body.String())
	}
}

func assertVerifiedPaymentFields(t *testing.T, status, ncfNumber string, itbis int) {
	t.Helper()
	if status != "confirmed" {
		t.Fatalf("expected confirmed status, got %q", status)
	}
	if ncfNumber != "B0100000001" {
		t.Fatalf("expected NCF B0100000001, got %q", ncfNumber)
	}
	if itbis <= 0 {
		t.Fatalf("expected positive ITBIS, got %d", itbis)
	}
}

func TestFunctionalLifecycle_SSEStreamAndExplicit503Errors(t *testing.T) {
	t.Parallel()
	handler := newLifecycleTestHandler(t, true, true)
	server := httptest.NewServer(handler)
	defer server.Close()

	ctx, cancel := context.WithTimeout(t.Context(), 5*time.Second)
	defer cancel()
	sseReq, err := http.NewRequestWithContext(ctx, http.MethodGet, server.URL+"/v1/stream", nil)
	if err != nil {
		t.Fatalf("build SSE request: %v", err)
	}
	sseReq.Header.Set("Authorization", "Bearer rf-token-maria")
	sseResp, err := server.Client().Do(sseReq)
	if err != nil {
		t.Fatalf("open SSE stream: %v", err)
	}
	defer func() { _ = sseResp.Body.Close() }()
	if sseResp.StatusCode != http.StatusOK {
		t.Fatalf("expected 200 on SSE stream, got %d", sseResp.StatusCode)
	}

	lineCh := make(chan string, 16)
	go func() {
		scanner := bufio.NewScanner(sseResp.Body)
		for scanner.Scan() {
			lineCh <- scanner.Text()
		}
	}()

	connected := waitForSSELinePrefix(ctx, lineCh, ": connected")
	if !connected {
		t.Fatalf("expected SSE stream handshake comment")
	}

	createCatalogArticleForTest(t, handler, "art-sse-check", 3)
	receivedEventFrame := waitForSSELinePrefix(ctx, lineCh, "data:")
	if !receivedEventFrame {
		t.Fatalf("expected SSE stream to emit data frame after catalog mutation")
	}

	assert503Code := func(method, path, payload, expectedCode string) {
		t.Helper()
		rec := doJSONRequest(t, handler, method, path, "rf-token-maria", payload)
		if rec.Code != http.StatusServiceUnavailable {
			t.Fatalf("expected 503 on %s %s, got %d: %s", method, path, rec.Code, rec.Body.String())
		}
		if !strings.Contains(rec.Body.String(), expectedCode) {
			t.Fatalf("expected error code %s on %s %s, got %s", expectedCode, method, path, rec.Body.String())
		}
	}

	assert503Code(http.MethodPost, "/v1/payments/card-session", `{"eventId":"evt-1","amountRD":1500}`, "CARD_GATEWAY_NOT_CONFIGURED")
	assert503Code(http.MethodPost, "/v1/rosa/chat", `{"message":"Dame ideas","eventType":"Boda","guests":80,"budgetRD":50000}`, "AI_PROVIDER_NOT_CONFIGURED")
	assert503Code(http.MethodPost, "/v1/bot/whatsapp/send", `{"recipientPhone":"+18095550101","message":"Hola"}`, "WHATSAPP_PROVIDER_NOT_CONFIGURED")
	assert503Code(http.MethodPost, "/v1/auth/otp/request", `{"email":"maria@rosafiesta.do"}`, "SMTP_PROVIDER_NOT_CONFIGURED")
}

func waitForSSELinePrefix(ctx context.Context, lineCh <-chan string, prefix string) bool {
	for {
		select {
		case <-ctx.Done():
			return false
		case line := <-lineCh:
			if strings.HasPrefix(line, prefix) {
				return true
			}
		}
	}
}
