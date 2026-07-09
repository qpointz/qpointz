# WI-395 - Analysis copilot tests and docs

## Goal

Complete test coverage and update durable documentation for the inline-chat foundation and
Analysis copilot specialization.

## Requirements

- Add or update tests covering the full Analysis copilot workflow:
  - latest snapshot included on send
  - dedicated `analysis-copilot` profile resolution
  - SQL inline artifact strip rendering
  - Apply
  - Apply & Run
  - Undo and redo after chat apply
  - General Chat artifact rendering remains unchanged
- Add or update tests for decided contracts:
  - `context.values` is sent without a required host/view type discriminator
  - `context.version` is optional
  - Analysis snapshots do not include raw result rows
  - Analysis copilot uses `analysis-copilot`; General Chat profile behavior is unchanged
  - Apply & Run closes prior execution, resets pagination, and runs full applied SQL
  - Apply & Run keeps applied SQL and shows the existing error pattern on failure
  - Undo/redo state is session-only and resets after Save
- Update `docs/design/ui/mill-ui/` documentation with the final implemented behavior.
- Ensure documentation preserves the terminology distinction:
  - inline chat is the generic context-aware mechanism
  - Analysis copilot is the Analysis-specific functionality/profile
  - `inline-analysis` is only the frontend host/rendering key
- Update docs that currently describe superseded behavior:
  - `docs/design/ai/chat-artefact-architecture.md`
  - `docs/design/ui/mill-ui/ARCHITECTURE.md`
  - public `mill-ui` docs if they still describe `host-apply`
  - backend API/backlog docs if they still describe the legacy inline-chat endpoint
- Hard remove legacy inline-chat API references from documentation and code:
  - no `POST /api/v1/inline-chat/messages` endpoint documented as available, planned, or
    backward-compatible
  - no backlog row requesting implementation of the legacy inline-chat endpoint
  - no frontend service helper, mock path, test, or feature flag depending on a legacy inline-chat
    endpoint
  - unified chat (`POST /api/v1/ai/chats` + SSE messages) remains the only documented inline-chat
    transport
- Document that manual verification assumes inline chat is already enabled in the environment.
- Do not add or update feature-flag recipes, presets, or management documentation in this story.
- Update backend API/design documentation for the optional `context.values` send-message contract.
- Keep the story under `in-progress/` after the first completed WI; do not archive unless the user
  explicitly requests story closure.

## Acceptance Criteria

- Local UI test suite passes.
- UI production build passes.
- Documentation accurately describes how future Model and Knowledge inline chats should reuse the
  foundation.
- Documentation does not conflate the `inline-analysis` host key with the `analysis-copilot`
  backend profile id.
- Documentation no longer presents silent `host-apply` or markdown SQL extraction as the intended
  Analysis copilot behavior.
- Documentation records that the mock preview is a manual UX checkpoint, not a complete backend
  simulation.
- Legacy inline-chat API documentation/code references are removed, not deprecated.
- Feature-flag management is not changed by this story.
- The story tracker is updated according to `docs/workitems/RULES.md` when the WI is completed.

## Verification

```bash
cd ui/mill-ui && npm run test -- --run
cd ui/mill-ui && npm run build
```
