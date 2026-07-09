# WI-396 - Analysis copilot backend profile

## Goal

Implement the backend `analysis-copilot` agent profile and wire turn-level `context.values` from the
Analysis inline chat send path into the agent runtime so real-backend Analysis copilot sessions work
without `Unknown profile` errors.

## Prerequisites

- **WI-393** â€” frontend sends `context.values` on inline chat message POST.
- **WI-394** â€” Analysis host apply/run/settings/undo behavior is stable (copilot UX the profile
  should support).
- **Recommended:** complete **WI-395** UI closeout first so the frontend contract is frozen; this WI
  may start immediately after WI-394 if backend work should run in parallel with doc/test closeout.

## Requirements

### Profile registration

- Add a new isolated profile document to `ai/mill-ai/src/main/resources/profiles/` (or extend
  `platform-agent-profiles.yaml` per repo convention):
  - **id:** `analysis-copilot`
  - **not** a prompt variant of `data-analysis`
- Starting capability stack (locked in WI-389 / `INLINE-CHAT-FOUNDATION.md`):
  - `conversation`
  - `sql-query`
  - `schema` (schema exploration)
  - `metadata` + `metadata-authoring`
- **Exclude** initially: `chart-mapping`, `chart` capabilities, and other General Chat-only stacks
  unless explicitly required for a minimal copilot reply.

### Profile intent and prompts

- Add profile-level intent routing prompts tuned for Analysis copilot turns:
  - reason primarily over the user's **current SQL**, not generic open-ended chat
  - prefer SQL rewrite/optimization/explanation over unrelated schema tours unless the user asks
  - emit SQL proposals as structured `sql` artifacts suitable for inline artifact strips (not prose
    fenced SQL as the primary apply path)
- Add a deterministic, minimal Analysis turn-context prompt section. Do **not** dump the full
  `context.values` map into the prompt.
- Default prompt injection includes only:
  - sanitized `sql.current` when present
  - bounded query title/name when present
  - bounded query description when present
  - bounded `execution.last.error` when present, because it helps correct human-written SQL
- Dialect is backend-defined by the SQL capability/runtime. Client-supplied dialect is accepted on
  the wire but is not authoritative prompt context.
- Query title/name and description are lightweight human context only; they are not authoritative
  object state and must be length-limited before injection.
- Document prompt keys and routing behavior under `docs/design/agentic/` or
  `docs/design/ai/` (component-appropriate path).

### Send-message `context.values` (backend)

- Extend `SendMessageHttpRequest` plus service/runtime send APIs to keep context optional:
  ```json
  "context": { "values": { ... }, "version": <optional int> }
  ```
- The boundary contract on UI and server side is `sendMessage(chatId, message, context?)`.
  `context` must remain optional for General Chat and non-contextual sends.
- **Tolerance:** unknown keys in `values` must not fail the request; profile/capabilities read only
  what they understand.
- **Ephemeral:** turn context is input for the agent run only. Do not persist `context.values` on the
  durable `Turn` row in this WI unless an existing persistence hook already requires a minimal audit
  field (prefer not).
- Map accepted Analysis keys into runtime (see WI-389 contract):
  - `sql.current`, `sql.dialect`
  - `artifact.query.id`, `artifact.query.name`, `artifact.query.description`, `artifact.query.dirty`
  - `execution.last.id`, `execution.last.status`, `execution.last.rowCount`
  - `execution.last.columns` (name/type only)
  - `execution.last.error` (string)
- Each individual `context.values` value is limited to 4096 bytes (4 KB) after serialization or
  canonical string conversion. Oversized values are truncated or ignored by implementation policy;
  they must not fail the whole send path unless malformed.
- **Never** inject raw result rows from client context into prompts or tools.
- Do not pass raw `context.values` into `ToolExecutionContext` by default. The LLM sees minimal
  prompt context and maps needed values into explicit tool parameters when it calls tools. Future
  direct backend access to a context value requires a typed opt-in adapter and tests.
- Plumb optional turn context into runtime/prompt assembly so the model sees the minimal Analysis
  context on each turn.
- Rehydration for contextual chats with `contextType = analysis` must resolve profile
  `analysis-copilot` successfully.

### API and runtime behavior

- `GET /api/v1/ai/profiles` and profile inspect must list `analysis-copilot`.
- Validate `profileId` at chat create time. Unknown profile ids must fail immediately; do not allow
  chats with unknown profile ids to be created and fail later on send.
- Contextual chat create with `profileId: analysis-copilot` + `contextType: analysis` must succeed
  because the profile is registered.
- `POST /api/v1/ai/chats/{chatId}/messages` with optional Analysis `context.values` must complete a
  run (or fail only for ordinary agent/LLM errors, not unknown profile / unknown context envelope).
- Profile change on contextual Analysis chats remains disallowed (existing unified-chat rule).

### Tests

- Unit/slice tests:
  - profile loads from YAML and appears in registry matrix
  - `SendMessageHttpRequest` deserializes `context.values`; omitted `context` unchanged
  - unknown context keys do not break send handling
  - unknown `profileId` is rejected at create time
  - `analysis-copilot` create succeeds
  - prompt context includes only sanitized SQL, bounded title/description, and bounded
    `execution.last.error`
  - context values over 4096 bytes are truncated or ignored per implementation policy
- Service tests (`mill-ai-service`):
  - create + send for `analysis` contextual chat with `analysis-copilot` - no `Unknown profile`
  - assert prompt input includes `sql.current` when provided in context
  - assert raw `context.values` are not passed into tool execution context by default
- Use deterministic mocked tests for structured SQL artifact evidence. Mock the model/runtime
  response or scenario harness so tests prove the `analysis-copilot` profile can route a SQL
  artifact through the backend protocol into the existing structured artifact stream.
- Optional live-LLM scenario may stay gated; it must not be the only acceptance evidence.

### Documentation

- Update `docs/design/ui/mill-ui/INLINE-CHAT-FOUNDATION.md` â€” mark backend profile as implemented
  (not â€œdocumented onlyâ€).
- Add or extend agentic design doc for `analysis-copilot` capability matrix and context consumption.
- Update `docs/workitems/in-progress/analysis-inline-chat-foundation/GAPS.md` backend row when done.

## Non-Goals

- Model/Knowledge inline chat profiles.
- Persisting `context.values` on turns for replay.
- Changing General Chat `data-analysis` profile behavior.
- mill-ui changes beyond fixes strictly required for backend contract mismatches.
- Live-LLM CI promotion (optional scenario may stay gated).

## Acceptance Criteria

- `analysis-copilot` exists in platform agent profiles and is discoverable via the profiles API.
- Analysis inline chat against real backend (`npm run dev` + mill-service, no `VITE_CHAT_API=mock`)
  can create a contextual chat and send a message without `Unknown profile 'analysis-copilot'`.
- Send accepts optional `context.values`; General Chat and non-contextual sends continue to work
  without context.
- Default prompt context is minimal: sanitized SQL, bounded title/description, and bounded
  `execution.last.error` only.
- Tool calls receive context values only when the LLM maps visible prompt context into explicit tool
  parameters; raw `context.values` are not passed to tools by default.
- Create rejects unknown profile ids immediately.
- Mocked tests prove SQL-oriented copilot replies can produce structured SQL artifacts consumable by
  existing inline strip UI.
- Unknown keys in `context.values` do not fail the send path.
- Each individual context value is limited to 4096 bytes.
- Tests cover profile registration and send-message context deserialization at minimum.
- Design docs describe implemented behavior and distinguish `analysis-copilot` (backend) from
  `inline-analysis` (frontend host key).

## Verification

```bash
./gradlew :ai:mill-ai:test :ai:mill-ai-service:test
./gradlew :ai:mill-ai-test:testIT
cd ui/mill-ui && npm run test -- --run
```

Manual (real backend):

```bash
# mill-service running; ui without mock chat
cd ui/mill-ui && npm run dev
# /analysis â†’ open copilot â†’ send "optimize this query"
# Expect: no Unknown profile; assistant reply streams
```

