package domain

import (
	"errors"
	"strings"
)

var (
	ErrArticleNotFound          = errors.New("article not found")
	ErrArticleInUse             = errors.New("no puedes eliminar este artículo porque tiene reservas activas en eventos")
	ErrPackageNotFound          = errors.New("package not found")
	ErrCategoryNotFound         = errors.New("category not found")
	ErrCategoryInUse            = errors.New("no puedes eliminar esta categoría porque tiene artículos asociados")
	ErrCategoryAlreadyExists    = errors.New("ya existe una categoría con ese nombre")
	ErrTrendNotFound            = errors.New("trend not found")
	ErrTrendAlreadyExists       = errors.New("trend with the same title already exists")
	ErrInvalidSeasonBanner      = errors.New("invalid season banner input")
	ErrInvalidCatalog           = errors.New("invalid catalog input")
	ErrInsufficientStockForDate = errors.New("stock insuficiente para la fecha solicitada")
	ErrInvalidDateRange         = errors.New("rango de fechas inválido")
)

type Article struct {
	ID                string   `json:"id" doc:"Unique article identifier."`
	Name              string   `json:"name" doc:"Commercial article name."`
	CategoryID        string   `json:"categoryId" doc:"Category identifier."`
	PhotoKey          string   `json:"photoKey" doc:"Asset image key."`
	PhotoURL          string   `json:"photoUrl,omitempty" doc:"Uploaded media URL."`
	DailyPriceDop     int      `json:"dailyPriceDop" doc:"Daily rental or unit price in DOP."`
	TotalStock        int      `json:"totalStock" doc:"Total warehouse stock."`
	DamagedStock      int      `json:"damagedStock,omitempty" doc:"Damaged or quarantined stock units."`
	AvailableStock    int      `json:"availableStock" doc:"Currently available stock."`
	LowStockThreshold int      `json:"lowStockThreshold" doc:"Low stock warning threshold."`
	Description       string   `json:"description" doc:"Article description."`
	Visible           bool     `json:"visible" doc:"Whether the article is visible in the public catalog."`
	Dimensions        string   `json:"dimensions,omitempty" doc:"Physical dimensions specification."`
	Material          string   `json:"material,omitempty" doc:"Primary material specification."`
	ColorVariants     []string `json:"colorVariants,omitempty" doc:"Available color variants."`
	IsDirectSale      bool     `json:"isDirectSale,omitempty" doc:"True for direct-sale balloon items."`
}

// UsableStock retorna el stock físico utilizable descontando unidades dañadas.
func (a Article) UsableStock() int {
	usable := a.TotalStock - a.DamagedStock
	if usable < 0 {
		return 0
	}
	return usable
}

func (a Article) IsLowStock() bool {
	if a.AvailableStock <= 0 {
		return false
	}
	return a.AvailableStock <= a.LowStockThreshold
}

func (a Article) MatchesQuery(lowerQuery string) bool {
	if lowerQuery == "" {
		return true
	}
	lowerName := strings.ToLower(a.Name)
	if strings.Contains(lowerName, lowerQuery) {
		return true
	}
	lowerCat := strings.ToLower(a.CategoryID)
	return strings.Contains(lowerCat, lowerQuery)
}

type PackageItem struct {
	ArticleID    string `json:"articleId" doc:"Included article identifier."`
	ArticleName  string `json:"articleName" doc:"Included article display name."`
	Quantity     int    `json:"quantity" doc:"Included quantity."`
	UnitPriceDop int    `json:"unitPriceDop" doc:"Unit price in DOP."`
}

type PackageBundle struct {
	ID            string        `json:"id" doc:"Package identifier."`
	Name          string        `json:"name" doc:"Package commercial name."`
	EventType     string        `json:"eventType" doc:"Target event type."`
	PhotoKey      string        `json:"photoKey" doc:"Package photo key."`
	PhotoURL      string        `json:"photoUrl,omitempty" doc:"Uploaded media URL."`
	TotalPriceDop int           `json:"totalPriceDop" doc:"Package price in DOP."`
	GuestCapacity int           `json:"guestCapacity" doc:"Recommended guest capacity."`
	ItemsSummary  string        `json:"itemsSummary" doc:"Human-readable summary of included items."`
	Visible       bool          `json:"visible" doc:"Whether the package is visible in the catalog."`
	Items         []PackageItem `json:"items,omitempty" doc:"Structured items included in the package."`
}

type Category struct {
	ID        string `json:"id" doc:"Category identifier."`
	Name      string `json:"name" doc:"Category name."`
	IlloKey   string `json:"illoKey" doc:"Vector illustration key."`
	ItemCount int    `json:"itemCount" doc:"Number of articles in the category."`
}

// Trend represents a seasonal event trend card displayed on the Personal Home screen.
type Trend struct {
	ID              string `json:"id" doc:"Trend identifier."`
	Title           string `json:"title" doc:"Trend title."`
	EventsLabel     string `json:"eventsLabel" doc:"Secondary label or event count metric."`
	PhotoKey        string `json:"photoKey" doc:"Fallback catalog illustration/photo key."`
	PhotoURL        string `json:"photoUrl,omitempty" doc:"Optional uploaded media URL."`
	PhotoBgHex      string `json:"photoBgHex" doc:"Background tint hex for the card image."`
	LinkedCategory  string `json:"linkedCategory,omitempty" doc:"Optional category filter on click."`
	LinkedPackageID string `json:"linkedPackageId,omitempty" doc:"Optional package identifier on click."`
	SortOrder       int    `json:"sortOrder" doc:"Display sort priority."`
	Active          bool   `json:"active" doc:"Whether the trend is visible in Personal Home."`
}

// SeasonBanner represents the configurable seasonal hero banner shown on Personal Home.
type SeasonBanner struct {
	ID               string `json:"id" doc:"Banner identifier ('default')."`
	BadgeLabel       string `json:"badgeLabel" doc:"Upper seasonal badge text."`
	Title            string `json:"title" doc:"Main hero headline."`
	CtaLabel         string `json:"ctaLabel" doc:"Action button label."`
	TargetScreenCode string `json:"targetScreenCode" doc:"Target personal screen code on CTA click."`
	Active           bool   `json:"active" doc:"Whether the banner is published in Personal Home."`
}

type AvailabilityDay struct {
	Date      string `json:"date" doc:"ISO calendar date (YYYY-MM-DD)."`
	Reserved  int    `json:"reserved" doc:"Units reserved by active events on this date."`
	Available int    `json:"available" doc:"Remaining units available for rental on this date."`
	LowStock  bool   `json:"lowStock" doc:"True when available units are at or below low stock threshold."`
}

type ArticleAvailability struct {
	ArticleID   string            `json:"articleId" doc:"Article identifier."`
	ArticleName string            `json:"articleName" doc:"Article name."`
	Month       string            `json:"month" doc:"Queried month (YYYY-MM)."`
	TotalStock  int               `json:"totalStock" doc:"Total warehouse stock."`
	Days        []AvailabilityDay `json:"days" doc:"Per-day availability breakdown."`
}

func CanonicalArticles() []Article {
	out := canonicalFurnitureArticles()
	out = append(out, canonicalDecorArticles()...)
	return out
}

func canonicalFurnitureArticles() []Article {
	return []Article{
		{
			ID:                "silla-tiffany",
			Name:              "Silla Tiffany",
			CategoryID:        "mobiliario",
			PhotoKey:          "silla-tiffany",
			DailyPriceDop:     4,
			TotalStock:        240,
			AvailableStock:    120,
			LowStockThreshold: 20,
			Description:       "Silla Tiffany dorada y blanca con cojín incluido.",
			Visible:           true,
			Dimensions:        "40 x 42 x 92 cm",
			Material:          "Resina reforzada UV y cojín tapizado",
			ColorVariants:     []string{"Dorado", "Blanco", "Cristal"},
			IsDirectSale:      false,
		},
		{
			ID:                "mesa-plegable",
			Name:              "Mesa plegable",
			CategoryID:        "mobiliario",
			PhotoKey:          "mesa-plegable",
			DailyPriceDop:     20,
			TotalStock:        40,
			AvailableStock:    18,
			LowStockThreshold: 5,
			Description:       "Mesa rectangular plegable de 8 puestos para banquete o buffet.",
			Visible:           true,
			Dimensions:        "183 x 76 x 74 cm",
			Material:          "Polietileno de alta densidad y acero",
			ColorVariants:     []string{"Blanco"},
			IsDirectSale:      false,
		},
		{
			ID:                "mesa-redonda",
			Name:              "Mesa redonda",
			CategoryID:        "mobiliario",
			PhotoKey:          "mesa-redonda",
			DailyPriceDop:     25,
			TotalStock:        35,
			AvailableStock:    12,
			LowStockThreshold: 5,
			Description:       "Mesa redonda para 10 invitados ideal para bodas y quinceañeras.",
			Visible:           true,
			Dimensions:        "152 cm diámetro x 75 cm",
			Material:          "Madera sellada y base plegable",
			ColorVariants:     []string{"Natural", "Blanco"},
			IsDirectSale:      false,
		},
	}
}

func canonicalDecorArticles() []Article {
	return []Article{
		{
			ID:                "centro-floral",
			Name:              "Centro floral",
			CategoryID:        "floral",
			PhotoKey:          "centro-floral",
			DailyPriceDop:     35,
			TotalStock:        50,
			AvailableStock:    15,
			LowStockThreshold: 6,
			Description:       "Arreglo floral natural en tonos rosa rubor, marfil y eucalipto.",
			Visible:           true,
			Dimensions:        "35 x 35 x 45 cm",
			Material:          "Base cerámica dorada y follaje natural",
			ColorVariants:     []string{"Rosa rubor", "Marfil", "Terracota"},
			IsDirectSale:      false,
		},
		{
			ID:                "arco-rosa",
			Name:              "Arco de globos",
			CategoryID:        "arcos",
			PhotoKey:          "arco-rosa",
			DailyPriceDop:     95,
			TotalStock:        10,
			AvailableStock:    3,
			LowStockThreshold: 4,
			Description:       "Arco orgánico de globos rosa pastel, dorado cromado y follaje.",
			Visible:           true,
			Dimensions:        "240 x 220 cm",
			Material:          "Estructura metálica dorada y látex biodegradable",
			ColorVariants:     []string{"Rosa pastel y dorado", "Blanco y salvia", "Lila"},
			IsDirectSale:      false,
		},
		{
			ID:                "mesa-helados",
			Name:              "Mesa dulce",
			CategoryID:        "dulces",
			PhotoKey:          "mesa-helados",
			DailyPriceDop:     150,
			TotalStock:        8,
			AvailableStock:    2,
			LowStockThreshold: 3,
			Description:       "Estación completa de postres y dulces con bases de cristal.",
			Visible:           true,
			Dimensions:        "200 x 80 x 210 cm",
			Material:          "Madera laqueada y bases de cristal",
			ColorVariants:     []string{"Rosa empolvado", "Blanco marfil"},
			IsDirectSale:      false,
		},
	}
}

func CanonicalBalloonArticles() []Article {
	out := canonicalHeliumBalloons()
	out = append(out, canonicalCustomBalloons()...)
	return out
}

func canonicalHeliumBalloons() []Article {
	return []Article{
		{
			ID:                "balloon-bouquet-helio",
			Name:              "Bouquet de globos con helio",
			CategoryID:        "arcos",
			PhotoKey:          "arco-rosa",
			DailyPriceDop:     28,
			TotalStock:        80,
			AvailableStock:    64,
			LowStockThreshold: 10,
			Description:       "Ramillete de 7 globos con helio certificado, pesa forrada y cintas satinadas.",
			Visible:           true,
			Dimensions:        "160 cm altura regulable",
			Material:          "Látex premium y foil metalizado",
			ColorVariants:     []string{"Rosa oro", "Dorado champagne", "Azul cielo"},
			IsDirectSale:      true,
		},
		{
			ID:                "balloon-numero-metalico",
			Name:              "Globo número metálico gigante",
			CategoryID:        "arcos",
			PhotoKey:          "arco-rosa",
			DailyPriceDop:     22,
			TotalStock:        100,
			AvailableStock:    85,
			LowStockThreshold: 12,
			Description:       "Número metalizado de 40 pulgadas inflado con helio y base decorativa.",
			Visible:           true,
			Dimensions:        "102 cm alto",
			Material:          "Foil Mylar de alta retención",
			ColorVariants:     []string{"Dorado", "Rosa gold", "Plateado"},
			IsDirectSale:      true,
		},
	}
}

func canonicalCustomBalloons() []Article {
	return []Article{
		{
			ID:                "balloon-burbuja-confeti",
			Name:              "Globo burbuja cristal con confeti",
			CategoryID:        "arcos",
			PhotoKey:          "arco-rosa",
			DailyPriceDop:     48,
			TotalStock:        45,
			AvailableStock:    34,
			LowStockThreshold: 8,
			Description:       "Esfera transparente personalizada con vinil caligráfico y confeti metálico.",
			Visible:           true,
			Dimensions:        "60 cm diámetro",
			Material:          "PVC elástico cristalino",
			ColorVariants:     []string{"Confeti dorado", "Confeti rosa", "Plumas blancas"},
			IsDirectSale:      true,
		},
		{
			ID:                "balloon-kit-guirnalda",
			Name:              "Kit guirnalda orgánica DIY",
			CategoryID:        "arcos",
			PhotoKey:          "arco-rosa",
			DailyPriceDop:     65,
			TotalStock:        35,
			AvailableStock:    26,
			LowStockThreshold: 6,
			Description:       "Caja lista para instalar con 95 globos calibrados, cinta perforada y adhesivos.",
			Visible:           true,
			Dimensions:        "300 cm longitud armada",
			Material:          "Látex biodegradable doble capa",
			ColorVariants:     []string{"Rosa fiesta", "Neutros boho", "Tropical"},
			IsDirectSale:      true,
		},
	}
}

func CanonicalCategories() []Category {
	return []Category{
		{ID: "mobiliario", Name: "Mobiliario", IlloKey: "sillas", ItemCount: 3},
		{ID: "floral", Name: "Centros florales", IlloKey: "flores", ItemCount: 1},
		{ID: "arcos", Name: "Arcos y globos", IlloKey: "globos", ItemCount: 5},
		{ID: "dulces", Name: "Mesas dulces", IlloKey: "dulces", ItemCount: 1},
	}
}

// CanonicalTrends returns demo trend cards used only in full demo seed mode.
func CanonicalTrends() []Trend {
	return []Trend{
		{
			ID:             "trend-graduaciones",
			Title:          "Graduaciones",
			EventsLabel:    "2.4K eventos",
			PhotoKey:       "graduacion",
			PhotoBgHex:     "#F3EEF6",
			LinkedCategory: "",
			SortOrder:      1,
			Active:         true,
		},
		{
			ID:              "trend-bodas",
			Title:           "Bodas",
			EventsLabel:     "1.8K eventos",
			PhotoKey:        "boda",
			PhotoBgHex:      "#FDE6F2",
			LinkedPackageID: "pkg-boda-real",
			SortOrder:       2,
			Active:          true,
		},
		{
			ID:          "trend-quinceaneras",
			Title:       "Quinceañeras",
			EventsLabel: "1.5K eventos",
			PhotoKey:    "quinceanera",
			PhotoBgHex:  "#FCE8F6",
			SortOrder:   3,
			Active:      true,
		},
		{
			ID:          "trend-baby-shower",
			Title:       "Baby Shower",
			EventsLabel: "1.2K eventos",
			PhotoKey:    "baby_shower",
			PhotoBgHex:  "#E0F7F2",
			SortOrder:   4,
			Active:      true,
		},
	}
}

// CanonicalSeasonBanner returns the demo seasonal hero banner used only in full demo seed mode.
func CanonicalSeasonBanner() SeasonBanner {
	return SeasonBanner{
		ID:               "default",
		BadgeLabel:       "NUEVA TEMPORADA 2026",
		Title:            "Crea momentos inolvidables",
		CtaLabel:         "Explorar paquetes",
		TargetScreenCode: "s21",
		Active:           true,
	}
}

func CanonicalPackages() []PackageBundle {
	return []PackageBundle{
		{
			ID:            "pkg-boda-real",
			Name:          "Boda Real Rosa & Dorado",
			EventType:     "Boda",
			PhotoKey:      "boda",
			TotalPriceDop: 4500,
			GuestCapacity: 120,
			ItemsSummary:  "120 sillas Tiffany · 12 mesas redondas · 12 centros florales · 1 arco de globos · 1 mesa dulce",
			Visible:       true,
			Items: []PackageItem{
				{ArticleID: "silla-tiffany", ArticleName: "Silla Tiffany", Quantity: 120, UnitPriceDop: 4},
				{ArticleID: "mesa-redonda", ArticleName: "Mesa redonda", Quantity: 12, UnitPriceDop: 25},
				{ArticleID: "centro-floral", ArticleName: "Centro floral", Quantity: 12, UnitPriceDop: 35},
				{ArticleID: "arco-rosa", ArticleName: "Arco de globos", Quantity: 1, UnitPriceDop: 95},
				{ArticleID: "mesa-helados", ArticleName: "Mesa dulce", Quantity: 1, UnitPriceDop: 150},
			},
		},
		{
			ID:            "pkg-quince-crystal",
			Name:          "Quinceañera Valentina Crystal",
			EventType:     "Quinceañera",
			PhotoKey:      "quinceanera",
			TotalPriceDop: 6200,
			GuestCapacity: 150,
			ItemsSummary:  "150 sillas Tiffany · 15 mesas redondas · 15 centros florales · 2 arcos de globos · 1 mesa dulce",
			Visible:       true,
			Items: []PackageItem{
				{ArticleID: "silla-tiffany", ArticleName: "Silla Tiffany", Quantity: 150, UnitPriceDop: 4},
				{ArticleID: "mesa-redonda", ArticleName: "Mesa redonda", Quantity: 15, UnitPriceDop: 25},
				{ArticleID: "centro-floral", ArticleName: "Centro floral", Quantity: 15, UnitPriceDop: 35},
				{ArticleID: "arco-rosa", ArticleName: "Arco de globos", Quantity: 2, UnitPriceDop: 95},
				{ArticleID: "mesa-helados", ArticleName: "Mesa dulce", Quantity: 1, UnitPriceDop: 150},
			},
		},
		{
			ID:            "pkg-cumple-infantil",
			Name:          "Cumpleaños Infantil Mágico",
			EventType:     "Cumpleaños",
			PhotoKey:      "cumple-infantil",
			TotalPriceDop: 405,
			GuestCapacity: 40,
			ItemsSummary:  "40 sillas Tiffany · 2 mesas plegables · 1 arco de globos · 1 mesa dulce",
			Visible:       true,
			Items: []PackageItem{
				{ArticleID: "silla-tiffany", ArticleName: "Silla Tiffany", Quantity: 40, UnitPriceDop: 4},
				{ArticleID: "mesa-plegable", ArticleName: "Mesa plegable", Quantity: 2, UnitPriceDop: 20},
				{ArticleID: "arco-rosa", ArticleName: "Arco de globos", Quantity: 1, UnitPriceDop: 95},
				{ArticleID: "mesa-helados", ArticleName: "Mesa dulce", Quantity: 1, UnitPriceDop: 150},
			},
		},
	}
}
