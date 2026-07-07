# Gaps and open decisions — ai-chart-mapping

**Story:** [`STORY.md`](STORY.md)
**Branch:** `restart/ai-chart-mapping-after-stage1`
**Status:** in progress — clean restart branch after Stage 1; Stages 2-6 definitions retained and progress reset

This document captures **gaps**, **ambiguities**, and **decisions still needed** before or during implementation. Resolved items should move into WI acceptance criteria, STORY architectural table, or design docs, then struck from here.

---

## 0. Implementation completeness (baseline)

| Area | Expected (story scope) | Current repo state |
|------|------------------------|-------------------|
| `sql-query` capability + execution/schema tools | WI-338–WI-341 | **Done** — `describe_sql` / `execute_sql`, Spring wiring, MCP tests |
| Design contract (`generated-chart`) | WI-366 | **Planned** — definitions restored; implementation progress reset |
| `sql-query.generated-sql` `title` / `description` | WI-367 | **Planned** — definition retained; implementation progress reset |
| `chart-mapping` capability + catalog tool | WI-368 | **Planned** — definition retained; implementation progress reset |
| Runtime / wire / persistence (`chart.generated`) | WI-369 | **Planned** — definition retained; implementation progress reset |
| mill-ui chart parse / render / Run all | WI-370 | **Planned** — definition retained; implementation progress reset |
| Scenario / baseline tests (`*Chart*`) | STORY Verify | **Planned** — run as WIs land |
| Story tracker | STORY | WI-338-WI-341 `[x]`; WI-366-WI-370 `[ ]` |

**Verdict:** Stage 1 remains implemented as the prerequisite baseline. Stages 2-6 should be implemented cleanly from the retained definitions and locked gap decisions.

---

## Stage 7 - Unified query artifact (savepoint — planned follow-up)

**Gap:** Split `generated-sql` + `generated-chart` durable rows and dual SSE wire parts do not scale
to EXPLAIN plan, lineage, or additional panel types. A prior implementation attempt used a transitional `embeddedChart` merge (`SqlChartArtifactPersistence`); this reset branch keeps only the locked direction.

**Locked direction (2026-07-02):**

| Topic | Decision |
|-------|----------|
| Durable unit | One `generated-query` per SQL statement (`persistKind: query.generated`) |
| UI panels | `presentations[]` — `grid` always server-injected (`required: true`) |
| Chart | `presentations[]` with `kind: chart` from chart-mapping (semantic spec unchanged) |
| EXPLAIN / lineage | `attachments[]` (facts) + optional presentation tab — **not** LLM visualizations |
| Capabilities | Stay separate; only emission/persistence unify |
| Migration | Re-evaluate when the unified query artifact follow-up starts; do not assume `embeddedChart` exists on this reset branch |

**Design:** [`query-artifact-presentations.md`](../../../design/agentic/query-artifact-presentations.md)

**Does not block** `ai-chart-mapping` closure.

---

## Stage 1 - SQL-query execution foundation (WI-338-WI-341)

### 1. `sql-query` tool split and output contracts - **LOCKED**

**Gap:** The absorbed execution stage extends the existing `sql-query` capability with two related
but different tools:

| Tool | Purpose | Contract |
|------|---------|----------|
| `describe_sql` | Return SQL description and result schema metadata only. | Base `sql-query` execution envelope. |
| `execute_sql` | Return bounded rows plus the same SQL description/schema metadata. | Strict extension of `describe_sql`. |

**Locked decision:**

- Tool names are `describe_sql` and `execute_sql`.
- This is a new contract, so the only schema-description tool name is `describe_sql`; there is no
  alias and no alternate schema field such as `columns`.
- Both tools share one canonical base envelope with `sql`, `dialectId`, `schema`, `warnings`, and
  `source` where available.
- The schema field is named `schema`.
- `execute_sql` is an extension of `describe_sql`: it returns the same base fields plus bounded
  result data and execution metadata.
- `execute_sql.rows` should be JSON row objects keyed by schema field name for the first contract.

Base `describe_sql` shape:

```json
{
  "artifactType": "sql-description",
  "sql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
  "dialectId": "CALCITE",
  "schema": [
    { "name": "country", "type": "STRING", "nullable": true },
    { "name": "client_count", "type": "BIG_INT", "nullable": false }
  ],
  "warnings": [],
  "source": {
    "kind": "execution",
    "maxRows": 1
  }
}
```

`execute_sql` extension shape:

```json
{
  "artifactType": "sql-result",
  "sql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
  "dialectId": "CALCITE",
  "schema": [
    { "name": "country", "type": "STRING", "nullable": true },
    { "name": "client_count", "type": "BIG_INT", "nullable": false }
  ],
  "warnings": [],
  "source": {
    "kind": "execution"
  },
  "rows": [
    { "country": "DE", "client_count": 42 }
  ],
  "rowCount": 1,
  "truncated": false,
  "hasMore": false,
  "limit": 1000
}
```

**Owner:** **WI-338** records the contract in design docs; **WI-339** implements shared DTO/model and
tests.

---

### 2. Schema probe execution strategy - **LOCKED**

**Gap:** `describe_sql` may execute or probe SQL internally for the MVP, but the exact
strategy is unspecified.

Considered options:

| Option                      | Detail                                                        | Tradeoff                                                           |
| --------------------------- | ------------------------------------------------------------- | ------------------------------------------------------------------ |
| Execute with `maxRows=1`    | Run the validated query and inspect the result metadata.      | Simple; may still be expensive for queries without pushdown/limit. |
| Wrap with zero-row limit    | Dialect-specific `SELECT * FROM (...) WHERE 1=0` / `LIMIT 0`. | Not materially better for this flow if the backend still physically plans/reads data; also requires dialect-safe wrapping rules. |
| Data-plane prepare/describe | Ask backend for metadata without execution.                   | Best long term; may not exist uniformly today.                     |

**Locked decision:**

- MVP strategy is execution-backed describe: `describe_sql` uses the same data-plane execution path
  as `execute_sql` with `maxRows = 1`, then discards rows and returns only the shared base envelope.
- The tool contract stays strategy-independent. Callers only see `describe_sql` output; they must not
  depend on whether it was produced by execution, SQL wrapping, or native describe/prepare later.
- Implement the backend behind a replaceable strategy/port seam, e.g. `SqlDescriptionStrategy` or
  `SqlQueryExecutionPort.describe(...)`, so the internal implementation can later switch to
  zero-row wrapping or native backend metadata without changing the tool contract.
- The default MVP strategy should be named clearly, e.g. `ExecuteOneRowSqlDescriptionStrategy`.
- `describe_sql` must not expose the sampled row. It may include source metadata such as
  `{ "kind": "execution", "maxRows": 1 }`.
- Wrapping and native prepare/describe are future strategies, not part of the MVP implementation.
- Timeout/cancellation/error handling should reuse the query-result execution path and map failures
  to the structured `sql-query` execution error model.

**Owner:** **WI-338** design; **WI-339** port/handler; **WI-340** backend wiring.

---

### 3. Validated SQL input boundary - **LOCKED**

**Gap:** The new `sql-query` execution tools run SQL, but the story must define whether they validate SQL
themselves or require already validated SQL from orchestration.

**Locked decision:**

- Mill data access is generally read-only for this flow; `sql-query` does not add a separate
  read-only enforcement layer.
- SQL validation happens before `describe_sql` or `execute_sql` are invoked.
- In the chart flow, orchestration must obtain validated SQL before calling
  `sql-query.describe_sql` or `sql-query.execute_sql`.
- To preserve capability isolation, the `sql-query` capability must not invoke or depend on any
  upstream validation capability/tool internally.
- `sql-query` receives SQL as input and assumes the caller has satisfied the validation
  prerequisite.
- `sql-query` may still perform local input hygiene that is intrinsic to the tool boundary, such as
  rejecting blank SQL and applying limits/timeouts, but it must not own SQL semantic validation.
- Backend/data-plane failures should be surfaced as `sql-query` execution errors, not SQL validation
  diagnostics.

**Owner:** **WI-338** contract; **WI-339** implementation; **WI-341** tests for blank input /
backend failure, not SQL semantic validation.

---

### 4. Type normalization for chart validation - **LOCKED**

**Gap:** `describe_sql` must output types that chart validation can reason about, but data
backends may return native types, JDBC types, Calcite types, or string labels.

**Locked decision:**

- `schema[].type` reuses Mill logical type names from
  `LogicalDataType.LogicalDataTypeId` in [`proto/common.proto`](../../../../proto/common.proto):
  `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `BINARY`, `BOOL`, `DATE`, `FLOAT`, `DOUBLE`,
  `INTERVAL_DAY`, `INTERVAL_YEAR`, `STRING`, `TIMESTAMP`, `TIMESTAMP_TZ`, `TIME`, and `UUID`.
- `NOT_SPECIFIED_TYPE` is not a chartable type. Tool output may map it to `UNKNOWN`, and chart
  validation must reject it for roles requiring typed data.
- Chart-facing schema uses only logical type name for chart reasoning. It must not include
  precision, scale, or length in the chart-validation contract.
- `nullable` remains useful and may be included; it is orthogonal to chart type compatibility.
- `nativeType` may be included when the adapter can map backend/JDBC metadata, but it is optional
  diagnostic metadata only; validators and chart logic must not require it (see gap 8c).
- Chart validation maps Mill logical types to internal chart families:
  - numeric: `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE`
  - temporal/orderable: `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ`
  - categorical/text: `STRING`, `BOOL`, `UUID`
  - binary/unsupported: `BINARY`
  - interval/support-by-chart-only: `INTERVAL_DAY`, `INTERVAL_YEAR`
  - unknown/reject: `UNKNOWN` / `NOT_SPECIFIED_TYPE`
- Mill currently has no exact `DECIMAL` logical type; JDBC `NUMERIC`/`DECIMAL` are mapped to
  `DOUBLE` in the Mill type system, which is sufficient for charting but not precision-sensitive
  numeric semantics.

This is possible because the existing query-result schema already emits `typeId.name` from Mill
`LogicalDataTypeId` in `data/mill-data-query/.../QueryResultColumnSchema.kt`, and `mill-ui`
already normalizes names such as `INT`, `BIG_INT`, and `TIMESTAMP_TZ` for display.

**Owner:** **WI-338** defines vocabulary; **WI-339** implements normalization; **WI-366/WI-368**
consume it for chart validation.

---

### 5. SQL-query execution dependency port and backend result shape - **LOCKED**

**Gap:** WI-339 says to use `DataOperationDispatcher` or a thin port abstraction, but no port API is
specified.

**Locked decision:**

- Reuse the existing query-result execution plane instead of creating a parallel SQL execution
  backend.
- The Spring adapter for `sql-query` should inject `QueryResultExecutionService` from
  `data/mill-data-query` via `mill-data-autoconfigure`, not call the HTTP
  `/api/v1/query` controller and not bypass the query plane with direct `DataOperationDispatcher`
  usage.
- `SqlQueryExecutionPort` in `mill-ai` remains a Spring-free abstraction, but its default
  autoconfigure implementation is a thin adapter over `QueryResultExecutionService`.
- `execute_sql` creates a query-result session with `defaultFormat = rows-objects`,
  `includeFirstPage = true`, and `firstPageSize = bounded maxRows`, then maps the first page to the
  tool envelope and deletes the session after use unless a later WI deliberately introduces
  durable query-session pointers.
- `describe_sql` uses the same adapter with `includeFirstPage = true` and `firstPageSize = 1`,
  maps `firstPage.schema`, discards `firstPage.data`, and deletes the session after use. The
  strategy remains replaceable so later implementations can use a native describe/prepare path.
- Tool rows are JSON objects keyed by schema field name, matching the existing `rows-objects`
  marshaller.
- Schema is derived from `PagedQueryPayload.columnSchema`. The tool contract trims query-service
  presentation metadata down to `{ name, type, nullable, nativeType? }`; it does not expose
  query-service `idx`, `precision`, `scale`, or `length` in the chart-facing schema.
- Paging fields map from the query-result envelope: `rowCount`, `totalResult`, and `hasNext` drive
  `rowCount`, optional total metadata, `truncated`, and `hasMore` in `execute_sql`.
- Caller identity maps to query-result `CallerContext` using the same tenant semantics as
  `mill-data-query-service`.

**Owner:** **WI-339** port design; **WI-340** Spring/autoconfigure implementation.

---

### 6. Profile and runtime access for `describe_sql` - **LOCKED**

**Gap:** Chart mapping needs schema probing in normal data-analysis chat flows. The story must make
that runtime dependency explicit without creating a second capability that always travels with
`sql-query`.

**Locked decision:**

- Keep `sql-query` in the `data-analysis` profile. Charting runs in `data-analysis`, and chart
  mapping requires `sql-query.describe_sql` after SQL validation and before chart config creation.
- `data-analysis` should expose the `sql-query` capability as a normal runtime dependency for this
  story; do not create a hidden out-of-profile schema-probe path.
- Do not introduce a separate `data-query` capability or `data-execution` profile for this story.
- External MCP clients that need validation plus bounded SQL execution should use `data-analysis` or
  another explicit profile that includes `sql-query`.
- `schema-exploration` continues to exclude `sql-query.*`.
- Tool-level exposure controls for `describe_sql` vs `execute_sql` are not required to lock this
  story. If the platform only supports capability-level profile composition, `data-analysis`
  includes both tools and relies on normal orchestration/prompt rules to use `describe_sql` for
  chart schema probing. If later tool-level gating exists, it may expose `describe_sql` more broadly,
  but the story must not depend on that capability.

**Owner:** **WI-340** profile/wiring decision; **WI-368** chart profile routing.

---

### 7. SQL-query execution limits, truncation, and snapshot relationship - **LOCKED**

**Gap:** `execute_sql` has row limits, chart rendering has catalog snapshot limits, and the existing
UI query service has paged result limits. The relationship needs a shared query-plane contract so
AI tools and chart UI do not implement separate execution semantics.

**Locked decision:**

- Extend the shared query-result execution plane with explicit result mode semantics:
  `resultMode: "paged" | "full"`.
- This should be a query-service/query-engine concept first, not only an AI tool concept. Chart
  artifacts carry SQL like SQL artifacts do, so UI/runtime execution should reuse the same query
  service semantics as `sql-query.execute_sql`.
- `paged` returns one bounded presentation page plus paging/session metadata.
- `full` returns an accumulated bounded result up to caller limit and server hard cap.
- `full` never means unbounded. It must report whether the returned result is complete.
- `sql-query.execute_sql` exposes the same semantic:
  - input: `resultMode`, `maxRows`, and for paged mode optional `pageIndex` / `pageSize` or their
    first-page equivalent
  - output: the shared base schema envelope plus rows and completeness metadata
- `describe_sql` remains separate and uses the query-result execution plane with one row internally,
  then discards row data.
- Chart rendering uses `full` mode for bounded chart snapshots because a chart needs a complete
  snapshot within chart/catalog limits, not a grid page.
- Data/grid inspection uses `paged` mode.
- If `full` mode hits the caller limit or server cap, the response must set truncation metadata and
  chart rendering must not silently treat the result as complete.

Required metadata names:

| Field | Meaning |
|-------|---------|
| `rowCount` | Number of rows returned in this response. |
| `totalResult` | Total rows when known; `null` or omitted only if the query plane cannot know yet. |
| `hasMore` | More rows exist or may exist beyond the returned rows. |
| `truncated` | Returned rows are capped by caller/server limits. |
| `limit` | Effective row limit used for the response. |
| `resultMode` | Echo of `paged` or `full`. |

Recommended query-plane implementation direction:

- Add a reusable service/API path that can either return a page from `QueryResultExecutionService`
  or accumulate pages into a bounded full result.
- Keep existing `/api/v1/query` paging semantics intact; add the `full` semantic as an extension or
  helper, not as a replacement.
- The accumulation logic belongs near `QueryResultExecutionService` or a query-result facade so
  `sql-query.execute_sql`, chart snapshot loading, and future clients do not duplicate page walking.

**Owner:** **WI-338** limits contract; **WI-340** configuration; **WI-370** UI snapshot integration.

---

### 8. SQL-query MCP proof strategy and fixtures - **LOCKED**

**Gap:** WI-341 lists MCP tests but does not lock the fixture strategy or how to avoid flaky backend
dependencies.

**Locked decision:**

- Use the standard Skymill integration fixture for HTTP MCP `testIT`, matching existing
  query-service and data-plane integration test practice.
- Core MCP executor tests should mock `SqlQueryExecutionPort` so they prove capability contracts,
  profile exposure, and error mapping without requiring a backend.
- HTTP MCP `testIT` should use the real Skymill-backed query plane to prove end-to-end tool
  invocation through the MCP transport.
- Do not assert exact row counts, exact concrete row values, or data population-dependent totals.
  Skymill demo data may change.
- Assertions should focus on stable contracts:
  - tool inventory includes `sql-query.validate_sql`, `sql-query.describe_sql`, and
    `sql-query.execute_sql`
  - `schema-exploration` excludes `sql-query.*`
  - `data-analysis` includes `sql-query` at profile/unit level
  - `describe_sql` returns `artifactType`, `sql`, `dialectId`, `schema`, `warnings`, and `source`
  - `describe_sql` does not return `rows`
  - `execute_sql` returns the same base fields plus `resultMode`, `rows`, `rowCount`, `truncated`,
    `hasMore`, and `limit`
  - `schema[]` entries contain stable structural fields such as `name`, Mill logical `type`, and
    `nullable`
  - row payload shape is an array of objects when rows are returned, without asserting exact values
- For row-returning tests, use small bounded queries and structural assertions only. It is acceptable
  for the returned row array to be empty if the selected Skymill fixture query is valid but the data
  population changes; prefer fixture queries expected to have rows, but do not make exact values part
  of the test contract.
- Test `schema-exploration` exclusion at catalog listing. Invocation rejection can be covered if the
  MCP executor has an existing clean pattern for unauthorized tool calls; otherwise catalog exclusion
  is the required proof for WI-341.

**Owner:** **WI-341**.

---

### 8a. `execute_sql` default `resultMode` - **LOCKED**

**Gap:** WI-338 lists `resultMode` (`paged` or `full`) on `execute_sql` input but leaves the default
**TBD**. Callers, MCP schema examples, and autoconfigure defaults need one canonical value.

**Locked decision:**

- Default `execute_sql.resultMode` is **`paged`**.
- Callers that need a bounded full snapshot (chart rendering, compact tool answers) must pass
  `resultMode: "full"` explicitly.
- Manifest JSON schema, design docs, and autoconfigure defaults must use `paged` when the input is
  omitted.

**Owner:** **WI-338**.

---

### 8b. `source.kind` vocabulary for execution tools and artifacts - **LOCKED**

**Gap:** Story-local docs disagreed on `source.kind` values for execution-backed schema probe and row
return (`execution-probe`, `execution`, `execute`).

**Locked decision:**

- Canonical `source.kind` is **`execution`** across `describe_sql`, `execute_sql`, and related
  `sql-description` / `sql-result` artifact envelopes.
- `describe_sql` may add probe context under `source`, e.g. `{ "kind": "execution", "maxRows": 1 }`.
- `execute_sql` uses `{ "kind": "execution" }` unless additional execution metadata is needed
  (limits, truncation) at the top level.
- Do not introduce alternate kind values such as `execution-probe` or `execute` in normative
  contracts or examples.

**Owner:** **WI-338**.

---

### 8c. `nativeType` in chart-facing schema output (MVP) - **LOCKED**

**Gap:** Tool contracts listed optional `nativeType` on `schema[]` entries, but
[`QueryResultColumnSchema.kt`](../../../../data/mill-data-query/src/main/kotlin/io/qpointz/mill/data/query/engine/QueryResultColumnSchema.kt)
does not emit it today.

**Locked decision:**

- The Spring/query-result **adapter may map `nativeType`** from backend or JDBC metadata when readily
  available.
- `nativeType` remains **fully optional** — omit the field when unknown or unmapped.
- Validators, chart mapping, MCP tests, and UI must **not** require `nativeType`; Mill logical
  `type` is the chart-facing contract.
- Contract examples should not treat `nativeType` as normative; it is unlikely to be used in MVP
  flows and may be dropped from examples entirely.
- WI-339/WI-340 should not block on native-type mapping; implement only when cheap from existing
  metadata.

**Owner:** **WI-338** (contract); **WI-339** / **WI-340** (adapter mapping when available).

---

### 8d. `v3-mcp-capability-exposure.md` §2 SQL execution rule - **LOCKED**

**Gap:** [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) §2
still states that **`sql-query` exposes `validate_sql` only** and **SQL execution remains
host-side**. `sql-query.execute_sql` introduces bounded SQL execution over MCP and in-process agent
tools, so the architectural rules table must be updated without implying unbounded or
validation-bypassing execution.

**Locked decision:** Amend §2 (and §15 tool inventory intro if needed) to state:

- `sql-query` owns SQL generation/validation, generated SQL artifacts, and the related bounded
  execution/schema-probe tools.
- `sql-query.describe_sql` and `sql-query.execute_sql` operate on **already validated** SQL input;
  they do not generate, rewrite, or semantically validate SQL.
- No separate `data-query` capability is introduced. The implementation still reuses the existing
  query-result execution plane behind the `sql-query` tool boundary.

**Owner:** **WI-338**.

---

## Stage 2 - Chart contract foundation (WI-366)

### 9. Result schema input to chart-mapping - **LOCKED**

**Gap:** STORY and WI-368 assume chart mapping consumes **existing generated SQL + known result schema**, but **no WI defines how the runtime/model obtains that schema**.

Open questions:

| Question | Notes |
|----------|-------|
| Tool input shape | Does `validate_chart_spec` require a `schema[]` argument from runtime orchestration, or does the handler read **`last-sql-result`** / execution metadata from pointers? |
| Pre-execution chart requests | User says "show last result as pie chart" **before** Run — schema may exist only on a prior turn's `data` artefact (`columns` on wire) or not at all |
| Schema without execution | Can mapping proceed from **inferred** columns (model guess) vs **must** fail until execution? STORY implies schema-bound validation — source of truth unclear |
| Column types | Mill query columns use string types; chart catalog expects `number`, `string`, `date` — **normalization rules** not specified |

**Decision needed:** Normative contract for schema provenance (pointer lookup tool vs mandatory tool args vs hybrid).

**Locked decision:** Chart schema must come from `sql-query.describe_sql`, which may actually
run or server-side probe the validated SQL after reasoning/SQL validation and before chart
configuration. The model must not infer the result schema from SQL text alone.

Required orchestration:

```text
reasoning / SQL selection
  -> sql-query validates SQL and emits generated-sql
  -> runtime calls sql-query.describe_sql for result schema and Mill logical types
  -> chart-mapping receives generated-sql + runtime schema metadata
  -> chart-mapping validates encodings and emits generated-chart
```

The execution/probe step belongs to runtime orchestration through the `sql-query` capability, not to
the chart-mapping capability. Chart-mapping still does not generate, rewrite, validate, or execute
SQL. If the runtime cannot obtain trustworthy schema metadata, chart-mapping must return a structured
diagnostic instead of a `generated-chart` artifact.

The canonical design doc for this decision is
[`docs/design/agentic/chart-artifact-contract.md`](../../../design/agentic/chart-artifact-contract.md)
§4.

**Owner:** **WI-338** (tool contract); **WI-339** (capability tools); **WI-340** (runtime/profile
access); **WI-366** (chart contract); **WI-368** (chart validator use).

---

### 10. Canonical design doc path - **LOCKED**

**Gap:** WI-366 item 10 says update `docs/design/ai/` or `docs/design/agentic/` but does **not** name a single canonical file (contrast: metadata-authoring hub in `metadata-facet-catalog-v3.md`).

| Option | Approach |
|--------|----------|
| **A** | New `docs/design/agentic/chart-artifact-contract.md` (recommended — mirrors artifact-foundation cross-links) |
| **B** | Extend [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) with a chart section |
| **C** | Extend [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) only |

**Decision needed:** Pick one hub + which secondary docs get cross-links (`chat-artefact-architecture`, `v3-mcp-capability-exposure`, optional `docs/public/src/` operator guide at story close).

**Locked decision:** Use [`docs/design/agentic/chart-artifact-contract.md`](../../../design/agentic/chart-artifact-contract.md)
as the canonical design hub. `artifact-foundation.md` and `docs/design/agentic/README.md` link to
it. Do not move the chart contract to `docs/design/ai/`; chat-specific rendering details can be
cross-linked from `chat-artefact-architecture.md` when WI-370 lands.

**Owner:** **WI-366** completes the normative schema in the locked hub; later WIs update surface
docs only when their implementation details land.

---

### 11. Schema shape inconsistencies (STORY vs WI-366 vs catalog) - **LOCKED**

**Gap:** Examples and WIs disagreed on field placement and encoding vocabulary.

**Locked decision:** Chart configuration uses **chart-type-specific, industry-standard semantic roles**
that are valid for that chart family. Roles are **renderer-agnostic** (not ECharts/Vega/Plotly config)
but follow familiar analytics terminology so authors, validators, and UI compilers share one vocabulary
per chart type.

| Chart type | Required encodings | Optional encodings | Chart-type options (in `options`) |
|------------|-------------------|--------------------|-----------------------------------|
| `bar` | `category`, `value` | `series` | `orientation` (`vertical` \| `horizontal`), `stacked` |
| `line` | `x`, `y` | `series` | — (MVP) |
| `area` | `x`, `y` | `series` | `stacked` |
| `scatter` | `x`, `y` | `series`, `color` | — (MVP) |
| `pie` | `category`, `value` | — | — (MVP) |

Rules:

- **Role names are chart-type-specific.** Validators must reject encoding roles that are not declared
  for the selected `chartType` in the chart catalog (e.g. `x`/`y` on `bar`; `category`/`value` on
  `scatter`).
- **Cartesian charts** (`line`, `area`, `scatter`) use **`x` and `y`** — the usual axis-dimension
  vocabulary for plots with continuous or ordered horizontal and vertical measures.
- **Categorical comparison / part-to-whole charts** (`bar`, `pie`) use **`category` and `value`** —
  the usual dimension-and-measure vocabulary for comparing categories or slice sizes.
- **`orientation` is not a top-level artifact field.** Bar layout options live under **`options`**, mirroring
  the chart catalog (`options.orientation`, `options.stacked`). The emitted artifact copies normalized
  options from validation output; do not duplicate options at the payload root.
- **MVP `chartType` set:** `bar`, `line`, `pie`, `scatter`, `area` — all five are in scope for WI-366
  contract, WI-368 catalog/validator, and WI-370 renderer support.
- **`encodings` payload:** include only roles valid for `chartType`; omit unused roles (null placeholders
  are not required).
- **`columns`:** snapshot of the trusted result schema from `sql-query.describe_sql` using Mill logical
  type names (`STRING`, `BIG_INT`, …); every `encodings.*.field` must reference a `columns[].name`.
- **`presentation.sort`:** optional array, **at most 3** entries; each entry is
  `{ "field": "<column name>", "direction": "asc" | "desc" }`; `field` must exist in `columns` (need
  not be an encoded field, but typically sorts by a measure or category column).

Normative per-chart definitions, encoding field constraints, catalog alignment, and examples live in
[`chart-artifact-contract.md`](../../../design/agentic/chart-artifact-contract.md) §5–§9.

Per-chart **implementation specs** with ECharts compilation mapping (draft for review):
[`docs/design/agentic/charts/`](../../../design/agentic/charts/README.md).

**Owner:** **WI-366** (document); **WI-368** (catalog + validator); **WI-370** (compiler role mapping).

---

### 12. Query-refinement and validation failure contract - **LOCKED**

**Gap:** STORY and WI-368 require a diagnostic when result shape is unsuitable, but the wire shape and
UI behavior were undefined.

**Locked decision:**

- Chart validation uses **`validate_chart_spec`** with the same pattern as **`validate_sql`**:
  structured tool result with `passed`, `code`, `message`, optional `normalizedChart` on success.
- **`emitsOnSuccess` → `generated-chart` only when `passed === true`.**
- **No failed chart artifact in UI** — on any validation failure, the user sees **assistant text
  only** (model explains from tool `message`). No empty chart card, no `failed-chart` wire part, no
  persisted failure artifact in chat history.
- `chart-validation` wrapper is **`persist: false`** (tool-loop metadata, like `sql-validation`).

**Why validation does not pass** — see failure taxonomy in
[`chart-artifact-contract.md`](../../../design/agentic/chart-artifact-contract.md) §10.4:

| Category | Examples |
|----------|----------|
| **A. Prerequisites** | No SQL yet, `describe_sql` failed, empty schema |
| **B. Spec mistakes** | Wrong encoding roles, unknown fields, renderer config in payload |
| **C. Result shape / SQL grain** | `query_refinement_needed` — need GROUP BY, extra column, aggregation; chart-mapping does not rewrite SQL |
| **D. Catalog constraints** | Too many pie slices, non-positive values |
| **E. Loop exhausted** | Three failed validation attempts |

**Owner:** **WI-366** (contract §10); **WI-368** (validator + prompts); **WI-369** (scenario baselines).

---

### 13. Forward-compatible families - **LOCKED (verified)**

**Gap:** STORY requires WI-366 to verify treemap/sunburst/histogram representability without
implementing them.

**Verification:** The MVP `generated-chart` shape is **sufficient** — no artifact schema change
required. Reserved roles (`hierarchy` as ordered field array, `bin`, `size`) plus existing `x`/`y`/
`value` cover all planned future families.

| Family | Roles | ECharts path (future) |
|--------|-------|----------------------|
| treemap / sunburst | `hierarchy[]` + `value` | `series.type: treemap` / `sunburst`; compiler builds tree from flat rows |
| histogram | `bin` + `value` + `options.binCount` | `series.type: bar` (semantic `histogram` distinct) |
| heatmap | `x` + `y` + `value` | `series.type: heatmap` |
| bubble | `x` + `y` + `size` (+ `series`) | `scatter` + `symbolSize` from `size` |

Non-normative appendix with worked examples:
[`forward-compatible-families.md`](../../../design/agentic/charts/forward-compatible-families.md).

Until catalog + renderer exist: `validate_chart_spec` → `unsupported_chart_type` (text reply only).

**Owner:** **WI-366** (documented); future WIs for implementation.

---

### 14. Multi-chart turns - **LOCKED**

**Gap:** Story did not define multiple chart artefacts per turn (e.g. "bar and pie of the same data").

**Locked decision:**

| Scenario | Artifacts |
|----------|-----------|
| **One query, multiple chart views** | **One** `generated-chart` artifact with shared `sql` + `columns` and **`charts[]`** (1–5 configs, each with `chartKey`, `chartType`, encodings, …) |
| **Multiple queries, each with chart(s)** | **One chart artifact per query** (multi-artifact turn); each artifact has its own `sql`, `columns`, and `charts[]` |

Rules:

- Chart artifact = **single query, single result schema, one data snapshot at Run time**.
- Never merge two different SQL statements into one chart artifact.
- Canonical shape uses **`charts[]`**; legacy flat single-chart root fields are ingest-only normalization.
- **`last-chart`** pointer = last chart **artifact** (whole bundle), singular — not per `chartKey`.
- Max **5** configs per artifact; max **5** chart artifacts per turn (MVP).

Full spec: [`multi-chart-artifact-model.md`](../../../design/agentic/charts/multi-chart-artifact-model.md).

**Owner:** **WI-366** (contract); **WI-368** (validator merge); **WI-369** (multi-artifact scenarios); **WI-370** (UI sub-tabs for `charts.length > 1`).

---

## Stage 3 - Generated SQL context (WI-367)

### 15. WI-367 title/description production - **LOCKED**

**Gap:** Generated SQL artifacts have no durable human-readable headline; chart decoration and SQL
cards cannot show meaningful context.

**Locked decision (production path A):**

- **`title` and `description` are first-class mandatory fields on `validate_sql` input** — supplied
  by the LLM on every validation call.
- Handler runs **input hygiene first**; missing/blank/out-of-range values → `passed: false`,
  `code: missing_context_fields` **before** SQL semantic validation.
- Successful tool results **echo** `title` and `description`; `ArtifactEmissionCoordinator` copies
  them into emitted `generated-sql` (`OnToolSuccess`).
- **`sql-query.generated-sql` protocol** lists both fields as **required** on new emissions.
- **No deterministic server fallback** for new artifacts (no table-name heuristics or utterance
  parsing). The model must provide the pair; rejection forces retry.
- **`sql-query.system`** instructs the model to pass `title` + `description` on every `validate_sql`
  call and to refresh them on SQL refinement.
- **Legacy replay only:** pre-WI-367 artifacts without fields remain parseable; UI uses replay
  fallback (`"Generated query"` + truncated SQL line) — not used for new LLM calls.

Field bounds: `title` 3–120 chars; `description` 10–500 chars (non-blank after trim).

Normative spec: [`generated-sql-artifact-context.md`](../../../design/agentic/generated-sql-artifact-context.md).

**Owner:** **WI-367**.

---

### 16. Wire preservation (SQL title/description) - **LOCKED**

**Gap:** Persisted `generated-sql` will carry mandatory `title` and `description` (Gap 15), but GET/SSE
replay and mill-ui today drop them.

**Current state (implementation debt, not policy):**

- [`ArtifactWireMapper.mapSql`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt) emits only `sql` + optional `dialectId`
- [`parseSqlArtifact`](../../../../ui/mill-ui/src/utils/artifactWireParse.ts) ignores `title` / `description`
- [`SqlArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/SqlArtifactCard.tsx) hard-codes **“Generated SQL”**

**Locked decision (follows from Gap 15 mandatory fields):**

| Layer | Rule |
|-------|------|
| **Persistence** | Store full `generated-sql` payload including `title` and `description` (no change to policy — already persisted in inner payload; wire layer was the leak). |
| **`ArtifactWireMapper.mapSql`** | **Must pass through** `title` and `description` when present on inner payload. |
| **Live SSE structured parts** | Same fields on SQL artifact parts when emitted (WI-367 implementation). |
| **`parseSqlArtifact` / `ChatMessageArtifact`** | Extend `kind: 'sql'` with optional `title?`, `description?` for replay compat. |
| **SQL card UI** | **Primary heading = `title`**; **`description`** as subtitle/body; dialect remains secondary metadata; SQL code block unchanged. |
| **New emissions after WI-367** | Both fields always present — UI always shows them (not optional in practice). |
| **Legacy replay only** | When fields **absent** on old rows: title = **`"Generated query"`**; optional one-line subtitle = first line of SQL trimmed to **80** chars (no description block). |

**Chart artifact wire** (full `charts[]`, encodings, etc.) remains **WI-369** — same pass-through
principle, separate mapper branch when `generated-chart` ships.

Normative SQL wire/UI rules: [`generated-sql-artifact-context.md`](../../../design/agentic/generated-sql-artifact-context.md) §10.

**Owner:** **WI-367** (SQL wire + UI); **WI-369** (chart wire).

---

## Stage 4 - Chart capability and routing (WI-368)

### 17. "Last result" and active pointer semantics - **LOCKED**

**Gap:** Chart follow-ups ("show last result as pie chart", "make this a bar chart") need a defined
way to resolve which SQL/chart context to use.

**Locked decision:** Same approach as **`sql-query` last-query refinement today** — **prompt rules +
conversation / turn artifact context**. **No** new pointer-read tool for MVP (no `get_query_context`
/ Phase 2).

| Topic | Decision |
|-------|------------|
| LLM resolution | Model reads prior **`generated-sql`** / **`generated-chart`** artifacts in thread; `chart-mapping.system` mirrors sql-query follow-up phrasing |
| Tool handlers | **`validate_chart_spec` receives explicit `sql`** copied by the model; handlers do **not** read `ActiveArtifactPointerStore` |
| Schema | **`sql-query.describe_sql`** after validated SQL (Gap 9) |
| **`last-sql` / `last-chart` pointers** | Server-side upsert on persist (WI-369); tests/replay; **not** exposed to LLM as tools in MVP |
| **`last-chart` vs `last-sql`** | **Complement** — `last-sql` = latest SQL; `last-chart` = latest chart bundle for "make this a …" |
| **Multi-SQL same turn** | Model disambiguates using mandatory **`title` / `description`** on each `generated-sql` (Gap 15) plus user wording; ask in prose if still unclear — no chart emission |
| **No SQL in context** | Route to **`sql-query`** first |

Normative spec: [`chart-context-resolution.md`](../../../design/agentic/charts/chart-context-resolution.md).

**Owner:** **WI-368** (prompts); **WI-369** (`last-chart` pointer + scenarios).

---

### 18. `data-analysis` profile intent - chart routing missing - **LOCKED**

**Gap:** Chart requests need routing on **`data-analysis`** without overloading **`sql-query`**.

**Locked decision (WI-363 pattern):**

| Layer | Decision |
|-------|----------|
| **`chart-mapping.intent`** | Capability-local — **`CHART_MAP`** vs **`CHAT`**; all chart classification logic lives in **`chart-mapping.yaml`** |
| **`sql-query.intent`** | **`DATA_QUERY` only** — no chart / visualize intent on sql-query |
| **`data-analysis` profile** | Add **`chart-mapping`** to `capabilities:`; extend **`data-analysis.intent`** with **one composition bullet** referencing `chart-mapping.intent` + mixed-turn rules (`DATA_QUERY` + `CHART_MAP` for e.g. "plot revenue by month") — **no duplicated chart routing prose in the profile** |
| Mixed turns | Decompose like facet + SQL: SQL subtask first when needed, then chart validation |

Normative spec: [`chart-routing-intent.md`](../../../design/agentic/charts/chart-routing-intent.md).

**Owner:** **WI-368**; cross-link **WI-366**.

---

### 19. Emission path: tool-only vs protocol final - **LOCKED**

**Gap:** Whether chart artifacts emit via validator auto-emit or model-invoked protocol final.

**Locked decision (follows Gap 12 + Gap 14):**

| Path | MVP |
|------|-----|
| **`validate_chart_spec` + `emitsOnSuccess`** when `passed === true` | **Yes — only emission path** |
| **`emissionStrategy: OnToolSuccess`** on `generated-chart` descriptor | **Yes** — coordinator builds `ProtocolFinal` from tool result (same as `generated-sql`) |
| Model invokes **`chart-mapping.generated-chart`** protocol | **Forbidden** in prompts |
| **`chart-mapping.generated-chart` protocol in YAML** | **Yes** — schema/coordinator/docs only (not model-called) |

**Why:** Gap 14 allows **multiple chart artifacts per turn** (one per query). Runtime
`ArtifactEmissionCoordinator.emitOnToolSuccess` emits **one final per successful
`validate_chart_spec`** in the iteration. Direct model protocol final is **single-shot per
invocation**; without a batch chart protocol, it would **lose all but the last chart** on multi-query
turns. Same query + multiple views uses **one** validator call with **`charts[]`** → one artifact.

Normative spec: [`chart-emission-path.md`](../../../design/agentic/charts/chart-emission-path.md)
(also [`chart-artifact-contract.md`](../../../design/agentic/chart-artifact-contract.md) §10.3).

**Owner:** **WI-368** (YAML + prompts); **WI-369** (descriptor + multi-artifact scenarios).

---

### 20. MCP tool inventory - **LOCKED**

**Gap:** MCP docs and exposure must support **external agents** reusing renderer-independent chart
specs without mill-ui.

**Locked decision:**

| Topic | Decision |
|-------|----------|
| **Principle** | Renderer-independent semantic spec → MCP exposes **catalog + query + schema/data + chart validation** with **sufficient structured tool outputs** for external render |
| **Profile** | **`data-analysis`** — includes `chart-mapping` + `sql-query` (+ schema, sql-dialect, …) |
| **Chart tools** | `chart-mapping.list_supported_charts`, `chart-mapping.validate_chart_spec` (auto-exposed from YAML when capability ships) |
| **SQL / data tools** | Existing `sql-query.validate_sql`, `describe_sql`, `execute_sql` (Stage 1) |
| **External minimum** | `normalizedChart` + optional `execute_sql` rows + `describe_sql` schema + catalog — no server ECharts |
| **Docs** | Update [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) §15 + [`chart-mcp-exposure.md`](../../../design/agentic/charts/chart-mcp-exposure.md) |
| **Tests** | `CapabilityMcpCatalogTest` + HTTP MCP `testIT` (mirror WI-341) — **WI-368** |

Normative spec: [`chart-mcp-exposure.md`](../../../design/agentic/charts/chart-mcp-exposure.md).

**Owner:** **WI-368** (inventory, catalog tests, example pointer).

---

## Stage 5 - Runtime, wire, and scenario proof (WI-369)

### 21. Scenario / test proof strategy - **LOCKED**

**Gap:** How to prove chart mapping/wiring without flaky live LLM; whether live conversations can seed
scripted scenario packs.

**Locked decision (WI-351 Option D):**

| Topic | Decision |
|-------|----------|
| **CI proof** | **Layered mock-LLM + per-layer unit tests** — real handlers, scripted model; **no live LLM in CI** |
| **Scope** | Mapping, emit, persist, pointers, SSE, GET wire — automated; chart **quality/UX** — **manual** |
| **Harness** | Extend **`ArtifactEmitScenariosIT`** with scripted chart packs + baselines (mirror SQL/facet emit POC) |
| **Live → scripted** | **`GET /api/v1/ai/chats/{id}/scenario-export`** with **`mill.ai.chat.scenario-capture.enabled=true`**; operator adds `verify:` manually |
| **Export gap** | Run-event path already captures `validate_chart_spec`; **`buildScriptFromArtifacts` fallback lacks chart** — fix in **WI-369** |
| **verifyHints** | Generic by `persistKind` — **`chart.generated` already exported in hints** |

Normative spec: [`chart-test-proof-strategy.md`](../../../design/agentic/charts/chart-test-proof-strategy.md) (L1–L6 table).

**Owner:** **WI-369** (L2–L5, packs, exporter); **WI-368** (L1); **WI-370** (L6 Vitest).

---

## Stage 6 - mill-ui rendering and execution UX (WI-370)

### 22. UI grouping and artefact kind model - **LOCKED**

**Gap:** Chart artefacts are **self-contained** (embed `sql`, encodings, …) but must render in the same
card family as SQL/data. Current UI model pairs `sql` + `data` only.

**Locked decision (2026-07-02):** **Extend `sql-data-composite`** — do **not** introduce a separate
`ArtefactKind` or expand-registry entry for chart.

| Topic | Decision |
|-------|----------|
| Group type | Add optional `chart?` to [`SqlDataCompositeGroup`](../../../../ui/mill-ui/src/components/chat/artifactPreview/types.ts) |
| `ArtefactKind` | Unchanged: `sql-data-composite` \| `facet-proposal` |
| Tabs | When `chart` present: **Chart \| Data \| SQL**; else **Data \| SQL** (today) |
| Chart-only turn | One composite with `chart` only; SQL/Data tabs use embedded chart payload fields |
| Merge rule | Attach `chart` to composite by lineage or normalized SQL match (same as data pairing) |
| Expand | Reuse `SqlDataExpandedView` / `sql-data-composite` expand payload — no `chart` expand kind |
| Shared renderer | `components/charts/ChartRenderer` stays chat-agnostic; shell stays in composite previews |

Rejected for this story: standalone **`chart`** group (Option A), separate type name
`sql-data-chart-composite` (Option B naming only), duplicate SQL card + chart card (Option C).

Normative spec: [`chart-ui-composite.md`](../../../design/agentic/charts/chart-ui-composite.md).

**Owner:** **WI-370**.

---

### 23. Chart data snapshot vs query pagination - **LOCKED**

**Gap:** Chart rendering needs a **bounded full snapshot**; chat SQL Run today uses one paged grid
page via [`readStoredQueryPageSize()`](../../../../ui/mill-ui/src/services/queryService.ts).

**Locked decision (2026-07-02):**

| Topic | Decision |
|-------|----------|
| Chart fetch | `queryService.executeQuery` gains `resultMode: 'full'` + `maxRows` (Gap 7) |
| UI helper | `fetchChartSnapshot(sql, maxRows)` wraps full mode — chart components do not page manually |
| Data tab | Stays `resultMode: 'paged'` with user page size |
| Limits | `maxRows` from chart catalog `hardLimit` when known, else conservative UI default |
| Truncated full result | Chart tab errors / refinement prompt — never silent partial render |
| Reuse `data` artefact | Only when same SQL, sufficient row count, within limit, and not truncated |
| Implementation | Shared accumulation (same semantics as [`QueryResultSqlQueryExecutionPort.executeFull`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/sqlquery/QueryResultSqlQueryExecutionPort.kt)); may live in `queryService.ts` first, optional later HTTP `POST /api/v1/query` `resultMode` extension |

Normative spec: [`chart-snapshot-fetch.md`](../../../design/agentic/charts/chart-snapshot-fetch.md).

**Owner:** **WI-370**; catalog limits from **WI-368** / **WI-366**.

---

### 24. `assistantReplyView` and layout routing - **LOCKED**

**Gap:** Chart turns need a layout hint; [`AssistantReplyView`](../../../../ui/mill-ui/src/types/chat.ts)
had no `chart-primary`.

**Locked decision (2026-07-02):** Add **`chart-primary`**. Reply **chrome is the same as
`sql-primary`** (section label + artefact stack + optional prose) — no new layout fork.

| Topic | Decision |
|-------|----------|
| Enum | Add `chart-primary` to `AssistantReplyView` + `assistantReplyViewFromWire` |
| Chrome | Reuse `StructuredReplyLayout` / `MessageArtifactComposer` paths — identical to SQL |
| Section title | `"Chart"` via `structuredReplySectionTitle` |
| Derivation | After `facet-primary`, before `sql-primary`: chart artefact → `chart-primary` |
| Mixed SQL + chart | `chart-primary` (composite card per Gap 22) |
| SSE hint | `item.completed` `partType: 'chart'` → `chart-primary` |
| GET replay | Client derives from wire `kind: 'chart'`; server field still optional |

Rejected: reuse `artifact-primary` for chart; derive layout only from groups without view enum;
separate chart layout chrome.

Normative spec: [`chart-reply-view.md`](../../../design/agentic/charts/chart-reply-view.md).

**Owner:** **WI-370**.

---

### 25. Chat Run all semantics for charts - **LOCKED**

**Gap:** Run all only runs SQL via [`collectChatSqlTargets`](../../../../ui/mill-ui/src/services/chatSqlExecution.ts).

**Locked decision (2026-07-02):**

| Topic | Decision |
|-------|----------|
| Collect | `collectChatChartTargets` — `kind: 'chart'` artefacts |
| Order | **Same traversal as SQL** — bottom-up messages, reverse within turn; no topological sort |
| Execution | **Two passes:** (1) existing SQL loop, (2) chart snapshot + render per chart target |
| Idempotency | **Not a separate layer** — pass 2 reuses matching `data` from pass 1 via Gap 23 snapshot reuse; collector `seen` set only (same as SQL) |
| Re-run | May re-execute queries on repeat Run all — acceptable, matches SQL behaviour today |

Rejected: lineage topological sort; global SQL dedupe map; interleaved SQL/chart per message in one pass.

Normative spec: [`chart-run-all.md`](../../../design/agentic/charts/chart-run-all.md).

**Owner:** **WI-370**.

---

### 26. Inline chat treatments - **LOCKED (deferred)**

**Gap:** Whether `inline-analysis` / `inline-model` / `inline-knowledge` render chart artefacts.

**Locked decision (2026-07-02):** **Out of scope for WI-370.** Inline hosts need a different UX
(less space, special treatment) — deferred to a follow-up.

| Topic | WI-370 behaviour |
|-------|------------------|
| Scope | Chart preview / Run / expand / Run-all chart pass → **`general` chat only** |
| Inline composite with chart | Existing inline `sql-data-composite` treatment — **Data \| SQL only**, no Chart tab |
| Chart-only turn in inline | `prose-only` fallback (no chart card) |
| `chatArtifactTreatments` | No new inline rows for chart in WI-370 |
| Future | Compact inline chart UX — separate story/WI |

Normative spec: [`chart-inline-deferred.md`](../../../design/agentic/charts/chart-inline-deferred.md).

**Owner:** **WI-370** (interim gate); follow-up WI for inline chart UX.

---

### 27. Legacy / reuse references - **LOCKED**

**Gap:** Whether to port [`mill-grinder-ui/ChartView.tsx`](../../../../ui/mill-grinder-ui/src/component/data/ChartView.tsx)
or build greenfield `components/charts/`.

**Locked decision (2026-07-02):** **Strictly greenfield.** Implement from scratch under
`ui/mill-ui/src/components/charts/`. Legacy grinder chart code is **lightly inspirational only** —
planned mill-ui architecture is conceptually different (semantic spec → compiler → `ChartRenderer`).

| Topic | Decision |
|-------|----------|
| Implementation | New modules per STORY layout — no port from mill-grinder-ui |
| Naming | `ChartRenderer` + compiler — **not** `ChartView.tsx` in mill-ui |
| Legacy | Optional comment-level inspiration; no package dependency |
| Compiler | Driven by [`echarts-compiler-contract.md`](./echarts-compiler-contract.md) + per-type specs |

Rejected: port/wrap grinder `ChartView`; grinder option builders; parity tests against legacy.

Normative spec: [`chart-greenfield-implementation.md`](../../../design/agentic/charts/chart-greenfield-implementation.md).

**Owner:** **WI-370**.

---

## Stage 7 - Story process and closure

### 28. Story process and tracking gaps - **LOCKED**

**Gap:** Process for BACKLOG, MILESTONE, docs, and feature flags at story end.

**Locked decision (2026-07-02):**

| Topic | Decision |
|-------|----------|
| **`BACKLOG.md`** | Update at **story closure** — set related rows **`done`**; prune at release only |
| **`MILESTONE.md`** | **Not at story closure** — update only when drafting a **new version / release** |
| **Design docs** | **Review and update on current branch** as WIs ship; final pass at closure |
| **Public docs** (`docs/public/src/`) | **Same** — reflect chart capability, artefacts, mill-ui behaviour before MR/closure |
| **Feature flag** | **Not needed** — profile + artefact presence drive exposure |
| **Branch name** | Reset branch is `restart/ai-chart-mapping-after-stage1`. |

Normative checklist: [`chart-story-closure.md`](chart-story-closure.md).

**Owner:** Story owner at **explicit closure** (BACKLOG/archive); ongoing doc hygiene during WI-370.

---

## Summary - decisions for product / tech review

| # | Topic | Status |
|---|--------|--------|
| 0 | Implementation | **None started** - docs-only on branch |
| 1 | `sql-query` tool split and output contracts | **LOCKED** |
| 2 | Schema probe execution strategy | **LOCKED** |
| 3 | Validated SQL input boundary | **LOCKED** |
| 4 | Type normalization for chart validation | **LOCKED** |
| 5 | SQL-query execution dependency port and backend result shape | **LOCKED** |
| 6 | Profile and runtime access for `describe_sql` | **LOCKED** |
| 7 | SQL-query execution limits, truncation, and snapshot relationship | **LOCKED** |
| 8 | SQL-query MCP proof strategy and fixtures | **LOCKED** |
| 8a | `execute_sql` default `resultMode` | **LOCKED** (`paged`) |
| 8b | `source.kind` vocabulary for execution tools and artifacts | **LOCKED** (`execution`) |
| 8c | `nativeType` in chart-facing schema output (MVP) | **LOCKED** (adapter maps when available; optional) |
| 8d | `v3-mcp-capability-exposure.md` §2 SQL execution rule | **LOCKED** |
| 9 | Result schema input to chart-mapping | **LOCKED** |
| 10 | Canonical design doc path | **LOCKED** |
| 11 | Schema shape inconsistencies (STORY vs WI-366 vs catalog) | **LOCKED** |
| 12 | Query-refinement and validation failure contract | **LOCKED** |
| 13 | Forward-compatible families | **LOCKED (verified)** |
| 14 | Multi-chart turns | **LOCKED** |
| 15 | WI-367 title/description production | **LOCKED** |
| 16 | Wire preservation (SQL title/description) | **LOCKED** |
| 17 | "Last result" and active pointer semantics | **LOCKED** |
| 18 | `data-analysis` profile intent - chart routing missing | **LOCKED** |
| 19 | Emission path: tool-only vs protocol final | **LOCKED** |
| 20 | MCP tool inventory | **LOCKED** |
| 21 | Scenario / test proof strategy | **LOCKED** |
| 22 | UI grouping and artefact kind model | **LOCKED** (extend `sql-data-composite`) |
| 23 | Chart data snapshot vs query pagination | **LOCKED** (`queryService` full mode) |
| 24 | `assistantReplyView` and layout routing | **LOCKED** (`chart-primary`, SQL-equivalent chrome) |
| 25 | Chat Run all semantics for charts | **LOCKED** (collect chart; document order; two passes) |
| 26 | Inline chat treatments | **LOCKED (deferred)** — general chat only in WI-370 |
| 27 | Legacy / reuse references | **LOCKED** (greenfield; grinder inspirational only) |
| 28 | Story process and tracking gaps | **LOCKED** (BACKLOG at closure; MILESTONE at release; docs on branch) |

---

## Recommended Resolution Order

1. **Stage 1 / WI-338-WI-341** - lock design docs (gaps **8a-8d** resolved); implement
   `sql-query.describe_sql` and `sql-query.execute_sql`; Stage 1 gaps **1-8c** are locked.
2. **Stage 2 / WI-366** - complete chart contract JSON schema in the locked design hub; resolve
   **gaps 9-14**.
3. **Stage 3 / WI-367** - add generated SQL title/description and preserve them on wire; resolve
   **gaps 15-16**.
4. **Stage 4 / WI-368** - implement chart capability, catalog, profile routing, and validator;
   resolve **gaps 17-20**.
5. **Stage 5 / WI-369** - wire chart artifacts and scenario proof; resolve **gap 21**.
6. **Stage 6 / WI-370** - implement mill-ui chart rendering, snapshots, Run all; gaps **22-27** locked.
7. **Story closure** - [`chart-story-closure.md`](chart-story-closure.md); gap **28** locked.


---

## Related

- [`STORY.md`](STORY.md) — goal, principles, WI order
- [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) — descriptor / emission patterns
- [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) — mill-ui treatments and replay
- [`completed/20260629-metadata-authoring-profiles/GAPS.md`](../completed/20260629-metadata-authoring-profiles/GAPS.md) — reference format for gap analysis
