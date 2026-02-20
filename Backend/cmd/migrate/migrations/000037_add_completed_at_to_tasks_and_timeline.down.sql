DROP INDEX IF EXISTS idx_event_timeline_items_completed_at;
DROP INDEX IF EXISTS idx_event_tasks_completed_at;
ALTER TABLE event_timeline_items DROP COLUMN IF EXISTS completed_at;
ALTER TABLE event_tasks DROP COLUMN IF EXISTS completed_at;
