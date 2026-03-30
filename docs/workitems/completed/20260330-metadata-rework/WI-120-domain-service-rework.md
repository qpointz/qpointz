# WI-120 — Domain + Service Rework

**Story:** Metadata Rework
**Spec sections:** §4, §5, §6, §7, §15
**Depends on:** WI-119

## Objective

Rewrite `mill-metadata-core` as a pure module: domain types with no coordinates, repository
interfaces with no framework imports, service implementations as plain constructor-injected
classes. **No** Spring/JPA in core. **No** `MetadataAuditRepository` on services — audit is listener-only (SPEC §6.5). Introduce **`MetadataView`** (repos + `MetadataContext` → effective read model) for **`data/*`** read paths per SPEC §5.6. File **I/O** adapters stay in `mill-metadata-persistence` (canonical YAML per SPEC §15).

## Scope

### 1. URN canonicalisation in `MetadataEntityUrn`

`MetadataEntityUrn.canonicalize(urn: String)` must:
- Lowercase the entire URN string.
- Trim leading/trailing whitespace.
- Reject any input that does not begin with `urn:mill/` (after normalisation) — throw a
  descriptive `IllegalArgumentException`.

All service methods that accept a URN or id argument call `canonicalize` before passing to
a repository. This is the **only** normalisation needed — repositories store and query lowercase
values and so can use plain equality queries.

### 3. `mill-metadata-core` purity pass

Ensure build file declares only `mill-core` as a dependency.
No `spring-*`, `jakarta.persistence.*`, or `jackson-*` direct imports anywhere in
`mill-metadata-core`. CI check: `./gradlew :metadata:mill-metadata-core:dependencies` shows
no framework artifacts.

### 4. `MetadataEntity` — strip coordinates (SPEC §5.3)

```kotlin
data class MetadataEntity(
    val id: String,           // full URN
    val kind: String?,        // opaque; null = untyped
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
```

Remove: `schemaName`, `tableName`, `attributeName`, `facets: MutableMap<...>`, any `type: MetadataType` field.

### 5. Remove `MetadataType` and coordinate helpers (SPEC §5.7)

Delete from `mill-metadata-core`:
- `MetadataType` enum (SCHEMA, TABLE, ATTRIBUTE, CONCEPT, CATALOG)
- `MetadataEntityUrn.forSchema/forTable/forAttribute/forConcept`
- `MetadataEntityIds`
- `EntityReference` (coordinate-typed)
- `RelationFacet` (typed) — move to `mill-data-schema-core`
- `StructuralFacet` — move to `mill-data-schema-core`
- `MetadataFacetInstanceRow` — replaced by `FacetInstance`

Keep in `mill-metadata-core`:
- `MetadataEntityUrn.canonicalize(urn: String): String` — generic normaliser only.

### 6. `FacetInstance` domain type (SPEC §5.4)

Introduce `FacetInstance` (from WI-119 stub) as the primary facet row domain type.
`uid` is generated on first `save` if absent (UUID). Immutable thereafter.
Include **`mergeAction`** (`SET` \| `TOMBSTONE` \| `CLEAR`) — persisted in **`metadata_facet.merge_action`** (WI-122).

### 7. Repository interfaces (SPEC §6)

Ensure all four interfaces exist in `mill-metadata-core` with no framework annotations:

- `MetadataEntityRepository` — `findByKind(kind: String)` not `findByType(MetadataType)`.
- `FacetRepository` — operates on `FacetInstance`; no coordinate queries.
- `FacetTypeRepository` — `findByKey`, `save`, `delete` by URN.
- `MetadataScopeRepository`, `MetadataAuditRepository` — unchanged contracts (SPEC §6.5).
- `MetadataSeedLedgerRepository` — added in SPEC §6.6 (**WI-126** wires JPA impl).

NoOp implementations for all four in `mill-metadata-core`:
`NoOpMetadataEntityRepository`, `NoOpFacetRepository`, `NoOpFacetTypeRepository`,
`NoOpMetadataScopeRepository`.

### 8. Service interfaces + implementations (SPEC §7)

All in `mill-metadata-core`, no Spring annotations:

**`MetadataEntityService` / `DefaultMetadataEntityService`:**
- `findByKind(kind: String)` replaces `findByType(MetadataType)`.
- `create` validates URN form; stamps `createdAt`/`lastModifiedAt`; delegates to repository.
- `delete` cascades to facets via `FacetRepository.deleteByEntity`.

**`FacetService` / `DefaultFacetService`** (names per SPEC §7.2):
- `resolve` / `resolveByType` — **caller-ordered** `MetadataContext`; last-wins; interpret **`merge_action`** via a dedicated **repository-agnostic** collaborator (**`MetadataReader`** or equivalent) — **not** inside `JpaFacetRepository`.
- `assign`, `update`, `unassign`, `unassignAll` — SINGLE vs MULTIPLE per SPEC.

**`MetadataReader` (or equivalent):** centralises merge semantics for stored assignments; repositories load/save rows only (SPEC §5.6).

**`FacetCatalog` / `DefaultFacetCatalog`:**
- Uses `JsonUtils.defaultJsonMapper()` from `mill-core` for schema validation.
- No Spring annotations.

**`MetadataScopeService` / `DefaultMetadataScopeService`** — plain class, no annotations.

**`MetadataImportService`** — uses `YamlUtils.defaultYamlMapper()`. Validates entity ids are
full URNs. Delegates to `MetadataEntityService` + `FacetService`.

**Removed:** `MetadataService` (monolith dissolved); `MetadataEditService` (dissolved);
`findRelatedEntities` (coordinate traversal; data-layer concern).

### 9. Legacy file adapters

Delete **legacy** file repository code from **`mill-metadata-persistence`** that implements the **old** on-disk format (details in **WI-124**). **`mill-metadata-core`** carries only serializers/snapshot services — no filesystem I/O.

### 10. `MetadataYamlSerializer` + `MetadataSnapshotService` (SPEC §15.4–15.5)

Both live in `mill-metadata-core` (pure domain types + `YamlUtils`; no Spring, no JPA).

**`MetadataYamlSerializer`** — static utility:
- `serialize(scopes, definitions, entities, facetsByEntity): String` — produces canonical multi-document YAML.
- `deserialize(yaml: String): MetadataYamlDocument` — parses multi-document YAML; lowercases URNs; generates stable uids for assignments that omit them.
- Unknown `kind:` values are skipped.

**`MetadataSnapshotService` / `DefaultMetadataSnapshotService`** — plain class, constructor-injected repositories:
- `snapshotAll(out: Writer)` — full export of all entities, facets, definitions, and scopes.
- `snapshotEntities(entityIds, out)` — partial export; pulls only referenced definitions and scopes.
- KDoc on interface, all methods, and parameters.

### 11. KDoc coverage

All new and modified types, interfaces, and methods in `mill-metadata-core` must carry
KDoc to method + parameter level. NoOp stubs: at minimum a one-line KDoc on the class.

## Done Criteria

- `./gradlew :metadata:mill-metadata-core:dependencies` shows no `spring-*`, `jakarta.*`, or `jackson-*` direct artifacts.
- `MetadataEntity` has no coordinate fields.
- `MetadataType` enum deleted.
- `FacetInstance` exists; `MetadataFacetInstanceRow` deleted.
- All core **repository** NoOp stubs exist (entity, facet, facet type, scope per SPEC §4.4).
- Service implementations compile with constructor injection only.
- `MetadataEntityUrn.canonicalize` lowercases the full URN and rejects non-`urn:mill/` input.
- All service methods canonicalise incoming URN/id arguments before passing to repositories.
- All new/modified code has KDoc.
- Unit tests pass (`./gradlew :metadata:mill-metadata-core:test`).
