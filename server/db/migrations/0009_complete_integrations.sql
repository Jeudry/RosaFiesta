-- Migración integral: preferencias de usuario persistentes, trazabilidad de recordatorios de pago
-- y unicidad idempotente por sesión de pasarela de pagos.

ALTER TABLE users ADD COLUMN IF NOT EXISTS push_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS whatsapp_alerts_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_receipts_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS biometric_lock_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS auto_sync_wifi_only BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE events ADD COLUMN IF NOT EXISTS last_payment_reminder_at TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS last_payment_reminder_channel TEXT NOT NULL DEFAULT '';

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_gateway_session_id
    ON payments (gateway_session_id) WHERE gateway_session_id <> '';
