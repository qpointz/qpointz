-- V3: User identity schema and default seed data.
-- Canonical users, local credentials, identity bridge, groups, memberships, user profiles,
-- and auth event audit trail.
-- All authentication methods (local, OAuth, future PAT) resolve to users.user_id.
--
-- validated: email confirmed / account activated — must be TRUE for login to succeed.
-- locked:    account administratively locked — must be FALSE for login to succeed.

CREATE TABLE users (
    user_id       VARCHAR(255)  PRIMARY KEY,
    status        VARCHAR(32)   NOT NULL DEFAULT 'ACTIVE',
    display_name  VARCHAR(512),
    primary_email VARCHAR(512),
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL,
    validated     BOOLEAN       NOT NULL DEFAULT TRUE,
    locked        BOOLEAN       NOT NULL DEFAULT FALSE,
    lock_date     TIMESTAMP,
    lock_reason   VARCHAR(1024)
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

-- Auth event audit trail

CREATE TABLE auth_events (
    event_id       VARCHAR(255)  PRIMARY KEY,
    event_type     VARCHAR(64)   NOT NULL,
    user_id        VARCHAR(255),
    subject        VARCHAR(512),
    ip_address     VARCHAR(64),
    user_agent     VARCHAR(1024),
    failure_reason VARCHAR(255),
    occurred_at    TIMESTAMP     NOT NULL
);

CREATE INDEX idx_auth_events_user_id     ON auth_events (user_id);
CREATE INDEX idx_auth_events_event_type  ON auth_events (event_type);
CREATE INDEX idx_auth_events_occurred_at ON auth_events (occurred_at);

-- Default admin user seed.
-- Password hash uses {noop} prefix — replace in production.
-- Stable UUIDs: user 000...001, credential 000...002, identity 000...003, group 000...010

INSERT INTO users (user_id, status, display_name, primary_email, created_at, updated_at, validated, locked)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'ACTIVE',
    'Administrator',
    'admin@mill.local',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    TRUE,
    FALSE
);

INSERT INTO user_credentials (credential_id, user_id, password_hash, algorithm, enabled, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    '{noop}admin',
    'noop',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- subject = 'admin' — this is the value entered in the login username field
INSERT INTO user_identities (identity_id, provider, subject, user_id, claims_snapshot, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000003',
    'local',
    'admin',
    '00000000-0000-0000-0000-000000000001',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO groups (group_id, group_name, description)
VALUES (
    '00000000-0000-0000-0000-000000000010',
    'admins',
    'Default administrators group'
);

INSERT INTO group_memberships (user_id, group_id)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000010'
);

INSERT INTO user_profiles (user_id, display_name, email, theme, locale, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrator',
    'admin@mill.local',
    NULL,
    NULL,
    CURRENT_TIMESTAMP
);
