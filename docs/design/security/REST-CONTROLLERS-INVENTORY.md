# REST Controllers & Operations Inventory

This document is a **code-first inventory** of REST controllers and their operations.

**Authorization (filter chains, anonymous access, sufficiency assessment):** see **[`rest-api-authorization-inventory.md`](rest-api-authorization-inventory.md)** — use that document for the harmonization story.

- **Source of truth**: controller annotations in code (cross-checked against runtime OpenAPI)
- **Runtime OpenAPI cross-check**: `http://localhost:8080/v3/api-docs`
- **Snapshot artifact** (downloaded during inventory): `_openapi-api-docs.json` (same folder)

## Summary

- **Controllers found in code (prod)**: 17 (see auth inventory for full list)
- **Controllers found in OpenAPI (runtime)**: varies by enabled modules (AI, query, export, analysis)
- **OpenAPI operations (runtime)**: re-verify on target deployment

## Inventory (from code)

### `services/mill-service-common`

#### `io.qpointz.mill.service.controllers.ApplicationDescriptorController`

- **Base path**: `/.well-known`
- **Operations**
  - **GET** `/.well-known/mill` — `getInfo()`

### `services/mill-data-http-service`

#### `io.qpointz.mill.data.backend.access.http.controllers.AccessServiceController`

- **Base path**: `/services/jet`
- **Consumes / produces** (class-level): `application/json`, `application/protobuf`
- **Operations**
  - **POST** `/services/jet/ListSchemas` — `listSchemas(...)`
  - **POST** `/services/jet/Handshake` — `handshake(...)`
  - **POST** `/services/jet/GetSchema` — `getSchema(...)`
  - **POST** `/services/jet/GetDialect` — `getDialect(...)`
  - **POST** `/services/jet/ParseSql` — `parseSql(...)`
  - **POST** `/services/jet/SubmitQuery` — `submitQuery(...)`
  - **POST** `/services/jet/FetchQueryResult` — `fetchQueryResult(...)`

### `data/mill-data-schema-service`

#### `io.qpointz.mill.data.schema.api.SchemaExplorerController`

- **Base path**: `/api/v1/schema`
- **Deprecated query param**: `context` — use `scope` instead (accepted when `scope` absent)
- **Operations**
  - **GET** `/api/v1/schema/context` — `getContext()`
  - **GET** `/api/v1/schema` — `listSchemas(...)`
  - **GET** `/api/v1/schema/tree` — `getTree(...)`
  - **GET** `/api/v1/schema/model` — `getModelRoot(...)`
  - **GET** `/api/v1/schema/{schemaName}` — `getSchema(...)`
  - **GET** `/api/v1/schema/{schemaName}/tables/{tableName}` — `getTable(...)`
  - **GET** `/api/v1/schema/{schemaName}/tables/{tableName}/columns/{columnName}` — `getColumn(...)`
  - **GET** ~~`/api/v1/schema/schemas`~~ — `listSchemasLegacy(...)` — **DEPRECATED** → `/api/v1/schema`
  - **GET** ~~`/api/v1/schema/schemas/{schemaName}`~~ — `getSchemaLegacy(...)` — **DEPRECATED**
  - **GET** ~~`/api/v1/schema/schemas/{schemaName}/tables/{tableName}`~~ — `getTableLegacy(...)` — **DEPRECATED**
  - **GET** ~~`/api/v1/schema/schemas/.../columns/{columnName}`~~ — `getColumnLegacy(...)` — **DEPRECATED**

### `metadata/mill-metadata-service`

#### `io.qpointz.mill.metadata.api.MetadataEntityController`

- **Base path**: `/api/v1/metadata/entities`
- **Operations**
  - **GET** `/api/v1/metadata/entities` — `listEntities(...)`
  - **GET** `/api/v1/metadata/entities/{id}` — `getEntityById(...)`
  - **GET** `/api/v1/metadata/entities/{id}/facets/merge-trace` — `getFacetMergeTrace(...)`
  - **GET** `/api/v1/metadata/entities/{id}/facets` — `getEntityFacets(...)`
  - **GET** `/api/v1/metadata/entities/{id}/facets/{typeKey}` — `getEntityFacetByType(...)`
  - **POST** `/api/v1/metadata/entities` — `createEntity(...)`
  - **PUT** `/api/v1/metadata/entities/{id}` — `overwriteEntity(...)`
  - **PATCH** `/api/v1/metadata/entities/{id}` — `patchEntityOverwrite(...)`
  - **DELETE** `/api/v1/metadata/entities/{id}` — `deleteEntity(...)`
  - **POST** `/api/v1/metadata/entities/{id}/facets/{typeKey}` — `assignFacet(...)`
  - **PATCH** `/api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` — `patchFacetPayload(...)`
  - **DELETE** `/api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` — `deleteFacetByUid(...)`
  - **DELETE** `/api/v1/metadata/entities/{id}/facets/{typeKey}` — `deleteFacetsAtScope(...)` (query param: `scope=...`)
  - **GET** `/api/v1/metadata/entities/{id}/history` — `getEntityHistory(...)`

#### `io.qpointz.mill.metadata.api.MetadataFacetController`

- **Base path**: `/api/v1/metadata/facets`
- **Operations**
  - **GET** `/api/v1/metadata/facets` — `listFacetTypes(...)`
  - **GET** `/api/v1/metadata/facets/{typeKey}` — `getFacetTypeByKey(...)`
  - **POST** `/api/v1/metadata/facets` — `registerFacetType(...)`
  - **PUT** `/api/v1/metadata/facets/{typeKey}` — `updateFacetType(...)`
  - **DELETE** `/api/v1/metadata/facets/{typeKey}` — `deleteFacetType(...)`

#### `io.qpointz.mill.metadata.api.MetadataImportExportController`

- **Base path**: `/api/v1/metadata`
- **Operations**
  - **POST** `/api/v1/metadata/import` — `importMetadata(...)` (consumes `multipart/form-data`)
  - **GET** `/api/v1/metadata/export` — `exportMetadata(...)` (produces `text/yaml`)

#### `io.qpointz.mill.metadata.api.MetadataScopeController`

- **Base path**: `/api/v1/metadata/scopes`
- **Operations**
  - **GET** `/api/v1/metadata/scopes` — `listScopes()`
  - **POST** `/api/v1/metadata/scopes` — `createScope(...)`
  - **DELETE** `/api/v1/metadata/scopes/{scopeSlug}` — `deleteScope(...)`

### `services/mill-analysis-service`

#### `io.qpointz.mill.analysis.queries.web.SavedQueriesRestController`

- **Base path**: `/api/v1/analysis/queries`
- **Operations**
  - **GET** `/api/v1/analysis/queries` — `list()`
  - **GET** `/api/v1/analysis/queries/{queryId}` — `getById(...)`
  - **POST** `/api/v1/analysis/queries` — `create(...)`
  - **PUT** `/api/v1/analysis/queries/{queryId}` — `update(...)`
  - **DELETE** `/api/v1/analysis/queries/{queryId}` — `delete(...)`

#### `io.qpointz.mill.analysis.queries.web.AnalysisDialectRestController`

- **Base path**: `/api/v1/analysis/dialect`
- **Operations**
  - **GET** `/api/v1/analysis/dialect` — `getDialect()`

### `services/mill-data-query-service`

#### `io.qpointz.mill.data.query.web.QueryResultRestController`

- **Base path**: `/api/v1/query`
- **Operations**
  - **POST** `/api/v1/query` — `create(...)`
  - **GET** `/api/v1/query` — `listNotSupported()` (405)
  - **GET** `/api/v1/query/{executionId}` — `getSession(...)` (metadata or paged rows via query params)
  - **DELETE** `/api/v1/query/{executionId}` — `delete(...)`

### `services/mill-export-service`

#### `io.qpointz.mill.export.ExportRestController`

- **Base path**: `/services/export`
- **Operations**
  - **GET** `/services/export/formats` — `formats()`
  - **GET** `/services/export/catalog` — `catalog(...)`
  - **GET** `/services/export/schemas` — `listSchemas(...)`
  - **GET** `/services/export/schemas/{schema}` — `schemaDetail(...)`
  - **GET** `/services/export/schemas/{schema}/tables/{table}` — `exportTable(...)`
  - **POST** `/services/export/sql` — `exportSql(...)` (consumes `text/plain`)

### `ai/mill-ai-service`

#### `io.qpointz.mill.ai.service.AiChatController`

- **Base path**: `/api/v1/ai/chats`
- **Produces** (class-level): `application/json`
- **Operations**
  - **GET** `/api/v1/ai/chats` — `listChats()`
  - **POST** `/api/v1/ai/chats` — `createChat(...)`
  - **GET** `/api/v1/ai/chats/{chatId}` — `getChat(...)`
  - **PATCH** `/api/v1/ai/chats/{chatId}` — `updateChat(...)`
  - **DELETE** `/api/v1/ai/chats/{chatId}` — `deleteChat(...)`
  - **GET** `/api/v1/ai/chats/{chatId}/messages` — `listMessages(...)`
  - **POST** `/api/v1/ai/chats/{chatId}/messages` — `sendMessage(...)` (produces `text/event-stream`)
  - **POST** `/api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result` — `attachExecutionResult(...)`
  - **GET** `/api/v1/ai/chats/context-types/{contextType}/contexts/{contextId}` — `getChatByContext(...)`

#### `io.qpointz.mill.ai.service.AiProfileController`

- **Base path**: `/api/v1/ai/profiles`
- **Produces** (class-level): `application/json`
- **Operations**
  - **GET** `/api/v1/ai/profiles` — `listProfiles()`
  - **GET** `/api/v1/ai/profiles/{profileId}` — `getProfile(...)`

### `security/mill-security-auth-service`

#### `io.qpointz.mill.security.auth.controllers.AuthPublicController`

- **Base path**: `/auth/public`
- **Operations**
  - **POST** `/auth/public/login` — `login(...)`
  - **POST** `/auth/public/register` — `register(...)`

#### `io.qpointz.mill.security.auth.controllers.AuthController`

- **Base path**: `/auth`
- **Operations**
  - **GET** `/auth/me` — `getMe(...)`
  - **PATCH** `/auth/profile` — `updateProfile(...)`
  - **POST** `/auth/logout` — `logout(...)`

### `ai/legacy/mill-ai-v1-nlsql-chat-service` (**Legacy v1** — not on default classpath)

Superseded by **`/api/v1/ai/chats/**`** (`mill-ai-service`). See [`rest-api-authorization-inventory.md`](rest-api-authorization-inventory.md#legacy-ai-v1-chat-ailegacymill-ai-v1-nlsql-chat-service).

#### `io.qpointz.mill.ai.nlsql.controllers.NlSqlChatController`

- **Base path**: `/api/nl2sql`
- **Enable gate**: `@ConditionalOnService("ai-nl2data")`
- **Consumes / produces** (class-level): consumes `application/json`, produces `application/json`
- **Operations**
  - **GET** `/api/nl2sql/chats` — `listChats()`
  - **POST** `/api/nl2sql/chats` — `createChat(...)`
  - **GET** `/api/nl2sql/chats/{chatId}` — `getChat(...)`
  - **PATCH** `/api/nl2sql/chats/{chatId}` — `updateChat(...)`
  - **DELETE** `/api/nl2sql/chats/{chatId}` — `deleteChat(...)`
  - **GET** `/api/nl2sql/chats/{chatId}/messages` — `listChatMessages(...)`
  - **POST** `/api/nl2sql/chats/{chatId}/messages` — `postChatMessages(...)`
  - **GET** `/api/nl2sql/chats/{chatId}/stream` — `chatStream(...)` (produces `text/event-stream`)

### `core/mill-test-kit` (non-prod / test utility)

#### `io.qpointz.mill.test.services.TestController`

- **Base path**: `/test-security/`
- **Operations**
  - **GET** `/test-security/auth-info` — `authInfo()`

## OpenAPI cross-check (runtime)

### Present in OpenAPI

The runtime OpenAPI (`/v3/api-docs`) includes:

- `/.well-known/mill`
- `/api/v1/ai/chats/**`
- `/api/v1/ai/profiles/**`
- `/api/v1/metadata/**`
- `/api/v1/schema/**`
- `/auth/**`
- `/services/jet/**`

### Missing from OpenAPI (but present in code)

- `ai/mill-ai-v1-nlsql-chat-service` — `/api/nl2sql/**` (`NlSqlChatController`)
- `core/mill-test-kit` — `/test-security/**` (`TestController`) (expected to be non-prod / not wired into the running app)

## Notes

- **Retired route (no controller):** `POST /api/v1/queries/execute` — replaced by `/api/v1/query` session API ([`docs/design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md`](../ui/mill-ui/BACKEND-API-REQUIREMENTS.md)).
- No non-annotated functional routes were found (no WebFlux `RouterFunction` / `router {}` patterns detected).
