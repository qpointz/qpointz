# WI-005: Eliminate Legacy Metadata Implementation

**Type:** refactoring
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-005-eliminate-legacy-metadata`

---

## Goal

Remove the legacy metadata implementation entirely. The v2 faceted metadata system
(`mill-metadata-core`, `mill-metadata-service`, `mill-metadata-autoconfigure`) is the
replacement. The legacy code is a parallel implementation that must be eliminated to
reduce confusion, duplication, and maintenance burden.

## Related Backlog Items

- M-13: Remove legacy adapter layer: migrate all usages to MetadataService directly
- M-14: Migrate SchemaMessageSpec to MetadataEntity; remove legacy model classes

## Related Design Documents

- `docs/design/metadata/metadata-provider-refactoring-plan.md`
- `docs/design/metadata/metadata-implementation-roadmap.md`
- `docs/design/metadata/metadata-service-design.md`
- `docs/design/metadata/value-mapping-tactical-solution.md`

---

## Current State

### Legacy (to be removed)

**Module:** `metadata/mill-metadata-provider`
**Package:** `io.qpointz.mill.data.backend.metadata`

Interfaces:

- `MetadataProvider` — core interface: `getModel()`, `getSchemas()`, `getTables()`,
  `getRelations()`, `getTable()`, `getAllValueMappings()`, `getAllValueMappingSources()`.
  Contains nested records `ValueMappingWithContext` and `ValueMappingSourceWithContext`.
- `AnnotationsRepository` — provides descriptions, value mappings, value mapping sources.
- `RelationsProvider` — provides relations.

Legacy implementations (file-based):

- `MetadataProviderImpl` — wires `SchemaProvider` + `AnnotationsRepository` + `RelationsProvider`
- `FileRepository` — YAML parser (legacy format: `model/schemas/tables/attributes`)
- `FileAnnotationsRepository` — reads descriptions and value mappings from `FileRepository`
- `FileRelationsProvider` — reads relations from `FileRepository`
- `NoneAnnotationsRepository` — no-op fallback
- `NoneRelationsProvider` — no-op fallback

V2 bridge implementations (in same module, using `MetadataService` from v2 core):

- `MetadataV2AnniotationsProvider` — implements `AnnotationsRepository` by delegating
  to `MetadataService` and reading `DescriptiveFacet`/`ValueMappingFacet`
- `MetadataV2RelationsProvider` — implements `RelationsProvider` by delegating
  to `MetadataService` and reading `RelationFacet`

Legacy model classes:

- `model/Model.java` — `name`, `description`
- `model/Schema.java` — `name`, `description`, `tables`
- `model/Table.java` — `name`, `description`, `attributes`
- `model/Attribute.java` — `name`, `description`, `type`, `valueMappings`
- `model/Relation.java` — `source`, `target`, `attributeRelation`, `cardinality`
- `model/ValueMapping.java` — `userTerm`, `databaseValue`, etc.

Legacy configuration:

- `MetadataConfiguration` (in `data/mill-data-autoconfigure`) — Spring beans for
  `MetadataProviderImpl`, `FileRepository`, `FileAnnotationsRepository`,
  `FileRelationsProvider`, `NoneAnnotationsRepository`, `NoneRelationsProvider`
- `LegacyAutoconfiguration` (in `data/mill-data-autoconfigure`) — wires
  `MetadataConfiguration` and `DefaultServiceConfiguration`

### V2 (the replacement, already implemented)

**Module:** `metadata/mill-metadata-core`
**Package:** `io.qpointz.mill.metadata`

- `MetadataEntity` — document-style entity with scoped facets
- `MetadataFacet` / `AbstractFacet` / `FacetRegistry` — facet plugin system
- Core facets: `StructuralFacet`, `DescriptiveFacet`, `RelationFacet`, `ConceptFacet`,
  `ValueMappingFacet`
- `MetadataRepository` interface + `FileMetadataRepository` (YAML, multi-file)
- `MetadataService` — service layer

**Module:** `metadata/mill-metadata-autoconfigure`

- `MetadataProperties`, `MetadataCoreConfiguration`, `MetadataRepositoryAutoConfiguration`

**Module:** `metadata/mill-metadata-service`

- REST controllers: `MetadataController`, `SchemaExplorerController`, `FacetController`
- DTOs: `MetadataEntityDto`, `FacetDto`, `TreeNodeDto`, `SearchResultDto`

---

## Consumers of Legacy Code

### AI module — `ai/mill-ai-v1-core` (main sources)

| File | Uses |
|------|------|
| `ChatApplication.java` | `MetadataProvider` (injected) |
| `IntentSpecs.java` | `MetadataProvider` |
| `MessageSpecs.java` | `MetadataProvider` |
| `messages/specs/SchemaMessageSpec.java` | `MetadataProvider`, legacy model classes (`Schema`, `Table`, `Attribute`, `Relation`) |
| `components/ValueMappingComponents.java` | `MetadataProvider` (value mappings) |
| `reasoners/DefaultReasoner.java` | `MetadataProvider` |
| `reasoners/StepBackReasoner.java` | `MetadataProvider` |
| `chat/messages/MessageSpecs.java` | `MetadataProvider` |

### AI module — `ai/mill-ai-v1-core` (tests)

| File | Uses |
|------|------|
| `SchemaMessageSpecTest.java` | `MetadataProvider`, legacy model classes |
| `ValueMappingComponentsTest.java` | `MetadataProvider` |
| `BaseIntentTestIT.java` | `MetadataProvider` |
| `ReasoningTestIT.java` | `MetadataProvider` |
| `RefineIntentTestIt.java` | `MetadataProvider` |
| `EnrichModelIntentTestIT.java` | `MetadataProvider` |
| `GetDataIIntentTestIT.java` | `MetadataProvider` |
| `GetChartIntentTestIT.java` | `MetadataProvider` |
| `DoConversationIntentTestIT.java` | `MetadataProvider` |
| `ExplainIntentTestIT.java` | `MetadataProvider` |
| `stepback/StepBackReasonerIntegrationTest.java` | `MetadataProvider` |
| `scenarios/ChatAppScenarioBase.java` | `MetadataProvider` |
| `scenarios/ChatAppScenarioContext.java` | `MetadataProvider` |

### AI service — `ai/mill-ai-v1-nlsql-chat-service`

| File | Uses |
|------|------|
| `ChatProcessor.java` | `MetadataProvider` |

### Data autoconfigure — `data/mill-data-autoconfigure`

| File | Uses |
|------|------|
| `MetadataConfiguration.java` | All legacy beans |
| `LegacyAutoconfiguration.java` | `MetadataConfiguration` |

### Data backend core — `data/mill-data-backend-core`

| File | Uses |
|------|------|
| `ServiceHandler.java` | `MetadataProvider` (injected, calls `getSchemas`, `getTables`) |

### Gradle build files

| File | Dependency |
|------|------------|
| `settings.gradle.kts` | `include(":metadata:mill-metadata-provider")` |
| `data/mill-data-autoconfigure/build.gradle.kts` | `api(project(":metadata:mill-metadata-provider"))` |
| `metadata/build.gradle.kts` | `dokka(project(":metadata:mill-metadata-provider"))` |

---

## Migration Strategy

Replace `MetadataProvider` with `MetadataService` directly in all consumers. No adapter
layer — the v2 bridge classes (`MetadataV2AnniotationsProvider`, `MetadataV2RelationsProvider`)
already demonstrate the mapping, and their logic should be inlined into the call sites
or extracted into thin helpers within the AI module.

### Critical migration: `SchemaMessageSpec`

`SchemaMessageSpec` is the most complex consumer. It uses the legacy model classes
(`Schema`, `Table`, `Attribute`, `Relation`) to build prompt messages for the LLM.
This must be rewritten to use `MetadataService` directly, reading `DescriptiveFacet`,
`StructuralFacet`, `RelationFacet`, and `ValueMappingFacet` from `MetadataEntity`.

### Critical migration: `ValueMappingComponents`

Uses `MetadataProvider.getAllValueMappings()` and `MetadataProvider.getAllValueMappingSources()`.
Replace with calls to `MetadataService.findByType(ATTRIBUTE)` and reading
`ValueMappingFacet` from each entity — same logic as `MetadataV2AnniotationsProvider`
currently does, but without the legacy interface in between.

### Critical migration: `ServiceHandler`

Uses `MetadataProvider.getSchemas()` and `MetadataProvider.getTables()`. Replace with
`MetadataService` calls or keep using `SchemaProvider` directly (which is the actual
source of physical schema information).

---

## Steps

### 1. Update AI module to depend on `mill-metadata-core` directly

Add `mill-metadata-core` dependency to `ai/mill-ai-v1-core/build.gradle.kts`.
Remove dependency on `mill-metadata-provider` (transitive through `data/mill-data-autoconfigure`
or direct).

### 2. Migrate AI consumers from `MetadataProvider` to `MetadataService`

For each file in the AI consumer list above:

- Replace `MetadataProvider` injection with `MetadataService`
- Replace `getSchemas()` / `getTables()` / `getTable()` with `MetadataService.findByType()`
  and `MetadataService.findByLocation()` reading `DescriptiveFacet` / `StructuralFacet`
- Replace `getRelations()` with `MetadataService` reading `RelationFacet`
- Replace `getAllValueMappings()` with `MetadataService.findByType(ATTRIBUTE)` +
  `ValueMappingFacet`
- Replace `getAllValueMappingSources()` similarly

### 3. Rewrite `SchemaMessageSpec`

This class builds LLM prompts from the legacy model hierarchy. Rewrite to:

- Accept `MetadataService` instead of `MetadataProvider`
- Query entities by type (SCHEMA, TABLE, ATTRIBUTE)
- Read `DescriptiveFacet` for display names, descriptions, business meanings
- Read `StructuralFacet` for physical types
- Read `RelationFacet` for relationships
- Read `ConceptFacet` for business concepts

The output format (the prompt text) should remain the same — only the data source changes.

### 4. Migrate `ServiceHandler`

Replace `MetadataProvider` usage with either `MetadataService` or `SchemaProvider`
directly, depending on what information is needed (physical schema vs enriched metadata).

### 5. Migrate `ChatProcessor`

Replace `MetadataProvider` injection with `MetadataService` in
`ai/mill-ai-v1-nlsql-chat-service`.

### 6. Remove `MetadataConfiguration` legacy beans

In `data/mill-data-autoconfigure/MetadataConfiguration.java`:

- Remove all beans: `defaultMetadataProvider`, `metadataFileRepository`,
  `fileAnnotationsRepository`, `fileRelationsProvider`, `noneAnnotationsRepository`,
  `noneRelationsProvider`
- Either delete the class entirely or keep it only if there are non-legacy beans
  that still need to live there

### 7. Remove `LegacyAutoconfiguration`

Delete `data/mill-data-autoconfigure/LegacyAutoconfiguration.java` if it has no
remaining purpose after removing `MetadataConfiguration`.

### 8. Delete `metadata/mill-metadata-provider` module

Delete the entire module:

**Main sources (17 files):**
- `MetadataProvider.java`
- `AnnotationsRepository.java`
- `RelationsProvider.java`
- `impl/MetadataProviderImpl.java`
- `impl/NoneAnnotationsRepository.java`
- `impl/NoneRelationsProvider.java`
- `impl/file/FileRepository.java`
- `impl/file/FileAnnotationsRepository.java`
- `impl/file/FileRelationsProvider.java`
- `impl/v2/MetadataV2AnniotationsProvider.java`
- `impl/v2/MetadataV2RelationsProvider.java`
- `model/Model.java`
- `model/Schema.java`
- `model/Table.java`
- `model/Attribute.java`
- `model/Relation.java`
- `model/ValueMapping.java`

**Test sources (6 files):**
- `MetadataProviderImplTest.java`
- `FileAnnotationsRepositoryTest.java`
- `ValueMappingTest.java`
- `ValueMappingSampleTest.java`
- `MetadataV2RelationsProviderTest.java`
- `MetadataV2AnnotationsProviderTest.java`

**Build file:**
- `metadata/mill-metadata-provider/build.gradle.kts`

### 9. Remove Gradle references

- `settings.gradle.kts`: remove `include(":metadata:mill-metadata-provider")`
- `data/mill-data-autoconfigure/build.gradle.kts`: remove
  `api(project(":metadata:mill-metadata-provider"))`
- `metadata/build.gradle.kts`: remove
  `dokka(project(":metadata:mill-metadata-provider"))`

### 10. Update AI test fixtures

All test and testIT files in `ai/mill-ai-v1-core` that mock or stub `MetadataProvider`
must be updated to mock or stub `MetadataService` instead.

### 11. Clean up legacy YAML format support

If any YAML config files still use the legacy format (`model/schemas/tables/attributes`
without facets), migrate them to the v2 faceted format. The v2 `FileMetadataRepository`
uses `entities` array with scoped `facets` map.

Check `metadata/mill-metadata-provider/src/test/resources/` and any application
config files for legacy YAML format.

---

## Persistence-Friendly Domain Model Review

Same approach as WI-003 (policy format redesign): the metadata domain model in
`mill-metadata-core` must be storage-agnostic and persistence-ready without being
tied to JPA or any specific storage technology.

### Design Decision: JSON Bags, Not Normalized Facets

Facets are **not** persisted as normalized relational tables. Each facet is stored
as a JSON/JSONB blob. The entity table exposes only a minimal set of common
properties as indexed columns for querying. The full facet content lives inside the
JSON bag and is deserialized at read time.

This means:
- No per-facet-type tables (no `descriptive_facets`, `relation_facets`, etc.)
- No ORM mapping of individual facet fields to columns
- Facet schema evolution is handled by the JSON structure, not by DB migrations
- Querying by facet content (when needed) uses JSON path expressions at the DB level

### Target Table Layout (Conceptual)

```
metadata_entity
├── id              VARCHAR   PK
├── type            VARCHAR   indexed  (SCHEMA, TABLE, ATTRIBUTE, CONCEPT)
├── schema_name     VARCHAR   indexed  (nullable)
├── table_name      VARCHAR   indexed  (nullable)
├── attribute_name  VARCHAR   indexed  (nullable)
├── facets          JSONB            — the entire facets map as a JSON bag
├── created_at      TIMESTAMP
├── updated_at      TIMESTAMP
├── created_by      VARCHAR
└── updated_by      VARCHAR
```

The `facets` column holds the full `Map<String, Map<String, Object>>` structure
serialized as JSON. The repository reads it back and deserializes on demand.

Composite index on `(schema_name, table_name, attribute_name)` covers the
hierarchical lookup pattern used by `findByLocation()`.

### Current State Assessment

`MetadataEntity` is already close to this model:
- Pure Java, no Spring annotations
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok)
- Column-ready fields: `id`, `type`, `schemaName`, `tableName`, `attributeName`,
  `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- Facets stored as `Map<String, Map<String, Object>>` — already a JSON bag

**Issues to fix:**

1. **Static `ObjectMapper` in domain class** — the entity owns serialization
   infrastructure (`ObjectMapper` instance, `convertValue` calls in `getFacet()`
   and `getMergedFacet()`). This must move out. The domain class should be a plain
   data holder. Deserialization belongs in the repository or a `FacetConverter`
   utility.

2. **No `Serializable`** — add `implements Serializable` for cache/transfer
   readiness.

### Changes Required

#### 1. Extract `ObjectMapper` from `MetadataEntity`

Move all `ObjectMapper` usage out of the entity into a `FacetConverter` utility:

```java
public class FacetConverter {
    private final ObjectMapper objectMapper;
    public <T> Optional<T> convert(Object raw, Class<T> facetClass) { ... }
}
```

`MetadataEntity.getFacet()` becomes a thin accessor that returns the raw `Object`
from the map. Typed conversion is done by the caller (service layer or converter).
Alternatively, `MetadataService` wraps the conversion so consumers don't deal with
raw objects.

#### 2. Keep `Map<String, Map<String, Object>>` for facets

The facets map stays as-is — it **is** the JSON bag. No typed `MetadataFacet` map
at the entity level. The entity is a persistence-friendly DTO; typed facet objects
are a service-layer concern.

#### 3. Add `Serializable`

Add `implements Serializable` to `MetadataEntity`.

#### 4. Ensure default constructors

Already satisfied by Lombok `@NoArgsConstructor`.

#### 5. Import/export capability

Following WI-003's pattern, define interfaces for metadata import/export:

```java
public interface MetadataExporter {
    void export(Collection<MetadataEntity> entities, OutputStream target);
}

public interface MetadataImporter {
    Collection<MetadataEntity> importFrom(InputStream source);
}
```

Supported formats: JSON, YAML (at minimum). This enables migration between
storage backends (file -> DB, DB -> file) and version-controlled metadata.
The existing `FileMetadataRepository` already reads YAML — the exporter is the
missing piece.

### Scope Note

This is a **preparation** step. The goal is to make `MetadataEntity` clean enough
that a JPA repository (with a `@Column(columnDefinition = "jsonb")` on `facets`)
or any other backend can be plugged in without changing the domain class. Actual
JPA/persistence implementation is out of scope for this WI.

---

## Additional Cleanup: Relocate `SchemaExplorerController`

`SchemaExplorerController` in `mill-metadata-service` builds a hierarchical
schema → table → attribute tree, performs text search, and has a lineage endpoint.
This is a **consumer** of metadata, not a manager of it — it imposes a
data-layer interpretation (schemas contain tables contain attributes) that does
not belong in the context-free metadata module.

Move `SchemaExplorerController` (and its DTOs `TreeNodeDto`, `SearchResultDto`)
to a higher-level service module (e.g. `data/` or the grinder service). The
metadata module should expose only raw entity/facet CRUD APIs.

See **WI-007** for details.

---

## Related Work Items

- **WI-006** — Facet Type Library / Facet Catalog: persistent facet type
  descriptors, `_type` marker, target type validation, JSON Schema content
  validation, per-deployment configuration. Depends on WI-005 (clean base).
- **WI-007** — Metadata service layer cleanup: relocate `SchemaExplorerController`
  to data layer.

---

## Risks

1. **NL2SQL prompt regression** — `SchemaMessageSpec` builds LLM prompts from the
   legacy model. Even if the same data comes from `MetadataService`, subtle
   differences (field ordering, nulls vs empty strings, missing descriptions) could
   silently change prompt quality and degrade query accuracy. No automated test
   catches this unless integration tests cover the exact prompts.

2. **Value mapping silent breakage** — `ValueMappingComponents` relies on alias
   expansion, similarity thresholds, context strings. The v2 bridge
   (`MetadataV2AnniotationsProvider`) already does this mapping, but inlining it
   into AI code without the bridge could miss edge cases. Failures here are
   silent — the LLM just generates worse SQL.

3. **Missing `MetadataService` bean in deployments** — Legacy had fallbacks
   (`NoneAnnotationsRepository`, `NoneRelationsProvider`). If
   `mill-metadata-autoconfigure` isn't on the classpath or no YAML is configured,
   `MetadataService` might not exist as a bean. Need a fallback or clear startup
   failure.

4. **Circular dependency risk** — `ServiceHandler` (in `data/mill-data-backend-core`)
   currently uses `MetadataProvider`. Replacing with `MetadataService` means
   `data-backend-core` depends on `mill-metadata-core`. Need to verify this doesn't
   create a cycle.

5. **Legacy YAML configs in production** — Any deployment still using the legacy
   format (`model/schemas/tables/attributes` without `entities/facets`) will break
   at runtime. Migration must happen before or alongside the code change.

6. **AI integration test infrastructure** — All `testIT` tests mock/stub
   `MetadataProvider`. Rewiring to `MetadataService` means rebuilding test fixtures.
   If test coverage was thin to begin with, regressions slip through.

---

## Out of Scope

- Facet Type Library / Facet Catalog (see WI-006).
- Relocating `SchemaExplorerController` (see WI-007).
- JPA repository implementation (future phase per the roadmap).
- New metadata features (enrichments, editing, composite repo).
- Migrating the Grinder UI React `MetadataProvider.tsx` — this is a React context
  provider, not related to the Java legacy class.

---

## Verification

1. `metadata/mill-metadata-provider` module no longer exists.
2. No Java source file imports from `io.qpointz.mill.data.backend.metadata` package
   (except the `configuration` subpackage if `MetadataConfiguration.java` is retained
   for non-legacy beans).
3. `./gradlew build` succeeds from project root.
4. `./gradlew test` passes in all affected modules:
   - `ai/mill-ai-v1-core`
   - `ai/mill-ai-v1-nlsql-chat-service`
   - `data/mill-data-autoconfigure`
   - `data/mill-data-backend-core`
   - `metadata/mill-metadata-core`
   - `metadata/mill-metadata-service`
5. All AI integration tests (`testIT`) pass.
6. No references to `MetadataProvider`, `AnnotationsRepository`, `RelationsProvider`,
   `FileRepository` (legacy), or legacy model classes (`model.Schema`, `model.Table`,
   `model.Attribute`, `model.Relation`, `model.Model`) remain in any source file.
7. Grinder UI continues to work (it uses REST API from `mill-metadata-service`, which
   is unaffected).

## Estimated Effort

Large — touches ~35 files across 4 modules. The AI module is the heaviest due to
`SchemaMessageSpec` rewrite and test fixture updates. The actual deletion is mechanical
but the migration of consumers requires understanding how each one uses metadata.
