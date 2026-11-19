-- name: UpsertPayment :exec
INSERT INTO payments (
    id, receipt_number, idempotency_key, event_id, event_title, client_id,
    client_email, client_name, amount_dop, method, kind, bank_name,
    paid_date_label, status, voucher_url, reference_number, verified_by,
    verified_at_iso, rejection_reason, ncf_type, ncf_number, client_rnc,
    subtotal_dop, itbis_dop, security_deposit_dop, gateway_session_id,
    gateway_payment_intent_id, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10, $11, $12,
    $13, $14, $15, $16, $17,
    $18, $19, $20, $21, $22,
    $23, $24, $25, $26,
    $27, $28
)
ON CONFLICT (idempotency_key) DO UPDATE SET
    status = EXCLUDED.status,
    bank_name = EXCLUDED.bank_name,
    receipt_number = EXCLUDED.receipt_number,
    event_title = EXCLUDED.event_title,
    kind = EXCLUDED.kind,
    voucher_url = EXCLUDED.voucher_url,
    reference_number = EXCLUDED.reference_number,
    verified_by = EXCLUDED.verified_by,
    verified_at_iso = EXCLUDED.verified_at_iso,
    rejection_reason = EXCLUDED.rejection_reason,
    ncf_type = EXCLUDED.ncf_type,
    ncf_number = EXCLUDED.ncf_number,
    client_rnc = EXCLUDED.client_rnc,
    subtotal_dop = EXCLUDED.subtotal_dop,
    itbis_dop = EXCLUDED.itbis_dop,
    security_deposit_dop = EXCLUDED.security_deposit_dop,
    gateway_session_id = EXCLUDED.gateway_session_id,
    gateway_payment_intent_id = EXCLUDED.gateway_payment_intent_id;

-- name: ListPayments :many
SELECT id, receipt_number, idempotency_key, event_id, event_title, client_id,
       client_email, client_name, amount_dop, method, kind, bank_name,
       paid_date_label, status, voucher_url, reference_number, verified_by,
       verified_at_iso, rejection_reason, ncf_type, ncf_number, client_rnc,
       subtotal_dop, itbis_dop, security_deposit_dop, gateway_session_id,
       gateway_payment_intent_id, created_at
FROM payments
ORDER BY created_at, id;

-- name: AllocateNextNCF :one
UPDATE ncf_sequences
SET last_number = last_number + 1
WHERE ncf_type = $1
  AND last_number < max_number
RETURNING prefix, last_number;

-- name: UpsertRefund :exec
INSERT INTO refunds (
    id, idempotency_key, event_id, event_title, client_name, paid_dop,
    refund_percent, refund_dop, reason, status, approved_by, approved_at_label,
    gateway_refund_id, gateway_status, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10, $11, $12,
    $13, $14, $15
)
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    approved_by = EXCLUDED.approved_by,
    approved_at_label = EXCLUDED.approved_at_label,
    gateway_refund_id = EXCLUDED.gateway_refund_id,
    gateway_status = EXCLUDED.gateway_status,
    reason = EXCLUDED.reason;

-- name: ListRefunds :many
SELECT id, idempotency_key, event_id, event_title, client_name, paid_dop,
       refund_percent, refund_dop, reason, status, approved_by, approved_at_label,
       gateway_refund_id, gateway_status, created_at
FROM refunds
ORDER BY created_at DESC, id ASC;
