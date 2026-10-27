-- name: UpsertUser :exec
INSERT INTO users (id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    permissions = EXCLUDED.permissions,
    username = EXCLUDED.username,
    birth_date = EXCLUDED.birth_date;

-- name: GetUserByID :one
SELECT id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date
FROM users
WHERE id = $1;

-- name: GetUserByEmail :one
SELECT id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date
FROM users
WHERE email = $1;

-- name: ListAllUsers :many
SELECT id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date
FROM users
ORDER BY created_at ASC;

-- name: UpdateUserPasswordByEmail :exec
UPDATE users
SET password_hash = $2
WHERE email = $1;

-- name: DeleteUserByID :exec
DELETE FROM users
WHERE id = $1;

-- name: UpsertSession :exec
INSERT INTO sessions (token_hash, user_id, active_view, created_at, expires_at)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (token_hash) DO UPDATE SET
    active_view = EXCLUDED.active_view,
    expires_at = EXCLUDED.expires_at;

-- name: GetSessionByToken :one
SELECT token_hash, user_id, active_view, created_at, expires_at
FROM sessions
WHERE token_hash = $1;

-- name: UpdateUserSessionsView :exec
UPDATE sessions
SET active_view = $2
WHERE user_id = $1;
