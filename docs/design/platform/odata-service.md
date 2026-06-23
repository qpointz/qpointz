# OData v4 Service Design

**Status:** Shipped (WI-326–328 on `feat/odata-service`); design baseline WI-325  
**Story:** [`docs/workitems/completed/20260623-odata-service/STORY.md`](../../workitems/completed/20260623-odata-service/STORY.md)  
**Backlog:** P-41

---

## Goal

Expose Mill physical data as an **OData v4** read API for BI tools (Power BI, Excel, etc.): service document, `$metadata` (CSDL), entity set reads with query options, and **`$expand`** on navigation properties declared in metadata.

Entity sets map to **physical tables** from `SchemaProvider`, enriched with **descriptive** and **relation** metadata facets from `SchemaFacetService`.

**Platform role:** standalone data-access feature. OData is **not** a phase of [Substrait → RelNode internal IR migration](substrait-to-relnode-migration.md); it reuses the existing dispatcher + `PlanRewriteChain` via a RelNode→Substrait adapter.

---

## URL and configuration

| Item | Value |
|------|--------|
| Base path | `/services/odata/{schema}.svc` (RWS service root must end with `.svc`; catalog at `/services/odata/schemas`) |
| Enable | `mill.data.services.odata.enable` |
| Public host hint | `mill.data.services.odata.external-host` (same pattern as `export` / `query`) |
| Optional | `default-scope` (metadata facet resolution), `max-top` |
| Security | `/services/**` — [`rest-api-authorization-inventory.md`](../security/rest-api-authorization-inventory.md) |
| Activation | `@ConditionalOnService(value = "odata", group = "data")` |

---

## Modules

| Module | Path | Role |
|--------|------|------|
| `mill-data-odata` | `data/mill-data-odata/` | EDM model, EDM→`RexNode`, `ODataRelComposer`, `ODataQueryExecutor`; **no Spring** |
| `mill-data-odata-service` | `services/mill-data-odata-service/` | RWS parser/processor/renderer, synchronous MVC controller (`MillODataSyncService`), properties, `testIT`, wired into `mill-service` |
| `mill-data-backend-core` | (shared) | `SubstraitPlanExecutor` — Substrait `Plan` → `DataOperationDispatcher` (no Calcite) |
| `mill-data-backends` | (shared) | `RelBuilderFactory`, `RelToSubstraitPlanConverter`, `RelPlanDispatcherBridge` |

### Module boundary (locked)

**No Calcite dependencies in `mill-data-backend-core`.** RelNode composition and Rel→Substrait conversion live in **`mill-data-backends`** (alongside `PlanConverter`, `CalciteSqlProvider`).

`mill-data-odata` depends on `mill-data-backends` (**`implementation`**, WI-327+) for `RelBuilderFactory` and `RelPlanDispatcherBridge`. This couples the feature module to the Calcite backend — tolerated; dependency is not re-exported as `api`.

OData code **must not** open `CalciteContextFactory`, `FrameworkConfig`, or `VolcanoPlanner` directly — inject **`RelBuilderFactory`** and **`RelPlanDispatcherBridge`** only.

---

## EDM model (locked)

- **One OData service per physical schema** (`/services/odata/{schema}.svc`).
- **One entity set per physical table** in that schema.
- **Container:** named after the schema (e.g. `skymill`).
- **Entity set naming:** physical table name (e.g. `cities`).
- **Entity type name:** physical table name; **namespace:** `Mill.{schema}`.
- **Properties:** physical columns; `DatabaseType` → Edm primitive (complex types deferred — D-2/D-3/D-4).
- **Annotations:** descriptive facets → `@Core.Description` where available.
- **Navigation properties:** from `RelationFacet.Relation` when both endpoints are in the **same schema**; cardinality from `RelationCardinality`.
- **Metadata scope:** default global `MetadataContext`; optional `scope` query param for facet resolution (same semantics as schema explorer).

---

## Query execution: RelNode compose, Substrait execute

### Pipeline

```text
OData URI ($filter, $select, $expand, $orderby, $top, $skip)
  → RWS odata_parser (ODataUri)
  → MillODataDataSource → ODataQueryExecutor
  → EdmPropertyResolver (EDM property → column / RexInputRef)
  → ODataExpressionToRex (RWS query AST → RexNode)
  → ODataRelComposer via RelBuilderFactory:
        scan → filter → project → join($expand) → sort → fetch
  → RelPlanDispatcherBridge [mill-data-backends]
        → RelToSubstraitPlanConverter (SubstraitRelVisitor.convert)
        → SubstraitPlanExecutor [mill-data-backend-core]
        → DataOperationDispatcher (PlanRewriteChain → ExecutionProvider)
  → VectorBlockIterator → odata_renderer → OData JSON
```

Same extra hop as the SQL path: **RelNode → Substrait → RelNode** at execution. Policy rewrites apply via existing **`PlanRewriteChain`** on the Substrait plan.

### Shared backend artifacts (WI-327)

| Artifact | Module | Purpose |
|----------|--------|---------|
| `RelBuilderFactory` | `mill-data-backends` | Catalog-bound `RelBuilder` for composers |
| `RelToSubstraitPlanConverter` | `mill-data-backends` | `RelRoot` → `io.substrait.plan.Plan` via `SubstraitRelVisitor` |
| `RelPlanDispatcherBridge` | `mill-data-backends` | Converter + executor for RelNode callers |
| `SubstraitPlanExecutor` | `mill-data-backend-core` | `Plan` → `QueryRequest` → `dispatcher.execute` (generalize export pattern) |

### `$filter` / EDM → `RexNode`

| Concern | Rule |
|---------|------|
| Property resolution | Edm property → physical column; unknown → **400** |
| Literals | `RexLiteral` with correct `RelDataType` |
| Operators (v1) | `eq`, `ne`, `gt`, `ge`, `lt`, `le`, `and`, `or`, `not`; `contains` / `startswith` / `endswith` on strings |
| Null | `IS NULL` / `IS NOT NULL` |
| Unsupported | **400** OData error — **never** ignore `$filter` or full-table scan fallback |
| Expand filters | Bind to correct join alias field refs |

**Forbidden:** OData → SQL string → `SqlProvider` for predicates.

### Other query options

| Option | v1 |
|--------|-----|
| `$select` | `RelBuilder.project` |
| `$orderby` | `RelBuilder.sort` |
| `$top` / `$skip` | Fetch / offset (respect `max-top`) |
| `$expand` | `RelBuilder.join` from `RelationFacet` keys (prefer attribute keys over `joinSql` parse) |
| `$count` | Deferred (v1.1) |

### `$expand` and adapter coverage

Join chains are supported in principle — the SQL path already round-trips multi-join plans through `SubstraitRelVisitor`. Risk is **Rel shape mismatch** from `ODataRelComposer` vs `SqlToRelConverter`, not missing join support.

**WI-327 gate tests** (Skymill): scan → single expand → dual expand (alias stress) → short chain → expand + `$filter`. Converter failure → **400** `NotSupported`, never silent full scan.

**v1 limits (until matrix green):** max expand depth **2**, max **3** navigations per request.

---

## Relationship to Substrait migration

OData does **not** block or advance the [substrait-to-relnode-migration](substrait-to-relnode-migration.md) story. It uses Substrait at the dispatcher boundary intentionally.

| Topic | OData v1 behaviour |
|-------|-------------------|
| Execution IR | Compose **RelNode**; execute via **Substrait Plan** + existing dispatcher |
| `PlanRewriteChain` | **Applies** (security / facet rewrites on Substrait plan) |
| SQL / gRPC paths | Unchanged |
| Future migration | When SQL/export move to RelNode-only execution, OData adapter may be retired or shortened |

---

## Out of scope (v1)

- OData v2, CUD, `$batch`, actions/functions, delta feeds
- Non-physical / metadata-only entity sets
- OpenAPI (OData uses `$metadata` CSDL)
- Complex/nested Edm types (D-2/D-3/D-4)
- Multi-query `$expand` fallback (deferred v1.1)

---

## Library and platform

| Item | Choice |
|------|--------|
| OData framework | **[RWS/odata](https://github.com/RWS/odata)** `com.sdl:*` **2.16.1** (`odata_api`, `odata_edm`, `odata_parser`, `odata_processor`, `odata_renderer`; **no** Pekko `odata_service`) |
| Retired | Apache Olingo (Attic Dec 2025) — **do not use** |
| Platform JDK | **Java 25** (RWS 2.14.1+ bytecode); **WI-324** prerequisite |
| Spring alignment | RWS 2.16.1 targets Spring Boot **4.0.6** (matches Mill) |

Pin all `com.sdl` artifacts to the same version in `libs.versions.toml`. Prefer modular deps over `odata_webservice` fat container.

---

## Risks

- **JDK 25** blast radius across repo/CI (WI-324)
- RWS vendor framework + Pekko/Scala transitive deps
- RWS `DataSource` JPA-oriented examples — Mill must push filters via `ODataQueryExecutor`
- EDM → `RexNode` coverage vs BI client `$filter` dialect
- `SubstraitRelVisitor` rejection of some `ODataRelComposer` Rel shapes (`$expand` + `$select`)
- `mill-data-odata` → `mill-data-backends` coupling (tolerated; `implementation` scope)
- Edm → `RelDataType` mapping edge cases (decimals, timestamps)
- Power BI `$expand` depth and navigation naming

---

## References

- [`query-result-execution-service.md`](query-result-execution-service.md) — thin HTTP adapter pattern
- [`ExportVectorBlockSource`](../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/export/ExportVectorBlockSource.java) — Plan → dispatcher pattern
- [`CalciteSqlProvider`](../../../data/mill-data-backends/src/main/java/io/qpointz/mill/data/backend/calcite/providers/CalciteSqlProvider.java) — `SubstraitRelVisitor.convert` prior art
- [`SchemaFacetService`](../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacetService.kt)
- [`RelationFacet`](../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/facet/RelationFacet.kt)
- Skymill relations: [`test/datasets/skymill/skymill-meta-repository.yaml`](../../../test/datasets/skymill/skymill-meta-repository.yaml)
