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

Status: **closed** — external consumer/docs updated (**WI-265**, **WI-257**).

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

**What remains open is no longer the story-local decision.** External docs and **WI-257** were updated with **WI-265** (session **`/api/v1/query/**`**; **no** **`POST /api/v1/queries/execute`**).

**Residual external follow-up:** none for execution routing — **WI-265** + **WI-257** text updates landed the **`/api/v1/query/…`** model.

### ~~2. Authorization and ownership semantics~~

Status: **closed** — **Session ownership** is **tenant-only** (single string, e.g. **`userId`** / **`sub`**) at **`create`**; compared on every operation; **`403`** vs **`404`** per **[`STORY.md`](STORY.md)** **Session ownership**; **WI-262** / **WI-264** / **WI-265** aligned.

**Decision (v1):**

- Persist **one** **tenant** identifier per session (authenticated user identity from **`mill-service`** / Spring Security).
- **`CallerContext`** carries at least **tenant**; no multi-field ownership tuple for v1.
- **WI-265** design doc + **BACKEND** repeat the same one-line rule (no drift).

**Previously “remaining” narrative gap:** ~~tenant vs principal vs caller tuple~~ — **resolved** by **tenant-only** storage.

### ~~3. External pagination contract is mostly locked, but one wire-shape choice remains~~

Status: **closed** — **`totalResult`** is **always** present on **`/rows`** and in **metadata**; **`JSON null`** means **unknown** total cardinality; **non-null** number means **known**. **Do not omit** the field (**[`STORY.md`](STORY.md)** **Paging contract**; **WI-264** OpenAPI + **WI-265** **`testIT`**).

**Previously open:** ~~omit vs `null`~~ — **resolved** to **explicit `null`** only.

### ~~4. Result format negotiation is mostly locked, but response-shape and status choices remain~~

Status: **closed** — **[`STORY.md`](STORY.md)** **Format negotiation**, **HTTP status semantics**, **WI-264** OpenAPI table.

**Locked summary:**

- **Marshaller JSON slot:** top-level **`data`** (always that name for JSON **`/rows`** responses).
- **`POST /api/v1/query`:** **`201 Created`** when response is **creation-only** (no first page); **`200 OK`** when body **includes** the first page; **OpenAPI** documents both (or a request flag that selects one path).
- **`DELETE /api/v1/query/{executionId}`:** **`204 No Content`**, no body.
- **SQL / plan failures** (valid HTTP request): **`422 Unprocessable Entity`** on **`POST`** and **`PUT`**; **`400`** for malformed JSON / bad query params / unknown **`format`**, etc.

**Previously open:** ~~payload property name~~, ~~`POST`/`DELETE`/SQL status picks~~ — **resolved** as above.

### ~~5. Concurrency, invalidation, and replace semantics are not testable yet~~

Status: **closed** — **[`STORY.md`](STORY.md)** **Concurrency, invalidation, and replace**; **WI-262** ( **`epoch`**, RW ordering, unit tests); **WI-264** ( **`409`**, OpenAPI, **`testIT`** hooks); **WI-265** (design doc + Skymill **`testIT`** matrix).

**Locked summary:**

- **`epoch`:** **`0`** at **`create`**, **`+1`** on each successful **`replace`**; in metadata and **`/rows`** envelope; optional **`epoch`** query on **`GET …/rows`** → **`409`** if stale.
- **`replace` vs reads:** **Per-session read–write lock** (or equivalent) — **`replace`** waits for in-flight reads; reads block during **`replace`**; **no** post-replace pages from old buffers.
- **Eviction / `DELETE`:** subsequent **`GET`** → **`404`** (not **`409`**).
- **Live data + refills:** **Weak consistency** across refills in sliding-window mode — **KDoc** + **WI-265** design doc.

**Executable expectations:** **WI-262** unit tests + **WI-264**/**WI-265** **`testIT`** per WI acceptance ( **`409`** after replace with old **`epoch`**, **`404`** after delete/eviction, concurrent ordering where practical).

## Recommended follow-up edits

1. ~~Update WI-264 to lock the public endpoint contract and pagination model.~~ (**Done** — route + envelope; **`totalResult`** unknown = **`JSON null`**, field always present — **§3** above.)
2. ~~Update WI-262 and WI-264 to define ownership and authorization rules across all operations.~~ (**Done** — **STORY** **Session ownership** tenant-only + **WI-262** / **WI-264** / **WI-265** + **§2** above.)
3. ~~Update WI-263 and WI-264 to define format negotiation, media types, and error behavior.~~ (**Done** — **STORY** + **WI-264** + **§4** above.)
4. ~~Update WI-265 to require contract and concurrency tests, not only documentation.~~ (**Done** — **STORY** **Concurrency** + **WI-262**–**WI-265** + **§5** above.)
5. **Gap §1 follow-through:** decision locked in this file — apply **BACKEND** / **WI-257** /
   **`mill-ui`** updates in WI-265 (and related WIs); **breaking** — **no** dual execution contract and **no** deprecation period for **`POST /api/v1/queries/execute`**.
6. **Implementation-readiness follow-through:** ~~lock the remaining single-choice wire contract decisions~~ (**done** — **`totalResult`** **`null`**, **`data`**, **`POST` `201`/`200`**, **`DELETE` `204`**, **`422`** SQL — **§3**–**§4**). Remaining work is **Gap §1** doc propagation (**WI-265** / **WI-257** / **`mill-ui`**).
