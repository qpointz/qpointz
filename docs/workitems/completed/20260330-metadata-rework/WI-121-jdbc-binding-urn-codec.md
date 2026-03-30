# WI-121 — JDBC Binding: `MetadataEntityUrnCodec` + `SchemaFacetService` URN Integration

**Story:** Metadata Rework
**Spec sections:** §12
**Depends on:** WI-119
**Parallel with:** WI-120

## Objective

Wire `mill-data-schema-core` to the new metadata model: implement `MetadataEntityUrnCodec`,
move `RelationFacet` and `StructuralFacet` here, and update `SchemaFacetService` to look up
entities by URN instead of by coordinates. **Read paths** must use **`MetadataView`** (SPEC §5.6)
with appropriate **`MetadataContext`** — **not** direct metadata repository calls from data-layer controllers.

## Scope

### 1. `MetadataEntityUrnCodec` implementation (SPEC §12.2)

In `mill-data-schema-core`, implement `MetadataEntityUrnCodec`:

```kotlin
// Default implementation — lowercase, dot-joined local-id
class DefaultMetadataEntityUrnCodec : MetadataEntityUrnCodec {
    override fun forSchema(schema: String): String =
        "urn:mill/metadata/entity:${schema.lowercase()}"
    override fun forTable(schema: String, table: String): String =
        "urn:mill/metadata/entity:${schema.lowercase()}.${table.lowercase()}"
    override fun forAttribute(schema: String, table: String, column: String): String =
        "urn:mill/metadata/entity:${schema.lowercase()}.${table.lowercase()}.${column.lowercase()}"
    override fun decode(urn: String): CatalogPath { ... }
}
```

KDoc on interface and implementation.

### 2. `SchemaEntityKinds` constants (SPEC §12.3)

```kotlin
object SchemaEntityKinds {
    const val SCHEMA    = "schema"
    const val TABLE     = "table"
    const val ATTRIBUTE = "attribute"
}
```

These are the `kind` strings passed to `MetadataEntityService.create(MetadataEntity(kind = "table", ...))`.

### 3. Move `RelationFacet` + `StructuralFacet` from `mill-metadata-core` (SPEC §12.5)

- Move these typed classes to `mill-data-schema-core`.
- Update all import sites in `data/*`.
- Remove them from `mill-metadata-core` (WI-120 deletes them from core side; this WI
  recreates them in `mill-data-schema-core`).

### 4. `SchemaFacetService` — URN-based lookups (SPEC §12.6)

Update all entity lookups to construct URNs via `MetadataEntityUrnCodec` instead of passing
raw coordinate strings:

```kotlin
// Before
metadataEntityService.findByLocation(schema = "public", table = "orders")

// After
val urn = urnCodec.forTable("public", "orders")
metadataEntityService.findById(urn)
```

No coordinate parameters (`schema`, `table`, `attribute`) are passed to any
`MetadataEntityService` or `FacetService` method.

### 5. `SchemaFacetAutoConfiguration` — register data-owned facet types (SPEC §12.4, §8.5)

On startup, call `FacetTypeRepository.save(...)` for:
- `urn:mill/metadata/facet-type:structural` with its `FacetTypeManifest`.
- `urn:mill/metadata/facet-type:relation` with its `FacetTypeManifest`.
- `urn:mill/metadata/facet-type:value-mapping` with its `FacetTypeManifest`.

`applicableTo` for `structural`/`relation`: `["table", "attribute"]` (opaque kind strings).
`applicableTo` for `value-mapping`: `["attribute"]`.

### 6. KDoc coverage

All new and modified types, methods, and parameters in `mill-data-schema-core` must carry KDoc.

## Done Criteria

- `MetadataEntityUrnCodec` interface (from WI-119) implemented by `DefaultMetadataEntityUrnCodec`.
- `SchemaEntityKinds` constants exist.
- `RelationFacet`, `StructuralFacet` compile in `mill-data-schema-core`; no longer in `mill-metadata-core`.
- `SchemaFacetService` has no coordinate-based metadata API calls.
- `SchemaFacetAutoConfiguration` registers the three data-owned facet types at startup.
- `./gradlew :data:mill-data-schema-core:test` passes.
- All new/modified code has KDoc.
