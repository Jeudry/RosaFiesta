-- Make events able to behave as drafts (nameless, dateless) so they can act
-- as the user's "active event" while they browse the catalog.

-- 1. Allow date to be NULL — drafts may not have a chosen date yet.
ALTER TABLE events ALTER COLUMN date DROP NOT NULL;

-- 2. Allow name to be empty/default — drafts don't need a name until they
--    are confirmed. Existing rows that are non-empty stay untouched.
ALTER TABLE events ALTER COLUMN name DROP NOT NULL;

-- 3. Default new events to 'draft' (was 'planning'). Existing rows keep
--    whatever status they already had.
ALTER TABLE events ALTER COLUMN status SET DEFAULT 'draft';

-- 4. Add a CHECK so the status column is always one of the known values.
--    'planning' is preserved as a legacy value to avoid breaking old rows.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'events_status_check'
    ) THEN
        ALTER TABLE events
            ADD CONSTRAINT events_status_check
            CHECK (status IN (
                'draft', 'planning', 'requested', 'adjusted',
                'confirmed', 'paid', 'completed', 'cancelled'
            ));
    END IF;
END $$;

-- 5. Index to quickly find a user's single draft event.
CREATE UNIQUE INDEX IF NOT EXISTS events_user_active_draft
    ON events (user_id)
    WHERE status = 'draft';

-- 6. event_items: add variant_id and price_snapshot to match what cart_items
--    used to capture, so the catalog "+" button can record exactly which
--    variant the user picked and the price at the moment of adding.
ALTER TABLE event_items
    ADD COLUMN IF NOT EXISTS variant_id UUID REFERENCES article_variants(id) ON DELETE SET NULL;
ALTER TABLE event_items
    ADD COLUMN IF NOT EXISTS price_snapshot DECIMAL(12, 2);

-- 7. The old (event_id, article_id) UNIQUE prevented adding two variants of
--    the same article. Drop it and replace with a per-variant uniqueness so
--    "Tiffany Rosa" and "Tiffany Negra" can coexist as separate line items.
ALTER TABLE event_items DROP CONSTRAINT IF EXISTS event_items_event_id_article_id_key;
CREATE UNIQUE INDEX IF NOT EXISTS event_items_event_article_variant_unique
    ON event_items (event_id, article_id, COALESCE(variant_id, '00000000-0000-0000-0000-000000000000'::uuid));
