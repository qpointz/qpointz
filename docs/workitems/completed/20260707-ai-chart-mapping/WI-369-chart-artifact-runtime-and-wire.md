# WI-369 - SQL artifact wire and scenario proof

Status: `done`  
Type: `feature`, `test`  
Area: `ai`, `services`

## Problem Statement

After SQL artifact finalization and chart visualization validation exist, the runtime must persist,
stream, replay, and test one enriched `sql.generated` artifact with chart visualization entries.

## Goal

Wire enriched generated SQL artifacts through routing, persistence, SSE, REST replay, and scenario
tests with `visualizations[]` preserved.

## Locked Decisions

- Live and replayed enriched artifacts remain `kind: "sql"` / `wirePartType: sql`.
- REST replay preserves `sql`, `info`, `schema`, `visualizations[]`, and `profiling[]` where present.
- Tests must prove requested chart enrichment happens before final SQL artifact persistence.
- Failed chart validation must not publish failed visualization entries.
- Follow-up chart enrichment updates the same resolved `sql.generated` artifact row; validation
  failure leaves the row unchanged.
- Scenario proof uses mock/scripted flows with no live LLM dependency in CI.
- Tests target the nested SQL artifact shape only; no migration test is required for old flat rows.

## In Scope

1. Update artifact descriptor expectations for `generated-sql`:
   - `protocolId: sql-query.generated-sql`
   - `artifactKind: generated-sql`
   - `persistKind: sql.generated`
   - `pointerKeys: [last-sql]`
   - `wirePartType: sql`
   - `presentation: structured`
2. Ensure the routed/persisted payload is the finalized nested artifact from WI-366/WI-367.
3. Extend `ArtifactWireMapper` so GET replay preserves:
   - `sql`
   - `info`
   - `schema`
   - `visualizations[]`
   - `profiling[]`
   - lifecycle/status fields already present on wire artifacts
4. Extend SSE expectations so live structured SQL parts preserve the same nested payload.
5. Ensure row snapshots are not persisted inside `visualizations[]`.
6. Add runtime tests proving no partial artifact is persisted before requested chart validation
   completes.
7. Add scenario tests:
   - SQL-only request persists one SQL artifact without visualizations.
   - Fresh chart request persists one SQL artifact with a chart visualization.
   - "show last result as pie chart" updates the resolved existing SQL artifact with a chart
     visualization and keeps the same artifact id.
   - Chart validation fails for an absent field and no SQL artifact with failed visualization is
     persisted for that chart request.
   - Follow-up chart validation failure leaves the existing SQL artifact unchanged.
   - `query_refinement_needed` produces assistant text and no invalid visualization.
   - Renderer-specific config is refused.
   - Multiple chart visualizations for one SQL result are preserved in one `visualizations[]` array.
8. Update scenario export/hints so enriched `sql.generated` artifacts can reconstruct chart
   validation steps where the run-event path is unavailable.
9. Update scenario proof docs that still reference the previous topology so they verify enriched
   `sql.generated` artifacts and not a second wire/persistence path.

## Out of Scope

- UI rendering.
- Query execution changes beyond using existing `describe_sql` / `execute_sql` outputs.
- New chart image/export backend.
- Separate chart persistence, pointer, protocol, or wire part.

## Acceptance Criteria

- [x] Enriched `sql.generated` artifacts persist and replay via `GET /api/v1/ai/chats/{id}`.
- [x] Live SSE emits structured `sql` parts with nested `sql`, `info`, `schema`, `visualizations[]`,
      and `profiling[]` where present.
- [x] Wire mapping preserves chart visualization configs exactly enough for UI compilation.
- [x] Wire mapping does not persist or replay chart row snapshots.
- [x] Runtime tests prove requested chart enrichment happens before final SQL artifact persistence.
- [x] Runtime tests prove chart validation failure does not publish failed visualization entries.
- [x] Follow-up enrichment tests prove successful chart validation updates the same artifact row and
      failed validation leaves the row unchanged.
- [x] Scenario proof uses mock/scripted flows; no live LLM is required in CI.
- [x] Scenario export includes enough information to verify enriched SQL artifacts and chart
      validation steps.
- [x] Existing SQL, data, and facet artifact tests remain green.
- [x] No tests or fixtures assert a chart-specific persist kind, pointer, protocol, or wire part.

## Suggested Commit

`[feat] WI-369: wire enriched SQL artifacts`
