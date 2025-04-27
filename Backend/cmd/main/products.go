package main

import (
	"Backend/cmd/main/view_models"
	"Backend/internal/store/models"
	"context"
	"github.com/go-chi/chi/v5"
	"net/http"
	"strconv"
)

type productKey string

const productCtx postKey = "product"

// @Summary		Creates Product
// @Description	Creates a new product with the provided info
// @Tags			products
// @Accept			json
// @Produce		json
// @Param			payload	body	view_models.CreateProductPayload	true	"Product creation payload"
// @Security		ApiKeyAuth
// @Success		201	{object}	models.Product	"Created product"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/products [post]
func (app *Application) createProductHandler(w http.ResponseWriter, r *http.Request) {
	var payload view_models.CreateProductPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	product := &models.Product{
		BaseModel: models.BaseModel{
			CreatedBy: &user.UserName,
		},
		Name:        payload.Name,
		Description: payload.Description,
		Price:       payload.Price,
		RentalPrice: payload.RentalPrice,
		Color:       payload.Color,
		Size:        payload.Size,
		ImageURL:    payload.ImageURL,
		Stock:       0,
	}

	ctx := r.Context()

	if err := app.Store.Products.Create(ctx, product); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusCreated, product); err != nil {

		app.internalServerError(w, r, err)
	}
}

// @Summary		Update Product
// @Description	Update a product with the provided info
// @Tags			products
// @Accept			json
// @Produce		json
// @Param			id		path	string								true	"Product ID"
// @Param			payload	body	view_models.UpdateProductPayload	true	"Product update payload"
// @Security		ApiKeyAuth
// @Success		200	{object}	models.Product	"Updated product"
// @Failure		400	{object}	error			"Bad request"
// @Failure		500	{object}	error			"Internal server error"
// @Router			/products/{id} [put]
func (app *Application) updateProductHandler(w http.ResponseWriter, r *http.Request) {
	var payload view_models.UpdateProductPayload

	if err := readJson(w, r, &payload); err != nil {
		app.badRequest(w, r, err)
	}

	if err := Validate.Struct(payload); err != nil {
		app.badRequest(w, r, err)
		return
	}

	user := GetUserFromCtx(r)

	product := GetProductFromCtx(r)

	product.Name = payload.Name
	product.Description = payload.Description
	product.Price = payload.Price
	product.RentalPrice = payload.RentalPrice
	product.Color = payload.Color
	product.Size = payload.Size
	product.ImageURL = payload.ImageURL
	product.Stock = payload.Stock

	product.UpdatedBy = &user.UserName

	if err := app.Store.Products.Update(r.Context(), product); err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, product); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Delete Product
// @Description	Delete a product by its ID
// @Tags			products
// @Accept			json
// @Produce		json
// @Security		ApiKeyAuth
// @Param			productId	path		int		true	"Product ID"
// @Success		204			{object}	string	"Product deleted successfully"
// @Failure		400			{object}	error	"Bad request"
// @Failure		404			{object}	error	"Product not found"
// @Failure		500			{object}	error	"Internal server error"
// @Router			/products/{productId} [delete]
func (app *Application) deleteProductHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	product := GetProductFromCtx(r)

	user := GetUserFromCtx(r)

	product.DeletedBy = &user.UserName

	if err := app.Store.Products.Delete(ctx, product); err != nil {
		app.internalServerError(w, r, err)
		return
	}
}

// @Summary		Get all Products
// @Description	Get all products
// @Tags			products
// @Accept			json
// @Produce		json
// @Security		ApiKeyAuth
// @Success		200	{object}	[]models.Product	"List of products"
// @Failure		500	{object}	error				"Internal server error"
// @Router			/products [get]
func (app *Application) getAllProductsHandler(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	products, err := app.Store.Products.GetAll(ctx)

	if err != nil {
		app.internalServerError(w, r, err)
		return
	}

	if err := app.jsonResponse(w, http.StatusOK, products); err != nil {
		app.internalServerError(w, r, err)
	}
}

// @Summary		Get Product
// @Description	Get a product by its ID
// @Tags			products
// @Accept			json
// @Produce		json
// @Security		ApiKeyAuth
// @Header			200			{string}	Authorization
// @Param			productId	path		int				true	"Product ID"
// @Success		200			{object}	models.Product	"Product"
// @Failure		400			{object}	error			"Bad request"
// @Failure		404			{object}	error			"Product not found"
// @Failure		500			{object}	error			"Internal server error"
// @Router			/products/{productId} [get]
func (app *Application) getProductHandler(w http.ResponseWriter, r *http.Request) {
	product := GetProductFromCtx(r)

	if err := app.jsonResponse(w, http.StatusOK, product); err != nil {
		app.internalServerError(w, r, err)
	}
}

func (app *Application) productsContextMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idParam := chi.URLParam(r, "productId")
		idAsInt, err := strconv.ParseInt(idParam, 10, 64)

		if err != nil {
			app.internalServerError(w, r, err)
			return
		}

		ctx := r.Context()

		product, err := app.Store.Products.GetById(ctx, idAsInt)

		if err != nil {
			app.handleError(w, r, err)
			return
		}

		ctx = context.WithValue(ctx, productCtx, product)

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func GetProductFromCtx(r *http.Request) *models.Product {
	product, _ := r.Context().Value(productCtx).(*models.Product)
	return product
}
