-- name: UpsertQuote :exec
INSERT INTO quotes (
    id, event_id, client_name, event_title, total_dop, deposit_dop,
    balance_dop, status, version, notes, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10, $11
)
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    notes = EXCLUDED.notes,
    version = EXCLUDED.version;

-- name: ListQuotes :many
SELECT id, event_id, client_name, event_title, total_dop, deposit_dop,
       balance_dop, status, version, notes, created_at
FROM quotes
ORDER BY created_at, id;

-- name: GetQuoteByID :one
SELECT id, event_id, client_name, event_title, total_dop, deposit_dop,
       balance_dop, status, version, notes, created_at
FROM quotes
WHERE id = $1;

-- name: UpdateQuoteStatusAndNotes :exec
UPDATE quotes
SET status = $2,
    notes = $3
WHERE id = $1;
