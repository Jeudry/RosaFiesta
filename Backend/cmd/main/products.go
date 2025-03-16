package main

import (
	"Backend/cmd/main/view_models"
	"Backend/internal/store/models"
	"net/http"
)

type productKey string

const productCtx postKey = "product"

//	@Summary		Creates Product
//	@Description	Creates a new product with the provided info
//	@Tags			products
//	@Accept			json
//	@Produce		json
//	@Param			payload	body	view_models.CreateProductPayload	true	"Product creation payload"
//	@Security		ApiKeyAuth
//	@Header			Authorization
//	@Success		201	{object}	models.Product	"Created product"
//	@Failure		400	{object}	error			"Bad request"
//	@Failure		500	{object}	error			"Internal server error"
//	@Router			/products [post]

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
