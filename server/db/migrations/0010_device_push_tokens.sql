-- Migración de tokens de notificaciones push por dispositivo y variante de aplicación.

CREATE TABLE IF NOT EXISTS device_push_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform TEXT NOT NULL,
    token TEXT NOT NULL,
    app_variant TEXT NOT NULL DEFAULT 'personal',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_device_push_token UNIQUE (user_id, platform, token)
);

CREATE INDEX IF NOT EXISTS idx_device_push_tokens_user_enabled
    ON device_push_tokens (user_id, enabled)
    WHERE enabled = TRUE;
