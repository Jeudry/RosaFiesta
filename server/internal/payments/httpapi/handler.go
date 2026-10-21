// Package httpapi expone los endpoints HTTP de pagos, comprobantes y finanzas.
package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
)

// Module conecta el servicio de pagos al enrutador Huma.
type Module struct {
	Service *app.Service
}

// PaymentsListInput filtra pagos por evento.
type PaymentsListInput struct {
	EventID string `query:"eventId" doc:"ID opcional del evento"`
}

// PaymentsListOutput devuelve el listado de pagos.
type PaymentsListOutput struct {
	Body struct {
		Payments []domain.Payment `json:"payments"`
	}
}

// FinanceSummaryOutput devuelve las métricas financieras del mes.
type FinanceSummaryOutput struct {
	Body struct {
		Summary domain.FinanceSummary `json:"summary"`
	}
}

// RegisterPaymentInput recibe el registro de un pago o abono.
type RegisterPaymentInput struct {
	Body struct {
		EventID       string               `json:"eventId" minLength:"2"`
		EventTitle    string               `json:"eventTitle" minLength:"2"`
		ClientName    string               `json:"clientName" minLength:"2"`
		AmountRD      int                  `json:"amountRD" minimum:"1"`
		Method        domain.PaymentMethod `json:"method"`
		Kind          domain.PaymentKind   `json:"kind"`
		ReferenceCode string               `json:"referenceCode"`
	}
}

// PaymentOutput envuelve un pago individual.
type PaymentOutput struct {
	Body struct {
		Payment domain.Payment `json:"payment"`
	}
}

// Register registra las rutas HTTP del módulo de pagos.
func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "payments-finance-summary",
		Method:      http.MethodGet,
		Path:        "/v1/payments/summary",
		Summary:     "Obtener resumen financiero del mes (s69, A20)",
		Tags:        []string{"Payments"},
	}, func(_ context.Context, _ *struct{}) (*FinanceSummaryOutput, error) {
		out := &FinanceSummaryOutput{}
		out.Body.Summary = m.Service.GetSummary()
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "payments-list",
		Method:      http.MethodGet,
		Path:        "/v1/payments",
		Summary:     "Listar pagos y comprobantes recibidos",
		Tags:        []string{"Payments"},
	}, func(_ context.Context, in *PaymentsListInput) (*PaymentsListOutput, error) {
		out := &PaymentsListOutput{}
		out.Body.Payments = m.Service.ListPayments(in.EventID)
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "payments-register",
		Method:      http.MethodPost,
		Path:        "/v1/payments",
		Summary:     "Registrar anticipo o saldo pendiente (s30, s31, s38, s70)",
		Tags:        []string{"Payments"},
	}, func(_ context.Context, in *RegisterPaymentInput) (*PaymentOutput, error) {
		p, err := m.Service.RegisterPayment(
			in.Body.EventID,
			in.Body.EventTitle,
			in.Body.ClientName,
			in.Body.AmountRD,
			in.Body.Method,
			in.Body.Kind,
			in.Body.ReferenceCode,
		)
		if err != nil {
			return nil, huma.Error400BadRequest(err.Error())
		}
		out := &PaymentOutput{}
		out.Body.Payment = p
		return out, nil
	})
}
