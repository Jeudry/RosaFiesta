-- Módulo identity: cuentas de clientes, administradora (María) y equipo (Pedro, Yuli, Andrés).
CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT NOT NULL DEFAULT '',
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('client', 'admin', 'staff')),
    permissions TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE sessions (
    token_hash TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    active_view TEXT NOT NULL DEFAULT 'personal' CHECK (active_view IN ('personal', 'admin')),
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);
