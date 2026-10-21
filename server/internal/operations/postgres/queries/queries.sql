-- name: UpsertLead :exec
INSERT INTO leads (id, full_name, phone, event_type, estimated_dop, stage, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    stage = EXCLUDED.stage,
    estimated_dop = EXCLUDED.estimated_dop;

-- name: ListLeads :many
SELECT id, full_name, phone, event_type, estimated_dop, stage, created_at
FROM leads
ORDER BY created_at, id;

-- name: UpsertSupplier :exec
INSERT INTO suppliers (id, name, specialty, phone, rating)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    specialty = EXCLUDED.specialty,
    phone = EXCLUDED.phone,
    rating = EXCLUDED.rating;

-- name: ListSuppliers :many
SELECT id, name, specialty, phone, rating
FROM suppliers
ORDER BY id;

-- name: UpsertReview :exec
INSERT INTO reviews (id, event_id, client_name, rating, comment, admin_reply, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    admin_reply = EXCLUDED.admin_reply;
