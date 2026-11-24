// Package domain define los contratos de archivos multimedia y su validación por magic bytes.
package domain

import (
	"bytes"
	"errors"
	"path/filepath"
	"regexp"
	"strings"
	"time"
)

var (
	ErrEmptyPayload        = errors.New("media payload is empty")
	ErrFileTooLarge        = errors.New("media file exceeds maximum size")
	ErrUnsupportedMimeType = errors.New("unsupported media file format")
	ErrAssetNotFound       = errors.New("media asset not found")
)

// Asset describe un archivo binario almacenado y servible por /v1/media/{id}.
type Asset struct {
	ID          string    `json:"id"`
	OwnerID     string    `json:"owner_id"`
	OwnerRole   string    `json:"owner_role"`
	Purpose     string    `json:"purpose"`
	Filename    string    `json:"filename"`
	MimeType    string    `json:"mime_type"`
	ByteSize    int64     `json:"byte_size"`
	SHA256      string    `json:"sha256"`
	StoragePath string    `json:"-"`
	PublicURL   string    `json:"url"`
	CreatedAt   time.Time `json:"created_at"`
}

// UploadInput contiene los bytes validados para almacenar un recurso multimedia.
type UploadInput struct {
	OwnerID   string
	OwnerRole string
	Purpose   string
	Filename  string
	MimeType  string
	Data      []byte
}

// DetectMimeType inspecciona los encabezados binarios reales para validar el formato permitido.
func DetectMimeType(data []byte) (string, string, error) {
	if len(data) == 0 {
		return "", "", ErrEmptyPayload
	}
	if isJPEG(data) {
		return "image/jpeg", ".jpg", nil
	}
	if isPNG(data) {
		return "image/png", ".png", nil
	}
	if isWebP(data) {
		return "image/webp", ".webp", nil
	}
	if isPDF(data) {
		return "application/pdf", ".pdf", nil
	}
	if isSVG(data) {
		return "image/svg+xml", ".svg", nil
	}
	return "", "", ErrUnsupportedMimeType
}

// SanitizeFilename elimina rutas peligrosas y preserva un nombre limpio.
func SanitizeFilename(raw, fallbackExt string) string {
	base := filepath.Base(strings.TrimSpace(raw))
	switch base {
	case "", ".", "/":
		return "archivo" + fallbackExt
	}
	cleaned := strings.NewReplacer("..", "", "/", "_", "\\", "_", " ", "_").Replace(base)
	if cleaned == "" {
		return "archivo" + fallbackExt
	}
	return cleaned
}

func isJPEG(data []byte) bool {
	if len(data) < 3 {
		return false
	}
	hasSOI := data[0] == 0xFF
	if !hasSOI {
		return false
	}
	if data[1] != 0xD8 {
		return false
	}
	return data[2] == 0xFF
}

func isPNG(data []byte) bool {
	pngMagic := []byte{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
	if len(data) < len(pngMagic) {
		return false
	}
	return bytes.HasPrefix(data, pngMagic)
}

func isWebP(data []byte) bool {
	if len(data) < 12 {
		return false
	}
	hasRiff := bytes.Equal(data[0:4], []byte("RIFF"))
	if !hasRiff {
		return false
	}
	return bytes.Equal(data[8:12], []byte("WEBP"))
}

func isPDF(data []byte) bool {
	if len(data) < 4 {
		return false
	}
	return bytes.HasPrefix(data, []byte("%PDF"))
}

var svgEventAttrPattern = regexp.MustCompile(`(?i)\bon[a-z]+\s*=`)

// IsPrivatePurpose defaults empty and unknown purposes to private; only explicit catalog/public asset purposes are public.
func IsPrivatePurpose(purpose string) bool {
	normalized := strings.ToLower(strings.TrimSpace(purpose))
	switch normalized {
	case "catalog", "category", "category-icon", "package", "trend", "avatar":
		return false
	default:
		return true
	}
}

func isSVG(data []byte) bool {
	trimmed := bytes.TrimPrefix(data, []byte{0xEF, 0xBB, 0xBF})
	trimmed = bytes.TrimSpace(trimmed)
	if len(trimmed) < 5 {
		return false
	}
	if trimmed[0] != '<' {
		return false
	}
	headerLimit := len(trimmed)
	if headerLimit > 1024 {
		headerLimit = 1024
	}
	lowerHeader := strings.ToLower(string(trimmed[:headerLimit]))
	if !strings.Contains(lowerHeader, "<svg") {
		return false
	}
	lowerFull := strings.ToLower(string(trimmed))
	blocked := []string{"<script", "javascript:", "<animate", "<foreignobject", "<set", "data:text/html"}
	for _, marker := range blocked {
		if strings.Contains(lowerFull, marker) {
			return false
		}
	}
	return !svgEventAttrPattern.MatchString(lowerFull)
}
