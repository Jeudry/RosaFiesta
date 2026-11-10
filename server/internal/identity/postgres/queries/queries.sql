-- name: UpsertUser :exec
INSERT INTO users (id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date, avatar_key)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    permissions = EXCLUDED.permissions,
    username = EXCLUDED.username,
    birth_date = EXCLUDED.birth_date,
    avatar_key = EXCLUDED.avatar_key;

-- name: GetUserByID :one
SELECT id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date, avatar_key
FROM users
WHERE id = $1;

-- name: GetUserByEmail :one
SELECT id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date, avatar_key
FROM users
WHERE email = $1;

-- name: ListAllUsers :many
SELECT id, full_name, email, phone, password_hash, role, permissions, created_at, username, birth_date, avatar_key
FROM users
ORDER BY created_at ASC;

-- name: UpdateUserPasswordByEmail :exec
UPDATE users
SET password_hash = $2
WHERE email = $1;

-- name: UpdateUserPasswordByID :exec
UPDATE users
SET password_hash = $2
WHERE id = $1;

-- name: DeleteUserByID :exec
DELETE FROM users
WHERE id = $1;

-- name: UpsertSession :exec
INSERT INTO sessions (token_hash, user_id, active_view, created_at, expires_at)
VALUES ($1, $2, $3, $4, $5)
ON CONFLICT (token_hash) DO UPDATE SET
    active_view = EXCLUDED.active_view,
    expires_at = EXCLUDED.expires_at;

-- name: GetSessionByTokenHash :one
SELECT token_hash, user_id, active_view, created_at, expires_at
FROM sessions
WHERE token_hash = $1 AND expires_at > $2;

-- name: DeleteSessionByTokenHash :exec
DELETE FROM sessions
WHERE token_hash = $1;

-- name: DeleteSessionsByUserID :exec
DELETE FROM sessions
WHERE user_id = $1;

-- name: UpdateUserSessionsView :exec
UPDATE sessions
SET active_view = $2
WHERE user_id = $1;

-- name: UpsertOTPCode :exec
INSERT INTO auth_otp_codes (
    email, code_hash, attempts, verified, reset_token_hash, used, expires_at, reset_expires_at, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9
)
ON CONFLICT (email) DO UPDATE SET
    code_hash = EXCLUDED.code_hash,
    attempts = EXCLUDED.attempts,
    verified = EXCLUDED.verified,
    reset_token_hash = EXCLUDED.reset_token_hash,
    used = EXCLUDED.used,
    expires_at = EXCLUDED.expires_at,
    reset_expires_at = EXCLUDED.reset_expires_at,
    created_at = EXCLUDED.created_at;

-- name: GetOTPCodeByEmail :one
SELECT email, code_hash, attempts, verified, reset_token_hash, used, expires_at, reset_expires_at, created_at
FROM auth_otp_codes
WHERE email = $1;

-- name: IncrementOTPAttempts :exec
UPDATE auth_otp_codes
SET attempts = attempts + 1
WHERE email = $1;

-- name: MarkOTPVerified :exec
UPDATE auth_otp_codes
SET verified = TRUE,
    reset_token_hash = $2,
    reset_expires_at = $3
WHERE email = $1;

-- name: MarkOTPUsed :exec
UPDATE auth_otp_codes
SET used = TRUE
WHERE email = $1;

-- name: UpsertAdminClient :exec
INSERT INTO admin_clients (
    id, full_name, email, phone, notes, events_count, total_spent_dop, balance_dop, updated_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9
)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    notes = EXCLUDED.notes,
    events_count = EXCLUDED.events_count,
    total_spent_dop = EXCLUDED.total_spent_dop,
    balance_dop = EXCLUDED.balance_dop,
    updated_at = EXCLUDED.updated_at;

-- name: ListAdminClients :many
SELECT id, full_name, email, phone, notes, events_count, total_spent_dop, balance_dop, updated_at
FROM admin_clients
ORDER BY id ASC;

-- name: UpsertStaffMember :exec
INSERT INTO staff_members (
    id, user_id, full_name, email, phone, role_label, role, permissions, active, created_at
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10
)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    role_label = EXCLUDED.role_label,
    role = EXCLUDED.role,
    permissions = EXCLUDED.permissions,
    active = EXCLUDED.active;

-- name: ListStaffMembers :many
SELECT id, user_id, full_name, email, phone, role_label, role, permissions, active, created_at
FROM staff_members
ORDER BY created_at ASC, id ASC;
