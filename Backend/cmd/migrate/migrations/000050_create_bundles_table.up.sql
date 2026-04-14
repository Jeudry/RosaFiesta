-- Bundles (e.g., "Bodas Rosa", "Cumpleaños Infantil", "Quinceañera Premium")
CREATE TABLE bundles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_percent DECIMAL(5,2) DEFAULT 0.0,
    image_url TEXT,
    is_active BOOLEAN DEFAULT true,
    min_price DECIMAL(10,2) DEFAULT 0.0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Articles in each bundle
CREATE TABLE bundle_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_id UUID NOT NULL REFERENCES bundles(id) ON DELETE CASCADE,
    article_id UUID NOT NULL REFERENCES articles(id),
    quantity INT NOT NULL DEFAULT 1,
    is_optional BOOLEAN DEFAULT false,
    sort_order INT DEFAULT 0
);

CREATE INDEX idx_bundle_items_bundle_id ON bundle_items(bundle_id);
CREATE INDEX idx_bundle_items_article_id ON bundle_items(article_id);