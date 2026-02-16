CREATE TABLE
IF NOT EXISTS events
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4
(),
    user_id UUID NOT NULL REFERENCES users
(id) ON
DELETE CASCADE,
    name VARCHAR(255)
NOT NULL,
    date TIMESTAMP
WITH TIME ZONE,
    location VARCHAR
(255),
    guest_count INT DEFAULT 0,
    budget DECIMAL
(12, 2) DEFAULT 0.00,
    status VARCHAR
(50) NOT NULL DEFAULT 'planning', -- planning, confirmed, completed
    created_at TIMESTAMP
(0)
WITH TIME ZONE NOT NULL DEFAULT NOW
(),
    updated_at TIMESTAMP
(0)
WITH TIME ZONE NOT NULL DEFAULT NOW
()
);

CREATE INDEX idx_events_user_id ON events(user_id);
