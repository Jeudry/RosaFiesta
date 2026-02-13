-- Create article_variant_attributes table
CREATE TABLE
IF NOT EXISTS article_variant_attributes
(
    variant_id UUID NOT NULL,
    key VARCHAR
(255) NOT NULL,
    value VARCHAR
(255) NOT NULL,
    
    CONSTRAINT pk_variant_attributes PRIMARY KEY
(variant_id, key),
    CONSTRAINT fk_attributes_variants
        FOREIGN KEY
(variant_id)
        REFERENCES article_variants
(id)
        ON
DELETE CASCADE
);

-- Create article_variant_dimensions table
CREATE TABLE
IF NOT EXISTS article_variant_dimensions
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4
(),
    variant_id UUID NOT NULL,
    height DECIMAL
(10,2),
    width DECIMAL
(10,2),
    depth DECIMAL
(10,2),
    weight DECIMAL
(10,2),
    
    CONSTRAINT fk_dimensions_variants
        FOREIGN KEY
(variant_id)
        REFERENCES article_variants
(id)
        ON
DELETE CASCADE
);
