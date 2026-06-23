# OData v4 service

**Backlog:** [P-41](../../BACKLOG.md) · **Status:** `closed` (2026-06-23)

## Cold start

**New to this story?** Start with [`COLDSTART.md`](COLDSTART.md) — branch setup, Gradle map, class inventory,
adapter pipeline, testIT scenarios, and verify commands. No other context required.

## Goal

Deliver read-only **OData v4** over Mill **physical tables**, enriched with descriptive and relation
metadata facets, including **`$expand`** on navigation properties from `RelationFacet`.

Expose **`/services/odata/{schema}.svc`** for **live query** BI clients (Power BI OData, Tableau OData, Excel): service document,
`$metadata`, entity set reads with `$filter`, `$select`, `$orderby`, `$top`, `$skip`, and **`$expand`** on relation facets.
Entity sets use **plain table names** within each schema service (e.g. `/services/odata/skymill.svc/cities`).

## Platform role (locked)

Standalone OData feature — **not** a phase of [Substrait → RelNode migration](../../../design/platform/substrait-to-relnode-migration.md).

Compose queries as **Calcite RelNode** (`ODataRelComposer`); execute via **RelNode→Substrait adapter** and the existing **`DataOperationDispatcher`** path (same hop as SQL today). **`PlanRewriteChain`** applies for policy rewrites.

**Design:** [`docs/design/platform/odata-service.md`](../../../design/platform/odata-service.md)  
**Branch:** `feat/odata-service`

## Problem statement

Mill today has no OData surface. Current Mill services use Substrait
roundtrips (SQL → RelNode → Substrait → RelNode) or `PlanHelper` Substrait scans for export.

This story adds OData with **EDM → RexNode** predicate pushdown and **RelBuilder** query composition,
then a shared **Rel→Substrait adapter** in `mill-data-backends` — no SQL string building for OData.

## Architecture decisions (locked)

### 1. Compose RelNode, execute Substrait

- OData URI options → `ODataExpressionToRex` → `ODataRelComposer` → `RelPlanDispatcherBridge`.
- **Forbidden:** OData → SQL string → `SqlProvider`.
- **Forbidden:** OData opening `CalciteContextFactory`, `FrameworkConfig`, `VolcanoPlanner` — use injected `RelBuilderFactory` only.

### 2. Module boundary — no Calcite in backend-core

| Artifact | Module |
|----------|--------|
| `SubstraitPlanExecutor` | `mill-data-backend-core` |
| `RelBuilderFactory`, `RelToSubstraitPlanConverter`, `RelPlanDispatcherBridge` | `mill-data-backends` |
| OData composers / executor | `mill-data-odata` (`implementation` dep on backends, WI-327) |

### 3. Protocol stack — RWS/SDL OData 2.16.x

- **[RWS/odata](https://github.com/RWS/odata)** (`com.sdl:*` **2.16.1**) for URI parse, EDM model types, JSON/`$metadata` render, Spring MVC controller.
- **Not** Apache Olingo (Attic, retired Dec 2025).
- **Platform:** **Java 25** required (RWS 2.14.1+ bytecode); delivered in **WI-324** before OData modules.

### 4. `$filter` = EDM → `RexNode`, not SQL

- RWS `com.sdl.odata.api.processor.query.*` criteria AST → `RexNode` via `RelBuilderFactory`’s `RexBuilder`.
- Untranslatable filter → **400**; **no** full-table scan fallback.

### 5. Entity sets = physical tables only

- One entity set per `SchemaProvider` table; name = **table name** (schema from service root `{schema}.svc`).
- Navigations from `RelationFacet`; inverse relations where metadata defines them.

### 6. HTTP surface

- Base path: `/services/odata/{schema}.svc` (+ `GET /services/odata/schemas` catalog)
- `@ConditionalOnService(value = "odata", group = "data")`
- Config: `mill.data.services.odata.*` (mirror `export` / `query`)

### 7. Accepted coupling

`mill-data-odata` → `mill-data-backends` is tolerated for RelBuilder + adapter facades; not re-exported as `api`.

## Target execution pipeline

```text
GET /services/odata/skymill.svc/{entitySet}?$filter=...&$expand=...
  → RWS odata_controller + odata_parser (mill-data-odata-service)
  → MillODataDataSource → ODataQueryExecutor (mill-data-odata)
  → EdmPropertyResolver + ODataExpressionToRex (RWS query AST)
  → ODataRelComposer (RelBuilderFactory)
  → RelPlanDispatcherBridge → SubstraitPlanExecutor → DataOperationDispatcher
  → VectorBlockIterator → odata_renderer → OData JSON payload
```

## Code map (existing — read before WI-326)

| Area | Path |
|------|------|
| Facet merge | `data/mill-data-schema-core/.../SchemaFacetService.kt` |
| Relation facet | `data/mill-data-schema-core/.../facet/RelationFacet.kt` |
| Physical catalog | `data/mill-data-backend-core/.../SchemaProvider.java` |
| Dispatcher | `data/mill-data-backend-core/.../DataOperationDispatcherImpl.java` |
| Plan → dispatcher | `data/mill-data-backend-core/.../ExportVectorBlockSource.java` |
| Rel→Substrait prior art | `data/mill-data-backends/.../CalciteSqlProvider.java` |
| Substrait scan (export only) | `data/mill-data-backend-core/.../PlanHelper.java` |
| Skymill join IT | `data/mill-data-backends/.../SkymillJoinPerformanceIT.kt` |
| Export HTTP pattern | `services/mill-export-service/` |
| Query HTTP pattern | `services/mill-data-query-service/` |
| Skymill metadata | `test/datasets/skymill/skymill-meta-repository.yaml` |

## Modules (this story)

| Module | Path |
|--------|------|
| `mill-data-odata` | `data/mill-data-odata/` |
| `mill-data-odata-service` | `services/mill-data-odata-service/` |

## Out of scope

- OData v2, CUD, `$batch`, actions/functions, delta feeds
- Non-physical entity sets
- Substrait migration / `DataHandler` RelNode-only execution (separate story)
- `mill-service` changes beyond dependency + `application.yml` odata enable block

## Work item order

| Seq | WI | Depends on | Summary |
|-----|-----|------------|---------|
| 1 | WI-325 | — | Design + story docs (**done**) |
| 2 | WI-324 | WI-325 | **Java 25** platform bump (RWS prerequisite) |
| 3 | WI-326 | WI-324 | `mill-data-odata` RWS `EntityDataModel` |
| 4 | WI-327 | WI-326 | RWS AST→Rex, RelComposer, adapter + executor |
| 5 | WI-328 | WI-327 | RWS HTTP + Skymill testIT |
| 6 | WI-329 | WI-328 | Public + platform docs sweep |

## Work Items

- [x] WI-325 — Design contract + story docs (`WI-325-odata-design-contract.md`)
- [x] WI-324 — Java 25 platform bump (`WI-324-jdk25-platform-bump.md`)
- [x] WI-326 — `mill-data-odata` RWS EDM + type mapping (`WI-326-odata-edm-and-type-mapping.md`)
- [x] WI-327 — Adapter pipeline + EDM→Rex + RelComposer (`WI-327-odata-relnode-query-bridge.md`)
- [x] WI-328 — `mill-data-odata-service` HTTP + Skymill testIT (`WI-328-odata-rest-and-wiring.md`)
- [x] WI-329 — Public + platform docs sweep (`WI-329-odata-docs-and-closure-prep.md`)

## Implementation delivery workflow (normative)

After **each** WI (324, 326–329), in order — **do not start the next WI until steps 1–5 are complete**:

1. **Verify locally** — run the WI verify commands from [`COLDSTART.md`](COLDSTART.md) (tests / compile for that WI).
2. **Update tracker** — mark the WI `[x]` in this `STORY.md`; update the corresponding `WI-NNN-*.md` (status, acceptance notes) in the **same changeset** as the implementation.
3. **Commit** — one logical commit per WI (`[change]` WI-324, `[feat]` WIs 326–328, `[docs]` WI-329). The commit **must include** tracker updates (`STORY.md`, `WI-*.md`) plus all code/config for that WI — full working copy per [`RULES.md`](../../RULES.md).
4. **Push** — `git push origin feat/odata-service` immediately after the commit (do not batch multiple WIs on one push).
5. **CI/CD** — confirm the GitLab pipeline for the pushed commit is **green** before starting the next WI. If red, fix on the same branch, commit, push again, re-check until green.

**Closure:** MR !405 (`feat/odata-service` → `dev`); story archived **2026-06-23** per [`RULES.md`](../../RULES.md).

## Story closure — reconcile `docs/design` (WI-329)

| Document | Action |
|----------|--------|
| `platform/odata-service.md` | Align with shipped behaviour |
| `platform/module-inventory.md` | Add new modules |
| `platform/mill-data-lane-onepager.md` | OData on service surface |
| `security/REST-CONTROLLERS-INVENTORY.md` | OData servlet section |
| `docs/public/src/data-access/odata.md` | Operator guide |

Archived at [`docs/workitems/completed/20260623-odata-service/`](.).

## Common pitfalls

- Building SQL strings for `$filter` — use `RexNode` / `RelBuilder.filter`.
- Putting `RelToSubstraitPlanConverter` in `mill-data-backend-core` — **forbidden** (Calcite).
- Opening `CalciteContextFactory` from OData — inject `RelBuilderFactory` only.
- Olingo or JDK 21 with RWS 2.16.x — **forbidden** (use `com.sdl` 2.16.1 on Java 25).
- RWS JPA `DataSource` fetch without push-down — wire `MillODataDataSource` to `ODataQueryExecutor`.
- Kotlin `@ConfigurationProperties` for `mill.data.services.odata.*` — use **Java**.
- Ignoring `RelationFacet` for `$expand` — do not invent FK inference without metadata.
- Assuming adapter rejects all joins — gate-test Skymill tiers; SQL path already proves multi-join conversion.
