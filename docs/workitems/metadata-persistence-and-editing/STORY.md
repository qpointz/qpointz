# Metadata — Persistence, Scopes, and Read-only Service + UI

Establish relational persistence as the primary metadata storage model, redesign the metadata REST
layer under `/api/v1/metadata/**`, introduce explicit scopes and context composition, and wire
the `mill-ui` model view to the real backend with read-only access.

User editing (write API + UI forms) and the promotion workflow are **deferred to a follow-up
story**. The WI files for those items (`WI-090`, `WI-091`) are retained in this folder for
reference.

Physical schema browsing (`/api/v1/schema/**`) is explicitly **out of scope** for this story.
It will live in a new `data/mill-data-schema-service` module delivered in a follow-up WI.

## Work Items

- [x] WI-085 — Metadata Service API Cleanup (`WI-085-metadata-service-cleanup.md`)
- [x] WI-086 — Metadata REST Controller Redesign (`WI-086-metadata-rest-controller-redesign.md`)
- [x] WI-087 — Metadata Relational Persistence (`WI-087-metadata-relational-persistence.md`)
- [ ] WI-089 — Metadata Scopes and Context Composition (`WI-089-metadata-scopes-and-contexts.md`)
- [ ] WI-092 — `mill-ui` Model View: Real Backend Binding and Inline Chat Disable (`WI-092-mill-ui-model-view-backend-binding.md`)

### Deferred (follow-up story)

- WI-090 — Metadata User Editing (`WI-090-metadata-user-editing.md`)
  Facet types are dynamic (deployment-configurable strings, not a fixed enum). A generic editing
  UI requires a schema-driven form renderer — read facet type descriptor, generate input controls.
  The renderer contract and UX pattern are not yet defined.

- WI-091 — Metadata Promotion Workflow (`WI-091-metadata-promotion-workflow.md`)
  Promotion is a multi-party workflow (request → review → approve/reject). Open design questions:
  which roles can promote to which scopes, whether promotions are instant or queued, and how the
  review UI is surfaced. Depends on WI-089 scope model being live and the workflow state machine
  being defined.

---

## Dependency Map

```
WI-085  ──► WI-086 ──► (new controllers, import/export, MetadataTargetType removed)
WI-087  ──────────────► (new JPA module — can run in parallel with WI-086)
                         └──► WI-089 (scopes extend JPA schema + service layer)
WI-086 + WI-087  ──────► WI-092 (mill-ui read-only binding)
```

WI-085/WI-086 touch `mill-metadata-service` and `mill-metadata-core` only.
WI-087 creates `mill-metadata-persistence` and is independent of REST work.

---


---

## Implementation Standards

These apply to every WI in this story without exception.

### Language
- **Kotlin** for all production code: services, controllers, entities, repositories, adapters, domain types, configuration classes.
- **Java** for `@ConfigurationProperties`-bound classes only — the `spring-boot-configuration-processor` generates `META-INF/spring-configuration-metadata.json` automatically. Kotlin requires a hand-maintained `META-INF/additional-spring-configuration-metadata.json`; use Java to avoid that burden.

### Documentation
- **KDoc** on every production class, function, and parameter — entities, repositories, services, controllers, DTOs, and configuration classes.
- **JavaDoc** on every Java `@ConfigurationProperties` class and its fields.
- Test classes and methods are exempt. Public test-utility helpers should be documented.

### OpenAPI documentation
- Every controller class carries `@Tag(name=, description=)`.
- Every endpoint method carries `@Operation(summary=, description=)` — `description` is required
  for any non-trivial behaviour (error conditions, query parameter semantics, idempotency).
- Every endpoint carries `@ApiResponses` listing **all** possible HTTP status codes, each with
  `@ApiResponse(responseCode=, description=, content=@Content(schema=@Schema(implementation=)))`.
- List responses use `@Content(array = @ArraySchema(schema = @Schema(implementation = Foo::class)))`.
- Error responses reference `MillStatusDetails::class` as the schema implementation.
- Follow `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt`
  as the canonical reference for annotation depth and description quality.

### Logging
- Every service method that mutates state logs at `INFO`: operation, entity id, actor id.
- Observer/async paths log at `INFO` on success, `WARN` on swallowed exceptions (include exception message and entity id).
- Autoconfiguration classes log at `DEBUG` when beans are created or skipped.
- Controllers do not log — logging belongs in the service layer.

### Unit tests (`src/test/`)
- Every service class has a `<Name>Test` with mocked dependencies.
- Every controller has a `@WebMvcTest`-based `<Name>Test` — mock the service, test HTTP semantics (status codes, response shape, error mapping).
- Every non-trivial domain type has a unit test covering all branches.
- Naming: `shouldX_whenY`.

### Integration tests (`src/testIT/`)
- JPA adapters tested against H2 via `@SpringBootTest` with Flyway migration applied.
- REST controllers tested via `MockMvc` with a real Spring context and H2.
- Each `testIT` source set has its own `TestXxxApplication` bootstrap class.
- `src/testIT/resources/application.yml` wires H2 datasource and `spring.flyway.locations=classpath:db/migration`.

---

## Architecture Overview

### Module map

| Module | Role |
|--------|------|
| `metadata/mill-metadata-core` | Domain model, repository interfaces, service layer — zero persistence annotations |
| `metadata/mill-metadata-persistence` | **New (WI-087).** JPA entities, Spring Data repos, adapter implementations |
| `metadata/mill-metadata-autoconfigure` | Spring Boot wiring; conditionally activates file-backed or JPA-backed beans |
| `metadata/mill-metadata-service` | REST controllers and DTOs for `/api/v1/metadata/**` |
| `persistence/mill-persistence` | Shared Flyway migrations; V4 adds metadata tables |

### REST API — `/api/v1/metadata`

All controllers are registered as `@Bean` instances inside autoconfiguration classes — **not**
picked up by component scan. Follow the `AiChatServiceConfiguration` pattern.

#### Entities — `MetadataEntityController`

`@ConditionalOnBean(MetadataRepository::class)`

```
GET    /api/v1/metadata/entities                       ?schema=X&table=Y&context=<csv-urns>
GET    /api/v1/metadata/entities/{id}                  ?context=<csv-urns>
GET    /api/v1/metadata/entities/{id}/facets           ?context=<csv-urns>   → all facets merged (WI-086)
GET    /api/v1/metadata/entities/{id}/facets/{type}    ?context=<csv-urns>
POST   /api/v1/metadata/entities                       → 201 + Location header  (WI-090)
PUT    /api/v1/metadata/entities/{id}                  → 200                    (WI-090)
PATCH  /api/v1/metadata/entities/{id}                  → 200                    (WI-090)
DELETE /api/v1/metadata/entities/{id}                  → 204                    (WI-090)
PUT    /api/v1/metadata/entities/{id}/facets/{type}    ?context=<urn> → 200     (WI-090)
DELETE /api/v1/metadata/entities/{id}/facets/{type}    ?context=<urn> → 204     (WI-090)
GET    /api/v1/metadata/entities/{id}/history          operation audit log for entity (WI-090)
```

`context` is a comma-separated list of scope URN keys; last scope wins. Defaults to
`urn:mill/metadata/scope:global` when omitted.

#### Facet type catalog — `MetadataFacetController`

`@ConditionalOnBean(FacetCatalog::class)`. `targetType` is a plain `String` — no enum.

```
GET    /api/v1/metadata/facets                         ?targetType=<urn>&enabledOnly=false
GET    /api/v1/metadata/facets/{typeKey}
POST   /api/v1/metadata/facets                         → 201
PUT    /api/v1/metadata/facets/{typeKey}               → 200
DELETE /api/v1/metadata/facets/{typeKey}               → 204
```

`{typeKey}` is prefixed slug (local part after `urn:mill/metadata/facet-type:`): `descriptive`, `governance`.
Also accepts legacy short keys. Response bodies use full URN strings.

#### Import / Export — `MetadataImportExportController`

`@ConditionalOnBean(MetadataImportService::class)`

```
POST   /api/v1/metadata/import                         multipart YAML → bulk upsert → 200
GET    /api/v1/metadata/export                         → YAML dump
```

#### Scopes — `MetadataScopeController` (WI-089)

`@ConditionalOnBean(MetadataScopeService::class)`

```
GET    /api/v1/metadata/scopes
POST   /api/v1/metadata/scopes                         → 201
DELETE /api/v1/metadata/scopes/{scopeSlug}             → 204 (409 if global)
```

`{scopeSlug}` is prefixed slug (local part after `urn:mill/metadata/scope:`): `global`, `user:alice`, `team:eng`.
Response bodies use full URN strings. No `scopes/my` endpoint.

#### Promotions — `MetadataPromotionController` (WI-091, deferred)

`@ConditionalOnBean(MetadataPromotionService::class)`

```
GET    /api/v1/metadata/promotions                     ?entityId=X&facetType=Y
GET    /api/v1/metadata/promotions/pending
POST   /api/v1/metadata/promotions                     → 201
POST   /api/v1/metadata/promotions/{id}/approve        → 200
POST   /api/v1/metadata/promotions/{id}/reject         → 200
```

### Persistence contract purity rule

All classes in `mill-metadata-core` must remain free of JPA/persistence annotations. JPA entities
live exclusively in `mill-metadata-persistence` under `io.qpointz.mill.persistence.metadata.jpa.*`
and are mapped to domain types before being returned through any public interface.

### `targetType` is a plain String

`MetadataTargetType` enum is deleted in WI-086. `FacetTypeDescriptor.applicableTo` becomes
`Set<String>`. Callers pass any label the deployment defines — `"table"`, `"schema"`, `"column"`,
`"concept"`, or custom types. No code change needed to support new entity types.

### URN Slug Encoding — `UrnSlug` (`core/mill-core`)

URN strings (e.g. `urn:mill/metadata/scope:global`) are the authoritative keys used in storage,
response bodies, and query parameters. They cannot be placed in URL **path segments** as-is
because the forward slash (`/`) is a path separator.

`UrnSlug` is a shared utility in `core/mill-core` that supports two slug modes:

#### Mode 1 — Full slug (generic)

Used when there is no fixed URN namespace for the path segment. Encodes the entire URN.

```
urn:mill/metadata/facet-type:descriptive
  1. drop "urn:"          →  mill/metadata/facet-type:descriptive
  2. replace "-" → "--"   →  mill/metadata/facet--type:descriptive
  3. replace "/" → "-"    →  mill-metadata-facet--type:descriptive
```

Reverse:
```
mill-metadata-facet--type:descriptive
  1. mark "--" as sentinel
  2. replace "-" → "/"    →  mill/metadata/facet<DASH>type:descriptive
  3. restore sentinel → "-" →  mill/metadata/facet-type:descriptive
  4. prepend "urn:"        →  urn:mill/metadata/facet-type:descriptive
```

#### Mode 2 — Prefixed slug (namespace-scoped, preferred)

When the controller owns a single URN namespace (all values share the same prefix), the common
prefix is stripped and only the local identifier is used as the path segment. The prefix is
declared on the controller and used to validate and reconstruct the full URN.

```
prefix = "urn:mill/metadata/scope:"

urn:mill/metadata/scope:global      →  global
urn:mill/metadata/scope:user:alice  →  user:alice
urn:mill/metadata/scope:team:eng    →  team:eng
```

The controller calls `UrnSlug.decode(slug, urnPrefix)` which:
1. Prepends `urnPrefix` to the slug → full URN.
2. Validates the result starts with the expected prefix (rejects malformed inputs with 400).

This mode only accepts URNs within the declared namespace — passing a scope slug to a facet
endpoint is rejected.

Colons (`:`) survive both modes unchanged — they are valid in URL path segments after the first.

**`UrnSlug` object** — `io.qpointz.mill.UrnSlug` in `core/mill-core`:

```kotlin
/** Bijective, URL-safe encoding of Mill URN keys for REST path segments. */
object UrnSlug {

    // ── Mode 1: Full slug ───────────────────────────────────────────────

    /** Encode a full URN to a URL-safe slug (drop "urn:", escape "-", replace "/" with "-"). */
    fun encode(urn: String): String

    /** Decode a full slug back to its URN. Throws [IllegalArgumentException] on malformed input. */
    fun decode(slug: String): String

    // ── Mode 2: Prefixed slug ───────────────────────────────────────────

    /**
     * Encode a URN to a prefixed slug by stripping [urnPrefix].
     * Throws [IllegalArgumentException] if [urn] does not start with [urnPrefix].
     *
     * Example: encode("urn:mill/metadata/scope:global", "urn:mill/metadata/scope:") → "global"
     */
    fun encode(urn: String, urnPrefix: String): String

    /**
     * Decode a prefixed slug back to its URN by prepending [urnPrefix].
     * Validates that the resulting URN starts with [urnPrefix]; throws [IllegalArgumentException]
     * if not (e.g. if the client passes a scope slug to a facet endpoint).
     *
     * Example: decode("user:alice", "urn:mill/metadata/scope:") → "urn:mill/metadata/scope:user:alice"
     */
    fun decode(slug: String, urnPrefix: String): String

    // ── Helpers ────────────────────────────────────────────────────────

    /** True if the value is a full URN (starts with "urn:"). */
    fun isUrn(value: String): Boolean = value.startsWith("urn:")

    /**
     * Normalise a path variable that may be a full URN, a prefixed slug, or a legacy short key.
     * [urnPrefix] restricts accepted URNs to that namespace.
     * [shortKeyExpander] handles legacy short keys (e.g. "descriptive" → full URN).
     */
    fun normalise(
        value: String,
        urnPrefix: String,
        shortKeyExpander: (String) -> String = { throw IllegalArgumentException("Unknown key: $it") }
    ): String = when {
        isUrn(value) -> value.also { require(it.startsWith(urnPrefix)) { "URN not in namespace $urnPrefix" } }
        else         -> runCatching { decode(value, urnPrefix) }.getOrElse { shortKeyExpander(value) }
    }
}
```

**Convention — path segments vs query params:**

| Location | Form | Example |
|----------|------|---------|
| URL path segment `/{scopeSlug}` | **prefixed slug** | `global` · `user:alice` · `team:eng` |
| URL path segment `/{typeKey}` | **prefixed slug** | `descriptive` · `governance` |
| Query parameter `?context=` | **prefixed slugs**, comma-separated | `global` · `global,user:alice` · `global,user:123,chat:xyz` |
| Request/response body JSON | **full URN** | `"urn:mill/metadata/scope:global"` |

Controllers decode path variables:
- Scope: `UrnSlug.normalise(slug, MetadataUrns.SCOPE_PREFIX)`
- Facet type: `UrnSlug.normalise(typeKey, MetadataUrns.FACET_TYPE_PREFIX, MetadataUrns::normaliseFacetTypeKey)`

### Scope-key encoding contract

`MetadataEntity.facets` is `Map<facetType, Map<scopeKey, payload>>` using full URN strings:

| Scope type | Key format |
|-----------|-----------|
| Global | `"urn:mill/metadata/scope:global"` |
| User | `"urn:mill/metadata/scope:user:<userId>"` |
| Team | `"urn:mill/metadata/scope:team:<teamName>"` |
| Role | `"urn:mill/metadata/scope:role:<roleName>"` |

Legacy short keys (`"global"`, `"user:x"`) are normalised to URN form on import via
`MetadataUrns.normaliseScopeKey`.

### Configuration properties

```yaml
mill:
  metadata:
    storage:
      type: file             # file (default) | jpa
    import-on-startup:       # optional — resource path, e.g. classpath:metadata/example.yml
```

When `import-on-startup` is set, a startup `ApplicationRunner` calls `MetadataImportService`
in `MERGE` mode after context is ready.

### Storage mode selection

```
mill.metadata.storage.type = file   (default — FileMetadataRepository)
mill.metadata.storage.type = jpa    (WI-087 — JpaMetadataRepository)
```

### Metadata change observer chain

All write operations (create, update, delete, promote, import) emit a `MetadataChangeEvent`
through a single `MetadataChangeObserver` dependency. Multiple consumers register themselves as
observer beans; the autoconfiguration assembles them into a `MetadataChangeObserverChain`.

This keeps services decoupled from consumers — adding a search indexer or webhook notifier
requires zero changes to `MetadataEditService` or `MetadataPromotionService`.

```
MetadataEditService ──► MetadataChangeObserver (chain)
                                │
                    ┌───────────┴────────────────────┐
          JpaMetadataChangeObserver        IndexingMetadataChangeObserver (future WI)
          (WI-087 — writes audit log)      (triggers search re-index)
          implements MetadataChangeObserverDelegate
```

**Types in `mill-metadata-core`** (zero framework dependencies):

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
    // Promoted is NOT included — deferred to WI-091. Add it when WI-091 is in scope.
}

/** Primary observer interface — injected into services. Implemented only by the chain. */
fun interface MetadataChangeObserver {
    fun onEvent(event: MetadataChangeEvent)
}

/**
 * Marker interface for leaf observer implementations (audit writers, indexers, etc.).
 * Spring collects all MetadataChangeObserverDelegate beans and injects them into
 * MetadataChangeObserverChain. This avoids circular dependency: the chain itself
 * is a MetadataChangeObserver bean but is NOT a MetadataChangeObserverDelegate.
 */
interface MetadataChangeObserverDelegate : MetadataChangeObserver

/** Composite — iterates all delegates; swallows individual failures. */
class MetadataChangeObserverChain(
    private val delegates: List<MetadataChangeObserverDelegate>
) : MetadataChangeObserver {
    override fun onEvent(event: MetadataChangeEvent) =
        delegates.forEach { runCatching { it.onEvent(event) }.onFailure { log.warn("observer failed", it) } }
}

/** Fallback — no-op, used when no delegates are registered. */
object NoOpMetadataChangeObserver : MetadataChangeObserver {
    override fun onEvent(event: MetadataChangeEvent) = Unit
}
```

**Autoconfiguration assembly** (in `MetadataImportExportAutoConfiguration`):

```kotlin
@Bean @ConditionalOnMissingBean(MetadataChangeObserver::class)
fun metadataChangeObserverChain(
    delegates: List<MetadataChangeObserverDelegate>  // ← delegates only, not the chain itself
): MetadataChangeObserver = if (delegates.isEmpty()) NoOpMetadataChangeObserver
                            else MetadataChangeObserverChain(delegates)
```

The `@ConditionalOnMissingBean(MetadataChangeObserver::class)` guard ensures teams can override
with a custom `MetadataChangeObserver` bean if needed.

**WI delivery:**
- WI-086 — defines `MetadataChangeEvent` (no `Promoted`), `MetadataChangeObserver`, `MetadataChangeObserverDelegate`, `MetadataChangeObserverChain`, `NoOpMetadataChangeObserver`
- WI-087 — delivers `JpaMetadataChangeObserver implements MetadataChangeObserverDelegate`; wires chain in autoconfigure
- WI-090 — `MetadataEditService` injects `MetadataChangeObserver` (the chain)
- WI-091 (deferred) — adds `Promoted` event subtype; `MetadataPromotionService` injects `MetadataChangeObserver`
- Future — any new observer implements `MetadataChangeObserverDelegate` and is picked up by the chain with no service changes

---

## Key Reference Files

| File | Why |
|------|-----|
| `metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/repository/MetadataRepository.kt` | Contract interface the JPA adapter must implement |
| `metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/MetadataEntity.kt` | Facet map structure; scope precedence; audit fields |
| `metadata/mill-metadata-autoconfigure/src/main/kotlin/io/qpointz/mill/metadata/configuration/MetadataCoreConfiguration.kt` | `registerPlatformFacetTypes()` — five types mirrored as V4 seed rows |
| `metadata/mill-metadata-autoconfigure/src/main/kotlin/io/qpointz/mill/metadata/configuration/MetadataRepositoryAutoConfiguration.kt` | Existing file-backed wiring; JPA config must interlock via `@ConditionalOnProperty` |
| `metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/repository/file/FileMetadataRepository.kt` | YAML parsing logic reused by `MetadataImportService` |
| `metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/repository/file/FileFacetTypeRepository.kt` | YAML facet-type parsing reused by `MetadataImportService` |
| `security/mill-security-persistence/src/main/kotlin/io/qpointz/mill/persistence/security/jpa/configuration/SecurityJpaConfiguration.kt` | Template for `MetadataJpaPersistenceAutoConfiguration` |
| `security/mill-security-persistence/build.gradle.kts` | Template for `mill-metadata-persistence/build.gradle.kts` |
| `persistence/mill-persistence/src/main/resources/db/migration/V3__user_identity.sql` | SQL style and seed insert pattern for V4 |
| `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt` | `@Tag`; `MillStatuses` usage; thin controller pattern |
| `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatServiceConfiguration.kt` | Controller-as-`@Bean` in `@AutoConfiguration` — the wiring pattern to follow |
| `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatExceptionHandler.kt` | Template for `MetadataExceptionHandler` |
| `core/mill-core/src/main/java/io/qpointz/mill/excepions/statuses/MillStatuses.java` | `notFoundRuntime`, `badRequestRuntime`, `forbiddenRuntime`, `unprocessableRuntime` |
| `ai/mill-ai-v3-persistence/src/main/kotlin/io/qpointz/mill/persistence/ai/jpa/adapters/JpaConversationStore.kt` | Adapter pattern: JPA entities → domain types, `@Transactional` |

---

## WI-085 — Metadata Service API Cleanup

**Goal:** Fix `MessageHelper` parse errors, remove dead `ProtobufUtils`, register HTTP
`ServiceDescriptor`, rename `mill.metadata.v2.*` config prefix to `mill.metadata.*`.

### Files to modify

| File | Change |
|------|--------|
| `MetadataController.kt` | Remove stale `ProtobufUtils` imports and hard-coded `@CrossOrigin` lists |
| `MetadataRepositoryAutoConfiguration.kt` | Register metadata HTTP `ServiceDescriptor` bean; rename property prefix |
| `MetadataProperties.kt` | Change `@ConfigurationProperties` prefix from `mill.metadata.v2` to `mill.metadata` |

**Tests:** No new files. `:metadata:mill-metadata-service:test` must stay green.

---

## WI-086 — Metadata REST Controller Redesign

**Goal:** Replace all existing metadata controllers with the new `/api/v1/metadata/**` URL scheme.
Delete `MetadataTargetType` enum. Introduce `MetadataImportService` and import/export endpoints.
Wire everything through `@AutoConfiguration` — no component-scanned controllers.

### Controllers to delete

| File | Replacement |
|------|-------------|
| `MetadataController.kt` | `MetadataEntityController` |
| `FacetController.kt` | merged into `MetadataEntityController` |
| `FacetTypeController.kt` | `MetadataFacetController` |
| `SchemaExplorerController.kt` | **deleted** — schema browsing belongs in `data/mill-data-schema-service` (future WI) |

### Domain changes in `mill-metadata-core`

| Change | Detail |
|--------|--------|
| Delete `MetadataTargetType` enum | Replace all usages with `String` |
| `FacetTypeDescriptor.applicableTo` | `Set<MetadataTargetType>` → `Set<String>` |
| Add `MetadataChangeEvent` sealed class | Subtypes: `EntityCreated`, `EntityUpdated`, `EntityDeleted`, `FacetUpdated`, `FacetDeleted`, `Imported`. **`Promoted` is NOT included — deferred to WI-091.** |
| Add `MetadataChangeObserver` | `fun interface`; single `onEvent(event)` method. Injected into services. Only the chain implements this. |
| Add `MetadataChangeObserverDelegate` | Marker interface extending `MetadataChangeObserver`. Leaf observers (audit, indexer) implement this. Chain collects `List<MetadataChangeObserverDelegate>`. |
| Add `MetadataChangeObserverChain` | Composite; accepts `List<MetadataChangeObserverDelegate>`; swallows per-delegate failures |
| Add `NoOpMetadataChangeObserver` | Fallback singleton when no delegates are registered |
| Add `MetadataImportService` | Reuses YAML parsing from `FileMetadataRepository` / `FileFacetTypeRepository`; `import(resource, mode: ImportMode)` and `export(): String`; emits `Imported` event per entity via injected `MetadataChangeObserver` |

### New files in `mill-metadata-service`

| File | Purpose |
|------|---------|
| `MetadataExceptionHandler.kt` | `@RestControllerAdvice` covering all five new controllers. Copy structure from `AiChatExceptionHandler`. |
| `MetadataEntityController.kt` | `/api/v1/metadata/entities` — read endpoints; write stubs activated in WI-090. `@Tag(name="metadata-entities")`. |
| `MetadataFacetController.kt` | `/api/v1/metadata/facets` — full CRUD for facet type catalog. `@Tag(name="metadata-facets")`. |
| `MetadataImportExportController.kt` | `POST /import`, `GET /export`. `@Tag(name="metadata-import-export")`. |

### New autoconfiguration classes in `mill-metadata-autoconfigure`

| File | Beans registered |
|------|-----------------|
| `MetadataEntityServiceAutoConfiguration.kt` | `MetadataEntityController` `@ConditionalOnBean(MetadataRepository::class)`; `MetadataFacetController` `@ConditionalOnBean(FacetCatalog::class)` |
| `MetadataImportExportAutoConfiguration.kt` | `MetadataImportService` bean; `MetadataImportExportController` `@ConditionalOnBean(MetadataImportService::class)`; `MetadataStartupImportRunner` `@ConditionalOnProperty(prefix="mill.metadata", name=["import-on-startup"])`; `MetadataChangeObserverChain` assembled from all **`MetadataChangeObserverDelegate`** beans `@ConditionalOnMissingBean(MetadataChangeObserver::class)` (falls back to `NoOpMetadataChangeObserver` when delegate list is empty) |

`MetadataStartupImportRunner` implements `ApplicationRunner`. Reads the configured resource path
and calls `MetadataImportService.import(resource, MERGE)` after context is ready.

### Tests

| Test | Coverage |
|------|---------|
| `MetadataExceptionHandlerTest` | Each `MillStatus` category maps to correct HTTP status |
| `MetadataEntityControllerTest` | `GET /entities` → 200; `GET /entities/{id}` → 200; unknown id → 404 |
| `MetadataFacetControllerTest` | `GET /facets` → 200; unknown typeKey → 404; `POST` → 201; delete mandatory → 409 |
| `MetadataImportExportControllerTest` | `POST /import` → 200; `GET /export` → 200 YAML body |
| `MetadataImportServiceTest` | Unit: export→import round-trip; MERGE preserves existing; REPLACE wipes first |

---

## WI-087 — Metadata Relational Persistence (new module)

### Module scaffold

`settings.gradle.kts` — add after existing metadata lines:
```kotlin
include(":metadata:mill-metadata-persistence")
```

`metadata/mill-metadata-persistence/build.gradle.kts` — follow `security/mill-security-persistence/build.gradle.kts`:
- `api(project(":metadata:mill-metadata-core"))`
- `api(project(":persistence:mill-persistence"))`
- `implementation(libs.boot.starter.data.jpa)`
- `kotlin("reflect")`
- `testIT` suite: H2 runtime, `boot.starter.test`, `assertj`, `mockito.kotlin`

### Flyway migration — `V4__metadata.sql`

> **Authoritative definition:** `WI-087-metadata-relational-persistence.md` §"Flyway Migration".
> The complete SQL (including all seed INSERTs with URN keys) lives there.
> Do not maintain a duplicate here — WI-087 is the single source of truth.

Location: `persistence/mill-persistence/src/main/resources/db/migration/`

Tables created:
- `metadata_scope` — URN PK (`urn:mill/metadata/scope:global` seeded)
- `metadata_entity` — entity registry with location columns
- `metadata_facet_scope` — scoped facet payloads (BIGINT IDENTITY PK)
- `metadata_facet_type` — facet type catalog (five platform types seeded with URN `type_key`)
- `metadata_promotion` — promotion audit trail (deferred activation — WI-091)
- `metadata_operation_audit` — full change audit log with CLOB before/after snapshots

Seed `scope_id` = `urn:mill/metadata/scope:global`. Seed `type_key` values:
`urn:mill/metadata/facet-type:descriptive`, `structural`, `relation`, `concept`, `value-mapping`
(all with URN prefix). Seed `applicable_to_json` uses URN entity-type strings.

```sql
-- Stub: see WI-087 for the full CREATE TABLE and INSERT statements.
CREATE TABLE metadata_scope (
    scope_id     VARCHAR(255) PRIMARY KEY,
    scope_type   VARCHAR(32)  NOT NULL,
    reference_id VARCHAR(255),
    display_name VARCHAR(512),
    owner_id     VARCHAR(255),
    visibility   VARCHAR(32)  NOT NULL DEFAULT 'PUBLIC',
    created_at   TIMESTAMP    NOT NULL
);
CREATE UNIQUE INDEX uq_metadata_scope_type_ref ON metadata_scope (scope_type, reference_id);
INSERT INTO metadata_scope (scope_id, scope_type, reference_id, display_name, owner_id, visibility, created_at)
VALUES ('urn:mill/metadata/scope:global', 'GLOBAL', NULL, 'Global', NULL, 'PUBLIC', CURRENT_TIMESTAMP);

CREATE TABLE metadata_entity (
    entity_id      VARCHAR(255) PRIMARY KEY,
    entity_type    VARCHAR(64)  NOT NULL,
    schema_name    VARCHAR(512),
    table_name     VARCHAR(512),
    attribute_name VARCHAR(512),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255)
);
CREATE INDEX idx_metadata_entity_type ON metadata_entity (entity_type);
CREATE UNIQUE INDEX uq_metadata_entity_location
    ON metadata_entity (schema_name, table_name, attribute_name);

CREATE TABLE metadata_facet_scope (
    facet_scope_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    entity_id      VARCHAR(255) NOT NULL REFERENCES metadata_entity (entity_id) ON DELETE CASCADE,
    facet_type     VARCHAR(128) NOT NULL,
    scope_id       VARCHAR(255) NOT NULL REFERENCES metadata_scope (scope_id) ON DELETE CASCADE,
    payload_json   TEXT         NOT NULL DEFAULT '{}',
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    CONSTRAINT uq_metadata_facet_scope UNIQUE (entity_id, facet_type, scope_id)
);
CREATE INDEX idx_mfs_entity ON metadata_facet_scope (entity_id);
CREATE INDEX idx_mfs_facet  ON metadata_facet_scope (facet_type);
CREATE INDEX idx_mfs_scope  ON metadata_facet_scope (scope_id);

CREATE TABLE metadata_facet_type (
    type_key            VARCHAR(128) PRIMARY KEY,
    mandatory           BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    display_name        VARCHAR(512),
    description         TEXT,
    applicable_to_json  TEXT         NOT NULL DEFAULT '[]',
    version             VARCHAR(64),
    content_schema_json TEXT,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

-- Seed: platform facet types — see WI-087 for full INSERT with URN type_key and applicable_to_json.
-- type_key uses urn:mill/metadata/facet-type:* and applicable_to uses urn:mill/metadata/entity-type:*

CREATE TABLE metadata_promotion (
    promotion_id    VARCHAR(255) PRIMARY KEY,
    entity_id       VARCHAR(255) NOT NULL REFERENCES metadata_entity (entity_id) ON DELETE CASCADE,
    facet_type      VARCHAR(128) NOT NULL,
    source_scope_id VARCHAR(255) NOT NULL REFERENCES metadata_scope (scope_id),
    target_scope_id VARCHAR(255) NOT NULL REFERENCES metadata_scope (scope_id),
    status          VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    requested_by    VARCHAR(255) NOT NULL,
    reviewed_by     VARCHAR(255),
    requested_at    TIMESTAMP    NOT NULL,
    reviewed_at     TIMESTAMP,
    notes           TEXT
);
CREATE INDEX idx_mp_entity ON metadata_promotion (entity_id, facet_type);
CREATE INDEX idx_mp_status ON metadata_promotion (status);

-- Operation audit log: full history of all create/update/delete/promote/import operations.
-- payload_before / payload_after are CLOBs containing JSON snapshots of affected facet data.
CREATE TABLE metadata_operation_audit (
    audit_id        VARCHAR(255) PRIMARY KEY,
    operation_type  VARCHAR(32)  NOT NULL,   -- CREATE | UPDATE | DELETE | PROMOTE | IMPORT
    entity_id       VARCHAR(255),
    facet_type      VARCHAR(128),            -- NULL for entity-level operations
    scope_key       VARCHAR(512),            -- NULL for entity-level operations
    actor_id        VARCHAR(255)  NOT NULL,
    occurred_at     TIMESTAMP     NOT NULL,
    payload_before  CLOB,                    -- JSON snapshot before change; NULL for CREATE
    payload_after   CLOB,                    -- JSON snapshot after change; NULL for DELETE
    change_summary  VARCHAR(1024)            -- human-readable brief of what changed
);
CREATE INDEX idx_moa_entity    ON metadata_operation_audit (entity_id);
CREATE INDEX idx_moa_actor     ON metadata_operation_audit (actor_id);
CREATE INDEX idx_moa_occurred  ON metadata_operation_audit (occurred_at);
CREATE INDEX idx_moa_operation ON metadata_operation_audit (operation_type);
```

### JPA entities — `io.qpointz.mill.persistence.metadata.jpa.entities`

| Class | Table | Notes |
|-------|-------|-------|
| `MetadataScopeEntity` | `metadata_scope` | `scopeId (PK)`, `scopeType`, `referenceId`, audit fields |
| `MetadataEntityRecord` | `metadata_entity` | `entityId (PK)`, `entityType`, location fields, audit; `@OneToMany(cascade=ALL, orphanRemoval=true)` → `MetadataFacetScopeEntity` |
| `MetadataFacetScopeEntity` | `metadata_facet_scope` | `facetScopeId (PK, IDENTITY)`, `entityId`, `facetType`, `scopeId`, `payloadJson TEXT`, audit |
| `MetadataFacetTypeEntity` | `metadata_facet_type` | `typeKey (PK)`; `applicableToJson TEXT`, `contentSchemaJson TEXT` |
| `MetadataPromotionEntity` | `metadata_promotion` | `promotionId (PK)`, relation ids, `status`, requestor/reviewer audit |
| `MetadataOperationAuditEntity` | `metadata_operation_audit` | `auditId (PK)`, `operationType`, `entityId`, `facetType?`, `scopeKey?`, `actorId`, `occurredAt`, `payloadBefore CLOB`, `payloadAfter CLOB`, `changeSummary` |

### Spring Data repositories — `io.qpointz.mill.persistence.metadata.jpa.repositories`

| Interface | Key finders |
|-----------|------------|
| `MetadataEntityJpaRepository` | `findBySchemaNameAndTableNameAndAttributeName(...)`, `findByEntityType(type)` |
| `MetadataFacetScopeJpaRepository` | `findByEntityId(id)`, `findByEntityIdAndFacetTypeAndScopeId(...)` |
| `MetadataFacetTypeJpaRepository` | standard |
| `MetadataScopeJpaRepository` | `findByScopeTypeAndReferenceId(type, ref)` |
| `MetadataPromotionJpaRepository` | `findByEntityIdAndFacetType(...)`, `findByStatus(status)` |
| `MetadataOperationAuditJpaRepository` | `findByEntityId(id)`, `findByActorId(actorId)`, `findByEntityIdAndFacetType(...)`, `findByOperationTypeAndOccurredAtBetween(...)` |

### Adapters — `io.qpointz.mill.persistence.metadata.jpa.adapters`

**`JpaMetadataRepository`** implements `MetadataRepository`:
- `toDomain(record, facetRows, scopeEntities)` — rebuilds `facets` map: resolves `scopeId → scopeKey` string, deserialises `payloadJson → Map<String,Any?>`.
- `save(entity)` `@Transactional` — upserts `metadata_entity`; for each `(facetType, scopeMap)`: `resolveOrCreateScope(scopeKey) → scopeId`, upserts `metadata_facet_scope`.
- `resolveOrCreateScope(scopeKey)` — queries `MetadataScopeJpaRepository`; creates row if absent.

**`JpaFacetTypeRepository`** implements `FacetTypeRepository`:
- `applicableToJson` → `Set<String>` (plain strings, no enum).
- `contentSchemaJson` → `Map<String,Any?>?`.

**`JpaMetadataChangeObserver`** implements `MetadataChangeObserverDelegate`:
- `onEvent(event)` `@Async` — maps each `MetadataChangeEvent` subtype to a
  `MetadataOperationAuditEntity`: extracts `operationType`, `entityId`, `facetType?`, `scopeKey?`,
  `actorId`, `occurredAt`; serialises `payloadBefore`/`payloadAfter` fields to JSON CLOBs via
  Jackson; persists via `MetadataOperationAuditJpaRepository`.
- Fire-and-forget: exceptions caught and logged at `WARN`, never rethrown.
- Logs at `INFO` for every audit entry recorded (operation, entity id, actor).

Add to `MetadataJpaPersistenceAutoConfiguration`:
```kotlin
@Bean
fun jpaMetadataChangeObserver(...): MetadataChangeObserverDelegate = JpaMetadataChangeObserver(...)
```

Registered as `MetadataChangeObserverDelegate` (not `MetadataChangeObserver`) so the chain
autoconfiguration can collect all delegates without ambiguity.

### Autoconfiguration — `MetadataJpaPersistenceAutoConfiguration`

```kotlin
@AutoConfiguration
@ConditionalOnClass(name = ["io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord"])
@ConditionalOnProperty(prefix = "mill.metadata.storage", name = ["type"], havingValue = "jpa")
@EntityScan(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.entities"])
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.repositories"])
class MetadataJpaPersistenceAutoConfiguration {
    @Bean @ConditionalOnMissingBean(MetadataRepository::class)
    fun jpaMetadataRepository(...): MetadataRepository = JpaMetadataRepository(...)

    @Bean @Primary @ConditionalOnMissingBean(FacetTypeRepository::class)
    fun jpaFacetTypeRepository(...): FacetTypeRepository = JpaFacetTypeRepository(...)
}
```

Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Build changes:
- `mill-metadata-autoconfigure/build.gradle.kts` — `compileOnly(project(":metadata:mill-metadata-persistence"))`
- `apps/mill-service/build.gradle.kts` — `runtimeOnly(project(":metadata:mill-metadata-persistence"))`

### Tests

**Unit:** `JpaMetadataRepositoryTest`, `JpaFacetTypeRepositoryTest` — mock Spring Data repos; verify domain mapping without DB.

**Integration (`testIT`):**
- `TestMetadataPersistenceApplication` — `@SpringBootApplication` importing `MetadataJpaPersistenceAutoConfiguration`
- `JpaMetadataRepositoryIT` — save/findById/findByLocation/deleteById/multi-scope round-trip
- `JpaFacetTypeRepositoryIT` — save/find/delete; verify `applicableTo` strings survive round-trip
- `application.yml` — H2, `spring.flyway.locations=classpath:db/migration`, `mill.metadata.storage.type=jpa`

---

## WI-089 — Metadata Scopes and Context Composition

> **Canonical definition:** `WI-089-metadata-scopes-and-contexts.md`. The notes below are a
> summary only; in case of conflict, the WI file takes precedence.

### Key design decisions

- `MetadataScope` is a **slim domain type** — `scopeId` (full URN), `displayName?`, `ownerId?`, `createdAt`.
  No `scopeType`, `referenceId`, or `visibility` fields on the domain type — those are persistence
  concerns encoded in the URN key.
- **No `PrincipalScopeResolver`** and **no `GET /scopes/my`** endpoint. Callers compose
  `?context=` from known URN slugs themselves. Platform does not guess intent.
- **No `InMemoryMetadataScopeRepository`** in this WI — file-backed mode without a DB simply
  does not have scope management. Scope CRUD requires JPA storage.
- `MetadataContext` is `data class MetadataContext(val scopes: List<String>)` where each string
  is a full URN. `MetadataContext.parse(csv)` accepts comma-separated prefixed slugs and expands
  each via `MetadataUrns.normaliseScopePath`. Unknown scope types (e.g. `chat:xyz`) are valid —
  they silently contribute no facets during resolution.

### JPA adapter in `mill-metadata-persistence`

**`JpaMetadataScopeRepository`** implements `MetadataScopeRepository`; delegates to
`MetadataScopeJpaRepository`; maps `MetadataScopeEntity ↔ MetadataScope`.

Add to `MetadataJpaPersistenceAutoConfiguration`:
```kotlin
@Bean @ConditionalOnMissingBean(MetadataScopeRepository::class)
fun jpaMetadataScopeRepository(...): MetadataScopeRepository = JpaMetadataScopeRepository(...)
```

### Autoconfiguration — in `MetadataEntityServiceAutoConfiguration`

```kotlin
@Bean @ConditionalOnBean(MetadataScopeRepository::class)
fun metadataScopeService(repo: MetadataScopeRepository): MetadataScopeService = MetadataScopeService(repo)

@Bean @ConditionalOnBean(MetadataScopeService::class)
fun metadataScopeController(svc: MetadataScopeService): MetadataScopeController = ...
```

### Tests
- `MetadataContextTest` — unit: slug parsing, merge order, unknown scope silently skipped.
- `MetadataScopeServiceTest` — unit: mock repo; create/delete/globalScope guard.
- `JpaMetadataScopeRepositoryIT` — integration: save/findById/findAll/deleteById; global scope seeded by V4.
- `MetadataScopeControllerTest` — `@WebMvcTest`: `GET /scopes` 200; `POST /scopes` 201; `DELETE /scopes/global` 409.

---

## WI-090 — Metadata User Editing

### New domain types in `mill-metadata-core`

### New service in `mill-metadata-core`

**`MetadataEditService.kt`** (constructor: `MetadataRepository`, `MetadataScopeService`, `FacetCatalog`, `MetadataChangeObserver`):
- `createEntity(entity, actorId)` — validates, stamps audit fields, saves. Emits `EntityCreated`.
- `updateEntity(id, patch, actorId)` — loads, merges non-null patch fields, saves. Emits `EntityUpdated(before, after)`.
- `deleteEntity(id, actorId)` — scope ownership check, deletes. Emits `EntityDeleted`.
- `setFacet(entityId, facetType, scopeKey, facetData, actorId)` — validates, checks scope ownership, saves. Emits `FacetUpdated(before, after)`.
- `deleteFacet(entityId, facetType, scopeKey, actorId)` — removes scope entry. Emits `FacetDeleted`.
- All events emitted via injected `MetadataChangeObserver.onEvent(...)` after successful persistence. Observer failures never block or roll back the operation.
- Throws: `MillStatuses.notFoundRuntime`, `MillStatuses.forbiddenRuntime`, `MillStatuses.unprocessableRuntime`.

Add to `MetadataRepositoryAutoConfiguration`:
```kotlin
@Bean @ConditionalOnBean(MetadataRepository::class, FacetCatalog::class)
fun metadataEditService(repo, scopeService, catalog): MetadataEditService = MetadataEditService(...)
```

### Write endpoints added to `MetadataEntityController`

No new controller file. The write operations (`POST`, `PUT`, `PATCH`, `DELETE /entities/{id}`,
`PUT`/`DELETE /entities/{id}/facets/{type}`) stub out in WI-086 and are fully wired here by
injecting `MetadataEditService`.

Actor id sourced from `SecurityContextHolder`; fallback to `X-Actor-Id` request header.

### New DTOs
- `FacetWriteRequest(facetType: String, scope: String = "global", data: Any?)`
- `PatchEntityRequest` — nullable fields covering the v1 editable surface (displayName, description, businessDomain, businessOwner, tags, classification).

### Tests
- `MetadataEditServiceTest` — unit: mock all; `createEntity` stamps audit fields; `setFacet` rejects unknown facet type; `deleteEntity` throws `NOT_FOUND` for missing id; `setFacet` throws `FORBIDDEN` when actor does not own scope.
- `MetadataEntityControllerTest` (extend from WI-086) — add: `POST /entities` → 201; `DELETE /entities/{id}` → 204; bad body → 422; missing entity → 404.

---

## WI-091 — Metadata Promotion Workflow

### New domain types in `mill-metadata-core`

**`MetadataPromotion.kt`** — pure data class: `promotionId`, `entityId`, `facetType`,
`sourceScopeId`, `targetScopeId`, `status: PromotionStatus` (`PENDING/APPROVED/REJECTED`),
`requestedBy`, `reviewedBy?`, `requestedAt`, `reviewedAt?`, `notes?`.

**`MetadataPromotionRepository.kt`** — interface: `save`, `findById`, `findByEntityIdAndFacetType`,
`findByStatus`, `findPendingForTargetScope`.

### New service in `mill-metadata-core`

**`MetadataPromotionService.kt`** (constructor includes `MetadataChangeObserver`):
- `requestPromotion(entityId, facetType, sourceScopeKey, targetScopeKey, requestedBy, notes?)` — creates `PENDING`; auto-approves if target is `GLOBAL` and actor has admin privileges.
- `approvePromotion(promotionId, reviewedBy)` — sets `APPROVED`, calls `executePromotion`.
- `rejectPromotion(promotionId, reviewedBy, notes?)` — sets `REJECTED`.
- `executePromotion(promotion)` (private) — loads source facet, calls `MetadataEditService.setFacet` against target scope. Emits `Promoted(sourceScopeKey, targetScopeKey, payload)` via `MetadataChangeObserver`. Source scope is **not** deleted.
- `getPendingForScope(targetScopeKey)` — review queue.

### JPA adapter in `mill-metadata-persistence`

**`JpaMetadataPromotionRepository.kt`** — implements `MetadataPromotionRepository`; standard delegate + `MetadataPromotionEntity ↔ MetadataPromotion` mapping.

Add to `MetadataJpaPersistenceAutoConfiguration`:
```kotlin
@Bean @ConditionalOnMissingBean(MetadataPromotionRepository::class)
fun jpaMetadataPromotionRepository(...): MetadataPromotionRepository = JpaMetadataPromotionRepository(...)
```

### New controller in `mill-metadata-service`

**`MetadataPromotionController.kt`** — `@RestController @RequestMapping("/api/v1/metadata/promotions") @Tag(name="metadata-promotions")`.

Registered as `@Bean @ConditionalOnBean(MetadataPromotionService::class)` in
`MetadataEntityServiceAutoConfiguration`.

### New DTOs
- `PromotionRequestDto(entityId, facetType, sourceScope, targetScope, notes?)`
- `PromotionResponseDto(promotionId, status, requestedBy, requestedAt, reviewedBy?, reviewedAt?, notes?)`

### Tests
- `MetadataPromotionServiceTest` — unit: `requestPromotion` → PENDING; `approvePromotion` calls `executePromotion`; auto-approve path for global target; source scope not deleted.
- `JpaMetadataPromotionRepositoryIT` — integration: save, `findByEntityIdAndFacetType`, `findByStatus`.
- `MetadataPromotionControllerTest` — `@WebMvcTest`: `POST /api/v1/metadata/promotions` → 201; unknown `{id}` → 404; `POST /{id}/approve` → 200.

---

## Verification

| WI | Command |
|----|---------|
| WI-085, WI-086 | `./gradlew :metadata:mill-metadata-service:test` |
| WI-087 | `./gradlew :metadata:mill-metadata-persistence:test :metadata:mill-metadata-persistence:testIT` |
| WI-089, WI-090, WI-091 (service layer) | `./gradlew :metadata:mill-metadata-core:test` |
| WI-089, WI-090, WI-091 (REST layer) | `./gradlew :metadata:mill-metadata-service:test` |
| WI-089, WI-091 (JPA adapters) | `./gradlew :metadata:mill-metadata-persistence:testIT` |

Full gate before story closure:
```bash
./gradlew :metadata:mill-metadata-core:test \
          :metadata:mill-metadata-persistence:test \
          :metadata:mill-metadata-persistence:testIT \
          :metadata:mill-metadata-service:test
```
