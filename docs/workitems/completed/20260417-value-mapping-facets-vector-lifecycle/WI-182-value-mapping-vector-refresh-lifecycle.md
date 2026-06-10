# WI-182 — Vector refresh lifecycle (startup + global schedule)

| Field | Value |
|--------|--------|
| **Story** | [`value-mapping-facets-vector-lifecycle`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `ai` |
| **Depends on** | [**WI-181**](WI-181-value-mapping-facet-types.md) — facet definitions, **`ValueSource`** factories, **`ValueMappingService.syncFromSource`**. [`WI-180`](../../completed/20260416-implement-value-mappings/WI-180-value-mapping-service-orchestrator.md), [`WI-179`](../../completed/20260416-implement-value-mappings/WI-179-sync-vectors-hydration.md). **Tests:** stubs/mocks for facet payloads (§ *Discovery*). [**WI-172**](../metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md): catalog bridge when available (coordination, not a substitute for § Production metadata retrieval below). [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) — refresh state persistence (integrate when ready). |

## Definition of done — WI-182 only (no phasing drift)

**WI-182 is `done` iff every box below is true.** There is **no** “partial WI-182” for this story: implementers **do not** split or reinterpret scope — either deliver the list or defer the whole WI to a later milestone.

| # | Criterion |
|---|-----------|
| 1 | **Orchestrator** Spring bean exists (**§ In-scope goals §1**) — sole production entry for global refresh; **`syncFromSource`** + [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) callback integration. |
| 2 | **`APP_STARTUP`**, **`SCHEDULED_TICK`**, in-process **`ON_DEMAND`** each call the **same** internal pipeline with correct **run kind** + facet gating (**§ In-scope goals §2–3**). |
| 3 | **§ Production metadata retrieval** implemented against **`mill-data-metadata`** (generic facet load). |
| 4 | **`ON_DEMAND`** is invocable **in-process** (tests / package API) — **not** a public HTTP controller. |
| 5 | **`mill.ai.value-mapping.refresh.*`** implemented with **locked** types (**§ Configuration** — **`schedule.interval`** = **`Duration`**). |
| 6 | **§ Per-attribute pass algorithm** and **§ Global pass failure semantics** implemented as written. |
| 7 | **§ Acceptance criteria (required)** — all items satisfied. |

**Explicitly not part of WI-182 done:** HTTP, **`mill-ui`**, or “MVP without scheduler” — those are [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md) or a **different** story. This file does **not** describe a phased delivery; [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md) is the **only** place HTTP/UI acceptance lives.

## Problem

After [**implement-value-mappings**](../../completed/20260416-implement-value-mappings/STORY.md), the vector store updates when callers invoke **`ValueMappingService`** per attribute. There is no **platform-wide** pass that:

- Runs **once at startup** (with correct facet gating),  
- Repeats on a **configurable schedule** with **per-attribute** due logic vs persisted **`last_refresh_at`**, and  
- Supports **in-process** **on-demand** refresh for one attribute (programmatic **`ON_DEMAND`** — HTTP is [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md)),

all through one **orchestrator** that delegates to **§ Production metadata retrieval** + **`syncFromSource`**.

## In-scope goals (required for WI-182)

1. **Refresh orchestrator (Spring)** — A dedicated **Spring bean** (name TBD, e.g. **`ValueMappingRefreshOrchestrator`**) is the **only** production entry that performs global refresh. It invokes **§ Production metadata retrieval**, builds **`ValueSource`**, calls **`ValueMappingService.syncFromSource`** with an orchestrator-implemented **`ValueMappingSyncProgressCallback`**, and updates [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) state. The callback **reduces/debounces** per-element progress from the service (e.g. persist **`refresh_progress_percent`** only at **10%** boundaries — [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) § *Integration*). The orchestrator **always** knows the **run kind** (see below) and passes it through for logging / metrics; facet interpretation **depends on run kind**.
2. **Run kinds (code / logs / metrics)** — **`APP_STARTUP`** — one pass after metadata + AI autoconfigure (`mill.ai.enabled=true`), ordered (**`ApplicationRunner`** / **`SmartLifecycle`**). **`SCHEDULED_TICK`** — fired on a fixed **global** interval (**§ Configuration**). **`ON_DEMAND`** — **in-process** trigger only for WI-182; **ignores** facet **`data.refreshInterval`** (always runs **`syncFromSource`** for the targeted attribute(s)). **HTTP**-triggered **`ON_DEMAND`** is [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md).
3. **Facet gating by run kind** — **Startup:** consider only attributes whose primary facet has **`data.refreshAtStartUp` = true** (and **`data.enabled`** as required by **WI-181**), and only when global **`mill.ai.value-mapping.refresh.on-startup.enabled`** is **true**. **Scheduled:** for each candidate attribute, read **`ai_value_mapping_state.last_refresh_at`** (**WI-184**). If the primary facet defines **`data.refreshInterval`**: if **`last_refresh_at`** is **null**, treat as **due** (first run); else compute **due** = **`last_refresh_at` + refreshInterval** and refresh **only** if **now ≥ due** (and facet still enabled). If **due** is not reached, **bypass** that attribute for this tick. Attributes **without** **`refreshInterval`** are **not** refreshed by **scheduled** passes (see **WI-181**). **On-demand (`ON_DEMAND`):** no interval check; **`refreshInterval`** ignored; **single-attribute** scope (**`entity_res`** / URN) for WI-182 tests/API.
4. **Discovery** — Enumerate attributes that carry [**WI-181**](WI-181-value-mapping-facet-types.md) facet types. **Tests:** stub or mock facet attachment + payloads (and realistic **`attributeUrn`** per **WI-181** § *Attribute identity*); **no** requirement for a live metadata service or full **WI-172** bridge in **`test` / `testIT`** to prove the refresh loop. **Production:** follow **§ Production metadata retrieval** below; align with **WI-172** when the bridge exposes stable APIs.
5. **Refresh state** — Implemented via [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md). The orchestrator **integrates** that store: read **`last_refresh_at`** for **scheduled** eligibility; write **state** around each **`syncFromSource`** run (after **reducing/debouncing** callback events — § In-scope goals §1); **failure policy** and column names follow **WI-184**. If **`CatalogPath`** + **`SchemaProvider`** show the attribute has **metadata but no physical** catalog element, **log**, **skip** **`syncFromSource`**, and persist **`last_refresh_status` = `STALE`** (**WI-184**; see [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) § *Normative decisions*).
6. **Per attribute** — Build the appropriate **`ValueSource`** (distinct / static / composite per WI-181) and call **`ValueMappingService.syncFromSource(attributeUrn, source, embeddingModelId, progressCallback)`** (not a duplicate WI-179 implementation). **`progressCallback`** is the orchestrator’s [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) § *Integration* implementation.
7. **Configuration** — Under **`mill.ai.value-mapping.refresh.*`** (same **`mill.ai.value-mapping`** prefix as other value-mapping keys — see **§ Configuration**). **`spring-configuration-metadata.json`** + **`CONFIGURATION_INVENTORY.md`** + [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md).

**HTTP + `mill-ui`:** [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md) — **out of scope** for WI-182 **done** (see **§ Definition of done**).

### Configuration (`mill.ai.value-mapping.refresh.*`)

| Property | Meaning | Default |
|----------|---------|---------|
| **`on-startup.enabled`** | Global gate: when **false**, **no** **`APP_STARTUP`** refresh runs (facet flags ignored for startup). | **`true`** |
| **`schedule.enabled`** | When **false**, **no** **`SCHEDULED_TICK`** runs (scheduler not registered). When **true**, scheduled ticks are **active**. | **`true`** |
| **`schedule.interval`** | Fixed cadence for **how often** the **scheduled** job **wakes** to **evaluate** due attributes (not the same as per-facet **`refreshInterval`**). | **`PT15M`** |

**`schedule.interval` binding (locked — no implementer choice):**

| Layer | Rule |
|-------|------|
| **Property key** | **`mill.ai.value-mapping.refresh.schedule.interval`** (kebab-case in YAML). |
| **Java field** | **`interval`** on nested **`Refresh.Schedule`** under **`ValueMappingConfigurationProperties`** — type **`java.time.Duration`**, default **`Duration.ofMinutes(15)`**. |
| **Binder** | Spring Boot **`@ConfigurationProperties`** for **`mill.ai.value-mapping`** — **`Duration`** conversion uses Boot’s **built-in** `String` → `Duration` ( **`15m`**, **`PT15M`**, **`1h`** all valid). **No** custom converter unless a module cannot use `Duration` (then document exception in MR). |
| **Metadata** | **`spring-configuration-metadata.json`** entry: **`java.time.Duration`**. |

### Global pass failure semantics (locked)

Applies to **multi-attribute** passes (**`APP_STARTUP`**, **`SCHEDULED_TICK`**) and to **single-attribute** **`ON_DEMAND`**. **One** policy for all run kinds — no matrix by kind.

#### Per-attribute loop (operational algorithm)

For **each** candidate **`entity_res`** in **natural** discovery order (for that run kind):

1. **Acquire** the per-attribute **mutex** (single-flight). If another thread holds it, **block** or **skip** per mutex policy — **never** run two **`syncFromSource`** for the same **`entity_res`** concurrently.
2. **STALE path:** If **`CatalogPath`** + **`SchemaProvider`** ⇒ no physical column → persist **`STALE`**, **`IDLE`**, **skip** **`syncFromSource`**; **goto** next attribute.
3. **Begin run:** Persist **`current_state` = `REFRESHING`**, progress reset per [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md).
4. **Invoke** **`syncFromSource`** with the orchestrator-built **`ValueSource`** and **`progressCallback`**.
5. **Normal completion:** From **`onRunComplete`** / callback aggregates → set **`last_refresh_status`** (**COMPLETED** / **PARTIAL** / **FAILED**), **`last_refresh_values_count`** = **successful** element count (per [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) **Decisions**), **`last_refresh_at`**, **`current_state` = `IDLE`**. **No** exception thrown **out of** the orchestrator for this attribute.
6. **Throwable** from **`syncFromSource`** (unexpected): **Log** **ERROR** with **`entity_res`** + stack cause; persist **`last_refresh_status` = `FAILED`** (or **PARTIAL** if partial callback state exists), **`status_detail`** truncated, **`current_state` = `IDLE`** when safe; **do not** rethrow **upward** in a way that aborts the **whole** multi-attribute pass.
7. **Next attribute:** Continue to the next **`entity_res`** — **always**, unless the process is exiting.

**Orchestrator’s** **`runStartup` / `runScheduledTick` / `runOnDemand`** methods **must not** declare **`throws`** that would fail **`ApplicationContext`** for a single-attribute business failure. **Uncaught** errors only from **infrastructure** (bean wiring, DB down) — not from one bad column.

| Situation | Behaviour |
|-----------|-----------|
| **Element-level** failures inside **`syncFromSource`** | Reported via **`onElementProcessed`** / **`onRunComplete`** → **PARTIAL** or **FAILED** for that attribute; orchestrator persists [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md) row; **continue** pass. |
| **After the pass finishes** | **`ApplicationContext`** **up**; **`SCHEDULED_TICK`** **next** wake unchanged. Operators read **`last_refresh_status`**. |
| **Catastrophic** (DB down, metadata unreachable) | Standard Spring failure — **not** “one column failed”. |

**Not in scope:** failing the whole JVM or blocking startup on first attribute failure — **forbidden** for per-attribute refresh failures.

**Concurrency:** **At most one** in-flight **`syncFromSource`** per **`entity_res`** — orchestrator **blocks** (mutex / single-flight) overlapping triggers ([**STORY.md**](STORY.md) § *Gaps*, closed).

**Discovery order:** **Natural** order from metadata enumeration — **no** extra sort ([**STORY.md**](STORY.md) § *Gaps*, closed).

## Production metadata retrieval (`mill-data-metadata` + `ValueSource` assembly)

**Normative:** Load facet rows and assemble **`CompositeValueSource`** per attribute — consolidated spec for this story (formerly “G-12” in the retired gaps doc).

**Owner:** this WI + [**WI-181**](WI-181-value-mapping-facet-types.md) types. **Implementation** of the read path lives in **[`data/mill-data-metadata`](../../../../data/mill-data-metadata)** (repositories / JPA or equivalent — not ad hoc SQL only inside AI modules). Prefer **generic** entity/facet query surfaces; **avoid** hyperspecialized value-mapping-only methods on core metadata types ([**STORY.md**](STORY.md) § *Gaps*, closed).

**Load (logic equivalent to this SQL prototype):** join **`metadata_entity`** → **`metadata_entity_facet`** → **`metadata_facet_type`**, and keep rows where **`metadata_facet_type.type_res`** is one of:

- `urn:mill/metadata/facet-type:ai-column-value-mapping`
- `urn:mill/metadata/facet-type:ai-column-value-mapping-values`

```sql
-- Prototype only — express the same logic in mill-data-metadata, not necessarily verbatim SQL.
SELECT mef.*, mft.*
FROM metadata_entity me
INNER JOIN metadata_entity_facet mef ON me.entity_id = mef.entity_id
INNER JOIN metadata_facet_type mft ON mef.facet_type_id = mft.facet_type_id
WHERE mft.type_res IN (
  'urn:mill/metadata/facet-type:ai-column-value-mapping',
  'urn:mill/metadata/facet-type:ai-column-value-mapping-values'
);
```

**Per entity (attribute / column only):** use **`entity_res`** as **`ModelEntityUrn`** (**WI-181** § *Attribute identity*). For each attribute:

1. **Primary facet** — `ai-column-value-mapping`. If **`data.enabled`**: add **`DistinctColumnValueSource`** (DISTINCT over the backend column). If **`nullValues.indexNull`** (**WI-181** § *NULL and `content` length*): add a **separate** child **`ValueSource`** that yields **one** row for the NULL bucket (not folded into DISTINCT unless the backend naturally returns NULL in DISTINCT).
2. **Static facets** — For each **`ai-column-value-mapping-values`** facet on the same attribute (only when the primary facet exists — **WI-181** Appendix): add **`StaticListValueSource`** from that facet’s payload.
3. **Combine** — Build **`CompositeValueSource`** over those children in a stable order (e.g. DISTINCT, optional NULL bucket, then static lists; dedupe policy in **`ValueMappingService`** per **WI-181**).
4. **Refresh** — **`ValueMappingService.syncFromSource(attributeUrn, source, embeddingModelId)`** (invoked only by the **orchestrator** — in-process **`ON_DEMAND`** or [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md) HTTP delegating to the same orchestrator).

[**WI-172**](../metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md) remains the legacy/faceted **resolution** bridge; this section is the **refresh-time** metadata read + **`ValueSource`** assembly contract.

## Non-goals

- Defining facet payloads or **`ValueSource`** implementations (**WI-181**).
- **`ValueMappingCapability`** / resolver (**WI-183**).
- Full **metadata CRUD** UI for facets — only **manual** / **on-demand** reindex controls for operators (no need to expose **`ON_DEMAND`** in copy).
- **For [`value-mapping-facets-vector-lifecycle`](STORY.md) completion:** public **HTTP** and **`mill-ui`** — [**WI-182b**](WI-182b-value-mapping-refresh-http-ui.md) only (**§ Definition of done** lists what **does** close the story for WI-182).

## Acceptance criteria

**Required for WI-182 / story [`value-mapping-facets-vector-lifecycle`](STORY.md) completion:**

- **§ Definition of done** — every row satisfied (this is the **only** WI-182 completion definition).
- **Orchestrator** bean exists; **startup** / **scheduled** / **`ON_DEMAND`** paths call the same internal pipeline with distinct **run kind** + facet gating per **§ In-scope goals**.
- **§ Per-attribute loop** + **§ Global pass failure semantics** implemented: failed attribute does **not** abort the pass or fail Spring startup (**test** asserts **continue** after injected attribute failure).
- Integration test proves **startup** refresh respects **`refreshAtStartUp`** (mock facets); drives **multiple** attributes via **`syncFromSource`** (in-memory **`EmbeddingStore`** + deterministic embed acceptable). **`attributeUrn`** values are realistic **`ModelEntityUrn`** strings (**WI-181**).
- Integration test proves **scheduled** tick **bypasses** an attribute when **`last_refresh_at` + refreshInterval** is still in the future (repository stub or **WI-184** test double).
- **`schedule.enabled=false`** disables the scheduler; **`on-startup.enabled=false`** skips startup pass.
- **`schedule.interval`** binds as **`java.time.Duration`** (see § Configuration).
- **`ON_DEMAND`:** integration or unit test invokes the orchestrator (or equivalent **in-process** API) for a single **attribute URN** and proves **`syncFromSource`** runs regardless of **`refreshInterval`**.
- Structured logs at start/end with **run kind** + **attribute count** (full metrics optional; see [`value-mapping-observability-actions.md`](../../../design/ai/value-mapping-observability-actions.md)).

**Deferred (follow-up WI — not required for WI-182 done):**

- **REST** WebMvc test for **`/api/v1/ai/value-mapping/…`**.
- **`ui/mill-ui`** admin action.

## Deliverables

- Orchestrator + autoconfigure + unit/integration tests per **§ Acceptance criteria** (required block).
- Orchestrator **hooks** for **`ON_DEMAND`** (in-process). **REST** + **`mill-ui`** — follow-up WI only.
- [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md) — **`mill.ai.value-mapping.refresh.*`** keys (**`Duration`** for **`schedule.interval`**).
