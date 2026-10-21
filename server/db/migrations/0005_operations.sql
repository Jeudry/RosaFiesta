-- Módulo operations: leads CRM, proveedores, reseñas verificadas, bot WhatsApp y notificaciones.
CREATE TABLE leads (
    id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    event_type TEXT NOT NULL,
    estimated_dop INT NOT NULL,
    stage TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE suppliers (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    specialty TEXT NOT NULL,
    phone TEXT NOT NULL,
    rating TEXT NOT NULL
);

CREATE TABLE reviews (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL,
    client_name TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,
    admin_reply TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL
);
