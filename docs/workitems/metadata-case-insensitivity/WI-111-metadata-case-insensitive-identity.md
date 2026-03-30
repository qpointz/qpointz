# WI-111 — Metadata case-insensitive identity (entity coordinates + persistence)

Status: **`superseded`** for story **metadata identity / URN platform** (see [`STORY.md`](./STORY.md) **Locked decisions**).  
Type: `🐛 fix` / `✨ feature` (correctness + contract) — historical  
Area: `metadata`, `data` (`mill-data-schema-core`, `mill-data-schema-service`)  
Backlog ref: `M-28` (see **BACKLOG** — may be closed as superseded by **M-29**)  
Story: [`STORY.md`](./STORY.md)

## Supersession (locked)

Product choice **(b) greenfield:** no coordinate duplicate-merge migration. **WI-112**+ define **URN** identity and canonicalisation; **WI-115** drops coordinate columns on fresh DDL. **Do not** implement WI-111 merge migration on this branch.

**Code documentation:** **N/A** for this WI on the URN story branch (superseded). If anyone revives coordinate-based work elsewhere, apply [`STORY.md`](./STORY.md) **Code documentation** rules there.

## Coordination (historical)

This WI described the **legacy coordinate model**. The URN story **replaces** that approach for new work.

## Goal

Enforce **one canonical metadata entity** per logical schema/table/attribute regardless of identifier casing; align JPA, REST, and schema-explorer code paths so mixed case cannot create duplicate `metadata_entity` / `metadata_facet` chains.

**Deliverables:**

- Idempotent reads/writes for the same logical entity under differing casing.
- Data migration merging duplicates (survivor `entity_id`, re-point `metadata_facet`, delete losers).
- JPA IT + controller + `SchemaFacetServiceImpl` / `SchemaExplorerService` tests for casing.

## In scope

- Case-insensitive uniqueness and lookup for `entity_res` and `(schema_name, table_name, attribute_name)`.
- Migration to merge existing case-only duplicates.
- `MetadataEntityController`, `JpaMetadataRepository`, `SchemaFacetServiceImpl`, `SchemaExplorerService` behaviour and tests.
- Design note under `docs/design/metadata/` for canonical casing and API contract.

## Out of scope (phase 1)

- Case-insensitive equality for **all** arbitrary `urn:mill/...` strings (facet-type suffixes, opaque ids) unless explicitly listed in the design note as follow-up.

## Problem

Observed duplicate rows for the same logical object, e.g. `TABLE,moneta,accounts` vs `TABLE,MONETA,ACCOUNTS`, and multiple `metadata_facet` rows attached to different surrogate `entity_id` values. Today:

1. **JPA / DB:** `metadata_entity` uses `uq_metadata_entity_location` on `(schema_name, table_name, attribute_name)` with default **case-sensitive** string equality (typical for PostgreSQL/H2). Distinct casing ⇒ distinct rows.
2. **`entity_res`:** Lookups use exact match (`findByEntityRes`, JPQL `e.entityRes = :entityRes`). `moneta.accounts` and `MONETA.ACCOUNTS` are different keys.
3. **Schema facet join (partial fix already):** `SchemaFacetServiceImpl` matches metadata coordinates to the physical schema with `coordinateEquals(..., ignoreCase = true)` — good — but **`getTable` / `getColumn`** still use **case-sensitive** `==` on table/column names from the path vs proto names, so API behaviour can disagree with entity matching.
4. **Metadata REST seed path:** `MetadataEntityController.existsInPhysicalSchema` uses **case-sensitive** `it.name == tableName` / column checks against `entityId` segments, while physical JDBC names are often uppercased. That can cause **404** on facet writes for one casing and success for another, encouraging duplicate inserts.
5. **Schema explorer list:** `SchemaExplorerService.listSchemas` builds `entitiesBySchema` with `associateBy { it.schemaName!! }` (**case-sensitive** keys), so schema-level metadata can fail to attach when path casing differs from stored metadata casing.

**URN namespaces (facet type, scope, entity type):** `MetadataUrns` normalises short keys to full URNs but does not define case rules for arbitrary suffixes (e.g. custom facet types, `user:` scopes). This WI should **explicitly scope** which identifiers are CI:

- **In scope (recommended phase 1):** Physical-coordinate identity — `schemaName`, `tableName`, `attributeName` / dot-separated `MetadataEntity.id` used as `entity_res` for SCHEMA/TABLE/ATTRIBUTE entities.
- **Out of scope or follow-up:** Changing equality rules for every `urn:mill/...` string globally (risky for opaque IDs); document whether facet-type and scope URNs are compared case-insensitively or remain exact.

## Proposed design (for implementation)

### 1. Canonical form (product decision)

Pick one and document in `docs/design/metadata/`:

- **Option A (simplest):** Normalise coordinates to **lower case** on every write (and in migration). Matches common unquoted SQL identifier behaviour; may be wrong for case-sensitive catalogues — document limitation.
- **Option B:** Normalise to **physical schema spelling** (first match from `SchemaProvider` when seeding); harder but preserves display casing.

### 2. Database uniqueness

Replace or supplement `uq_metadata_entity_location` so uniqueness is **case-insensitive**:

- PostgreSQL: unique index on `(lower(schema_name), lower(table_name), lower(attribute_name))` with NULL-safe expression (use `coalesce` sentinels or partial indexes per entity type if needed).
- H2 (tests): equivalent functional unique constraint if supported; otherwise enforce in service layer + test-only constraint.

Align `entity_res` uniqueness: either store canonical `entity_res` only, or add `entity_res_normalised` + unique index on `lower(entity_res)`.

### 3. Application layer

- **`JpaMetadataRepository.save`:** Before insert/update, resolve existing row by **normalised** location and/or `entity_res`; merge into survivor id.
- **`MetadataEntityJpaRepository`:** Add `@Query` finders using `lower()` for location and `entity_res` (or use normalised columns).
- **`MetadataEntityController`:** Refactor `existsInPhysicalSchema` / `toPhysicalMetadataEntity` to use the same **case-insensitive** coordinate matching as `SchemaFacetServiceImpl` (shared helper in `mill-metadata-core` or `mill-data-schema-core` to avoid drift).
- **`SchemaFacetServiceImpl`:** Change `getTable` / `getColumn` (and `getSchema` if needed) to use `coordinateEquals` for nested lookups, not raw `==`.
- **`SchemaExplorerService.listSchemas`:** Resolve schema-level metadata with case-insensitive keying (e.g. map by `lowercase` or linear scan with `coordinateEquals`).

### 4. URN / API contract

- Document that **entity path ids** in metadata API (`entityId` dot notation) are **case-insensitive**; responses should return **canonical** casing (per decision above).
- Optional: normalise incoming path variables in controllers once, before service calls.

## Modules / paths

| Area | Module / file |
|------|----------------|
| Domain + rules | `metadata/mill-metadata-core` — optional `MetadataEntityIds` helper |
| JPA entities | `metadata/mill-metadata-persistence/.../MetadataEntityRecord.kt` |
| Repositories | `MetadataEntityJpaRepository.kt`, `MetadataFacetJpaRepository.kt` (queries) |
| Adapter | `JpaMetadataRepository.kt` |
| REST | `metadata/mill-metadata-service/.../MetadataEntityController.kt` |
| Schema join | `data/mill-data-schema-core/.../SchemaFacetServiceImpl.kt` |
| REST explorer | `data/mill-data-schema-service/.../SchemaExplorerService.kt` |
| Import | `metadata/mill-metadata-service/.../DefaultMetadataImportService.kt` (entity ids from YAML) |

## Acceptance criteria

- No two `metadata_entity` rows differ only by case in `(schema_name, table_name, attribute_name)` after migration + new writes.
- Integration test: create entity via API with mixed case; second write with different case updates same row / facets.
- `SchemaFacetServiceImpl` IT or unit test: `getTable` finds table when path casing differs from proto.
- Documentation: short design note under `docs/design/metadata/` stating CI rules and canonical casing.

## Dependencies / risks

- **Migration complexity:** Merging facet rows must preserve `facet_uid` where clients reference it; handle conflicts (two facets same type/scope on duplicate entities).
- **Multi-DB:** CI unique indexes differ by dialect; abstract in migrations per profile.
- **Quoted identifiers:** If the platform later supports case-sensitive SQL identifiers, CI metadata keys may need a separate story.
