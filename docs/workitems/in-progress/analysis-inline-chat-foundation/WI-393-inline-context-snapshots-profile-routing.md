# WI-393 - Context snapshots and inline profile routing

## Goal

Add the shared frontend plumbing that lets inline chats send the latest host-view state and choose
the backend profile required by the host-specific copilot behavior.

## Requirements

- Add a typed inline context snapshot provider registry.
- Let host views register and unregister a snapshot provider by inline context type and context id.
- Call the snapshot provider immediately before sending each inline chat message.
- Extend the frontend chat send path to carry optional contextual metadata for the turn:
  - `context?: { values: Record<string, unknown>; version?: number }`
  - `version` is optional and should be omitted unless a concrete migration need appears.
  - no host/view `type` discriminator is required in the wire envelope.
  - the UI consumer decides which context keys to provide.
  - the selected profile/capability decides which context keys it understands and how to use them.
  - unknown context keys are tolerated by backend/profile consumers.
  - context is turn input, not canonical object state.
- Add inline profile resolution:
  - Analysis copilot uses the hardcoded backend profile id `analysis-copilot` for now.
  - General Chat keeps its existing `data-analysis` default behavior.
  - General Chat session picker and `VITE_MILL_AI_PROFILE` do not affect the Analysis copilot.
  - Mock mode may synthesize `analysis-copilot` in the create-chat wire even before the backend
    profile exists.
- Analysis snapshot must include the latest editor and execution state:
  - `sql.current`: SQL text
  - `sql.dialect`: SQL dialect when known
  - active saved-query id, name, and description
  - active saved-query dirty state
  - last execution id/status when present
  - result columns and row count when present
  - current execution or SQL error summary when present
  - no raw result rows
- `contextId = '__analysis__'` may remain the chat identity for unsaved Analysis context; the
  current SQL state must be carried in `context.values`.

## Acceptance Criteria

- Inline chat sends the latest Analysis snapshot with each message, not only the original context id.
- The send wire contract uses a generic optional `context.values` map and does not require a
  host-specific context type.
- `context.version` is optional, not required.
- Analysis context values include current SQL, query metadata, execution metadata, and error
  summary, but no raw result rows.
- Existing inline chat context creation still passes context type, id, label, and entity type.
- General Chat profile selection and profile picker behavior are unchanged.
- Analysis copilot create calls use backend profile id `analysis-copilot` in real and mock modes.
- Snapshot provider cleanup prevents stale providers after route/context changes.
- Tests cover profile resolution and latest-state snapshot capture.
- Tests prove unknown context keys do not break the send path.

## Verification

```bash
cd ui/mill-ui && npm run test -- --run
```
