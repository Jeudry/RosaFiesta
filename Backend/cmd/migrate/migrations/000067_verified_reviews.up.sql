-- Feature #14: Verified Reviews with Event Photos
-- Add verification status to reviews

ALTER TABLE event_reviews ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT false;
ALTER TABLE event_reviews ADD COLUMN IF NOT EXISTS verified_at TIMESTAMPTZ;
ALTER TABLE event_reviews ADD COLUMN IF NOT EXISTS verified_by UUID REFERENCES users(id);
ALTER TABLE event_reviews ADD COLUMN IF NOT EXISTS event_photos_count INTEGER DEFAULT 0;
ALTER TABLE event_reviews ADD COLUMN IF NOT EXISTS client_confirmed BOOLEAN DEFAULT false;

CREATE INDEX idx_event_reviews_verified ON event_reviews(is_verified);
