-- name: UpsertCategory :exec
INSERT INTO categories (id, name, illo_key, item_count)
VALUES ($1, $2, $3, $4)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    illo_key = EXCLUDED.illo_key,
    item_count = EXCLUDED.item_count;

-- name: ListCategories :many
SELECT id, name, illo_key, item_count
FROM categories
ORDER BY id;

-- name: UpsertArticle :exec
INSERT INTO articles (id, name, category_id, photo_key, daily_price_dop, total_stock, low_stock_threshold, description)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    category_id = EXCLUDED.category_id,
    photo_key = EXCLUDED.photo_key,
    daily_price_dop = EXCLUDED.daily_price_dop,
    total_stock = EXCLUDED.total_stock,
    low_stock_threshold = EXCLUDED.low_stock_threshold,
    description = EXCLUDED.description;

-- name: ListArticles :many
SELECT id, name, category_id, photo_key, daily_price_dop, total_stock, low_stock_threshold, description
FROM articles
ORDER BY id;

-- name: UpsertPackage :exec
INSERT INTO packages (id, name, event_type, photo_key, total_price_dop, guest_capacity, items_summary)
VALUES ($1, $2, $3, $4, $5, $6, $7)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    event_type = EXCLUDED.event_type,
    photo_key = EXCLUDED.photo_key,
    total_price_dop = EXCLUDED.total_price_dop,
    guest_capacity = EXCLUDED.guest_capacity,
    items_summary = EXCLUDED.items_summary;

-- name: ListPackages :many
SELECT id, name, event_type, photo_key, total_price_dop, guest_capacity, items_summary
FROM packages
ORDER BY id;
