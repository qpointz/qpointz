# WI-327 — RWS query AST → Rex, RelComposer, and Rel→Substrait adapter

**Story:** [`odata-service`](STORY.md) · **Backlog:** P-41  
**Status:** done  
**Depends on:** WI-326

## Goal

Predicate pushdown and query composition via **Calcite RelNode**, then execution through the existing
**Substrait dispatcher path** (adapter + `PlanRewriteChain`). No SQL strings.

**Before coding:** read [`COLDSTART.md`](COLDSTART.md) § adapter pipeline and § Skymill converter gate.

## Deliverables

### `mill-data-backend-core` (no Calcite)

1. **`SubstraitPlanExecutor`** — `Plan` + `QueryExecutionConfig` → `VectorBlockIterator` via `DataOperationDispatcher` (generalize export pattern)

### `mill-data-backends`

2. **`RelBuilderFactory`** + **`CalciteRelBuilderFactory`**
3. **`RelToSubstraitPlanConverter`** + **`CalciteRelToSubstraitPlanConverter`** — extract `SubstraitRelVisitor.convert` from `CalciteSqlProvider`
4. **`RelPlanDispatcherBridge`** — `RelRoot` → converter → `SubstraitPlanExecutor`
5. **`RelToSubstraitCompatibilityTest`** — Skymill tiers T0–T5 (see COLDSTART)

### `mill-data-odata`

6. **`EdmPropertyResolver`** — EDM property → column / field index
7. **`ODataExpressionToRex`** — RWS `com.sdl.odata.api.processor.query.*` criteria AST → `RexNode`
8. **`ODataRelComposer`** — scan, filter, project, join (`$expand`), sort, limit
9. **`ODataQueryExecutor`** — compose + `RelPlanDispatcherBridge`
10. Golden tests: `$filter` → Rel explain; expand plan shapes
11. **`build.gradle.kts`:** `implementation(project(":data:mill-data-backends"))`; add `odata_parser` if needed for AST types in core

### Autoconfigure (same WI or start of WI-328)

12. Spring beans for `RelBuilderFactory`, `RelToSubstraitPlanConverter`, `RelPlanDispatcherBridge`, `SubstraitPlanExecutor`

## Acceptance

- Backends converter gate tests green (T0–T5)
- OData unit tests green; mocked bridge proves `RelRoot` reaches converter
- `dispatcher.execute` receives `QueryRequest` with **`plan`** set
- No HTTP

## Prior art (in repo)

- `CalciteSqlProvider` — `SubstraitRelVisitor.convert`
- `ExportVectorBlockSource` — Plan → dispatcher

## Completion (normative)

After verify passes: update **tracker** (`STORY.md` `[x]`, this file status) → **commit** (include tracker in commit) → **push** → **CI green**. See [`STORY.md`](STORY.md) § Implementation delivery workflow.
