// Package dbtest prepara una base PostgreSQL limpia para las pruebas de integración.
package dbtest

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"io/fs"
	"os"
	"testing"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"

	dbfiles "github.com/Jeudry/rosafiesta/server/db"
	"github.com/Jeudry/rosafiesta/server/internal/platform/db"
)

// Pool devuelve un pool sobre un esquema propio y vacío, con todas las migraciones aplicadas.
func Pool(t *testing.T) *pgxpool.Pool {
	t.Helper()
	url := os.Getenv("TEST_DATABASE_URL")
	if url == "" {
		t.Skip("TEST_DATABASE_URL no está definida: se omite la prueba de integración")
	}
	ctx := context.Background()
	schema := randomSchema(t)

	var admin *pgxpool.Pool
	var err error
	for range 20 {
		admin, err = db.Open(ctx, url)
		if err == nil {
			break
		}
		time.Sleep(250 * time.Millisecond)
	}
	if err != nil {
		t.Fatalf("open test database: %v", err)
	}
	if _, err := admin.Exec(ctx, "CREATE SCHEMA "+schema); err != nil {
		admin.Close()
		t.Fatalf("create schema: %v", err)
	}
	t.Cleanup(func() {
		_, _ = admin.Exec(context.WithoutCancel(ctx), "DROP SCHEMA "+schema+" CASCADE")
		admin.Close()
	})

	config, err := pgxpool.ParseConfig(url)
	if err != nil {
		t.Fatalf("parse url: %v", err)
	}
	config.ConnConfig.RuntimeParams["search_path"] = schema
	pool, err := pgxpool.NewWithConfig(ctx, config)
	if err != nil {
		t.Fatalf("open schema pool: %v", err)
	}
	t.Cleanup(pool.Close)

	migrations, err := fs.Sub(dbfiles.Migrations, "migrations")
	if err != nil {
		t.Fatalf("migrations dir: %v", err)
	}
	if err := db.Migrate(ctx, pool, migrations); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	return pool
}

func randomSchema(t *testing.T) string {
	t.Helper()
	buf := make([]byte, 6)
	if _, err := rand.Read(buf); err != nil {
		t.Fatalf("random schema name: %v", err)
	}
	return fmt.Sprintf("t_%s", hex.EncodeToString(buf))
}
