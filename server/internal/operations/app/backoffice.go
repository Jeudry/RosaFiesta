package app

import (
	"context"
	"fmt"
	"strings"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type CreateReviewCommand struct {
	EventID    string
	ClientName string
	Rating     int
	Comment    string
}

type UpsertSupplierCommand struct {
	ID          string
	Name        string
	Category    string
	Specialty   string
	ContactName string
	Phone       string
	Email       string
	Website     string
	Rating      string
	Notes       string
}

type CreateLeadCommand struct {
	FullName       string
	Phone          string
	Email          string
	EventType      string
	EventDateLabel string
	Guests         int
	EstimatedDop   int
	Stage          string
	Notes          string
}

type UpdateLeadCommand struct {
	Stage        *string
	EstimatedDop *int
	Notes        *string
}

type UpdateSettingsCommand struct {
	CompanyName               string
	RNC                       string
	Address                   string
	Phone                     string
	WhatsApp                  string
	Email                     string
	Instagram                 string
	Facebook                  string
	BusinessHours             string
	ItbisPercent              *int
	DepositPercent            int
	WhatsAppAutoAlertsEnabled *bool
	DeliveryZones             []domain.DeliveryZone
	HasDeliveryZones          bool
}

type UpsertBotFAQCommand struct {
	ID       string
	Question string
	Answer   string
	Category string
	Active   bool
}

type UpsertBotKeywordCommand struct {
	ID             string
	Keyword        string
	AutoReply      string
	HandoffToHuman bool
}

type UpdateBotThreadCommand struct {
	BotPaused     *bool
	AssignedAgent *string
	LastMessage   *string
}

func (s *Service) ListReviews(ctx context.Context, eventID string) []domain.Review {
	source := s.loadReviews(ctx)
	filter := strings.TrimSpace(eventID)
	if filter == "" {
		return source
	}
	out := make([]domain.Review, 0, len(source))
	for _, rev := range source {
		if rev.EventID == filter {
			out = append(out, rev)
		}
	}
	return out
}

func (s *Service) loadReviews(ctx context.Context) []domain.Review {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListReviews(ctx); err == nil {
			s.mu.Lock()
			s.reviews = rows
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Review, len(s.reviews))
	copy(out, s.reviews)
	return out
}

func (s *Service) CreateReview(
	ctx context.Context,
	cmd CreateReviewCommand,
	caller platformhttp.Principal,
) (domain.Review, error) {
	if cmd.Rating < 1 {
		return domain.Review{}, domain.ErrInvalidRating
	}
	if cmd.Rating > 5 {
		return domain.Review{}, domain.ErrInvalidRating
	}
	comment := strings.TrimSpace(cmd.Comment)
	if comment == "" {
		return domain.Review{}, domain.ErrInvalidOperationsInput
	}
	eventID := strings.TrimSpace(cmd.EventID)
	s.mu.RLock()
	eventChecker := s.eventChecker
	s.mu.RUnlock()
	if eventChecker != nil {
		if eventID == "" {
			return domain.Review{}, domain.ErrInvalidOperationsInput
		}
		if err := eventChecker.VerifyEventOwnership(ctx, eventID, caller); err != nil {
			return domain.Review{}, err
		}
	}
	clientName := resolveReviewClientName(cmd.ClientName, caller)
	now := s.clock.Now()
	review := domain.Review{
		ID:             "rev-" + uuid.NewString()[:8],
		EventID:        eventID,
		ClientName:     clientName,
		Rating:         cmd.Rating,
		Comment:        comment,
		CreatedAtLabel: fmt.Sprintf("%d %s", now.Day(), shortMonthLabel(int(now.Month()))),
	}
	s.mu.Lock()
	s.reviews = append([]domain.Review{review}, s.reviews...)
	s.mu.Unlock()

	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertReview(ctx, review, now); err != nil {
			return domain.Review{}, err
		}
	}
	return review, nil
}

func (s *Service) ReplyReview(
	ctx context.Context,
	reviewID string,
	reply string,
	caller platformhttp.Principal,
) (domain.Review, error) {
	return s.ReplyReviewWithVerification(ctx, reviewID, reply, nil, caller)
}

// ReplyReviewWithVerification guarda la respuesta del administrador y el estado de verificación de la reseña.
func (s *Service) ReplyReviewWithVerification(
	ctx context.Context,
	reviewID string,
	reply string,
	verified *bool,
	caller platformhttp.Principal,
) (domain.Review, error) {
	cleanReply := strings.TrimSpace(reply)
	if cleanReply == "" {
		return domain.Review{}, domain.ErrInvalidOperationsInput
	}
	_ = s.loadReviews(ctx)
	targetID := strings.TrimSpace(reviewID)
	now := s.clock.Now()

	s.mu.Lock()
	idx := -1
	for i := range s.reviews {
		if s.reviews[i].ID == targetID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.Review{}, domain.ErrReviewNotFound
	}
	s.reviews[idx].AdminReply = cleanReply
	if verified != nil {
		s.reviews[idx].Verified = *verified
	}
	updated := s.reviews[idx]
	s.mu.Unlock()

	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertReview(ctx, updated, now); err != nil {
			return domain.Review{}, err
		}
	}
	s.recordAudit(ctx, caller, "REPLY_REVIEW", "review", updated.ID, "Respuesta publicada en reseña")
	return updated, nil
}

func (s *Service) ListSuppliers(ctx context.Context, category string) []domain.Supplier {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListSuppliers(ctx); err == nil {
			s.mu.Lock()
			s.suppliers = rows
			s.mu.Unlock()
		}
	}
	filter := strings.TrimSpace(category)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Supplier, 0, len(s.suppliers))
	for _, sup := range s.suppliers {
		if filter != "" {
			if !strings.EqualFold(sup.Category, filter) {
				continue
			}
		}
		out = append(out, sup)
	}
	return out
}

func (s *Service) UpsertSupplier(ctx context.Context, cmd UpsertSupplierCommand) (domain.Supplier, error) {
	name := strings.TrimSpace(cmd.Name)
	if name == "" {
		return domain.Supplier{}, domain.ErrInvalidOperationsInput
	}
	id := strings.TrimSpace(cmd.ID)
	if id == "" {
		id = "sup-" + uuid.NewString()[:8]
	}
	rating := strings.TrimSpace(cmd.Rating)
	if rating == "" {
		rating = "5.0"
	}
	sup := domain.Supplier{
		ID:          id,
		Name:        name,
		Category:    strings.TrimSpace(cmd.Category),
		Specialty:   strings.TrimSpace(cmd.Specialty),
		ContactName: strings.TrimSpace(cmd.ContactName),
		Phone:       strings.TrimSpace(cmd.Phone),
		Email:       strings.TrimSpace(cmd.Email),
		Website:     strings.TrimSpace(cmd.Website),
		Rating:      rating,
		Notes:       strings.TrimSpace(cmd.Notes),
	}
	s.saveSupplierInMemory(sup)
	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertSupplier(ctx, sup); err != nil {
			return domain.Supplier{}, err
		}
	}
	return sup, nil
}

// PatchSupplier actualiza parcialmente un proveedor existente y registra la auditoría.
func (s *Service) PatchSupplier(
	ctx context.Context,
	id string,
	cmd UpsertSupplierCommand,
	caller platformhttp.Principal,
) (domain.Supplier, error) {
	existingList := s.ListSuppliers(ctx, "")
	targetID := strings.TrimSpace(id)
	var current domain.Supplier
	found := false
	for _, item := range existingList {
		if item.ID == targetID {
			current = item
			found = true
			break
		}
	}
	if !found {
		return domain.Supplier{}, domain.ErrSupplierNotFound
	}
	merged := UpsertSupplierCommand{
		ID:          targetID,
		Name:        nonEmptyOr(cmd.Name, current.Name),
		Category:    nonEmptyOr(cmd.Category, current.Category),
		Specialty:   nonEmptyOr(cmd.Specialty, current.Specialty),
		ContactName: nonEmptyOr(cmd.ContactName, current.ContactName),
		Phone:       nonEmptyOr(cmd.Phone, current.Phone),
		Email:       nonEmptyOr(cmd.Email, current.Email),
		Website:     nonEmptyOr(cmd.Website, current.Website),
		Rating:      nonEmptyOr(cmd.Rating, current.Rating),
		Notes:       nonEmptyOr(cmd.Notes, current.Notes),
	}
	updated, err := s.UpsertSupplier(ctx, merged)
	if err != nil {
		return domain.Supplier{}, err
	}
	s.recordAudit(ctx, caller, "UPDATE_SUPPLIER", "supplier", updated.ID, "Actualizó proveedor "+updated.Name)
	return updated, nil
}

// DeleteSupplier elimina un proveedor del directorio en memoria y PostgreSQL.
func (s *Service) DeleteSupplier(ctx context.Context, id string, caller platformhttp.Principal) error {
	_ = s.ListSuppliers(ctx, "")
	targetID := strings.TrimSpace(id)
	if targetID == "" {
		return domain.ErrSupplierNotFound
	}
	removed := s.removeSupplierInMemory(targetID)
	if !removed {
		return domain.ErrSupplierNotFound
	}
	if store, enabled := s.storeEnabled(); enabled {
		if delStore, ok := store.(DeletableStore); ok {
			if err := delStore.DeleteSupplier(ctx, targetID); err != nil {
				return err
			}
		}
	}
	s.recordAudit(ctx, caller, "DELETE_SUPPLIER", "supplier", targetID, "Eliminó proveedor "+targetID)
	return nil
}

func (s *Service) removeSupplierInMemory(targetID string) bool {
	s.mu.Lock()
	defer s.mu.Unlock()
	for idx := range s.suppliers {
		if s.suppliers[idx].ID != targetID {
			continue
		}
		s.suppliers = append(s.suppliers[:idx], s.suppliers[idx+1:]...)
		return true
	}
	return false
}

func (s *Service) saveSupplierInMemory(sup domain.Supplier) {
	s.mu.Lock()
	defer s.mu.Unlock()
	for idx := range s.suppliers {
		if s.suppliers[idx].ID == sup.ID {
			s.suppliers[idx] = sup
			return
		}
	}
	s.suppliers = append(s.suppliers, sup)
}

func (s *Service) ListLeads(ctx context.Context, stage string) []domain.Lead {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListLeads(ctx); err == nil {
			s.mu.Lock()
			s.leads = rows
			s.mu.Unlock()
		}
	}
	filter := strings.TrimSpace(stage)
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Lead, 0, len(s.leads))
	for _, lead := range s.leads {
		if filter != "" {
			if !strings.EqualFold(lead.Stage, filter) {
				continue
			}
		}
		out = append(out, lead)
	}
	return out
}

func (s *Service) CreateLead(ctx context.Context, cmd CreateLeadCommand) (domain.Lead, error) {
	fullName := strings.TrimSpace(cmd.FullName)
	if fullName == "" {
		return domain.Lead{}, domain.ErrInvalidOperationsInput
	}
	stage := strings.TrimSpace(cmd.Stage)
	if stage == "" {
		stage = "nuevo"
	}
	lead := domain.Lead{
		ID:             "lead-" + uuid.NewString()[:8],
		FullName:       fullName,
		Phone:          strings.TrimSpace(cmd.Phone),
		Email:          strings.TrimSpace(cmd.Email),
		EventType:      strings.TrimSpace(cmd.EventType),
		EventDateLabel: strings.TrimSpace(cmd.EventDateLabel),
		Guests:         cmd.Guests,
		EstimatedDop:   cmd.EstimatedDop,
		Stage:          stage,
		Notes:          strings.TrimSpace(cmd.Notes),
	}
	now := s.clock.Now()
	s.mu.Lock()
	s.leads = append(s.leads, lead)
	s.mu.Unlock()
	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertLead(ctx, lead, now); err != nil {
			return domain.Lead{}, err
		}
	}
	return lead, nil
}

func (s *Service) UpdateLead(ctx context.Context, id string, cmd UpdateLeadCommand) (domain.Lead, error) {
	_ = s.ListLeads(ctx, "")
	targetID := strings.TrimSpace(id)
	now := s.clock.Now()

	s.mu.Lock()
	idx := -1
	for i := range s.leads {
		if s.leads[i].ID == targetID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.Lead{}, domain.ErrLeadNotFound
	}
	applyLeadPatch(&s.leads[idx], cmd)
	updated := s.leads[idx]
	s.mu.Unlock()

	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertLead(ctx, updated, now); err != nil {
			return domain.Lead{}, err
		}
	}
	return updated, nil
}

func applyLeadPatch(lead *domain.Lead, cmd UpdateLeadCommand) {
	if cmd.Stage != nil {
		trimmed := strings.TrimSpace(*cmd.Stage)
		if trimmed != "" {
			lead.Stage = trimmed
		}
	}
	if cmd.EstimatedDop != nil {
		if *cmd.EstimatedDop >= 0 {
			lead.EstimatedDop = *cmd.EstimatedDop
		}
	}
	if cmd.Notes != nil {
		lead.Notes = strings.TrimSpace(*cmd.Notes)
	}
}

func (s *Service) GetSettings(ctx context.Context) (domain.BusinessSettings, []domain.DeliveryZone) {
	if store, enabled := s.storeEnabled(); enabled {
		if dbSet, err := store.GetBusinessSettings(ctx); err == nil {
			s.mu.Lock()
			s.settings = dbSet
			s.mu.Unlock()
		}
		if dbZones, err := store.ListDeliveryZones(ctx); err == nil {
			s.mu.Lock()
			s.deliveryZones = dbZones
			s.mu.Unlock()
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	zones := make([]domain.DeliveryZone, len(s.deliveryZones))
	copy(zones, s.deliveryZones)
	return s.settings, zones
}

func (s *Service) GetPublicSettings(ctx context.Context) domain.PublicBusinessSettings {
	settings, zones := s.GetSettings(ctx)
	return settings.ToPublic(zones)
}

func (s *Service) UpdateSettings(
	ctx context.Context,
	cmd UpdateSettingsCommand,
	caller platformhttp.Principal,
) (domain.BusinessSettings, []domain.DeliveryZone, error) {
	current, currentZones := s.GetSettings(ctx)
	updated, err := mergeSettingsFields(current, cmd)
	if err != nil {
		return domain.BusinessSettings{}, nil, err
	}
	zones := currentZones
	shouldReplaceZones := cmd.HasDeliveryZones
	if !shouldReplaceZones {
		shouldReplaceZones = len(cmd.DeliveryZones) > 0
	}
	if shouldReplaceZones {
		zones = make([]domain.DeliveryZone, len(cmd.DeliveryZones))
		copy(zones, cmd.DeliveryZones)
	}
	now := s.clock.Now()
	s.mu.Lock()
	s.settings = updated
	s.deliveryZones = zones
	s.mu.Unlock()

	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertBusinessSettings(ctx, updated, now); err != nil {
			return domain.BusinessSettings{}, nil, err
		}
		if err := persistDeliveryZones(ctx, store, zones, shouldReplaceZones); err != nil {
			return domain.BusinessSettings{}, nil, err
		}
	}
	s.recordAudit(ctx, caller, "UPDATE_SETTINGS", "business_settings", "default", "Actualizó configuración fiscal y operativa")
	return updated, zones, nil
}

func persistDeliveryZones(
	ctx context.Context,
	store Store,
	zones []domain.DeliveryZone,
	replaced bool,
) error {
	if replaced {
		if replacer, ok := store.(DeliveryZoneReplacer); ok {
			return replacer.ReplaceDeliveryZones(ctx, zones)
		}
	}
	for _, zone := range zones {
		if err := store.UpsertDeliveryZone(ctx, zone); err != nil {
			return err
		}
	}
	return nil
}

func mergeSettingsFields(base domain.BusinessSettings, cmd UpdateSettingsCommand) (domain.BusinessSettings, error) {
	base.CompanyName = nonEmptyOr(cmd.CompanyName, base.CompanyName)
	base.RNC = nonEmptyOr(cmd.RNC, base.RNC)
	base.Address = nonEmptyOr(cmd.Address, base.Address)
	base.Phone = nonEmptyOr(cmd.Phone, base.Phone)
	base.WhatsApp = nonEmptyOr(cmd.WhatsApp, base.WhatsApp)
	base.Email = nonEmptyOr(cmd.Email, base.Email)
	base.Instagram = nonEmptyOr(cmd.Instagram, base.Instagram)
	base.Facebook = nonEmptyOr(cmd.Facebook, base.Facebook)
	base.BusinessHours = nonEmptyOr(cmd.BusinessHours, base.BusinessHours)
	if cmd.ItbisPercent != nil {
		if *cmd.ItbisPercent < 0 {
			return domain.BusinessSettings{}, domain.ErrInvalidOperationsInput
		}
		if *cmd.ItbisPercent > 100 {
			return domain.BusinessSettings{}, domain.ErrInvalidOperationsInput
		}
		base.ItbisPercent = *cmd.ItbisPercent
	}
	if cmd.DepositPercent > 0 {
		base.DepositPercent = cmd.DepositPercent
	}
	if cmd.WhatsAppAutoAlertsEnabled != nil {
		base.WhatsAppAutoAlertsEnabled = *cmd.WhatsAppAutoAlertsEnabled
	}
	return base, nil
}

func nonEmptyOr(candidate, fallback string) string {
	trimmed := strings.TrimSpace(candidate)
	if trimmed != "" {
		return trimmed
	}
	return fallback
}

func (s *Service) ListNotifications(ctx context.Context, caller platformhttp.Principal) []domain.Notification {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListNotifications(ctx); err == nil {
			s.mu.Lock()
			s.notifications = rows
			s.mu.Unlock()
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Notification, 0, len(s.notifications))
	for _, ntf := range s.notifications {
		if isNotificationVisibleToCaller(ntf, caller) {
			out = append(out, ntf)
		}
	}
	return out
}

func isNotificationVisibleToCaller(ntf domain.Notification, caller platformhttp.Principal) bool {
	if caller.Role == "admin" {
		return ntf.RecipientRole != "client"
	}
	if caller.Role == "staff" {
		return ntf.RecipientRole == "staff"
	}
	if ntf.RecipientRole != "client" {
		return false
	}
	if ntf.UserID == "" {
		return false
	}
	return ntf.UserID == caller.ID.String()
}

// MarkNotificationReadForCaller verifica pertenencia antes de marcar la notificación como leída.
func (s *Service) MarkNotificationReadForCaller(
	ctx context.Context,
	id string,
	caller platformhttp.Principal,
) (domain.Notification, error) {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListNotifications(ctx); err == nil {
			s.mu.Lock()
			s.notifications = rows
			s.mu.Unlock()
		}
	}
	targetID := strings.TrimSpace(id)
	s.mu.Lock()
	idx := -1
	for i := range s.notifications {
		if s.notifications[i].ID == targetID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.Notification{}, domain.ErrNotificationNotFound
	}
	ntf := s.notifications[idx]
	if !isNotificationVisibleToCaller(ntf, caller) {
		s.mu.Unlock()
		return domain.Notification{}, domain.ErrNotificationForbidden
	}
	s.notifications[idx].IsRead = true
	updated := s.notifications[idx]
	store := s.store
	s.mu.Unlock()

	if store != nil {
		if store.Enabled() {
			if err := store.MarkNotificationRead(ctx, targetID); err != nil {
				return domain.Notification{}, err
			}
		}
	}
	return updated, nil
}

func (s *Service) MarkNotificationRead(ctx context.Context, id string) (domain.Notification, error) {
	adminCaller := platformhttp.Principal{Role: "admin"}
	return s.MarkNotificationReadForCaller(ctx, id, adminCaller)
}

func (s *Service) ListInsurancePolicies(ctx context.Context) []domain.InsurancePolicy {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListInsurancePolicies(ctx); err == nil {
			s.mu.Lock()
			s.policies = rows
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.InsurancePolicy, len(s.policies))
	copy(out, s.policies)
	return out
}

func (s *Service) ListAuditLogs(ctx context.Context) []domain.AuditLog {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListAuditLogs(ctx); err == nil {
			s.mu.Lock()
			s.auditLogs = rows
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.AuditLog, len(s.auditLogs))
	copy(out, s.auditLogs)
	return out
}

// RecordAuditLog registra explícitamente una entrada en la bitácora operativa de auditoría.
func (s *Service) RecordAuditLog(
	ctx context.Context,
	caller platformhttp.Principal,
	action string,
	entityType string,
	entityID string,
	summary string,
) {
	s.recordAudit(ctx, caller, action, entityType, entityID, summary)
}

func (s *Service) recordAudit(
	ctx context.Context,
	caller platformhttp.Principal,
	action string,
	entityType string,
	entityID string,
	summary string,
) {
	now := s.clock.Now()
	actorName := strings.TrimSpace(caller.FullName)
	if actorName == "" {
		actorName = "Sistema"
	}
	actorRole := strings.TrimSpace(caller.Role)
	if actorRole == "" {
		actorRole = "admin"
	}
	entry := domain.AuditLog{
		ID:             "aud-" + uuid.NewString()[:8],
		ActorName:      actorName,
		ActorRole:      actorRole,
		Action:         action,
		EntityType:     entityType,
		EntityID:       entityID,
		Summary:        summary,
		CreatedAtLabel: fmt.Sprintf("%d %s · %s", now.Day(), shortMonthLabel(int(now.Month())), now.Format("03:04 PM")),
	}
	s.mu.Lock()
	s.auditLogs = append([]domain.AuditLog{entry}, s.auditLogs...)
	s.mu.Unlock()
	if store, enabled := s.storeEnabled(); enabled {
		_ = store.UpsertAuditLog(ctx, entry, now)
	}
}

func (s *Service) ListBotFAQs(ctx context.Context) []domain.BotFAQ {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListBotFAQs(ctx); err == nil {
			s.mu.Lock()
			s.botFAQs = rows
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.BotFAQ, len(s.botFAQs))
	copy(out, s.botFAQs)
	return out
}

func (s *Service) UpsertBotFAQ(ctx context.Context, cmd UpsertBotFAQCommand) (domain.BotFAQ, error) {
	question := strings.TrimSpace(cmd.Question)
	answer := strings.TrimSpace(cmd.Answer)
	if question == "" {
		return domain.BotFAQ{}, domain.ErrInvalidOperationsInput
	}
	if answer == "" {
		return domain.BotFAQ{}, domain.ErrInvalidOperationsInput
	}
	id := strings.TrimSpace(cmd.ID)
	if id == "" {
		id = "faq-" + uuid.NewString()[:8]
	}
	category := nonEmptyOr(cmd.Category, "general")
	faq := domain.BotFAQ{
		ID:       id,
		Question: question,
		Answer:   answer,
		Category: category,
		Active:   cmd.Active,
	}
	s.saveBotFAQInMemory(faq)
	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertBotFAQ(ctx, faq); err != nil {
			return domain.BotFAQ{}, err
		}
	}
	return faq, nil
}

// DeleteBotFAQ elimina una pregunta frecuente del bot WhatsApp.
func (s *Service) DeleteBotFAQ(ctx context.Context, id string, caller platformhttp.Principal) error {
	_ = s.ListBotFAQs(ctx)
	targetID := strings.TrimSpace(id)
	if !s.removeBotFAQInMemory(targetID) {
		return domain.ErrBotEntryNotFound
	}
	if store, enabled := s.storeEnabled(); enabled {
		if delStore, ok := store.(DeletableStore); ok {
			if err := delStore.DeleteBotFAQ(ctx, targetID); err != nil {
				return err
			}
		}
	}
	s.recordAudit(ctx, caller, "DELETE_BOT_FAQ", "bot_faq", targetID, "Eliminó FAQ de bot WhatsApp")
	return nil
}

func (s *Service) removeBotFAQInMemory(targetID string) bool {
	if targetID == "" {
		return false
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	for idx := range s.botFAQs {
		if s.botFAQs[idx].ID != targetID {
			continue
		}
		s.botFAQs = append(s.botFAQs[:idx], s.botFAQs[idx+1:]...)
		return true
	}
	return false
}

func (s *Service) saveBotFAQInMemory(faq domain.BotFAQ) {
	s.mu.Lock()
	defer s.mu.Unlock()
	for idx := range s.botFAQs {
		if s.botFAQs[idx].ID == faq.ID {
			s.botFAQs[idx] = faq
			return
		}
	}
	s.botFAQs = append(s.botFAQs, faq)
}

func (s *Service) ListBotKeywords(ctx context.Context) []domain.BotKeyword {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListBotKeywords(ctx); err == nil {
			s.mu.Lock()
			s.botKeywords = rows
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.BotKeyword, len(s.botKeywords))
	copy(out, s.botKeywords)
	return out
}

func (s *Service) UpsertBotKeyword(ctx context.Context, cmd UpsertBotKeywordCommand) (domain.BotKeyword, error) {
	keyword := strings.TrimSpace(cmd.Keyword)
	autoReply := strings.TrimSpace(cmd.AutoReply)
	if keyword == "" {
		return domain.BotKeyword{}, domain.ErrInvalidOperationsInput
	}
	if autoReply == "" {
		return domain.BotKeyword{}, domain.ErrInvalidOperationsInput
	}
	id := strings.TrimSpace(cmd.ID)
	if id == "" {
		id = "kw-" + uuid.NewString()[:8]
	}
	kw := domain.BotKeyword{
		ID:             id,
		Keyword:        keyword,
		AutoReply:      autoReply,
		HandoffToHuman: cmd.HandoffToHuman,
	}
	s.saveBotKeywordInMemory(kw)
	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertBotKeyword(ctx, kw); err != nil {
			return domain.BotKeyword{}, err
		}
	}
	return kw, nil
}

// DeleteBotKeyword elimina una palabra clave automática del bot WhatsApp.
func (s *Service) DeleteBotKeyword(ctx context.Context, id string, caller platformhttp.Principal) error {
	_ = s.ListBotKeywords(ctx)
	targetID := strings.TrimSpace(id)
	if !s.removeBotKeywordInMemory(targetID) {
		return domain.ErrBotEntryNotFound
	}
	if store, enabled := s.storeEnabled(); enabled {
		if delStore, ok := store.(DeletableStore); ok {
			if err := delStore.DeleteBotKeyword(ctx, targetID); err != nil {
				return err
			}
		}
	}
	s.recordAudit(ctx, caller, "DELETE_BOT_KEYWORD", "bot_keyword", targetID, "Eliminó regla de palabra clave del bot")
	return nil
}

func (s *Service) removeBotKeywordInMemory(targetID string) bool {
	if targetID == "" {
		return false
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	for idx := range s.botKeywords {
		if s.botKeywords[idx].ID != targetID {
			continue
		}
		s.botKeywords = append(s.botKeywords[:idx], s.botKeywords[idx+1:]...)
		return true
	}
	return false
}

func (s *Service) saveBotKeywordInMemory(kw domain.BotKeyword) {
	s.mu.Lock()
	defer s.mu.Unlock()
	for idx := range s.botKeywords {
		if s.botKeywords[idx].ID == kw.ID {
			s.botKeywords[idx] = kw
			return
		}
	}
	s.botKeywords = append(s.botKeywords, kw)
}

func (s *Service) ListBotThreads(ctx context.Context) []domain.BotThread {
	if store, enabled := s.storeEnabled(); enabled {
		if rows, err := store.ListBotThreads(ctx); err == nil {
			s.mu.Lock()
			s.botThreads = rows
			s.mu.Unlock()
			return rows
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.BotThread, len(s.botThreads))
	copy(out, s.botThreads)
	return out
}

func (s *Service) UpdateBotThread(ctx context.Context, id string, cmd UpdateBotThreadCommand) (domain.BotThread, error) {
	_ = s.ListBotThreads(ctx)
	targetID := strings.TrimSpace(id)
	nowLabel := s.clock.Now().Format("03:04 PM")

	s.mu.Lock()
	idx := -1
	for i := range s.botThreads {
		if s.botThreads[i].ID == targetID {
			idx = i
			break
		}
	}
	if idx < 0 {
		s.mu.Unlock()
		return domain.BotThread{}, domain.ErrBotThreadNotFound
	}
	applyBotThreadPatch(&s.botThreads[idx], cmd, nowLabel)
	updated := s.botThreads[idx]
	s.mu.Unlock()

	if store, enabled := s.storeEnabled(); enabled {
		if err := store.UpsertBotThread(ctx, updated); err != nil {
			return domain.BotThread{}, err
		}
	}
	return updated, nil
}

func applyBotThreadPatch(thread *domain.BotThread, cmd UpdateBotThreadCommand, updatedAtLabel string) {
	if cmd.BotPaused != nil {
		thread.BotPaused = *cmd.BotPaused
	}
	if cmd.AssignedAgent != nil {
		thread.AssignedAgent = strings.TrimSpace(*cmd.AssignedAgent)
	}
	if cmd.LastMessage != nil {
		trimmed := strings.TrimSpace(*cmd.LastMessage)
		if trimmed != "" {
			thread.LastMessage = trimmed
		}
	}
	thread.UpdatedAtLabel = updatedAtLabel
}

// ExportReport calcula el reporte real mensual usando agregaciones vivas e ITBIS configurado.
func (s *Service) ExportReport(ctx context.Context, reportType, period string) (domain.ReportExport, error) {
	settings, _ := s.GetSettings(ctx)
	s.mu.RLock()
	revFn := s.octRevenueFn
	snapshotFn := s.reportSnapshotFn
	now := s.clock.Now()
	s.mu.RUnlock()

	if snapshotFn != nil {
		months := 6
		if strings.EqualFold(strings.TrimSpace(period), "12m") {
			months = 12
		}
		snapshot, err := snapshotFn.BuildReportSnapshot(ctx, months, now)
		if err != nil {
			return domain.ReportExport{}, err
		}
		snapshot.ItbisPercent = settings.ItbisPercent
		return domain.BuildReportExportFromSnapshot(reportType, period, snapshot, now)
	}
	currentRevenue := 0
	if revFn != nil {
		currentRevenue = revFn(ctx)
	}
	snapshot := domain.ReportSnapshot{
		ItbisPercent: settings.ItbisPercent,
		Months: []domain.ReportMonthMetric{
			{
				Year:     now.Year(),
				Month:    now.Month(),
				IncomeRD: currentRevenue,
			},
		},
	}
	return domain.BuildReportExportFromSnapshot(reportType, period, snapshot, now)
}

// HasSentEventReminder consulta si el recordatorio especificado ya fue enviado para un evento.
func (s *Service) HasSentEventReminder(ctx context.Context, eventID string, userID uuid.UUID, reminderKind string) (bool, error) {
	key := eventID + ":" + userID.String() + ":" + reminderKind
	if store, enabled := s.storeEnabled(); enabled {
		if remStore, ok := store.(ReminderStore); ok {
			return remStore.HasSentEventReminder(ctx, eventID, userID, reminderKind)
		}
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.sentReminders[key], nil
}

// MarkEventReminderSent registra el envío idempotente de un recordatorio de evento.
func (s *Service) MarkEventReminderSent(ctx context.Context, eventID string, userID uuid.UUID, reminderKind string) error {
	now := s.clock.Now()
	key := eventID + ":" + userID.String() + ":" + reminderKind
	s.mu.Lock()
	if s.sentReminders == nil {
		s.sentReminders = make(map[string]bool)
	}
	s.sentReminders[key] = true
	store := s.store
	s.mu.Unlock()
	if store != nil {
		if store.Enabled() {
			if remStore, ok := store.(ReminderStore); ok {
				return remStore.InsertSentEventReminder(ctx, eventID, userID, reminderKind, now)
			}
		}
	}
	return nil
}

func resolveReviewClientName(requested string, caller platformhttp.Principal) string {
	authenticated := strings.TrimSpace(caller.FullName)
	if authenticated == "" {
		authenticated = strings.TrimSpace(caller.Email)
	}
	if caller.IsClient() {
		if authenticated != "" {
			return authenticated
		}
		return "Cliente Verificado"
	}
	trimmed := strings.TrimSpace(requested)
	if trimmed != "" {
		return trimmed
	}
	if authenticated != "" {
		return authenticated
	}
	return "Cliente Verificado"
}

func shortMonthLabel(month int) string {
	months := [12]string{
		"ene", "feb", "mar", "abr", "may", "jun",
		"jul", "ago", "sep", "oct", "nov", "dic",
	}
	idx := month - 1
	if idx < 0 {
		idx = 0
	}
	if idx >= len(months) {
		idx = len(months) - 1
	}
	return months[idx]
}
