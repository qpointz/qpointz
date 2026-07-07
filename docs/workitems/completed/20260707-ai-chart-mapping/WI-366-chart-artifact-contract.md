# WI-366 - SQL artifact visualization protocol

Status: `done`  
Type: `design`, `feature`  
Area: `ai`, `services`, `ui`

## Problem Statement

Chart configuration belongs inside the durable SQL artifact. The protocol needs to avoid duplicated
query context, awkward UI grouping, and premature persistence before requested enrichment is
complete.

The generated SQL artifact must become the single durable analytical artifact for a SQL statement.
It needs a clear nested protocol that can hold SQL details, human context, optional schema,
visualization configs, and future profiling data.

## Goal

Define the canonical `sql-query.generated-sql` payload shape for enriched SQL artifacts. Chart
visualizations are entries in `visualizations[]`.

## Locked Decisions

- The durable artifact remains `sql-query.generated-sql` persisted as `sql.generated`.
- Root sections are `sql`, `info`, optional `schema`, optional `visualizations[]`, and optional
  `profiling[]`.
- `schema[]` comes from `describe_sql` and is required before chart visualization validation.
- Chart configs are renderer-agnostic `visualizations[]` entries with semantic roles.
- Row snapshots are runtime render inputs, not durable payload.
- Story naming is canonical: use `visualizations[].key`, `schema[]`, nested `sql`, and nested
  `info`; do not add a legacy alias layer for new emissions.
- Existing chart design docs are legacy until rewritten; prefer this WI and `STORY.md` where they
  conflict.
- Completion is driven by a turn-scoped **completion plan** (G-26 Option D), not by validator side
  effects. Document `completionMode` recipes alongside the artifact protocol.

## In Scope

1. Define the nested generated SQL artifact payload:
   - `artifactType: "generated-sql"`
   - `sql.text`
   - `sql.dialectId`
   - `sql.statementKind`
   - `sql.source`
   - `sql.validationWarnings`
   - `info.title`
   - `info.description`
   - optional `schema[]`
   - optional `visualizations[]`
   - optional `profiling[]`
2. Define `schema[]` as the trusted result schema returned by `sql-query.describe_sql`.
3. Define `visualizations[]` as renderer-agnostic visualization configs.
4. Define the MVP chart visualization entry:
   - `key`
   - `kind: "chart"`
   - optional `title`
   - optional `description`
   - `chartType`
   - `encodings`
   - optional `options`
   - optional `presentation`
5. Keep chart encoding roles semantic and chart-type-specific:
   - `bar` and `pie`: `category`, `value`, optional supported roles from the catalog
   - `line` and `area`: `x`, `y`, optional `series`
   - `scatter`: `x`, `y`, optional `series` or `color`
6. Define `profiling[]` as a reserved section for future non-LLM query analysis payloads. This WI
   documents the section but does not implement any profiling producer.
7. Document that durable artifacts do not embed row snapshots.
8. Document validation failure behavior: invalid visualization requests produce assistant text only;
   they do not add failed entries to `visualizations[]`.
9. Update story-local references so this WI is the protocol source for the rewritten story.
10. Update the design hub docs that still describe the previous topology:
    - `docs/design/agentic/chart-artifact-contract.md`
    - `docs/design/agentic/generated-sql-artifact-context.md`
    - chart specs under `docs/design/agentic/charts/`
    - relevant cross-links from artifact/chat architecture docs
11. Update `COLDSTART.md` after the protocol docs are aligned.
12. Document **completion plan** recipes (ephemeral, turn-scoped — not a durable artifact):
    - `completionMode: sql-only` — required steps: `validate_sql`
    - `completionMode: sql-with-chart` — required steps: `validate_sql`, `describe_sql`,
      `validate_chart_spec`
    - `completionMode: enrich-existing` — follow-up on resolved `sql.generated`; required steps per
      G-27
    - terminal rules for chart validation failure (G-28)
13. Document **validation purity** (G-29): `validate_*` tools return structured results only; no
    `emitsOnSuccess` durable emission from validators.

## Out of Scope

- Implementing the completion coordinator; that belongs to WI-367.
- Implementing chart-mapping tools; that belongs to WI-368.
- Implementing persistence, SSE, and REST replay; that belongs to WI-369.
- Implementing UI rendering; that belongs to WI-370.
- Implementing explain-plan, lineage, or profiling producers.

## Acceptance Criteria

- [x] The SQL artifact payload is documented with `sql`, `info`, optional `schema`,
      `visualizations[]`, and optional `profiling[]`.
- [x] The document states that `sql-query.generated-sql`, `sql.generated`, `wirePartType: sql`, and
      `last-sql` remain the protocol/persistence surface.
- [x] Chart visualization configs are documented as `visualizations[]` entries with `kind: "chart"`.
- [x] The protocol supports one or more chart visualizations for the same SQL result.
- [x] The protocol keeps all chart configuration inside SQL artifact `visualizations[]`.
- [x] Renderer-specific config is explicitly forbidden in durable visualization entries.
- [x] Every chart encoding field must reference a column in `schema[]`.
- [x] Row snapshots are documented as runtime render inputs, not durable artifact payload.
- [x] `profiling[]` is documented as reserved future query analysis data.
- [x] Future chart families can be represented by new visualization entries without changing the
      root SQL artifact structure.
- [x] Design docs no longer present the previous chart topology as the active implementation path.
- [x] `COLDSTART.md` points future implementers at the SQL artifact visualization model.
- [x] Completion plan `completionMode` recipes and terminal rules are documented.
- [x] Validation purity is documented: validators do not emit durable artifacts.

## Suggested Commit

`[docs] WI-366: define SQL visualization protocol`
