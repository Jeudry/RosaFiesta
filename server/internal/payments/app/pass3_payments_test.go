package app_test

import (
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

func TestDGII607CSVSanitizationASTBoundaryAndExpiredNCF(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	// 2026-11-01T02:30:00Z is 2026-10-31 22:30 AST (October in Santo Domingo, NOT November).
	astOctBoundary := fixedClock{now: time.Date(2026, time.November, 1, 2, 30, 0, 0, time.UTC)}
	svc := app.NewService().WithClock(astOctBoundary)
	svc.ResetEmpty(ctx)
	admin := platformhttp.Principal{FullName: "María", Role: "admin"}

	_, err := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:       "=CMD('calc')",
		EventTitle:    "Evento",
		ClientName:    "@evil",
		AmountRD:      1180,
		Method:        "efectivo",
		NCFType:       "B02",
		ReferenceCode: "CSV-TEST-1",
	}, admin)
	if err != nil {
		t.Fatalf("register cash failed: %v", err)
	}

	octCSV, err := svc.ExportDGII607CSV(ctx, "2026-10")
	if err != nil {
		t.Fatalf("ExportDGII607CSV failed: %v", err)
	}
	if !strings.Contains(octCSV, "'=CMD('calc')") {
		t.Fatalf("expected formula-prefixed cell neutralized in AST October CSV: %q", octCSV)
	}
	if !strings.Contains(octCSV, "'@evil") {
		t.Fatalf("expected @-prefixed cell neutralized in CSV: %q", octCSV)
	}

	novCSV, _ := svc.ExportDGII607CSV(ctx, "2026-11")
	if strings.Contains(novCSV, "CSV-TEST-1") {
		t.Fatalf("expected 2026-11-01T02:30Z ref excluded from 2026-11 AST export: %q", novCSV)
	}
	if strings.Contains(novCSV, "@evil") {
		t.Fatalf("expected 2026-11-01T02:30Z client excluded from 2026-11 AST export: %q", novCSV)
	}

	svc.SetNCFValidUntil("B02", "2020-01-01")
	_, expErr := svc.RegisterPaymentForCaller(ctx, app.RegisterPaymentCommand{
		EventID:       "evt-exp",
		EventTitle:    "Evento",
		ClientName:    "Cliente",
		AmountRD:      500,
		Method:        "efectivo",
		NCFType:       "B02",
		ReferenceCode: "EXP-NCF-1",
	}, admin)
	if !errors.Is(expErr, domain.ErrNCFSequenceExpired) {
		t.Fatalf("expected ErrNCFSequenceExpired, got %v", expErr)
	}
}

func newPass3StripeMockServer(t *testing.T, refundCalls *atomic.Int32) *httptest.Server {
	t.Helper()
	return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		switch r.URL.Path {
		case "/v1/checkout/sessions/cs_usd_bad":
			_, _ = w.Write([]byte(`{"id":"cs_usd_bad","payment_status":"paid","currency":"usd","amount_total":10000,"metadata":{"event_id":"evt-1"}}`))
		case "/v1/checkout/sessions/cs_dop_round":
			_, _ = w.Write([]byte(`{"id":"cs_dop_round","payment_intent":"pi_round_1","payment_status":"paid","currency":"dop","amount_total":10051,"metadata":{"event_id":"evt-1"}}`))
		case "/v1/refunds":
			refundCalls.Add(1)
			time.Sleep(15 * time.Millisecond)
			_, _ = w.Write([]byte(`{"id":"re_conc_1","status":"succeeded","payment_intent":"pi_round_1"}`))
		default:
			http.NotFound(w, r)
		}
	}))
}

func TestStripeCurrencyAndCentavoRounding(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 24, 15, 0, 0, 0, time.UTC)}
	var refundCalls atomic.Int32
	stripeMock := newPass3StripeMockServer(t, &refundCalls)
	defer stripeMock.Close()

	svc := app.NewService().WithClock(fixed).WithStripeSecretKey("sk_test_pass3").WithStripeAPIBaseURL(stripeMock.URL)
	svc.ResetEmpty(ctx)
	admin := platformhttp.Principal{ID: uuid.New(), Role: "admin", FullName: "María"}

	if _, err := svc.ConfirmCardSession(ctx, "cs_usd_bad", admin); !errors.Is(err, domain.ErrInvalidPaymentCurrency) {
		t.Fatalf("expected ErrInvalidPaymentCurrency for usd session, got %v", err)
	}
	pay, roundErr := svc.ConfirmCardSession(ctx, "cs_dop_round", admin)
	if roundErr != nil {
		t.Fatalf("ConfirmCardSession DOP rounding failed: %v", roundErr)
	}
	if pay.AmountRD != 101 {
		t.Fatalf("expected 10051 centavos rounded to 101 DOP, got %d", pay.AmountRD)
	}
}

func TestConcurrentApproveRefundGuarded(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fixed := fixedClock{now: time.Date(2026, time.November, 24, 15, 0, 0, 0, time.UTC)}
	var refundCalls atomic.Int32
	stripeMock := newPass3StripeMockServer(t, &refundCalls)
	defer stripeMock.Close()

	svc := app.NewService().WithClock(fixed).WithStripeSecretKey("sk_test_pass3").WithStripeAPIBaseURL(stripeMock.URL)
	svc.ResetEmpty(ctx)
	admin := platformhttp.Principal{ID: uuid.New(), Role: "admin", FullName: "María"}
	if _, err := svc.ConfirmCardSession(ctx, "cs_dop_round", admin); err != nil {
		t.Fatalf("ConfirmCardSession failed: %v", err)
	}
	if err := svc.RecordCancellationRefund(ctx, "evt-1", "Evento", "Cliente", "Cancelación", 101, 101, "tier"); err != nil {
		t.Fatalf("RecordCancellationRefund failed: %v", err)
	}
	refundID := svc.ListRefunds(ctx)[0].ID

	var wg sync.WaitGroup
	errs := make(chan error, 2)
	for i := 0; i < 2; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			_, err := svc.ApproveRefundWithOptions(ctx, refundID, app.ApproveRefundOptions{ApproverName: "María"})
			errs <- err
		}()
	}
	wg.Wait()
	close(errs)

	successes := 0
	alreadyApproved := 0
	for err := range errs {
		if err == nil {
			successes++
		} else if errors.Is(err, domain.ErrRefundAlreadyApproved) {
			alreadyApproved++
		}
	}
	if successes != 1 {
		t.Fatalf("expected 1 success, got ok=%d dup=%d", successes, alreadyApproved)
	}
	if alreadyApproved != 1 {
		t.Fatalf("expected 1 ErrRefundAlreadyApproved, got ok=%d dup=%d", successes, alreadyApproved)
	}
	if refundCalls.Load() != 1 {
		t.Fatalf("expected exactly 1 Stripe refund call, got %d", refundCalls.Load())
	}
}
