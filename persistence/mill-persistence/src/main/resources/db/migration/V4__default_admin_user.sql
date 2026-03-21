-- V4: Default admin user seed.
-- Creates a local admin/admin user for development and initial deployment.
-- Password hash uses {noop} prefix (NoOpPasswordHasher) — replace with a stronger
-- PasswordHasher bean in production or update this row after first login.
--
-- Stable UUIDs ensure idempotent re-runs across identical H2 instances.
-- user_id:       00000000-0000-0000-0000-000000000001
-- credential_id: 00000000-0000-0000-0000-000000000002
-- identity_id:   00000000-0000-0000-0000-000000000003
-- group_id:      00000000-0000-0000-0000-000000000010

INSERT INTO users (user_id, status, display_name, primary_email, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'ACTIVE',
    'Administrator',
    'admin@mill.local',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
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

-- subject = 'admin' — this is the value entered in the login email/username field
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
