# Mill Security Design - Forms Login, User Profiles, and PAT

**Status:** Proposed  
**Date:** March 21, 2026  
**Scope:** HTTP auth UX + user identity persistence + personal access tokens (PAT) with future OAuth compatibility

## 1. Goal

Deliver a security model that supports:

- user forms/basic login in `mill-ui`
- persistent user profiles
- user-issued PAT tokens
- API authentication using PAT tokens
- identity/profile persistence model reusable for OAuth providers
- ability to disable security completely for local/dev scenarios

This document is design-only and does not include implementation details.

## 2. Current Baseline

## 2.1 Existing strengths

- Route-level security already exists in `services/mill-service-security` via `SecurityFilterChain` configs.
- `mill.security.enable` already drives secured vs `permitAll` behavior through `@ConditionalOnSecurity`.
- Authentication methods are modular (`basic`, `oauth2-resource-server`, `entra-id-token`).
- Core policy model is isolated in `core/mill-security` and can reuse authorities/groups.

## 2.2 Current gaps for this goal

- `mill-ui` login/profile are currently mock UX and not backed by auth APIs.
- Basic auth user store is file-based (`passwd.yml`), not persistent or user-managed.
- No PAT data model, issuance endpoint, or bearer validation provider.
- No persistent user profile domain.
- No provider-agnostic user identity model for first-class OAuth reuse.

## 3. Design Principles

1. **One canonical user model** independent of auth method.
2. **Credentials are pluggable** (password, PAT, OAuth identity), not separate user silos.
3. **Security off mode must remain first-class** and deterministic.
4. **Token secrets are never stored in plaintext**; only one-time return on creation.
5. **Authorization remains policy/authority based** to preserve current policy engine integration.

## 4. Target Identity Architecture

## 4.1 Domain model (OAuth-ready)

Use a provider-neutral persistence model:

- `users`
  - stable internal user id
  - account status (active, disabled, locked)
  - display metadata (display name, primary email)
  - created/updated timestamps

- `user_credentials`
  - local credential records (password hash and metadata)
  - supports password rotation, disablement, and future credential families

- `user_identities`
  - external identity mapping table
  - fields: `provider`, `subject`, `user_id`, optional claims snapshot
  - unique key `(provider, subject)`
  - enables OAuth onboarding without changing user/profile tables

- `groups` and `group_memberships`
  - maps users to authority/group names consumed by current policy selection

- `user_profiles`
  - user preferences/settings (theme, locale, UI options, etc.)
  - separate from credentials for clean lifecycle and OAuth reuse

## 4.2 Why this shape satisfies OAuth reuse

OAuth users and local users both resolve to `users.id`.
Only the identity/credential entry changes by provider type:

- local/basic -> `user_credentials` + `user_identities(provider=local, subject=username)`
- OAuth/OIDC -> `user_identities(provider=oidc|entra, subject=<issuer-sub>)`

Profiles, groups, and PAT ownership stay unchanged.

## 5. Personal Access Token (PAT) Design

## 5.1 PAT storage model

Add `personal_access_tokens` with:

- token id (public identifier/prefix)
- owner `user_id`
- token name/description
- hashed secret (never plaintext)
- scopes
- created/updated timestamps
- expiry timestamp
- revoked timestamp
- optional last-used timestamp and audit metadata

## 5.2 PAT format and lifecycle

- Create token -> return plaintext token once to caller.
- Persist only hash + metadata.
- Validate bearer token by id lookup + constant-time hash comparison.
- Enforce expiry/revocation/scope on every request.

## 5.3 PAT and future OAuth

PAT is owned by canonical `users.id`, so OAuth onboarding does not require PAT migration.
The same user can authenticate via OAuth for UI and PAT for automation.

## 6. Auth/API Surface

## 6.1 Required endpoints

- `POST /auth/login` (forms/basic login)
- `POST /auth/logout`
- `GET /auth/me` (resolved user + authorities/groups + profile projection)
- `GET /auth/pats` (list token metadata only)
- `POST /auth/pats` (issue PAT, return token once)
- `DELETE /auth/pats/{tokenId}` (revoke)
- optional `PATCH /profile` (update profile)

## 6.2 API authentication behavior

Protected APIs should accept:

- `Authorization: Bearer <pat>`
- existing secure modes as configured (`basic`, OAuth resource server, etc.)

PAT should integrate as another authentication method in the existing method chain.

## 7. Runtime Modes and Security Toggle

Define explicit runtime modes:

- **OFF** (`mill.security.enable=false`)
  - all routes are permit-all
  - no auth required
  - anonymous identity used by downstream security dispatcher

- **ON** (`mill.security.enable=true`)
  - route-level auth enforced
  - configured authentication methods active (basic/OAuth/PAT)
  - principal and authorities propagated to downstream services

Important: OFF mode should be deterministic and documented as a supported deployment mode.

## 8. Integration with Existing Modules

## 8.1 `services/mill-service-security`

- Keep route security chain pattern.
- Add PAT authentication method/provider to current `AuthenticationMethod` ecosystem.
- Add auth/profile/PAT controllers under existing auth route space.

## 8.2 `core/mill-security`

- Keep policy/action model unchanged.
- Reuse authorities from authenticated principal as today.

## 8.3 `mill-ui`

- Replace mock `AuthContext` flow with backend-backed login/me/logout.
- Replace placeholder profile panel data with `/auth/me` and profile endpoints.
- Add PAT management section under profile/access.

## 8.4 AI and other secured APIs

- Ensure service-level user resolution uses authenticated principal when security is ON.
- Keep anonymous/static resolver behavior for OFF mode.

## 9. Phased Delivery Plan

## Phase 1: Identity persistence foundation

- Introduce canonical user, identity, credential, group, profile, and PAT schema.
- Keep existing auth behavior operational during migration.

## Phase 2: Forms login + user profile APIs

- Implement login/logout/me/profile endpoints.
- Wire `mill-ui` login/profile to backend.

## Phase 3: PAT issuance and PAT authentication

- Implement PAT create/list/revoke endpoints.
- Add PAT bearer validation provider to API auth flow.

## Phase 4: OAuth federation compatibility

- Add OAuth identity mapping onto `user_identities`.
- Keep user profile and PAT ownership unchanged.

## Phase 5: Hardening and operations

- Audit logs and token usage telemetry.
- Rotation/revocation workflows.
- Admin controls for account/token lifecycle.

## 10. Risks and Guardrails

- **Risk:** Tying profile to one auth mode (e.g., basic user file) blocks OAuth reuse.  
  **Guardrail:** Canonical `users` + `user_identities` from day one.

- **Risk:** Storing PAT plaintext.  
  **Guardrail:** Store hash only; plaintext shown once.

- **Risk:** Security OFF drift or ambiguous behavior.  
  **Guardrail:** Explicit mode semantics and integration tests for ON/OFF.

- **Risk:** Identity mismatch between transport auth and downstream policy engine.  
  **Guardrail:** Standardize principal/authority propagation path for all transports.

## 11. Open Design Questions

1. Session-based UI login vs token-based UI login (or hybrid)?
2. PAT scope model granularity (global, service-level, endpoint-level)?
3. Multi-tenant requirements in initial cut?
4. Whether to support PAT for UI use or API automation only?
5. Account bootstrap strategy for first admin user in secure mode?

## 12. Acceptance Criteria (Design Level)

- `mill-ui` can authenticate with forms login against backend APIs.
- User profile data persists and is retrievable via `/auth/me`.
- Users can issue, list, and revoke PATs.
- PATs authenticate protected APIs when security is ON.
- Security can be fully disabled via `mill.security.enable=false`.
- Data model supports adding OAuth identities without schema rewrite.

