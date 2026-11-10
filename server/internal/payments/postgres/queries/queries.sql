-- name: UpsertPayment :exec
INSERT INTO payments (
    id, receipt_number, idempotency_key, event_id, event_title, client_id,
    client_email, client_name, amount_dop, method, kind, bank_name,
    paid_date_label, status, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10, $11, $12,
    $13, $14, $15
)
ON CONFLICT (idempotency_key) DO UPDATE SET
    status = EXCLUDED.status,
    bank_name = EXCLUDED.bank_name,
    receipt_number = EXCLUDED.receipt_number,
    event_title = EXCLUDED.event_title,
    kind = EXCLUDED.kind;

-- name: ListPayments :many
SELECT id, receipt_number, idempotency_key, event_id, event_title, client_id,
       client_email, client_name, amount_dop, method, kind, bank_name,
       paid_date_label, status, created_at
FROM payments
ORDER BY created_at, id;

-- name: UpsertRefund :exec
INSERT INTO refunds (
    id, idempotency_key, event_id, event_title, client_name, paid_dop,
    refund_percent, refund_dop, reason, status, approved_by, approved_at_label, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10, $11, $12, $13
)
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    approved_by = EXCLUDED.approved_by,
    approved_at_label = EXCLUDED.approved_at_label,
    reason = EXCLUDED.reason;

-- name: ListRefunds :many
SELECT id, idempotency_key, event_id, event_title, client_name, paid_dop,
       refund_percent, refund_dop, reason, status, approved_by, approved_at_label, created_at
FROM refunds
ORDER BY created_at DESC, id ASC;
