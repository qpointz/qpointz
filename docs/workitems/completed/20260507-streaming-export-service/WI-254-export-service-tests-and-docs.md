# WI-254 — Export service tests, limits, design doc

Status: `planned`  
Type: `test`, `docs`  
Area: `services`, `platform`  
Backlog refs: **P-36**

## Goal

Harden the export story with **automated tests**, **operational limits** (row cap, timeout, max response), and a **design document** (e.g. `docs/design/platform/export-service.md` or `docs/design/data/`).

## Scope

1. **testIT** (or WebMvcTest + test slice): **`/services/export`** discovery (`/catalog`, `/formats`, `/schemas`, `/schemas/{schema}`) and streaming (`/sql`, `/schemas/{schema}/tables/{table}`) happy paths + 4xx error paths; **`enable: false`** (or absent `export` group per project convention) does not register controllers; **`formats: [csv]`** hides non-csv entries from **`GET /formats`** / **`catalog.formats`** and rejects other **`?format=`** with **400**. Assert **`/.well-known/mill`** includes **`data-export`** + export **`connections`** when export is enabled, and omits them when disabled.
2. Configuration: document **`mill.data.services.export`** (`enable`, **`external-host`**, **`formats`**, optional **limits**) in **`application.yml`** example + design doc; align **`external-host`** with **`mill.data.services.http`**.
3. Design doc: endpoint map (**`/catalog`** with root **`formats`** / **HTTP-effective** set, **`/formats`**, **`/schemas`**, **`/schemas/{schema}`**, **`POST /sql`**, table streaming path), **configuration table** for **`mill.data.services.export`**, **`ExportServiceDescriptor` / `ExportConnectionDescriptor`** and **`/.well-known/mill`** shape, **OpenAPI / SpringDoc** (`@Tag`, `@Operation` on export controllers; link to Swagger UI path if stable), security model, plan vs SQL paths, **SPI vs HTTP format visibility**, Parquet deferral.
4. **OpenAPI regression:** lightweight test or CI checklist that **`GroupedOpenApi` `api`** document includes **`/services/export`** paths when export is enabled (optional **WebMvcTest** + **`springdoc-openapi`** test dependency, or manual sign-off documented in WI).

## Acceptance

- `./gradlew` test tasks relevant to new modules are green in CI.
- **SpringDoc / OpenAPI:** export **`@RestController`** handlers are present in the generated **`api`** OpenAPI document with a dedicated **`@Tag`**; not a merge blocker to hand-verify Swagger UI once per release if automated check is deferred.
- **`docs/design/refactoring/05-configuration-keys.md`** gains rows for **`mill.data.services.export.*`** (optional but preferred for cross-module discovery).

## Depends on

**WI-253**
