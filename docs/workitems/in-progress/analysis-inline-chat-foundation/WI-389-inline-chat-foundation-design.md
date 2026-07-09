# WI-389 - Inline chat foundation design

## Goal

Document the reusable inline-chat architecture before implementing Analysis copilot behavior.

## Status

**Complete** — normative design: [`docs/design/ui/mill-ui/INLINE-CHAT-FOUNDATION.md`](../../../design/ui/mill-ui/INLINE-CHAT-FOUNDATION.md)

## Background

The existing UI already has shared inline-chat infrastructure and an Analysis chat trigger. This
work item formalizes how specialized inline chats should work so the Analysis copilot does not
become a one-off implementation.

## Requirements

- Define inline chat as a context-aware assistant attached to a host view.
- Define terminology:
  - inline chat is the generic context-aware UI/session mechanism
  - Analysis copilot is the Analysis-specific inline-chat functionality
  - `analysis-copilot` is the backend profile id for Analysis copilot
  - `inline-analysis` is the frontend host/rendering key, not a backend profile id
- Capture the reusable responsibilities:
  - context snapshot collection
  - inline profile routing
  - host-interpreted inline chat settings
  - inline artifact strip rendering
  - host action dispatch
  - host-owned application of generated artifacts
- Define the typed host action model by extending the existing `hostIntegrations.ts` API rather
  than introducing a new event bus:
  - replace the single-purpose `registerHostApplyHandler` / `applyArtifactToHost` shape with a
    typed registry such as `registerInlineHostHandler(chatType, handler)` and
    `dispatchInlineHostAction(chatType, action)`
  - key handlers by `ChatType` / frontend host key, such as `inline-analysis`
  - use discriminated action payloads, starting with `sql.apply`, `sql.applyAndRun`, and
    `sql.copy`
  - allow future context-specific actions such as `facet.exclude`
  - host handlers own all mutations and async work; artifact renderers only dispatch actions
  - dispatch returns handled/unhandled status for UI fallback
- Define that this typed host action model supersedes silent `host-apply` behavior for Analysis.
- Define that Analysis markdown SQL extraction via `useInlineChatListener` is not the intended
  apply path after this story; SQL application must come from artifact strip host actions.
- Define Stage 0 as the design gate for all implementation WIs:
  - WI-390 must align legacy API cleanup with the unified chat transport defined here.
  - WI-391 and WI-392 must not invent artifact-strip or host-action contracts outside this design.
  - WI-393 must use the context wire and Analysis snapshot keys defined here.
  - WI-394 must use the apply/run/settings/undo semantics defined here.
- Define the specialized inline artifact presentation mode as `inline-artifact-strip`.
- Define the visual intent for `inline-artifact-strip`:
  - compact, recognizable, and quiet
  - one horizontal strip per artifact proposal
  - left side shows artifact identity such as `<Query title>.sql` or `Facet type: <title>`
  - right side shows a small set of context-specific actions
  - clicking the strip can open a popover/overlay preview
  - preview may reuse the full artifact renderer in read-only mode, without primary action buttons
- Define non-SQL artifact strip treatment for Analysis copilot:
  - non-SQL artifacts render as compact strips, not full drawer cards
  - strip identity is minimal: artifact type plus title when available
  - clicking opens a popover/overlay with the full artifact view
  - an `open in` action may appear when the artifact has a natural destination, such as Model view
  - `open in` destinations are artifact-specific and host-owned
  - `sql-data-composite` does not embed a data grid in the drawer strip
- Define a generic settings model for inline chats:
  - settings are owned by the inline chat/session foundation
  - each host decides which settings are visible
  - each host decides what a setting means for its context
  - unsupported settings are ignored by that host
- Include initial setting keys:
  - `autoapply.enabled`
  - `autorun.enabled`
- Define Analysis copilot as the first specialized inline-chat behavior on the Analysis host.
- Define Analysis copilot interpretation:
  - `autoapply.enabled` applies SQL proposals to the editor when they arrive
  - `autorun.enabled` runs the query after an applied SQL proposal
  - `autorun.enabled` has no effect unless SQL is applied by explicit action or by auto-apply
  - Analysis setting controls are visible only when an inline chat exists for the current Analysis
    context
- Define Analysis user-facing labels:
  - `autoapply.enabled` -> "Auto-apply SQL proposals"
  - `autorun.enabled` -> "Run after applying"
- Define the Analysis behavior matrix:
  - auto-apply off, auto-run off: show proposal; user chooses Apply or Apply & Run
  - auto-apply off, auto-run on: show proposal; user must apply; Apply also runs
  - auto-apply on, auto-run off: apply proposal automatically; user runs manually
  - auto-apply on, auto-run on: apply proposal automatically, then run
- Define the turn context wire contract:
  - send-message accepts optional `context`
  - `context.values` is a generic `Record<string, unknown>` supplied by the UI consumer
  - `context.version` is optional and should be omitted unless a concrete migration need appears
  - the envelope must not require a host/view `type` discriminator such as `analysis`
  - unknown keys are tolerated
  - context is turn input, not canonical object state
  - profile/capability code owns interpretation and prompt/tool injection
- Define the Analysis context keys at behavior level:
  - current SQL text
  - SQL dialect when known
  - active saved-query id, name, description, and dirty state
  - last execution id/status when present
  - result columns and row count when present
  - current execution or SQL error summary when present
  - no raw result rows
- Define the Analysis copilot backend profile contract:
  - profile id: `analysis-copilot`
  - new isolated profile, not a prompt variant of `data-analysis`
  - must reason primarily around the supplied query/context values
  - starting capabilities: conversation, `sql-query`, schema exploration, and metadata authoring
  - chart capabilities are out of the starting profile scope
- Define the Analysis Apply & Run policy:
  - close the prior execution session before running
  - reset pagination to page 1/default page size
  - run the full applied SQL document, not a selected fragment
  - mark the editor dirty when applied SQL differs from the saved snapshot
  - disable Apply & Run while execution is in progress
  - keep applied SQL if execution fails
  - show failures with the existing Analysis/query error pattern
- Define editor undo/redo scope:
  - UI/session-level only
  - not persisted
  - resets after Save
  - chat apply is one SQL undo step
  - Apply & Run undo does not revert execution results
- Define the mock/manual preview policy:
  - the mock preview exists to evaluate user interaction and visual feel
  - mock behavior may be incomplete when complete simulation would be fragile
  - prefer local fixtures or synthesized proposal state over brittle backend emulation
- Explicitly record that backend profile/capability implementation is out of scope for this story.

## Acceptance Criteria

- A design document under `docs/design/ui/mill-ui/` explains the inline chat foundation.
- The design consistently uses inline chat for the generic mechanism, Analysis copilot for the
  Analysis-specific functionality, `analysis-copilot` for the backend profile id, and
  `inline-analysis` only for the frontend host/rendering key.
- The design distinguishes General Chat artifacts from inline artifact strips.
- The design defines `inline-artifact-strip` as the specialized inline artifact presentation mode.
- The design defines compact non-SQL artifact strips for Analysis copilot, including minimal
  type/title identity, popup full view, and optional artifact-specific `open in` action.
- The design defines the Analysis context snapshot fields and host actions at behavior level.
- The design defines typed host actions as an extension of `hostIntegrations.ts`, not a new event
  bus, and explicitly supersedes silent `host-apply` plus markdown SQL extraction for Analysis SQL
  application.
- The design defines the exact generic `context.values` send-message contract and states that
  `context.version` is optional.
- The design defines the generic inline settings model and Analysis interpretation for
  `autoapply.enabled` and `autorun.enabled`.
- The design documents default-off settings, user-facing labels, and the Analysis behavior matrix.
- The design documents `analysis-copilot` profile expectations and distinguishes backend profile id
  from UI host key `inline-analysis`.
- The design documents Apply & Run semantics and UI/session-only undo/redo semantics.
- The design documents mock preview goals and explicitly allows incomplete mocks to avoid fragile
  implementation.
- The design lists future extension points for Model and Knowledge inline chats.
- No production code is changed by this WI except documentation references if needed.

## Verification

- Review the design document for consistency with `GENERAL-CHAT-DESIGN.md`,
  `ARCHITECTURE.md`, and the current `ui/mill-ui` inline chat implementation.
