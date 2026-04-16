-- Add quote approval fields to events table
ALTER TABLE events ADD COLUMN IF NOT EXISTS quote_approved_at TIMESTAMP;
ALTER TABLE events ADD COLUMN IF NOT EXISTS quote_approved_by UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE events ADD COLUMN IF NOT EXISTS quote_rejected_at TIMESTAMP;
ALTER TABLE events ADD COLUMN IF NOT EXISTS quote_rejected_by UUID REFERENCES users(id) ON DELETE SET NULL;