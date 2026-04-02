# Layered metadata sources and inferred facets

**Status:** as-built (story `metadata-and-ui-improve-and-clean`, April 2026)  
**Related backlog:** `docs/workitems/BACKLOG.md` — **M-31** (delivered); **M-32** (facet type catalog DEFINED/OBSERVED follow-up)

## Objective

Schema-bound metadata visible to users and APIs is the **combination** of:

1. **Captured facets** — persisted as **`FacetAssignment`** rows; editable through metadata services and UI when permitted. The read model uses **`FacetInstance`** with **`FacetOrigin.CAPTURED`** and a stable **`assignmentUid`** / **`uid`** tied to storage.
2. **Inferred facets** — produced at **read time** by **`MetadataSource`** implementations (for example logical layout from **`SchemaProvider`**). They are **not** stored as assignments and **must not** be created, updated, or deleted through metadata mutation APIs.

Aggregation for metadata REST is implemented in **`mill-metadata-core`** via **`FacetInstanceReadMerge`**, invoked from **`DefaultFacetService.resolve`**. Schema explorer integration continues to use **`SchemaFacetService`** in **`mill-data-schema-core`** for physical-schema trees; inferred structural facets from **`LogicalLayoutMetadataSource`** ( **`mill-data-metadata`**) align entity coordinates with that catalog.

## Core contracts

### `MetadataSource` (read-only)

- **`MetadataSource`** (`mill-metadata-core`, package `io.qpointz.mill.metadata.source`) is a **readonly** contract per contributing source: **`fetchForEntity(entityId, MetadataReadContext) -> List<FacetInstance>`**.
- Each source exposes a stable **`originId`** string (see **`MetadataOriginIds`**) used for attribution, debugging, and optional **origin filtering** on reads.
- **No** save/update/delete on this interface. Writes remain on **`FacetRepository`** (**`FacetReadSide`** + **`FacetWriteSide`**) / **`FacetService`** / REST.

### `FacetOrigin` and `FacetInstance`

- **`FacetOrigin.CAPTURED`** — row backed by **`FacetAssignment`** (repository source).
- **`FacetOrigin.INFERRED`** — synthetic read-time row; **`assignmentUid`** is null; mutations must be rejected.

**`FacetInstance`** is the **unified read DTO** (origin, **`originId`**, payload, scope, timestamps, etc.). The persisted store uses **`FacetAssignment`**; **`RepositoryMetadataSource`** maps assignments to **`FacetInstance`** with **`FacetOrigin.CAPTURED`**.

### `MetadataReadContext`

- Carries an **ordered scope list** (last wins for duplicate facet types per merge rules) and optional **origin allow-list**.
- **`MetadataReadContext.parse(scopeParam, originParam)`** parses HTTP query parameters: comma-separated **`scope`**, comma-separated **`origin`** (when present, only those **`MetadataSource.originId`** values contribute). Legacy **`context`** is accepted as an alias for **`scope`** when **`scope`** is absent.

### Registered sources (typical deployment)

| Source | Module | `originId` (typical) | Role |
|--------|--------|----------------------|------|
| **`RepositoryMetadataSource`** | `mill-metadata-core` | `MetadataOriginIds.REPOSITORY_LOCAL` | Loads persisted **`FacetAssignment`** rows as captured facets (backed by **`FacetReadSide`** in read paths). |
| **`LogicalLayoutMetadataSource`** | `mill-data-metadata` | `MetadataOriginIds.LOGICAL_LAYOUT` | Infers structural / descriptive facets from **logical** **`SchemaProvider`** catalog, including the **`model`** root entity summary (WI-137 / WI-138). |
| **Flow descriptor `MetadataSource`** | `mill-data-backends` + **`FlowDescriptorMetadataSourceAutoConfiguration`** in **`mill-data-autoconfigure`** (`io.qpointz.mill.autoconfigure.data.backend.flow`) | `MetadataOriginIds.FLOW` (`flow`) | **Backend-specific** inferred facets from flow YAML descriptors (storage, table inputs, column binding hints). See [`backend-provided-metadata.md`](backend-provided-metadata.md) and archived story **[`SPEC.md`](../workitems/completed/20260402-flow-source-ui-facets/SPEC.md)**. |

**`LogicalLayoutMetadataSourceAutoConfiguration`** (in **`mill-data-autoconfigure`**, package **`data.schema`**) registers **`LogicalLayoutMetadataSource`** when **`SchemaProvider`** is available.

**Flow** metadata registration is **not** in that class: it uses **`mill.data.backend.type=flow`**, **`mill.data.backend.flow.metadata.enabled`**, and lives beside **`FlowBackendAutoConfiguration`** so wiring stays backend-specific.

## Merge behaviour

- **`FacetInstanceReadMerge`** composes all Spring **`MetadataSource`** beans, applies **`MetadataReadContext`** scope ordering and optional origin muting, and merges duplicate facet types (composition-first; captured contributions win over inferred when both exist for the same facet type — see unit tests **`FacetInstanceReadMergeTest`** and **SPEC** in the story folder).
- No separate public **`CompositeMetadataSource`** type; merge is centralized in **`FacetInstanceReadMerge`** + **`DefaultFacetService`**.

## REST API

**`MetadataEntityController`** (OpenAPI tag **`metadata-entities`**):

- Read endpoints support **`?scope=`** (comma-separated scope URNs) and **`?origin=`** (optional filter by contributing source id). **`FacetInstanceDto`** (not legacy orphan DTO names) exposes **`origin`**, **`originId`**, **`editable`**, **`assignmentUid`**, and payload for clients.
- Writes validate targets: updates/deletes against **inferred** uids are rejected (**422** / domain rules per **SPEC**).

## UI

- **Mill UI** Data Model shows a **full constellation** of facets where the product surfaces them; **edit** actions apply only when the row is **captured** and the principal may mutate metadata (**`editable`** on DTO).

## Mutation guards

- Inferred contributions are **never** persisted via **`FacetRepository`**.
- **`FacetService`** / REST reject operations that target inferred-only or synthetic uids.

## Related documents

- [`backend-provided-metadata.md`](backend-provided-metadata.md) — purpose of backend-specific inferred facets, flow configuration, Spring placement.
- [`facet-class-elimination.md`](facet-class-elimination.md) — demotion of Kotlin facet classes; **`FacetPayloadUtils`** for typed payload helpers.
- [`mill-metadata-domain-model.md`](mill-metadata-domain-model.md) — URNs, entities, assignments.
