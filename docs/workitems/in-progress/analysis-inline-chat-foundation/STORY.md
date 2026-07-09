# Inline chat foundation and Analysis copilot

**Status:** `in-progress` — WI-389 through WI-396 complete (story closure pending)

## Delivery summary

Reusable **inline chat** foundation for context-bound copilots, with **Analysis copilot** as the first
specialization (`analysis-copilot` backend profile; `inline-analysis` UI host key).

| WI | Delivered |
|----|-----------|
| **389** | Design contract — `INLINE-CHAT-FOUNDATION.md`, profile routing, artifact strip model |
| **390** | Legacy inline-chat API removed; unified chat path only |
| **391** | Compact **inline artifact strips** (not full cards) with host actions |
| **392** | Mock Analysis preview — keyword SQL synthesis for manual UX before backend |
| **393** | Per-turn **context snapshots** (`context.values`) + inline profile routing |
| **394** | Analysis host — explicit Apply / Apply & Run / Copy, auto-apply/run settings, editor undo/redo |
| **395** | Tests + doc reconciliation (UI + design docs) |
| **396** | Backend **`analysis-copilot`** profile, `TurnContextValues` / sanitizer, prompt injection |

**UX highlights:** resizable editor/drawer split; copilot settings (`automation.mode` radio) in drawer
header extensible menu; toolbar keeps standalone inline-chat button (matches dev); **WI-400** polish —
Analysis copilot chrome, starter prompts, responsive artifact strips; **WI-401–403** compact artifact
pills with icon actions; **post-WI-403 polish** (`ed6b272d`) — per-artifact applied dot, hover
expand hint, facet reject strikethrough, scroll stability on status updates (see
`WI-403-post-polish.md`).

**Explicitly deferred:** deterministic artifact **publish validation** → separate story
[`artifact-publish-validation`](../../planned/artifact-publish-validation/STORY.md) (A-98).

**Explicitly deferred:** **related-object persistence and restore** (inline chat UI presence after
reload, unified related content / chat references, explicit query↔chat relations) → separate story
[`context-relations`](../../planned/context-relations/STORY.md) (U-19). The foundation story binds
the drawer to `(contextType, contextId)` in-session only; **transcript** may exist in `ai_chat` after
the first message, but **session indicators, drawer state, and copilot settings are not persisted**
across a full page reload.

**Still pending for story closure:** `MILESTONE.md`, MR-ready squash, archive to
`docs/workitems/completed/`.

## Goal

Define the reusable inline-chat mechanism and deliver the first specialization through the
Analysis copilot.

Inline chat means the context-aware chat mechanism attached to a host view. The Analysis copilot is
the Analysis-specific inline-chat functionality that helps the user author and iterate on the
current SQL query. Future Model and Knowledge inline chats should reuse the same profile routing,
context snapshot, host action, and inline artifact strip foundation while supplying their own
context-specific behavior.

## Terminology

- **Inline chat:** the generic context-aware chat UI/session mechanism attached to a host view.
- **Analysis copilot:** the Analysis-specific inline-chat functionality and backend profile
  contract, using profile id `analysis-copilot`.
- **`inline-analysis`:** the frontend host/rendering key for Analysis inline-chat integration. It
  is not a backend profile id.

## Product Intent

Users should be able to ask the Analysis copilot for help with the query they are currently
editing. The assistant must reason over the latest editor state, not just the saved query identity.
When the assistant proposes SQL, the user reviews a compact inline artifact and explicitly applies
it to the editor. The user can also choose to apply and run the proposed SQL in one action.
Analysis may expose opt-in inline chat settings, such as automatic apply and automatic run, but
those settings are host-interpreted and visible only while an inline chat exists for the current
Analysis context.

The inline chat surface is intentionally smaller and less flexible than General Chat. It should not
render full artifact cards inside the drawer by default. Instead, it uses compact inline artifact
strips that name the generated object and expose host-specific actions.

## Scope

- Deliver an early UI preview path so the Analysis copilot interaction can be tested manually
  before backend profile and capability work exists.
- Define the reusable inline chat foundation for context snapshots, profile routing, host actions,
  host-interpreted settings, and inline artifact strip presentation.
- Implement the Analysis copilot as the first specialized inline-chat behavior on the Analysis
  host.
- Send the latest Analysis state with each inline chat turn so the assistant can reason over current
  SQL, dirty state, and recent execution state.
- Render SQL artifacts in the Analysis copilot as inline artifact strips with Apply, Apply & Run,
  and Copy actions, while allowing opt-in Analysis settings to automate apply/run behavior.
- Add editor-level undo/redo support so chat-applied SQL can be reverted like normal edits.
- Document backend profile and capability expectations without implementing backend capability
  changes in this story.

## Non-Goals

- No backend implementation of new AI profiles or capabilities **in WI-389–WI-395** (see **WI-396**
  for the `analysis-copilot` backend profile after Analysis host behavior is complete).
- No Model or Knowledge specialized inline chat behavior beyond reusable foundation hooks.
- No full artifact card rendering inside inline chat.
- No automatic SQL overwrite on artifact arrival unless the user has enabled the Analysis copilot
  auto-apply setting.
- No raw result-row injection into prompt context.
- **No deterministic runtime artifact publish validation** — deferred to separate story
  [`artifact-publish-validation`](../../planned/artifact-publish-validation/STORY.md) (WI-397–399);
  this story preserves the *expectation* that backend-validated SQL is the norm, but does not
  implement the platform publish gate.

## Follow-up (separate story)

| Story | Backlog | Scope |
|-------|---------|--------|
| [`artifact-publish-validation`](../../planned/artifact-publish-validation/STORY.md) | A-98 | Runtime publish gate, capability validators by key, correction loop, prompt cleanup |

## Work Items

- [x] WI-389 - Inline chat foundation design (`WI-389-inline-chat-foundation-design.md`)
- [x] WI-390 - Legacy inline-chat API removal (`WI-390-legacy-inline-chat-api-removal.md`)
- [x] WI-391 - Inline artifact strips (`WI-391-inline-artifact-strips.md`)
- [x] WI-392 - Mock Analysis copilot preview (`WI-392-mock-analysis-inline-chat-preview.md`)
- [x] WI-393 - Context snapshots and inline profile routing (`WI-393-inline-context-snapshots-profile-routing.md`)
- [x] WI-394 - Analysis SQL apply and editor history (`WI-394-analysis-inline-sql-apply-history.md`)
- [x] WI-395 - Analysis copilot tests and docs (`WI-395-analysis-inline-chat-tests-docs.md`)
- [x] WI-396 - Analysis copilot backend profile (`WI-396-analysis-copilot-backend-profile.md`)
- [x] WI-400 - Inline chat UI polish (`WI-400-inline-chat-ui-polish.md`)
- [x] WI-401 - Analysis copilot automation radio (`WI-401-analysis-copilot-automation-radio.md`)
- [x] WI-402 - Inline artifact pill shell (`WI-402-inline-artifact-pill-shell.md`)
- [x] WI-403 - Inline artifact pill integration (`WI-403-inline-artifact-pill-integration.md`)

### Stage 8 — Overall inline chat UI behaviour

| Stage | WI | Scope |
|-------|-----|--------|
| 8 — UI behaviour | WI-404 | Design gate — composer, splitter, host binding (`WI-404-inline-chat-ui-behaviour-design.md`) |
| 8 — UI behaviour | WI-405 | Inline composer General Chat style (`WI-405-inline-composer-general-chat-style.md`) |
| 8 — UI behaviour | WI-406 | Splitter resize limits (`WI-406-splitter-resize-limits.md`) |
| 8 — UI behaviour | WI-407 | Host context session binding (`WI-407-host-context-session-binding.md`) |

- [x] WI-404 - Inline chat UI behaviour design (`WI-404-inline-chat-ui-behaviour-design.md`)
- [x] WI-405 - Inline composer General Chat style (`WI-405-inline-composer-general-chat-style.md`)
- [x] WI-406 - Splitter resize limits (`WI-406-splitter-resize-limits.md`)
- [x] WI-407 - Host context session binding (`WI-407-host-context-session-binding.md`)

**Stage 8 manual checks:**

- Switch between two saved Analysis queries with existing sessions — drawer follows the active query.
- Select a query with no session — drawer hides; prior sessions remain in the session list.
- Inline composer: no separate footer box; matches General Chat chrome; autosizes.
- Drag splitter past 560px on a wide viewport (up to ~50% of content width).
- Navigate Analysis → Model (entity with session) — correct model session opens; navigate to host
  without session — drawer hides.

### Stage 7 — Backend profile (after WI-394)

| Stage | WI | Ready when | Depends on |
|-------|-----|------------|------------|
| 7 — Backend profile | WI-396 | WI-394 complete (WI-395 recommended first) | WI-393, WI-389 design contract |

## Verify

```bash
cd ui/mill-ui && npm run test -- --run
cd ui/mill-ui && npm run build
```

## Manual Verification

Run the UI in mock mode and inspect the first specialized inline-chat behavior before backend
integration:

```bash
cd ui/mill-ui
npm run dev
```

Manual checks:

- Open `/analysis`, select or create a query, and open the inline chat drawer from the Analysis
  header.
- Send a prose-only mock prompt and verify the drawer feels compact and readable next to the SQL
  editor.
- Send a prompt that produces a mock SQL proposal and verify it renders as an inline artifact strip,
  not as a full SQL/Data/Chart card.
- Verify the strip title, description, artifact type label, and Apply / Apply & Run / Copy actions
  are visible without crowding the drawer.
- Verify Analysis inline-chat settings are visible only when the inline chat exists for the current
  Analysis context.
- Toggle auto-apply and auto-run settings and verify they change Analysis behavior without changing
  the generic inline chat renderer.
- Apply a SQL proposal and confirm the editor changes while preserving an undo path.
- Use Apply & Run and confirm the query executes in the Analysis result area.
- Check desktop and narrow viewport widths for overlapping text, clipped controls, or unusable
  drawer density.

## Assumptions

- Analysis copilot uses a dedicated backend profile id named `analysis-copilot`.
- The UI may continue to use `inline-analysis` as the chat host/treatment key; that is distinct
  from the backend profile id and must not be treated as the copilot profile.
- SQL artifacts in the Analysis copilot are previewed first by default; auto-apply and auto-run are
  opt-in Analysis settings.
- Undo/redo is editor-level history, not artifact-level restore.
- The first implementation slice should prioritize manual UX evaluation over complete mock fidelity:
  users should be able to feel drawer sizing, artifact strip density, and Analysis workflow before
  backend work.
