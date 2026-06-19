# ai-chat-persistence

## Goal

Fix and enhance AI v3 chat persistence so that:

1. General chats survive mill-service restart when the database is left intact.
2. Created chats are owned by the authenticated user; list and access are scoped per user.
3. Ephemeral runtime artifacts (`sql.validation`, tool-emitted `sql.result`) are not written to `ai_artifact`; durable replay artifacts remain persisted.
4. Each transcript turn records which agent profile was active (profile-switch history survives reload).

---

## Cold start (from empty context)

**Audience:** implementer or agent with **no prior chat history** — everything needed to execute this story is below and in linked `WI-*.md` files.

### Branch and workspace

| Item | Value |
|------|--------|
| **Branch** | `git fetch origin && git checkout -b feat/ai-chat-persistence origin/dev` (or continue on `qpointz-ai-chat-persistence-incomplete` if already checked out) |
| **Working directory** | Repository root (`./gradlew`, `settings.gradle.kts`) |
| **Story folder** | `docs/workitems/in-progress/ai-chat-persistence/` (moved from `planned/` on first WI completion) |
| **Related story** | [`pgvector-flyway-extension`](../../completed/20260618-pgvector-flyway-extension/STORY.md) — WI-322 (V9), **closed 2026-06-18** |

### Preconditions

| Check | Detail |
|--------|--------|
| **JDK** | Java 21 |
| **Build** | `./gradlew` from repo root; AI modules also `./gradlew` from `ai/` when scoped |
| **Flyway baseline** | Latest SQL migration: [`V8__saved_queries.sql`](../../../persistence/mill-persistence/src/main/resources/db/migration/V8__saved_queries.sql). **V9** = WI-322 pgvector; **V10** = WI-317 `ai_chat` |
| **Dev DB** | Default mill-service uses H2; Postgres for pgvector profile / integration |
| **Docs location** | Work items under **`docs/workitems/`**; design under **`docs/design/agentic/`** |

### Root cause (restart bug)

- [`AiV3AutoConfiguration`](../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3AutoConfiguration.kt) registers **`InMemoryChatRegistry`** always.
- [`JpaChatRegistry`](../../../ai/mill-ai-persistence/src/main/kotlin/io/qpointz/mill/persistence/ai/jpa/adapters/JpaChatRegistry.kt) exists but is **never** a Spring bean in [`AiV3JpaConfiguration`](../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3JpaConfiguration.kt).
- [`UnifiedChatService.listChats()`](../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt) reads **registry only** — not `ConversationStore` / DB transcript.
- Split tables today: `ai_chat_metadata` (list) vs `ai_conversation` (turns) — see [WI-317](WI-317-jpa-chat-registry.md).

### Naming (today → target)

| Today | Target (WI-317) |
|-------|-----------------|
| `ai_chat_metadata` + `ai_conversation` (split, no FK) | **`ai_chat`** — single parent row on `createChat` |
| `ai_conversation_turn` | **`ai_chat_turn`** — FK `chat_id` → `ai_chat` CASCADE |
| `*.conversation_id` | **`chat_id`** on memory, artifacts, pointers |
| HTTP `chatId` vs DB `conversationId` | Align on **`chat_id` / `chatId`** |

### Locked decisions (do not re-debate)

| Topic | Decision |
|--------|-----------|
| **DB naming** | Unify on **`chat`** (`ai_chat`, `ai_chat_turn`, `chat_id`) — not `conversation` |
| **Per-turn profile** | **`profile_id`** on each `ai_chat_turn` row |
| **Profile lock** | Capture `profileId` at **`sendMessage` start**; user + assistant turns in one exchange share it (toolbar PATCH mid-stream does not split Q&A) |
| **Header vs turn profile** | `ai_chat.profile_id` = current for **next** message; turn rows = **immutable** history |
| **WI-322 pgvector** | Soft-fail if extension not installed on **Postgres**; **H2 never fails Flyway** (Java skip + catch-all); permission denied on Postgres → fail Flyway |
| **Orphan rows (V10 migrate)** | Backfill `user_id` from `mill.ai.chat.default-user-id`; turn `profile_id` from parent chat profile |
| **Ownership column** | `ai_chat.user_id` — no `created_by_username` in this story |
| **Ephemeral artifacts** | `persist: false` on descriptors; client attach `POST …/execution-result` stays durable |

### Work item order (locked)

| Seq | WI | Flyway | Depends | Deliverable |
|-----|-----|--------|---------|-------------|
| **1** | [WI-322](../../completed/20260618-pgvector-flyway-extension/WI-322-pgvector-extension-flyway-migration.md) | V9 | — | Optional `CREATE EXTENSION vector` (Java migration) |
| **2** | [WI-317](WI-317-jpa-chat-registry.md) | V10 | — | Unified schema + JPA `ChatRegistry` + per-turn profile |
| **3** | [WI-318](WI-318-auth-bound-user-ownership.md) | — | WI-317 | `SecurityUserIdResolver` + ownership (A-75) |
| **4** | [WI-319](WI-319-ephemeral-artifact-routing.md) | — | — | `persist: false` routing (parallel with 318 OK) |
| **5** | [WI-320](WI-320-integration-tests-chat-docs.md) | — | 317–319 | IT + agentic design docs |
| **6** | [WI-321](WI-321-capability-descriptor-documentation.md) | — | WI-319 | Capability YAML schema doc |

WI-322 and WI-317 are **independent** (V9 vs V10); seq 1–2 can swap.

### Bootstrap sequence

1. **WI-322** — See [pgvector STORY](../../completed/20260618-pgvector-flyway-extension/STORY.md) (closed). `./gradlew :persistence:mill-persistence:testIT`
2. **WI-317** — Flyway V10 + entities + `jpaChatRegistry` bean + turn writers + `TurnResponse.profileId`.  
   `./gradlew :persistence:mill-persistence:testIT :ai:mill-ai-autoconfigure:testIT :ai:mill-ai-service:testIT`
3. **WI-318** — `./gradlew :ai:mill-ai-service:test :ai:mill-ai-service:testIT`
4. **WI-319** — `./gradlew :ai:mill-ai:test :ai:mill-ai-test:testIT` (if scenarios touched)
5. **WI-320** — Full IT matrix + update `docs/design/agentic/*.md`
6. **WI-321** — Docs only under `docs/design/agentic/`

**Per WI:** mark `[x]` in tracking list below, update WI file if needed, **one commit** with full working copy ([RULES](../RULES.md)).

### Verification commands (story-level)

```bash
# From repo root — after WI-317+
./gradlew :persistence:mill-persistence:testIT
./gradlew :ai:mill-ai-autoconfigure:testIT
./gradlew :ai:mill-ai-service:testIT

# After WI-319
./gradlew :ai:mill-ai:test

# Optional manual (WI-320 checklist): mill-service + Postgres + mill-ui
# --spring.profiles.active=ai,security,oauth (or local equivalent)
```

### Commit prefix examples

- `[feat] WI-322: optional pgvector Flyway migration V9`
- `[feat] WI-317: unified ai_chat schema and JPA ChatRegistry`
- `[feat] WI-318: SecurityUserIdResolver and chat ownership`
- `[feat] WI-319: ephemeral artifact routing with persist flag`
- `[docs] WI-320: chat persistence IT and design docs`
- `[docs] WI-321: capability YAML descriptor documentation`

### Story closure ([RULES](../RULES.md))

**Closed 2026-06-19** — archived to [`completed/20260619-ai-chat-persistence/`](../completed/20260619-ai-chat-persistence/STORY.md).

---

## Context (summary)

### Behaviour gaps

- **`ChatRegistry` not JPA-wired** — sidebar list in-memory after restart
- **`UserIdResolver`** → `"default"` until WI-318; no ownership checks (BACKLOG **A-75**)
- **Profile switch** — only header updated; no per-turn column today
- **Artifact bloat** — `sql.validation`, tool `sql.result` persisted to `ai_artifact`

### Target schema summary

**`ai_chat`** — `chat_id`, `user_id`, `profile_id` (current), name, type, favorites, context fields, timestamps.

**`ai_chat_turn`** — `turn_id`, `chat_id`, **`profile_id`**, `role`, `text`, `position`, `urn`, `created_at`.

## Artifact policy (target)

| Kind | Live SSE | Persist to `ai_artifact` |
|------|----------|--------------------------|
| `sql.validation` | No | No |
| Tool `sql.result` | Yes | No |
| Client attach `sql.result` (`POST …/execution-result`) | N/A | Yes (GET replay) |
| `sql.generated`, facet, schema capture | Yes | Yes |

## Out of scope (follow-up)

- Denormalized `created_by_username` on `ai_chat`
- Per-message profile badges in mill-ui
- Historical artifact purge migration
- `relation_record` orphan cleanup on chat delete (see **ai-chat-table-naming** WI-324 notes)

**Delivered in follow-up story [`ai-chat-table-naming`](../completed/20260619-ai-chat-table-naming/STORY.md):** BACKLOG **A-76/A-77** — hard-delete cascade for transcript/artifacts/run events (**WI-323**, **WI-324**).

## Design references

- [`docs/design/agentic/v3-chat-service.md`](../../../design/agentic/v3-chat-service.md)
- [`docs/design/agentic/v3-conversation-persistence.md`](../../../design/agentic/v3-conversation-persistence.md)
- [`docs/design/agentic/v3-implementation-findings.md`](../../../design/agentic/v3-implementation-findings.md)
- [`docs/design/agentic/artifact-foundation.md`](../../../design/agentic/artifact-foundation.md)
- [`docs/design/agentic/v3-capability-manifest.md`](../../../design/agentic/v3-capability-manifest.md)

## Work Items (tracking list)

- [x] WI-322 — Optional pgvector Flyway Java migration ([`../../completed/20260618-pgvector-flyway-extension/WI-322-pgvector-extension-flyway-migration.md`](../../completed/20260618-pgvector-flyway-extension/WI-322-pgvector-extension-flyway-migration.md)) — **V9** (story closed)
- [x] WI-317 — Unified `ai_chat` schema, per-turn `profile_id`, JPA wiring ([`WI-317-jpa-chat-registry.md`](WI-317-jpa-chat-registry.md)) — **V10**
- [x] WI-318 — Auth-bound user and ownership checks ([`WI-318-auth-bound-user-ownership.md`](WI-318-auth-bound-user-ownership.md))
- [x] WI-319 — Ephemeral artifact routing ([`WI-319-ephemeral-artifact-routing.md`](WI-319-ephemeral-artifact-routing.md))
- [x] WI-320 — Integration tests and chat design docs ([`WI-320-integration-tests-chat-docs.md`](WI-320-integration-tests-chat-docs.md))
- [x] WI-321 — Capability descriptor format documentation ([`WI-321-capability-descriptor-documentation.md`](WI-321-capability-descriptor-documentation.md))
