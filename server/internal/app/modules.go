// Package app ensambla todos los módulos de dominio del monolito modular RosaFiesta.
package app

import (
	"context"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"

	catalogapp "github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	catalogdomain "github.com/Jeudry/rosafiesta/server/internal/catalog/domain"
	cataloghttp "github.com/Jeudry/rosafiesta/server/internal/catalog/httpapi"
	catalogpg "github.com/Jeudry/rosafiesta/server/internal/catalog/postgres"
	eventsapp "github.com/Jeudry/rosafiesta/server/internal/events/app"
	eventsdomain "github.com/Jeudry/rosafiesta/server/internal/events/domain"
	eventshttp "github.com/Jeudry/rosafiesta/server/internal/events/httpapi"
	eventspg "github.com/Jeudry/rosafiesta/server/internal/events/postgres"
	"github.com/Jeudry/rosafiesta/server/internal/health"
	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	identitydomain "github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	identityhttp "github.com/Jeudry/rosafiesta/server/internal/identity/httpapi"
	identitypg "github.com/Jeudry/rosafiesta/server/internal/identity/postgres"
	operationsapp "github.com/Jeudry/rosafiesta/server/internal/operations/app"
	operationshttp "github.com/Jeudry/rosafiesta/server/internal/operations/httpapi"
	operationspg "github.com/Jeudry/rosafiesta/server/internal/operations/postgres"
	paymentsapp "github.com/Jeudry/rosafiesta/server/internal/payments/app"
	paymentsdomain "github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	paymentshttp "github.com/Jeudry/rosafiesta/server/internal/payments/httpapi"
	paymentspg "github.com/Jeudry/rosafiesta/server/internal/payments/postgres"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	quotesapp "github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	quotesdomain "github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	quoteshttp "github.com/Jeudry/rosafiesta/server/internal/quotes/httpapi"
	quotespg "github.com/Jeudry/rosafiesta/server/internal/quotes/postgres"
)

// Dependencies agrupa las dependencias de infraestructura compartidas por los módulos.
type Dependencies struct {
	Pool  *pgxpool.Pool
	Clock clock.Clock
}

// SeedDatabase inserta de forma idempotente todos los datos canónicos de la Sección 6 en PostgreSQL mediante sqlc.
func SeedDatabase(ctx context.Context, pool *pgxpool.Pool, clk clock.Clock) error {
	if pool == nil {
		return nil
	}
	if clk == nil {
		clk = clock.System{}
	}
	now := clk.Now()
	expires := now.Add(30 * 24 * time.Hour)

	idRepo := identitypg.NewRepository(pool)
	for _, u := range canonicalSeedUsers(now) {
		if err := idRepo.UpsertUser(ctx, u.user, u.sessionKey, expires); err != nil {
			return err
		}
	}

	catSvc := catalogapp.NewService()
	catRepo := catalogpg.NewRepository(pool)
	if err := catRepo.Seed(ctx, catSvc.Categories(ctx), catalogdomain.CanonicalArticles(), catSvc.Packages(ctx)); err != nil {
		return err
	}

	evtRepo := eventspg.NewRepository(pool)
	for _, e := range eventsdomain.CanonicalEvents() {
		if err := evtRepo.UpsertEvent(ctx, e, now); err != nil {
			return err
		}
	}

	quoRepo := quotespg.NewRepository(pool)
	for _, q := range quotesdomain.CanonicalQuotes() {
		if err := quoRepo.UpsertQuote(ctx, q, now); err != nil {
			return err
		}
	}

	payRepo := paymentspg.NewRepository(pool)
	for _, p := range paymentsdomain.CanonicalPayments() {
		if err := payRepo.UpsertPayment(ctx, p, now); err != nil {
			return err
		}
	}

	opsRepo := operationspg.NewRepository(pool)
	return opsRepo.SeedCanonical(ctx, now)
}

type seedUser struct {
	user       identitydomain.User
	sessionKey string
}

func canonicalSeedUsers(now time.Time) []seedUser {
	return []seedUser{
		{
			sessionKey: "rf-token-camila",
			user: identitydomain.User{
				ID: uuid.MustParse("11111111-1111-1111-1111-111111111111"), FullName: "Camila Rodríguez",
				Email: "camila@rosafiesta.do", Phone: "809-555-0142", Role: identitydomain.RoleClient,
				ActiveView: identitydomain.ViewPersonal, CreatedAt: now,
			},
		},
		{
			sessionKey: "rf-token-maria",
			user: identitydomain.User{
				ID: uuid.MustParse("22222222-2222-2222-2222-222222222222"), FullName: "María (Dueña)",
				Email: "maria@rosafiesta.do", Phone: "809-555-0100", Role: identitydomain.RoleAdmin,
				Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
				ActiveView:  identitydomain.ViewAdmin, CreatedAt: now,
			},
		},
		{
			sessionKey: "rf-token-yuli",
			user: identitydomain.User{
				ID: uuid.MustParse("33333333-3333-3333-3333-333333333333"), FullName: "Yuli",
				Email: "yuli@rosafiesta.do", Phone: "809-555-0102", Role: identitydomain.RoleStaff,
				Permissions: []string{"decoracion", "tareas"},
				ActiveView:  identitydomain.ViewAdmin, CreatedAt: now,
			},
		},
	}
}

// Modules devuelve todos los módulos HTTP del monolito listos para registrarse en el router.
func Modules(deps Dependencies) []httpapi.Module {
	clk := deps.Clock
	if clk == nil {
		clk = clock.System{}
	}
	if deps.Pool != nil {
		_ = SeedDatabase(context.Background(), deps.Pool, clk)
	}
	eventsStore := eventspg.NewRepository(deps.Pool)
	identityStore := identitypg.NewRepository(deps.Pool)
	catalogStore := catalogpg.NewRepository(deps.Pool)
	quotesStore := quotespg.NewRepository(deps.Pool)
	paymentsStore := paymentspg.NewRepository(deps.Pool)
	operationsStore := operationspg.NewRepository(deps.Pool)

	eventsSvc := eventsapp.NewService().WithStore(eventsStore)
	identitySvc := identityapp.NewService(clk, eventsSvc).WithStore(identityStore)
	catalogSvc := catalogapp.NewService().WithStore(catalogStore)
	quotesSvc := quotesapp.NewService().WithStore(quotesStore)
	paymentsSvc := paymentsapp.NewService().WithStore(paymentsStore)
	operationsSvc := operationsapp.NewService().WithStore(operationsStore)

	base := []httpapi.Module{
		health.Module{},
		identityhttp.Module{Service: identitySvc},
		cataloghttp.Module{Service: catalogSvc},
		eventshttp.Module{Service: eventsSvc},
		quoteshttp.Module{Service: quotesSvc},
		paymentshttp.Module{Service: paymentsSvc},
		operationshttp.Module{Service: operationsSvc},
	}
	return append(base, devModules(func() {
		if deps.Pool != nil {
			_ = SeedDatabase(context.Background(), deps.Pool, clk)
		}
		*eventsSvc = *eventsapp.NewService().WithStore(eventsStore)
		*identitySvc = *identityapp.NewService(clk, eventsSvc).WithStore(identityStore)
		*catalogSvc = *catalogapp.NewService().WithStore(catalogStore)
		*quotesSvc = *quotesapp.NewService().WithStore(quotesStore)
		*paymentsSvc = *paymentsapp.NewService().WithStore(paymentsStore)
		*operationsSvc = *operationsapp.NewService().WithStore(operationsStore)
	})...)
}
