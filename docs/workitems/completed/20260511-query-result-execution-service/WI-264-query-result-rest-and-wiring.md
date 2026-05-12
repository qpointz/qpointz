# WI-264 — REST surface + mill-service wiring

Status: `done`  
Type: `feature`  
Area: `services`  
Backlog refs: **D-8**, **U-13** (consumer)

**Story:** [`STORY.md`](STORY.md) — **WI-264** (tracker row 3). **Delivery:** when this WI is finished, mark its tracker + **[`STORY.md`](STORY.md)** **Tracker** / **Work Items**, then **one commit** for the full tree (**[`RULES.md`](../../RULES.md)**).

## Language

- **Kotlin** — controllers, HTTP mapping, security bridge, Spring **`@Configuration`** in **`mill-data-query-service`** under **`src/main/kotlin`**.
- **Java** — **`@ConfigurationProperties(prefix = "mill.data.services.query")`** (and any other metadata-bound keys for this module) under **`src/main/java`** **only**, with **`spring-boot-configuration-processor`**.

## Tracker (this WI)

- [x] Gradle module **`services/mill-data-query-service`** — **`src/main/kotlin`** for controllers/services/config (**Kotlin**); **`src/main/java`** **only** for **`@ConfigurationProperties(prefix = "mill.data.services.query")`** + **`spring-boot-configuration-processor`** (generated **`spring-configuration-metadata.json`** — see **[`STORY.md`](STORY.md)** **Implementation conventions**); OpenAPI **`@Tag`** on the controller class
- [x] **OpenAPI on every handler:** Springdoc **`@Operation`** plus **`@ApiResponse` / `@ApiResponses`** (or project-standard OpenAPI 3 annotations) so each **`POST`/`GET`/`DELETE`** under **`/api/v1/query/**` documents **success** and **error** status codes and response bodies/schemas — see **OpenAPI (controllers)** subsection below; generated spec must stay in sync with **`MockMvc`** / **`WebTestClient`** expectations
- [x] **Request mapping** (no **`/executions`** segment): **`POST /api/v1/query`** (create); **query-driven** **`GET /api/v1/query/{executionId}`** — without **`pageIndex`**: metadata; with **`pageIndex`**: **`pageSize`**, **`format`**, **`epoch`** optional (**[`STORY.md`](STORY.md)** **Format negotiation**, **Concurrency**); **`DELETE /api/v1/query/{executionId}`** → **`core`** **`delete`** (**explicit deallocation** per **[`STORY.md`](STORY.md)**). **`replace`** is **not** an HTTP route (in-process **`QueryResultExecutionService`** only).
- [x] **Breaking — no deprecation:** **do not** register **`POST /api/v1/queries/execute`** or any compatibility shim (see **[`GAPS.md`](GAPS.md) §1**)
- [x] Thin REST: **all** handlers delegate **only** to **`mill-data-query`** (no duplicated paging); inject **`ResultMarshallerRegistry`** (SPI-backed bean from **WI-263**) — resolve **`format`** / **`Accept`** per **[`STORY.md`](STORY.md)** **Format negotiation**; set response **`Content-Type`** from selected marshaller; **`400`** / **`406`** as specified there
- [x] **`Authentication` → `CallerContext`** bridge: populate **tenant** only (e.g. **`userId`** / **`sub`** / principal **name** — one stable string per **[`STORY.md`](STORY.md)** **Session ownership**); **`401`** unauthenticated; **`403`** known **`executionId`** but **stored tenant ≠ current tenant**; **`404`** unknown **`executionId`**
- [x] **`@ConditionalOnService(value = "query", group = "data")`** (or project equivalent) on query-result MVC config/controllers — reads **`mill.data.services.query.enable`** (same pattern as **`export`** / **`http`**)
- [x] **`apps/mill-service`**: **`implementation(project(":services:mill-data-query-service"))`**
- [x] Optional **`StreamingResponseBody` / `Flux<DataBuffer>`** adapters (deps **scoped to `-service`**)

## Goal

Introduce **`services/mill-data-query-service`**: MVC controllers with **all** execution-session routes under **`/api/v1/query/`** — **`POST /api/v1/query`** (create) and **query-driven** **`GET /api/v1/query/{executionId}`** (metadata **or** paged slice) plus **`DELETE`** (**no** **`/executions`** segment). Controllers **delegating exclusively** into **`mill-data-query`** — **no duplicated** paging logic. **Only** this tree is the public execution surface — **no** legacy **`/queries/execute`** endpoint.

Expose:

- **`POST`** create, **`GET {executionId}`** metadata or **paged** body when **`pageIndex`** is present (**`pageSize`** only with **`pageIndex`** — **[`STORY.md`](STORY.md)**), **`DELETE {executionId}`** (deallocate session + result buffers — **core** `delete`). No public **`offset`/`limit`** pair on **`/api/v1/query/**`.
- Unified **page envelope** on **paged `GET`**: **`epoch`**, **`pageIndex`**, **`pageSize`**, **`rowCount`**, **`totalResult`** (**always** serialized; **`null`** = unknown per **[`STORY.md`](STORY.md)**), **`hasNext`**, **`hasPrevious`**, **`schema`**, plus marshaller payload under the OpenAPI-locked top-level property ( **`data`** per **[`STORY.md`](STORY.md)** unless renamed consistently) — semantics per **[`STORY.md`](STORY.md)** **Paging contract**; **`400`** on invalid **`pageIndex`** / **`pageSize`** / unknown **`format`**
- **Format negotiation** per **[`STORY.md`](STORY.md)** — standard MIME only; **`406`** when **`Accept`** cannot be satisfied and **`format`** is absent
- Optional **chunked/streaming** responses (**`StreamingResponseBody`**, **`Flux<DataBuffer>`**, etc.) bridging **WI-263** marshaller **sink** APIs

Wire **`implementation(project(":services:mill-data-query-service"))`** into **`apps/mill-service`**.

**Optional:** **`ConditionalOnService`**/`mill` annotations parity with [`ExportRestController`](../../../../services/mill-export-service/src/main/java/io/qpointz/mill/export/ExportRestController.java).

## Scope

1. **`mill-data-query-service` Gradle** + **Java** **`@ConfigurationProperties(prefix = "mill.data.services.query")`** + **Kotlin** MVC (**`@RestController`**, **`Authentication` → `CallerContext`** bridge per **[`STORY.md`](STORY.md)** **Session ownership**) + Springdoc **OpenAPI** annotations (**`@Tag`**, **`@Operation`**, **`@ApiResponse`/`@ApiResponses`**) on **every** handler per the **OpenAPI (controllers)** subsection below; **KDoc**/**JavaDoc** to **parameter** level on all new/changed production code. **Primary** Skymill **`testIT`** lives in this WI (**[`STORY.md`](STORY.md)** **Work item order**); **WI-265** adds docs + sweep + **supplementary** tests only.
2. **`JvmTestSuite` `testIT`** with **Skymill** profile + **`skymill.datasets.dir`** (mirror **[`mill-data-schema-core/build.gradle.kts`](../../../../data/mill-data-schema-core/build.gradle.kts)** and **[`MillGrpcSkymillQueryIT`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java)**); REST **`testIT`** exercises **`/api/v1/query/**` against Skymill-backed dispatcher.
3. **`mill-service`** dependency + bean visibility.
4. OpenAPI / controller names use path parameter **`executionId`** (distinct from saved **`queryId`** on **`/api/v1/queries/{queryId}`**). Review generated **`/v3/api-docs`** (or configured path) in **`testIT`** or manual checklist — no operation missing **`4xx`** entries that the implementation actually returns.

### Paging and buffering (normative for this WI)

Implement exactly **[`STORY.md`](STORY.md)** sections **Paging contract** and **Server and client buffering (backward paging)** — OpenAPI + examples for **paged** **`GET /api/v1/query/{executionId}?pageIndex=…`**, envelope fields (**including `epoch`**, **`totalResult`** as **required** property with **`nullable: true`** — unknown = **`null`**, never omit), defaults, and max **`pageSize`**. Document that **backward** navigation uses the **same** route with a **lower `pageIndex`**; server-side **`backwardCacheBuffers`** / snapshot behavior is **WI-262**; clients have **no** required local page cache.

### Format negotiation (normative for this WI)

Implement **[`STORY.md`](STORY.md)** **Format negotiation and `Content-Type`**: optional **`format`** on **paged `GET`** (with **`pageIndex`**); **`Accept`** handling; selection order and **`format`**-wins precedence; **`400`** / **`406`**; single JSON object = envelope + **`data`**; response **`Content-Type`** from marshaller metadata (**WI-263**). OpenAPI documents built-in **`application/json`** responses and **`format`** enum for **`rows-objects`** / **`rows-compact-batch`**.

### HTTP status semantics (normative for this WI)

Implement **[`STORY.md`](STORY.md)** **HTTP status semantics**: **`POST`** → **`201`** when response is creation-only, **`200`** when first page is included; **`DELETE`** → **`204`** empty body; **`422`** for valid-request SQL/plan failures on **`POST`**; **`400`** for malformed client input. Document both **`POST`** success variants in OpenAPI (or a request flag that forces one path).

### Concurrency and `epoch` (normative for this WI)

Implement **[`STORY.md`](STORY.md)** **Concurrency, invalidation, and replace**: metadata + **paged `GET`** include **`epoch`**; optional **`epoch`** query on **paged `GET`** → **`409`** when stale; **`replace`** serialized vs reads per **STORY**; **`404`** after **`DELETE`** / eviction. OpenAPI documents **`409`** on **paged `GET`** and **`epoch`** in success schemas.

### OpenAPI (controllers) — response codes

Annotations live **on the controller** (or dedicated operation interfaces if the project uses that pattern) so **`libs.springdoc.openapi.starter.webmvc.ui`** produces a complete spec **without** hand-maintaining a parallel YAML file.

**Minimum documented statuses per route** (per **[`STORY.md`](STORY.md)** **HTTP status semantics**):

| Route | Success | Client / auth errors | Other |
|--------|---------|----------------------|--------|
| **`POST /api/v1/query`** | **`201`** (creation-only — **no** first page in body) **or** **`200`** (creation + first page — document both response schemas) | **`400`** (malformed body, limits, bad **`defaultFormat`**), **`401`**, **`403`** (wrong tenant) | **`422`** (well-formed request; SQL/plan cannot execute) |
| **`GET /api/v1/query/{executionId}`** (no **`pageIndex`**) | **`200`** + metadata schema | **`400`** (e.g. **`pageSize`** / **`format`** / **`epoch`** without **`pageIndex`**), **`401`**, **`403`** (wrong tenant), **`404`** | — |
| **`GET /api/v1/query/{executionId}`** (with **`pageIndex`**) | **`200`** + envelope + **`data`** schema(s) per **`format`** | **`400`** (**`pageIndex`/`pageSize`**, unknown **`format`**), **`401`**, **`403`** (wrong tenant), **`404`** | **`406`** (**`Accept`** unsatisfiable); **`409`** (optional **`epoch`** query **≠** current — **[`STORY.md`](STORY.md)** **Concurrency**) |
| **`DELETE /api/v1/query/{executionId}`** | **`204`** (no body) | **`401`**, **`403`** (wrong tenant), **`404`** | — |
| **`GET /api/v1/query`** (optional) | — | — | **`405`** if implemented |

Reuse or reference a **shared error/problem** schema in OpenAPI if the repo already defines one for **`mill-service`**; otherwise define a small **`error` + `code`** object inline for this **`@Tag`** and reference it from **`@ApiResponse`** entries.

## Acceptance

- **`testIT`** (Skymill) proving controller + **`DataOperationDispatcher`** integration — **`@ActiveProfiles("skymill")`**, **`flow-skymill-it.yaml`** / **`application-skymill.yml`** as needed, **`skymill.datasets.dir`** on **`testIT`** task; SQL aligned with **[`test/it-querycases/skymill-sql.json`](../../../../test/it-querycases/skymill-sql.json)** where practical (same pattern as **[`MillGrpcSkymillQueryIT`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java)**). At least one **paged `GET /api/v1/query/{executionId}?pageIndex=…`** sequence that requests **page 1 then page 0** (backward) and asserts envelope consistency. At least one **paged `GET`** per built-in **`format`** with **`Content-Type: application/json`** and distinct **`data`** shapes; **`400`** for unknown **`format`**; **`406`** for unsatisfiable **`Accept`** (no **`format`** override).
- Error model: **`404`** unknown **`executionId`**, **`403`** known id but **wrong tenant**, **`401`** unauthenticated; **`422`** for SQL/plan failures on **`POST /api/v1/query`** with structured **`error` + `code`**; **`400`** for malformed client input.
- **`DELETE /api/v1/query/{executionId}`** covered (**`204`** empty body; session removed; follow-up **`GET`** → **`404`**).
- **`POST /api/v1/query`:** **`testIT`** (or unit) covering **`201`** creation-only vs **`200`** with first page when that mode is enabled; **`422`** on invalid SQL with valid JSON body.
- **OpenAPI:** spot-check (or automate) that **each** **`/api/v1/query/**` operation lists the **OpenAPI (controllers)** response codes; **`406`** / **`409`** appear only on **paged `GET`** (with **`pageIndex`**); **`DELETE`** documents **`204`** only (no **`200`** success variant).
- **KDoc**/**JavaDoc** complete (**parameter** level) on all new/changed production types in this module.
- **`epoch` on paged `GET`:** **`testIT`** or unit flow proving a **paged `GET`** with **`epoch`** **≠** current session value returns **`409`** when that check is enabled; **`GET`** after **`DELETE`** / eviction → **`404`**. (Optional separate coverage: in-process **`replace`** increments **`epoch`** — **WI-262** / service tests.)

## Depends on

**WI-262**, **WI-263**

## Notes

Harmonize with **[`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)** in **WI-265**; additive fields preferred. Public path model, paging, **format negotiation**, **HTTP status semantics** (**`data`**, **`POST` `201`/`200`**, **`DELETE` `204`**, **`422`** SQL), and **concurrency / `epoch`** are fixed in **[`STORY.md`](STORY.md)**; **[`GAPS.md`](GAPS.md) §1** / **§3** / **§4** / **§5** are **closed** in **STORY** + **WI-262**–**WI-265**.
