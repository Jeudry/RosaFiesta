CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image_url TEXT,
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated TIMESTAMP,
    updated_by VARCHAR(255),
    deleted TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_deleted ON categories(deleted);
