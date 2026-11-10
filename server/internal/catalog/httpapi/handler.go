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

	huma.Register(api, huma.Operation{
		OperationID: "catalog-availability",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/availability",
		Summary:     "Compute per-day reserved and available stock for an article and month",
		Tags:        []string{"Catalog"},
	}, m.handleAvailability)
}

func (m Module) registerAdminRoutes(api huma.API) {
	mw := m.inventoryMiddlewares(api)

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

func (m Module) handleListPackages(ctx context.Context, _ *struct{}) (*PackagesOutput, error) {
	out := &PackagesOutput{}
	out.Body.Packages = m.Service.Packages(ctx)
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
