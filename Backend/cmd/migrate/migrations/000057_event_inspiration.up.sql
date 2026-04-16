-- Mood Board / Inspiration Photos for events
CREATE TABLE event_inspiration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    caption TEXT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_event_inspiration_event_id ON event_inspiration(event_id);
CREATE INDEX idx_event_inspiration_uploaded_at ON event_inspiration(uploaded_at);