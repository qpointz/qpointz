# mill-ui Analysis modes: SQL and Visual Analysis

Add a second authoring mode to **Analysis** so Mill supports two intentionally different user
experiences over the same data/query platform:

- **SQL Analysis** for technical users who want direct SQL, schema-aware editing, result paging,
  export, and saved query reuse.
- **Visual Analysis** for business users who want a guided, Contour-style path/board workflow:
  choose a source, add visual transformation boards, inspect results at each step, and present or
  materialize selected outputs without writing SQL.

This story is a **planning and foundation story**. It defines the product model, contracts, UI
navigation, and initial implementation slices for bringing Visual Analysis into Mill as an extra
capability of the existing Analysis area, not as a separate application.

## Product Intent

Mill should expose **one Analysis area** with multiple analysis types. Users should not have to know
which backend service or transport powers the experience. The create flow decides the authoring mode:

1. **New SQL Analysis** - opens the current SQL editor/result-grid workflow.
2. **New Visual Analysis** - opens a visual workspace with paths and boards.

Both modes should share platform services where possible:

- Schema/model discovery from the existing schema explorer APIs.
- Query execution and page retrieval through **`/api/v1/query/**`**.
- File export through **`/services/export/**`**.
- Search, dashboard counts, permissions, ownership, tags, and audit semantics.
- Future materialization into durable datasets/jobs.

The modes differ in their saved **spec** and authoring UI. SQL Analysis stores user-authored SQL.
Visual Analysis stores a structured board graph that compiles to SQL / relational plans for preview
and execution.

## Contour-Inspired Capabilities

The target Visual Analysis workflow is inspired by Palantir Foundry Contour documentation:

- Analyses contain one or more **paths**; a path starts from a dataset/table or another path result.
- Paths contain ordered **boards**; boards filter, transform, aggregate, join, visualize, and inspect
  data.
- Each board exposes a previewable output so users can verify logic incrementally.
- Parameters can be defined once and used by filters/expressions/dashboards.
- Dashboard mode presents selected boards with parameter overrides.
- Path results can eventually be saved/materialized as durable datasets or pipeline logic.

Mill must implement these concepts in Mill terms and UX, not copy Contour branding or UI. Use
**Visual Analysis**, **Path**, and **Board** as product vocabulary unless a later design review
chooses different names.

## Target Domain Model

```text
Analysis
  id
  type: SQL | VISUAL
  name
  description
  owner / visibility / permissions
  tags
  createdAt / updatedAt
  specVersion
  spec

SqlAnalysisSpec
  sql
  parameters[]

VisualAnalysisSpec
  paths[]
  parameters[]
  dashboard?

AnalysisPath
  id
  name
  sourceRef
  boards[]
  resultRef?

Board
  id
  type
  name
  description?
  inputRef
  operationSpec
  layout?
```

## Initial Visual Board Set

The first usable Visual Analysis release should include only the board types needed for an honest
business-user workflow:

| Board | Purpose | Notes |
|-------|---------|-------|
| `source` | Select table, saved SQL analysis, or path result | Reads schema/catalog metadata |
| `table` | Inspect rows after any board | Uses paged query sessions |
| `filter` | Build predicates visually | Supports parameter values |
| `select` | Choose, reorder, rename columns | Also supports dropping columns |
| `expression` | Derive a new column | Uses controlled expression grammar |
| `aggregate` | Group by + measures | Supports count, unique count, min, max, sum, mean |
| `join` | Join another table/path result | Inner and left join first; more later |
| `chart` | Present simple chart | Bar, line, scatter, histogram after MVP |
| `calculation` | KPI cards | Count/sum/mean/min/max cards |
| `result` | Preview and optionally materialize path output | Materialization later |

The MVP should prioritize correctness, previewability, and comprehensible generated SQL over breadth.

## Design Constraints

- Do not fork Analysis into a separate frontend module; extend `ui/mill-ui`.
- Do not add a new compute engine in the first story. Compile Visual Analysis specs into SQL or
  existing relational execution primitives.
- Do not bypass the existing query result session API for previews.
- Do not couple UI board specs directly to JPA entities.
- Treat persisted specs as versioned contracts; include `specVersion`.
- Keep SQL mode intact for technical users.
- Keep Visual Analysis language business-friendly: columns, filters, groups, measures, joins,
  charts, paths; avoid exposing relational algebra terms in primary UI.

## Key Risks

- **Spec drift:** board JSON must be stable and versioned before users create durable analyses.
- **Generated SQL correctness:** every board compiler needs deterministic tests with known SQL/plans.
- **Large data previews:** previews must page and cap data; never load full result sets into the UI.
- **Join ambiguity:** business users need metadata-assisted join suggestions, but the first version
  must still support explicit manual join keys.
- **Expression safety:** expression board should start with a constrained grammar/function catalog,
  not arbitrary unchecked SQL snippets.
- **Permissions:** visual analyses can reference multiple data resources; sharing must account for
  underlying data access.
- **Materialization semantics:** saving path results as durable datasets/jobs should be a later,
  explicit story with lineage and rebuild semantics.

## References

- [`docs/design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)
- [`docs/design/platform/query-result-execution-service.md`](../../../design/platform/query-result-execution-service.md)
- [`docs/design/platform/export-service.md`](../../../design/platform/export-service.md)
- [`docs/public/src/mill-ui.md`](../../../public/src/mill-ui.md)
- Palantir Contour overview: <https://www.palantir.com/docs/foundry/contour/overview>
- Palantir Contour core concepts: <https://www.palantir.com/docs/foundry/contour/core-concepts>
- Palantir Contour boards: <https://www.palantir.com/docs/foundry/contour/boards-overview/>
- Palantir Contour parameters: <https://www.palantir.com/docs/foundry/contour/analysis-parameterize/>

## Work Items

- [ ] WI-267 - Analysis mode product model and API contract (`WI-267-analysis-mode-product-model.md`)
- [ ] WI-268 - Visual Analysis spec, board IR, and compiler design (`WI-268-visual-analysis-spec-and-compiler-design.md`)
- [ ] WI-269 - Analysis catalog persistence and REST planning (`WI-269-analysis-catalog-persistence-rest-planning.md`)
- [ ] WI-270 - mill-ui Analysis mode selection and routing plan (`WI-270-mill-ui-analysis-mode-selection-routing.md`)
- [ ] WI-271 - Visual Analysis board MVP implementation plan (`WI-271-visual-analysis-board-mvp-plan.md`)
- [ ] WI-272 - Visual Analysis parameters, dashboards, sharing, and materialization roadmap (`WI-272-visual-analysis-advanced-roadmap.md`)
