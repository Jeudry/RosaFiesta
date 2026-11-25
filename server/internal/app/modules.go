package app

import (
	"context"
	"errors"
	"log/slog"
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
	defaultHash := identityapp.CanonicalSeedPasswordHash()
	return []seedUser{
		{
			sessionKey: "rf-token-maria",
			user: identitydomain.User{
				ID:           uuid.MustParse("22222222-2222-2222-2222-222222222222"),
				Username:     "maria",
				FullName:     "María (Dueña)",
				Email:        "maria@rosafiesta.do",
				Phone:        "809-555-0100",
				BirthDate:    "12/04/1990",
				Active:       true,
				Role:         identitydomain.RoleAdmin,
				Permissions:  []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "finanzas", "reembolsos", "reportes", "ajustes"},
				ActiveView:   identitydomain.ViewAdmin,
				CreatedAt:    now,
				PasswordHash: defaultHash,
			},
		},
		{
			sessionKey: "rf-token-yuli",
			user: identitydomain.User{
				ID:           uuid.MustParse("33333333-3333-3333-3333-333333333333"),
				Username:     "yuli",
				FullName:     "Yuli",
				Email:        "yuli@rosafiesta.do",
				Phone:        "809-555-0102",
				BirthDate:    "22/09/1996",
				Active:       true,
				Role:         identitydomain.RoleStaff,
				Permissions:  []string{"decoracion", "tareas"},
				ActiveView:   identitydomain.ViewAdmin,
				CreatedAt:    now,
				PasswordHash: defaultHash,
			},
		},
	}
}

func canonicalSeedUsers(now time.Time) []seedUser {
	defaultHash := identityapp.CanonicalSeedPasswordHash()
	return []seedUser{
		{
			sessionKey: "rf-token-camila",
			user: identitydomain.User{
				ID:           uuid.MustParse("11111111-1111-1111-1111-111111111111"),
				Username:     "camila",
				FullName:     "Camila Rodríguez",
				Email:        "camila@rosafiesta.do",
				Phone:        "809-555-0142",
				BirthDate:    "15/08/1998",
				Active:       true,
				Role:         identitydomain.RoleClient,
				ActiveView:   identitydomain.ViewPersonal,
				CreatedAt:    now,
				PasswordHash: defaultHash,
			},
		},
		{
			sessionKey: "rf-token-maria",
			user: identitydomain.User{
				ID:           uuid.MustParse("22222222-2222-2222-2222-222222222222"),
				Username:     "maria",
				FullName:     "María (Dueña)",
				Email:        "maria@rosafiesta.do",
				Phone:        "809-555-0100",
				BirthDate:    "12/04/1990",
				Active:       true,
				Role:         identitydomain.RoleAdmin,
				Permissions:  []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "finanzas", "reembolsos", "reportes", "ajustes"},
				ActiveView:   identitydomain.ViewAdmin,
				CreatedAt:    now,
				PasswordHash: defaultHash,
			},
		},
		{
			sessionKey: "rf-token-yuli",
			user: identitydomain.User{
				ID:           uuid.MustParse("33333333-3333-3333-3333-333333333333"),
				Username:     "yuli",
				FullName:     "Yuli",
				Email:        "yuli@rosafiesta.do",
				Phone:        "809-555-0102",
				BirthDate:    "22/09/1996",
				Active:       true,
				Role:         identitydomain.RoleStaff,
				Permissions:  []string{"decoracion", "tareas"},
				ActiveView:   identitydomain.ViewAdmin,
				CreatedAt:    now,
				PasswordHash: defaultHash,
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
	mods, _ := BuildModules(deps)
	return mods
}

// BuildModules ensambla todos los módulos HTTP del sistema validando dependencias críticas de producción.
func BuildModules(deps Dependencies) ([]httpapi.Module, error) {
	base, svcs, clk, err := coreModules(deps)
	if err != nil {
		return nil, err
	}
	onReset := buildResetHandler(deps.Pool, clk, svcs, deps.AdminOnlySeed)
	return append(base, devModules(onReset)...), nil
}

func coreModules(deps Dependencies) ([]httpapi.Module, assembledServices, clock.Clock, error) {
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
	svcs, err := wireServicesChecked(deps, clk)
	if err != nil {
		return nil, assembledServices{}, nil, err
	}
	if deps.Config.Env == config.EnvProduction {
		if bootErr := ensureProductionAdmin(context.Background(), svcs.identity, deps.Config); bootErr != nil {
			return nil, assembledServices{}, nil, bootErr
		}
	}
	var readiness health.ReadinessChecker
	if deps.Pool != nil {
		readiness = deps.Pool
	}
	base := []httpapi.Module{
		health.Module{
			Readiness: readiness,
			Checks: []health.NamedCheck{
				{Name: "storage", Check: svcs.media.CheckHealth},
			},
		},
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
	return base, svcs, clk, nil
}

func ensureProductionAdmin(ctx context.Context, identitySvc *identityapp.Service, cfg config.Config) error {
	created, err := identitySvc.EnsureBootstrapAdmin(
		ctx,
		cfg.BootstrapAdminEmail,
		cfg.BootstrapAdminPassword,
		cfg.BootstrapAdminName,
	)
	if err == nil {
		if created {
			slog.Info("production_bootstrap_admin_created", slog.String("email", cfg.BootstrapAdminEmail))
		}
		return nil
	}
	if errors.Is(err, identityapp.ErrBootstrapAdminMissing) {
		slog.Error(
			"production_bootstrap_admin_missing",
			slog.String("error", err.Error()),
			slog.String("action", "set BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD"),
		)
		return nil
	}
	slog.Error("production_bootstrap_admin_failed", slog.String("error", err.Error()))
	return err
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

func wireServicesChecked(deps Dependencies, clk clock.Clock) (assembledServices, error) {
	broker := realtimeapp.NewBroker(clk.Now).StartPostgresFanout(deps.Context, deps.Pool)
	smtpSender := buildMailSender(deps.Config)
	pushSender := buildPushSender(deps)
	mediaSvc, mediaErr := buildMediaServiceChecked(deps, clk)
	if mediaErr != nil {
		return assembledServices{}, mediaErr
	}

	eventsSvc := eventsapp.NewService().WithClock(clk).WithRealtimePublisher(broker)
	identitySvc := identityapp.NewServiceWithOptions(clk, eventsSvc, buildIdentityOptions(deps, smtpSender))
	catalogSvc := catalogapp.NewService().WithReservationReader(eventsSvc).WithRealtimePublisher(broker)
	stockAdapter := catalogStockAdapter{catalog: catalogSvc}

	quotesSvc := buildQuotesService(deps.Config, clk, catalogSvc, stockAdapter, broker, smtpSender)
	paymentsSvc := buildPaymentsService(deps.Config, clk, eventsSvc, quotesSvc, broker, smtpSender)
	operationsSvc := buildOperationsService(deps.Config, clk, paymentsSvc, stockAdapter, broker)

	connectCrossModuleBridges(stockAdapter, catalogSvc, eventsSvc, quotesSvc, paymentsSvc, operationsSvc, identitySvc, smtpSender, pushSender)

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
	if deps.Config.Env == config.EnvProduction {
		resetServicesEmpty(context.Background(), svcs)
	} else if deps.AdminOnlySeed {
		resetServicesAdminOnly(context.Background(), svcs)
	}
	attachPostgresStores(deps.Pool, clk, svcs)
	svcs.identity.StartMaintenanceLoop(deps.Context, time.Hour)
	NewEventReminderWorker(eventsSvc, identitySvc, operationsSvc, smtpSender, pushSender, clk).Start(deps.Context, 15*time.Minute)
	return svcs, nil
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

func buildMediaServiceChecked(deps Dependencies, clk clock.Clock) (*mediaapp.Service, error) {
	svc := mediaapp.NewService(buildMediaStore(deps.Pool), deps.Config.MediaStorageDir, deps.Config.MediaMaxBytes, clk.Now)
	if !strings.EqualFold(strings.TrimSpace(deps.Config.MediaStorageBackend), "s3") {
		return svc, nil
	}
	s3Cfg := mediaapp.S3Config{
		Endpoint:        deps.Config.S3Endpoint,
		Bucket:          deps.Config.S3Bucket,
		Region:          deps.Config.S3Region,
		AccessKeyID:     deps.Config.S3AccessKeyID,
		SecretAccessKey: deps.Config.S3SecretAccessKey,
		PublicBaseURL:   deps.Config.S3PublicBaseURL,
		MaxBytes:        deps.Config.MediaMaxBytes,
		Clock:           clk.Now,
	}
	s3Svc, err := svc.WithS3Storage(s3Cfg)
	if err != nil {
		slog.Error("media_s3_init_failed", slog.String("error", err.Error()), slog.String("bucket", s3Cfg.Bucket))
		if deps.Config.Env == config.EnvProduction {
			return nil, err
		}
		return svc, nil
	}
	return s3Svc, nil
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
		WithWhatsAppConfig(cfg.WhatsAppPhoneNumberID, cfg.WhatsAppAccessToken, cfg.WhatsAppVerifyToken).
		WithWhatsAppAppSecret(cfg.WhatsAppAppSecret).
		WithRequireWhatsAppSignature(cfg.Env == config.EnvProduction)
}

func connectCrossModuleBridges(
	stockAdapter catalogStockAdapter,
	catalogSvc *catalogapp.Service,
	eventsSvc *eventsapp.Service,
	quotesSvc *quotesapp.Service,
	paymentsSvc *paymentsapp.Service,
	operationsSvc *operationsapp.Service,
	identitySvc *identityapp.Service,
	smtpSender *mailer.SMTPSender,
	pushSender push.Sender,
) {
	quoteAdapter := quoteLifecycleAdapter{events: eventsSvc, operations: operationsSvc, identity: identitySvc, pushSender: pushSender}
	prefAdapter := userPreferencesAdapter{identity: identitySvc, operations: operationsSvc, emailSender: smtpSender, pushSender: pushSender}
	payAdapter := paymentVerifiedAdapter{events: eventsSvc, operations: operationsSvc, identity: identitySvc, pushSender: pushSender}
	settingsAdapter := businessSettingsAdapter{operations: operationsSvc}
	refundAdapter := refundApprovedAdapter{events: eventsSvc, catalog: catalogSvc, operations: operationsSvc, identity: identitySvc, pushSender: pushSender}
	reportAdapter := reportSnapshotAdapter{payments: paymentsSvc, events: eventsSvc, catalog: catalogSvc, identity: identitySvc}

	eventsSvc.
		WithStockManager(stockAdapter).
		WithRefundRecorder(paymentsSvc).
		WithPaymentReminderDispatcher(prefAdapter).
		WithLogisticsProvisioner(eventLogisticsAdapter{operations: operationsSvc})
	quotesSvc.
		WithEventProvisioner(quoteAdapter).
		WithAdminNotifier(quoteAdapter).
		WithQuoteSentNotifier(quoteAdapter).
		WithDepositRateProvider(settingsAdapter).
		WithEmailPreferenceChecker(prefAdapter)
	paymentsSvc.
		WithVerifiedSynchronizer(payAdapter).
		WithPaymentRejectedHook(payAdapter).
		WithEmailPreferenceChecker(prefAdapter).
		WithDamageExpensesReader(damageExpensesAdapter{operations: operationsSvc}).
		WithTaxRateProvider(settingsAdapter).
		WithRefundApprovedHook(refundAdapter)
	operationsSvc.
		WithEventOwnershipChecker(reviewEventAdapter{events: eventsSvc}).
		WithReportSnapshotProvider(reportAdapter).
		WithOfflineDispatcher(offlineDispatcherAdapter{events: eventsSvc, operations: operationsSvc, payments: paymentsSvc})
}

func attachPostgresStores(pool *pgxpool.Pool, clk clock.Clock, svcs assembledServices) {
	svcs.events.WithStore(eventspg.NewRepository(pool))
	svcs.identity.WithStore(identitypg.NewRepository(pool).WithClock(clk))
	svcs.catalog.WithStore(catalogpg.NewRepository(pool))
	svcs.quotes.WithStore(quotespg.NewRepository(pool))
	svcs.payments.WithStore(paymentspg.NewRepository(pool))
	svcs.operations.WithStore(operationspg.NewRepository(pool))
}

func resetServicesEmpty(ctx context.Context, svcs assembledServices) {
	svcs.events.ResetEmpty(ctx)
	svcs.identity.ResetEmpty(ctx)
	svcs.catalog.ResetEmpty(ctx)
	svcs.quotes.ResetEmpty(ctx)
	svcs.payments.ResetEmpty(ctx)
	svcs.operations.ResetEmpty(ctx)
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
