# WI-232 — Tests and verification (mill-ui general chat)

Status: `done`  
Type: `🧪 test` / `✅ chore`  
Area: `ui`  
Story: [`STORY.md`](STORY.md) — **run last** for the wired **229–231** stack.

## Depends on

- **WI-229**–**WI-231** (code + scaffolding under test).

## Reviewer checklist

- Paste **`npm run test`** / **`npm run build`** summary (mill-ui root) into MR.
- **`WI-233`**: MR description or **`WI-233`** checklist shows **metadata + AI** owner **review** (**ack name + date**) — design may land **same MR** as **WI-231** or **adjacent**.
- Manual smoke bullets from acceptance criteria exercised or explicitly **skipped** with reason; **`viewChat`** (**General Chat**) on in defaults (**`WI-230`**).

## Goal

Lock in **`WI-229–231`** with automated tests and a short manual checklist; capture **sign-off linkage** for **`WI-233`** design (no bespoke runtime tests until metadata scope APIs exist unless stubbed elsewhere).

## Acceptance criteria

- [x] Vitest: [`chatService.test.ts`](../../../../ui/mill-ui/src/services/__tests__/chatService.test.ts) (and siblings) — fetch mocks for JSON endpoints; SSE stream fixture using **today’s** event shapes (`item.diagnostic`, optional `item.tool.call`/`result`, text `item.part.updated`, `completed`/`failed`) — assert **thinking / progress updates** clear appropriately; verify **no dependency** on hypothetical new SSE `type` strings
- [x] Vitest: assert **`createChat`** body includes **`profileId`** when the UI supplies one; **`listAgentProfiles`** mock returns multiple ids when testing picker helpers
- [x] **`npm run test`** and **`npm run build`** pass in `ui/mill-ui` (**evidence below, 2026-05-05**)
- [ ] Manual: mill-service + `mill.ai.enabled`, UI against proxied `/api` — confirm **`viewChat`** is **enabled** in resolved flags (defaults + remote merge) so **`/chat`** is reachable; verify **`credentials: 'include'`** on **`GET /api/v1/ai/profiles`** and all chat calls when security is on; **list**, open, stream, rename, delete; security smoke; **`item.diagnostic`** wait feedback (profile that emits diagnostics); concise tool cues; **`GET /api/v1/ai/profiles`** sanity; with **`chatAgentPicker`** **on**: create two chats **different `profileId`** when ≥2 profiles exist and confirm detail metadata differs; flag **off** → env/server default unchanged; optionally `./gradlew :ai:mill-ai-v3-cli:run` — **MR author / QA before prod**

- [ ] **`WI-233`** design note **peer-reviewed** by metadata + AI owners — checkboxes at bottom of [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) (fill **name + date** before merge)

## Evidence for MR

**Automated (2026-05-05, `ui/mill-ui`):**

- `npm run test -- --run` — **Test Files 53 passed (53)**, **Tests 474 passed (474)**.
- `npm run build` — `tsc -b && vite build` succeeded (Vite production client build).

**Manual / owner ack:** complete the remaining acceptance checkboxes above before release; paste smoke notes and reviewer signatures into the MR description.
