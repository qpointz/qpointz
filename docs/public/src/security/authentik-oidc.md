# Authentik (OIDC) with Mill

This guide describes how to configure **Mill Service** (Spring Boot) and **Mill UI** so users can sign in through **[Authentik](https://goauthentik.io/)** using **OpenID Connect**. Mill uses Spring Security **`oauth2Login`** for the browser authorization-code flow and a **JWT resource server** to validate access tokens from the same issuer.

---

## What you need

- An Authentik **OAuth2/OpenID provider** with a known **issuer** URL (metadata at `{issuer}/.well-known/openid-configuration`).
- A **client id** and **client secret** registered for Mill.
- **Redirect URI** registered at the IdP for Spring Security’s default callback pattern:

  `{millBaseUrl}/login/oauth2/code/{registrationId}`

  Example when Mill listens on `http://localhost:8080` and the Spring registration id is `authentik`:

  `http://localhost:8080/login/oauth2/code/authentik`

- **RS256 (asymmetric) signing** for ID tokens so Spring can validate via **JWKS**. If the provider signs ID tokens with HS256 only and publishes an **empty JWKS**, OIDC login fails with algorithm or key mismatch errors. Configure an RSA signing key on the provider (see the [local development](../../../design/platform/local-dev-authentik.md) blueprint notes for the pattern used in this repository).

---

## Mill Service (Spring Boot)

### 1. Enable the OAuth client and JWT resource server

Configure **`spring.security.oauth2.client`** with a **provider** (`issuer-uri` = issuer base, no `.well-known` suffix) and a **registration** (grant type `authorization_code`, scopes such as `openid`, `profile`, `email`, redirect URI as above).

Enable Mill’s JWT resource server and point **`jwk-set-uri`** at the same issuer’s JWKS endpoint (typically advertised in OIDC discovery).

Set **`mill.security.enable: true`**.

### 2. Use discovery for UserInfo

Let Spring resolve **`userinfo_endpoint`** from OIDC metadata. On recent Authentik versions, overriding **`user-info-uri`** to an application-scoped path that does not exist causes **`invalid_user_info_response`** during login. If you see that error, remove custom user-info overrides and fix scopes or IdP routing instead. Details are documented under *UserInfo endpoint* in [`local-dev-authentik.md`](../../../design/platform/local-dev-authentik.md).

### 3. Optional: redirect to the Mill UI dev server after login

When the SPA is served by **Vite** on another origin (for example `http://localhost:5173`) while OAuth callbacks hit Mill on **8080**, configure:

| Property | Purpose |
|----------|---------|
| `mill.security.oauth2.login.default-success-url` | Absolute URL to open after a successful `oauth2Login` when no saved request applies (for example `http://localhost:5173/app/`). |
| `mill.security.oauth2.login.always-use-default-success-url` | When `true` together with a non-blank default URL, Mill registers `defaultSuccessUrl(url, true)` so incidental hits (for example favicon) are not used as the post-login redirect. |

Leave both unset in production if the default Spring behavior (saved request or site root) is sufficient.

### 4. Repository reference profile

The Mill repository ships an **`oauth`** Spring profile in `apps/mill-service/application.yml` that wires **Authentik** defaults for local development (`dev-client` / `dev-secret`, issuer and JWKS under `http://localhost:19000/...`). Activate it when running against the [local Authentik](../../../design/platform/local-dev-authentik.md) compose stack:

```bash
./gradlew :apps:mill-service:bootRun --args='--spring.profiles.active=oauth'
```

Override secrets with environment variables **`AUTHENTIK_CLIENT_ID`** and **`AUTHENTIK_CLIENT_SECRET`** where needed.

For **HTTP vs HTTPS** to the IdP and JVM trust implications, see the platform design doc linked above.

---

## Mill UI

- OAuth2 authorization starts at the **servlet root**: `/oauth2/authorization/{registrationId}` (not under the SPA base path such as `/app/`). Wrong prefixes are blocked by `/app/**` security rules and bounce back to the login page.
- The **Continue with Authentik** button is controlled by the **`loginAuthentik`** feature flag (see Mill UI feature-flag documentation in the repository). Set it to `false` in environments where Authentik is not offered.
- For **local development** with Vite, `ui/mill-ui/vite.config.ts` proxies `/oauth2/**`, OAuth callback paths, and `/logout` to the Mill backend so the browser can complete the flow while the SPA is on port **5173**.

---

## First login and `/auth/me`

After OIDC login, Mill may resolve the canonical user id on **`GET /auth/me`**. For OAuth principals, if no identity row exists yet, Mill can **provision** a user on first contact so the profile endpoint succeeds without a separate registration step (behavior subject to your deployment’s identity policy).

---

## Further reading

- [Local development: Authentik](../../../design/platform/local-dev-authentik.md) — Docker Compose, worker, blueprints, discovery URLs.
- [OAuth2 OIDC integration (design)](../../../design/security/oauth2-oidc-mill-authentik.md) — filter chains, SPA login route, and implementation pointers.
