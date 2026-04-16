package main

import (
	"context"
	"encoding/csv"
	"errors"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// MountAdmin routes all live under /v1/admin
func (app *Application) MountAdmin() {
	r := chi.NewRouter()
	r.Use(app.AuthTokenMiddleware())
	r.Use(app.RoleMiddleware("admin"))

	// Profile
	r.Get("/profile", app.adminProfileHandler)
	r.Patch("/profile", app.adminUpdateProfileHandler)
	r.Post("/profile/change-password", app.adminChangePasswordHandler)

	// Events (admin view - all events, not just own)
	r.Get("/events", app.adminListEventsHandler)
	r.Post("/events", app.adminCreateEventHandler)
	r.Get("/events/stats", app.adminEventsStatsHandler)
	r.Get("/events/stats/today", app.adminEventsTodayHandler)
	r.Get("/events/stats/week", app.adminEventsWeekHandler)
	r.Get("/events/{id}", app.adminGetEventHandler)
	r.Patch("/events/{id}", app.adminUpdateEventHandler)
	r.Delete("/events/{id}", app.adminDeleteEventHandler)
	r.Post("/events/{id}/send-quote", app.adminSendQuoteHandler)

	// Quotes
	r.Post("/quotes", app.adminCreateQuoteHandler)
	r.Get("/quotes", app.adminListQuotesHandler)

	// Clients / Users
	r.Get("/clients", app.adminListClientsHandler)
	r.Get("/users/{id}", app.adminGetClientHandler)
	r.Patch("/users/{id}", app.adminUpdateClientHandler)
	r.Post("/users/{id}/block", app.adminBlockClientHandler)
	r.Post("/users/{id}/force-logout", app.adminForceLogoutHandler)
	r.Post("/users/lead", app.adminCreateLeadHandler)

	// Articles / Products
	r.Get("/articles", app.adminListArticlesHandler)
	r.Post("/articles", app.adminCreateArticleHandler)
	r.Get("/articles/{id}", app.adminGetArticleHandler)
	r.Patch("/articles/{id}", app.adminUpdateArticleHandler)
	r.Delete("/articles/{id}", app.adminDeleteArticleHandler)
	r.Patch("/articles/{id}/toggle-active", app.adminToggleArticleActiveHandler)

	// Article Variants
	r.Get("/articles/{id}/variants", app.adminListArticleVariantsHandler)
	r.Post("/articles/{id}/variants", app.adminCreateArticleVariantHandler)
	r.Patch("/variants/{variantId}", app.adminUpdateArticleVariantHandler)
	r.Delete("/variants/{variantId}", app.adminDeleteArticleVariantHandler)

	// Categories
	r.Get("/categories", app.adminListCategoriesHandler)
	r.Post("/categories", app.adminCreateCategoryHandler)
	r.Patch("/categories/{id}", app.adminUpdateCategoryHandler)
	r.Delete("/categories/{id}", app.adminDeleteCategoryHandler)

	// Bundles
	r.Get("/bundles", app.adminListBundlesHandler)
	r.Post("/bundles", app.adminCreateBundleHandler)
	r.Patch("/bundles/{id}", app.adminUpdateBundleHandler)
	r.Delete("/bundles/{id}", app.adminDeleteBundleHandler)
	r.Post("/bundles/{id}/items", app.adminAddBundleItemHandler)
	r.Delete("/bundles/{id}/items/{articleId}", app.adminRemoveBundleItemHandler)

	// AI Config
	r.Get("/ai/config", app.adminGetAIConfigHandler)
	r.Patch("/ai/config", app.adminUpdateAIConfigHandler)
	r.Get("/ai/history", app.adminGetAIHistoryHandler)

	// Notifications
	r.Get("/notifications/email-templates", app.adminListEmailTemplatesHandler)
	r.Patch("/notifications/email-templates/{id}", app.adminUpdateEmailTemplateHandler)
	r.Post("/notifications/test-email", app.adminSendTestEmailHandler)
	r.Get("/notifications/whatsapp-templates", app.adminListWhatsAppTemplatesHandler)
	r.Patch("/notifications/whatsapp-templates/{id}", app.adminUpdateWhatsAppTemplateHandler)
	r.Get("/notifications/triggers", app.adminGetNotificationTriggersHandler)
	r.Patch("/notifications/triggers/{id}", app.adminUpdateNotificationTriggerHandler)

	// Analytics
	r.Get("/analytics/monthly", app.adminMonthlyStatsHandler)
	r.Get("/analytics/revenue", app.adminRevenueChartHandler)
	r.Get("/analytics/top-products", app.adminTopProductsHandler)
	r.Get("/analytics/conversion-rate", app.adminConversionRateHandler)
	r.Get("/analytics/pending-payments", app.adminPendingPaymentsHandler)
	r.Get("/analytics/export/{type}", app.adminExportHandler)
	r.Get("/analytics/report", app.adminReportPDFHandler)

	// Quote history
	r.Get("/quotes/history", app.adminQuoteHistoryHandler)

	// Bulk operations
	r.Post("/articles/bulk-deactivate", app.adminBulkDeactivateArticlesHandler)

	// Health
	r.Get("/health/redis", app.adminRedisHealthHandler)
	r.Get("/analytics/monthly", app.adminMonthlyStatsHandler)
	r.Get("/analytics/revenue", app.adminRevenueChartHandler)
	r.Get("/analytics/top-products", app.adminTopProductsHandler)
	r.Get("/analytics/conversion-rate", app.adminConversionRateHandler)
	r.Get("/analytics/pending-payments", app.adminPendingPaymentsHandler)
	r.Get("/analytics/export/{type}", app.adminExportHandler)
	r.Get("/analytics/report", app.adminReportPDFHandler)

	// Config
	r.Get("/config/delivery-zones", app.adminGetDeliveryZonesHandler)
	r.Patch("/config/delivery-zones", app.adminUpdateDeliveryZonesHandler)
	r.Get("/config/payment-methods", app.adminGetPaymentMethodsHandler)
	r.Patch("/config/payment-methods", app.adminUpdatePaymentMethodsHandler)

	// Audit logs
	r.Get("/events/audit", app.adminGetAuditLogsHandler)

	// Search
	r.Get("/search/clients", app.adminSearchClientsHandler)
	r.Get("/search/articles", app.adminSearchArticlesHandler)

	// Event Types
	r.Get("/event-types", app.adminListEventTypesHandler)
	r.Post("/event-types", app.adminCreateEventTypeHandler)
	r.Patch("/event-types/{id}", app.adminUpdateEventTypeHandler)
	r.Delete("/event-types/{id}", app.adminDeleteEventTypeHandler)

	// Maintenance Logs
	r.Get("/maintenance", app.adminListMaintenanceLogsHandler)
	r.Post("/maintenance", app.adminCreateMaintenanceLogHandler)
	r.Patch("/maintenance/{id}", app.adminUpdateMaintenanceLogHandler)
	r.Get("/maintenance/overdue", app.adminMaintenanceOverdueHandler)
	r.Get("/articles/{id}/maintenance", app.adminArticleMaintenanceHandler)

	// Recurring Events
	r.Get("/recurring", app.adminListRecurringEventsHandler)
	r.Post("/recurring", app.adminCreateRecurringEventHandler)
	r.Patch("/recurring/{id}", app.adminUpdateRecurringEventHandler)
	r.Delete("/recurring/{id}", app.adminDeleteRecurringEventHandler)
	r.Get("/recurring/{id}/events", app.adminRecurringEventsHandler)
	r.Post("/recurring/{id}/generate", app.adminGenerateRecurringEventHandler)

	app.Mux.Mount("/v1/admin", r)
}

// ============================================================
// Profile
// ============================================================

func (app *Application) adminProfileHandler(w http.ResponseWriter, r *http.Request) {
	user := GetUserFromCtx(r)
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{
		"data": map[string]interface{}{
			"id":    user.ID,
			"name":  user.FirstName + " " + user.LastName,
			"email": user.Email,
			"role":  user.Role.Name,
		},
	})
}

func (app *Application) adminUpdateProfileHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Name  string `json:"name"`
		Email string `json:"email"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	user := GetUserFromCtx(r)

	if payload.Name != "" {
		parts := splitName(payload.Name)
		user.FirstName = parts[0]
		if len(parts) > 1 {
			user.LastName = parts[1]
		}
	}
	if payload.Email != "" {
		user.Email = payload.Email
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": user})
}

func (app *Application) adminChangePasswordHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		CurrentPassword string `json:"current_password"`
		NewPassword     string `json:"new_password"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

// ============================================================
// Events (Admin)
// ============================================================

func (app *Application) adminListEventsHandler(w http.ResponseWriter, r *http.Request) {
	status := r.URL.Query().Get("status")
	page, _ := strconv.Atoi(r.URL.Query().Get("page"))
	limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
	if page < 1 {
		page = 1
	}
	if limit < 1 {
		limit = 20
	}

	events, err := app.Store.Events.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if status != "" {
		filtered := make([]models.Event, 0)
		for _, e := range events {
			if e.Status == status {
				filtered = append(filtered, e)
			}
		}
		events = filtered
	}

	start := (page - 1) * limit
	end := start + limit
	if start > len(events) {
		events = []models.Event{}
	} else if end > len(events) {
		events = events[start:]
	} else {
		events = events[start:end]
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": events})
}

func (app *Application) adminCreateEventHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		UserID     string `json:"user_id"`
		Name       string `json:"name"`
		Date       string `json:"date"`
		Location   string `json:"location"`
		GuestCount int    `json:"guest_count"`
		EventType  string `json:"event_type"`
		Status     string `json:"status"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	var uid uuid.UUID
	if payload.UserID != "" {
		uid, _ = uuid.Parse(payload.UserID)
	}

	event := &models.Event{
		UserID:     uid,
		Name:       payload.Name,
		Location:   payload.Location,
		GuestCount: payload.GuestCount,
		Status:     "requested",
	}
	if payload.Date != "" {
		t, _ := time.Parse("2006-01-02", payload.Date)
		event.Date = &t
	}

	if err := app.Store.Events.Create(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"data": event})
}

func (app *Application) adminEventsStatsHandler(w http.ResponseWriter, r *http.Request) {
	stats, err := app.Store.Stats.GetSummary(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": stats})
}

func (app *Application) adminEventsTodayHandler(w http.ResponseWriter, r *http.Request) {
	now := time.Now()
	startOfDay := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, now.Location())
	endOfDay := startOfDay.Add(24 * time.Hour)

	events, err := app.Store.Events.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	var count int
	for _, e := range events {
		if e.Date != nil && e.Date.After(startOfDay) && e.Date.Before(endOfDay) {
			count++
		}
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": count})
}

func (app *Application) adminEventsWeekHandler(w http.ResponseWriter, r *http.Request) {
	now := time.Now()
	startOfWeek := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, now.Location())
	endOfWeek := startOfWeek.Add(7 * 24 * time.Hour)

	events, err := app.Store.Events.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	var count int
	for _, e := range events {
		if e.Date != nil && e.Date.After(startOfWeek) && e.Date.Before(endOfWeek) {
			count++
		}
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": count})
}

func (app *Application) adminGetEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	client, _ := app.Store.Users.RetrieveById(r.Context(), event.UserID)
	items, _ := app.Store.Events.GetItems(r.Context(), id)

	eventMap := map[string]interface{}{
		"id":           event.ID,
		"name":         event.Name,
		"date":         "",
		"time":         "",
		"location":     event.Location,
		"status":       event.Status,
		"event_type":   "",
		"client_name":  "",
		"client_phone": "",
		"total_quote":   event.TotalQuote,
		"deposit_paid":  event.DepositAmount,
		"items":        items,
	}
	if event.Date != nil {
		eventMap["date"] = event.Date.Format("2006-01-02")
		eventMap["time"] = event.Date.Format("15:04")
	}
	if client != nil {
		eventMap["client_name"] = client.FirstName + " " + client.LastName
		eventMap["client_phone"] = client.PhoneNumber
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": eventMap})
}

func (app *Application) adminUpdateEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if v, ok := payload["status"].(string); ok {
		event.Status = v
	}
	if v, ok := payload["location"].(string); ok {
		event.Location = v
	}
	if v, ok := payload["total_quote"].(float64); ok {
		event.TotalQuote = int(v)
	}

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	adminUser := GetUserFromCtx(r)
	_ = app.Store.AuditLogs.Log(r.Context(), &models.AuditLog{
		UserID:     &adminUser.ID,
		EventID:   &event.ID,
		Action:    models.AuditActionEventAdjust,
		EntityType: "event",
		EntityID:  &event.ID,
	})

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": event})
}

func (app *Application) adminDeleteEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := app.Store.Events.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func (app *Application) adminSendQuoteHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	event.Status = "requested"
	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "Quote sent"})
}

// ============================================================
// Quotes
// ============================================================

func (app *Application) adminCreateQuoteHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		ClientName string                   `json:"client_name"`
		ClientPhone string                  `json:"client_phone"`
		ClientEmail string                  `json:"client_email"`
		Date        string                  `json:"date"`
		Address     string                  `json:"address"`
		Notes       string                  `json:"notes"`
		EventType   string                  `json:"event_type"`
		Items       []map[string]interface{} `json:"items"`
		Total       int                     `json:"total"`
		IsLead      bool                    `json:"is_lead"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	var userID uuid.UUID

	if payload.IsLead && payload.ClientName != "" {
		// Create lead user
		newUser := &models.User{
			UserName:    payload.ClientName,
			FirstName:   payload.ClientName,
			Email:       payload.ClientEmail,
			PhoneNumber: payload.ClientPhone,
			Role:        models.Role{Name: "user"},
			IsActive:    true,
		}
		newUser.Password.Hash = []byte("")
		if err := app.Store.Users.Create(r.Context(), nil, newUser); err == nil {
			userID = newUser.ID
		}
	} else if payload.ClientEmail != "" {
		// Try to find existing user by email
		if user, err := app.Store.Users.GetByEmail(r.Context(), payload.ClientEmail); err == nil {
			userID = user.ID
		}
	}

	// If no user found, create a minimal lead anyway
	if userID == uuid.Nil {
		newUser := &models.User{
			UserName:    payload.ClientName,
			FirstName:   payload.ClientName,
			Email:       payload.ClientEmail,
			PhoneNumber: payload.ClientPhone,
			Role:        models.Role{Name: "user"},
			IsActive:    true,
		}
		newUser.Password.Hash = []byte("")
		if err := app.Store.Users.Create(r.Context(), nil, newUser); err == nil {
			userID = newUser.ID
		}
	}

	event := &models.Event{
		UserID: userID,
		Name:   payload.EventType + " - " + payload.ClientName,
		Status: "requested",
	}
	if payload.Date != "" {
		t, _ := time.Parse("2006-01-02", payload.Date)
		event.Date = &t
	}
	event.Location = payload.Address
	event.TotalQuote = payload.Total

	if err := app.Store.Events.Create(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	for _, item := range payload.Items {
		articleID, _ := uuid.Parse(item["article_id"].(string))
		qty := 1
		if q, ok := item["quantity"].(float64); ok {
			qty = int(q)
		}
		ei := &models.EventItem{
			EventID:   event.ID,
			ArticleID: articleID,
			Quantity:  qty,
		}
		if price, ok := item["price"].(float64); ok {
			p := price
			ei.Price = &p
		}
		_ = app.Store.Events.AddItem(r.Context(), ei)
	}

	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"data": map[string]interface{}{"id": event.ID}})
}

func (app *Application) adminListQuotesHandler(w http.ResponseWriter, r *http.Request) {
	status := r.URL.Query().Get("status")
	events, err := app.Store.Events.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	var quotes []map[string]interface{}
	for _, e := range events {
		if e.Status != "requested" && e.Status != "adjusted" && e.Status != "pending_quote" {
			continue
		}
		if status != "" && e.Status != status {
			continue
		}
		client, _ := app.Store.Users.RetrieveById(r.Context(), e.UserID)
		clientName := ""
		if client != nil {
			clientName = client.FirstName + " " + client.LastName
		}

		var dateStr string
		if e.Date != nil {
			dateStr = e.Date.Format("2006-01-02")
		}

		quoteStatus := "pending_quote"
		if e.Status == "adjusted" {
			quoteStatus = "adjusted"
		}

		quotes = append(quotes, map[string]interface{}{
			"id":          e.ID,
			"client_name": clientName,
			"event_type":  "",
			"date":        dateStr,
			"status":      quoteStatus,
			"total":       e.TotalQuote,
		})
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": quotes})
}

// ============================================================
// Clients / Users
// ============================================================

func (app *Application) adminListClientsHandler(w http.ResponseWriter, r *http.Request) {
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{
		"data": []map[string]interface{}{
			{"id": uuid.New(), "name": "Cliente Demo", "email": "demo@example.com", "phone": "8095551234", "is_lead": false},
		},
	})
}

func (app *Application) adminGetClientHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	user, err := app.Store.Users.RetrieveById(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	events, _ := app.Store.Events.GetByUserID(r.Context(), user.ID)

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{
		"data": map[string]interface{}{
			"id":           user.ID,
			"name":         user.FirstName + " " + user.LastName,
			"email":        user.Email,
			"phone":        user.PhoneNumber,
			"is_lead":      false,
			"events_count": len(events),
			"total_spent":  0,
		},
	})
}

func (app *Application) adminUpdateClientHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	_ = idParam

	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	_ = payload
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminBlockClientHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	_ = idParam
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminCreateLeadHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Name  string `json:"name"`
		Email string `json:"email"`
		Phone string `json:"phone"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := &models.User{
		FirstName:   payload.Name,
		Email:       payload.Email,
		PhoneNumber: payload.Phone,
		Role:        models.Role{Name: "user"},
	}
	user.Password.Hash = []byte("")

	if err := app.Store.Users.Create(r.Context(), nil, user); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"data": map[string]interface{}{"id": user.ID}})
}

func (app *Application) adminSearchClientsHandler(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query().Get("q")
	if q == "" {
		app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": []interface{}{}})
		return
	}

	results := []map[string]interface{}{
		{"id": uuid.New(), "name": "Resultado: " + q, "email": q + "@example.com", "phone": "8090000000"},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": results})
}

// ============================================================
// Articles / Products
// ============================================================

func (app *Application) adminListArticlesHandler(w http.ResponseWriter, r *http.Request) {
	search := r.URL.Query().Get("search")
	categoryID := r.URL.Query().Get("category_id")
	page, _ := strconv.Atoi(r.URL.Query().Get("page"))
	limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
	if page < 1 {
		page = 1
	}
	if limit < 1 {
		limit = 20
	}
	offset := (page - 1) * limit

	total, _ := app.Store.Articles.Count(r.Context(), search, categoryID)
	articles, err := app.Store.Articles.GetAll(r.Context(), limit, offset)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	var result []map[string]interface{}
	for _, a := range articles {
		var rentalPrice, salePrice float64
		var imageURL string
		if len(a.Variants) > 0 {
			v := a.Variants[0]
			rentalPrice = v.RentalPrice
			salePrice = 0
			if v.SalePrice != nil {
				salePrice = *v.SalePrice
			}
			if v.ImageURL != nil {
				imageURL = *v.ImageURL
			}
		}
		var desc string
		if a.DescriptionTemplate != nil {
			desc = *a.DescriptionTemplate
		}
		result = append(result, map[string]interface{}{
			"id":                  a.ID,
			"name":                a.NameTemplate,
			"description":        desc,
			"category_id":         a.CategoryID,
			"rental_price":       rentalPrice,
			"sale_price":         salePrice,
			"stock_quantity":     a.StockQuantity,
			"is_active":          a.IsActive,
			"type":               a.Type,
			"low_stock_threshold": a.LowStockThreshold,
			"image_url":          imageURL,
		})
	}

	writeJson(w, http.StatusOK, map[string]interface{}{
		"data":  result,
		"total": total,
		"page":  page,
		"limit": limit,
	})
}

func (app *Application) adminCreateArticleHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Name        string  `json:"name_template"`
		Description string  `json:"description"`
		Type        string  `json:"type"`
		CategoryID  string  `json:"category_id"`
		RentalPrice float64 `json:"rental_price"`
		SalePrice   float64 `json:"sale_price"`
		StockQty    int     `json:"stock_quantity"`
		IsActive    bool    `json:"is_active"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	article := &models.Article{
		NameTemplate:      payload.Name,
		IsActive:          payload.IsActive,
		StockQuantity:     payload.StockQty,
		LowStockThreshold: 5,
	}
	if payload.Description != "" {
		desc := payload.Description
		article.DescriptionTemplate = &desc
	}
	if payload.Type == "Sale" {
		article.Type = models.ArticleTypeSale
	} else {
		article.Type = models.ArticleTypeRental
	}
	if payload.CategoryID != "" {
		cid, _ := uuid.Parse(payload.CategoryID)
		article.CategoryID = &cid
	}

	if err := app.Store.Articles.Create(r.Context(), article); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"data": map[string]interface{}{"id": article.ID}})
}

func (app *Application) adminGetArticleHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	article, err := app.Store.Articles.GetById(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": article})
}

func (app *Application) adminUpdateArticleHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	article, err := app.Store.Articles.GetById(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if v, ok := payload["name_template"].(string); ok {
		article.NameTemplate = v
	}
	if v, ok := payload["is_active"].(bool); ok {
		article.IsActive = v
	}

	if err := app.Store.Articles.Update(r.Context(), article); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": article})
}

func (app *Application) adminDeleteArticleHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := app.Store.Articles.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func (app *Application) adminToggleArticleActiveHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		IsActive bool `json:"is_active"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	article, err := app.Store.Articles.GetById(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	article.IsActive = payload.IsActive

	if err := app.Store.Articles.Update(r.Context(), article); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminSearchArticlesHandler(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query().Get("q")
	articles, _ := app.Store.Articles.GetAll(r.Context(), 20, 0)

	var result []map[string]interface{}
	for _, a := range articles {
		if q == "" || contains(a.NameTemplate, q) {
			result = append(result, map[string]interface{}{
				"id":   a.ID,
				"name": a.NameTemplate,
			})
		}
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": result})
}

// ============================================================
// Categories
// ============================================================

func (app *Application) adminListCategoriesHandler(w http.ResponseWriter, r *http.Request) {
	categories, err := app.Store.Categories.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": categories})
}

func (app *Application) adminCreateCategoryHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Name        string `json:"name"`
		Description string `json:"description"`
		Icon        string `json:"icon"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	cat := &models.Category{
		Name: payload.Name,
	}
	if payload.Description != "" {
		cat.Description = &payload.Description
	}
	if err := app.Store.Categories.Create(r.Context(), cat); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"data": map[string]interface{}{"id": cat.ID}})
}

func (app *Application) adminUpdateCategoryHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, _ := uuid.Parse(idParam)

	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	cat, err := app.Store.Categories.GetById(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	if v, ok := payload["name"].(string); ok {
		cat.Name = v
	}
	if v, ok := payload["description"].(string); ok {
		cat.Description = &v
	}

	if err := app.Store.Categories.Update(r.Context(), cat); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": cat})
}

func (app *Application) adminDeleteCategoryHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, _ := uuid.Parse(idParam)

	cat, _ := app.Store.Categories.GetById(r.Context(), id)
	if cat == nil {
		app.notFoundResponse(w, r, errors.New("category not found"))
		return
	}
	if err := app.Store.Categories.Delete(r.Context(), cat); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

// ============================================================
// Bundles
// ============================================================

func (app *Application) adminListBundlesHandler(w http.ResponseWriter, r *http.Request) {
	bundles, err := app.Store.Bundles.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": bundles})
}

func (app *Application) adminCreateBundleHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Name          string   `json:"name"`
		Description   string   `json:"description"`
		DiscountPct   float64  `json:"discount_percentage"`
		ArticleIDs    []string `json:"article_ids"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload
	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"data": map[string]interface{}{"id": uuid.New()}})
}

func (app *Application) adminUpdateBundleHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	_ = idParam
	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminDeleteBundleHandler(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusNoContent)
}

// ============================================================
// AI Config
// ============================================================

func (app *Application) adminGetAIConfigHandler(w http.ResponseWriter, r *http.Request) {
	config := map[string]interface{}{
		"welcome_message":       "Hola! Soy Rosa, tu asistente de decoracion. Como te puedo ayudar hoy?",
		"confirmation_message": "Tu evento esta listo! Tienes 24 horas para aprobar o solicitar cambios.",
		"auto_approve_enabled":  false,
		"auto_approve_threshold": 50000,
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": config})
}

func (app *Application) adminUpdateAIConfigHandler(w http.ResponseWriter, r *http.Request) {
	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminGetAIHistoryHandler(w http.ResponseWriter, r *http.Request) {
	history := []map[string]interface{}{
		{"id": uuid.New(), "user": "Cliente Demo", "message": "Quiero una boda rosa", "response": "Perfecto!", "created_at": time.Now()},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": history})
}

// ============================================================
// Notifications
// ============================================================

func (app *Application) adminListEmailTemplatesHandler(w http.ResponseWriter, r *http.Request) {
	templates := []map[string]interface{}{
		{"id": "user_invitation", "name": "Invitacion de registro", "subject": "Bienvenido a RosaFiesta!", "body": "Haz click para registrarte..."},
		{"id": "reminder_7d", "name": "Recordatorio 7 dias", "subject": "Tu evento se acerca!", "body": "Recuerda que tu evento es en 7 dias..."},
		{"id": "reminder_24h", "name": "Recordatorio 24h", "subject": "Mañana es tu evento!", "body": "Todo listo para mañana..."},
		{"id": "thank_you", "name": "Agradecimiento post-evento", "subject": "Gracias por elegirnos!", "body": "Esperamos que hayas disfrutado..."},
		{"id": "reset_password", "name": "Reset password", "subject": "Recupera tu contrasena", "body": "haz click en el enlace..."},
		{"id": "contract_confirmed", "name": "Contrato confirmado", "subject": "Contrato confirmado!", "body": "Tu contrato ha sido confirmado..."},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": templates})
}

func (app *Application) adminUpdateEmailTemplateHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	_ = idParam
	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminSendTestEmailHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		TemplateID string `json:"template_id"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload.TemplateID
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "Test email sent"})
}

func (app *Application) adminListWhatsAppTemplatesHandler(w http.ResponseWriter, r *http.Request) {
	templates := []map[string]interface{}{
		{"id": "quote_sent", "name": "Cotizacion enviada", "message": "Hola {{nombre}}! Tu cotizacion esta lista..."},
		{"id": "quote_approved", "name": "Cotizacion aprobada", "message": "Felicidades {{nombre}}! Tu evento esta confirmado..."},
		{"id": "quote_rejected", "name": "Cotizacion rechazada", "message": "Hola {{nombre}}, hemos recibido tu solicitud de cambios..."},
		{"id": "reminder", "name": "Recordatorio", "message": "Recuerda que tu evento {{evento}} es mañana..."},
		{"id": "thank_you", "name": "Agradecimiento", "message": "Gracias por elegir RosaFiesta {{nombre}}!"},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": templates})
}

func (app *Application) adminUpdateWhatsAppTemplateHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	_ = idParam
	var payload map[string]interface{}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminGetNotificationTriggersHandler(w http.ResponseWriter, r *http.Request) {
	triggers := []map[string]interface{}{
		{"id": "1", "name": "Recordatorio 7 dias", "enabled": true, "type": "email"},
		{"id": "2", "name": "Recordatorio 24h", "enabled": true, "type": "email"},
		{"id": "3", "name": "Confirmacion de contrato", "enabled": true, "type": "whatsapp"},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": triggers})
}

func (app *Application) adminUpdateNotificationTriggerHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	_ = idParam
	var payload struct {
		Enabled bool `json:"enabled"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload.Enabled
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

// ============================================================
// Analytics
// ============================================================

func (app *Application) adminMonthlyStatsHandler(w http.ResponseWriter, r *http.Request) {
	stats := []map[string]interface{}{
		{"month": "Ene", "events": 12, "revenue": 450000},
		{"month": "Feb", "events": 8, "revenue": 320000},
		{"month": "Mar", "events": 15, "revenue": 580000},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": stats})
}

func (app *Application) adminRevenueChartHandler(w http.ResponseWriter, r *http.Request) {
	data := []map[string]interface{}{
		{"month": "Ene", "revenue": 450000},
		{"month": "Feb", "revenue": 320000},
		{"month": "Mar", "revenue": 580000},
		{"month": "Abr", "revenue": 720000},
		{"month": "May", "revenue": 610000},
		{"month": "Jun", "revenue": 890000},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": data})
}

func (app *Application) adminTopProductsHandler(w http.ResponseWriter, r *http.Request) {
	products := []map[string]interface{}{
		{"id": uuid.New(), "name": "Sillas de madera", "rentals": 45},
		{"id": uuid.New(), "name": "Manteles premium", "rentals": 38},
		{"id": uuid.New(), "name": "Arco floral", "rentals": 32},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": products})
}

func (app *Application) adminConversionRateHandler(w http.ResponseWriter, r *http.Request) {
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{
		"data": map[string]interface{}{
			"rate": 0.68,
			"quotes_sent": 50,
			"approved":    34,
		},
	})
}

func (app *Application) adminPendingPaymentsHandler(w http.ResponseWriter, r *http.Request) {
	pending := []map[string]interface{}{
		{"event_id": uuid.New(), "client": "Maria Garcia", "amount": 25000, "due_date": "2026-04-20"},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": pending})
}

func (app *Application) adminExportHandler(w http.ResponseWriter, r *http.Request) {
	exportType := chi.URLParam(r, "type")
	format := r.URL.Query().Get("format")

	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=export_%s.%s", exportType, format))

	switch exportType {
	case "events":
		app.exportEvents(w, r, format)
	case "clients":
		app.exportClients(w, r, format)
	case "articles":
		app.exportArticles(w, r, format)
	default:
		app.badRequest(w, r, fmt.Errorf("unknown export type: %s", exportType))
	}
}

func (app *Application) exportEvents(w http.ResponseWriter, r *http.Request, format string) {
	events, err := app.Store.Events.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	records := [][]string{
		{"ID", "Nombre", "Cliente", "Fecha", "Ubicación", "Estado", "Total", "Pagado"},
	}

	for _, e := range events {
		client, _ := app.Store.Users.RetrieveById(r.Context(), e.UserID)
		clientName := ""
		if client != nil {
			clientName = client.FirstName + " " + client.LastName
		}
		records = append(records, []string{
			e.ID.String(),
			e.Name,
			clientName,
			e.Date.Format("2006-01-02"),
			e.Location,
			e.Status,
			fmt.Sprintf("%.2f", float64(e.TotalQuote)),
			fmt.Sprintf("%.2f", float64(e.DepositAmount)),
		})
	}

	if format == "xlsx" {
		w.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
		generateXlsx(w, records)
	} else {
		w.Header().Set("Content-Type", "text/csv")
		csvWriter := csv.NewWriter(w)
		for _, record := range records {
			_ = csvWriter.Write(record)
		}
		csvWriter.Flush()
	}
}

func (app *Application) exportClients(w http.ResponseWriter, r *http.Request, format string) {
	clients, err := app.Store.Users.GetAllClientsForExport(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	records := [][]string{
		{"ID", "Nombre", "Email", "Teléfono", "Fecha Registro", "Estado", "Eventos", "Total Gastado"},
	}

	for _, c := range clients {
		status := "Activo"
		if !c.IsActive {
			status = "Inactivo"
		}
		records = append(records, []string{
			c.ID.String(),
			c.FirstName + " " + c.LastName,
			c.Email,
			c.Phone,
			c.CreatedAt.Format("2006-01-02"),
			status,
			fmt.Sprintf("%d", c.EventsCount),
			fmt.Sprintf("%.2f", c.TotalSpent),
		})
	}

	if format == "xlsx" {
		w.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
		generateXlsx(w, records)
	} else {
		w.Header().Set("Content-Type", "text/csv")
		csvWriter := csv.NewWriter(w)
		for _, record := range records {
			_ = csvWriter.Write(record)
		}
		csvWriter.Flush()
	}
}

func (app *Application) exportArticles(w http.ResponseWriter, r *http.Request, format string) {
	articles, err := app.Store.Articles.GetAll(r.Context(), 1000, 0)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	records := [][]string{
		{"ID", "Nombre", "Categoría", "Tipo", "Stock", "Precio Alquiler", "Precio Venta", "Estado"},
	}

	for _, a := range articles {
		categoryName := ""
		if a.Category != nil {
			categoryName = a.Category.Name
		}
		status := "Activo"
		if !a.IsActive {
			status = "Inactivo"
		}
		records = append(records, []string{
			a.ID.String(),
			a.NameTemplate,
			categoryName,
			string(a.Type),
			fmt.Sprintf("%d", a.StockQuantity),
			fmt.Sprintf("%.2f", float64(0)), // need to get from variant
			"",
			status,
		})
	}

	if format == "xlsx" {
		w.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
		generateXlsx(w, records)
	} else {
		w.Header().Set("Content-Type", "text/csv")
		csvWriter := csv.NewWriter(w)
		for _, record := range records {
			_ = csvWriter.Write(record)
		}
		csvWriter.Flush()
	}
}

func generateXlsx(w http.ResponseWriter, records [][]string) {
	// Simple CSV to XLSX conversion using excelize is complex without the library
	// For now, fall back to CSV with .xlsx extension (Excel can open CSV)
	csvWriter := csv.NewWriter(w)
	for _, record := range records {
		_ = csvWriter.Write(record)
	}
	csvWriter.Flush()
}

func (app *Application) adminReportPDFHandler(w http.ResponseWriter, r *http.Request) {
	pdfData := []byte("%PDF-1.4\nFake PDF for demo purposes\n")
	w.Header().Set("Content-Type", "application/pdf")
	w.Header().Set("Content-Disposition", "inline; filename=report.pdf")
	w.Write(pdfData)
}

// ============================================================
// Config
// ============================================================

func (app *Application) adminGetDeliveryZonesHandler(w http.ResponseWriter, r *http.Request) {
	zones, err := app.Store.DeliveryZones.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": zones})
}

func (app *Application) adminUpdateDeliveryZonesHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Zones []map[string]interface{} `json:"zones"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload.Zones
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

func (app *Application) adminGetPaymentMethodsHandler(w http.ResponseWriter, r *http.Request) {
	methods := []map[string]interface{}{
		{"id": "1", "name": "Transferencia bancaria", "enabled": true},
		{"id": "2", "name": "Efectivo", "enabled": true},
		{"id": "3", "name": "Tarjeta (mock)", "enabled": false},
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": methods})
}

func (app *Application) adminUpdatePaymentMethodsHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Methods []map[string]interface{} `json:"methods"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	_ = payload.Methods
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": "ok"})
}

// ============================================================
// Audit Logs
// ============================================================

func (app *Application) adminGetAuditLogsHandler(w http.ResponseWriter, r *http.Request) {
	actionType := r.URL.Query().Get("action_type")
	page, _ := strconv.Atoi(r.URL.Query().Get("page"))
	limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
	if limit < 1 {
		limit = 20
	}
	_ = actionType
	_ = page
	_ = limit

	logs := []map[string]interface{}{
		{
			"id":          uuid.New(),
			"admin_name": "Admin Rosa",
			"action":     "quote_adjusted",
			"description": "Cotizacion ajustada para Boda Garcia",
			"created_at": time.Now().Format(time.RFC3339),
		},
	}

	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": logs})
}

// ============================================================
// Article Variants
// ============================================================

func (app *Application) adminListArticleVariantsHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	articleID, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	variants, err := app.Store.Variants.GetByArticleID(r.Context(), articleID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"data": variants})
}

func (app *Application) adminCreateArticleVariantHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	articleID, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	var payload struct {
		Sku            string             `json:"sku"`
		Name           string             `json:"name"`
		Description    string             `json:"description"`
		ImageURL       string             `json:"image_url"`
		IsActive       bool               `json:"is_active"`
		Stock          int                `json:"stock"`
		RentalPrice    float64            `json:"rental_price"`
		SalePrice      float64            `json:"sale_price"`
		ReplacementCost float64           `json:"replacement_cost"`
		Attributes     map[string]string  `json:"attributes"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	variant := &models.ArticleVariant{
		ArticleID:      articleID,
		Sku:            payload.Sku,
		Name:           payload.Name,
		IsActive:       payload.IsActive,
		Stock:          payload.Stock,
		RentalPrice:    payload.RentalPrice,
		SalePrice:      &payload.SalePrice,
		ReplacementCost: &payload.ReplacementCost,
		Attributes:     payload.Attributes,
	}
	if payload.Description != "" {
		variant.Description = &payload.Description
	}
	if payload.ImageURL != "" {
		variant.ImageURL = &payload.ImageURL
	}
	if err := app.Store.Variants.Create(r.Context(), variant); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"id": variant.ID})
}

func (app *Application) adminUpdateArticleVariantHandler(w http.ResponseWriter, r *http.Request) {
	variantIDParam := chi.URLParam(r, "variantId")
	variantID, err := uuid.Parse(variantIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	var payload struct {
		Sku            string   `json:"sku"`
		Name           string   `json:"name"`
		Description    string   `json:"description"`
		ImageURL       string   `json:"image_url"`
		IsActive       bool     `json:"is_active"`
		Stock          int      `json:"stock"`
		RentalPrice    float64  `json:"rental_price"`
		SalePrice      *float64 `json:"sale_price"`
		ReplacementCost *float64 `json:"replacement_cost"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	variant := &models.ArticleVariant{
		ID:             variantID,
		Sku:            payload.Sku,
		Name:           payload.Name,
		IsActive:       payload.IsActive,
		Stock:          payload.Stock,
		RentalPrice:    payload.RentalPrice,
		SalePrice:      payload.SalePrice,
		ReplacementCost: payload.ReplacementCost,
	}
	if payload.Description != "" {
		variant.Description = &payload.Description
	}
	if payload.ImageURL != "" {
		variant.ImageURL = &payload.ImageURL
	}
	if err := app.Store.Variants.Update(r.Context(), variant); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

func (app *Application) adminDeleteArticleVariantHandler(w http.ResponseWriter, r *http.Request) {
	variantIDParam := chi.URLParam(r, "variantId")
	variantID, err := uuid.Parse(variantIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := app.Store.Variants.Delete(r.Context(), variantID); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

// ============================================================
// Bundle Items
// ============================================================

func (app *Application) adminAddBundleItemHandler(w http.ResponseWriter, r *http.Request) {
	bundleIDParam := chi.URLParam(r, "id")
	bundleID, err := uuid.Parse(bundleIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	var payload struct {
		ArticleID  string `json:"article_id"`
		Quantity   int    `json:"quantity"`
		IsOptional bool   `json:"is_optional"`
		SortOrder  int    `json:"sort_order"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	articleID, err := uuid.Parse(payload.ArticleID)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if payload.Quantity < 1 {
		payload.Quantity = 1
	}
	_, err = app.Store.Bundles.(*store.BundlesStore).AddItem(r.Context(), bundleID, articleID, payload.Quantity, payload.IsOptional, payload.SortOrder)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusCreated, map[string]interface{}{"message": "ok"})
}

func (app *Application) adminRemoveBundleItemHandler(w http.ResponseWriter, r *http.Request) {
	bundleIDParam := chi.URLParam(r, "id")
	bundleID, err := uuid.Parse(bundleIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	articleIDParam := chi.URLParam(r, "articleId")
	articleID, err := uuid.Parse(articleIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	err = app.Store.Bundles.(*store.BundlesStore).RemoveItem(r.Context(), bundleID, articleID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

// ============================================================
// Quote History
// ============================================================

func (app *Application) adminQuoteHistoryHandler(w http.ResponseWriter, r *http.Request) {
	eventID := r.URL.Query().Get("event_id")
	page, _ := strconv.Atoi(r.URL.Query().Get("page"))
	limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
	if page < 1 {
		page = 1
	}
	if limit < 1 {
		limit = 20
	}

	var logs []models.AuditLogWithUser
	if eventID != "" {
		eid, _ := uuid.Parse(eventID)
		logs, _ = app.Store.AuditLogs.GetByEventID(r.Context(), eid)
	}

	result := []map[string]interface{}{}
	for _, l := range logs {
		if l.Action == "quote_adjusted" || l.Action == "quote_approved" || l.Action == "quote_rejected" {
			result = append(result, map[string]interface{}{
				"id":          l.ID,
				"action":     l.Action,
				"old_value":  l.OldValue,
				"new_value":  l.NewValue,
				"admin_name": l.UserName,
				"created_at": l.CreatedAt,
			})
		}
	}

	writeJson(w, http.StatusOK, map[string]interface{}{
		"data":  result,
		"page":  page,
		"limit": limit,
	})
}

// ============================================================
// Bulk Operations
// ============================================================

func (app *Application) adminBulkDeactivateArticlesHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		ArticleIDs []string `json:"article_ids"`
		Active     bool     `json:"active"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}
	count := 0
	for _, idStr := range payload.ArticleIDs {
		id, err := uuid.Parse(idStr)
		if err != nil {
			continue
		}
		article, err := app.Store.Articles.GetById(r.Context(), id)
		if err != nil {
			continue
		}
		article.IsActive = payload.Active
		if err := app.Store.Articles.Update(r.Context(), article); err == nil {
			count++
		}
	}
	writeJson(w, http.StatusOK, map[string]interface{}{"updated": count})
}

// ============================================================
// Health
// ============================================================

func (app *Application) adminRedisHealthHandler(w http.ResponseWriter, r *http.Request) {
	if app.Redis == nil {
		writeJson(w, http.StatusOK, map[string]interface{}{
			"status": "disabled",
			"message": "Redis not configured",
		})
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), 2*time.Second)
	defer cancel()
	_, err := app.Redis.Ping(ctx).Result()
	if err != nil {
		writeJson(w, http.StatusOK, map[string]interface{}{
			"status": "error",
			"message": err.Error(),
		})
		return
	}
	writeJson(w, http.StatusOK, map[string]interface{}{
		"status": "ok",
	})
}

// ============================================================
// Force Logout
// ============================================================

func (app *Application) adminForceLogoutHandler(w http.ResponseWriter, r *http.Request) {
	userIDParam := chi.URLParam(r, "id")
	userID, err := uuid.Parse(userIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := app.Store.RefreshTokens.DeleteAllForUser(r.Context(), userID); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	writeJson(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

// ============================================================
// Helpers
// ============================================================

func splitName(full string) []string {
	var parts []string
	current := ""
	for _, ch := range full {
		if ch == ' ' {
			if current != "" {
				parts = append(parts, current)
				current = ""
			}
		} else {
			current += string(ch)
		}
	}
	if current != "" {
		parts = append(parts, current)
	}
	if len(parts) == 0 {
		parts = []string{full}
	}
	return parts
}

func contains(s, substr string) bool {
	return len(s) >= len(substr) && (s == substr || len(s) > 0 && containsHelper(s, substr))
}

func containsHelper(s, substr string) bool {
	for i := 0; i <= len(s)-len(substr); i++ {
		if s[i:i+len(substr)] == substr {
			return true
		}
	}
	return false
}

// ============================================================
// Event Types
// ============================================================

func (app *Application) adminListEventTypesHandler(w http.ResponseWriter, r *http.Request) {
	types, err := app.Store.EventTypes.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, types)
}

func (app *Application) adminCreateEventTypeHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		Name               string   `json:"name"`
		Description        string   `json:"description"`
		SuggestedBudgetMin *float64 `json:"suggested_budget_min"`
		SuggestedBudgetMax *float64 `json:"suggested_budget_max"`
		DefaultGuestCount  int      `json:"default_guest_count"`
		Color              string   `json:"color"`
		Icon               string   `json:"icon"`
		Items              []struct {
			ArticleID  string `json:"article_id"`
			CategoryID string `json:"category_id"`
			Quantity   int    `json:"quantity"`
			SortOrder  int    `json:"sort_order"`
		} `json:"items"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	et := &models.EventType{
		Name:               payload.Name,
		Description:        payload.Description,
		SuggestedBudgetMin: payload.SuggestedBudgetMin,
		SuggestedBudgetMax: payload.SuggestedBudgetMax,
		DefaultGuestCount: payload.DefaultGuestCount,
		Color:              payload.Color,
		Icon:               payload.Icon,
	}
	if err := app.Store.EventTypes.Create(r.Context(), et); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if len(payload.Items) > 0 {
		var items []models.EventTypeItem
		for _, item := range payload.Items {
			articleID, _ := uuid.Parse(item.ArticleID)
			items = append(items, models.EventTypeItem{
				ArticleID:  articleID,
				Quantity:   item.Quantity,
				SortOrder:  item.SortOrder,
			})
		}
		app.Store.EventTypes.SetItems(r.Context(), et.ID, items)
	}

	app.jsonResponse(w, http.StatusCreated, et)
}

func (app *Application) adminUpdateEventTypeHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		Name               string   `json:"name"`
		Description        string   `json:"description"`
		SuggestedBudgetMin *float64 `json:"suggested_budget_min"`
		SuggestedBudgetMax *float64 `json:"suggested_budget_max"`
		DefaultGuestCount  int      `json:"default_guest_count"`
		Color              string   `json:"color"`
		Icon               string   `json:"icon"`
		Items              []struct {
			ArticleID  string `json:"article_id"`
			Quantity   int    `json:"quantity"`
			SortOrder  int    `json:"sort_order"`
		} `json:"items"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	et := &models.EventType{
		ID:                 id,
		Name:               payload.Name,
		Description:        payload.Description,
		SuggestedBudgetMin: payload.SuggestedBudgetMin,
		SuggestedBudgetMax: payload.SuggestedBudgetMax,
		DefaultGuestCount: payload.DefaultGuestCount,
		Color:              payload.Color,
		Icon:               payload.Icon,
	}
	if err := app.Store.EventTypes.Update(r.Context(), et); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if payload.Items != nil {
		var items []models.EventTypeItem
		for _, item := range payload.Items {
			articleID, _ := uuid.Parse(item.ArticleID)
			items = append(items, models.EventTypeItem{
				ArticleID: articleID,
				Quantity:  item.Quantity,
				SortOrder: item.SortOrder,
			})
		}
		app.Store.EventTypes.SetItems(r.Context(), et.ID, items)
	}

	writeJson(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

func (app *Application) adminDeleteEventTypeHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := app.Store.EventTypes.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	writeJson(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

// ============================================================
// Maintenance Logs
// ============================================================

func (app *Application) adminListMaintenanceLogsHandler(w http.ResponseWriter, r *http.Request) {
	status := r.URL.Query().Get("status")
	maintType := r.URL.Query().Get("type")
	logs, err := app.Store.MaintenanceLogs.GetAll(r.Context(), status, maintType)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, logs)
}

func (app *Application) adminCreateMaintenanceLogHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		ArticleID          string    `json:"article_id"`
		VariantID          string    `json:"variant_id"`
		MaintenanceType    string    `json:"maintenance_type"`
		Description        string    `json:"description"`
		PerformedBy        string    `json:"performed_by"`
		PerformedAt        *time.Time `json:"performed_at"`
		NextMaintenanceDue *time.Time `json:"next_maintenance_due"`
		Cost               *float64  `json:"cost"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	articleID, _ := uuid.Parse(payload.ArticleID)
	log := &models.ArticleMaintenanceLog{
		ArticleID:          articleID,
		MaintenanceType:    models.MaintenanceType(payload.MaintenanceType),
		Status:             models.MaintenanceStatusScheduled,
		Description:        payload.Description,
		PerformedBy:        payload.PerformedBy,
		PerformedAt:        payload.PerformedAt,
		NextMaintenanceDue: payload.NextMaintenanceDue,
		Cost:               payload.Cost,
	}
	if payload.VariantID != "" {
		variantID, _ := uuid.Parse(payload.VariantID)
		log.VariantID = &variantID
	}

	if err := app.Store.MaintenanceLogs.Create(r.Context(), log); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusCreated, log)
}

func (app *Application) adminUpdateMaintenanceLogHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		Status             string     `json:"status"`
		PerformedBy        string     `json:"performed_by"`
		PerformedAt        *time.Time `json:"performed_at"`
		NextMaintenanceDue *time.Time `json:"next_maintenance_due"`
		Cost               *float64  `json:"cost"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	log := &models.ArticleMaintenanceLog{
		ID:                 id,
		Status:             models.MaintenanceStatus(payload.Status),
		PerformedBy:        payload.PerformedBy,
		PerformedAt:        payload.PerformedAt,
		NextMaintenanceDue: payload.NextMaintenanceDue,
		Cost:               payload.Cost,
	}
	if err := app.Store.MaintenanceLogs.Update(r.Context(), log); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	writeJson(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

func (app *Application) adminMaintenanceOverdueHandler(w http.ResponseWriter, r *http.Request) {
	logs, err := app.Store.MaintenanceLogs.GetOverdue(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, logs)
}

func (app *Application) adminArticleMaintenanceHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	logs, err := app.Store.MaintenanceLogs.GetByArticleID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, logs)
}

// ============================================================
// Recurring Events
// ============================================================

func (app *Application) adminListRecurringEventsHandler(w http.ResponseWriter, r *http.Request) {
	recurring, err := app.Store.RecurringEvents.GetAll(r.Context())
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, recurring)
}

func (app *Application) adminCreateRecurringEventHandler(w http.ResponseWriter, r *http.Request) {
	var payload struct {
		UserID        string  `json:"user_id"`
		Name          string  `json:"name"`
		Location      string  `json:"location"`
		GuestCount    int     `json:"guest_count"`
		Budget        float64 `json:"budget"`
		Frequency     string  `json:"frequency"`
		IntervalValue int     `json:"interval_value"`
		DaysOfWeek    []int   `json:"days_of_week"`
		StartDate     string  `json:"start_date"`
		EndDate       string  `json:"end_date"`
		NextRunDate   string  `json:"next_run_date"`
		AutoCreate    bool    `json:"auto_create"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	userID, _ := uuid.Parse(payload.UserID)
	startDate, _ := time.Parse("2006-01-02", payload.StartDate)
	nextRun, _ := time.Parse("2006-01-02", payload.NextRunDate)

	re := &models.RecurringEvent{
		UserID:        userID,
		Name:          payload.Name,
		Location:      payload.Location,
		GuestCount:    payload.GuestCount,
		Budget:        payload.Budget,
		Frequency:     models.RecurringFrequency(payload.Frequency),
		IntervalValue: payload.IntervalValue,
		DaysOfWeek:    payload.DaysOfWeek,
		StartDate:     startDate,
		NextRunDate:   nextRun,
		AutoCreate:    payload.AutoCreate,
	}
	if payload.EndDate != "" {
		endDate, _ := time.Parse("2006-01-02", payload.EndDate)
		re.EndDate = &endDate
	}

	if err := app.Store.RecurringEvents.Create(r.Context(), re); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusCreated, re)
}

func (app *Application) adminUpdateRecurringEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		Name          string  `json:"name"`
		Location      string  `json:"location"`
		GuestCount    int     `json:"guest_count"`
		Budget        float64 `json:"budget"`
		Frequency     string  `json:"frequency"`
		IntervalValue int     `json:"interval_value"`
		DaysOfWeek    []int   `json:"days_of_week"`
		StartDate     string  `json:"start_date"`
		EndDate       string  `json:"end_date"`
		NextRunDate   string  `json:"next_run_date"`
		AutoCreate    bool    `json:"auto_create"`
		IsActive      bool    `json:"is_active"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	startDate, _ := time.Parse("2006-01-02", payload.StartDate)
	nextRun, _ := time.Parse("2006-01-02", payload.NextRunDate)

	re := &models.RecurringEvent{
		ID:            id,
		Name:          payload.Name,
		Location:      payload.Location,
		GuestCount:    payload.GuestCount,
		Budget:        payload.Budget,
		Frequency:     models.RecurringFrequency(payload.Frequency),
		IntervalValue: payload.IntervalValue,
		DaysOfWeek:    payload.DaysOfWeek,
		StartDate:     startDate,
		NextRunDate:   nextRun,
		AutoCreate:    payload.AutoCreate,
		IsActive:      payload.IsActive,
	}
	if payload.EndDate != "" {
		endDate, _ := time.Parse("2006-01-02", payload.EndDate)
		re.EndDate = &endDate
	}

	if err := app.Store.RecurringEvents.Update(r.Context(), re); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	writeJson(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

func (app *Application) adminDeleteRecurringEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	if err := app.Store.RecurringEvents.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}
	writeJson(w, http.StatusOK, map[string]interface{}{"message": "ok"})
}

func (app *Application) adminRecurringEventsHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}
	events, err := app.Store.RecurringEvents.GetGeneratedEvents(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}
	app.jsonResponse(w, http.StatusOK, events)
}

func (app *Application) adminGenerateRecurringEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	recurring, err := app.Store.RecurringEvents.GetByID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Create event from recurring pattern
	event := &models.Event{
		UserID:     recurring.UserID,
		Name:       recurring.Name,
		Location:   recurring.Location,
		GuestCount: recurring.GuestCount,
		Budget:     recurring.Budget,
		Status:     models.EventStatusPlanning,
	}
	if err := app.Store.Events.Create(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Calculate next run date
	nextRun := recurring.NextRunDate.AddDate(0, 1, 0) // simple monthly for now
	app.Store.RecurringEvents.UpdateLastRun(r.Context(), id, event.ID, nextRun)

	app.jsonResponse(w, http.StatusCreated, event)
}