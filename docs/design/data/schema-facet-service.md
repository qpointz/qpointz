# Schema Facet Service

**Status:** Implemented (WI-062)
**Module:** `data/mill-data-schema-core`
**Package:** `io.qpointz.mill.data.schema`

---

## Purpose

`SchemaFacetService` is the aggregation boundary that merges the physical schema exposed by
`SchemaProvider` with schema-bound metadata from `MetadataRepository`. It produces a unified
view consumed by the AI schema capability layer without exposing internal subsystem separation.

The two inputs play distinct roles that must not be blurred:

- `SchemaProvider` — authoritative source of physical schema existence (schemas, tables, attributes)
- `MetadataRepository` — optional descriptive enrichment that may or may not exist for any
  given physical entity

---

## Domain Model

### `WithFacets` interface

Common contract implemented by all `*WithFacets` objects:

```kotlin
interface WithFacets {
    val metadata: MetadataEntity?   // null = no metadata matched
    val facets: SchemaFacets
    val hasMetadata: Boolean        // default impl: metadata != null
}
```

### `SchemaFacets`

Facet holder backed by a `Map<String, MetadataFacet>` keyed on facet type. Provides typed
convenience properties for the standard platform facet types and a generic `facetByType` for
custom or future types:

```kotlin
class SchemaFacets(facets: Set<MetadataFacet>) {
    val descriptive: DescriptiveFacet?
    val structural: StructuralFacet?
    val relation: RelationFacet?
    val concept: ConceptFacet?
    val valueMapping: ValueMappingFacet?
    fun <T : MetadataFacet> facetByType(type: String): T?
}
```

### `*WithFacets` data classes

Each data class carries full physical properties plus the `WithFacets` contract:

| Class | Physical properties |
|---|---|
| `SchemaWithFacets` | `schemaName`, `tables` |
| `SchemaTableWithFacets` | `schemaName`, `tableName`, `tableType`, `attributes` |
| `SchemaAttributeWithFacets` | `schemaName`, `tableName`, `attributeName`, `fieldIndex`, `dataType` |

Physical properties are non-negotiable — they are always present regardless of metadata coverage.

### `SchemaFacetResult`

```kotlin
data class SchemaFacetResult(
    val schemas: List<SchemaWithFacets>,
    val unboundMetadata: List<MetadataEntity>
)
```

`unboundMetadata` holds metadata entities whose coordinates (`schemaName` / `tableName` /
`attributeName`) did not match any physical entity. These are stale or orphaned entries —
present in the metadata store but referencing physical objects that no longer exist.

---

## Matching Logic

`SchemaFacetServiceImpl` matches `MetadataEntity` coordinates to physical schema coordinates:

| Level | Match condition |
|---|---|
| Schema | `entity.schemaName == schemaName`, `tableName == null`, `attributeName == null` |
| Table | `entity.schemaName == schemaName`, `entity.tableName == tableName`, `attributeName == null` |
| Attribute | All three coordinates match |

All physical entities are preserved in the result tree even when no metadata is found. Unmatched
metadata entities are collected into `SchemaFacetResult.unboundMetadata`.

Facets are resolved from the matched entity using a configurable scope (default: `"global"`).

---

## Service Interface

```kotlin
interface SchemaFacetService {
    fun getSchemas(): SchemaFacetResult
}
```

`SchemaFacetServiceImpl` is constructed with a `SchemaProvider`, a `MetadataRepository`, and
an optional scope string. It has no Spring dependencies and is wired via
`SchemaFacetAutoConfiguration` in `data/mill-data-autoconfigure`.

---

## Module Placement

| Module | Contents |
|---|---|
| `data/mill-data-schema-core` | Pure Kotlin — domain model, service interface, `SchemaFacetServiceImpl` |
| `data/mill-data-autoconfigure` | `SchemaFacetAutoConfiguration` — Spring bean wiring |

`SchemaFacetAutoConfiguration` activates when both a `SchemaProvider` bean and a
`MetadataRepository` bean are present on the context.
