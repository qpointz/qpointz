# User Profile Extensibility

**Status:** Design reference
**Scope:** How to extend the user profile model beyond the core attributes delivered in WI-088.
This document is not a work item — it describes the extension options for future planning.

---

## Core Profile (WI-088 baseline)

`user_profiles` owns the stable, universal attributes every user has:

| Column | Type | Notes |
|--------|------|-------|
| `user_id` | VARCHAR | FK → `users.id`, PK |
| `display_name` | VARCHAR | nullable |
| `email` | VARCHAR | nullable |
| `theme` | VARCHAR | `light` / `dark` / `system` |
| `locale` | VARCHAR | nullable |
| `updated_at` | TIMESTAMP | |

---

## Extension Options

### Option A — New nullable column in `user_profiles`

Use when the attribute is universal (applies to every user regardless of domain) and is stable
enough to deserve a first-class column.

**How:**
1. Add a Flyway migration with a new nullable column to `user_profiles`.
2. Add the field to `UserProfileRecord`, `UserProfileResponse`, and `UserProfilePatch`.
3. Existing rows get `NULL`; existing clients that do not send the field leave it unchanged.

**Examples of candidates:** `timezone`, `avatarUrl`, `phoneNumber`.

**Rule:** Do not add speculative columns. A column earns its place when a concrete feature needs it.

---

### Option B — Domain extension table

Use when the attribute is domain-specific (AI preferences, notification settings, etc.) and should
not pollute the core profile schema.

**How:**
1. Domain team creates a new table in their persistence module (e.g. `user_ai_preferences` in
   `mill-ai-v3-persistence`), with `user_id` FK → `users.id`.
2. The domain team owns the Flyway migration, entity, repository, and adapter — no changes to
   `mill-security-persistence`.
3. The domain exposes its own slice of profile data via its own API or composites into the
   relevant response (e.g. a richer `GET /auth/me` variant or a dedicated endpoint).

**Examples:**
| Table | Owner module | Purpose |
|-------|-------------|---------|
| `user_ai_preferences` | `mill-ai-v3-persistence` | LLM model preferences, response style |
| `user_notification_settings` | future notifications module | Channel, frequency, opt-outs |
| `user_pat_settings` | future PAT module | Default PAT expiry, scope presets |

**Rule:** Domain extension tables reference `users.id` directly — never through `user_profiles`.
This keeps the extension independent of whether the user has a profile row yet.

---

## Decision Guide

| Attribute | Option |
|-----------|--------|
| Universal, stable, simple scalar | A — new nullable column |
| Domain-specific or owned by a non-security module | B — domain extension table |
| Temporary / speculative | Neither — wait until the need is clear |

---

## What Does NOT Belong Here

- JSON blobs / catch-all maps — not relational, not queryable, not typed.
- Credential data — belongs in `user_credentials`.
- Identity data — belongs in `user_identities`.
- Group/role data — belongs in `group_memberships`.
