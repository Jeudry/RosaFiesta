-- Actualización funcional integral: medios binarios, ledger de reservas por fecha,
-- NCF / ITBIS / comprobantes bancarios, closeout, adjuntos de chat, outbox e itinerario logístico.

CREATE TABLE IF NOT EXISTS media_assets (
    id TEXT PRIMARY KEY,
    owner_id TEXT NOT NULL DEFAULT '',
    owner_role TEXT NOT NULL DEFAULT '',
    purpose TEXT NOT NULL DEFAULT 'general',
    filename TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    byte_size BIGINT NOT NULL CHECK (byte_size > 0),
    sha256 TEXT NOT NULL DEFAULT '',
    storage_path TEXT NOT NULL,
    public_url TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_media_assets_owner
    ON media_assets (owner_id, created_at DESC);

ALTER TABLE articles ADD COLUMN IF NOT EXISTS photo_url TEXT NOT NULL DEFAULT '';
ALTER TABLE articles ADD COLUMN IF NOT EXISTS damaged_stock INT NOT NULL DEFAULT 0;
ALTER TABLE packages ADD COLUMN IF NOT EXISTS photo_url TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS stock_reservations (
    id TEXT PRIMARY KEY,
    article_id TEXT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    event_id TEXT NOT NULL,
    quote_id TEXT NOT NULL DEFAULT '',
    quantity INT NOT NULL CHECK (quantity > 0),
    pickup_date_iso TEXT NOT NULL,
    return_date_iso TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('held', 'confirmed', 'released', 'cancelled')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stock_reservations_lookup
    ON stock_reservations (article_id, status, pickup_date_iso, return_date_iso);

CREATE INDEX IF NOT EXISTS idx_stock_reservations_event
    ON stock_reservations (event_id);

ALTER TABLE events ADD COLUMN IF NOT EXISTS event_date_iso TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS pickup_date_iso TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS return_date_iso TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS quote_id TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS closeout_completed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE events ADD COLUMN IF NOT EXISTS closeout_notes TEXT NOT NULL DEFAULT '';

ALTER TABLE quotes ADD COLUMN IF NOT EXISTS event_date_iso TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS package_id TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS contract_pdf_url TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS signed_at_iso TEXT NOT NULL DEFAULT '';

ALTER TABLE payments ADD COLUMN IF NOT EXISTS voucher_url TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS reference_number TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS verified_by TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS verified_at_iso TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS rejection_reason TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS ncf_type TEXT NOT NULL DEFAULT 'B02';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS ncf_number TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS client_rnc TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS subtotal_dop INT NOT NULL DEFAULT 0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS itbis_dop INT NOT NULL DEFAULT 0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS security_deposit_dop INT NOT NULL DEFAULT 0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_session_id TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS ncf_sequences (
    ncf_type TEXT PRIMARY KEY,
    prefix TEXT NOT NULL,
    last_number BIGINT NOT NULL DEFAULT 0,
    max_number BIGINT NOT NULL DEFAULT 99999999,
    valid_until_iso TEXT NOT NULL DEFAULT '2027-12-31'
);

INSERT INTO ncf_sequences (ncf_type, prefix, last_number, max_number, valid_until_iso)
VALUES
    ('B01', 'B01', 0, 99999999, '2027-12-31'),
    ('B02', 'B02', 0, 99999999, '2027-12-31')
ON CONFLICT (ncf_type) DO NOTHING;

ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS attachment_url TEXT NOT NULL DEFAULT '';

ALTER TABLE damage_incidents ADD COLUMN IF NOT EXISTS photo_url TEXT NOT NULL DEFAULT '';
ALTER TABLE damage_incidents ADD COLUMN IF NOT EXISTS resolution_notes TEXT NOT NULL DEFAULT '';
ALTER TABLE damage_incidents ADD COLUMN IF NOT EXISTS charge_payment_id TEXT NOT NULL DEFAULT '';

ALTER TABLE offline_queue ADD COLUMN IF NOT EXISTS operation_type TEXT NOT NULL DEFAULT '';
ALTER TABLE offline_queue ADD COLUMN IF NOT EXISTS payload_json JSONB NOT NULL DEFAULT '{}'::jsonb;
ALTER TABLE offline_queue ADD COLUMN IF NOT EXISTS idempotency_key TEXT NOT NULL DEFAULT '';

CREATE UNIQUE INDEX IF NOT EXISTS idx_offline_queue_idempotency
    ON offline_queue (idempotency_key) WHERE idempotency_key <> '';

CREATE TABLE IF NOT EXISTS event_logistics (
    event_id TEXT PRIMARY KEY,
    event_title TEXT NOT NULL,
    zone TEXT NOT NULL DEFAULT '',
    window_label TEXT NOT NULL DEFAULT '',
    driver_name TEXT NOT NULL DEFAULT '',
    vehicle TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'programada',
    updated_at_label TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS delivery_stops (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES event_logistics(event_id) ON DELETE CASCADE,
    stop_order INT NOT NULL,
    title TEXT NOT NULL,
    address TEXT NOT NULL,
    time_label TEXT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_delivery_stops_event
    ON delivery_stops (event_id, stop_order);
