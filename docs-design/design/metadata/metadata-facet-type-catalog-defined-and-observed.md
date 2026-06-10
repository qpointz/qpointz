# Facet type catalog UI: DEFINED and OBSERVED types

**Status:** design / backlog  
**Tracked:** `docs/workitems/BACKLOG.md` — **M-32**  
**Domain reference:** [`FacetTypeSource`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/FacetTypeSource.kt) (`DEFINED` vs `OBSERVED`), [`FacetType`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/FacetType.kt) runtime row SPEC §5.5.

## Problem

Runtime facet types have two origins:

| Source | Meaning |
|--------|---------|
| **DEFINED** | A [`FacetTypeDefinition`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/FacetTypeDefinition.kt) exists; descriptor-driven editors apply. |
| **OBSERVED** | No definition registered; runtime row created when facet **assignments** reference a type key that was not yet in the catalog (see [`DefaultFacetService`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/service/DefaultFacetService.kt)). |

The metadata REST **list facet types** path is implemented by [`FacetTypeManagementService.list`](../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/service/FacetTypeManagementService.kt), which currently uses **`facetCatalog.listDefinitions()`** only. That surface is **DEFINED-centric**: **OBSERVED** types that exist only in `metadata_facet_type` (via [`FacetTypeRepository`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/repository/FacetTypeRepository.kt), including [`findObserved`](../../../metadata/mill-metadata-persistence/src/main/kotlin/io/qpointz/mill/persistence/metadata/jpa/adapters/JpaRuntimeFacetTypeRepository.kt)) do **not** appear in the Mill UI **facet type** admin list ([`FacetTypesListPage`](../../../ui/mill-ui/src/components/admin/model/FacetTypesListPage.tsx) → `facetTypeService.list`).

Operators and metadata capture workflows need to **see the full catalog** in one place: both **defined** and **observed** type keys, so unexpected or AI-assisted captures show up and can be promoted to full definitions when appropriate.

## Goal

- **Facet type view** (admin catalog): show **union** of all facet types relevant to the deployment — at minimum **every DEFINED** manifest **plus** **every OBSERVED** runtime key not already covered by a definition.
- **Clear labeling** per row: `DEFINED` vs `OBSERVED` (and optional badges: mandatory, enabled — where applicable).
- **Behavior**:
  - **DEFINED** rows: today’s behavior (open descriptor editor, CRUD per policy).
  - **OBSERVED** rows: **visible**; editing may be **restricted** (e.g. read-only detail, “promote to defined” flow, or JSON-only stub) — product choice, but **visibility** is the first deliverable.

## API / implementation sketch (non-binding)

- Extend list endpoint (`GET /api/v1/metadata/facets`) or add a query flag (e.g. `includeObserved=true`) to merge **`FacetTypeRepository.findObserved()`** (keys without a definition) into the response with a **`source`** / **`facetTypeSource`** field on [`FacetTypeManifest`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/facet/FacetTypeManifest.kt) or a parallel DTO.
- Ensure **deduplication**: DEFINED wins when both exist for the same canonical type key.
- OpenAPI and `mill-ui` types updated; list filters (e.g. “Defined only”, “Observed only”) optional.

## Acceptance

- With at least one OBSERVED-only facet type in the database, the facet type admin page lists it alongside defined types, with source indicated.
- No regression: all DEFINED types still listed as today (subject to existing `enabledOnly` / `targetType` filters).

## Relation to M-31

**M-31** (layered `MetadataSource`, ephemeral **facet instances**) is about **read-time** merged **assignments**. **M-32** is about **facet type registry** visibility (DEFINED vs OBSERVED **type keys**). They complement each other for metadata capture observability but are separate deliverables.
