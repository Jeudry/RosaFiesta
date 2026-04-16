-- Remove quote approval fields from events table
ALTER TABLE events DROP COLUMN IF EXISTS quote_rejected_by;
ALTER TABLE events DROP COLUMN IF EXISTS quote_rejected_at;
ALTER TABLE events DROP COLUMN IF EXISTS quote_approved_by;
ALTER TABLE events DROP COLUMN IF EXISTS quote_approved_at;