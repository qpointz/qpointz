# WI-178 — Value mappings stack documentation (pre–story closure)

**Story:** [`implement-value-mappings`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `done` |
| **Type** | `📝 docs` |
| **Area** | `ai`, `docs` |
| **Depends on** | **Logical:** [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md)–[**WI-177**](WI-177-vector-store-harness.md) **substance** should exist or be near-final so docs match reality; may run as a **final sweep** before story archive. |

## Problem Statement

[**implement-value-mappings**](STORY.md) spans **providers**, **embedding harness**, **vector store harness**, and **DB repository**. Without a coordinated doc pass, **config schema** changes are easy to lose between **`docs/design`**, **public reference**, and **inventory** tables.

## Goal

- **Property implementation:** new **`mill.ai.*`** `@ConfigurationProperties` classes are **Java** in **`mill-ai-v3-autoconfigure`** (generated **`spring-configuration-metadata.json`**) — align docs with [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md) / [**WI-176**](WI-176-embedding-model-harness.md) / [**WI-177**](WI-177-vector-store-harness.md).
- **Authoritative design:** extend [`docs/design/ai/mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md) whenever **`mill.ai.*`** keys change (see **Checklist** below).
- **Platform inventory:** keep [`docs/design/platform/CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md) and [`docs/design/platform/mill-configuration.md`](../../../design/platform/mill-configuration.md) in sync with new prefixes / rows.
- **Public mirror:** update [`docs/public/src/reference/mill-ai-configuration.md`](../../../public/src/reference/mill-ai-configuration.md) so operators see the same **intended** shape (abbreviated; full spec remains in design).
- **Index:** refresh [`docs/design/ai/README.md`](../../../design/ai/README.md) row for `mill-ai-configuration.md` if the scope line changes.
- **Story context:** ensure [`STORY.md`](STORY.md) and this folder’s WIs remain cross-linked after merges (**no orphaned WI numbers**).

### Checklist — `docs/design` (config schema relevance)

| Document | Why update |
|----------|------------|
| [`docs/design/ai/mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md) | **Primary** spec for **`mill.ai.providers`**, **`mill.ai.embedding-model`**, **`mill.ai.value-mapping`**, **`mill.ai.vector-store`** and resolution flows. |
| [`docs/design/platform/CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md) | Prefix → `@ConfigurationProperties` inventory; add rows for new **`mill.ai.*`** keys. |
| [`docs/design/platform/mill-configuration.md`](../../../design/platform/mill-configuration.md) | High-level **Mill AI** table; add keys / WI references. |
| [`docs/design/ai/README.md`](../../../design/ai/README.md) | Index description for `mill-ai-configuration.md`. |
| [`docs/design/ai/value-mapping-observability-actions.md`](../../../design/ai/value-mapping-observability-actions.md) | **Action points** for metrics backlog (stays open until follow-up WI). |
| [`docs/design/agentic/v3-mill-ai-v3-data-boundary.md`](../../../design/agentic/v3-mill-ai-v3-data-boundary.md) | Optional short note if **`mill-ai-v3-data`** gains vector-store **testIT** ([**WI-177**](WI-177-vector-store-harness.md)). |
| [`docs/design/refactoring/05-configuration-keys.md`](../../../design/refactoring/05-configuration-keys.md) | **Optional** — legacy key tracker; add **`mill.ai.vector-store.*`** when stable if you still maintain this file. |

### Out of scope for this WI (owner)

- **`apps/mill-service/src/main/resources/application.yml`** — **not** edited inside **WI-178**. **Operators** maintain this file: when **`mill.ai.providers`**, **`mill.ai.embedding-model`**, **`mill.ai.vector-store`**, and related keys ship, **mirror** the **intended** YAML shape from [`docs/design/ai/mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md) (and [`docs/public/src/reference/mill-ai-configuration.md`](../../../public/src/reference/mill-ai-configuration.md)) into **defaults / profile samples** under `apps/mill-service` so runtime config matches design. **WI-178** only **checks** that design docs and inventory list those keys — **not** the app YAML itself unless scope is explicitly expanded.

## Acceptance Criteria

- Design + inventory + public reference **aligned** on **`mill.ai.*`** for **WI-175 / WI-176 / WI-177** (and follow-on keys when those WIs close).
- **[`STORY.md`](STORY.md)** remains the single **execution-order** index for this folder.

## Closure

Update [`MILESTONE.md`](../../MILESTONE.md); mark **BACKLOG** docs row **done**.
