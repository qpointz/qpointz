# WI-086 — Metadata REST Controller Redesign

Status: `planned`
Type: `✨ feature`
Area: `metadata`, `platform`

## Goal

Replace all existing metadata controllers with the new `/api/v1/metadata/**` URL scheme.
Delete `MetadataTargetType` enum; replace with plain `String`. Introduce the Mill URN naming
convention for metadata objects. Implement `MetadataImportService` with a YAML import format
that supports both entities and optional custom facet type definitions. Wire everything through
`@AutoConfiguration` — no component-scanned controllers.

---

## Mill URN Naming Convention

All mill object identifiers follow: `urn:mill/<domain>/<type>:<local-id>`

| Object | URN pattern | Examples |
|--------|-------------|---------|
| Facet type | `urn:mill/metadata/facet-type:<name>` | `urn:mill/metadata/facet-type:descriptive` |
| Scope | `urn:mill/metadata/scope:<type>[:<ref>]` | `urn:mill/metadata/scope:global` · `urn:mill/metadata/scope:user:alice` |
| Entity type | `urn:mill/metadata/entity-type:<name>` | `urn:mill/metadata/entity-type:schema` · `urn:mill/metadata/entity-type:table` |

**Platform facet type URNs** (replacing old short keys and `MetadataTargetType` enum values):

| Short key (old) | URN (new) | Applies to |
|-----------------|-----------|-----------|
| `descriptive` | `urn:mill/metadata/facet-type:descriptive` | `entity-type:schema`, `table`, `attribute` |
| `structural` | `urn:mill/metadata/facet-type:structural` | `entity-type:table`, `attribute` |
| `relation` | `urn:mill/metadata/facet-type:relation` | `entity-type:table` |
| `concept` | `urn:mill/metadata/facet-type:concept` | `entity-type:concept` |
| `value-mapping` | `urn:mill/metadata/facet-type:value-mapping` | `entity-type:attribute` |

**Scope URN key format** (`MetadataEntity.facets` inner scope map key):

| Scope type | Key format |
|-----------|-----------|
| Global | `urn:mill/metadata/scope:global` |
| User | `urn:mill/metadata/scope:user:<userId>` |
| Team | `urn:mill/metadata/scope:team:<teamName>` |
| Role | `urn:mill/metadata/scope:role:<roleName>` |

**Backward compatibility:** The import service normalises legacy short names to URNs on read.
`MetadataEntity.getMergedFacet()` scope key lookups (`"global"`, `"user:x"`, etc.) are also
updated to use URN format via `MetadataUrns`.

A single `MetadataUrns` utility object in `mill-metadata-core` provides all constants and
conversion helpers:

```kotlin
object MetadataUrns {
    /** URN namespace prefixes — used by [UrnSlug] for prefixed-slug encoding on path variables. */
    const val FACET_TYPE_PREFIX = "urn:mill/metadata/facet-type:"
    const val SCOPE_PREFIX      = "urn:mill/metadata/scope:"
    const val ENTITY_TYPE_PREFIX = "urn:mill/metadata/entity-type:"

    const val FACET_TYPE_DESCRIPTIVE   = "urn:mill/metadata/facet-type:descriptive"
    const val FACET_TYPE_STRUCTURAL    = "urn:mill/metadata/facet-type:structural"
    const val FACET_TYPE_RELATION      = "urn:mill/metadata/facet-type:relation"
    const val FACET_TYPE_CONCEPT       = "urn:mill/metadata/facet-type:concept"
    const val FACET_TYPE_VALUE_MAPPING = "urn:mill/metadata/facet-type:value-mapping"

    const val ENTITY_TYPE_SCHEMA    = "urn:mill/metadata/entity-type:schema"
    const val ENTITY_TYPE_TABLE     = "urn:mill/metadata/entity-type:table"
    const val ENTITY_TYPE_ATTRIBUTE = "urn:mill/metadata/entity-type:attribute"
    const val ENTITY_TYPE_CONCEPT   = "urn:mill/metadata/entity-type:concept"

    const val SCOPE_GLOBAL = "urn:mill/metadata/scope:global"
    fun scopeUser(userId: String)   = "urn:mill/metadata/scope:user:$userId"
    fun scopeTeam(teamName: String) = "urn:mill/metadata/scope:team:$teamName"
    fun scopeRole(roleName: String) = "urn:mill/metadata/scope:role:$roleName"

    /** Normalise a legacy short facet-type key to its URN. Returns the input unchanged if already a URN. */
    fun normaliseFacetTypeKey(key: String): String = when (key) {
        "descriptive"   -> FACET_TYPE_DESCRIPTIVE
        "structural"    -> FACET_TYPE_STRUCTURAL
        "relation"      -> FACET_TYPE_RELATION
        "concept"       -> FACET_TYPE_CONCEPT
        "value-mapping" -> FACET_TYPE_VALUE_MAPPING
        else            -> key
    }

    /** Normalise a legacy scope key to its URN form. */
    fun normaliseScopeKey(key: String): String = when {
        key == "global"         -> SCOPE_GLOBAL
        key.startsWith("user:") -> "urn:mill/metadata/scope:$key"
        key.startsWith("team:") -> "urn:mill/metadata/scope:$key"
        key.startsWith("role:") -> "urn:mill/metadata/scope:$key"
        key.startsWith("urn:")  -> key
        else                    -> key
    }

    /**
     * Encode a URN to a URL-safe slug for use in path segments.
     * Delegates to [UrnSlug.encode] from `core/mill-core`.
     */
    fun toSlug(urn: String): String = UrnSlug.encode(urn)

    /**
     * Normalise a facet-type path variable — accepts prefixed slug, legacy short key, or full URN.
     * Controllers call this on every `{typeKey}` path variable.
     * Example inputs: "descriptive", "governance", "urn:mill/metadata/facet-type:descriptive"
     */
    fun normaliseFacetTypePath(value: String): String =
        UrnSlug.normalise(value, FACET_TYPE_PREFIX, ::normaliseFacetTypeKey)

    /**
     * Normalise a scope path variable — accepts prefixed slug or full URN.
     * Only URNs within the [SCOPE_PREFIX] namespace are accepted.
     * Example inputs: "global", "user:alice", "team:eng"
     */
    fun normaliseScopePath(value: String): String =
        UrnSlug.normalise(value, SCOPE_PREFIX)
}
```

---

## Domain Changes in `mill-metadata-core`

### 1. Delete `MetadataTargetType` enum (`Enums.kt`)

Remove the `MetadataTargetType` enum. Update all call sites:

| Before | After |
|--------|-------|
| `applicableTo: Set<MetadataTargetType>?` | `applicableTo: Set<String>?` |
| `isApplicableTo(t: MetadataTargetType)` | `isApplicableTo(t: String)` |
| `MetadataTargetType.TABLE` | `MetadataUrns.ENTITY_TYPE_TABLE` |
| `MetadataTargetType.ANY` sentinel | Remove — null/empty set means "applies to all" |

Updated `FacetTypeDescriptor.isApplicableTo`:
```kotlin
fun isApplicableTo(targetType: String): Boolean {
    val targets = applicableTo
    if (targets.isNullOrEmpty()) return true
    return targetType in targets
}
```

Update `MetadataEntity.getMergedFacet` to use URN scope keys:
- `scopedFacets["global"]` → `scopedFacets[MetadataUrns.SCOPE_GLOBAL]`
- `scopedFacets["role:$role"]` → `scopedFacets[MetadataUrns.scopeRole(role)]`
- etc.

### 2. Add `MetadataUrns` object (`MetadataUrns.kt`)

New file — `io.qpointz.mill.metadata.domain.MetadataUrns`. Full definition in the URN section
above. KDoc each constant and function.

Depends on `UrnSlug` from `core/mill-core` (add `api(project(":core:mill-core"))` dependency if
not already present in `mill-metadata-core/build.gradle.kts`).

`UrnSlug` is a new utility object in `core/mill-core` — `io.qpointz.mill.UrnSlug`. See
STORY.md §"URN Slug Encoding" for full specification and algorithm. Must be delivered in this WI
before the controller layer can normalise path variables.

### 3. Add `MetadataChangeObserver` types (`MetadataChangeObserver.kt`)

New file — `io.qpointz.mill.metadata.domain`:

```kotlin
sealed class MetadataChangeEvent {
    abstract val entityId: String
    abstract val actorId: String
    abstract val occurredAt: Instant

    data class EntityCreated(override val entityId: String, override val actorId: String,
        override val occurredAt: Instant, val entity: MetadataEntity) : MetadataChangeEvent()
    data class EntityUpdated(override val entityId: String, override val actorId: String,
        override val occurredAt: Instant,
        val before: MetadataEntity, val after: MetadataEntity) : MetadataChangeEvent()
    data class EntityDeleted(override val entityId: String, override val actorId: String,
        override val occurredAt: Instant, val entity: MetadataEntity) : MetadataChangeEvent()
    data class FacetUpdated(override val entityId: String, override val actorId: String,
        override val occurredAt: Instant, val facetType: String,
        val scopeKey: String, val before: Any?, val after: Any?) : MetadataChangeEvent()
    data class FacetDeleted(override val entityId: String, override val actorId: String,
        override val occurredAt: Instant, val facetType: String,
        val scopeKey: String, val payload: Any?) : MetadataChangeEvent()
    data class Imported(override val entityId: String, override val actorId: String,
        override val occurredAt: Instant, val entity: MetadataEntity,
        val mode: ImportMode) : MetadataChangeEvent()
    // Promoted is NOT defined here — deferred to WI-091.
}

/** Primary observer interface — injected into services. Only the chain implements this directly. */
fun interface MetadataChangeObserver {
    /** Receives a metadata change event. Must not throw — swallow and log on failure. */
    fun onEvent(event: MetadataChangeEvent)
}

/**
 * Marker interface for leaf observer implementations (JPA audit writer, search indexer, etc.).
 * Spring collects all [MetadataChangeObserverDelegate] beans into [MetadataChangeObserverChain].
 * The chain itself is a [MetadataChangeObserver] bean but NOT a [MetadataChangeObserverDelegate],
 * which avoids the circular-dependency problem where a List<MetadataChangeObserver> injection
 * would include the chain itself.
 */
interface MetadataChangeObserverDelegate : MetadataChangeObserver

/** Composite — iterates all delegates; logs and swallows individual failures. */
class MetadataChangeObserverChain(
    private val delegates: List<MetadataChangeObserverDelegate>
) : MetadataChangeObserver {
    private val log = LoggerFactory.getLogger(MetadataChangeObserverChain::class.java)
    override fun onEvent(event: MetadataChangeEvent) =
        delegates.forEach {
            runCatching { it.onEvent(event) }
                .onFailure { e -> log.warn("Observer failed for {}: {}", event::class.simpleName, e.message) }
        }
}

/** No-op singleton used when no delegates are registered. */
object NoOpMetadataChangeObserver : MetadataChangeObserver {
    override fun onEvent(event: MetadataChangeEvent) = Unit
}
```

### 4. Add `MetadataImportService` interface (`MetadataImportService.kt`)

New file — `io.qpointz.mill.metadata.service`:

```kotlin
enum class ImportMode { MERGE, REPLACE }

data class ImportResult(
    /** Number of entities saved (inserted or updated). */
    val entitiesImported: Int,
    /** Number of custom facet types registered from the facet-types section. */
    val facetTypesImported: Int,
    /** Non-fatal per-row errors accumulated during import. Import continues after each. */
    val errors: List<String>
)

interface MetadataImportService {
    /**
     * Import metadata from a Spring [Resource] (YAML document or multi-document YAML).
     *
     * The resource may contain an `entities:` section and an optional `facet-types:` section.
     * Multiple YAML documents separated by `---` are supported.
     *
     * Facet type keys and scope keys in the YAML may use legacy short names (`descriptive`,
     * `global`) or URN notation. Both forms are normalised to URN before persistence.
     *
     * Platform facet types (descriptive, structural, relation, concept, value-mapping) are
     * always known and do not need to appear in `facet-types:`.
     *
     * @param resource Spring resource (classpath:, file:, etc.)
     * @param mode     MERGE preserves entities not mentioned in the file.
     *                 REPLACE deletes all entities in the global scope before importing.
     * @param actorId  Identity of the importing actor; recorded in audit entries.
     * @return summary of the import operation.
     */
    fun import(
        resource: Resource,
        mode: ImportMode = ImportMode.MERGE,
        actorId: String = "system"
    ): ImportResult

    /**
     * Export all metadata entities as a YAML document compatible with [import].
     * Output always uses URN-notation keys for facet types and scopes.
     *
     * @param scopeKey export only facets stored under this scope key.
     *                 Defaults to [MetadataUrns.SCOPE_GLOBAL].
     */
    fun export(scopeKey: String = MetadataUrns.SCOPE_GLOBAL): String
}
```

---

## YAML Import Format

### Extended format — with custom facet type definitions

```yaml
# Optional: custom facet type definitions.
# Platform types (descriptive, structural, relation, concept, value-mapping) never need declaration.
facet-types:
  - typeKey: urn:mill/metadata/facet-type:governance
    displayName: Governance
    description: Data governance and ownership metadata
    applicableTo:
      - urn:mill/metadata/entity-type:schema
      - urn:mill/metadata/entity-type:table
    mandatory: false
    enabled: true
---
entities:
  - id: myschema
    type: SCHEMA
    schemaName: myschema
    createdAt: '2025-01-01T00:00:00Z'
    updatedAt: '2025-01-01T00:00:00Z'
    facets:
      urn:mill/metadata/facet-type:descriptive:       # URN key (preferred for new files)
        urn:mill/metadata/scope:global:
          displayName: My Schema
      urn:mill/metadata/facet-type:governance:        # custom type defined above
        urn:mill/metadata/scope:global:
          dataOwner: team-platform
```

### Legacy format — backward compatible (existing test datasets)

```yaml
entities:
  - id: moneta
    type: SCHEMA
    schemaName: moneta
    createdAt: '2025-11-05T10:00:00Z'
    updatedAt: '2025-11-05T10:00:00Z'
    facets:
      descriptive:          # short key — normalised to URN on import
        global:             # short scope key — normalised to URN on import
          displayName: Moneta
          description: Client moneta schema
```

Both formats are valid input. The import service normalises all keys to URN before persisting.

### `DefaultMetadataImportService` — implementation notes

Package: `io.qpointz.mill.metadata.service`

1. **Multi-document splitting** — split raw YAML string on `\n---\n`; parse each fragment.
2. **`facet-types:` handling** — for each `FacetTypeDescriptor`: call
   `MetadataUrns.normaliseFacetTypeKey` on `typeKey` and each `applicableTo` string;
   save via `FacetTypeRepository`.
3. **Key normalisation** — after Jackson deserialises each `MetadataEntity`, re-key
   `entity.facets`: outer keys via `normaliseFacetTypeKey`, inner scope keys via
   `normaliseScopeKey`.
4. **REPLACE mode** — call `repo.deleteAll()` before saving entities.
   `MetadataRepository` must expose `deleteAll(): Unit` — add to the interface if not already
   present. `FileMetadataRepository` implements it by clearing the in-memory map and removing
   the backing file content; `JpaMetadataRepository` delegates to `MetadataEntityJpaRepository.deleteAll()`.
5. **Observer emission** — per entity: `observer.onEvent(MetadataChangeEvent.Imported(…))`.
6. **Error accumulation** — catch per-entity exceptions; add message to `errors`; never abort
   the whole import.
7. **Export** — fetch `repo.findAll()`, filter each entity's facets to the requested scope,
   serialise via `YamlMetadataExporter`; prepend `facet-types:` section with non-platform
   descriptors from `FacetTypeRepository.findAll()`.

Reuse `YamlMetadataImporter` / `YamlMetadataExporter` from `MetadataIO.kt` for the entity
serialisation step.

---

## Controllers to Delete

| File | Replacement |
|------|-------------|
| `MetadataController.kt` | `MetadataEntityController` |
| `FacetController.kt` | merged into `MetadataEntityController` facet endpoints |
| `FacetTypeController.kt` | `MetadataFacetController` |
| `SchemaExplorerController.kt` | **deleted** — schema browsing is out of scope (future `data/mill-data-schema-service`) |

---

## New Controllers in `mill-metadata-service`

### `MetadataExceptionHandler.kt`

`@RestControllerAdvice` — no `assignableTypes` restriction (covers all controllers in the
application context). Lives in **`mill-metadata-service`** alongside the controllers it covers.
Maps `MillStatusRuntimeException` to HTTP responses. Copy structure from `AiChatExceptionHandler`.

`MillStatus` → HTTP mapping:

| MillStatus | HTTP | Scenario |
|-----------|------|---------|
| `NOT_FOUND` | 404 | Entity or facet type not found |
| `BAD_REQUEST` | 400 | Malformed body or invalid YAML |
| `CONFLICT` | 409 | Delete of mandatory facet type |
| `FORBIDDEN` | 403 | Actor lacks scope permission |
| `UNPROCESSABLE_ENTITY` | 422 | Facet payload fails content-schema validation |
| Unhandled | 500 | Unexpected error |

Copy structure from `AiChatExceptionHandler`.

### `MetadataEntityController.kt`

`@RestController @RequestMapping("/api/v1/metadata/entities")`
`@Tag(name = "metadata-entities")`

Read endpoints only in this WI. Write stubs added in WI-090.

```
GET  /api/v1/metadata/entities                          ?schema=X&table=Y&context=<csv-urns>
GET  /api/v1/metadata/entities/{id}                     ?context=<csv-urns>
GET  /api/v1/metadata/entities/{id}/facets              ?context=<csv-urns>  → all facets merged
GET  /api/v1/metadata/entities/{id}/facets/{typeKey}    ?context=<csv-urns>  → single facet type merged
```

- `{id}` — entity ID string (no slashes; not a URN slug).
- `{typeKey}` — facet type as a **prefixed slug** (local part after `urn:mill/metadata/facet-type:`).
  Examples: `descriptive`, `structural`, `governance`.
  Controller normalises via `MetadataUrns.normaliseFacetTypePath(typeKey)` — also accepts
  legacy short keys and full URNs for developer convenience.
- `context` — comma-separated **prefixed slugs** (local part after `urn:mill/metadata/scope:`).
  Examples: `global`, `global,user:alice`, `global,user:123,chat:xyz`.
  Defaults to `MetadataContext.global()` when omitted.
  Parsed server-side via `MetadataContext.parse(contextParam)` which expands each slug to a
  full URN via `MetadataUrns.normaliseScopePath`. No `encodeURIComponent` needed for slugs;
  colons are valid in query parameter values.
- `GET /entities/{id}/facets` returns `Map<facetTypeUrn, FacetResponseDto>` — one entry per
  facet type present under any scope in the context. Facet types with no data are omitted.
  Keys are full URN strings. `FacetResponseDto { facetType: String, payload: Any? }`.
- All facet response shapes use the wrapped `FacetResponseDto` — never bare payloads.
- Returns `MetadataEntityDto` / `FacetResponseDto`.

### `MetadataFacetController.kt`

`@RestController @RequestMapping("/api/v1/metadata/facets")`
`@Tag(name = "metadata-facets")`

```
GET    /api/v1/metadata/facets              ?targetType=<urn>&enabledOnly=false
GET    /api/v1/metadata/facets/{typeKey}
POST   /api/v1/metadata/facets              → 201 + Location header
PUT    /api/v1/metadata/facets/{typeKey}    → 200
DELETE /api/v1/metadata/facets/{typeKey}    → 204 (409 if mandatory=true)
```

- `{typeKey}` in path — prefixed slug (`descriptive`, `governance`) or legacy short key.
  Controller normalises via `MetadataUrns.normaliseFacetTypePath(typeKey)`.
- `targetType` query param — prefixed slug or full URN; normalised via `MetadataUrns.normaliseFacetTypePath`.
- Response DTO `applicableTo` always returns full URN strings.
Request/response DTO: `FacetTypeDescriptorDto` mirrors `FacetTypeDescriptor` with
`applicableTo: Set<String>`.

### `MetadataImportExportController.kt`

`@RestController @RequestMapping("/api/v1/metadata")`
`@Tag(name = "metadata-import-export")`

```
POST  /api/v1/metadata/import    multipart/form-data; file=<yaml>  → 200 ImportResultDto
GET   /api/v1/metadata/export    ?scope=<urn>                      → 200 text/yaml
```

`ImportResultDto(entitiesImported, facetTypesImported, errors[])`.

---

## Autoconfiguration in `mill-metadata-autoconfigure`

### `MetadataEntityServiceAutoConfiguration.kt`

```kotlin
@AutoConfiguration
class MetadataEntityServiceAutoConfiguration {
    @Bean @ConditionalOnBean(MetadataRepository::class)
    fun metadataEntityController(svc: MetadataService): MetadataEntityController = ...

    @Bean @ConditionalOnBean(FacetCatalog::class)
    fun metadataFacetController(catalog: FacetCatalog): MetadataFacetController = ...
}
```

### `MetadataImportExportAutoConfiguration.kt`

```kotlin
@AutoConfiguration
class MetadataImportExportAutoConfiguration {
    @Bean @ConditionalOnBean(MetadataRepository::class)
    fun metadataImportService(
        repo: MetadataRepository,
        facetTypeRepo: FacetTypeRepository,
        observer: MetadataChangeObserver
    ): MetadataImportService = DefaultMetadataImportService(repo, facetTypeRepo, observer)

    @Bean @ConditionalOnBean(MetadataImportService::class)
    fun metadataImportExportController(svc: MetadataImportService): MetadataImportExportController = ...

    @Bean
    @ConditionalOnProperty(prefix = "mill.metadata", name = ["import-on-startup"])
    fun metadataStartupImportRunner(
        svc: MetadataImportService,
        props: MetadataProperties
    ): ApplicationRunner = ApplicationRunner {
        val resource = ClassPathResource(props.importOnStartup!!)
        svc.import(resource, ImportMode.MERGE, actorId = "system")
    }

    /** Assembles all MetadataChangeObserverDelegate beans into the single MetadataChangeObserver chain. */
    @Bean @ConditionalOnMissingBean(MetadataChangeObserver::class)
    fun metadataChangeObserverChain(
        delegates: List<MetadataChangeObserverDelegate>  // delegates only — chain not included
    ): MetadataChangeObserver =
        if (delegates.isEmpty()) NoOpMetadataChangeObserver
        else MetadataChangeObserverChain(delegates)
}
```

`MetadataProperties` is a **Java** `@ConfigurationProperties(prefix = "mill.metadata")` class
(renamed from the old `v2` prefix; covered in WI-085).

---

## Tests

| Test | Coverage |
|------|---------|
| `UrnSlugTest` (in `core/mill-core`) | `encode` round-trips all platform URNs; `decode` is inverse of `encode`; hyphens in local-id survive via `--` escaping; `normalise` accepts URN / slug / short key |
| `MetadataUrnsTest` | `normaliseFacetTypeKey` all platform types + URN pass-through; `normaliseScopeKey` all variants; `normaliseFacetTypePath` accepts slug, short key, and full URN; `normaliseScopePath` accepts slug and full URN |
| `MetadataEntityControllerTest` (`@WebMvcTest`) | `GET /entities` → 200; `GET /entities/{id}` → 200; `GET /entities/{id}/facets` → 200 merged map; unknown id → 404; `context` param forwarded as `MetadataContext`; omitted `context` defaults to global |
| `MetadataFacetControllerTest` (`@WebMvcTest`) | `GET /facets` → 200; short-key `GET /facets/descriptive` resolves; `POST` → 201; delete mandatory → 409 |
| `MetadataImportExportControllerTest` (`@WebMvcTest`) | `POST /import` with moneta YAML → 200; `GET /export` → `text/yaml` content type |
| `MetadataExceptionHandlerTest` | Each `MillStatus` → correct HTTP code |
| `DefaultMetadataImportServiceTest` | Legacy short-key YAML normalised to URNs; `facet-types:` registers custom type; REPLACE wipes before import; per-entity error accumulated without aborting; export→import round-trip |

Integration test for import:
- Load `test/datasets/moneta/moneta-meta-repository.yaml` via classpath resource.
- Call `metadataImportService.import(resource, MERGE, "test")`.
- Assert `entitiesImported > 0`, `errors` is empty.
- Assert `repo.findById("moneta.clients")` is present with `descriptive` facet under
  `urn:mill/metadata/scope:global`.

---

## Acceptance Criteria

- Old `/api/metadata/v1/**` controllers are deleted; new `/api/v1/metadata/**` active.
- `MetadataTargetType` enum removed; all `applicableTo` fields use `Set<String>`.
- Importing `moneta-meta-repository.yaml` and `skymill-meta-repository.yaml` succeeds with
  zero errors; facet type keys stored as URNs.
- Custom facet types declared in `facet-types:` YAML section visible via
  `GET /api/v1/metadata/facets`.
- `GET /api/v1/metadata/export` produces YAML that round-trips through `POST /import`.
- All unit and `@WebMvcTest` tests pass.
