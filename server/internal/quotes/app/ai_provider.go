package app

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"net/textproto"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

const (
	defaultMaxAudioDurationMs = 25000
	defaultRateLimitMax       = 20
	defaultRateLimitWindow    = time.Minute
	defaultAITimeout          = 25 * time.Second
)

// AIProviderConfig define las credenciales y endpoints de los proveedores reales de Rosa IA y STT.
type AIProviderConfig struct {
	AIProvider      string
	AIModel         string
	AIAPIKey        string
	AIBaseURL       string
	STTProvider     string
	STTModel        string
	STTAPIURL       string
	STTAPIKey       string
	RateLimitMax    int
	RateLimitWindow time.Duration
}

type slidingWindowLimiter struct {
	mu        sync.Mutex
	lastSweep time.Time
	history   map[string][]time.Time
}

func newSlidingWindowLimiter() *slidingWindowLimiter {
	return &slidingWindowLimiter{
		history: make(map[string][]time.Time),
	}
}

func (l *slidingWindowLimiter) allow(callerKey string, now time.Time, maxCalls int, window time.Duration) bool {
	if maxCalls <= 0 {
		maxCalls = defaultRateLimitMax
	}
	if window <= 0 {
		window = defaultRateLimitWindow
	}
	cutoff := now.Add(-window)

	l.mu.Lock()
	defer l.mu.Unlock()
	l.evictExpiredLocked(now, cutoff, window)
	kept := filterRecentTimestamps(l.history[callerKey], cutoff)
	if len(kept) >= maxCalls {
		l.history[callerKey] = kept
		return false
	}
	l.history[callerKey] = append(kept, now)
	return true
}

func (l *slidingWindowLimiter) evictExpiredLocked(now, cutoff time.Time, window time.Duration) {
	shouldSweep := l.lastSweep.IsZero()
	if !shouldSweep {
		shouldSweep = !now.Before(l.lastSweep.Add(window))
	}
	if !shouldSweep {
		return
	}
	for key, stamps := range l.history {
		kept := filterRecentTimestamps(stamps, cutoff)
		if len(kept) == 0 {
			delete(l.history, key)
			continue
		}
		l.history[key] = kept
	}
	l.lastSweep = now
}

func filterRecentTimestamps(stamps []time.Time, cutoff time.Time) []time.Time {
	kept := make([]time.Time, 0, len(stamps))
	for _, stamped := range stamps {
		if !stamped.Before(cutoff) {
			kept = append(kept, stamped)
		}
	}
	return kept
}

func normalizeAIProviderConfig(cfg AIProviderConfig) AIProviderConfig {
	cfg.AIProvider = strings.ToLower(strings.TrimSpace(cfg.AIProvider))
	if cfg.AIProvider == "" {
		cfg.AIProvider = "gemini"
	}
	cfg.AIModel = strings.TrimSpace(cfg.AIModel)
	if cfg.AIModel == "" {
		cfg.AIModel = defaultAIModelFor(cfg.AIProvider)
	}
	cfg.AIAPIKey = strings.TrimSpace(cfg.AIAPIKey)
	cfg.AIBaseURL = strings.TrimRight(strings.TrimSpace(cfg.AIBaseURL), "/")
	cfg.STTProvider = strings.ToLower(strings.TrimSpace(cfg.STTProvider))
	if cfg.STTProvider == "" {
		cfg.STTProvider = cfg.AIProvider
	}
	cfg.STTModel = strings.TrimSpace(cfg.STTModel)
	if cfg.STTModel == "" {
		cfg.STTModel = defaultSTTModelFor(cfg.STTProvider)
	}
	cfg.STTAPIURL = strings.TrimSpace(cfg.STTAPIURL)
	cfg.STTAPIKey = strings.TrimSpace(cfg.STTAPIKey)
	return cfg
}

func defaultAIModelFor(provider string) string {
	switch provider {
	case "groq":
		return "llama-3.3-70b-versatile"
	case "openai":
		return "gpt-4o-mini"
	default:
		return "gemini-2.5-flash-lite"
	}
}

func defaultSTTModelFor(provider string) string {
	switch provider {
	case "groq":
		return "whisper-large-v3-turbo"
	case "openai":
		return "gpt-4o-mini-transcribe"
	default:
		return "gemini-2.5-flash-lite"
	}
}

func callerRateLimitKey(ctx context.Context, caller platformhttp.Principal) string {
	if caller.ID != uuid.Nil {
		return caller.ID.String()
	}
	email := strings.TrimSpace(caller.Email)
	if email != "" {
		return strings.ToLower(email)
	}
	if clientIP := strings.TrimSpace(platformhttp.ClientIP(ctx)); clientIP != "" {
		return "ip:" + clientIP
	}
	return "anonymous"
}

func buildRosaSystemPrompt(
	isAdmin bool,
	req domain.RosaProposalRequest,
	skus []domain.CatalogSKU,
	snap domain.RosaAdminSnapshot,
) string {
	var builder strings.Builder
	builder.WriteString("Eres Rosa, asesora de eventos y alquileres de RosaFiesta (República Dominicana). ")
	builder.WriteString("Responde siempre en español claro y amable usando EXCLUSIVAMENTE los artículos del catálogo real listado abajo. ")
	builder.WriteString(`Devuelve SOLO un JSON válido con el esquema: {"reply":"texto","suggestedItems":[{"articleCode":"RF-001","name":"Nombre","quantity":10}]}.`)
	builder.WriteString("\nCatálogo disponible:\n")
	for _, item := range skus {
		fmt.Fprintf(&builder, "- %s (%s): RD$%d/día, disponibles=%d\n", item.Code, item.Name, item.UnitPriceRD, item.AvailableStock)
	}
	if req.EventType != "" {
		fmt.Fprintf(&builder, "Evento: %s, invitados=%d, presupuestoRD=%d\n", req.EventType, req.Guests, req.BudgetRD)
	}
	if isAdmin {
		fmt.Fprintf(&builder, "Resumen operativo: cotizacionesPendientes=%d, firmadas=%d, bajoStock=%s\n",
			snap.PendingQuotesCount, snap.SignedQuotesCount, strings.Join(snap.LowStockItems, ", "))
	}
	return builder.String()
}

func callAIChatProvider(
	ctx context.Context,
	client *http.Client,
	cfg AIProviderConfig,
	systemPrompt string,
	userMessage string,
) (domain.RosaChatReply, error) {
	if cfg.AIAPIKey == "" {
		return domain.RosaChatReply{}, domain.ErrAIProviderNotConfigured
	}
	switch cfg.AIProvider {
	case "groq":
		baseURL := defaultURL(cfg.AIBaseURL, "https://api.groq.com")
		endpoint := baseURL + "/openai/v1/chat/completions"
		return callOpenAICompatChat(ctx, client, endpoint, cfg.AIProvider, cfg.AIModel, cfg.AIAPIKey, systemPrompt, userMessage)
	case "openai":
		baseURL := defaultURL(cfg.AIBaseURL, "https://api.openai.com")
		endpoint := baseURL + "/v1/chat/completions"
		return callOpenAICompatChat(ctx, client, endpoint, cfg.AIProvider, cfg.AIModel, cfg.AIAPIKey, systemPrompt, userMessage)
	default:
		baseURL := defaultURL(cfg.AIBaseURL, "https://generativelanguage.googleapis.com")
		return callGeminiChat(ctx, client, baseURL, cfg.AIModel, cfg.AIAPIKey, systemPrompt, userMessage)
	}
}

func callGeminiChat(
	ctx context.Context,
	client *http.Client,
	baseURL string,
	model string,
	apiKey string,
	systemPrompt string,
	userMessage string,
) (domain.RosaChatReply, error) {
	endpoint := fmt.Sprintf("%s/v1beta/models/%s:generateContent?key=%s", baseURL, model, apiKey)
	body := map[string]any{
		"systemInstruction": map[string]any{
			"parts": []map[string]string{{"text": systemPrompt}},
		},
		"contents": []map[string]any{
			{"role": "user", "parts": []map[string]string{{"text": userMessage}}},
		},
		"generationConfig": map[string]string{
			"responseMimeType": "application/json",
		},
	}
	respBytes, err := postJSON(ctx, client, endpoint, "", body)
	if err != nil {
		return domain.RosaChatReply{}, err
	}
	rawText, err := extractGeminiText(respBytes)
	if err != nil {
		return domain.RosaChatReply{}, err
	}
	return parseStructuredRosaReply(rawText, "gemini", model), nil
}

func callOpenAICompatRawText(
	ctx context.Context,
	client *http.Client,
	endpoint string,
	provider string,
	model string,
	apiKey string,
	systemPrompt string,
	userMessage string,
) (string, error) {
	body := map[string]any{
		"model": model,
		"messages": []map[string]string{
			{"role": "system", "content": systemPrompt},
			{"role": "user", "content": userMessage},
		},
	}
	respBytes, err := postJSON(ctx, client, endpoint, apiKey, body)
	if err != nil {
		return "", err
	}
	var parsed struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
		} `json:"choices"`
	}
	if err := json.Unmarshal(respBytes, &parsed); err != nil {
		return "", err
	}
	if len(parsed.Choices) == 0 {
		return "", fmt.Errorf("empty %s completion response", provider)
	}
	return parsed.Choices[0].Message.Content, nil
}

func callOpenAICompatChat(
	ctx context.Context,
	client *http.Client,
	endpoint string,
	provider string,
	model string,
	apiKey string,
	systemPrompt string,
	userMessage string,
) (domain.RosaChatReply, error) {
	rawText, err := callOpenAICompatRawText(ctx, client, endpoint, provider, model, apiKey, systemPrompt, userMessage)
	if err != nil {
		return domain.RosaChatReply{}, err
	}
	return parseStructuredRosaReply(rawText, provider, model), nil
}

func extractGeminiText(raw []byte) (string, error) {
	var parsed struct {
		Candidates []struct {
			Content struct {
				Parts []struct {
					Text string `json:"text"`
				} `json:"parts"`
			} `json:"content"`
		} `json:"candidates"`
	}
	if err := json.Unmarshal(raw, &parsed); err != nil {
		return "", err
	}
	if len(parsed.Candidates) == 0 {
		return "", fmt.Errorf("empty gemini candidates")
	}
	parts := parsed.Candidates[0].Content.Parts
	if len(parts) == 0 {
		return "", fmt.Errorf("empty gemini parts")
	}
	return parts[0].Text, nil
}

func parseStructuredRosaReply(rawText, provider, model string) domain.RosaChatReply {
	cleaned := stripMarkdownJSONFence(rawText)
	var structured struct {
		Reply          string                     `json:"reply"`
		SuggestedItems []domain.RosaSuggestedItem `json:"suggestedItems"`
	}
	if err := json.Unmarshal([]byte(cleaned), &structured); err == nil {
		trimmedReply := strings.TrimSpace(structured.Reply)
		if trimmedReply != "" {
			return domain.RosaChatReply{
				Reply:          trimmedReply,
				SuggestedItems: structured.SuggestedItems,
				Provider:       provider,
				Model:          model,
			}
		}
	}
	return domain.RosaChatReply{
		Reply:    strings.TrimSpace(rawText),
		Provider: provider,
		Model:    model,
	}
}

func stripMarkdownJSONFence(raw string) string {
	trimmed := strings.TrimSpace(raw)
	if !strings.HasPrefix(trimmed, "```") {
		return trimmed
	}
	newlineIdx := strings.IndexByte(trimmed, '\n')
	if newlineIdx > 0 {
		trimmed = trimmed[newlineIdx+1:]
	}
	trimmed = strings.TrimSuffix(trimmed, "```")
	return strings.TrimSpace(trimmed)
}

func callAISketchProvider(
	ctx context.Context,
	client *http.Client,
	cfg AIProviderConfig,
	prompt string,
	eventType string,
	guests int,
) (domain.RosaSketchResponse, error) {
	effectiveGuests := guests
	if effectiveGuests <= 0 {
		effectiveGuests = 60
	}
	systemPrompt := buildRosaSketchSystemPrompt(eventType, effectiveGuests)
	userMessage := strings.TrimSpace(prompt)
	if userMessage == "" {
		userMessage = fmt.Sprintf("Organiza distribución elegante para %d invitados (%s).", effectiveGuests, eventType)
	}
	rawJSON, err := requestAIJSONText(ctx, client, cfg, systemPrompt, userMessage)
	if err != nil {
		return domain.RosaSketchResponse{}, err
	}
	return parseStructuredSketchResponse(rawJSON, cfg.AIProvider, cfg.AIModel), nil
}

func buildRosaSketchSystemPrompt(eventType string, guests int) string {
	return fmt.Sprintf(
		"Eres Rosa IA diseñadora espacial de eventos en República Dominicana. "+
			"Responde EXCLUSIVAMENTE JSON válido con llaves: "+
			`{"reply":"texto breve","eventWidthMeters":number,"eventDepthMeters":number,"elements":[{"id":"el-1","kind":"mesa|arco|dulces|iluminacion|escenario","label":"nombre","xRatio":0.5,"yRatio":0.5,"capacity":8}]}. `+
			"xRatio y yRatio deben estar entre 0.08 y 0.92. Evento=%s, invitados=%d.",
		strings.TrimSpace(eventType), guests,
	)
}

func requestAIJSONText(
	ctx context.Context,
	client *http.Client,
	cfg AIProviderConfig,
	systemPrompt string,
	userMessage string,
) (string, error) {
	switch cfg.AIProvider {
	case "groq":
		endpoint := defaultURL(cfg.AIBaseURL, "https://api.groq.com") + "/openai/v1/chat/completions"
		return callOpenAICompatRawText(ctx, client, endpoint, cfg.AIProvider, cfg.AIModel, cfg.AIAPIKey, systemPrompt, userMessage)
	case "openai":
		endpoint := defaultURL(cfg.AIBaseURL, "https://api.openai.com") + "/v1/chat/completions"
		return callOpenAICompatRawText(ctx, client, endpoint, cfg.AIProvider, cfg.AIModel, cfg.AIAPIKey, systemPrompt, userMessage)
	default:
		baseURL := defaultURL(cfg.AIBaseURL, "https://generativelanguage.googleapis.com")
		endpoint := fmt.Sprintf("%s/v1beta/models/%s:generateContent?key=%s", baseURL, cfg.AIModel, cfg.AIAPIKey)
		body := map[string]any{
			"systemInstruction": map[string]any{"parts": []map[string]string{{"text": systemPrompt}}},
			"contents":          []map[string]any{{"role": "user", "parts": []map[string]string{{"text": userMessage}}}},
			"generationConfig":  map[string]string{"responseMimeType": "application/json"},
		}
		respBytes, err := postJSON(ctx, client, endpoint, "", body)
		if err != nil {
			return "", err
		}
		return extractGeminiText(respBytes)
	}
}

func parseStructuredSketchResponse(rawText, provider, model string) domain.RosaSketchResponse {
	cleaned := stripMarkdownJSONFence(rawText)
	var parsed domain.RosaSketchResponse
	if err := json.Unmarshal([]byte(cleaned), &parsed); err == nil {
		if len(parsed.Elements) > 0 {
			parsed.Provider = provider
			parsed.Model = model
			if parsed.EventWidthM <= 0 {
				parsed.EventWidthM = 12
			}
			if parsed.EventDepthM <= 0 {
				parsed.EventDepthM = 10
			}
			return parsed
		}
	}
	return domain.RosaSketchResponse{
		Reply:       "Propuesta base aplicada según capacidad del salón.",
		EventWidthM: 12,
		EventDepthM: 10,
		Elements:    []domain.RosaSketchElement{},
		Provider:    provider,
		Model:       model,
	}
}

func inferChatIntent(message string, suggested []domain.RosaSuggestedItem) string {
	if len(suggested) > 0 {
		return "catalog_recommendation"
	}
	lower := strings.ToLower(message)
	if strings.Contains(lower, "cotiz") {
		return "quote_assistance"
	}
	return "assistant_chat"
}

func callSTTProvider(
	ctx context.Context,
	client *http.Client,
	cfg AIProviderConfig,
	cmd TranscribeCommand,
	analysis domain.AudioAnalysis,
	locale string,
) (string, string, error) {
	if cfg.STTAPIURL != "" {
		text, err := callCustomJSONSTT(ctx, client, cfg.STTAPIURL, sttAuthToken(cfg), cmd, locale)
		return text, "external_stt", err
	}
	if cfg.STTAPIKey == "" {
		return "", "", domain.ErrSTTProviderNotConfigured
	}
	switch cfg.STTProvider {
	case "groq":
		baseURL := defaultURL(cfg.AIBaseURL, "https://api.groq.com")
		text, err := callWhisperMultipartSTT(ctx, client, baseURL+"/openai/v1/audio/transcriptions", cfg.STTModel, cfg.STTAPIKey, cmd, analysis, locale)
		return text, "groq", err
	case "openai":
		baseURL := defaultURL(cfg.AIBaseURL, "https://api.openai.com")
		text, err := callWhisperMultipartSTT(ctx, client, baseURL+"/v1/audio/transcriptions", cfg.STTModel, cfg.STTAPIKey, cmd, analysis, locale)
		return text, "openai", err
	default:
		baseURL := defaultURL(cfg.AIBaseURL, "https://generativelanguage.googleapis.com")
		text, err := callGeminiAudioSTT(ctx, client, baseURL, cfg.STTModel, cfg.STTAPIKey, cmd, analysis, locale)
		return text, "gemini", err
	}
}

func sttAuthToken(cfg AIProviderConfig) string {
	if cfg.STTAPIKey != "" {
		return cfg.STTAPIKey
	}
	return cfg.AIAPIKey
}

func callGeminiAudioSTT(
	ctx context.Context,
	client *http.Client,
	baseURL string,
	model string,
	apiKey string,
	cmd TranscribeCommand,
	analysis domain.AudioAnalysis,
	locale string,
) (string, error) {
	endpoint := fmt.Sprintf("%s/v1beta/models/%s:generateContent?key=%s", baseURL, model, apiKey)
	mimeType, _ := resolveAudioFormat(cmd.MimeType)
	instruction := fmt.Sprintf("Transcribe literalmente este audio en idioma %s sin agregar explicaciones.", locale)
	if strings.TrimSpace(cmd.PromptHint) != "" {
		instruction = instruction + " Contexto: " + strings.TrimSpace(cmd.PromptHint)
	}
	encoded := base64.StdEncoding.EncodeToString(analysis.RawBytes)
	body := map[string]any{
		"contents": []map[string]any{
			{
				"role": "user",
				"parts": []map[string]any{
					{"text": instruction},
					{"inlineData": map[string]string{"mimeType": mimeType, "data": encoded}},
				},
			},
		},
	}
	respBytes, err := postJSON(ctx, client, endpoint, "", body)
	if err != nil {
		return "", err
	}
	rawText, err := extractGeminiText(respBytes)
	if err != nil {
		return "", err
	}
	return strings.TrimSpace(rawText), nil
}

func callWhisperMultipartSTT(
	ctx context.Context,
	client *http.Client,
	endpoint string,
	model string,
	apiKey string,
	cmd TranscribeCommand,
	analysis domain.AudioAnalysis,
	locale string,
) (string, error) {
	var bodyBuf bytes.Buffer
	writer := multipart.NewWriter(&bodyBuf)
	mimeType, ext := resolveAudioFormat(cmd.MimeType)
	if err := writeMultipartAudioFile(writer, "audio."+ext, mimeType, analysis.RawBytes); err != nil {
		return "", err
	}
	_ = writer.WriteField("model", model)
	_ = writer.WriteField("language", shortLanguageCode(locale))
	if strings.TrimSpace(cmd.PromptHint) != "" {
		_ = writer.WriteField("prompt", strings.TrimSpace(cmd.PromptHint))
	}
	_ = writer.Close()

	reqCtx, cancel := context.WithTimeout(ctx, defaultAITimeout)
	defer cancel()
	httpReq, err := http.NewRequestWithContext(reqCtx, http.MethodPost, endpoint, &bodyBuf)
	if err != nil {
		return "", err
	}
	httpReq.Header.Set("Content-Type", writer.FormDataContentType())
	httpReq.Header.Set("Authorization", "Bearer "+apiKey)
	respBytes, err := executeHTTPRequest(client, httpReq)
	if err != nil {
		return "", err
	}
	var parsed struct {
		Text string `json:"text"`
	}
	if err := json.Unmarshal(respBytes, &parsed); err != nil {
		return "", err
	}
	return strings.TrimSpace(parsed.Text), nil
}

func writeMultipartAudioFile(writer *multipart.Writer, fileName, mimeType string, content []byte) error {
	header := make(textproto.MIMEHeader)
	header.Set("Content-Disposition", fmt.Sprintf(`form-data; name="file"; filename="%s"`, fileName))
	header.Set("Content-Type", mimeType)
	part, err := writer.CreatePart(header)
	if err != nil {
		return err
	}
	_, err = part.Write(content)
	return err
}

func callCustomJSONSTT(
	ctx context.Context,
	client *http.Client,
	sttURL string,
	apiKey string,
	cmd TranscribeCommand,
	locale string,
) (string, error) {
	payload := map[string]string{
		"audioBase64": cmd.AudioBase64,
		"mimeType":    cmd.MimeType,
		"locale":      locale,
		"promptHint":  cmd.PromptHint,
	}
	respBytes, err := postJSON(ctx, client, sttURL, apiKey, payload)
	if err != nil {
		return "", err
	}
	var parsed struct {
		Transcript string `json:"transcript"`
	}
	if err := json.Unmarshal(respBytes, &parsed); err != nil {
		return "", err
	}
	trimmed := strings.TrimSpace(parsed.Transcript)
	if trimmed == "" {
		return "", domain.ErrInvalidAudioPayload
	}
	return trimmed, nil
}

func postJSON(
	ctx context.Context,
	client *http.Client,
	endpoint string,
	bearerToken string,
	payload any,
) ([]byte, error) {
	rawBody, err := json.Marshal(payload)
	if err != nil {
		return nil, err
	}
	reqCtx, cancel := context.WithTimeout(ctx, defaultAITimeout)
	defer cancel()
	httpReq, err := http.NewRequestWithContext(reqCtx, http.MethodPost, endpoint, bytes.NewReader(rawBody))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	if bearerToken != "" {
		httpReq.Header.Set("Authorization", "Bearer "+bearerToken)
	}
	return executeHTTPRequest(client, httpReq)
}

func executeHTTPRequest(client *http.Client, httpReq *http.Request) ([]byte, error) {
	activeClient := client
	if activeClient == nil {
		activeClient = &http.Client{Timeout: defaultAITimeout}
	}
	resp, err := activeClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer func() { _ = resp.Body.Close() }()
	respBytes, err := io.ReadAll(io.LimitReader(resp.Body, 2*1024*1024))
	if err != nil {
		return nil, err
	}
	if resp.StatusCode >= http.StatusMultipleChoices {
		return nil, fmt.Errorf("provider returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(respBytes)))
	}
	return respBytes, nil
}

func resolveAudioFormat(mimeType string) (string, string) {
	cleaned := strings.ToLower(strings.TrimSpace(mimeType))
	if idx := strings.IndexByte(cleaned, ';'); idx > 0 {
		cleaned = strings.TrimSpace(cleaned[:idx])
	}
	switch cleaned {
	case "audio/mp4", "audio/m4a", "audio/x-m4a":
		return "audio/mp4", "m4a"
	case "audio/mpeg", "audio/mp3":
		return "audio/mpeg", "mp3"
	case "audio/ogg":
		return "audio/ogg", "ogg"
	case "audio/webm":
		return "audio/webm", "webm"
	default:
		return "audio/wav", "wav"
	}
}

func shortLanguageCode(locale string) string {
	trimmed := strings.TrimSpace(locale)
	if trimmed == "" {
		return "es"
	}
	if idx := strings.IndexAny(trimmed, "-_"); idx > 0 {
		return strings.ToLower(trimmed[:idx])
	}
	return strings.ToLower(trimmed)
}

func defaultURL(override, fallback string) string {
	trimmed := strings.TrimRight(strings.TrimSpace(override), "/")
	if trimmed != "" {
		return trimmed
	}
	return fallback
}
