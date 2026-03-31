# Facet Class Elimination — Removing "Blessed" Concrete Facet Types

**Status:** Proposed
**Date:** March 2026
**Story:** `metadata-and-ui-improve-and-clean`

---

## Problem Statement

The metadata module maintains **two parallel representations** for facet types that contradict
each other architecturally:

### System A — Generic / Schema-Driven (current as-built)

`FacetInstance` treats every facet type identically. Facet payloads are opaque
`Map<String, Any?>` governed by `FacetTypeManifest` + `FacetPayloadSchema`.

```kotlin
data class FacetInstance(
    val uid: String,
    val entityId: String,
    val facetTypeKey: String,    // e.g. "urn:mill/metadata/facet-type:descriptive"
    val scopeKey: String,
    val mergeAction: MergeAction,
    val payload: Map<String, Any?>,   // opaque JSON
    ...
)
```

Repositories, persistence, services, REST API, import/export — all operate on `FacetInstance`.
Adding a new facet type requires only a manifest JSON definition. No code changes.

### System B — Concrete Kotlin Classes (legacy holdover)

Six classes in `domain/core/` give special treatment to a subset of facet types:

| Class | `facetType` key | Role |
|-------|-----------------|------|
| `DescriptiveFacet` | `"descriptive"` | Labels, tags, business context |
| `ConceptFacet` | `"concept"` | Business concepts with targets |
| `ValueMappingFacet` | `"value-mapping"` | Term-to-value normalisation |
| `TableLocator` | *(helper)* | Schema + table pair for relation payloads |
| `TableType` | *(enum)* | Physical table category |
| `ConceptTarget` | *(nested)* | Where a concept applies physically |

These extend `AbstractFacet` / `MetadataFacet` which defines its own lifecycle:

```kotlin
interface MetadataFacet {
    val facetType: String
    fun setOwner(owner: MetadataEntity)
    fun validate()
    fun merge(other: MetadataFacet): MetadataFacet
}
```

The bridge between the two systems is `FacetClassResolver` (maps type-key strings to Kotlin
classes) and `FacetConverter` (Jackson `convertValue` from `Map<String, Any?>` to typed class).

## Architectural Violations

| # | Issue | Detail |
|---|-------|--------|
| 1 | **Dual shape definition** | Facet structure defined in both `FacetPayloadSchema` (manifest) and Kotlin class fields. Changes to one can silently drift from the other. |
| 2 | **Dual merge semantics** | `FacetInstance.mergeAction` (row-level UPSERT/DELETE) vs `DescriptiveFacet.merge(other)` (field-level Kotlin logic). Unclear which governs runtime behaviour. |
| 3 | **Dual validation** | `FacetPayloadSchema` field types/required flags vs `ConceptFacet.validate()` / `ValueMappingFacet.validate()`. They can disagree. |
| 4 | **Broken extensibility promise** | Adding a facet type via manifest is zero-code. But typed access requires a concrete class + `FacetClassResolver` registration — making some types "more equal than others". |
| 5 | **Cross-module coupling** | `data/` and `ai/` import `DescriptiveFacet`, `ValueMappingFacet`, etc. from `mill-metadata-core`, coupling them to specific facet shapes at compile time. |

## Affected Files (Concrete Facet Classes)

### Classes to demote (remove lifecycle, keep as data containers)

These live in `metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/`:

- `MetadataFacet.kt` — interface + `AbstractFacet` base class
- `domain/core/DescriptiveFacet.kt`
- `domain/core/ConceptFacet.kt`
- `domain/core/ValueMappingFacet.kt`
- `domain/core/TableLocator.kt`
- `domain/core/TableType.kt`
- `domain/core/ConceptTarget.kt`

### Bridge infrastructure to remove

- `FacetClassResolver.kt` — `FacetClassResolver` interface + `DefaultFacetClassResolver`
- `FacetConverter.kt` — Jackson `convertValue` bridge

### Consumer call-sites (modules importing concrete classes)

| Module | Files |
|--------|-------|
| `data/mill-data-schema-core` | `SchemaFacetServiceImpl`, `SchemaFacets` |
| `data/mill-data-schema-service` | `SchemaExplorerService` |
| `ai/mill-ai-v3` | `SchemaToolHandlers`, `SchemaAuthoringCapability` |
| `ai/mill-ai-v3-cli` | `DemoSchemaFacetService` |
| `ai/mill-ai-v1-core` | `SchemaMessageSpec`, `ValueMappingComponents`, `NlsqlMetadataFacets` |
| `ai/mill-ai-v1-nlsql-chat-service` | `ChatProcessor` |
| `metadata/mill-metadata-autoconfigure` | `MetadataCoreConfiguration` |
| `ui/mill-ui` | `schema.ts`, `schemaService.ts` (TypeScript equivalents) |

## Target State

Concrete facet classes are **demoted to plain data containers** (DTOs / payload helpers):

1. **Remove `MetadataFacet` interface and `AbstractFacet`** — no facet-specific lifecycle
   (`merge`, `validate`, `setOwner`) in domain classes. All merge and validation is handled
   by the generic `FacetInstance` + `FacetPayloadSchema` pipeline.

2. **Keep data container classes** (`DescriptiveFacet`, `ConceptFacet`, `ValueMappingFacet`,
   `TableLocator`, `TableType`, `ConceptTarget`) as plain `data class` / enum types with no
   base class and no lifecycle methods. They simplify `Map<String, Any?>` payload handling for
   consumers.

3. **Relocate or keep in place** — the data containers may stay in `mill-metadata-core` for
   shared access, or move to a lightweight `mill-metadata-types` module. Decision deferred to
   implementation WI.

4. **Remove `FacetClassResolver` and `FacetConverter`** — consumers deserialise payloads
   themselves via standard Jackson if they need typed access. A shared utility function
   (e.g. `FacetPayloadUtils.convert<T>(payload, clazz)`) can replace `FacetConverter` without
   the registry indirection.

5. **Update consumer call-sites** — `data/`, `ai/`, `autoconfigure` stop calling `merge()`,
   `validate()`, `setOwner()` on concrete classes. They use `FacetInstance.payload` +
   optional Jackson conversion to data containers.

## Unused Classes (immediate cleanup)

The inventory also identified dead code that can be removed outright:

| Class | Location | Reason |
|-------|----------|--------|
| `FacetTypeConflictException` | `domain/facet/exceptions/` | Never thrown or caught |
| `FacetTypeNotFoundException` | `domain/facet/exceptions/` | Never thrown or caught |
| `FacetTypeDescriptorDto` | `service/api/dto/` | Zero references |
| `FacetDto` | `service/api/dto/` | Zero references |
| `MetadataView` | `core/service/` | Zero code consumers |
| `MetadataSnapshotService` | `core/service/` | Only impl is also unused |
| `DefaultMetadataSnapshotService` | `core/service/` | Never wired or instantiated |
| `ResourceResolver` | `core/repository/file/` | Interface; never injected |
| `SpringResourceResolver` | `autoconfigure/repository/file/` | Never wired as bean |
| `PlatformFacetTypeDefinitions` | `domain/facet/` | Zero code consumers |
| `TreeNodeDto` | `service/api/dto/` | Only legacy `mill-grinder-ui` (retired) |
| `SearchResultDto` | `service/api/dto/` | Only legacy `mill-grinder-ui` (retired) |
| `FacetPayloadFieldJsonSerde` | `domain/facet/` | Only its own unit test (classes inside used via Jackson annotations — verify before removing) |

## Related Documents

- [`mill-metadata-domain-model.md`](mill-metadata-domain-model.md) — canonical `FacetInstance` model
- [`metadata-service-design.md`](metadata-service-design.md) — service architecture (historical facet class references)
- [`dynamic-facet-types-schema-and-validation.md`](dynamic-facet-types-schema-and-validation.md) — typed vs dynamic facet design discussion
- [`facet-type-descriptor-formats.md`](facet-type-descriptor-formats.md) — manifest JSON contract
