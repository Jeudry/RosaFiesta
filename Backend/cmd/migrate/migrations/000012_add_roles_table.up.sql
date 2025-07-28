CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    level int NOT NULL DEFAULT 0,
    description TEXT
);

INSERT INTO roles (name, level, description)
VALUES ('admin', 2, 'Administrator role');

INSERT INTO roles (name, level, description)
VALUES ('moderator', 1, 'Moderator role');

INSERT INTO roles (name, level, description)
VALUES ('user', 0, 'Default user role');