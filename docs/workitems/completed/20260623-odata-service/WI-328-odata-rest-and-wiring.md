# WI-328 — `mill-data-odata-service` HTTP and wiring (RWS)

**Story:** [`odata-service`](STORY.md) · **Backlog:** P-41  
**Status:** done  
**Depends on:** WI-327

## Goal

RWS/SDL OData v4 HTTP under `/services/odata/v4/`, Spring wiring, Skymill testIT (live query for Power BI / Tableau).

**Before coding:** read [`COLDSTART.md`](COLDSTART.md) § WI-328 skeleton and § testIT scenarios.

## Scope

- `services/mill-data-odata-service/`
- RWS **`odata_controller`** + **`odata_parser`** + **`odata_renderer`** (`com.sdl` **2.16.1**)
- **`MillODataDataSource`** (implements `com.sdl.odata.api.processor.datasource.DataSource`) — delegates reads to `ODataQueryExecutor` (push-down; no JPA fetch)
- `@ConditionalOnService(value = "odata", group = "data")`
- `mill.data.services.odata.*` (`@ConfigurationProperties`, Java)
- `mill-service` dependency + `application.yml`
- testIT: `$metadata`, entity GET, `$filter`, `$expand`
- Update `REST-CONTROLLERS-INVENTORY.md`

## Out of scope

- `odata_webservice` standalone distribution (embed controller in Mill only)
- Olingo servlet

## Completion (normative)

After verify passes: update **tracker** (`STORY.md` `[x]`, this file status) → **commit** (include tracker in commit) → **push** → **CI green**. See [`STORY.md`](STORY.md) § Implementation delivery workflow.
