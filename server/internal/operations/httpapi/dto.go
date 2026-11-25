// Package httpapi expone las rutas HTTP de operaciones, logística, incidencias, CRM y bot WhatsApp.
package httpapi

import (
	"errors"
	"net/http"
	"strings"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
	"github.com/Jeudry/rosafiesta/server/internal/operations/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type EventLogisticsInput struct {
	EventID string `path:"eventId" doc:"Event identifier."`
}

type EventLogisticsOutput struct {
	Body struct {
		Logistics         domain.EventLogistics `json:"logistics"`
		CompletionPercent int                   `json:"completionPercent"`
	}
}

func newLogisticsOutput(logi domain.EventLogistics) *EventLogisticsOutput {
	out := &EventLogisticsOutput{}
	out.Body.Logistics = logi
	out.Body.CompletionPercent = logi.CompletionPercent()
	return out
}

type ToggleChecklistInput struct {
	EventID string `path:"eventId" doc:"Event identifier."`
	ItemID  string `path:"itemId" doc:"Checklist step identifier."`
	Body    struct {
		Completed bool   `json:"completed"`
		StaffName string `json:"staffName,omitempty"`
	}
}

type RouteStopsOutput struct {
	Body struct {
		Stops []domain.RouteStop `json:"stops"`
	}
}

// ReportDamageInput recibe los datos de daño o faltante post-evento incluyendo foto opcional.
type ReportDamageInput struct {
	EventID string `path:"eventId" doc:"Event identifier."`
	Body    struct {
		ArticleCode   string `json:"articleCode" minLength:"2"`
		ArticleName   string `json:"articleName" minLength:"2"`
		DamagedUnits  int    `json:"damagedUnits" minimum:"0"`
		MissingUnits  int    `json:"missingUnits" minimum:"0"`
		ReplacementRD int    `json:"replacementRD" minimum:"0"`
		Notes         string `json:"notes"`
		PhotoURL      string `json:"photoUrl,omitempty"`
	}
}

// ToCommand convierte el payload HTTP de daño al comando de aplicación.
func (in *ReportDamageInput) ToCommand() app.ReportDamageCommand {
	return app.ReportDamageCommand{
		EventID:       in.EventID,
		ArticleCode:   in.Body.ArticleCode,
		ArticleName:   in.Body.ArticleName,
		DamagedUnits:  in.Body.DamagedUnits,
		MissingUnits:  in.Body.MissingUnits,
		ReplacementRD: in.Body.ReplacementRD,
		Notes:         in.Body.Notes,
		PhotoURL:      in.Body.PhotoURL,
	}
}

// ResolveDamageInput recibe el identificador de la incidencia y notas de resolución opcionales.
type ResolveDamageInput struct {
	ID   string `path:"id" doc:"Damage incident identifier."`
	Body *struct {
		Notes           string `json:"notes,omitempty"`
		ResolutionNotes string `json:"resolutionNotes,omitempty"`
	}
}

// Notes extrae notas de resolución desde el cuerpo JSON opcional.
func (in *ResolveDamageInput) Notes() string {
	if in.Body == nil {
		return ""
	}
	clean := strings.TrimSpace(in.Body.ResolutionNotes)
	if clean != "" {
		return clean
	}
	return strings.TrimSpace(in.Body.Notes)
}

// SyncOfflineQueueInput acepta un lote opcional de mutaciones fuera de línea a sincronizar.
type SyncOfflineQueueInput struct {
	Body *struct {
		Mutations []app.EnqueueOfflineMutationCommand `json:"mutations,omitempty"`
	}
}

// Mutations extrae mutaciones entrantes desde un cuerpo JSON opcional.
func (in *SyncOfflineQueueInput) Mutations() []app.EnqueueOfflineMutationCommand {
	if in.Body == nil {
		return nil
	}
	return in.Body.Mutations
}

type ReportDamageOutput struct {
	Body struct {
		Report domain.DamageReport `json:"report"`
	}
}

type DamagesListInput struct {
	EventID string `query:"eventId" doc:"Optional event identifier filter."`
}

type DamagesListOutput struct {
	Body struct {
		Damages []domain.DamageReport `json:"damages"`
	}
}

type OfflineQueueOutput struct {
	Body struct {
		Queue []domain.OfflineMutation `json:"queue"`
	}
}

type ReviewsListInput struct {
	EventID string `query:"eventId" doc:"Optional event identifier filter."`
}

type ReviewsListOutput struct {
	Body struct {
		Reviews []domain.Review `json:"reviews"`
	}
}

type CreateReviewInput struct {
	Body struct {
		EventID    string `json:"eventId" minLength:"2"`
		ClientName string `json:"clientName,omitempty"`
		Rating     int    `json:"rating" minimum:"1" maximum:"5"`
		Comment    string `json:"comment" minLength:"3"`
	}
}

func (in *CreateReviewInput) ToCommand() app.CreateReviewCommand {
	return app.CreateReviewCommand{
		EventID:    in.Body.EventID,
		ClientName: in.Body.ClientName,
		Rating:     in.Body.Rating,
		Comment:    in.Body.Comment,
	}
}

type ReplyReviewInput struct {
	ID   string `path:"id" doc:"Review identifier."`
	Body struct {
		AdminReply string `json:"adminReply" minLength:"2"`
		Verified   *bool  `json:"verified,omitempty"`
	}
}

type ReviewOutput struct {
	Body struct {
		Review domain.Review `json:"review"`
	}
}

type SuppliersListInput struct {
	Category string `query:"category" doc:"Optional supplier category filter."`
}

type SuppliersListOutput struct {
	Body struct {
		Suppliers []domain.Supplier `json:"suppliers"`
	}
}

type UpsertSupplierInput struct {
	Body struct {
		ID          string `json:"id,omitempty"`
		Name        string `json:"name" minLength:"2"`
		Category    string `json:"category"`
		Specialty   string `json:"specialty"`
		ContactName string `json:"contactName,omitempty"`
		Phone       string `json:"phone"`
		Email       string `json:"email,omitempty"`
		Website     string `json:"website,omitempty"`
		Rating      string `json:"rating,omitempty"`
		Notes       string `json:"notes,omitempty"`
	}
}

func (in *UpsertSupplierInput) ToCommand() app.UpsertSupplierCommand {
	return app.UpsertSupplierCommand{
		ID:          in.Body.ID,
		Name:        in.Body.Name,
		Category:    in.Body.Category,
		Specialty:   in.Body.Specialty,
		ContactName: in.Body.ContactName,
		Phone:       in.Body.Phone,
		Email:       in.Body.Email,
		Website:     in.Body.Website,
		Rating:      in.Body.Rating,
		Notes:       in.Body.Notes,
	}
}

type SupplierOutput struct {
	Body struct {
		Supplier domain.Supplier `json:"supplier"`
	}
}

type PatchSupplierInput struct {
	ID   string `path:"id" doc:"Supplier identifier."`
	Body struct {
		Name        string `json:"name,omitempty"`
		Category    string `json:"category,omitempty"`
		Specialty   string `json:"specialty,omitempty"`
		ContactName string `json:"contactName,omitempty"`
		Phone       string `json:"phone,omitempty"`
		Email       string `json:"email,omitempty"`
		Website     string `json:"website,omitempty"`
		Rating      string `json:"rating,omitempty"`
		Notes       string `json:"notes,omitempty"`
	}
}

func (in *PatchSupplierInput) ToCommand() app.UpsertSupplierCommand {
	return app.UpsertSupplierCommand{
		ID:          in.ID,
		Name:        in.Body.Name,
		Category:    in.Body.Category,
		Specialty:   in.Body.Specialty,
		ContactName: in.Body.ContactName,
		Phone:       in.Body.Phone,
		Email:       in.Body.Email,
		Website:     in.Body.Website,
		Rating:      in.Body.Rating,
		Notes:       in.Body.Notes,
	}
}

type OperationsEntityByIDInput struct {
	ID string `path:"id" doc:"Entity identifier."`
}

type DeleteEntityOutput struct {
	Body struct {
		Deleted bool   `json:"deleted"`
		ID      string `json:"id"`
	}
}

type LeadsListInput struct {
	Stage string `query:"stage" doc:"Optional CRM pipeline stage filter."`
}

type LeadsListOutput struct {
	Body struct {
		Leads []domain.Lead `json:"leads"`
	}
}

type CreateLeadInput struct {
	Body struct {
		FullName       string `json:"fullName" minLength:"2"`
		Phone          string `json:"phone" minLength:"5"`
		Email          string `json:"email,omitempty"`
		EventType      string `json:"eventType" minLength:"2"`
		EventDateLabel string `json:"eventDateLabel,omitempty"`
		Guests         int    `json:"guests,omitempty"`
		EstimatedDop   int    `json:"estimatedDop,omitempty"`
		Stage          string `json:"stage,omitempty"`
		Notes          string `json:"notes,omitempty"`
	}
}

func (in *CreateLeadInput) ToCommand() app.CreateLeadCommand {
	return app.CreateLeadCommand{
		FullName:       in.Body.FullName,
		Phone:          in.Body.Phone,
		Email:          in.Body.Email,
		EventType:      in.Body.EventType,
		EventDateLabel: in.Body.EventDateLabel,
		Guests:         in.Body.Guests,
		EstimatedDop:   in.Body.EstimatedDop,
		Stage:          in.Body.Stage,
		Notes:          in.Body.Notes,
	}
}

type UpdateLeadInput struct {
	ID   string `path:"id" doc:"Lead identifier."`
	Body struct {
		Stage        *string `json:"stage,omitempty"`
		EstimatedDop *int    `json:"estimatedDop,omitempty"`
		Notes        *string `json:"notes,omitempty"`
	}
}

func (in *UpdateLeadInput) ToCommand() app.UpdateLeadCommand {
	return app.UpdateLeadCommand{
		Stage:        in.Body.Stage,
		EstimatedDop: in.Body.EstimatedDop,
		Notes:        in.Body.Notes,
	}
}

type LeadOutput struct {
	Body struct {
		Lead domain.Lead `json:"lead"`
	}
}

type PublicSettingsOutput struct {
	Body struct {
		Settings domain.PublicBusinessSettings `json:"settings"`
	}
}

type SettingsOutput struct {
	Body struct {
		Settings      domain.BusinessSettings `json:"settings"`
		DeliveryZones []domain.DeliveryZone   `json:"deliveryZones"`
	}
}

type UpdateSettingsInput struct {
	Body struct {
		CompanyName               string                `json:"companyName,omitempty"`
		RNC                       string                `json:"rnc,omitempty"`
		Address                   string                `json:"address,omitempty"`
		Phone                     string                `json:"phone,omitempty"`
		WhatsApp                  string                `json:"whatsapp,omitempty"`
		Email                     string                `json:"email,omitempty"`
		Instagram                 string                `json:"instagram,omitempty"`
		Facebook                  string                `json:"facebook,omitempty"`
		BusinessHours             string                `json:"businessHours,omitempty"`
		ItbisPercent              *int                  `json:"itbisPercent,omitempty" minimum:"0" maximum:"100"`
		DepositPercent            int                   `json:"depositPercent,omitempty"`
		WhatsAppAutoAlertsEnabled *bool                 `json:"whatsappAutoAlertsEnabled,omitempty"`
		DeliveryZones             []domain.DeliveryZone `json:"deliveryZones,omitempty"`
	}
}

func (in *UpdateSettingsInput) ToCommand() app.UpdateSettingsCommand {
	return app.UpdateSettingsCommand{
		CompanyName:               in.Body.CompanyName,
		RNC:                       in.Body.RNC,
		Address:                   in.Body.Address,
		Phone:                     in.Body.Phone,
		WhatsApp:                  in.Body.WhatsApp,
		Email:                     in.Body.Email,
		Instagram:                 in.Body.Instagram,
		Facebook:                  in.Body.Facebook,
		BusinessHours:             in.Body.BusinessHours,
		ItbisPercent:              in.Body.ItbisPercent,
		DepositPercent:            in.Body.DepositPercent,
		WhatsAppAutoAlertsEnabled: in.Body.WhatsAppAutoAlertsEnabled,
		DeliveryZones:             in.Body.DeliveryZones,
		HasDeliveryZones:          in.Body.DeliveryZones != nil,
	}
}

type NotificationsOutput struct {
	Body struct {
		Notifications []domain.Notification `json:"notifications"`
	}
}

type NotificationByIDInput struct {
	ID string `path:"id" doc:"Notification identifier."`
}

type NotificationOutput struct {
	Body struct {
		Notification domain.Notification `json:"notification"`
	}
}

type InsuranceOutput struct {
	Body struct {
		Policies []domain.InsurancePolicy `json:"policies"`
	}
}

type AuditLogsOutput struct {
	Body struct {
		Logs []domain.AuditLog `json:"logs"`
	}
}

type BotFAQsOutput struct {
	Body struct {
		FAQs []domain.BotFAQ `json:"faqs"`
	}
}

type UpsertBotFAQInput struct {
	Body struct {
		ID       string `json:"id,omitempty"`
		Question string `json:"question" minLength:"3"`
		Answer   string `json:"answer" minLength:"3"`
		Category string `json:"category,omitempty"`
		Active   bool   `json:"active"`
	}
}

func (in *UpsertBotFAQInput) ToCommand() app.UpsertBotFAQCommand {
	return app.UpsertBotFAQCommand{
		ID:       in.Body.ID,
		Question: in.Body.Question,
		Answer:   in.Body.Answer,
		Category: in.Body.Category,
		Active:   in.Body.Active,
	}
}

type BotFAQOutput struct {
	Body struct {
		FAQ domain.BotFAQ `json:"faq"`
	}
}

type BotKeywordsOutput struct {
	Body struct {
		Keywords []domain.BotKeyword `json:"keywords"`
	}
}

type UpsertBotKeywordInput struct {
	Body struct {
		ID             string `json:"id,omitempty"`
		Keyword        string `json:"keyword" minLength:"2"`
		AutoReply      string `json:"autoReply" minLength:"3"`
		HandoffToHuman bool   `json:"handoffToHuman"`
	}
}

func (in *UpsertBotKeywordInput) ToCommand() app.UpsertBotKeywordCommand {
	return app.UpsertBotKeywordCommand{
		ID:             in.Body.ID,
		Keyword:        in.Body.Keyword,
		AutoReply:      in.Body.AutoReply,
		HandoffToHuman: in.Body.HandoffToHuman,
	}
}

type BotKeywordOutput struct {
	Body struct {
		Keyword domain.BotKeyword `json:"keyword"`
	}
}

type BotThreadsOutput struct {
	Body struct {
		Threads []domain.BotThread `json:"threads"`
	}
}

type UpdateBotThreadInput struct {
	ID   string `path:"id" doc:"WhatsApp bot thread identifier."`
	Body struct {
		BotPaused     *bool   `json:"botPaused,omitempty"`
		AssignedAgent *string `json:"assignedAgent,omitempty"`
		LastMessage   *string `json:"lastMessage,omitempty"`
	}
}

func (in *UpdateBotThreadInput) ToCommand() app.UpdateBotThreadCommand {
	return app.UpdateBotThreadCommand{
		BotPaused:     in.Body.BotPaused,
		AssignedAgent: in.Body.AssignedAgent,
		LastMessage:   in.Body.LastMessage,
	}
}

type BotThreadOutput struct {
	Body struct {
		Thread domain.BotThread `json:"thread"`
	}
}

type ReportExportInput struct {
	Type   string `query:"type" doc:"Report type: income, events, clients, inventory, or dgii."`
	Period string `query:"period" doc:"Report period: 6m or 12m."`
}

type ReportExportOutput struct {
	Body struct {
		Report domain.ReportExport `json:"report"`
	}
}

// SendWhatsAppReplyInput solicita enviar un mensaje saliente por WhatsApp Cloud API.
type SendWhatsAppReplyInput struct {
	Body struct {
		ThreadID       string `json:"threadId,omitempty"`
		RecipientPhone string `json:"recipientPhone,omitempty"`
		Message        string `json:"message" minLength:"1"`
	}
}

// VerifyWhatsAppWebhookInput captura los parámetros de verificación de Meta Cloud API.
type VerifyWhatsAppWebhookInput struct {
	Mode      string `query:"hub.mode"`
	Token     string `query:"hub.verify_token"`
	Challenge string `query:"hub.challenge"`
}

// PlainTextOutput devuelve el challenge en texto plano esperado por Meta Webhook.
type PlainTextOutput struct {
	ContentType string `header:"Content-Type"`
	Body        []byte
}

// IngestWhatsAppWebhookInput recibe la carga cruda del webhook de Meta WhatsApp.
type IngestWhatsAppWebhookInput struct {
	Signature256 string `header:"X-Hub-Signature-256"`
	RawBody      []byte
}

// WhatsAppWebhookAckOutput confirma la recepción del webhook de WhatsApp.
type WhatsAppWebhookAckOutput struct {
	Body struct {
		Received bool `json:"received"`
	}
}

type operationsErrorSpec struct {
	target error
	status int
	code   string
	policy string
}

var operationsErrorSpecs = []operationsErrorSpec{
	{domain.ErrWhatsAppNotConfigured, http.StatusServiceUnavailable, "WHATSAPP_PROVIDER_NOT_CONFIGURED", "WhatsAppCloudAPIConfiguration"},
	{domain.ErrWhatsAppVerifyFailed, http.StatusForbidden, "WHATSAPP_WEBHOOK_FORBIDDEN", "WhatsAppWebhookTokenVerification"},
	{domain.ErrWebhookSignatureInvalid, http.StatusForbidden, "WHATSAPP_SIGNATURE_INVALID", "WhatsAppWebhookSignatureVerification"},
	{domain.ErrNotificationForbidden, http.StatusForbidden, "NOTIFICATION_FORBIDDEN", "NotificationOwnershipPolicy"},
	{domain.ErrOfflineActionForbidden, http.StatusForbidden, "OFFLINE_ACTION_FORBIDDEN", "OfflineSyncPermissionPolicy"},
	{domain.ErrOperationForbidden, http.StatusForbidden, "OPERATION_FORBIDDEN", "EventOperationOwnershipPolicy"},
	{domain.ErrReviewForbidden, http.StatusForbidden, "REVIEW_FORBIDDEN", "ReviewEventOwnershipPolicy"},
	{domain.ErrReviewEventNotFound, http.StatusNotFound, "REVIEW_EVENT_NOT_FOUND", "ReviewEventMustExist"},
}

func mapOperationsError(err error, endpoint, defaultCode, policy string, defaultStatus int) *platformhttp.APIError {
	for _, spec := range operationsErrorSpecs {
		if errors.Is(err, spec.target) {
			return platformhttp.NewAPIError(spec.status, spec.code, err.Error(), endpoint, spec.policy, err.Error())
		}
	}
	if isOperationsNotFound(err) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			defaultCode,
			err.Error(),
			endpoint,
			policy,
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		defaultStatus,
		defaultCode,
		err.Error(),
		endpoint,
		policy,
		err.Error(),
	)
}

func isOperationsNotFound(err error) bool {
	if errors.Is(err, domain.ErrOperationNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrChecklistNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrDamageIncidentNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrReviewNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrLeadNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrNotificationNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrSupplierNotFound) {
		return true
	}
	if errors.Is(err, domain.ErrBotEntryNotFound) {
		return true
	}
	return errors.Is(err, domain.ErrBotThreadNotFound)
}
