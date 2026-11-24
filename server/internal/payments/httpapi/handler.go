// Package httpapi registra las rutas REST del módulo de pagos, NCF y reembolsos.
package httpapi

import (
	"context"
	"encoding/json"
	"io"
	"net/http"

	"github.com/danielgtaylor/huma/v2"
	"github.com/go-chi/chi/v5"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// Module enlaza el servicio de pagos y verificación con el enrutador Huma.
type Module struct {
	Service *app.Service
	Auth    platformhttp.PrincipalResolver
}

// RegisterRoutes registra webhooks crudos que requieren validar el payload binario HMAC.
func (m Module) RegisterRoutes(router chi.Router) {
	router.Post("/v1/payments/webhooks/stripe", m.handleStripeWebhook)
}

// Register registra todas las rutas de pagos, pasarela, reporte fiscal 607 y reembolsos.
func (m Module) Register(api huma.API) {
	m.registerPaymentRoutes(api)
	m.registerFiscalRoutes(api)
	m.registerRefundRoutes(api)
}

func (m Module) authMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)}
}

func (m Module) permissionMiddlewares(api huma.API, permission string) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequirePermission(api, m.Auth, permission)}
}

func (m Module) registerPaymentRoutes(api huma.API) {
	authMW := m.authMiddlewares(api)
	pagosMW := m.permissionMiddlewares(api, "pagos")

	huma.Register(api, huma.Operation{
		OperationID: "payments-finance-summary",
		Method:      http.MethodGet,
		Path:        "/v1/payments/summary",
		Summary:     "Get live monthly financial summary metrics",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: pagosMW,
	}, m.handleGetSummary)

	huma.Register(api, huma.Operation{
		OperationID: "payments-analytics",
		Method:      http.MethodGet,
		Path:        "/v1/payments/analytics",
		Summary:     "Get period-filtered financial and event analytics from live database",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: pagosMW,
	}, m.handleGetAnalytics)

	huma.Register(api, huma.Operation{
		OperationID: "payments-list",
		Method:      http.MethodGet,
		Path:        "/v1/payments",
		Summary:     "List payments and receipts scoped by caller role",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleListPayments)

	huma.Register(api, huma.Operation{
		OperationID: "payments-register",
		Method:      http.MethodPost,
		Path:        "/v1/payments",
		Summary:     "Register a deposit, balance, or replacement payment",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleRegisterPayment)
}

func (m Module) registerFiscalRoutes(api huma.API) {
	authMW := m.authMiddlewares(api)
	finanzasMW := m.permissionMiddlewares(api, "finanzas")

	huma.Register(api, huma.Operation{
		OperationID: "payments-verify-transfer",
		Method:      http.MethodPost,
		Path:        "/v1/payments/{id}/verify",
		Summary:     "Verify a pending transfer payment and issue sequential NCF",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: finanzasMW,
	}, m.handleVerifyPayment)

	huma.Register(api, huma.Operation{
		OperationID: "payments-reject-transfer",
		Method:      http.MethodPost,
		Path:        "/v1/payments/{id}/reject",
		Summary:     "Reject a pending bank transfer payment with reason",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: finanzasMW,
	}, m.handleRejectPayment)

	huma.Register(api, huma.Operation{
		OperationID: "payments-create-card-session",
		Method:      http.MethodPost,
		Path:        "/v1/payments/card-session",
		Summary:     "Create an online card checkout session",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleCreateCardSession)

	huma.Register(api, huma.Operation{
		OperationID: "payments-confirm-card-session",
		Method:      http.MethodPost,
		Path:        "/v1/payments/card-session/{sessionId}/confirm",
		Summary:     "Verify a Stripe checkout session and confirm the payment idempotently",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: authMW,
	}, m.handleConfirmCardSession)

	huma.Register(api, huma.Operation{
		OperationID: "payments-export-dgii-607",
		Method:      http.MethodGet,
		Path:        "/v1/payments/dgii-607",
		Summary:     "Export DGII Form 607 CSV for verified NCF payments",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: finanzasMW,
	}, m.handleExportDGII607)
}

func (m Module) registerRefundRoutes(api huma.API) {
	refundMW := m.permissionMiddlewares(api, "reembolsos")

	huma.Register(api, huma.Operation{
		OperationID: "payments-list-refunds",
		Method:      http.MethodGet,
		Path:        "/v1/refunds",
		Summary:     "List cancellation refund requests",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: refundMW,
	}, m.handleListRefunds)

	huma.Register(api, huma.Operation{
		OperationID: "payments-approve-refund",
		Method:      http.MethodPost,
		Path:        "/v1/refunds/{id}/approve",
		Summary:     "Approve a pending refund request",
		Tags:        []string{"Payments"},
		Security:    platformhttp.Secured(),
		Middlewares: refundMW,
	}, m.handleApproveRefund)
}

func (m Module) handleGetSummary(ctx context.Context, _ *struct{}) (*FinanceSummaryOutput, error) {
	out := &FinanceSummaryOutput{}
	out.Body.Summary = m.Service.GetSummary(ctx)
	return out, nil
}

func (m Module) handleGetAnalytics(ctx context.Context, in *AnalyticsInput) (*AnalyticsOutput, error) {
	out := &AnalyticsOutput{}
	out.Body.Analytics = m.Service.ComputeAnalytics(ctx, in.Range)
	return out, nil
}

func (m Module) handleConfirmCardSession(ctx context.Context, in *ConfirmCardSessionInput) (*PaymentOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	payment, err := m.Service.ConfirmCardSession(ctx, in.SessionID, caller)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/payments/card-session/"+in.SessionID+"/confirm", "CARD_SESSION_CONFIRM_FAILED", "ConfirmCardSession", http.StatusBadRequest)
	}
	out := &PaymentOutput{}
	out.Body.Payment = payment
	return out, nil
}

func (m Module) handleStripeWebhook(writer http.ResponseWriter, req *http.Request) {
	rawBody, readErr := io.ReadAll(io.LimitReader(req.Body, 1<<20))
	if readErr != nil {
		writeWebhookJSON(writer, http.StatusBadRequest, map[string]any{"code": "INVALID_WEBHOOK_BODY"})
		return
	}
	sig := req.Header.Get("Stripe-Signature")
	payment, err := m.Service.HandleStripeWebhook(req.Context(), rawBody, sig)
	if err != nil {
		writeWebhookJSON(writer, http.StatusUnauthorized, map[string]any{"code": "WEBHOOK_VERIFICATION_FAILED", "message": err.Error()})
		return
	}
	writeWebhookJSON(writer, http.StatusOK, map[string]any{"received": true, "paymentId": payment.ID})
}

func writeWebhookJSON(writer http.ResponseWriter, status int, payload any) {
	writer.Header().Set("Content-Type", "application/json")
	writer.WriteHeader(status)
	_ = json.NewEncoder(writer).Encode(payload)
}

func (m Module) handleListPayments(ctx context.Context, in *PaymentsListInput) (*PaymentsListOutput, error) {
	caller, authenticated := platformhttp.CurrentPrincipal(ctx)
	out := &PaymentsListOutput{}
	out.Body.Payments = m.Service.ListPaymentsForCaller(ctx, in.EventID, caller, authenticated)
	return out, nil
}

func (m Module) handleRegisterPayment(ctx context.Context, in *RegisterPaymentInput) (*PaymentOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	payment, err := m.Service.RegisterPaymentForCaller(ctx, cmd, caller)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/payments", "PAYMENT_VALIDATION_FAILED", "RegisterPaymentValidation", http.StatusBadRequest)
	}
	out := &PaymentOutput{}
	out.Body.Payment = payment
	return out, nil
}

func (m Module) handleListRefunds(ctx context.Context, _ *struct{}) (*RefundsListOutput, error) {
	out := &RefundsListOutput{}
	out.Body.Refunds = m.Service.ListRefunds(ctx)
	return out, nil
}

func (m Module) handleApproveRefund(ctx context.Context, in *ApproveRefundInput) (*RefundOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	opts := in.ToOptions(caller.FullName)
	refund, err := m.Service.ApproveRefundWithOptions(ctx, in.ID, opts)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/refunds/"+in.ID+"/approve", "REFUND_APPROVE_FAILED", "ApproveRefund", http.StatusBadRequest)
	}
	out := &RefundOutput{}
	out.Body.Refund = refund
	return out, nil
}

func (m Module) handleVerifyPayment(ctx context.Context, in *VerifyPaymentInput) (*PaymentOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	payment, err := m.Service.VerifyPaymentForCaller(ctx, in.ID, in.Notes(), caller)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/payments/"+in.ID+"/verify", "PAYMENT_VERIFY_FAILED", "VerifyPayment", http.StatusBadRequest)
	}
	out := &PaymentOutput{}
	out.Body.Payment = payment
	return out, nil
}

func (m Module) handleRejectPayment(ctx context.Context, in *RejectPaymentInput) (*PaymentOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	payment, err := m.Service.RejectPaymentForCaller(ctx, in.ID, in.Reason(), caller)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/payments/"+in.ID+"/reject", "PAYMENT_REJECT_FAILED", "RejectPayment", http.StatusBadRequest)
	}
	out := &PaymentOutput{}
	out.Body.Payment = payment
	return out, nil
}

func (m Module) handleCreateCardSession(ctx context.Context, in *CreateCardSessionInput) (*CardSessionOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	session, err := m.Service.CreateCardSession(ctx, in.ToCommand(), caller)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/payments/card-session", "CARD_SESSION_FAILED", "CreateCardSession", http.StatusBadRequest)
	}
	out := &CardSessionOutput{}
	out.Body.Session = session
	return out, nil
}

func (m Module) handleExportDGII607(ctx context.Context, in *DGII607Input) (*DGII607Output, error) {
	csvData, err := m.Service.ExportDGII607CSV(ctx, in.Period)
	if err != nil {
		return nil, mapPaymentError(err, "GET /v1/payments/dgii-607", "DGII_EXPORT_FAILED", "ExportDGII607", http.StatusInternalServerError)
	}
	out := &DGII607Output{}
	out.Body.Period = in.Period
	out.Body.CSV = csvData
	return out, nil
}
