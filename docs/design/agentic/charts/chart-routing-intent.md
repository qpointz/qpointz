# Chart routing and intent (Gap 18)

**Status:** Locked design input
**Pattern:** Same as WI-363 — capability-local intents; profile composes a non-overlapping union
**Reference:** [`metadata-facet-catalog-v3.md`](../metadata-facet-catalog-v3.md) § Prompt enforcement

## Summary

| Layer | Responsibility |
|-------|----------------|
| **`chart-mapping.intent`** | Defines **`CHART_MAP`** (visualization / chart config) vs **`CHAT`** — **capability-local** |
| **`sql-query.intent`** | **`DATA_QUERY`** only — query and data retrieval; **no chart intent** |
| **`data-analysis` profile** | Lists **`chart-mapping`** in `capabilities:`; **`data-analysis.intent`** adds one composition bullet + mixed-turn decomposition — **does not duplicate chart classification logic** |

Chart routing logic lives in **`chart-mapping.yaml`**, not in the profile YAML body beyond composition.

---

## Profile changes (`platform-agent-profiles.yaml`)

### Capabilities list

Add **`chart-mapping`** to **`data-analysis`** (alongside existing `sql-query`, `schema`,
`metadata-authoring`, …). Gap 6 already locked **`sql-query`** on this profile for `describe_sql`.

```yaml
capabilities:
  - conversation
  - schema
  - metadata
  - metadata-authoring
  - sql-dialect
  - sql-query
  - chart-mapping    # new
  - value-mapping
```

### Profile intent prompt (composition only)

Extend **`data-analysis.intent`** with a **reference** to `chart-mapping.intent` — same style as
existing bullets for `sql-query`, `schema`, and `metadata-authoring`:

```text
Profile `data-analysis` composes non-overlapping capability intents. Apply in order; one message may match multiple:

1. **sql-query.intent** — DATA_QUERY when the user wants data, SQL, counts, or aggregates.
2. **chart-mapping.intent** — CHART_MAP when the user wants a chart, plot, graph, or visualization.
3. **schema.intent** — EXPLORE when the user wants schema structure only (no SQL generation).
4. **metadata-authoring.intent** — AUTHOR_FACET when the user documents metadata (…).

Mixed utterances may require multiple intents in one turn (e.g. DATA_QUERY + CHART_MAP for
"plot revenue by month"; AUTHOR_FACET + DATA_QUERY for facet + SQL).
Decompose into subtasks; complete every subtask — do not stop after the first structured result
when another remains.

When none of the above apply, treat as CHAT (conversation capability).
```

The profile prompt **does not** define what “chart” means — that is entirely in
**`chart-mapping.intent`**.

---

## Capability-local intent (`chart-mapping.intent`)

Define in **`capabilities/chart-mapping.yaml`**:

| Label | When |
|-------|------|
| **`CHART_MAP`** | User asks to chart, plot, graph, visualize, or change chart type on existing or implied query results ("show as pie", "bar chart", "make this a line chart") |
| **`CHAT`** | General conversation unrelated to visualization |

**Not in scope for `sql-query.intent`:** do not add `CHART_MAP`, `VISUALIZE`, or chart routing to
`sql-query`. SQL capability stays query + validated SQL + optional `describe_sql` / `execute_sql`
for **data** paths.

---

## Mixed SQL + chart same turn

Mirror metadata-authoring mixed-turn rules:

| User example | Intents | Subtask order |
|--------------|---------|---------------|
| "plot revenue by month" | `DATA_QUERY` + `CHART_MAP` | 1. `sql-query` → `validate_sql` → `generated-sql` 2. `describe_sql` 3. `chart-mapping` → `validate_chart_spec` → `generated-chart` |
| "show last result as pie chart" | `CHART_MAP` only (SQL already in thread) | Resolve SQL (Gap 17) → `describe_sql` → chart validation |
| "count orders by status" | `DATA_QUERY` only | SQL path only |
| "document customer_id and show order counts" | `AUTHOR_FACET` + `DATA_QUERY` | Existing facet + SQL decomposition (unchanged) |

When **`CHART_MAP`** applies but no suitable **`generated-sql`** exists, chart-mapping **does not**
generate SQL — route subtask to **`sql-query`** first (text or implicit decomposition via profile
intent).

When chart shape needs different SQL grain, chart-mapping returns **`query_refinement_needed`**;
**`sql-query`** owns refinement (Gap 12 / contract §10).

---

## Prompt boundaries

| Prompt | Chart-related content |
|--------|----------------------|
| **`chart-mapping.intent`** | `CHART_MAP` classification |
| **`chart-mapping.system`** | Workflow, SQL boundary, last-result rules (Gap 17), `describe_sql` before validate |
| **`chart-mapping.spec`** | Semantic chart vocabulary, validator loop |
| **`sql-query.intent`** | **None** |
| **`sql-query.system`** | Query/refinement/validate only; remove or avoid chart-mapping workflow duplication (schema probe for charts belongs in **`chart-mapping.system`**) |
| **`data-analysis.intent`** | Composition bullet + mixed-turn decomposition only |

---

## Non-goals

- Chart intent classes on **`sql-query`** capability
- Full chart routing prose duplicated inside **`data-analysis.intent`**
- Separate **`chart-analysis`** profile for MVP (chart-mapping on **`data-analysis`** only)

---

## WI ownership

| WI | Work |
|----|------|
| **WI-368** | `chart-mapping.yaml` intents/prompts; extend `data-analysis` profile capabilities + composition bullet |
| **WI-366** | Cross-link from design hub |
