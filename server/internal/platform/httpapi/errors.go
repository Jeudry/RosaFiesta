package httpapi

import (
	"encoding/json"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

// APIError implementa huma.StatusError con código de negocio estable, endpoint, política y razones censuradas.
type APIError struct {
	Status   int      `json:"status" doc:"Código de estado HTTP."`
	Title    string   `json:"title" doc:"Título estándar HTTP."`
	Detail   string   `json:"detail" doc:"Detalle legible con datos sensibles censurados."`
	Code     string   `json:"code" doc:"Código estable de error de negocio o seguridad."`
	Message  string   `json:"message" doc:"Mensaje seguro para mostrar o registrar."`
	Endpoint string   `json:"endpoint,omitempty" doc:"Método y ruta del endpoint invocado."`
	Policy   string   `json:"policy,omitempty" doc:"Política de negocio o autorización evaluada."`
	Reasons  []string `json:"reasons,omitempty" doc:"Motivos diagnósticos con datos sensibles censurados."`
}

// Error devuelve el mensaje censurado del error.
func (e *APIError) Error() string {
	return e.Detail
}

// GetStatus devuelve el código de estado HTTP para Huma.
func (e *APIError) GetStatus() int {
	return e.Status
}

// NewAPIError construye un APIError censurando automáticamente cualquier dato sensible en message y reasons.
func NewAPIError(status int, code, message, endpoint, policy string, reasons ...string) *APIError {
	safeMsg := redact.Text(message)
	if safeMsg == "" {
		safeMsg = http.StatusText(status)
	}
	safeReasons := redact.Reasons(reasons...)
	if len(safeReasons) == 0 {
		safeReasons = []string{safeMsg}
	}
	return &APIError{
		Status:   status,
		Title:    http.StatusText(status),
		Detail:   safeMsg,
		Code:     code,
		Message:  safeMsg,
		Endpoint: endpoint,
		Policy:   policy,
		Reasons:  safeReasons,
	}
}

// WriteAPIError escribe un APIError en formato JSON estructurado sobre un huma.Context.
func WriteAPIError(ctx huma.Context, apiErr *APIError) {
	ctx.SetHeader("Content-Type", "application/problem+json")
	ctx.SetStatus(apiErr.Status)
	_ = json.NewEncoder(ctx.BodyWriter()).Encode(apiErr)
}

// WriteHTTPAPIError escribe un APIError directamente en un http.ResponseWriter estándar.
func WriteHTTPAPIError(w http.ResponseWriter, apiErr *APIError) {
	w.Header().Set("Content-Type", "application/problem+json")
	w.WriteHeader(apiErr.Status)
	_ = json.NewEncoder(w).Encode(apiErr)
}
