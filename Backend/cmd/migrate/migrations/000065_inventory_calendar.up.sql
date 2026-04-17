-- Feature #2: Live Inventory Calendar
-- Real-time availability by date for all articles

CREATE TABLE IF NOT EXISTS inventory_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES article_variants(id),
    event_id UUID REFERENCES events(id),
    event_date DATE NOT NULL,
    quantity_used INTEGER NOT NULL DEFAULT 0,
    status TEXT DEFAULT 'reserved' CHECK (status IN ('reserved', 'confirmed', 'delivered', 'returned')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_inventory_availability_article ON inventory_availability(article_id);
CREATE INDEX idx_inventory_availability_date ON inventory_availability(event_date);
CREATE INDEX idx_inventory_availability_event ON inventory_availability(event_id);
