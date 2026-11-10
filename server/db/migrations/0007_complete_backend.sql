CREATE TABLE IF NOT EXISTS auth_otp_codes (
    email TEXT PRIMARY KEY,
    code_hash TEXT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    reset_token_hash TEXT NOT NULL DEFAULT '',
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMPTZ NOT NULL,
    reset_expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_key TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS admin_clients (
    id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT NOT NULL DEFAULT '',
    notes TEXT NOT NULL DEFAULT '',
    events_count INT NOT NULL DEFAULT 0,
    total_spent_dop INT NOT NULL DEFAULT 0,
    balance_dop INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS staff_members (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL DEFAULT '',
    phone TEXT NOT NULL DEFAULT '',
    role_label TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('admin', 'staff')),
    permissions TEXT NOT NULL DEFAULT '',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE articles ADD COLUMN IF NOT EXISTS available_stock INT NOT NULL DEFAULT 0;
ALTER TABLE articles ADD COLUMN IF NOT EXISTS visible BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE articles ADD COLUMN IF NOT EXISTS dimensions TEXT NOT NULL DEFAULT '';
ALTER TABLE articles ADD COLUMN IF NOT EXISTS material TEXT NOT NULL DEFAULT '';
ALTER TABLE articles ADD COLUMN IF NOT EXISTS color_variants TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE articles ADD COLUMN IF NOT EXISTS is_direct_sale BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE packages ADD COLUMN IF NOT EXISTS visible BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS package_items (
    package_id TEXT NOT NULL REFERENCES packages(id) ON DELETE CASCADE,
    article_id TEXT NOT NULL,
    article_name TEXT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_dop INT NOT NULL DEFAULT 0,
    PRIMARY KEY (package_id, article_id)
);

ALTER TABLE events ADD COLUMN IF NOT EXISTS code TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS zone TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS assigned_lead TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS paid_deposit_dop INT NOT NULL DEFAULT 0;
ALTER TABLE events ADD COLUMN IF NOT EXISTS client_email TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS sketch_json TEXT NOT NULL DEFAULT '{}';
ALTER TABLE events ADD COLUMN IF NOT EXISTS cancellation_reason TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS event_items (
    event_id TEXT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    article_code TEXT NOT NULL,
    name TEXT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_dop INT NOT NULL DEFAULT 0,
    PRIMARY KEY (event_id, article_code)
);

ALTER TABLE event_tasks ADD COLUMN IF NOT EXISTS stage TEXT NOT NULL DEFAULT 'montaje_sitio';
ALTER TABLE event_tasks ADD COLUMN IF NOT EXISTS completed_at_label TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS event_guests (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    rsvp_status TEXT NOT NULL DEFAULT 'pending' CHECK (rsvp_status IN ('pending', 'confirmed', 'declined')),
    table_label TEXT NOT NULL DEFAULT '',
    dietary_notes TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS event_schedule_blocks (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    time_range TEXT NOT NULL,
    title TEXT NOT NULL,
    owner_name TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'pending',
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id TEXT PRIMARY KEY,
    thread_id TEXT NOT NULL,
    event_id TEXT NOT NULL DEFAULT '',
    event_title TEXT NOT NULL DEFAULT '',
    sender_id TEXT NOT NULL DEFAULT '',
    sender_name TEXT NOT NULL,
    sender_role TEXT NOT NULL DEFAULT 'client',
    recipient_label TEXT NOT NULL DEFAULT '',
    body TEXT NOT NULL,
    attachment_name TEXT NOT NULL DEFAULT '',
    unread BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE quotes ADD COLUMN IF NOT EXISTS code TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS client_id TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS client_email TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS event_date_label TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS venue TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS guests INT NOT NULL DEFAULT 0;
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS lines_json JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS delivery_fee_dop INT NOT NULL DEFAULT 0;
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS discount_dop INT NOT NULL DEFAULT 0;
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS signature_svg TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS signed_by_name TEXT NOT NULL DEFAULT '';
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS rejection_reason TEXT NOT NULL DEFAULT '';

ALTER TABLE payments ADD COLUMN IF NOT EXISTS receipt_number TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS event_title TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS client_id TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS client_email TEXT NOT NULL DEFAULT '';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS kind TEXT NOT NULL DEFAULT 'anticipo';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS bank_name TEXT NOT NULL DEFAULT '';

ALTER TABLE refunds ADD COLUMN IF NOT EXISTS event_title TEXT NOT NULL DEFAULT '';
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS client_name TEXT NOT NULL DEFAULT '';
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'pending';
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS approved_by TEXT NOT NULL DEFAULT '';
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS approved_at_label TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS damage_incidents (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL,
    article_code TEXT NOT NULL,
    article_name TEXT NOT NULL,
    damaged_units INT NOT NULL DEFAULT 0,
    missing_units INT NOT NULL DEFAULT 0,
    replacement_dop INT NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'open',
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS offline_queue (
    id TEXT PRIMARY KEY,
    action_label TEXT NOT NULL,
    entity_code TEXT NOT NULL,
    created_at_label TEXT NOT NULL,
    synced BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE leads ADD COLUMN IF NOT EXISTS email TEXT NOT NULL DEFAULT '';
ALTER TABLE leads ADD COLUMN IF NOT EXISTS notes TEXT NOT NULL DEFAULT '';
ALTER TABLE leads ADD COLUMN IF NOT EXISTS event_date_label TEXT NOT NULL DEFAULT '';
ALTER TABLE leads ADD COLUMN IF NOT EXISTS guests INT NOT NULL DEFAULT 0;

ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS category TEXT NOT NULL DEFAULT '';
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS contact_name TEXT NOT NULL DEFAULT '';
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS email TEXT NOT NULL DEFAULT '';
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS website TEXT NOT NULL DEFAULT '';
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS notes TEXT NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS business_settings (
    id TEXT PRIMARY KEY,
    company_name TEXT NOT NULL,
    rnc TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL,
    whatsapp TEXT NOT NULL,
    email TEXT NOT NULL,
    instagram TEXT NOT NULL,
    facebook TEXT NOT NULL,
    business_hours TEXT NOT NULL,
    itbis_percent INT NOT NULL DEFAULT 18,
    deposit_percent INT NOT NULL DEFAULT 30,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS delivery_zones (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    fee_dop INT NOT NULL DEFAULT 0,
    eta_minutes INT NOT NULL DEFAULT 45,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    recipient_role TEXT NOT NULL,
    user_id TEXT NOT NULL DEFAULT '',
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'general',
    action_path TEXT NOT NULL DEFAULT '',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at_label TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS insurance_policies (
    id TEXT PRIMARY KEY,
    provider_name TEXT NOT NULL,
    policy_number TEXT NOT NULL,
    coverage_type TEXT NOT NULL,
    max_coverage_dop INT NOT NULL DEFAULT 0,
    valid_until_label TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'active'
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id TEXT PRIMARY KEY,
    actor_name TEXT NOT NULL,
    actor_role TEXT NOT NULL,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    summary TEXT NOT NULL,
    created_at_label TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS bot_faqs (
    id TEXT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'general',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS bot_keywords (
    id TEXT PRIMARY KEY,
    keyword TEXT NOT NULL,
    auto_reply TEXT NOT NULL,
    handoff_to_human BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS bot_threads (
    id TEXT PRIMARY KEY,
    customer_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    last_message TEXT NOT NULL,
    bot_paused BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_agent TEXT NOT NULL DEFAULT '',
    updated_at_label TEXT NOT NULL
);
