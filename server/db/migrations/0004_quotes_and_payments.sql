-- Módulos quotes y payments: cotizaciones, pagos con idempotencia y reembolsos.
CREATE TABLE quotes (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL,
    client_name TEXT NOT NULL,
    event_title TEXT NOT NULL,
    total_dop INT NOT NULL,
    deposit_dop INT NOT NULL,
    balance_dop INT NOT NULL,
    status TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    notes TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE payments (
    id TEXT PRIMARY KEY,
    idempotency_key TEXT NOT NULL UNIQUE,
    event_id TEXT NOT NULL,
    client_name TEXT NOT NULL,
    amount_dop INT NOT NULL CHECK (amount_dop > 0),
    method TEXT NOT NULL CHECK (method IN ('transferencia', 'efectivo', 'tarjeta', 'transfer', 'cash', 'card')),
    paid_date_label TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'confirmed',
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE refunds (
    id TEXT PRIMARY KEY,
    idempotency_key TEXT NOT NULL UNIQUE,
    event_id TEXT NOT NULL,
    paid_dop INT NOT NULL,
    refund_percent INT NOT NULL,
    refund_dop INT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
