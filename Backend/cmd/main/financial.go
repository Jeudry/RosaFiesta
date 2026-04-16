package main

import (
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/render"
	"github.com/google/uuid"
)

func (app *Application) MountFinancialRoutes() http.Handler {
	r := chi.NewRouter()

	r.Route("/categories", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getFinancialCategoriesHandler)
		r.Post("/", app.createFinancialCategoryHandler)
	})

	r.Route("/records", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getFinancialRecordsHandler)
		r.Post("/", app.createFinancialRecordHandler)
		r.Post("/{id}/reconcile", app.reconcileFinancialRecordHandler)
	})

	r.Route("/summary", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getFinancialSummaryHandler)
	})

	r.Route("/income-by-category", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getIncomeByCategoryHandler)
	})

	r.Route("/expenses-by-category", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getExpensesByCategoryHandler)
	})

	r.Route("/invoices", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getInvoicesHandler)
		r.Post("/", app.createInvoiceHandler)
	})

	r.Route("/vendors", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/", app.getExpenseVendorsHandler)
		r.Post("/", app.createExpenseVendorHandler)
		r.Route("/{vendorId}/payments", func(r chi.Router) {
			r.Get("/", app.getVendorPaymentsHandler)
			r.Post("/", app.createVendorPaymentHandler)
		})
	})

	r.Route("/insurance", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Get("/articles", app.getArticleInsuranceHandler)
		r.Post("/articles", app.createArticleInsuranceHandler)
		r.Get("/event", app.getEventInsuranceHandler)
		r.Post("/event", app.createEventInsuranceHandler)
		r.Get("/claims", app.getInsuranceClaimsHandler)
		r.Post("/claims", app.createInsuranceClaimHandler)

		r.With(app.RoleMiddleware("admin")).Patch("/claims/{claimId}/status", app.updateInsuranceClaimStatusHandler)
		r.With(app.RoleMiddleware("admin")).Get("/all", app.getAllArticleInsuranceHandler)
	})

	r.Route("/paypal", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Post("/create-order", app.createPayPalOrderHandler)
		r.Post("/capture-order", app.capturePayPalOrderHandler)
		r.Get("/event/{eventId}", app.getPayPalTransactionsByEventHandler)
	})

	r.Route("/audit", func(r chi.Router) {
		r.Use(app.AuthTokenMiddleware())
		r.Use(app.RoleMiddleware("admin"))
		r.Get("/client/{userId}", app.getClientAuditLogHandler)
		r.Get("/", app.getAllAuditLogsHandler)
	})

	return r
}

func (app *Application) MountClientPortalRoutes() http.Handler {
	r := chi.NewRouter()

	r.Use(app.AuthTokenMiddleware())

	r.Get("/dashboard", app.clientDashboardHandler)
	r.Get("/events/{id}", app.clientEventDetailHandler)
	r.Get("/events/{id}/documents", app.clientEventDocumentsHandler)
	r.Get("/events/{id}/payments", app.clientEventPaymentsHandler)
	r.Get("/notifications", app.clientNotificationsHandler)

	return r
}

func (app *Application) getFinancialCategoriesHandler(w http.ResponseWriter, r *http.Request) {
	categories, err := app.Store.Financial.GetAllFinancialCategories(r.Context())
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": categories})
}

func (app *Application) createFinancialCategoryHandler(w http.ResponseWriter, r *http.Request) {
	var cat models.FinancialCategory
	if err := json.NewDecoder(r.Body).Decode(&cat); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	if err := app.Store.Financial.CreateFinancialCategory(r.Context(), &cat); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": cat})
}

func (app *Application) getFinancialRecordsHandler(w http.ResponseWriter, r *http.Request) {
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")
	recordType := r.URL.Query().Get("type")
	categoryID := r.URL.Query().Get("category_id")
	limit := r.URL.Query().Get("limit")
	offset := r.URL.Query().Get("offset")

	if startDate == "" || endDate == "" {
		render.JSON(w, r, map[string]interface{}{
			"error":   "bad_request",
			"message": "start_date and end_date are required",
		})
		return
	}

	limitInt := 50
	offsetInt := 0
	if limit != "" {
		if parsed, err := strconv.Atoi(limit); err == nil && parsed > 0 && parsed <= 100 {
			limitInt = parsed
		}
	}
	if offset != "" {
		if parsed, err := strconv.Atoi(offset); err == nil && parsed >= 0 {
			offsetInt = parsed
		}
	}

	records, err := app.Store.Financial.GetFinancialRecords(r.Context(), startDate, endDate, recordType, categoryID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	total := len(records)
	if offsetInt > 0 || limitInt < total {
		if offsetInt < len(records) {
			endIdx := offsetInt + limitInt
			if endIdx > len(records) {
				endIdx = len(records)
			}
			records = records[offsetInt:endIdx]
		}
	}

	render.JSON(w, r, map[string]interface{}{"data": records, "total": total, "limit": limitInt, "offset": offsetInt})
}

func (app *Application) createFinancialRecordHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		EventID         string  `json:"event_id"`
		CategoryID      string  `json:"category_id"`
		Type            string  `json:"type"`
		Amount          float64 `json:"amount"`
		Currency        string  `json:"currency"`
		Description     string  `json:"description"`
		ReferenceNumber string  `json:"reference_number"`
		PaymentMethod   string  `json:"payment_method"`
		RecordDate      string  `json:"record_date"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	eventID, _ := uuid.Parse(req.EventID)
	categoryID, _ := uuid.Parse(req.CategoryID)
	recordDate, _ := time.Parse("2006-01-02", req.RecordDate)

	rec := &models.FinancialRecord{
		EventID:       &eventID,
		CategoryID:    categoryID,
		Type:          req.Type,
		Amount:        req.Amount,
		Currency:      req.Currency,
		Description:   req.Description,
		PaymentMethod: &req.PaymentMethod,
		RecordDate:    recordDate,
	}

	if err := app.Store.Financial.CreateFinancialRecord(r.Context(), rec); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": rec})
}

func (app *Application) reconcileFinancialRecordHandler(w http.ResponseWriter, r *http.Request) {
	id, _ := uuid.Parse(chi.URLParam(r, "id"))

	if err := app.Store.Financial.ReconcileRecord(r.Context(), id); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": "reconciled"})
}

func (app *Application) getFinancialSummaryHandler(w http.ResponseWriter, r *http.Request) {
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")

	if startDate == "" {
		startDate = time.Now().AddDate(0, -1, 0).Format("2006-01-02")
	}
	if endDate == "" {
		endDate = time.Now().Format("2006-01-02")
	}

	summary, err := app.Store.Financial.GetFinancialSummary(r.Context(), startDate, endDate)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": summary})
}

func (app *Application) getIncomeByCategoryHandler(w http.ResponseWriter, r *http.Request) {
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")

	if startDate == "" {
		startDate = time.Now().AddDate(0, -1, 0).Format("2006-01-02")
	}
	if endDate == "" {
		endDate = time.Now().Format("2006-01-02")
	}

	data, err := app.Store.Financial.GetIncomeByCategory(r.Context(), startDate, endDate)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": data})
}

func (app *Application) getExpensesByCategoryHandler(w http.ResponseWriter, r *http.Request) {
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")

	if startDate == "" {
		startDate = time.Now().AddDate(0, -1, 0).Format("2006-01-02")
	}
	if endDate == "" {
		endDate = time.Now().Format("2006-01-02")
	}

	data, err := app.Store.Financial.GetExpensesByCategory(r.Context(), startDate, endDate)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": data})
}

func (app *Application) getInvoicesHandler(w http.ResponseWriter, r *http.Request) {
	clientID := r.URL.Query().Get("client_id")
	status := r.URL.Query().Get("status")

	invoices, err := app.Store.Financial.GetInvoices(r.Context(), clientID, status)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": invoices})
}

func (app *Application) createInvoiceHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		EventID        string  `json:"event_id"`
		ClientID       string  `json:"client_id"`
		Subtotal       float64 `json:"subtotal"`
		TaxAmount      float64 `json:"tax_amount"`
		DiscountAmount float64 `json:"discount_amount"`
		Total          float64 `json:"total"`
		Currency       string  `json:"currency"`
		Status         string  `json:"status"`
		IssueDate      string  `json:"issue_date"`
		DueDate        string  `json:"due_date"`
		Notes          string  `json:"notes"`
		Terms          string  `json:"terms"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	clientID, _ := uuid.Parse(req.ClientID)
	eventID, _ := uuid.Parse(req.EventID)
	issueDate, _ := time.Parse("2006-01-02", req.IssueDate)

	inv := &models.Invoice{
		InvoiceNumber:  "INV-" + time.Now().Format("20060102") + "-" + uuid.New().String()[:8],
		EventID:        &eventID,
		ClientID:       clientID,
		Subtotal:       req.Subtotal,
		TaxAmount:      req.TaxAmount,
		DiscountAmount: req.DiscountAmount,
		Total:          req.Total,
		Currency:       req.Currency,
		Status:         req.Status,
		IssueDate:      issueDate,
	}

	if req.DueDate != "" {
		dueDate, _ := time.Parse("2006-01-02", req.DueDate)
		inv.DueDate = &dueDate
	}

	if err := app.Store.Financial.CreateInvoice(r.Context(), inv); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": inv})
}

func (app *Application) getExpenseVendorsHandler(w http.ResponseWriter, r *http.Request) {
	vendors, err := app.Store.Financial.GetExpenseVendors(r.Context())
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": vendors})
}

func (app *Application) createExpenseVendorHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Name        string `json:"name"`
		ContactName string `json:"contact_name"`
		Email       string `json:"email"`
		Phone       string `json:"phone"`
		Address     string `json:"address"`
		Category    string `json:"category"`
		Notes       string `json:"notes"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	vendor := &models.ExpenseVendor{
		Name:     req.Name,
		IsActive: true,
	}

	if req.ContactName != "" {
		vendor.ContactName = &req.ContactName
	}
	if req.Email != "" {
		vendor.Email = &req.Email
	}
	if req.Phone != "" {
		vendor.Phone = &req.Phone
	}
	if req.Address != "" {
		vendor.Address = &req.Address
	}
	if req.Category != "" {
		vendor.Category = &req.Category
	}
	if req.Notes != "" {
		vendor.Notes = &req.Notes
	}

	if err := app.Store.Financial.CreateExpenseVendor(r.Context(), vendor); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": vendor})
}

func (app *Application) getVendorPaymentsHandler(w http.ResponseWriter, r *http.Request) {
	vendorID, _ := uuid.Parse(chi.URLParam(r, "vendorId"))

	payments, err := app.Store.Financial.GetVendorPayments(r.Context(), vendorID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": payments})
}

func (app *Application) createVendorPaymentHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Amount          float64 `json:"amount"`
		Currency        string  `json:"currency"`
		PaymentDate     string  `json:"payment_date"`
		PaymentMethod   string  `json:"payment_method"`
		ReferenceNumber string  `json:"reference_number"`
		Description     string  `json:"description"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	vendorID, _ := uuid.Parse(chi.URLParam(r, "vendorId"))
	paymentDate, _ := time.Parse("2006-01-02", req.PaymentDate)

	payment := &models.VendorPayment{
		VendorID:      vendorID,
		Amount:        req.Amount,
		Currency:      req.Currency,
		PaymentDate:   paymentDate,
		PaymentMethod: req.PaymentMethod,
	}

	if req.ReferenceNumber != "" {
		payment.ReferenceNumber = &req.ReferenceNumber
	}
	if req.Description != "" {
		payment.Description = &req.Description
	}

	if err := app.Store.Financial.CreateVendorPayment(r.Context(), payment); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": payment})
}

func (app *Application) getArticleInsuranceHandler(w http.ResponseWriter, r *http.Request) {
	articleIDStr := r.URL.Query().Get("article_id")
	if articleIDStr == "" {
		render.JSON(w, r, map[string]interface{}{
			"error":   "bad_request",
			"message": "article_id is required",
		})
		return
	}

	articleID, err := uuid.Parse(articleIDStr)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	insurance, err := app.Store.Insurance.GetArticleInsurance(r.Context(), articleID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": insurance})
}

func (app *Application) createArticleInsuranceHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		ArticleID      string  `json:"article_id"`
		PolicyNumber   string  `json:"policy_number"`
		Provider       string  `json:"provider"`
		CoverageType   string  `json:"coverage_type"`
		CoverageAmount float64 `json:"coverage_amount"`
		Premium        float64 `json:"premium"`
		Deductible     float64 `json:"deductible"`
		Terms          string  `json:"terms"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	articleID, _ := uuid.Parse(req.ArticleID)

	insurance := &models.ArticleInsurance{
		ArticleID:      articleID,
		PolicyNumber:   req.PolicyNumber,
		Provider:       req.Provider,
		CoverageType:   req.CoverageType,
		CoverageAmount: req.CoverageAmount,
		Premium:        req.Premium,
		Deductible:     req.Deductible,
		IsActive:       true,
	}

	if req.Terms != "" {
		insurance.Terms = &req.Terms
	}

	if err := app.Store.Insurance.CreateArticleInsurance(r.Context(), insurance); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": insurance})
}

func (app *Application) getEventInsuranceHandler(w http.ResponseWriter, r *http.Request) {
	eventIDStr := r.URL.Query().Get("event_id")
	if eventIDStr == "" {
		render.JSON(w, r, map[string]interface{}{
			"error":   "bad_request",
			"message": "event_id is required",
		})
		return
	}

	eventID, err := uuid.Parse(eventIDStr)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	insurance, err := app.Store.Insurance.GetEventInsurance(r.Context(), eventID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": insurance})
}

func (app *Application) createEventInsuranceHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		InsuranceID     string   `json:"insurance_id"`
		ArticlesCovered []string `json:"articles_covered"`
		TotalCoverage   float64  `json:"total_coverage"`
		PremiumPaid     float64  `json:"premium_paid"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	eventIDStr := r.URL.Query().Get("event_id")
	eventID, _ := uuid.Parse(eventIDStr)
	insuranceID, _ := uuid.Parse(req.InsuranceID)

	eventInsurance := &models.EventInsurance{
		EventID:         eventID,
		InsuranceID:     insuranceID,
		ArticlesCovered: req.ArticlesCovered,
		TotalCoverage:   req.TotalCoverage,
		PremiumPaid:     req.PremiumPaid,
		Status:          "active",
	}

	if err := app.Store.Insurance.CreateEventInsurance(r.Context(), eventInsurance); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": eventInsurance})
}

func (app *Application) getInsuranceClaimsHandler(w http.ResponseWriter, r *http.Request) {
	status := r.URL.Query().Get("status")

	claims, err := app.Store.Insurance.GetInsuranceClaims(r.Context(), status)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": claims})
}

func (app *Application) createInsuranceClaimHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		EventInsuranceID string  `json:"event_insurance_id"`
		IncidentType     string  `json:"incident_type"`
		Description      string  `json:"description"`
		ClaimedAmount    float64 `json:"claimed_amount"`
		IncidentDate     string  `json:"incident_date"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	eventInsuranceID, _ := uuid.Parse(req.EventInsuranceID)
	incidentDate, _ := time.Parse("2006-01-02", req.IncidentDate)

	claim := &models.InsuranceClaim{
		EventInsuranceID: eventInsuranceID,
		ClaimNumber:      "CLM-" + time.Now().Format("20060102") + "-" + uuid.New().String()[:8],
		IncidentType:     req.IncidentType,
		Description:      req.Description,
		ClaimedAmount:    req.ClaimedAmount,
		Status:           "pending",
		IncidentDate:     incidentDate,
		FiledDate:        time.Now(),
	}

	if err := app.Store.Insurance.CreateInsuranceClaim(r.Context(), claim); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": claim})
}

func (app *Application) updateInsuranceClaimStatusHandler(w http.ResponseWriter, r *http.Request) {
	claimID, _ := uuid.Parse(chi.URLParam(r, "claimId"))

	var req struct {
		Status         string   `json:"status"`
		ApprovedAmount *float64 `json:"approved_amount"`
		Notes          string   `json:"notes"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	if err := app.Store.Insurance.UpdateInsuranceClaimStatus(r.Context(), claimID, req.Status, req.ApprovedAmount, req.Notes); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": "updated"})
}

func (app *Application) getAllArticleInsuranceHandler(w http.ResponseWriter, r *http.Request) {
	insurance, err := app.Store.Insurance.GetAllArticleInsurance(r.Context())
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": insurance})
}

func (app *Application) createPayPalOrderHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		EventID  string  `json:"event_id"`
		Amount   float64 `json:"amount"`
		Currency string  `json:"currency"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	user := r.Context().Value("user").(*models.User)
	eventID, _ := uuid.Parse(req.EventID)

	tx := &models.PayPalTransaction{
		PayPalOrderID: "PP-" + time.Now().Format("20060102150405") + uuid.New().String()[:8],
		EventID:       eventID,
		UserID:        user.ID,
		Amount:        req.Amount,
		Currency:      req.Currency,
		Status:        "pending",
	}

	if err := app.Store.PayPal.CreateTransaction(r.Context(), tx); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	render.JSON(w, r, map[string]interface{}{
		"data":            tx,
		"paypal_order_id": tx.PayPalOrderID,
	})
}

func (app *Application) capturePayPalOrderHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		PayPalOrderID string `json:"paypal_order_id"`
		CaptureID     string `json:"capture_id"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	if err := app.Store.PayPal.UpdateTransactionCapture(r.Context(), req.PayPalOrderID, req.CaptureID); err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	render.JSON(w, r, map[string]interface{}{"data": "captured"})
}

func (app *Application) getPayPalTransactionsByEventHandler(w http.ResponseWriter, r *http.Request) {
	eventID, _ := uuid.Parse(chi.URLParam(r, "eventId"))

	txs, err := app.Store.PayPal.GetTransactionsByEvent(r.Context(), eventID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": txs})
}

func (app *Application) getClientAuditLogHandler(w http.ResponseWriter, r *http.Request) {
	userID, _ := uuid.Parse(chi.URLParam(r, "userId"))

	logs, err := app.Store.Audit.GetClientAuditLog(r.Context(), userID, 50)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{"data": logs})
}

func (app *Application) getAllAuditLogsHandler(w http.ResponseWriter, r *http.Request) {
	userIDStr := r.URL.Query().Get("user_id")
	action := r.URL.Query().Get("action")
	entityType := r.URL.Query().Get("entity_type")
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")
	limit := 100
	offset := 0

	var userID *uuid.UUID
	if userIDStr != "" {
		uid, _ := uuid.Parse(userIDStr)
		userID = &uid
	}

	logs, total, err := app.Store.Audit.GetAllAuditLogs(r.Context(), userID, action, entityType, startDate, endDate, limit, offset)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}
	render.JSON(w, r, map[string]interface{}{
		"data":  logs,
		"total": total,
	})
}

func (app *Application) clientDashboardHandler(w http.ResponseWriter, r *http.Request) {
	user := r.Context().Value("user").(*models.User)

	events, err := app.Store.Events.GetByUserID(r.Context(), user.ID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	var upcoming *models.Event
	var recent []models.Event
	now := time.Now()

	for _, e := range events {
		if e.Date.After(now) && upcoming == nil {
			upcoming = &e
		} else if len(recent) < 5 {
			recent = append(recent, e)
		}
	}

	render.JSON(w, r, map[string]interface{}{
		"data": map[string]interface{}{
			"user":           user,
			"upcoming_event": upcoming,
			"recent_events":  recent,
		},
	})
}

func (app *Application) clientEventDetailHandler(w http.ResponseWriter, r *http.Request) {
	user := r.Context().Value("user").(*models.User)
	eventID, _ := uuid.Parse(chi.URLParam(r, "id"))

	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil || event.UserID != user.ID {
		render.JSON(w, r, map[string]interface{}{
			"error":   "not_found",
			"message": "Event not found",
		})
		return
	}

	render.JSON(w, r, map[string]interface{}{"data": event})
}

func (app *Application) clientEventDocumentsHandler(w http.ResponseWriter, r *http.Request) {
	user := r.Context().Value("user").(*models.User)
	eventID, _ := uuid.Parse(chi.URLParam(r, "id"))

	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil || event.UserID != user.ID {
		render.JSON(w, r, map[string]interface{}{
			"error":   "not_found",
			"message": "Event not found",
		})
		return
	}

	render.JSON(w, r, map[string]interface{}{
		"data": map[string]interface{}{
			"quote_url":    "/v1/events/" + eventID.String() + "/quote",
			"contract_url": "/v1/events/" + eventID.String() + "/contract",
		},
	})
}

func (app *Application) clientEventPaymentsHandler(w http.ResponseWriter, r *http.Request) {
	user := r.Context().Value("user").(*models.User)
	eventID, _ := uuid.Parse(chi.URLParam(r, "id"))

	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil || event.UserID != user.ID {
		render.JSON(w, r, map[string]interface{}{
			"error":   "not_found",
			"message": "Event not found",
		})
		return
	}

	installments, err := app.Store.Installments.GetInstallmentByEventID(r.Context(), eventID)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	render.JSON(w, r, map[string]interface{}{"data": installments})
}

func (app *Application) clientNotificationsHandler(w http.ResponseWriter, r *http.Request) {
	user := r.Context().Value("user").(*models.User)

	notifications, err := app.Store.Notifications.GetUserNotifications(r.Context(), user.ID, 20)
	if err != nil {
		render.JSON(w, r, map[string]interface{}{"error": err.Error()})
		return
	}

	render.JSON(w, r, map[string]interface{}{"data": notifications})
}
