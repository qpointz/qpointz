# WI-291 — Client query execute + attach-result

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ui`, `ai` |
| **Depends on** | [**WI-289**](WI-289-ai-sql-view-design-contract.md) |
| **Enables** | [**WI-292**](WI-292-ai-sql-view-artifact-preview-ui.md) |

## Goal

Wire **Run** to **`queryService`** for chat types whose treatment includes Run (v1: **`general`**
`condensed-preview` only — not `inline-analysis` host-apply). Persist execution metadata on the turn
so GET replay can lazy-hydrate chat preview grids without re-executing SQL.

## Deliver

### mill-ui (required)

- **Run / Re-run** calls [`queryService.executeQuery`](../../../../ui/mill-ui/src/services/queryService.ts)
  with same semantics as [`QueryPlayground.handleExecute`](../../../../ui/mill-ui/src/components/queries/QueryPlayground.tsx).
- On success: append client `data` artefact to message state; eager grid from `QueryResult`.
- **No** AI-chat proxy for execution.
- **No** client salvage — SQL artefact arrives via SSE from artefacts foundation.

### mill-ai-service (attach — for replay)

- `POST /api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result`:
  - Body: `executionId`, columns, `rowCount`, `truncated`, sql snapshot.
  - Persists `sql.result` artefact (`artifactType: sql-result`); links to turn.
  - Aligns with `sql-result` descriptor in artefacts-branch [`sql-query.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml).
  - **Does not** call `QueryResultExecutionService`.

### Types

- Client `ChatMessageArtifact` kind `data` aligned with [`QueryResult`](../../../../ui/mill-ui/src/types/query.ts).

## Explicitly out

- Server-side query execution.
- `StructuredPart(partType=data)` on agent SSE stream.
- Lazy hydration hook (WI-292 `useLazyArtifactData`).
- Salvage inference.

## Acceptance criteria

- [ ] Run in chat uses `/api/v1/query` via `queryService` only.
- [ ] After Run, message state includes `data` artefact with `executionId`.
- [ ] Attach endpoint persists metadata when called after successful Run.
- [ ] GET replay (WI-290) returns attached `executionId` for history turns.
