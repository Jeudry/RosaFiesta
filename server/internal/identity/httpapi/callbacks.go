package httpapi

import (
	"bytes"
	"crypto/rand"
	"embed"
	"encoding/base64"
	"html/template"
	"net/http"
	"strings"

	"github.com/go-chi/chi/v5"
)

//go:embed templates/*.html
var callbackTemplatesFS embed.FS

var callbackTemplates = template.Must(template.ParseFS(callbackTemplatesFS, "templates/*.html"))

type oauthCallbackPayload struct {
	Type             string `json:"type"`
	Code             string `json:"code"`
	State            string `json:"state"`
	Error            string `json:"error"`
	ErrorDescription string `json:"errorDescription"`
}

type oauthCallbackViewData struct {
	ScriptNonce   string
	Payload       oauthCallbackPayload
	TargetOrigins []string
}

type paymentCallbackPayload struct {
	Type      string `json:"type"`
	SessionID string `json:"sessionId"`
	EventID   string `json:"eventId"`
	Cancelled bool   `json:"cancelled"`
}

type paymentCallbackViewData struct {
	ScriptNonce   string
	Headline      string
	Description   string
	Payload       paymentCallbackPayload
	TargetOrigins []string
}

func (m Module) RegisterRoutes(router chi.Router) {
	router.Get("/callback", m.handleOAuthCallbackBridge)
	router.Post("/callback", m.handleOAuthCallbackBridge)
	router.Get("/pagos", m.handlePaymentCallbackBridge)
}

func (m Module) handleOAuthCallbackBridge(w http.ResponseWriter, r *http.Request) {
	nonce := generateScriptNonce()
	viewData := oauthCallbackViewData{
		ScriptNonce:   nonce,
		Payload:       extractOAuthCallbackPayload(r),
		TargetOrigins: buildCallbackTargetOrigins(m.AllowedWebOrigins),
	}
	renderBridgeTemplate(w, "oauth_callback.html", nonce, viewData)
}

func extractOAuthCallbackPayload(r *http.Request) oauthCallbackPayload {
	if r.Method == http.MethodPost {
		_ = r.ParseForm()
		if len(r.PostForm) > 0 {
			return oauthCallbackPayload{
				Type:             "rosafiesta-oauth-callback",
				Code:             strings.TrimSpace(r.PostForm.Get("code")),
				State:            strings.TrimSpace(r.PostForm.Get("state")),
				Error:            strings.TrimSpace(r.PostForm.Get("error")),
				ErrorDescription: strings.TrimSpace(r.PostForm.Get("error_description")),
			}
		}
	}
	query := r.URL.Query()
	return oauthCallbackPayload{
		Type:             "rosafiesta-oauth-callback",
		Code:             strings.TrimSpace(query.Get("code")),
		State:            strings.TrimSpace(query.Get("state")),
		Error:            strings.TrimSpace(query.Get("error")),
		ErrorDescription: strings.TrimSpace(query.Get("error_description")),
	}
}

func (m Module) handlePaymentCallbackBridge(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	cancelledFlag := strings.TrimSpace(query.Get("cancelled")) == "1"
	headline := "Pago recibido"
	description := "Estamos confirmando tu pago y regresando a RosaFiesta."
	if cancelledFlag {
		headline = "Pago cancelado"
		description = "No se realizó ningún cargo. Puedes volver a intentarlo desde RosaFiesta."
	}
	nonce := generateScriptNonce()
	viewData := paymentCallbackViewData{
		ScriptNonce: nonce,
		Headline:    headline,
		Description: description,
		Payload: paymentCallbackPayload{
			Type:      "rosafiesta-payment-callback",
			SessionID: strings.TrimSpace(query.Get("session_id")),
			EventID:   strings.TrimSpace(query.Get("event_id")),
			Cancelled: cancelledFlag,
		},
		TargetOrigins: buildCallbackTargetOrigins(m.AllowedWebOrigins),
	}
	renderBridgeTemplate(w, "payment_callback.html", nonce, viewData)
}

func buildCallbackTargetOrigins(allowedOrigins []string) []string {
	origins := make([]string, 0, len(allowedOrigins))
	seen := make(map[string]struct{}, len(allowedOrigins))
	for _, rawOrigin := range allowedOrigins {
		cleanOrigin := strings.TrimSpace(rawOrigin)
		if cleanOrigin == "" {
			continue
		}
		if _, exists := seen[cleanOrigin]; exists {
			continue
		}
		seen[cleanOrigin] = struct{}{}
		origins = append(origins, cleanOrigin)
	}
	return origins
}

func generateScriptNonce() string {
	buf := make([]byte, 16)
	_, _ = rand.Read(buf)
	return base64.RawURLEncoding.EncodeToString(buf)
}

func renderBridgeTemplate(w http.ResponseWriter, name, nonce string, data any) {
	var buf bytes.Buffer
	if err := callbackTemplates.ExecuteTemplate(&buf, name, data); err != nil {
		http.Error(w, "callback render error", http.StatusInternalServerError)
		return
	}
	cspPolicy := "default-src 'none'; style-src 'unsafe-inline'; script-src 'nonce-" + nonce + "'; frame-ancestors 'none'"
	w.Header().Set("Content-Security-Policy", cspPolicy)
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.Header().Set("Cache-Control", "no-store")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write(buf.Bytes())
}
