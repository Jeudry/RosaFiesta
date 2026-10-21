// Package app ensambla todos los módulos de dominio del monolito modular RosaFiesta.
package app

import (
	catalogapp "github.com/Jeudry/rosafiesta/server/internal/catalog/app"
	cataloghttp "github.com/Jeudry/rosafiesta/server/internal/catalog/httpapi"
	eventsapp "github.com/Jeudry/rosafiesta/server/internal/events/app"
	eventshttp "github.com/Jeudry/rosafiesta/server/internal/events/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/health"
	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	identityhttp "github.com/Jeudry/rosafiesta/server/internal/identity/httpapi"
	operationsapp "github.com/Jeudry/rosafiesta/server/internal/operations/app"
	operationshttp "github.com/Jeudry/rosafiesta/server/internal/operations/httpapi"
	paymentsapp "github.com/Jeudry/rosafiesta/server/internal/payments/app"
	paymentshttp "github.com/Jeudry/rosafiesta/server/internal/payments/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
	"github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	quotesapp "github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	quoteshttp "github.com/Jeudry/rosafiesta/server/internal/quotes/httpapi"
)

// Dependencies agrupa las dependencias de infraestructura compartidas por los módulos.
type Dependencies struct {
	Clock clock.Clock
}

// Modules devuelve todos los módulos HTTP del monolito listos para registrarse en el router.
func Modules(deps Dependencies) []httpapi.Module {
	clk := deps.Clock
	if clk == nil {
		clk = clock.System{}
	}
	eventsSvc := eventsapp.NewService()
	identitySvc := identityapp.NewService(clk, eventsSvc)
	catalogSvc := catalogapp.NewService()
	quotesSvc := quotesapp.NewService()
	paymentsSvc := paymentsapp.NewService()
	operationsSvc := operationsapp.NewService()

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
		*eventsSvc = *eventsapp.NewService()
		*identitySvc = *identityapp.NewService(clk, eventsSvc)
		*catalogSvc = *catalogapp.NewService()
		*quotesSvc = *quotesapp.NewService()
		*paymentsSvc = *paymentsapp.NewService()
		*operationsSvc = *operationsapp.NewService()
	})...)
}
