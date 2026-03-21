# WI-088 - mill-ui User Profile UI

Status: `done`
Type: `feature`
Area: `security`, `ui`
Backlog refs: `SEC-3b`

## Problem Statement

`mill-ui` already has a complete `ProfileLayout` with General/Settings/Access sections and an
`AppHeader` user menu showing avatar + user info — but both are fully hardcoded to "Demo User" /
`"demo@datachat.io"` with placeholder `"DC"` initials. The backend has no profile read/update
endpoints. After WI-087 delivers real auth, these components need to be wired to actual user data.

## Goal

Implement `GET /auth/me` profile extension and `PATCH /auth/profile` on the backend, then replace
all hardcoded user data in `mill-ui` with live data from `AuthContext`.

## Baseline State

**Backend (`security/mill-security-auth-service` — created in WI-087):**
- `GET /auth/me` returns `AuthMeResponse` without profile data yet.
- `UserProfileRecord` + `UserProfileRepository` exist from WI-085 but not wired to any endpoint.

**Frontend (`ui/mill-ui/`):**
- `src/components/profile/ProfileLayout.tsx` — **already exists** with 3 sections (General,
  Settings, Access), each gated by `flags.profileGeneral/profileSettings/profileAccess`. Sidebar
  shows hardcoded `"Demo User"` and `"demo@datachat.io"` avatar. `ProfilePanel` content is a
  placeholder ("This section is under construction.").
- `src/components/layout/AppHeader.tsx` — user `Menu` **already exists** with hardcoded `"DC"`
  `Avatar`, `"Demo User"`, `"demo@datachat.io"`. Already navigates to `/profile` via "Profile" item.
- `src/App.tsx` — after WI-087, `AuthContext` carries a real `AuthMeResponse` user object.
- Icons: `react-icons/hi2` (Heroicons v2).

## Extensibility Design

The current iteration delivers a minimal profile (display name, email, locale). The design is
intentionally relational and additive — no JSON blobs or catch-all maps.

### Relational extension pattern

- **New core attributes** → new nullable columns in `user_profiles` via a Flyway migration.
  Backward-compatible: existing rows get `NULL`; existing clients that omit the field leave it
  unchanged. The DTO gains a new optional field with a sensible default.
- **Domain-specific extensions** → a separate extension table per domain, linked by `userId` FK,
  owned by that domain's persistence module. Each domain composes its own slice of profile data
  at the API level — no cross-domain coupling in `user_profiles`.
  See `docs/design/security/user-profile-extensibility.md` for the full extension model.

### UI extensibility

Profile sections are already gated by `FeatureFlags`. Each `ProfilePanel` section receives a
`UserProfileResponse` prop (not `useAuth()` directly) so new sections can be added and tested
in isolation without coupling to `AuthContext`.

---

## In Scope

### Backend (`security/mill-security-auth-service`, Kotlin)

1. **`UserProfileService`**:
   - `getOrCreate(userId: String): UserProfileRecord` — lazy-creates an empty profile on first call.
     `userId` is always the canonical `users.id` from `UserIdentityResolutionService` — the same
     userId regardless of auth method (basic or OAuth).
   - `update(userId: String, patch: UserProfilePatch): UserProfileRecord` — partial update;
     deep-merges `extensions` map.

2. **Extend `GET /auth/me`** to include profile data:
   - Calls `UserProfileService.getOrCreate(userId)` after resolving the principal.
   - Adds `profile: UserProfileResponse` to `AuthMeResponse`.
   - Security-off: `profile` is `null` for the anonymous user.

3. **`PATCH /auth/profile`**:
   - Accepts `UserProfilePatch` JSON body (see DTO below).
   - Validates email format if present.
   - Returns `200 UserProfileResponse` on success; `401` if unauthenticated; `422` on validation failure.
   - Partial update: only updates fields present in the patch; deep-merges `extensions`.

4. **`UserProfileResponse`** DTO (Kotlin data class, fully KDoc'd):
   - `userId: String`, `displayName: String?`, `email: String?`, `locale: String?`
   - Note: theme is managed client-side via Mantine's `ThemeContext` — not stored server-side for now.
   - New attributes are added as nullable fields when needed (relational, not a catch-all map).

5. **`UserProfilePatch`** DTO (Kotlin data class, fully KDoc'd):
   - `displayName: String?`, `email: String?`, `locale: String?` — all optional; absent = no change.

### Frontend (`ui/mill-ui/`)

6. **TypeScript types** — `UserProfileResponse` and `UserProfilePatch` mirror the backend DTOs;
   new fields are added as optional properties when the backend DTO gains them.

7. **`updateProfile(patch)` in `authService.ts`**:
   - `PATCH /auth/profile`; on success returns updated `UserProfileResponse`.

8. **`AuthContext` extension** — add `updateProfile(patch): Promise<void>` action that calls
   `authService.updateProfile()` and merges the result into the `user` state.

9. **Wire real data into `AppHeader`**:
   - Replace hardcoded `"DC"` with computed initials from `user.profile?.displayName ?? user.username`.
   - Replace hardcoded `"Demo User"` with `user.profile?.displayName ?? user.username`.
   - Replace hardcoded `"demo@datachat.io"` with `user.profile?.email ?? ''`.
   - Hide user menu entirely when `!user.securityEnabled` (security-off mode).

10. **Wire real data into `ProfileLayout` sidebar**:
    - Replace hardcoded avatar initials, display name, and email with values from `useAuth()`.

11. **Implement `ProfilePanel` sections** (replacing placeholders):
    - Each section receives `profile: UserProfileResponse` as a prop — **not** `useAuth()` directly.
      This keeps sections testable in isolation and decoupled from `AuthContext`.
    - **General** (`profileGeneral`): editable form — display name (`TextInput`), email (`TextInput`).
      "Save" button calls `AuthContext.updateProfile()`; shows Mantine `Notification` on success or error.
    - **Settings** (`profileSettings`): locale selector (`Select`). Saves via `updateProfile()`.
    - **Access** (`profileAccess`): placeholder "Personal access tokens — coming soon." PAT management
      is deferred (SEC-2) but the section should exist visibly so the nav item is not wasted.

## Out of Scope

- Theme preference stored server-side (Mantine `ThemeContext` already handles this client-side)
- Avatar image upload
- Password change
- PAT management implementation (deferred — SEC-2; Access section shows placeholder only)
- Admin management of other users' profiles

## Documentation

All Kotlin production code (service, DTOs, configuration) must carry full KDoc down to method
and parameter level. TypeScript types and service functions should carry JSDoc comments.

## Dependencies

- WI-085 (`UserProfileRecord`, `UserProfileRepository`)
- WI-086 (JPA-backed authenticated users)
- WI-087 (`mill-security-auth-service` module, `AuthMeResponse`)
- WI-090 (real `AuthContext` and `authService.ts` foundation in `mill-ui`)

## Implementation Plan

1. Implement `UserProfileService.getOrCreate()` + `update()` with full KDoc.
2. Add `UserProfileResponse` + `UserProfilePatch` DTOs with KDoc; extend `AuthMeResponse` with
   `profile: UserProfileResponse?`.
3. Wire `getOrCreate()` into `GET /auth/me`.
4. Implement `PATCH /auth/profile` in `AuthController` with KDoc.
5. Unit tests (`src/test/`): `UserProfileServiceTest` with mocked repository — `getOrCreate`
   creates on first call, returns existing on second; `update` persists only present fields.
6. Integration tests (`src/testIT/`): lazy profile creation, partial update, re-read via
   `GET /auth/me`; `PATCH /auth/profile` with missing fields (partial update).
7. Add `updateProfile()` to `authService.ts` (JSDoc) and `AuthContext`.
8. Replace hardcoded user data in `AppHeader` and `ProfileLayout` sidebar with `useAuth()`.
9. Implement General and Settings `ProfilePanel` sections with save + Mantine `Notification`.
10. Add Access section placeholder.
11. Vitest tests: `AppHeader` shows real initials/name/email; General form calls `updateProfile()`;
    Settings saves locale; Access section shows placeholder.

## Acceptance Criteria

- First `GET /auth/me` after login returns a non-null `profile` (auto-created).
- `PATCH /auth/profile { displayName: "Alice" }` persists and is reflected in next `GET /auth/me`.
- `AppHeader` shows correct user initials and display name after login.
- `ProfileLayout` sidebar shows the real user's name and email.
- General section form saves display name and email; shows success notification.
- Settings section saves locale.
- Access section is visible but shows a "coming soon" placeholder.
- In security-off mode, user menu is hidden.
- Integration tests pass against H2.

## Deliverables

- This work item definition.
- `UserProfileService` (fully KDoc'd) + `UserProfileResponse` + `UserProfilePatch` DTOs (Kotlin, KDoc'd).
- Extended `AuthMeResponse` with `profile` field (backend + TypeScript).
- `PATCH /auth/profile` in `AuthController` (fully KDoc'd).
- `updateProfile()` in `authService.ts` (JSDoc) + `AuthContext`.
- Real data wired into `AppHeader` and `ProfileLayout`.
- Implemented General + Settings `ProfilePanel` content; Access placeholder.
- Unit tests (`UserProfileServiceTest`).
- Integration tests (H2, `testIT` suite).
- Vitest tests for UI components.
