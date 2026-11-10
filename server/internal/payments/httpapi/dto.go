package httpapi

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"github.com/Jeudry/rosafiesta/server/internal/payments/app"
	"github.com/Jeudry/rosafiesta/server/internal/payments/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type PaymentsListInput struct {
	EventID string `query:"eventId" doc:"Optional event identifier filter."`
}

type PaymentsListOutput struct {
	Body struct {
		Payments []domain.Payment `json:"payments"`
	}
}

type FinanceSummaryOutput struct {
	Body struct {
		Summary domain.FinanceSummary `json:"summary"`
	}
}

type RegisterPaymentInput struct {
	Body struct {
		EventID       string `json:"eventId" minLength:"2"`
		EventTitle    string `json:"eventTitle" minLength:"2"`
		ClientEmail   string `json:"clientEmail,omitempty"`
		ClientName    string `json:"clientName" minLength:"2"`
		AmountRD      int    `json:"amountRD" minimum:"1"`
		Method        string `json:"method" minLength:"2"`
		Kind          string `json:"kind"`
		BankName      string `json:"bankName,omitempty"`
		ReferenceCode string `json:"referenceCode"`
	}
}

func (in *RegisterPaymentInput) ToCommand() app.RegisterPaymentCommand {
	return app.RegisterPaymentCommand{
		EventID:       in.Body.EventID,
		EventTitle:    in.Body.EventTitle,
		ClientEmail:   in.Body.ClientEmail,
		ClientName:    in.Body.ClientName,
		AmountRD:      in.Body.AmountRD,
		Method:        in.Body.Method,
		Kind:          in.Body.Kind,
		BankName:      in.Body.BankName,
		ReferenceCode: in.Body.ReferenceCode,
	}
}

type PaymentOutput struct {
	Body struct {
		Payment domain.Payment `json:"payment"`
	}
}

type RefundsListOutput struct {
	Body struct {
		Refunds []domain.Refund `json:"refunds"`
	}
}

type ApproveRefundInput struct {
	ID      string `path:"id" doc:"Refund identifier."`
	RawBody []byte
}

func (in *ApproveRefundInput) Approver(defaultName string) string {
	trimmedRaw := strings.TrimSpace(string(in.RawBody))
	if trimmedRaw != "" {
		var payload struct {
			ApprovedBy string `json:"approvedBy"`
		}
		if err := json.Unmarshal(in.RawBody, &payload); err == nil {
			clean := strings.TrimSpace(payload.ApprovedBy)
			if clean != "" {
				return clean
			}
		}
	}
	return strings.TrimSpace(defaultName)
}

type RefundOutput struct {
	Body struct {
		Refund domain.Refund `json:"refund"`
	}
}

func mapPaymentError(err error, endpoint, defaultCode, policy string, defaultStatus int) *platformhttp.APIError {
	if errors.Is(err, domain.ErrPaymentForbidden) {
		return platformhttp.NewAPIError(
			http.StatusForbidden,
			"FORBIDDEN_PAYMENT_ACCESS",
			err.Error(),
			endpoint,
			"ClientPaymentIsolation",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrRefundNotFound) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			"REFUND_NOT_FOUND",
			err.Error(),
			endpoint,
			"RefundMustExist",
			err.Error(),
		)
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
