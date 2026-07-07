# Chart context resolution (Gap 17)

**Status:** Locked design input
**Related:** [`../generated-sql-artifact-context.md`](../generated-sql-artifact-context.md) (mandatory SQL `title` / `description`)

## Problem

Chart-mapping needs to know **which query** to chart for utterances like "show last result as a pie
chart" or "make this a bar chart". The platform already resolves "last query" for SQL refinement
without a dedicated pointer-read tool.

## Locked decision

Use the **same approach as `sql-query` today**: **prompt rules + conversation / turn artifact
context**. **No** new `get_query_context` / `resolve_last_sql_artifact` tool for MVP (no Phase 2
complication).

| Mechanism | Role |
|-----------|------|
| **LLM + prompts** | Primary — model reads prior turns and artifacts in thread, applies chart-mapping follow-up rules |
| **`last-sql` / `last-chart` pointers** | Server-side only — persist on artifact save; tests, replay, future runtime hooks; **not** exposed as LLM tools in MVP |
| **`validate_chart_spec`** | Always receives **explicit `sql`** (and schema from `describe_sql`) in tool args — model copies from the artifact it resolved |

Chart-mapping must **not** read `ActiveArtifactPointerStore` inside tool handlers for MVP.

---

## Prompt resolution rules (`chart-mapping.system`)

Mirror [`sql-query.system`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml) follow-up vocabulary:

| User phrasing | Resolve to |
|---------------|------------|
| "last query", "last result", "same data", "that query" | Most recent **`generated-sql`** in the conversation (same semantics as **`last-sql`**) |
| "this chart", "make this a bar chart", "change to pie" | Most recent **`generated-chart`** (same semantics as **`last-chart`**) — reuse embedded `sql` |
| Same turn, SQL just emitted | Current turn's **`generated-sql`** (often the latest artifact) |
| No SQL artifact available | Do **not** chart — route to **`sql-query`** first; text reply only |

After resolving SQL:

1. Ensure SQL is validated (`validate_sql` already passed for that artifact, or re-validate if refined).
2. Call **`sql-query.describe_sql`** for schema.
3. Call **`chart-mapping.validate_chart_spec`** with explicit `sql`, encodings, and schema context.

---

## Multiple SQL queries on one turn (Gap 14)

When a turn produced **several** `generated-sql` artifacts, **`last-sql` pointer alone is ambiguous**
(it points at the last upsert only). The model **disambiguates from thread context** using:

- **`title` and `description`** on each generated-sql artifact (mandatory since Gap 15)
- User wording ("the revenue query", "clients by country")
- Artifact order on the turn

If still ambiguous, the model **asks a clarifying question** in prose — do not emit a chart.

Example turn:

```text
generated-sql  title: "Monthly revenue"     …
generated-sql  title: "Clients by country"   …
generated-chart …                            …
User: "show the country one as a pie chart"
  → model selects Clients by country SQL by title, not last-sql upsert blindly
```

---

## Pointer keys (WI-369)

| Pointer | Updated when | Meaning |
|---------|--------------|---------|
| `last-sql` | `generated-sql` persisted | Latest SQL artifact id (server-side) |
| `last-chart` | `generated-chart` persisted | Latest chart artifact id (server-side) |
| `last-sql-result` | data/execution attach | Unchanged; **not** used for chart validation (schema via `describe_sql`) |

`last-chart` **complements** `last-sql`; it does not replace it.

---

## Non-goals (MVP)

- LLM tool to read pointer store
- Runtime auto-injection of SQL into `validate_chart_spec` without model resolution
- Inferring result schema from SQL text or from `last-sql-result` row payloads

---

## WI ownership

| WI | Work |
|----|------|
| **WI-368** | `chart-mapping.system` / `.spec` follow-up rules; tests with multi-SQL titles |
| **WI-369** | `pointerKeys: [last-chart]` on descriptor; scenario "show last result as pie chart" |
