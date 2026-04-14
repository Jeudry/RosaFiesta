package db

import (
	"context"
	"database/sql"
	"fmt"
	"log"
	"math/rand"

	"Backend/internal/store"
	"Backend/internal/store/models"

	"github.com/google/uuid"
)

var userNames = []string{
	"John",
	"Alice",
	"Bob",
	"Charlie",
	"David",
	"Emily",
	"Frank",
	"Grace",
	"Harry",
	"Ian",
	"Jack",
	"Kate",
	"Lee",
	"Mike",
	"Nick",
	"Oscar",
	"Paul",
	"Queen",
	"Robert",
	"Sam",
	"Tom",
	"Ursula",
	"Victor",
	"Will",
	"Xavier",
	"Yvonne",
	"Zachary",
	"Rosa",
	"Olivia",
	"Emma",
	"Liam",
	"Noah",
	"Oliver",
	"William",
	"James",
	"Logan",
	"Benjamin",
	"Lucas",
	"Michael",
	"Ethan",
	"Alexander",
	"Daniel",
	"Matthew",
	"Aiden",
	"Jackson",
	"Sebastian",
	"Joseph",
	"Samuel",
	"David",
	"Johnathan",
}

var titles = []string{
	"Hoy es un buen día para empezar algo nuevo",
	"Cree en ti, todo lo demás llegará",
	"Los pequeños momentos son los que hacen la vida grande",
	"No olvides sonreír, alguien lo necesita hoy",
	"Cambia el mundo, empieza contigo",
	"Tiempo más amor es la fórmula perfecta",
	"Los sueños grandes empiezan con pasos pequeños",
	"Disfruta el ahora, el futuro se construye solo",
	"Hoy puede ser el día que marque la diferencia",
	"La vida tiene ritmo, encuentra tu canción",
	"Ser feliz no es perfecto, es ser auténtico",
	"¿Cuál es tu meta esta semana? Hazla realidad",
	"Un consejo para hoy, vive sin prisa pero sin pausa",
	"Cada día es una nueva oportunidad, aprovéchala",
	"Sueña hoy, vive mañana",
	"Cada amanecer trae una nueva posibilidad",
	"Si no es hoy, ¿cuándo? Es tu momento",
	"Déjate llevar, la vida siempre encuentra su curso",
	"Lo bueno está por venir, confía en el proceso",
	"Hoy es el día para brillar como nunca",
}

var contents = []string{
	"La vida es un camino que no se puede volar, debemos tomar decisiones con responsabilidad y compromiso.",
	"El éxito es la suma de pequenos esfuerzos repetidos diariamente.",
	"La vida es un viaje, no un destino, y cada paso es una aventura.",
	" La vida es un camino que no se puede volar, debemos tomar decisiones con responsabilidad y compromiso.",
	"El éxito es la suma de pequenos esfuerzos repetidos diariamente.",
	"La vida es un viaje, no un destino, y cada paso es una aventura.",
	" La vida es un camino que no se puede volar, debemos tomar decisiones con responsabilidad y compromiso.",
	"El éxito es la suma de pequenos esfuerzos repetidos diariamente.",
	"La vida es un viaje, no un destino, y cada paso es una aventura.",
	" La vida es un camino que no se puede volar, debemos tomar decisiones con responsabilidad y compromiso.",
}

var tags = []string{
	"motivación",
	"inspiración",
	"crecimiento",
	"positividad",
	"éxito",
	"vida",
	"metas",
	"superación",
	"cambio",
	"resiliencia",
	"actitud",
	"determinación",
	"confianza",
	"esperanza",
	"momentos",
	"propósito",
	"aprendizaje",
	"fortaleza",
	"acción",
	"logros",
}

var comments = []string{
	"Me encanta tu escritura!",
	"Estoy aprendiendo mucho de ti!",
	"Me encanta tu post!",
	"¡Espero que te guste!",
	"Gracias por compartir!",
	"Me encanta tu post!",
	"Estoy aprendiendo mucho de ti!",
	"¡Espero que te guste!",
	"Gracias por compartir!",
	"Me encanta tu post!",
}

func Seed(store store.Storage, db *sql.DB) error {
	ctx := context.Background()

	// Seed Categories
	categories := generateCategories()
	for _, category := range categories {
		if err := store.Categories.Create(ctx, category); err != nil {
			log.Println("Error creating category (might exist): ", err)
		}
	}

	// Build a category lookup by name so articles can reference them by ID.
	// If a category already existed, re-read its ID from the DB.
	catByName := make(map[string]*models.Category)
	for _, c := range categories {
		if c.ID == uuid.Nil {
			var existingID uuid.UUID
			if err := db.QueryRowContext(ctx,
				`SELECT id FROM categories WHERE name = $1 LIMIT 1`,
				c.Name,
			).Scan(&existingID); err == nil {
				c.ID = existingID
			}
		}
		catByName[c.Name] = c
	}

	users := generateUsers(100)

	// Create users one by one (skip duplicates instead of rolling back everything)
	for _, user := range users {
		userTx, _ := db.BeginTx(ctx, nil)
		if err := store.Users.Create(ctx, userTx, user); err != nil {
			_ = userTx.Rollback()
			log.Println("Skipping user (might exist):", user.UserName)
			continue
		}
		userTx.Commit()
	}

	posts := generatePosts(200, users)

	for _, post := range posts {
		if err := store.Posts.Create(ctx, post); err != nil {
			log.Println("Skipping post:", err)
		}
	}

	// Seed Articles (decoration and furniture products)
	articles := generateArticles(catByName)
	for _, article := range articles {
		if err := store.Articles.Create(ctx, article); err != nil {
			log.Println("Error creating article (might exist):", err)
		}
	}

	// Seed Bundles
	if err := seedBundles(ctx, db); err != nil {
		log.Println("Error seeding bundles:", err)
	}

	return nil
}

func generateCategories() []*models.Category {
	admin := "Admin"

	cat := func(name, desc, icon, image string) *models.Category {
		d := desc
		i := icon
		img := image
		return &models.Category{
			BaseModel:   models.BaseModel{CreatedBy: &admin},
			Name:        name,
			Description: &d,
			Icon:        &i,
			ImageURL:    &img,
		}
	}

	return []*models.Category{
		cat("Furniture", "Sillas, mesas y mobiliario para tu evento",
			"chair",
			"https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=800"),
		cat("Decor", "Detalles decorativos para crear ambiente",
			"auto_awesome",
			"https://images.unsplash.com/photo-1478146059778-26028b07395a?w=800"),
		cat("Iluminación", "Luces, neón y guirnaldas para ambientar",
			"lightbulb",
			"https://images.unsplash.com/photo-1514849302-984523450cf4?w=800"),
		cat("Floral", "Arreglos, arcos y centros de mesa florales",
			"local_florist",
			"https://images.unsplash.com/photo-1519741497674-611481863552?w=800"),
		cat("Globos", "Arcos de globos y guirnaldas para toda celebración",
			"celebration",
			"https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=800"),
		cat("Mantelería", "Manteles, caminos de mesa y servilletas",
			"table_restaurant",
			"https://images.unsplash.com/photo-1464699908537-0954e50791ee?w=800"),
		cat("Mesa Dulce", "Decoración completa para mesas de dulces y postres",
			"cake",
			"https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=800"),
		cat("Letreros", "Letreros neón y backdrops personalizados",
			"auto_awesome_motion",
			"https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800"),
	}
}

func generateUsers(quantity int) []*models.User {
	users := make([]*models.User, quantity)

	for i := 0; i < quantity; i++ {
		users[i] = &models.User{
			UserName: userNames[i%len(userNames)] + fmt.Sprintf("%d", i),
			Email:    userNames[i%len(userNames)] + fmt.Sprintf("%d", i) + "@example.com",
			Role: models.Role{
				Name: "user",
			},
		}
	}
	return users
}

// articleSeed is a lightweight struct used to build full Article objects
// before inserting them via the Articles store.
type articleSeed struct {
	name        string
	description string
	category    string // matches the category Name in generateCategories
	articleType models.ArticleType
	sku         string
	imageURL    string
	rentalPrice float64
	stock       int
	color       string
	material    string
}

// generateArticles builds a curated set of decoration & furniture rental
// products that match the event-planning domain of RosaFiesta.
// Uses stable Unsplash image URLs so products render immediately without
// any additional asset setup on the backend.
func generateArticles(catByName map[string]*models.Category) []*models.Article {
	admin := "Admin"

	seeds := []articleSeed{
		// ── Furniture ────────────────────────────────────────────────
		{
			name:        "Silla Tiffany Cristal",
			description: "Silla de acrílico transparente, elegante y versátil para cualquier tipo de evento.",
			category:    "Furniture",
			articleType: models.ArticleTypeRental,
			sku:         "SILLA-TIFFANY-CRISTAL",
			imageURL:    "https://images.unsplash.com/photo-1503602642458-232111445657?w=800",
			rentalPrice: 3.50,
			stock:       200,
			color:       "Transparente",
			material:    "Acrílico",
		},
		{
			name:        "Silla Chiavari Dorada",
			description: "Silla italiana clásica en dorado, perfecta para bodas y eventos de lujo.",
			category:    "Furniture",
			articleType: models.ArticleTypeRental,
			sku:         "SILLA-CHIAVARI-ORO",
			imageURL:    "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=800",
			rentalPrice: 4.50,
			stock:       150,
			color:       "Dorado",
			material:    "Madera con acabado metálico",
		},
		{
			name:        "Mesa Redonda 8 Personas",
			description: "Mesa redonda de 1.80m, capacidad para 8 invitados. Ideal para banquetes.",
			category:    "Furniture",
			articleType: models.ArticleTypeRental,
			sku:         "MESA-REDONDA-180",
			imageURL:    "https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=800",
			rentalPrice: 15.00,
			stock:       40,
			color:       "Natural",
			material:    "Madera",
		},
		{
			name:        "Mesa Imperial Rectangular",
			description: "Mesa rectangular de 2.40m, ideal para eventos corporativos o cenas largas.",
			category:    "Furniture",
			articleType: models.ArticleTypeRental,
			sku:         "MESA-IMPERIAL-240",
			imageURL:    "https://images.unsplash.com/photo-1478146896981-b80fe463b330?w=800",
			rentalPrice: 18.00,
			stock:       25,
			color:       "Natural",
			material:    "Madera",
		},
		{
			name:        "Lounge Blanco Modular",
			description: "Set de sofás modulares en cuero blanco para áreas VIP y cócteles.",
			category:    "Furniture",
			articleType: models.ArticleTypeRental,
			sku:         "LOUNGE-BLANCO-SET",
			imageURL:    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800",
			rentalPrice: 75.00,
			stock:       8,
			color:       "Blanco",
			material:    "Cuero sintético",
		},

		// ── Decor ────────────────────────────────────────────────────
		{
			name:        "Arco Floral Romántico",
			description: "Arco de flores frescas en tonos rosas y blancos. Perfecto para bodas.",
			category:    "Floral",
			articleType: models.ArticleTypeRental,
			sku:         "ARCO-FLORAL-ROSA",
			imageURL:    "https://images.unsplash.com/photo-1519741497674-611481863552?w=800",
			rentalPrice: 280.00,
			stock:       5,
			color:       "Rosa y Blanco",
			material:    "Flores frescas",
		},
		{
			name:        "Centro de Mesa con Velas",
			description: "Arreglo floral con candelabros de cristal para mesas de banquete.",
			category:    "Floral",
			articleType: models.ArticleTypeRental,
			sku:         "CENTRO-VELAS-CRISTAL",
			imageURL:    "https://images.unsplash.com/photo-1478146059778-26028b07395a?w=800",
			rentalPrice: 35.00,
			stock:       40,
			color:       "Dorado",
			material:    "Flores y cristal",
		},
		{
			name:        "Globos Orgánicos Pastel",
			description: "Guirnalda de globos en tonos pastel, perfecta para baby showers y cumpleaños.",
			category:    "Globos",
			articleType: models.ArticleTypeRental,
			sku:         "GLOBOS-ORG-PASTEL",
			imageURL:    "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=800",
			rentalPrice: 120.00,
			stock:       15,
			color:       "Pastel",
			material:    "Látex biodegradable",
		},
		{
			name:        "Luces String Vintage",
			description: "Guirnalda de luces tipo Edison, 10 metros. Ambiente cálido y romántico.",
			category:    "Iluminación",
			articleType: models.ArticleTypeRental,
			sku:         "LUCES-STRING-10M",
			imageURL:    "https://images.unsplash.com/photo-1514849302-984523450cf4?w=800",
			rentalPrice: 25.00,
			stock:       30,
			color:       "Cálido",
			material:    "Cobre y vidrio",
		},
		{
			name:        "Backdrop de Flores Eternas",
			description: "Panel de flores artificiales premium 2x2m, ideal para fotos.",
			category:    "Floral",
			articleType: models.ArticleTypeRental,
			sku:         "BACKDROP-FLORES-2X2",
			imageURL:    "https://images.unsplash.com/photo-1513725673957-ab1b1a7f1b65?w=800",
			rentalPrice: 180.00,
			stock:       6,
			color:       "Multicolor",
			material:    "Flores artificiales premium",
		},
		{
			name:        "Camino de Mesa Dorado",
			description: "Runner de tela con bordados dorados, 3 metros de largo.",
			category:    "Mantelería",
			articleType: models.ArticleTypeRental,
			sku:         "CAMINO-DORADO-3M",
			imageURL:    "https://images.unsplash.com/photo-1464699908537-0954e50791ee?w=800",
			rentalPrice: 8.00,
			stock:       60,
			color:       "Dorado",
			material:    "Lino bordado",
		},
		{
			name:        "Candelabros de Cristal",
			description: "Candelabros altos de cristal para mesa, elegantes y sofisticados.",
			category:    "Decor",
			articleType: models.ArticleTypeRental,
			sku:         "CANDELABRO-CRISTAL",
			imageURL:    "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=800",
			rentalPrice: 12.00,
			stock:       50,
			color:       "Transparente",
			material:    "Cristal",
		},
		{
			name:        "Arco Globos Gender Reveal",
			description: "Arco de globos rosa y azul especial para eventos de revelación de género.",
			category:    "Globos",
			articleType: models.ArticleTypeRental,
			sku:         "ARCO-GLOBOS-GR",
			imageURL:    "https://images.unsplash.com/photo-1530103043960-ef38714abb15?w=800",
			rentalPrice: 95.00,
			stock:       10,
			color:       "Rosa y Azul",
			material:    "Látex premium",
		},
		{
			name:        "Mesa Dulcera Temática",
			description: "Decoración completa para mesa de dulces, incluye bandejas y decoración.",
			category:    "Mesa Dulce",
			articleType: models.ArticleTypeRental,
			sku:         "MESA-DULCERA-KIT",
			imageURL:    "https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=800",
			rentalPrice: 150.00,
			stock:       8,
			color:       "Multicolor",
			material:    "Kit completo",
		},
		{
			name:        "Neón LED Personalizado",
			description: "Letrero neón LED con nombre personalizado, 80cm de largo.",
			category:    "Letreros",
			articleType: models.ArticleTypeRental,
			sku:         "NEON-LED-CUSTOM",
			imageURL:    "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800",
			rentalPrice: 65.00,
			stock:       12,
			color:       "Rosa neón",
			material:    "LED flex",
		},
	}

	articles := make([]*models.Article, 0, len(seeds))
	for _, s := range seeds {
		desc := s.description
		variantName := s.color
		if variantName == "" {
			variantName = "Standard"
		}
		imageURL := s.imageURL

		var categoryID *uuid.UUID
		if cat, ok := catByName[s.category]; ok && cat.ID != uuid.Nil {
			id := cat.ID
			categoryID = &id
		}

		article := &models.Article{
			BaseModel: models.BaseModel{
				CreatedBy: &admin,
			},
			NameTemplate:        s.name,
			DescriptionTemplate: &desc,
			Type:                s.articleType,
			CategoryID:          categoryID,
			IsActive:            true,
			StockQuantity:       s.stock,
			Variants: []models.ArticleVariant{
				{
					Sku:         s.sku,
					Name:        variantName,
					Description: &desc,
					ImageURL:    &imageURL,
					IsActive:    true,
					Stock:       s.stock,
					RentalPrice: s.rentalPrice,
					Attributes: map[string]string{
						"color":    s.color,
						"material": s.material,
					},
				},
			},
		}
		articles = append(articles, article)
	}

	// ── Multi-variant products ────────────────────────────────────────
	// Add extra variants to selected products so the UI can demo the
	// color-circle picker and image carousel.

	multiVariants := map[string][]struct {
		sku, name, color, imageURL string
		rentalPrice                float64
		stock                      int
	}{
		"Silla Tiffany Cristal": {
			{sku: "SILLA-TIFFANY-ROSA", name: "Rosa", color: "Rosa", imageURL: "https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=800", rentalPrice: 3.50, stock: 80},
			{sku: "SILLA-TIFFANY-GOLD", name: "Dorada", color: "Dorado", imageURL: "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=800", rentalPrice: 4.00, stock: 60},
			{sku: "SILLA-TIFFANY-NEGRO", name: "Negro", color: "Negro", imageURL: "https://images.unsplash.com/photo-1551298370-9d3d53740c72?w=800", rentalPrice: 3.50, stock: 100},
		},
		"Neón LED Personalizado": {
			{sku: "NEON-LED-AZUL", name: "Azul neón", color: "Azul", imageURL: "https://images.unsplash.com/photo-1563206767-5b18f218e8de?w=800", rentalPrice: 65.00, stock: 10},
			{sku: "NEON-LED-BLANCO", name: "Blanco cálido", color: "Blanco", imageURL: "https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=800", rentalPrice: 65.00, stock: 8},
		},
		"Arco Floral Romántico": {
			{sku: "ARCO-FLORAL-LAVANDA", name: "Lavanda", color: "Morado", imageURL: "https://images.unsplash.com/photo-1522748906645-95d8adfd52c7?w=800", rentalPrice: 280.00, stock: 3},
			{sku: "ARCO-FLORAL-BLANCO", name: "Blanco puro", color: "Blanco", imageURL: "https://images.unsplash.com/photo-1478146059778-26028b07395a?w=800", rentalPrice: 260.00, stock: 4},
		},
	}

	for _, a := range articles {
		extras, ok := multiVariants[a.NameTemplate]
		if !ok {
			continue
		}
		desc := ""
		if a.DescriptionTemplate != nil {
			desc = *a.DescriptionTemplate
		}
		for _, e := range extras {
			imgURL := e.imageURL
			a.Variants = append(a.Variants, models.ArticleVariant{
				Sku:         e.sku,
				Name:        e.name,
				Description: &desc,
				ImageURL:    &imgURL,
				IsActive:    true,
				Stock:       e.stock,
				RentalPrice: e.rentalPrice,
				Attributes: map[string]string{
					"color":    e.color,
					"material": a.Variants[0].Attributes["material"],
				},
			})
		}
	}

	return articles
}

func generatePosts(quantity int, users []*models.User) []*models.Post {
	posts := make([]*models.Post, quantity)

	for i := 0; i < quantity; i++ {
		user := users[rand.Intn(len(users))]

		posts[i] = &models.Post{
			Title:   titles[rand.Intn(len(titles))],
			Content: contents[rand.Intn(len(contents))],
			UserID:  user.ID,
			Tags: []string{
				tags[rand.Intn(len(tags))],
				tags[rand.Intn(len(tags))],
			},
			Comments: []models.Comment{
				{
					Content: comments[rand.Intn(len(comments))],
					UserID:  user.ID,
				},
			},
		}
	}

	return posts
}

// seedBundles inserts themed bundles with their items.
func seedBundles(ctx context.Context, db *sql.DB) error {
	// Helper to get article ID by SKU
	getArticleID := func(sku string) (uuid.UUID, error) {
		var id uuid.UUID
		err := db.QueryRowContext(ctx, `SELECT id FROM articles WHERE name_template = $1 LIMIT 1`, sku).Scan(&id)
		return id, err
	}

	type bundleSeed struct {
		name            string
		description     string
		discountPercent float64
		imageURL        string
		minPrice        float64
		items           []struct {
			sku        string
			quantity   int
			isOptional bool
		}
	}

	seeds := []bundleSeed{
		{
			name:            "Bodas Rosa",
			description:     "Paquete romántico completo para bodas. Incluye trasero floral, centros de mesa con velas, mantelería premium y cristalería elegante.",
			discountPercent: 15,
			imageURL:        "https://images.unsplash.com/photo-1519741497674-611481863552?w=800",
			minPrice:        850,
			items: []struct {
				sku        string
				quantity   int
				isOptional bool
			}{
				{"Arco Floral Romántico", 1, false},
				{"Centro de Mesa con Velas", 8, false},
				{"Camino de Mesa Dorado", 8, false},
				{"Candelabros de Cristal", 8, false},
				{"Backdrop de Flores Eternas", 1, true},
			},
		},
		{
			name:            "Cumpleaños Infantil",
			description:     "Kit completo para fiestas infantiles. Globos, banners, decoración de mesa y un animado centro de mesa.",
			discountPercent: 10,
			imageURL:        "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=800",
			minPrice:        450,
			items: []struct {
				sku        string
				quantity   int
				isOptional bool
			}{
				{"Globos Orgánicos Pastel", 1, false},
				{"Mesa Dulcera Temática", 1, false},
				{"Centro de Mesa con Velas", 2, false},
				{"Arco Globos Gender Reveal", 1, true},
			},
		},
		{
			name:            "Quinceañera Premium",
			description:     "Paquete de lujo para quinceañeras. Arco floral dramático, arco de globos, centros de mesa y letrero neón personalizado.",
			discountPercent: 12,
			imageURL:        "https://images.unsplash.com/photo-1513725673957-ab1b1a7f1b65?w=800",
			minPrice:        1200,
			items: []struct {
				sku        string
				quantity   int
				isOptional bool
			}{
				{"Arco Floral Romántico", 1, false},
				{"Arco Globos Gender Reveal", 1, false},
				{"Centro de Mesa con Velas", 10, false},
				{"Neón LED Personalizado", 1, true},
				{"Backdrop de Flores Eternas", 1, true},
			},
		},
		{
			name:            "Evento Corporativo",
			description:     "Solución profesional para eventos empresariales. Mantelería elegante, centros de mesa sobrios y señalización de calidad.",
			discountPercent: 8,
			imageURL:        "https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=800",
			minPrice:        600,
			items: []struct {
				sku        string
				quantity   int
				isOptional bool
			}{
				{"Camino de Mesa Dorado", 10, false},
				{"Centro de Mesa con Velas", 5, false},
				{"Luces String Vintage", 2, false},
				{"Candelabros de Cristal", 5, true},
			},
		},
	}

	for _, seed := range seeds {
		// Insert bundle
		var bundleID uuid.UUID
		err := db.QueryRowContext(ctx, `
			INSERT INTO bundles (name, description, discount_percent, image_url, min_price, is_active)
			VALUES ($1, $2, $3, $4, $5, true)
			ON CONFLICT DO NOTHING
			RETURNING id`,
			seed.name, seed.description, seed.discountPercent, seed.imageURL, seed.minPrice,
		).Scan(&bundleID)
		if err != nil {
			// Bundle might already exist
			log.Printf("Bundle %s might already exist: %v", seed.name, err)
			continue
		}

		// Insert bundle items
		for i, item := range seed.items {
			articleID, err := getArticleID(item.sku)
			if err != nil {
				log.Printf("Article not found for SKU %s: %v", item.sku, err)
				continue
			}

			_, err = db.ExecContext(ctx, `
				INSERT INTO bundle_items (bundle_id, article_id, quantity, is_optional, sort_order)
				VALUES ($1, $2, $3, $4, $5)`,
				bundleID, articleID, item.quantity, item.isOptional, i,
			)
			if err != nil {
				log.Printf("Error inserting bundle item %s: %v", item.sku, err)
			}
		}
	}

	return nil
}
