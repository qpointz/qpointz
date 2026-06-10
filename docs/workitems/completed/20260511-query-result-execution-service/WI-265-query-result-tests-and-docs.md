# WI-265 — Integration tests + design doc + BACKEND requirements

Status: `done`  
Type: `docs` / `feature`  
Area: `data`, `platform`  
Backlog refs: **D-8**, **U-13**

**Story:** [`STORY.md`](STORY.md) — **WI-265** (tracker row 4). **Depends on WI-264** (merge-ready HTTP + **baseline** **`testIT`**). **Delivery:** when this WI is finished, mark its tracker + **[`STORY.md`](STORY.md)** **Tracker** / **Work Items**, then **one commit** for the full tree (**[`RULES.md`](../../RULES.md)**).

## Language

This WI is **mostly documentation**. Any code touched outside **Java** **`@ConfigurationProperties`** follow-ups stays **Kotlin** per **[`STORY.md`](STORY.md)** **Implementation conventions**.

## Tracker (this WI)

- [x] **`docs/design/platform/query-result-execution-service.md`** — **Baseline** exists in repo; **WI-265** verifies **maintainer + implementer** depth vs shipped code, adds **final `mill.data.services.query.*` and `mill.data.query.*` key table** from Java **`@ConfigurationProperties`**, ensures **OpenAPI parity**, and adds **cross-links** from reconciled **`docs/design/**`** files. Content must remain usable by **any** future consumer (**`mill-ui`**, **`mill-service`**, other HTTP clients, in-process embedders, NL-to-SQL / analysis features) without reading only WIs. **Must include** (verify or extend): **purpose and boundaries**; **module map** (`mill-data-query`, `mill-data-query-service`, Gradle coordinates, `apps/mill-service` wiring); **runtime / bean diagram** (dispatcher, registry, session store, controllers); **full HTTP narrative** matching **[`STORY.md`](STORY.md)** (`POST` / query-driven **`GET`/`DELETE`**, **Session ownership**, **Paging**, **`totalResult` null**, **`data`**, **Format negotiation**, **HTTP status semantics** `201`/`200`/`204`/`422`, **Concurrency** / **`epoch`** / **`409`**); **in-process vs REST** embedding (`CallerContext`, tenant-only ownership; **`replace`** in-process only); **SPI** (**`ResultMarshallerProvider`**, **`META-INF/services`**, collision policy); **`mill.data.services.query.*` and `mill.data.query.*`** configuration reference; **error model** and **consistency** (refill, eviction); **Skymill `testIT`** how to run and extend; **related design links** (UI BACKEND, export, data lane one-pager as applicable); **breaking** note vs **`POST /api/v1/queries/execute`**. Record **Kotlin** / **Java**-for-properties / **parameter-level** KDoc policy from **[`STORY.md`](STORY.md)** **Implementation conventions**
- [x] **[`docs/design/platform/README.md`](../../../design/platform/README.md)** — **index row** present for **`query-result-execution-service.md`** (add or refresh one-line description)
- [x] **[`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)** Queries domain: **Session ownership** (tenant-only, e.g. **`userId`** — **[`STORY.md`](STORY.md)**); **`POST /api/v1/query`** (optional **`defaultFormat`**); **query-driven** **`GET /api/v1/query/{executionId}`** — without **`pageIndex`**: metadata (**`epoch`**, **`totalResult`**, …); with **`pageIndex`**: **`pageSize`**, optional **`format`**, optional **`epoch`**, **`Accept`**, envelope + **`schema`** + **`data`** + **`epoch`** + **`totalResult`** (**JSON `null`** = unknown, field **never omitted**), **`Content-Type: application/json`** for built-ins; **`DELETE`** deallocates — **no** **`/executions`** segment; **no** public **`offset`/`limit`**; **`409`** stale **`epoch`** on paged reads; **HTTP status codes** table consistent with **Springdoc** on controllers (**[`WI-264`](WI-264-query-result-rest-and-wiring.md)** **OpenAPI (controllers)**); **remove** normative **`POST /api/v1/queries/execute`** (**breaking** — **no** “deprecated” stub)
- [x] **`docs/design` sweep (breaking)** — align with **[`STORY.md`](STORY.md)** **Story closure — reconcile `docs/design`** table: **[`BACKEND-BACKLOG.md`](../../../design/ui/mill-ui/BACKEND-BACKLOG.md)** (**G-10**, **B-4**), **[`ARCHITECTURE.md`](../../../design/ui/mill-ui/ARCHITECTURE.md)** (Queries / **`queryService`** row), **[`UI-ELEMENT-INVENTORY.md`](../../../design/ui/mill-ui/UI-ELEMENT-INVENTORY.md)** (**`queryService`** / analysis execute); same **STORY** list for export / codebase / client docs — **fix or remove** wrong HTTP path claims (**no** deprecation banners)
- [x] Mirror **`ui/mill-ui/docs/BACKEND-API-REQUIREMENTS.md`** if it duplicates the canonical doc
- [x] **`testIT` @ `skymill` (supplementary)** — only scenarios **not** already asserted in **WI-264** acceptance (e.g. extra edge cases); if **WI-264** already covers the full matrix, **skip** or add **minimal** regression hooks. **`skymill.datasets.dir`** / profile wiring stays aligned with **[`mill-data-schema-core/build.gradle.kts`](../../../../data/mill-data-schema-core/build.gradle.kts)** and **[`MillGrpcSkymillQueryIT`](../../../../services/mill-data-grpc-service/src/testIT/java/io/qpointz/mill/data/backend/grpc/MillGrpcSkymillQueryIT.java)**.
- [x] **`./gradlew`** affected modules **`test`** + **`testIT`** green after doc edits
- [x] **WI-257** / [**U-13**](../../BACKLOG.md): saved-query **`GET /api/v1/queries`** unchanged; execution only under **`/api/v1/query`**; **`mill-ui`** **must** drop mock **`/queries/execute`** — **breaking**, **no** deprecation shim

## Goal

Finalize delivery documentation and coverage:

1. **`docs/design/platform/query-result-execution-service.md`** — **baseline** canonical platform doc (**already in repo**); **WI-265** verifies alignment with shipped OpenAPI, adds the **definitive `mill.data.services.query.*` and `mill.data.query.*` table** from Java **`@ConfigurationProperties`**, and refreshes **cross-links** after the **`docs/design` sweep**. Content: architecture (**programmatic-first**, REST adapter, presentation **`pageIndex`/`pageSize`** vs **`executionBufferRows`**, **backward** buffering, marshalling + **standard MIME**, **`format`** / **`Accept`** / **`data`**, **`epoch`** / **`409`**, **`replace`** serialization, eviction, **refill / live-data** weak consistency, extension and scale notes); **integration** sections for HTTP consumers and in-process callers; **module / bean / config** reference; opening **breaking** note vs **`POST /api/v1/queries/execute`** / **B-4** — **no** compatibility period.
2. Update **[`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)** Queries domain: execution sessions at **`POST /api/v1/query`** and **query-driven** **`GET /api/v1/query/{executionId}`** (same path model as **[`STORY.md`](STORY.md)** **Session ownership**, **Paging contract**, **Format negotiation**, **Concurrency**); document **`DELETE /api/v1/query/{executionId}`** as explicit **deallocation**; full envelope + **`data`** + **`epoch`**; **`defaultFormat`** / **`format`** / **`Accept`** / **`400`**/**`406`**/**`409`**; **delete** normative **`POST /api/v1/queries/execute`** (**breaking** — see **[`GAPS.md`](GAPS.md) §1**).
3. **`docs/design` sweep** — every file under **Story closure — reconcile `docs/design`** in **[`STORY.md`](STORY.md)** **must** be updated so **`POST /api/v1/queries/execute`** does **not** appear as a supported or transitional **mill-service** / **mill-ui** contract (**no** “deprecated” sections, **no** dual primary routes).
4. **Supplementary `testIT`** — add only if **WI-264** acceptance did not already cover a required matrix row; otherwise document how to run **WI-264** tests from **[`query-result-execution-service.md`](../../../design/platform/query-result-execution-service.md)**.
5. **Spring configuration metadata** — **`@ConfigurationProperties`** implemented in **Java** with **`spring-boot-configuration-processor`** only (**no** Kotlin properties + manual **`additional-spring-configuration-metadata.json`** in these modules — **[`STORY.md`](STORY.md)** **Implementation conventions**).

## Scope

**Documentation-first:** BACKEND + **`docs/design` sweep** + platform doc parity with **WI-264** OpenAPI. **Supplementary `testIT`** only when **WI-264** left explicit gaps; otherwise rely on **WI-264** baseline and keep **`./gradlew`** green after doc-only changes.

**One commit** for this WI when done, with tracker updates per **[`STORY.md`](STORY.md)** **WI completion — commit and tracker**.

## Acceptance

- `./gradlew` relevant modules **`test`** + **`testIT`** **green** (baseline from **WI-264**; any **WI-265** supplementary **`testIT`** must not regress the suite).
- BACKEND doc describes **`/api/v1/query`** session API only; **`POST /api/v1/queries/execute`** is **absent** from normative text (**breaking**); **WI-257** scope stays saved-query **GET**s — execution client uses **`/api/v1/query/**` only.
- **`docs/design/platform/query-result-execution-service.md`** exists, meets **Tracker** depth (**maintainer + implementer** — a new engineer can locate modules, beans, SPIs, HTTP rules, and **`testIT`** from this file alone), and is **indexed** in **[`docs/design/platform/README.md`](../../../design/platform/README.md)**.
- **`query-result-execution-service.md`** is **linked** from reconciled **`docs/design/ui/mill-ui/**`** (and any other **`docs/design/**`** files per **Story closure** table) where execution architecture or cross-component data access is discussed.
- **`docs/design` sweep** (**BACKEND-BACKLOG**, **ARCHITECTURE**, **UI-ELEMENT-INVENTORY** minimum): **no** remaining normative **`/queries/execute`** path for **mill-ui**; **additional** **STORY**-listed files (export / codebase / client) **must not** imply a deprecated parallel route — **remove** or rewrite.
- Design doc + platform notes mention **Kotlin** implementation, **Java**-only **`@ConfigurationProperties`**, **Skymill `testIT`**, and **parameter-level** KDoc/JavaDoc policy (**Story** **Implementation conventions**), and **cross-component** integration pointers per **Tracker**.

## Depends on

**WI-264**

## Notes

**MILESTONE.md** / full **story closure** per **RULES.md** occurs when **all** story WIs are done and MR-ready.
