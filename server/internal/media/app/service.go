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

const defaultMaxMediaBytes = 10 << 20

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
	asset := domain.Asset{
		ID:          id,
		OwnerID:     strings.TrimSpace(input.OwnerID),
		OwnerRole:   strings.TrimSpace(input.OwnerRole),
		Purpose:     normalizePurpose(input.Purpose),
		Filename:    domain.SanitizeFilename(input.Filename, ext),
		MimeType:    mimeType,
		ByteSize:    int64(len(input.Data)),
		SHA256:      hex.EncodeToString(sum[:]),
		StoragePath: storageRef,
		PublicURL:   s.blobs.PublicURL(objectKey, defaultPublicURL),
		CreatedAt:   s.clock().UTC(),
	}
	if err := s.store.SaveAsset(ctx, asset); err != nil {
		_ = s.blobs.Delete(ctx, storageRef)
		return domain.Asset{}, err
	}
	return asset, nil
}

// Open lee los metadatos y los bytes reales del recurso solicitado.
func (s *Service) Open(ctx context.Context, id string) (domain.Asset, []byte, error) {
	trimmed := strings.TrimSpace(id)
	if trimmed == "" {
		return domain.Asset{}, nil, domain.ErrAssetNotFound
	}
	asset, err := s.store.FindAssetByID(ctx, trimmed)
	if err != nil {
		return domain.Asset{}, nil, err
	}
	content, readErr := s.blobs.Get(ctx, asset.StoragePath)
	if readErr != nil {
		return domain.Asset{}, nil, domain.ErrAssetNotFound
	}
	return asset, content, nil
}

func normalizePurpose(raw string) string {
	trimmed := strings.ToLower(strings.TrimSpace(raw))
	if trimmed == "" {
		return "general"
	}
	return trimmed
}
