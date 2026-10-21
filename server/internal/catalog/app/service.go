// Package app implementa los casos de uso del catálogo, paquetes y disponibilidad por fecha (`s18`–`s23`, `s63`, `A09`, `A23`–`A25`).
package app

import (
	"context"
	"strings"
	"sync"

	"github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
)

// Service gestiona el catálogo, las categorías y los paquetes temáticos.
type Service struct {
	mu         sync.RWMutex
	articles   []domain.Article
	categories []domain.Category
	packages   []domain.PackageBundle
}

// NewService construye el servicio de catálogo con los datos canónicos del diseño.
func NewService() *Service {
	return &Service{
		articles: domain.CanonicalArticles(),
		categories: []domain.Category{
			{ID: "mobiliario", Name: "Mobiliario", IlloKey: "sillas", ItemCount: 3},
			{ID: "floral", Name: "Centros florales", IlloKey: "flores", ItemCount: 1},
			{ID: "arcos", Name: "Arcos y globos", IlloKey: "globos", ItemCount: 1},
			{ID: "dulces", Name: "Mesas dulces", IlloKey: "dulces", ItemCount: 1},
		},
		packages: []domain.PackageBundle{
			{
				ID: "pkg-boda-real", Name: "Boda Real Rosa & Dorado", EventType: "Boda", PhotoKey: "boda",
				TotalPriceDop: 4500, GuestCapacity: 120,
				ItemsSummary: "120 sillas Tiffany · 12 mesas redondas · 12 centros florales · 1 arco de globos · 1 mesa dulce",
			},
			{
				ID: "pkg-quince-crystal", Name: "Quinceañera Valentina Crystal", EventType: "Quinceañera", PhotoKey: "quinceanera",
				TotalPriceDop: 6200, GuestCapacity: 150,
				ItemsSummary: "150 sillas Tiffany · 15 mesas redondas · 15 centros florales · 2 arcos de globos · 1 mesa dulce",
			},
			{
				ID: "pkg-cumple-infantil", Name: "Cumpleaños Infantil Mágico", EventType: "Cumpleaños", PhotoKey: "cumple-infantil",
				TotalPriceDop: 405, GuestCapacity: 40,
				ItemsSummary: "40 sillas Tiffany · 2 mesas plegables · 1 arco de globos · 1 mesa dulce",
			},
		},
	}
}

// Articles devuelve los artículos filtrados por búsqueda opcional (`s18`, `s80`, `A09`).
func (s *Service) Articles(_ context.Context, query string) []domain.Article {
	s.mu.RLock()
	defer s.mu.RUnlock()
	q := strings.ToLower(strings.TrimSpace(query))
	var out []domain.Article
	for _, item := range s.articles {
		if q == "" || strings.Contains(strings.ToLower(item.Name), q) || strings.Contains(strings.ToLower(item.CategoryID), q) {
			out = append(out, item)
		}
	}
	return out
}

// Categories devuelve las categorías del catálogo (`s20`).
func (s *Service) Categories(_ context.Context) []domain.Category {
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.Category, len(s.categories))
	copy(out, s.categories)
	return out
}

// Packages devuelve los paquetes temáticos (`s21`, `s22`, `A24`).
func (s *Service) Packages(_ context.Context) []domain.PackageBundle {
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]domain.PackageBundle, len(s.packages))
	copy(out, s.packages)
	return out
}
