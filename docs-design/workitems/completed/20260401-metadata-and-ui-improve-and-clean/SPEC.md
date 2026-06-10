# Spec: metadata-and-ui-improve-and-clean

**Story branch:** `fix/metadata-and-ui-improve-and-clean`  
**Normative contract for this story** — implementation work items are defined in `WI-*.md` and must stay consistent with this document.

**Design docs (narrative / diagrams):**

- [`docs/design/metadata/facet-class-elimination.md`](../../../design/metadata/facet-class-elimination.md)
- [`docs/design/metadata/metadata-layered-sources-and-ephemeral-facets.md`](../../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)

**Historical checklist:** [`DESIGN-GAPS.md`](DESIGN-GAPS.md) — decisions are absorbed into this spec (§3h, §3i, and §3 generally). Prefer this file for execution.

---

## 0. Implementation standards (all code WIs)

- **Languages:** **Kotlin** preferred; **Java** where the module already uses Java. New `@ConfigurationProperties` in autoconfigure modules: **Java** (so `spring-boot-configuration-processor` generates metadata) **or** **Kotlin** with hand-maintained `META-INF/additional-spring-configuration-metadata.json` — never Kotlin property classes without that metadata file.
- **Java / Kotlin:** **JavaDoc / KDoc** on new or materially changed production code — **through class, method, and parameter level** (types, services, configuration, controllers, DTOs). Tests exempt unless documenting a public test helper.
- **TypeScript / React (`.tsx`):** document **down to function level** for exported functions and non-trivial components or hooks; follow existing UI patterns.
- **OpenAPI:** Any **materially changed or new** HTTP endpoint, query parameter, request body, or response body in scope for this story must be reflected in the **published OpenAPI** for that service (Springdoc / annotations / YAML — whichever the module uses), including **error responses** when behaviour changes. Reviews treat missing or stale OpenAPI as **incomplete** API work. (Hands-on application is concentrated in **WI-134** and any WI that adds controllers.)
- **Legacy SPA `ui/mill-grinder-ui`:** **Abandoned** — not part of this story (no builds, no client regen, no fixes). Add **`ui/mill-grinder-ui/`** to **`.cursorignore`** at the **repository root** so Cursor excludes it from indexing. **Mill** UI work is **`ui/mill-ui`** only.
- **Tracking and commits (per WI):** When a work item is complete, mark **`[x]`** in [`STORY.md`](STORY.md), update the **`WI-*.md`** status if used, update [`MILESTONE.md`](../../MILESTONE.md) / [`BACKLOG.md`](../../BACKLOG.md) when a listed item is satisfied, then **one full-tree commit** for that WI; clean working tree before the next WI. Prefix `[feat]` / `[fix]` / `[change]` / `[docs]` / `[wip]`; imperative summary; no `Co-Authored-By`. See [`RULES.md`](../../RULES.md).

---

## 1. Metadata cleanup — dead code removal

Remove classes with **zero** (or documented retired) production consumers. Full inventory and verification commands: [`WI-130`](WI-130-remove-dead-code.md).

**Scheduling:** Run as the **final code WI** in §6 so architecture changes expose new orphans; optional early tier-1 removals are allowed if they do not conflict with active refactors.

---

## 2. Facet class demotion

Eliminate the `MetadataFacet` / `AbstractFacet` lifecycle layer so facet payloads stay **schema-driven** and **generic** (`FacetInstance`), per [`facet-class-elimination.md`](../../../design/metadata/facet-class-elimination.md).

### 2a. Demote concrete facet classes

Remove `merge()`, `validate()`, `setOwner()` from:

- `MetadataFacet` and `AbstractFacet`
- `DescriptiveFacet`, `ConceptFacet`, `ValueMappingFacet` — plain `data class`
- `TableLocator`, `TableType`, `ConceptTarget` — unchanged if they have no lifecycle

### 2b. Remove bridge infrastructure

- `FacetClassResolver` + `DefaultFacetClassResolver`
- `FacetConverter` (Jackson `convertValue` bridge)
- Optional: small **`FacetPayloadUtils`** (or inline helpers) decided during [`WI-140`](WI-140-facet-class-demotion.md)

### 2c. Consumer updates

| Area | Files |
|------|-------|
| `data/mill-data-schema-core` | `SchemaFacetServiceImpl`, `SchemaFacets` |
| `data/mill-data-schema-service` | `SchemaExplorerService` |
| `ai/mill-ai-v3` | `SchemaToolHandlers`, `SchemaAuthoringCapability` |
| `ai/mill-ai-v3-cli` | `DemoSchemaFacetService` |
| `ai/mill-ai-v1-core` | `SchemaMessageSpec`, `ValueMappingComponents`, `NlsqlMetadataFacets` |
| `ai/mill-ai-v1-nlsql-chat-service` | `ChatProcessor` |
| `metadata/mill-metadata-autoconfigure` | `MetadataCoreConfiguration` |

**Execution:** [`WI-140`](WI-140-facet-class-demotion.md).

---

## 3. Layered metadata sources and inferred facets

**Backlog:** M-31  
**Narrative:** [`metadata-layered-sources-and-ephemeral-facets.md`](../../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)

Effective metadata is the **merge** of:

1. **Captured** — persisted **`FacetAssignment`** (domain); JPA maps via **`JpaFacetInstance`**. API/read view uses **`FacetInstance`** with `FacetOrigin.CAPTURED`.
2. **Inferred** — computed at read time (logical schema, policy, etc.). Not stored; not mutable via metadata APIs. **`FacetInstance`** with `FacetOrigin.INFERRED`.

### 3a. Core types and repository split

- **Naming / migration (WI-132):** Production today has **`FacetInstance`** in **`mill-metadata-core`** as the **persisted row** shape (`uid`, `entityId`, `facetTypeKey`, `scopeKey`, payload, audit fields, etc.). The spec’s **unified read DTO** reuses the name **`FacetInstance`** (`origin`, `originId`, `assignmentUid`, payload, …). **You cannot introduce the new read type without first freeing the name:** rename the existing persistence type to **`FacetAssignment`** (and align **`FacetRepository`** / **`FacetReadSide`** / JPA **`JpaFacetInstance`**) **before** adding the new **`FacetInstance`** read model. Expect a **broad rename cascade** across modules; **`WI-132`** tracks the ordered steps explicitly.
- **`MetadataSource`:** per-origin **read-only** hook: `originId`, `fetchForEntity(entityId, MetadataReadContext): List<FacetInstance>`. Alias `contributeForEntity` allowed; semantics are **read**. **Not** entity CRUD; **not** the optional persisted-catalog bundle **`PersistenceCatalogReads`** (`EntityReadSide` + `FacetReadSide`).
- **`FacetInstance`:** unified **read DTO** (captured + inferred): `origin`, `originId`, `assignmentUid`, payload, etc. Retired doc name: **`FacetContribution`**. Exists **only after** the persistence row is renamed **`FacetAssignment`** (see above).
- **`FacetAssignment`:** domain row for **`FacetRepository`** / read-write sides; **no** `@Entity` in core contracts — this is the **renamed** former core `FacetInstance` row type.
- **`FacetOrigin`:** `CAPTURED` | `INFERRED`.
- **`RepositoryMetadataSource`:** implements **`MetadataSource`** using **`FacetReadSide`**; maps assignments → **`FacetInstance`** with `CAPTURED`.
- **No `MetadataRepository` type.** **`EntityRepository`** = **`EntityReadSide`** + **`EntityWriteSide`** (evolution of `MetadataEntityRepository`). **`FacetRepository`** = **`FacetReadSide`** + **`FacetWriteSide`**.
- **Modules:** contracts in **`mill-metadata-core`**; JPA types in persistence / adapter modules.
- **`data/mill-data-schema-*`:** consumes registered **`List<MetadataSource>`** (or explicit composition) for read-path merges; output **`List<FacetInstance>`** per entity.

Detail: [`WI-132`](WI-132-metadata-source-contract-and-repository-adapter.md).

### 3b. Combining contributions (logical rules)

In **`SchemaFacetServiceImpl`** and any other read orchestration:

- Collect contributions per entity into **one** list (or set, per implementation), subject to **`MetadataReadContext`** (§3h).
- Inferred sources should use **distinct facet types**; duplicate inferred **type** for the same entity = **misconfiguration**, not a supported merge.
- **Scopes:** ordered resolution as today. **Origins:** **no** priority ordering; only participate or mute (§3h).
- Expose a **`facetsResolved`** (or equivalent) list on **`SchemaFacets`** / **`*WithFacets`** with `origin`, `originId`, `assignmentUid` per instance.

**Merge placement:** §3i (composition-first; discouraged global `@Primary` aggregating `FacetRepository`).

### 3c. Read APIs + OpenAPI

- Responses expose **resolved** facets with instance-level **`origin`**, **`originId`**, **`assignmentUid`** (one DTO shape for all origins; no parallel origin-specific DTOs).
- **Schema-facing** GETs (`SchemaExplorerService` / explorer DTOs): extend per [`WI-134`](WI-134-resolved-facets-read-api-and-openapi.md).
- **Metadata entity** GETs in **`mill-metadata-service`** that return merged facets must apply the **same** resolved shape and **query parameters** (`scope`, `origin`) where applicable.
- **Breaking rename:** read query parameter **`context`** → **`scope`**. Optional **`origin`** (comma-separated URNs or slugs). Semantics: §3h.
- **Active UI consumer:** **`ui/mill-ui`** only — **hand-written** services; update **`context`** → **`scope`** (and **`origin`** where applicable) in **`fetch`** call sites in **WI-134**; verify **`npm run build`**. **`ui/mill-grinder-ui`** is **abandoned** (§0); out of scope for client regen.
- **OpenAPI** on the **server** side must still document all changed REST contracts (see §0); there is **no** requirement to regenerate or build the legacy **mill-grinder-ui** client.

### 3d. Mutation guards

**`FacetService`** and REST **PUT/DELETE** on facets must reject targets that are not real persisted assignments (`FacetOrigin` / uid checks). Detail: [`WI-135`](WI-135-mutation-guards-for-ephemeral-facets.md).

### 3e. UI — full constellation

Data Model / explorer shows **all** effective facets (captured + inferred). Captured UX unchanged. Inferred: read-only; **`originId`** pill replaces edit/delete where applicable. Detail: [`WI-136`](WI-136-ui-full-facet-constellation-view.md).

### 3f. Model root entity

- Stable URN: **`urn:mill/metadata/entity:model-entity`**, **`entityKind = model`**.
- Metadata-only; **not** part of SQL name resolution or physical taxonomy; meaning owned by **`mill-data-schema-*`**.
- **Attachment:** no global “matrix” of where every inferred facet must attach; sources project onto entities that make sense. Duplicate inferred facet **types** on one entity remains a misconfiguration signal.
- **Product requirements:** `model` appears in Data Model explorer, in schema explorer APIs (`SchemaExplorerService` / DTOs), and as **`assignableTo`** wherever manifests and authoring need it.

Detail: [`WI-137`](WI-137-model-root-entity.md).

### 3g. Backend-driven inferred facets

- **`data/mill-data-schema-*`** depends on **`MetadataSource`** for read-side metadata, not on wrapping writes.
- **This story — logical layout:** inferred facets from **`SchemaProvider`** / Mill relational model (`Schema` / `Table` / `Field`). Detail: [`WI-138`](WI-138-backend-logical-layout-inferred-facets.md).
- **Follow-up story — physical sources:** flow/JDBC/filesystem-oriented descriptors, secrets-adjacent fields — **`not`** in this story’s MVP. Safety rules are **per concrete source**, co-authored with that source. Placeholder / deferral doc: [`WI-139`](WI-139-flow-physical-source-inferred-facets.md).

### 3h. `MetadataReadContext` and read query parameters

- **`MetadataReadContext`** — rename/evolution of **`MetadataContext`**. Carries at least **ordered scopes** and **origins** (for muting / filtering **`MetadataSource`** contributions by **`originId`**).
- **Origin muting semantics:** **`origin` absent** or **empty** ⇒ all origins active; **non-empty** ⇒ only listed **`originId`** values contribute.
- **HTTP:** parameters **`scope`** and optional **`origin`**; comma-separated URNs or slugs; **`scope`** replaces legacy **`context`** (breaking for clients).
- Parsing, normalization, and type definitions: implemented with [`WI-132`](WI-132-metadata-source-contract-and-repository-adapter.md) / [`WI-134`](WI-134-resolved-facets-read-api-and-openapi.md) as appropriate.

### 3i. Read-side aggregation vs `FacetRepository`

**As-built (before `MetadataSource` ships):** [`MetadataEntityController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt) uses **`facetRepository`** for merge-trace and write guards; **merged GETs** use **`facetService.resolve`** (`MetadataView` uses the same). There is **no** production **`AggregatingFacetRepository`**.

**Target:** facet **aggregation** is **read-only** and **`MetadataSource`-shaped** — merge **`List<FacetInstance>`** from each registered source after **`MetadataReadContext`**, **not** by replacing **`FacetRepository`** reads globally.

- **Default:** **composition** — inject **`List<MetadataSource>`** (or fixed collaborators) into **`FacetService`**, **`SchemaFacetServiceImpl`**, or thin adapters; merge inline until a **second** consumer needs the **same** algorithm. Then extract a small **`CompositeMetadataSource`** (or similar), still **read-only**.
- **Discouraged default:** **`@Primary` `FacetRepository`** whose read methods synthesize captured + inferred for **every** injector — conflicts with callers needing **raw** persisted rows and spreads merge logic.
- **Writes:** remain **`FacetWriteSide`** / persistence **`FacetRepository`** without pretending merged reads are “the repository.”
- Narrative alignment: [`metadata-layered-sources-and-ephemeral-facets.md`](../../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md) (Full population / implementation note).

Detail: [`WI-133`](WI-133-read-path-facet-merge.md).

---

## 4. UI improvements

**Done (no open WI):**

- Explorer shell: **`ExplorerSplitLayout`** + **`ViewPaneHeader`** (historical **WI-131**).
- `platform-bootstrap.yaml` description / cardinality cleanup.

Further explorer / facet UX for multi-source display is **`WI-136`**.

---

## 5. Remaining product choices

Decisions below are taken **during the listed WI**; this section should shrink as choices land.

| Topic | Resolution |
|-------|------------|
| Keep demoted facet types in **`mill-metadata-core`** vs split **`mill-metadata-types`** | Decide in [`WI-140`](WI-140-facet-class-demotion.md) (default: keep in core unless cycle or size forces split). |
| **`FacetPayloadUtils`** vs inline Jackson in consumers | Decide in [`WI-140`](WI-140-facet-class-demotion.md). |
| Split §2c consumer updates into multiple WIs if **WI-140** becomes too large | Allow follow-up WIs only if **WI-140** explicitly documents the split; otherwise one WI owns the §2c table. |
| Further §4 UI scope beyond **WI-136** | None for this story unless added via new backlog item. |

**Settled (for reference):**

- `data/mill-data-schema-*` read path uses **`MetadataSource`**; repositories own writes.
- `MetadataContext` → **`MetadataReadContext`**; read params **`scope`** / **`origin`**.
- Stable **`model`** identity; metadata taxonomy stays agnostic of **`model`** semantics.
- Core modules stay framework-free; Spring-only in `*-autoconfigure`, `*-service`, etc.
- Inferred sources own facet types; duplicate inferred type per entity = wiring error.

---

## 6. Recommended WI order

Execute **in this order** (dependencies):

1. **[`WI-132`](WI-132-metadata-source-contract-and-repository-adapter.md)** — Contracts, **`RepositoryMetadataSource`**, **`MetadataReadContext`**.

2. **[`WI-137`](WI-137-model-root-entity.md)** — `model` root URN, explorer DTOs, **`assignableTo`**. **Depends on:** WI-132.

3. **[`WI-133`](WI-133-read-path-facet-merge.md)** — Read-path merge (**`FacetService.resolve`**, **`SchemaFacetServiceImpl`**, composition-first per §3i). **Depends on:** WI-132, WI-137.

4. **[`WI-138`](WI-138-backend-logical-layout-inferred-facets.md)** — **`SchemaProvider`** inferred **`MetadataSource`**. **Depends on:** WI-132, WI-137, WI-133.

5. **[`WI-134`](WI-134-resolved-facets-read-api-and-openapi.md)** — Resolved facet DTOs, **`scope`** / **`origin`**, OpenAPI + **metadata** + **schema** GETs; **hand-updated `ui/mill-ui`** call sites; **no** **mill-grinder-ui** work. **Depends on:** WI-133, WI-138.

6. **[`WI-135`](WI-135-mutation-guards-for-ephemeral-facets.md)** — Mutation guards. **Depends on:** WI-132, WI-133 (coordinate with WI-134 for OpenAPI error shapes if needed).

7. **[`WI-136`](WI-136-ui-full-facet-constellation-view.md)** — UI constellation + **`model`** tree. **Depends on:** WI-134, WI-137.

8. **[`WI-140`](WI-140-facet-class-demotion.md)** — §2 facet class demotion and §2c consumers. **Depends on:** WI-132–WI-136 feature set stable enough not to fight concurrent type renames (or negotiate parallel merge with implementers).

9. **[`WI-130`](WI-130-remove-dead-code.md)** — Final dead-code sweep. **Depends on:** WI-140 (or document exceptions).

10. **[`WI-141`](WI-141-story-documentation-closure.md)** — **`docs/design/`** architecture refresh (incl. **`FacetOrigin`**, multi-source behaviour) and **`docs/public/`** user guide. **Depends on:** implementation WIs intended for this branch (at minimum through WI-130).

**Deferred (follow-up story, not branch-blocking):**

- **[`WI-139`](WI-139-flow-physical-source-inferred-facets.md)** — Physical / flow **`MetadataSource`** and per-source safety.

---

## 7. Out of scope

- Removing live Jackson helpers inside **`FacetPayloadFieldJsonSerde`** (types are used via annotations).
- **`FacetTypeManifestInvalidException`** — keep (used by normalizer).
- **M-32** facet catalog (DEFINED vs OBSERVED) — separate story.
- **Global** safety policy for all physical sources — **per-source** in the story that introduces each source.

---

## 8. Story closure (process)

When the branch is merge-ready, also: archive story folder per [`RULES.md`](../../RULES.md); update **MILESTONE** / **BACKLOG**; squash commits per team practice. Documentation deliverables for reviewers are centralized in **WI-141**.
