// Package httpapi publica las rutas HTTP del catálogo, categorías, paquetes e inventario (`s01`, `s18`–`s23`, `s63`, `A09`, `A23`–`A25`).
package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	catalogapp "github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

// Module conecta el catálogo al enrutador Huma.
type Module struct {
	Service *catalogapp.Service
}

// ArticlesInput filtra artículos por texto (`s80`).
type ArticlesInput struct {
	Query string `query:"q" doc:"Búsqueda opcional por nombre o categoría."`
}

// ArticlesOutput devuelve la lista de artículos con sus precios por día en RD$.
type ArticlesOutput struct {
	Body struct {
		Articles []domain.Article `json:"articles" doc:"Artículos de alquiler con foto y precio por día."`
	}
}

// CategoriesOutput devuelve las categorías del catálogo (`s20`).
type CategoriesOutput struct {
	Body struct {
		Categories []domain.Category `json:"categories" doc:"Categorías con su ilustración."`
	}
}

// PackagesOutput devuelve los paquetes temáticos (`s21`, `A24`).
type PackagesOutput struct {
	Body struct {
		Packages []domain.PackageBundle `json:"packages" doc:"Paquetes preconfigurados para eventos."`
	}
}

// Register registra las rutas públicas y administrativas del catálogo.
func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-articles",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/articles",
		Summary:     "Lista los artículos de alquiler y su disponibilidad (s01, s18, A09, s63)",
		Tags:        []string{"Catalog"},
	}, func(ctx context.Context, in *ArticlesInput) (*ArticlesOutput, error) {
		out := &ArticlesOutput{}
		out.Body.Articles = m.Service.Articles(ctx, in.Query)
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-categories",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/categories",
		Summary:     "Lista las categorías del catálogo (s20)",
		Tags:        []string{"Catalog"},
	}, func(ctx context.Context, _ *struct{}) (*CategoriesOutput, error) {
		out := &CategoriesOutput{}
		out.Body.Categories = m.Service.Categories(ctx)
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "catalog-list-packages",
		Method:      http.MethodGet,
		Path:        "/v1/catalog/packages",
		Summary:     "Lista los paquetes temáticos (s21, s22, A24)",
		Tags:        []string{"Catalog"},
	}, func(ctx context.Context, _ *struct{}) (*PackagesOutput, error) {
		out := &PackagesOutput{}
		out.Body.Packages = m.Service.Packages(ctx)
		return out, nil
	})
}
