# Query result execution service (data layer)

Deliver **`data/mill-data-query`** (library) and **`services/mill-data-query-service`** (HTTP): programmatic-first execution sessions (**`VectorBlock`** pages + DTO projections), **Caffeine** idle eviction, **execution-buffer** window vs presentation **`pageSize`** (**`GET …/rows`** + **`pageIndex`/`pageSize`** — **Paging contract** below), **marshaller** sinks (**`OutputStream` / `Consumer<ByteBuffer>`**) with **`ServiceLoader`** SPI (**Kotlin** provider implementations; **`META-INF/services`** — WI-263) and a **Spring-assembled `ResultMarshallerRegistry`** bean (startup load — WI-263), optional **streaming HTTP** adapters (**`Flux<DataBuffer>`**), **REST** thin layer (**all** routes under **`/api/v1/query/`** — WI-264), **`PUT`** replace.

**Planning reference:** Cursor plan **`Data query-result service-a1cf6269.plan.md`** (workspace **`.cursor/plans/`**); **WI-265** creates [`docs/design/platform/query-result-execution-service.md`](../../../design/platform/query-result-execution-service.md).

**Related backlog:** **[D-8](../../BACKLOG.md)**  
**Consumer overlap:** **[U-13](../../BACKLOG.md)** / [`mill-ui-analysis-full-stack`](../mill-ui-analysis-full-stack/STORY.md) — **[WI-257](../mill-ui-analysis-full-stack/WI-257-analysis-queries-rest-api.md)** (saved-query **GET**s). **`POST /api/v1/queries/execute`** is **removed** (**breaking**): the **only** supported UI execution HTTP surface is **`/api/v1/query/…`** — **no** deprecation period, **no** compatibility controller, **no** dual normative contract (see **[`GAPS.md`](GAPS.md) §1**).

---

## HTTP API sketch (`/api/v1/query/…`)

**Base path (locked):** **`/api/v1/query/`** — every route added for **session query execution** lives under this prefix. Saved-query catalog remains **`/api/v1/queries`** (see **WI-257**); it is **not** part of this tree.

**Resource model:** **`query`** is the HTTP resource — **`POST /api/v1/query`** creates a **session**; **`/api/v1/query/{executionId}`** addresses one session instance. There is **no** nested **`/executions`** (or other sibling) collection under **`query`**. Path **`executionId`** is an **opaque** token for this API only (returned on create) — not a saved-query **`queryId`** from **`/api/v1/queries/{queryId}`**.

### Endpoint sketch

| Method | Path (sketch) | Role |
|--------|----------------|------|
| **`POST`** | `/api/v1/query` | **Create** session: SQL (or plan reference per core API), optional execution/presentation limits, optional default marshaller format. Returns opaque **`executionId`** and initial metadata (and optionally first page — **WI-264** locks details). |
| **`GET`** | `/api/v1/query/{executionId}` | **Metadata**: schema hints, truncation, required **`epoch`** (see **Concurrency, invalidation, and replace**), optional **`totalResult`** when known, marshaller hints, optional session defaults (e.g. default **`pageSize`**). **No** large row payload unless explicitly folded into this route in WI-264. Paging envelope on **`/rows`** is **normative** for navigation flags (see **Paging contract** below). |
| **`GET`** | `/api/v1/query/{executionId}/rows` | **Paged result** body: marshaller output for one presentation page. **Query params (locked):** **`pageIndex`**, **`pageSize`** (see **Paging contract**); optional **`format`** (**Format negotiation**); optional **`epoch`** (optimistic check — **Concurrency**). **Not** supported: **`offset` / `limit`** on this tree. |
| **`PUT`** | `/api/v1/query/{executionId}` | **Replace** SQL/plan for the **same** **`executionId`** (tear down prior buffers; same **owner** only). |
| **`DELETE`** | `/api/v1/query/{executionId}` | **Explicitly deallocate** server-side result state: remove the session, evict Caffeine entry, drop buffers, release dispatcher-backed cursors as applicable. Clients **should** call **`DELETE`** when done paging to free memory and bound resource use. |

**Optional (WI-264):** **`GET /api/v1/query`** without **`{executionId}`** — return **405** or omit from OpenAPI if unused. Streaming **`GET`** (chunked body or **`StreamingResponseBody` / `Flux<DataBuffer>`**) for large slices — still under **`/api/v1/query/…`**, not a second top-level API.

### Paging contract (locked)

**Canonical request:** **`GET /api/v1/query/{executionId}/rows?pageIndex=&pageSize=`** — **`pageIndex`** + **`pageSize`** document the public paging model on **`/api/v1/query/**`; optional **`format`**, **`epoch`** per other sections. Internally the core may map **`offset = pageIndex × pageSize`** against buffers or dispatcher fetch; that mapping is **not** a second client-facing contract.

**Response envelope** (for **`application/json`** **`/rows`** responses: paging fields at the top level plus marshaller body under **`data`** — **Format negotiation** below; field names and OpenAPI in **WI-264**):

| Field | Purpose |
|--------|---------|
| **`epoch`** | Monotonic session generation: **`0`** at **`create`**, **`+1`** on each successful **`replace`**. Echoed on every **`/rows`** response; must match **`GET` metadata** for the same instant. |
| **`pageIndex`** | Presentation page returned (**0-based**, same as request after normalization). |
| **`pageSize`** | Requested page size for this call (after defaulting). |
| **`rowCount`** | Actual rows in this response body (**0 … pageSize**); **`rowCount < pageSize`** implies end of result for this execution. |
| **`totalResult`** | Present only when total result cardinality is **known** for this execution; otherwise **omit** (or **`null`** — pick one in OpenAPI and keep consistent). |
| **`hasPrevious`** | **`true`** iff **`pageIndex > 0`** and the read is valid for the session **epoch** (see **WI-262** / **WI-264** for replace invalidation). |
| **`hasNext`** | If **`totalResult`** is known: **`hasNext`** iff more rows exist after this page (e.g. **`pageIndex × pageSize + rowCount < totalResult`**). If **`totalResult`** is unknown: **`hasNext = false`** when **`rowCount < pageSize`**; when **`rowCount == pageSize`**, **`hasNext`** is whatever the session engine reports (**not exhausted** vs buffer end — **WI-262** is normative). |

**WI-265** carries contract tests (total known, total unknown, partial last page) and BACKEND copy.

### Server and client buffering (backward paging)

**Dispatcher reality:** **`DataOperationDispatcher`** (**`submitQuery` / `fetchResult`**) is **forward-only** cursor paging over **`VectorBlock`** — no reverse iterator. **Backward** UI (**smaller `pageIndex`**) is entirely **`QueryResultExecutionService`** responsibility:

- **Full snapshot** path (under size threshold): any **`pageIndex`** is served from retained snapshot rows while the session lives.
- **Sliding-window** path: **`executionBufferRows`** with **`backwardCacheBuffers` / `forwardCacheBuffers`** plus **refill on miss** (may re-fetch / re-query per **WI-262**). KDoc must describe miss and exhaustion behavior.

**Idle eviction** (**Caffeine `expireAfterAccess`**) and **`replace` / `DELETE`** drop server buffers; clients must not assume backward requests stay cheap after eviction or long idle without a new **`POST`**.

**Client obligations:** **No** required browser-side page cache — backward navigation is **`GET …/rows`** with a **lower `pageIndex`**. **Optional** client cache of recent pages is a UX optimization only. Clients **should** call **`DELETE`** when done (above) to release server memory.

### Format negotiation and `Content-Type` (locked)

**Standard MIME types only** for built-ins and for any SPI marshaller: declare **IANA-registered** types (e.g. **`application/json`**, future **`text/csv`**) — **no** Mill-specific vendor `application/vnd.*` types as the normative primary type for **`rows-objects`** / **`rows-compact-batch`**.

**Authoritative wire type per marshaller:** each **`ResultMarshaller`** implementation exposes its response **`Content-Type`** (and the set of **`Accept`** values it can satisfy, typically the same or a small list) on the **WI-263** contract; the HTTP adapter sets the response **`Content-Type`** from the **selected** marshaller. Built-ins **`rows-objects`** and **`rows-compact-batch`** both use **`application/json`** — clients **must** use the **`format`** query (or session **`defaultFormat`**) to pick between them; **`Content-Type` alone** does not distinguish the two.

**Selection order** for **`GET …/rows`:** (1) **`format`** query parameter if present and valid → marshaller by **format id**; (2) else match **`Accept`** against registered marshallers’ declared types (**`*/*`** or **`application/json`** matches any marshaller that lists **`application/json`**); (3) else session **`defaultFormat`** from **`POST`/`PUT`** body; (4) else server default **`rows-objects`**.

**Precedence:** if **`format`** and **`Accept`** disagree on which marshaller to use, **`format` wins**.

**Errors:** unknown **`format`** id → **`400`**. **`Accept`** cannot be satisfied (no **`format`** override) → **`406`**.

**JSON response shape:** one **`application/json`** object: **paging envelope** fields (**`pageIndex`**, … — **Paging contract**) plus **one** marshaller-owned top-level property for the variant payload; **WI-264** OpenAPI locks the property name (**`data`** unless a clearer name is chosen consistently in OpenAPI + examples). Non-JSON or **streaming** responses remain possible per marshaller; they use their own standard **`Content-Type`** and documented body rules (**WI-264**).

### Concurrency, invalidation, and replace (locked)

**`epoch`:** Integer **`0`** when the session is **created**; incremented by **`1`** (or strictly monotonic) on each **successful** **`PUT` replace** for the same **`executionId`**. **`GET /api/v1/query/{executionId}`** (metadata) and **`GET …/rows`** responses **include** the current **`epoch`** (see **Paging contract** table).

**Optional optimistic check on `GET …/rows`:** Query parameter **`epoch`**. If the client sends it and it **does not equal** the server’s current session **`epoch`**, respond **`409 Conflict`** with structured **`error` + `code`** (stale view — e.g. **`replace`** completed since the client’s last metadata read). If **`epoch`** is **omitted**, the server serves the **current** epoch only — **no `409`** from **`replace`** alone because reads and **`replace`** are **serialized** (below).

**`replace` vs reads (same `executionId`):** **Per-session read–write ordering:** **`replace` is exclusive** — it **waits** for in-flight **`getPage`/`metadata`** to complete; **new reads block** until **`replace`** finishes. After **`replace`** returns, **no** **`/rows`** payload may come from **pre-replace** buffers. (**Implementation:** **`ReentrantReadWriteLock`** or equivalent on the session.)

**`DELETE` and Caffeine eviction:** Unknown **`executionId`** → **`404`** (same as today). **Not** **`409`**.

**Live data across refills (sliding window):** Refills may **re-execute** SQL; later pages can reflect **newer committed** source data than earlier pages in the **same** session — **not** a single stable snapshot unless the **full snapshot** materialization path applies. **WI-262** **KDoc** and **WI-265** design doc must state this **weak / read-level** consistency for operators.

### Structure (layers)

1. **HTTP (`mill-data-query-service`)** — Spring MVC controllers in **`services/`**: authentication → **`CallerContext`**, map HTTP errors (**`401`** unauthenticated, **`403`** wrong owner, **`404`** unknown **`executionId`**, **`409`** stale **`epoch`** on **`GET …/rows`** per **Concurrency**), delegate to core. **No** paging or envelope semantics duplicated in controllers — parse **`pageIndex`/`pageSize`**, validate bounds, delegate; envelope filled from core (**WI-264**). **OpenAPI:** controller methods declare **all** applicable responses (**`200`/`201`/`204`**, **`400`**, **`401`**, **`403`**, **`404`**, **`406`**, **`409`**, **`405`** if used, plus **`422`** or **`400`** for SQL/validation errors per **WI-264** error model) so **`springdoc-openapi`** output is complete.
2. **Core (`mill-data-query`)** — **`QueryResultExecutionService`**: Caffeine session store, buffer window + refill vs **`DataOperationDispatcher`** (**`submitQuery` / `fetchResult` / `execute`**), presentation **`pageSize`** vs **`executionBufferRows`**, **`epoch`** + **replace** serialization + invalidation per **Concurrency** above, marshaller invocation with **blocking** sinks.
3. **Payloads** — JSON metadata and control plane; **page body** shaped by marshaller (**`rows-objects`**, **`rows-compact-batch`**, … per **WI-263**). **`Content-Type`** and **format negotiation** — **Format negotiation** section above; **WI-264** + **WI-265** implement and test (**[`GAPS.md`](GAPS.md) §4** closed). **Concurrency / eviction** — **Concurrency** section; **WI-262** / **WI-265** tests; **[`GAPS.md`](GAPS.md) §5** closed.

Normative OpenAPI and example JSON land in **WI-264** (controllers) and **WI-265** (**BACKEND-API-REQUIREMENTS.md** + design doc). **OpenAPI on controllers:** every route documents **success and error** responses with **HTTP status codes** on the **`@RestController`** (Springdoc **`@Operation`**, **`@ApiResponse` / `@ApiResponses`**, or project-equivalent) so the generated spec matches runtime behaviour — **WI-264** (see **OpenAPI (controllers)** there).

---

## Modules and dependencies

### New Gradle modules

| Module                          | Path                                                                                   | Role                                                                                                                                                                                                                     | Main dependencies (Gradle)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| ------------------------------- | -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`mill-data-query`**    | [`data/mill-data-query/`](../../../../data/mill-data-query/) (new)       | Spring-free **Kotlin** (`src/main/kotlin`) **session engine**, **`QueryResultExecutionService`**, Caffeine, buffer/refill, **`ResultMarshaller`** + **`ResultMarshallerRegistry`**, **SPI** (**`ResultMarshallerProvider`**, **`META-INF/services`**) + built-in providers (**WI-262**, **WI-263**). **KDoc** on all production API down to **parameter** level.                                                                     | **`project(":data:mill-data-backend-core")`** (**`DataOperationDispatcher`**, **`VectorBlockIterator`** contract), **`libs.caffeine`**, logging bundle; tests: JUnit, AssertJ, Mockito per module conventions.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **`mill-data-query-service`** | [`services/mill-data-query-service/`](../../../../services/mill-data-query-service/) (new) | **Spring MVC** REST under **`/api/v1/query/`** — **Kotlin** controllers/services (`src/main/kotlin`); **Java** **only** for **`@ConfigurationProperties`** (`mill.query-result.*`) to generate **`spring-configuration-metadata.json`**. OpenAPI **`@Tag`**, optional streaming adapters, **`ConditionalOnService`** if aligned with export (**WI-264**). **KDoc**/**JavaDoc** down to **parameter** level on all new/changed production code. | **`project(":data:mill-data-query")`**, **`project(":data:mill-data-autoconfigure")`** (dispatcher + backend beans in composed **`mill-service`**), **`project(":data:mill-data-backend-core")`** (types in controller signatures / tests), **`project(":services:mill-service-api")`** (same pattern as [`mill-export-service`](../../../../services/mill-export-service/build.gradle.kts); library stays in **`data/`** like **`mill-data-schema-core`** + HTTP in **`services/`**), **`libs.boot.starter.webmvc`**, Jackson, **`libs.springdoc.openapi.starter.webmvc.ui`**; optional WebFlux / **`DataBuffer`** only here. Spring Security types for **`Authentication` → `CallerContext`** bridge (compile scope consistent with other MVC services). |

**Register includes** in **[`settings.gradle.kts`](../../../../settings.gradle.kts):** **`include(":data:mill-data-query")`** with other **`data:`** modules; **`include(":services:mill-data-query-service")`** with other **`services:`** modules.

### Existing modules to touch

| Location | Change |
|----------|--------|
| **[`apps/mill-service/build.gradle.kts`](../../../../apps/mill-service/build.gradle.kts)** | **`implementation(project(":services:mill-data-query-service"))`** so the app composes query-result beans with **`mill-data-autoconfigure`** (same pattern as other data-facing services). |
| **[`settings.gradle.kts`](../../../../settings.gradle.kts)** | **`include`** for both new modules (above). |

**Non-code (breaking):** [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) and **`mill-ui`** **`queryService`** **must** switch to **`/api/v1/query/**`; **[WI-257](../mill-ui-analysis-full-stack/WI-257-analysis-queries-rest-api.md)** stays on saved-query **GET**s only — **WI-265** + UI work (**no** retained **`/queries/execute`** docs or mocks).

### Runtime composition (conceptual)

```text
apps/mill-service
  └── mill-data-query-service (REST, /api/v1/query/…)
        └── mill-data-query (sessions, marshallers)
              └── mill-data-backend-core (DataOperationDispatcher)
        └── mill-data-autoconfigure (dispatcher + backend wiring; already on mill-service)
```

**`mill-data-query`** must **not** depend on Spring Web, Reactor, or security APIs — only **`CallerContext`** (story-defined or shared type from a small **`core`** contract if introduced in **WI-262**).

---

## Cold start (from empty branch)

Branching (**[RULES](../../RULES.md)**): **`git fetch origin && git checkout -b feat/query-result-execution-service origin/dev`** (or reuse your active story branch consistently with MR target).

Working directory: **repository root** (where **`settings.gradle.kts`** and **`./gradlew`** live).

### Preconditions

| Check | Detail |
|--------|--------|
| **JDK / Gradle** | **Java 21**; use root **`./gradlew`** (**[CLAUDE.md](../../../../CLAUDE.md)** build commands). |
| **Skymill fixture** | **WI-265**: dataset tree **`test/datasets/skymill/`** committed in repo (**parquet**, **`skymill-canonical.yaml`**, README). No separate download unless your clone is shallow-incomplete. |
| **Deps catalog** | **Caffeine**: **`libs.caffeine`** in [`libs.versions.toml`](../../../../libs.versions.toml). |

### Conventions to copy (minimal reading list)

| Need | Follow |
|------|--------|
| **Data-layer Spring MVC module** packaged into **`mill-service`** | [`data/mill-data-schema-service/build.gradle.kts`](../../../../data/mill-data-schema-service/build.gradle.kts) (**`publishArtifacts = false`**, **`testIT`**, OpenAPI MVC). **Language:** follow **Implementation conventions** below (**Kotlin** + **Java** properties only). |
| **`testIT` + `JvmTestSuite` + `skymill.datasets.dir`** | [`data/mill-data-schema-core/build.gradle.kts`](../../../../data/mill-data-schema-core/build.gradle.kts) — **`targets { all { testTask.configure { systemProperty("skymill.datasets.dir", …) }}}`**. |
| **Skymill profile + Flow YAML slice** | [`services/mill-data-grpc-service/src/testIT/resources/application-skymill.yml`](../../../../services/mill-data-grpc-service/src/testIT/resources/application-skymill.yml), [`flow-skymill-it.yaml`](../../../../services/mill-data-grpc-service/src/testIT/resources/flow-skymill-it.yaml), driver IT [`MillGrpcSkymillQueryIT`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java). |
| **Shared SQL corpus** | [`test/it-querycases/skymill-sql.json`](../../../../test/it-querycases/skymill-sql.json) (keep expectations aligned across transports). |
| **Execution API** | [`DataOperationDispatcher`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcher.java), proto **[`proto/data_connect_svc.proto`](../../../../proto/data_connect_svc.proto)** — **`VectorBlock`**, **`QueryExecutionConfig.fetchSize`**, **`submitQuery` / `fetchResult` / `execute`**. |
| **REST conditional activation** | [`ConditionalOnService`](../../../../core/mill-annotations/src/main/java/io/qpointz/mill/annotations/service/ConditionalOnService.java); export sample [`ExportRestController`](../../../../services/mill-export-service/src/main/java/io/qpointz/mill/export/ExportRestController.java). |
| **`mill-service` inclusion** | [`apps/mill-service/build.gradle.kts`](../../../../apps/mill-service/build.gradle.kts) — add **`implementation(project(":services:mill-data-query-service"))`** in **WI-264**. |

### Bootstrap sequence (matches WIs)

1. **`settings.gradle.kts`** — **`include(":data:mill-data-query")`** (under **`data:`**) and **`include(":services:mill-data-query-service")`** (under **`services:`**).  
2. **WI-262** — Gradle **`mill-data-query`** — **Kotlin** (`src/main/kotlin`); **`io.qpointz.plugins.mill`** + **`project(":data:mill-data-backend-core")`**, **`libs.caffeine`**. Prefer **dual test suites** per **[CLAUDE.md](../../../../CLAUDE.md)** (**`testing { suites { testIT … } }`**); use **`testIT`** in **`mill-data-query`** only when a Spring slice is required (otherwise **`test`** only). Implement session + dispatcher wiring; **`./gradlew :data:mill-data-query:test`**.  
3. **WI-263** — Marshallers inside **core** (**Kotlin**, blocking sinks) + **SPI** + **`ResultMarshallerRegistry`**; Spring **`ServiceLoader`** wiring via **Kotlin** `@Configuration` / `@Bean` (**Java** only for **`@ConfigurationProperties`** — see **Implementation conventions**).  
4. **WI-264** — **`mill-data-query-service`** under **`services/`**, MVC + **`mill-service`** dependency; add **`testIT`** wired for **Skymill** (profile, **`flow-skymill-it.yaml`**, **`application-skymill.yml`**, **`skymill.datasets.dir`** — see **Conventions to copy**); **`./gradlew :services:mill-data-query-service:test`** + **`./gradlew :services:mill-data-query-service:testIT`**; smoke **`apps/mill-service`**: **`./gradlew :apps/mill-service:compileJava`** (or **`build -x test`** locally).  
5. **WI-265** — Extend Skymill **`testIT`** coverage + design doc under **`docs/design/platform/`**, update [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md); **`./gradlew :services:mill-data-query-service:testIT`** green.  

### Verification commands (after modules exist)

```bash
# From repo root
./gradlew :data:mill-data-query:test
./gradlew :services:mill-data-query-service:test
./gradlew :services:mill-data-query-service:testIT   # Skymill — WI-264 suite + WI-265 coverage
```

### Implementation conventions (locked)

| Topic | Rule |
| ----- | ---- |
| **Language** | **`mill-data-query`** and **`mill-data-query-service`** production code in **Kotlin** under **`src/main/kotlin`** (domain, sessions, marshallers, SPI providers, controllers, bridges, **`@Configuration`** / **`@Bean`** that are **not** metadata-bound). |
| **Java (Spring configuration metadata only)** | **Java** is used **only** for **`@ConfigurationProperties`** types (e.g. `mill.query-result.*`) so **`spring-boot-configuration-processor`** generates **`META-INF/spring-configuration-metadata.json`** automatically — **do not** implement **`@ConfigurationProperties`** in Kotlin in these modules (avoids hand-written **`additional-spring-configuration-metadata.json`** per **[CLAUDE.md](../../../../CLAUDE.md)**). |
| **Documentation** | **KDoc** on all new/changed **Kotlin** production types, **constructors**, **properties**, **functions**, and **every method parameter**. **JavaDoc** to the **same depth** on **Java** configuration classes. (Repository standard: down to **parameter** level.) |
| **`testIT` (Skymill)** | Integration tests run against the **Skymill** schema/dataset: **`@ActiveProfiles("skymill")`**, **`src/testIT/resources/flow-skymill-it.yaml`**, **`application-skymill.yml`** as required by the slice, and **`JvmTestSuite`** **`systemProperty("skymill.datasets.dir", rootProject.file("test/datasets/skymill").absolutePath)`** on the **`testIT`** task. **References:** [`MillGrpcSkymillQueryIT.java`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java), [`mill-data-schema-core/build.gradle.kts`](../../../../data/mill-data-schema-core/build.gradle.kts) (**`testIT`** **`systemProperty`** block), shared expectations **[`test/it-querycases/skymill-sql.json`](../../../../test/it-querycases/skymill-sql.json)**. |

### Story closure — reconcile `docs/design`

This story **replaces** (**breaking**) earlier `docs/design` material that assumed a **one-shot JSON wrapper** over the data plane (**`POST /api/v1/queries/execute`**, **B-4** “JSON query execution wrapper”, **G-10** jet-vs-UI shape, **ARCHITECTURE** Queries row, and related UI inventory prose). **Normative** execution sessions for the composed **`mill-service`** + **`mill-ui`** stack are **`POST /api/v1/query`**, **`GET /api/v1/query/{executionId}`** (metadata), **`GET /api/v1/query/{executionId}/rows`** (paged data), and lifecycle **`PUT`/`DELETE`** on **`{executionId}`** (see **HTTP API sketch** above and **[`GAPS.md`](GAPS.md) §1**).

**WI-265** carries the main edits (canonical platform doc + **BACKEND-API-REQUIREMENTS**). **Before story archive** (**[`RULES.md`](../../RULES.md)** — move to **`docs/workitems/completed/…`**), verify every row below is **fully updated**: **no** “deprecated” execute route, **no** “temporary dual contract”, **no** leaving the old path documented as supported.

| Document | Action |
| -------- | ------ |
| [`docs/design/platform/query-result-execution-service.md`](../../../design/platform/query-result-execution-service.md) | **Create** in **WI-265** — canonical architecture (**SPI**, registry, sessions, **`DELETE`** deallocation); state **breaking** replacement of the old execute path. |
| [`docs/design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md) | **WI-265** — **Delete** the **`POST /api/v1/queries/execute`** contract from the normative Queries domain (**breaking** — do **not** keep a deprecation section). |
| [`docs/design/ui/mill-ui/BACKEND-BACKLOG.md`](../../../design/ui/mill-ui/BACKEND-BACKLOG.md) | **WI-265** — Rewrite **G-10** / **B-4** (or remove obsolete rows) so they describe **`/api/v1/query/**` + **D-8** only — **no** parallel “JSON execute wrapper” backlog item for the old URL. |
| [`docs/design/ui/mill-ui/ARCHITECTURE.md`](../../../design/ui/mill-ui/ARCHITECTURE.md) | **WI-265** — **Queries** / **`queryService`**: **`/api/v1/query/**` session model only; **remove** **`POST /api/v1/queries/execute`** from normative tables (**no** “historical” stub row). |
| [`docs/design/ui/mill-ui/UI-ELEMENT-INVENTORY.md`](../../../design/ui/mill-ui/UI-ELEMENT-INVENTORY.md) | **WI-265** — **`queryService`** / analysis execute: document **`/api/v1/query/**` only (**same** delivery as **`mill-ui`** client — **no** mock **`/queries/execute`** left as the documented default). |

**Additional `docs/design` files** (same **WI-265** sweep if they still imply **mill-ui** uses **`/queries/execute`** for HTTP): [`docs/design/platform/data-export-service.md`](../../../design/platform/data-export-service.md), [`docs/design/platform/CODEBASE_ANALYSIS_CURRENT.md`](../../../design/platform/CODEBASE_ANALYSIS_CURRENT.md), [`docs/design/client/py-cold-start.md`](../../../design/client/py-cold-start.md), [`docs/design/client/01-adonet-provider-start-here.md`](../../../design/client/01-adonet-provider-start-here.md) — **fix or remove** the wrong implication; a **single** pointer to **`query-result-execution-service.md`** is fine **only** where the topic is cross-transport context (**no** deprecation callouts).

---

## Tracker

| WI | Document | Status | Depends on | Summary |
|----|----------|--------|------------|---------|
| **WI-262** | [`WI-262-query-result-core-sessions.md`](WI-262-query-result-core-sessions.md) | planned | — | **`mill-data-query`** (**Kotlin**), sessions, Caffeine, buffer window, **`VectorBlock`**, **KDoc** |
| **WI-263** | [`WI-263-query-result-marshallers.md`](WI-263-query-result-marshallers.md) | planned | WI-262 | Marshaller SPI (**Kotlin** core) + Spring registry bean, **KDoc** |
| **WI-264** | [`WI-264-query-result-rest-and-wiring.md`](WI-264-query-result-rest-and-wiring.md) | planned | WI-262, WI-263 | **Kotlin** REST + **Java** `@ConfigurationProperties`, **`mill-service`**, **Skymill `testIT`** |
| **WI-265** | [`WI-265-query-result-tests-and-docs.md`](WI-265-query-result-tests-and-docs.md) | planned | WI-264 | Skymill **`testIT`** depth, platform + **BACKEND-API** + **`docs/design` sweep** (**breaking**), design doc notes language/docs policy |

**Placement ([RULES.md](../../RULES.md)):** folder stays **`planned/`** until the first **`[x]`** in **Work Items**; then **`git mv`** → **`docs/workitems/in-progress/query-result-execution-service/`** in that WI’s commit.

## Work Items

Normative checklist (must match **`Tracker`**). Set **`[ ]` → `[x]`** when the WI is **done** (**one commit = full intentional tree** per **RULES.md**).

- [ ] **WI-262** — **`mill-data-query`** (**Kotlin**, **KDoc**): programmatic execution sessions + Caffeine + buffer window (`WI-262-query-result-core-sessions.md`)
- [ ] **WI-263** — Result marshaller SPI + Spring registry + built-in formats + streaming sinks (**Kotlin**, **KDoc**) (`WI-263-query-result-marshallers.md`)
- [ ] **WI-264** — REST (**Kotlin**) + **Java** `@ConfigurationProperties` + **`mill-service`** + **Skymill `testIT`** + optional streaming HTTP (`WI-264-query-result-rest-and-wiring.md`)
- [ ] **WI-265** — Skymill **`testIT`** depth, platform design doc (language + **Skymill IT** policy), **`docs/design` reconciliation** (see **Story closure** above), BACKEND requirements, **Java** metadata only (`WI-265-query-result-tests-and-docs.md`)
