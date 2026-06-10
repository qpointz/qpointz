# WI-268 - Visual Analysis spec, board IR, and compiler design

Status: `planned`
Type: `feature`, `docs`
Area: `data`, `services`, `ui`
Backlog refs: **U-14**

## Goal

Design the versioned **Visual Analysis spec** and the compiler that turns paths/boards into executable
SQL or relational plans.

The compiler is the core technical boundary: the UI edits board specs, while backend services compile
those specs into queries for preview, export, and eventual materialization.

## Scope

1. Define `VisualAnalysisSpec` JSON:
   - `specVersion`
   - `paths[]`
   - `parameters[]`
   - optional `dashboard`
2. Define `AnalysisPath`:
   - stable `id`
   - `name`
   - `sourceRef`
   - ordered `boards[]`
   - optional `resultRef`
3. Define `SourceRef` variants:
   - physical table: schema/table reference
   - saved SQL analysis result
   - Visual Analysis path result
   - future dataset/materialized result
4. Define first board specs:
   - `SourceBoardSpec`
   - `TableBoardSpec`
   - `FilterBoardSpec`
   - `SelectBoardSpec`
   - `ExpressionBoardSpec`
   - `AggregateBoardSpec`
   - `JoinBoardSpec`
   - `ChartBoardSpec`
   - `CalculationBoardSpec`
   - `ResultBoardSpec`
5. Define compiler output:
   - SQL string for `/api/v1/query` preview.
   - Column schema expectations.
   - Parameters to bind or inline safely.
   - Warnings for unsupported/ambiguous specs.
6. Define deterministic compiler tests:
   - one fixture per board type.
   - chained-board fixtures.
   - invalid board specs.
   - join ambiguity and missing-column cases.
7. Define correctness diagnostics:
   - non-deterministic window/order warnings.
   - timezone bucketing/display choices for date/time boards.
   - type mismatch warnings for filters, joins, expressions, and aggregates.

## Acceptance

- A versioned Visual Analysis JSON schema is documented.
- Every MVP board type has a minimal operation spec and validation rules.
- Compiler output contract is documented and testable.
- Generated SQL/plans stay backend-owned; UI does not hand-author arbitrary SQL for visual boards.
- Known limitations are explicit.

## Dependencies

- Schema explorer APIs must provide column names and logical/physical types.
- Query result sessions must support preview execution and paging.

## Non-Goals

- No production materialization in this WI.
- No dashboard builder implementation in this WI.
- No map/time-series/anomaly boards in the MVP compiler.
