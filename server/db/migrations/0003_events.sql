-- Módulo events: eventos, artículos reservados, tareas, cronograma, invitados y bocetos.
CREATE TABLE events (
    id TEXT PRIMARY KEY,
    client_id UUID NOT NULL,
    client_name TEXT NOT NULL,
    title TEXT NOT NULL,
    event_type TEXT NOT NULL,
    photo_key TEXT NOT NULL,
    venue TEXT NOT NULL,
    event_date_label TEXT NOT NULL,
    days_until INT NOT NULL,
    guests_count INT NOT NULL,
    articles_count INT NOT NULL,
    total_dop INT NOT NULL,
    deposit_dop INT NOT NULL,
    paid_dop INT NOT NULL,
    balance_dop INT NOT NULL,
    balance_due_label TEXT NOT NULL,
    status TEXT NOT NULL,
    date_changes_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE event_tasks (
    id TEXT PRIMARY KEY,
    event_id TEXT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    assignee_name TEXT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);
