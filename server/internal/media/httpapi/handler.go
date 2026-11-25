// Package httpapi expone los endpoints HTTP de carga y entrega binaria de archivos.
package httpapi

import (
	"bytes"
	"context"
	"encoding/base64"
	"errors"
	"fmt"
	"net/http"
	"strings"

	"github.com/danielgtaylor/huma/v2"
	"github.com/go-chi/chi/v5"

	"github.com/Jeudry/rosafiesta/server/internal/media/app"
	"github.com/Jeudry/rosafiesta/server/internal/media/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// Module registra POST /v1/media en Huma y GET /v1/media/{id} en chi.
type Module struct {
	Service *app.Service
	Auth    platformhttp.Authenticator
}

type uploadMediaInput struct {
	Body struct {
		Filename   string `json:"filename" doc:"Nombre del archivo original"`
		MimeType   string `json:"mime_type,omitempty" doc:"Tipo MIME declarado"`
		Purpose    string `json:"purpose,omitempty" doc:"Propósito (catalog, voucher, chat, damage, avatar)"`
		DataBase64 string `json:"data_base64" doc:"Contenido binario codificado en Base64"`
	}
}

type uploadMediaOutput struct {
	Status int
	Body   domain.Asset
}

// Register registra la operación POST /v1/media en la especificación OpenAPI.
func (m Module) Register(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID:  "upload-media-asset",
		Method:       http.MethodPost,
		Path:         "/v1/media",
		Summary:      "Subir imagen o PDF al almacenamiento del servidor",
		Tags:         []string{"Media"},
		MaxBodyBytes: 16 << 20,
		Security:     []map[string][]string{{platformhttp.SecurityScheme: {}}},
		Middlewares:  huma.Middlewares{platformhttp.RequireAuth(api, m.Auth)},
	}, m.uploadHandler)
}

// RegisterRoutes registra el endpoint binario directo GET /v1/media/{id}.
func (m Module) RegisterRoutes(router chi.Router) {
	router.Get("/v1/media/{id}", m.serveMediaHandler)
}

func (m Module) uploadHandler(ctx context.Context, in *uploadMediaInput) (*uploadMediaOutput, error) {
	caller, _ := platformhttp.CurrentPrincipal(ctx)
	decoded, err := decodeBase64Payload(in.Body.DataBase64)
	if err != nil {
		return nil, mapUploadError(err)
	}
	asset, err := m.Service.Upload(ctx, domain.UploadInput{
		OwnerID:   caller.ID.String(),
		OwnerRole: caller.Role,
		Purpose:   in.Body.Purpose,
		Filename:  in.Body.Filename,
		MimeType:  in.Body.MimeType,
		Data:      decoded,
	})
	if err != nil {
		return nil, mapUploadError(err)
	}
	return &uploadMediaOutput{Status: http.StatusCreated, Body: asset}, nil
}

func (m Module) serveMediaHandler(w http.ResponseWriter, r *http.Request) {
	asset, err := m.Service.FindAsset(r.Context(), chi.URLParam(r, "id"))
	if err != nil {
		http.Error(w, "recurso multimedia no encontrado", http.StatusNotFound)
		return
	}
	isPrivate := domain.IsPrivatePurpose(asset.Purpose)
	if !m.authorizeMediaAccess(w, r, asset, isPrivate) {
		return
	}
	content, readErr := m.Service.ReadBlob(r.Context(), asset)
	if readErr != nil {
		http.Error(w, "recurso multimedia no encontrado", http.StatusNotFound)
		return
	}
	if _, _, sniffErr := domain.DetectMimeType(content); sniffErr != nil {
		http.Error(w, "archivo multimedia inválido", http.StatusUnsupportedMediaType)
		return
	}
	writeMediaResponseHeaders(w, asset, isPrivate)
	http.ServeContent(w, r, asset.Filename, asset.CreatedAt, bytes.NewReader(content))
}

func (m Module) authorizeMediaAccess(w http.ResponseWriter, r *http.Request, asset domain.Asset, isPrivate bool) bool {
	if !isPrivate {
		return true
	}
	caller, authenticated := platformhttp.AuthenticateHTTPRequest(r, m.Auth)
	if !authenticated {
		http.Error(w, "autenticación requerida", http.StatusUnauthorized)
		return false
	}
	if caller.IsClient() {
		if asset.OwnerID != caller.ID.String() {
			http.Error(w, "sin permisos para acceder a este recurso", http.StatusForbidden)
			return false
		}
	}
	return true
}

func writeMediaResponseHeaders(w http.ResponseWriter, asset domain.Asset, isPrivate bool) {
	headers := w.Header()
	headers.Set("Content-Type", asset.MimeType)
	if isPrivate {
		headers.Set("Cache-Control", "private, max-age=3600")
	} else {
		headers.Set("Cache-Control", "public, max-age=86400")
	}
	headers.Set("ETag", fmt.Sprintf(`"%s"`, asset.SHA256))
	headers.Set("X-Content-Type-Options", "nosniff")
	headers.Set("Content-Security-Policy", "sandbox")
}

func decodeBase64Payload(raw string) ([]byte, error) {
	cleaned := stripDataURLPrefix(strings.TrimSpace(raw))
	if cleaned == "" {
		return nil, domain.ErrEmptyPayload
	}
	decoded, err := base64.StdEncoding.DecodeString(cleaned)
	if err != nil {
		return nil, domain.ErrUnsupportedMimeType
	}
	return decoded, nil
}

func stripDataURLPrefix(value string) string {
	commaIdx := strings.Index(value, ",")
	if commaIdx < 0 {
		return value
	}
	header := value[:commaIdx]
	isBase64DataURL := strings.Contains(header, "base64")
	if !isBase64DataURL {
		return value
	}
	return value[commaIdx+1:]
}

func mapUploadError(err error) error {
	switch {
	case errors.Is(err, domain.ErrEmptyPayload):
		return platformhttp.NewAPIError(http.StatusBadRequest, "MEDIA_EMPTY", "El archivo enviado está vacío.", "/v1/media", "authenticated", "data_base64 requerido")
	case errors.Is(err, domain.ErrFileTooLarge):
		return platformhttp.NewAPIError(http.StatusRequestEntityTooLarge, "MEDIA_TOO_LARGE", "El archivo supera el tamaño máximo permitido.", "/v1/media", "authenticated", "reduce el peso del archivo")
	case errors.Is(err, domain.ErrUnsupportedMimeType):
		return platformhttp.NewAPIError(http.StatusBadRequest, "MEDIA_UNSUPPORTED_TYPE", "Solo se permiten imágenes SVG, JPEG, PNG, WebP o PDF.", "/v1/media", "authenticated", "magic bytes no válidos")
	default:
		return platformhttp.NewAPIError(http.StatusInternalServerError, "MEDIA_STORAGE_ERROR", "No se pudo almacenar el archivo.", "/v1/media", "authenticated", "error de almacenamiento")
	}
}
