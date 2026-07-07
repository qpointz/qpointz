# Cold start handoff — ai-chart-mapping

Use this document when resuming the chart-mapping story from a fresh checkout or another computer.

## Current State

- Branch: `restart/ai-chart-mapping-after-stage1`
- Story folder: `docs/workitems/in-progress/ai-chart-mapping/`
- Story status: `in-progress`
- **Stage 1 complete:** WI-338–341
- **Stages 2–5 in progress:** WI-366–370 (SQL `visualizations[]`, completion coordinator, chart-mapping, wire, mill-ui)
- **Next:** story closure after WI-370 + verify block in `STORY.md`
- Work item tracker: see [`STORY.md`](STORY.md)

## Files To Read First

1. [`STORY.md`](STORY.md) — scope, flow, artifact direction, UI behaviour, WI order.
2. [`GAPS.md`](GAPS.md) — locked decisions (completion plan, validation purity).
3. [`sql-artifact-visualization-protocol.md`](../../../design/agentic/sql-artifact-visualization-protocol.md) — **normative protocol** (WI-366).
4. [`charts/README.md`](../../../design/agentic/charts/README.md) — per-chart MVP specs.
5. The WI you are about to implement: [`WI-366-chart-artifact-contract.md`](WI-366-chart-artifact-contract.md)
6. [`chart-story-closure.md`](chart-story-closure.md) — BACKLOG/docs/MILESTONE policy (Gap 28).

## Locked Intent

- `sql-query` owns SQL generation, validation, and SQL artifacts.
- `sql-query` owns bounded SQL execution and result-schema probing through `describe_sql` and
  `execute_sql`; no separate `data-query` capability.
- `chart-mapping` must not generate, rewrite, validate, or execute SQL.
- Chart configs live in **`sql.generated.visualizations[]`** (`kind: "chart"`); no separate chart artifact.
- Chart configuration is renderer-agnostic; no ECharts/Vega/Plotly payloads from the model.
- Supported chart types are exposed through a capability tool such as `list_supported_charts`, not
  injected into prompts.
- Chart rendering in `ui/mill-ui` must be shared infrastructure, not chat-only code.
- Chart rendering requires a bounded full data snapshot via shared query-service `full` result mode,
  separate from paged Data tab inspection.
- Validation failures → assistant text only; no failed chart artifact card.

## Recommended Resume Flow

1. Pull the branch and confirm clean state:

   ```bash
   git fetch origin
   git checkout restart/ai-chart-mapping-after-stage1
   git pull --ff-only
   git status --short --branch
   ```

2. Read [`GAPS.md`](GAPS.md), then start **WI-366**.
3. For each WI:
   - resolve its assigned `GAPS.md` sections
   - implement scoped code/docs/tests
   - update the checkbox in `STORY.md`
   - commit the full intentional working copy for that WI
   - push the branch

## Important Locked Decisions (Stages 2-6)

Retained from the prior definition pass (see [`GAPS.md`](GAPS.md) and `docs/design/agentic/charts/chart-*.md`):

- Result schema provenance for chart mapping is locked to `sql-query.describe_sql`.
- Stage 1 gaps **8a–8c** are locked: `execute_sql` defaults to **`paged`**; `source.kind` is
  **`execution`**; `nativeType` is optional adapter metadata only.
- **Gap 17** — last-result context: prompt + turn artifacts; multi-SQL via `title`/`description`
  ([`chart-context-resolution.md`](../../../design/agentic/charts/chart-context-resolution.md)).
- **Gap 18** — `CHART_MAP` on `chart-mapping.intent`; `data-analysis` composes only
  ([`chart-routing-intent.md`](../../../design/agentic/charts/chart-routing-intent.md)).
- **Gap 19** — emission via completion coordinator when plan completes (not validator `emitsOnSuccess`)
  ([`chart-emission-path.md`](../../../design/agentic/charts/chart-emission-path.md)).
- **Gap 20** — MCP inventory on `data-analysis` (catalog + SQL + chart validation)
  ([`chart-mcp-exposure.md`](../../../design/agentic/charts/chart-mcp-exposure.md)).
- **Gap 21** — test proof: layered mock-LLM, scripted `ArtifactEmitScenariosIT`, scenario-export
  workflow ([`chart-test-proof-strategy.md`](../../../design/agentic/charts/chart-test-proof-strategy.md));
  manual E2E out of CI.

**Stage 6 gaps 22-28 are locked** for later WI-370 implementation. Closure policy: [`chart-story-closure.md`](chart-story-closure.md).

## Validation Commands By Story End

```bash
./gradlew :ai:mill-ai:test --tests "*SqlQueryExecution*"
./gradlew :ai:mill-ai-mcp-core:test
./gradlew :ai:mill-ai-mcp-transport-http:testIT --tests "*SqlQueryExecution*"
./gradlew :ai:mill-ai:test --tests "*Chart*"
./gradlew :ai:mill-ai-test:testIT --tests "*Chart*"
./gradlew :ai:mill-ai-service:test --tests "*ArtifactWireMapper*"
cd ui/mill-ui && npm run test -- --run
cd ui/mill-ui && npm run build
```

## Do Not Forget

- Story folder stays in `in-progress/` until story closure.
- Do **not** update `MILESTONE.md` at story closure — only when preparing a new **release version**.
- Update `BACKLOG.md` at **story closure** only (`done` rows; prune at release per `RULES.md`).
- **Do** update design and public docs on the branch as WIs ship ([`chart-story-closure.md`](chart-story-closure.md)).
- No feature flag for chart capability or UI.
- Do not modify unrelated files or revert user changes outside this story.
