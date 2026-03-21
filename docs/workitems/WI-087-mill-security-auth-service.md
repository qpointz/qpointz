# WI-087 - mill-security-auth-service Backend

Status: `done`
Type: `feature`
Area: `security`
Backlog refs: `SEC-3a`

## Problem Statement

No backend auth HTTP endpoints exist. `mill-ui` login, logout, and session checks require
`POST /auth/public/login`, `POST /auth/logout`, and `GET /auth/me`. These must integrate cleanly
into the existing `SecurityFilterChain` chain without modifying `mill-service-security`.

## Goal

Implement the `security/mill-security-auth-service` Kotlin module: REST controllers, DTOs, two
`SecurityFilterChain` configuration classes, and the `ApplicationDescriptor.name` extension.
Frontend wiring is deferred to WI-090.

## Baseline: Existing Spring Security Chain

`security/mill-security-autoconfigure` defines several `SecurityFilterChain` beans:

- `AuthRoutesSecurityConfiguration` — **`@Order(0)`**, `securityMatcher("/id/**", "/oauth2/**",
  "/login/**", "/logout/**", "/auth/**", "/error**")`, `anyRequest().permitAll()`. Applies all
  configured `AuthenticationMethod` login/security configs to this chain.
- `ApiSecurityConfiguration` — `/api/**`
- `AppSecurityConfiguration` — `/app/**`
- `WellKnownSecurityConfiguration` — `/.well-known/**`, unconditional `permitAll()`
- `ServicesSecurityConfiguration` — service routes

`SecurityConfig` (also in `mill-security-autoconfigure`) provides `AuthenticationMethods`,
`AuthenticationProviders`, and the `AuthenticationManager` bean (all `@ConditionalOnSecurity`).

## Security Chain Design

Two configuration classes following established patterns in `mill-security-autoconfigure`.

### `AuthPublicSecurityConfiguration` — modelled after `WellKnownSecurityConfiguration`

Handles `/auth/public/**` (login, register WI-089, future forgot-password). Always publicly
accessible — no `@ConditionalOnSecurity`.

```
@Bean @Order(-6)   // before AuthRoutesSecurityConfiguration @Order(0)
fun permitAuthPublicPaths(http: HttpSecurity): SecurityFilterChain
    securityMatcher("/auth/public/**")
    anyRequest().permitAll()
    csrf disabled
```

### `AuthSecuredSecurityConfiguration` — modelled after `ApiSecurityConfiguration` (dual-bean)

Handles `/auth/me`, `/auth/logout`, `/auth/profile`.

```
@Bean @Order(-5) @ConditionalOnSecurity
fun secureAuthPaths(http: HttpSecurity, authenticationMethods: AuthenticationMethods): SecurityFilterChain
    securityMatcher("/auth/me", "/auth/logout", "/auth/profile")
    /auth/me     → permitAll()   (controller returns 401 or anonymous user)
    /auth/logout → permitAll()   (graceful no-op if session already gone)
    /auth/profile → authenticated()
    applies authenticationMethods.getProviders() securityConfig
    csrf disabled

@Bean @Order(-5) @ConditionalOnSecurity(false)
fun permitAuthPaths(http: HttpSecurity): SecurityFilterChain
    securityMatcher("/auth/me", "/auth/logout", "/auth/profile")
    anyRequest().permitAll()
    csrf disabled
```

**Login is handled programmatically in `AuthPublicController`** — no custom filter. The controller
calls `AuthenticationManager.authenticate()` directly (injected as optional; absent when security
is off), creates the HTTP session, and sets the `SecurityContext`. This avoids the complexity of a
custom `AbstractAuthenticationProcessingFilter` and is consistent with Spring Boot 3.x practice.

**Chain order summary for `/auth/**`:**

| Order | Chain | Matcher | Condition |
|-------|-------|---------|-----------|
| `-6` | `AuthPublicSecurityConfiguration` | `/auth/public/**` | always |
| `-5` | `AuthSecuredSecurityConfiguration` | `/auth/me`, `/auth/logout`, `/auth/profile` | security ON |
| `-5` | `AuthSecuredSecurityConfiguration` | `/auth/me`, `/auth/logout`, `/auth/profile` | security OFF |
| `0` | `AuthRoutesSecurityConfiguration` | `/id/**`, `/oauth2/**`, `/login/**`, `/logout/**`, `/auth/**` | security ON |

## Principal Identity Extraction

`GET /auth/me` calls `UserIdentityResolutionService.resolve(provider, subject)` after extracting
`(provider, subject)` from the authenticated principal using a private helper:

| Authentication type | provider | subject |
|---------------------|----------|---------|
| `UsernamePasswordAuthenticationToken` | `"local"` | `authentication.name` |
| `OAuth2AuthenticationToken` | `authentication.authorizedClientRegistrationId` | `authentication.principal.name` |
| unauthenticated / anonymous | — | — → returns `401` or anonymous response |

## In Scope

### New module `security/mill-security-auth-service` (Kotlin)

Module dependencies:
- `security/mill-security` — `UserIdentityResolutionService` interface, `@ConditionalOnSecurity`
- `security/mill-security-autoconfigure` — `AuthenticationManager`, `AuthenticationMethods`
- `spring-boot-starter-web` — REST controllers
- `spring-boot-starter-security` — `HttpSecurity`, `SecurityFilterChain`

`apps/mill-service` adds `mill-security-auth-service` alongside `mill-well-known-service`.

`build.gradle.kts` follows the `mill-ai-v3-persistence` pattern (see CLAUDE.md Testing Structure).

**`@ConfigurationProperties` note**: `AuthPublicSecurityConfiguration` and
`AuthSecuredSecurityConfiguration` are pure `SecurityFilterChain` wiring — they do not bind
configuration properties, so no metadata generation is required for this module. If future work
adds `@ConfigurationProperties` classes here, follow the CLAUDE.md rule: Java (annotation
processor) or Kotlin + `additional-spring-configuration-metadata.json`.

1. **`AuthMeResponse`** DTO (Kotlin data class, fully KDoc'd):
   - `userId: String` — canonical `users.id` (never the OAuth subject)
   - `email: String?`
   - `displayName: String?`
   - `groups: List<String>`
   - `securityEnabled: Boolean`

2. **`AuthPublicController`** at `/auth/public` (fully KDoc'd):
   - `POST /auth/public/login` — JSON body `{ email: String, password: String }`; calls
     `AuthenticationManager.authenticate()` programmatically (optional injection — absent when
     security off); success → creates `HttpSession`, sets `SecurityContext`, returns
     `200 AuthMeResponse` + `Set-Cookie: JSESSIONID`; failure → `401` structured `ErrorResponse`.
     When security is off → returns anonymous `AuthMeResponse(securityEnabled=false)`.

3. **`AuthController`** at `/auth` (fully KDoc'd):
   - `POST /auth/logout` — invalidates `HttpSession`; returns `200` (no-op if already logged out).
   - `GET /auth/me` — extracts `(provider, subject)` from `Authentication` (see Principal
     Identity Extraction above); resolves canonical `userId` via
     `UserIdentityResolutionService.resolve()`; returns `AuthMeResponse`. Unauthenticated → `401`.
     Security off → anonymous `AuthMeResponse(securityEnabled=false)`.

4. **`AuthPublicSecurityConfiguration`** — `@Order(-6)` unconditional bean (fully KDoc'd).

5. **`AuthSecuredSecurityConfiguration`** — dual-bean `@Order(-5)` (fully KDoc'd).

6. **`ApplicationDescriptor.name` extension** — add `name: String` field to `ApplicationDescriptor`
   (in `services/mill-service-api`) populated from `spring.application.name` (default `"Mill"`).
   `ApplicationDescriptorConfiguration` passes it through. Frontend reads it in WI-090.

7. **OpenAPI annotation** on both controllers (SpringDoc).

### Security-off behaviour

When `mill.security.enable=false`:
- `GET /auth/me` returns `AuthMeResponse(userId="anonymous", email=null, securityEnabled=false, groups=[])`.
- `POST /auth/public/login` returns the same anonymous `AuthMeResponse` without attempting authentication.

## Out of Scope

- mill-ui wiring (WI-090)
- User registration (WI-089)
- User profile endpoints (WI-088)
- "Forgot password?" (future WI)
- OAuth/SSO provider buttons (SEC-4)
- PAT management (SEC-2)

## Dependencies

- Pre-requisite module restructuring — `mill-security-autoconfigure` must exist with `AuthenticationManager` bean
- WI-085 (`JpaUserIdentityResolutionService` in `mill-security-persistence`) — canonical userId resolution in `GET /auth/me`
- WI-086 (`JpaPasswordAuthenticationConfiguration`) — JPA-backed users; file-store acceptable for dev

## Implementation Plan

1. Create `security/mill-security-auth-service` module; add to `settings.gradle.kts`.
2. Write `build.gradle.kts` (see CLAUDE.md Testing Structure).
3. Add `name: String` field to `ApplicationDescriptor`; wire from `spring.application.name`.
4. Implement `AuthMeResponse` DTO with full KDoc.
5. Implement `AuthPublicSecurityConfiguration` with full KDoc.
6. Implement `AuthSecuredSecurityConfiguration` with full KDoc.
7. Implement `AuthPublicController.login()` — programmatic auth, session creation, security-off fallback; full KDoc.
8. Implement `AuthController.logout()` and `AuthController.getMe()` with principal extraction
   and security-off fallback; full KDoc.
9. Add OpenAPI annotations.
10. Unit tests (`src/test/`): `AuthPublicControllerTest`, `AuthControllerTest` with mocked
    `AuthenticationManager` and `UserIdentityResolutionService` — login success, wrong password,
    security-off, `getMe` authenticated vs unauthenticated, logout.
11. Integration tests (`src/testIT/`): full Spring Boot context with H2 — login success,
    wrong password → `401`, logout → session invalidated, `GET /auth/me` round-trip,
    security-off mode, chain precedence (`@Order(-6)`/`-5` take priority over `@Order(0)`).

## Acceptance Criteria

- `POST /auth/public/login` with correct credentials returns `200 AuthMeResponse` + session cookie.
- `POST /auth/public/login` with wrong credentials returns `401` JSON error (no redirect).
- `GET /auth/me` after login returns correct user; `401` after logout.
- `POST /auth/logout` invalidates session; subsequent `GET /auth/me` returns `401`.
- When `mill.security.enable=false`, `GET /auth/me` returns anonymous `AuthMeResponse(securityEnabled=false)`.
- `GET /.well-known/mill` includes the `name` field.
- All controllers and configuration classes carry full KDoc.
- Unit and integration tests pass.

## Deliverables

- `security/mill-security-auth-service` Kotlin module with `build.gradle.kts`.
- `ApplicationDescriptor.name` field extension.
- `AuthMeResponse` DTO (fully KDoc'd).
- `AuthPublicSecurityConfiguration` (`@Order(-6)` unconditional, fully KDoc'd).
- `AuthSecuredSecurityConfiguration` (`@Order(-5)` dual-bean, fully KDoc'd).
- `AuthPublicController` with `POST /auth/public/login` (programmatic auth, fully KDoc'd).
- `AuthController` with `GET /auth/me` and `POST /auth/logout` (fully KDoc'd).
- OpenAPI annotations on both controllers.
- Unit tests and integration tests (`testIT` suite).
