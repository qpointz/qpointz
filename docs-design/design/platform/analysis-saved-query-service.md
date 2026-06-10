# Analysis saved-query HTTP service

## Purpose

The Analysis stack persists **saved SQL queries** for the mill-ui playground and exposes them under **`/api/v1/analysis/**`**, separate from ad hoc **query-result sessions** (`/api/v1/query/**`) and **file export** (`/services/export/**`).

## Modules

| Module | Role |
|--------|------|
| **`services/mill-analysis-api`** | Domain contract: **`SavedQuery`**, **`SavedQueryCatalog`** port (no JPA/Spring). |
| **`persistence/mill-analysis-persistence`** | JPA entity, repository, **`JpaSavedQueryCatalog`** adapter; Flyway table in shared **`mill-persistence`** (`V8__saved_queries.sql`). |
| **`services/mill-analysis-service`** | REST controllers and DTO mappers; auto-configured when **`DataOperationDispatcher`** is present. |

**`apps/mill-service`** depends on **`mill-analysis-service`** and pulls persistence at **runtime** (`runtimeOnly` **`mill-analysis-persistence`**).

## Endpoints

| Method | Path | Role |
|--------|------|------|
| GET | `/api/v1/analysis/dialect` | Active SQL dialect from **`mill.data.sql.dialect`** (editor highlighting, identifier quotes, function lists). |
| GET | `/api/v1/analysis/queries` | List saved queries (ordered by **`updatedAt`** desc). |
| GET | `/api/v1/analysis/queries/{id}` | Single saved query. |
| POST | `/api/v1/analysis/queries` | Create; optional **`id`** in body (slug generated from **`name`** when omitted). |
| PUT | `/api/v1/analysis/queries/{id}` | Update name, description, SQL, tags. |
| DELETE | `/api/v1/analysis/queries/{id}` | Remove catalog entry (**`204`**). |

Ad hoc execution and paging remain on **`/api/v1/query/**`** — see [Query result execution service](query-result-execution-service.md).

## Security

Controllers live under **`/api/**`** and follow **`ApiSecurityConfiguration`** (same-origin session / bearer rules as other JSON APIs).

## Discovery and wiring

- Auto-configuration: **`AnalysisQueriesWebAutoConfiguration`** (imports via **`META-INF/spring/...AutoConfiguration.imports`**).
- Beans register when **`DataOperationDispatcher`** exists; **no** dependency on AI modules.
- Seed rows in **`V8__saved_queries.sql`** provide demo sidebar content after Flyway migrate.

## Related docs

- UI contract: [mill-ui BACKEND-API-REQUIREMENTS](../ui/mill-ui/BACKEND-API-REQUIREMENTS.md) § Domain: Analysis.
- Story archive: [`docs/workitems/completed/20260609-mill-ui-analysis-full-stack/STORY.md`](../../workitems/completed/20260609-mill-ui-analysis-full-stack/STORY.md).
