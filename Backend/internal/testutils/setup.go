package testutils

import (
	"context"
	"database/sql"
	"fmt"
	"os/exec"
	"path/filepath"
	"runtime"
	"testing"
	"time"

	"github.com/go-redis/redis/v8"
	_ "github.com/lib/pq"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

type TestDatabase struct {
	Db        *sql.DB
	Container testcontainers.Container
	ConnStr   string
}

type TestRedis struct {
	Client    *redis.Client
	Container testcontainers.Container
	Addr      string
}

// SetupTestDatabase starts a Postgres container and returns a connection.
// It also runs migrations using the 'migrate' CLI tool found in PATH.
func SetupTestDatabase(t *testing.T) *TestDatabase {
	ctx := context.Background()

	req := testcontainers.ContainerRequest{
		Image:        "postgres:13-alpine",
		ExposedPorts: []string{"5432/tcp"},
		Env: map[string]string{
			"POSTGRES_USER":     "testuser",
			"POSTGRES_PASSWORD": "testpassword",
			"POSTGRES_DB":       "testdb",
		},
		WaitingFor: wait.ForLog("database system is ready to accept connections").
			WithOccurrence(2).
			WithStartupTimeout(10 * time.Second),
	}

	container, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		t.Fatalf("failed to start postgres container: %s", err)
	}

	host, err := container.Host(ctx)
	if err != nil {
		t.Fatalf("failed to get container host: %s", err)
	}

	port, err := container.MappedPort(ctx, "5432")
	if err != nil {
		t.Fatalf("failed to get container port: %s", err)
	}

	connStr := fmt.Sprintf("postgres://testuser:testpassword@%s:%s/testdb?sslmode=disable", host, port.Port())

	db, err := sql.Open("postgres", connStr)
	if err != nil {
		t.Fatalf("failed to open db connection: %s", err)
	}

	if err = db.Ping(); err != nil {
		t.Fatalf("failed to ping db: %s", err)
	}

	// Run Migrations
	// Assuming the tests are running from root or we can find the migrations folder relative to this file
	_, filename, _, _ := runtime.Caller(0)
	projectRoot := filepath.Join(filepath.Dir(filename), "../..")
	migrationsPath := filepath.Join(projectRoot, "cmd/migrate/migrations")

	// Use the 'migrate' CLI tool
	cmd := exec.Command("migrate", "-path", migrationsPath, "-database", connStr, "up")
	if out, err := cmd.CombinedOutput(); err != nil {
		t.Fatalf("failed to run migrations: %s\nOutput: %s", err, string(out))
	}

	return &TestDatabase{
		Db:        db,
		Container: container,
		ConnStr:   connStr,
	}
}

func (td *TestDatabase) Teardown(t *testing.T) {
	if err := td.Db.Close(); err != nil {
		t.Logf("failed to close db: %s", err)
	}
	if err := td.Container.Terminate(context.Background()); err != nil {
		t.Fatalf("failed to terminate container: %s", err)
	}
}

func SetupTestRedis(t *testing.T) *TestRedis {
	ctx := context.Background()

	req := testcontainers.ContainerRequest{
		Image:        "redis:6-alpine",
		ExposedPorts: []string{"6379/tcp"},
		WaitingFor:   wait.ForLog("Ready to accept connections"),
	}

	container, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		t.Fatalf("failed to start redis container: %s", err)
	}

	host, err := container.Host(ctx)
	if err != nil {
		t.Fatalf("failed to get container host: %s", err)
	}

	port, err := container.MappedPort(ctx, "6379")
	if err != nil {
		t.Fatalf("failed to get container port: %s", err)
	}

	addr := fmt.Sprintf("%s:%s", host, port.Port())

	rdb := redis.NewClient(&redis.Options{
		Addr: addr,
	})

	if err := rdb.Ping(ctx).Err(); err != nil {
		t.Fatalf("failed to ping redis: %s", err)
	}

	return &TestRedis{
		Client:    rdb,
		Container: container,
		Addr:      addr,
	}
}

func (tr *TestRedis) Teardown(t *testing.T) {
	if err := tr.Client.Close(); err != nil {
		t.Logf("failed to close redis client: %s", err)
	}
	if err := tr.Container.Terminate(context.Background()); err != nil {
		t.Fatalf("failed to terminate redis container: %s", err)
	}
}
