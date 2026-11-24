-- Persist configurable quote deposit percent and add missing query indexes.
ALTER TABLE quotes
    ADD COLUMN IF NOT EXISTS deposit_percent INTEGER NOT NULL DEFAULT 30;

CREATE INDEX IF NOT EXISTS idx_sessions_user_expires ON sessions (user_id, expires_at);
CREATE INDEX IF NOT EXISTS idx_events_client_id ON events (client_id);
CREATE INDEX IF NOT EXISTS idx_event_guests_event_id ON event_guests (event_id);
CREATE INDEX IF NOT EXISTS idx_event_schedule_blocks_event_id ON event_schedule_blocks (event_id);
CREATE INDEX IF NOT EXISTS idx_event_tasks_event_id ON event_tasks (event_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_thread_id ON chat_messages (thread_id);
CREATE INDEX IF NOT EXISTS idx_payments_event_id ON payments (event_id);
CREATE INDEX IF NOT EXISTS idx_damage_incidents_event_id ON damage_incidents (event_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_event_id ON reviews (event_id);
