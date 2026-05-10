# OAuth2 / OIDC (Authentik) integration with Mill UI

This note summarizes how Mill wires **Spring Security** for browser **OIDC login** (`oauth2Login`) together with **JWT resource server** validation, and how that interacts with the **Mill UI** SPA. Operator steps live in the public doc **[Authentik (OIDC)](../../public/src/security/authentik-oidc.md)**; local IdP containers and blueprints are in **[Local development: Authentik](../platform/local-dev-authentik.md)**.

## Filter chains

- **`/app/**`** — Packaged SPA shell. Public entry points (`/app/login`, `/app/register`, `/app/assets/**`, and root shell assets such as icons) must stay **permitAll** so `oauth2Login`’s `loginPage("/app/login")` does not recurse through authenticated-only matchers.
- **Auth routes** (`/oauth2/**`, `/login/**`, `/id/**`, `/auth/**`, `/logout/**`, `/error`) — A dedicated chain applies **`applyLoginConfig`** only. **`applySecurityConfig`** (resource-server JWT on the same `HttpSecurity` as `oauth2Login`) is **not** applied on this chain, because combining both breaks the OIDC callback and surfaces as `/app/login?error`.

## Login page and success URL

- `OAuth2ResourceServiceAuthenticationMethod` registers **`oauth2Login`** with **`loginPage("/app/login")`** (replacing the legacy static `/id/login.html` entry).
- Optional **`mill.security.oauth2.login.*`** properties configure **`defaultSuccessUrl`** / **`alwaysUseDefaultSuccessUrl`** for cross-origin SPA development (for example Vite on **5173** while callbacks remain on Mill **8080**).

## Identity resolution

- **`AuthController`** resolves `(provider, subject)` from the session. For **`OAuth2AuthenticationToken`**, if **`resolve`** returns no row, Mill may call **`resolveOrProvision`** so **`GET /auth/me`** succeeds after the first OIDC login without a separate provisioning hook.

## UI and reverse proxy

- Mill UI builds OAuth links to **`/oauth2/authorization/{registrationId}`** at the servlet root; see **`oauth2AuthorizationHref`** in `ui/mill-ui` and Vite **`server.proxy`** for local dev.

## Related sources

| Area | Location |
|------|----------|
| Sample `oauth` profile YAML | `apps/mill-service/application.yml` |
| OAuth2 login properties | `MillOAuth2LoginProperties`, `OAuth2AuthenticationConfiguration` (`security/mill-security-autoconfigure`) |
| `oauth2Login` wiring | `OAuth2ResourceServiceAuthenticationMethod` (`security/mill-service-security`) |
| Authentik blueprint (RS256, scopes) | `deploy/local-dev/authentik/blueprints/02-applications.yaml` |
