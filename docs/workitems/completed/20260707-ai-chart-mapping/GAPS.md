# Gaps and locked decisions - ai-chart-mapping

**Story:** [`STORY.md`](STORY.md)
**Branch:** `restart/ai-chart-mapping-after-stage1`
**Status:** story closed **2026-07-07** (archived under `docs/workitems/completed/20260707-ai-chart-mapping/`)
**Readiness reviewed:** 2026-07-03

The pre-rewrite analysis is archived at
[`NOTES/GAPS-before-sql-visualizations-rewrite.md`](NOTES/GAPS-before-sql-visualizations-rewrite.md).

**How to read this file**

| Label | Meaning |
|-------|---------|
| **Decision locked** | Story/WI text is enough to implement; no further product input needed. |
| **Delivery pending** | Decision exists but code/docs/tests are not shipped yet. |
| **Blocking** | Must be decided before the listed WI stage starts coding. |

---

## Stage 2+ implementation readiness

Implementation is in progress on branch `restart/ai-chart-mapping-after-stage1`. Shipped WIs are marked in the baseline table below.

| Stage | WI | Shipped? |
|-------|-----|----------|
| 2 | WI-366 | **yes** |
| 2 | WI-367 | **yes** |
| 3 | WI-368 | **yes** |
| 4 | WI-369 | **yes** |
| 5 | WI-370 | **yes** |

| Stage | WI | Spec ready? | Can start now? | Blocking gaps |
|-------|-----|-------------|----------------|---------------|
| 2 | WI-366 | **yes** | **yes** | none |
| 2 | WI-367 | yes | after WI-366 | none |
| 3 | WI-368 | yes | after WI-367 completion coordinator | none |
| 4 | WI-369 | yes | after WI-368 | none |
| 5 | WI-370 | yes | after WI-369 wire shape | none |

**Verdict**

- **Start now:** WI-366 (protocol + completion recipes in docs).
- **Story merge-ready:** yes — Stages 1–5 shipped; folder archived **2026-07-07**.
- **Blocking gaps:** none open.
- **Direction:** G-26 Option D (completion plan) + G-29 (validation tools are side-effect free).

**Recommended implementation order**

```text
WI-366  →  WI-367  →  WI-368  →  WI-369  →  WI-370
(protocol   completion   chart        wire         UI
 + recipes)  coordinator  capability   proof
```

---

## Runtime architecture (locked)

### Completion plan (G-26 Option D)

Ephemeral **turn-scoped completion plan** — not a durable chat artifact. The runtime knows required
steps up front; finalize is deterministic.

```text
Model declares completionMode on validate_sql (sql-only / sql-with-chart)
  OR validate_chart_spec opens enrich-existing on a resolved artifact (G-30)
  → runtime registers plan + opens SQL artifact draft
  → each tool completes one plan step and merges into draft
  → completion coordinator emits ProtocolFinal only when plan rules say finalize
```

| `completionMode` | How plan opens | Required steps (order) | Finalize when |
|------------------|----------------|------------------------|---------------|
| `sql-only` | `validate_sql` input | `validate_sql` | `validate_sql` succeeds |
| `sql-with-chart` | `validate_sql` input | `validate_sql` → `describe_sql` → `validate_chart_spec` (repeatable, G-32) | all required steps succeed |
| `enrich-existing` | `validate_chart_spec` with resolved target (G-30) | `describe_sql` if schema missing → `validate_chart_spec` (repeatable, G-32) | chart step(s) succeed (G-27 in-place update) |

**Terminal rules**

| Case | Behavior |
|------|----------|
| `sql-with-chart` `describe_sql` fails | Terminal failure: **discard draft, no new `sql.generated` persist**; assistant text only (G-31) |
| `sql-with-chart` chart step fails | Terminal failure: **discard draft, no new `sql.generated` persist**; assistant text only (G-28) |
| `enrich-existing` chart step fails | **No write** to existing row (G-27) |
| Step called out of plan order | Coordinator rejects or queues per recipe; tools do not persist |

Recipes are declared in capability/runtime registry (WI-366 docs, WI-367 code) — not inferred from
which tools happened to run (supersedes prior Option B).

### Validation tools are pure (G-29)

**Validation tools must not emit durable artifacts.** No `emitsOnSuccess` on validators.

| Tool kind | Returns | Must NOT |
|-----------|---------|----------|
| `validate_sql` | `passed`, `code`, `message`, normalized fields | Trigger `generated-sql` persist |
| `validate_chart_spec` | `passed`, `code`, `message`, `normalizedVisualization` | Trigger any persist kind |
| Other `validate_*` tools (story + platform) | Structured pass/fail + normalized payload | Trigger `OnToolSuccess` durable emit |

**Rationale:** validation is a gate, not a side effect. Assembly and persistence belong to the
**completion coordinator** when the plan is satisfied.

**WI-367 removes** `validate_sql` → `emitsOnSuccess` → `generated-sql`. **WI-368 must not add**
`emitsOnSuccess` on `validate_chart_spec`. Prompts must stop telling the model that validation
"emits automatically".

### Responsibility split

| Layer | Owns |
|-------|------|
| **Model + prompts** | Choose `completionMode`; call tools; supply inputs |
| **`sql-query`** | `validate_sql`, `describe_sql`, `execute_sql` — validators/probes only |
| **`chart-mapping`** | `list_supported_charts`, `validate_chart_spec` — no SQL, no persist |
| **Completion coordinator (WI-367)** | Plan registry, draft, step status, merge, finalize gate, `ProtocolFinal` |
| **Capabilities (tools)** | Pure structured results merged by coordinator |
| **Persistence / wire (WI-369)** | Finalized nested `sql.generated` only |
| **UI (WI-370)** | Render finalized `visualizations[]` |

---

## Implementation baseline

| Area | Codebase today | Target (locked) | Owning WI | Shipped? |
|------|----------------|-----------------|-----------|----------|
| SQL execution/schema tools | `describe_sql` / `execute_sql` done | Stage 1 locks | WI-338–WI-341 | **yes** |
  | SQL artifact protocol | Flat `sql-query.generated-sql` | Nested sections + completion recipes | WI-366 | **yes** |
  | Completion coordinator | `validate_sql` → `emitsOnSuccess` emit | Plan + draft + deterministic finalize | WI-367 | no |
| Chart capability | Missing | Pure `validate_chart_spec` | WI-368 | no |
| Wire/scenario proof | Flat wire mapper | Enriched `sql.generated` round-trip | WI-369 | no |
| mill-ui charts | Flat `kind: sql` only | Chart view from `visualizations[]` | WI-370 | no |

---

## Locked decisions

| Area | Decision | Owner |
|------|----------|-------|
| Durable unit | `sql-query.generated-sql` → `sql.generated`; chart config inside that artifact. | WI-366 |
| Payload shape | `sql`, `info`, optional `schema`, `visualizations[]`, `profiling[]`. | WI-366 |
| Canonical names | `visualizations[].key`, `schema[]`, nested `sql`, nested `info`. | WI-366 |
| Completion plan | Turn-scoped plan with `completionMode` recipes; not a durable artifact. | WI-366 / WI-367 |
| Finalization | Only completion coordinator emits `ProtocolFinal` when plan rules satisfied. | WI-367 |
| Validation purity | No `emitsOnSuccess` on any `validate_*` tool in this story. | WI-367 / WI-368 |
| Draft storage | Turn-scoped drafts keyed by plan id + normalized SQL text. | WI-367 |
| Follow-up enrichment (G-27) | `enrich-existing` plan; in-place update of resolved `sql.generated` row. | WI-367 / WI-369 |
| Enrich-existing entry (G-30) | Follow-up turns open plan via `validate_chart_spec` + `targetArtifactId` (or equivalent resolved id). | WI-366 / WI-367 / WI-368 |
| Probe failure (G-31) | `sql-with-chart` `describe_sql` fail → terminal; no new persist. | WI-367 / WI-369 |
| Multi-chart steps (G-32) | `validate_chart_spec` is repeatable per plan; merge by `key` until finalize. | WI-367 / WI-368 |
| Merge semantics | Schema → draft; visualizations by unique `key`; duplicate `key` replaces. | WI-367 / WI-368 |
| Chart terminal failure | `sql-with-chart` chart fail → no new persist; `enrich-existing` fail → no write. | WI-367 / WI-369 |
| Chart capability | `chart-mapping` never generates, rewrites, validates, or executes SQL. | WI-368 |
| Wire / UI / long-term | Unchanged from prior locks (G-5, G-18–G-23, G-25). | WI-369 / WI-370 |

---

## Blocking gaps (resolved)

### G-26. Finalize signal — **locked (Option D: completion plan)**

**Supersedes:** Option B (reactive defer on `validate_chart_spec` invocation).

**Owner WI:** WI-366 (recipes in docs) / WI-367 (coordinator)

**Locked decision:** Model declares `completionMode` on `validate_sql` for `sql-only` and
`sql-with-chart`. For follow-up enrichment, `validate_chart_spec` opens an `enrich-existing` plan
(G-30). Runtime registers required steps from recipe; finalize only when `canFinalize(plan)` per
terminal rules above.

---

### G-27. Follow-up chart enrichment — **locked (in-place update)**

**Owner WI:** WI-367 / WI-369

**Locked decision:** `enrich-existing` plan on a resolved `sql.generated` artifact; success updates
same artifact id; failure leaves row unchanged. Plan is opened by G-30, not by `validate_sql`.

---

### G-30. `enrich-existing` plan entry — **locked**

**Owner WI:** WI-366 (docs) / WI-367 / WI-368

**Problem:** Follow-up turns ("show last result as pie chart") often have no `validate_sql` call.
`completionMode` on `validate_sql` cannot open the plan.

**Locked decision:** The first chart action in a follow-up turn is **`validate_chart_spec`** with a
resolved target reference:

- **`targetArtifactId`** — preferred when the model resolved a specific `sql.generated` row, or
- equivalent resolution via `last-sql` + `info.title` / `info.description` disambiguation when id is
  omitted.

That call opens an **`enrich-existing`** completion plan on the coordinator (loads persisted
artifact into enrichment draft). No separate `begin_enrichment` tool for MVP.

**WI-368** documents `targetArtifactId` (or equivalent) on `validate_chart_spec` input.

---

### G-31. `describe_sql` failure on `sql-with-chart` — **locked**

**Owner WI:** WI-367 / WI-369

**Locked decision:** If `describe_sql` fails after `validate_sql` succeeded in a `sql-with-chart`
plan, treat as **terminal failure** — same as G-28 chart validation failure: discard draft, **no new
`sql.generated` persist**, assistant text only.

---

### G-32. Multiple chart visualizations per plan — **locked**

**Owner WI:** WI-367 / WI-368

**Locked decision:** The `validate_chart_spec` plan step is **repeatable** within one completion
plan. Each successful call merges one visualization by `key` into the draft. The coordinator
finalizes when all required non-repeatable steps succeeded and the model/planner iteration ends per
recipe (same turn: after last chart step in the tool batch, or explicit finalize when no further
chart tools are pending — WI-367 implements the concrete gate).

Multiple views (bar + pie) = multiple `validate_chart_spec` calls with distinct `key` values before
finalize.

---

### G-29. Validation tools must not persist — **locked**

**Owner WI:** WI-367 / WI-368

**Locked decision:** Remove `emitsOnSuccess` from `validate_sql`. Do not add it to
`validate_chart_spec`. All validation tools return structured results only; completion coordinator
owns durable emission. Apply this rule to new validators in this story; align existing `validate_*`
tools elsewhere when touched.

---

## Reviewed gaps — delivery pending

| Gap | Decision | Delivery owner |
|-----|----------|----------------|
| G-1 – G-25 | Prior closures unchanged; delivery not started. | WI-366 – WI-370 |
| G-11 | Finalize per completion plan (G-26), not tool chronology. | WI-367 |
| G-28 | `sql-with-chart` chart fail → no new persist (terminal rule in plan). | WI-367 / WI-369 |
| G-30 | `enrich-existing` opened by `validate_chart_spec` + target ref. | WI-366 / WI-367 / WI-368 |
| G-31 | `sql-with-chart` `describe_sql` fail → terminal, no new persist. | WI-367 / WI-369 |
| G-32 | Repeatable `validate_chart_spec` step; merge by `key`. | WI-367 / WI-368 |

---

## Open gaps requiring clarification

**None.**

---

## Superseded planning

- **Option B** (defer finalize when chart tool seen) — superseded by completion plan (G-26).
- **`emitsOnSuccess` on validators** — antipattern for this story (G-29).
- Legacy `docs/design/agentic/charts/*` dual-artifact topology — update per WI-366 / WI-369.

Normative sources: `STORY.md`, this file, active WI files.
