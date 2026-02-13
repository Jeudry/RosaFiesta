-- Rename table
ALTER TABLE IF EXISTS products RENAME TO articles;

-- Rename constraints and indexes if necessary (Postgres usually handles basic rename, but explicit is safer for indexes/constraints if named)
-- Assuming default naming or no explicit names for now, but will ensure columns match requirements

-- Modify columns for ArticleEntity
-- id (UUID) - already exists
-- name (VARCHAR) -> name_template
ALTER TABLE articles RENAME COLUMN name TO name_template;

-- description (TEXT) -> description_template
ALTER TABLE articles RENAME COLUMN description TO description_template;

-- price, rental_price, color, size, image_url, stock - These move to variants, so DROP from articles
ALTER TABLE articles DROP COLUMN IF EXISTS price;
ALTER TABLE articles DROP COLUMN IF EXISTS rental_price;
ALTER TABLE articles DROP COLUMN IF EXISTS color;
ALTER TABLE articles DROP COLUMN IF EXISTS size;
ALTER TABLE articles DROP COLUMN IF EXISTS image_url;
ALTER TABLE articles DROP COLUMN IF EXISTS stock;

-- Add new columns for ArticleEntity
ALTER TABLE articles ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE articles ADD COLUMN IF NOT EXISTS type VARCHAR(50);
-- category_id TO BE ADDED below as Foreign Key

-- Add category_id FK (assuming categories table exists from previous migrations)
ALTER TABLE articles ADD COLUMN IF NOT EXISTS category_id UUID;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_articles_categories') THEN
        ALTER TABLE articles
            ADD CONSTRAINT fk_articles_categories
            FOREIGN KEY (category_id)
            REFERENCES categories (id);
    END IF;
END $$;
