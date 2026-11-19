package app

import (
	"context"
	"strings"
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
	mediaapp "github.com/Jeudry/rosafiesta/server/internal/media/app"
	mediahttp "github.com/Jeudry/rosafiesta/server/internal/media/httpapi"
	mediapg "github.com/Jeudry/rosafiesta/server/internal/media/postgres"
	operationsapp "github.com/Jeudry/rosafiesta/server/internal/operations/app"
	operationshttp "github.com/Jeudry/rosafiesta/server/internal/operations/httpapi"
	operationspg "github.com/Jeudry/rosafiesta/server/internal/operations/postgres"
	paymentsapp "github.com/Jeudry/rosafiesta/server/internal/payments/app"
	paymentshttp "github.com/Jeudry/rosafiesta/server/internal/payments/httpapi"
	paymentspg "github.com/Jeudry/rosafiesta/server/internal/payments/postgres"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/config"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/mailer"
	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
	quotesapp "github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	quotesdomain "github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
	quoteshttp "github.com/Jeudry/rosafiesta/server/internal/quotes/httpapi"
	quotespg "github.com/Jeudry/rosafiesta/server/internal/quotes/postgres"
	realtimeapp "github.com/Jeudry/rosafiesta/server/internal/realtime/app"
	realtimehttp "github.com/Jeudry/rosafiesta/server/internal/realtime/httpapi"
)

type Dependencies struct {
	Context       context.Context
	Pool          *pgxpool.Pool
	Clock         clock.Clock
	Config        config.Config
	OTPNotifier   identityapp.OTPNotifier
	PushSender    push.Sender
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
				Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "finanzas", "reembolsos", "reportes", "ajustes"},
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
				Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "finanzas", "reembolsos", "reportes", "ajustes"},
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
	media      *mediaapp.Service
	broker     *realtimeapp.Broker
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
		identityhttp.Module{
			Service:           svcs.identity,
			AllowedWebOrigins: deps.Config.CORSAllowedOrigins,
		},
		cataloghttp.Module{Service: svcs.catalog, Auth: svcs.identity},
		eventshttp.Module{Service: svcs.events, Auth: svcs.identity},
		quoteshttp.Module{Service: svcs.quotes, Auth: svcs.identity},
		paymentshttp.Module{Service: svcs.payments, Auth: svcs.identity},
		operationshttp.Module{Service: svcs.operations, Auth: svcs.identity},
		mediahttp.Module{Service: svcs.media, Auth: svcs.identity},
		realtimehttp.Module{Broker: svcs.broker, Auth: svcs.identity},
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

func buildMediaStore(pool *pgxpool.Pool) mediaapp.Store {
	if pool != nil {
		return mediapg.NewRepository(pool)
	}
	return mediapg.NewMemoryStore()
}

func wireServices(deps Dependencies, clk clock.Clock) assembledServices {
	broker := realtimeapp.NewBroker(clk.Now).StartPostgresFanout(deps.Context, deps.Pool)
	smtpSender := buildMailSender(deps.Config)
	pushSender := buildPushSender(deps)
	mediaSvc := buildMediaService(deps, clk)

	eventsSvc := eventsapp.NewService().WithClock(clk).WithRealtimePublisher(broker)
	identitySvc := identityapp.NewServiceWithOptions(clk, eventsSvc, buildIdentityOptions(deps, smtpSender))
	catalogSvc := catalogapp.NewService().WithReservationReader(eventsSvc).WithRealtimePublisher(broker)
	stockAdapter := catalogStockAdapter{catalog: catalogSvc}

	quotesSvc := buildQuotesService(deps.Config, clk, catalogSvc, stockAdapter, broker, smtpSender)
	paymentsSvc := buildPaymentsService(deps.Config, clk, eventsSvc, quotesSvc, broker, smtpSender)
	operationsSvc := buildOperationsService(deps.Config, clk, paymentsSvc, stockAdapter, broker)

	connectCrossModuleBridges(stockAdapter, eventsSvc, quotesSvc, paymentsSvc, operationsSvc, identitySvc, smtpSender, pushSender)

	svcs := assembledServices{
		events:     eventsSvc,
		identity:   identitySvc,
		catalog:    catalogSvc,
		quotes:     quotesSvc,
		payments:   paymentsSvc,
		operations: operationsSvc,
		media:      mediaSvc,
		broker:     broker,
	}
	if deps.AdminOnlySeed {
		resetServicesAdminOnly(context.Background(), svcs)
	}
	attachPostgresStores(deps.Pool, clk, svcs)
	svcs.identity.StartMaintenanceLoop(deps.Context, time.Hour)
	return svcs
}

func buildMailSender(cfg config.Config) *mailer.SMTPSender {
	return mailer.NewSMTPSender(mailer.SMTPConfig{
		Host:     cfg.SMTPHost,
		Port:     cfg.SMTPPort,
		From:     cfg.SMTPFrom,
		Username: cfg.SMTPUsername,
		Password: cfg.SMTPPassword,
	})
}

func buildMediaService(deps Dependencies, clk clock.Clock) *mediaapp.Service {
	svc := mediaapp.NewService(buildMediaStore(deps.Pool), deps.Config.MediaStorageDir, deps.Config.MediaMaxBytes, clk.Now)
	if !strings.EqualFold(strings.TrimSpace(deps.Config.MediaStorageBackend), "s3") {
		return svc
	}
	s3Cfg := mediaapp.S3Config{
		Endpoint:        deps.Config.S3Endpoint,
		Bucket:          deps.Config.S3Bucket,
		Region:          deps.Config.S3Region,
		AccessKeyID:     deps.Config.S3AccessKeyID,
		SecretAccessKey: deps.Config.S3SecretAccessKey,
		PublicBaseURL:   deps.Config.S3PublicBaseURL,
		Clock:           clk.Now,
	}
	if s3Svc, err := svc.WithS3Storage(s3Cfg); err == nil {
		return s3Svc
	}
	return svc
}

func buildPushSender(deps Dependencies) push.Sender {
	if deps.PushSender != nil {
		return deps.PushSender
	}
	nowFn := clkOrSystem(deps.Clock)
	var fcmSender push.Sender = push.NewHTTPSender(push.Config{
		EndpointURL: deps.Config.FCMServerURL,
		ServerKey:   deps.Config.FCMServerKey,
	})
	fcmV1 := push.NewFCMV1Sender(push.FCMV1Config{
		ServiceAccountJSON: deps.Config.FCMServiceAccountJSON,
		ProjectID:          deps.Config.FCMProjectID,
		EndpointURL:        deps.Config.FCMServerURL,
		Clock:              nowFn,
	})
	if fcmV1.IsConfigured() {
		fcmSender = fcmV1
	}
	apnsSender := push.NewAPNSSender(push.APNSConfig{
		KeyID:         deps.Config.APNSKeyID,
		TeamID:        deps.Config.APNSTeamID,
		BundleID:      deps.Config.APNSBundleID,
		PrivateKeyPEM: deps.Config.APNSPrivateKey,
		UseSandbox:    deps.Config.APNSUseSandbox,
		Clock:         nowFn,
	})
	webPushSender := push.NewWebPushSender(push.WebPushConfig{
		VAPIDPublicKey:  deps.Config.WebPushVAPIDPublicKey,
		VAPIDPrivateKey: deps.Config.WebPushVAPIDPrivateKey,
		Subject:         deps.Config.WebPushVAPIDSubject,
		Clock:           nowFn,
	})
	return push.NewMultiSender(fcmSender, apnsSender).WithWebPush(webPushSender)
}

func clkOrSystem(clk clock.Clock) func() time.Time {
	if clk == nil {
		return time.Now
	}
	return clk.Now
}

func buildQuotesService(
	cfg config.Config,
	clk clock.Clock,
	catalogSvc *catalogapp.Service,
	stockAdapter catalogStockAdapter,
	broker *realtimeapp.Broker,
	smtpSender *mailer.SMTPSender,
) *quotesapp.Service {
	aiCfg := quotesapp.AIProviderConfig{
		AIProvider:  cfg.AIProvider,
		AIModel:     cfg.AIModel,
		AIAPIKey:    cfg.AIProviderAPIKey,
		STTProvider: cfg.STTProvider,
		STTModel:    cfg.STTModel,
		STTAPIURL:   cfg.STTAPIURL,
		STTAPIKey:   cfg.STTAPIKey,
	}
	return quotesapp.NewService().
		WithClock(clk).
		WithCatalog(catalogSvc).
		WithAIProviderConfig(aiCfg).
		WithStockChecker(stockAdapter).
		WithEmailSender(smtpSender).
		WithRealtimePublisher(broker)
}

func buildPaymentsService(
	cfg config.Config,
	clk clock.Clock,
	eventsSvc *eventsapp.Service,
	quotesSvc *quotesapp.Service,
	broker *realtimeapp.Broker,
	smtpSender *mailer.SMTPSender,
) *paymentsapp.Service {
	return paymentsapp.NewService().
		WithClock(clk).
		WithMetricsReaders(eventsSvc, quotesSvc).
		WithEmailSender(smtpSender).
		WithRealtimePublisher(broker).
		WithStripeSecretKey(cfg.StripeSecretKey).
		WithStripeWebhookSecret(cfg.StripeWebhookSecret).
		WithPublicBaseURL(cfg.PublicAppBaseURL)
}

func buildOperationsService(
	cfg config.Config,
	clk clock.Clock,
	paymentsSvc *paymentsapp.Service,
	stockAdapter catalogStockAdapter,
	broker *realtimeapp.Broker,
) *operationsapp.Service {
	return operationsapp.NewService().
		WithClock(clk).
		WithRevenueProvider(func(ctx context.Context) int {
			return paymentsSvc.GetSummary(ctx).OctRevenueRD
		}).
		WithDamagedStockAdjuster(stockAdapter).
		WithReplacementChargeRecorder(damageChargeAdapter{payments: paymentsSvc}).
		WithRealtimePublisher(broker).
		WithWhatsAppConfig(cfg.WhatsAppPhoneNumberID, cfg.WhatsAppAccessToken, cfg.WhatsAppVerifyToken)
}

func connectCrossModuleBridges(
	stockAdapter catalogStockAdapter,
	eventsSvc *eventsapp.Service,
	quotesSvc *quotesapp.Service,
	paymentsSvc *paymentsapp.Service,
	operationsSvc *operationsapp.Service,
	identitySvc *identityapp.Service,
	smtpSender *mailer.SMTPSender,
	pushSender push.Sender,
) {
	quoteAdapter := quoteLifecycleAdapter{
		events:     eventsSvc,
		operations: operationsSvc,
		identity:   identitySvc,
		pushSender: pushSender,
	}
	prefAdapter := userPreferencesAdapter{
		identity:    identitySvc,
		operations:  operationsSvc,
		emailSender: smtpSender,
		pushSender:  pushSender,
	}
	damageReader := damageExpensesAdapter{operations: operationsSvc}
	eventsSvc.WithStockManager(stockAdapter).WithRefundRecorder(paymentsSvc).WithPaymentReminderDispatcher(prefAdapter)
	quotesSvc.WithEventProvisioner(quoteAdapter).WithAdminNotifier(quoteAdapter)
	paymentsSvc.
		WithVerifiedSynchronizer(paymentVerifiedAdapter{events: eventsSvc, operations: operationsSvc}).
		WithEmailPreferenceChecker(prefAdapter).
		WithDamageExpensesReader(damageReader)
	operationsSvc.WithOfflineDispatcher(offlineDispatcherAdapter{
		events:     eventsSvc,
		operations: operationsSvc,
		payments:   paymentsSvc,
	})
}

func attachPostgresStores(pool *pgxpool.Pool, clk clock.Clock, svcs assembledServices) {
	svcs.events.WithStore(eventspg.NewRepository(pool))
	svcs.identity.WithStore(identitypg.NewRepository(pool).WithClock(clk))
	svcs.catalog.WithStore(catalogpg.NewRepository(pool))
	svcs.quotes.WithStore(quotespg.NewRepository(pool))
	svcs.payments.WithStore(paymentspg.NewRepository(pool))
	svcs.operations.WithStore(operationspg.NewRepository(pool))
}

func resetServicesAdminOnly(ctx context.Context, svcs assembledServices) {
	svcs.events.ResetEmpty(ctx)
	svcs.identity.ResetAdminOnly(ctx)
	svcs.catalog.ResetEmpty(ctx)
	svcs.quotes.ResetEmpty(ctx)
	svcs.payments.ResetEmpty(ctx)
	svcs.operations.ResetEmpty(ctx)
}

func buildIdentityOptions(deps Dependencies, smtpSender *mailer.SMTPSender) identityapp.Options {
	return identityapp.Options{
		Env:                   deps.Config.Env,
		JWTSecret:             deps.Config.JWTSecret,
		SMTPHost:              deps.Config.SMTPHost,
		SMTPPort:              deps.Config.SMTPPort,
		SMTPFrom:              deps.Config.SMTPFrom,
		SMTPUsername:          deps.Config.SMTPUsername,
		SMTPPassword:          deps.Config.SMTPPassword,
		RequireConfiguredSMTP: deps.Config.RequireConfiguredSMTP,
		EmailSender:           smtpSender,
		GoogleClientID:        deps.Config.GoogleClientID,
		GoogleClientSecret:    deps.Config.GoogleClientSecret,
		AppleClientID:         deps.Config.AppleClientID,
		AppleClientSecret:     deps.Config.AppleClientSecret,
		AppleTeamID:           deps.Config.AppleTeamID,
		AppleKeyID:            deps.Config.AppleKeyID,
		ApplePrivateKey:       deps.Config.ApplePrivateKey,
		WebPushVAPIDPublicKey: deps.Config.WebPushVAPIDPublicKey,
		Notifier:              deps.OTPNotifier,
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
