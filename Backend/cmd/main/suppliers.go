package main

import (
	"net/http"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type createSupplierPayload struct {
	Name        string `json:"name" validate:"required,max=255"`
	ContactName string `json:"contact_name" validate:"omitempty,max=255"`
	Email       string `json:"email" validate:"omitempty,email"`
	Phone       string `json:"phone" validate:"omitempty,max=50"`
	Website     string `json:"website" validate:"omitempty,url"`
	Notes       string `json:"notes" validate:"omitempty"`
}

func (app *Application) addSupplierHandler(w http.ResponseWriter, r *http.Request) {
	var payload createSupplierPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	supplier := &models.Supplier{
		UserID:      user.ID,
		Name:        payload.Name,
		ContactName: payload.ContactName,
		Email:       payload.Email,
		Phone:       payload.Phone,
		Website:     payload.Website,
		Notes:       payload.Notes,
	}

	if err := app.Store.Suppliers.Create(r.Context(), supplier); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, supplier); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) getSuppliersHandler(w http.ResponseWriter, r *http.Request) {
	user := GetUserFromCtx(r)

	suppliers, err := app.Store.Suppliers.GetByUserID(r.Context(), user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if suppliers == nil {
		suppliers = []models.Supplier{}
	}

	if err := app.jsonResponse(w, http.StatusOK, suppliers); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) getSupplierHandler(w http.ResponseWriter, r *http.Request) {
	idStr := chi.URLParam(r, "id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	supplier, err := app.Store.Suppliers.GetByID(r.Context(), id)
	if err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	// Verify ownership
	user := GetUserFromCtx(r)
	if supplier.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, supplier); err != nil {
		app.internalServerError(w, r, err)
	}
}

type updateSupplierPayload struct {
	Name        *string `json:"name" validate:"omitempty,max=255"`
	ContactName *string `json:"contact_name" validate:"omitempty,max=255"`
	Email       *string `json:"email" validate:"omitempty,email"`
	Phone       *string `json:"phone" validate:"omitempty,max=50"`
	Website     *string `json:"website" validate:"omitempty,url"`
	Notes       *string `json:"notes" validate:"omitempty"`
}

func (app *Application) updateSupplierHandler(w http.ResponseWriter, r *http.Request) {
	idStr := chi.URLParam(r, "id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	var payload updateSupplierPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	supplier, err := app.Store.Suppliers.GetByID(r.Context(), id)
	if err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	// Verify ownership
	user := GetUserFromCtx(r)
	if supplier.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	if payload.Name != nil {
		supplier.Name = *payload.Name
	}
	if payload.ContactName != nil {
		supplier.ContactName = *payload.ContactName
	}
	if payload.Email != nil {
		supplier.Email = *payload.Email
	}
	if payload.Phone != nil {
		supplier.Phone = *payload.Phone
	}
	if payload.Website != nil {
		supplier.Website = *payload.Website
	}
	if payload.Notes != nil {
		supplier.Notes = *payload.Notes
	}

	if err := app.Store.Suppliers.Update(r.Context(), supplier); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, supplier); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) deleteSupplierHandler(w http.ResponseWriter, r *http.Request) {
	idStr := chi.URLParam(r, "id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	supplier, err := app.Store.Suppliers.GetByID(r.Context(), id)
	if err != nil {
		if err == store.ErrNotFound {
			app.notFoundResponse(w, r, err)
			return
		}
		app.internalServerError(w, r, err)
		return
	}

	// Verify ownership
	user := GetUserFromCtx(r)
	if supplier.UserID != user.ID {
		app.unauthorized(w, r, nil)
		return
	}

	if err := app.Store.Suppliers.Delete(r.Context(), id); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
