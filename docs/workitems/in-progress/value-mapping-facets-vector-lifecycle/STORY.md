# Value mapping — metadata facets, vector lifecycle

**Milestone:** **0.8.0**

**Predecessor:** [`implement-value-mappings`](../../completed/20260416-implement-value-mappings/STORY.md) (**WI-174–WI-180**) — `ValueMappingService`, `VectorMappingSynchronizer`, `EmbeddingStore` harness, and column sync are **in place**. This story **does not** redefine the WI-179 sync routine; it **drives** it from **metadata facet types**, adds a formal **`ValueSource`** + **`syncFromSource`**, **operational** triggers, and shared **embedding / vector store** wiring for indexing. **Production resolver + `ValueMappingCapability`** — [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) — is a **separate story** (§ *Closed (reference)*).

## Goal

1. **Facet types + `ValueSource` (WI-181)** — Two **column-targeted** facet kinds: primary **`ai-column-value-mapping`** + optional **`ai-column-value-mapping-values`**; seed **`FacetTypeDefinition`** entries in **`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`**. **DISTINCT** backend values with **`context`** / **`similarityThreshold`** / refresh controls; **static** pairs in the second facet. Formal **`ValueSource`** port with **distinct / static / composite** implementations. **`ValueMappingService`:** **`syncFromSource(...)`** plus existing list-based sync for callers that already have entries. **Shared beans:** one **`EmbeddingHarness`** + one **`EmbeddingStore`** wired to **`VectorMappingSynchronizer`** for indexing (same beans for the production resolver in [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md), separate story — not a fat `ValueMappingService`).
2. **Refresh state DB (WI-184)** — Persist per-column refresh metadata for ops, stats, and **`STALE`** when metadata references a column that no longer exists in **`SchemaProvider`**. Per-attribute outcomes (**COMPLETED** / **PARTIAL** / **FAILED**) reflect each **`syncFromSource`** run.
3. **Startup (WI-182)** — Discover all WI-181–tagged columns over metadata and run **global refresh** via **`syncFromSource`** per attribute (uses **WI-184** when persisting state). **Multi-attribute passes** continue after a single attribute fails — see [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) § *Global pass failure semantics* (does **not** fail Spring startup on one bad column).
4. **Schedule (WI-182)** — Same routine as startup on a **configurable** **`Duration`** interval (**`mill.ai.value-mapping.refresh.schedule-interval`**).
5. **Test dataset extras (WI-185)** — Optional **[`skymill-extras-seed.yaml`](../../../../test/datasets/skymill/skymill-extras-seed.yaml)** / **[`moneta-extras-seed.yaml`](../../../../test/datasets/moneta/moneta-extras-seed.yaml)** beside canonical seeds so local runs and tests can load **value-mapping** facets on representative columns (**depends on WI-181** `FacetTypeDefinition` in **`platform-bootstrap.yaml`**).

## Work Items

**WI order is flexible:** the table below is a **suggested** sequence for MR planning; **reorder** (e.g. land **WI-184** before or in parallel with **WI-182**, or adjust checklist order) whenever it reduces risk or matches team capacity. **Only respect real dependencies** (see § *Dependency hints*).

| Ref | WI         | Document                                                                                                                                                                                    |
| --- | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A   | **WI-181** | [`WI-181-value-mapping-facet-types.md`](WI-181-value-mapping-facet-types.md) — facet types, `ValueSource`, `syncFromSource`, entry semantics, shared harness/store rule                     |
| B   | **WI-184** | [`WI-184-value-mapping-refresh-state-persistence.md`](WI-184-value-mapping-refresh-state-persistence.md) — enums, DDL, repository, **`STALE`**, per-column refresh state                    |
| C   | **WI-182** | [`WI-182-value-mapping-vector-refresh-lifecycle.md`](WI-182-value-mapping-vector-refresh-lifecycle.md) — orchestrator, startup + scheduled + in-process **`ON_DEMAND`**; **not** HTTP/**`mill-ui`** (§ *Scope boundary*) |
| D   | **WI-185** | [`WI-185-value-mapping-test-dataset-seeds.md`](WI-185-value-mapping-test-dataset-seeds.md) — optional Skymill/Moneta test dataset value-mapping seeds (needs **WI-181** facet registration) |

### Dependency hints (do not reorder across these)

- **WI-181** first among **181 / 182 / 184 / 185** for **facet types** + **`syncFromSource`** + shared beans — **WI-182** and **WI-185** assume **WI-181** `FacetTypeDefinition` (and usually **`ValueSource`**) exist.
- **WI-182** needs a **sketched** refresh-state contract to integrate (**WI-184**); **WI-184** and **WI-182** may **overlap** or ship in either order once the DDL/API shape is agreed.
- **WI-185** only after **WI-181** registers facet types in **`platform-bootstrap.yaml`** (seed YAML references those URNs).

## Suggested execution order

1. **WI-181** — Facet definitions, **`ValueSource`**, **`syncFromSource`**, entry/synchronizer semantics, autoconfigure **shared** bean wiring for harness + store.
2. **WI-184** — Persistence (enums, DDL, repository, including **`STALE`** when metadata has no physical catalog match); can overlap **WI-182** once contract is sketched.
3. **WI-182** — Discovery + **`syncFromSource`** loop at startup + schedule; **integrates WI-184** refresh state.
4. **WI-185** — Ship [`test/datasets/skymill/skymill-extras-seed.yaml`](../../../../test/datasets/skymill/skymill-extras-seed.yaml) and [`test/datasets/moneta/moneta-extras-seed.yaml`](../../../../test/datasets/moneta/moneta-extras-seed.yaml); document loader order (`mill.metadata.seed.resources[n]`). **Depends on WI-181** (`FacetTypeDefinition` in `platform-bootstrap.yaml`). **WI-183** (resolver + capability) is a **separate story** — run after **WI-181** / **WI-182** when the vector corpus exists; see [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md).

## Normative decisions

Product decisions for this story live in the work items — especially [**WI-181**](WI-181-value-mapping-facet-types.md) (facets, dedup, `attributeUrn`, appendix YAML, **§ 5b** embedding profile change), [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) (orchestrator, **§ Scope boundary**, **§ Global pass failure semantics**, metadata load, **`CompositeValueSource`**), [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) (refresh state, **`STALE`**, **`last_refresh_values_count`**). Resolver / RAG resolution for the agent is [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) (**separate story**). The former **`GAPS-AND-OPEN-DECISIONS.md`** file has been **retired**; do not add new gap rows there.

**Implementation-ready bar:** A developer should not need **local product reinterpretation** of **done**. [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) **§ Definition of done** is a **closed checklist** (orchestrator-only — HTTP/UI is [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md)). Operational semantics: [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) **§ Per-attribute loop** + **§ Global pass failure semantics**; [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) **Decisions** (single source of truth, including **`last_refresh_values_count`**). Embedding model change: [**WI-181**](WI-181-value-mapping-facet-types.md) § **5b** + **mandatory** acceptance test bullet.

## Gaps and open points

### Closed (reference — decisions landed in WIs)

| Topic | Notes |
|--------|--------|
| **Retrieval depth** | **Top-1** then **gate**: facet **`similarityThreshold`**; implementation maps to **`EmbeddingSearchRequest.minScore`**. [**WI-181**](WI-181-value-mapping-facet-types.md). |
| **Progress / `syncFromSource`** | Optional **`ValueMappingSyncProgressCallback`** on **`syncFromSource`**; **per-element** events from **`ValueMappingService`**; [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) orchestrator **reduces/debounces** before **`ai_value_mapping_state`**. [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) § *Integration*. |
| **WI-185 extras seeds** | **[`skymill-extras-seed.yaml`](../../../../test/datasets/skymill/skymill-extras-seed.yaml)** and **[`moneta-extras-seed.yaml`](../../../../test/datasets/moneta/moneta-extras-seed.yaml)** are **standalone** optional metadata files ([**WI-185**](WI-185-value-mapping-test-dataset-seeds.md)). **No** in-repo import IT requirement; **no** obligation to stay aligned with qsynth / [`test/skymill.yaml`](../../../../test/skymill.yaml) / [`test/moneta.yaml`](../../../../test/moneta.yaml). Authors add paths to their **test bench** `mill.metadata.seed.resources` and confirm behaviour locally. |
| **WI-182 vs WI-182b** | **WI-182** **done** = [**§ Definition of done**](WI-182-value-mapping-vector-refresh-lifecycle.md#definition-of-done--wi-182-only-no-phasing-drift) **only** (orchestrator + in-process **`ON_DEMAND`** + locked config). **No** phasing drift — [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md) is **separate** (HTTP + **`mill-ui`**). |
| **Observability** | **Out of scope** for this story. Read-only HTTP for **`ai_value_mapping_state`**, **Micrometer** / **metrics**, and **structured logs** (run kind, attribute URN) will be a **separate story**; action backlog in [`value-mapping-observability-actions.md`](../../../design/ai/value-mapping-observability-actions.md). |
| **WI-183 capability + resolver** | [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) — production **`ValueMappingResolver`**, **`ValueMappingCapability`**, RAG-only resolution, indexing SQL norms, shared beans with sync — **separate story** from this milestone bundle; depends on **WI-181** and **recommended** after **WI-182** populates the store. Coordinates with [**WI-157**](../ai-value-mapping-capability/STORY.md). |
| **Normative design doc (WI-181)** | **[`value-mapping-indexing-facet-types.md`](../../../design/metadata/value-mapping-indexing-facet-types.md)** under **`docs/design/metadata/`**; linked from [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md) § **`mill.ai.value-mapping`**. Anti-drift vs [`value-mapping-tactical-solution.md`](../../../design/metadata/value-mapping-tactical-solution.md) is documented in the facet doc § *Relationship to tactical YAML*; tactical doc links back to WI-181 / facet model. [**WI-181**](WI-181-value-mapping-facet-types.md) Goal + Acceptance updated accordingly. |
| **WI-172 vs WI-182 refresh read path** | **Not competing:** [**WI-172**](../metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md) is **query-time** — legacy vs faceted **value resolution** (bridge + parity). [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) § *Production metadata retrieval* is **refresh-time** — load facet rows via **`mill-data-metadata`** and assemble **`ValueSource`** for vector indexing. **v1 global refresh is not blocked on WI-172.** The bridge is **not** the refresh loader ([**WI-172** § Coordination](../metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md)); the same split is explicit in [**WI-182** § *Production metadata retrieval*](WI-182-value-mapping-vector-refresh-lifecycle.md). |
| **UI / HTTP: WI-183 vs WI-182 vs WI-173** | **Three surfaces** — easy to confuse: (1) [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) — **chat/agent** path: production **`ValueMappingResolver`** / **`ValueMappingCapability`** tools; **no** new HTTP, **no** UI ([§ *Non-goals*](WI-183-value-mapping-capability-vector-retrieval.md#non-goals)). (2) [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) — **operators**: **on-demand / scheduled / startup** refresh via the **orchestrator** (manual reindex semantics); **Spring HTTP** for **`ON_DEMAND`** (**`/api/v1/ai/value-mapping/…`** on **`mill-ai-v3-service`**) + **`mill-ui`** are **follow-ups** — **out of scope** for this story folder (see § *Closed* row *Admin REST for `ON_DEMAND`*). (3) [**WI-173**](../metadata-value-mapping/WI-173-metadata-value-mapping-api-and-ui.md) — **metadata** REST + **metadata browser** to **browse/inspect** value-mapping facet data. MRs should name which surface they touch so reindex admin is not mistaken for metadata CRUD or for agent-only work. |
| **Global pass failure policy** | **Locked:** [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) § *Per-attribute loop* (step-by-step) + § *Global pass failure semantics* — element failures → **PARTIAL**/**FAILED** + **continue**; unexpected **Throwable** → **log** + **FAILED** + **continue**; **never** abort multi-attribute pass for one bad column; **`APP_STARTUP`** **does not** fail **`ApplicationContext`**. |
| **Concurrent refresh per attribute** | **At most one** active refresh per **`entity_res`**: **block** (mutex / single-flight) so a second **`syncFromSource`** for the same attribute does **not** run until the in-flight run finishes — applies across **startup**, **scheduled**, and **on-demand** triggers. [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) orchestrator owns the guard. |
| **Embedding model / profile change** | **Owned by [**WI-181**](WI-181-value-mapping-facet-types.md) § 5b** — **mandatory automated acceptance** (see § **5b** and **Acceptance criteria** list). [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) only passes **current** model id. Config: [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md). |
| **Facet read path in `mill-data-metadata`** | Implement [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) § *Production metadata retrieval* using **generic** metadata APIs (entity + facet joins) — **avoid** narrow, value-mapping–only methods on core entity/repository types; prefer composable queries or facades so metadata classes stay reusable. Details land in **`mill-data-metadata`** during implementation. |
| **Global pass attribute order** | **No** extra sort key: use **natural** iteration order from discovery / enumeration (whatever the metadata query returns). |
| **Vector segment metadata (`TextSegment`)** | **Planned (implementation, not this planning-only pass):** **`VectorMappingSynchronizer`** merges **`AttributeValueEntry.metadata`** (substitution **`value`**, **`isNull`**, etc.) into **`TextSegment`** for **`EmbeddingStore`** so [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) can resolve from **`metadata["value"]`**. [**WI-181**](WI-181-value-mapping-facet-types.md) §4 — **`DefaultVectorMappingSynchronizer`** (or successor) carries those keys on the segment (not only DB JSON). |
| **Admin REST for `ON_DEMAND` (HTTP)** | **Planned surface:** **`ai/mill-ai-v3-service`**, controller path prefix **`/api/v1/ai/value-mapping/…`**. Shipping the **Spring MVC controller** is **out of scope** for this story folder ([`value-mapping-facets-vector-lifecycle`](STORY.md)); orchestrator + internal / test **`ON_DEMAND`** hooks remain in scope for [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md). HTTP follow-up references this path. |

### Open — follow-ups and clarifications

*None — prior rows moved to § *Closed* or superseded above.*

## Related stories and design

- Completed stack: [`../../completed/20260416-implement-value-mappings/STORY.md`](../../completed/20260416-implement-value-mappings/STORY.md)
- Facet-driven RAG scope: [`../ai-value-mapping-capability/STORY.md`](../ai-value-mapping-capability/STORY.md) (**WI-157**)
- HTTP + **`mill-ui`** for refresh (follow-up): [`WI-182b-value-mapping-refresh-http-ui.md`](WI-182b-value-mapping-refresh-http-ui.md)
- Metadata bridge / APIs: [`../metadata-value-mapping/STORY.md`](../metadata-value-mapping/STORY.md) (**WI-172**, **WI-173**)
- Configuration: [`../../../design/ai/mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md), [`../../../design/platform/CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md)

## Placement

Per [`RULES.md`](../../RULES.md), this folder moved to **`docs/workitems/in-progress/value-mapping-facets-vector-lifecycle/`** once the first WI was completed; it will be archived under **`docs/workitems/completed/`** when the story closes.

## Work Items (tracking)

**Suggested:** **181 → (184 ∥ 182)** → **185** after **181**; adjust order per § *Work Items* / *Dependency hints*. **WI-183** — separate story.

- [x] WI-181 — Facet types, `ValueSource`, indexing API, shared harness/store (`WI-181-value-mapping-facet-types.md`)
- [x] WI-184 — Refresh state persistence, `STALE` (`WI-184-value-mapping-refresh-state-persistence.md`)
- [x] WI-182 — Vector refresh lifecycle: startup + schedule (`WI-182-value-mapping-vector-refresh-lifecycle.md`)
- [x] WI-185 — Test dataset value-mapping seeds: Skymill + Moneta (`WI-185-value-mapping-test-dataset-seeds.md`)
