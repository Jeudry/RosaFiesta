-- name: UpsertEvent :exec
INSERT INTO events (
    id, code, client_id, client_name, client_email, title, event_type, photo_key, venue, zone,
    event_date_label, event_date_iso, pickup_date_iso, return_date_iso, days_until, guests_count,
    articles_count, total_dop, deposit_dop, paid_dop, paid_deposit_dop, balance_dop, balance_due_label,
    status, date_changes_count, assigned_lead, sketch_json, cancellation_reason, quote_id,
    closeout_completed, closeout_notes, last_payment_reminder_at, last_payment_reminder_channel, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10,
    $11, $12, $13, $14, $15, $16,
    $17, $18, $19, $20, $21, $22, $23,
    $24, $25, $26, $27, $28, $29,
    $30, $31, $32, $33, $34
)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    client_id = EXCLUDED.client_id,
    client_name = EXCLUDED.client_name,
    client_email = EXCLUDED.client_email,
    title = EXCLUDED.title,
    event_type = EXCLUDED.event_type,
    photo_key = EXCLUDED.photo_key,
    venue = EXCLUDED.venue,
    zone = EXCLUDED.zone,
    event_date_label = EXCLUDED.event_date_label,
    event_date_iso = EXCLUDED.event_date_iso,
    pickup_date_iso = EXCLUDED.pickup_date_iso,
    return_date_iso = EXCLUDED.return_date_iso,
    days_until = EXCLUDED.days_until,
    guests_count = EXCLUDED.guests_count,
    articles_count = EXCLUDED.articles_count,
    total_dop = EXCLUDED.total_dop,
    deposit_dop = EXCLUDED.deposit_dop,
    paid_dop = EXCLUDED.paid_dop,
    paid_deposit_dop = EXCLUDED.paid_deposit_dop,
    balance_dop = EXCLUDED.balance_dop,
    balance_due_label = EXCLUDED.balance_due_label,
    status = EXCLUDED.status,
    date_changes_count = EXCLUDED.date_changes_count,
    assigned_lead = EXCLUDED.assigned_lead,
    sketch_json = EXCLUDED.sketch_json,
    cancellation_reason = EXCLUDED.cancellation_reason,
    quote_id = EXCLUDED.quote_id,
    closeout_completed = EXCLUDED.closeout_completed,
    closeout_notes = EXCLUDED.closeout_notes,
    last_payment_reminder_at = EXCLUDED.last_payment_reminder_at,
    last_payment_reminder_channel = EXCLUDED.last_payment_reminder_channel;

-- name: ListEvents :many
SELECT id, code, client_id, client_name, client_email, title, event_type, photo_key, venue, zone,
       event_date_label, event_date_iso, pickup_date_iso, return_date_iso, days_until, guests_count,
       articles_count, total_dop, deposit_dop, paid_dop, paid_deposit_dop, balance_dop, balance_due_label,
       status, date_changes_count, assigned_lead, sketch_json, cancellation_reason, quote_id,
       closeout_completed, closeout_notes, last_payment_reminder_at, last_payment_reminder_channel, created_at
FROM events
ORDER BY created_at, id;

-- name: GetEventByID :one
SELECT id, code, client_id, client_name, client_email, title, event_type, photo_key, venue, zone,
       event_date_label, event_date_iso, pickup_date_iso, return_date_iso, days_until, guests_count,
       articles_count, total_dop, deposit_dop, paid_dop, paid_deposit_dop, balance_dop, balance_due_label,
       status, date_changes_count, assigned_lead, sketch_json, cancellation_reason, quote_id,
       closeout_completed, closeout_notes, last_payment_reminder_at, last_payment_reminder_channel, created_at
FROM events
WHERE id = $1;

-- name: UpdateEventDate :exec
UPDATE events
SET event_date_label = $2,
    event_date_iso = $3,
    pickup_date_iso = $4,
    return_date_iso = $5,
    days_until = $6,
    date_changes_count = $7,
    total_dop = $8,
    balance_dop = $9
WHERE id = $1;

-- name: UpdateEventStatusAndReason :exec
UPDATE events
SET status = $2,
    cancellation_reason = $3
WHERE id = $1;

-- name: DeleteEventItems :exec
DELETE FROM event_items
WHERE event_id = $1;

-- name: UpsertEventItem :exec
INSERT INTO event_items (event_id, article_code, name, quantity, unit_price_dop)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (event_id, article_code) DO UPDATE SET
    name = EXCLUDED.name,
    quantity = EXCLUDED.quantity,
    unit_price_dop = EXCLUDED.unit_price_dop;

-- name: ListAllEventItems :many
SELECT event_id, article_code, name, quantity, unit_price_dop
FROM event_items
ORDER BY event_id, article_code;

-- name: UpsertEventGuest :exec
INSERT INTO event_guests (id, event_id, full_name, rsvp_status, table_label, dietary_notes, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    rsvp_status = EXCLUDED.rsvp_status,
    table_label = EXCLUDED.table_label,
    dietary_notes = EXCLUDED.dietary_notes;

-- name: ListEventGuests :many
SELECT id, event_id, full_name, rsvp_status, table_label, dietary_notes, created_at
FROM event_guests
WHERE event_id = $1
ORDER BY created_at ASC, id ASC;

-- name: UpsertScheduleBlock :exec
INSERT INTO event_schedule_blocks (id, event_id, time_range, title, owner_name, status, sort_order)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    time_range = EXCLUDED.time_range,
    title = EXCLUDED.title,
    owner_name = EXCLUDED.owner_name,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order;

-- name: ListScheduleBlocks :many
SELECT id, event_id, time_range, title, owner_name, status, sort_order
FROM event_schedule_blocks
WHERE event_id = $1
ORDER BY sort_order ASC, id ASC;

-- name: DeleteScheduleBlock :exec
DELETE FROM event_schedule_blocks
WHERE event_id = $1 AND id = $2;

-- name: UpsertChatMessage :exec
INSERT INTO chat_messages (
    id, thread_id, event_id, event_title, sender_id, sender_name,
    sender_role, recipient_label, body, attachment_name, attachment_url, unread, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10, $11, $12, $13
)
ON CONFLICT (id) DO UPDATE SET
    body = EXCLUDED.body,
    attachment_url = EXCLUDED.attachment_url,
    unread = EXCLUDED.unread;

-- name: ListAllChatMessages :many
SELECT id, thread_id, event_id, event_title, sender_id, sender_name,
       sender_role, recipient_label, body, attachment_name, attachment_url, unread, created_at
FROM chat_messages
ORDER BY created_at ASC, id ASC;

-- name: ListChatMessagesByThread :many
SELECT id, thread_id, event_id, event_title, sender_id, sender_name,
       sender_role, recipient_label, body, attachment_name, attachment_url, unread, created_at
FROM chat_messages
WHERE thread_id = $1
ORDER BY created_at ASC, id ASC;
