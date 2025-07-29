CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) >= 3 AND char_length(name) <= 256),
    description TEXT CHECK (description IS NULL OR (char_length(description) >= 5 AND char_length(description) <= 20000)),
    price DECIMAL(10, 2) NOT NULL CHECK (price > 5 AND price < 10000000),
    rental_price DECIMAL(10, 2) CHECK (rental_price IS NULL OR (rental_price > 5 AND rental_price < 10000000)),
    color BIGINT NOT NULL CHECK (color >= 0 AND color <= 4294967295),
    size DECIMAL(10, 2) NOT NULL CHECK (size <= 99999),
    image_url TEXT CHECK (image_url IS NULL OR char_length(image_url) <= 3000),
    stock INT NOT NULL DEFAULT 0 CHECK (stock <= 100000000),
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);