# Spec: metadata-and-ui-improve-and-clean

**Story branch:** `fix/metadata-and-ui-improve-and-clean`
**Design docs:**
- [`docs/design/metadata/facet-class-elimination.md`](../../design/metadata/facet-class-elimination.md)
- [`docs/design/metadata/metadata-layered-sources-and-ephemeral-facets.md`](../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)

---

## 1. Metadata Cleanup — Dead Code Removal

Remove classes with zero production consumers (see WI-130 for full inventory).

**Status:** WI-130 defined, pending execution.

---

## 2. Facet Class Demotion

Eliminate the `MetadataFacet` / `AbstractFacet` lifecycle layer.

### 2a. Demote concrete facet classes to plain data containers

Remove `merge()`, `validate()`, `setOwner()` from:
- `MetadataFacet` interface + `AbstractFacet` base class
- `DescriptiveFacet`, `ConceptFacet`, `ValueMappingFacet` — become plain `data class`
- `TableLocator`, `TableType`, `ConceptTarget` — keep as-is (no lifecycle to remove)

### 2b. Remove bridge infrastructure

- `FacetClassResolver` interface + `DefaultFacetClassResolver`
- `FacetConverter` (Jackson `convertValue` bridge)
- Optional replacement: `FacetPayloadUtils.convert<T>(payload, clazz)` inline helper

### 2c. Update consumer call-sites

| Area | Files |
|------|-------|
| `data/mill-data-schema-core` | `SchemaFacetServiceImpl`, `SchemaFacets` |
| `data/mill-data-schema-service` | `SchemaExplorerService` |
| `ai/mill-ai-v3` | `SchemaToolHandlers`, `SchemaAuthoringCapability` |
| `ai/mill-ai-v3-cli` | `DemoSchemaFacetService` |
| `ai/mill-ai-v1-core` | `SchemaMessageSpec`, `ValueMappingComponents`, `NlsqlMetadataFacets` |
| `ai/mill-ai-v1-nlsql-chat-service` | `ChatProcessor` |
| `metadata/mill-metadata-autoconfigure` | `MetadataCoreConfiguration` |

---

## 3. Layered Metadata Sources and Ephemeral Facets

**Backlog:** M-31 | **Design doc:** [`metadata-layered-sources-and-ephemeral-facets.md`](../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)

Full schema-bound metadata must be the **combination** of:
1. **Captured facets** — persisted `FacetInstance` rows, editable via metadata services and UI.
2. **Ephemeral / system facets** — derived at read time (physical schema, authorization/policy). Never stored, never mutated through metadata APIs.

### 3a. `MetadataSource` contract

- Define `MetadataSource` read-only interface + `FacetContribution` DTO + `FacetProvenanceKind` enum (`CAPTURED`, `EPHEMERAL`, `SYSTEM`).
- `RepositoryMetadataSource` adapter — wraps existing `FacetRepository` / `MetadataReader` reads.
- Module placement TBD (see Open Questions).

### 3b. Merge in `SchemaFacetServiceImpl`

- Aggregate all registered `MetadataSource` contributions per coordinate.
- Document precedence rules per facet type (structural = physical wins; descriptive/relation/concept = captured; authorization = system-only).
- Expose `facetsResolved` list shape on `SchemaFacets` / `*WithFacets` with per-instance `provenance`, `editable`, `assignmentUid`.

### 3c. Read API + OpenAPI

- List endpoint returning resolved facets with instance-level provenance fields.
- OpenAPI shape updated; regenerate UI client stubs.

### 3d. Mutation guards

- `FacetService` / REST update & delete reject targets that are not real persisted assignments (provenance check / synthetic uid guard).

### 3e. UI: full constellation view

- Data Model / schema explorer shows **all** effective facets (captured + ephemeral/system).
- Edit/delete/create chrome visible only for `editable: true` rows.
- Ephemeral rows rendered read-only in place.

---

## 4. UI Improvements

> **TODO:** Describe the UI work here.
>
> Possible areas (fill in or strike):
> - Facet viewer / editor improvements (`FacetViewer.tsx`, `EntityDetails.tsx`)
> - Concepts UI (`ConceptsView.tsx`, `ConceptsSidebar.tsx`)
> - Data-model layout polish (`MetadataLayout.tsx`, `MetadataSidebar.tsx`)
> - BACKLOG U-12: per-facet-type view/edit component registration
> - Other:

---

## 5. Open Questions

> **TODO:** Resolve before WIs are written.

- [ ] Keep `DescriptiveFacet` etc. in `mill-metadata-core` or move to a new `mill-metadata-types` module?
- [ ] Introduce `FacetPayloadUtils` helper or let consumers do raw Jackson inline?
- [ ] Split `data/` and `ai/` consumer updates into separate WIs or one WI per consumer group?
- [ ] Module placement of `MetadataSource` interface: `mill-metadata-core` vs `mill-data-schema-core`?
- [ ] Overlap rule when two sources supply the same facet type for one coordinate: non-overlapping ownership by facet type, or field-level merge?
- [ ] Scope of UI improvements (section 4)?

---

## 6. Out of Scope

- `FacetPayloadFieldJsonSerde` — classes inside are live (used via Jackson annotations). Do not remove.
- `FacetTypeManifestInvalidException` — used by `FacetTypeManifestNormalizer`. Keep.
- M-32 facet type catalog (DEFINED vs OBSERVED types) — separate story.
