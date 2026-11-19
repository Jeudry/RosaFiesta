CREATE TABLE IF NOT EXISTS catalog_trends (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    events_label TEXT NOT NULL DEFAULT '',
    photo_key TEXT NOT NULL DEFAULT 'boda',
    photo_url TEXT NOT NULL DEFAULT '',
    photo_bg_hex TEXT NOT NULL DEFAULT '#F3EEF6',
    linked_category TEXT NOT NULL DEFAULT '',
    linked_package_id TEXT NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS season_banners (
    id TEXT PRIMARY KEY,
    badge_label TEXT NOT NULL DEFAULT '',
    title TEXT NOT NULL DEFAULT '',
    cta_label TEXT NOT NULL DEFAULT '',
    target_screen_code TEXT NOT NULL DEFAULT 's21',
    active BOOLEAN NOT NULL DEFAULT FALSE
);
