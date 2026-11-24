-- Remediación integral de auditoría: preferencias completas, revocación persistente,
-- favoritos de usuario, tareas de evento, recordatorios enviados y bajas lógicas de staff.

ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS event_reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language TEXT NOT NULL DEFAULT 'es';
ALTER TABLE users ADD COLUMN IF NOT EXISTS dark_theme_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS saved_payment_method TEXT NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_zone TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS revoked_tokens (
    token_hash TEXT PRIMARY KEY,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_revoked_tokens_expires
    ON revoked_tokens (expires_at);

CREATE TABLE IF NOT EXISTS user_favorites (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, article_id)
);

CREATE TABLE IF NOT EXISTS event_team_tasks (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    assignee_name TEXT NOT NULL DEFAULT '',
    done BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_event_team_tasks_event
    ON event_team_tasks (event_id, created_at);

CREATE TABLE IF NOT EXISTS sent_event_reminders (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL,
    user_id UUID NOT NULL,
    reminder_kind TEXT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (event_id, user_id, reminder_kind)
);
