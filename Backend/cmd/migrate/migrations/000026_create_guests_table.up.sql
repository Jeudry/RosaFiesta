
CREATE TABLE
IF NOT EXISTS guests
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid
(),
    event_id UUID NOT NULL REFERENCES events
(id) ON
DELETE CASCADE,
    name VARCHAR(255)
NOT NULL,
    email VARCHAR
(255),
    phone VARCHAR
(50),
    rsvp_status VARCHAR
(50) DEFAULT 'pending', -- pending, confirmed, declined
    plus_one BOOLEAN DEFAULT FALSE,
    dietary_restrictions TEXT,
    created_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
(),
    updated_at TIMESTAMP
WITH TIME ZONE DEFAULT NOW
()
);

CREATE INDEX idx_guests_event_id ON guests(event_id);
