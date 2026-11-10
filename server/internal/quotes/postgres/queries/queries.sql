-- name: UpsertQuote :exec
INSERT INTO quotes (
    id, code, event_id, client_id, client_email, client_name, event_title,
    event_date_label, venue, guests, lines_json, delivery_fee_dop, discount_dop,
    total_dop, deposit_dop, balance_dop, status, version, notes,
    signature_svg, signed_by_name, rejection_reason, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7,
    $8, $9, $10, $11, $12, $13,
    $14, $15, $16, $17, $18, $19,
    $20, $21, $22, $23
)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    event_id = EXCLUDED.event_id,
    client_id = EXCLUDED.client_id,
    client_email = EXCLUDED.client_email,
    client_name = EXCLUDED.client_name,
    event_title = EXCLUDED.event_title,
    event_date_label = EXCLUDED.event_date_label,
    venue = EXCLUDED.venue,
    guests = EXCLUDED.guests,
    lines_json = EXCLUDED.lines_json,
    delivery_fee_dop = EXCLUDED.delivery_fee_dop,
    discount_dop = EXCLUDED.discount_dop,
    total_dop = EXCLUDED.total_dop,
    deposit_dop = EXCLUDED.deposit_dop,
    balance_dop = EXCLUDED.balance_dop,
    status = EXCLUDED.status,
    notes = EXCLUDED.notes,
    version = EXCLUDED.version,
    signature_svg = EXCLUDED.signature_svg,
    signed_by_name = EXCLUDED.signed_by_name,
    rejection_reason = EXCLUDED.rejection_reason;

-- name: ListQuotes :many
SELECT id, code, event_id, client_id, client_email, client_name, event_title,
       event_date_label, venue, guests, lines_json, delivery_fee_dop, discount_dop,
       total_dop, deposit_dop, balance_dop, status, version, notes,
       signature_svg, signed_by_name, rejection_reason, created_at
FROM quotes
ORDER BY created_at, id;

-- name: GetQuoteByID :one
SELECT id, code, event_id, client_id, client_email, client_name, event_title,
       event_date_label, venue, guests, lines_json, delivery_fee_dop, discount_dop,
       total_dop, deposit_dop, balance_dop, status, version, notes,
       signature_svg, signed_by_name, rejection_reason, created_at
FROM quotes
WHERE id = $1;
