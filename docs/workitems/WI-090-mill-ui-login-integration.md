# WI-090 - mill-ui Login Integration

Status: `done`
Type: `feature`
Area: `ui`
Backlog refs: `SEC-3a`

## Problem Statement

`mill-ui` has all the auth UI scaffolding in place but it is entirely mock — `AuthContext` in
`App.tsx` is explicitly labelled as a lightweight mock and `isAuthenticated` starts as `true` with
no real API call. The `LoginPage` calls an `onLogin` callback that just flips a boolean. The
`AppHeader` user menu shows hardcoded `"Demo User"` / `"demo@datachat.io"`. The backend endpoints
from WI-087 are in place; this WI wires the UI to them.

## Goal

Wire the existing `mill-ui` login page and `AuthContext` to the real backend endpoints
(`POST /auth/public/login`, `POST /auth/logout`, `GET /auth/me`, `GET /.well-known/mill`).

## Baseline State

**Backend (delivered in WI-087):**
- `POST /auth/public/login` — returns `AuthMeResponse` + session cookie.
- `POST /auth/logout` — invalidates session.
- `GET /auth/me` — returns `AuthMeResponse` or `401`.
- `GET /.well-known/mill` — now includes `name: String`.
- `AuthMeResponse` shape: `{ userId, email, displayName, groups, securityEnabled }`.

**Frontend (`ui/mill-ui/`):**
- `App.tsx` — mock `AuthContext`; `isAuthenticated` starts `true`; login/logout flip booleans.
- `LoginPage.tsx` — `onLogin` is a no-arg callback; `"Sign up"` is a dead link.
- `AppHeader.tsx` — hardcoded `"DC"` / `"Demo User"` / `"demo@datachat.io"`.
- `src/services/` — no `authService.ts` yet; `api.ts` barrel exists.

## In Scope

1. **`authService.ts`** (new, `src/services/authService.ts`):
   - `login(email: string, password: string): Promise<AuthMeResponse>` — `POST /auth/public/login`
   - `logout(): Promise<void>` — `POST /auth/logout`
   - `getMe(): Promise<AuthMeResponse | null>` — `GET /auth/me`; returns `null` on `401`
   - Export via `src/services/api.ts` barrel

2. **Replace mock `AuthContext` in `App.tsx`**:
   - State: `{ user: AuthMeResponse | null, loading: boolean }`
   - On mount: call `authService.getMe()`. `401` + security enabled → `user=null` (shows login).
     `securityEnabled=false` → `user=anonymousUser` (skip login, never show login page).
   - `isAuthenticated` derived from `user !== null`.
   - While `loading`: render full-screen Mantine `Loader` — no flash of login page or app shell.
   - `login(email, password)`: call `authService.login()`, set `user`.
   - `logout()`: call `authService.logout()`, set `user=null`.

3. **`LoginPage` must move inside the router**. `App.tsx` currently renders `<LoginPage />`
   outside `<Routes>`. After this WI, a `RequireAuth` wrapper redirects unauthenticated users to
   `/login`, and `<Route path="/login" element={<LoginPage />} />` is added inside `<Routes>`.
   This is required so WI-089 (`/register`) and future WI (`/forgot-password`) work as real routes.

4. **`LoginPage` wiring**:
   - `onLogin` prop signature becomes `(email: string, password: string) => Promise<void>`.
   - Form submit calls `onLogin(email, password)`; shows inline Mantine `Alert` on error (`401`).
   - `"Sign up"` link navigates to `/register` (route implemented in WI-089).

5. **`APP_NAME` from backend** — on app startup call `GET /.well-known/mill` and read `name`;
   fall back to `'Mill'`. Resolves the existing `// TODO: will come from the backend` comment in
   `App.tsx`.

6. **Security-off behaviour** in UI:
   - `AuthContext` receives `securityEnabled=false` → `isAuthenticated=true`, login never shown.
   - `"Log out"` item in `AppHeader` user menu hidden when `!user.securityEnabled`.

## Out of Scope

- User registration form / `/register` route (WI-089)
- Profile data wiring in `AppHeader` and `ProfileLayout` (WI-088)
- OAuth/SSO provider buttons (flags exist; wiring is SEC-4)

## Dependencies

- WI-087 (`mill-security-auth-service` backend endpoints, `AuthMeResponse`, `ApplicationDescriptor.name`)
- WI-088 depends on this WI for `AuthContext.user` carrying a real `AuthMeResponse`

## Implementation Plan

1. Add `authService.ts` (`login`, `logout`, `getMe`); export from `api.ts`.
2. Replace mock `AuthContext` in `App.tsx` with real state + loading; wire `login`/`logout` to service.
3. Move `LoginPage` inside router; add `RequireAuth` wrapper; add `/login` route.
4. Update `LoginPage` `onLogin` prop to `(email, password) => Promise<void>`; add error `Alert`.
5. Wire `"Sign up"` link to navigate to `/register`.
6. Wire `APP_NAME` from `GET /.well-known/mill` on startup.
7. Hide `"Log out"` menu item when `!user.securityEnabled`.
8. Unit tests (Vitest):
   - `authService` — mocked fetch: `login` success, `login` `401`, `logout`, `getMe` success,
     `getMe` `401` returns `null`.
   - `App.tsx` `AuthContext` — loading state on mount, `getMe` success sets user, `getMe` `401`
     leaves user null, security-off sets anonymous user.
   - `LoginPage` — error `Alert` shown on `401`, `"Sign up"` navigates to `/register`,
     `RequireAuth` redirects unauthenticated to `/login`.

## Acceptance Criteria

- `authService.login()` sets session cookie; subsequent `authService.getMe()` returns correct user.
- `authService.logout()` clears session; `getMe()` returns `null`.
- While bootstrap `getMe()` is in-flight, a full-screen `Loader` is shown (no flash).
- When `securityEnabled=false`, app loads directly without showing login page; `"Log out"` hidden.
- `LoginPage` shows inline error on wrong password.
- `"Sign up"` link navigates to `/register`.
- `APP_NAME` is populated from backend; falls back to `'Mill'`.
- Vitest tests pass.

## Deliverables

- `src/services/authService.ts`.
- Replaced `AuthContext` in `App.tsx` (real API, loading state, security-off).
- `LoginPage` inside router with `RequireAuth`; error display; `"Sign up"` → `/register`.
- `APP_NAME` from `GET /.well-known/mill`.
- Vitest unit tests.
