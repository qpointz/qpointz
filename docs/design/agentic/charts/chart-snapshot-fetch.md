# Chart snapshot fetch (query-service full mode)

Normative for **WI-370** / Gap 23. Chart rendering needs a **bounded full result**, not a grid page.

Aligns with Gap 7 ([`GAPS.md`](../../../workitems/in-progress/ai-chart-mapping/GAPS.md) §7) and
[`QueryResultSqlQueryExecutionPort`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/sqlquery/QueryResultSqlQueryExecutionPort.kt)
`executeFull` semantics.

## Split: paged vs full

| Surface | `resultMode` | Purpose |
|---------|--------------|---------|
| Data tab grid, Analysis playground | `paged` | User page size; session + `fetchQueryPage` |
| Chart tab, Run on chart composite | `full` | Accumulate up to catalog/UI `maxRows` in one helper call |

Grid page size and chart snapshot limit are **independent** — never use `readStoredQueryPageSize()` for charts.

## mill-ui API

Extend [`QueryExecuteOptions`](../../../../ui/mill-ui/src/types/query.ts):

```ts
export interface QueryExecuteOptions {
  pageSize?: number;           // paged only (default)
  resultMode?: 'paged' | 'full'; // default 'paged'
  maxRows?: number;              // full only; required when resultMode === 'full'
}
```

Add a thin helper (name illustrative):

```ts
fetchChartSnapshot(sql: string, maxRows: number): Promise<ChartSnapshotResult>
```

`ChartSnapshotResult` carries `rows`, `columns`, `truncated`, `hasMore`, `totalResult`, `limit` —
same completeness metadata as `sql-query.execute_sql` full mode.

Implementation delegates to `queryService.executeQuery(sql, { resultMode: 'full', maxRows })`.

## Query-service behaviour

**One logical fetch** from the UI caller's perspective (no manual page loop in chart components).

Preferred implementation path for WI-370:

1. **mill-ui `queryService`**: when `resultMode === 'full'`, create a session via `POST /api/v1/query`,
   accumulate pages server-side or client-side using the same loop as `executeFull`, close session,
   return bounded rows + truncation flags.
2. **Optional follow-up**: expose `resultMode` / `maxRows` on `POST /api/v1/query` so accumulation
   lives entirely in `mill-data-query-service` (single HTTP round-trip). Not required for first
   chart ship if the shared accumulation helper is centralized in `queryService.ts`.

Hard caps: `maxRows` is clamped to chart catalog `hardLimit` when known, else a conservative UI
default (e.g. 500). If `truncated === true`, Chart tab shows refinement error — never a silent partial chart.

## Reuse of existing `data` artefact

Before executing, check whether composite `data` already satisfies the chart:

- Same normalized SQL
- `rowCount >=` rows needed for chart encodings **and**
- `rowCount <=` chart snapshot limit **and**
- `truncated !== true` (or explicit `resultMode: full` on stored artefact when available)

Otherwise refetch via `fetchChartSnapshot`.

## Owner

**WI-370** — `queryService` + chart Run paths; catalog limits from **WI-368** / **WI-366**.
