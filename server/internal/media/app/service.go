// Package app implementa la carga y entrega de recursos multimedia.
package app

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/media/domain"
)

const defaultMaxMediaBytes = 16 << 20

// Store abstrae la persistencia de metadatos de archivos multimedia.
type Store interface {
	SaveAsset(ctx context.Context, asset domain.Asset) error
	FindAssetByID(ctx context.Context, id string) (domain.Asset, error)
}

// Service valida, persiste en disco + base de datos y entrega archivos cargados.
type Service struct {
	store      Store
	blobs      BlobStore
	storageDir string
	maxBytes   int64
	clock      func() time.Time
}

// NewService crea un servicio de archivos multimedia con límites y directorio configurables.
func NewService(store Store, storageDir string, maxBytes int64, clock func() time.Time) *Service {
	if maxBytes <= 0 {
		maxBytes = defaultMaxMediaBytes
	}
	if strings.TrimSpace(storageDir) == "" {
		storageDir = filepath.Join(os.TempDir(), "rosafiesta-media")
	}
	if clock == nil {
		clock = time.Now
	}
	return &Service{
		store:      store,
		blobs:      localBlobStore{storageDir: storageDir},
		storageDir: storageDir,
		maxBytes:   maxBytes,
		clock:      clock,
	}
}

// Upload valida tamaño y firma binaria real, escribe el archivo en el almacén activo y registra metadatos.
func (s *Service) Upload(ctx context.Context, input domain.UploadInput) (domain.Asset, error) {
	if len(input.Data) == 0 {
		return domain.Asset{}, domain.ErrEmptyPayload
	}
	if int64(len(input.Data)) > s.maxBytes {
		return domain.Asset{}, domain.ErrFileTooLarge
	}
	mimeType, ext, err := domain.DetectMimeType(input.Data)
	if err != nil {
		return domain.Asset{}, err
	}
	id := "med-" + uuid.NewString()
	objectKey := id + ext
	storageRef, err := s.blobs.Put(ctx, objectKey, mimeType, input.Data)
	if err != nil {
		return domain.Asset{}, err
	}
	sum := sha256.Sum256(input.Data)
	defaultPublicURL := fmt.Sprintf("/v1/media/%s", id)
	purpose := normalizePurpose(input.Purpose)
	publicURL := defaultPublicURL
	if !domain.IsPrivatePurpose(purpose) {
		publicURL = s.blobs.PublicURL(objectKey, defaultPublicURL)
	}
	asset := domain.Asset{
		ID:          id,
		OwnerID:     strings.TrimSpace(input.OwnerID),
		OwnerRole:   strings.TrimSpace(input.OwnerRole),
		Purpose:     purpose,
		Filename:    domain.SanitizeFilename(input.Filename, ext),
		MimeType:    mimeType,
		ByteSize:    int64(len(input.Data)),
		SHA256:      hex.EncodeToString(sum[:]),
		StoragePath: storageRef,
		PublicURL:   publicURL,
		CreatedAt:   s.clock().UTC(),
	}
	if err := s.store.SaveAsset(ctx, asset); err != nil {
		_ = s.blobs.Delete(ctx, storageRef)
		return domain.Asset{}, err
	}
	return asset, nil
}

// FindAsset carga únicamente los metadatos del recurso sin leer sus bytes todavía.
func (s *Service) FindAsset(ctx context.Context, id string) (domain.Asset, error) {
	trimmed := strings.TrimSpace(id)
	if trimmed == "" {
		return domain.Asset{}, domain.ErrAssetNotFound
	}
	return s.store.FindAssetByID(ctx, trimmed)
}

// ReadBlob lee los bytes reales de un recurso ya localizado y autorizado.
func (s *Service) ReadBlob(ctx context.Context, asset domain.Asset) ([]byte, error) {
	content, readErr := s.blobs.Get(ctx, asset.StoragePath)
	if readErr != nil {
		return nil, domain.ErrAssetNotFound
	}
	return content, nil
}

// Open lee los metadatos y los bytes reales del recurso solicitado.
func (s *Service) Open(ctx context.Context, id string) (domain.Asset, []byte, error) {
	asset, err := s.FindAsset(ctx, id)
	if err != nil {
		return domain.Asset{}, nil, err
	}
	content, err := s.ReadBlob(ctx, asset)
	if err != nil {
		return domain.Asset{}, nil, err
	}
	return asset, content, nil
}

// CheckHealth verifica que el almacén de medios configurado esté disponible para escritura/lectura.
func (s *Service) CheckHealth(_ context.Context) error {
	if s == nil {
		return fmt.Errorf("media service not initialized")
	}
	switch store := s.blobs.(type) {
	case localBlobStore:
		return os.MkdirAll(store.storageDir, 0o750)
	case s3BlobStore:
		if !store.cfg.IsConfigured() {
			return fmt.Errorf("s3 storage credentials incomplete")
		}
		return nil
	default:
		return nil
	}
}

func normalizePurpose(raw string) string {
	trimmed := strings.ToLower(strings.TrimSpace(raw))
	if trimmed == "" {
		return "general"
	}
	return trimmed
}
