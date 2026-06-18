# REST API Authorization Inventory

Code-first inventory of Mill REST endpoints, their **transport-layer** authorization (Spring Security filter chains), **application-layer** checks (controller/service), anonymous access, and a preliminary **sufficiency assessment** for a harmonization story.

**Scan date:** 2026-06-18  
**Primary runtime:** `apps/mill-service` (aggregates modules below)  
**Quick reference:** [Consolidated endpoint matrix](#consolidated-endpoint-matrix) (single table)  
**Related:** endpoint list without auth columns — [`REST-CONTROLLERS-INVENTORY.md`](REST-CONTROLLERS-INVENTORY.md); policy model — [`../platform/policy-guide.md`](../platform/policy-guide.md)

---

## Executive summary

| Metric | Value |
|--------|------:|
| Production `@RestController` classes (main source) | 17 |
| Documented HTTP operations | **89** ([matrix](#consolidated-endpoint-matrix)) |
| **Deprecated** paths (`@Deprecated` on handler) | **4** (schema `/schemas/…` aliases) |
| **Legacy (v1)** module routes (`/api/nl2sql/**`) | **8** (not on default `mill-service` classpath) |
| **Retired** routes (documented, no controller) | **1** (`POST /api/v1/queries/execute`) |
| Endpoints with `@PreAuthorize` / `@Secured` / role checks | **0** |
| Default `mill.security.enable` in `application.yml` | **`false`** (marked *LEGACY subject to review* — all matched API paths anonymous unless another chain applies) |

**Current model:** authorization is almost entirely **authentication gating** via ordered `SecurityFilterChain` beans in `security/mill-security-autoconfigure` and `security/mill-security-auth-service`. There is **no consistent RBAC or resource-ownership layer** on REST controllers. Data **policies** (row/column/table) apply at **query execution** (`SecurityDispatcher` / `PolicyEvaluator`), not uniformly at the HTTP boundary.

**Highest-priority gaps for harmonization:**

1. **Security off by default** — local/dev deployments expose data, metadata mutation, export, and AI chat without credentials.
2. **Authenticated-but-not-authorized** — any valid principal can mutate metadata scopes, facet types, saved queries, and (when security is on) read/write most resources.
3. **Missing ownership checks (AI v3)** — chat get/update/delete/message/SSE by `chatId` does not verify `userId`; default `PropertiesUserIdResolver` collapses all users to one id.
4. **Stub scope authorization** — `MetadataEntityController.requireScopeWriteAllowed()` is a no-op placeholder.
5. **Client-supplied audit actor** — metadata import accepts `?actor=` from the caller.
6. **Always-anonymous diagnostics** — `/actuator/valuemap`, `/actuator/beans` are `permitAll` even when security is enabled.
7. **Inconsistent write auth** — metadata entity writes require authentication in-controller; scope/facet/import endpoints do not.

---

## Security architecture (transport layer)

### Master switch

| Property | Default | Effect |
|----------|---------|--------|
| `mill.security.enable` | `false` | When `false`, `@ConditionalOnSecurity(false)` beans **permit all** on `/api/**`, `/app/**`, `/services/**`, Swagger. When `true`, same paths require **authenticated** (any mechanism). |

Configuration entry: `security/mill-security-autoconfigure/.../SecurityConfig.java`.

### Filter chains (by precedence)

| Order | Configuration class | Path matcher | Security enabled | Security disabled |
|------:|---------------------|--------------|------------------|-------------------|
| `HIGHEST_PRECEDENCE` | `MillServiceActuatorInspectSecurityConfiguration` | `/actuator/valuemap/**`, `/actuator/beans/**` | **permitAll** | **permitAll** |
| `-6` | `AuthPublicSecurityConfiguration` | `/auth/public/**` | **permitAll** | **permitAll** |
| `-5` | `AuthSecuredSecurityConfiguration` | `/auth/me`, `/auth/logout`, `/auth/profile` | `/me`, `/logout` permitAll; `/profile` **authenticated** | permitAll |
| `0` | `AuthRoutesSecurityConfiguration` | `/id/**`, `/oauth2/**`, `/login/**`, `/logout/**`, `/auth/**`, `/error**` | **permitAll** (+ login config) | *(bean inactive — security off)* |
| `0` | `WellKnownSecurityConfiguration` | `/.well-known/**` | **permitAll** | **permitAll** |
| `1` | `ApiSecurityConfiguration` | `/api/**` | **authenticated** | **permitAll** |
| `1` | `AppSecurityConfiguration` | `/app/**` | static/login/register permitAll; rest **authenticated** | **permitAll** |
| `1` | `ServicesSecurityConfiguration` | `/services/**` | **authenticated** | **permitAll** |
| `1` | `SwaggerSecurityConfig` | `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` | **authenticated** | **permitAll** |

**Not explicitly matched:** `/actuator/health/**` (and other actuator endpoints). Behavior depends on Spring Boot defaults and whether any other chain matches; not harmonized with the table above.

### Authentication mechanisms (when enabled)

Registered via `AuthenticationMethod` SPI (`security/mill-service-security`):

- HTTP Basic (`BasicAuthenticationMethod`)
- OAuth2 login + JWT resource server (`OAuth2ResourceServiceAuthenticationMethod`)
- Entra ID bearer (`EntraIdAuthenticationMethod`)

No method-level Spring Security annotations are used on controllers.

### Application-layer patterns

| Pattern | Where | Notes |
|---------|-------|-------|
| `requireAuthenticatedActor()` | `MetadataEntityController` writes | Rejects anonymous; no role/scope enforcement |
| `requireScopeWriteAllowed()` | `MetadataEntityController` | **Stub — always allows** |
| `requireCallerContext()` | `QueryResultRestController` | Maps principal → tenant; session **403** on wrong tenant |
| `UserIdResolver` | AI v3 chat | Default static user id; no security principal integration |
| `SecurityDispatcher` / policies | Data plane (Jet, SQL execution) | Row/column/table policy at query time, not REST metadata |

---

<h2 id="legend-inventory-tables">Legend (inventory tables)</h2>

| Column | Meaning |
|--------|---------|
| **Status** | Endpoint lifecycle — see [Endpoint lifecycle](#endpoint-lifecycle-legacy--deprecated). Omitted when **Current**. |
| **Filter auth (on)** | Rule when `mill.security.enable=true` |
| **Filter auth (off)** | Rule when `mill.security.enable=false` |
| **App auth** | Controller/service checks beyond the filter chain |
| **Anonymous OK** | Can succeed without credentials (given filter + app rules) |
| **Sufficient?** | Opinion for production; **No** = needs harmonization |

### Status values

| Status | Meaning |
|--------|---------|
| **Current** | Supported; intended for production use |
| **Deprecated** | Still served; `@Deprecated` and/or OpenAPI `deprecated=true`; replacement documented — schedule removal |
| **Legacy (v1)** | AI v1 NL2SQL module under `ai/legacy/`; superseded by `/api/v1/ai/chats/**`; not wired in default `mill-service` |
| **Retired** | Documented or previously advertised; **no handler** in current codebase |
| **Dev-only** | Exposed for local diagnostics; avoid in production |
| **Test-only** | Test kit; not production |

---

<h2 id="consolidated-endpoint-matrix">Consolidated endpoint matrix</h2>

Single-table view of **all documented REST surfaces** (production controllers, platform routes, legacy, retired, test). Column definitions: [Legend](#legend-inventory-tables). **Controller** names link to detailed sections below.

**Filter (on/off)** — `authenticated` when `mill.security.enable=true`, else `permitAll` for `/api/**`, `/services/**`, and most `/auth/**` paths (exceptions noted). **Anon OK** — request can succeed without credentials when security is **off** (`mill.security.enable=false`), unless app layer rejects (e.g. metadata writes return 401 from controller even if filter permits).

| Controller                                                            | M      | Path                                                                          | Status          | Filter (on/off)       | App auth                        | Anon OK        | Suff?   |
| --------------------------------------------------------------------- | ------ | ----------------------------------------------------------------------------- | --------------- | --------------------- | ------------------------------- | -------------- | ------- |
| [ApplicationDescriptorController](#application-descriptor-controller) | GET    | `/.well-known/mill` (+ `/`, trailing variants)                                | Current         | permitAll / permitAll | none                            | **Yes**        | Yes     |
| [SpringDoc](#springdoc-swagger)                                       | GET    | `/v3/api-docs/**`                                                             | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SpringDoc](#springdoc-swagger)                                       | GET    | `/swagger-ui.html`, `/swagger-ui/**`                                          | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [Actuator](#actuator)                                                 | GET    | `/actuator/health/**`                                                         | Current         | *(unmatched)*         | none                            | Likely **Yes** | Review  |
| [Actuator](#actuator)                                                 | GET    | `/actuator/valuemap/**`                                                       | Dev-only        | permitAll / permitAll | none                            | **Yes**        | No      |
| [Actuator](#actuator)                                                 | GET    | `/actuator/beans/**`                                                          | Dev-only        | permitAll / permitAll | none                            | **Yes**        | No      |
| [AuthPublicController](#auth-public-controller)                       | POST   | `/auth/public/login`                                                          | Current         | permitAll / permitAll | credential check when on        | **Yes**        | Yes     |
| [AuthPublicController](#auth-public-controller)                       | POST   | `/auth/public/register`                                                       | Current         | permitAll / permitAll | `allow-registration` gate       | **Yes**        | Cond    |
| [AuthController](#auth-controller)                                    | GET    | `/auth/me`                                                                    | Current         | permitAll / permitAll | 401 if no session when on       | **Yes**        | Yes     |
| [AuthController](#auth-controller)                                    | PATCH  | `/auth/profile`                                                               | Current         | auth / permit         | authenticated principal         | off: **Yes**   | No      |
| [AuthController](#auth-controller)                                    | POST   | `/auth/logout`                                                                | Current         | permitAll / permitAll | none                            | **Yes**        | Yes     |
| [OAuth infra](#oauth2-infrastructure)                                 | *      | `/oauth2/**`, `/login/**`, `/logout/**`, `/id/**`                             | Current         | permitAll when on     | browser login flows             | **Yes**        | Yes     |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/context`                                                      | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema`                                                              | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/schemas`                                                      | **Deprecated**  | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/tree`                                                         | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/model`                                                        | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/{schemaName}`                                                 | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/schemas/{schemaName}`                                         | **Deprecated**  | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/{schemaName}/tables/{tableName}`                              | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/schemas/{schemaName}/tables/{tableName}`                      | **Deprecated**  | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName}`         | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SchemaExplorerController](#schema-explorer-controller)               | GET    | `/api/v1/schema/schemas/{schemaName}/tables/{tableName}/columns/{columnName}` | **Deprecated**  | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | GET    | `/api/v1/metadata/entities`                                                   | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | GET    | `/api/v1/metadata/entities/{id}`                                              | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | GET    | `/api/v1/metadata/entities/{id}/facets/merge-trace`                           | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | GET    | `/api/v1/metadata/entities/{id}/facets`                                       | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | GET    | `/api/v1/metadata/entities/{id}/facets/{typeKey}`                             | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | GET    | `/api/v1/metadata/entities/{id}/history`                                      | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataEntityController](#metadata-entity-controller)               | POST   | `/api/v1/metadata/entities`                                                   | Current         | auth / permit         | requireActor                    | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | PUT    | `/api/v1/metadata/entities/{id}`                                              | Current         | auth / permit         | requireActor                    | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | PATCH  | `/api/v1/metadata/entities/{id}`                                              | Current         | auth / permit         | requireActor                    | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | DELETE | `/api/v1/metadata/entities/{id}`                                              | Current         | auth / permit         | requireActor                    | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | POST   | `/api/v1/metadata/entities/{id}/facets/{typeKey}`                             | Current         | auth / permit         | requireActor + stub scope       | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | PATCH  | `/api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}`                  | Current         | auth / permit         | requireActor + stub scope       | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | DELETE | `/api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}`                  | Current         | auth / permit         | requireActor + stub scope       | off: **Yes**†  | No      |
| [MetadataEntityController](#metadata-entity-controller)               | DELETE | `/api/v1/metadata/entities/{id}/facets/{typeKey}`                             | Current         | auth / permit         | requireActor + stub scope       | off: **Yes**†  | No      |
| [MetadataFacetController](#metadata-facet-controller)                 | GET    | `/api/v1/metadata/facets`                                                     | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataFacetController](#metadata-facet-controller)                 | GET    | `/api/v1/metadata/facets/{typeKey}`                                           | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataFacetController](#metadata-facet-controller)                 | POST   | `/api/v1/metadata/facets`                                                     | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataFacetController](#metadata-facet-controller)                 | PUT    | `/api/v1/metadata/facets/{typeKey}`                                           | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataFacetController](#metadata-facet-controller)                 | DELETE | `/api/v1/metadata/facets/{typeKey}`                                           | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataScopeController](#metadata-scope-controller)                 | GET    | `/api/v1/metadata/scopes`                                                     | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataScopeController](#metadata-scope-controller)                 | POST   | `/api/v1/metadata/scopes`                                                     | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataScopeController](#metadata-scope-controller)                 | DELETE | `/api/v1/metadata/scopes/{scopeSlug}`                                         | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [MetadataImportExportController](#metadata-import-export-controller)  | POST   | `/api/v1/metadata/import`                                                     | Current         | auth / permit         | client `?actor=`                | off: **Yes**   | No      |
| [MetadataImportExportController](#metadata-import-export-controller)  | GET    | `/api/v1/metadata/export`                                                     | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SavedQueriesRestController](#saved-queries-rest-controller)          | GET    | `/api/v1/analysis/queries`                                                    | Current         | auth / permit         | global catalog                  | off: **Yes**   | No      |
| [SavedQueriesRestController](#saved-queries-rest-controller)          | GET    | `/api/v1/analysis/queries/{queryId}`                                          | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SavedQueriesRestController](#saved-queries-rest-controller)          | POST   | `/api/v1/analysis/queries`                                                    | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SavedQueriesRestController](#saved-queries-rest-controller)          | PUT    | `/api/v1/analysis/queries/{queryId}`                                          | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [SavedQueriesRestController](#saved-queries-rest-controller)          | DELETE | `/api/v1/analysis/queries/{queryId}`                                          | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [AnalysisDialectRestController](#analysis-dialect-rest-controller)    | GET    | `/api/v1/analysis/dialect`                                                    | Current         | auth / permit         | none                            | off: **Yes**   | Yes     |
| [QueryResultRestController](#query-result-rest-controller)            | POST   | `/api/v1/query`                                                               | Current         | auth / permit         | requireCaller + SQL policies    | off: **Yes**†  | Partial |
| [QueryResultRestController](#query-result-rest-controller)            | GET    | `/api/v1/query`                                                               | Current         | auth / permit         | 405                             | off: **Yes**   | n/a     |
| [QueryResultRestController](#query-result-rest-controller)            | GET    | `/api/v1/query/{executionId}`                                                 | Current         | auth / permit         | requireCaller; 403 wrong tenant | off: **Yes**†  | Partial |
| [QueryResultRestController](#query-result-rest-controller)            | DELETE | `/api/v1/query/{executionId}`                                                 | Current         | auth / permit         | requireCaller                   | off: **Yes**†  | Partial |
| [AiChatController](#ai-chat-controller)                               | GET    | `/api/v1/ai/chats`                                                            | Current         | auth / permit         | UserIdResolver list             | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | POST   | `/api/v1/ai/chats`                                                            | Current         | auth / permit         | UserIdResolver create           | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | GET    | `/api/v1/ai/chats/{chatId}`                                                   | Current         | auth / permit         | no ownership                    | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | PATCH  | `/api/v1/ai/chats/{chatId}`                                                   | Current         | auth / permit         | no ownership                    | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | DELETE | `/api/v1/ai/chats/{chatId}`                                                   | Current         | auth / permit         | no ownership                    | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | GET    | `/api/v1/ai/chats/{chatId}/messages`                                          | Current         | auth / permit         | no ownership                    | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | POST   | `/api/v1/ai/chats/{chatId}/messages`                                          | Current         | auth / permit         | SSE; no ownership               | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | POST   | `/api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result`                   | Current         | auth / permit         | no ownership                    | off: **Yes**   | No      |
| [AiChatController](#ai-chat-controller)                               | GET    | `/api/v1/ai/chats/context-types/{contextType}/contexts/{contextId}`           | Current         | auth / permit         | UserIdResolver scope            | off: **Yes**   | Partial |
| [AiProfileController](#ai-profile-controller)                         | GET    | `/api/v1/ai/profiles`                                                         | Current         | auth / permit         | none                            | off: **Yes**   | Yes     |
| [AiProfileController](#ai-profile-controller)                         | GET    | `/api/v1/ai/profiles/{profileId}`                                             | Current         | auth / permit         | none                            | off: **Yes**   | Yes     |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/ListSchemas`                                                   | Current         | auth / permit         | data plane                      | off: **Yes**   | Partial |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/Handshake`                                                     | Current         | auth / permit         | data plane                      | off: **Yes**   | Partial |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/GetSchema`                                                     | Current         | auth / permit         | data plane                      | off: **Yes**   | Partial |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/GetDialect`                                                    | Current         | auth / permit         | data plane                      | off: **Yes**   | Partial |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/ParseSql`                                                      | Current         | auth / permit         | data plane                      | off: **Yes**   | Partial |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/SubmitQuery`                                                   | Current         | auth / permit         | SQL policies                    | off: **Yes**   | Partial |
| [AccessServiceController](#access-service-controller)                 | POST   | `/services/jet/FetchQueryResult`                                              | Current         | auth / permit         | data plane                      | off: **Yes**   | Partial |
| [ExportRestController](#export-rest-controller)                       | GET    | `/services/export/formats`                                                    | Current         | auth / permit         | none                            | off: **Yes**   | Yes     |
| [ExportRestController](#export-rest-controller)                       | GET    | `/services/export/catalog`                                                    | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [ExportRestController](#export-rest-controller)                       | GET    | `/services/export/schemas`                                                    | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [ExportRestController](#export-rest-controller)                       | GET    | `/services/export/schemas/{schema}`                                           | Current         | auth / permit         | none                            | off: **Yes**   | No      |
| [ExportRestController](#export-rest-controller)                       | GET    | `/services/export/schemas/{schema}/tables/{table}`                            | Current         | auth / permit         | streams data                    | off: **Yes**   | No      |
| [ExportRestController](#export-rest-controller)                       | POST   | `/services/export/sql`                                                        | Current         | auth / permit         | SQL export                      | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | GET    | `/api/nl2sql/chats`                                                           | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | POST   | `/api/nl2sql/chats`                                                           | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | GET    | `/api/nl2sql/chats/{chatId}`                                                  | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | PATCH  | `/api/nl2sql/chats/{chatId}`                                                  | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | DELETE | `/api/nl2sql/chats/{chatId}`                                                  | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | GET    | `/api/nl2sql/chats/{chatId}/messages`                                         | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | POST   | `/api/nl2sql/chats/{chatId}/messages`                                         | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [NlSqlChatController](#nlsql-chat-controller)                         | GET    | `/api/nl2sql/chats/{chatId}/stream`                                           | **Legacy (v1)** | auth / permit         | none                            | off: **Yes**   | No      |
| [Retired route](#retired-routes)                                      | POST   | `/api/v1/queries/execute`                                                     | **Retired**     | —                     | —                               | —              | —       |
| [TestController](#test-controller)                                    | GET    | `/test-security/auth-info`                                                    | **Test-only**   | auth when wired       | none                            | —              | —       |

**Abbreviations:** `auth` = authenticated; `permit` = permitAll; `requireActor` = `requireAuthenticatedActor()`; `requireCaller` = `requireCallerContext()`; **Suff?** = sufficient for production (Cond = conditional); **M** = HTTP method.

† Filter permits anonymous when security off, but **app layer returns 401** (metadata writes, query sessions).

**Row counts:** 89 rows (76 current production‡ + 4 deprecated + 8 legacy v1 + 1 retired + 1 test-only; platform/OAuth rows included). ‡AI v3 and some service modules require profile/flags (`mill.ai.enabled`, `mill.data.services.*.enable`).

---

<h2 id="endpoint-lifecycle-legacy--deprecated">Endpoint lifecycle (legacy & deprecated)</h2>

Consolidated register of non-**Current** routes and related deprecations. Harmonization story should treat **Deprecated** / **Legacy** / **Retired** rows as removal or migration candidates, not greenfield auth design targets.

### Deprecated REST paths (code-marked)

Source: `SchemaExplorerController.kt` — `@Deprecated("…This path will be removed.")` + OpenAPI `deprecated = true`.

| Status | Method | Path | Replacement | Notes |
|--------|--------|------|-------------|-------|
| **Deprecated** | GET | `/api/v1/schema/schemas` | `GET /api/v1/schema` | Same handler as canonical list |
| **Deprecated** | GET | `/api/v1/schema/schemas/{schemaName}` | `GET /api/v1/schema/{schemaName}` | |
| **Deprecated** | GET | `/api/v1/schema/schemas/{schemaName}/tables/{tableName}` | `GET /api/v1/schema/{schemaName}/tables/{tableName}` | |
| **Deprecated** | GET | `/api/v1/schema/schemas/{schemaName}/tables/{tableName}/columns/{columnName}` | `GET /api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName}` | |

### Deprecated query parameters (not separate routes)

| Status | Parameter | Where | Replacement |
|--------|-----------|-------|-------------|
| **Deprecated** | `context` | All schema explorer GETs that accept scope | `scope` (used when `scope` absent — migration alias) |

<h3 id="legacy-ai-v1-chat">Legacy AI v1 chat (ai/legacy/mill-ai-v1-nlsql-chat-service)</h3>

Module: **`@ConditionalOnService("ai-nl2data")`** — not enabled in default `application.yml`. **mill-ui** migrated to v3 ([`docs/workitems/BACKLOG.md`](../../workitems/BACKLOG.md) **U-11**). See [`docs/design/ai/ai-v1-integration/README.md`](../ai/ai-v1-integration/README.md).

| Status | Method | Path | v3 replacement |
|--------|--------|------|----------------|
| **Legacy (v1)** | GET | `/api/nl2sql/chats` | `GET /api/v1/ai/chats` |
| **Legacy (v1)** | POST | `/api/nl2sql/chats` | `POST /api/v1/ai/chats` |
| **Legacy (v1)** | GET | `/api/nl2sql/chats/{chatId}` | `GET /api/v1/ai/chats/{chatId}` |
| **Legacy (v1)** | PATCH | `/api/nl2sql/chats/{chatId}` | `PATCH /api/v1/ai/chats/{chatId}` |
| **Legacy (v1)** | DELETE | `/api/nl2sql/chats/{chatId}` | `DELETE /api/v1/ai/chats/{chatId}` |
| **Legacy (v1)** | GET | `/api/nl2sql/chats/{chatId}/messages` | `GET /api/v1/ai/chats/{chatId}/messages` |
| **Legacy (v1)** | POST | `/api/nl2sql/chats/{chatId}/messages` | `POST /api/v1/ai/chats/{chatId}/messages` (SSE) |
| **Legacy (v1)** | GET | `/api/nl2sql/chats/{chatId}/stream` | v3 uses **POST** `/messages` for SSE (no separate stream GET) |

Auth when wired: same `/api/**` filter rules as other API routes. **Do not extend** v1 auth; migrate callers to v3.

<h3 id="retired-routes">Retired routes (no controller)</h3>

| Status | Method | Path | Replacement | Source |
|--------|--------|------|-------------|--------|
| **Retired** | POST | `/api/v1/queries/execute` | Session API `POST /api/v1/query` (+ paging on `GET /api/v1/query/{executionId}`) | [`docs/design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md`](../ui/mill-ui/BACKEND-API-REQUIREMENTS.md) |

### Dev-only / test-only surfaces

| Status | Method | Path | Notes |
|--------|--------|------|-------|
| **Dev-only** | GET | `/actuator/beans/**` | Explicit `permitAll`; `application.yml` warns *avoid in production* |
| **Dev-only** | GET | `/actuator/valuemap/**` | Wiring snapshot; `permitAll` always |
| **Test-only** | GET | `/test-security/auth-info` | `core/mill-test-kit`; own filter chain in tests |

### Related non-REST deprecations (auth story context)

| Item | Status | Notes |
|------|--------|-------|
| `mill.security.enable: false` default | **Legacy config** | Comment in `application.yml`: *LEGACY subject to review* |
| Static OAuth page `/id/login.html` | **Legacy UI** | Replaced by `/app/login` SPA shell (`OAuth2ResourceServiceAuthenticationMethod` javadoc) |
| `services/mill-grinder-ui` | **Retired module** | No new REST work; UI is `ui/mill-ui` |

---

## Discovery & platform

<h3 id="application-descriptor-controller">ApplicationDescriptorController — /.well-known</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/.well-known/mill` (+ trailing variants) | permitAll / permitAll | none | **Yes** | **Yes** — public discovery document |

<h3 id="springdoc-swagger">SpringDoc / Swagger</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/v3/api-docs/**` | authenticated / permitAll | none | off: No; on: needs auth | **No** when security off — full API surface exposed |
| GET | `/swagger-ui.html`, `/swagger-ui/**` | authenticated / permitAll | none | same | same |

<h3 id="actuator">Actuator (Spring Boot)</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/actuator/health/**` | *(unmatched by Mill chains)* | none | Likely **Yes** in default local config | **Review** — OK for probes if network-restricted |
| GET | `/actuator/valuemap/**` | permitAll / permitAll | none | **Yes** | **No** — **Dev-only** wiring snapshot without auth |
| GET | `/actuator/beans/**` | permitAll / permitAll | none | **Yes** | **No** — **Dev-only**; full bean graph |

Source: `apps/mill-service/.../MillServiceActuatorInspectSecurityConfiguration.java`, `application.yml` (`management.endpoints.web.exposure.include`).

---

## Authentication (`security/mill-security-auth-service`)

<h3 id="auth-public-controller">AuthPublicController — /auth/public</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| POST | `/auth/public/login` | permitAll / permitAll | validates credentials when enabled; returns synthetic anonymous user when disabled | **Yes** | **Yes** for login; rate-limit / lockout out of scope here |
| POST | `/auth/public/register` | permitAll / permitAll | gated by `mill.security.allow-registration` (403 when false) | **Yes** (attempt) | **Conditional** — OK when registration disabled |

<h3 id="auth-controller">AuthController — /auth</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/auth/me` | permitAll / permitAll | returns **401** if no session when enabled | **Yes** (401 body) | **Yes** |
| PATCH | `/auth/profile` | authenticated / permitAll | requires authenticated principal | off: **Yes**; on: No | **No** when security off |
| POST | `/auth/logout` | permitAll / permitAll | graceful without session | **Yes** | **Yes** |

<h3 id="oauth2-infrastructure">OAuth2 / session infrastructure (no Mill @RestController)</h3>

| Path prefix | Filter | Anonymous OK | Sufficient? |
|-------------|--------|--------------|-------------|
| `/oauth2/**`, `/login/**`, `/logout/**`, `/id/**` | permitAll when security on | **Yes** | **Yes** — required for browser login |

---

<h2 id="schema-explorer-controller">Schema explorer (data/mill-data-schema-service)</h2>

Base: `/api/v1/schema` — `SchemaExplorerController`. All GETs accept optional **`scope`**; **`context`** query param is **Deprecated** (alias when `scope` absent).

| Status | Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|--------|------|-------------------|----------|--------------|-------------|
| Current | GET | `/api/v1/schema/context` | authenticated / permitAll | none | off: **Yes** | **No** — exposes catalog context |
| Current | GET | `/api/v1/schema` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Deprecated** | GET | `/api/v1/schema/schemas` | authenticated / permitAll | none | off: **Yes** | **No** — use `/api/v1/schema` |
| Current | GET | `/api/v1/schema/tree` | authenticated / permitAll | none | off: **Yes** | **No** |
| Current | GET | `/api/v1/schema/model` | authenticated / permitAll | none | off: **Yes** | **No** |
| Current | GET | `/api/v1/schema/{schemaName}` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Deprecated** | GET | `/api/v1/schema/schemas/{schemaName}` | authenticated / permitAll | none | off: **Yes** | **No** |
| Current | GET | `/api/v1/schema/{schemaName}/tables/{tableName}` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Deprecated** | GET | `/api/v1/schema/schemas/{schemaName}/tables/{tableName}` | authenticated / permitAll | none | off: **Yes** | **No** |
| Current | GET | `/api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName}` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Deprecated** | GET | `/api/v1/schema/schemas/{schemaName}/tables/{tableName}/columns/{columnName}` | authenticated / permitAll | none | off: **Yes** | **No** |

**Note:** Read-only catalog; data policies do not apply at this layer. Any authenticated user sees full physical schema.

---

## Metadata (`metadata/mill-metadata-service`)

<h3 id="metadata-entity-controller">MetadataEntityController — /api/v1/metadata/entities</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/metadata/entities` | authenticated / permitAll | none | off: **Yes** | **No** — full entity list |
| GET | `/api/v1/metadata/entities/{id}` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/api/v1/metadata/entities/{id}/facets/merge-trace` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/api/v1/metadata/entities/{id}/facets` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/api/v1/metadata/entities/{id}/facets/{typeKey}` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/api/v1/metadata/entities/{id}/history` | authenticated / permitAll | none | off: **Yes** | **No** |
| POST | `/api/v1/metadata/entities` | authenticated / permitAll | **requireAuthenticatedActor** | off: **Yes** | **No** — any authenticated user |
| PUT | `/api/v1/metadata/entities/{id}` | authenticated / permitAll | **requireAuthenticatedActor** | off: **Yes** | **No** |
| PATCH | `/api/v1/metadata/entities/{id}` | authenticated / permitAll | **requireAuthenticatedActor** | off: **Yes** | **No** |
| DELETE | `/api/v1/metadata/entities/{id}` | authenticated / permitAll | **requireAuthenticatedActor** | off: **Yes** | **No** |
| POST | `/api/v1/metadata/entities/{id}/facets/{typeKey}` | authenticated / permitAll | auth + **stub scope check** | off: **Yes** | **No** |
| PATCH | `/api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` | authenticated / permitAll | auth + **stub scope check** | off: **Yes** | **No** |
| DELETE | `/api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` | authenticated / permitAll | auth + **stub scope check** | off: **Yes** | **No** |
| DELETE | `/api/v1/metadata/entities/{id}/facets/{typeKey}` | authenticated / permitAll | auth + **stub scope check** | off: **Yes** | **No** |

<h3 id="metadata-facet-controller">MetadataFacetController — /api/v1/metadata/facets</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/metadata/facets` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/api/v1/metadata/facets/{typeKey}` | authenticated / permitAll | none | off: **Yes** | **No** |
| POST | `/api/v1/metadata/facets` | authenticated / permitAll | **none** | off: **Yes** | **No** — catalog mutation |
| PUT | `/api/v1/metadata/facets/{typeKey}` | authenticated / permitAll | **none** | off: **Yes** | **No** |
| DELETE | `/api/v1/metadata/facets/{typeKey}` | authenticated / permitAll | **none** | off: **Yes** | **No** |

<h3 id="metadata-scope-controller">MetadataScopeController — /api/v1/metadata/scopes</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/metadata/scopes` | authenticated / permitAll | none | off: **Yes** | **No** |
| POST | `/api/v1/metadata/scopes` | authenticated / permitAll | **none** | off: **Yes** | **No** |
| DELETE | `/api/v1/metadata/scopes/{scopeSlug}` | authenticated / permitAll | **none** | off: **Yes** | **No** |

<h3 id="metadata-import-export-controller">MetadataImportExportController — /api/v1/metadata</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| POST | `/api/v1/metadata/import` | authenticated / permitAll | **none**; `actor` query param client-supplied | off: **Yes** | **No** — bulk REPLACE/MERGE |
| GET | `/api/v1/metadata/export` | authenticated / permitAll | none | off: **Yes** | **No** — full YAML export |

---

## Analysis / saved queries (`services/mill-analysis-service`)

<h3 id="saved-queries-rest-controller">SavedQueriesRestController — /api/v1/analysis/queries</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/analysis/queries` | authenticated / permitAll | none; **global catalog** | off: **Yes** | **No** — no per-user isolation |
| GET | `/api/v1/analysis/queries/{queryId}` | authenticated / permitAll | none | off: **Yes** | **No** |
| POST | `/api/v1/analysis/queries` | authenticated / permitAll | none | off: **Yes** | **No** |
| PUT | `/api/v1/analysis/queries/{queryId}` | authenticated / permitAll | none | off: **Yes** | **No** |
| DELETE | `/api/v1/analysis/queries/{queryId}` | authenticated / permitAll | none | off: **Yes** | **No** |

<h3 id="analysis-dialect-rest-controller">AnalysisDialectRestController — /api/v1/analysis/dialect</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/analysis/dialect` | authenticated / permitAll | none | off: **Yes** | **Yes** — non-sensitive config |

---

## Query result sessions (`services/mill-data-query-service`)

<h3 id="query-result-rest-controller">QueryResultRestController — /api/v1/query</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| POST | `/api/v1/query` | authenticated / permitAll | **requireCallerContext**; SQL via data plane | off: **Yes** | **Partial** — tenant isolation on sessions; policies at SQL layer |
| GET | `/api/v1/query` | authenticated / permitAll | returns 405 | off: **Yes** | n/a |
| GET | `/api/v1/query/{executionId}` | authenticated / permitAll | **requireCallerContext**; **403** wrong tenant | off: **Yes** | **Partial** |
| DELETE | `/api/v1/query/{executionId}` | authenticated / permitAll | **requireCallerContext** | off: **Yes** | **Partial** |

---

## AI v3 chat (`ai/mill-ai-service`, profile `mill.ai.enabled=true`)

<h3 id="ai-chat-controller">AiChatController — /api/v1/ai/chats</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/ai/chats` | authenticated / permitAll | filters by **UserIdResolver** | off: **Yes** | **No** — resolver often static |
| POST | `/api/v1/ai/chats` | authenticated / permitAll | assigns **UserIdResolver** user | off: **Yes** | **No** |
| GET | `/api/v1/ai/chats/{chatId}` | authenticated / permitAll | **no ownership check** | off: **Yes** | **No** — IDOR |
| PATCH | `/api/v1/ai/chats/{chatId}` | authenticated / permitAll | **no ownership check** | off: **Yes** | **No** |
| DELETE | `/api/v1/ai/chats/{chatId}` | authenticated / permitAll | **no ownership check** | off: **Yes** | **No** |
| GET | `/api/v1/ai/chats/{chatId}/messages` | authenticated / permitAll | **no ownership check** | off: **Yes** | **No** |
| POST | `/api/v1/ai/chats/{chatId}/messages` (SSE) | authenticated / permitAll | **no ownership check** | off: **Yes** | **No** |
| POST | `/api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result` | authenticated / permitAll | **no ownership check** | off: **Yes** | **No** |
| GET | `/api/v1/ai/chats/context-types/{contextType}/contexts/{contextId}` | authenticated / permitAll | scoped to resolver user | off: **Yes** | **Partial** |

<h3 id="ai-profile-controller">AiProfileController — /api/v1/ai/profiles</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/api/v1/ai/profiles` | authenticated / permitAll | none | off: **Yes** | **Yes** — discovery |
| GET | `/api/v1/ai/profiles/{profileId}` | authenticated / permitAll | none | off: **Yes** | **Yes** |

---

## Data plane HTTP (`services/mill-data-http-service`)

<h3 id="access-service-controller">AccessServiceController — /services/jet</h3>

Protobuf/JSON RPC-style POST endpoints:

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| POST | `/services/jet/ListSchemas` | authenticated / permitAll | data plane + **policies on query paths** | off: **Yes** | **Partial** — auth only at edge |
| POST | `/services/jet/Handshake` | authenticated / permitAll | same | off: **Yes** | **Partial** |
| POST | `/services/jet/GetSchema` | authenticated / permitAll | same | off: **Yes** | **Partial** |
| POST | `/services/jet/GetDialect` | authenticated / permitAll | same | off: **Yes** | **Partial** |
| POST | `/services/jet/ParseSql` | authenticated / permitAll | same | off: **Yes** | **Partial** |
| POST | `/services/jet/SubmitQuery` | authenticated / permitAll | **policy enforcement** on execution | off: **Yes** | **Partial** |
| POST | `/services/jet/FetchQueryResult` | authenticated / permitAll | same | off: **Yes** | **Partial** |

---

## Export (`services/mill-export-service`)

<h3 id="export-rest-controller">ExportRestController — /services/export</h3>

| Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|------|-------------------|----------|--------------|-------------|
| GET | `/services/export/formats` | authenticated / permitAll | none | off: **Yes** | **Yes** |
| GET | `/services/export/catalog` | authenticated / permitAll | none | off: **Yes** | **No** — schema + download URLs |
| GET | `/services/export/schemas` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/services/export/schemas/{schema}` | authenticated / permitAll | none | off: **Yes** | **No** |
| GET | `/services/export/schemas/{schema}/tables/{table}` | authenticated / permitAll | streams table data | off: **Yes** | **No** — unauthenticated export |
| POST | `/services/export/sql` | authenticated / permitAll | executes SQL export | off: **Yes** | **No** |

---

<h2 id="legacy-non-runtime">Legacy / non-runtime (reference only)</h2>

<h3 id="nlsql-chat-controller">NlSqlChatController — Legacy AI v1</h3>

Full route list: [Legacy AI v1 chat](#legacy-ai-v1-chat) above. Per-endpoint auth when the legacy module is enabled:

| Status | Method | Path | Filter (on / off) | App auth | Anonymous OK | Sufficient? |
|--------|--------|------|-------------------|----------|--------------|-------------|
| **Legacy (v1)** | GET | `/api/nl2sql/chats` | authenticated / permitAll | none | off: **Yes** | **No** — superseded by v3 |
| **Legacy (v1)** | POST | `/api/nl2sql/chats` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Legacy (v1)** | GET | `/api/nl2sql/chats/{chatId}` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Legacy (v1)** | PATCH | `/api/nl2sql/chats/{chatId}` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Legacy (v1)** | DELETE | `/api/nl2sql/chats/{chatId}` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Legacy (v1)** | GET | `/api/nl2sql/chats/{chatId}/messages` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Legacy (v1)** | POST | `/api/nl2sql/chats/{chatId}/messages` | authenticated / permitAll | none | off: **Yes** | **No** |
| **Legacy (v1)** | GET | `/api/nl2sql/chats/{chatId}/stream` | authenticated / permitAll | none | off: **Yes** | **No** — v3 SSE via POST `/messages` |

<h3 id="test-controller">TestController — core/mill-test-kit</h3>

| Status | Method | Path | Filter (on / off) | Notes |
|--------|--------|------|-------------------|-------|
| **Test-only** | GET | `/test-security/auth-info` | authenticated when test chain wired | Not in production `mill-service` |

---

## Cross-cutting inconsistencies

| Topic | Observation |
|-------|-------------|
| **Two authorization layers** | Transport (`authenticated`) vs application (sparse, ad hoc) vs data policies (query only) — not documented per endpoint |
| **Read vs write** | Metadata reads anonymous when security off; writes on entities require auth in-controller but sibling controllers do not |
| **Ownership** | Query sessions enforce tenant; AI chats and saved queries do not |
| **Role model unused on REST** | `GrantedAuthoritiesPolicySelector` feeds SQL policies, not REST |
| **Default posture** | `mill.security.enable: false` marked `## LEGACY subject to review` in `application.yml` |
| **OpenAPI vs code** | Prior inventory: 58 runtime operations; analysis/query/export endpoints may appear when modules enabled — re-verify with `/v3/api-docs` on target deployment |
| **CORS** | `AiChatController` allows `localhost:5173` and `8080` — independent of security enable flag |

---

## Suggested story themes (harmonization backlog)

1. **Define target security posture** — security on in non-local profiles; document exceptions.
2. **REST authorization contract** — roles/scopes (e.g. `metadata:read`, `metadata:admin`, `data:query`, `ai:chat:own`); map to filter chains + `@PreAuthorize` or a shared `AuthorizationService`.
3. **Resource ownership** — AI chats, saved queries, query sessions (already partial), metadata scopes (`ownerId`).
4. **Implement `requireScopeWriteAllowed`** — wire to scope ownership / RBAC.
5. **Remove client-supplied `actor`** — derive from `SecurityContext` only.
6. **Integrate `UserIdResolver` with security principal** — replace `PropertiesUserIdResolver` in secured deployments.
7. **Actuator & Swagger policy** — authenticate or disable sensitive endpoints in production.
8. **Endpoint inventory CI** — codegen or test that fails when controllers drift from this doc.
9. **Deprecation cleanup** — remove schema `/schemas/…` aliases and decommission `/api/nl2sql/**` after caller migration; drop retired `/api/v1/queries/execute` from any external docs.

---

## Key source files

| Area | Path |
|------|------|
| Security master | `security/mill-security-autoconfigure/.../SecurityConfig.java` |
| API / services / app chains | `security/mill-security-autoconfigure/.../ApiSecurityConfiguration.java`, `ServicesSecurityConfiguration.java`, `AppSecurityConfiguration.java` |
| Auth endpoints | `security/mill-security-auth-service/.../Auth*SecurityConfiguration.kt`, `Auth*Controller.kt` |
| Actuator override | `apps/mill-service/.../MillServiceActuatorInspectSecurityConfiguration.java` |
| Metadata writes | `metadata/mill-metadata-service/.../MetadataEntityController.kt` |
| Query tenant | `services/mill-data-query-service/.../QueryCallerSupport.kt` |
| AI ownership gap | `ai/mill-ai-service/.../UnifiedChatService.kt`, `ai/mill-ai/.../UserIdResolver.kt` |
| Data policies | `data/mill-data-autoconfigure/.../DefaultFilterChainConfiguration.java` |

---

## Maintenance

Re-scan when adding `@RestController` classes or `SecurityFilterChain` beans:

```bash
rg '@RestController|SecurityFilterChain|permitAll|authenticated' --glob '*.{java,kt}' security services metadata data ai apps
```

Cross-check runtime OpenAPI: `GET /v3/api-docs` on a fully configured `mill-service` instance.
