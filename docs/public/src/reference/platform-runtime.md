# Platform runtime (Mill services)

Mill’s **Spring Boot** applications (for example `apps/mill-service`, metadata and data HTTP services, security auth service, AI v1 chat service) share a single **version catalog** (`libs.versions.toml` in the repository root). The table below reflects the **Spring Boot 4** migration line (archived story [`spring4-migration-day-2`](../../../workitems/completed/20260430-spring4-migration-day-2/STORY.md)). For rationale, phased checklist, and module impact, see the internal design doc [Spring Boot 3.5 to 4.0 migration plan](../../../design/platform/spring4-migration-plan.md).

| Area | Version / policy |
|------|------------------|
| Java | **21** for building and running the full stack (required by **Spring AI 2.0** milestone artifacts). |
| Spring Boot | **4.0.6** |
| Spring Framework / Security | **7.0** (managed by Spring Boot) |
| Jackson | **3.1.x** (`tools.jackson` coordinates; JSON/YAML via `JsonMapper` / `YAMLMapper`) |
| SpringDoc OpenAPI | **3.0.3** (WebMVC and WebFlux starters where used) |
| Spring AI | **2.0.0-M5** (milestone; NL-to-SQL and related modules) |
| gRPC (data plane) | **grpc-java 1.79.x**; production server in `services/mill-data-grpc-service` (no `net.devh` starter) |
| Jakarta EE | **11** baseline with Boot 4 (Servlet **6.1**, etc.) |

## JDBC driver and integration tests

The **JDBC driver** module is not a Spring Boot application, but its **`testIT`** suite may start an embedded **Skymill** gRPC stack (`EmbeddedSkymillGrpcServer`) so tests do not depend on a fixed host/port. That wiring is described in the migration plan’s **WI-208** notes and the design doc above.

## HTTP data export (`/services/export`)

When **`mill.data.services.export.enable`** is true and the **`export`** service group is on the classpath, Mill exposes **streaming table and SQL exports** under **`/services/export`** (catalog, effective format list, **`GET`** per-table streams via a Substrait plan, **`POST /sql`** with a plain-text SQL body). Formats are registered with the **`ExportFormatProvider`** SPI; **`mill.data.services.export.formats`** can restrict which ids appear on the wire. **mill-ui** can download from Analysis results and export whole tables from the Model view when the corresponding feature flags are enabled.

Operator-facing detail: internal design [Streaming HTTP export service](../../../design/platform/export-service.md); configuration keys in [05-configuration-keys](../../../design/refactoring/05-configuration-keys.md) (`mill.data.services.export.*`). Story archive: [`docs/workitems/completed/20260507-streaming-export-service/STORY.md`](../../../workitems/completed/20260507-streaming-export-service/STORY.md).

## HTTP Analysis saved queries (`/api/v1/analysis`)

When **`mill-service`** wires the Analysis stack (**`mill-analysis-service`** + **`mill-analysis-persistence`**), the UI can persist and load saved SQL under **`/api/v1/analysis/queries`** and read the configured dialect from **`/api/v1/analysis/dialect`**. Flyway migration **`V8__saved_queries.sql`** (in shared **`mill-persistence`**) creates the **`saved_query`** table and demo seed rows. Internal design [Analysis saved-query service](../../../design/platform/analysis-saved-query-service.md); story archive [`docs/workitems/completed/20260609-mill-ui-analysis-full-stack/STORY.md`](../../../workitems/completed/20260609-mill-ui-analysis-full-stack/STORY.md).

## HTTP ad hoc query sessions (`/api/v1/query`)

When **`mill.data.services.query.enable`** is true and the **`query`** data service group is on the classpath, **`mill-service`** exposes **session-based** ad hoc SQL execution under **`/api/v1/query/`**: **`POST /api/v1/query`** creates a session; **`GET /api/v1/query/{executionId}`** returns metadata **or** a paged result slice depending on query parameters; **`DELETE /api/v1/query/{executionId}`** deallocates. Built-in JSON marshallers include **`rows-objects`** and **`rows-compact-batch`**. Internal design [Query result execution service](../../../design/platform/query-result-execution-service.md); story archive [`docs/workitems/completed/20260511-query-result-execution-service/STORY.md`](../../../workitems/completed/20260511-query-result-execution-service/STORY.md).

## Verifying a checkout

From the repository root (see also [Installation](../installation.md)):

- `./gradlew build` — compile, unit tests, assemble
- `./gradlew testIT` — integration suites (where configured per module)

Record full-repo **`clean build`**, **`test`**, and **`testIT`** results in merge requests for large platform upgrades.
