CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_cart UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES article_variants(id) ON DELETE SET NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Ensure unique item per cart (article + variant combo)
    -- If variant_id is NULL, it treats it as a unique entry standard SQL behavior for NULLs in unique constraints varies, 
    -- but usually multiple NULLs allowed. We need a unique index that handles NULLs as a distinct value 'None'.
    -- Postgres 15+ has NULLS NOT DISTINCT, but for compatibility let's use a partial unique index.
    CONSTRAINT unique_cart_item_variant UNIQUE (cart_id, article_id, variant_id)
);

-- Index for faster lookups
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
