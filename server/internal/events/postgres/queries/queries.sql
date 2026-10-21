-- name: UpsertEvent :exec
INSERT INTO events (
    id, client_id, client_name, title, event_type, photo_key, venue, event_date_label,
    days_until, guests_count, articles_count, total_dop, deposit_dop, paid_dop,
    balance_dop, balance_due_label, status, date_changes_count, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8,
    $9, $10, $11, $12, $13, $14,
    $15, $16, $17, $18, $19
)
ON CONFLICT (id) DO UPDATE SET
    event_date_label = EXCLUDED.event_date_label,
    days_until = EXCLUDED.days_until,
    total_dop = EXCLUDED.total_dop,
    status = EXCLUDED.status,
    date_changes_count = EXCLUDED.date_changes_count;

-- name: ListEvents :many
SELECT id, client_id, client_name, title, event_type, photo_key, venue, event_date_label,
       days_until, guests_count, articles_count, total_dop, deposit_dop, paid_dop,
       balance_dop, balance_due_label, status, date_changes_count, created_at
FROM events
ORDER BY created_at, id;

-- name: GetEventByID :one
SELECT id, client_id, client_name, title, event_type, photo_key, venue, event_date_label,
       days_until, guests_count, articles_count, total_dop, deposit_dop, paid_dop,
       balance_dop, balance_due_label, status, date_changes_count, created_at
FROM events
WHERE id = $1;

-- name: UpdateEventDate :exec
UPDATE events
SET event_date_label = $2,
    days_until = $3,
    date_changes_count = $4,
    total_dop = $5
WHERE id = $1;

-- name: UpdateEventStatus :exec
UPDATE events
SET status = $2
WHERE id = $1;

-- name: UpsertEventTask :exec
INSERT INTO event_tasks (id, event_id, title, assignee_name, completed)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (id) DO UPDATE SET
    completed = EXCLUDED.completed,
    assignee_name = EXCLUDED.assignee_name;
