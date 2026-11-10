package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type Module struct {
	Service *app.Service
	Auth    platformhttp.PrincipalResolver
}

func (m Module) Register(api huma.API) {
	m.registerPaymentRoutes(api)
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
	approver := in.Approver(caller.FullName)
	refund, err := m.Service.ApproveRefund(ctx, in.ID, approver)
	if err != nil {
		return nil, mapPaymentError(err, "POST /v1/refunds/"+in.ID+"/approve", "REFUND_APPROVE_FAILED", "ApproveRefund", http.StatusBadRequest)
	}
	out := &RefundOutput{}
	out.Body.Refund = refund
	return out, nil
}
