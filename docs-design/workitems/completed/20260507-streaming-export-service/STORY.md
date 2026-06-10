# Streaming HTTP export service (`/services/export`)

Deliver a **`mill-export-service`** module under `/services/export` that serves query and table exports in **CSV, TSV, XLSX, Avro, and JSON** (new format module). **Layering:** format encoders (**WI-250/251**) accept only **`VectorBlockIterator` + `OutputStream`**—they **do not** execute queries. **`ExportVectorBlockSource`** (**WI-252**) in **`mill-data-backend-core`** runs **`DataOperationDispatcher`** (SQL body or plan-native table scan → iterator). Spring **`ExportFacility`** (**WI-253**) wires vector source + **`ExportFormatRegistry`**. **Formats are not static:** **WI-250** defines provider SPI + registry interface in **`mill-data-source-core`**; **`mill-data-autoconfigure`** exposes an **`ExportFormatRegistry` `@Bean`** that loads providers via **`ServiceLoader`** and **`META-INF/services`** so new format JARs extend the catalogue without editing export service code. **`mill.data.services.export`** (**`enable`**, **`external-host`**, **`formats`**) toggles the service, resolves **absolute** catalog URLs (**`external-host`** like **`mill.data.services.http`**), and restricts **HTTP-visible** formats (**`*`** / empty / missing = all SPI formats on the wire). **Streaming** means format writers **flush incrementally to `OutputStream`** without holding the **full** result set in memory (bounded only by vector batch / small buffers). **Table exports** (`GET /services/export/schemas/{schema}/tables/{table}`) must use a **Substrait logical plan** (`PlanHelper` NamedScan → `QueryRequest.setPlan`)—no hand-written `SELECT *` SQL string. **Ad-hoc SQL export** (`POST /services/export/sql` with SQL body) uses the existing SQL parse path on the dispatcher **inside the backend vector source**, not inside format modules.

**Related backlog:** **[P-36](../../BACKLOG.md)** (and aligns with legacy **[P-11](../../BACKLOG.md)** “data export” theme at a higher level).

**Design references (to create or extend during WIs):**

- Cursor plan (**two stories**): [`.cursor/plans/Analysis view story-06fc75ec.plan.md`](../../../../.cursor/plans/Analysis%20view%20story-06fc75ec.plan.md) — Story 1 section
- [`DataOperationDispatcherImpl`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcherImpl.java) — plan vs SQL on `QueryRequest`
- [`PlanHelper`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/PlanHelper.java) — `createNamedScan`, `createPlan`
- [`ServicesSecurityConfiguration`](../../../../security/mill-security-autoconfigure/src/main/java/io/qpointz/mill/security/configuration/ServicesSecurityConfiguration.java) — `/services/**`
- [`DataOperationColumnDistinctValueLoader`](../../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/valuemap/DataOperationColumnDistinctValueLoader.kt) — dispatcher + `RecordReaders` precedent (SQL today; table export uses plan)

## Work Items

- [x] WI-250 — Export format provider SPI + `ExportFormatRegistry` interface (`mill-data-source-core`); SPI-backed registry `@Bean` (`mill-data-autoconfigure`); `META-INF/services` only — no static format list (`WI-250-export-format-spi.md`)
- [x] WI-251 — `mill-data-format-json` + adapters from `VectorBlockIterator` to existing row writers (`WI-251-export-format-json-and-adapters.md`)
- [x] WI-252 — `ExportVectorBlockSource` in `mill-data-backend-core` (SQL + plan-native table scan → iterator only) (`WI-252-export-vector-block-source-backend.md`)
- [x] WI-253 — `mill-export-service`: `ExportFacility` + HTTP API + catalog + **`ExportServiceDescriptor` / `ExportConnectionDescriptor`** + **OpenAPI (SpringDoc) `@Tag` / `@Operation` on REST controllers** (`WI-253-mill-export-service-http.md`)
- [x] WI-254 — Tests, limits, and design documentation (`WI-254-export-service-tests-and-docs.md`)
- [x] WI-255 — Optional: mill-ui `QueryResults` download via export service (`WI-255-mill-ui-export-download-optional.md`)
- [x] WI-261 — mill-ui Model view: **TABLE** detail **Export** split button (formats menu) before **Add Facet**, feature flag **`modelTableExportEnabled`** (default **true**) (`WI-261-mill-ui-model-table-export.md`)
