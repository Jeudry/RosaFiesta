package httpapi

import (
	"errors"
	"net/http"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/app"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/domain"
)

type QuotesListInput struct {
	Status string `query:"status" doc:"Optional quote status filter (pending, sent, approved, signed, declined)."`
}

type QuotesListOutput struct {
	Body struct {
		Quotes []domain.Quote `json:"quotes"`
	}
}

type QuoteByIDInput struct {
	ID string `path:"id" doc:"Quote identifier."`
}

type QuoteOutput struct {
	Body struct {
		Quote domain.Quote `json:"quote"`
	}
}

func newQuoteOutput(quote domain.Quote) *QuoteOutput {
	out := &QuoteOutput{}
	out.Body.Quote = quote
	return out
}

type CreateQuoteInput struct {
	Body struct {
		EventID        string             `json:"eventId,omitempty"`
		EventTitle     string             `json:"eventTitle" minLength:"2"`
		ClientName     string             `json:"clientName" minLength:"2"`
		ClientEmail    string             `json:"clientEmail,omitempty"`
		EventDateLabel string             `json:"eventDateLabel" minLength:"2"`
		Venue          string             `json:"venue" minLength:"2"`
		Guests         int                `json:"guests" minimum:"1"`
		Lines          []domain.QuoteLine `json:"lines"`
		DeliveryFeeRD  int                `json:"deliveryFeeRD" minimum:"0"`
		DiscountRD     int                `json:"discountRD" minimum:"0"`
	}
}

func (in *CreateQuoteInput) ToCommand() app.CreateQuoteCommand {
	return app.CreateQuoteCommand{
		EventID:        in.Body.EventID,
		EventTitle:     in.Body.EventTitle,
		ClientName:     in.Body.ClientName,
		ClientEmail:    in.Body.ClientEmail,
		EventDateLabel: in.Body.EventDateLabel,
		Venue:          in.Body.Venue,
		Guests:         in.Body.Guests,
		Lines:          in.Body.Lines,
		DeliveryFeeRD:  in.Body.DeliveryFeeRD,
		DiscountRD:     in.Body.DiscountRD,
	}
}

type SignContractInput struct {
	ID   string `path:"id" doc:"Quote identifier."`
	Body struct {
		SignedByName string `json:"signedByName" minLength:"2"`
		SignatureSVG string `json:"signatureSvg" minLength:"5"`
	}
}

type DeclineQuoteInput struct {
	ID   string `path:"id" doc:"Quote identifier."`
	Body struct {
		Reason string `json:"reason" minLength:"2"`
	}
}

type RosaProposalInput struct {
	Body struct {
		EventType          string   `json:"eventType"`
		Guests             int      `json:"guests" minimum:"1"`
		BudgetRD           int      `json:"budgetRD" minimum:"0"`
		Prompt             string   `json:"prompt,omitempty"`
		ExcludedCategories []string `json:"excludedCategories,omitempty"`
	}
}

func (in *RosaProposalInput) ToRequest() domain.RosaProposalRequest {
	return domain.RosaProposalRequest{
		EventType:          in.Body.EventType,
		Guests:             in.Body.Guests,
		BudgetRD:           in.Body.BudgetRD,
		Prompt:             in.Body.Prompt,
		ExcludedCategories: in.Body.ExcludedCategories,
	}
}

type RosaProposalOutput struct {
	Body struct {
		Proposal domain.RosaMoodboardProposal `json:"proposal"`
	}
}

type RosaChatInput struct {
	Body struct {
		Message            string   `json:"message" minLength:"1"`
		EventType          string   `json:"eventType,omitempty"`
		Guests             int      `json:"guests,omitempty"`
		BudgetRD           int      `json:"budgetRD,omitempty"`
		ExcludedCategories []string `json:"excludedCategories,omitempty"`
	}
}

func (in *RosaChatInput) ToRequest() domain.RosaProposalRequest {
	return domain.RosaProposalRequest{
		EventType:          in.Body.EventType,
		Guests:             in.Body.Guests,
		BudgetRD:           in.Body.BudgetRD,
		Prompt:             in.Body.Message,
		ExcludedCategories: in.Body.ExcludedCategories,
	}
}

type RosaChatOutput struct {
	Body domain.RosaChatResponse
}

type RosaTranscribeInput struct {
	Body struct {
		AudioBase64 string `json:"audioBase64" minLength:"4"`
		MimeType    string `json:"mimeType,omitempty"`
		Locale      string `json:"locale,omitempty"`
		PromptHint  string `json:"promptHint,omitempty"`
	}
}

func (in *RosaTranscribeInput) ToCommand() app.TranscribeCommand {
	return app.TranscribeCommand{
		AudioBase64: in.Body.AudioBase64,
		MimeType:    in.Body.MimeType,
		Locale:      in.Body.Locale,
		PromptHint:  in.Body.PromptHint,
	}
}

type RosaTranscribeOutput struct {
	Body domain.TranscriptionResult
}

func mapQuoteError(err error, endpoint, defaultCode, policy string, defaultStatus int) *platformhttp.APIError {
	if errors.Is(err, domain.ErrQuoteForbidden) {
		return platformhttp.NewAPIError(
			http.StatusForbidden,
			"FORBIDDEN_QUOTE_ACCESS",
			err.Error(),
			endpoint,
			"ClientQuoteIsolation",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrQuoteNotFound) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			"QUOTE_NOT_FOUND",
			err.Error(),
			endpoint,
			"QuoteMustExist",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrInvalidAudioPayload) {
		return platformhttp.NewAPIError(
			http.StatusBadRequest,
			"ROSA_AUDIO_INVALID",
			err.Error(),
			endpoint,
			"ValidateBase64Audio",
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
