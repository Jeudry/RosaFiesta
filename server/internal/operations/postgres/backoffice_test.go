package postgres

import (
	"context"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"

	"github.com/Jeudry/rosafiesta/server/internal/operations/postgres/sqlcgen"
)

type recordedReminderExec struct {
	sql  string
	args []any
}

type fakeReminderDBTX struct {
	execs []recordedReminderExec
}

func (f *fakeReminderDBTX) Exec(_ context.Context, sql string, args ...interface{}) (pgconn.CommandTag, error) {
	f.execs = append(f.execs, recordedReminderExec{sql: sql, args: args})
	return pgconn.CommandTag{}, nil
}

func (f *fakeReminderDBTX) Query(_ context.Context, _ string, _ ...interface{}) (pgx.Rows, error) {
	return nil, nil
}

func (f *fakeReminderDBTX) QueryRow(_ context.Context, _ string, _ ...interface{}) pgx.Row {
	return nil
}

func TestInsertSentEventReminder_PopulatesUniqueIDAndUserID(t *testing.T) {
	t.Parallel()

	db := &fakeReminderDBTX{}
	repo := &Repository{queries: sqlcgen.New(db)}

	userA := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	userB := uuid.MustParse("44444444-4444-4444-4444-444444444444")
	now := time.Date(2026, time.November, 24, 12, 0, 0, 0, time.UTC)

	if err := repo.InsertSentEventReminder(context.Background(), "evt-1", userA, "pre_event", now); err != nil {
		t.Fatalf("first InsertSentEventReminder failed: %v", err)
	}
	if err := repo.InsertSentEventReminder(context.Background(), "evt-2", userB, "pre_event", now); err != nil {
		t.Fatalf("second InsertSentEventReminder failed: %v", err)
	}
	if len(db.execs) != 2 {
		t.Fatalf("expected 2 SQL execs, got %d", len(db.execs))
	}

	firstID, _ := db.execs[0].args[0].(string)
	secondID, _ := db.execs[1].args[0].(string)
	if firstID == "" {
		t.Fatal("expected non-empty reminder id on first insert")
	}
	if secondID == "" {
		t.Fatal("expected non-empty reminder id on second insert")
	}
	if firstID == secondID {
		t.Fatalf("expected unique reminder ids, got duplicate %q", firstID)
	}

	firstUser, _ := db.execs[0].args[2].(uuid.UUID)
	secondUser, _ := db.execs[1].args[2].(uuid.UUID)
	if firstUser != userA {
		t.Fatalf("expected userA=%s, got %s", userA, firstUser)
	}
	if secondUser != userB {
		t.Fatalf("expected userB=%s, got %s", userB, secondUser)
	}
}
