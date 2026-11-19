-- Trazabilidad de intents/reembolsos en Stripe e índice para limpieza de refresh tokens vencidos.

ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_payment_intent_id TEXT NOT NULL DEFAULT '';

ALTER TABLE refunds ADD COLUMN IF NOT EXISTS gateway_refund_id TEXT NOT NULL DEFAULT '';
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS gateway_status TEXT NOT NULL DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_cleanup
    ON refresh_tokens (revoked, expires_at, updated_at);
