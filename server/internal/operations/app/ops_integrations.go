package app

import (
	"bytes"
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"crypto/subtle"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"strings"
	"time"
	"unicode"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	realtimedomain "github.com/Jeudry/rosafiesta/server/internal/realtime/domain"
)

// EnsureLogisticsForConfirmedEvent crea de forma idempotente logística, checklist y paradas para un evento confirmado.
func (s *Service) EnsureLogisticsForConfirmedEvent(
	ctx context.Context,
	eventID string,
	title string,
	venue string,
	zone string,
	dateLabel string,
) error {
	cleanID := strings.TrimSpace(eventID)
	if cleanID == "" {
		return domain.ErrInvalidOperationsInput
	}
	store, hasStore := s.storeEnabled()
	if s.logisticsAlreadyExists(ctx, cleanID, store, hasStore) {
		return nil
	}

	effectiveVenue := resolveVenueText(venue, zone)
	logi := domain.EventLogistics{ //nolint:gosec // Metadatos logísticos del evento, sin credenciales
		EventID:        cleanID,
		EventTitle:     strings.TrimSpace(title),
		DateLabel:      strings.TrimSpace(dateLabel),
		SetupWindow:    "09:00 AM – 12:00 PM",
		TeardownWindow: "11:00 PM – 01:00 AM",
		Venue:          effectiveVenue,
		Checklist:      buildDefaultEventChecklist(cleanID),
	}

	s.mu.Lock()
	nextOrder := len(s.routeStops) + 1
	stops := buildConfirmedRouteStops(cleanID, logi.EventTitle, effectiveVenue, nextOrder)
	s.logistics[cleanID] = logi
	s.routeStops = append(s.routeStops, stops...)
	s.mu.Unlock()

	if err := persistNewLogisticsBundle(ctx, store, hasStore, logi, zone, stops); err != nil {
		return err
	}
	s.publishOpsEvent(realtimedomain.EventLogisticsUpdated, cleanID, "Itinerario logístico y paradas creadas para "+logi.EventTitle, map[string]any{
		"eventId": cleanID,
		"stops":   len(stops),
	})
	return nil
}

func (s *Service) logisticsAlreadyExists(ctx context.Context, eventID string, store Store, hasStore bool) bool {
	s.mu.RLock()
	_, inMem := s.logistics[eventID]
	s.mu.RUnlock()
	if inMem {
		return true
	}
	if !hasStore {
		return false
	}
	_, inDB := s.loadLogisticsFromStore(ctx, store, eventID)
	return inDB
}

func resolveVenueText(venue, zone string) string {
	cleanVenue := strings.TrimSpace(venue)
	if cleanVenue != "" {
		return cleanVenue
	}
	cleanZone := strings.TrimSpace(zone)
	if cleanZone != "" {
		return cleanZone
	}
	return "Santo Domingo"
}

func buildConfirmedRouteStops(eventID, title, venue string, startOrder int) []domain.RouteStop {
	return []domain.RouteStop{
		{
			Order:       startOrder,
			EventID:     eventID,
			EventTitle:  "Entrega · " + title,
			Venue:       venue,
			WindowLabel: "09:00 AM – 12:00 PM",
			Status:      "programado",
		},
		{
			Order:       startOrder + 1,
			EventID:     eventID,
			EventTitle:  "Recogida · " + title,
			Venue:       venue,
			WindowLabel: "11:00 PM – 01:00 AM",
			Status:      "programado",
		},
	}
}

func buildDefaultEventChecklist(eventID string) []domain.ChecklistItem {
	prefix := "chk-" + eventID + "-"
	return []domain.ChecklistItem{
		{ID: prefix + "1", Stage: domain.StageWarehouseLoad, Title: "Conteo e inspección de carga en bodega"},
		{ID: prefix + "2", Stage: domain.StageInTransit, Title: "Despacho en ruta y verificación de horario"},
		{ID: prefix + "3", Stage: domain.StageOnSiteSetup, Title: "Montaje completo y validación en sitio"},
		{ID: prefix + "4", Stage: domain.StageTeardown, Title: "Desmontaje y conteo de retorno post-evento"},
	}
}

func persistNewLogisticsBundle(
	ctx context.Context,
	store Store,
	hasStore bool,
	logi domain.EventLogistics,
	zone string,
	stops []domain.RouteStop,
) error {
	if !hasStore {
		return nil
	}
	if err := store.UpsertEventLogistics(ctx, logi, zone, "programada", logi.DateLabel); err != nil {
		return err
	}
	for _, item := range logi.Checklist {
		if err := store.UpsertChecklistItem(ctx, logi.EventID, item); err != nil {
			return err
		}
	}
	return store.ReplaceDeliveryStops(ctx, logi.EventID, stops)
}

func (s *Service) applyDamageSideEffects(ctx context.Context, report *domain.DamageReport) error {
	s.mu.RLock()
	adjuster := s.stockAdjuster
	recorder := s.chargeRecorder
	s.mu.RUnlock()

	totalAffected := report.DamagedUnits + report.MissingUnits
	if adjuster != nil {
		if totalAffected > 0 {
			if err := adjuster.AdjustDamagedStock(ctx, report.ArticleCode, totalAffected); err != nil {
				return err
			}
		}
	}
	if recorder != nil {
		if report.ReplacementRD > 0 {
			chargeID, err := recorder.RecordDamageCharge(ctx, *report)
			if err != nil {
				return err
			}
			report.ChargePaymentID = strings.TrimSpace(chargeID)
		}
	}
	return nil
}

func (s *Service) storeAndSaveDamageReport(ctx context.Context, eventID string, report domain.DamageReport, now time.Time) error {
	s.mu.Lock()
	logi, ok := s.logistics[eventID]
	if !ok {
		s.mu.Unlock()
		return domain.ErrOperationNotFound
	}
	logi = cloneEventLogistics(logi)
	logi.DamageReports = append(logi.DamageReports, report)
	s.logistics[eventID] = logi
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			return store.RecordDamageIncident(ctx, report, now)
		}
	}
	return nil
}

// ResolveDamage marca una incidencia como resuelta, restaura el inventario dañado y emite evento SSE.
func (s *Service) ResolveDamage(ctx context.Context, incidentID, notes string) (domain.DamageReport, error) {
	targetID := strings.TrimSpace(incidentID)
	resolved, alreadyDone, eventID, err := s.markDamageResolved(ctx, targetID, notes)
	if err != nil {
		return domain.DamageReport{}, err
	}
	if alreadyDone {
		return resolved, nil
	}

	s.mu.RLock()
	adjuster := s.stockAdjuster
	store := s.store
	s.mu.RUnlock()

	if adjuster != nil {
		if resolved.DamagedUnits > 0 {
			if adjErr := adjuster.AdjustDamagedStock(ctx, resolved.ArticleCode, -resolved.DamagedUnits); adjErr != nil {
				return domain.DamageReport{}, adjErr
			}
		}
	}
	if store != nil {
		if store.Enabled() {
			if persistErr := store.RecordDamageIncident(ctx, resolved, s.clock.Now()); persistErr != nil {
				return domain.DamageReport{}, persistErr
			}
		}
	}
	s.publishOpsEvent(realtimedomain.EventLogisticsUpdated, eventID, "Incidencia resuelta: "+resolved.ArticleName, map[string]any{
		"incidentId": resolved.ID,
		"eventId":    eventID,
		"status":     resolved.Status,
	})
	return resolved, nil
}

func (s *Service) markDamageResolved(ctx context.Context, targetID, notes string) (domain.DamageReport, bool, string, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	report, eventID, found := s.findDamageInMemoryLocked(targetID)
	if !found {
		loaded, loadedEvent, ok := s.findAndLoadDamageFromStoreLocked(ctx, targetID)
		if !ok {
			return domain.DamageReport{}, false, "", domain.ErrDamageIncidentNotFound
		}
		report = loaded
		eventID = loadedEvent
	}
	if report.Status == "resuelto" {
		return report, true, eventID, nil
	}
	report.Status = "resuelto"
	cleanNotes := strings.TrimSpace(notes)
	if cleanNotes != "" {
		report.ResolutionNotes = cleanNotes
	}
	s.updateDamageInMemoryLocked(eventID, report)
	return report, false, eventID, nil
}

func (s *Service) findDamageInMemoryLocked(targetID string) (domain.DamageReport, string, bool) {
	for eventID, logi := range s.logistics {
		for _, item := range logi.DamageReports {
			if item.ID == targetID {
				return item, eventID, true
			}
		}
	}
	return domain.DamageReport{}, "", false
}

func (s *Service) findAndLoadDamageFromStoreLocked(ctx context.Context, targetID string) (domain.DamageReport, string, bool) {
	if s.store == nil {
		return domain.DamageReport{}, "", false
	}
	if !s.store.Enabled() {
		return domain.DamageReport{}, "", false
	}
	list, err := s.store.ListDamageIncidents(ctx, "")
	if err != nil {
		return domain.DamageReport{}, "", false
	}
	for _, item := range list {
		if item.ID == targetID {
			return item, item.EventID, true
		}
	}
	return domain.DamageReport{}, "", false
}

func (s *Service) updateDamageInMemoryLocked(eventID string, updated domain.DamageReport) {
	logi, ok := s.logistics[eventID]
	if !ok {
		return
	}
	cloned := cloneEventLogistics(logi)
	for idx := range cloned.DamageReports {
		if cloned.DamageReports[idx].ID == updated.ID {
			cloned.DamageReports[idx] = updated
			s.logistics[eventID] = cloned
			return
		}
	}
	cloned.DamageReports = append(cloned.DamageReports, updated)
	s.logistics[eventID] = cloned
}

// EnqueueOfflineMutation encola una mutación fuera de línea deduplicando por idempotencyKey.
func (s *Service) EnqueueOfflineMutation(
	ctx context.Context,
	cmd EnqueueOfflineMutationCommand,
) (domain.OfflineMutation, error) {
	idemKey := strings.TrimSpace(cmd.IdempotencyKey)
	if idemKey != "" {
		if existing, found := s.findOfflineByIdempotencyKey(ctx, idemKey); found {
			return existing, nil
		}
	}
	now := s.clock.Now()
	mutation := domain.OfflineMutation{
		ID:             "off-" + uuid.NewString()[:8],
		ActionLabel:    strings.TrimSpace(cmd.ActionLabel),
		EntityCode:     strings.TrimSpace(cmd.EntityCode),
		OperationType:  strings.TrimSpace(cmd.OperationType),
		PayloadJSON:    strings.TrimSpace(cmd.PayloadJSON),
		IdempotencyKey: idemKey,
		CreatedAt:      now.Format("03:04 PM"),
		Synced:         false,
	}

	s.mu.Lock()
	s.offlineQueue = append(s.offlineQueue, mutation)
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			if err := store.UpsertOfflineMutation(ctx, mutation, now); err != nil {
				return domain.OfflineMutation{}, err
			}
		}
	}
	return mutation, nil
}

func (s *Service) findOfflineByIdempotencyKey(ctx context.Context, key string) (domain.OfflineMutation, bool) {
	current := s.ListOfflineQueue(ctx)
	for _, item := range current {
		if item.IdempotencyKey == key {
			return item, true
		}
	}
	return domain.OfflineMutation{}, false
}

// SyncOfflineMutations encola opcionalmente nuevas mutaciones, despacha operaciones tipadas y sincroniza la cola.
func (s *Service) SyncOfflineMutations(
	ctx context.Context,
	incoming []EnqueueOfflineMutationCommand,
	caller platformhttp.Principal,
) ([]domain.OfflineMutation, error) {
	for _, cmd := range incoming {
		if _, err := s.EnqueueOfflineMutation(ctx, cmd); err != nil {
			return nil, err
		}
	}
	queue := s.ListOfflineQueue(ctx)
	s.mu.RLock()
	dispatcher := s.offlineDispatcher
	store := s.store
	s.mu.RUnlock()

	for idx := range queue {
		if err := s.syncSingleMutation(ctx, &queue[idx], dispatcher, store, caller); err != nil {
			return nil, err
		}
	}
	s.mu.Lock()
	s.offlineQueue = queue
	s.mu.Unlock()
	return queue, nil
}

func (s *Service) syncSingleMutation(
	ctx context.Context,
	item *domain.OfflineMutation,
	dispatcher OfflineMutationDispatcher,
	store Store,
	caller platformhttp.Principal,
) error {
	if item.Synced {
		return nil
	}
	if dispatcher != nil {
		if strings.TrimSpace(item.OperationType) != "" {
			if err := dispatcher.DispatchMutation(ctx, item.OperationType, item.PayloadJSON, caller); err != nil {
				return err
			}
		}
	}
	item.Synced = true
	if store != nil {
		if store.Enabled() {
			return store.UpsertOfflineMutation(ctx, *item, s.clock.Now())
		}
	}
	return nil
}

// SendWhatsAppBotReply envía una respuesta saliente usando Meta WhatsApp Cloud API.
func (s *Service) SendWhatsAppBotReply(
	ctx context.Context,
	threadID string,
	recipientPhone string,
	message string,
) (domain.BotThread, error) {
	phoneID, token, apiBase := s.whatsAppConfigSnapshot()
	if phoneID == "" {
		return domain.BotThread{}, domain.ErrWhatsAppNotConfigured
	}
	if token == "" {
		return domain.BotThread{}, domain.ErrWhatsAppNotConfigured
	}
	cleanMsg := strings.TrimSpace(message)
	if cleanMsg == "" {
		return domain.BotThread{}, domain.ErrInvalidOperationsInput
	}
	_ = s.ListBotThreads(ctx)
	targetPhone, err := s.resolveWhatsAppRecipient(threadID, recipientPhone)
	if err != nil {
		return domain.BotThread{}, err
	}
	if err := executeWhatsAppSend(ctx, apiBase, phoneID, token, targetPhone, cleanMsg); err != nil {
		return domain.BotThread{}, err
	}
	updated := s.recordOutboundWhatsAppThread(ctx, threadID, targetPhone, cleanMsg)
	s.publishOpsEvent(realtimedomain.EventChatMessage, updated.ID, "Respuesta WhatsApp enviada a "+updated.CustomerName, map[string]any{
		"threadId": updated.ID,
		"phone":    updated.Phone,
		"message":  cleanMsg,
	})
	return updated, nil
}

// VerifyWhatsAppWebhook valida el desafío de verificación inicial de Meta Cloud API.
func (s *Service) VerifyWhatsAppWebhook(mode, token, challenge string) (string, error) {
	s.mu.RLock()
	expected := s.whatsAppVerifyToken
	s.mu.RUnlock()
	if expected == "" {
		return "", domain.ErrWhatsAppNotConfigured
	}
	if strings.TrimSpace(mode) != "subscribe" {
		return "", domain.ErrWhatsAppVerifyFailed
	}
	if subtle.ConstantTimeCompare([]byte(strings.TrimSpace(token)), []byte(expected)) != 1 {
		return "", domain.ErrWhatsAppVerifyFailed
	}
	return challenge, nil
}

// VerifyWhatsAppSignature verifica la firma X-Hub-Signature-256 enviada por Meta Cloud API.
func (s *Service) VerifyWhatsAppSignature(signatureHeader string, payload []byte) error {
	s.mu.RLock()
	secret := s.whatsAppAppSecret
	s.mu.RUnlock()

	cleanSig := strings.TrimSpace(signatureHeader)
	if secret == "" {
		if cleanSig != "" {
			return domain.ErrWebhookSignatureInvalid
		}
		return nil
	}
	lowerSig := strings.ToLower(cleanSig)
	if !strings.HasPrefix(lowerSig, "sha256=") {
		return domain.ErrWebhookSignatureInvalid
	}
	providedHex := strings.TrimPrefix(lowerSig, "sha256=")
	providedBytes, err := hex.DecodeString(providedHex)
	if err != nil {
		return domain.ErrWebhookSignatureInvalid
	}
	mac := hmac.New(sha256.New, []byte(secret))
	_, _ = mac.Write(payload)
	expectedBytes := mac.Sum(nil)
	if !hmac.Equal(providedBytes, expectedBytes) {
		return domain.ErrWebhookSignatureInvalid
	}
	return nil
}

// IngestWhatsAppWebhookSigned valida la firma HMAC del webhook antes de procesar eventos entrantes.
func (s *Service) IngestWhatsAppWebhookSigned(ctx context.Context, signatureHeader string, payload []byte) error {
	if err := s.VerifyWhatsAppSignature(signatureHeader, payload); err != nil {
		return err
	}
	return s.IngestWhatsAppWebhook(ctx, payload)
}

// IngestWhatsAppWebhook procesa mensajes entrantes de Meta WhatsApp y actualiza hilos de bot + SSE.
func (s *Service) IngestWhatsAppWebhook(ctx context.Context, payload []byte) error {
	events := parseWhatsAppInboundEvents(payload)
	if len(events) == 0 {
		return nil
	}
	_ = s.ListBotThreads(ctx)
	for _, inbound := range events {
		updated := s.upsertInboundWhatsAppThread(ctx, inbound)
		s.publishOpsEvent(realtimedomain.EventChatMessage, updated.ID, "Mensaje WhatsApp recibido de "+updated.CustomerName, map[string]any{
			"threadId": updated.ID,
			"phone":    updated.Phone,
			"message":  updated.LastMessage,
		})
		s.dispatchWhatsAppAutoReply(ctx, updated, inbound.Text)
	}
	return nil
}

func (s *Service) dispatchWhatsAppAutoReply(ctx context.Context, thread domain.BotThread, inboundText string) {
	if thread.BotPaused {
		return
	}
	replyText, handoff, matched := s.matchWhatsAppAutoReply(ctx, inboundText)
	if !matched {
		return
	}
	if handoff {
		paused := true
		_, _ = s.UpdateBotThread(ctx, thread.ID, UpdateBotThreadCommand{BotPaused: &paused})
		s.CreateNotification(
			ctx,
			"Conversación WhatsApp requiere asesor",
			thread.CustomerName+": "+inboundText,
			"whatsapp",
			"admin",
			"",
		)
	}
	phoneID, token, _ := s.whatsAppConfigSnapshot()
	if phoneID == "" {
		return
	}
	if token == "" {
		return
	}
	if _, err := s.SendWhatsAppBotReply(ctx, thread.ID, thread.Phone, replyText); err != nil {
		slog.Warn("whatsapp_autoreply_failed", slog.String("thread_id", thread.ID), slog.String("error", err.Error()))
	}
}

func (s *Service) matchWhatsAppAutoReply(ctx context.Context, inboundText string) (string, bool, bool) {
	normalized := strings.ToLower(strings.TrimSpace(inboundText))
	if normalized == "" {
		return "", false, false
	}
	for _, kw := range s.ListBotKeywords(ctx) {
		term := strings.ToLower(strings.TrimSpace(kw.Keyword))
		if term != "" {
			if strings.Contains(normalized, term) {
				return kw.AutoReply, kw.HandoffToHuman, true
			}
		}
	}
	for _, faq := range s.ListBotFAQs(ctx) {
		if !faq.Active {
			continue
		}
		if faqMatchesText(faq.Question, normalized) {
			return faq.Answer, false, true
		}
	}
	return "", false, false
}

func faqMatchesText(question string, normalizedInbound string) bool {
	words := strings.Fields(strings.ToLower(question))
	matchedWords := 0
	for _, word := range words {
		cleaned := strings.Trim(word, "¿?¡!.,;:")
		if len(cleaned) < 4 {
			continue
		}
		if strings.Contains(normalizedInbound, cleaned) {
			matchedWords++
		}
	}
	return matchedWords >= 2
}

type inboundWhatsAppMsg struct {
	From string
	Name string
	Text string
}

func parseWhatsAppInboundEvents(raw []byte) []inboundWhatsAppMsg {
	var metaPayload struct {
		From    string `json:"from"`
		Name    string `json:"name"`
		Message string `json:"message"`
		Entry   []struct {
			Changes []struct {
				Value struct {
					Contacts []struct {
						Profile struct {
							Name string `json:"name"`
						} `json:"profile"`
						WaID string `json:"wa_id"`
					} `json:"contacts"`
					Messages []struct {
						From string `json:"from"`
						Text struct {
							Body string `json:"body"`
						} `json:"text"`
					} `json:"messages"`
				} `json:"value"`
			} `json:"changes"`
		} `json:"entry"`
	}
	if err := json.Unmarshal(raw, &metaPayload); err != nil {
		return nil
	}
	out := collectMetaEntryMessages(&metaPayload)
	if len(out) == 0 {
		if strings.TrimSpace(metaPayload.Message) != "" {
			out = append(out, inboundWhatsAppMsg{
				From: strings.TrimSpace(metaPayload.From),
				Name: strings.TrimSpace(metaPayload.Name),
				Text: strings.TrimSpace(metaPayload.Message),
			})
		}
	}
	return out
}

func collectMetaEntryMessages(metaPayload *struct {
	From    string `json:"from"`
	Name    string `json:"name"`
	Message string `json:"message"`
	Entry   []struct {
		Changes []struct {
			Value struct {
				Contacts []struct {
					Profile struct {
						Name string `json:"name"`
					} `json:"profile"`
					WaID string `json:"wa_id"`
				} `json:"contacts"`
				Messages []struct {
					From string `json:"from"`
					Text struct {
						Body string `json:"body"`
					} `json:"text"`
				} `json:"messages"`
			} `json:"value"`
		} `json:"changes"`
	} `json:"entry"`
}) []inboundWhatsAppMsg {
	out := make([]inboundWhatsAppMsg, 0)
	for _, entry := range metaPayload.Entry {
		for _, change := range entry.Changes {
			nameByWaID := make(map[string]string, len(change.Value.Contacts))
			for _, contact := range change.Value.Contacts {
				nameByWaID[contact.WaID] = contact.Profile.Name
			}
			for _, msg := range change.Value.Messages {
				text := strings.TrimSpace(msg.Text.Body)
				if text == "" {
					continue
				}
				out = append(out, inboundWhatsAppMsg{
					From: strings.TrimSpace(msg.From),
					Name: strings.TrimSpace(nameByWaID[msg.From]),
					Text: text,
				})
			}
		}
	}
	return out
}

func (s *Service) upsertInboundWhatsAppThread(ctx context.Context, msg inboundWhatsAppMsg) domain.BotThread {
	nowLabel := s.clock.Now().Format("03:04 PM")
	name := msg.Name
	if name == "" {
		name = msg.From
	}
	s.mu.Lock()
	updated := s.findOrCreateThreadLocked("", msg.From, name, msg.Text, nowLabel)
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			_ = store.UpsertBotThread(ctx, updated)
		}
	}
	return updated
}

func (s *Service) whatsAppConfigSnapshot() (phoneID, token, apiBase string) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	apiBase = s.whatsAppAPIBaseURL
	if apiBase == "" {
		apiBase = defaultWhatsAppAPIBaseURL
	}
	return s.whatsAppPhoneID, s.whatsAppAccessToken, apiBase
}

func (s *Service) resolveWhatsAppRecipient(threadID, recipientPhone string) (string, error) {
	explicit := strings.TrimSpace(recipientPhone)
	if explicit != "" {
		return explicit, nil
	}
	cleanThread := strings.TrimSpace(threadID)
	s.mu.RLock()
	defer s.mu.RUnlock()
	for _, th := range s.botThreads {
		if th.ID == cleanThread {
			if strings.TrimSpace(th.Phone) != "" {
				return strings.TrimSpace(th.Phone), nil
			}
		}
	}
	return "", domain.ErrBotThreadNotFound
}

func (s *Service) recordOutboundWhatsAppThread(ctx context.Context, threadID, phone, msg string) domain.BotThread {
	nowLabel := s.clock.Now().Format("03:04 PM")
	s.mu.Lock()
	updated := s.findOrCreateThreadLocked(threadID, phone, phone, msg, nowLabel)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.UpsertBotThread(ctx, updated)
		}
	}
	return updated
}

func (s *Service) findOrCreateThreadLocked(threadID, phone, customerName, message, nowLabel string) domain.BotThread {
	cleanID := strings.TrimSpace(threadID)
	cleanPhone := strings.TrimSpace(phone)
	for idx := range s.botThreads {
		if matchBotThread(s.botThreads[idx], cleanID, cleanPhone) {
			s.botThreads[idx].LastMessage = message
			s.botThreads[idx].UpdatedAtLabel = nowLabel
			return s.botThreads[idx]
		}
	}
	created := domain.BotThread{
		ID:             resolveThreadID(cleanID),
		CustomerName:   customerName,
		Phone:          cleanPhone,
		LastMessage:    message,
		UpdatedAtLabel: nowLabel,
	}
	s.botThreads = append(s.botThreads, created)
	return created
}

func matchBotThread(th domain.BotThread, targetID, targetPhone string) bool {
	if targetID != "" {
		if th.ID == targetID {
			return true
		}
	}
	if targetPhone != "" {
		return normalizeDigits(th.Phone) == normalizeDigits(targetPhone)
	}
	return false
}

func resolveThreadID(explicitID string) string {
	if explicitID != "" {
		return explicitID
	}
	return "wa-" + uuid.NewString()[:8]
}

func normalizeDigits(raw string) string {
	var b strings.Builder
	for _, ch := range raw {
		if unicode.IsDigit(ch) {
			b.WriteRune(ch)
		}
	}
	return b.String()
}

func executeWhatsAppSend(ctx context.Context, apiBase, phoneID, accessToken, to, message string) error {
	endpoint := fmt.Sprintf("%s/%s/messages", strings.TrimRight(apiBase, "/"), phoneID)
	bodyBytes, err := json.Marshal(map[string]any{
		"messaging_product": "whatsapp",
		"to":                to,
		"type":              "text",
		"text":              map[string]string{"body": message},
	})
	if err != nil {
		return err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, endpoint, bytes.NewReader(bodyBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{Timeout: 15 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()

	if resp.StatusCode < 200 {
		rawResp, _ := io.ReadAll(io.LimitReader(resp.Body, 4096))
		return fmt.Errorf("whatsapp api returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawResp)))
	}
	if resp.StatusCode >= 300 {
		rawResp, _ := io.ReadAll(io.LimitReader(resp.Body, 4096))
		return fmt.Errorf("whatsapp api returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawResp)))
	}
	return nil
}

func (s *Service) publishOpsEvent(
	eventType realtimedomain.EventType,
	entityID string,
	summary string,
	payload map[string]any,
) {
	s.mu.RLock()
	pub := s.realtimePub
	s.mu.RUnlock()
	if pub == nil {
		return
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     eventType,
		Audience: realtimedomain.AudienceAdmin,
		EntityID: entityID,
		Summary:  summary,
		Payload:  payload,
	})
}

// CreateNotification registra una notificación operativa y la difunde por SSE.
func (s *Service) CreateNotification(
	ctx context.Context,
	title string,
	body string,
	category string,
	recipientRole string,
	userID string,
) domain.Notification {
	now := s.clock.Now()
	role := strings.TrimSpace(recipientRole)
	if role == "" {
		role = "admin"
	}
	ntf := domain.Notification{
		ID:             "ntf-" + uuid.NewString()[:8],
		RecipientRole:  role,
		UserID:         strings.TrimSpace(userID),
		Title:          strings.TrimSpace(title),
		Body:           strings.TrimSpace(body),
		Category:       strings.TrimSpace(category),
		ActionPath:     "/notifications",
		IsRead:         false,
		CreatedAtLabel: now.Format("03:04 PM"),
	}
	s.mu.Lock()
	s.notifications = append([]domain.Notification{ntf}, s.notifications...)
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			_ = store.UpsertNotification(ctx, ntf, now)
		}
	}
	s.publishNotificationEvent(ntf)
	return ntf
}

func (s *Service) publishNotificationEvent(ntf domain.Notification) {
	s.mu.RLock()
	pub := s.realtimePub
	s.mu.RUnlock()
	if pub == nil {
		return
	}
	audience := realtimedomain.AudienceAdmin
	clientID := ""
	if ntf.RecipientRole == "client" {
		if strings.TrimSpace(ntf.UserID) != "" {
			audience = realtimedomain.AudienceClient
			clientID = strings.TrimSpace(ntf.UserID)
		}
	}
	pub.Publish(realtimedomain.RealtimeEvent{
		Type:     realtimedomain.EventNotificationCreated,
		Audience: audience,
		ClientID: clientID,
		EntityID: ntf.ID,
		Summary:  ntf.Title,
		Payload: map[string]any{
			"notificationId": ntf.ID,
			"recipientRole":  ntf.RecipientRole,
			"category":       ntf.Category,
		},
	})
}
