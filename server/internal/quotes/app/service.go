// Package app coordina cotizaciones, contratos digitales, disponibilidad y asistentes de RosaFiesta.
package app

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	catalogdomain "github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

// StockChecker valida disponibilidad de inventario para una cotización en la fecha solicitada.
type StockChecker interface {
	CheckQuoteAvailability(ctx context.Context, rawDate, excludeEventID string, lines []domain.QuoteLine) error
}

// EventProvisioner aprovisiona o actualiza un evento al firmarse una cotización.
type EventProvisioner interface {
	ProvisionEventFromQuote(ctx context.Context, quote domain.Quote) (string, error)
}

// AdminNotifier notifica al equipo administrativo cuando se firma un contrato.
type AdminNotifier interface {
	NotifyQuoteSigned(ctx context.Context, quote domain.Quote) error
}

type CatalogReader interface {
	Articles(ctx context.Context, query string) []catalogdomain.Article
}

// DepositRateProvider provee el porcentaje de anticipo vigente configurado en ajustes.
type DepositRateProvider interface {
	CurrentDepositPercent(ctx context.Context) int
}

// QuoteSentNotifier notifica al cliente cuando una cotización es enviada por administración.
type QuoteSentNotifier interface {
	NotifyQuoteSent(ctx context.Context, quote domain.Quote)
}

// EmailPreferenceChecker verifica si el cliente permite correos de confirmación.
type EmailPreferenceChecker interface {
	AllowsEmailReceipts(ctx context.Context, clientID string, email string) bool
}

type Store interface {
	Enabled() bool
	ListQuotes(ctx context.Context) ([]domain.Quote, error)
	UpsertQuote(ctx context.Context, quote domain.Quote, now time.Time) error
}

// TransactionalSigner ejecuta la firma del contrato bajo bloqueo de fila FOR UPDATE.
type TransactionalSigner interface {
	SignQuoteTx(
		ctx context.Context,
		id string,
		now time.Time,
		applySign func(existing domain.Quote) (domain.Quote, error),
	) (domain.Quote, error)
}

type CreateQuoteCommand struct {
	EventID         string
	EventTitle      string
	ClientName      string
	ClientEmail     string
	EventDateLabel  string
	EventDateISO    string
	PackageID       string
	Venue           string
	Guests          int
	Lines           []domain.QuoteLine
	DeliveryFeeRD   int
	DiscountRD      int
	Notes           string
	ContactPhone    string
	SendImmediately bool
}

type EnrichAndSendQuoteCommand struct {
	EventTitle     string
	Venue          string
	EventDateLabel string
	EventDateISO   string
	Guests         int
	Lines          []domain.QuoteLine
	DeliveryFeeRD  *int
	DiscountRD     *int
	Notes          string
}

type TranscribeCommand struct {
	AudioBase64 string
	MimeType    string
	Locale      string
	PromptHint  string
}

type Service struct {
	mu               sync.RWMutex
	clock            clock.Clock
	store            Store
	catalog          CatalogReader
	stockChecker     StockChecker
	eventProvisioner EventProvisioner
	adminNotifier    AdminNotifier
	quoteSentNotif   QuoteSentNotifier
	depositProvider  DepositRateProvider
	emailPrefChecker EmailPreferenceChecker
	emailSender      mailer.Sender
	realtimePub      realtimedomain.Publisher
	aiConfig         AIProviderConfig
	httpClient       *http.Client
	rateLimiter      *slidingWindowLimiter
	quotes           map[string]domain.Quote
	order            []string
	nextSeq          int
}

func NewService() *Service {
	svc := &Service{
		clock:       clock.System{},
		aiConfig:    normalizeAIProviderConfig(AIProviderConfig{}),
		httpClient:  &http.Client{Timeout: defaultAITimeout},
		rateLimiter: newSlidingWindowLimiter(),
	}
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
	cfg := s.aiConfig
	cfg.STTAPIURL = strings.TrimSpace(sttAPIURL)
	cfg.AIAPIKey = strings.TrimSpace(aiAPIKey)
	s.aiConfig = normalizeAIProviderConfig(cfg)
	s.mu.Unlock()
	return s
}

// WithAIProviderConfig configura proveedores, claves, modelos y límites de Rosa IA y STT.
func (s *Service) WithAIProviderConfig(cfg AIProviderConfig) *Service {
	s.mu.Lock()
	s.aiConfig = normalizeAIProviderConfig(cfg)
	s.mu.Unlock()
	return s
}

// WithStockChecker configura el verificador de disponibilidad de inventario.
func (s *Service) WithStockChecker(checker StockChecker) *Service {
	s.mu.Lock()
	s.stockChecker = checker
	s.mu.Unlock()
	return s
}

// WithEventProvisioner configura el aprovisionador automático de eventos desde cotizaciones firmadas.
func (s *Service) WithEventProvisioner(provisioner EventProvisioner) *Service {
	s.mu.Lock()
	s.eventProvisioner = provisioner
	s.mu.Unlock()
	return s
}

// WithAdminNotifier configura el notificador al equipo administrativo.
func (s *Service) WithAdminNotifier(notifier AdminNotifier) *Service {
	s.mu.Lock()
	s.adminNotifier = notifier
	s.mu.Unlock()
	return s
}

// WithDepositRateProvider configura el proveedor dinámico de porcentaje de anticipo.
func (s *Service) WithDepositRateProvider(provider DepositRateProvider) *Service {
	s.mu.Lock()
	s.depositProvider = provider
	s.mu.Unlock()
	return s
}

// WithQuoteSentNotifier configura el notificador al cliente cuando se envía una cotización.
func (s *Service) WithQuoteSentNotifier(notifier QuoteSentNotifier) *Service {
	s.mu.Lock()
	s.quoteSentNotif = notifier
	s.mu.Unlock()
	return s
}

// WithEmailPreferenceChecker configura la verificación de preferencia de recibos por correo.
func (s *Service) WithEmailPreferenceChecker(checker EmailPreferenceChecker) *Service {
	s.mu.Lock()
	s.emailPrefChecker = checker
	s.mu.Unlock()
	return s
}

// WithEmailSender configura el cliente de correo electrónico para confirmaciones de contrato.
func (s *Service) WithEmailSender(sender mailer.Sender) *Service {
	s.mu.Lock()
	s.emailSender = sender
	s.mu.Unlock()
	return s
}

// WithRealtimePublisher configura el publicador de eventos SSE para cotizaciones.
func (s *Service) WithRealtimePublisher(pub realtimedomain.Publisher) *Service {
	s.mu.Lock()
	s.realtimePub = pub
	s.mu.Unlock()
	return s
}

// WithHTTPClient configura el cliente HTTP utilizado al invocar proveedores externos de IA/STT.
func (s *Service) WithHTTPClient(client *http.Client) *Service {
	if client != nil {
		s.mu.Lock()
		s.httpClient = client
		s.mu.Unlock()
	}
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

func (s *Service) ResetEmpty(_ context.Context) {
	s.mu.Lock()
	s.quotes = make(map[string]domain.Quote)
	s.order = nil
	s.nextSeq = 1200
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
	lines, discountRD, err := s.normalizeQuoteInputsForCaller(ctx, cmd.Lines, cmd.DiscountRD, caller)
	if err != nil {
		return domain.Quote{}, err
	}
	cmd.Lines = lines
	cmd.DiscountRD = discountRD
	depositPct := s.resolveDepositPercent(ctx)
	total, deposit, balance, err := domain.CalculateQuoteTotalsWithDeposit(cmd.Lines, cmd.DeliveryFeeRD, cmd.DiscountRD, depositPct)
	if err != nil {
		return domain.Quote{}, err
	}
	if err := s.checkQuoteStockAvailability(ctx, cmd.EventDateISO, cmd.EventDateLabel, cmd.EventID, cmd.Lines); err != nil {
		return domain.Quote{}, err
	}
	now := s.clock.Now()
	quote := s.buildAndStoreCreatedQuote(ctx, cmd, caller, total, depositPct, deposit, balance, now)
	store := s.currentStore()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertQuote(ctx, quote, now); err != nil {
				return domain.Quote{}, err
			}
		}
	}
	return quote, nil
}

func (s *Service) normalizeQuoteInputsForCaller(
	ctx context.Context,
	rawLines []domain.QuoteLine,
	rawDiscountRD int,
	caller platformhttp.Principal,
) ([]domain.QuoteLine, int, error) {
	if !caller.IsClient() {
		return rawLines, rawDiscountRD, nil
	}
	skus := s.loadLiveCatalogSKUs(ctx)
	byRef := make(map[string]domain.CatalogSKU, len(skus)*2)
	for _, sku := range skus {
		if codeKey := strings.ToLower(strings.TrimSpace(sku.Code)); codeKey != "" {
			byRef[codeKey] = sku
		}
		if idKey := strings.ToLower(strings.TrimSpace(sku.ID)); idKey != "" {
			byRef[idKey] = sku
		}
	}
	normalized := make([]domain.QuoteLine, 0, len(rawLines))
	for _, line := range rawLines {
		if line.Quantity <= 0 {
			return nil, 0, domain.ErrInvalidQuoteItem
		}
		ref := strings.ToLower(strings.TrimSpace(line.ArticleCode))
		sku, matched := byRef[ref]
		if !matched {
			return nil, 0, domain.ErrInvalidQuoteItem
		}
		name := strings.TrimSpace(sku.Name)
		if name == "" {
			name = strings.TrimSpace(line.Name)
		}
		normalized = append(normalized, domain.QuoteLine{
			ArticleCode: line.ArticleCode,
			Name:        name,
			Quantity:    line.Quantity,
			UnitPriceRD: sku.UnitPriceRD,
		})
	}
	return normalized, 0, nil
}

func (s *Service) resolveDepositPercent(ctx context.Context) int {
	s.mu.RLock()
	provider := s.depositProvider
	s.mu.RUnlock()
	if provider == nil {
		return 30
	}
	pct := provider.CurrentDepositPercent(ctx)
	if pct <= 0 {
		return 30
	}
	if pct > 100 {
		return 30
	}
	return pct
}

func (s *Service) buildAndStoreCreatedQuote(
	ctx context.Context,
	cmd CreateQuoteCommand,
	caller platformhttp.Principal,
	total, depositPercent, deposit, balance int,
	now time.Time,
) domain.Quote {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.syncFromStoreLocked(ctx)
	id, code := s.allocateUniqueQuoteIdentifiersLocked(now)
	clientID, clientEmail, clientName := resolveQuoteClientIdentity(cmd, caller)
	initialStatus := domain.QuoteStatusPending
	isAdminCaller := !caller.IsClient()
	shouldSendNow := cmd.SendImmediately && isAdminCaller
	if shouldSendNow {
		initialStatus = domain.QuoteStatusSent
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
		EventDateISO:   strings.TrimSpace(cmd.EventDateISO),
		PackageID:      strings.TrimSpace(cmd.PackageID),
		Venue:          strings.TrimSpace(cmd.Venue),
		Guests:         cmd.Guests,
		Lines:          cmd.Lines,
		DeliveryFeeRD:  cmd.DeliveryFeeRD,
		DiscountRD:     cmd.DiscountRD,
		TotalRD:        total,
		DepositPercent: depositPercent,
		DepositRD:      deposit,
		BalanceRD:      balance,
		Status:         initialStatus,
		Notes:          strings.TrimSpace(cmd.Notes),
		ContactPhone:   strings.TrimSpace(cmd.ContactPhone),
	}
	s.quotes[id] = quote
	s.order = append(s.order, id)
	return quote
}

func resolveQuoteClientIdentity(cmd CreateQuoteCommand, caller platformhttp.Principal) (string, string, string) {
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
	return clientID, clientEmail, clientName
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
	now := s.clock.Now()
	store := s.currentStore()
	if isQuoteStoreTxEnabled(store) {
		signer := store.(TransactionalSigner)
		return s.signContractTransactional(ctx, signer, id, cleanSigner, signatureSVG, caller, now)
	}
	quote, err := s.GetQuoteForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Quote{}, err
	}
	signedQuote, err := s.applyAndProvisionQuoteSign(ctx, quote, cleanSigner, signatureSVG, caller, now)
	if err != nil {
		return domain.Quote{}, err
	}
	if err := s.persistSignedQuote(ctx, signedQuote, now); err != nil {
		return domain.Quote{}, err
	}
	s.dispatchSignedQuoteNotifications(ctx, signedQuote)
	return signedQuote, nil
}

func isQuoteStoreTxEnabled(store Store) bool {
	if store == nil {
		return false
	}
	if !store.Enabled() {
		return false
	}
	_, ok := store.(TransactionalSigner)
	return ok
}

func (s *Service) signContractTransactional(
	ctx context.Context,
	signer TransactionalSigner,
	id string,
	cleanSigner string,
	signatureSVG string,
	caller platformhttp.Principal,
	now time.Time,
) (domain.Quote, error) {
	callerID := callerIDString(caller)
	signedQuote, err := signer.SignQuoteTx(ctx, id, now, func(existing domain.Quote) (domain.Quote, error) {
		if caller.IsClient() {
			if !existing.BelongsToClient(callerID, caller.Email, caller.FullName) {
				return domain.Quote{}, domain.ErrQuoteForbidden
			}
		}
		return s.applyAndProvisionQuoteSign(ctx, existing, cleanSigner, signatureSVG, caller, now)
	})
	if err != nil {
		return domain.Quote{}, err
	}
	s.mu.Lock()
	s.quotes[signedQuote.ID] = signedQuote
	s.mu.Unlock()
	s.dispatchSignedQuoteNotifications(ctx, signedQuote)
	return signedQuote, nil
}

func (s *Service) applyAndProvisionQuoteSign(
	ctx context.Context,
	quote domain.Quote,
	cleanSigner string,
	signatureSVG string,
	caller platformhttp.Principal,
	now time.Time,
) (domain.Quote, error) {
	if quote.Status != domain.QuoteStatusSent {
		return domain.Quote{}, domain.ErrQuoteNotSent
	}
	if err := s.checkQuoteStockAvailability(ctx, quote.EventDateISO, quote.EventDateLabel, quote.EventID, quote.Lines); err != nil {
		return domain.Quote{}, err
	}
	quote.Status = domain.QuoteStatusSigned
	quote.SignedByName = cleanSigner
	if caller.ID != uuid.Nil {
		quote.SignedByUserID = caller.ID.String()
	}
	quote.SignatureSVG = signatureSVG
	quote.SignedAtISO = now.UTC().Format(time.RFC3339)
	quote.ContractPDFURL = "/v1/quotes/" + quote.ID + "/contract"
	quote.ContractHashSHA256 = domain.ComputeQuoteContractHash(quote)
	if err := s.provisionEventForSignedQuote(ctx, &quote); err != nil {
		return domain.Quote{}, err
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
	if quote.Status == domain.QuoteStatusSigned {
		return domain.Quote{}, domain.ErrQuoteAlreadyDecided
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
			if err := store.UpsertQuote(ctx, quote, now); err != nil {
				return domain.Quote{}, err
			}
		}
	}
	return quote, nil
}

// SendQuoteForAdmin enriches a pending/declined quote with final items, delivery, discount and notes,
// and marks it as sent/approved so the client can review, accept and sign it.
func (s *Service) SendQuoteForAdmin(
	ctx context.Context,
	id string,
	cmd EnrichAndSendQuoteCommand,
	caller platformhttp.Principal,
) (domain.Quote, error) {
	if caller.IsClient() {
		return domain.Quote{}, domain.ErrQuoteForbidden
	}
	quote, err := s.GetQuoteForCaller(ctx, id, caller, true)
	if err != nil {
		return domain.Quote{}, err
	}
	if quote.Status == domain.QuoteStatusSigned {
		return domain.Quote{}, domain.ErrQuoteAlreadyDecided
	}
	enriched := applyQuoteEnrichment(quote, cmd)
	depositPct := s.resolveDepositPercent(ctx)
	total, deposit, balance, err := domain.CalculateQuoteTotalsWithDeposit(enriched.Lines, enriched.DeliveryFeeRD, enriched.DiscountRD, depositPct)
	if err != nil {
		return domain.Quote{}, err
	}
	if err := s.checkQuoteStockAvailability(ctx, enriched.EventDateISO, enriched.EventDateLabel, enriched.EventID, enriched.Lines); err != nil {
		return domain.Quote{}, err
	}
	enriched.TotalRD = total
	enriched.DepositPercent = depositPct
	enriched.DepositRD = deposit
	enriched.BalanceRD = balance
	enriched.Status = domain.QuoteStatusSent
	enriched.RejectionReason = ""

	now := s.clock.Now()
	s.mu.Lock()
	s.quotes[id] = enriched
	store := s.store
	sentNotif := s.quoteSentNotif
	pub := s.realtimePub
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if err := store.UpsertQuote(ctx, enriched, now); err != nil {
				return domain.Quote{}, err
			}
		}
	}
	if sentNotif != nil {
		sentNotif.NotifyQuoteSent(ctx, enriched)
	}
	publishQuoteSentEvent(pub, enriched)
	return enriched, nil
}

func applyQuoteEnrichment(quote domain.Quote, cmd EnrichAndSendQuoteCommand) domain.Quote {
	if trimmedTitle := strings.TrimSpace(cmd.EventTitle); trimmedTitle != "" {
		quote.EventTitle = trimmedTitle
	}
	if trimmedVenue := strings.TrimSpace(cmd.Venue); trimmedVenue != "" {
		quote.Venue = trimmedVenue
	}
	if trimmedDateLabel := strings.TrimSpace(cmd.EventDateLabel); trimmedDateLabel != "" {
		quote.EventDateLabel = trimmedDateLabel
	}
	if trimmedDateISO := strings.TrimSpace(cmd.EventDateISO); trimmedDateISO != "" {
		quote.EventDateISO = trimmedDateISO
	}
	if cmd.Guests > 0 {
		quote.Guests = cmd.Guests
	}
	if len(cmd.Lines) > 0 {
		quote.Lines = cmd.Lines
	}
	if cmd.DeliveryFeeRD != nil {
		quote.DeliveryFeeRD = *cmd.DeliveryFeeRD
	}
	if cmd.DiscountRD != nil {
		quote.DiscountRD = *cmd.DiscountRD
	}
	if trimmedNotes := strings.TrimSpace(cmd.Notes); trimmedNotes != "" {
		quote.Notes = trimmedNotes
	}
	return quote
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
) (domain.RosaChatResponse, error) {
	s.mu.RLock()
	cfg := s.aiConfig
	client := s.httpClient
	limiter := s.rateLimiter
	s.mu.RUnlock()

	if cfg.AIAPIKey == "" {
		return domain.RosaChatResponse{}, domain.ErrAIProviderNotConfigured
	}
	if limiter != nil {
		allowed := limiter.allow(callerRateLimitKey(ctx, caller), s.clock.Now(), cfg.RateLimitMax, cfg.RateLimitWindow)
		if !allowed {
			return domain.RosaChatResponse{}, domain.ErrAIRateLimited
		}
	}
	skus := s.loadLiveCatalogSKUs(ctx)
	var snap domain.RosaAdminSnapshot
	if caller.IsAdminOrStaff() {
		snap = s.buildAdminSnapshot(ctx)
	}
	systemPrompt := buildRosaSystemPrompt(caller.IsAdminOrStaff(), req, skus, snap)
	reply, err := callAIChatProvider(ctx, client, cfg, systemPrompt, message)
	if err != nil {
		return domain.RosaChatResponse{}, err
	}
	return domain.RosaChatResponse{
		Reply:          reply.Reply,
		Intent:         inferChatIntent(message, reply.SuggestedItems),
		SuggestedItems: reply.SuggestedItems,
		Provider:       reply.Provider,
		Model:          reply.Model,
	}, nil
}

func (s *Service) GenerateRosaSketch(
	ctx context.Context,
	prompt string,
	eventType string,
	guests int,
	caller platformhttp.Principal,
) (domain.RosaSketchResponse, error) {
	s.mu.RLock()
	cfg := s.aiConfig
	client := s.httpClient
	limiter := s.rateLimiter
	s.mu.RUnlock()

	if cfg.AIAPIKey == "" {
		return domain.RosaSketchResponse{}, domain.ErrAIProviderNotConfigured
	}
	if limiter != nil {
		allowed := limiter.allow(callerRateLimitKey(ctx, caller), s.clock.Now(), cfg.RateLimitMax, cfg.RateLimitWindow)
		if !allowed {
			return domain.RosaSketchResponse{}, domain.ErrAIRateLimited
		}
	}
	return callAISketchProvider(ctx, client, cfg, prompt, eventType, guests)
}

// TranscribeAudio analiza y transcribe una nota de voz sin contexto de llamador explícito.
func (s *Service) TranscribeAudio(ctx context.Context, cmd TranscribeCommand) (domain.TranscriptionResult, error) {
	return s.TranscribeAudioForCaller(ctx, cmd, platformhttp.Principal{})
}

// TranscribeAudioForCaller aplica rate limiting y transcribe una nota de voz con el proveedor STT configurado.
func (s *Service) TranscribeAudioForCaller(
	ctx context.Context,
	cmd TranscribeCommand,
	caller platformhttp.Principal,
) (domain.TranscriptionResult, error) {
	s.mu.RLock()
	cfg := s.aiConfig
	client := s.httpClient
	limiter := s.rateLimiter
	s.mu.RUnlock()

	if limiter != nil {
		allowed := limiter.allow(callerRateLimitKey(ctx, caller), s.clock.Now(), cfg.RateLimitMax, cfg.RateLimitWindow)
		if !allowed {
			return domain.TranscriptionResult{}, domain.ErrAIRateLimited
		}
	}
	analysis, err := domain.AnalyzeBase64Audio(cmd.AudioBase64)
	if err != nil {
		return domain.TranscriptionResult{}, err
	}
	if analysis.DurationMs > defaultMaxAudioDurationMs {
		return domain.TranscriptionResult{}, domain.ErrAudioTooLong
	}
	locale := strings.TrimSpace(cmd.Locale)
	if locale == "" {
		locale = "es-DO"
	}

	transcript, provider, err := callSTTProvider(ctx, client, cfg, cmd, analysis, locale)
	if err != nil {
		return domain.TranscriptionResult{}, err
	}
	return domain.TranscriptionResult{
		Transcript:    transcript,
		Locale:        locale,
		DurationMs:    analysis.DurationMs,
		SampleRateHz:  analysis.SampleRateHz,
		RMSEnergy:     analysis.RMSEnergy,
		VoiceDetected: analysis.VoiceDetected,
		Provider:      provider,
	}, nil
}

// BuildContractPDFForCaller genera el contrato PDF de la cotización si el llamador tiene acceso.
func (s *Service) BuildContractPDFForCaller(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
	authenticated bool,
) ([]byte, string, error) {
	quote, err := s.GetQuoteForCaller(ctx, id, caller, authenticated)
	if err != nil {
		return nil, "", err
	}
	pdfBytes := domain.BuildQuoteContractPDF(quote)
	filename := fmt.Sprintf("contrato-%s.pdf", strings.ToLower(strings.TrimSpace(quote.Code)))
	if filename == "contrato-.pdf" {
		filename = "contrato-" + quote.ID + ".pdf"
	}
	return pdfBytes, filename, nil
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
	out := make([]domain.CatalogSKU, 0, len(liveArticles))
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
	if merged.EventDateISO == "" {
		merged.EventDateISO = existing.EventDateISO
	}
	if merged.PackageID == "" {
		merged.PackageID = existing.PackageID
	}
	if merged.SignedAtISO == "" {
		merged.SignedAtISO = existing.SignedAtISO
	}
	if merged.ContractPDFURL == "" {
		merged.ContractPDFURL = existing.ContractPDFURL
	}
	return merged
}

func (s *Service) currentStore() Store {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	return store
}

func (s *Service) checkQuoteStockAvailability(
	ctx context.Context,
	eventDateISO string,
	eventDateLabel string,
	excludeEventID string,
	lines []domain.QuoteLine,
) error {
	s.mu.RLock()
	checker := s.stockChecker
	s.mu.RUnlock()
	if checker == nil {
		return nil
	}
	rawDate := strings.TrimSpace(eventDateISO)
	if rawDate == "" {
		rawDate = strings.TrimSpace(eventDateLabel)
	}
	if err := checker.CheckQuoteAvailability(ctx, rawDate, strings.TrimSpace(excludeEventID), lines); err != nil {
		if errors.Is(err, domain.ErrInsufficientStock) {
			return err
		}
		return fmt.Errorf("%w: %w", domain.ErrInsufficientStock, err)
	}
	return nil
}

func (s *Service) provisionEventForSignedQuote(ctx context.Context, quote *domain.Quote) error {
	s.mu.RLock()
	provisioner := s.eventProvisioner
	s.mu.RUnlock()
	if provisioner == nil {
		return nil
	}
	eventID, err := provisioner.ProvisionEventFromQuote(ctx, *quote)
	if err != nil {
		if errors.Is(err, domain.ErrInsufficientStock) {
			return err
		}
		if strings.Contains(strings.ToLower(err.Error()), "stock insuficiente") {
			return fmt.Errorf("%w: %w", domain.ErrInsufficientStock, err)
		}
		return err
	}
	if strings.TrimSpace(eventID) != "" {
		quote.EventID = strings.TrimSpace(eventID)
	}
	return nil
}

func (s *Service) persistSignedQuote(ctx context.Context, quote domain.Quote, now time.Time) error {
	s.mu.Lock()
	s.quotes[quote.ID] = quote
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			return store.UpsertQuote(ctx, quote, now)
		}
	}
	return nil
}

func (s *Service) dispatchSignedQuoteNotifications(ctx context.Context, quote domain.Quote) {
	s.mu.RLock()
	notifier := s.adminNotifier
	sender := s.emailSender
	prefChecker := s.emailPrefChecker
	pub := s.realtimePub
	s.mu.RUnlock()

	if notifier != nil {
		_ = notifier.NotifyQuoteSigned(ctx, quote)
	}
	allowEmail := true
	if prefChecker != nil {
		allowEmail = prefChecker.AllowsEmailReceipts(ctx, quote.ClientID, quote.ClientEmail)
	}
	if allowEmail {
		sendQuoteSignedEmail(ctx, sender, quote)
	}
	publishQuoteSignedEvent(pub, quote)
}

func sendQuoteSignedEmail(ctx context.Context, sender mailer.Sender, quote domain.Quote) {
	if sender == nil {
		return
	}
	if !sender.IsConfigured() {
		return
	}
	targetEmail := strings.TrimSpace(quote.ClientEmail)
	if targetEmail == "" {
		return
	}
	depositPct := quote.DepositPercent
	if depositPct <= 0 {
		depositPct = 30
	}
	subject := fmt.Sprintf("Contrato firmado - %s (%s)", quote.EventTitle, quote.Code)
	body := fmt.Sprintf("Hola %s,\n\nTu contrato (%s) para \"%s\" ha sido firmado correctamente.\nTotal: RD$%d · Anticipo requerido (%d%%): RD$%d.\n",
		quote.ClientName, quote.Code, quote.EventTitle, quote.TotalRD, depositPct, quote.DepositRD)
	_ = sender.SendEmail(ctx, targetEmail, subject, body)
}

func publishQuoteSentEvent(pub realtimedomain.Publisher, quote domain.Quote) {
	if pub == nil {
		return
	}
	audience := realtimedomain.AudienceAdmin
	if quote.ClientID != "" {
		audience = realtimedomain.AudienceClient
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     realtimedomain.EventQuoteSent,
		Audience: audience,
		ClientID: quote.ClientID,
		EntityID: quote.ID,
		Summary:  "Cotización lista para firma: " + quote.EventTitle,
		Payload: map[string]any{
			"quoteId": quote.ID,
			"eventId": quote.EventID,
			"totalRD": quote.TotalRD,
		},
	})
}

func publishQuoteSignedEvent(pub realtimedomain.Publisher, quote domain.Quote) {
	if pub == nil {
		return
	}
	audience := realtimedomain.AudienceAdmin
	if quote.ClientID != "" {
		audience = realtimedomain.AudienceClient
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     realtimedomain.EventQuoteSigned,
		Audience: audience,
		ClientID: quote.ClientID,
		EntityID: quote.ID,
		Payload: map[string]any{
			"quoteId":        quote.ID,
			"eventId":        quote.EventID,
			"contractPdfUrl": quote.ContractPDFURL,
			"signedAtIso":    quote.SignedAtISO,
		},
	})
}

func callerIDString(caller platformhttp.Principal) string {
	if caller.ID == uuid.Nil {
		return ""
	}
	return caller.ID.String()
}
