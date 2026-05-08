# Gaps Review — Query Result Execution Service

This document captures planning and specification gaps identified during review of the
`query-result-execution-service` story and its work items.

## Scope reviewed

- `STORY.md`
- `WI-262-query-result-core-sessions.md`
- `WI-263-query-result-marshallers.md`
- `WI-264-query-result-rest-and-wiring.md`
- `WI-265-query-result-tests-and-docs.md`
- Related overlap:
  - `docs/workitems/planned/mill-ui-analysis-full-stack/WI-257-analysis-queries-rest-api.md`
  - `docs/design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md`

## Gaps

### ~~1. Query execution endpoint contract (narrow scope)~~

Status: **closed for story-local planning; pending propagation to external consumer/docs**

**Scope clarification:** The overlap is **only** the **query execution** path. Saved-query
metadata endpoints are **out of scope** for this gap:

- `GET /api/v1/queries` and `GET /api/v1/queries/{queryId}` remain the saved-query catalog.
- This story owns only execution-session HTTP routes under **`/api/v1/query/`**.

**Story-local contract now locked:** the current story and WIs consistently define exactly one
execution HTTP surface:

- **Base path:** **`/api/v1/query/`**
- **Create:** **`POST /api/v1/query`**
- **Session resource:** **`/api/v1/query/{executionId}`**
- **Operations on that resource:** metadata / paging / replace / **`DELETE`** deallocation
- **Not allowed:** legacy **`POST /api/v1/queries/execute`**
- **Not allowed:** sibling top-level routes such as **`/api/v1/query-executions`**
- **Not allowed:** nested **`/executions`** segment under **`/api/v1/query`**

This is now consistent across:

- [`STORY.md`](STORY.md) — story summary, HTTP sketch, module plan, and breaking-change note
- [`WI-264-query-result-rest-and-wiring.md`](WI-264-query-result-rest-and-wiring.md) — controller
  routes and acceptance
- [`WI-265-query-result-tests-and-docs.md`](WI-265-query-result-tests-and-docs.md) — required doc
  rewrites and contract tests

**What remains open is no longer the story-local decision.** The remaining work is propagation into
external consumer and design docs that still describe the old synchronous execution route.

Residual external follow-up:

- [`WI-257-analysis-queries-rest-api.md`](../mill-ui-analysis-full-stack/WI-257-analysis-queries-rest-api.md)
  still includes **`POST /api/v1/queries/execute`** and must be resoped to saved-query **`GET`**s only.
- [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) still
  documents **`POST /api/v1/queries/execute`** and must be rewritten to the **`/api/v1/query/…`**
  session model.

**Acceptance / closure condition:**

- Story-local docs are already reconciled.
- WI-265 must remove the external normative **`POST /api/v1/queries/execute`** contract and replace
  it with the **`/api/v1/query/…`** model.
- WI-265 must also drive the required design-doc sweep and UI-facing doc updates so no parallel
  deprecated execution route remains documented.

### ~~2. Authorization and ownership semantics~~

Status: **closed for story-local planning; capture final ownership rule in design doc**

The earlier version of this story left ownership and visibility underspecified. The current WIs now
lock the key decisions:

- [`WI-262-query-result-core-sessions.md`](WI-262-query-result-core-sessions.md) requires
  **every** core operation to take **`CallerContext`**.
- WI-262 also locks **session owner** checks on reads and writes, plus **opaque `executionId`**
  generation distinct from saved-query **`queryId`** values.
- [`WI-264-query-result-rest-and-wiring.md`](WI-264-query-result-rest-and-wiring.md) now defines
  **`401`** unauthenticated, **`403`** wrong owner for a known **`executionId`**, and **`404`**
  unknown **`executionId`**.
- [`WI-265-query-result-tests-and-docs.md`](WI-265-query-result-tests-and-docs.md) requires REST
  coverage for cross-user **`403`** where practical.

Remaining gap:

- The exact semantics of **tenant** vs **principal** vs full **caller-context tuple** are still
  described at a high level rather than fully normalized into one short rule statement in
  **`STORY.md`**.

Acceptance needed:

- Keep WI-262/WI-264/WI-265 aligned on the same ownership rule.
- The design doc should state the final ownership rule in one place so implementation and API docs
  do not drift.

### ~~3. External pagination contract is ambiguous~~

Status: **closed** — normative contract is **[`STORY.md`](STORY.md)** sections **Paging contract** and **Server and client buffering (backward paging)**; **WI-264** / **WI-265** / **WI-262** updated to match.

**Locked externally:**

- **Rows:** **`GET /api/v1/query/{executionId}/rows?pageIndex=&pageSize=`** only (**no** public **`offset`/`limit`** pair on **`/api/v1/query/**`).
- **Envelope:** **`pageIndex`**, **`pageSize`**, **`rowCount`**, optional **`totalResult`**, **`hasNext`**, **`hasPrevious`** — semantics in **STORY** (including unknown total and partial last page).

**Implementation must follow** OpenAPI + tests from **WI-264** / **WI-265**; residual risk is only **drift** if code diverges from these docs.

### ~~4. Result format negotiation is not specified at HTTP contract level~~

Status: **closed** — **[`STORY.md`](STORY.md)** **Format negotiation and `Content-Type`**; **WI-263** (marshaller declares **standard** MIME types, built-ins both **`application/json`**); **WI-264** (HTTP mapping, **`data`** payload slot, **`400`/`406`**); **WI-265** (docs + **`testIT`** matrix).

**Locked summary:**

- **Standard MIME only** (no vendor `application/vnd.*` for built-ins); **`Content-Type`** comes from the **selected `ResultMarshaller`**.
- **`format`** query on **`GET …/rows`** + **`Accept`** + session **`defaultFormat`** + server default — order and **`format`**-wins precedence in **STORY**.
- **`rows-objects`** vs **`rows-compact-batch`**: both **`application/json`** — pick variant with **`format`** / **`defaultFormat`**.
- **JSON body:** paging envelope + top-level **`data`** (OpenAPI in **WI-264**).
- **Errors:** unknown **`format`** → **`400`**; unsatisfiable **`Accept`** (no **`format`**) → **`406`**.

### ~~5. Concurrency, invalidation, and replace semantics are not testable yet~~

Status: **closed** — **[`STORY.md`](STORY.md)** **Concurrency, invalidation, and replace**; **WI-262** ( **`epoch`**, RW ordering, unit tests); **WI-264** ( **`409`**, OpenAPI, **`testIT`** hooks); **WI-265** (design doc + Skymill **`testIT`** matrix).

**Locked summary:**

- **`epoch`:** **`0`** at **`create`**, **`+1`** on each successful **`replace`**; in metadata and **`/rows`** envelope; optional **`epoch`** query on **`GET …/rows`** → **`409`** if stale.
- **`replace` vs reads:** **Per-session read–write lock** (or equivalent) — **`replace`** waits for in-flight reads; reads block during **`replace`**; **no** post-replace pages from old buffers.
- **Eviction / `DELETE`:** subsequent **`GET`** → **`404`** (not **`409`**).
- **Live data + refills:** **Weak consistency** across refills in sliding-window mode — **KDoc** + **WI-265** design doc.

**Executable expectations:** **WI-262** unit tests + **WI-264**/**WI-265** **`testIT`** per WI acceptance ( **`409`** after replace with old **`epoch`**, **`404`** after delete/eviction, concurrent ordering where practical).

## Recommended follow-up edits

1. ~~Update WI-264 to lock the public endpoint contract and pagination model.~~ (**Done** — **STORY** + **WI-262**–**WI-265** + **§3** above.)
2. Update WI-262 and WI-264 to define ownership and authorization rules across all operations.
3. ~~Update WI-263 and WI-264 to define format negotiation, media types, and error behavior.~~ (**Done** — **STORY** **Format negotiation** + **WI-263**–**WI-265** + **§4** above.)
4. ~~Update WI-265 to require contract and concurrency tests, not only documentation.~~ (**Done** — **STORY** **Concurrency** + **WI-262**–**WI-265** + **§5** above.)
5. **Gap §1 follow-through:** decision locked in this file — apply **BACKEND** / **WI-257** /
   **`mill-ui`** updates in WI-265 (and related WIs); **breaking** — **no** dual execution contract and **no** deprecation period for **`POST /api/v1/queries/execute`**.
