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

-- name: DeleteCategory :exec
DELETE FROM categories
WHERE id = $1;

-- name: CountArticlesByCategory :one
SELECT COUNT(*)::int
FROM articles
WHERE LOWER(TRIM(category_id)) = LOWER(TRIM($1));

-- name: UpsertArticle :exec
INSERT INTO articles (
    id, name, category_id, photo_key, photo_url, daily_price_dop, total_stock,
    damaged_stock, available_stock, low_stock_threshold, description, visible,
    dimensions, material, color_variants, is_direct_sale
) VALUES (
    $1, $2, $3, $4, $5, $6, $7,
    $8, $9, $10, $11, $12,
    $13, $14, $15, $16
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    category_id = EXCLUDED.category_id,
    photo_key = EXCLUDED.photo_key,
    photo_url = EXCLUDED.photo_url,
    daily_price_dop = EXCLUDED.daily_price_dop,
    total_stock = EXCLUDED.total_stock,
    damaged_stock = EXCLUDED.damaged_stock,
    available_stock = EXCLUDED.available_stock,
    low_stock_threshold = EXCLUDED.low_stock_threshold,
    description = EXCLUDED.description,
    visible = EXCLUDED.visible,
    dimensions = EXCLUDED.dimensions,
    material = EXCLUDED.material,
    color_variants = EXCLUDED.color_variants,
    is_direct_sale = EXCLUDED.is_direct_sale;

-- name: ListArticles :many
SELECT id, name, category_id, photo_key, photo_url, daily_price_dop, total_stock,
       damaged_stock, available_stock, low_stock_threshold, description, visible,
       dimensions, material, color_variants, is_direct_sale
FROM articles
WHERE is_direct_sale = FALSE
ORDER BY id;

-- name: ListAllArticles :many
SELECT id, name, category_id, photo_key, photo_url, daily_price_dop, total_stock,
       damaged_stock, available_stock, low_stock_threshold, description, visible,
       dimensions, material, color_variants, is_direct_sale
FROM articles
ORDER BY id;

-- name: ListBalloonArticles :many
SELECT id, name, category_id, photo_key, photo_url, daily_price_dop, total_stock,
       damaged_stock, available_stock, low_stock_threshold, description, visible,
       dimensions, material, color_variants, is_direct_sale
FROM articles
WHERE is_direct_sale = TRUE
ORDER BY id;

-- name: DeleteArticle :exec
DELETE FROM articles
WHERE id = $1;

-- name: UpsertPackage :exec
INSERT INTO packages (id, name, event_type, photo_key, photo_url, total_price_dop, guest_capacity, items_summary, visible)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    event_type = EXCLUDED.event_type,
    photo_key = EXCLUDED.photo_key,
    photo_url = EXCLUDED.photo_url,
    total_price_dop = EXCLUDED.total_price_dop,
    guest_capacity = EXCLUDED.guest_capacity,
    items_summary = EXCLUDED.items_summary,
    visible = EXCLUDED.visible;

-- name: ListPackages :many
SELECT id, name, event_type, photo_key, photo_url, total_price_dop, guest_capacity, items_summary, visible
FROM packages
ORDER BY id;

-- name: DeletePackage :exec
DELETE FROM packages
WHERE id = $1;

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

-- name: ListActiveReservations :many
SELECT id, article_id, event_id, quote_id, quantity, pickup_date_iso, return_date_iso, status, created_at, updated_at
FROM stock_reservations
WHERE status IN ('held', 'confirmed')
ORDER BY pickup_date_iso, id;

-- name: DeleteReservationsByEvent :exec
DELETE FROM stock_reservations
WHERE event_id = $1;

-- name: UpsertStockReservation :exec
INSERT INTO stock_reservations (
    id, article_id, event_id, quote_id, quantity, pickup_date_iso, return_date_iso, status, created_at, updated_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10
)
ON CONFLICT (id) DO UPDATE SET
    article_id = EXCLUDED.article_id,
    event_id = EXCLUDED.event_id,
    quote_id = EXCLUDED.quote_id,
    quantity = EXCLUDED.quantity,
    pickup_date_iso = EXCLUDED.pickup_date_iso,
    return_date_iso = EXCLUDED.return_date_iso,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- name: UpdateReservationStatusByEvent :exec
UPDATE stock_reservations
SET status = $2,
    updated_at = $3
WHERE event_id = $1;

-- name: ListTrends :many
SELECT id, title, events_label, photo_key, photo_url, photo_bg_hex,
       linked_category, linked_package_id, sort_order, active
FROM catalog_trends
ORDER BY sort_order ASC, id ASC;

-- name: UpsertTrend :exec
INSERT INTO catalog_trends (
    id, title, events_label, photo_key, photo_url, photo_bg_hex,
    linked_category, linked_package_id, sort_order, active
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10
)
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    events_label = EXCLUDED.events_label,
    photo_key = EXCLUDED.photo_key,
    photo_url = EXCLUDED.photo_url,
    photo_bg_hex = EXCLUDED.photo_bg_hex,
    linked_category = EXCLUDED.linked_category,
    linked_package_id = EXCLUDED.linked_package_id,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

-- name: DeleteTrend :exec
DELETE FROM catalog_trends
WHERE id = $1;

-- name: GetSeasonBanner :one
SELECT id, badge_label, title, cta_label, target_screen_code, active
FROM season_banners
WHERE id = 'default'
LIMIT 1;

-- name: UpsertSeasonBanner :exec
INSERT INTO season_banners (
    id, badge_label, title, cta_label, target_screen_code, active
) VALUES (
    $1, $2, $3, $4, $5, $6
)
ON CONFLICT (id) DO UPDATE SET
    badge_label = EXCLUDED.badge_label,
    title = EXCLUDED.title,
    cta_label = EXCLUDED.cta_label,
    target_screen_code = EXCLUDED.target_screen_code,
    active = EXCLUDED.active;
