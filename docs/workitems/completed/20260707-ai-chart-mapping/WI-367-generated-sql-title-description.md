# WI-367 - SQL artifact completion coordinator

Status: `done`
Type: `feature`, `test`
Area: `ai`, `services`

## Problem Statement

The current emission model treats successful SQL validation as the point where a generated SQL
artifact is emitted and persisted (`validate_sql` → `emitsOnSuccess`). That couples validation to
side effects and cannot support multi-step enrichment (schema, chart) deterministically.

The runtime needs a **completion plan** plus draft assembly so only the completed SQL artifact is
persisted when the plan is satisfied.

## Goal

Implement a turn-scoped **SQL artifact completion coordinator** that:

1. Registers a completion plan from `completionMode` (or `enrich-existing` entry).
2. Merges pure validator/probe tool results into a draft.
3. Emits `ProtocolFinal` / persists `sql.generated` **only** when `canFinalize(plan)` is true.

## Locked Decisions

- **Validation is pure (G-29):** remove `emitsOnSuccess` from `validate_sql`. Validators return
  structured pass/fail only; they do not persist durable artifacts.
- **Completion plan (G-26 Option D):** finalize is driven by declared recipes, not by inferring
  intent from which tools ran.
- `completionMode` on `validate_sql` input (default `sql-only` when omitted):
  - `sql-only` — steps: `validate_sql` → finalize on success
  - `sql-with-chart` — steps: `validate_sql`, `describe_sql`, `validate_chart_spec` → finalize when
    all succeed
- `enrich-existing` entry for follow-up chart (G-27): resolve target `sql.generated`, steps:
  `describe_sql` if schema missing, `validate_chart_spec` → in-place update on success
- Terminal rule (G-28): `sql-with-chart` chart validation failure → discard draft, **no new**
  `sql.generated` persist; assistant text only
- `enrich-existing` chart failure → no write to existing row
- New final artifacts require non-blank `info.title` and `info.description` (from `validate_sql`).
- `last-sql` points to the final persisted SQL artifact.
- Draft state is turn-scoped, keyed by plan id + normalized SQL text.
- Breaking shape changes accepted; no migration for old flat rows.

## In Scope

1. Remove `validate_sql` → `emitsOnSuccess` → `generated-sql` from `sql-query.yaml` and coordinator
   auto-emit for validators.
2. Require `title`, `description`, and `completionMode` on `validate_sql` input (`sql-only` default).
3. Validate `title` and `description` before SQL semantic validation (3–120 / 10–500 chars).
4. On successful `validate_sql`, open completion plan + draft (`sql`, `info`; empty `schema`,
   `visualizations[]`, `profiling[]`).
5. Implement **SqlArtifactCompletionCoordinator** (name illustrative):
   - track plan steps and status (`PENDING` / `SUCCEEDED` / `FAILED`)
   - merge `describe_sql` → `schema[]`
   - merge successful `validate_chart_spec` → `visualizations[]` (by `key`)
   - `canFinalize(plan)` / `finalize(plan)` → single `ProtocolFinal`
6. Wire coordinator into `LangChain4jAgent` tool loop **after** tool execution (replaces validator
   `emitOnToolSuccess` for `generated-sql`).
7. Register completion recipes (YAML or code registry) per G-26 table in `GAPS.md`.
8. Follow-up `enrich-existing`: load persisted artifact, open enrichment plan, update same row on
   success.
9. Merge semantics: multiple visualizations append by unique `key`; duplicate `key` replaces;
   multiple SQL statements → separate plans/drafts/finals per turn.
10. Update `sql-query` prompts: validators do not "emit automatically"; coordinator finalizes when
    plan completes.
11. Update `ArtifactEmissionCoordinator.buildGeneratedSqlPayload` for nested shape from draft.
12. Unit and integration tests for plans, finalize gates, terminal failures, enrich-existing.

## Out of Scope

- `validate_chart_spec` implementation — WI-368 (must not add `emitsOnSuccess`).
- UI chart rendering — WI-370.
- Backward compatibility for flat payloads.
- Structured profile intent classifier (prompt `CHART_MAP` remains model guidance only).

## Acceptance Criteria

- [x] `validate_sql` has no `emitsOnSuccess`; successful validation does not persist alone.
- [x] `validate_sql` input requires `title` and `description`; accepts `completionMode`.
- [x] Missing or invalid context fields fail before SQL semantic validation.
- [x] `sql-only` plan finalizes and persists one `sql.generated` after `validate_sql` succeeds.
- [x] `sql-with-chart` plan does not persist until `describe_sql` and `validate_chart_spec` succeed.
- [x] `sql-with-chart` chart validation failure does not persist a new `sql.generated`.
- [x] `enrich-existing` success updates the same artifact id; failure leaves row unchanged.
- [x] Final artifacts use nested `sql` and `info` from WI-366.
- [x] No code path persists chart config outside coordinator finalize.
- [x] Tests cover all completion modes, terminal rules, merge semantics, and multi-SQL turns.

## Suggested Commit

`[feat] WI-367: SQL artifact completion coordinator`
