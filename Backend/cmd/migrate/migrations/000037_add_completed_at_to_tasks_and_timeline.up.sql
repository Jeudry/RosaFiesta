-- Add completed_at column to event_tasks
ALTER TABLE event_tasks ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

-- Add completed_at column to event_timeline_items
ALTER TABLE event_timeline_items ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

-- Index for performance in reporting
CREATE INDEX idx_event_tasks_completed_at ON event_tasks(completed_at);
CREATE INDEX idx_event_timeline_items_completed_at ON event_timeline_items(completed_at);
