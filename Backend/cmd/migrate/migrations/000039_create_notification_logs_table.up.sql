CREATE TABLE
IF NOT EXISTS notification_logs
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid
(),
    event_id UUID NOT NULL REFERENCES events
(id) ON
DELETE CASCADE,
    type VARCHAR(255)
NOT NULL, -- "pre-event-reminder" or "post-event-review"
    sent_at TIMESTAMP
WITH TIME ZONE NOT NULL DEFAULT NOW
(),
    UNIQUE
(event_id, type)
);

CREATE INDEX idx_notification_logs_event_id ON notification_logs(event_id);
