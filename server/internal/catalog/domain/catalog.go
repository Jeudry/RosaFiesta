// Package domain define las reglas puras del catálogo, precios por día, disponibilidad y alertas de stock bajo.
package domain

import "errors"

// ErrArticleNotFound indica que el artículo no existe en el catálogo.
var ErrArticleNotFound = errors.New("article not found")

// Article representa un artículo real de alquiler en RosaFiesta (`s18`, `s19`, `A09`, `A23`).
type Article struct {
	ID                string `json:"id" doc:"Clave única del artículo (ej. silla-tiffany)."`
	Name              string `json:"name" doc:"Nombre comercial del artículo."`
	CategoryID        string `json:"categoryId" doc:"Categoría a la que pertenece."`
	PhotoKey          string `json:"photoKey" doc:"Clave de la foto real en frontend/assets/images."`
	DailyPriceDop     int    `json:"dailyPriceDop" doc:"Precio de alquiler por día en RD$."`
	TotalStock        int    `json:"totalStock" doc:"Stock total en almacén."`
	AvailableStock    int    `json:"availableStock" doc:"Stock disponible para la fecha consultada (s63)."`
	LowStockThreshold int    `json:"lowStockThreshold" doc:"Umbral para insignia sol-tint de stock bajo."`
	Description       string `json:"description" doc:"Descripción del artículo."`
}

// IsLowStock indica si debe mostrarse la etiqueta `sol-tint` ("Stock bajo").
func (a Article) IsLowStock() bool {
	return a.AvailableStock > 0 && a.AvailableStock <= a.LowStockThreshold
}

// PackageBundle representa un paquete temático (`s21`, `s22`, `A24`, `A25`).
type PackageBundle struct {
	ID            string `json:"id" doc:"Identificador del paquete."`
	Name          string `json:"name" doc:"Nombre del paquete temático."`
	EventType     string `json:"eventType" doc:"Tipo de evento asociado."`
	PhotoKey      string `json:"photoKey" doc:"Foto real del paquete."`
	TotalPriceDop int    `json:"totalPriceDop" doc:"Precio del paquete en RD$."`
	GuestCapacity int    `json:"guestCapacity" doc:"Capacidad de invitados."`
	ItemsSummary  string `json:"itemsSummary" doc:"Resumen de artículos incluidos."`
}

// Category representa una categoría con su ilustración (`s20`, `Illo`).
type Category struct {
	ID        string `json:"id" doc:"Identificador de la categoría."`
	Name      string `json:"name" doc:"Nombre de la categoría."`
	IlloKey   string `json:"illoKey" doc:"Ilustración vectorial (globos, flores, sillas, dulces)."`
	ItemCount int    `json:"itemCount" doc:"Cantidad de artículos en la categoría."`
}

// CanonicalArticles devuelve los artículos oficiales con los precios por día de la sección 6 del diseño.
func CanonicalArticles() []Article {
	return []Article{
		{
			ID: "silla-tiffany", Name: "Silla Tiffany", CategoryID: "mobiliario", PhotoKey: "silla-tiffany",
			DailyPriceDop: 4, TotalStock: 240, AvailableStock: 120, LowStockThreshold: 20,
			Description: "Silla Tiffany dorada y blanca con cojín incluido.",
		},
		{
			ID: "mesa-plegable", Name: "Mesa plegable", CategoryID: "mobiliario", PhotoKey: "mesa-plegable",
			DailyPriceDop: 20, TotalStock: 40, AvailableStock: 18, LowStockThreshold: 5,
			Description: "Mesa rectangular plegable de 8 puestos para banquete o buffet.",
		},
		{
			ID: "mesa-redonda", Name: "Mesa redonda", CategoryID: "mobiliario", PhotoKey: "mesa-redonda",
			DailyPriceDop: 25, TotalStock: 35, AvailableStock: 12, LowStockThreshold: 5,
			Description: "Mesa redonda para 10 invitados ideal para bodas y quinceañeras.",
		},
		{
			ID: "centro-floral", Name: "Centro floral", CategoryID: "floral", PhotoKey: "centro-floral",
			DailyPriceDop: 35, TotalStock: 50, AvailableStock: 15, LowStockThreshold: 6,
			Description: "Arreglo floral natural en tonos rosa rubor, marfil y eucalipto.",
		},
		{
			ID: "arco-rosa", Name: "Arco de globos", CategoryID: "arcos", PhotoKey: "arco-rosa",
			DailyPriceDop: 95, TotalStock: 10, AvailableStock: 3, LowStockThreshold: 4,
			Description: "Arco orgánico de globos rosa pastel, dorado cromado y follaje.",
		},
		{
			ID: "mesa-helados", Name: "Mesa dulce", CategoryID: "dulces", PhotoKey: "mesa-helados",
			DailyPriceDop: 150, TotalStock: 8, AvailableStock: 2, LowStockThreshold: 3,
			Description: "Estación completa de postres y dulces con bases de cristal.",
		},
	}
}
