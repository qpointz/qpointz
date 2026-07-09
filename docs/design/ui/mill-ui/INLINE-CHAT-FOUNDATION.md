# Inline chat foundation

Architecture and contracts for context-aware inline chat in **mill-ui**. General Chat design and
transport details live in [GENERAL-CHAT-DESIGN.md](GENERAL-CHAT-DESIGN.md) and
[ai-v3-chat-transport-extensions.md](../../agentic/ai-v3-chat-transport-extensions.md).

**Story:** [`analysis-inline-chat-foundation`](../../../workitems/completed/20260709-analysis-inline-chat-foundation/STORY.md)

---

## Terminology

| Term | Meaning |
|------|---------|
| **Inline chat** | Generic context-aware chat UI/session mechanism attached to a host view |
| **Analysis copilot** | Analysis-specific inline-chat functionality and backend profile contract |
| **`analysis-copilot`** | Backend profile id for the Analysis copilot |
| **`inline-analysis`** | Frontend host/rendering key only â€” **not** a backend profile id |

---

## Transport (normative)

Inline chat uses the **unified AI v3 chat API** only:

| Operation | Endpoint |
|-----------|----------|
| Create contextual chat | `POST /api/v1/ai/chats` with `contextType`, `contextId`, `contextLabel`, `contextEntityType`, `profileId` |
| Resolve by context | `GET /api/v1/ai/chats/context-types/{contextType}/contexts/{contextId}` |
| Send message (SSE) | `POST /api/v1/ai/chats/{chatId}/messages` |
| Hydrate transcript | `GET /api/v1/ai/chats/{chatId}` |

The legacy `POST /api/v1/inline-chat/messages` endpoint was **intentionally removed** (hard removal,
not deprecation). `InlineChatContext` and all inline hosts call `chatService` â€” the same service as
General Chat.

See [v3-chat-service.md](../../agentic/v3-chat-service.md) for server behaviour.

---

## Reusable responsibilities

Each specialized inline chat (Analysis copilot first; Model and Knowledge later) shares:

1. **Context snapshot collection** â€” host supplies per-turn metadata via `context.values`
2. **Inline profile routing** â€” host chooses backend `profileId` at create time
3. **Host-interpreted settings** â€” generic keys; each host decides visibility and semantics
4. **Inline artifact strip rendering** â€” compact drawer presentation (`inline-artifact-strip`)
5. **Host action dispatch** â€” typed actions from strips to host handlers
6. **Host-owned application** â€” mutations, run, undo, and settings live in the host view

---

## Context snapshot wire contract

Send-message accepts optional context:

```json
{
  "message": "Optimize this query",
  "context": {
    "values": {
      "sql.current": "select * from sales.orders",
      "sql.dialect": "CALCITE",
      "artifact.query.id": "q-123",
      "artifact.query.name": "Top orders",
      "artifact.query.description": "High-value customer order review",
      "artifact.query.dirty": true,
      "execution.last.id": "exec-1",
      "execution.last.status": "completed",
      "execution.last.rowCount": 100,
      "execution.last.columns": [{ "name": "country", "type": "STRING" }],
      "execution.last.error": "Syntax error near FROM"
    }
  }
}
```

| Topic | Decision |
|-------|----------|
| Transport | Optional `context.values` on `POST .../messages` |
| Boundary | `sendMessage(chatId, message, context?)` on UI and server sides |
| Version | Omit `context.version` unless a migration need appears |
| Host discriminator | None required in envelope |
| Persistence | **Ephemeral** — turn input for prompt use; not stored on `Turn` in foundation story |
| Unsaved query | `contextId = '__analysis__'` remains chat identity; `context.values` carries live SQL |
| Columns | Names/types only — no cell values |
| Error | String `execution.last.error` for v1 |
| Dirty | Boolean `artifact.query.dirty`; include `sql.current` and saved identity keys |
| Size limit | Each individual `context.values` value is limited to 4096 bytes |

Backend consumption of `context.values` remains optional. General Chat and non-contextual sends must
work without context.

### Backend prompt consumption

Analysis copilot backend prompt injection is intentionally minimal. Do not dump the full
`context.values` map into the prompt.

Default prompt context:

- sanitized `sql.current` when present
- bounded query title/name when present
- bounded query description when present
- bounded `execution.last.error` when present, because it helps correct human-written SQL

Dialect is backend-defined by the SQL capability/runtime. Client-supplied dialect can be accepted on
the wire, but it is not authoritative prompt context.

Query title/name and description are lightweight human context only. They are not authoritative
object state and must be length-limited before injection.

Tools do not receive hidden/raw `context.values` by default. The LLM maps visible prompt context
into explicit tool parameters when calling tools. Future direct backend access to a context value
requires a typed opt-in adapter and tests.

---
## Inline artifact strips

General Chat uses full artifact cards (condensed/expand). Inline chat uses **`inline-artifact-strip`**
— a specialized presentation mode built on **`InlineArtifactPillStrip`**:

- Single-line pill row (~30px): colored **type badge** (SQL, DQ, C, AI, M) + **headline** + chevron
- **Strip icons:** host-actionable (Apply, Apply & Run, Open in model) or copilot-effect (Accept, Reject)
- **Popover:** read-only body + secondary actions (**Copy** only for SQL/facet — not on the pill)
- **`sql-data-composite`:** SQL identity in headline; optional description in popover only; **no data grid** in drawer

Action placement is enforced by `stripActionsForInline` / `popoverActionsForInline` in
[`inlineArtifactActionPlacement.ts`](../../../ui/mill-ui/src/components/chat/artifactPreview/inlineArtifactActionPlacement.ts).
Strip actions stay visible in all automation modes so older proposals remain actionable.

### Actions (Analysis copilot, SQL)

| Action | Host payload |
|--------|--------------|
| Apply | `sql.apply` |
| Apply & Run | `sql.applyAndRun` |
| Copy | `sql.copy` |

Future host actions (e.g. `facet.exclude`, `open-in-model`) are artifact- and host-specific.

### Multiple SQL proposals per turn

- First SQL proposal = default for turn-level auto-apply / auto-run
- All proposals render as strips; later proposals stay independently actionable
- No auto-fallback to a second proposal if first host handling fails

### Non-SQL artifacts (Analysis)

- Compact facet pills (type badge + headline), not full drawer cards
- Strip: Accept, Reject, Open in model — same `chatService.acceptArtifact` / `rejectArtifact` as General Chat (`useFacetProposalLifecycle`)
- Popover: read-only facet body + Copy

This mode **supersedes** silent `host-apply` for Analysis SQL. Markdown SQL extraction via
`useInlineChatListener` is **not** the intended apply path.

---

## Host action model

Extend [`hostIntegrations.ts`](../../../ui/mill-ui/src/components/chat/artifactPreview/hostIntegrations.ts)
â€” no new event bus:

```typescript
registerInlineHostHandler(chatType, handler)
dispatchInlineHostAction(chatType, action) â†’ { handled: boolean }
```

- Key: `ChatType` / frontend host key (e.g. `inline-analysis`)
- Discriminated union: `sql.apply`, `sql.applyAndRun`, `sql.copy`; future e.g. `facet.exclude`
- One handler per `chatType`
- Strips dispatch only; host owns mutations, run, settings, history
- Dispatch returns handled/unhandled for UI fallback

---

## Inline chat settings (host-interpreted)

Settings are owned by the inline chat/session foundation. Each host decides which settings are
visible and what they mean. Unsupported settings are ignored.

| Key | Analysis label | Meaning |
|-----|----------------|---------|
| `automation.mode` | Auto | `manual` — preview; `apply` — auto-apply first SQL per turn; `run` — apply then run |

Default: **`manual`**.

| automation.mode | Behavior |
|-----------------|----------|
| `manual` | Show proposal; user chooses Apply or Apply & Run |
| `apply` | Auto-apply first SQL proposal per turn; user runs manually |
| `run` | Auto-apply first proposal, then execute |

`run` never executes without applying first. Strip icon actions stay visible in all modes.
Analysis setting controls are visible only when an inline chat exists for the current Analysis
context. Mode is stored per `InlineChatSession` today (not a global user preference).

---

## Interaction behaviour (Stage 8)

### Host session binding

Each inline host is keyed by **`(contextType, contextId)`**. When the user focuses a host:

- If a session exists for that key, the runtime **activates** it and **opens** the drawer.
- If no session exists, the drawer **closes** without destroying other sessions.
- Leaving inline-capable routes (`/chat`, overview, etc.) closes the drawer.

| Host | Binding source |
|------|----------------|
| Analysis | `QueryPlayground` — `activeQueryId` (or `__analysis__` for unsaved) |
| Model | URL path → dotted entity id (`/model/sales/customers` → `sales.customers`) |
| Knowledge | URL path → concept id |

`getSessionByContext(contextType, contextId)` is the canonical lookup; `getSessionByContextId` is
deprecated for hosts that can share ids across types.

### Session indicators

- **Toolbar / header red dot** on `InlineChatButton`: session exists for the **current** host only.
- **Sidebar teal chat icon**: session exists for that row's host; not tied to drawer open state.

### Composer

The inline drawer composer shares the transcript pane (no separate bordered footer). It reuses
General Chat surface styling (`composerSurfaceStyle`, floating gradient) via `ChatInputBox`
`variant="inline"` — minimal initial height, autosize up to five rows.

### Resizable split

When the drawer is open, `HorizontalSplitPane` divides main content and the embedded drawer. The right
pane may grow up to **50%** of usable width (`maxRightFraction={0.5}`), with `minLeftPx={280}` and
`minRightPx={260}`. Persisted width is clamped on load via `horizontalSplitPaneMath`.

---

## Analysis copilot (first specialization)

### Backend profile contract

| Field | Value |
|-------|-------|
| Profile id | `analysis-copilot` |
| Isolation | New YAML profile, not a `data-analysis` variant |
| Capabilities | conversation, `sql-query`, schema exploration, metadata authoring |
| Out of scope initially | chart capabilities |
| Status | **Implemented** (WI-396) in `platform-agent-profiles.yaml` |

Frontend hardcodes `analysis-copilot` for Analysis copilot create; ignores General Chat picker /
`VITE_MILL_AI_PROFILE`. Mock mode accepts/synthesizes `analysis-copilot`.

Chat create validates `profileId`; unknown profiles fail immediately. Per-turn `context.values` are
ephemeral prompt input for `analysis-copilot` (not persisted on durable turns).

### Apply & Run policy

- Close prior execution session before running
- Reset pagination to page 1 / default page size
- Run full applied SQL document, not a selected fragment
- Mark editor dirty when applied SQL differs from saved snapshot
- Disable Apply & Run while execution is in progress
- Keep applied SQL if execution fails
- Show failures with existing Analysis/query error pattern

### Editor undo/redo

- UI/session-level only; not persisted; resets after Save
- Chat apply = one SQL undo step
- Apply & Run = one SQL undo step (results not reverted)
- Toolbar Undo/Redo; prefer CodeMirror transactions when practical

---

## Mock / manual preview policy

The mock preview exists to evaluate drawer sizing, strip density, and workflow â€” not backend-perfect
SSE simulation. Prefer **keyword triggers** and **local synthesized artifact state** over brittle
backend emulation. General Chat mock behaviour is unchanged.

---

## Future extension points

| Host | Context keys (examples) | Profile (TBD) | Actions (examples) |
|------|-------------------------|---------------|-------------------|
| Model (`inline-model`) | entity id, facet selection | TBD | `facet.exclude`, `open-in-model` |
| Knowledge (`inline-knowledge`) | concept id, definition excerpt | TBD | `open-in-knowledge` |

Reuse: snapshot provider registry, profile routing hook, strip renderer, host action registry,
settings shell.

---

## Related documents

| Document | Role |
|----------|------|
| [GENERAL-CHAT-DESIGN.md](GENERAL-CHAT-DESIGN.md) | Shared `ChatService`, SSE, General Chat UX |
| [chat-artefact-architecture.md](../../ai/chat-artefact-architecture.md) | Artifact wire shapes and replay |
| [ARCHITECTURE.md](ARCHITECTURE.md) | mill-ui module layout |
| [BACKEND-API-REQUIREMENTS.md](BACKEND-API-REQUIREMENTS.md) | REST/SSE expectations per domain |


