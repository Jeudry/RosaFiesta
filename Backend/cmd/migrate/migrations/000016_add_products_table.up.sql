CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    rental_price DECIMAL(10, 2),
    color BIGINT NOT NULL,
    size DECIMAL(10, 2) NOT NULL,
    image_url TEXT,
    stock INT NOT NULL DEFAULT 0,
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);