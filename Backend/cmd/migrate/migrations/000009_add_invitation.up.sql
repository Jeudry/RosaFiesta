CREATE TABLE IF NOT EXISTS user_invitations (
    token bytea PRIMARY KEY,
    user_id UUID NOT NULL
)