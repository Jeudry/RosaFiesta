CREATE TABLE
IF NOT EXISTS event_timeline_items
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid
(),
    event_id UUID NOT NULL REFERENCES events
(id) ON
DELETE CASCADE,
    title VARCHAR(255)
NOT NULL,
    description TEXT,
    start_time TIMESTAMP
WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP
WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP
WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX
IF NOT EXISTS idx_timeline_event_id ON event_timeline_items
(event_id);
CREATE INDEX
IF NOT EXISTS idx_timeline_start_time ON event_timeline_items
(start_time);
