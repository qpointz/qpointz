-- V3: User identity schema.
-- Canonical users, local credentials, identity bridge, groups, memberships, and user profiles.
-- All authentication methods (local, OAuth, future PAT) resolve to users.user_id.

CREATE TABLE users (
    user_id       VARCHAR(255)  PRIMARY KEY,
    status        VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    display_name  VARCHAR(512),
    primary_email VARCHAR(512),
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL
);

CREATE INDEX idx_users_primary_email ON users (primary_email);

-- Local password credentials (password-only; OAuth users have no row here)

CREATE TABLE user_credentials (
    credential_id VARCHAR(255)  PRIMARY KEY,
    user_id       VARCHAR(255)  NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    password_hash VARCHAR(1024) NOT NULL,
    algorithm     VARCHAR(64)   NOT NULL,
    enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL
);

CREATE INDEX idx_user_credentials_user_id ON user_credentials (user_id);

-- Identity bridge: maps (provider, subject) → user_id for all auth methods

CREATE TABLE user_identities (
    identity_id      VARCHAR(255) PRIMARY KEY,
    provider         VARCHAR(128) NOT NULL,
    subject          VARCHAR(512) NOT NULL,
    user_id          VARCHAR(255) NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    claims_snapshot  TEXT,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT uq_user_identities_provider_subject UNIQUE (provider, subject)
);

CREATE INDEX idx_user_identities_user_id ON user_identities (user_id);

-- Groups and memberships

CREATE TABLE groups (
    group_id    VARCHAR(255)  PRIMARY KEY,
    group_name  VARCHAR(255)  NOT NULL,
    description VARCHAR(1024),
    CONSTRAINT uq_groups_group_name UNIQUE (group_name)
);

CREATE TABLE group_memberships (
    user_id  VARCHAR(255) NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    group_id VARCHAR(255) NOT NULL REFERENCES groups (group_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

CREATE INDEX idx_group_memberships_group_id ON group_memberships (group_id);

-- User profiles (created lazily on first access)

CREATE TABLE user_profiles (
    user_id      VARCHAR(255) PRIMARY KEY REFERENCES users (user_id) ON DELETE CASCADE,
    display_name VARCHAR(512),
    email        VARCHAR(512),
    theme        VARCHAR(32),
    locale       VARCHAR(64),
    updated_at   TIMESTAMP    NOT NULL
);
