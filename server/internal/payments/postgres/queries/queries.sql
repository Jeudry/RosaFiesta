-- name: UpsertPayment :exec
INSERT INTO payments (
    id, idempotency_key, event_id, client_name, amount_dop,
    method, paid_date_label, status, created_at
) VALUES (
    $1, $2, $3, $4, $5,
    $6, $7, $8, $9
)
ON CONFLICT (idempotency_key) DO NOTHING;

-- name: ListPayments :many
SELECT id, idempotency_key, event_id, client_name, amount_dop,
       method, paid_date_label, status, created_at
FROM payments
ORDER BY created_at, id;

-- name: UpsertRefund :exec
INSERT INTO refunds (
    id, idempotency_key, event_id, paid_dop, refund_percent, refund_dop, reason, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8
)
ON CONFLICT (idempotency_key) DO NOTHING;
