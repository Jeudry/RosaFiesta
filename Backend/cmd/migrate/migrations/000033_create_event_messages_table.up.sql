CREATE TABLE
IF NOT EXISTS event_messages
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid
(),
    event_id UUID NOT NULL REFERENCES events
(id) ON
DELETE CASCADE,
    sender_id UUID
NOT NULL REFERENCES users
(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
()
);

-- Optimize message retrieval by event
CREATE INDEX idx_event_messages_event_id ON event_messages(event_id);
