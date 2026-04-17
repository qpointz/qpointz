# WI-184 — Value mapping refresh state persistence

| Field | Value |
|--------|--------|
| **Story** | [`value-mapping-facets-vector-lifecycle`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `ai`, `persistence` |
| **Depends on** | [`WI-174`](../../completed/20260416-implement-value-mappings/WI-174-value-mapping-embedding-repository.md) / [`WI-179`](../../completed/20260416-implement-value-mappings/WI-179-sync-vectors-hydration.md) patterns. [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) **consumes** this repository for startup/schedule (implement **after** or in parallel once contract is stable). |

## Decisions (locked)

**Single source of truth:** DDL column semantics below are **final**. The **§ Goal** DDL list **repeats** these definitions verbatim — **no** “TBD in KDoc”; implement JPA/KDoc to match this table.

| Topic | Decision |
|--------|-----------|
| **Table** | **`ai_value_mapping_state`** |
| **Column key** | **`entity_res`** — same name and convention as **`metadata_entity.entity_res`** (`VARCHAR(512) NOT NULL`): stores the attribute instance URN, e.g. `urn:mill/model/attribute:schema.table.column` ([**WI-181**](WI-181-value-mapping-facet-types.md) § *Attribute identity*). Primary key. |
| **Status / lifecycle in DB** | **String columns** (no DB `ENUM` type): values match Kotlin enum names exactly. |
| **`last_refresh_status`** | **`COMPLETED`** — every value in the run succeeded. **`PARTIAL`** — at least one succeeded **and** at least one failed. **`FAILED`** — **all** values failed to refresh (nothing usable). **`STALE`** — metadata still references the attribute, but the **physical** schema/table/column is **missing** from **`SchemaProvider`** after **`CatalogPath`** parse (**log and skip** refresh; [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) § *Normative decisions*). |
| **`current_state`** | **`REFRESHING`** — run in progress. **`IDLE`** — no active run. |
| **`next_scheduled_refresh_at`** | **Persisted** for **informational** display only: computed when a run **finishes** from **last refresh** + parsed **`data.refreshInterval`** ([**WI-181**](WI-181-value-mapping-facet-types.md) Appendix / primary facet). The scheduler’s **actual** next run is still **calculated in application logic** (this column is not the sole source of truth for scheduling). |
| **Optional free text** | Add **`status_detail`** `TEXT` nullable — unstructured notes / last error snippet; **no** required schema. |
| **HTTP API** | **Read-only** observability endpoints optional here; **admin** REST for **manual** / **on-demand** column reindex (run kind **`ON_DEMAND`**) is **[WI-182](WI-182-value-mapping-vector-refresh-lifecycle.md)** (uses this repository). |
| **Consumers** | **[WI-182](WI-182-value-mapping-vector-refresh-lifecycle.md)** orchestrator reads **`last_refresh_at`** on **scheduled** ticks to decide per-attribute **bypass** vs **refresh** when **`data.refreshInterval`** is set. |
| **Lifecycle updates** | **On refresh begin:** set **`current_state`** = `REFRESHING`, reset progress as below. **Progress:** while **`REFRESHING`**, persist **`refresh_progress_percent`** (0–100). Recompute when the count of **successfully processed** values crosses each **10%** step of **total values to refresh** (e.g. at 10%, 20%, …; if a step is skipped because processing is fast, update on the **next** boundary **≥** 10% progress). Total is known after DISTINCT / source enumeration. **Fine-grained** per-element events come from **`ValueMappingService.syncFromSource`** callbacks (§ *Integration* below); the **orchestrator** **reduces/debounces** them before persisting. **On end:** set **`current_state`** = `IDLE`, final **`last_refresh_status`**, clear or freeze progress at 100% as appropriate. |
| **DDL location** | **`persistence/mill-persistence/src/main/resources/db/migration/`** — new **`V*__….sql`** after current highest revision (e.g. **`V7__ai_value_mapping_state.sql`**). Do not edit **`V1`–`V6`** baselines. |
| **`last_refresh_values_count`** | **Locked:** **`BIGINT` nullable.** Semantics: count of **value elements that completed successfully** (embed + persist per § *Integration* — **`onElementProcessed`** success) in the **last completed** **`syncFromSource`** run for this **`entity_res`**. **Excludes** elements that failed or threw. **Set to `null`** if no run completed cleanly (e.g. only **`STALE`** skip, or catastrophic abort before **`onRunComplete`**). **Set to `0`** if a run completed but **zero** successes. JPA field KDoc copies this paragraph **verbatim** — **no** further interpretation. |

## Problem

This WI specifies **persisted** per-column refresh metadata so operators and **WI-182** can observe health, **PARTIAL** vs **FAILED** outcomes, **STALE** when the physical catalog no longer matches metadata (**WI-183** § *Normative decisions*), and **in-progress** progress. Normative enums/DDL live in this document.

## Goal

1. **Kotlin enums** — Mirror DB strings: last outcome `COMPLETED` \| `PARTIAL` \| `FAILED` \| `STALE`; run state `REFRESHING` \| `IDLE`. JPA maps to **`String`** columns.
2. **DDL** — Create **`ai_value_mapping_state`** with at least:
   - **`entity_res`** `VARCHAR(512)` PRIMARY KEY (attribute URN, aligned with **`metadata_entity`**).
   - **`last_refresh_at`** `TIMESTAMPTZ` nullable.
   - **`next_scheduled_refresh_at`** `TIMESTAMPTZ` nullable — informational (see Decisions).
   - **`last_refresh_values_count`** `BIGINT` nullable — **exactly** as **Decisions** **`last_refresh_values_count`** (successful element count from last completed sync; **`null`** vs **`0`** per table).
   - **`last_refresh_status`** `VARCHAR(32)` NOT NULL — `COMPLETED` \| `PARTIAL` \| `FAILED` \| `STALE`.
   - **`current_state`** `VARCHAR(32)` NOT NULL — `REFRESHING` \| `IDLE`.
   - **`refresh_progress_percent`** `INT` nullable — meaningful while **`REFRESHING`**; 0–100, updated at **10%** boundaries (see Decisions).
   - **`status_detail`** `TEXT` nullable — optional free text.
   - **`updated_at`** `TIMESTAMPTZ` NOT NULL default now.
3. **Repository port** — `mill-ai-v3`: load/update by **`entity_res`**; transitions at run **start** / **progress** / **end** per Decisions.
4. **JPA entity + adapter** — Under **`mill-ai-v3-persistence`** (or module that owns `ai_*` tables); follow **`ai_value_mapping`** patterns.
5. **WI-182** — Drive state updates around **`ValueMappingService.syncFromSource`** via an orchestrator-supplied callback; see § *Integration*.

### Integration: progress callback on `syncFromSource` (locked)

**Decision:** **`ValueMappingService.syncFromSource`** takes an **optional** last argument — a **`ValueMappingSyncProgressCallback`** (name TBD) defined in **`mill-ai-v3`** with **no** persistence types (neutral DTOs / primitives only). Tests and thin callers pass **`null`** (no-op).

**Division of responsibility**

| Layer | Responsibility |
|--------|----------------|
| **`ValueMappingService`** | After **enumerating** work (post-dedupe entry list), call **`onBegin(attributeUrn, totalValues)`**. **After each value** is processed (embed + persist per WI-179 — one logical **element**), call **`onElementProcessed(...)`** with **per-element** outcome (success vs failure, optional detail). At end of run, call **`onRunComplete(runSummary)`** with aggregates so the consumer can derive **COMPLETED** / **PARTIAL** / **FAILED**. Emit **every** element event — **no** bucketing or DB writes here. |
| **[WI-182](WI-182-value-mapping-vector-refresh-lifecycle.md) orchestrator** | **Supplies** the callback. **Reduces** and **debounces** the stream of service events before touching **`ai_value_mapping_state`**: e.g. only persist **`refresh_progress_percent`** when crossing **10%** boundaries ([Decisions](#decisions-locked)), coalesce rapid updates, avoid writing the row on **every** element. Owns **run lifecycle** (**`REFRESHING`** / **`IDLE`**, **`last_refresh_status`**, **`status_detail`**, **`last_refresh_at`**, …) by calling the § Goal repository — **not** inside **`ValueMappingService`**. |

**Normative:** Fine-grained progress originates in the **service**; **stability** of persisted state is the **orchestrator’s** policy (reduction + debouncing + **10%** display rules).

Document the callback interface and invocation order in **`mill-ai-v3`** KDoc; reference this section from [**WI-181**](WI-181-value-mapping-facet-types.md) **`syncFromSource`** API.

## Non-goals

- **Admin REST** for **`ON_DEMAND`** / **manual** reindex — **[WI-182](WI-182-value-mapping-vector-refresh-lifecycle.md)** (this WI supplies persistence only).
- Optional **read-only** HTTP for **`ai_value_mapping_state`** rows — may land here or in WI-182; not required for WI-182 MVP if logs suffice.
- Metadata UI (**WI-173**).
- Changing **WI-179** sync core — only **state** around it.

### Future: Mill-wide event bus

**Aspirational design** (not implemented): see [`docs/design/platform/general-event-bus.md`](../../../design/platform/general-event-bus.md). **Value mapping** refresh is listed there as a **candidate** consumer; until then, **§ Integration** callback plus orchestrator **reduce/debounce** remain the practical path.

## Proposed DDL sketch (review before merge)

```sql
-- Example — final file e.g. V7__ai_value_mapping_state.sql
CREATE TABLE ai_value_mapping_state (
    entity_res VARCHAR(512) NOT NULL PRIMARY KEY,
    last_refresh_at TIMESTAMPTZ,
    next_scheduled_refresh_at TIMESTAMPTZ,
    last_refresh_values_count BIGINT,
    last_refresh_status VARCHAR(32) NOT NULL DEFAULT 'FAILED',
    current_state VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    refresh_progress_percent INT,
    status_detail TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Acceptance criteria

- Flyway migration in **`persistence/mill-persistence/.../migration/`**; **`testIT`** / H2 round-trip.
- Unit tests for status rules: **FAILED** = all fail; **PARTIAL** = mixed; **COMPLETED** = all success; **STALE** = physical catalog miss with metadata present; progress updates at **10%** steps with **orchestrator debouncing** of per-element callbacks (§ *Integration*). **`last_refresh_values_count`** matches **successful** row count per **Decisions**.
- [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) documents integration hooks.
- Spec for this table is **closed** in this WI; **code** delivery completes the story item.

## Deliverables

- Enums + DDL + repository + tests; **no** admin **`ON_DEMAND`** REST in this WI (**WI-182**).
