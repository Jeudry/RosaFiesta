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
		EventID         string             `json:"eventId,omitempty"`
		EventTitle      string             `json:"eventTitle" minLength:"2"`
		ClientName      string             `json:"clientName" minLength:"2"`
		ClientEmail     string             `json:"clientEmail,omitempty"`
		EventDateLabel  string             `json:"eventDateLabel" minLength:"2"`
		EventDateISO    string             `json:"eventDateIso,omitempty"`
		PackageID       string             `json:"packageId,omitempty"`
		Venue           string             `json:"venue" minLength:"2"`
		Guests          int                `json:"guests" minimum:"1"`
		Lines           []domain.QuoteLine `json:"lines"`
		DeliveryFeeRD   int                `json:"deliveryFeeRD" minimum:"0"`
		DiscountRD      int                `json:"discountRD" minimum:"0"`
		Notes           string             `json:"notes,omitempty"`
		SendImmediately bool               `json:"sendImmediately,omitempty"`
	}
}

func (in *CreateQuoteInput) ToCommand() app.CreateQuoteCommand {
	return app.CreateQuoteCommand{
		EventID:         in.Body.EventID,
		EventTitle:      in.Body.EventTitle,
		ClientName:      in.Body.ClientName,
		ClientEmail:     in.Body.ClientEmail,
		EventDateLabel:  in.Body.EventDateLabel,
		EventDateISO:    in.Body.EventDateISO,
		PackageID:       in.Body.PackageID,
		Venue:           in.Body.Venue,
		Guests:          in.Body.Guests,
		Lines:           in.Body.Lines,
		DeliveryFeeRD:   in.Body.DeliveryFeeRD,
		DiscountRD:      in.Body.DiscountRD,
		Notes:           in.Body.Notes,
		SendImmediately: in.Body.SendImmediately,
	}
}

type SendQuoteInput struct {
	ID   string `path:"id" doc:"Quote identifier."`
	Body struct {
		EventTitle     string             `json:"eventTitle,omitempty"`
		Venue          string             `json:"venue,omitempty"`
		EventDateLabel string             `json:"eventDateLabel,omitempty"`
		EventDateISO   string             `json:"eventDateIso,omitempty"`
		Guests         int                `json:"guests,omitempty"`
		Lines          []domain.QuoteLine `json:"lines,omitempty"`
		DeliveryFeeRD  *int               `json:"deliveryFeeRD,omitempty"`
		DiscountRD     *int               `json:"discountRD,omitempty"`
		Notes          string             `json:"notes,omitempty"`
	}
}

func (in *SendQuoteInput) ToCommand() app.EnrichAndSendQuoteCommand {
	return app.EnrichAndSendQuoteCommand{
		EventTitle:     in.Body.EventTitle,
		Venue:          in.Body.Venue,
		EventDateLabel: in.Body.EventDateLabel,
		EventDateISO:   in.Body.EventDateISO,
		Guests:         in.Body.Guests,
		Lines:          in.Body.Lines,
		DeliveryFeeRD:  in.Body.DeliveryFeeRD,
		DiscountRD:     in.Body.DiscountRD,
		Notes:          in.Body.Notes,
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

type RosaSketchInput struct {
	Body struct {
		Prompt    string `json:"prompt" minLength:"2"`
		EventType string `json:"eventType,omitempty"`
		Guests    int    `json:"guests,omitempty"`
	}
}

type RosaSketchOutput struct {
	Body domain.RosaSketchResponse
}

type quoteErrorSpec struct {
	target error
	status int
	code   string
	policy string
}

var quoteErrorSpecs = []quoteErrorSpec{
	{domain.ErrQuoteForbidden, http.StatusForbidden, "FORBIDDEN_QUOTE_ACCESS", "ClientQuoteIsolation"},
	{domain.ErrQuoteNotFound, http.StatusNotFound, "QUOTE_NOT_FOUND", "QuoteMustExist"},
	{domain.ErrQuoteAlreadyDecided, http.StatusConflict, "QUOTE_ALREADY_DECIDED", "QuoteTransitionGuard"},
	{domain.ErrQuoteNotSent, http.StatusConflict, "QUOTE_NOT_SENT", "QuoteSentRequiredBeforeSign"},
	{domain.ErrInvalidQuoteItem, http.StatusBadRequest, "INVALID_QUOTE_ITEM", "QuoteCatalogPriceValidation"},
	{domain.ErrInsufficientStock, http.StatusConflict, "STOCK_INSUFFICIENT_FOR_DATE", "CheckDateAvailability"},
	{domain.ErrAIProviderNotConfigured, http.StatusServiceUnavailable, "AI_PROVIDER_NOT_CONFIGURED", "RequireConfiguredAIProvider"},
	{domain.ErrSTTProviderNotConfigured, http.StatusServiceUnavailable, "STT_PROVIDER_NOT_CONFIGURED", "RequireConfiguredSTTProvider"},
	{domain.ErrAudioTooLong, http.StatusBadRequest, "ROSA_AUDIO_TOO_LONG", "ValidateAudioDuration"},
	{domain.ErrAIRateLimited, http.StatusTooManyRequests, "ROSA_RATE_LIMITED", "RateLimitRosaChat"},
	{domain.ErrInvalidAudioPayload, http.StatusBadRequest, "ROSA_AUDIO_INVALID", "ValidateBase64Audio"},
}

func mapQuoteError(err error, endpoint, defaultCode, policy string, defaultStatus int) *platformhttp.APIError {
	for _, spec := range quoteErrorSpecs {
		if errors.Is(err, spec.target) {
			return platformhttp.NewAPIError(
				spec.status,
				spec.code,
				err.Error(),
				endpoint,
				spec.policy,
				err.Error(),
			)
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
