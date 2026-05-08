# WI-264 — REST surface + mill-service wiring

Status: `planned`  
Type: `feature`  
Area: `services`  
Backlog refs: **D-8**, **U-13** (consumer)

**Story:** [`STORY.md`](STORY.md) — **WI-264** (tracker row 3).

## Tracker (this WI)

- [ ] Gradle module **`services/mill-data-query-service`** — **`src/main/kotlin`** for controllers/services/config (**Kotlin**); **`src/main/java`** **only** for **`@ConfigurationProperties`** (`mill.query-result.*`) + **`spring-boot-configuration-processor`** (generated **`spring-configuration-metadata.json`** — see **[`STORY.md`](STORY.md)** **Implementation conventions**); OpenAPI **`@Tag`** on the controller class
- [ ] **OpenAPI on every handler:** Springdoc **`@Operation`** plus **`@ApiResponse` / `@ApiResponses`** (or project-standard OpenAPI 3 annotations) so each **`POST`/`GET`/`PUT`/`DELETE`** under **`/api/v1/query/**` documents **success** and **error** status codes and response bodies/schemas — see **OpenAPI (controllers)** subsection below; generated spec must stay in sync with **`MockMvc`** / **`WebTestClient`** expectations
- [ ] **Request mapping** (no **`/executions`** segment): **`POST /api/v1/query`** (create); **`GET /api/v1/query/{executionId}`** (metadata); **paged rows** **`GET /api/v1/query/{executionId}/rows?pageIndex=&pageSize=&format=&epoch=`** (**`format`**, **`epoch`** optional — **[`STORY.md`](STORY.md)** **Format negotiation**, **Concurrency**); **`PUT /api/v1/query/{executionId}`** (replace); **`DELETE /api/v1/query/{executionId}`** → **`core`** **`delete`** (**explicit deallocation** per **[`STORY.md`](STORY.md)**)
- [ ] **Breaking — no deprecation:** **do not** register **`POST /api/v1/queries/execute`** or any compatibility shim (see **[`GAPS.md`](GAPS.md) §1**)
- [ ] Thin REST: **all** handlers delegate **only** to **`mill-data-query`** (no duplicated paging); inject **`ResultMarshallerRegistry`** (SPI-backed bean from **WI-263**) — resolve **`format`** / **`Accept`** per **[`STORY.md`](STORY.md)** **Format negotiation**; set response **`Content-Type`** from selected marshaller; **`400`** / **`406`** as specified there
- [ ] **`principal` → `CallerContext`** bridge; **`401`** unauthenticated; **`403`** wrong owner for known **`executionId`**; **`404`** unknown **`executionId`**
- [ ] **`ConditionalOnService`** (or project equivalent) where applicable
- [ ] **`apps/mill-service`**: **`implementation(project(":services:mill-data-query-service"))`**
- [ ] Optional **`StreamingResponseBody` / `Flux<DataBuffer>`** adapters (deps **scoped to `-service`**)

## Goal

Introduce **`services/mill-data-query-service`**: MVC controllers with **all** execution-session routes under **`/api/v1/query/`** — **`POST /api/v1/query`** (create) and **`/api/v1/query/{executionId}`** for metadata, paging, replace, and **`DELETE`** (**no** **`/executions`** segment). Controllers **delegating exclusively** into **`mill-data-query`** — **no duplicated** paging logic. **Only** this tree is the public execution surface — **no** legacy **`/queries/execute`** endpoint.

Expose:

- **`POST`** create, **`PUT {executionId}`** replace, **`GET`** metadata, **`GET {executionId}/rows`** with **`pageIndex`** + **`pageSize`** only (no public **`offset`/`limit`** pair on **`/api/v1/query/**`), **`DELETE {executionId}`** (deallocate session + result buffers — **core** `delete`)
- Unified **page envelope** on **`/rows`**: **`epoch`**, **`pageIndex`**, **`pageSize`**, **`rowCount`**, optional **`totalResult`**, **`hasNext`**, **`hasPrevious`**, plus marshaller payload under the OpenAPI-locked top-level property ( **`data`** per **[`STORY.md`](STORY.md)** unless renamed consistently) — semantics per **[`STORY.md`](STORY.md)** **Paging contract**; **`400`** on invalid **`pageIndex`** / **`pageSize`** / unknown **`format`**
- **Format negotiation** per **[`STORY.md`](STORY.md)** — standard MIME only; **`406`** when **`Accept`** cannot be satisfied and **`format`** is absent
- Optional **chunked/streaming** responses (**`StreamingResponseBody`**, **`Flux<DataBuffer>`**, etc.) bridging **WI-263** marshaller **sink** APIs

Wire **`implementation(project(":services:mill-data-query-service"))`** into **`apps/mill-service`**.

**Optional:** **`ConditionalOnService`**/`mill` annotations parity with [`ExportRestController`](../../../../services/mill-export-service/src/main/java/io/qpointz/mill/export/ExportRestController.java).

## Scope

1. **`mill-data-query-service` Gradle** + **Java** **`@ConfigurationProperties`** (`mill.query-result.*`) + **Kotlin** MVC (**`@RestController`**, principal → **`CallerContext`** bridge) + Springdoc **OpenAPI** annotations (**`@Tag`**, **`@Operation`**, **`@ApiResponse`/`@ApiResponses`**) on **every** handler per the **OpenAPI (controllers)** subsection below; **KDoc**/**JavaDoc** to **parameter** level on all new/changed production code.
2. **`JvmTestSuite` `testIT`** with **Skymill** profile + **`skymill.datasets.dir`** (mirror **[`mill-data-schema-core/build.gradle.kts`](../../../../data/mill-data-schema-core/build.gradle.kts)** and **[`MillGrpcSkymillQueryIT`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java)**); REST **`testIT`** exercises **`/api/v1/query/**` against Skymill-backed dispatcher.
3. **`mill-service`** dependency + bean visibility.
4. OpenAPI / controller names use path parameter **`executionId`** (distinct from saved **`queryId`** on **`/api/v1/queries/{queryId}`**). Review generated **`/v3/api-docs`** (or configured path) in **`testIT`** or manual checklist — no operation missing **`4xx`** entries that the implementation actually returns.

### Paging and buffering (normative for this WI)

Implement exactly **[`STORY.md`](STORY.md)** sections **Paging contract** and **Server and client buffering (backward paging)** — OpenAPI + examples for **`GET …/rows`**, envelope fields (**including `epoch`**), defaults, and max **`pageSize`**. Document that **backward** navigation uses the **same** route with a **lower `pageIndex`**; server-side **`backwardCacheBuffers`** / snapshot behavior is **WI-262**; clients have **no** required local page cache.

### Format negotiation (normative for this WI)

Implement **[`STORY.md`](STORY.md)** **Format negotiation and `Content-Type`**: optional **`format`** on **`GET …/rows`**; **`Accept`** handling; selection order and **`format`**-wins precedence; **`400`** / **`406`**; single JSON object = envelope + **`data`** (or the name chosen in OpenAPI); response **`Content-Type`** from marshaller metadata (**WI-263**). OpenAPI documents built-in **`application/json`** responses and **`format`** enum for **`rows-objects`** / **`rows-compact-batch`**.

### Concurrency and `epoch` (normative for this WI)

Implement **[`STORY.md`](STORY.md)** **Concurrency, invalidation, and replace**: metadata + **`/rows`** include **`epoch`**; optional **`epoch`** query on **`GET …/rows`** → **`409`** when stale; **`replace`** serialized vs reads per **STORY**; **`404`** after **`DELETE`** / eviction. OpenAPI documents **`409`** on **`GET …/rows`** and **`epoch`** in success schemas.

### OpenAPI (controllers) — response codes

Annotations live **on the controller** (or dedicated operation interfaces if the project uses that pattern) so **`libs.springdoc.openapi.starter.webmvc.ui`** produces a complete spec **without** hand-maintaining a parallel YAML file.

**Minimum documented statuses per route** (adjust **`201` vs `200`** / **`204` vs `200`** only if the team picks one convention — document the chosen code explicitly):

| Route | Success | Client / auth errors | Other |
|--------|---------|----------------------|--------|
| **`POST /api/v1/query`** | **`200`** or **`201`** + create response schema | **`400`** (body / limits / **`defaultFormat`**), **`401`**, **`403`** | Structured SQL / validation failure (**`400`** or **`422`** — pick one, document schema) |
| **`GET /api/v1/query/{executionId}`** | **`200`** + metadata schema | **`401`**, **`403`**, **`404`** | — |
| **`GET …/rows`** | **`200`** + envelope + **`data`** schema(s) per **`format`** | **`400`** (**`pageIndex`/`pageSize`**, unknown **`format`**), **`401`**, **`403`**, **`404`** | **`406`** (**`Accept`** unsatisfiable); **`409`** (optional **`epoch`** query **≠** current — **[`STORY.md`](STORY.md)** **Concurrency**) |
| **`PUT /api/v1/query/{executionId}`** | **`200`** + replace acknowledgement schema | **`400`**, **`401`**, **`403`**, **`404`** | Same SQL error pattern as **`POST`** |
| **`DELETE /api/v1/query/{executionId}`** | **`204`** (preferred) or **`200`** with empty/minimal body — document exactly | **`401`**, **`403`**, **`404`** | — |
| **`GET /api/v1/query`** (optional) | — | — | **`405`** if implemented |

Reuse or reference a **shared error/problem** schema in OpenAPI if the repo already defines one for **`mill-service`**; otherwise define a small **`error` + `code`** object inline for this **`@Tag`** and reference it from **`@ApiResponse`** entries.

## Acceptance

- **`testIT`** (Skymill) proving controller + **`DataOperationDispatcher`** integration — **`@ActiveProfiles("skymill")`**, **`flow-skymill-it.yaml`** / **`application-skymill.yml`** as needed, **`skymill.datasets.dir`** on **`testIT`** task; SQL aligned with **[`test/it-querycases/skymill-sql.json`](../../../../test/it-querycases/skymill-sql.json)** where practical (same pattern as **[`MillGrpcSkymillQueryIT`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java)**). At least one **`GET …/rows`** sequence that requests **page 1 then page 0** (backward) and asserts envelope consistency. At least one **`GET …/rows`** per built-in **`format`** with **`Content-Type: application/json`** and distinct **`data`** shapes; **`400`** for unknown **`format`**; **`406`** for unsatisfiable **`Accept`** (no **`format`** override).
- Error model: **`404`** unknown **`executionId`**, **`403`** known id but wrong owner, **`401`** unauthenticated; SQL errors structured (`error` + `code`) where feasible.
- **`DELETE /api/v1/query/{executionId}`** covered (session removed; follow-up **`GET`** → **`404`**).
- **KDoc**/**JavaDoc** complete (**parameter** level) on all new/changed production types in this module.
- **OpenAPI:** spot-check (or automate) that **each** **`/api/v1/query/**` operation lists the **OpenAPI (controllers)** response codes; **`406`** appears only on **`GET …/rows`**; **`409`** on **`GET …/rows`** when **`epoch`** optimistic check is enabled; **`DELETE`** documents **`204`** or **`200`** consistently with implementation.
- **`PUT` replace + `epoch`:** **`testIT`** or unit flow proving **`epoch`** increments and a **`GET …/rows?epoch=<old>`** returns **`409`** after replace; **`GET`** after **`DELETE`** / eviction → **`404`**.

## Depends on

**WI-262**, **WI-263**

## Notes

Harmonize with **[`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)** in **WI-265**; additive fields preferred. Public path model, paging, **format negotiation**, and **concurrency / `epoch`** are fixed in **[`STORY.md`](STORY.md)**; **[`GAPS.md`](GAPS.md) §1** / **§3** / **§4** / **§5** are **closed** in **STORY** + **WI-262**–**WI-265**.
