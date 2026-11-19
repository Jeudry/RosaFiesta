// Package postgres implementa la persistencia de medios en PostgreSQL y memoria.
package postgres

import (
	"context"
	"errors"
	"sync"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"

	"github.com/Jeudry/rosafiesta/server/internal/media/domain"
)

// Repository almacena metadatos de archivos en PostgreSQL.
type Repository struct {
	pool *pgxpool.Pool
}

// NewRepository crea un repositorio de medios respaldado por pgxpool.
func NewRepository(pool *pgxpool.Pool) *Repository {
	return &Repository{pool: pool}
}

// SaveAsset inserta o actualiza la ficha de un recurso multimedia.
func (r *Repository) SaveAsset(ctx context.Context, asset domain.Asset) error {
	const query = `
INSERT INTO media_assets (
	id, owner_id, owner_role, purpose, filename, mime_type, byte_size, sha256, storage_path, public_url, created_at
) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)`
	_, err := r.pool.Exec(
		ctx,
		query,
		asset.ID,
		asset.OwnerID,
		asset.OwnerRole,
		asset.Purpose,
		asset.Filename,
		asset.MimeType,
		asset.ByteSize,
		asset.SHA256,
		asset.StoragePath,
		asset.PublicURL,
		asset.CreatedAt,
	)
	return err
}

// FindAssetByID recupera la metadata de un recurso por su identificador.
func (r *Repository) FindAssetByID(ctx context.Context, id string) (domain.Asset, error) {
	const query = `
SELECT id, owner_id, owner_role, purpose, filename, mime_type, byte_size, sha256, storage_path, public_url, created_at
FROM media_assets
WHERE id = $1`
	var asset domain.Asset
	err := r.pool.QueryRow(ctx, query, id).Scan(
		&asset.ID,
		&asset.OwnerID,
		&asset.OwnerRole,
		&asset.Purpose,
		&asset.Filename,
		&asset.MimeType,
		&asset.ByteSize,
		&asset.SHA256,
		&asset.StoragePath,
		&asset.PublicURL,
		&asset.CreatedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return domain.Asset{}, domain.ErrAssetNotFound
		}
		return domain.Asset{}, err
	}
	return asset, nil
}

// MemoryStore almacena metadatos de medios en memoria para pruebas unitarias sin DB.
type MemoryStore struct {
	mu     sync.RWMutex
	assets map[string]domain.Asset
}

// NewMemoryStore crea un almacén en memoria para metadatos multimedia.
func NewMemoryStore() *MemoryStore {
	return &MemoryStore{assets: make(map[string]domain.Asset)}
}

// SaveAsset guarda un activo en memoria.
func (m *MemoryStore) SaveAsset(_ context.Context, asset domain.Asset) error {
	m.mu.Lock()
	m.assets[asset.ID] = asset
	m.mu.Unlock()
	return nil
}

// FindAssetByID busca un activo en memoria por ID.
func (m *MemoryStore) FindAssetByID(_ context.Context, id string) (domain.Asset, error) {
	m.mu.RLock()
	asset, ok := m.assets[id]
	m.mu.RUnlock()
	if !ok {
		return domain.Asset{}, domain.ErrAssetNotFound
	}
	return asset, nil
}
