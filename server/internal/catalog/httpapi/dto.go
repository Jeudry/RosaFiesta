package httpapi

import (
	"errors"
	"net/http"

	catalogapp "github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type ArticlesInput struct {
	Query         string `query:"q" doc:"Optional search filter by name or category."`
	Kind          string `query:"kind" doc:"Catalog kind filter: rental, balloon, or all."`
	IncludeHidden bool   `query:"includeHidden" doc:"Include hidden inventory items."`
}

func (in *ArticlesInput) ToFilter() catalogapp.ArticleFilter {
	return catalogapp.ArticleFilter{
		Query:         in.Query,
		Kind:          in.Kind,
		IncludeHidden: in.IncludeHidden,
	}
}

type ArticlesOutput struct {
	Body struct {
		Articles []domain.Article `json:"articles" doc:"Catalog articles with daily prices and stock."`
	}
}

type ArticleOutput struct {
	Body struct {
		Article domain.Article `json:"article" doc:"Catalog article record."`
	}
}

type CreateArticleInput struct {
	Body struct {
		ID                string   `json:"id,omitempty" doc:"Optional article slug identifier."`
		Name              string   `json:"name" doc:"Commercial article name."`
		CategoryID        string   `json:"categoryId,omitempty" doc:"Category identifier."`
		PhotoKey          string   `json:"photoKey,omitempty" doc:"Photo asset key."`
		DailyPriceDop     int      `json:"dailyPriceDop" doc:"Daily rental or sale price in DOP."`
		TotalStock        int      `json:"totalStock" doc:"Total warehouse stock."`
		AvailableStock    *int     `json:"availableStock,omitempty" doc:"Initial available stock."`
		LowStockThreshold int      `json:"lowStockThreshold,omitempty" doc:"Low stock warning threshold."`
		Description       string   `json:"description,omitempty" doc:"Article description."`
		Visible           *bool    `json:"visible,omitempty" doc:"Catalog visibility flag."`
		Dimensions        string   `json:"dimensions,omitempty" doc:"Physical dimensions."`
		Material          string   `json:"material,omitempty" doc:"Primary material."`
		ColorVariants     []string `json:"colorVariants,omitempty" doc:"Color variants."`
		IsDirectSale      bool     `json:"isDirectSale,omitempty" doc:"True for direct-sale balloon items."`
	}
}

func (in *CreateArticleInput) ToCommand() catalogapp.CreateArticleCommand {
	return catalogapp.CreateArticleCommand{
		ID:                in.Body.ID,
		Name:              in.Body.Name,
		CategoryID:        in.Body.CategoryID,
		PhotoKey:          in.Body.PhotoKey,
		DailyPriceDop:     in.Body.DailyPriceDop,
		TotalStock:        in.Body.TotalStock,
		AvailableStock:    in.Body.AvailableStock,
		LowStockThreshold: in.Body.LowStockThreshold,
		Description:       in.Body.Description,
		Visible:           in.Body.Visible,
		Dimensions:        in.Body.Dimensions,
		Material:          in.Body.Material,
		ColorVariants:     in.Body.ColorVariants,
		IsDirectSale:      in.Body.IsDirectSale,
	}
}

type UpdateArticleInput struct {
	ID   string `path:"id" doc:"Article identifier."`
	Body struct {
		Name              *string   `json:"name,omitempty" doc:"Updated article name."`
		CategoryID        *string   `json:"categoryId,omitempty" doc:"Updated category identifier."`
		PhotoKey          *string   `json:"photoKey,omitempty" doc:"Updated photo key."`
		DailyPriceDop     *int      `json:"dailyPriceDop,omitempty" doc:"Updated daily price in DOP."`
		TotalStock        *int      `json:"totalStock,omitempty" doc:"Updated total stock."`
		AvailableStock    *int      `json:"availableStock,omitempty" doc:"Updated available stock."`
		LowStockThreshold *int      `json:"lowStockThreshold,omitempty" doc:"Updated low stock threshold."`
		Description       *string   `json:"description,omitempty" doc:"Updated description."`
		Visible           *bool     `json:"visible,omitempty" doc:"Updated visibility flag."`
		Dimensions        *string   `json:"dimensions,omitempty" doc:"Updated dimensions."`
		Material          *string   `json:"material,omitempty" doc:"Updated material."`
		ColorVariants     *[]string `json:"colorVariants,omitempty" doc:"Updated color variants."`
	}
}

func (in *UpdateArticleInput) ToCommand() catalogapp.UpdateArticleCommand {
	return catalogapp.UpdateArticleCommand{
		Name:              in.Body.Name,
		CategoryID:        in.Body.CategoryID,
		PhotoKey:          in.Body.PhotoKey,
		DailyPriceDop:     in.Body.DailyPriceDop,
		TotalStock:        in.Body.TotalStock,
		AvailableStock:    in.Body.AvailableStock,
		LowStockThreshold: in.Body.LowStockThreshold,
		Description:       in.Body.Description,
		Visible:           in.Body.Visible,
		Dimensions:        in.Body.Dimensions,
		Material:          in.Body.Material,
		ColorVariants:     in.Body.ColorVariants,
	}
}

type CategoriesOutput struct {
	Body struct {
		Categories []domain.Category `json:"categories" doc:"Catalog categories."`
	}
}

type PackagesOutput struct {
	Body struct {
		Packages []domain.PackageBundle `json:"packages" doc:"Catalog event packages."`
	}
}

type PackageOutput struct {
	Body struct {
		Package domain.PackageBundle `json:"package" doc:"Event package bundle."`
	}
}

type SavePackageInput struct {
	Body struct {
		ID            string               `json:"id,omitempty" doc:"Optional package identifier."`
		Name          string               `json:"name" doc:"Package name."`
		EventType     string               `json:"eventType,omitempty" doc:"Target event type."`
		PhotoKey      string               `json:"photoKey,omitempty" doc:"Photo key."`
		TotalPriceDop int                  `json:"totalPriceDop" doc:"Total package price in DOP."`
		GuestCapacity int                  `json:"guestCapacity" doc:"Guest capacity."`
		ItemsSummary  string               `json:"itemsSummary,omitempty" doc:"Items summary text."`
		Visible       *bool                `json:"visible,omitempty" doc:"Package visibility flag."`
		Items         []domain.PackageItem `json:"items,omitempty" doc:"Included items list."`
	}
}

func (in *SavePackageInput) ToCommand() catalogapp.SavePackageCommand {
	return catalogapp.SavePackageCommand{
		ID:            in.Body.ID,
		Name:          in.Body.Name,
		EventType:     in.Body.EventType,
		PhotoKey:      in.Body.PhotoKey,
		TotalPriceDop: in.Body.TotalPriceDop,
		GuestCapacity: in.Body.GuestCapacity,
		ItemsSummary:  in.Body.ItemsSummary,
		Visible:       in.Body.Visible,
		Items:         in.Body.Items,
	}
}

type UpdatePackageInput struct {
	ID   string `path:"id" doc:"Package identifier."`
	Body struct {
		Name          string               `json:"name" doc:"Package name."`
		EventType     string               `json:"eventType,omitempty" doc:"Target event type."`
		PhotoKey      string               `json:"photoKey,omitempty" doc:"Photo key."`
		TotalPriceDop int                  `json:"totalPriceDop" doc:"Total package price in DOP."`
		GuestCapacity int                  `json:"guestCapacity" doc:"Guest capacity."`
		ItemsSummary  string               `json:"itemsSummary,omitempty" doc:"Items summary text."`
		Visible       *bool                `json:"visible,omitempty" doc:"Package visibility flag."`
		Items         []domain.PackageItem `json:"items,omitempty" doc:"Included items list."`
	}
}

func (in *UpdatePackageInput) ToCommand() catalogapp.SavePackageCommand {
	return catalogapp.SavePackageCommand{
		ID:            in.ID,
		Name:          in.Body.Name,
		EventType:     in.Body.EventType,
		PhotoKey:      in.Body.PhotoKey,
		TotalPriceDop: in.Body.TotalPriceDop,
		GuestCapacity: in.Body.GuestCapacity,
		ItemsSummary:  in.Body.ItemsSummary,
		Visible:       in.Body.Visible,
		Items:         in.Body.Items,
	}
}

type AvailabilityInput struct {
	ArticleID string `query:"articleId" doc:"Article identifier to inspect."`
	Month     string `query:"month" doc:"Calendar month in YYYY-MM format."`
}

type AvailabilityOutput struct {
	Body struct {
		Availability domain.ArticleAvailability `json:"availability" doc:"Per-day reserved and available stock."`
	}
}

func mapCatalogError(err error, endpoint string) *platformhttp.APIError {
	if errors.Is(err, domain.ErrArticleNotFound) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			"CATALOG_ARTICLE_NOT_FOUND",
			err.Error(),
			endpoint,
			"LookupCatalogArticle",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrPackageNotFound) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			"CATALOG_PACKAGE_NOT_FOUND",
			err.Error(),
			endpoint,
			"LookupCatalogPackage",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"CATALOG_INVALID_INPUT",
		err.Error(),
		endpoint,
		"ValidateCatalogInput",
		err.Error(),
	)
}
