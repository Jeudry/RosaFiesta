package db

import (
	"Backend/internal/store"
	"Backend/internal/store/models"
	"context"
	"database/sql"
	"fmt"
	"log"
	"math/rand"
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

	users := generateUsers(100)
	tx, _ := db.BeginTx(ctx, nil)

	for _, user := range users {
		if err := store.Users.Create(ctx, tx, user); err != nil {
			_ = tx.Rollback()
			log.Println("Error creating user: ", err)
			return err
		}
	}

	tx.Commit()

	posts := generatePosts(200, users)

	for _, post := range posts {
		if err := store.Posts.Create(ctx, post); err != nil {
			return err
		}
	}

	return nil
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
