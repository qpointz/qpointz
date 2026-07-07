# Query artifact: presentations and attachments

**Status:** Locked design savepoint (2026-07-02)
**Supersedes:** split `generated-sql` + `generated-chart` durable artifacts (legacy replay supported)
**Related:** [`artifact-foundation.md`](./artifact-foundation.md), [`chart-artifact-contract.md`](./chart-artifact-contract.md), [`charts/multi-chart-artifact-model.md`](./charts/multi-chart-artifact-model.md)

## Problem

Chart mapping (WI-366–370) introduced **two durable artifact kinds** per analytical turn:
`sql.generated` and `chart.generated`. UI merge logic and `embeddedChart` persistence glue reunite
them after the fact. That does not scale to:

- multiple chart views on one query (partially solved via `charts[]`)
- SQL analytics (EXPLAIN plan, lineage) that are **not** LLM-authored visualizations
- future panels (query narrative, comparison, stats)

## Principle

**One durable query artifact per SQL statement on a turn.** Capabilities remain separate
(`sql-query`, `chart-mapping`, future `sql-analytics`, `metadata`). Tools validate and produce
structured output. The runtime **assembles** one bundle — not multiple competing artifacts.

Row data (`sql.result`) stays **outside** the artifact: bounded execution snapshots are runtime
inputs for grid/chart renderers, not LLM reasoning output.

## Artifact type: `generated-query`

| Field | Required | Role |
|-------|----------|------|
| `artifactType` | yes | Always `"generated-query"` |
| `title` | yes | Human headline (from `validate_sql`) |
| `description` | yes | Plain-language summary |
| `sql` | yes | Canonical validated SQL |
| `dialectId` | yes | Active dialect |
| `statementKind` | yes | e.g. `select` |
| `source` | yes | Typically `"generated"` |
| `validationWarnings` | yes | May be `[]` |
| `columns` | no | Result schema from `describe_sql` when available |
| `presentations` | yes | UI panels (see below) |
| `attachments` | no | Structured facts from non-LLM tools (see below) |

`persistKind`: **`query.generated`** (migration alias: read compat for `sql.generated` rows).

Pointer: **`last-query`** replaces split `last-sql` + `last-chart` for whole-bundle follow-ups.

## Two layers: presentations vs attachments

Do **not** put SQL analytics (EXPLAIN, lineage) into a single `visualizations[]` array that implies
LLM reasoning. Split responsibilities:

### `presentations[]` — what the UI shows (tabs / panels)

Explicit render intents. Each entry has:

| Field | Role |
|-------|------|
| `presentationKey` | Stable id within artifact (e.g. `grid`, `tier-bar`, `plan`) |
| `kind` | Renderer discriminator: `grid`, `chart`, `explain-plan`, `lineage`, … |
| `producer` | Capability or `system` that contributed this panel |
| `required` | When `true`, UI must always offer this panel (only `grid`) |
| `attachmentRef` | Optional link to `attachments[].key` for data-backed panels |

**Invariant:** After successful `validate_sql`, the server **always** injects:

```json
{ "presentationKey": "grid", "kind": "grid", "producer": "system", "required": true }
```

Users never lose the table path because the LLM omitted a visualization.

Chart panels are added when `validate_chart_spec` passes — same artifact, append/replace by
`presentationKey`. Multiple charts: multiple `presentations[]` entries with `kind: chart` (or one
entry with `charts[]` inside — see migration note below).

### `attachments[]` — structured facts (SQL / metadata analytics)

Optional blobs produced by **tools**, not invented by the model:

| `kind` | Typical producer | Example |
|--------|------------------|---------|
| `explain-plan` | `sql-query` / `sql-analytics` | Calcite/ engine EXPLAIN JSON |
| `lineage` | `metadata` + SQL parse | Entity/column refs used in query |
| `stats` | engine (future) | Cost, row estimates |

```json
{
  "key": "explainPlan",
  "kind": "explain-plan",
  "producer": "sql-query",
  "payload": { "format": "json", "plan": { } }
}
```

A **presentation** can reference an attachment:

```json
{
  "presentationKey": "plan",
  "kind": "explain-plan",
  "producer": "sql-query",
  "attachmentRef": "explainPlan"
}
```

Attachments may be **lazy**: filled when the user opens the tab or clicks Run; idempotent refresh
updates the same artifact row.

## Capability contribution model

| Capability | Contributes to artifact | Emits separate durable artifact? |
|------------|-------------------------|----------------------------------|
| `sql-query` | Core fields + `grid` presentation | **No** (target) |
| `chart-mapping` | `chart` presentation(s) | **No** (target) |
| `sql-analytics` (future) | `attachments.explainPlan` + optional presentation | **No** |
| `metadata` (future) | `attachments.lineage` + optional presentation | **No** |
| Execution / client | `sql.result` rows | **Yes** — runtime only |

LLM **reasons** which presentations to *request* (e.g. bar chart). EXPLAIN and lineage are
**computed**; the model may trigger the tool ("show plan") but does not author the plan JSON.

## Emission (target)

Today (transitional): two `ProtocolFinal` events (`generated-sql`, `generated-chart`); persistence
may merge chart into `embeddedChart` on the SQL row.

Target:

1. `validate_sql` → upsert turn scratchpad `QueryArtifactDraft` → emit **one**
   `ProtocolFinal(generated-query)` with core + `presentations: [grid]`.
2. `describe_sql` → update draft `columns` (no new artifact).
3. `validate_chart_spec` → append chart presentation(s) → re-emit or persist-update same artifact.
4. Future: `explain_sql` → append `attachments` + optional presentation.

Single SSE `wirePartType: query` per turn (migration: accept legacy `sql` + `chart` parts).

## UI mapping

| `presentation.kind` | Tab / panel | Data source |
|---------------------|-------------|-------------|
| `grid` | Data | Runtime `sql.result` / query execution |
| `chart` | Chart | Snapshot + chart compiler (`charts/` specs) |
| `explain-plan` | Plan | `attachments[attachmentRef]` |
| `lineage` | Lineage | `attachments[attachmentRef]` |
| (root) | SQL | `sql` field on artifact |

Default tab: first non-grid presentation if present, else grid (live); replay may prefer chart when
present (see [`charts/chart-reply-view.md`](./charts/chart-reply-view.md)).

Reuse existing [`sql-data-composite`](../../ui/mill-ui/src/components/chat/artifactPreview/types.ts)
chrome until native `query` card lands; then tabs driven by `presentations[]`.

## Migration from current branch

| Current | Transitional (branch) | Target |
|---------|----------------------|--------|
| `generated-sql` + `generated-chart` rows | `embeddedChart` on `sql.generated` | one `query.generated` row |
| `charts[]` on chart payload | `embeddedChart.charts` | `presentations` with `kind: chart` |
| `wirePartType` sql + chart | fan-out replay via `toWireResponses` | single `query` part |
| `last-sql`, `last-chart` | both point at merged row | `last-query` |

Compat shim on read: synthesize `presentations` from `embeddedChart` + implicit `grid`; map
`generated-chart` wire parts to chart presentations.

## Non-goals (this contract)

- Embedding row snapshots in the query artifact
- Combining two different SQL statements into one artifact
- Renderer-specific config (ECharts options) in durable payload
- Replacing facet-proposal or other non-query artifact families

## Checklist (implementation stories)

See [`query-artifact-presentations/STORY.md`](../../workitems/planned/query-artifact-presentations/STORY.md).
