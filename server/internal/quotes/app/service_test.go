package app_test

import (
	"context"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/google/uuid"

	catalogdomain "github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

type fakeStockChecker struct {
	err error
}

func (f fakeStockChecker) CheckQuoteAvailability(_ context.Context, _, _ string, _ []domain.QuoteLine) error {
	return f.err
}

type fakeEventProvisioner struct {
	provisionedQuote domain.Quote
	eventID          string
	err              error
}

func (f *fakeEventProvisioner) ProvisionEventFromQuote(_ context.Context, quote domain.Quote) (string, error) {
	f.provisionedQuote = quote
	return f.eventID, f.err
}

type fakeAdminNotifier struct {
	notifiedQuotes []domain.Quote
}

func (f *fakeAdminNotifier) NotifyQuoteSigned(_ context.Context, quote domain.Quote) error {
	f.notifiedQuotes = append(f.notifiedQuotes, quote)
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

type fakeRealtimePublisher struct {
	events []realtimedomain.RealtimeEvent
}

func (f *fakeRealtimePublisher) Publish(event realtimedomain.RealtimeEvent) realtimedomain.RealtimeEvent {
	f.events = append(f.events, event)
	return event
}

type fakeCatalogReader struct {
	articles []catalogdomain.Article
}

func (f fakeCatalogReader) Articles(_ context.Context, _ string) []catalogdomain.Article {
	return f.articles
}

func buildTestWAVBase64(durationSec int) string {
	const sampleRate = 8000
	dataLen := sampleRate * 2 * durationSec
	buf := make([]byte, 44+dataLen)
	copy(buf[0:4], "RIFF")
	binary.LittleEndian.PutUint32(buf[4:8], uint32(36+dataLen))
	copy(buf[8:12], "WAVE")
	copy(buf[12:16], "fmt ")
	binary.LittleEndian.PutUint32(buf[16:20], 16)
	binary.LittleEndian.PutUint16(buf[20:22], 1)
	binary.LittleEndian.PutUint16(buf[22:24], 1)
	binary.LittleEndian.PutUint32(buf[24:28], sampleRate)
	binary.LittleEndian.PutUint32(buf[28:32], sampleRate*2)
	binary.LittleEndian.PutUint16(buf[32:34], 2)
	binary.LittleEndian.PutUint16(buf[34:36], 16)
	copy(buf[36:40], "data")
	binary.LittleEndian.PutUint32(buf[40:44], uint32(dataLen))
	return base64.StdEncoding.EncodeToString(buf)
}

func TestCreateAndSignQuoteRejectsInsufficientStock(t *testing.T) {
	svc := app.NewService().WithStockChecker(fakeStockChecker{err: errors.New("sin disponibilidad para RF-001")})
	caller := platformhttp.Principal{ID: uuid.New(), Email: "ana@rosafiesta.do", FullName: "Ana", Role: "client"}

	_, err := svc.CreateQuoteForCaller(context.Background(), app.CreateQuoteCommand{
		EventTitle:     "Boda Conflicto",
		ClientName:     "Ana",
		EventDateLabel: "2026-12-20",
		EventDateISO:   "2026-12-20",
		Venue:          "Hotel Embajador",
		Guests:         120,
		Lines: []domain.QuoteLine{
			{ArticleCode: "RF-001", Name: "Silla Tiffany", Quantity: 120, UnitPriceRD: 4},
		},
	}, caller)
	if !errors.Is(err, domain.ErrInsufficientStock) {
		t.Fatalf("expected ErrInsufficientStock on create, got %v", err)
	}

	openSvc := app.NewService()
	created, err := openSvc.CreateQuoteForCaller(context.Background(), app.CreateQuoteCommand{
		EventTitle:     "Boda Válida",
		ClientName:     "Ana",
		EventDateLabel: "2026-12-22",
		EventDateISO:   "2026-12-22",
		Venue:          "Hotel Embajador",
		Guests:         100,
		Lines: []domain.QuoteLine{
			{ArticleCode: "RF-001", Name: "Silla Tiffany", Quantity: 100, UnitPriceRD: 4},
		},
	}, caller)
	if err != nil {
		t.Fatalf("create failed unexpectedly: %v", err)
	}

	admin := platformhttp.Principal{ID: uuid.New(), Email: "maria@rosafiesta.do", FullName: "María", Role: "admin"}
	if _, err := openSvc.SendQuoteForAdmin(context.Background(), created.ID, app.EnrichAndSendQuoteCommand{}, admin); err != nil {
		t.Fatalf("send quote failed: %v", err)
	}
	openSvc.WithStockChecker(fakeStockChecker{err: errors.New("reservado por otro cliente")})
	_, signErr := openSvc.SignContractForCaller(context.Background(), created.ID, "Ana", "<svg/>", caller)
	if !errors.Is(signErr, domain.ErrInsufficientStock) {
		t.Fatalf("expected ErrInsufficientStock on sign, got %v", signErr)
	}
}

func TestSignContractProvisionsEventNotifiesAndEmitsRealtime(t *testing.T) {
	provisioner := &fakeEventProvisioner{eventID: "evt-provisioned-1"}
	notifier := &fakeAdminNotifier{}
	mailer := &fakeEmailSender{configured: true}
	pub := &fakeRealtimePublisher{}

	svc := app.NewService().
		WithEventProvisioner(provisioner).
		WithAdminNotifier(notifier).
		WithEmailSender(mailer).
		WithRealtimePublisher(pub)

	caller := platformhttp.Principal{
		ID:       uuid.New(),
		Email:    "camila@rosafiesta.do",
		FullName: "Camila Rodríguez",
		Role:     "client",
	}
	quote, err := svc.CreateQuoteForCaller(context.Background(), app.CreateQuoteCommand{
		EventTitle:     "Boda Primavera",
		ClientName:     "Camila Rodríguez",
		EventDateLabel: "15 Dic 2026",
		EventDateISO:   "2026-12-15",
		PackageID:      "pkg-boda-imperial",
		Venue:          "Jardín Botánico",
		Guests:         120,
		Lines: []domain.QuoteLine{
			{ArticleCode: "RF-001", Name: "Silla Tiffany dorada", Quantity: 120, UnitPriceRD: 4},
		},
	}, caller)
	if err != nil {
		t.Fatalf("create quote failed: %v", err)
	}
	admin := platformhttp.Principal{ID: uuid.New(), Email: "maria@rosafiesta.do", FullName: "María", Role: "admin"}
	if _, err := svc.SendQuoteForAdmin(context.Background(), quote.ID, app.EnrichAndSendQuoteCommand{}, admin); err != nil {
		t.Fatalf("send quote failed: %v", err)
	}

	signed, err := svc.SignContractForCaller(context.Background(), quote.ID, "Camila Rodríguez", "<svg>sig</svg>", caller)
	if err != nil {
		t.Fatalf("sign quote failed: %v", err)
	}
	assertSignedQuoteSideEffects(t, quote.ID, signed, notifier, mailer, pub)
}

func assertSignedQuoteSideEffects(
	t *testing.T,
	quoteID string,
	signed domain.Quote,
	notifier *fakeAdminNotifier,
	mailer *fakeEmailSender,
	pub *fakeRealtimePublisher,
) {
	t.Helper()
	if signed.Status != domain.QuoteStatusSigned {
		t.Fatalf("expected signed status, got %s", signed.Status)
	}
	if signed.EventID != "evt-provisioned-1" {
		t.Fatalf("expected provisioned eventId, got %q", signed.EventID)
	}
	if signed.SignedAtISO == "" {
		t.Fatalf("expected signedAtIso to be set")
	}
	if signed.ContractPDFURL == "" {
		t.Fatalf("expected contractPdfUrl to be set")
	}
	if len(notifier.notifiedQuotes) != 1 {
		t.Fatalf("expected 1 admin notification, got %d", len(notifier.notifiedQuotes))
	}
	if notifier.notifiedQuotes[0].ID != quoteID {
		t.Fatalf("unexpected notified quote ID: %q", notifier.notifiedQuotes[0].ID)
	}
	if len(mailer.sentTo) != 1 {
		t.Fatalf("expected 1 confirmation email, got %d", len(mailer.sentTo))
	}
	if mailer.sentTo[0] != "camila@rosafiesta.do" {
		t.Fatalf("unexpected email recipient: %q", mailer.sentTo[0])
	}
	assertQuoteSSESequence(t, pub.events, []realtimedomain.EventType{
		realtimedomain.EventQuoteCreated,
		realtimedomain.EventQuoteSent,
		realtimedomain.EventQuoteSigned,
	})
}

func assertQuoteSSESequence(t *testing.T, events []realtimedomain.RealtimeEvent, expected []realtimedomain.EventType) {
	t.Helper()
	if len(events) != len(expected) {
		t.Fatalf("expected %d SSE events %v, got %d (%+v)", len(expected), expected, len(events), events)
	}
	for idx, want := range expected {
		if events[idx].Type != want {
			t.Fatalf("expected SSE[%d]=%s, got %s (%+v)", idx, want, events[idx].Type, events)
		}
	}
}

func TestClientQuoteDerivesCatalogPricingAndRequiresSentBeforeSign(t *testing.T) {
	svc := app.NewService()
	caller := platformhttp.Principal{
		ID:       uuid.New(),
		Email:    "cliente@rosafiesta.do",
		FullName: "Cliente",
		Role:     "client",
	}

	_, badErr := svc.CreateQuoteForCaller(context.Background(), app.CreateQuoteCommand{
		EventTitle:     "Evento Inválido",
		EventDateLabel: "2026-12-20",
		EventDateISO:   "2026-12-20",
		Lines: []domain.QuoteLine{
			{ArticleCode: "non-existent-sku", Name: "Fake", Quantity: 5, UnitPriceRD: 1},
		},
	}, caller)
	if !errors.Is(badErr, domain.ErrInvalidQuoteItem) {
		t.Fatalf("expected ErrInvalidQuoteItem for unknown client SKU, got %v", badErr)
	}

	created, err := svc.CreateQuoteForCaller(context.Background(), app.CreateQuoteCommand{
		EventTitle:     "Evento Precio Catálogo",
		EventDateLabel: "2026-12-20",
		EventDateISO:   "2026-12-20",
		DeliveryFeeRD:  500,
		DiscountRD:     9999,
		Lines: []domain.QuoteLine{
			{ArticleCode: "RF-001", Name: "Tampered", Quantity: 10, UnitPriceRD: 1},
		},
	}, caller)
	if err != nil {
		t.Fatalf("create quote failed: %v", err)
	}
	if created.Lines[0].UnitPriceRD != 4 {
		t.Fatalf("expected catalog unit price 4 RD$, got %d", created.Lines[0].UnitPriceRD)
	}
	if created.DiscountRD != 0 {
		t.Fatalf("expected client discount forced to 0, got %d", created.DiscountRD)
	}
	if created.TotalRD != 540 {
		t.Fatalf("expected total 10*4 + 500 = 540, got %d", created.TotalRD)
	}

	_, signErr := svc.SignContractForCaller(context.Background(), created.ID, "Cliente", "<svg/>", caller)
	if !errors.Is(signErr, domain.ErrQuoteNotSent) {
		t.Fatalf("expected ErrQuoteNotSent when signing pending quote, got %v", signErr)
	}
}

func TestRosaAIAndSTTReturnNotConfiguredWithoutStubsAndEnforceAudioDuration(t *testing.T) {
	svc := app.NewService()
	caller := platformhttp.Principal{ID: uuid.New(), Role: "client"}

	_, chatErr := svc.ChatWithRosa(context.Background(), "Hola Rosa", domain.RosaProposalRequest{}, caller)
	if !errors.Is(chatErr, domain.ErrAIProviderNotConfigured) {
		t.Fatalf("expected ErrAIProviderNotConfigured when key missing, got %v", chatErr)
	}

	shortWAV := buildTestWAVBase64(2)
	_, sttErr := svc.TranscribeAudio(context.Background(), app.TranscribeCommand{AudioBase64: shortWAV, MimeType: "audio/wav"})
	if !errors.Is(sttErr, domain.ErrSTTProviderNotConfigured) {
		t.Fatalf("expected ErrSTTProviderNotConfigured when key missing, got %v", sttErr)
	}

	tooLongWAV := buildTestWAVBase64(26)
	_, longErr := svc.TranscribeAudio(context.Background(), app.TranscribeCommand{AudioBase64: tooLongWAV, MimeType: "audio/wav"})
	if !errors.Is(longErr, domain.ErrAudioTooLong) {
		t.Fatalf("expected ErrAudioTooLong for 26s wav, got %v", longErr)
	}

	proposal := svc.GenerateRosaProposal(context.Background(), domain.RosaProposalRequest{
		EventType: "Boda",
		Guests:    120,
		BudgetRD:  4500,
	})
	if len(proposal.SuggestedLines) == 0 {
		t.Fatalf("expected deterministic proposal lines without AI key")
	}
}

func TestRosaChatUsesLiveCatalogOnlyCallsProviderAndAppliesRateLimit(t *testing.T) {
	var capturedSystemPrompt string
	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		raw, _ := io.ReadAll(req.Body)
		var body struct {
			SystemInstruction struct {
				Parts []struct {
					Text string `json:"text"`
				} `json:"parts"`
			} `json:"systemInstruction"`
		}
		_ = json.Unmarshal(raw, &body)
		if len(body.SystemInstruction.Parts) > 0 {
			capturedSystemPrompt = body.SystemInstruction.Parts[0].Text
		}
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(`{"candidates":[{"content":{"parts":[{"text":"{\"reply\":\"Te sugiero 40 sillas reales.\",\"suggestedItems\":[{\"articleCode\":\"RF-001\",\"name\":\"Silla Real\",\"quantity\":40}]}"}]}}]}`))
	}))
	defer server.Close()

	svc := app.NewService().
		WithCatalog(fakeCatalogReader{
			articles: []catalogdomain.Article{
				{ID: "silla-tiffany", Name: "Silla Real", CategoryID: "mobiliario", DailyPriceDop: 12, AvailableStock: 60},
			},
		}).
		WithHTTPClient(server.Client()).
		WithAIProviderConfig(app.AIProviderConfig{
			AIProvider:      "gemini",
			AIAPIKey:        "test-key",
			AIBaseURL:       server.URL,
			RateLimitMax:    1,
			RateLimitWindow: time.Minute,
		})

	caller := platformhttp.Principal{ID: uuid.New(), Role: "client"}
	reply, err := svc.ChatWithRosa(context.Background(), "Recomiéndame sillas", domain.RosaProposalRequest{EventType: "Boda", Guests: 40, BudgetRD: 2000}, caller)
	if err != nil {
		t.Fatalf("expected Gemini mock chat to succeed, got %v", err)
	}
	if reply.Provider != "gemini" {
		t.Fatalf("expected gemini provider, got %q", reply.Provider)
	}
	if len(reply.SuggestedItems) != 1 {
		t.Fatalf("expected 1 suggested item, got %+v", reply.SuggestedItems)
	}
	if reply.SuggestedItems[0].ArticleCode != "RF-001" {
		t.Fatalf("expected RF-001 suggested item, got %+v", reply.SuggestedItems)
	}
	if strings.Contains(capturedSystemPrompt, "luces-bistro") {
		t.Fatalf("expected fake SKU luces-bistro removed from prompt, got: %s", capturedSystemPrompt)
	}
	if strings.Contains(capturedSystemPrompt, "manteleria-lino") {
		t.Fatalf("expected fake SKU manteleria-lino removed from prompt, got: %s", capturedSystemPrompt)
	}

	_, rateErr := svc.ChatWithRosa(context.Background(), "Segunda consulta", domain.RosaProposalRequest{}, caller)
	if !errors.Is(rateErr, domain.ErrAIRateLimited) {
		t.Fatalf("expected ErrAIRateLimited on second call, got %v", rateErr)
	}
}

func TestRosaWhisperSTTCallsProviderWithMultipartAudio(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {
		if !strings.HasPrefix(req.Header.Get("Content-Type"), "multipart/form-data") {
			t.Fatalf("expected multipart request, got %q", req.Header.Get("Content-Type"))
		}
		if req.Header.Get("Authorization") != "Bearer groq-test-key" {
			t.Fatalf("missing bearer auth header")
		}
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(`{"text":"Necesito ochenta sillas doradas"}`))
	}))
	defer server.Close()

	svc := app.NewService().
		WithHTTPClient(server.Client()).
		WithAIProviderConfig(app.AIProviderConfig{
			STTProvider: "groq",
			STTAPIKey:   "groq-test-key",
			AIBaseURL:   server.URL,
		})

	res, err := svc.TranscribeAudio(context.Background(), app.TranscribeCommand{
		AudioBase64: buildTestWAVBase64(2),
		MimeType:    "audio/wav",
		Locale:      "es-DO",
	})
	if err != nil {
		t.Fatalf("expected Whisper STT to succeed, got %v", err)
	}
	if res.Transcript != "Necesito ochenta sillas doradas" {
		t.Fatalf("unexpected transcript: %q", res.Transcript)
	}
	if res.Provider != "groq" {
		t.Fatalf("unexpected provider: %q", res.Provider)
	}
}

func TestRosaGenerateSketchCallsAIAndParsesLayout(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, _ *http.Request) {
		writer.Header().Set("Content-Type", "application/json")
		_, _ = writer.Write([]byte(`{"candidates":[{"content":{"parts":[{"text":"{\"reply\":\"Distribución lista\",\"eventWidthMeters\":14,\"eventDepthMeters\":10,\"elements\":[{\"id\":\"el-1\",\"kind\":\"arco\",\"label\":\"Arco Principal\",\"xRatio\":0.5,\"yRatio\":0.2,\"capacity\":0}]}"}]}}]}`))
	}))
	defer server.Close()

	svc := app.NewService().
		WithHTTPClient(server.Client()).
		WithAIProviderConfig(app.AIProviderConfig{
			AIProvider: "gemini",
			AIAPIKey:   "test-key",
			AIBaseURL:  server.URL,
		})
	caller := platformhttp.Principal{ID: uuid.New(), Role: "client"}
	sketch, err := svc.GenerateRosaSketch(context.Background(), "Boda jardín con arco al centro", "Boda", 80, caller)
	if err != nil {
		t.Fatalf("GenerateRosaSketch failed: %v", err)
	}
	if len(sketch.Elements) != 1 {
		t.Fatalf("expected 1 sketch element, got %+v", sketch.Elements)
	}
	if sketch.Elements[0].Label != "Arco Principal" {
		t.Fatalf("unexpected element label: %+v", sketch.Elements[0])
	}
}

func TestSendQuoteForAdminEnrichesAndMarksSent(t *testing.T) {
	svc := app.NewService()
	clientCaller := platformhttp.Principal{ID: uuid.New(), Role: "client", Email: "cliente@ejemplo.do", FullName: "Cliente Real"}
	adminCaller := platformhttp.Principal{ID: uuid.New(), Role: "admin", FullName: "Admin Rosa"}

	created, err := svc.CreateQuoteForCaller(context.Background(), app.CreateQuoteCommand{
		EventTitle:     "Cumpleaños de Ana",
		ClientName:     "Cliente Real",
		ClientEmail:    "cliente@ejemplo.do",
		EventDateLabel: "Sáb 15 dic",
		Venue:          "Salón Jardín",
		Guests:         50,
		Lines: []domain.QuoteLine{
			{ArticleCode: "RF-002", Name: "Silla Plastica", Quantity: 10, UnitPriceRD: 15},
		},
		DeliveryFeeRD: 350,
		DiscountRD:    0,
	}, clientCaller)
	if err != nil {
		t.Fatalf("CreateQuoteForCaller failed: %v", err)
	}
	if created.Status != domain.QuoteStatusPending {
		t.Fatalf("expected created client quote to start pending, got %q", created.Status)
	}

	_, forbiddenErr := svc.SendQuoteForAdmin(context.Background(), created.ID, app.EnrichAndSendQuoteCommand{
		Notes: "No permitido",
	}, clientCaller)
	if !errors.Is(forbiddenErr, domain.ErrQuoteForbidden) {
		t.Fatalf("expected ErrQuoteForbidden for client caller, got %v", forbiddenErr)
	}

	newDelivery := 500
	newDiscount := 50
	sent, err := svc.SendQuoteForAdmin(context.Background(), created.ID, app.EnrichAndSendQuoteCommand{
		Venue:         "Jardín Los Rosales",
		DeliveryFeeRD: &newDelivery,
		DiscountRD:    &newDiscount,
		Notes:         "Incluye montaje 2 horas antes",
	}, adminCaller)
	if err != nil {
		t.Fatalf("SendQuoteForAdmin failed: %v", err)
	}
	if sent.Status != domain.QuoteStatusSent {
		t.Fatalf("expected status 'sent', got %q", sent.Status)
	}
	if sent.TotalRD != 650 {
		t.Fatalf("expected catalog-derived recalculated total 650, got %d", sent.TotalRD)
	}
	if sent.Notes != "Incluye montaje 2 horas antes" {
		t.Fatalf("expected admin notes persisted, got %q", sent.Notes)
	}
}
