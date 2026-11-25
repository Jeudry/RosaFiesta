// Package httpapi expone los endpoints HTTP del módulo de pagos, NCF y reembolsos.
package httpapi

import (
	"errors"
	"net/http"
	"strings"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// PaymentsListInput filtra el listado de pagos por evento opcional.
type PaymentsListInput struct {
	EventID string `query:"eventId" doc:"Optional event identifier filter."`
}

// PaymentsListOutput devuelve el listado de pagos visibles para el llamador.
type PaymentsListOutput struct {
	Body struct {
		Payments []domain.Payment `json:"payments"`
	}
}

// FinanceSummaryOutput devuelve las métricas financieras agregadas del tablero.
type FinanceSummaryOutput struct {
	Body struct {
		Summary domain.FinanceSummary `json:"summary"`
	}
}

// AnalyticsInput recibe el filtro temporal de analítica financiera.
type AnalyticsInput struct {
	Range string `query:"range" doc:"Range filter: 7d, 30d, 90d, 365d, or all."`
}

// AnalyticsOutput devuelve la analítica real agregada de pagos y eventos.
type AnalyticsOutput struct {
	Body struct {
		Analytics domain.PaymentsAnalytics `json:"analytics"`
	}
}

// ConfirmCardSessionInput identifica la sesión de Stripe Checkout a confirmar.
type ConfirmCardSessionInput struct {
	SessionID string `path:"sessionId" minLength:"4" doc:"Stripe checkout session identifier."`
}

// RegisterPaymentInput representa el cuerpo para registrar un pago manual o electrónico.
type RegisterPaymentInput struct {
	Body struct {
		EventID            string `json:"eventId" minLength:"2"`
		EventTitle         string `json:"eventTitle" minLength:"2"`
		ClientEmail        string `json:"clientEmail,omitempty"`
		ClientName         string `json:"clientName" minLength:"2"`
		AmountRD           int    `json:"amountRD" minimum:"1"`
		Method             string `json:"method" minLength:"2"`
		Kind               string `json:"kind"`
		BankName           string `json:"bankName,omitempty"`
		ReferenceCode      string `json:"referenceCode"`
		VoucherURL         string `json:"voucherUrl,omitempty"`
		ReferenceNumber    string `json:"referenceNumber,omitempty"`
		NCFType            string `json:"ncfType,omitempty"`
		ClientRNC          string `json:"clientRnc,omitempty"`
		SecurityDepositDOP int    `json:"securityDepositDop,omitempty" minimum:"0"`
		ContactPhone       string `json:"contactPhone,omitempty"`
	}
}

// ToCommand convierte la entrada HTTP al comando de aplicación.
func (in *RegisterPaymentInput) ToCommand() app.RegisterPaymentCommand {
	return app.RegisterPaymentCommand{
		EventID:            in.Body.EventID,
		EventTitle:         in.Body.EventTitle,
		ClientEmail:        in.Body.ClientEmail,
		ClientName:         in.Body.ClientName,
		AmountRD:           in.Body.AmountRD,
		Method:             in.Body.Method,
		Kind:               in.Body.Kind,
		BankName:           in.Body.BankName,
		ReferenceCode:      in.Body.ReferenceCode,
		VoucherURL:         in.Body.VoucherURL,
		ReferenceNumber:    in.Body.ReferenceNumber,
		NCFType:            in.Body.NCFType,
		ClientRNC:          in.Body.ClientRNC,
		SecurityDepositDOP: in.Body.SecurityDepositDOP,
		ContactPhone:       in.Body.ContactPhone,
	}
}

// PaymentOutput envuelve un pago individual en la respuesta HTTP.
type PaymentOutput struct {
	Body struct {
		Payment domain.Payment `json:"payment"`
	}
}

// VerifyPaymentInput recibe el identificador y notas opcionales para verificar un pago.
type VerifyPaymentInput struct {
	ID   string `path:"id" doc:"Payment identifier."`
	Body *struct {
		Notes  string `json:"notes,omitempty"`
		Reason string `json:"reason,omitempty"`
	}
}

// Notes extrae notas o comentario de verificación desde el cuerpo opcional JSON.
func (in *VerifyPaymentInput) Notes() string {
	if in.Body == nil {
		return ""
	}
	trimmed := strings.TrimSpace(in.Body.Notes)
	if trimmed != "" {
		return trimmed
	}
	return strings.TrimSpace(in.Body.Reason)
}

// RejectPaymentInput recibe el identificador y motivo opcional para rechazar un pago.
type RejectPaymentInput struct {
	ID   string `path:"id" doc:"Payment identifier."`
	Body *struct {
		Reason string `json:"reason,omitempty"`
	}
}

// Reason extrae el motivo de rechazo desde el cuerpo opcional JSON.
func (in *RejectPaymentInput) Reason() string {
	if in.Body == nil {
		return ""
	}
	return strings.TrimSpace(in.Body.Reason)
}

// CreateCardSessionInput solicita una sesión de checkout de tarjeta.
type CreateCardSessionInput struct {
	Body struct {
		EventID      string `json:"eventId" minLength:"2"`
		EventTitle   string `json:"eventTitle,omitempty"`
		AmountRD     int    `json:"amountRD" minimum:"1"`
		Kind         string `json:"kind,omitempty"`
		NCFType      string `json:"ncfType,omitempty"`
		ClientRNC    string `json:"clientRnc,omitempty"`
		ContactPhone string `json:"contactPhone,omitempty"`
	}
}

// ToCommand convierte la solicitud HTTP en un comando CreateCardSessionCommand.
func (in *CreateCardSessionInput) ToCommand() app.CreateCardSessionCommand {
	return app.CreateCardSessionCommand{
		EventID:      in.Body.EventID,
		EventTitle:   in.Body.EventTitle,
		AmountRD:     in.Body.AmountRD,
		Kind:         in.Body.Kind,
		NCFType:      in.Body.NCFType,
		ClientRNC:    in.Body.ClientRNC,
		ContactPhone: in.Body.ContactPhone,
	}
}

// CardSessionOutput devuelve el enlace de checkout generado para el cliente.
type CardSessionOutput struct {
	Body struct {
		Session domain.CardSessionResult `json:"session"`
	}
}

// DGII607Input recibe el filtro de período para exportar el reporte fiscal 607.
type DGII607Input struct {
	Period string `query:"period" doc:"Optional period filter (YYYY-MM or month label)."`
}

// DGII607Output contiene el contenido CSV del reporte DGII 607.
type DGII607Output struct {
	Body struct {
		Period string `json:"period,omitempty"`
		CSV    string `json:"csv"`
	}
}

// RefundsListOutput devuelve la lista de reembolsos.
type RefundsListOutput struct {
	Body struct {
		Refunds []domain.Refund `json:"refunds"`
	}
}

// ApproveRefundInput recibe la aprobación de una solicitud de reembolso.
type ApproveRefundInput struct {
	ID   string `path:"id" doc:"Refund identifier."`
	Body *struct {
		ApprovedBy       string `json:"approvedBy,omitempty"`
		Method           string `json:"method,omitempty"`
		ReleaseInventory *bool  `json:"releaseInventory,omitempty"`
		NotifyWhatsApp   *bool  `json:"notifyWhatsapp,omitempty"`
		ClientMessage    string `json:"clientMessage,omitempty"`
	}
}

// ToOptions convierte la entrada HTTP a ApproveRefundOptions.
func (in *ApproveRefundInput) ToOptions(defaultName string) app.ApproveRefundOptions {
	opts := app.ApproveRefundOptions{
		ApproverName: in.Approver(defaultName),
	}
	if in.Body != nil {
		opts.Method = strings.TrimSpace(in.Body.Method)
		opts.ClientMessage = strings.TrimSpace(in.Body.ClientMessage)
		if in.Body.ReleaseInventory != nil {
			opts.ReleaseInventory = *in.Body.ReleaseInventory
		}
		if in.Body.NotifyWhatsApp != nil {
			opts.NotifyWhatsApp = *in.Body.NotifyWhatsApp
		}
	}
	return opts
}

// Approver extrae el nombre de quien aprueba el reembolso desde el cuerpo o principal.
func (in *ApproveRefundInput) Approver(defaultName string) string {
	if in.Body != nil {
		clean := strings.TrimSpace(in.Body.ApprovedBy)
		if clean != "" {
			return clean
		}
	}
	return strings.TrimSpace(defaultName)
}

// RefundOutput envuelve un reembolso en la respuesta HTTP.
type RefundOutput struct {
	Body struct {
		Refund domain.Refund `json:"refund"`
	}
}

type paymentErrorSpec struct {
	target error
	status int
	code   string
	policy string
}

var paymentErrorSpecs = []paymentErrorSpec{
	{domain.ErrPaymentForbidden, http.StatusForbidden, "FORBIDDEN_PAYMENT_ACCESS", "ClientPaymentIsolation"},
	{domain.ErrPaymentNotFound, http.StatusNotFound, "PAYMENT_NOT_FOUND", "PaymentMustExist"},
	{domain.ErrPaymentNotPending, http.StatusConflict, "PAYMENT_NOT_PENDING", "PaymentPendingVerificationRequired"},
	{domain.ErrInvalidFiscalRNC, http.StatusBadRequest, "INVALID_FISCAL_RNC", "DominicanNCFAndRNCValidation"},
	{domain.ErrCardGatewayNotConfigured, http.StatusServiceUnavailable, "CARD_GATEWAY_NOT_CONFIGURED", "StripeGatewayConfiguration"},
	{domain.ErrInvalidWebhookSignature, http.StatusUnauthorized, "INVALID_WEBHOOK_SIGNATURE", "VerifyStripeHMACSignature"},
	{domain.ErrCardSessionNotPaid, http.StatusConflict, "CARD_SESSION_NOT_PAID", "StripeCheckoutPaidRequired"},
	{domain.ErrRefundNotFound, http.StatusNotFound, "REFUND_NOT_FOUND", "RefundMustExist"},
	{domain.ErrRefundAlreadyApproved, http.StatusConflict, "REFUND_ALREADY_APPROVED", "RefundIdempotentApprovalGuard"},
	{domain.ErrCardCheckoutRequired, http.StatusForbidden, "CARD_CHECKOUT_REQUIRED", "RequireStripeCheckoutForClientCard"},
	{domain.ErrClientCashRestricted, http.StatusForbidden, "CLIENT_CASH_PAYMENT_RESTRICTED", "RequireAdminForDirectCash"},
	{domain.ErrInvalidPaymentCurrency, http.StatusBadRequest, "INVALID_PAYMENT_CURRENCY", "RequireDOPCurrency"},
	{domain.ErrNCFSequenceExpired, http.StatusConflict, "NCF_SEQUENCE_EXPIRED", "ValidNCFSequenceRequired"},
}

func mapPaymentError(err error, endpoint, defaultCode, policy string, defaultStatus int) *platformhttp.APIError {
	for _, spec := range paymentErrorSpecs {
		if errors.Is(err, spec.target) {
			return platformhttp.NewAPIError(spec.status, spec.code, err.Error(), endpoint, spec.policy, err.Error())
		}
	}
	return platformhttp.NewAPIError(
		defaultStatus,
		defaultCode,
		err.Error(),
		endpoint,
		policy,
		err.Error(),
	)
}
