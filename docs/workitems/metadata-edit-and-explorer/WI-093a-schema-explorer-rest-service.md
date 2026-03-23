# WI-093a — Schema Explorer REST Service (backend)

Status: `planned`
Type: `✨ feature`
Area: `data`
Depends on: `mill-data-schema-core` (already exists), `mill-data-autoconfigure` (already exists)

---

## Goal

Expose the Calcite physical schema — schemas, tables, columns — enriched with metadata facets as
a read-only REST API under `/api/v1/schema/**`. The domain layer (`SchemaFacetService`,
`SchemaFacetServiceImpl`, `SchemaWithFacets`, etc.) already exists in `mill-data-schema-core`.
This WI delivers a new `data/mill-data-schema-service` module that wraps those types with a
Spring Web controller and DTOs.

---

## Existing Domain Layer (current and target alignment)

**`data/mill-data-schema-core`** already contains:

| Class | Role |
|-------|------|
| `SchemaFacetService` | Interface currently `getSchemas(): SchemaFacetResult`; this WI updates it to `getSchemas(context: MetadataContext = MetadataContext.global()): SchemaFacetResult` |
| `SchemaFacetServiceImpl` | Merges `SchemaProvider` + `MetadataRepository` in one pass |
| `SchemaFacetResult` | `schemas: List<SchemaWithFacets>`, `unboundMetadata: List<MetadataEntity>` |
| `SchemaWithFacets` | Schema + tables + `SchemaFacets` |
| `SchemaTableWithFacets` | Table + columns (renamed from attributes) + `SchemaFacets` |
| `SchemaColumnWithFacets` | Column (renamed from attribute) + `DataType` + `SchemaFacets` |
| `SchemaFacets` | Typed facet holder: `descriptive`, `structural`, `relation`, etc. |
| `WithFacets` | Base interface: `metadata`, `facets`, `hasMetadata` |

`SchemaFacetServiceImpl.getSchemas()` currently calls `metadataRepository.findAll()` once, then
matches all metadata entities to physical coordinates in memory — single metadata query per
request.

For this WI, schema-core and REST semantics should be aligned:
- Use `column` naming (not `attribute`) for physical model exposure.
- Preserve semantic compatibility for ai-v3 consumers while performing the hard rename.
- Keep one metadata scan per request.

### Core interface alignment required

- `SchemaFacetService` must be updated in this WI to accept parsed context:
  `getSchemas(context: MetadataContext = MetadataContext.global()): SchemaFacetResult`.
- `SchemaFacetServiceImpl` resolves facets using the ordered scopes from `MetadataContext`
  (last scope wins), matching metadata-service behavior.
- API service passes parsed context into the core service directly (no ad-hoc wrapper merge logic).

---

## URL Design

Mirrors the metadata API pattern (`/api/v1/metadata/entities/{id}/facets/{typeKey}`) — plural
resource nouns for sub-collections, explicit path segments, no ambiguity.

```
GET /api/v1/schema/context                                              → current context (reserved contract)
GET /api/v1/schema?context=<scopes>                                     → list schemas
GET /api/v1/schema/{schema}?context=<scopes>                            → schema detail
GET /api/v1/schema/{schema}/tables?context=<scopes>                     → table list
GET /api/v1/schema/{schema}/tables/{table}?context=<scopes>             → table detail
GET /api/v1/schema/{schema}/tables/{table}/columns?context=<scopes>     → column list
GET /api/v1/schema/{schema}/tables/{table}/columns/{column}?context=<scopes> → column detail
```

No `/tree` endpoint. The UI loads each level lazily on expand. `SchemaFacetService.getSchemas()`
loads the full tree internally in one call; the controller traverses and trims the result to
return only the requested level.

The `/schemas/` intermediate prefix is dropped — `/api/v1/schema` already scopes all endpoints.
`context` follows metadata semantics: comma-separated scope slugs/URNs with global fallback.

### Context parsing/validation policy

- Context parsing must mirror `metadata/mill-metadata-service` behavior:
  - accepts comma-separated slugs and URNs
  - normalises with `MetadataUrns.normaliseScopePath`
  - preserves order; last scope wins in merge resolution
  - defaults to global when omitted/blank
- Malformed context input returns `400 Bad Request` with a clear message.
- Unknown scope types are allowed for now (authorization and scope-ownership checks are deferred).
- The `400` error contract must be explicitly documented in OpenAPI for each endpoint accepting
  `context`.
- Apply the same malformed-context guard pattern in `metadata/mill-metadata-service` for
  consistency across metadata and schema APIs.

#### Required parsing call site

- Parsing location is mandatory and must be consistent:
  - `SchemaExplorerController` accepts raw `context: String?` request param.
  - `SchemaExplorerService` parses once at method entry via shared metadata-core utility:
    `val ctx = MetadataContext.parse(context)`.
  - `SchemaExplorerService` passes `ctx` to `schemaFacetService.getSchemas(ctx)`.
- Do not duplicate ad-hoc parsing logic in multiple places; use `MetadataContext.parse(...)`
  directly to stay aligned with `mill-metadata-service`.

---

### Reserved context endpoint (phase 1)

- Add `GET /api/v1/schema/context` to expose the currently selected context for UI wiring.
- Phase 1 behavior is intentionally fixed:
  - always returns `global` (slug) and `urn:mill/metadata/scope:global` (URN)
  - no mutation endpoint yet
  - `selectorEnabled=false`
- This endpoint reserves the contract for a future context selector without changing schema data
  endpoints later.

Example response:

```json
{
  "selectedContext": "global",
  "selectedContextUrn": "urn:mill/metadata/scope:global",
  "availableScopes": [
    {
      "slug": "global",
      "urn": "urn:mill/metadata/scope:global",
      "selectable": true
    }
  ],
  "selectorEnabled": false
}
```

---

## Module Structure

### Dependency chain

```
mill-data-schema-core          ← domain (SchemaFacetService, *WithFacets, SchemaFacets)
    ↑ depends on: mill-metadata-core (MetadataRepository interface), mill-data-backend-core (SchemaProvider)

mill-data-schema-service       ← REST (controller, DTOs)
    ↑ depends on: mill-data-schema-core ONLY
    ↑ NO direct dependency on any metadata repository or autoconfigure

metadata/mill-metadata-autoconfigure  ← wiring: creates SchemaFacetService bean
    ↑ knows which MetadataRepository is active (JPA / file / NoOp)
    ↑ depends on: mill-data-schema-core (to reference SchemaFacetService/Impl)
```

`mill-data-schema-service` is completely agnostic to the metadata storage backend. It only
depends on the `SchemaFacetService` interface from `mill-data-schema-core`. Whether the
underlying `MetadataRepository` is JPA, file-backed, or NoOp is resolved by
`mill-metadata-autoconfigure` at runtime.

### Module language/documentation requirements

- New module production code should be Kotlin-first.
- `@ConfigurationProperties` classes should be implemented in Java to ensure Spring metadata
  generation for configuration properties.
- All written or changed production code must include KDoc/JavaDoc at method and parameter level.

---

### `settings.gradle.kts`

Add after `":data:mill-data-schema-core"`:
```kotlin
include(":data:mill-data-schema-service")
```

### `data/mill-data-schema-service/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill physical schema explorer REST service"
    publishArtifacts = false
}

dependencies {
    implementation(project(":data:mill-data-schema-core"))
    // NO dependency on metadata autoconfigure or any repository — backend agnostic
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.web)
    implementation(libs.bundles.jackson)
    implementation(libs.springdoc.openapi.webmvc)
    implementation(libs.bundles.logging)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.web)
                runtimeOnly(libs.h2.database)
            }
        }
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.web)
                    implementation(libs.mockito.kotlin)
                }
            }
        }
    }
}
tasks.named<Test>("testIT") {
    testLogging { events("passed", "failed", "skipped") }
}
```

### `apps/mill-service/build.gradle.kts`

Add:
```kotlin
implementation(project(":data:mill-data-schema-service"))
```

---

## Response DTOs

Package: `io.qpointz.mill.data.schema.api.dto`

### `SchemaListItemDto` — item in `GET /api/v1/schema`

```kotlin
data class SchemaListItemDto(
    val id: String,                 // schemaName
    val metadataEntityId: String?,  // matched metadata id (attachment target)
    val name: String,               // schemaName
    val tableCount: Int,
    val facets: Map<String, FacetEnvelopeDto>? // descriptive only on list endpoints; omit when empty
)
```

### `SchemaDto` — `GET /api/v1/schema/{schema}`

```kotlin
data class SchemaDto(
    val id: String,
    val metadataEntityId: String?,
    val name: String,
    val tables: List<TableSummaryDto>,
    val facets: Map<String, FacetEnvelopeDto>? // full facet map; omit when empty
)

data class TableSummaryDto(
    val id: String,                 // "{schema}.{table}"
    val metadataEntityId: String?,
    val name: String,
    val tableType: String,          // "TABLE" | "VIEW"
    val facets: Map<String, FacetEnvelopeDto>? // descriptive only; omit when empty
)
```

### `TableDto` — `GET /api/v1/schema/{schema}/tables/{table}`

```kotlin
data class TableDto(
    val id: String,                 // "{schema}.{table}"
    val metadataEntityId: String?,
    val schemaName: String,
    val name: String,
    val tableType: String,
    val columns: List<ColumnDto>,
    val facets: Map<String, FacetEnvelopeDto>? // full facet map; omit when empty
)
```

### `ColumnDto` — inline in `TableDto.columns` and standalone in `GET /{schema}/tables/{table}/columns/{column}`

```kotlin
data class ColumnDto(
    val id: String,                 // "{schema}.{table}.{column}"
    val metadataEntityId: String?,
    val schemaName: String,
    val tableName: String,
    val name: String,
    val fieldIndex: Int,
    val type: DataTypeDescriptor,   // neutral Mill type view, no proto exposure
    val nullable: Boolean,
    val facets: Map<String, FacetEnvelopeDto>? // full map in detail; descriptive-only in list
)
```

### `SchemaContextDto` — `GET /api/v1/schema/context`

```kotlin
data class ScopeOptionDto(
    val slug: String,
    val urn: String,
    val selectable: Boolean
)

data class SchemaContextDto(
    val selectedContext: String,       // "global" in phase 1
    val selectedContextUrn: String,    // "urn:mill/metadata/scope:global" in phase 1
    val availableScopes: List<ScopeOptionDto>,
    val selectorEnabled: Boolean       // false in phase 1
)
```

### `DataTypeDescriptor` — REST-safe type projection

```kotlin
data class DataTypeDescriptor(
    val type: String,               // canonical Mill type id (STRING, INTEGER, DECIMAL, ...)
    val nullability: String? = null,
    val precision: Int? = null,
    val scale: Int? = null
)
```

### Facet serialisation

Use metadata-style URN-keyed envelopes:

```kotlin
data class FacetEnvelopeDto(
    val facetType: String,          // full URN, e.g. urn:mill/metadata/facet-type:descriptive
    val payload: Any?
)
```

- Keys in `facets` must be full facet URNs.
- Values must include `facetType` and `payload`.
- Omit `facets` field when empty (absence means empty collection).
- List endpoints include only descriptive facet (`urn:mill/metadata/facet-type:descriptive`) when present.

---

## Controller

Package: `io.qpointz.mill.data.schema.api`
File: `SchemaExplorerController.kt`

```kotlin
@RestController
@RequestMapping("/api/v1/schema")
class SchemaExplorerController(private val service: SchemaExplorerService) {

    @GetMapping("/context")
    fun getContext(): SchemaContextDto = service.getContext()

    @GetMapping
    fun listSchemas(@RequestParam(required = false) context: String?): List<SchemaListItemDto> =
        service.listSchemas(context)

    @GetMapping("/{schema}")
    fun getSchema(@PathVariable schema: String, @RequestParam(required = false) context: String?): SchemaDto =
        service.getSchema(schema, context) ?: throw MillStatuses.notFoundRuntime("Schema not found: $schema")

    @GetMapping("/{schema}/tables")
    fun listTables(@PathVariable schema: String, @RequestParam(required = false) context: String?): List<TableSummaryDto> =
        (service.getSchema(schema, context) ?: throw MillStatuses.notFoundRuntime("Schema not found: $schema")).tables

    @GetMapping("/{schema}/tables/{table}")
    fun getTable(@PathVariable schema: String, @PathVariable table: String, @RequestParam(required = false) context: String?): TableDto =
        service.getTable(schema, table, context)
            ?: throw MillStatuses.notFoundRuntime("Table not found: $schema.$table")

    @GetMapping("/{schema}/tables/{table}/columns")
    fun listColumns(@PathVariable schema: String, @PathVariable table: String, @RequestParam(required = false) context: String?): List<ColumnDto> =
        (service.getTable(schema, table, context)
            ?: throw MillStatuses.notFoundRuntime("Table not found: $schema.$table")).columns

    @GetMapping("/{schema}/tables/{table}/columns/{column}")
    fun getColumn(
        @PathVariable schema: String,
        @PathVariable table: String,
        @PathVariable column: String,
        @RequestParam(required = false) context: String?
    ): ColumnDto = service.getColumn(schema, table, column, context)
            ?: throw MillStatuses.notFoundRuntime("Column not found: $schema.$table.$column")
}
```

404s propagate via `@RestControllerAdvice` — reuse `MetadataExceptionHandler` or register a
new `SchemaExceptionHandler` following the same pattern.

Global direction: map `MillStatusException` to HTTP responses consistently across services.

---

## Service

Package: `io.qpointz.mill.data.schema.api`
File: `SchemaExplorerService.kt`

Constructor: `SchemaFacetService`, `ObjectMapper`

```kotlin
@Service
class SchemaExplorerService(
    private val schemaFacetService: SchemaFacetService,
    private val mapper: ObjectMapper
) {
    fun getContext(): SchemaContextDto
    fun listSchemas(context: String? = null): List<SchemaListItemDto>
    fun getSchema(schemaName: String, context: String? = null): SchemaDto?
    fun getTable(schemaName: String, tableName: String, context: String? = null): TableDto?
    fun getColumn(schemaName: String, tableName: String, columnName: String, context: String? = null): ColumnDto?
}
```

All methods parse raw request context once, then call
`schemaFacetService.getSchemas(parsedContext)` once per request and traverse the result
(one metadata query + in-memory matching).

Recommended helper in service:

```kotlin
private fun parseContext(raw: String?): MetadataContext = try {
    MetadataContext.parse(raw)
} catch (ex: IllegalArgumentException) {
    throw MillStatuses.badRequestRuntime("Malformed context parameter: ${raw ?: "<blank>"}")
}
```

Mapping helpers:
- `SchemaWithFacets.toListItem()` → `SchemaListItemDto`
- `SchemaWithFacets.toDto()` → `SchemaDto`
- `SchemaTableWithFacets.toSummary()` → `TableSummaryDto`
- `SchemaTableWithFacets.toDto()` → `TableDto`
- `SchemaColumnWithFacets.toDto()` → `ColumnDto`
- `SchemaFacets.toUrnEnvelopeMap(mapper)` → `Map<String, FacetEnvelopeDto>?` — omit field when empty

---

## Autoconfiguration ownership

Wiring ownership is split:
- `metadata/mill-metadata-autoconfigure`: metadata-owned beans and context-aware `SchemaFacetService` construction.
- `data/mill-data-autoconfigure`: data-module-dependent wiring only.

New file: `metadata/mill-metadata-autoconfigure/src/main/kotlin/io/qpointz/mill/metadata/configuration/SchemaFacetAutoConfiguration.kt`

```kotlin
@AutoConfiguration
@AutoConfigureAfter(
    MetadataRepositoryAutoConfiguration::class,
    MetadataJpaPersistenceAutoConfiguration::class
)
@ConditionalOnClass(name = ["io.qpointz.mill.data.schema.SchemaFacetService"])
class SchemaFacetAutoConfiguration {

    /**
     * Creates [SchemaFacetService] using whichever [MetadataRepository] is active.
     * JPA, file-backed, and NoOp repositories are all compatible.
     */
    @Bean
    @ConditionalOnMissingBean(SchemaFacetService::class)
    @ConditionalOnBean(SchemaProvider::class)
    fun schemaFacetService(
        schemaProvider: SchemaProvider,
        metadataRepository: MetadataRepository
    ): SchemaFacetService = SchemaFacetServiceImpl(schemaProvider, metadataRepository)
}
```

Register in `metadata/mill-metadata-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Add to `metadata/mill-metadata-autoconfigure/build.gradle.kts`:
```kotlin
compileOnly(project(":data:mill-data-schema-core"))
compileOnly(project(":data:mill-data-backend-core"))
```

---

## Tests

### `SchemaExplorerServiceTest` (unit, `src/test/`)

Mock `SchemaFacetService` to return a result with two schemas (`sales`, `hr`):
- `sales` has two tables; each table has two columns; `sales` has a descriptive facet.
- `hr` has one table; no facets.

Assertions:
- `listSchemas()` returns two `SchemaListItemDto`; `sales.tableCount == 2`.
- `getSchema("sales")` returns `SchemaDto` with two `TableSummaryDto` items and non-null facets.
- `getSchema("missing")` returns `null`.
- `getTable("sales", "customers")` returns `TableDto` with two `ColumnDto` items.
- `getColumn("sales", "customers", "email")` returns `ColumnDto` with `type` set.
- `getColumn("sales", "customers", "missing")` returns `null`.

### `SchemaExplorerControllerWebMvcIT` (`@WebMvcTest`, `src/testIT/`)

- `GET /api/v1/schema/context` → 200 with `selectedContext=global`, `selectorEnabled=false`.
- `GET /api/v1/schema` → 200 JSON array with two schemas.
- `GET /api/v1/schema/sales` → 200 with `SchemaDto`.
- `GET /api/v1/schema/missing` → 404.
- `GET /api/v1/schema/sales/tables` → 200 JSON array with two tables.
- `GET /api/v1/schema/sales/tables/customers` → 200 with `TableDto`.
- `GET /api/v1/schema/sales/tables/missing` → 404.
- `GET /api/v1/schema/sales/tables/customers/columns` → 200 JSON array.
- `GET /api/v1/schema/sales/tables/customers/columns/email` → 200 with `ColumnDto`.
- `GET /api/v1/schema/sales/tables/customers/columns/missing` → 404.
- Malformed `context` (e.g. `context=,`) returns 400 with clear error payload.

### `SchemaExplorerControllerIT` (`src/testIT/`, in-memory H2 context)

- Boot full app context with in-memory H2 and verify endpoint behavior end-to-end.
- Provide a `SchemaProvider` test fixture via `@TestConfiguration` (required), because
  `SchemaFacetAutoConfiguration` is conditional on `SchemaProvider` and H2 alone does not provide it.
- Fixture should return deterministic schema/table/column data (e.g. `sales.customers.email`) used by
  endpoint assertions.
- Verify `GET /api/v1/schema/context` contract returns hardcoded global values.
- Verify context parsing parity with metadata-service semantics (slugs/URNs/order/default-global).
- Verify malformed context returns 400 in integration path.
- Verify OpenAPI includes `400` response documentation for malformed context.
- Verify matching malformed-context handling in `metadata/mill-metadata-service` endpoints.

### Logging and OpenAPI quality checks

- Controller/service logs include request path key, context, resolved entity coordinates, and
  collection sizes (without full payload dumps).
- OpenAPI detail level matches service standards used in `ai/mill-ai-v3-service`:
  operations, parameters, response schemas/examples, and error responses including 400.

---

## Acceptance Criteria

- All seven endpoint patterns return correct responses against a real datasource.
- `GET /api/v1/schema/context` returns hardcoded global context in phase 1.
- `GET /api/v1/schema` returns schema names and table count only — no table or column data.
- All endpoints accept `context` and default to global when omitted.
- Every schema/table/column payload includes `metadataEntityId` when metadata attachment exists.
- `facets` keys are full URNs with `{facetType,payload}` envelopes.
- `facets` is omitted when empty (absence means empty collection).
- List endpoints expose descriptive facet only.
- Malformed `context` input returns `400 Bad Request` with a clear message.
- OpenAPI documents `400` malformed-context response for all context-aware endpoints.
- OpenAPI documents `/api/v1/schema/context` contract and phase-1 fixed behavior.
- Controller integration tests run in `testIT` with in-memory H2 context.
- `@WebMvcTest` controller slice tests are also placed under `src/testIT` for consistency with
  Spring-context test placement rules in this repository.
- `metadata/mill-metadata-service` applies the same malformed-context guard behavior for
  consistency (`400` on malformed context input).
- Unit tests use mocks for service/controller logic.
- All endpoints appear in `/v3/api-docs`.
- Unit and controller tests pass.
- `./gradlew :data:mill-data-schema-service:test` passes.
- `./gradlew :data:mill-data-schema-service:testIT` passes.
