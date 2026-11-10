package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/operations/app"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type Module struct {
	Service *app.Service
	Auth    platformhttp.PrincipalResolver
}

func (m Module) Register(api huma.API) {
	m.registerLogisticsAndDamageRoutes(api)
	m.registerRouteAndOfflineHandlers(api)
	m.registerReviewsRoutes(api)
	m.registerSuppliersAndLeadsRoutes(api)
	m.registerSettingsAndNotificationsRoutes(api)
	m.registerGovernanceAndReportsRoutes(api)
	m.registerBotFAQAndKeywordRoutes(api)
	m.registerBotThreadRoutes(api)
}

func (m Module) authMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)}
}

func (m Module) permMiddlewares(api huma.API, perm string) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequirePermission(api, m.Auth, perm)}
}

func (m Module) anyPermMiddlewares(api huma.API, perms ...string) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequireAnyPermission(api, m.Auth, perms...)}
}

func (m Module) registerLogisticsAndDamageRoutes(api huma.API) {
	fieldMW := m.anyPermMiddlewares(api, "tareas", "eventos", "inventario", "decoracion")

	huma.Register(api, huma.Operation{
		OperationID: "operations-get-event-logistics",
		Method:      http.MethodGet,
		Path:        "/v1/operations/events/{eventId}",
		Summary:     "Get event logistics, crew, and setup checklist",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleGetEventLogistics)

	huma.Register(api, huma.Operation{
		OperationID: "operations-toggle-checklist-item",
		Method:      http.MethodPatch,
		Path:        "/v1/operations/events/{eventId}/checklist/{itemId}",
		Summary:     "Toggle a setup or teardown checklist item",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleToggleChecklist)

	huma.Register(api, huma.Operation{
		OperationID: "operations-report-damage",
		Method:      http.MethodPost,
		Path:        "/v1/operations/events/{eventId}/damages",
		Summary:     "Record a post-event damage or missing item incident",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleReportDamage)

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-damages",
		Method:      http.MethodGet,
		Path:        "/v1/operations/damages",
		Summary:     "List recorded damage and missing item incidents",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleListDamages)
}

func (m Module) registerRouteAndOfflineHandlers(api huma.API) {
	fieldMW := m.anyPermMiddlewares(api, "tareas", "eventos", "inventario", "decoracion")

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-route-stops",
		Method:      http.MethodGet,
		Path:        "/v1/operations/routes/today",
		Summary:     "List daily delivery and pickup route stops",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleListRouteStops)

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-offline-queue",
		Method:      http.MethodGet,
		Path:        "/v1/operations/offline-queue",
		Summary:     "List pending offline field mutations",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleListOfflineQueue)

	huma.Register(api, huma.Operation{
		OperationID: "operations-sync-offline-queue",
		Method:      http.MethodPost,
		Path:        "/v1/operations/offline-queue/sync",
		Summary:     "Synchronize pending offline mutations with PostgreSQL",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: fieldMW,
	}, m.handleSyncOfflineQueue)
}

func (m Module) registerReviewsRoutes(api huma.API) {
	authMW := m.authMiddlewares(api)
	replyMW := m.anyPermMiddlewares(api, "eventos", "ajustes")

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-reviews",
		Method:      http.MethodGet,
		Path:        "/v1/reviews",
		Summary:     "List verified client reviews",
		Tags:        []string{"Operations"},
	}, m.handleListReviews)

	huma.Register(api, huma.Operation{
		OperationID: "operations-create-review",
		Method:      http.MethodPost,
		Path:        "/v1/reviews",
		Summary:     "Submit a verified event review",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleCreateReview)

	huma.Register(api, huma.Operation{
		OperationID: "operations-reply-review",
		Method:      http.MethodPatch,
		Path:        "/v1/reviews/{id}/reply",
		Summary:     "Publish an official response to a client review",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: replyMW,
	}, m.handleReplyReview)
}

func (m Module) registerSuppliersAndLeadsRoutes(api huma.API) {
	supMW := m.anyPermMiddlewares(api, "inventario", "eventos", "ajustes")
	leadMW := m.anyPermMiddlewares(api, "eventos", "ajustes")

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-suppliers",
		Method:      http.MethodGet,
		Path:        "/v1/operations/suppliers",
		Summary:     "List external suppliers and partners",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: supMW,
	}, m.handleListSuppliers)

	huma.Register(api, huma.Operation{
		OperationID: "operations-upsert-supplier",
		Method:      http.MethodPost,
		Path:        "/v1/operations/suppliers",
		Summary:     "Create or update a supplier directory entry",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: supMW,
	}, m.handleUpsertSupplier)

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-leads",
		Method:      http.MethodGet,
		Path:        "/v1/operations/leads",
		Summary:     "List CRM pipeline leads",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: leadMW,
	}, m.handleListLeads)

	huma.Register(api, huma.Operation{
		OperationID: "operations-create-lead",
		Method:      http.MethodPost,
		Path:        "/v1/operations/leads",
		Summary:     "Create a new CRM lead",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: leadMW,
	}, m.handleCreateLead)

	huma.Register(api, huma.Operation{
		OperationID: "operations-update-lead",
		Method:      http.MethodPatch,
		Path:        "/v1/operations/leads/{id}",
		Summary:     "Update CRM lead stage, estimate, or notes",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: leadMW,
	}, m.handleUpdateLead)
}

func (m Module) registerSettingsAndNotificationsRoutes(api huma.API) {
	settingsMW := m.permMiddlewares(api, "ajustes")
	authMW := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "operations-get-public-settings",
		Method:      http.MethodGet,
		Path:        "/v1/operations/settings/public",
		Summary:     "Get public Santo Domingo showroom, contact, and delivery zone info",
		Tags:        []string{"Operations"},
	}, m.handleGetPublicSettings)

	huma.Register(api, huma.Operation{
		OperationID: "operations-get-settings",
		Method:      http.MethodGet,
		Path:        "/v1/operations/settings",
		Summary:     "Get full administrative business settings and delivery zones",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: settingsMW,
	}, m.handleGetSettings)

	huma.Register(api, huma.Operation{
		OperationID: "operations-update-settings",
		Method:      http.MethodPut,
		Path:        "/v1/operations/settings",
		Summary:     "Update administrative business settings and delivery zones",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: settingsMW,
	}, m.handleUpdateSettings)

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-notifications",
		Method:      http.MethodGet,
		Path:        "/v1/notifications",
		Summary:     "List notifications visible to the authenticated user",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleListNotifications)

	huma.Register(api, huma.Operation{
		OperationID: "operations-mark-notification-read",
		Method:      http.MethodPatch,
		Path:        "/v1/notifications/{id}/read",
		Summary:     "Mark a notification as read",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleMarkNotificationRead)
}

func (m Module) registerGovernanceAndReportsRoutes(api huma.API) {
	insMW := m.anyPermMiddlewares(api, "inventario", "ajustes")
	auditMW := m.permMiddlewares(api, "ajustes")
	reportMW := m.permMiddlewares(api, "reportes")

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-insurance",
		Method:      http.MethodGet,
		Path:        "/v1/operations/insurance",
		Summary:     "List active inventory and fleet insurance policies",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: insMW,
	}, m.handleListInsurance)

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-audit-logs",
		Method:      http.MethodGet,
		Path:        "/v1/operations/audit-logs",
		Summary:     "List administrative and operational audit trail entries",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: auditMW,
	}, m.handleListAuditLogs)

	huma.Register(api, huma.Operation{
		OperationID: "operations-export-report",
		Method:      http.MethodGet,
		Path:        "/v1/reports/export",
		Summary:     "Export analytical or DGII ITBIS report with 6m/12m series and CSV",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: reportMW,
	}, m.handleExportReport)
}

func (m Module) registerBotFAQAndKeywordRoutes(api huma.API) {
	botMW := m.anyPermMiddlewares(api, "eventos", "ajustes")

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-bot-faqs",
		Method:      http.MethodGet,
		Path:        "/v1/operations/bot/faqs",
		Summary:     "List WhatsApp bot automated FAQ entries",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: botMW,
	}, m.handleListBotFAQs)

	huma.Register(api, huma.Operation{
		OperationID: "operations-upsert-bot-faq",
		Method:      http.MethodPost,
		Path:        "/v1/operations/bot/faqs",
		Summary:     "Create or update a WhatsApp bot FAQ entry",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: botMW,
	}, m.handleUpsertBotFAQ)

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-bot-keywords",
		Method:      http.MethodGet,
		Path:        "/v1/operations/bot/keywords",
		Summary:     "List WhatsApp bot keyword triggers",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: botMW,
	}, m.handleListBotKeywords)

	huma.Register(api, huma.Operation{
		OperationID: "operations-upsert-bot-keyword",
		Method:      http.MethodPost,
		Path:        "/v1/operations/bot/keywords",
		Summary:     "Create or update a WhatsApp bot keyword trigger",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: botMW,
	}, m.handleUpsertBotKeyword)
}

func (m Module) registerBotThreadRoutes(api huma.API) {
	botMW := m.anyPermMiddlewares(api, "eventos", "ajustes")

	huma.Register(api, huma.Operation{
		OperationID: "operations-list-bot-threads",
		Method:      http.MethodGet,
		Path:        "/v1/operations/bot/threads",
		Summary:     "List WhatsApp customer conversation threads and handoff states",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: botMW,
	}, m.handleListBotThreads)

	huma.Register(api, huma.Operation{
		OperationID: "operations-update-bot-thread",
		Method:      http.MethodPatch,
		Path:        "/v1/operations/bot/threads/{id}",
		Summary:     "Pause or resume WhatsApp bot and assign a human agent",
		Tags:        []string{"Operations"},
		Security:    platformhttp.Secured(),
		Middlewares: botMW,
	}, m.handleUpdateBotThread)
}

func (m Module) handleGetEventLogistics(ctx context.Context, in *EventLogisticsInput) (*EventLogisticsOutput, error) {
	logi, err := m.Service.GetEventLogistics(ctx, in.EventID)
	if err != nil {
		return nil, mapOperationsError(err, "GET /v1/operations/events/"+in.EventID, "LOGISTICS_NOT_FOUND", "EventLogisticsMustExist", http.StatusNotFound)
	}
	return newLogisticsOutput(logi), nil
}

func (m Module) handleToggleChecklist(ctx context.Context, in *ToggleChecklistInput) (*EventLogisticsOutput, error) {
	logi, err := m.Service.ToggleChecklistItem(ctx, in.EventID, in.ItemID, in.Body.Completed, in.Body.StaffName)
	if err != nil {
		return nil, mapOperationsError(err, "PATCH /v1/operations/events/"+in.EventID+"/checklist/"+in.ItemID, "CHECKLIST_ITEM_NOT_FOUND", "ChecklistItemMustExist", http.StatusNotFound)
	}
	return newLogisticsOutput(logi), nil
}

func (m Module) handleReportDamage(ctx context.Context, in *ReportDamageInput) (*ReportDamageOutput, error) {
	report, err := m.Service.ReportDamage(
		ctx,
		in.EventID,
		in.Body.ArticleCode,
		in.Body.ArticleName,
		in.Body.DamagedUnits,
		in.Body.MissingUnits,
		in.Body.ReplacementRD,
		in.Body.Notes,
	)
	if err != nil {
		return nil, mapOperationsError(err, "POST /v1/operations/events/"+in.EventID+"/damages", "DAMAGE_REPORT_INVALID", "ReportDamageValidation", http.StatusBadRequest)
	}
	out := &ReportDamageOutput{}
	out.Body.Report = report
	return out, nil
}

func (m Module) handleListDamages(ctx context.Context, in *DamagesListInput) (*DamagesListOutput, error) {
	out := &DamagesListOutput{}
	out.Body.Damages = m.Service.ListDamages(ctx, in.EventID)
	return out, nil
}

func (m Module) handleListRouteStops(_ context.Context, _ *struct{}) (*RouteStopsOutput, error) {
	out := &RouteStopsOutput{}
	out.Body.Stops = m.Service.ListRouteStops()
	return out, nil
}

func (m Module) handleListOfflineQueue(ctx context.Context, _ *struct{}) (*OfflineQueueOutput, error) {
	out := &OfflineQueueOutput{}
	out.Body.Queue = m.Service.ListOfflineQueue(ctx)
	return out, nil
}

func (m Module) handleSyncOfflineQueue(ctx context.Context, _ *struct{}) (*OfflineQueueOutput, error) {
	out := &OfflineQueueOutput{}
	out.Body.Queue = m.Service.SyncOfflineQueue(ctx)
	return out, nil
}

func (m Module) handleListReviews(ctx context.Context, in *ReviewsListInput) (*ReviewsListOutput, error) {
	out := &ReviewsListOutput{}
	out.Body.Reviews = m.Service.ListReviews(ctx, in.EventID)
	return out, nil
}

func (m Module) handleCreateReview(ctx context.Context, in *CreateReviewInput) (*ReviewOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	rev, err := m.Service.CreateReview(ctx, cmd, caller)
	if err != nil {
		return nil, mapOperationsError(err, "POST /v1/reviews", "REVIEW_INVALID", "CreateReviewValidation", http.StatusBadRequest)
	}
	out := &ReviewOutput{}
	out.Body.Review = rev
	return out, nil
}

func (m Module) handleReplyReview(ctx context.Context, in *ReplyReviewInput) (*ReviewOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	rev, err := m.Service.ReplyReview(ctx, in.ID, in.Body.AdminReply, caller)
	if err != nil {
		return nil, mapOperationsError(err, "PATCH /v1/reviews/"+in.ID+"/reply", "REVIEW_REPLY_FAILED", "ReplyReviewValidation", http.StatusBadRequest)
	}
	out := &ReviewOutput{}
	out.Body.Review = rev
	return out, nil
}

func (m Module) handleListSuppliers(ctx context.Context, in *SuppliersListInput) (*SuppliersListOutput, error) {
	out := &SuppliersListOutput{}
	out.Body.Suppliers = m.Service.ListSuppliers(ctx, in.Category)
	return out, nil
}

func (m Module) handleUpsertSupplier(ctx context.Context, in *UpsertSupplierInput) (*SupplierOutput, error) {
	cmd := in.ToCommand()
	sup, err := m.Service.UpsertSupplier(ctx, cmd)
	if err != nil {
		return nil, mapOperationsError(err, "POST /v1/operations/suppliers", "SUPPLIER_INVALID", "UpsertSupplierValidation", http.StatusBadRequest)
	}
	out := &SupplierOutput{}
	out.Body.Supplier = sup
	return out, nil
}

func (m Module) handleListLeads(ctx context.Context, in *LeadsListInput) (*LeadsListOutput, error) {
	out := &LeadsListOutput{}
	out.Body.Leads = m.Service.ListLeads(ctx, in.Stage)
	return out, nil
}

func (m Module) handleCreateLead(ctx context.Context, in *CreateLeadInput) (*LeadOutput, error) {
	cmd := in.ToCommand()
	lead, err := m.Service.CreateLead(ctx, cmd)
	if err != nil {
		return nil, mapOperationsError(err, "POST /v1/operations/leads", "LEAD_INVALID", "CreateLeadValidation", http.StatusBadRequest)
	}
	out := &LeadOutput{}
	out.Body.Lead = lead
	return out, nil
}

func (m Module) handleUpdateLead(ctx context.Context, in *UpdateLeadInput) (*LeadOutput, error) {
	cmd := in.ToCommand()
	lead, err := m.Service.UpdateLead(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapOperationsError(err, "PATCH /v1/operations/leads/"+in.ID, "LEAD_NOT_FOUND", "UpdateLeadValidation", http.StatusBadRequest)
	}
	out := &LeadOutput{}
	out.Body.Lead = lead
	return out, nil
}

func (m Module) handleGetPublicSettings(ctx context.Context, _ *struct{}) (*PublicSettingsOutput, error) {
	out := &PublicSettingsOutput{}
	out.Body.Settings = m.Service.GetPublicSettings(ctx)
	return out, nil
}

func (m Module) handleGetSettings(ctx context.Context, _ *struct{}) (*SettingsOutput, error) {
	settings, zones := m.Service.GetSettings(ctx)
	out := &SettingsOutput{}
	out.Body.Settings = settings
	out.Body.DeliveryZones = zones
	return out, nil
}

func (m Module) handleUpdateSettings(ctx context.Context, in *UpdateSettingsInput) (*SettingsOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	settings, zones, err := m.Service.UpdateSettings(ctx, cmd, caller)
	if err != nil {
		return nil, mapOperationsError(err, "PUT /v1/operations/settings", "SETTINGS_UPDATE_FAILED", "UpdateSettingsValidation", http.StatusBadRequest)
	}
	out := &SettingsOutput{}
	out.Body.Settings = settings
	out.Body.DeliveryZones = zones
	return out, nil
}

func (m Module) handleListNotifications(ctx context.Context, _ *struct{}) (*NotificationsOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	out := &NotificationsOutput{}
	out.Body.Notifications = m.Service.ListNotifications(ctx, caller)
	return out, nil
}

func (m Module) handleMarkNotificationRead(ctx context.Context, in *NotificationByIDInput) (*NotificationOutput, error) {
	ntf, err := m.Service.MarkNotificationRead(ctx, in.ID)
	if err != nil {
		return nil, mapOperationsError(err, "PATCH /v1/notifications/"+in.ID+"/read", "NOTIFICATION_NOT_FOUND", "NotificationMustExist", http.StatusNotFound)
	}
	out := &NotificationOutput{}
	out.Body.Notification = ntf
	return out, nil
}

func (m Module) handleListInsurance(ctx context.Context, _ *struct{}) (*InsuranceOutput, error) {
	out := &InsuranceOutput{}
	out.Body.Policies = m.Service.ListInsurancePolicies(ctx)
	return out, nil
}

func (m Module) handleListAuditLogs(ctx context.Context, _ *struct{}) (*AuditLogsOutput, error) {
	out := &AuditLogsOutput{}
	out.Body.Logs = m.Service.ListAuditLogs(ctx)
	return out, nil
}

func (m Module) handleExportReport(ctx context.Context, in *ReportExportInput) (*ReportExportOutput, error) {
	report, err := m.Service.ExportReport(ctx, in.Type, in.Period)
	if err != nil {
		return nil, mapOperationsError(err, "GET /v1/reports/export", "REPORT_EXPORT_INVALID", "ExportReportValidation", http.StatusBadRequest)
	}
	out := &ReportExportOutput{}
	out.Body.Report = report
	return out, nil
}

func (m Module) handleListBotFAQs(ctx context.Context, _ *struct{}) (*BotFAQsOutput, error) {
	out := &BotFAQsOutput{}
	out.Body.FAQs = m.Service.ListBotFAQs(ctx)
	return out, nil
}

func (m Module) handleUpsertBotFAQ(ctx context.Context, in *UpsertBotFAQInput) (*BotFAQOutput, error) {
	cmd := in.ToCommand()
	faq, err := m.Service.UpsertBotFAQ(ctx, cmd)
	if err != nil {
		return nil, mapOperationsError(err, "POST /v1/operations/bot/faqs", "BOT_FAQ_INVALID", "UpsertBotFAQValidation", http.StatusBadRequest)
	}
	out := &BotFAQOutput{}
	out.Body.FAQ = faq
	return out, nil
}

func (m Module) handleListBotKeywords(ctx context.Context, _ *struct{}) (*BotKeywordsOutput, error) {
	out := &BotKeywordsOutput{}
	out.Body.Keywords = m.Service.ListBotKeywords(ctx)
	return out, nil
}

func (m Module) handleUpsertBotKeyword(ctx context.Context, in *UpsertBotKeywordInput) (*BotKeywordOutput, error) {
	cmd := in.ToCommand()
	kw, err := m.Service.UpsertBotKeyword(ctx, cmd)
	if err != nil {
		return nil, mapOperationsError(err, "POST /v1/operations/bot/keywords", "BOT_KEYWORD_INVALID", "UpsertBotKeywordValidation", http.StatusBadRequest)
	}
	out := &BotKeywordOutput{}
	out.Body.Keyword = kw
	return out, nil
}

func (m Module) handleListBotThreads(ctx context.Context, _ *struct{}) (*BotThreadsOutput, error) {
	out := &BotThreadsOutput{}
	out.Body.Threads = m.Service.ListBotThreads(ctx)
	return out, nil
}

func (m Module) handleUpdateBotThread(ctx context.Context, in *UpdateBotThreadInput) (*BotThreadOutput, error) {
	cmd := in.ToCommand()
	th, err := m.Service.UpdateBotThread(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapOperationsError(err, "PATCH /v1/operations/bot/threads/"+in.ID, "BOT_THREAD_NOT_FOUND", "UpdateBotThreadValidation", http.StatusBadRequest)
	}
	out := &BotThreadOutput{}
	out.Body.Thread = th
	return out, nil
}
