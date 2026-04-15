package main

import (
	"errors"
	"fmt"
	"io"
	"net/http"
	"time"

	evm "Backend/cmd/main/view_models/events"
	"Backend/internal/pdf"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// createEventHandler godoc
//
//	@Summary		Create a new event
//	@Description	Create a new event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			payload	body		models.CreateEventPayload	true	"Event payload"
//	@Success		201		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events [post]
func (app *Application) createEventHandler(w http.ResponseWriter, r *http.Request) {
	var payload models.CreateEventPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	// Parse date - Try RFC3339 first, then common Flutter/ISO8601 formats
	var date time.Time
	date, err := time.Parse(time.RFC3339, payload.Date)
	if err != nil {
		// Try without Z (common in some Flutter clients)
		date, err = time.Parse("2006-01-02T15:04:05.000", payload.Date)
		if err != nil {
			date, err = time.Parse("2006-01-02T15:04:05", payload.Date)
			if err != nil {
				app.badRequest(w, r, fmt.Errorf("invalid date format: %v", err))
				return
			}
		}
	}

	event := &models.Event{
		UserID:     user.ID,
		Name:       payload.Name,
		Date:       &date,
		Location:   payload.Location,
		GuestCount: payload.GuestCount,
		Budget:     payload.Budget,
		Status:     "planning",
	}

	if err := app.Store.Events.Create(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Audit log
	_ = app.Store.AuditLogs.Log(r.Context(), &models.AuditLog{
		UserID:     &user.ID,
		EventID:   &event.ID,
		Action:    models.AuditActionEventCreate,
		EntityType: "event",
		EntityID:  &event.ID,
	})

	if err := app.jsonResponse(w, http.StatusCreated, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getEventHandler godoc
//
//	@Summary		Get event by ID
//	@Description	Get event by ID
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{object}	models.Event
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id} [get]
func (app *Application) getEventHandler(w http.ResponseWriter, r *http.Request) {
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

	// Authorization check: ensure user owns the event
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getUserEventsHandler godoc
//
//	@Summary		Get all events for current user
//	@Description	Get all events for current user
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Success		200	{array}		models.Event
//	@Failure		500	{object}	error
//	@Router			/events [get]
func (app *Application) getUserEventsHandler(w http.ResponseWriter, r *http.Request) {
	user := GetUserFromCtx(r)

	events, err := app.Store.Events.GetByUserID(r.Context(), user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if events == nil {
		events = []models.Event{}
	}

	if err := app.jsonResponse(w, http.StatusOK, events); err != nil {
		app.internalServerError(w, r, err)
	}
}

// updateEventHandler godoc
//
//	@Summary		Update an event
//	@Description	Update an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string							true	"Event ID"
//	@Param			payload	body		models.UpdateEventPayload	true	"Event payload"
//	@Success		200		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id} [put]
func (app *Application) updateEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload models.UpdateEventPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
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

	// Authorization check
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to update this event"))
		return
	}

	if payload.Name != nil {
		event.Name = *payload.Name
	}
	if payload.Date != nil {
		var date time.Time
		date, err := time.Parse(time.RFC3339, *payload.Date)
		if err != nil {
			date, err = time.Parse("2006-01-02T15:04:05.000", *payload.Date)
			if err != nil {
				date, err = time.Parse("2006-01-02T15:04:05", *payload.Date)
				if err != nil {
					app.badRequest(w, r, fmt.Errorf("invalid date format: %v", err))
					return
				}
			}
		}
		event.Date = &date
	}
	if payload.Location != nil {
		event.Location = *payload.Location
	}
	if payload.GuestCount != nil {
		event.GuestCount = *payload.GuestCount
	}
	if payload.Budget != nil {
		event.Budget = *payload.Budget
	}
	if payload.Status != nil {
		event.Status = *payload.Status
	}

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// deleteEventHandler godoc
//
//	@Summary		Delete an event
//	@Description	Delete an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		204	{object}	nil
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id} [delete]
func (app *Application) deleteEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Fetch first to check ownership
	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to delete this event"))
		return
	}

	if err := app.Store.Events.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// addEventItemHandler godoc
//
//	@Summary		Add item to event
//	@Description	Add item to event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string					true	"Event ID"
//	@Param			payload	body		object					true	"Item payload"
//	@Success		201		{object}	models.EventItem
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/items [post]
func (app *Application) addEventItemHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		ArticleID uuid.UUID `json:"article_id"`
		Quantity  int       `json:"quantity"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to modify this event"))
		return
	}

	item := &models.EventItem{
		EventID:   eventID,
		ArticleID: payload.ArticleID,
		Quantity:  payload.Quantity,
	}
	if item.Quantity <= 0 {
		item.Quantity = 1
	}

	// Phase 17: Availability & Inventory Check
	availability, err := app.Store.Articles.GetAvailability(r.Context(), payload.ArticleID, *event.Date)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	if availability < item.Quantity {
		app.badRequest(w, r, errors.New("insufficient stock for this date"))
		return
	}

	if err := app.Store.Events.AddItem(r.Context(), item); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, item); err != nil {
		app.internalServerError(w, r, err)
	}
}

// removeEventItemHandler godoc
//
//	@Summary		Remove item from event
//	@Description	Remove item from event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			itemId	path		string	true	"Item ID"
//	@Success		204		{object}	nil
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/items/{itemId} [delete]
func (app *Application) removeEventItemHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	itemIDParam := chi.URLParam(r, "itemId")
	itemID, err := uuid.Parse(itemIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to modify this event"))
		return
	}

	if err := app.Store.Events.RemoveItem(r.Context(), eventID, itemID); err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// getEventItemsHandler godoc
//
//	@Summary		Get items for an event
//	@Description	Get items for an event
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{array}		models.EventItem
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/items [get]
func (app *Application) getEventItemsHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	items, err := app.Store.Events.GetItems(r.Context(), eventID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if items == nil {
		items = []models.EventItem{}
	}

	if err := app.jsonResponse(w, http.StatusOK, items); err != nil {
		app.internalServerError(w, r, err)
	}
}

// payEventHandler godoc
//
//	@Summary		Simulate event payment
//	@Description	Simulate event payment and update status to 'paid'
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			payload	body		object	true	"Payment payload"
//	@Success		200		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/pay [post]
func (app *Application) payEventHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		PaymentMethod string `json:"payment_method" validate:"required"`
		Phone        string `json:"phone"`
		IsDeposit    bool   `json:"is_deposit"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
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

	// Authorization check
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to pay for this event"))
		return
	}

	if event.Status != "confirmed" {
		app.badRequest(w, r, errors.New("only confirmed events can be paid"))
		return
	}

	// Update phone number if provided
	if payload.Phone != "" {
		if err := app.Store.Users.UpdatePhoneNumber(r.Context(), user.ID, payload.Phone); err != nil {
			app.internalServerError(w, r, err)
			return
		}
	}

	now := time.Now()

	// Handle deposit vs full payment
	if payload.IsDeposit {
		// Deposit payment - 50% of total quote
		if event.DepositPaid {
			app.badRequest(w, r, errors.New("deposit already paid for this event"))
			return
		}

		// Calculate 50% deposit
		depositAmount := event.TotalQuote / 2
		remainingAmount := event.TotalQuote - depositAmount

		// Set deposit fields
		event.DepositPaid = true
		event.DepositAmount = depositAmount
		event.DepositPaidAt = &now
		event.RemainingAmount = remainingAmount

		// Set installment due date to 7 days before event (or 30 days from now, whichever is earlier)
		dueDate := now.AddDate(0, 0, 30) // Default: 30 days from now
		if event.Date != nil {
			daysUntilEvent := int(time.Until(*event.Date).Hours() / 24)
			if daysUntilEvent > 7 {
				dueDate = now.AddDate(0, 0, daysUntilEvent-7)
			} else {
				dueDate = now.AddDate(0, 0, 7) // Minimum 7 days
			}
		}
		event.InstallmentDueDate = &dueDate

		// Create pending installment payment record for the remaining amount
		_, err = app.Store.Installments.CreateInstallmentPayment(r.Context(), event.ID, remainingAmount, &dueDate)
		if err != nil {
			app.internalServerError(w, r, err)
			return
		}

		// Mark the deposit payment as completed via installment system
		// (We use event.PaymentStatus for tracking overall status)
		event.PaymentStatus = "deposit_paid"
		event.PaymentMethod = &payload.PaymentMethod
		event.PaidAt = &now
		// Event status remains "confirmed" until full payment
	} else {
		// Full payment
		if event.DepositPaid {
			// This is the final payment - mark the pending installment as paid
			installments, _ := app.Store.Installments.GetInstallmentByEventID(r.Context(), event.ID)
			for _, inst := range installments {
				if inst.PaymentStatus == "pending" {
					_ = app.Store.Installments.MarkPaid(r.Context(), inst.ID, payload.PaymentMethod)
				}
			}
			event.PaymentStatus = "completed"
		} else {
			// Paying full amount directly (no deposit was made)
			event.PaymentStatus = "completed"
		}

		event.PaymentMethod = &payload.PaymentMethod
		event.PaidAt = &now
		event.Status = "paid"
		event.RemainingAmount = 0
	}

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Audit log
	_ = app.Store.AuditLogs.Log(r.Context(), &models.AuditLog{
		UserID:     &user.ID,
		EventID:   &event.ID,
		Action:    models.AuditActionEventPay,
		EntityType: "event",
		EntityID:  &event.ID,
		NewValue:  &payload.PaymentMethod,
	})

	// Notify based on payment type
	if payload.IsDeposit {
		_ = app.Notifications.NotifyStatusChange(r.Context(), user.FCMToken, event.Name, "Reserva pagada")
	} else {
		_ = app.Notifications.NotifyStatusChange(r.Context(), user.FCMToken, event.Name, "Pagado")
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getPaymentScheduleHandler godoc
//
//	@Summary		Get payment schedule for an event
//	@Description	Get payment schedule showing deposit status and remaining payments
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Success		200		{object}	models.PaymentSchedule
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/payment-schedule [get]
func (app *Application) getPaymentScheduleHandler(w http.ResponseWriter, r *http.Request) {
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

	// Authorization check
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	// Get pending installments
	installments, err := app.Store.Installments.GetInstallmentByEventID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Filter pending payments
	var pendingPayments []models.InstallmentPayment
	for _, inst := range installments {
		if inst.PaymentStatus == "pending" {
			pendingPayments = append(pendingPayments, inst)
		}
	}

	schedule := models.PaymentSchedule{
		DepositPaid:        event.DepositPaid,
		DepositAmount:      event.DepositAmount,
		DepositPaidAt:      event.DepositPaidAt,
		RemainingAmount:    event.RemainingAmount,
		InstallmentDueDate: event.InstallmentDueDate,
		TotalQuote:         event.TotalQuote,
		PendingPayments:    pendingPayments,
	}

	if err := app.jsonResponse(w, http.StatusOK, schedule); err != nil {
		app.internalServerError(w, r, err)
	}
}

// adjustQuoteHandler godoc
//
//	@Summary		Adjust event quote (Admin only)
//	@Description	Adjust event quote with additional costs and notes, and set status to 'adjusted'
//	@Tags			admin, events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			payload	body		object	true	"Adjustment payload"
//	@Success		200		{object}	models.Event
//	@Failure		400		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/admin/events/{id}/adjust [patch]
func (app *Application) adjustQuoteHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		AdditionalCosts float64 `json:"additional_costs" validate:"min=0"`
		AdminNotes      string  `json:"admin_notes"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
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

	// Update quotation fields
	event.AdditionalCosts = payload.AdditionalCosts
	event.AdminNotes = payload.AdminNotes
	event.Status = "adjusted"

	if err := app.Store.Events.Update(r.Context(), event); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Audit log
	adminUser := GetUserFromCtx(r)
	newVal := fmt.Sprintf("additional_costs=%.2f", payload.AdditionalCosts)
	_ = app.Store.AuditLogs.Log(r.Context(), &models.AuditLog{
		UserID:     &adminUser.ID,
		EventID:   &event.ID,
		Action:    models.AuditActionEventAdjust,
		EntityType: "event",
		EntityID:  &event.ID,
		NewValue:  &newVal,
	})

	// Phase 20: Notify user about adjustment
	// We need to fetch the user to get their FCM token
	user, err := app.Store.Users.RetrieveById(r.Context(), event.UserID)
	if err == nil && user.FCMToken != "" {
		title := "Cotización ajustada 🌸"
		body := fmt.Sprintf("Tu evento %s tiene una nueva cotización pendiente", event.Name)
		_ = app.Notifications.SendPush(r.Context(), user.FCMToken, title, body)
		_ = app.Store.NotificationLogs.LogNotification(r.Context(), event.ID, models.QuoteAdjusted)
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// approveQuoteHandler godoc
//
//	@Summary		Approve event quote
//	@Description	Approve the adjusted quote for an event and set status to 'paid'
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{object}	models.Event
//	@Failure		400	{object}	error
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/approve-quote [post]
func (app *Application) approveQuoteHandler(w http.ResponseWriter, r *http.Request) {
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

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to approve this quote"))
		return
	}

	if event.Status != models.EventStatusAdjusted {
		app.badRequest(w, r, errors.New("only events with 'adjusted' status can be approved"))
		return
	}

	if err := app.Store.Events.ApproveQuote(r.Context(), id, user.ID); err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	// Re-fetch the updated event
	event, err = app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Audit log
	_ = app.Store.AuditLogs.Log(r.Context(), &models.AuditLog{
		UserID:     &user.ID,
		EventID:   &event.ID,
		Action:    models.AuditActionEventPay,
		EntityType: "event",
		EntityID:  &event.ID,
	})

	// Send FCM notification
	eventOwner, err := app.Store.Users.RetrieveById(r.Context(), event.UserID)
	if err == nil && eventOwner.FCMToken != "" {
		title := "¡Evento aprobado! 🎉"
		body := fmt.Sprintf("Tu evento %s ha sido aprobado", event.Name)
		_ = app.Notifications.SendPush(r.Context(), eventOwner.FCMToken, title, body)
		_ = app.Store.NotificationLogs.LogNotification(r.Context(), event.ID, models.QuoteApproved)
	}

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

// rejectQuoteHandler godoc
//
//	@Summary		Reject event quote
//	@Description	Reject the adjusted quote for an event and set status to 'rejected'
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{object}	models.Event
//	@Failure		400	{object}	error
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/reject-quote [post]
func (app *Application) rejectQuoteHandler(w http.ResponseWriter, r *http.Request) {
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

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to reject this quote"))
		return
	}

	if event.Status != models.EventStatusAdjusted {
		app.badRequest(w, r, errors.New("only events with 'adjusted' status can be rejected"))
		return
	}

	if err := app.Store.Events.RejectQuote(r.Context(), id, user.ID); err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	// Re-fetch the updated event
	event, err = app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Audit log
	_ = app.Store.AuditLogs.Log(r.Context(), &models.AuditLog{
		UserID:     &user.ID,
		EventID:   &event.ID,
		Action:    models.AuditActionEventReject,
		EntityType: "event",
		EntityID:  &event.ID,
	})

	if err := app.jsonResponse(w, http.StatusOK, event); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) getEventDebriefHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	debrief, err := app.Store.Events.GetDebrief(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, debrief); err != nil {
		app.internalServerError(w, r, err)
	}
}

// uploadEventPhotoHandler godoc
//
//	@Summary		Upload photo for an event
//	@Description	Upload a photo to R2 and associate it with an event
//	@Tags			events
//	@Accept			multipart/form-data
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			file	formData	file	true	"Photo file"
//	@Param			caption	formData	string	false	"Photo caption"
//	@Success		200		{object}	models.EventPhoto
//	@Failure		400		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/photos [post]
func (app *Application) uploadEventPhotoHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := r.ParseMultipartForm(10 << 20); err != nil { // 10MB max
		app.badRequest(w, r, err)
		return
	}

	file, fileHeader, err := r.FormFile("file")
	if err != nil {
		app.badRequest(w, r, errors.New("file is required"))
		return
	}
	defer file.Close()

	filename := fileHeader.Filename
	caption := r.FormValue("caption")

	// Read file content
	content, err := io.ReadAll(file)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Determine content type from file header
	contentType := http.DetectContentType(content)
	if contentType == "application/octet-stream" {
		contentType = "image/jpeg"
	}

	var url string
	if app.R2 != nil {
		// Upload to R2
		url, err = app.R2.UploadFromBytes(r.Context(), id, filename, contentType, content)
		if err != nil {
			app.internalServerError(w, r, err)
			return
		}
	} else {
		// R2 not configured, use a placeholder URL
		url = fmt.Sprintf("https://pub.example.r2.cloudflarestorage.com/rosafiesta/events/%s/%s", id.String(), filename)
	}

	// Save to database
	photo := &models.EventPhoto{
		EventID: id,
		URL:     url,
	}
	if caption != "" {
		photo.Caption = &caption
	}

	if err := app.Store.EventPhotos.Create(r.Context(), photo); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, photo); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getEventPhotosHandler godoc
//
//	@Summary		Get photos for an event
//	@Description	Get all photos associated with an event
//	@Tags			events
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Success		200		{object}	[]models.EventPhoto
//	@Failure		500		{object}	error
//	@Router			/events/{id}/photos [get]
func (app *Application) getEventPhotosHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	photos, err := app.Store.EventPhotos.GetByEventID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, photos); err != nil {
		app.internalServerError(w, r, err)
	}
}

// calculateDeliveryHandler godoc
//
//	@Summary		Calculate delivery fee
//	@Description	Calculate delivery fee based on event address
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			payload	body		object	true	"Address payload"
//	@Success		200		{object}	models.DeliveryFeeResponse
//	@Failure		400		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/calculate-delivery [post]
func (app *Application) calculateDeliveryHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify event exists and user has access
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	var payload struct {
		Address string `json:"address" validate:"required"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if payload.Address == "" {
		app.badRequest(w, r, errors.New("address is required"))
		return
	}

	feeResponse, err := app.Store.DeliveryZones.CalculateFee(r.Context(), payload.Address)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, feeResponse); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getShareCardHandler godoc
//
//	@Summary		Get shareable event card as HTML
//	@Description	Returns an HTML page with event summary for sharing via WhatsApp/social media
//	@Tags			events
//	@Produce		html
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{string}	html
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/share-card [get]
func (app *Application) getShareCardHandler(w http.ResponseWriter, r *http.Request) {
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

	// Get first photo if available
	photos, _ := app.Store.EventPhotos.GetByEventID(r.Context(), id)
	photoUrl := ""
	if len(photos) > 0 {
		photoUrl = photos[0].URL
	}

	eventDate := ""
	if event.Date != nil {
		eventDate = event.Date.Format("2 de enero de 2006")
	}

	statusLabel := map[string]string{
		"planning":   "Planeando",
		"requested":   "En Revision",
		"adjusted":   "Cotizacion Lista",
		"confirmed":  "Confirmado",
		"paid":       "Pagado y Reservado",
		"completed":  "Evento Finalizado",
	}[event.Status]
	if statusLabel == "" {
		statusLabel = event.Status
	}

	html := fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta property="og:title" content="Mi Evento: %s | RosaFiesta">
  <meta property="og:description" content="🎉 ¡Mi evento '%s' (%s) esta confirmado! Organizado con RosaFiesta 🌸">
  <meta property="og:type" content="website">
  <meta name="twitter:card" content="summary_large_image">
  <title>%s | RosaFiesta</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Segoe UI', system-ui, sans-serif; background: linear-gradient(135deg, #FF3CAC 0%%, #8B5CF6 100%%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
    .card { background: white; border-radius: 24px; overflow: hidden; max-width: 400px; width: 100%%; box-shadow: 0 20px 60px rgba(0,0,0,0.2); }
    .card-image { width: 100%%; height: 200px; object-fit: cover; background: linear-gradient(135deg, #FF3CAC 0%%, #8B5CF6 100%%); }
    .card-body { padding: 28px 24px; }
    .badge { display: inline-block; background: linear-gradient(135deg, #FF3CAC, #8B5CF6); color: white; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-bottom: 12px; }
    h1 { font-size: 26px; font-weight: 800; color: #1a1a2e; margin-bottom: 8px; line-height: 1.2; }
    .date { color: #8B5CF6; font-size: 16px; font-weight: 600; margin-bottom: 16px; display: flex; align-items: center; gap: 6px; }
    .location { color: #666; font-size: 14px; margin-bottom: 20px; display: flex; align-items: center; gap: 6px; }
    .footer { background: linear-gradient(135deg, #FF3CAC 0%%, #8B5CF6 100%%); padding: 20px; text-align: center; }
    .footer p { color: white; font-size: 14px; font-weight: 600; }
    .footer span { opacity: 0.9; font-size: 12px; }
  </style>
</head>
<body>
  <div class="card">
    <img src="%s" alt="Event photo" class="card-image" onerror="this.style.display='none'">
    <div class="card-body">
      <span class="badge">%s</span>
      <h1>%s</h1>
      <div class="date">📅 %s</div>
      <div class="location">📍 %s</div>
    </div>
    <div class="footer">
      <p>Organizado con RosaFiesta 🌸</p>
      <span>rosafiesta.com</span>
    </div>
  </div>
</body>
</html>`, event.Name, event.Name, statusLabel, event.Name, photoUrl, statusLabel, event.Name, eventDate, event.Location)

	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(html))
}

// uploadInspirationHandler godoc
//
//	@Summary		Upload inspiration photo for an event
//	@Description	Upload a mood board / inspiration photo to R2 and associate it with an event
//	@Tags			events
//	@Accept			multipart/form-data
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			file	formData	file	true	"Photo file"
//	@Param			caption	formData	string	false	"Photo caption"
//	@Success		200		{object}	models.EventInspiration
//	@Failure		400		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/inspiration [post]
func (app *Application) uploadInspirationHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := r.ParseMultipartForm(10 << 20); err != nil { // 10MB max
		app.badRequest(w, r, err)
		return
	}

	file, fileHeader, err := r.FormFile("file")
	if err != nil {
		app.badRequest(w, r, errors.New("file is required"))
		return
	}
	defer file.Close()

	filename := fileHeader.Filename
	caption := r.FormValue("caption")

	// Read file content
	content, err := io.ReadAll(file)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Determine content type from file header
	contentType := http.DetectContentType(content)
	if contentType == "application/octet-stream" {
		contentType = "image/jpeg"
	}

	var url string
	if app.R2 != nil {
		// Upload to R2 under inspiration/ prefix for mood boards
		uploadedURL, err := app.R2.UploadFromBytes(r.Context(), id, filename, contentType, content)
		if err != nil {
			app.internalServerError(w, r, err)
			return
		}
		url = uploadedURL
	} else {
		// R2 not configured, use a placeholder URL
		url = fmt.Sprintf("https://pub.example.r2.cloudflarestorage.com/rosafiesta/inspiration/%s/%s", id.String(), filename)
	}

	user := GetUserFromCtx(r)

	if err := app.Store.Inspiration.Upload(r.Context(), id, url, caption, user.ID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	inspiration := &models.EventInspiration{
		EventID:    id,
		PhotoURL:   url,
		UploadedBy: user.ID,
	}
	if caption != "" {
		inspiration.Caption = &caption
	}

	if err := app.jsonResponse(w, http.StatusOK, inspiration); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getInspirationHandler godoc
//
//	@Summary		Get inspiration photos for an event
//	@Description	Get all mood board / inspiration photos for an event
//	@Tags			events
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Success		200		{object}	[]models.EventInspiration
//	@Failure		500		{object}	error
//	@Router			/events/{id}/inspiration [get]
func (app *Application) getInspirationHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	photos, err := app.Store.Inspiration.GetByEventID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, photos); err != nil {
		app.internalServerError(w, r, err)
	}
}

// deleteInspirationHandler godoc
//
//	@Summary		Delete an inspiration photo
//	@Description	Delete an inspiration photo from an event's mood board
//	@Tags			events
//	@Param			id		path		string	true	"Event ID"
//	@Param			photoId	path		string	true	"Inspiration Photo ID"
//	@Success		204		{object}	nil
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/inspiration/{photoId} [delete]
func (app *Application) deleteInspirationHandler(w http.ResponseWriter, r *http.Request) {
	eventIDParam := chi.URLParam(r, "id")
	eventID, err := uuid.Parse(eventIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	photoIDParam := chi.URLParam(r, "photoId")
	photoID, err := uuid.Parse(photoIDParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), eventID)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to modify this event"))
		return
	}

	if err := app.Store.Inspiration.Delete(r.Context(), photoID); err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// getContractPDFHandler godoc
//
//	@Summary		Generate contract PDF for an event
//	@Description	Generates a formal contract PDF for a paid event with all items, terms, and signature lines
//	@Tags			events
//	@Produce		application/pdf
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{file}		file	"PDF contract"
//	@Failure		400	{object}	error
//	@Failure		403	{object}	error
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/contract [get]
func (app *Application) getContractPDFHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		app.notFoundResponse(w, r, err)
		return
	}

	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, nil)
		return
	}

	if event.Status != "paid" {
		app.badRequest(w, r, fmt.Errorf("contract is only available for paid events"))
		return
	}

	clientUser, err := app.Store.Users.RetrieveById(r.Context(), event.UserID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	items, err := app.Store.Events.GetItems(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	var contractItems []pdf.ContractItem
	var subtotal float64
	for _, item := range items {
		unitPrice := item.UnitPrice()
		lineTotal := unitPrice * float64(item.Quantity)
		subtotal += lineTotal
		name := ""
		if item.Article != nil {
			name = item.Article.NameTemplate
			if item.Variant != nil {
				name = fmt.Sprintf("%s - %s", item.Article.NameTemplate, item.Variant.Name)
			}
		}
		if name == "" {
			name = "Artículo"
		}
		contractItems = append(contractItems, pdf.ContractItem{
			Name:       name,
			Quantity:   item.Quantity,
			UnitPrice:  unitPrice,
			TotalPrice: lineTotal,
		})
	}

	eventDate := ""
	if event.Date != nil {
		eventDate = event.Date.Format("2 de enero de 2006")
	}

	paymentMethod := "No especificado"
	if event.PaymentMethod != nil {
		paymentMethod = *event.PaymentMethod
	}

	total := subtotal + event.AdditionalCosts
	depositPaid := total
	remainingAmount := 0.0

	dueDate := "N/A"
	if event.PaidAt != nil {
		dueDate = event.PaidAt.Format("02 de enero de 2006")
	}

	contractData := pdf.ContractData{
		EventName:       event.Name,
		EventDate:       eventDate,
		EventLocation:   event.Location,
		EventType:       "",
		ClientName:      formatClientName(clientUser),
		ClientEmail:     clientUser.Email,
		ClientPhone:     clientUser.PhoneNumber,
		Items:           contractItems,
		Subtotal:        subtotal,
		DeliveryFee:     0,
		AdditionalCosts: event.AdditionalCosts,
		Total:           total,
		DepositPaid:     depositPaid,
		RemainingAmount: remainingAmount,
		DueDate:         dueDate,
		PaymentMethod:   paymentMethod,
		GeneratedAt:     time.Now(),
	}

	pdfBytes, err := pdf.GenerateContract(contractData)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	dateStr := ""
	if event.Date != nil {
		dateStr = event.Date.Format("20060102")
	}
	fileName := fmt.Sprintf("contrato_%s_%s.pdf", sanitizeFileName(event.Name), dateStr)
	w.Header().Set("Content-Type", "application/pdf")
	w.Header().Set("Content-Disposition", fmt.Sprintf("inline; filename=\"%s\"", fileName))
	w.Write(pdfBytes)
}

// setEventColorsHandler godoc
//
//	@Summary		Set event color palette
//	@Description	Replace the color palette for an event with a new list of hex colors (max 5)
//	@Tags			events
//	@Accept			json
//	@Produce		json
//	@Param			id		path		string	true	"Event ID"
//	@Param			payload	body		object	true	"Colors payload {colors: ['#FFB800']}"
//	@Success		200		{array}	string	"Array of color hex strings"
//	@Failure		400		{object}	error
//	@Failure		401		{object}	error
//	@Failure		404		{object}	error
//	@Failure		500		{object}	error
//	@Router			/events/{id}/colors [put]
func (app *Application) setEventColorsHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload struct {
		Colors []string `json:"colors"`
	}
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if len(payload.Colors) > 5 {
		app.badRequest(w, r, errors.New("maximum 5 colors allowed"))
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to modify this event"))
		return
	}

	if err := app.Store.EventColors.SetColors(r.Context(), id, payload.Colors); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, payload.Colors); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getEventColorsHandler godoc
//
//	@Summary		Get event color palette
//	@Description	Get all color hex strings for a given event
//	@Tags			events
//	@Produce		json
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{array}	string	"Array of color hex strings"
//	@Failure		401	{object}	error
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/colors [get]
func (app *Application) getEventColorsHandler(w http.ResponseWriter, r *http.Request) {
	idParam := chi.URLParam(r, "id")
	id, err := uuid.Parse(idParam)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	// Verify ownership
	event, err := app.Store.Events.GetByID(r.Context(), id)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			app.notFoundResponse(w, r, err)
		} else {
			app.internalServerError(w, r, err)
		}
		return
	}
	user := GetUserFromCtx(r)
	if event.UserID != user.ID {
		app.forbidden(w, r, errors.New("you do not have permission to view this event"))
		return
	}

	colors, err := app.Store.EventColors.GetByEventID(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, colors); err != nil {
		app.internalServerError(w, r, err)
	}
}

// getMyReservationsHandler godoc
//
//	@Summary		Get all reservations for current user
//	@Description	Returns all events (past and upcoming) with payment summary, contract/photo/review availability
//	@Tags			events
//	@Produce		json
//	@Success		200	{array}	events.ReservationSummary
//	@Failure		401	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/my-reservations [get]
func (app *Application) getMyReservationsHandler(w http.ResponseWriter, r *http.Request) {
	user := GetUserFromCtx(r)

	events, err := app.Store.Events.GetByUserID(r.Context(), user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	summaries := make([]evm.ReservationSummary, 0, len(events))
	now := time.Now()

	for _, event := range events {
		dateStr := ""
		if event.Date != nil {
			dateStr = event.Date.Format("2006-01-02")
		}

		// Check if contract is ready (status = paid)
		contractReady := event.Status == "paid"

		// Receipt is available when paid
		receiptReady := event.PaidAt != nil

		// Check if event has photos
		photos, _ := app.Store.EventPhotos.GetByEventID(r.Context(), event.ID)
		hasPhotos := len(photos) > 0

		// Check if review was already given
		reviews, _ := app.Store.EventReviews.GetByEventID(r.Context(), event.ID)
		reviewGiven := len(reviews) > 0

		summaries = append(summaries, evm.ReservationSummary{
			ID:            event.ID,
			Name:          event.Name,
			Date:          &dateStr,
			Status:        event.Status,
			PaymentStatus: event.PaymentStatus,
			TotalQuote:    event.TotalQuote,
			DepositPaid:   event.DepositAmount,
			Remaining:     event.RemainingAmount,
			ContractReady: contractReady,
			ReceiptReady:  receiptReady,
			HasPhotos:     hasPhotos,
			GuestCount:    event.GuestCount,
			ReviewGiven:   reviewGiven,
			Location:      event.Location,
		})
	}

	_ = now // suppress unused warning

	if err := app.jsonResponse(w, http.StatusOK, summaries); err != nil {
		app.internalServerError(w, r, err)
	}
}
