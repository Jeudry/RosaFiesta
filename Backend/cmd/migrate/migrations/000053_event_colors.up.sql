-- Migration: 000053_event_colors
-- Create event_colors table for storing user's color palette for their event

CREATE TABLE IF NOT EXISTS event_colors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    color_hex VARCHAR(7) NOT NULL, -- #RRGGBB format
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Unique constraint: no duplicate colors per event
CREATE UNIQUE INDEX IF NOT EXISTS idx_event_colors_event_color ON event_colors(event_id, color_hex);

-- Index for fast lookups by event
CREATE INDEX IF NOT EXISTS idx_event_colors_event_id ON event_colors(event_id);