package main

import (
	"fmt"
	"net/http"
	"time"

	"Backend/internal/pdf"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// getQuotePDFHandler godoc
//
//	@Summary		Generate quote PDF for an event
//	@Description	Generates a professional PDF quote for a specific event with all items and pricing
//	@Tags			events
//	@Produce		application/pdf
//	@Param			id	path		string	true	"Event ID"
//	@Success		200	{file}		file	"PDF quote"
//	@Failure		400	{object}	error
//	@Failure		404	{object}	error
//	@Failure		500	{object}	error
//	@Router			/events/{id}/quote [get]
func (app *Application) getQuotePDFHandler(w http.ResponseWriter, r *http.Request) {
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

	user, err := app.Store.Users.RetrieveById(r.Context(), event.UserID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	items, err := app.Store.Events.GetItems(r.Context(), id)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Build quote items
	var quoteItems []pdf.QuoteItem
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
		quoteItems = append(quoteItems, pdf.QuoteItem{
			Name:       name,
			Quantity:   item.Quantity,
			UnitPrice:  unitPrice,
			TotalPrice: lineTotal,
		})
	}

	eventDate := ""
	if event.Date != nil {
		eventDate = event.Date.Format("02/01/2006")
	}

	paymentMethod := "pendiente"
	if event.PaymentMethod != nil {
		paymentMethod = *event.PaymentMethod
	}

	quoteData := pdf.QuoteData{
		EventName:      event.Name,
		EventDate:     eventDate,
		Location:      event.Location,
		ClientName:    formatClientName(user),
		ClientEmail:   user.Email,
		ClientPhone:   user.PhoneNumber,
		Items:         quoteItems,
		Subtotal:      subtotal,
		AdditionalCosts: event.AdditionalCosts,
		Total:         subtotal + event.AdditionalCosts,
		PaymentMethod: paymentMethod,
		AdminNotes:    event.AdminNotes,
		QuoteNumber:   generateQuoteNumber(event.ID),
		GeneratedAt:   time.Now(),
	}

	pdfBytes, err := pdf.GenerateQuotePDF(quoteData)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	fileName := fmt.Sprintf("cotizacion_%s.pdf", sanitizeFileName(event.Name))
	w.Header().Set("Content-Type", "application/pdf")
	w.Header().Set("Content-Disposition", fmt.Sprintf("inline; filename=\"%s\"", fileName))
	w.Write(pdfBytes)
}

func formatClientName(user *models.User) string {
	if user.FirstName != "" && user.LastName != "" {
		return fmt.Sprintf("%s %s", user.FirstName, user.LastName)
	}
	if user.FirstName != "" {
		return user.FirstName
	}
	if user.UserName != "" {
		return user.UserName
	}
	return user.Email
}

func generateQuoteNumber(eventID uuid.UUID) string {
	t := time.Now()
	return fmt.Sprintf("RF-%d%02d%02d-%s", t.Year(), t.Month(), t.Day(), eventID.String()[:8])
}

func sanitizeFileName(name string) string {
	if name == "" {
		return "evento"
	}
	// Simple sanitization
	result := ""
	for _, c := range name {
		if (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ' || c == '-' || c == '_' {
			result += string(c)
		}
	}
	if result == "" {
		return "evento"
	}
	return result
}
