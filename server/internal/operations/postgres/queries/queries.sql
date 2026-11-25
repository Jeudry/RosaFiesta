-- name: UpsertChecklistTask :exec
INSERT INTO event_tasks (id, event_id, stage, title, assignee_name, completed, completed_at_label)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    stage = EXCLUDED.stage,
    completed = EXCLUDED.completed,
    assignee_name = EXCLUDED.assignee_name,
    completed_at_label = EXCLUDED.completed_at_label;

-- name: ListChecklistTasksByEvent :many
SELECT id, event_id, stage, title, assignee_name, completed, completed_at_label
FROM event_tasks
WHERE event_id = $1
ORDER BY id;

-- name: UpsertDamageIncident :exec
INSERT INTO damage_incidents (
    id, event_id, article_code, article_name, damaged_units,
    missing_units, replacement_dop, notes, photo_url, resolution_notes, charge_payment_id, status, created_at
) VALUES (
    $1, $2, $3, $4, $5,
    $6, $7, $8, $9, $10, $11, $12, $13
)
ON CONFLICT (id) DO UPDATE SET
    damaged_units = EXCLUDED.damaged_units,
    missing_units = EXCLUDED.missing_units,
    replacement_dop = EXCLUDED.replacement_dop,
    notes = EXCLUDED.notes,
    photo_url = EXCLUDED.photo_url,
    resolution_notes = EXCLUDED.resolution_notes,
    charge_payment_id = EXCLUDED.charge_payment_id,
    status = EXCLUDED.status;

-- name: ListDamageIncidents :many
SELECT id, event_id, article_code, article_name, damaged_units,
       missing_units, replacement_dop, notes, photo_url, resolution_notes, charge_payment_id, status, created_at
FROM damage_incidents
ORDER BY created_at DESC, id ASC;

-- name: ListDamageIncidentsByEvent :many
SELECT id, event_id, article_code, article_name, damaged_units,
       missing_units, replacement_dop, notes, photo_url, resolution_notes, charge_payment_id, status, created_at
FROM damage_incidents
WHERE event_id = $1
ORDER BY created_at ASC, id ASC;

-- name: UpsertOfflineMutation :exec
INSERT INTO offline_queue (
    id, action_label, entity_code, operation_type, payload_json, idempotency_key, created_at_label, synced, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9
)
ON CONFLICT (id) DO UPDATE SET
    synced = EXCLUDED.synced,
    operation_type = EXCLUDED.operation_type,
    payload_json = EXCLUDED.payload_json;

-- name: ListOfflineMutations :many
SELECT id, action_label, entity_code, operation_type, payload_json, idempotency_key, created_at_label, synced, created_at
FROM offline_queue
ORDER BY created_at ASC, id ASC;

-- name: MarkAllOfflineMutationsSynced :exec
UPDATE offline_queue
SET synced = TRUE
WHERE synced = FALSE;

-- name: MarkOfflineMutationSyncedByID :exec
UPDATE offline_queue
SET synced = TRUE
WHERE id = $1;

-- name: UpsertLead :exec
INSERT INTO leads (id, full_name, phone, email, event_type, event_date_label, guests, estimated_dop, stage, notes, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    event_type = EXCLUDED.event_type,
    event_date_label = EXCLUDED.event_date_label,
    guests = EXCLUDED.guests,
    stage = EXCLUDED.stage,
    estimated_dop = EXCLUDED.estimated_dop,
    notes = EXCLUDED.notes;

-- name: ListLeads :many
SELECT id, full_name, phone, email, event_type, event_date_label, guests, estimated_dop, stage, notes, created_at
FROM leads
ORDER BY created_at, id;

-- name: UpsertSupplier :exec
INSERT INTO suppliers (id, name, category, specialty, contact_name, phone, email, website, rating, notes)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    specialty = EXCLUDED.specialty,
    contact_name = EXCLUDED.contact_name,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    website = EXCLUDED.website,
    rating = EXCLUDED.rating,
    notes = EXCLUDED.notes;

-- name: ListSuppliers :many
SELECT id, name, category, specialty, contact_name, phone, email, website, rating, notes
FROM suppliers
ORDER BY id;

-- name: UpsertReview :exec
INSERT INTO reviews (id, event_id, client_name, rating, comment, admin_reply, verified, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
ON CONFLICT (id) DO UPDATE SET
    admin_reply = EXCLUDED.admin_reply,
    verified = EXCLUDED.verified,
    comment = EXCLUDED.comment,
    rating = EXCLUDED.rating;

-- name: ListReviews :many
SELECT id, event_id, client_name, rating, comment, admin_reply, verified, created_at
FROM reviews
ORDER BY created_at DESC, id ASC;

-- name: UpsertBusinessSettings :exec
INSERT INTO business_settings (
    id, company_name, rnc, address, phone, whatsapp, email,
    instagram, facebook, business_hours, itbis_percent, deposit_percent,
    whatsapp_auto_alerts_enabled, updated_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7,
    $8, $9, $10, $11, $12,
    $13, $14
)
ON CONFLICT (id) DO UPDATE SET
    company_name = EXCLUDED.company_name,
    rnc = EXCLUDED.rnc,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    whatsapp = EXCLUDED.whatsapp,
    email = EXCLUDED.email,
    instagram = EXCLUDED.instagram,
    facebook = EXCLUDED.facebook,
    business_hours = EXCLUDED.business_hours,
    itbis_percent = EXCLUDED.itbis_percent,
    deposit_percent = EXCLUDED.deposit_percent,
    whatsapp_auto_alerts_enabled = EXCLUDED.whatsapp_auto_alerts_enabled,
    updated_at = EXCLUDED.updated_at;

-- name: GetBusinessSettings :one
SELECT id, company_name, rnc, address, phone, whatsapp, email,
       instagram, facebook, business_hours, itbis_percent, deposit_percent,
       whatsapp_auto_alerts_enabled, updated_at
FROM business_settings
WHERE id = 'default';

-- name: UpsertDeliveryZone :exec
INSERT INTO delivery_zones (id, name, fee_dop, eta_minutes, active)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    fee_dop = EXCLUDED.fee_dop,
    eta_minutes = EXCLUDED.eta_minutes,
    active = EXCLUDED.active;

-- name: DeleteDeliveryZonesNotIn :exec
DELETE FROM delivery_zones
WHERE NOT (id = ANY($1::text[]));

-- name: ListDeliveryZones :many
SELECT id, name, fee_dop, eta_minutes, active
FROM delivery_zones
ORDER BY id ASC;

-- name: UpsertNotification :exec
INSERT INTO notifications (
    id, recipient_role, user_id, title, body, category, action_path, is_read, created_at_label, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10
)
ON CONFLICT (id) DO UPDATE SET
    is_read = EXCLUDED.is_read;

-- name: ListNotifications :many
SELECT id, recipient_role, user_id, title, body, category, action_path, is_read, created_at_label, created_at
FROM notifications
ORDER BY created_at DESC, id ASC;

-- name: MarkNotificationRead :exec
UPDATE notifications
SET is_read = TRUE
WHERE id = $1;

-- name: UpsertInsurancePolicy :exec
INSERT INTO insurance_policies (id, provider_name, policy_number, coverage_type, max_coverage_dop, valid_until_label, status)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    max_coverage_dop = EXCLUDED.max_coverage_dop,
    valid_until_label = EXCLUDED.valid_until_label;

-- name: ListInsurancePolicies :many
SELECT id, provider_name, policy_number, coverage_type, max_coverage_dop, valid_until_label, status
FROM insurance_policies
ORDER BY id ASC;

-- name: UpsertAuditLog :exec
INSERT INTO audit_logs (id, actor_name, actor_role, action, entity_type, entity_id, summary, created_at_label, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
ON CONFLICT (id) DO NOTHING;

-- name: ListAuditLogs :many
SELECT id, actor_name, actor_role, action, entity_type, entity_id, summary, created_at_label, created_at
FROM audit_logs
ORDER BY created_at DESC, id ASC;

-- name: UpsertBotFAQ :exec
INSERT INTO bot_faqs (id, question, answer, category, active)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (id) DO UPDATE SET
    question = EXCLUDED.question,
    answer = EXCLUDED.answer,
    category = EXCLUDED.category,
    active = EXCLUDED.active;

-- name: ListBotFAQs :many
SELECT id, question, answer, category, active
FROM bot_faqs
ORDER BY id ASC;

-- name: UpsertBotKeyword :exec
INSERT INTO bot_keywords (id, keyword, auto_reply, handoff_to_human)
VALUES ($1, $2, $3, $4)
ON CONFLICT (id) DO UPDATE SET
    keyword = EXCLUDED.keyword,
    auto_reply = EXCLUDED.auto_reply,
    handoff_to_human = EXCLUDED.handoff_to_human;

-- name: ListBotKeywords :many
SELECT id, keyword, auto_reply, handoff_to_human
FROM bot_keywords
ORDER BY id ASC;

-- name: UpsertBotThread :exec
INSERT INTO bot_threads (id, customer_name, phone, last_message, bot_paused, assigned_agent, updated_at_label)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    last_message = EXCLUDED.last_message,
    bot_paused = EXCLUDED.bot_paused,
    assigned_agent = EXCLUDED.assigned_agent,
    updated_at_label = EXCLUDED.updated_at_label;

-- name: ListBotThreads :many
SELECT id, customer_name, phone, last_message, bot_paused, assigned_agent, updated_at_label
FROM bot_threads
ORDER BY id ASC;

-- name: UpsertEventLogistics :exec
INSERT INTO event_logistics (event_id, event_title, zone, window_label, driver_name, vehicle, status, updated_at_label)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
ON CONFLICT (event_id) DO UPDATE SET
    event_title = EXCLUDED.event_title,
    zone = EXCLUDED.zone,
    window_label = EXCLUDED.window_label,
    driver_name = EXCLUDED.driver_name,
    vehicle = EXCLUDED.vehicle,
    status = EXCLUDED.status,
    updated_at_label = EXCLUDED.updated_at_label;

-- name: ListEventLogistics :many
SELECT event_id, event_title, zone, window_label, driver_name, vehicle, status, updated_at_label
FROM event_logistics
ORDER BY event_id ASC;

-- name: DeleteDeliveryStopsByEvent :exec
DELETE FROM delivery_stops
WHERE event_id = $1;

-- name: UpsertDeliveryStop :exec
INSERT INTO delivery_stops (id, event_id, stop_order, title, address, time_label, completed)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    stop_order = EXCLUDED.stop_order,
    title = EXCLUDED.title,
    address = EXCLUDED.address,
    time_label = EXCLUDED.time_label,
    completed = EXCLUDED.completed;

-- name: ListAllDeliveryStops :many
SELECT id, event_id, stop_order, title, address, time_label, completed
FROM delivery_stops
ORDER BY event_id ASC, stop_order ASC;

-- name: DeleteSupplier :exec
DELETE FROM suppliers
WHERE id = $1;

-- name: DeleteBotFAQ :exec
DELETE FROM bot_faqs
WHERE id = $1;

-- name: DeleteBotKeyword :exec
DELETE FROM bot_keywords
WHERE id = $1;

-- name: HasSentEventReminder :one
SELECT EXISTS (
    SELECT 1
    FROM sent_event_reminders
    WHERE event_id = $1 AND user_id = $2 AND reminder_kind = $3
);

-- name: InsertSentEventReminder :exec
INSERT INTO sent_event_reminders (id, event_id, user_id, reminder_kind, sent_at)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (event_id, user_id, reminder_kind) DO NOTHING;
