-- Módulo catalog: categorías, artículos con foto real y paquetes temáticos.
CREATE TABLE categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    illo_key TEXT NOT NULL,
    item_count INT NOT NULL DEFAULT 0
);

CREATE TABLE articles (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category_id TEXT NOT NULL REFERENCES categories(id),
    photo_key TEXT NOT NULL,
    daily_price_dop INT NOT NULL CHECK (daily_price_dop > 0),
    total_stock INT NOT NULL CHECK (total_stock >= 0),
    low_stock_threshold INT NOT NULL DEFAULT 5,
    description TEXT NOT NULL DEFAULT ''
);

CREATE TABLE packages (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    event_type TEXT NOT NULL,
    photo_key TEXT NOT NULL,
    total_price_dop INT NOT NULL CHECK (total_price_dop > 0),
    guest_capacity INT NOT NULL CHECK (guest_capacity > 0),
    items_summary TEXT NOT NULL
);
