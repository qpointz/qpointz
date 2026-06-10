# WI-262 — Core module: programmatic sessions + Caffeine + buffer window

Status: `done`  
Type: `feature`  
Area: `data`  
Backlog refs: **D-8**

**Story:** [`STORY.md`](STORY.md) — **WI-262** (tracker row 1). **Delivery:** when this WI is finished, mark its tracker + **[`STORY.md`](STORY.md)** **Tracker** / **Work Items**, then **one commit** for the full tree (**[`RULES.md`](../../RULES.md)**).

## Language

- **Kotlin** — all **`mill-data-query`** implementation under **`src/main/kotlin`**.
- **Java** — **not** used in this module for **WI-262** (no **`@ConfigurationProperties`** here; **`mill.data.query.*`** binding lands where the **`QueryResultExecutionService`** **`@Bean`** is registered — **WI-262** / autoconfigure / **WI-264** as designed).

## Tracker (this WI)

- [x] Gradle module **`data/mill-data-query`** registered in **`settings.gradle.kts`** — **Kotlin** sources **`src/main/kotlin`** (no Spring Web / Security on compile classpath)
- [x] **`QueryResultExecutionService`**: **`create`**, **`replace`**, **`getPage`**, **`delete`**, **`metadata`** — **every** method takes **`CallerContext`** with at least **tenant** (see **[`STORY.md`](STORY.md)** **Session ownership**); **session tenant** (single string, e.g. **`userId`**, snapshot at **`create`**) must match on **all** reads and writes; mismatch → same denial path as unknown **`executionId`** (HTTP **`403`** vs **`404`** mapped in **WI-264**). Presentation **`VectorBlock`** slices are produced inside **`getPage`** via marshallers (no separate **`getPresentationVectorBlock`** on the public interface).
- [x] Opaque **`executionId`** generation and lookup (not related to saved-query **`queryId`** under **`/api/v1/queries/{queryId}`**)
- [x] **Programmatic configuration** surface for the session engine (constructor or builder type in **`mill-data-query`**) — values populated from Spring-bound **`mill.data.query.*`** at the **`QueryResultExecutionService`** **`@Bean`** factory (**no** YAML or **`@ConfigurationProperties`** inside **`mill-data-query`** itself)
- [x] **`delete`** tears down session state (buffers, dispatcher-backed cursors as applicable) — REST **`DELETE /api/v1/query/{executionId}`** in **WI-264** delegates here for **explicit deallocation**
- [x] **Caffeine** sessions **`expireAfterAccess`** (+ caps as designed)
- [x] **v1:** bounded **full materialization** into session buffers (**`maxMaterializedRows`**); backward **`pageIndex`** is served from the retained snapshot. **Sliding execution-buffer window** + refill on miss (**[`STORY.md`](STORY.md)** **Server and client buffering**) is a **follow-up** (same dispatcher forward-only constraint).
- [x] Core defines **authoritative** envelope inputs for REST: **`epoch`**, **`rowCount`**, **`hasNext`/`hasPrevious`**, **`totalResult`** (always set; use **Kotlin/Java `null`** at API boundary → **JSON `null`** on wire when unknown) per **[`STORY.md`](STORY.md)** **Paging contract** and **Concurrency, invalidation, and replace**
- [x] **`epoch`:** **`0`** on **`create`**, increment on each successful **`replace`**; optional client-supplied **`epoch`** on **`getPage`** / read path — mismatch → signal for HTTP **`409`** (**WI-264**)
- [x] **Per-session read–write ordering:** **`replace`** exclusive vs **`getPage`/`metadata`** (e.g. **`ReentrantReadWriteLock`**) per **[`STORY.md`](STORY.md)** **Concurrency** — no post-replace reads from torn buffers
- [x] Unit tests (window math / boundaries + **tenant** / unknown id) + **KDoc** on **every** new/changed **public** API — types, **constructors**, **properties**, **functions**, and **each method parameter** (see **[`STORY.md`](STORY.md)** **Implementation conventions**)

## Goal

Introduce **`data/mill-data-query`**: Spring-free library with **`QueryResultExecutionService`** (or equivalent) supporting **in-process only**:

- **`create`**, **`replace`** (same **`executionId`**, tear down prior buffers), **`getPage`/`readSlice`**, **`getPresentationVectorBlock` / …** (**borrowed `VectorBlock`** for presentation row-range), **`delete`**, **`metadata`**
- **`CallerContext`** on **every** call — carries at least **tenant** (authenticated user id string per **[`STORY.md`](STORY.md)** **Session ownership**); no `HttpServletRequest` dependency; **no** cross-tenant access
- **Caffeine** session store: **`expireAfterAccess`** (idle); optional **`expireAfterWrite`** + weight limits
- **`VectorBlock` / `DataOperationDispatcher`** orchestration; **no JDBC `ResultSet`**
- **Two materialization paths:** full snapshot under threshold; **sliding `executionBufferRows`** window with **`backwardCacheBuffers` / `forwardCacheBuffers`** and **re-query refill** on miss — **backward presentation paging** depends on this window (or snapshot), because **`DataOperationDispatcher`** / **`fetchResult`** advance only **forward**
- **Presentation** **`pageSize`** independent of **`executionBufferRows`**; **`pageIndex`** in core responses is **presentation-level** (aligned with HTTP **query-driven `GET /api/v1/query/{executionId}`** — **[`STORY.md`](STORY.md)** **Paging contract**)
- Envelopes: **`epoch`**, **`pageIndex`**, **`pageSize`**, **`rowCount`**, **`totalResult`** (required field, **`null`** = unknown), **`hasNext`/`hasPrevious`** — semantics locked in **[`STORY.md`](STORY.md)**; truncation and **refill vs live source data** (**weak consistency** on sliding-window refills) documented in **KDoc** per **Concurrency**

## Scope

1. Register **`settings.gradle.kts`** + **`build.gradle.kts`** for **`mill-data-query`** (`publishArtifacts` per team default); **`src/main/kotlin`** only for this module’s code (**Spring-free**).
2. Implement session lifecycle + cache + refill rules in **Kotlin**; wire **dispatcher** (+ existing backends autoconfigure consumed by **`mill-service`** in later WI).

## Acceptance

- Unit tests for window math / slice-from-buffer (**happy path + boundary**) without full Boot where feasible — include at least one **backward `pageIndex`** case (page *N* then *N−1*) on **snapshot** or **window hit** path.
- Unit tests for **tenant mismatch** and unknown **`executionId`** (deny path; no cross-tenant reads).
- Unit tests for **`replace`** → **`epoch`** increments; **optional `epoch` mismatch** on read → core denial path mapped to **`409`** in **WI-264**; **read–write lock** ordering (**`replace`** vs concurrent **`getPage`**) produces **no** mixed-buffer pages (assertable with contended threads or sequential ordering tests).
- **KDoc** on all public API per **[`STORY.md`](STORY.md)** (**parameter-level** required), including **refill-on-miss**, **`hasNext`** when **`totalResult`** is **`null`**, and **live-data / refill** consistency expectations.

## Depends on

None (uses existing **`mill-data-backend-core`**).

## Notes

Align **`executionBufferRows`** with **`QueryExecutionConfig.fetchSize`** where possible ([`proto/data_connect_svc.proto`](../../../../proto/data_connect_svc.proto)).

**Backward paging:** server-side buffering is **required** for cheap backward moves in windowed mode; HTTP clients rely on the **same** **`GET /api/v1/query/{executionId}`** with a smaller **`pageIndex`** (**[`STORY.md`](STORY.md)** **Server and client buffering**). Optional future range parameters (**`rowFrom`/`rowTo`**) would be an HTTP extension in **WI-264**, not a change to dispatcher direction.
