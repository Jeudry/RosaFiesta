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
INSERT INTO articles (
    id, name, category_id, photo_key, daily_price_dop, total_stock,
    available_stock, low_stock_threshold, description, visible,
    dimensions, material, color_variants, is_direct_sale
) VALUES (
    $1, $2, $3, $4, $5, $6,
    $7, $8, $9, $10,
    $11, $12, $13, $14
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    category_id = EXCLUDED.category_id,
    photo_key = EXCLUDED.photo_key,
    daily_price_dop = EXCLUDED.daily_price_dop,
    total_stock = EXCLUDED.total_stock,
    available_stock = EXCLUDED.available_stock,
    low_stock_threshold = EXCLUDED.low_stock_threshold,
    description = EXCLUDED.description,
    visible = EXCLUDED.visible,
    dimensions = EXCLUDED.dimensions,
    material = EXCLUDED.material,
    color_variants = EXCLUDED.color_variants,
    is_direct_sale = EXCLUDED.is_direct_sale;

-- name: ListArticles :many
SELECT id, name, category_id, photo_key, daily_price_dop, total_stock,
       available_stock, low_stock_threshold, description, visible,
       dimensions, material, color_variants, is_direct_sale
FROM articles
WHERE is_direct_sale = FALSE
ORDER BY id;

-- name: ListAllArticles :many
SELECT id, name, category_id, photo_key, daily_price_dop, total_stock,
       available_stock, low_stock_threshold, description, visible,
       dimensions, material, color_variants, is_direct_sale
FROM articles
ORDER BY id;

-- name: ListBalloonArticles :many
SELECT id, name, category_id, photo_key, daily_price_dop, total_stock,
       available_stock, low_stock_threshold, description, visible,
       dimensions, material, color_variants, is_direct_sale
FROM articles
WHERE is_direct_sale = TRUE
ORDER BY id;

-- name: UpsertPackage :exec
INSERT INTO packages (id, name, event_type, photo_key, total_price_dop, guest_capacity, items_summary, visible)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    event_type = EXCLUDED.event_type,
    photo_key = EXCLUDED.photo_key,
    total_price_dop = EXCLUDED.total_price_dop,
    guest_capacity = EXCLUDED.guest_capacity,
    items_summary = EXCLUDED.items_summary,
    visible = EXCLUDED.visible;

-- name: ListPackages :many
SELECT id, name, event_type, photo_key, total_price_dop, guest_capacity, items_summary, visible
FROM packages
ORDER BY id;

-- name: DeletePackageItems :exec
DELETE FROM package_items
WHERE package_id = $1;

-- name: UpsertPackageItem :exec
INSERT INTO package_items (package_id, article_id, article_name, quantity, unit_price_dop)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (package_id, article_id) DO UPDATE SET
    article_name = EXCLUDED.article_name,
    quantity = EXCLUDED.quantity,
    unit_price_dop = EXCLUDED.unit_price_dop;

-- name: ListPackageItems :many
SELECT package_id, article_id, article_name, quantity, unit_price_dop
FROM package_items
ORDER BY package_id, article_id;
