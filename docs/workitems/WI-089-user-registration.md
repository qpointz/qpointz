# WI-089 - User Registration

Status: `planned`
Type: `feature`
Area: `security`, `ui`
Backlog refs: `SEC-3a` (extension)

## Problem Statement

The `LoginPage` in `mill-ui` already renders a "Sign up" link (asserted by an existing test) but it
has no click handler — it is a dead placeholder. There is no `POST /auth/register` backend endpoint
and no registration form. New users can only be created by an administrator seeding the database
directly. Self-registration must be explicitly opt-in via configuration because open registration is
inappropriate for many deployments.

## Goal

Implement `POST /auth/register` on the backend and wire the "Sign up" link in `LoginPage` to a
registration form, gated by both a feature flag and a server-side configuration property.

## Baseline State

**Backend (`security/mill-security-auth-service` — created in WI-087):**
- `AuthPublicController` at `/auth/public` has `POST /auth/public/login`.
- `AuthController` at `/auth` has `GET /auth/me` and `POST /auth/logout`.
- `AuthServiceSecurityConfiguration @Order(-5)` owns the `SecurityFilterChain` with:
  - `securityMatcher("/auth/public/**", "/auth/me", "/auth/logout", "/auth/profile")`
  - `/auth/public/**` → `permitAll()` — registration fits here naturally.
- `UserIdentityResolutionService.resolveOrProvision("local", username, displayName, email)` from
  WI-085 already encapsulates the full user creation logic (UserRecord + UserIdentityRecord +
  UserProfileRecord). No new persistence work needed.
- No `POST /auth/public/register` exists.

**Frontend (`ui/mill-ui/`):**
- `LoginPage.tsx` renders `"Sign up"` as a dead styled `Text` with `cursor: pointer`.
- Test `should render sign up link` asserts it is visible.
- After WI-087, `LoginPage` is inside the router (WI-087 moves `App.tsx` auth flow into routed
  paths to support `authService.getMe()` bootstrap). `/register` route can be added.
- `FeatureFlags` in `src/features/defaults.ts` has `loginPassword: boolean` — registration is a
  natural companion. A new `loginRegistration: boolean` flag gates the sign-up link and form.

## Design Decisions

**Open registration is off by default.** Controlled by:
- Server-side: `mill.security.allow-registration=false` (default). Must be `true` to accept
  `POST /auth/register` requests; returns `403` otherwise.
- Client-side: `flags.loginRegistration` controls visibility of the "Sign up" link. The UI reads
  this from the backend feature/security descriptor (or defaults to `false`).

**Username vs email.** The `LoginPage` uses `"Email"` as the field label. Registration should
accept `email` as the login identifier (mapped to `UserRecord.primaryEmail` and
`UserIdentityRecord(provider="local", subject=email)`). This aligns the login and registration
forms. The decision to use email vs. an arbitrary username is finalised here.

**Auto-login after registration.** On successful `POST /auth/register`, the backend creates the user,
opens a session, and returns `201 AuthMeResponse` — the user is immediately logged in without a
second round-trip to `POST /auth/login`.

## In Scope

### Backend (`security/mill-security-auth-service`, Kotlin)

1. **`POST /auth/public/register`** added to `AuthPublicController` (same controller as
   `POST /auth/public/login`):
   - Request body: `RegisterRequest { email: String, password: String, displayName: String? }`
   - Checks `mill.security.allow-registration` — returns `403` if disabled.
   - Validates: email format, password non-empty (optional: minimum length).
   - Checks uniqueness: `UserIdentityRepository.findByProviderAndSubject("local", email)` —
     returns `409 Conflict` with structured error if already registered.
   - Calls `UserIdentityResolutionService.resolveOrProvision("local", email, displayName, email)`.
   - Creates `UserCredentialRecord` with BCrypt hash.
   - Opens session (same as successful login).
   - Returns `201 AuthMeResponse`.
   - No `SecurityFilterChain` changes needed: `/auth/public/**` is already `permitAll()` in the
     `@Order(-5)` chain from WI-087.

2. **`mill.security.allow-registration`** config property — default `false`, documented in
   `additional-spring-configuration-metadata.json`.

3. **`RegisterRequest`** DTO + `409 Conflict` error response shape.

4. **OpenAPI annotation** on `POST /auth/register`.

### Frontend (`ui/mill-ui/`)

5. **New `loginRegistration: boolean` feature flag** in `src/features/defaults.ts` (default `false`
   in production, `true` in dev/demo). Gates the "Sign up" link and the registration form.

6. **`register(email, password, displayName?)` in `authService.ts`**:
   - `POST /auth/public/register`; on `201` behaves like `login()` — sets user in `AuthContext`.
   - On `409` returns a descriptive error string.

7. **Registration form in `LoginPage.tsx`**:
   - Toggle between "Sign in" and "Create account" modes within the same `Paper` card (no separate
     page; consistent with the existing full-screen card layout).
   - "Sign up" click → switch to register mode showing: email, password, display name (optional).
   - "Sign in" back-link → return to login mode.
   - Submit calls `authService.register()`; shows inline `Alert` on `409` (already registered) or
     other errors.
   - On `201`, `AuthContext.user` is set and the app renders the main shell — same as login.
   - Visible only when `flags.loginRegistration` is `true`.

8. **Update existing `LoginPage` test** — the "Sign up" link test should verify the link is visible
   when `loginRegistration=true` and absent when `false`.

## Out of Scope

- Email verification / confirmation flow
- Password strength rules beyond minimum length
- Admin-managed user creation (separate admin WI)
- "Forgot password?" flow (separate WI — the link is also a dead placeholder)
- OAuth-based self-registration (SEC-4)

## Documentation

All Kotlin production code must carry full KDoc down to method and parameter level. TypeScript
service functions and types should carry JSDoc comments.

## Dependencies

- WI-085 (`UserIdentityResolutionService.resolveOrProvision()` + `UserCredentialRecord`)
- WI-086 (`UserCredentialRepository`, BCrypt `PasswordEncoder` bean)
- WI-087 (`AuthPublicController` in `mill-security-auth-service`, `/auth/public/**` security chain)
- WI-090 (`LoginPage` inside router, `authService.ts` barrel, `AuthContext`)

## Implementation Plan

1. Add `mill.security.allow-registration` config property with default `false`; document in
   `additional-spring-configuration-metadata.json`.
2. Add `RegisterRequest` DTO with full KDoc.
3. Implement `POST /auth/public/register` in `AuthPublicController` with full KDoc:
   check flag → validate → check uniqueness → `resolveOrProvision` → create credential →
   open session → return `201 AuthMeResponse`.
4. Unit tests (`src/test/`): registration success, duplicate `409`, disabled `403`, invalid
   email `422`.
5. Integration tests (`src/testIT/`): register success → `201` + session, duplicate → `409`,
   disabled → `403`.
6. Add `register()` to `authService.ts` (JSDoc).
7. Add `loginRegistration` feature flag to `defaults.ts` (default `false`).
8. Add `/register` route with `RegisterPage` component; `"Sign up"` link navigates to `/register`.
9. Implement `RegisterPage`: email, password, display name (optional); submit calls
   `authService.register()`; shows inline `Alert` on `409`; on `201` user is logged in.
10. Vitest tests: sign-up link hidden by default, visible with `loginRegistration=true`,
    `RegisterPage` form fields, `409` error display, successful registration navigates to app.

## Acceptance Criteria

- `POST /auth/public/register` with a new email creates the user and returns `201 AuthMeResponse`
  with an active session.
- `POST /auth/public/register` with an existing email returns `409`.
- `POST /auth/public/register` when `mill.security.allow-registration=false` returns `403`.
- In `mill-ui` with `loginRegistration=true`: "Sign up" link is visible; clicking it shows the
  registration form; successful submission logs the user in automatically.
- In `mill-ui` with `loginRegistration=false` (default): "Sign up" link is not rendered.
- The registered user's `UserProfileRecord` is auto-created and returned in `GET /auth/me`.
- Integration tests pass against H2.

## Deliverables

- This work item definition.
- `POST /auth/public/register` in `AuthPublicController` with full KDoc.
- `mill.security.allow-registration` config property + metadata.
- `RegisterRequest` DTO with KDoc.
- `register()` in `authService.ts` (JSDoc).
- `loginRegistration` feature flag in `defaults.ts`.
- `/register` route with `RegisterPage` component.
- Unit tests and integration tests (`testIT` suite).
- Vitest tests for `RegisterPage` and `"Sign up"` link visibility.
