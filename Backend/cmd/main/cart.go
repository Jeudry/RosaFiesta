package main

import (
	"net/http"

	"Backend/cmd/main/view_models/cart"
	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

// @Summary		Get Cart
// @Description	Get current user's cart
// @Tags			cart
// @Accept			json
// @Produce		json
// @Security		BearerAuth
// @Success		200	{object}	models.Cart	"User's Cart"
// @Failure		404	{object}	error		"Cart not found"
// @Failure		500	{object}	error		"Internal server error"
// @Router			/cart [get]
func (app *Application) getCartHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	user := GetUserFromCtx(r)

	cart, err := app.Store.Carts.GetByUserID(ctx, user.ID)
	if err != nil {
		if err == store.ErrNotFound { // Assuming store returns this or similar
			// If not found, return empty cart structure or 404?
			// Usually valid user should have a cart or we create one on fly?
			// For now, let's just return 404 or empty.
			// Strategy: Create a new cart if not exists?
			// Let's create one if not exists for better UX
			newCart := &models.Cart{
				UserID: user.ID,
			}
			if err := app.Store.Carts.Create(ctx, newCart); err != nil {
				app.internalServerError(w, r, err)
				return
			}
			// Return the empty new cart
			// Fetch again to ensure it's structured right or just return valid struct
			newCart.Items = []models.CartItem{}
			if err := app.jsonResponse(w, http.StatusOK, newCart); err != nil {
				app.internalServerError(w, r, err)
			}
			return
		}
		// If error is distinct from "not found" (e.g. DB error), handle it:
		// We need to check if we exposed ErrNotFound from store.
		// In store.go: ErrNotFound = errors.New("resource not found")
		// In carts.go: returns ErrNotFound.
		// We have to use that variable.
		// But ErrNotFound is in store package "Backend/internal/store".
		// We likely need to import it or define it.
		// `app.Store` is of type `store.Storage`.
		// Let's assume we handle generic error for now or check string if needed.
		// Ideally imports "Backend/internal/store".
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, cart); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Add Item to Cart
// @Description	Add an item to the cart or update quantity if exists
// @Tags			cart
// @Accept			json
// @Produce		json
// @Param			payload	body	cart.AddItemPayload	true	"Add item payload"
// @Security		BearerAuth
// @Success		200	{object}	models.Cart		"Updated Cart or success message"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/cart/items [post]
func (app *Application) addItemToCartHandler(w http.ResponseWriter, r *http.Request) {
	var payload cart.AddItemPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()
	user := GetUserFromCtx(r)

	// Ensure cart exists
	c, err := app.Store.Carts.GetByUserID(ctx, user.ID)
	if err != nil {
		// Create if not found
		c = &models.Cart{UserID: user.ID}
		if err := app.Store.Carts.Create(ctx, c); err != nil {
			app.internalServerError(w, r, err)
			return
		}
	}

	item := &models.CartItem{
		CartID:    c.ID,
		ArticleID: payload.ArticleID,
		VariantID: payload.VariantID,
		Quantity:  payload.Quantity,
	}

	if err := app.Store.Carts.AddItem(ctx, item); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Optionally return the full updated cart
	updatedCart, err := app.Store.Carts.GetByUserID(ctx, user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, updatedCart); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Update Cart Item Quantity
// @Description	Update the quantity of a specific cart item
// @Tags			cart
// @Accept			json
// @Produce		json
// @Param			itemId	path	string					true	"Cart Item ID"
// @Param			payload	body	cart.UpdateItemPayload	true	"Update payload"
// @Security		BearerAuth
// @Success		200	{object}	models.Cart		"Updated Cart"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/cart/items/{itemId} [patch]
func (app *Application) updateCartItemHandler(w http.ResponseWriter, r *http.Request) {
	var payload cart.UpdateItemPayload
	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	itemID, err := uuid.Parse(chi.URLParam(r, "itemId"))
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()

	if err := app.Store.Carts.UpdateItemQuantity(ctx, itemID, payload.Quantity); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Return updated cart
	user := GetUserFromCtx(r)
	updatedCart, err := app.Store.Carts.GetByUserID(ctx, user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, updatedCart); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Remove Item from Cart
// @Description	Remove an item from the cart
// @Tags			cart
// @Accept			json
// @Produce		json
// @Param			itemId	path	string	true	"Cart Item ID"
// @Security		BearerAuth
// @Success		200	{object}	models.Cart		"Updated Cart"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/cart/items/{itemId} [delete]
func (app *Application) removeCartItemHandler(w http.ResponseWriter, r *http.Request) {
	itemID, err := uuid.Parse(chi.URLParam(r, "itemId"))
	if err != nil {
		app.badRequest(w, r, err)
		return
	}

	ctx := r.Context()

	if err := app.Store.Carts.RemoveItem(ctx, itemID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	// Return updated cart
	user := GetUserFromCtx(r)
	updatedCart, err := app.Store.Carts.GetByUserID(ctx, user.ID)
	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, updatedCart); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Clear Cart
// @Description	Remove all items from the cart
// @Tags			cart
// @Accept			json
// @Produce		json
// @Security		BearerAuth
// @Success		204	{object}	nil		"Cart Cleared"
// @Failure		500	{object}	error	"Internal server error"
// @Router			/cart [delete]
func (app *Application) clearCartHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	user := GetUserFromCtx(r)

	// Get cart ID first
	cart, err := app.Store.Carts.GetByUserID(ctx, user.ID)
	if err != nil {
		// If no cart, nothing to clear
		app.jsonResponse(w, http.StatusNoContent, nil)
		return
	}

	if err := app.Store.Carts.ClearCart(ctx, cart.ID); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusNoContent, nil); err != nil {
		app.internalServerError(w, r, err)
	}
}
