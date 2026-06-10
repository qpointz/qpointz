# Query result execution service

**Audience:** maintainers and implementers (HTTP clients, UI, other services, in-process embedders). This is **not** end-user marketing copy.

**Work tracking:** delivery checklist, Gradle bootstrap, and WI ordering live in the story archive [`query-result-execution-service`](../../workitems/completed/20260511-query-result-execution-service/STORY.md). **Normative HTTP behaviour** described here is **aligned** with that story; if anything drifts during implementation, fix it in **one** reconciling change set (story + this file + OpenAPI).

---

## Breaking change vs legacy HTTP

The composed **`mill-service`** + **`mill-ui`** stack **must not** treat **`POST /api/v1/queries/execute`** as a supported execution surface. **Session-based** execution is **only** under **`/api/v1/query/**` (see [HTTP resource model](#http-resource-model)). There is **no** deprecation period and **no** dual normative contract. UI and backend requirement docs are reconciled in work item **WI-265** (see the story **Story closure — reconcile `docs/design`** table).

**Related backlog:** [D-8](../../workitems/BACKLOG.md) (data), consumer overlap [U-13](../../workitems/BACKLOG.md) / saved-query story [`20260609-mill-ui-analysis-full-stack`](../../workitems/completed/20260609-mill-ui-analysis-full-stack/STORY.md) (catalog under **`/api/v1/analysis/**`**; execution is **not** there).

---

## Purpose and boundaries

### What this stack does

- **Programmatic-first:** a Spring-free core (**`QueryResultExecutionService`**) owns **execution sessions**: SQL (or plan reference per core API), **paged** reads over **`VectorBlock`** via **`DataOperationDispatcher`** (`submitQuery` / `fetchResult` / `execute`), optional **marshaller** sinks, **Caffeine** idle eviction, and an optional in-process **`replace`** (not exposed over HTTP).
- **REST adapter:** **`mill-data-query-service`** exposes **all** execution routes under **`/api/v1/query/`**, maps **`Authentication`** to **`CallerContext`**, validates query params, delegates to the core, and fills HTTP status / headers from marshaller and policy.
- **Presentation vs engine window:** clients use **`pageIndex`** / **`pageSize`** on **`GET /api/v1/query/{executionId}`** when paging. Internally the core may use **`executionBufferRows`**, **`backwardCacheBuffers`**, **`forwardCacheBuffers`**, full snapshot, or refill-on-miss — those are **implementation details** behind the paging envelope.

### What it does not do

- **Saved-query catalog** is **`/api/v1/analysis/queries`** (Analysis namespace; distinct from session resource **`query`**). This service does **not** subsume catalog CRUD — see [`analysis-saved-query-service.md`](analysis-saved-query-service.md).
- **No** nested **`/executions`** (or sibling) collection under **`query`**; **`executionId`** is **opaque** for this API — it is **not** a saved-query **`queryId`**.
- **No** public **`offset` / `limit`** on this tree; **only** **`pageIndex`** / **`pageSize`** on **`GET /api/v1/query/{executionId}`** (paged mode).

### Who consumes it

| Consumer | Typical integration |
|----------|---------------------|
| **`mill-ui`** analysis / playground | HTTP: `queryService` → **`/api/v1/query/**`**; see [mill-ui BACKEND-API-REQUIREMENTS](../ui/mill-ui/BACKEND-API-REQUIREMENTS.md) after WI-265. |
| **`mill-service`** | **`apps/mill-service`** depends on **`mill-data-query-service`**; dispatcher and backends come from **`mill-data-autoconfigure`**. |
| Other HTTP clients | Same OpenAPI contract; respect **tenant-only** ownership and **`epoch`** semantics. |
| In-process (NL-to-SQL, automation, tests) | With **`mill-data-autoconfigure`** on the classpath: inject **`QueryResultExecutionService`** (from **`QueryResultEngineAutoConfiguration`**) and pass **`CallerContext`** per call — **no** **`mill-data-query-service`** required. **No** Spring Web on **`mill-data-query`**. |

Broader data-plane context: [mill-data-lane-onepager.md](mill-data-lane-onepager.md). gRPC / export parallels: [data-export-service.md](data-export-service.md) (different transport; same “thin adapter over engine” idea).

---

## Module map and Gradle

| Gradle module | Path (when present) | Role |
|---------------|---------------------|------|
| **`mill-data-query`** | `data/mill-data-query/` | Kotlin core: sessions, Caffeine, buffer/refill, **`epoch`**, marshaller registry **API**, **`ServiceLoader`** SPI for **`ResultMarshallerProvider`**. **No** Spring Web / Security / Reactor on this module’s compile classpath for domain code. |
| **`mill-data-autoconfigure`** | `data/mill-data-autoconfigure/` | Backends + **`DataOperationDispatcher`** (existing). **`QueryResultEngineAutoConfiguration`**: **`MillDataQueryProperties`** (`mill.data.query.*`), **`ResultMarshallerRegistry`**, **`QueryResultExecutionService`** when a dispatcher bean exists — **no** MVC; enables in-process sessions **without** **`mill-data-query-service`**. |
| **`mill-data-query-service`** | `services/mill-data-query-service/` | Kotlin Spring MVC REST, **`Authentication` → `CallerContext`**, Springdoc OpenAPI. **Java only** for **`@ConfigurationProperties`** under **`mill.data.services.query.*`** (`QueryResultServiceProperties`). |
| **`mill-data-backend-core`** | existing | **`DataOperationDispatcher`**, **`VectorBlock`**, execution types. |
| **`apps/mill-service`** | `apps/mill-service/` | **`implementation(project(":services:mill-data-query-service"))`** so the REST surface is packaged. |

**Includes:** root **`settings.gradle.kts`** must list **`:data:mill-data-query`** and **`:services:mill-data-query-service`**.

---

## Runtime composition

```text
apps/mill-service
  └── mill-data-query-service (REST, /api/v1/query/…)
  └── mill-data-autoconfigure
        ├── … (dispatcher + backends)
        └── QueryResultEngineAutoConfiguration → mill-data-query (QueryResultExecutionService, sessions, marshallers)
              └── mill-data-backend-core (DataOperationDispatcher)
```

**Beans (conceptual):**

- **Session store** — Caffeine-backed map from **`executionId`** to session state (buffers, marshaller defaults, **`epoch`**, **tenant**).
- **`ResultMarshallerRegistry`** — built-ins (**`rows-objects`**, **`rows-compact-batch`**, …) plus **`ServiceLoader`**-discovered providers; selection order on HTTP documented below.
- **Controllers** — parse/validate HTTP, build **`CallerContext`**, call core, set **`Content-Type`** from the **selected** marshaller.

Optional: **`ConditionalOnService`** on the REST module if the repo standard is to gate optional HTTP surfaces (mirror export service pattern).

---

## Session ownership (tenant only)

At **create**, the session stores **one** string **tenant** (e.g. **`userId`**, OIDC **`sub`**, or stable principal **name** — whatever **`mill-service`** exposes as per-request identity).

On **every** read/write (**`GET /api/v1/query/{executionId}`** in either mode, **`DELETE`**), the core compares **current request tenant** to **stored tenant**.

| Situation | HTTP |
|-----------|------|
| Unauthenticated | **`401`** |
| Known **`executionId`**, tenant mismatch | **`403`** |
| Unknown **`executionId`** | **`404`** (no cross-tenant existence leak) |

**`CallerContext`:** the core accepts **`CallerContext`** on each operation; for v1 it **must** carry at least this **tenant** string. Extra correlation fields are optional and **not** used for ownership.

---

## HTTP resource model

**Base path (locked):** **`/api/v1/query/`**

| Method | Path | Role |
|--------|------|------|
| **`POST`** | `/api/v1/query` | Create session; optional include first page in body (see status semantics). |
| **`GET`** | `/api/v1/query/{executionId}` | **Query-driven:** without **`pageIndex`** → metadata (**`executionId`**, **`epoch`**, **`totalResult`**, **`defaultFormat`**). With **`pageIndex`** → one presentation page; optional **`pageSize`** (defaults **50**), **`format`**, **`epoch`**. **`pageSize`** without **`pageIndex`** → **`400`**. **`format`** / **`epoch`** without **`pageIndex`** → **`400`**. |
| **`DELETE`** | `/api/v1/query/{executionId}` | Deallocate session, evict Caffeine, release dispatcher-backed resources. Clients **should** call when done paging. |

**Optional:** **`GET /api/v1/query`** without id → **`405`** or omit from OpenAPI.

---

## Paging contract (`GET /api/v1/query/{executionId}?pageIndex=…`)

**Canonical paged request:** **`GET /api/v1/query/{executionId}?pageIndex=&pageSize=`** (optional **`format`**, **`epoch`**). **Metadata:** same path **without** **`pageIndex`**.

**Envelope** (for **`application/json`** paged responses: paging fields at top level; **`schema`** column metadata; marshaller payload under **`data`**):

| Field | Purpose |
|--------|---------|
| **`epoch`** | Session generation (starts **`0`** at create; in-process **`replace`** may increment — **HTTP** does not expose **`replace`**). Echoed on paged responses and in metadata. |
| **`pageIndex`** | **0-based** presentation page returned (after normalization). |
| **`pageSize`** | Requested page size for this call (after defaulting). |
| **`rowCount`** | Rows in this response (**`0 … pageSize`**). **`rowCount < pageSize`** ⇒ end of result for this execution. |
| **`totalResult`** | **Always present** — use JSON **`null`** when cardinality is **unknown**; **never omit** the property. |
| **`hasPrevious`** | **`true`** iff **`pageIndex > 0`** and read is valid for current **`epoch`**. |
| **`hasNext`** | If **`totalResult`** known: logical “more rows after this page”. If **`totalResult`** is **`null`**: **`false`** when **`rowCount < pageSize`**; when **`rowCount == pageSize`**, engine reports whether more may exist (buffer edge vs exhausted — core KDoc is normative). |
| **`schema`** | Column descriptors from **`VectorBlock`** schema (`name`, `type`, `precision`, `scale`, `length`, `nullable`, `idx`). |
| **`data`** | Marshaller payload (built-in JSON shapes in OpenAPI / WI-264). |

Internal mapping **`offset = pageIndex × pageSize`** against buffers is **not** a second client-facing contract.

---

## Server vs client buffering (backward paging)

**`DataOperationDispatcher`** / **`fetchResult`** is **forward-only**. **Backward** (**smaller `pageIndex`**) is entirely the execution service:

- **Full snapshot** path (under threshold): any **`pageIndex`** while session lives.
- **Sliding window:** **`executionBufferRows`** with **`backwardCacheBuffers`** / **`forwardCacheBuffers`** and **refill on miss** (may re-fetch / re-query).

**Caffeine** **`expireAfterAccess`** and **`DELETE`** drop server buffers; clients must not assume backward stays cheap after idle or eviction without a new **`POST`**.

**Client:** no required browser cache; optional client-side page cache is UX-only.

---

## Format negotiation and `Content-Type`

- **Standard MIME types** for built-ins and SPI marshallers (e.g. **`application/json`**, future **`text/csv`**). Avoid Mill-vendor MIME as the **primary** normative type for built-in JSON row shapes.
- Each **`ResultMarshaller`** exposes authoritative **`Content-Type`** (and **`Accept`** it satisfies). The HTTP layer sets response **`Content-Type`** from the **selected** marshaller. Built-ins **`rows-objects`** and **`rows-compact-batch`** both use **`application/json`** — clients pick via **`format`** or session **`defaultFormat`**, not by **`Content-Type`** alone.

**Selection order for paged `GET /api/v1/query/{executionId}`:**

1. **`format`** query if present and valid → marshaller by format id  
2. Else match **`Accept`** against registered marshallers  
3. Else session **`defaultFormat`** from **`POST`** create body  
4. Else server default **`rows-objects`**

**Precedence:** **`format`** wins over **`Accept`** when both apply.

**Errors:** unknown **`format`** → **`400`**. **`Accept`** unsatisfiable (no **`format`** override) → **`406`**.

**JSON shape:** one object: envelope fields + **`schema`** (column metadata from the vector schema) + **`data`** (marshaller body).

---

## HTTP status semantics

| Case | Status |
|------|--------|
| **`POST`** create **only** (no first page in body) | **`201 Created`** (optional **`Location: /api/v1/query/{executionId}`**) |
| **`POST`** create **with** first paged-shaped page in same document | **`200 OK`** |
| **`DELETE`** success | **`204 No Content`**, **no** body |
| Malformed JSON, **`pageSize`** without **`pageIndex`**, bad **`pageIndex`/`pageSize`**, unknown **`format`**, contract violations | **`400`** |
| SQL / plan fails but request structurally valid (auth OK, limits OK) | **`422 Unprocessable Entity`** |
| **`GET /api/v1/query/{executionId}`** paged mode: **`Accept`** cannot be satisfied | **`406 Not Acceptable`** |
| **`GET /api/v1/query/{executionId}`** paged mode: stale **`epoch`** | **`409 Conflict`** (structured error **`code`**) |

---

## Concurrency and consistency

- **`epoch`:** included on metadata and paged **`GET`** responses. **HTTP** sessions keep **`epoch`** at **`0`** for the lifetime of the execution (no **`PUT`** replace). In-process **`QueryResultExecutionService.replace`** may still bump **`epoch`** for embedders that use it.
- **Optional `epoch` on paged `GET`:** mismatch ⇒ **`409`**. If **`epoch`** omitted, serve current epoch.
- **`DELETE` / eviction:** unknown id ⇒ **`404`** (not **`409`**).
- **Sliding window refills** may re-execute SQL — later pages may see **newer committed** data than earlier pages in the same session unless the **full snapshot** path applies (**weak read-level** consistency). Document in core KDoc and operator-facing UI copy as needed.

---

## In-process embedding vs REST

| Aspect | In-process (`mill-data-query`) | REST (`mill-data-query-service`) |
|--------|------------------------------|----------------------------------|
| **Entry** | **`QueryResultExecutionService`** (or façade bean name chosen in WI-262) | MVC controllers under **`/api/v1/query/`** |
| **Identity** | Caller supplies **`CallerContext`** with **tenant** each call | Derived from **`Authentication`** |
| **Paging / envelope** | Core returns the same logical envelope DTOs the REST layer maps to JSON | JSON + OpenAPI |
| **Marshalling** | Same registry / marshallers; may write to **`OutputStream`** or **`Consumer<ByteBuffer>`** | HTTP response body / streaming |

The core module **must not** depend on Spring Web types.

---

## SPI: `ResultMarshallerProvider`

- Providers register via Java **`ServiceLoader`** (**`META-INF/services/...`**).
- **Kotlin** implementations live in **`mill-data-query`** (or extension modules); Spring loads providers at startup and exposes a **`ResultMarshallerRegistry`** bean from **`QueryResultEngineAutoConfiguration`** in **`mill-data-autoconfigure`** (**WI-263**).
- **Collision policy** and discovery order: follow WI-263 / story (fail fast or deterministic merge — implement as specified there).

---

## Configuration

Prefixes follow **`apps/mill-service/src/main/resources/application.yml`** — **`mill.data.services.*`** for data-plane HTTP services, **`mill.data.*`** for shared data-layer tuning.

### Service layer — `mill.data.services.query.*`

**Java** [`QueryResultServiceProperties`](../../../services/mill-data-query-service/src/main/java/io/qpointz/mill/data/query/QueryResultServiceProperties.java) — **`@ConfigurationProperties(prefix = "mill.data.services.query")`** in **`mill-data-query-service`** (Spring Boot configuration processor → **`META-INF/spring-configuration-metadata.json`**).

**Typical keys** (mirror **`mill.data.services.http`** / **`export`**):

- **`enable`** — gates controllers and descriptors via **`@ConditionalOnService(value = "query", group = "data")`** → **`mill.data.services.query.enable`**  
- **`external-host`** — logical key into **`mill.application.hosts.externals`** for absolute URLs in discovery / OpenAPI samples (same semantics as HTTP export)  

### Core layer — `mill.data.query.*`

**Java** [`MillDataQueryProperties`](../../../data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/autoconfigure/data/query/MillDataQueryProperties.java) — **`@ConfigurationProperties(prefix = "mill.data.query")`**, wired in [`QueryResultEngineAutoConfiguration`](../../../data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/autoconfigure/data/query/QueryResultEngineAutoConfiguration.java) into **`QueryResultEngineSettings`**.

| Property | Type | Default | Purpose |
|----------|------|---------|---------|
| **`enabled`** | boolean | `true` | When **`false`**, **`QueryResultEngineAutoConfiguration`** does not register **`QueryResultExecutionService`** / registry beans (in-process + REST both require the engine). |
| **`max-materialized-rows`** | int | `100000` | Maximum rows read from **`execute`** in one scan (including a backward re-scan); aborts with an error beyond this cap. |
| **`max-cached-pages`** | int | `16` | Maximum number of presentation pages (fixed **`pageSize`** from the first materializing request) kept in memory. Forward paging trims the lowest page indices so at least **`pageIndex - (M - 1)`** remains (for **`M = max-cached-pages`**); backward inside that window does not re-query; backward outside it re-**`execute`** and prefetches the requested page plus up to **`M - 1`** following pages when the result is long enough. |
| **`default-fetch-size`** | int | `1024` | Dispatcher **`QueryExecutionConfig.fetchSize`** default on **`execute`**. |
| **`max-page-size`** | int | `10000` | Upper bound for presentation **`pageSize`** on paged **`GET /api/v1/query/{executionId}`**. |
| **`session-expire-after-access`** | Duration | `30m` | Caffeine idle eviction for sessions. |

The **Kotlin** core stays **Spring-free**; map **`MillDataQueryProperties`** into **`QueryResultEngineSettings`** at the **`@Bean`** boundary only.

**Do not** implement **`@ConfigurationProperties`** in Kotlin in these modules (avoids hand-written **`additional-spring-configuration-metadata.json`**) — see repository **[CLAUDE.md](../../../CLAUDE.md)**.

---

## Error model (summary)

| HTTP | Typical cause |
|------|----------------|
| **`400`** | Bad JSON, invalid paging, unknown **`format`**, contract violations |
| **`401`** | Unauthenticated |
| **`403`** | Known **`executionId`**, wrong tenant |
| **`404`** | Unknown **`executionId`** |
| **`406`** | **`Accept`** cannot be satisfied |
| **`409`** | Stale **`epoch`** on paged **`GET /api/v1/query/{executionId}`** |
| **`422`** | Valid request, SQL/plan execution failure |
| **`405`** | Unused optional route |

Structured error bodies: align with **`mill-service`** JSON error conventions and OpenAPI schemas (**WI-264**).

---

## Implementation conventions (short)

- **Kotlin** for **`mill-data-query`** and **`mill-data-query-service`** production code under **`src/main/kotlin`**.
- **Java** **only** for **`@ConfigurationProperties`**: **`mill.data.services.query.*`** on **`mill-data-query-service`**; **`mill.data.query.*`** on **`mill-data-autoconfigure`** (see **`MillDataQueryProperties`** / **`QueryResultEngineAutoConfiguration`**).
- **KDoc** / **JavaDoc** to **parameter** level on all new production API.

---

## Integration tests (Skymill)

- **`./gradlew :services:mill-data-query-service:testIT`**
- Profile **`skymill`**, **`flow-skymill-it.yaml`**, **`application-skymill.yml`**, **`skymill.datasets.dir`** pointing at **`test/datasets/skymill/`** (same pattern as **`mill-data-schema-core`** and **`MillGrpcSkymillQueryIT`**). The **`testIT`** Gradle suite sets **`skymill.datasets.dir`** via **`JvmTestSuite`**; flow YAML is registered with an absolute path from **`@DynamicPropertySource`** in **`MillDataQuerySkymillIT`**.
- Shared SQL expectations: **`test/it-querycases/skymill-sql.json`** (optional alignment for future cases).

Coverage goals include paging envelope (**`totalResult`** known vs **`null`**), format negotiation, **`epoch`** / **`409`**, **`DELETE`** / **`404`**, cross-tenant **`403`**, and **`POST`** **`201`** vs **`200`**.

---

## OpenAPI

Generated Springdoc output from **`mill-data-query-service`** controllers is **normative** alongside this document. Every route should declare **success and error** responses (**`200`/`201`/`204`**, **`400`**, **`401`**, **`403`**, **`404`**, **`406`**, **`409`**, **`422`**, **`405`** as applicable) per WI-264.

---

## Related design links

| Document | Relevance |
|----------|-----------|
| [mill-data-lane-onepager.md](mill-data-lane-onepager.md) | Where query execution sits in the wider data lane |
| [mill-configuration.md](mill-configuration.md) | Global configuration patterns |
| [../ui/mill-ui/BACKEND-API-REQUIREMENTS.md](../ui/mill-ui/BACKEND-API-REQUIREMENTS.md) | UI-facing HTTP requirements (**WI-265** updates Queries domain) |
| [../ui/mill-ui/ARCHITECTURE.md](../ui/mill-ui/ARCHITECTURE.md) | **`queryService`** / analysis (**WI-265**) |
| [data-export-service.md](data-export-service.md) | Parallel “adapter over engine” pattern on another transport |
| [../../workitems/completed/20260511-query-result-execution-service/STORY.md](../../workitems/completed/20260511-query-result-execution-service/STORY.md) | WI checklist, cold start, story closure table |

When adding new **`docs/design/**` prose that mentions “execute query over HTTP”, **link here** instead of duplicating the full contract.
