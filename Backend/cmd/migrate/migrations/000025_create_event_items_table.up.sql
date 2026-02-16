CREATE TABLE
IF NOT EXISTS event_items
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid
(),
    event_id UUID NOT NULL REFERENCES events
(id) ON
DELETE CASCADE,
    article_id UUID
NOT NULL REFERENCES articles
(id) ON
DELETE CASCADE,
    quantity INT
NOT NULL DEFAULT 1,
    created_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
(),
    updated_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
(),
    UNIQUE
(event_id, article_id)
);

CREATE INDEX
IF NOT EXISTS idx_event_items_event_id ON event_items
(event_id);
