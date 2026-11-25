package app_test

import (
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

type fixedClock struct {
	now time.Time
}

func (f fixedClock) Now() time.Time {
	return f.now
}

func TestService_DynamicSummaryClockAndBankNormalization(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 10, 14, 30, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	initial := svc.GetSummary(ctx)
	if initial.OctRevenueRD != 12450 {
		t.Fatalf("expected canonical OctRevenueRD=12450, got %d", initial.OctRevenueRD)
	}
	if initial.AccountsReceivable != 7724 {
		t.Fatalf("expected canonical AccountsReceivable=7724, got %d", initial.AccountsReceivable)
	}

	admin := platformhttp.Principal{
		ID:       uuid.MustParse("22222222-2222-2222-2222-222222222222"),
		Email:    "maria@rosafiesta.do",
		FullName: "María Rosario",
		Role:     "admin",
	}
	payment, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:       "evt-boda-real",
		EventTitle:    "Boda Real Rosa & Dorado",
		ClientName:    "Camila Rodríguez",
		AmountRD:      1500,
		Method:        "Banco Popular",
		Kind:          "saldo",
		ReferenceCode: "POP-TEST-001",
	}, admin)
	if err != nil {
		t.Fatalf("RegisterPaymentForCaller failed: %v", err)
	}
	if payment.Method != domain.MethodTransfer {
		t.Fatalf("expected normalized method %q, got %q", domain.MethodTransfer, payment.Method)
	}
	if payment.BankName != "Banco Popular" {
		t.Fatalf("expected bank name 'Banco Popular', got %q", payment.BankName)
	}
	if payment.PaidDateLabel != "10 nov" {
		t.Fatalf("expected PaidDateLabel '10 nov' from injected clock, got %q", payment.PaidDateLabel)
	}
	if payment.Status != domain.PaymentPendingVerification {
		t.Fatalf("expected transfer payment pending verification, got %q", payment.Status)
	}

	whilePending := svc.GetSummary(ctx)
	if whilePending.OctRevenueRD != 12450 {
		t.Fatalf("expected pending transfer not to inflate OctRevenueRD (want 12450, got %d)", whilePending.OctRevenueRD)
	}

	if _, verErr := svc.VerifyPaymentForCaller(ctx, payment.ID, "Voucher validado", admin); verErr != nil {
		t.Fatalf("VerifyPaymentForCaller failed: %v", verErr)
	}

	afterPayment := svc.GetSummary(ctx)
	if afterPayment.OctRevenueRD != 13950 {
		t.Fatalf("expected OctRevenueRD=13950 after RD$1500 payment verification, got %d", afterPayment.OctRevenueRD)
	}
	if afterPayment.AccountsReceivable != 6224 {
		t.Fatalf("expected AccountsReceivable=6224 after RD$1500 payment verification, got %d", afterPayment.AccountsReceivable)
	}
}

func TestService_RefundApprovalUpdatesSummary(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 10, 14, 30, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	approved, err := svc.ApproveRefund(ctx, "ref-baby-lucia", "María Rosario")
	if err != nil {
		t.Fatalf("ApproveRefund failed: %v", err)
	}
	if approved.Status != domain.RefundStatusApproved {
		t.Fatalf("expected refund status approved, got %q", approved.Status)
	}
	if approved.ApprovedAtLabel != "10 nov" {
		t.Fatalf("expected ApprovedAtLabel '10 nov', got %q", approved.ApprovedAtLabel)
	}

	afterRefund := svc.GetSummary(ctx)
	if afterRefund.OctRevenueRD != 11640 {
		t.Fatalf("expected OctRevenueRD=11640 after RD$810 refund approval, got %d", afterRefund.OctRevenueRD)
	}
}

func TestService_ClientScopedPaymentIsolation(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()

	camila := platformhttp.Principal{
		ID:       uuid.MustParse("11111111-1111-1111-1111-111111111111"),
		Email:    "camila@rosafiesta.do",
		FullName: "Camila Rodríguez",
		Role:     "client",
	}
	camilaPayments := svc.ListPaymentsForCaller(ctx, "", camila, true)
	if len(camilaPayments) != 2 {
		t.Fatalf("expected Camila to see only her 2 payments, got %d", len(camilaPayments))
	}

	admin := platformhttp.Principal{Role: "admin"}
	allPayments := svc.ListPaymentsForCaller(ctx, "", admin, true)
	if len(allPayments) != 4 {
		t.Fatalf("expected admin to see all 4 canonical payments, got %d", len(allPayments))
	}
}

type fakeSyncer struct {
	verified []domain.Payment
}

func (f *fakeSyncer) OnPaymentVerified(_ context.Context, payment domain.Payment) error {
	f.verified = append(f.verified, payment)
	return nil
}

type fakeEmailSender struct {
	configured bool
	sentTo     []string
	subjects   []string
}

func (f *fakeEmailSender) IsConfigured() bool {
	return f.configured
}

func (f *fakeEmailSender) SendEmail(_ context.Context, to, subject, _ string) error {
	f.sentTo = append(f.sentTo, to)
	f.subjects = append(f.subjects, subject)
	return nil
}

type fakePublisher struct {
	events []realtimedomain.RealtimeEvent
}

func (f *fakePublisher) Publish(event realtimedomain.RealtimeEvent) realtimedomain.RealtimeEvent {
	f.events = append(f.events, event)
	return event
}

func TestService_TransferVerificationNCFAndDGII607(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 12, 10, 0, 0, 0, time.UTC)}
	syncer := &fakeSyncer{}
	mailer := &fakeEmailSender{configured: true}
	pub := &fakePublisher{}
	svc := app.NewService().
		WithClock(fixed).
		WithVerifiedSynchronizer(syncer).
		WithEmailSender(mailer).
		WithRealtimePublisher(pub)
	svc.ResetEmpty(ctx)

	client := platformhttp.Principal{
		ID:       uuid.New(),
		Email:    "cliente@empresa.do",
		FullName: "Empresa Caribe",
		Role:     "client",
	}
	if _, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:   "evt-1",
		AmountRD:  1180,
		Method:    "transferencia",
		NCFType:   "B01",
		ClientRNC: "123",
	}, client); !errors.Is(err, domain.ErrInvalidFiscalRNC) {
		t.Fatalf("expected ErrInvalidFiscalRNC, got %v", err)
	}

	pending, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:         "evt-1",
		EventTitle:      "Evento Corporativo",
		AmountRD:        1180,
		Method:          "transferencia",
		Kind:            "anticipo",
		BankName:        "Banreservas",
		VoucherURL:      "https://cdn.rosafiesta.do/vouchers/01.jpg",
		ReferenceNumber: "BR-9001",
		NCFType:         "B01",
		ClientRNC:       "1-31-45678-9",
	}, client)
	if err != nil {
		t.Fatalf("register transfer failed: %v", err)
	}
	if pending.Status != domain.PaymentPendingVerification {
		t.Fatalf("unexpected pending status: %q", pending.Status)
	}
	if pending.NCFNumber != "" {
		t.Fatalf("expected empty NCF while pending, got %q", pending.NCFNumber)
	}
	if len(syncer.verified) != 0 {
		t.Fatalf("expected syncer not invoked before verify")
	}
	if svc.GetSummary(ctx).OctRevenueRD != 0 {
		t.Fatalf("expected zero-baseline revenue 0 for pending payment")
	}

	noFinanceStaff := platformhttp.Principal{Role: "staff", Permissions: []string{"decoracion"}}
	if _, err := svc.VerifyPaymentForCaller(ctx, pending.ID, "", noFinanceStaff); !errors.Is(err, domain.ErrPaymentForbidden) {
		t.Fatalf("expected ErrPaymentForbidden for staff without finanzas, got %v", err)
	}

	financeStaff := platformhttp.Principal{FullName: "Ana Finanzas", Role: "staff", Permissions: []string{"finanzas"}}
	verified, verErr := svc.VerifyPaymentForCaller(ctx, pending.ID, "Conciliado", financeStaff)
	if verErr != nil {
		t.Fatalf("VerifyPaymentForCaller failed: %v", verErr)
	}
	assertVerifiedResult(t, verified, syncer, mailer, svc.GetSummary(ctx).OctRevenueRD)
}

func assertVerifiedResult(
	t *testing.T,
	verified domain.Payment,
	syncer *fakeSyncer,
	mail *fakeEmailSender,
	rev int,
) {
	t.Helper()
	if verified.Status != domain.PaymentConfirmed {
		t.Fatalf("expected status confirmed, got %q", verified.Status)
	}
	if verified.NCFNumber != "B0100000001" {
		t.Fatalf("expected NCF B0100000001, got %q", verified.NCFNumber)
	}
	if len(syncer.verified) != 1 {
		t.Fatalf("expected syncer called once, got %d", len(syncer.verified))
	}
	if len(mail.sentTo) != 1 {
		t.Fatalf("expected receipt email sent once, got %d", len(mail.sentTo))
	}
	if rev != 1180 {
		t.Fatalf("expected revenue 1180, got %d", rev)
	}
}

func TestService_TransferRejectionExcludesSummaryAndDGII607(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 12, 10, 0, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)
	svc.ResetEmpty(ctx)
	client := platformhttp.Principal{ID: uuid.New(), Email: "cliente@empresa.do", FullName: "Empresa Caribe", Role: "client"}
	financeStaff := platformhttp.Principal{FullName: "Ana Finanzas", Role: "staff", Permissions: []string{"finanzas"}}

	first, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:       "evt-1",
		EventTitle:    "Evento Corporativo",
		AmountRD:      1180,
		Method:        "efectivo",
		NCFType:       "B02",
		ReferenceCode: "CASH-111",
	}, financeStaff)
	if err != nil {
		t.Fatalf("register cash failed: %v", err)
	}
	second, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:       "evt-1",
		EventTitle:    "Evento Corporativo",
		AmountRD:      500,
		Method:        "transferencia",
		ReferenceCode: "POP-9999",
	}, client)
	if err != nil {
		t.Fatalf("register second transfer failed: %v", err)
	}
	rejected, rejErr := svc.RejectPaymentForCaller(ctx, second.ID, "Monto ilegible", financeStaff)
	if rejErr != nil {
		t.Fatalf("RejectPaymentForCaller failed: %v", rejErr)
	}
	if rejected.Status != domain.PaymentRejected {
		t.Fatalf("expected rejected status, got %q", rejected.Status)
	}

	csvOut, csvErr := svc.ExportDGII607CSV(ctx, "2026-11")
	if csvErr != nil {
		t.Fatalf("ExportDGII607CSV failed: %v", csvErr)
	}
	if !strings.Contains(csvOut, first.NCFNumber) {
		t.Fatalf("expected verified payment NCF %q in DGII 607 CSV, got %q", first.NCFNumber, csvOut)
	}
	if strings.Contains(csvOut, "POP-9999") {
		t.Fatalf("expected rejected payment excluded from DGII 607 CSV, got %q", csvOut)
	}
}

func TestService_CreateCardSession_ConfiguredAndUnconfigured(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	svc := app.NewService()
	caller := platformhttp.Principal{Email: "camila@rosafiesta.do", Role: "client"}

	_, err := svc.CreateCardSession(ctx, app.CreateCardSessionCommand{
		EventID:    "evt-boda-real",
		EventTitle: "Boda Real",
		AmountRD:   2500,
	}, caller)
	if !errors.Is(err, domain.ErrCardGatewayNotConfigured) {
		t.Fatalf("expected ErrCardGatewayNotConfigured, got %v", err)
	}

	var gotAuth string
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotAuth = r.Header.Get("Authorization")
		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`{"id":"cs_test_123","url":"https://checkout.stripe.com/c/pay/cs_test_123"}`))
	}))
	defer server.Close()

	svc.WithStripeSecretKey("sk_test_mock_key").WithStripeAPIBaseURL(server.URL).WithPublicBaseURL("https://app.rosafiesta.do")
	session, sessErr := svc.CreateCardSession(ctx, app.CreateCardSessionCommand{
		EventID:    "evt-boda-real",
		EventTitle: "Boda Real",
		AmountRD:   2500,
		NCFType:    "B02",
	}, caller)
	if sessErr != nil {
		t.Fatalf("CreateCardSession with configured mock Stripe failed: %v", sessErr)
	}
	if gotAuth != "Bearer sk_test_mock_key" {
		t.Fatalf("expected Bearer auth header, got %q", gotAuth)
	}
	if session.SessionID != "cs_test_123" {
		t.Fatalf("expected session id cs_test_123, got %q", session.SessionID)
	}
	if session.Provider != "stripe" {
		t.Fatalf("expected provider stripe, got %q", session.Provider)
	}
}

func TestService_ConfirmCardSessionWebhookAndAnalytics(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.October, 25, 16, 0, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)

	stripeMock := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`{
			"id":"cs_paid_900",
			"payment_status":"paid",
			"currency":"dop",
			"amount_total":320000,
			"customer_email":"camila@rosafiesta.do",
			"metadata":{"event_id":"evt-boda-real","kind":"saldo","ncf_type":"B02"}
		}`))
	}))
	defer stripeMock.Close()

	svc.WithStripeSecretKey("sk_test_mock_key").WithStripeAPIBaseURL(stripeMock.URL)
	caller := platformhttp.Principal{
		ID:       uuid.MustParse("11111111-1111-1111-1111-111111111111"),
		Email:    "camila@rosafiesta.do",
		FullName: "Camila Rodríguez",
		Role:     "client",
	}

	confirmed, err := svc.ConfirmCardSession(ctx, "cs_paid_900", caller)
	if err != nil {
		t.Fatalf("ConfirmCardSession failed: %v", err)
	}
	if confirmed.AmountRD != 3200 {
		t.Fatalf("expected AmountRD=3200, got %+v", confirmed)
	}
	if confirmed.GatewaySessionID != "cs_paid_900" {
		t.Fatalf("expected GatewaySessionID cs_paid_900, got %+v", confirmed)
	}
	if confirmed.NCFNumber == "" {
		t.Fatalf("expected allocated NCFNumber, got %+v", confirmed)
	}
	confirmedAgain, errAgain := svc.ConfirmCardSession(ctx, "cs_paid_900", caller)
	if errAgain != nil {
		t.Fatalf("expected idempotent repeat confirmation without error: %v", errAgain)
	}
	if confirmedAgain.ID != confirmed.ID {
		t.Fatalf("expected same payment id on repeat confirmation, got id1=%s id2=%s", confirmed.ID, confirmedAgain.ID)
	}

	analytics := svc.ComputeAnalytics(ctx, "30d")
	if analytics.TotalRevenueRD < 3200 {
		t.Fatalf("expected analytics TotalRevenueRD >= 3200, got %+v", analytics)
	}
	if len(analytics.MonthlySeries) != 6 {
		t.Fatalf("expected 6 monthly points, got %+v", analytics)
	}
}

func newMockStripeRefundServer(t *testing.T, capturedForm *url.Values) *httptest.Server {
	t.Helper()
	return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		if r.URL.Path == "/v1/checkout/sessions/cs_card_refund_1" {
			_, _ = w.Write([]byte(`{
				"id":"cs_card_refund_1",
				"payment_intent":"pi_stripe_888",
				"payment_status":"paid",
				"currency":"dop",
				"amount_total":150000,
				"customer_email":"camila@rosafiesta.do",
				"metadata":{"event_id":"evt-stripe-refund","kind":"anticipo","ncf_type":"B02"}
			}`))
			return
		}
		if r.URL.Path == "/v1/refunds" {
			rawBody, _ := io.ReadAll(r.Body)
			*capturedForm, _ = url.ParseQuery(string(rawBody))
			_, _ = w.Write([]byte(`{"id":"re_stripe_777","status":"succeeded","payment_intent":"pi_stripe_888"}`))
			return
		}
		http.NotFound(w, r)
	}))
}

func assertPendingRefundApprovedByWebhook(t *testing.T, svc *app.Service, refundID string) {
	t.Helper()
	for _, ref := range svc.ListRefunds(t.Context()) {
		if ref.ID != refundID {
			continue
		}
		if ref.Status != domain.RefundStatusApproved {
			t.Fatalf("expected webhook to approve pending refund, got %+v", ref)
		}
		return
	}
	t.Fatalf("refund %s not found after webhook sync", refundID)
}

func TestService_ApproveRefundExecutesStripeRefundAndWebhookSync(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 15, 16, 0, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(fixed)
	svc.ResetEmpty(ctx)

	var gotRefundForm url.Values
	stripeMock := newMockStripeRefundServer(t, &gotRefundForm)
	defer stripeMock.Close()

	svc.WithStripeSecretKey("sk_test_refund_key").
		WithStripeWebhookSecret("whsec_test_refund").
		WithStripeAPIBaseURL(stripeMock.URL)

	caller := platformhttp.Principal{ID: uuid.New(), Email: "camila@rosafiesta.do", FullName: "Camila", Role: "client"}
	if _, err := svc.ConfirmCardSession(ctx, "cs_card_refund_1", caller); err != nil {
		t.Fatalf("ConfirmCardSession failed: %v", err)
	}
	if err := svc.RecordCancellationRefund(ctx, "evt-stripe-refund", "Evento Stripe", "Camila", "Cancelado", 1500, 1200, "tier"); err != nil {
		t.Fatalf("RecordCancellationRefund failed: %v", err)
	}
	refunds := svc.ListRefunds(ctx)
	approved, err := svc.ApproveRefund(ctx, refunds[0].ID, "María Dueña")
	if err != nil {
		t.Fatalf("ApproveRefund with Stripe failed: %v", err)
	}
	if approved.GatewayRefundID != "re_stripe_777" {
		t.Fatalf("expected Stripe refund id re_stripe_777, got %+v", approved)
	}
	if gotRefundForm.Get("payment_intent") != "pi_stripe_888" {
		t.Fatalf("expected payment_intent pi_stripe_888 sent to Stripe, got %q", gotRefundForm.Get("payment_intent"))
	}
	if gotRefundForm.Get("amount") != "120000" {
		t.Fatalf("expected amount 120000 centavos, got %q", gotRefundForm.Get("amount"))
	}

	if err := svc.RecordCancellationRefund(ctx, "evt-stripe-refund", "Evento Stripe", "Camila", "Segundo", 1500, 300, "tier"); err != nil {
		t.Fatalf("second RecordCancellationRefund failed: %v", err)
	}
	pendingSecond := svc.ListRefunds(ctx)[1]
	webhookPayload := []byte(`{"type":"refund.updated","data":{"object":{"id":"re_webhook_222","status":"succeeded","payment_intent":"pi_stripe_888","metadata":{"refund_id":"` + pendingSecond.ID + `"}}}}`)
	sigHeader := signTestStripeWebhook(webhookPayload, "whsec_test_refund", fixed.now)
	if _, whErr := svc.HandleStripeWebhook(ctx, webhookPayload, sigHeader); whErr != nil {
		t.Fatalf("HandleStripeWebhook refund.updated failed: %v", whErr)
	}
	assertPendingRefundApprovedByWebhook(t, svc, pendingSecond.ID)
}

func signTestStripeWebhook(rawBody []byte, secret string, now time.Time) string {
	ts := strconv.FormatInt(now.Unix(), 10)
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write([]byte(ts + "."))
	mac.Write(rawBody)
	return "t=" + ts + ",v1=" + hex.EncodeToString(mac.Sum(nil))
}
