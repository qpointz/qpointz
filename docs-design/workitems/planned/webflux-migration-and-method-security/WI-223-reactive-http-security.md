# WI-223 — Reactive HTTP security (filter chains + method security)

Status: `planned`  
Type: `refactoring`  
Area: `platform`, `security`  
Backlog refs: **P-34**, **P-1**

## Goal

Port servlet **`SecurityFilterChain`** / `HttpSecurity` configuration in **`mill-security-autoconfigure`** to **reactive** **`SecurityWebFilterChain`** / `ServerHttpSecurity`, preserving **path-based** semantics (`/api/**`, `/services/**`, `/.well-known/**`, `/app/**`, auth routes, swagger, etc.).

Enable **method-level security** for reactive controllers (**`@EnableReactiveMethodSecurity`**) so **`@PreAuthorize`** on handler methods is honored once **WI-227** lands.

## Scope

1. Mirror existing matchers and `authorizeExchange` rules from:
   - `ApiSecurityConfiguration`, `ServicesSecurityConfiguration`, `AppSecurityConfiguration`, `AuthRoutesSecurityConfiguration`, `WellKnownSecurityConfiguration`, `SwaggerSecurityConfig`, and related beans.
2. Ensure **`AuthenticationMethods`** (basic, OAuth2 resource server, Entra, etc.) still apply in a **WebFlux** security chain (refactor shared helpers if needed).
3. Document any intentional behavior deltas in a short note under [`docs/design/security/`](../../../design/security/) **or** completion notes in this WI (e.g. CSRF/CORS parity with current API chain).

## Acceptance

- With `mill.security.enable=true`, authenticated paths still require authentication; with `false`, **permit-all** behavior matches prior intent for the same path groups.
- A minimal **integration or slice test** proves a secured `/api/**` request returns **401/403** without credentials when security is on.

## Depends on

**WI-222**
