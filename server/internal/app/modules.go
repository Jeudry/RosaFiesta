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
	paymentshttp "github.com/Jeudry/rosafiesta/server/internal/payments/httpapi"
	paymentspg "github.com/Jeudry/rosafiesta/server/internal/payments/postgres"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	quotesapp "github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	quotesdomain "github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	quoteshttp "github.com/Jeudry/rosafiesta/server/internal/quotes/httpapi"
	quotespg "github.com/Jeudry/rosafiesta/server/internal/quotes/postgres"
)

type Dependencies struct {
	Pool          *pgxpool.Pool
	Clock         clock.Clock
	Config        config.Config
	OTPNotifier   identityapp.OTPNotifier
	AdminOnlySeed bool
}

func SeedAdminAccountsOnly(ctx context.Context, pool *pgxpool.Pool, clk clock.Clock) error {
	if pool == nil {
		return nil
	}
	if clk == nil {
		clk = clock.System{}
	}
	now := clk.Now()
	expires := now.Add(30 * 24 * time.Hour)
	idRepo := identitypg.NewRepository(pool).WithClock(clk)
	for _, entry := range adminOnlySeedUsers(now) {
		if err := idRepo.UpsertUser(ctx, entry.user, entry.sessionKey, expires); err != nil {
			return err
		}
	}
	for _, staff := range identityapp.DefaultAdminStaff(now) {
		if err := idRepo.UpsertStaffMember(ctx, staff); err != nil {
			return err
		}
	}
	return nil
}

func SeedDatabase(ctx context.Context, pool *pgxpool.Pool, clk clock.Clock) error {
	if pool == nil {
		return nil
	}
	if clk == nil {
		clk = clock.System{}
	}
	now := clk.Now()
	if err := seedIdentityAndCatalog(ctx, pool, clk, now); err != nil {
		return err
	}
	if err := seedEventsAndQuotes(ctx, pool, now); err != nil {
		return err
	}
	payRepo := paymentspg.NewRepository(pool)
	if err := payRepo.SeedCanonical(ctx, now); err != nil {
		return err
	}
	opsRepo := operationspg.NewRepository(pool)
	return opsRepo.SeedCanonical(ctx, now)
}

func seedIdentityAndCatalog(ctx context.Context, pool *pgxpool.Pool, clk clock.Clock, now time.Time) error {
	expires := now.Add(30 * 24 * time.Hour)
	idRepo := identitypg.NewRepository(pool).WithClock(clk)
	for _, entry := range canonicalSeedUsers(now) {
		if err := idRepo.UpsertUser(ctx, entry.user, entry.sessionKey, expires); err != nil {
			return err
		}
	}
	for _, cli := range identityapp.DefaultClients(now) {
		if err := idRepo.UpsertAdminClient(ctx, cli); err != nil {
			return err
		}
	}
	for _, staff := range identityapp.DefaultStaff(now) {
		if err := idRepo.UpsertStaffMember(ctx, staff); err != nil {
			return err
		}
	}
	catSvc := catalogapp.NewService()
	catRepo := catalogpg.NewRepository(pool)
	return catRepo.Seed(ctx, catSvc.Categories(ctx), catalogdomain.CanonicalArticles(), catSvc.Packages(ctx))
}

func seedEventsAndQuotes(ctx context.Context, pool *pgxpool.Pool, now time.Time) error {
	evtRepo := eventspg.NewRepository(pool)
	for _, evt := range eventsdomain.CanonicalEvents() {
		if err := evtRepo.UpsertEvent(ctx, evt, now); err != nil {
			return err
		}
	}
	if err := evtRepo.SeedCollaboration(ctx, now); err != nil {
		return err
	}
	quoRepo := quotespg.NewRepository(pool)
	for _, quote := range quotesdomain.CanonicalQuotes() {
		if err := quoRepo.UpsertQuote(ctx, quote, now); err != nil {
			return err
		}
	}
	return nil
}

type seedUser struct {
	user       identitydomain.User
	sessionKey string
}

func adminOnlySeedUsers(now time.Time) []seedUser {
	return []seedUser{
		{
			sessionKey: "rf-token-maria",
			user: identitydomain.User{
				ID:          uuid.MustParse("22222222-2222-2222-2222-222222222222"),
				Username:    "maria",
				FullName:    "María (Dueña)",
				Email:       "maria@rosafiesta.do",
				Phone:       "809-555-0100",
				BirthDate:   "12/04/1990",
				Role:        identitydomain.RoleAdmin,
				Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
				ActiveView:  identitydomain.ViewAdmin,
				CreatedAt:   now,
			},
		},
		{
			sessionKey: "rf-token-yuli",
			user: identitydomain.User{
				ID:          uuid.MustParse("33333333-3333-3333-3333-333333333333"),
				Username:    "yuli",
				FullName:    "Yuli",
				Email:       "yuli@rosafiesta.do",
				Phone:       "809-555-0102",
				BirthDate:   "22/09/1996",
				Role:        identitydomain.RoleStaff,
				Permissions: []string{"decoracion", "tareas"},
				ActiveView:  identitydomain.ViewAdmin,
				CreatedAt:   now,
			},
		},
	}
}

func canonicalSeedUsers(now time.Time) []seedUser {
	return []seedUser{
		{
			sessionKey: "rf-token-camila",
			user: identitydomain.User{
				ID:         uuid.MustParse("11111111-1111-1111-1111-111111111111"),
				Username:   "camila",
				FullName:   "Camila Rodríguez",
				Email:      "camila@rosafiesta.do",
				Phone:      "809-555-0142",
				BirthDate:  "15/08/1998",
				Role:       identitydomain.RoleClient,
				ActiveView: identitydomain.ViewPersonal,
				CreatedAt:  now,
			},
		},
		{
			sessionKey: "rf-token-maria",
			user: identitydomain.User{
				ID:          uuid.MustParse("22222222-2222-2222-2222-222222222222"),
				Username:    "maria",
				FullName:    "María (Dueña)",
				Email:       "maria@rosafiesta.do",
				Phone:       "809-555-0100",
				BirthDate:   "12/04/1990",
				Role:        identitydomain.RoleAdmin,
				Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
				ActiveView:  identitydomain.ViewAdmin,
				CreatedAt:   now,
			},
		},
		{
			sessionKey: "rf-token-yuli",
			user: identitydomain.User{
				ID:          uuid.MustParse("33333333-3333-3333-3333-333333333333"),
				Username:    "yuli",
				FullName:    "Yuli",
				Email:       "yuli@rosafiesta.do",
				Phone:       "809-555-0102",
				BirthDate:   "22/09/1996",
				Role:        identitydomain.RoleStaff,
				Permissions: []string{"decoracion", "tareas"},
				ActiveView:  identitydomain.ViewAdmin,
				CreatedAt:   now,
			},
		},
	}
}

type assembledServices struct {
	events     *eventsapp.Service
	identity   *identityapp.Service
	catalog    *catalogapp.Service
	quotes     *quotesapp.Service
	payments   *paymentsapp.Service
	operations *operationsapp.Service
}

func Modules(deps Dependencies) []httpapi.Module {
	base, svcs, clk := coreModules(deps)
	onReset := buildResetHandler(deps.Pool, clk, svcs, deps.AdminOnlySeed)
	return append(base, devModules(onReset)...)
}

func coreModules(deps Dependencies) ([]httpapi.Module, assembledServices, clock.Clock) {
	clk := deps.Clock
	if clk == nil {
		clk = clock.System{}
	}
	shouldAutoSeed := deps.Pool != nil
	if shouldAutoSeed {
		shouldAutoSeed = deps.Config.Env != config.EnvProduction
	}
	if shouldAutoSeed {
		seedSelectedMode(context.Background(), deps.Pool, clk, deps.AdminOnlySeed)
	}
	svcs := wireServices(deps, clk)
	var readiness health.ReadinessChecker
	if deps.Pool != nil {
		readiness = deps.Pool
	}
	base := []httpapi.Module{
		health.Module{Readiness: readiness},
		identityhttp.Module{Service: svcs.identity},
		cataloghttp.Module{Service: svcs.catalog, Auth: svcs.identity},
		eventshttp.Module{Service: svcs.events, Auth: svcs.identity},
		quoteshttp.Module{Service: svcs.quotes, Auth: svcs.identity},
		paymentshttp.Module{Service: svcs.payments, Auth: svcs.identity},
		operationshttp.Module{Service: svcs.operations, Auth: svcs.identity},
	}
	return base, svcs, clk
}

func seedSelectedMode(ctx context.Context, pool *pgxpool.Pool, clk clock.Clock, adminOnly bool) {
	if adminOnly {
		_ = SeedAdminAccountsOnly(ctx, pool, clk)
		return
	}
	_ = SeedDatabase(ctx, pool, clk)
}

func wireServices(deps Dependencies, clk clock.Clock) assembledServices {
	eventsStore := eventspg.NewRepository(deps.Pool)
	identityStore := identitypg.NewRepository(deps.Pool).WithClock(clk)
	catalogStore := catalogpg.NewRepository(deps.Pool)
	quotesStore := quotespg.NewRepository(deps.Pool)
	paymentsStore := paymentspg.NewRepository(deps.Pool)
	operationsStore := operationspg.NewRepository(deps.Pool)

	eventsSvc := eventsapp.NewService().WithClock(clk)
	idOpts := buildIdentityOptions(deps)
	identitySvc := identityapp.NewServiceWithOptions(clk, eventsSvc, idOpts)
	catalogSvc := catalogapp.NewService().WithReservationReader(eventsSvc)
	quotesSvc := quotesapp.NewService().
		WithClock(clk).
		WithCatalog(catalogSvc).
		WithAIOptions(deps.Config.STTAPIURL, deps.Config.AIProviderAPIKey)
	paymentsSvc := paymentsapp.NewService().
		WithClock(clk).
		WithMetricsReaders(eventsSvc, quotesSvc)
	eventsSvc.WithRefundRecorder(paymentsSvc)
	operationsSvc := operationsapp.NewService().
		WithClock(clk).
		WithRevenueProvider(func(ctx context.Context) int {
			return paymentsSvc.GetSummary(ctx).OctRevenueRD
		})

	if deps.AdminOnlySeed {
		resetServicesAdminOnly(context.Background(), assembledServices{
			events:     eventsSvc,
			identity:   identitySvc,
			catalog:    catalogSvc,
			quotes:     quotesSvc,
			payments:   paymentsSvc,
			operations: operationsSvc,
		})
	}

	eventsSvc.WithStore(eventsStore)
	identitySvc.WithStore(identityStore)
	catalogSvc.WithStore(catalogStore)
	quotesSvc.WithStore(quotesStore)
	paymentsSvc.WithStore(paymentsStore)
	operationsSvc.WithStore(operationsStore)

	return assembledServices{
		events:     eventsSvc,
		identity:   identitySvc,
		catalog:    catalogSvc,
		quotes:     quotesSvc,
		payments:   paymentsSvc,
		operations: operationsSvc,
	}
}

func resetServicesAdminOnly(ctx context.Context, svcs assembledServices) {
	svcs.events.ResetEmpty(ctx)
	svcs.identity.ResetAdminOnly(ctx)
	svcs.catalog.ResetEmpty(ctx)
	svcs.quotes.ResetEmpty(ctx)
	svcs.payments.ResetEmpty(ctx)
	svcs.operations.ResetEmpty(ctx)
}

func buildIdentityOptions(deps Dependencies) identityapp.Options {
	return identityapp.Options{
		Env:            deps.Config.Env,
		JWTSecret:      deps.Config.JWTSecret,
		SMTPHost:       deps.Config.SMTPHost,
		SMTPPort:       deps.Config.SMTPPort,
		SMTPFrom:       deps.Config.SMTPFrom,
		SMTPUsername:   deps.Config.SMTPUsername,
		SMTPPassword:   deps.Config.SMTPPassword,
		GoogleClientID: deps.Config.GoogleClientID,
		AppleClientID:  deps.Config.AppleClientID,
		Notifier:       deps.OTPNotifier,
	}
}

func buildResetHandler(
	pool *pgxpool.Pool,
	clk clock.Clock,
	svcs assembledServices,
	adminOnly bool,
) func(ctx context.Context) {
	return func(ctx context.Context) {
		if pool != nil {
			seedSelectedMode(ctx, pool, clk, adminOnly)
		}
		if adminOnly {
			resetServicesAdminOnly(ctx, svcs)
			return
		}
		svcs.events.Reset(ctx)
		svcs.identity.Reset(ctx)
		svcs.catalog.Reset(ctx)
		svcs.quotes.Reset(ctx)
		svcs.payments.Reset(ctx)
		svcs.operations.Reset(ctx)
	}
}
