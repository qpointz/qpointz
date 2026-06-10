# WI-142 — Typed URN codec

**Story:** typed-entity-urns  
**Status:** Open  
**Type:** refactoring  
**Area:** data, metadata

## Summary

Replace the flat `urn:mill/metadata/entity:<local>` form with per-class typed URNs
(`urn:mill/model/<class>:<id>`) in all codec, builder, and parser logic. After this WI the
new URN form is the authoritative identifier generated and decoded everywhere in the data
layer; the metadata layer continues to treat entity URNs as opaque strings and needs no
changes.

## New URN grammar

| Old | New |
|-----|-----|
| `urn:mill/metadata/entity:myschema` | `urn:mill/model/schema:myschema` |
| `urn:mill/metadata/entity:myschema.mytable` | `urn:mill/model/table:myschema.mytable` |
| `urn:mill/metadata/entity:myschema.mytable.mycol` | `urn:mill/model/attribute:myschema.mytable.mycol` |
| `urn:mill/metadata/entity:model-entity` | `urn:mill/model/model:model-entity` |
| `urn:mill/metadata/entity:concept:<id>` | `urn:mill/model/concept:<id>` |

The group `model` is intentionally broader than `data`: it covers relational catalog entities,
the logical model root, and taxonomy concepts — any entity that participates in the Mill
logical model.

The id segment continues to use lowercase dot-separated physical names. Case rules and
slug encoding are unchanged.

## Scope

### `data/mill-data-metadata`

- **`RelationalMetadataEntityUrns`** — replace single `PREFIX` constant with per-class
  prefix constants (`SCHEMA_PREFIX`, `TABLE_PREFIX`, `ATTRIBUTE_PREFIX`). Rewrite
  `forSchema()`, `forTable()`, `forAttribute()` to use the corresponding prefix.
  Rewrite `parseCatalogPath()` to derive schema/table/column from the URN class segment
  rather than dot-counting: `urn:mill/model/schema:<x>` → `CatalogPath(x, null, null)`;
  `urn:mill/model/table:<x>.<y>` → `CatalogPath(x, y, null)`;
  `urn:mill/model/attribute:<x>.<y>.<z>` → `CatalogPath(x, y, z)`.
- **`SchemaModelRoot.ENTITY_ID`** — change constant to `urn:mill/model/model:model-entity`.
  `ENTITY_LOCAL_ID` stays `model-entity`.
- **`CatalogPath`** — doc update only (no logic change).

### `data/mill-data-schema-core`

- **`DefaultMetadataEntityUrnCodec`** — delegates to `RelationalMetadataEntityUrns`;
  changes follow automatically. Verify `decode()` / `entityUrnMatchesPhysical()` still pass.
- **`SchemaEntityTypeUrns`** — these are facet-manifest `applicableTo` vocabulary URNs,
  **not** instance URNs. Keep as-is in this WI; revisit in WI-144 if `kind` unification
  makes them redundant.

### `metadata/mill-metadata-service`

- **`MetadataEntityIdResolver`** — `extractLocalPart()` hard-codes
  `urn:mill/metadata/entity:` prefix for legacy-key stripping. Generalise: accept any
  `urn:mill/…` URN and return it canonicalised; strip the class-specific prefix only when
  needed for dot-split legacy resolution. The `resolve()` call path uses `codec.forSchema/
  forTable/forAttribute` so it follows the codec automatically.
- **`MetadataUrns.ENTITY_PREFIX`** — this constant is now ambiguous (one prefix per class).
  Replace with `MODEL_ENTITY_SCHEMA_PREFIX`, `MODEL_ENTITY_TABLE_PREFIX`,
  `MODEL_ENTITY_ATTRIBUTE_PREFIX` constants, or remove `ENTITY_PREFIX` in favour of
  delegating to `RelationalMetadataEntityUrns` prefix constants.

### `data/mill-data-metadata` (consumers)

- **`LogicalLayoutMetadataSource`** — currently calls
  `RelationalMetadataEntityUrns.parseCatalogPath(eid)` directly. This still works after the
  codec change (same method, new logic). Verify that the model-root special-case comparison
  against `SchemaModelRoot.ENTITY_ID` uses `MetadataEntityUrn.canonicalize()` on both sides
  (already the case).

### Tests

- All unit tests in `data/mill-data-metadata`, `data/mill-data-schema-core`,
  `metadata/mill-metadata-service` that construct `urn:mill/metadata/entity:…` strings
  must be updated to use the new typed form.
- Add targeted unit tests for `parseCatalogPath()` covering all five class types.

## Out of scope

- Database migration (WI-143).
- YAML dataset updates (WI-143).
- Removing `entity.kind` (WI-144).

## Acceptance criteria

- `RelationalMetadataEntityUrns.forSchema/forTable/forAttribute` produce typed URNs.
- `parseCatalogPath()` correctly decodes all five class types to `CatalogPath`.
- `SchemaModelRoot.ENTITY_ID` is `urn:mill/model/model:model-entity`.
- `MetadataEntityIdResolver.resolve()` passes a URN through canonicalized without stripping.
- `DefaultMetadataEntityUrnCodec` round-trips typed URNs correctly.
- `./gradlew` tests for all touched modules pass.

## Testing

```bash
./gradlew :data:mill-data-metadata:test \
          :data:mill-data-schema-core:test \
          :metadata:mill-metadata-service:test \
          :metadata:mill-metadata-core:test
```

## Commit

One logical `[refactoring]` commit; update `STORY.md`; clean tree.
