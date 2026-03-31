# Design Gaps — metadata-and-ui-improve-and-clean

> **Superseded for execution:** normative text lives in [`SPEC.md`](SPEC.md) (§3h, §3i, and §3 generally). This file remains as a historical checklist snapshot.

This file tracks unresolved or partially resolved design points for the story.
It is intended as a walk-through checklist for refining the architecture before
or during implementation.

## Confirmed decisions

### Repository and source relationship (closed design)

- There is **no** single `MetadataRepository` type in `mill-metadata-core`.
  Persistence is **`EntityRepository`** + **`FacetRepository`** (each composed of
  **`*ReadSide`** + **`*WriteSide`** — see tracking list).
- **`MetadataSource`** is **only** the **per-origin facet read hook** (returns
  **`List<FacetInstance>`** given entity + **`MetadataReadContext`**). It is **not**
  the entity/facet **persisted-catalog** bundle; that optional bundle is
  **`PersistenceCatalogReads`** (`EntityReadSide` + `FacetReadSide`) or equivalent.
- **Writes** stay on **`EntityWriteSide`** / **`FacetWriteSide`** (via full
  repositories). **`MetadataSource`** has **no** mutations.

### Aggregation (`MetadataSource` read merge — not a “repository”)

**Code today:** There is **no** `AggregatingFacetRepository` / `AggregatingMetadataRepository`
in the tree yet (only WI-133 draft). [`MetadataEntityController`](../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt)
uses **`facetRepository`** directly only for **merge-trace** (`findByEntity`) and
**`findByUid`** (write-path guard). **Merged** facet **GET** responses use **`facetService.resolve`**, not an aggregating
**`FacetRepository`**.

**Design shift:** Facet **aggregation** is **read-side only** — merge **`List<FacetInstance>`**
from every registered **`MetadataSource`** (after **`MetadataReadContext`** scope/origin
rules). That is **`MetadataSource`** shaped, **not** **`FacetRepository`** shaped.

- **Merge implementation (dedicated class vs composition):** **Default to composition**
    until **reuse is identified** (second call site or tests that must share the exact
    same merge + muting). **Code today:** **`MetadataSource`** is not in production yet;
    **`FacetService.resolve`** owns merged facets for REST (**`MetadataEntityController`**,
    **`MetadataView`**); **`SchemaFacetServiceImpl`** is the schema tree path — there is
    **no** shared merger type yet, matching “no second consumer” of a dedicated composite.
    **Either** pattern remains valid once reuse appears.
  - **Dedicated small type** (**`CompositeMetadataSource`**, etc.): one place to unit-test
    merge + origin muting; easy to register as a Spring bean; implements
    **`MetadataSource`** if something needs a single “mega-source.”
  - **Composition only:** inject **`List<MetadataSource>`** (or fixed collaborators)
    into **`FacetService`**, **`SchemaFacetServiceImpl`**, or the REST layer and
    **merge inline** there — fine when call sites are few and merge rules stay
    covered by service-level tests. **No obligation** to introduce another public
    type if a service method already owns “resolved facets for entity.”
  - **Normative wording:** see [`metadata-layered-sources-and-ephemeral-facets.md`](../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)
    (**Full population** / implementation note).
- **Writes:** stay on **`FacetWriteSide`** / **`FacetRepository`** (persistence bean)
  **without** pretending reads are “the repository.” A façade may **compose**
  **`CompositeMetadataSource` + `FacetWriteSide`** for a single injectable where
  the **controller** or service needs both — conceptually
  **“aggregating read sides + metadata write side”**, **not**
  **`AggregatingMetadataRepository`** as a full **`FacetRepository` substitute**.
- **Optional legacy pattern:** **`@Primary` `FacetRepository`** whose **read** methods
  synthesize merged **captured + inferred** (WI-133 draft) — **discouraged** as the
  default: it bleeds resolution into every **`FacetRepository`** consumer and
  conflicts with endpoints that need **raw** persisted rows (merge-trace).
  Prefer **explicit** injection of composite read vs persistence read.

### Read context

- `MetadataReadContext` is the contract describing parameters used to retrieve
  metadata.
- It is the read-time "from" container for metadata resolution.
- `MetadataReadContext` is the evolution and rename of the existing
  `MetadataContext`.
- It should carry at least:
  - ordered list of active scopes
  - set/list of active origins
- Origin selection is primarily used to mute origins.
- Semantics:
  - omitted origins => all active
  - empty origins => all active
  - non-empty origins => only listed origins active
- API parameter names:
  - `scope` for scopes
  - `origin` for origins
- Both parameters accept comma-separated URNs or slugs.
- `scope` replaces the current `context` parameter on read APIs; this is a
  UI-breaking API change that must be reflected in OpenAPI and client updates.

## Tracking list

- [x] Define the exact `MetadataSource` interface shape.
  Notes:
  - **Closed.** **`MetadataSource`** = one **pluggable origin** for facet
    resolution. **Minimum contract:**
    - `val originId: String` — stable id (e.g. `"repository-local"`,
      `"mill.schema-provider"`); used for muting and UI attribution.
    - `fun fetchForEntity(entityId: String, context: MetadataReadContext): List<FacetInstance>`
      — returns **unified read-model** facets for this origin only.
      (`contributeForEntity` is an acceptable alias if preferred; semantics are
      **read**, not write.)
    - Implementations receive **`MetadataReadContext`** directly (scopes +
      origins for filtering/muting).
  - **Not** on `MetadataSource`: entity CRUD, loading **`FacetAssignment`** rows by
    themselves (repository-backed adapter uses **`FacetReadSide`** internally).
  - **`data/mill-data-schema-*`:** consumes registered **`List<MetadataSource>`**
    (or merger façade) + persisted readers as needed; merge output is
    **`List<FacetInstance>`** per entity.

- [x] Define the exact `MetadataRepository` interface split.
  Notes:
  - **Closed.** There is **no** type named **`MetadataRepository`** in core.
    Persistence contracts:
    - **`MetadataEntityRepository` → `EntityRepository`** = **`EntityReadSide`**
      + **`EntityWriteSide`** (find/exists vs save/delete).
    - **`FacetRepository`** = **`FacetReadSide`** + **`FacetWriteSide`** (today’s
      query vs mutation methods map 1:1 onto those sides).
    - **Domain persistable facet row** in core: **`FacetAssignment`** (working
      name); JPA internal **`JpaFacetInstance`** — **not** on the repository
      interface.
    - **`FacetInstance`** = **unified read model** (captured + inferred) with
      `origin`, `originId`, `assignmentUid`; **not** the JPA entity.
    - Optional **read-only** bundle for stored catalog: **`PersistenceCatalogReads`**
      (`EntityReadSide` + `FacetReadSide`) — **do not** overload **`MetadataSource`**
      for this unless WI-132’s hook is renamed.
    - **Facet-type** repos may follow the same **`FacetTypeDefinitionReadSide` /
      `FacetTypeDefinitionWriteSide`**, **`FacetTypeReadSide` /
      `FacetTypeWriteSide`** pattern in a follow-on refactor.

- [x] Document aggregation façade (supersedes vague “`AggregatingMetadataRepository`”).
  Notes:
  - **Closed.** Aggregation = **read merge of `MetadataSource` only** → prefer
    **`CompositeMetadataSource` / `AggregatingMetadataSource`**, not a type named
    `AggregatingMetadataRepository` that implements **`FacetRepository`**.
  - **Inject touchpoint:** REST **entity** controller / **`FacetService`** resolution
    path — today already **`facetService.resolve`** for merged reads; extend with
    inferred sources there or via composite **`MetadataSource`**, not by wrapping
    every **`FacetRepository`** call site.
  - **Write path:** unchanging **`FacetWriteSide`** / persistence **`FacetRepository`**.
  - **User sketch:** *aggregating read sides* + *metadata write side* in one façade is
    OK for a **narrow adapter**; name it to reflect **read merge** (`*MetadataSource`)
    **+** **write delegate**, not “repository” for both.

- [x] Define the exact `MetadataReadContext` shape.
  Notes:
  - `scopes`
  - `origins`
  - parsing from endpoint query params
  - normalization rules
  - whether origins should preserve order or just set semantics

- [x] Define the exact semantics of origin muting/filtering.
  Notes:
  - parameter name on APIs: `origin`
  - matching is by `originId`
  - both `scope` and `origin` accept slugs or URNs
  - omitted/empty means all active

- [x] Define the stable URN model for the top-level `model` entity.
  Notes:
  - exact URN shape: `urn:mill/metadata/entity:model-entity`
  - entity kind: `model`
  - fixed stable top-level metadata root for the schema/model view

- [x] Define entity attachment rules for inferred backend facets.
  Notes:
  - **Closed:** There is no story-wide prescriptive attachment matrix. Per
    [`SPEC.md` §3f](SPEC.md), `model` is a top-level metadata-only aggregate;
    inferred and captured facets attach as facet types and sources allow (e.g.
    description on `model` is fine). Each `MetadataSource` / WI may project
    facets onto the entities that make sense for that source (WI-138/139 stay
    illustrative, not normative placement rules).
  - **Story requirements** (implementation, mainly WI-137 + WI-136): `model`
    appears as a node in the Data Model explorer UI; `model` is returned as a
    first-class entity from schema explorer APIs (e.g. `SchemaExplorerService`);
    `model` is included as an `assignableTo` target in the UI and all relevant
    components (facet manifests, pickers, authoring flows) alongside other
    entity kinds.
  - Duplicate inferred facet **type** for one entity still signals
    misconfiguration (see precedence item above), not something to “merge away.”

- [x] Define the precedence matrix for multiple origins and facet types.
  Notes:
  - inferred subsystems should emit their own facet types
  - duplicate inferred facet types for the same entity indicate misconfiguration or miswiring
  - combining logic should stay simple: collect contributions for an entity into one list/set of facets
  - origins do not have any priority ordering
  - do not introduce field-level merge for inferred sources

- [x] Define resolved API contract updates.
  Notes:
  - one unified resolved facet DTO for all origins
  - no origin-specific DTO variants
  - `origin`
  - `originId`
  - `assignmentUid`
  - origin filter support

- [x] Define UI behavior for the `model` node and origin-labeled resolved facets.
  Notes:
  - tree placement
  - detail panel behavior
  - captured facets stay unchanged in the UI
  - inferred facets replace edit/delete controls with a pill showing `originId`

- [x] Define backend rollout scope.
  Notes:
  - **Closed:** First inferred-facet implementation targets the **Mill logical
    model** from [`SchemaProvider`](../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/SchemaProvider.java)
    (`proto` `Schema` / `Table` / `Field`). Any backend that exposes a
    `SchemaProvider` participates without a separate per-SKU rollout for that
    layer.
  - **Deferred (follow-up story, TBD):** Flow-, JDBC-, or other **physical-source**
    inferred facets (paths, storage, JDBC URLs, credentials-adjacent data).
    Former WI-139-style work moves with that story.

- [x] Define safety rules for physical-source exposure.
  Notes:
  - **Closed:** There is **no** single global Mill-wide safety checklist in this
    story. Rules are **per concrete physical `MetadataSource` / backend**
    (what may appear in payloads: URL redaction, path exposure, secrets, etc.)
    and are **authored together** with that backend’s inferred-facet work in the
    **follow-up story** where that source ships. This story’s SchemaProvider-only
    logical facets do not add a new physical exposure surface.
