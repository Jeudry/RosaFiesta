package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
	"github.com/Jeudry/rosafiesta/server/internal/quotes/app"
)

type Module struct {
	Service *app.Service
	Auth    platformhttp.PrincipalResolver
}

func (m Module) Register(api huma.API) {
	m.registerReadAndCreateRoutes(api)
	m.registerDecisionRoutes(api)
	m.registerRosaAssistantRoutes(api)
}

func (m Module) authMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)}
}

func (m Module) optionalAuthMiddlewares(api huma.API) huma.Middlewares {
	if m.Auth == nil {
		return nil
	}
	return huma.Middlewares{platformhttp.OptionalAuth(api, m.Auth)}
}

func (m Module) registerReadAndCreateRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-list",
		Method:      http.MethodGet,
		Path:        "/v1/quotes",
		Summary:     "List quotes and contract proposals scoped by caller role",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleListQuotes)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-get-by-id",
		Method:      http.MethodGet,
		Path:        "/v1/quotes/{id}",
		Summary:     "Get quote and digital contract detail",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleGetQuote)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-create",
		Method:      http.MethodPost,
		Path:        "/v1/quotes",
		Summary:     "Create a new quote with 30 percent deposit calculation",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleCreateQuote)
}

func (m Module) registerDecisionRoutes(api huma.API) {
	mw := m.authMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-send",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/{id}/send",
		Summary:     "Admin enriches, approves and sends a quote to the client",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleSendQuote)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-sign-contract",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/{id}/sign",
		Summary:     "Sign rental digital contract with SVG signature",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleSignContract)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-decline",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/{id}/decline",
		Summary:     "Decline quote proposal with reason",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDeclineQuote)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-duplicate",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/{id}/duplicate",
		Summary:     "Duplicate an existing quote into a new pending draft",
		Tags:        []string{"Quotes"},
		Security:    platformhttp.Secured(),
		Middlewares: mw,
	}, m.handleDuplicateQuote)
}

func (m Module) registerRosaAssistantRoutes(api huma.API) {
	optMW := m.optionalAuthMiddlewares(api)

	huma.Register(api, huma.Operation{
		OperationID: "quotes-rosa-assistant-proposal",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/rosa-proposal",
		Summary:     "Generate Rosa AI moodboard and live SKU quote proposal",
		Tags:        []string{"Quotes"},
		Middlewares: optMW,
	}, m.handleRosaProposal)

	huma.Register(api, huma.Operation{
		OperationID: "rosa-chat",
		Method:      http.MethodPost,
		Path:        "/v1/rosa/chat",
		Summary:     "Conversational Rosa AI design and operational assistant",
		Tags:        []string{"Quotes"},
		Middlewares: optMW,
	}, m.handleRosaChat)

	huma.Register(api, huma.Operation{
		OperationID: "rosa-transcribe",
		Method:      http.MethodPost,
		Path:        "/v1/rosa/transcribe",
		Summary:     "Analyze and transcribe voice notes for Rosa AI",
		Tags:        []string{"Quotes"},
		Middlewares: optMW,
	}, m.handleRosaTranscribe)

	huma.Register(api, huma.Operation{
		OperationID: "rosa-sketch",
		Method:      http.MethodPost,
		Path:        "/v1/quotes/rosa/sketch",
		Summary:     "Generate spatial layout elements for event sketch using configured AI provider",
		Tags:        []string{"Quotes"},
		Middlewares: optMW,
	}, m.handleRosaSketch)
}

func (m Module) handleListQuotes(ctx context.Context, in *QuotesListInput) (*QuotesListOutput, error) {
	caller, authenticated := platformhttp.CurrentPrincipal(ctx)
	out := &QuotesListOutput{}
	out.Body.Quotes = m.Service.ListQuotesForCaller(ctx, in.Status, caller, authenticated)
	return out, nil
}

func (m Module) handleGetQuote(ctx context.Context, in *QuoteByIDInput) (*QuoteOutput, error) {
	caller, authenticated := platformhttp.CurrentPrincipal(ctx)
	quote, err := m.Service.GetQuoteForCaller(ctx, in.ID, caller, authenticated)
	if err != nil {
		return nil, mapQuoteError(err, "GET /v1/quotes/"+in.ID, "QUOTE_NOT_FOUND", "QuoteMustExist", http.StatusNotFound)
	}
	return newQuoteOutput(quote), nil
}

func (m Module) handleCreateQuote(ctx context.Context, in *CreateQuoteInput) (*QuoteOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	quote, err := m.Service.CreateQuoteForCaller(ctx, cmd, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/quotes", "QUOTE_VALIDATION_FAILED", "CreateQuoteValidation", http.StatusBadRequest)
	}
	return newQuoteOutput(quote), nil
}

func (m Module) handleSendQuote(ctx context.Context, in *SendQuoteInput) (*QuoteOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	cmd := in.ToCommand()
	quote, err := m.Service.SendQuoteForAdmin(ctx, in.ID, cmd, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/quotes/"+in.ID+"/send", "QUOTE_SEND_FAILED", "SendQuoteValidation", http.StatusBadRequest)
	}
	return newQuoteOutput(quote), nil
}

func (m Module) handleSignContract(ctx context.Context, in *SignContractInput) (*QuoteOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	quote, err := m.Service.SignContractForCaller(ctx, in.ID, in.Body.SignedByName, in.Body.SignatureSVG, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/quotes/"+in.ID+"/sign", "QUOTE_SIGNATURE_INVALID", "DigitalSignatureRequired", http.StatusBadRequest)
	}
	return newQuoteOutput(quote), nil
}

func (m Module) handleDeclineQuote(ctx context.Context, in *DeclineQuoteInput) (*QuoteOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	quote, err := m.Service.DeclineQuoteForCaller(ctx, in.ID, in.Body.Reason, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/quotes/"+in.ID+"/decline", "QUOTE_DECLINE_FAILED", "DeclineQuoteValidation", http.StatusBadRequest)
	}
	return newQuoteOutput(quote), nil
}

func (m Module) handleDuplicateQuote(ctx context.Context, in *QuoteByIDInput) (*QuoteOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	quote, err := m.Service.DuplicateQuoteForCaller(ctx, in.ID, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/quotes/"+in.ID+"/duplicate", "QUOTE_DUPLICATE_FAILED", "DuplicateQuote", http.StatusBadRequest)
	}
	return newQuoteOutput(quote), nil
}

func (m Module) handleRosaProposal(ctx context.Context, in *RosaProposalInput) (*RosaProposalOutput, error) {
	req := in.ToRequest()
	out := &RosaProposalOutput{}
	out.Body.Proposal = m.Service.GenerateRosaProposal(ctx, req)
	return out, nil
}

func (m Module) handleRosaChat(ctx context.Context, in *RosaChatInput) (*RosaChatOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	req := in.ToRequest()
	reply, err := m.Service.ChatWithRosa(ctx, in.Body.Message, req, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/rosa/chat", "ROSA_CHAT_FAILED", "RosaAssistantChat", http.StatusBadGateway)
	}
	return &RosaChatOutput{Body: reply}, nil
}

func (m Module) handleRosaTranscribe(ctx context.Context, in *RosaTranscribeInput) (*RosaTranscribeOutput, error) {
	cmd := in.ToCommand()
	res, err := m.Service.TranscribeAudio(ctx, cmd)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/rosa/transcribe", "ROSA_AUDIO_INVALID", "ValidateBase64Audio", http.StatusBadRequest)
	}
	return &RosaTranscribeOutput{Body: res}, nil
}

func (m Module) handleRosaSketch(ctx context.Context, in *RosaSketchInput) (*RosaSketchOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	res, err := m.Service.GenerateRosaSketch(ctx, in.Body.Prompt, in.Body.EventType, in.Body.Guests, caller)
	if err != nil {
		return nil, mapQuoteError(err, "POST /v1/quotes/rosa/sketch", "ROSA_SKETCH_FAILED", "RosaSketchGeneration", http.StatusBadGateway)
	}
	return &RosaSketchOutput{Body: res}, nil
}
