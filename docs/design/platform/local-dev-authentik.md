# Local development: Authentik (OIDC IdP)

This document describes the **Authentik** stack wired in `deploy/local-dev/docker-compose.yml` for local OIDC/OAuth2 development (e.g. Spring Boot `oauth2Client` / `oauth2ResourceServer`). Official product documentation lives at [goauthentik.io](https://docs.goauthentik.io/).

## Goals

- Run a real IdP beside **Mill local Postgres** without a second database container.
- Seed **groups, users, an OAuth2 application, and access bindings** from Git-tracked blueprints.
- Prefer **HTTP** on a mapped port for simple JVM trust during Spring integration; optional HTTPS for browser-only tests.

## Compose topology

| Service | Role |
|---------|------|
| `postgres` | Host DB `mill` plus init-created DB `authentik` and role `authentik` (`deploy/local-dev/postgres/init/01-create-db.sql`). Authentik must own the `authentik` database for migrations on PostgreSQL 15+ (`ALTER DATABASE … OWNER`). |
| `authentik` | `command: server`, `shm_size: 512mb`, ports **19000→9000** (HTTP), **19443→9443** (HTTPS, self-signed). |
| `authentik-worker` | `command: worker` — **required**: bundled blueprints (default flows, tenant setup) are applied via background tasks; server-only startup leaves flows missing and login URLs return 404. |

**Critical:** the upstream image entrypoint is `ak` with **no default CMD**; omitting `command: server` exits immediately after printing Django help. See the [official 2026.2 compose](https://goauthentik.io/version/2026.2/lifecycle/container/compose.yml).

Shared environment is defined once (`x-authentik-environment`) and reused on **both** server and worker. Notable variables:

- `AUTHENTIK_URL` — set to `http://localhost:19000` by default so discovery `issuer` and links match browser access over HTTP.
- `AUTHENTIK_BOOTSTRAP_EMAIL` / `AUTHENTIK_BOOTSTRAP_PASSWORD` — first-run bootstrap for the built-in superuser **`akadmin`** (password is dev-only).

`authentik` / `authentik-worker` depend on Postgres **`service_healthy`** so bootstrap does not race against `pg_isready`.

## Blueprints (repo → container)

### Mount path

Custom files are mounted at **`/blueprints/custom`** (both server and worker).

Do **not** bind-mount over **`/blueprints` root** — that hides image-bundled `system/`, `default/`, and `migrations/` trees and breaks bootstrap (`bootstrap.yaml` missing).

### File extension

File discovery uses **`*.yaml` only** (`rglob("**/*.yaml")` in Authentik’s blueprint tasks). **`.yml` files are ignored.**

### Authoring rules

- Every blueprint file should declare **`metadata.name`**.
- Each `entries[]` row needs valid **`identifiers`** for its model (blueprint importer rejects “no or invalid identifiers”).
- Cross-file references use **`!Find [model, [field, value]]`**. References within the same file often use **`!KeyOf <entry-id>`** (e.g. application `provider: !KeyOf dev-provider`).

Repo layout: `deploy/local-dev/authentik/blueprints/` — ordered files for groups, users, applications/access.

### Passwords and groups on users

Blueprints may set **`password`** on `authentik_core.user` (plaintext in YAML; blueprint-only feature per [Models → user → password](https://docs.goauthentik.io/customize/blueprints/v1/models/)). Assign directory groups via **`attrs.groups`** as a list of `!Find` group references.

Do not commit production secrets; for shared environments prefer Admin UI or API for passwords.

### Applications and access control

- **`authentik_providers_oauth2.oauth2provider`** — client id/secret, flows (`!Find` default provider flows), redirect URIs.
- **`authentik_core.application`** — `slug`, `name`, `provider: !KeyOf …`, optional **`policy_engine_mode`** (`any` / `all`).
- **`authentik_policies.policybinding`** — bind **`target`** (e.g. application via `!Find [authentik_core.application, [slug, dev-app]]`) to a **`group`** (or `policy` / `user`) so only matching principals can launch or authorize the app. See [Bindings overview](https://docs.goauthentik.io/add-secure-apps/bindings-overview/) and [Manage applications](https://docs.goauthentik.io/add-secure-apps/applications/manage_apps/).

## OIDC discovery and Spring Boot

For the bundled **`dev-app`** application, OpenID configuration is served at:

`http://localhost:19000/application/o/dev-app/.well-known/openid-configuration`

Spring Boot **`issuer-uri`** should be the **issuer base** (no `.well-known` suffix), for example:

`http://localhost:19000/application/o/dev-app/`

Use the **same host** everywhere (`localhost` vs `127.0.0.1`) so the `issuer` claim matches Spring’s validation. OAuth2 **client id** / **secret** come from the provider (`dev-client` / `dev-secret` in the sample blueprint—not the application slug).

### UserInfo endpoint

Discovery advertises **`userinfo_endpoint`** (for local HTTP typically `http://localhost:19000/application/o/userinfo/`). Spring Boot should use that URL from OIDC metadata — do **not** override `user-info-uri` to `…/application/o/<app-slug>/userinfo/`; on Authentik **2026.2** that path returns **404**, which surfaces as `invalid_user_info_response` during `oauth2Login`.

If an older IdP build returned **403** on the global userinfo URL, fix IdP routing or scopes rather than pointing the client at a non-existent application-scoped userinfo path.

## HTTP vs HTTPS

- **HTTP (`19000`)** — no extra JVM trust; recommended for local Spring metadata and JWKS fetch.
- **HTTPS (`19443`)** — Authentik’s default **self-signed** certificate; the JVM must trust it (truststore / SSL bundle) or TLS fails.

## Operations

```bash
cd deploy/local-dev
docker compose up -d postgres authentik authentik-worker
```

- Admin UI: `http://localhost:19000/if/admin/` (trailing slash matters for some paths).
- Re-apply a file after edits: `docker exec mill-local-authentik-worker ak apply_blueprint /blueprints/custom/<file>.yaml` or wait for discovery.

## API and alternatives

Authentik exposes **REST API v3** under `/api/v3/` for CRUD on applications, providers, users, etc. Declarative **blueprints** remain preferable for repeatable local baselines; see [Blueprints](https://docs.goauthentik.io/customize/blueprints/) and [API](https://docs.goauthentik.io/developer-docs/api/).

## Relation to Mill security design

Mill’s own security architecture (profiles, PAT, future OAuth identity mapping) is documented under `docs/design/security/`. This Authentik stack is an **external OIDC fixture** for local integration testing; it does not replace Mill’s `mill.security.*` configuration model.

### Mill configuration and UI

- **Operator guide (published docs):** [Authentik (OIDC)](../../public/src/security/authentik-oidc.md) — enabling the `oauth` profile, JWT resource server, Mill UI feature flags, and troubleshooting.
- **Implementation overview (design):** [OAuth2 / OIDC integration](../security/oauth2-oidc-mill-authentik.md) — filter chains, `/app/login`, and code pointers.
