# Gaps and open decisions — context-relations

**Story:** [`STORY.md`](STORY.md)  
**Status:** `planned` — WI-408 not started  
**Last reviewed:** 2026-07-09

---

## 0. Known gaps (input from other stories)

### Analysis inline chat persistence (from `analysis-inline-chat-foundation`)

| Gap | Today | Target owner |
|-----|--------|----------------|
| UI session list / red dot after reload | Lost — `InlineChatContext` in-memory only | WI-408+ restore via relations + `getChatByContext` |
| Drawer open state after reload | Lost (split width only in `localStorage`) | Optional: persist preference or derive from “has active contextual chat” |
| Copilot `automation.mode` per session | In-memory `InlineChatSession.settings` | TBD — user profile default vs chat metadata vs relation payload |
| Query ↔ chat link | Implicit `ai_chat.context_id` = `saved_query.id` after first message | Explicit `has-contextual-chat` relation (or documented mirror of `ai_chat`) |
| Related conversations popover | `chatReferencesService` mock | Unified relation read API |
| Related content pills | `relatedContentService` mock | Unified relation read API |

---

## 1. Open decisions (resolve in WI-408)

| # | Topic | Options | Status |
|---|--------|---------|--------|
| G-1 | Relation store location | New `mill-relations` module vs extend `mill-persistence` vs projection table beside `ai_chat` | **Open** |
| G-2 | ObjectRef encoding in API | Path-based (`/objects/analysis/queries/{id}`) vs typed JSON body | **Open** |
| G-3 | Contextual chat relation vs `ai_chat` columns | Relation as read model only vs dual-write on `createChat` | **Open** |
| G-4 | Session settings persistence | Chat metadata JSON vs separate relation attributes vs defer | **Open** |
| G-5 | Merge with `concept-object-relations` | Shared relation table vs separate concept-link projection feeding same read API | **Open** |

---

## Resolution log

| Date | Item | Resolution |
|------|------|------------|
| 2026-07-09 | Story created | Deferred analysis inline chat **UI presence** persistence from inline-chat foundation |
