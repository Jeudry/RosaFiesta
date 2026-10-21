// Package db empaqueta las migraciones SQL dentro del binario del servidor.
package db

import "embed"

// Migrations contiene los archivos SQL de `migrations/`.
//
//go:embed migrations/*.sql
var Migrations embed.FS
