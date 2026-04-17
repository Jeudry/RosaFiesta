package main

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"
	"time"

	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/render"
	"github.com/google/uuid"
)

func (app *Application) MountLeadsRoutes() http.Handler {
	r := chi.NewRouter()

	r.Use(app.AuthTokenMiddleware())
	r.Use(app.RoleMiddleware("admin"))

	r.Get("/", app.getLeadsHandler)
	r.Get("/stats", app.getLeadsStatsHandler)
	r.Get("/{id}", app.getLeadByIDHandler)
	r.Post("/", app.createLeadHandler)
	r.Patch("/{id}/status", app.updateLeadStatusHandler)
	r.Post("/{id}/followups", app.addLeadFollowupHandler)
	r.Get("/{id}/followups", app.getLeadFollowupsHandler)
	r.Patch("/followups/{followupId}/complete", app.completeFollowupHandler)
	r.Get("/{id}/activities", app.getLeadActivitiesHandler)
	r.Get("/overdue-followups", app.getOverdueFollowupsHandler)

	return r
}

func (app *Application) MountAvailabilityRoutes() http.Handler {
	r := chi.NewRouter()

	r.Use(app.AuthTokenMiddleware())

	r.Get("/calendar", app.getCalendarViewHandler)
	r.Get("/article/{articleId}", app.getArticleAvailabilityHandler)
	r.Get("/date/{date}", app.getAllArticlesAvailabilityHandler)
	r.Post("/reserve", app.reserveInventoryHandler)
	r.Patch("/confirm", app.confirmInventoryHandler)
	r.Delete("/release", app.releaseInventoryHandler)

	return r
}

func (app *Application) MountChatbotRoutes() http.Handler {
	r := chi.NewRouter()

	r.Get("/faqs", app.getFAQsHandler)
	r.Get("/faqs/category/{category}", app.getFAQsByCategoryHandler)
	r.Post("/message", app.handleChatbotMessageHandler)
	r.Get("/conversations", app.getChatbotConversationsHandler)
	r.Patch("/conversations/{id}/feedback", app.provideChatbotFeedbackHandler)

	r.Use(app.AuthTokenMiddleware())
	r.Use(app.RoleMiddleware("admin"))
	r.Post("/faqs", app.createFAQHandler)
	r.Put("/faqs/{id}", app.updateFAQHandler)
	r.Delete("/faqs/{id}", app.deleteFAQHandler)

	return r
}

func (app *Application) MountReviewsRoutes() http.Handler {
	r := chi.NewRouter()

	r.Get("/event/{eventId}", app.getEventVerifiedReviewsHandler)
	r.Get("/verified", app.getAllVerifiedReviewsHandler)

	r.Use(app.AuthTokenMiddleware())
	r.Use(app.RoleMiddleware("admin"))
	r.Patch("/{id}/verify", app.verifyReviewHandler)

	return r
}

// Lead Handlers

func (app *Application) getLeadsHandler(w http.ResponseWriter, r *http.Request) {
	status := r.URL.Query().Get("status")
	assignedTo := r.URL.Query().Get("assigned_to")
	limitStr := r.URL.Query().Get("limit")
	offsetStr := r.URL.Query().Get("offset")

	limit := 50
	offset := 0
	if l, err := strconv.Atoi(limitStr); err == nil && l > 0 && l <= 100 {
		limit = l
	}
	if o, err := strconv.Atoi(offsetStr); err == nil && o >= 0 {
		offset = o
	}

	leads, total, err := app.Store.Leads.GetLeads(r.Context(), status, assignedTo, limit, offset)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	render.JSON(w, r, map[string]interface{}{
		"data":   leads,
		"total":  total,
		"limit":  limit,
		"offset": offset,
	})
}

func (app *Application) getLeadsStatsHandler(w http.ResponseWriter, r *http.Request) {
	stats, err := app.Store.Leads.GetLeadsStats(r.Context())
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": stats})
}

func (app *Application) getLeadByIDHandler(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(chi.URLParam(r, "id"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid lead id"})
		return
	}

	lead, err := app.Store.Leads.GetLeadByID(r.Context(), id)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": lead})
}

func (app *Application) createLeadHandler(w http.ResponseWriter, r *http.Request) {
	var req models.CreateLeadRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	lead := &models.Lead{
		Source:      req.Source,
		Status:      "new",
		Priority:    req.Priority,
		ClientName:  req.ClientName,
		ClientEmail: req.ClientEmail,
		ClientPhone: req.ClientPhone,
		EventType:   req.EventType,
		GuestCount:  req.GuestCount,
		BudgetMin:   req.BudgetMin,
		BudgetMax:   req.BudgetMax,
		Notes:       req.Notes,
	}

	if req.AssignedTo != "" {
		if id, err := uuid.Parse(req.AssignedTo); err == nil {
			lead.AssignedTo = &id
		}
	}
	if req.EventDate != "" {
		if dt, err := time.Parse("2006-01-02", req.EventDate); err == nil {
			lead.EventDate = &dt
		}
	}

	if err := app.Store.Leads.CreateLead(r.Context(), lead); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	app.Store.Leads.LogLeadActivity(r.Context(), lead.ID, "created", "Lead created from "+lead.Source)
	render.JSON(w, r, map[string]interface{}{"data": lead})
}

func (app *Application) updateLeadStatusHandler(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(chi.URLParam(r, "id"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid lead id"})
		return
	}

	var req models.UpdateLeadStatusRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	if err := app.Store.Leads.UpdateLeadStatus(r.Context(), id, req.Status); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	app.Store.Leads.LogLeadActivity(r.Context(), id, "status_changed", "Status changed to "+req.Status)
	render.JSON(w, r, map[string]interface{}{"data": map[string]string{"status": req.Status}})
}

func (app *Application) addLeadFollowupHandler(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(chi.URLParam(r, "id"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid lead id"})
		return
	}

	var req models.CreateFollowupRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	followUpDate, err := time.Parse("2006-01-02", req.FollowUpDate)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid date format"})
		return
	}

	followup := &models.LeadFollowup{
		LeadID:       id,
		FollowUpDate: followUpDate,
		FollowUpType: req.FollowUpType,
		Notes:        req.Notes,
	}

	if err := app.Store.Leads.AddLeadFollowup(r.Context(), followup); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	app.Store.Leads.LogLeadActivity(r.Context(), id, "follow_up_set", "Follow-up scheduled for "+req.FollowUpDate)
	render.JSON(w, r, map[string]interface{}{"data": followup})
}

func (app *Application) getLeadFollowupsHandler(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(chi.URLParam(r, "id"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid lead id"})
		return
	}

	followups, err := app.Store.Leads.GetLeadFollowups(r.Context(), id)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": followups})
}

func (app *Application) completeFollowupHandler(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(chi.URLParam(r, "followupId"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid followup id"})
		return
	}

	if err := app.Store.Leads.CompleteFollowup(r.Context(), id); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": map[string]bool{"completed": true}})
}

func (app *Application) getLeadActivitiesHandler(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(chi.URLParam(r, "id"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid lead id"})
		return
	}

	activities, err := app.Store.Leads.GetLeadActivities(r.Context(), id)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": activities})
}

func (app *Application) getOverdueFollowupsHandler(w http.ResponseWriter, r *http.Request) {
	followups, err := app.Store.Leads.GetOverdueFollowups(r.Context())
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": followups})
}

// Availability Handlers

func (app *Application) getCalendarViewHandler(w http.ResponseWriter, r *http.Request) {
	startStr := r.URL.Query().Get("start_date")
	endStr := r.URL.Query().Get("end_date")

	if startStr == "" || endStr == "" {
		render.JSON(w, r, map[string]interface{}{
			"error":   "bad_request",
			"message": "start_date and end_date are required",
		})
		return
	}

	startDate, err := time.Parse("2006-01-02", startStr)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid start_date format"})
		return
	}
	endDate, err := time.Parse("2006-01-02", endStr)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid end_date format"})
		return
	}

	days, err := app.Store.Availability.GetCalendarView(r.Context(), startDate, endDate)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": days})
}

func (app *Application) getArticleAvailabilityHandler(w http.ResponseWriter, r *http.Request) {
	articleID, err := uuid.Parse(chi.URLParam(r, "articleId"))
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid article id"})
		return
	}

	startStr := r.URL.Query().Get("start_date")
	endStr := r.URL.Query().Get("end_date")

	if startStr != "" && endStr != "" {
		startDate, _ := time.Parse("2006-01-02", startStr)
		endDate, _ := time.Parse("2006-01-02", endStr)
		days, err := app.Store.Availability.GetArticleAvailabilityRange(r.Context(), articleID, startDate, endDate)
		if err != nil {
			render.JSON(w, r, map[string]interface{}{"error": err.Error()})
			return
		}
		render.JSON(w, r, map[string]interface{}{"data": days})
		return
	}

	dateStr := r.URL.Query().Get("date")
	if dateStr != "" {
		date, _ := time.Parse("2006-01-02", dateStr)
		available, err := app.Store.Availability.CheckArticleAvailability(r.Context(), articleID, date)
		if err != nil {
			render.JSON(w, r, map[string]interface{}{"error": err.Error()})
			return
		}
		render.JSON(w, r, map[string]interface{}{"data": map[string]interface{}{
			"article_id": articleID,
			"date":       dateStr,
			"available":  available,
		}})
		return
	}

	render.JSON(w, r, map[string]interface{}{"error": "date or start_date/end_date required"})
}

func (app *Application) getAllArticlesAvailabilityHandler(w http.ResponseWriter, r *http.Request) {
	dateStr := chi.URLParam(r, "date")
	date, err := time.Parse("2006-01-02", dateStr)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": "invalid date format"})
		return
	}

	availability, err := app.Store.Availability.GetAllArticlesAvailability(r.Context(), date)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": availability})
}

func (app *Application) reserveInventoryHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		ArticleID string `json:"article_id"`
		EventID   string `json:"event_id"`
		Date      string `json:"date"`
		Quantity  int    `json:"quantity"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	articleID, _ := uuid.Parse(req.ArticleID)
	eventID, _ := uuid.Parse(req.EventID)
	date, _ := time.Parse("2006-01-02", req.Date)

	if err := app.Store.Availability.ReserveInventory(r.Context(), articleID, eventID, date, req.Quantity); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": map[string]string{"status": "reserved"}})
}

func (app *Application) confirmInventoryHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		ArticleID string `json:"article_id"`
		EventID   string `json:"event_id"`
		Date      string `json:"date"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	articleID, _ := uuid.Parse(req.ArticleID)
	eventID, _ := uuid.Parse(req.EventID)
	date, _ := time.Parse("2006-01-02", req.Date)

	if err := app.Store.Availability.ConfirmInventory(r.Context(), articleID, eventID, date); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": map[string]string{"status": "confirmed"}})
}

func (app *Application) releaseInventoryHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		ArticleID string `json:"article_id"`
		EventID   string `json:"event_id"`
		Date      string `json:"date"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	articleID, _ := uuid.Parse(req.ArticleID)
	eventID, _ := uuid.Parse(req.EventID)
	date, _ := time.Parse("2006-01-02", req.Date)

	if err := app.Store.Availability.ReleaseInventory(r.Context(), articleID, eventID, date); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": map[string]string{"status": "released"}})
}

// Chatbot Handlers

type FAQ struct {
	ID       string `json:"id"`
	Keyword  string `json:"keyword"`
	Question string `json:"question"`
	Answer   string `json:"answer"`
	Category string `json:"category"`
	Priority int    `json:"priority"`
}

func (app *Application) getFAQsHandler(w http.ResponseWriter, r *http.Request) {
	faqs := []FAQ{
		{ID: "1", Keyword: "precio", Question: "¿Cuánto cuesta?", Answer: "Nuestros precios varían según el tipo de evento. Contáctanos para una cotización personalizada.", Category: "precios", Priority: 10},
		{ID: "2", Keyword: "disponibilidad", Question: "¿Está disponible?", Answer: "Para verificar disponibilidad,我们需要 saber la fecha de tu evento. ¿Cuándo es?", Category: "reservas", Priority: 9},
		{ID: "3", Keyword: "alquiler", Question: "¿Cómo funciona el alquiler?", Answer: "El alquiler incluye entrega, montaje y recogida. El período típico es de 1-3 días.", Category: "servicios", Priority: 8},
		{ID: "4", Keyword: "boda", Question: "婚礼服务", Answer: "Sí, ofrecemos paquetes especiales para bodas. Tenemos decoración completa, mesas, sillas, iluminación y más.", Category: "eventos", Priority: 7},
	}
	render.JSON(w, r, map[string]interface{}{"data": faqs})
}

func (app *Application) getFAQsByCategoryHandler(w http.ResponseWriter, r *http.Request) {
	category := chi.URLParam(r, "category")
	faqs := []FAQ{
		{ID: "1", Keyword: "precio", Question: "¿Cuánto cuesta?", Answer: "Precios según tipo de evento.", Category: category},
	}
	render.JSON(w, r, map[string]interface{}{"data": faqs})
}

func (app *Application) handleChatbotMessageHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Message    string `json:"message"`
		Phone      string `json:"phone"`
		ClientName string `json:"client_name"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	response := "Gracias por tu mensaje. Un asesor se pondrá en contacto pronto."

	lowerMsg := req.Message
	if strings.Contains(lowerMsg, "precio") || strings.Contains(lowerMsg, "costo") || strings.Contains(lowerMsg, "cuánto") {
		response = "¡Hola! Para darte un precio exacto necesitamos saber: ¿Qué tipo de evento? ¿Cuántos invitados? ¿Qué fecha?"
	} else if strings.Contains(lowerMsg, "disponible") || strings.Contains(lowerMsg, "disponibilidad") {
		response = "Para verificar disponibilidad necesitamos la fecha de tu evento. ¿Cuándo es?"
	} else if strings.Contains(lowerMsg, "boda") {
		response = "¡Tenemos paquetes especiales para bodas! Incluye decoración, mesas, sillas e iluminación. ¿Te gustaría una cotización?"
	} else if strings.Contains(lowerMsg, "cumpleaños") || strings.Contains(lowerMsg, "fiesta") {
		response = "¡Celebramos tu especiales! Tenemos opciones para cumpleaños y fiestas. ¿Cuántos invitados esperas?"
	} else if strings.Contains(lowerMsg, "gracias") {
		response = "¡Gracias a ti! Estamos aquí para ayudarte. ¿Hay algo más en lo que podamos asistirte?"
	}

	render.JSON(w, r, map[string]interface{}{
		"data": map[string]interface{}{
			"response":    response,
			"was_helpful": nil,
		},
	})
}

func (app *Application) getChatbotConversationsHandler(w http.ResponseWriter, r *http.Request) {
	render.JSON(w, r, map[string]interface{}{"data": []interface{}{}})
}

func (app *Application) provideChatbotFeedbackHandler(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	var req struct {
		WasHelpful bool `json:"was_helpful"`
	}
	json.NewDecoder(r.Body).Decode(&req)
	render.JSON(w, r, map[string]interface{}{"data": map[string]interface{}{"id": id, "was_helpful": req.WasHelpful}})
}

func (app *Application) createFAQHandler(w http.ResponseWriter, r *http.Request) {
	var faq FAQ
	if err := json.NewDecoder(r.Body).Decode(&faq); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	faq.ID = uuid.New().String()
	render.JSON(w, r, map[string]interface{}{"data": faq})
}

func (app *Application) updateFAQHandler(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	var faq FAQ
	if err := json.NewDecoder(r.Body).Decode(&faq); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	faq.ID = id
	render.JSON(w, r, map[string]interface{}{"data": faq})
}

func (app *Application) deleteFAQHandler(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	render.JSON(w, r, map[string]interface{}{"data": map[string]string{"deleted": id}})
}

// Reviews Handlers

type VerifiedReview struct {
	ID               string `json:"id"`
	EventID          string `json:"event_id"`
	Rating           int    `json:"rating"`
	Comment          string `json:"comment"`
	IsVerified       bool   `json:"is_verified"`
	VerifiedAt       string `json:"verified_at"`
	EventPhotosCount int    `json:"event_photos_count"`
	CreatedAt        string `json:"created_at"`
}

func (app *Application) getEventVerifiedReviewsHandler(w http.ResponseWriter, r *http.Request) {
	eventID := chi.URLParam(r, "eventId")
	reviews := []VerifiedReview{
		{ID: "1", EventID: eventID, Rating: 5, Comment: "Excelente servicio", IsVerified: true, VerifiedAt: time.Now().Format(time.RFC3339), EventPhotosCount: 3, CreatedAt: time.Now().Format(time.RFC3339)},
	}
	render.JSON(w, r, map[string]interface{}{"data": reviews})
}

func (app *Application) getAllVerifiedReviewsHandler(w http.ResponseWriter, r *http.Request) {
	reviews := []VerifiedReview{
		{ID: "1", EventID: "event-1", Rating: 5, Comment: "¡Increíble!", IsVerified: true},
		{ID: "2", EventID: "event-2", Rating: 4, Comment: "Muy bueno", IsVerified: true},
	}
	render.JSON(w, r, map[string]interface{}{"data": reviews})
}

func (app *Application) verifyReviewHandler(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	var req struct {
		VerifiedBy string `json:"verified_by"`
	}
	json.NewDecoder(r.Body).Decode(&req)

	render.JSON(w, r, map[string]interface{}{"data": map[string]interface{}{
		"id":          id,
		"is_verified": true,
		"verified_at": time.Now().Format(time.RFC3339),
	}})
}
