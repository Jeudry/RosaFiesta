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
		PhotoUrl          string   `json:"photoUrl,omitempty" doc:"Uploaded media URL."`
		DailyPriceDop     int      `json:"dailyPriceDop" doc:"Daily rental or sale price in DOP."`
		TotalStock        int      `json:"totalStock" doc:"Total warehouse stock."`
		DamagedStock      int      `json:"damagedStock,omitempty" doc:"Units quarantined due to damage."`
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
		PhotoURL:          in.Body.PhotoUrl,
		DailyPriceDop:     in.Body.DailyPriceDop,
		TotalStock:        in.Body.TotalStock,
		DamagedStock:      in.Body.DamagedStock,
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
		PhotoUrl          *string   `json:"photoUrl,omitempty" doc:"Updated uploaded media URL."`
		DailyPriceDop     *int      `json:"dailyPriceDop,omitempty" doc:"Updated daily price in DOP."`
		TotalStock        *int      `json:"totalStock,omitempty" doc:"Updated total stock."`
		DamagedStock      *int      `json:"damagedStock,omitempty" doc:"Updated damaged stock count."`
		AvailableStock    *int      `json:"availableStock,omitempty" doc:"Updated available stock."`
		LowStockThreshold *int      `json:"lowStockThreshold,omitempty" doc:"Updated low stock threshold."`
		Description       *string   `json:"description,omitempty" doc:"Updated description."`
		Visible           *bool     `json:"visible,omitempty" doc:"Updated visibility flag."`
		Dimensions        *string   `json:"dimensions,omitempty" doc:"Updated dimensions."`
		Material          *string   `json:"material,omitempty" doc:"Updated material."`
		ColorVariants     *[]string `json:"colorVariants,omitempty" doc:"Updated color variants."`
	}
}

type DeleteArticleInput struct {
	ID string `path:"id" doc:"Article identifier to delete."`
}

type DeleteArticleOutput struct {
	Body struct {
		DeletedID string `json:"deletedId" doc:"Deleted article identifier."`
	}
}

func (in *UpdateArticleInput) ToCommand() catalogapp.UpdateArticleCommand {
	return catalogapp.UpdateArticleCommand{
		Name:              in.Body.Name,
		CategoryID:        in.Body.CategoryID,
		PhotoKey:          in.Body.PhotoKey,
		PhotoURL:          in.Body.PhotoUrl,
		DailyPriceDop:     in.Body.DailyPriceDop,
		TotalStock:        in.Body.TotalStock,
		DamagedStock:      in.Body.DamagedStock,
		AvailableStock:    in.Body.AvailableStock,
		LowStockThreshold: in.Body.LowStockThreshold,
		Description:       in.Body.Description,
		Visible:           in.Body.Visible,
		Dimensions:        in.Body.Dimensions,
		Material:          in.Body.Material,
		ColorVariants:     in.Body.ColorVariants,
	}
}

type PackagesInput struct {
	IncludeHidden bool `query:"includeHidden" doc:"Include hidden event packages."`
}

type DeletePackageInput struct {
	ID string `path:"id" doc:"Package identifier to delete."`
}

type DeletePackageOutput struct {
	Body struct {
		DeletedID string `json:"deletedId"`
	}
}

type CategoriesOutput struct {
	Body struct {
		Categories []domain.Category `json:"categories" doc:"Catalog categories."`
	}
}

type CategoryOutput struct {
	Body struct {
		Category domain.Category `json:"category" doc:"Catalog category record."`
	}
}

type SaveCategoryInput struct {
	Body struct {
		ID      string `json:"id,omitempty" doc:"Optional category slug identifier."`
		Name    string `json:"name" doc:"Category display name."`
		IlloKey string `json:"illoKey,omitempty" doc:"Illustration or icon key."`
	}
}

func (in *SaveCategoryInput) ToCommand() catalogapp.SaveCategoryCommand {
	return catalogapp.SaveCategoryCommand{
		ID:      in.Body.ID,
		Name:    in.Body.Name,
		IlloKey: in.Body.IlloKey,
	}
}

type UpdateCategoryInput struct {
	ID   string `path:"id" doc:"Category identifier."`
	Body struct {
		Name    string `json:"name" doc:"Updated category display name."`
		IlloKey string `json:"illoKey,omitempty" doc:"Updated illustration or icon key."`
	}
}

func (in *UpdateCategoryInput) ToCommand() catalogapp.SaveCategoryCommand {
	return catalogapp.SaveCategoryCommand{
		ID:      in.ID,
		Name:    in.Body.Name,
		IlloKey: in.Body.IlloKey,
	}
}

type DeleteCategoryInput struct {
	ID string `path:"id" doc:"Category identifier to delete."`
}

type DeleteCategoryOutput struct {
	Body struct {
		DeletedID string `json:"deletedId" doc:"Deleted category identifier."`
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
		PhotoUrl      string               `json:"photoUrl,omitempty" doc:"Uploaded media URL."`
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
		PhotoURL:      in.Body.PhotoUrl,
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
		PhotoUrl      string               `json:"photoUrl,omitempty" doc:"Uploaded media URL."`
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
		PhotoURL:      in.Body.PhotoUrl,
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

type AvailabilityCheckInput struct {
	Date           string `query:"date" doc:"Target event date in YYYY-MM-DD or readable format."`
	ArticleID      string `query:"articleId" doc:"Article identifier or name to validate."`
	Quantity       int    `query:"quantity" doc:"Requested quantity."`
	ExcludeEventID string `query:"excludeEventId" doc:"Optional event identifier excluded during edits."`
}

func (in *AvailabilityCheckInput) ToRequirements() []domain.StockRequirement {
	qty := in.Quantity
	if qty <= 0 {
		qty = 1
	}
	return []domain.StockRequirement{{ArticleRef: in.ArticleID, Quantity: qty}}
}

type AvailabilityCheckOutput struct {
	Body domain.AvailabilityCheckResult
}

type TrendsInput struct {
	IncludeInactive bool `query:"includeInactive" doc:"Include hidden trends."`
}

type TrendsOutput struct {
	Body struct {
		Trends []domain.Trend `json:"trends" doc:"Catalog trends."`
	}
}

type TrendOutput struct {
	Body struct {
		Trend domain.Trend `json:"trend" doc:"Catalog trend record."`
	}
}

type SaveTrendInput struct {
	Body struct {
		ID              string `json:"id,omitempty" doc:"Optional trend slug identifier."`
		Title           string `json:"title" doc:"Trend display title."`
		EventsLabel     string `json:"eventsLabel,omitempty" doc:"Secondary label or event count badge."`
		PhotoKey        string `json:"photoKey,omitempty" doc:"Fallback catalog photo key."`
		PhotoUrl        string `json:"photoUrl,omitempty" doc:"Optional uploaded media URL."`
		PhotoBgHex      string `json:"photoBgHex,omitempty" doc:"Background tint hex color."`
		LinkedCategory  string `json:"linkedCategory,omitempty" doc:"Optional category filter on tap."`
		LinkedPackageID string `json:"linkedPackageId,omitempty" doc:"Optional package identifier on tap."`
		SortOrder       int    `json:"sortOrder,omitempty" doc:"Display sort priority."`
		Active          *bool  `json:"active,omitempty" doc:"Visibility toggle."`
	}
}

func (in *SaveTrendInput) ToCommand() catalogapp.SaveTrendCommand {
	return catalogapp.SaveTrendCommand{
		ID:              in.Body.ID,
		Title:           in.Body.Title,
		EventsLabel:     in.Body.EventsLabel,
		PhotoKey:        in.Body.PhotoKey,
		PhotoURL:        in.Body.PhotoUrl,
		PhotoBgHex:      in.Body.PhotoBgHex,
		LinkedCategory:  in.Body.LinkedCategory,
		LinkedPackageID: in.Body.LinkedPackageID,
		SortOrder:       in.Body.SortOrder,
		Active:          in.Body.Active,
	}
}

type UpdateTrendInput struct {
	ID   string `path:"id" doc:"Trend identifier."`
	Body struct {
		Title           string `json:"title" doc:"Trend display title."`
		EventsLabel     string `json:"eventsLabel,omitempty" doc:"Secondary label or event count badge."`
		PhotoKey        string `json:"photoKey,omitempty" doc:"Fallback catalog photo key."`
		PhotoUrl        string `json:"photoUrl,omitempty" doc:"Optional uploaded media URL."`
		PhotoBgHex      string `json:"photoBgHex,omitempty" doc:"Background tint hex color."`
		LinkedCategory  string `json:"linkedCategory,omitempty" doc:"Optional category filter on tap."`
		LinkedPackageID string `json:"linkedPackageId,omitempty" doc:"Optional package identifier on tap."`
		SortOrder       int    `json:"sortOrder,omitempty" doc:"Display sort priority."`
		Active          *bool  `json:"active,omitempty" doc:"Visibility toggle."`
	}
}

func (in *UpdateTrendInput) ToCommand() catalogapp.SaveTrendCommand {
	return catalogapp.SaveTrendCommand{
		ID:              in.ID,
		Title:           in.Body.Title,
		EventsLabel:     in.Body.EventsLabel,
		PhotoKey:        in.Body.PhotoKey,
		PhotoURL:        in.Body.PhotoUrl,
		PhotoBgHex:      in.Body.PhotoBgHex,
		LinkedCategory:  in.Body.LinkedCategory,
		LinkedPackageID: in.Body.LinkedPackageID,
		SortOrder:       in.Body.SortOrder,
		Active:          in.Body.Active,
	}
}

type DeleteTrendInput struct {
	ID string `path:"id" doc:"Trend identifier to delete."`
}

type DeleteTrendOutput struct {
	Body struct {
		DeletedID string `json:"deletedId" doc:"Deleted trend identifier."`
	}
}

type SeasonBannerOutput struct {
	Body struct {
		Banner domain.SeasonBanner `json:"banner" doc:"Seasonal hero banner configuration."`
	}
}

type SaveSeasonBannerInput struct {
	Body struct {
		BadgeLabel       string `json:"badgeLabel,omitempty" doc:"Upper seasonal badge label."`
		Title            string `json:"title" doc:"Main hero headline."`
		CtaLabel         string `json:"ctaLabel,omitempty" doc:"Action button label."`
		TargetScreenCode string `json:"targetScreenCode,omitempty" doc:"Target screen code."`
		Active           bool   `json:"active" doc:"Whether banner is published in Home."`
	}
}

func (in *SaveSeasonBannerInput) ToCommand() catalogapp.UpdateSeasonBannerCommand {
	return catalogapp.UpdateSeasonBannerCommand{
		BadgeLabel:       in.Body.BadgeLabel,
		Title:            in.Body.Title,
		CtaLabel:         in.Body.CtaLabel,
		TargetScreenCode: in.Body.TargetScreenCode,
		Active:           in.Body.Active,
	}
}

func mapCatalogError(err error, endpoint string) *platformhttp.APIError {
	if mapped := mapCategoryCatalogError(err, endpoint); mapped != nil {
		return mapped
	}
	if mapped := mapTrendAndBannerError(err, endpoint); mapped != nil {
		return mapped
	}
	if errors.Is(err, domain.ErrInsufficientStockForDate) {
		return platformhttp.NewAPIError(http.StatusConflict, "STOCK_INSUFFICIENT_FOR_DATE", err.Error(), endpoint, "CheckRangeStockAvailability", err.Error())
	}
	if errors.Is(err, domain.ErrArticleNotFound) {
		return platformhttp.NewAPIError(http.StatusNotFound, "CATALOG_ARTICLE_NOT_FOUND", err.Error(), endpoint, "LookupCatalogArticle", err.Error())
	}
	if errors.Is(err, domain.ErrArticleInUse) {
		return platformhttp.NewAPIError(http.StatusConflict, "CATALOG_ARTICLE_IN_USE", err.Error(), endpoint, "CheckArticleReservations", err.Error())
	}
	if errors.Is(err, domain.ErrPackageNotFound) {
		return platformhttp.NewAPIError(http.StatusNotFound, "CATALOG_PACKAGE_NOT_FOUND", err.Error(), endpoint, "LookupCatalogPackage", err.Error())
	}
	return platformhttp.NewAPIError(http.StatusBadRequest, "CATALOG_INVALID_INPUT", err.Error(), endpoint, "ValidateCatalogInput", err.Error())
}

func mapTrendAndBannerError(err error, endpoint string) *platformhttp.APIError {
	switch {
	case errors.Is(err, domain.ErrTrendNotFound):
		return platformhttp.NewAPIError(http.StatusNotFound, "CATALOG_TREND_NOT_FOUND", err.Error(), endpoint, "LookupCatalogTrend", err.Error())
	case errors.Is(err, domain.ErrTrendAlreadyExists):
		return platformhttp.NewAPIError(http.StatusConflict, "CATALOG_TREND_EXISTS", err.Error(), endpoint, "EnsureUniqueTrend", err.Error())
	case errors.Is(err, domain.ErrInvalidSeasonBanner):
		return platformhttp.NewAPIError(http.StatusBadRequest, "CATALOG_SEASON_BANNER_INVALID", err.Error(), endpoint, "ValidateSeasonBanner", err.Error())
	default:
		return nil
	}
}

func mapCategoryCatalogError(err error, endpoint string) *platformhttp.APIError {
	switch {
	case errors.Is(err, domain.ErrCategoryNotFound):
		return platformhttp.NewAPIError(http.StatusNotFound, "CATALOG_CATEGORY_NOT_FOUND", err.Error(), endpoint, "LookupCatalogCategory", err.Error())
	case errors.Is(err, domain.ErrCategoryInUse):
		return platformhttp.NewAPIError(http.StatusConflict, "CATALOG_CATEGORY_IN_USE", err.Error(), endpoint, "CheckCategoryUsage", err.Error())
	case errors.Is(err, domain.ErrCategoryAlreadyExists):
		return platformhttp.NewAPIError(http.StatusConflict, "CATALOG_CATEGORY_EXISTS", err.Error(), endpoint, "EnsureUniqueCategory", err.Error())
	default:
		return nil
	}
}
