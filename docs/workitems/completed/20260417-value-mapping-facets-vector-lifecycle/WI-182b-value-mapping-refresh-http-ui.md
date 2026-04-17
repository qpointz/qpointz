# WI-182b — Value mapping refresh: HTTP + `mill-ui` (operator surface)

| Field | Value |
|--------|--------|
| **Story** | Optional follow-up to [`value-mapping-facets-vector-lifecycle`](STORY.md) — **not** in the story checklist until promoted. |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `ai`, `ui` |
| **Depends on** | [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) **complete** per its **§ Definition of done** — orchestrator + in-process **`ON_DEMAND`** must exist first. |

## Problem

Operators need a **supported** way to trigger **on-demand** vector refresh without calling Java APIs. [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) deliberately **excludes** HTTP and **`mill-ui`** from its **done** boundary; this WI adds those surfaces.

## Goal

1. **HTTP** — Spring MVC controller on **`ai/mill-ai-v3-service`**, path prefix **`/api/v1/ai/value-mapping/`** (exact resource paths TBD in implementation — e.g. POST on-demand refresh by **`entity_res`**). **Admin**-secured (align Mill patterns). Delegates to [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) orchestrator run kind **`ON_DEMAND`** only — **no** duplicate sync logic.
2. **`mill-ui`** — Admin action or screen: **manual** / **on-demand** labels (never **`ON_DEMAND`** in user-facing copy). Calls the same HTTP API or a shared client the API uses.

## Non-goals

- Orchestrator design (**WI-182**).
- Metadata browser value-mapping display (**WI-173**).

## Acceptance criteria

- WebMvc (or WebFlux) integration test: HTTP request triggers **`ON_DEMAND`** path on orchestrator (mock or test slice).
- **`mill-ui`**: at least one callable admin control wired to refresh (exact UX flexible).
- Documented in release notes / operator docs.

## Deliverables

- Controller + security + tests + **`mill-ui`** wiring.

**Note:** Reindex when **`mill.ai.value-mapping.embedding-model`** changes is specified and accepted under [**WI-181**](WI-181-value-mapping-facet-types.md) § **5b**, not this WI.
