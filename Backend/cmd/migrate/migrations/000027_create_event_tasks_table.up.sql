
CREATE TABLE
IF NOT EXISTS event_tasks
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid
(),
    event_id UUID NOT NULL REFERENCES events
(id) ON
DELETE CASCADE,
    title VARCHAR(255)
NOT NULL,
    description TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    due_date TIMESTAMP
WITH TIME ZONE,
    created_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
(),
    updated_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
()
);

CREATE INDEX idx_event_tasks_event_id ON event_tasks(event_id);
