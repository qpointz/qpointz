# Chart artifact emission path — superseded

**Status:** Superseded (G-26 completion plan + G-29 validation purity)

The prior **Gap 19** decision (`validate_chart_spec` + `emitsOnSuccess` → separate `generated-chart`) is **not** the active implementation path.

## Current model

| Layer | Responsibility |
|-------|----------------|
| `validate_sql` / `validate_chart_spec` | Pure validation — structured pass/fail only; **no** `emitsOnSuccess` |
| **SqlArtifactCompletionCoordinator** | Turn-scoped completion plan, draft merge, `ProtocolFinal` when `canFinalize` |
| Durable payload | `sql.generated` with optional `visualizations[]` (`kind: "chart"`) |

Normative protocol: [`../sql-artifact-visualization-protocol.md`](../sql-artifact-visualization-protocol.md).

## Historical note

Earlier text in this file described tool auto-emit mirroring `validate_sql` → `generated-sql`. That pattern is removed for validators; only the completion coordinator persists `sql.generated`.
