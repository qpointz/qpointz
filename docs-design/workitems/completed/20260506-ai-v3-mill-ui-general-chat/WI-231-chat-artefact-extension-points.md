# WI-231 — Chat artefact transport extension points

Status: `done`  
Type: `📐 design` / `✨ feature` (scaffolding only)  
Area: `ui`, `ai`  
Story: [`STORY.md`](STORY.md) — may proceed in parallel with **WI-233** doc; SSE hook assumes **WI-229** reducer integration.

## Depends on

- **WI-229** (centralised SSE parsing / **`onProgress`** hook).
- Coordination with **WI-233** (normative **metadata facet promotion** wording lives in **`ai-v3-chat-metadata-scope.md`** — **WI-231** only **cross-links**).

## Reviewer checklist

- Design doc **`docs/design/agentic/ai-v3-chat-transport-extensions.md`** (**or** an approved GENERAL-CHAT section) explicitly mentions **facet proposals** pointing to **`WI-233`** (avoid duplicating scope rules).
- Kotlin **KDoc** (if touched) and mill-ui behaviour **align with** [`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt): **forward-compatible** consumers — unknown `presentation` / `partType` on **`item.part.updated`** **do not** abort the session; text UI ignores non-text known-unknowns and may forward to **`extensionPayload`** (**not** “strict fail” — see **`STORY.md`** layer 1).
- **TS** **`chatTransport`** (or equivalent) is **additive** — default text UI unchanged; optional dev logging for forwarded structured events.

## Goal

Document and reserve **extension points** so SQL, metadata, charts, and other outputs can become **durable chat artefacts** without rewriting the REST client:

- Single design note: stream shape ≈ replay shape; `itemId` linkage; persistence sketch (`TurnResponse` / JPA evolution **design only**)
- Kotlin doc pointer on reserved `presentation` / `partType`
- TypeScript transport types + `reduceV3SseEvent` (or equivalent) with `extensionPayload` / optional `onStructuredPartDraft`

**No** execute-SQL API implementation, **no** DB migration, **no** chart/SQL UI in this WI.

## Acceptance criteria

- [x] Add **`docs/design/agentic/ai-v3-chat-transport-extensions.md`** (or approved section under [GENERAL-CHAT-DESIGN.md](../../../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md)) covering:
  - artefact model (live SSE vs durable replay)
  - mapping target parts (`SqlPart`, `DataPart`, `ChartPart`, metadata-oriented parts including **facet proposals**) ↔ planned SSE fields
  - persistence sketch (JSON column vs side table; link to `itemId`)
  - **Cross-reference** conversation **metadata facet** lifecycle and **chat-scoped** promotion — normative detail in **`WI-233`** / [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) (path as created)
  - candidate follow-up command API (name TBD, e.g. execute-sql action) — open questions only
  - **Forward compatibility:** mill-ui text client matches [`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) — **unknown** `presentation` / `partType` on **`item.part.updated`** → **no** main-bubble text merge, optional **`extensionPayload`**; **do not** abort the stream for benign server extensions (**`STORY.md`** layer 1)
- [x] KDoc tweak (only if justified) on [`ChatSseEvent.kt`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) **`ItemPartUpdated`**: cite file-level note — **frozen V1 text** vs **reserved future** structured `presentation` / `partType`; **consumers that only render conversation text should ignore unsupported combinations without failing** (mirror **`ItemToolCall`** guidance). Pointer to **`ai-v3-chat-transport-extensions.md`**. **Do not** document “fail on unknown partType” for thin UIs.
- [x] **[`ui/mill-ui/src/types/chatTransport.ts`](../../../../ui/mill-ui/src/types/chatTransport.ts)** (or agreed name): current SSE narrow types + `@future` stubs for artefact payloads; SSE loop exposes extension hook (**no-op** unless dev logging)
- [ ] Optional: feature-flagged placeholder UI branch for non-null structured payload (**off** by default) — deferred; `onNonTextPartUpdated` + transport helpers suffice for this story wave.

## Suggested follow-up backlog labels

`ai-v3-chat-execute-sql`, `ai-v3-chat-chart`, `ai-v3-structured-sse`, `ai-v3-chat-artefacts-persistence`, `ai-v3-chat-metadata-scope`, `ai-v3-chat-scope-facet-lifecycle`
