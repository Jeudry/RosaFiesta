// Package httpapi expone los endpoints HTTP de cotizaciones, contratos y Rosa IA.
package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

// Module conecta el servicio de cotizaciones al enrutador Huma.
type Module struct {
	Service *app.Service
}

// QuotesListInput filtra cotizaciones por estado.
type QuotesListInput struct {
	Status string `query:"status" doc:"Estado opcional (pending, sent, approved, signed, declined)"`
}

// QuotesListOutput devuelve el listado de cotizaciones.
type QuotesListOutput struct {
	Body struct {
		Quotes []domain.Quote `json:"quotes"`
	}
}

// QuoteByIDInput recibe el ID de la cotización.
type QuoteByIDInput struct {
	ID string `path:"id" doc:"ID de la cotización"`
}

// QuoteOutput envuelve una cotización individual.
type QuoteOutput struct {
	Body struct {
		Quote domain.Quote `json:"quote"`
	}
}

// CreateQuoteInput recibe los datos para emitir una cotización nueva.
type CreateQuoteInput struct {
	Body struct {
		EventTitle     string             `json:"eventTitle" minLength:"2"`
		ClientName     string             `json:"clientName" minLength:"2"`
		EventDateLabel string             `json:"eventDateLabel" minLength:"2"`
		Venue          string             `json:"venue" minLength:"2"`
		Guests         int                `json:"guests" minimum:"1"`
		Lines          []domain.QuoteLine `json:"lines"`
		DeliveryFeeRD  int                `json:"deliveryFeeRD" minimum:"0"`
		DiscountRD     int                `json:"discountRD" minimum:"0"`
	}
}

// SignContractInput recibe la firma digital del cliente (s28).
type SignContractInput struct {
	ID   string `path:"id" doc:"ID de la cotización"`
	Body struct {
		SignedByName string `json:"signedByName" minLength:"2"`
		SignatureSVG string `json:"signatureSvg" minLength:"5"`
	}
}

// DeclineQuoteInput recibe el motivo de rechazo (A08).
type DeclineQuoteInput struct {
	ID   string `path:"id" doc:"ID de la cotización"`
	Body struct {
		Reason string `json:"reason" minLength:"2"`
	}
}

// RosaProposalInput recibe parámetros para el asistente conversacional Rosa IA.
type RosaProposalInput struct {
	Body struct {
		EventType string `json:"eventType"`
		Guests    int    `json:"guests" minimum:"10"`
		BudgetRD  int    `json:"budgetRD" minimum:"200"`
	}
}

// RosaProposalOutput devuelve la propuesta y moodboard de Rosa IA.
type RosaProposalOutput struct {
	Body struct {
		Proposal domain.RosaMoodboardProposal `json:"proposal"`
	}
}

// Register registra los endpoints HTTP del módulo de cotizaciones.
func (m Module) Register(api huma.API) {
	m.registerReadAndCreateRoutes(api)
	m.registerDecisionAndAssistantRoutes(api)
}

func (m Module) registerReadAndCreateRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "quotes-list",
		Method:      http.MethodGet,
		Path:        "/v1/quotes",
		Summary:     "Listar cotizaciones y solicitudes recibidas",
		Tags:        []string{"Quotes"},
	}, func(_ context.Context, in *QuotesListInput) (*QuotesListOutput, error) {
		out := &QuotesListOutput{}
		out.Body.Quotes = m.Service.ListQuotes(in.Status)
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "quotes-get-by-id",
		Method:      http.MethodGet,
		Path:        "/v1/quotes/{id}",
		Summary:     "Obtener detalle de cotización y contrato",
		Tags:        []string{"Quotes"},
	}, func(_ context.Context, in *QuoteByIDInput) (*QuoteOutput, error) {
		q, err := m.Service.GetQuote(in.ID)
		if err != nil {
			return nil, huma.Error404NotFound(err.Error())
		}
		out := &QuoteOutput{}
		out.Body.Quote = q
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "quotes-create",
		Method:      http.MethodPost,
		Path:        "/v1/quotes",
		Summary:     "Crear nueva cotización con cálculo automático de 30% de anticipo",
		Tags:        []string{"Quotes"},
	}, func(_ context.Context, in *CreateQuoteInput) (*QuoteOutput, error) {
		q, err := m.Service.CreateQuote(
			in.Body.EventTitle,
			in.Body.ClientName,
			in.Body.EventDateLabel,
			in.Body.Venue,
			in.Body.Guests,
			in.Body.Lines,
			in.Body.DeliveryFeeRD,
			in.Body.DiscountRD,
		)
		if err != nil {
			return nil, huma.Error400BadRequest(err.Error())
		}
		out := &QuoteOutput{}
		out.Body.Quote = q
		return out, nil
	})
}

func (m Module) registerDecisionAndAssistantRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "quotes-sign-contract",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/{id}/sign",
		Summary:     "Firmar contrato digital de alquiler (s28)",
		Tags:        []string{"Quotes"},
	}, func(_ context.Context, in *SignContractInput) (*QuoteOutput, error) {
		q, err := m.Service.SignContract(in.ID, in.Body.SignedByName, in.Body.SignatureSVG)
		if err != nil {
			return nil, huma.Error400BadRequest(err.Error())
		}
		out := &QuoteOutput{}
		out.Body.Quote = q
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "quotes-decline",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/{id}/decline",
		Summary:     "Declinar solicitud con respuesta amable (A08)",
		Tags:        []string{"Quotes"},
	}, func(_ context.Context, in *DeclineQuoteInput) (*QuoteOutput, error) {
		q, err := m.Service.DeclineQuote(in.ID, in.Body.Reason)
		if err != nil {
			return nil, huma.Error404NotFound(err.Error())
		}
		out := &QuoteOutput{}
		out.Body.Quote = q
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "quotes-rosa-assistant-proposal",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/rosa-proposal",
		Summary:     "Generar propuesta y moodboard con el asistente Rosa IA (s40-s48, A16-A18)",
		Tags:        []string{"Quotes"},
	}, func(_ context.Context, in *RosaProposalInput) (*RosaProposalOutput, error) {
		out := &RosaProposalOutput{}
		out.Body.Proposal = m.Service.GenerateRosaProposal(in.Body.EventType, in.Body.Guests, in.Body.BudgetRD)
		return out, nil
	})
}
