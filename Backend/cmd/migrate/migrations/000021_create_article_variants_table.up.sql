-- Create article_variants table
CREATE TABLE
IF NOT EXISTS article_variants
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4
(),
    article_id UUID NOT NULL,
    sku VARCHAR
(255) UNIQUE NOT NULL,
    name VARCHAR
(255) NOT NULL,
    description TEXT,
    image_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    stock INT DEFAULT 0,
    rental_price DECIMAL
(19,4) NOT NULL,
    sale_price DECIMAL
(19,4),
    replacement_cost DECIMAL
(19,4),
    
    created_at TIMESTAMP
WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_variants_articles
        FOREIGN KEY
(article_id)
        REFERENCES articles
(id)
        ON
DELETE CASCADE
);

-- Index for variant searches
CREATE INDEX
IF NOT EXISTS idx_variants_article_id ON article_variants
(article_id);
