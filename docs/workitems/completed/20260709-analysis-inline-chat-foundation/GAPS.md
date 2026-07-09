# Gaps and open decisions — analysis-inline-chat-foundation

**Story:** [`STORY.md`](STORY.md)  
**Status:** `in-progress` — WI-389 through WI-407 complete (story closure pending)  
**Last reviewed:** 2026-07-09 (Stage 8 inline UI behaviour)

This document records **gaps found during planning**, their **locked resolutions**, and **execution
readiness** by stage. All product/design decisions below are **locked** unless explicitly marked
otherwise in the resolution log.

**Terminology lock:** "inline chat" names the generic context-aware UI/session mechanism;
"Analysis copilot" names the Analysis-specific inline-chat functionality and backend profile
contract; `analysis-copilot` is the backend profile id; `inline-analysis` is only the frontend
host/rendering key.

**Execution status:** WI-389 through WI-396 complete. Story archive / MR-ready squash still pending
user direction. See [§14](#14-execution-stages-and-readiness).

---

## 0. Implementation completeness (baseline)

| Area | WI | Expected (story scope) | Current repo state |
|------|-----|------------------------|-------------------|
| Inline chat foundation design doc | WI-389 | `docs/design/ui/mill-ui/` | **Done** — `INLINE-CHAT-FOUNDATION.md` |
| Legacy inline-chat API removal | WI-390 | Hard remove legacy docs/code | **Done** — unified chat only in docs/backlog |
| Inline artifact strips | WI-391 | `inline-artifact-strip` mode + actions | **Done** |
| Mock Analysis copilot preview | WI-392 | Manual UX preview with fixtures | **Done** — keyword triggers + synthesized SQL |
| Context snapshot + profile routing | WI-393 | Per-turn `context.values` + `analysis-copilot` | **Done** |
| Analysis host behavior | WI-394 | Explicit apply, settings, undo/redo | **Done** |
| Tests + final docs | WI-395 | E2E + doc reconciliation | **Done** (foundation coverage; closeout refinements welcome) |
| Backend `analysis-copilot` profile | WI-396 | Implement YAML + context consumption | **Done** |

**Verdict:** Shared inline chat foundation is implemented through WI-396 (`analysis-copilot` backend
profile, context snapshots, artifact strips, Analysis host settings/apply/undo). Story archive /
MR squash still pending.

---

## 1. Auto-apply vs explicit apply — **LOCKED**

**Was:** Codebase auto-applies Analysis copilot SQL unconditionally (effects in
`InterleavedAssistantReply`, `MessageArtifactComposer`, `useInlineChatListener`, `hostIntegrations`).

**Locked:**

- Preview-first by default; remove unconditional auto-apply and markdown SQL extraction.
- Generic settings (host-interpreted):

| Setting | Key | Analysis label | Meaning |
|---------|-----|----------------|---------|
| Automation mode | `automation.mode` | Auto | `manual` — preview only; `apply` — auto-apply first SQL per turn; `run` — apply then run |

| automation.mode | Behavior |
|-----------------|----------|
| `manual` | Show proposal; user chooses Apply or Apply & Run on strips |
| `apply` | Auto-apply first SQL proposal per turn; user runs manually |
| `run` | Auto-apply first proposal, then execute |

`run` never executes without applying first. Strip actions remain visible in all modes so older
proposals stay actionable. Mode is per inline session (not global). Future: user-profile default.

**Owner:** WI-389 (design) → WI-394 (host apply) + WI-401 (automation radio).

---

## 2. Inline artifact strip vs `host-apply` — **LOCKED**

**Locked:**

- Presentation mode: `inline-artifact-strip` (replaces `host-apply` for `inline-analysis` SQL).
- Grouping: existing kinds (`sql-data-composite`, facet groups).
- Actions: `apply`, `apply-and-run`, `copy`; future host actions (e.g. `exclude`, `open-in-model`).
- Visual: quiet horizontal strip; identity left, actions right; click opens preview.
- Preview: **Mantine Popover** with read-only full artifact render; no duplicate action buttons.
- Composite SQL+data: strip shows SQL identity + optional status/row-count metadata; **no data grid**
  in drawer.

**Owner:** WI-389, WI-391.

---

## 3. Context snapshot wire contract — **LOCKED**

**Locked:**

```json
{
  "message": "Optimize this query",
  "context": {
    "values": {
      "sql.current": "select * from sales.orders",
      "sql.dialect": "CALCITE",
      "artifact.query.id": "q-123",
      "artifact.query.name": "Top orders",
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

| Topic | Locked decision |
|-------|-----------------|
| Transport | Optional `context.values` on `POST .../messages` |
| Version | Omit `context.version` unless a migration need appears |
| Host discriminator | None required in envelope |
| Persistence | **Ephemeral** — turn input for prompt/tool use; not stored on `Turn` in this story |
| Backend consumption | Out of story; documented for follow-up profile/capability work |
| Unsaved query | `contextId = '__analysis__'` remains chat identity; `context.values` carries live SQL |
| Columns | Names/types only — no cell values |
| Error | **String** `execution.last.error` for v1 |
| Dirty | Boolean `artifact.query.dirty`; include `sql.current` and saved identity keys |

**Owner:** WI-389 (contract) → WI-393 (frontend).

---

## 4. `analysis-copilot` profile — **LOCKED (backend out of story)**

**Locked:**

- Backend profile id: `analysis-copilot` (new isolated YAML, not a `data-analysis` variant).
- UI host key: `inline-analysis` only.
- Capabilities: conversation, `sql-query`, schema exploration, metadata authoring; **no chart** initially.
- Frontend hardcodes `analysis-copilot` for Analysis copilot create; ignore General Chat picker / `VITE_MILL_AI_PROFILE`.
- Mock mode accepts/synthesizes `analysis-copilot` before backend exists.

**Owner:** WI-389 (document); **WI-396** (implement).

---

## 5. Mock preview path — **LOCKED**

**Locked:**

- Purpose: manual UX review, not backend-perfect SSE simulation.
- Order: artifact strips (WI-391) before mock preview (WI-392).
- Mock strategy: **keyword triggers** in mock `sendMessage` for Analysis context (`optimize` → prose,
  `rewrite` / `proposal` → SQL strip fixture); optional **local synthesized artifact state** when
  that avoids fragile SSE mocking.
- Preview-only fixtures live under existing mock mode; no new feature-flag recipe in this story.
- General Chat mock unchanged.

**Owner:** WI-392.

---

## 6. Apply & Run semantics — **LOCKED**

**Locked:** Close prior `activeExecutionIdRef`; reset pagination; run full applied document (not
selection); mark dirty when SQL differs from saved; disable while `isExecuting`; keep applied SQL on
failure; show errors via existing query/SQL error pattern; validation where backend supports it.

**Owner:** WI-394.

---

## 7. Editor undo/redo — **LOCKED**

**Locked:** UI/session-level only; not persisted; resets after Save; chat apply = one undo step;
Apply & Run = one SQL undo step (results not reverted); toolbar Undo/Redo; prefer CodeMirror
transactions when practical.

**Owner:** WI-394.

---

## 8. Non-SQL artefacts in Analysis copilot — **LOCKED**

**Locked:**

- Non-SQL artifacts use `inline-artifact-strip` (not full drawer cards).
- Strip shows artifact type + title (minimum identity).
- Click → popover with full read-only artifact view.
- Optional artifact-specific `open in` action (e.g. open in model view); host-owned.
- `sql-data-composite`: SQL strip + lightweight status only; no embedded grid.

**Owner:** WI-389, WI-391.

---

## 9. Host action model — **LOCKED**

**Locked:** Extend `hostIntegrations.ts` (no new event bus).

- `registerInlineHostHandler(chatType, handler)` / `dispatchInlineHostAction(chatType, action)`
- Discriminated union: `sql.apply`, `sql.applyAndRun`, `sql.copy`; future e.g. `facet.exclude`
- Key: `ChatType` (e.g. `inline-analysis`); one handler per type
- Strips dispatch only; host owns mutations, run, settings, history
- Remove Analysis SQL mutation via `useInlineChatListener` prose extraction

**Owner:** WI-389, WI-394.

---

## 10. Multiple SQL proposals per turn — **LOCKED**

**Locked:**

- First SQL proposal = default for turn-level auto-apply / auto-run.
- All proposals render as strips; later proposals stay independently actionable.
- No auto-fallback to second proposal if first host handling fails.

**Owner:** WI-391.

---

## 11. Documentation and API drift — **LOCKED**

**Locked:** Hard remove legacy `POST /api/v1/inline-chat/messages` from docs, backlog, and any
remaining code/mocks. Unified chat (`POST /api/v1/ai/chats` + SSE messages + optional
`context.values`) is the **only** normative inline-chat transport. Normative foundation doc:
`docs/design/ui/mill-ui/` (WI-389 output).

**Owner:** WI-390 (removal), WI-395 (reconciliation).

---

## 12. Feature flags — **LOCKED (out of scope)**

**Locked:** Assume inline chat is enabled for implementation and manual verification in this story.
No flag presets, docs, or management changes here.

---

## 13. WI-396 backend implementation gaps — **LOCKED**

The backend profile WI is now in story scope, so these implementation gaps are locked before
starting WI-396:

### 13.1 Runtime context boundary

Current backend send boundaries are message-only:

- `SendMessageHttpRequest` accepts only `message`.
- `ChatService.sendMessage(chatId, message)` accepts only message text.
- `AiV3ChatRuntime.send(metadata, message)` accepts only metadata and message text.

**Locked:** WI-396 must keep context optional across both UI and server boundaries. The turn-send
contract is:

```text
sendMessage(chatId, message, context?)
```

where `context` is optional and, when present, carries `context.values` plus optional
`context.version`. Introduce an explicit turn-send model, such as `SendMessageContext` /
`TurnContextValues`, and pass it through controller -> service -> runtime. Do not hide
`context.values` parsing only in the HTTP DTO, and do not require context for General Chat or
non-contextual sends.

### 13.2 Prompt injection path

Current system prompt assembly is profile + capabilities only. It does not receive the turn context.

**Locked:** WI-396 must define a deterministic but minimal Analysis turn-context prompt section.
Do not dump the full `context.values` map into the prompt.

Default prompt injection should include only:

- sanitized `sql.current` when present
- bounded query title/name when present
- bounded query description when present
- bounded `execution.last.error` when present, because it helps the LLM correct human-written SQL

Dialect is backend-defined by the SQL capability/runtime. If the Analysis editor SQL does not match
the backend dialect, correction is part of the SQL rewrite/validation flow; client-supplied dialect
must not be treated as authoritative prompt context.

Query title/name and description are lightweight human context only; they are not authoritative
object state and must be length-limited before injection.

Other context values, such as query id, dirty state, execution summary, or columns, may be accepted
on the wire but should not be injected by default. A capability may use specific values only when it
has an explicit reason and tests cover that behavior. Raw result rows must never be injected.

### 13.3 Tool parameter mapping

Tools should not receive hidden or generic `context.values` just because the client supplied them.
Backend validators/tools are already backend-context aware where needed, such as SQL dialect.

**Locked:** WI-396 must rely on the LLM to map visible prompt context into explicit tool parameters
when a tool call needs that information. Do not pass raw `context.values` into
`ToolExecutionContext` by default. If a future capability requires direct backend access to a
specific context value, it must opt in with a typed parameter/adapter and tests.

### 13.4 Context safety limits

`context.values` is generic and user/client supplied.

**Locked:** WI-396 must enforce or document concrete limits before prompt/tool injection:

- each individual `context.values` value is limited to 4096 bytes (4 KB) after serialization or
  canonical string conversion
- values larger than 4 KB are truncated or ignored according to the implementation policy, but must
  not fail the whole send path unless malformed
- accepted column fields are `name` and `type` only
- unknown keys are tolerated but ignored
- nested/raw row-like values are never injected

### 13.5 Profile validation timing

Current chat create stores `profileId` without resolving it; unknown profiles can fail later on
send.

**Locked:** WI-396 must validate `profileId` at create time and fail unknown profiles immediately.
Do not allow chats with unknown profile ids to be created and fail later on send. Tests must cover
unknown profile rejection on create and successful create for `analysis-copilot`.

### 13.6 Structured SQL artifact evidence

Structured SQL output depends on model behavior.

**Locked:** WI-396 must use deterministic mocked tests for structured SQL artifact evidence. Mock
the model/runtime response or scenario harness so tests prove the `analysis-copilot` profile can
route a SQL artifact through the backend protocol into the existing structured artifact stream.
Live-LLM proof may remain optional/gated and must not be the only acceptance evidence.

### 13.7 Verification command accuracy

Root Gradle includes AI modules under `:ai:*`; scenario packs may live under `:ai:mill-ai-test`.

**Locked:** WI-396 verification should include the exact backend modules it changes, likely:

```bash
./gradlew :ai:mill-ai:test :ai:mill-ai-service:test
./gradlew :ai:mill-ai-test:testIT
```

Use `:ai:mill-ai:testIT` only if that task exists and is relevant.

**Owner:** WI-396.

---

## 14. Execution stages and readiness

Implementation sequence:

1. WI-389 — Design gate  
2. WI-390 — Legacy inline-chat API removal  
3. WI-391 — Artifact strip foundation  
4. WI-392 — Manual UX preview  
5. WI-393 — Context/profile plumbing  
6. WI-394 — Analysis host behavior  
7. WI-395 — Closeout tests and docs  

**All product/design gaps are locked.** Stages are blocked only by prerequisite WI completion.

### Stage readiness matrix

| Stage | WI | Ready to execute? | Blocking gaps | Prerequisite WIs |
|-------|-----|-------------------|---------------|----------------|
| 0 — Design gate | WI-389 | **DONE** | None | — |
| 1 — Legacy API removal | WI-390 | **DONE** | None (decisions locked) | WI-389 |
| 2 — Artifact strips | WI-391 | **DONE** | None (decisions locked) | WI-389, WI-390 |
| 3 — Mock UX preview | WI-392 | **DONE** | None (decisions locked) | WI-391 |
| 4 — Context/profile | WI-393 | **DONE** | None | WI-389, WI-390 |
| 5 — Host behavior | WI-394 | **DONE** | None | WI-391, WI-393 |
| 6 — Closeout | WI-395 | **DONE** | None | WI-390–WI-394 |
| 7 — Backend profile | WI-396 | **DONE** | None | WI-393, WI-394 (WI-395 recommended) |

### Dependency graph

```text
WI-389 (Design)
  └─ WI-390 (Legacy API removal)
      ├─ WI-391 (Artifact strips) ──► WI-392 (Mock UX preview)
      │                                └─► WI-394 (Host behavior) ──► WI-395 (Closeout) ──► WI-396 (Backend profile)
      └─ WI-393 (Context/profile) ────────────────────────────────────▲
```

### Parallelism after WI-389 + WI-390

- **Path A (UX-first):** WI-391 → WI-392 → WI-394  
- **Path B (plumbing):** WI-393 in parallel with WI-391 / WI-392  
- WI-394 needs WI-391; WI-393 should complete before or with WI-394 for full snapshot wiring

### Risk

Starting code WIs before WI-389 writes the design doc risks re-implementing the current `host-apply`
/ auto-apply conflict under a new name.

---

## 15. Testing — **LOCKED (planned in WI-395)**

Coverage targets for WI-395:

- Snapshot provider register/unregister on route change
- Profile resolution: Analysis copilot → `analysis-copilot`; General Chat unchanged
- Strip actions dispatch host actions (no direct editor mutation from renderer)
- Apply / Apply & Run / Undo / Redo with `QueryPlayground`
- Regression: General Chat `SqlDataCondensedPreview` unchanged
- Mock Analysis context SQL fixture / keyword triggers

---

## 16. Out-of-scope confirmations — **LOCKED**

- No backend profile/capability implementation in this story
- No Model/Knowledge specialized hosts beyond hooks/types
- No full artefact card / expand pane as default inline drawer content
- No automatic SQL overwrite unless `autoapply.enabled` is on
- No raw result rows in prompt context (metadata only)
- Chart inline deferred per [`chart-inline-deferred.md`](../../../design/agentic/charts/chart-inline-deferred.md)
- Feature-flag management is a separate story

---

## 17. Stage 8 — Inline UI behaviour — **LOCKED**

**Was:** Switching Analysis queries left a stale session visible; splitter capped at 560px; inline
composer used a separate bordered footer box.

**Locked:**

### Host binding (WI-407)

| Rule | Behaviour |
|------|-----------|
| Session key | `(contextType, contextId)` |
| Host has session | Activate session + open drawer |
| Host has no session | Close drawer (sessions retained) |
| Leave inline routes | Close drawer |
| Analysis | `QueryPlayground` binds `activeQueryId` |
| Model / Knowledge | URL route sync (`InlineChatRouteHostSync`) |

### Indicators

| UI | Shows when |
|----|------------|
| `InlineChatButton` red dot | Session for **this** host type + id |
| Sidebar chat icon | Session exists for row host (independent of drawer open) |

### Composer (WI-405)

Same pane as transcript; General Chat `composerSurfaceStyle`; `variant="inline"` autosize (max 5 rows).

### Splitter (WI-406)

`maxRightFraction={0.5}`; `minLeftPx={280}`; `minRightPx={260}`; clamp via `horizontalSplitPaneMath`.

**Owner:** WI-404 (design) → WI-405, WI-406, WI-407.

---

## Resolution log

| Date | Item | Resolution |
|------|------|------------|
| 2026-07-08 | Inline apply/run automation | Preview-first default; `autoapply.enabled` / `autorun.enabled`; autorun only after apply |
| 2026-07-08 | Inline artifact presentation | `inline-artifact-strip`; Mantine Popover preview; no drawer data grid |
| 2026-07-08 | Non-SQL artifact strips | Compact strip (type + title); popover full view; optional `open in` |
| 2026-07-08 | Multiple SQL proposals | First = automation target; all strips actionable; no auto-fallback |
| 2026-07-08 | Turn context wire | Optional `context.values`; ephemeral; string error; column metadata only |
| 2026-07-08 | Host action integration | Extend `hostIntegrations.ts`; typed actions; no event bus; remove prose SQL listener |
| 2026-07-08 | Legacy inline-chat API | Hard remove; unified chat only |
| 2026-07-08 | Feature flags | Assume enabled; out of scope |
| 2026-07-08 | Terminology | inline chat / Analysis copilot / `analysis-copilot` / `inline-analysis` |
| 2026-07-08 | Analysis copilot profile | `analysis-copilot`; conversation + sql-query + schema + metadata authoring; no chart |
| 2026-07-08 | Mock preview | Keyword triggers + local fixtures; strips before preview |
| 2026-07-08 | Apply & Run | Close session, reset pagination, full document, dirty, keep SQL on failure |
| 2026-07-08 | Editor undo/redo | Session-level; one step per apply; reset on Save |
| 2026-07-08 | WI-396 backend gaps | Runtime context boundary, minimal prompt injection, LLM-driven tool parameter mapping, safety limits, profile validation timing, deterministic SQL artifact evidence, and verification commands are locked before backend implementation. |
| 2026-07-08 | All gaps locked | No open product/design decisions remain; stages blocked by WI prerequisites only |
| 2026-07-09 | Applied SQL strip marker | Green dot tracks **last applied artifact key** (`artifactId` or message-local ordinal), not editor SQL equality — identical SQL in different turns stays distinct (`ed6b272d`) |
| 2026-07-09 | Inline chat auto-scroll | `buildChatScrollSignature` ignores facet `status`; prevents scroll jump on accept/reject without new message content |
| 2026-07-09 | Pill expand affordance | Chevron replaced with hover-only expand icon; pill left-aligned at chat reply width |
| 2026-07-09 | Stage 8 host binding | Per `(contextType, contextId)` drawer bind; route sync for model/knowledge; analysis via `activeQueryId` |
| 2026-07-09 | Stage 8 composer | Same-pane inline composer with General Chat chrome (`variant="inline"`) |
| 2026-07-09 | Stage 8 splitter | Fraction-based max right width (50%); removed 560px hard cap |
