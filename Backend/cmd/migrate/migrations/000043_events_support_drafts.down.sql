-- Reverse 000043_events_support_drafts.up.sql

DROP INDEX IF EXISTS event_items_event_article_variant_unique;
ALTER TABLE event_items DROP COLUMN IF EXISTS price_snapshot;
ALTER TABLE event_items DROP COLUMN IF EXISTS variant_id;
-- Restore the old uniqueness constraint
ALTER TABLE event_items
    ADD CONSTRAINT event_items_event_id_article_id_key UNIQUE (event_id, article_id);

DROP INDEX IF EXISTS events_user_active_draft;
ALTER TABLE events DROP CONSTRAINT IF EXISTS events_status_check;
ALTER TABLE events ALTER COLUMN status SET DEFAULT 'planning';
ALTER TABLE events ALTER COLUMN name SET NOT NULL;
ALTER TABLE events ALTER COLUMN date SET NOT NULL;
