ALTER TABLE event_reviews DROP COLUMN IF EXISTS is_verified;
ALTER TABLE event_reviews DROP COLUMN IF EXISTS verified_at;
ALTER TABLE event_reviews DROP COLUMN IF EXISTS verified_by;
ALTER TABLE event_reviews DROP COLUMN IF EXISTS event_photos_count;
ALTER TABLE event_reviews DROP COLUMN IF EXISTS client_confirmed;
