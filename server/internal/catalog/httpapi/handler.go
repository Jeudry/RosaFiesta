package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	catalogapp "github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type Module struct {
	Service *catalogapp.Service
	Auth    platformhttp.PermissionAuthorizer
}

func (m Module) Register(api huma.API) {
	m.registerPublicRoutes(api)
	m.registerAdminRoutes(api)
}

func (m Module) registerPublicRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-articles",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/articles",
		Summary:     "List rental catalog articles and live stock",
		Tags:        []string{"Catalog"},
	}, m.handleListArticles)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-balloons",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/balloons",
		Summary:     "List direct-sale balloon items",
		Tags:        []string{"Catalog"},
	}, m.handleListBalloons)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-categories",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/categories",
		Summary:     "List catalog categories",
		Tags:        []string{"Catalog"},
	}, m.handleListCategories)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-packages",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/packages",
		Summary:     "List themed event packages and included items",
		Tags:        []string{"Catalog"},
	}, m.handleListPackages)

	m.registerPublicTrendAndBannerRoutes(api)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-availability",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/availability",
		Summary:     "Compute per-day reserved and available stock for an article and month",
		Tags:        []string{"Catalog"},
	}, m.handleAvailability)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-availability-check",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/availability-check",
		Summary:     "Check date-range turnaround availability for an article and quantity",
		Tags:        []string{"Catalog"},
	}, m.handleAvailabilityCheck)
}

func (m Module) registerPublicTrendAndBannerRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-trends",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/trends",
		Summary:     "List catalog seasonal trends",
		Tags:        []string{"Catalog"},
	}, m.handleListTrends)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-get-season-banner",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/season-banner",
		Summary:     "Get Personal Home seasonal hero banner",
		Tags:        []string{"Catalog"},
	}, m.handleGetSeasonBanner)
}

func (m Module) registerAdminRoutes(api huma.API) {
	mw := m.inventoryMiddlewares(api)
	m.registerArticleAndPackageAdminRoutes(api, mw)
	m.registerCategoryAdminRoutes(api, mw)
	m.registerTrendAndBannerAdminRoutes(api, mw)
}

func (m Module) registerArticleAndPackageAdminRoutes(api huma.API, mw huma.Middlewares) {
	huma.Register(api, huma.Operation{
		OperationID: "catalog-create-article",
		Method:      http.MethodPost,
		Path:        "/v1/catalog/articles",
		Summary:     "Create a new catalog article",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCreateArticle)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-update-article",
		Method:      http.MethodPatch,
		Path:        "/v1/catalog/articles/{id}",
		Summary:     "Update an existing catalog article",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdateArticle)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-delete-article",
		Method:      http.MethodDelete,
		Path:        "/v1/catalog/articles/{id}",
		Summary:     "Delete an unused catalog article",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeleteArticle)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-create-package",
		Method:      http.MethodPost,
		Path:        "/v1/catalog/packages",
		Summary:     "Create a new themed event package",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCreatePackage)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-update-package",
		Method:      http.MethodPut,
		Path:        "/v1/catalog/packages/{id}",
		Summary:     "Update an existing themed event package",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdatePackage)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-delete-package",
		Method:      http.MethodDelete,
		Path:        "/v1/catalog/packages/{id}",
		Summary:     "Delete a themed event package",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeletePackage)
}

func (m Module) registerCategoryAdminRoutes(api huma.API, mw huma.Middlewares) {
	huma.Register(api, huma.Operation{
		OperationID: "catalog-create-category",
		Method:      http.MethodPost,
		Path:        "/v1/catalog/categories",
		Summary:     "Create a new catalog category",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCreateCategory)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-update-category",
		Method:      http.MethodPatch,
		Path:        "/v1/catalog/categories/{id}",
		Summary:     "Update an existing catalog category",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdateCategory)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-delete-category",
		Method:      http.MethodDelete,
		Path:        "/v1/catalog/categories/{id}",
		Summary:     "Delete an unused catalog category",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeleteCategory)
}

func (m Module) registerTrendAndBannerAdminRoutes(api huma.API, mw huma.Middlewares) {
	huma.Register(api, huma.Operation{
		OperationID: "catalog-create-trend",
		Method:      http.MethodPost,
		Path:        "/v1/catalog/trends",
		Summary:     "Create a new catalog trend card",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCreateTrend)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-update-trend",
		Method:      http.MethodPut,
		Path:        "/v1/catalog/trends/{id}",
		Summary:     "Update an existing catalog trend card",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleUpdateTrend)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-delete-trend",
		Method:      http.MethodDelete,
		Path:        "/v1/catalog/trends/{id}",
		Summary:     "Delete a catalog trend card",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeleteTrend)

	huma.Register(api, huma.Operation{
		OperationID: "catalog-save-season-banner",
		Method:      http.MethodPut,
		Path:        "/v1/catalog/season-banner",
		Summary:     "Update the Personal Home seasonal hero banner",
		Tags:        []string{"Catalog"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleSaveSeasonBanner)
}

func (m Module) inventoryMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequirePermission(api, m.Auth, "inventario")}
}

func (m Module) handleListArticles(ctx context.Context, in *ArticlesInput) (*ArticlesOutput, error) {
	filter := in.ToFilter()
	out := &ArticlesOutput{}
	out.Body.Articles = m.Service.ArticlesFiltered(ctx, filter)
	return out, nil
}

func (m Module) handleListBalloons(ctx context.Context, _ *struct{}) (*ArticlesOutput, error) {
	out := &ArticlesOutput{}
	out.Body.Articles = m.Service.Balloons(ctx)
	return out, nil
}

func (m Module) handleListCategories(ctx context.Context, _ *struct{}) (*CategoriesOutput, error) {
	out := &CategoriesOutput{}
	out.Body.Categories = m.Service.Categories(ctx)
	return out, nil
}

func (m Module) handleListPackages(ctx context.Context, in *PackagesInput) (*PackagesOutput, error) {
	out := &PackagesOutput{}
	out.Body.Packages = m.Service.PackagesFiltered(ctx, in.IncludeHidden)
	return out, nil
}

func (m Module) handleAvailability(ctx context.Context, in *AvailabilityInput) (*AvailabilityOutput, error) {
	avail, err := m.Service.Availability(ctx, in.ArticleID, in.Month)
	if err != nil {
		return nil, mapCatalogError(err, "GET /v1/catalog/availability")
	}
	out := &AvailabilityOutput{}
	out.Body.Availability = avail
	return out, nil
}

func (m Module) handleAvailabilityCheck(ctx context.Context, in *AvailabilityCheckInput) (*AvailabilityCheckOutput, error) {
	res, err := m.Service.CheckAvailabilityForDate(ctx, in.Date, in.ExcludeEventID, in.ToRequirements())
	if err != nil {
		return nil, mapCatalogError(err, "GET /v1/catalog/availability-check")
	}
	return &AvailabilityCheckOutput{Body: res}, nil
}

func (m Module) handleCreateArticle(ctx context.Context, in *CreateArticleInput) (*ArticleOutput, error) {
	cmd := in.ToCommand()
	created, err := m.Service.CreateArticle(ctx, cmd)
	if err != nil {
		return nil, mapCatalogError(err, "POST /v1/catalog/articles")
	}
	out := &ArticleOutput{}
	out.Body.Article = created
	return out, nil
}

func (m Module) handleUpdateArticle(ctx context.Context, in *UpdateArticleInput) (*ArticleOutput, error) {
	cmd := in.ToCommand()
	updated, err := m.Service.UpdateArticle(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapCatalogError(err, "PATCH /v1/catalog/articles/"+in.ID)
	}
	out := &ArticleOutput{}
	out.Body.Article = updated
	return out, nil
}

func (m Module) handleDeleteArticle(ctx context.Context, in *DeleteArticleInput) (*DeleteArticleOutput, error) {
	if err := m.Service.DeleteArticle(ctx, in.ID); err != nil {
		return nil, mapCatalogError(err, "DELETE /v1/catalog/articles/"+in.ID)
	}
	out := &DeleteArticleOutput{}
	out.Body.DeletedID = in.ID
	return out, nil
}

func (m Module) handleCreatePackage(ctx context.Context, in *SavePackageInput) (*PackageOutput, error) {
	cmd := in.ToCommand()
	created, err := m.Service.CreatePackage(ctx, cmd)
	if err != nil {
		return nil, mapCatalogError(err, "POST /v1/catalog/packages")
	}
	out := &PackageOutput{}
	out.Body.Package = created
	return out, nil
}

func (m Module) handleUpdatePackage(ctx context.Context, in *UpdatePackageInput) (*PackageOutput, error) {
	cmd := in.ToCommand()
	updated, err := m.Service.UpdatePackage(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapCatalogError(err, "PUT /v1/catalog/packages/"+in.ID)
	}
	out := &PackageOutput{}
	out.Body.Package = updated
	return out, nil
}

func (m Module) handleDeletePackage(ctx context.Context, in *DeletePackageInput) (*DeletePackageOutput, error) {
	if err := m.Service.DeletePackage(ctx, in.ID); err != nil {
		return nil, mapCatalogError(err, "DELETE /v1/catalog/packages/"+in.ID)
	}
	out := &DeletePackageOutput{}
	out.Body.DeletedID = in.ID
	return out, nil
}

func (m Module) handleCreateCategory(ctx context.Context, in *SaveCategoryInput) (*CategoryOutput, error) {
	cmd := in.ToCommand()
	created, err := m.Service.CreateCategory(ctx, cmd)
	if err != nil {
		return nil, mapCatalogError(err, "POST /v1/catalog/categories")
	}
	out := &CategoryOutput{}
	out.Body.Category = created
	return out, nil
}

func (m Module) handleUpdateCategory(ctx context.Context, in *UpdateCategoryInput) (*CategoryOutput, error) {
	cmd := in.ToCommand()
	updated, err := m.Service.UpdateCategory(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapCatalogError(err, "PATCH /v1/catalog/categories/"+in.ID)
	}
	out := &CategoryOutput{}
	out.Body.Category = updated
	return out, nil
}

func (m Module) handleDeleteCategory(ctx context.Context, in *DeleteCategoryInput) (*DeleteCategoryOutput, error) {
	if err := m.Service.DeleteCategory(ctx, in.ID); err != nil {
		return nil, mapCatalogError(err, "DELETE /v1/catalog/categories/"+in.ID)
	}
	out := &DeleteCategoryOutput{}
	out.Body.DeletedID = in.ID
	return out, nil
}

func (m Module) handleListTrends(ctx context.Context, in *TrendsInput) (*TrendsOutput, error) {
	out := &TrendsOutput{}
	out.Body.Trends = m.Service.Trends(ctx, in.IncludeInactive)
	return out, nil
}

func (m Module) handleCreateTrend(ctx context.Context, in *SaveTrendInput) (*TrendOutput, error) {
	created, err := m.Service.CreateTrend(ctx, in.ToCommand())
	if err != nil {
		return nil, mapCatalogError(err, "POST /v1/catalog/trends")
	}
	out := &TrendOutput{}
	out.Body.Trend = created
	return out, nil
}

func (m Module) handleUpdateTrend(ctx context.Context, in *UpdateTrendInput) (*TrendOutput, error) {
	updated, err := m.Service.UpdateTrend(ctx, in.ID, in.ToCommand())
	if err != nil {
		return nil, mapCatalogError(err, "PUT /v1/catalog/trends/"+in.ID)
	}
	out := &TrendOutput{}
	out.Body.Trend = updated
	return out, nil
}

func (m Module) handleDeleteTrend(ctx context.Context, in *DeleteTrendInput) (*DeleteTrendOutput, error) {
	if err := m.Service.DeleteTrend(ctx, in.ID); err != nil {
		return nil, mapCatalogError(err, "DELETE /v1/catalog/trends/"+in.ID)
	}
	out := &DeleteTrendOutput{}
	out.Body.DeletedID = in.ID
	return out, nil
}

func (m Module) handleGetSeasonBanner(ctx context.Context, _ *struct{}) (*SeasonBannerOutput, error) {
	out := &SeasonBannerOutput{}
	out.Body.Banner = m.Service.SeasonBanner(ctx)
	return out, nil
}

func (m Module) handleSaveSeasonBanner(ctx context.Context, in *SaveSeasonBannerInput) (*SeasonBannerOutput, error) {
	updated, err := m.Service.UpdateSeasonBanner(ctx, in.ToCommand())
	if err != nil {
		return nil, mapCatalogError(err, "PUT /v1/catalog/season-banner")
	}
	out := &SeasonBannerOutput{}
	out.Body.Banner = updated
	return out, nil
}
