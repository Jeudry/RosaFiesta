package app

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	catalogdomain "github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

type CatalogReader interface {
	Articles(ctx context.Context, query string) []catalogdomain.Article
}

type Store interface {
	Enabled() bool
	ListQuotes(ctx context.Context) ([]domain.Quote, error)
	UpsertQuote(ctx context.Context, quote domain.Quote, now time.Time) error
}

type CreateQuoteCommand struct {
	EventID        string
	EventTitle     string
	ClientName     string
	ClientEmail    string
	EventDateLabel string
	Venue          string
	Guests         int
	Lines          []domain.QuoteLine
	DeliveryFeeRD  int
	DiscountRD     int
}

type TranscribeCommand struct {
	AudioBase64 string
	MimeType    string
	Locale      string
	PromptHint  string
}

type Service struct {
	mu        sync.RWMutex
	clock     clock.Clock
	store     Store
	catalog   CatalogReader
	sttAPIURL string
	aiAPIKey  string
	quotes    map[string]domain.Quote
	order     []string
	nextSeq   int
}

func NewService() *Service {
	svc := &Service{clock: clock.System{}}
	svc.Reset(context.Background())
	return svc
}

func (s *Service) WithClock(clk clock.Clock) *Service {
	if clk != nil {
		s.mu.Lock()
		s.clock = clk
		s.mu.Unlock()
	}
	return s
}

func (s *Service) WithCatalog(reader CatalogReader) *Service {
	s.mu.Lock()
	s.catalog = reader
	s.mu.Unlock()
	return s
}

func (s *Service) WithAIOptions(sttAPIURL, aiAPIKey string) *Service {
	s.mu.Lock()
	s.sttAPIURL = strings.TrimSpace(sttAPIURL)
	s.aiAPIKey = strings.TrimSpace(aiAPIKey)
	s.mu.Unlock()
	return s
}

func (s *Service) Reset(_ context.Context) {
	canonical := domain.CanonicalQuotes()
	quotes := make(map[string]domain.Quote, len(canonical))
	order := make([]string, 0, len(canonical))
	for _, item := range canonical {
		quotes[item.ID] = item
		order = append(order, item.ID)
	}
	s.mu.Lock()
	s.quotes = quotes
	s.order = order
	s.nextSeq = 1200 + len(canonical)
	s.mu.Unlock()
}

func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	s.syncFromStoreLocked(context.Background())
	s.mu.Unlock()
	return s
}

func (s *Service) PendingQuotesCount(ctx context.Context) int {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	count := 0
	for _, id := range s.order {
		if s.quotes[id].Status == domain.QuoteStatusPending {
			count++
		}
	}
	return count
}

func (s *Service) ListQuotes(status string) []domain.Quote {
	return s.ListQuotesForCaller(context.Background(), status, platformhttp.Principal{Role: "admin"}, true)
}

func (s *Service) ListQuotesForCaller(
	ctx context.Context,
	status string,
	caller platformhttp.Principal,
	authenticated bool,
) []domain.Quote {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	snapshot := make([]domain.Quote, 0, len(s.order))
	for _, id := range s.order {
		snapshot = append(snapshot, s.quotes[id])
	}
	s.mu.Unlock()

	trimmedStatus := strings.TrimSpace(status)
	result := make([]domain.Quote, 0, len(snapshot))
	callerID := callerIDString(caller)
	for _, item := range snapshot {
		if trimmedStatus != "" {
			if string(item.Status) != trimmedStatus {
				continue
			}
		}
		if authenticated {
			if caller.IsClient() {
				if !item.BelongsToClient(callerID, caller.Email, caller.FullName) {
					continue
				}
			}
		}
		result = append(result, item)
	}
	return result
}

func (s *Service) GetQuoteForCaller(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
	authenticated bool,
) (domain.Quote, error) {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	quote, ok := s.quotes[id]
	s.mu.Unlock()
	if !ok {
		return domain.Quote{}, domain.ErrQuoteNotFound
	}
	if authenticated {
		if caller.IsClient() {
			callerID := callerIDString(caller)
			if !quote.BelongsToClient(callerID, caller.Email, caller.FullName) {
				return domain.Quote{}, domain.ErrQuoteForbidden
			}
		}
	}
	return quote, nil
}

func (s *Service) CreateQuoteForCaller(
	ctx context.Context,
	cmd CreateQuoteCommand,
	caller platformhttp.Principal,
) (domain.Quote, error) {
	total, deposit, balance, err := domain.CalculateQuoteTotals(cmd.Lines, cmd.DeliveryFeeRD, cmd.DiscountRD)
	if err != nil {
		return domain.Quote{}, err
	}
	now := s.clock.Now()
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	id, code := s.allocateUniqueQuoteIdentifiersLocked(now)
	clientID := ""
	clientEmail := strings.TrimSpace(cmd.ClientEmail)
	clientName := strings.TrimSpace(cmd.ClientName)
	if caller.IsClient() {
		clientID = callerIDString(caller)
		clientEmail = caller.Email
		if clientName == "" {
			clientName = caller.FullName
		}
	}
	quote := domain.Quote{
		ID:             id,
		Code:           code,
		EventID:        strings.TrimSpace(cmd.EventID),
		ClientID:       clientID,
		ClientEmail:    clientEmail,
		EventTitle:     strings.TrimSpace(cmd.EventTitle),
		ClientName:     clientName,
		EventDateLabel: strings.TrimSpace(cmd.EventDateLabel),
		Venue:          strings.TrimSpace(cmd.Venue),
		Guests:         cmd.Guests,
		Lines:          cmd.Lines,
		DeliveryFeeRD:  cmd.DeliveryFeeRD,
		DiscountRD:     cmd.DiscountRD,
		TotalRD:        total,
		DepositPercent: 30,
		DepositRD:      deposit,
		BalanceRD:      balance,
		Status:         domain.QuoteStatusPending,
	}
	s.quotes[id] = quote
	s.order = append(s.order, id)
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			if err := store.UpsertQuote(ctx, quote, now); err != nil {
				return domain.Quote{}, err
			}
		}
	}
	return quote, nil
}

func (s *Service) DuplicateQuoteForCaller(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
) (domain.Quote, error) {
	source, err := s.GetQuoteForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Quote{}, err
	}
	now := s.clock.Now()
	s.mu.Lock()
	newID, newCode := s.allocateUniqueQuoteIdentifiersLocked(now)
	linesCopy := make([]domain.QuoteLine, len(source.Lines))
	copy(linesCopy, source.Lines)
	duplicated := source
	duplicated.ID = newID
	duplicated.Code = newCode
	duplicated.EventTitle = "Copia de " + source.EventTitle
	duplicated.Lines = linesCopy
	duplicated.Status = domain.QuoteStatusPending
	duplicated.SignatureSVG = ""
	duplicated.SignedByName = ""
	duplicated.RejectionReason = ""
	s.quotes[newID] = duplicated
	s.order = append(s.order, newID)
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			if err := store.UpsertQuote(ctx, duplicated, now); err != nil {
				return domain.Quote{}, err
			}
		}
	}
	return duplicated, nil
}

func (s *Service) SignContractForCaller(
	ctx context.Context,
	id string,
	signedByName string,
	signatureSVG string,
	caller platformhttp.Principal,
) (domain.Quote, error) {
	cleanSigner := strings.TrimSpace(signedByName)
	cleanSVG := strings.TrimSpace(signatureSVG)
	if cleanSigner == "" {
		return domain.Quote{}, domain.ErrInvalidSignature
	}
	if cleanSVG == "" {
		return domain.Quote{}, domain.ErrInvalidSignature
	}
	quote, err := s.GetQuoteForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Quote{}, err
	}
	now := s.clock.Now()
	quote.Status = domain.QuoteStatusSigned
	quote.SignedByName = cleanSigner
	quote.SignatureSVG = signatureSVG
	s.mu.Lock()
	s.quotes[id] = quote
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.UpsertQuote(ctx, quote, now)
		}
	}
	return quote, nil
}

func (s *Service) DeclineQuoteForCaller(
	ctx context.Context,
	id string,
	reason string,
	caller platformhttp.Principal,
) (domain.Quote, error) {
	quote, err := s.GetQuoteForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Quote{}, err
	}
	now := s.clock.Now()
	quote.Status = domain.QuoteStatusDeclined
	quote.RejectionReason = strings.TrimSpace(reason)
	s.mu.Lock()
	s.quotes[id] = quote
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.UpsertQuote(ctx, quote, now)
		}
	}
	return quote, nil
}

func (s *Service) GenerateRosaProposal(
	ctx context.Context,
	req domain.RosaProposalRequest,
) domain.RosaMoodboardProposal {
	skus := s.loadLiveCatalogSKUs(ctx)
	return domain.BuildRosaProposalFromCatalog(req, skus)
}

func (s *Service) ChatWithRosa(
	ctx context.Context,
	message string,
	req domain.RosaProposalRequest,
	caller platformhttp.Principal,
) domain.RosaChatResponse {
	skus := s.loadLiveCatalogSKUs(ctx)
	snap := s.buildAdminSnapshot(ctx)
	isAdmin := caller.IsAdminOrStaff()
	return domain.BuildRosaChatReply(message, isAdmin, req, skus, snap)
}

func (s *Service) TranscribeAudio(ctx context.Context, cmd TranscribeCommand) (domain.TranscriptionResult, error) {
	analysis, err := domain.AnalyzeBase64Audio(cmd.AudioBase64)
	if err != nil {
		return domain.TranscriptionResult{}, err
	}
	locale := strings.TrimSpace(cmd.Locale)
	if locale == "" {
		locale = "es-DO"
	}
	s.mu.RLock()
	sttURL := s.sttAPIURL
	apiKey := s.aiAPIKey
	s.mu.RUnlock()

	if sttURL != "" {
		if externalText, extErr := callExternalSTT(ctx, sttURL, apiKey, cmd, locale); extErr == nil {
			return domain.TranscriptionResult{
				Transcript:    externalText,
				Locale:        locale,
				DurationMs:    analysis.DurationMs,
				SampleRateHz:  analysis.SampleRateHz,
				RMSEnergy:     analysis.RMSEnergy,
				VoiceDetected: analysis.VoiceDetected,
				Provider:      "external_stt",
			}, nil
		}
	}
	transcript := buildAcousticTranscript(analysis, cmd.PromptHint)
	return domain.TranscriptionResult{
		Transcript:    transcript,
		Locale:        locale,
		DurationMs:    analysis.DurationMs,
		SampleRateHz:  analysis.SampleRateHz,
		RMSEnergy:     analysis.RMSEnergy,
		VoiceDetected: analysis.VoiceDetected,
		Provider:      "rosa_acoustic_analyzer",
	}, nil
}

func callExternalSTT(ctx context.Context, sttURL, apiKey string, cmd TranscribeCommand, locale string) (string, error) {
	payloadBytes, err := json.Marshal(map[string]string{
		"audioBase64": cmd.AudioBase64,
		"mimeType":    cmd.MimeType,
		"locale":      locale,
		"promptHint":  cmd.PromptHint,
	})
	if err != nil {
		return "", err
	}
	reqCtx, cancel := context.WithTimeout(ctx, 10*time.Second)
	defer cancel()
	httpReq, err := http.NewRequestWithContext(reqCtx, http.MethodPost, sttURL, bytes.NewReader(payloadBytes))
	if err != nil {
		return "", err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	if apiKey != "" {
		httpReq.Header.Set("Authorization", "Bearer "+apiKey)
	}
	resp, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return "", err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode >= 300 {
		return "", fmt.Errorf("external stt status %d", resp.StatusCode)
	}
	var parsed struct {
		Transcript string `json:"transcript"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&parsed); err != nil {
		return "", err
	}
	trimmed := strings.TrimSpace(parsed.Transcript)
	if trimmed == "" {
		return "", domain.ErrInvalidAudioPayload
	}
	return trimmed, nil
}

func buildAcousticTranscript(analysis domain.AudioAnalysis, hint string) string {
	trimmedHint := strings.TrimSpace(hint)
	if !analysis.VoiceDetected {
		if trimmedHint != "" {
			return trimmedHint
		}
		return "Audio recibido con baja energía de voz; describe tu evento o acerca el micrófono."
	}
	if trimmedHint != "" {
		return trimmedHint
	}
	return fmt.Sprintf("Nota de voz capturada (%d ms, %d Hz) para cotización con Rosa IA.", analysis.DurationMs, analysis.SampleRateHz)
}

func (s *Service) loadLiveCatalogSKUs(ctx context.Context) []domain.CatalogSKU {
	s.mu.RLock()
	reader := s.catalog
	s.mu.RUnlock()
	if reader == nil {
		return domain.DefaultCatalogSKUs()
	}
	liveArticles := reader.Articles(ctx, "")
	if len(liveArticles) == 0 {
		return domain.DefaultCatalogSKUs()
	}
	slugToCode := map[string]string{
		"silla-tiffany": "RF-001",
		"mesa-plegable": "RF-002",
		"mesa-redonda":  "RF-003",
		"centro-floral": "RF-004",
		"arco-rosa":     "RF-005",
		"mesa-helados":  "RF-006",
	}
	out := make([]domain.CatalogSKU, 0, len(liveArticles)+2)
	for _, art := range liveArticles {
		code := slugToCode[art.ID]
		if code == "" {
			code = art.ID
		}
		out = append(out, domain.CatalogSKU{
			ID:             art.ID,
			Code:           code,
			Name:           art.Name,
			CategoryID:     art.CategoryID,
			UnitPriceRD:    art.DailyPriceDop,
			AvailableStock: art.AvailableStock,
		})
	}
	out = append(out,
		domain.CatalogSKU{ID: "luces-bistro", Code: "RF-007", Name: "Guirnalda luces bistró cálidas", CategoryID: "iluminacion", UnitPriceRD: 45, AvailableStock: 20},
		domain.CatalogSKU{ID: "manteleria-lino", Code: "RF-008", Name: "Mantelería lino imperial", CategoryID: "manteleria", UnitPriceRD: 18, AvailableStock: 60},
	)
	return out
}

func (s *Service) buildAdminSnapshot(ctx context.Context) domain.RosaAdminSnapshot {
	s.mu.Lock()
	s.syncFromStoreLocked(ctx)
	pendingCount := 0
	signedCount := 0
	for _, id := range s.order {
		status := s.quotes[id].Status
		if status == domain.QuoteStatusPending {
			pendingCount++
		}
		if status == domain.QuoteStatusSigned {
			signedCount++
		}
	}
	reader := s.catalog
	s.mu.Unlock()

	lowStock := make([]string, 0)
	if reader != nil {
		for _, art := range reader.Articles(ctx, "") {
			if art.IsLowStock() {
				lowStock = append(lowStock, art.Name)
			}
		}
	}
	return domain.RosaAdminSnapshot{
		PendingQuotesCount: pendingCount,
		SignedQuotesCount:  signedCount,
		LowStockItems:      lowStock,
	}
}

func (s *Service) allocateUniqueQuoteIdentifiersLocked(now time.Time) (string, string) {
	base := 1200 + len(s.order) + 1
	if s.nextSeq < base {
		s.nextSeq = base
	} else {
		s.nextSeq++
	}
	suffix := uuid.NewString()[:6]
	upperSuffix := strings.ToUpper(suffix[:4])
	id := fmt.Sprintf("quo-%d-%s", s.nextSeq, suffix)
	code := fmt.Sprintf("COT-%d-%d-%s", now.Year(), s.nextSeq, upperSuffix)
	return id, code
}

func (s *Service) syncFromStoreLocked(ctx context.Context) {
	if s.store == nil {
		return
	}
	if !s.store.Enabled() {
		return
	}
	dbQuotes, err := s.store.ListQuotes(ctx)
	if err != nil {
		return
	}
	for _, dbQuote := range dbQuotes {
		existing, ok := s.quotes[dbQuote.ID]
		if ok {
			merged := mergeQuoteFromDB(existing, dbQuote)
			s.quotes[dbQuote.ID] = merged
		} else {
			s.quotes[dbQuote.ID] = dbQuote
			s.order = append(s.order, dbQuote.ID)
		}
	}
}

func mergeQuoteFromDB(existing, dbQuote domain.Quote) domain.Quote {
	merged := dbQuote
	if len(merged.Lines) == 0 {
		merged.Lines = existing.Lines
	}
	if merged.Venue == "" {
		merged.Venue = existing.Venue
	}
	if merged.Guests == 0 {
		merged.Guests = existing.Guests
	}
	if merged.EventDateLabel == "" {
		merged.EventDateLabel = existing.EventDateLabel
	}
	return merged
}

func callerIDString(caller platformhttp.Principal) string {
	if caller.ID == uuid.Nil {
		return ""
	}
	return caller.ID.String()
}
