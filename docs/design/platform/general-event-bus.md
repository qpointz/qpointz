# General event bus (Mill-wide)

**Status:** Aspirational — **not implemented** as a first-class Mill facility today. This document captures **requirements** and **candidate use cases** so future work can align modules without ad hoc callbacks.

## Purpose

Many flows need **decoupled notifications**: core services publish **lifecycle** or **progress** facts; **persistence**, **metrics**, and **future HTTP** layers subscribe without compile-time coupling. Today teams use **Spring `ApplicationEventPublisher`**, **direct callbacks**, or **orchestrator** beans; a **Mill event bus** would make contracts, **ordering**, and **delivery mode** (sync vs async) **explicit** and **reusable** across `core/`, `ai/`, `metadata/`, and `persistence/`.

## Desired capabilities

| Capability | Rationale |
|------------|-----------|
| **Typed events or stable topic names** | Subscribers match on **event type** or **name** (e.g. `value-mapping.refresh.progress`) with a **versioned payload DTO**. |
| **Correlation / run id** | Tie **start → progress → complete** for overlapping runs, retries, and logs. |
| **Per-key ordering** | Optional **serial** delivery per **business key** (e.g. `entity_res`) so progress never appears out of order. |
| **Sync vs async delivery** | **Synchronous** handlers for **same-transaction** DB updates; **async** for metrics-only — **explicit** per subscriber or bus policy. |
| **Module boundaries** | Bus **API** in a **low-dependency** module (`mill-core` or small `mill-events`); **JPA listeners** in `*-persistence` subscribe without reverse dependencies. |
| **Testability** | In-memory implementation + **assert** helpers for unit tests without full Spring context. |

**Interim:** Spring’s **application events** + **`@TransactionalEventListener`** approximate **sync-after-commit** semantics; they do not replace a **documented Mill contract** for cross-cutting domains.

## Candidate use cases

| Domain | Scenario | Notes |
|--------|----------|--------|
| **Value mapping** | Long-running **refresh / reindex** per column: **begin**, **progress** (e.g. 10% steps), **complete** with **COMPLETED** / **PARTIAL** / **FAILED**; persist **`ai_value_mapping_state`**, emit metrics. | Work item: [`WI-184`](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-184-value-mapping-refresh-state-persistence.md). Until a bus exists, **optional callbacks** on **`syncFromSource`** or a **thin orchestrator** carry state updates. |
| **Metadata** | Facet or entity changes that should **invalidate caches** or **trigger downstream sync** without `metadata-service` importing every consumer. | Future; shape TBD with [`metadata-service-design.md`](../metadata/metadata-service-design.md). |
| **Observability** | Standard **hooks** for Micrometer / structured logs with **shared attributes** (correlation id, deployment). | Align with [`../ai/value-mapping-observability-actions.md`](../ai/value-mapping-observability-actions.md) where relevant. |

## Non-goals (for a first version)

- **Distributed** broker (Kafka, etc.) — **in-process** first; bridge later if needed.
- **Guaranteed delivery** across restarts — operational concern for a later iteration.

## Related documents

- [`mill-configuration.md`](mill-configuration.md) — platform configuration map.
- [`CONFIGURATION_INVENTORY.md`](CONFIGURATION_INVENTORY.md) — Spring beans and properties.
