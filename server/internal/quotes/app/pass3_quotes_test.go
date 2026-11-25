package app_test

import (
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/google/uuid"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

type mockQuoteTxStore struct {
	quotes    map[string]domain.Quote
	failOnTx  bool
	txStarted bool
}

func (m *mockQuoteTxStore) Enabled() bool { return true }

func (m *mockQuoteTxStore) ListQuotes(_ context.Context) ([]domain.Quote, error) {
	out := make([]domain.Quote, 0, len(m.quotes))
	for _, q := range m.quotes {
		out = append(out, q)
	}
	return out, nil
}

func (m *mockQuoteTxStore) UpsertQuote(_ context.Context, q domain.Quote, _ time.Time) error {
	m.quotes[q.ID] = q
	return nil
}

func (m *mockQuoteTxStore) SignQuoteTx(
	_ context.Context,
	id string,
	_ time.Time,
	applySign func(existing domain.Quote) (domain.Quote, error),
) (domain.Quote, error) {
	m.txStarted = true
	if m.failOnTx {
		return domain.Quote{}, errors.New("simulated db commit failure")
	}
	existing, ok := m.quotes[id]
	if !ok {
		return domain.Quote{}, domain.ErrQuoteNotFound
	}
	updated, err := applySign(existing)
	if err != nil {
		return domain.Quote{}, err
	}
	m.quotes[id] = updated
	return updated, nil
}

func TestGeminiProviderUsesHeaderAndRedactsQuerySecrets(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	fakeKey := "AIzaSy" + "Pass3HeaderTestSecretKey123456"

	var gotHeaderKey string
	var gotRawQuery string
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotHeaderKey = r.Header.Get("x-goog-api-key")
		gotRawQuery = r.URL.RawQuery
		w.WriteHeader(http.StatusBadGateway)
		_, _ = w.Write([]byte("upstream fail with ?key=" + fakeKey))
	}))
	defer mockServer.Close()

	svc := app.NewService().WithAIProviderConfig(app.AIProviderConfig{
		AIProvider: "gemini",
		AIModel:    "gemini-2.5-flash-lite",
		AIAPIKey:   fakeKey,
		AIBaseURL:  mockServer.URL,
	})
	caller := platformhttp.Principal{ID: uuid.New(), Role: "client"}
	_, err := svc.ChatWithRosa(ctx, "Sugiere sillas doradas", domain.RosaProposalRequest{EventType: "Boda", Guests: 50}, caller)
	if err == nil {
		t.Fatal("expected error from 502 mock provider")
	}
	if gotHeaderKey != fakeKey {
		t.Fatalf("expected x-goog-api-key header set, got %q", gotHeaderKey)
	}
	if strings.Contains(gotRawQuery, "key=") {
		t.Fatalf("expected no ?key= query param, got %q", gotRawQuery)
	}
	if strings.Contains(err.Error(), fakeKey) {
		t.Fatalf("expected API key redacted from error, got %q", err.Error())
	}
}

func TestSignContractTransactionalHashAndErrorPropagation(t *testing.T) {
	t.Parallel()
	ctx := t.Context()
	signerID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	quote := domain.Quote{
		ID:          "quo-tx-1",
		Code:        "COT-2026-01",
		ClientID:    signerID.String(),
		ClientEmail: "camila@rosafiesta.do",
		ClientName:  "Camila",
		EventTitle:  "Boda Real",
		Venue:       "Jardín",
		Guests:      80,
		Lines:       []domain.QuoteLine{{ArticleCode: "RF-001", Name: "Silla", Quantity: 10, UnitPriceRD: 100}},
		TotalRD:     1000,
		DepositRD:   300,
		BalanceRD:   700,
		Status:      domain.QuoteStatusSent,
	}

	store := &mockQuoteTxStore{quotes: map[string]domain.Quote{quote.ID: quote}, failOnTx: true}
	svc := app.NewService().WithStore(store)
	caller := platformhttp.Principal{ID: signerID, Email: "camila@rosafiesta.do", Role: "client"}

	if _, err := svc.SignContractForCaller(ctx, quote.ID, "Camila Peña", "<svg/>", caller); err == nil {
		t.Fatal("expected DB commit error to propagate instead of being swallowed")
	}

	store.failOnTx = false
	signed, err := svc.SignContractForCaller(ctx, quote.ID, "Camila Peña", "<svg/>", caller)
	if err != nil {
		t.Fatalf("SignContractForCaller failed: %v", err)
	}
	if !store.txStarted {
		t.Fatal("expected TransactionalSigner invoked")
	}
	if signed.SignedByUserID != signerID.String() {
		t.Fatalf("expected SignedByUserID %s, got %q", signerID, signed.SignedByUserID)
	}
	if len(signed.ContractHashSHA256) != 64 {
		t.Fatalf("expected 64-char SHA-256 hash, got %q", signed.ContractHashSHA256)
	}
}
