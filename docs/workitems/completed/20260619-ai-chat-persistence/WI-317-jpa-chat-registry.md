# WI-317 — Unified `ai_chat` schema and JPA wiring

## Cold start

| Field | Value |
|-------|--------|
| **Story** | [ai-chat-persistence/STORY.md](STORY.md) — seq **2** (after optional WI-322) |
| **Depends on** | None (WI-322 V9 is independent) |
| **Flyway** | **`V10__ai_chat_unified.sql`** — do **not** use V8/V9 (taken) |
| **Commit** | `[feat] WI-317: unified ai_chat schema and JPA ChatRegistry` |

### Verify when done

```bash
./gradlew :persistence:mill-persistence:testIT
./gradlew :ai:mill-ai-autoconfigure:testIT
./gradlew :ai:mill-ai-service:testIT
./gradlew :ai:mill-ai:test --tests "*InMemory*"
```

### Primary files to touch

| Area | Path |
|------|------|
| Migration | `persistence/mill-persistence/src/main/resources/db/migration/V10__ai_chat_unified.sql` |
| Entities | `ai/mill-ai-persistence/.../entities/` → `ChatEntity`, `ChatTurnEntity`; remove/replace `ChatMetadataEntity`, `ConversationEntity`, `ConversationTurnEntity` |
| Stores | `JpaChatRegistry.kt`, `JpaConversationStore.kt`, repos |
| Bean wiring | `ai/mill-ai-autoconfigure/.../AiV3JpaConfiguration.kt` — **`jpaChatRegistry` `@Bean`** |
| Fallback | `ai/mill-ai-autoconfigure/.../AiV3AutoConfiguration.kt` — keep `@ConditionalOnMissingBean` |
| Domain | `ai/mill-ai/.../ConversationStore.kt` — `ConversationTurn.profileId` |
| Runtime | `LangChain4jAgent.kt`, `SchemaExplorationAgent.kt`, `StandardPersistenceProjector.kt` |
| Profile lock | `UnifiedChatService.sendMessage` + runtime session — lock `profileId` at send |
| API | `ai/mill-ai-service/.../dto/ChatDtos.kt` — `TurnResponse.profileId` |
| IT | `FlywayMigrationIT.kt`, `AiV3JpaAutoConfigurationIT.kt`, `AiChatControllerIT.kt` |

### Implementation checklist

1. Write V10 migration (create `ai_chat`, `ai_chat_turn`, rename `conversation_id` → `chat_id`, data copy, drop old tables).
2. Refactor JPA entities/repos/adapters to new table names.
3. Register **`JpaChatRegistry`** in `AiV3JpaConfiguration`.
4. Add `profileId` to domain turns; populate user/assistant writers; lock at send.
5. Expose `TurnResponse.profileId`.
6. Fix all testIT table name assertions.
7. Mark `[x]` WI-317 in [STORY.md](STORY.md); move folder to `in-progress/` if first `[x]`.

---

## Goal

Replace the split **`ai_chat_metadata` + `ai_conversation`** model with a **single consistent `chat` naming scheme**, wire JPA autoconfiguration, and ensure chat rows survive restart and back the sidebar list + ownership requirements.

User-selected direction: **unify** — one parent table, turn table renamed, `conversation_id` → `chat_id` on related tables where feasible.

## Problem

1. **Naming inconsistency:** `ai_conversation` / `ai_conversation_turn` vs `ai_chat_metadata`; HTTP uses `chatId`, persistence uses `conversationId`.
2. **Split lifecycle:** metadata on `createChat`, transcript header on first message — breaks logical 1:1 and complicates restart behaviour.
3. **Missing JPA bean:** `JpaChatRegistry` never registered; list reads in-memory metadata while transcript may already be in DB.
4. **No FK integrity:** metadata and transcript tables are not linked at the database layer.
5. **No per-turn profile:** mill-ui lets users switch agent profile mid-chat ([`ChatToolbar`](../../../../ui/mill-ui/src/components/chat/ChatToolbar.tsx) → `PATCH` [`updateChatProfile`](../../../../ui/mill-ui/src/services/chatService.ts)). Only **`ai_chat.profile_id`** (current selection) is updated; turns have no profile column — after reload you cannot see which agent answered each message.

## Target schema (Flyway `V10__ai_chat_unified.sql`)

### Parent — `ai_chat`

Single row per chat, created on **`createChat`** (not deferred to first message).

| Column | Notes |
|--------|--------|
| `chat_id` | PK |
| `user_id` | Owner (WI-318) |
| `profile_id` | Agent profile; mutable on general chats |
| `chat_name`, `chat_type`, `is_favorite` | From current metadata |
| `context_type`, `context_id`, `context_label`, `context_entity_type` | Contextual chats |
| `created_at`, `updated_at` | Resource timestamps |

Merges **`ai_chat_metadata`** + header fields from **`ai_conversation`**.

### Child — `ai_chat_turn`

Renamed from **`ai_conversation_turn`**.

| Column | Notes |
|--------|--------|
| `turn_id` | PK |
| `chat_id` | FK → `ai_chat(chat_id)` **ON DELETE CASCADE** |
| **`profile_id`** | **Agent profile active for this turn** — who the user was addressing / who replied |
| `role`, `text`, `position`, `urn`, `created_at` | Unchanged semantics |

**`profile_id` semantics (two roles, one column):**

| Turn `role` | `profile_id` meaning |
|-------------|----------------------|
| `user` | Active chat profile when the user sent the message (`ai_chat.profile_id` at send time) |
| `assistant` | Profile that executed the run (`RoutedAgentEvent.profileId` / run profile) |

`ai_chat.profile_id` remains the **current** profile for the next message (toolbar picker). Turn rows are an **immutable snapshot** per message — switching profile does not rewrite history.

**Profile lock at send (confirmed):** capture `profileId` once when `sendMessage` starts (runtime session / routing input). User and assistant turns for that exchange both use the **same** locked id — even if the toolbar PATCH completes mid-stream.

**Migration backfill:** existing turns → parent `ai_chat.profile_id` (or `ai_conversation.profile_id` pre-merge). Pre-migration mid-chat switches cannot be reconstructed.

### Drop

- `ai_conversation` (header absorbed into `ai_chat`)
- `ai_chat_metadata` (absorbed into `ai_chat`)

### Rename `conversation_id` → `chat_id` (+ optional FK to `ai_chat`)

| Table | FK on delete |
|-------|----------------|
| `chat_memory` | CASCADE (optional; align with hard-delete story) |
| `chat_memory_message` | via `chat_memory` |
| `ai_artifact` | CASCADE or service-layer delete (A-77) |
| `ai_active_artifact_pointer` | CASCADE via artifact/chat policy |
| `ai_run_event` | nullable `chat_id`; **no FK** (audit / orphaned runs OK) |

### Data migration

See **[Data migration SQL](#data-migration-sql-v10__ai_chat_unifiedsql)** below for the full scripted plan (single Flyway file, ordered phases).

---

## Data migration SQL (`V10__ai_chat_unified.sql`)

**Style:** one Flyway SQL script, **portable PostgreSQL + H2** (same pattern as [`V6__ai_embedding_model_and_value_mapping.sql`](../../../../persistence/mill-persistence/src/main/resources/db/migration/V6__ai_embedding_model_and_value_mapping.sql)). Flyway runs it in a **single transaction** — any step failure rolls back the whole migration.

**Not used:** Java migration for data (only WI-322 uses Java for optional `CREATE EXTENSION`). WI-317 is pure SQL.

### Phase 0 — assumptions

| Case | Handling |
|------|----------|
| Metadata only (`ai_chat_metadata`, no `ai_conversation`) | Row copied from metadata; timestamps from metadata |
| Both tables, same id (`chat_id = conversation_id`) | Merge: metadata columns win; `profile_id` = `COALESCE(m.profile_id, c.profile_id)`; `updated_at` = later of the two |
| Conversation only (orphan transcript) | Insert into `ai_chat` with `user_id = 'default'`, `chat_name = 'Recovered Chat'`, `chat_type = 'general'`, profile/timestamps from `ai_conversation` |
| Turn `profile_id` (new column) | Backfill from **`ai_chat.profile_id`** at migration time (cannot reconstruct mid-chat profile switches) |

Orphan placeholder `'default'` matches [`mill.ai.chat.default-user-id`](../../../../apps/mill-service/src/main/resources/application.yml) — document in migration header comment for ops.

### Phase 1 — create new tables (old tables untouched)

```sql
-- Parent (same columns as ai_chat_metadata today)
CREATE TABLE ai_chat (
    chat_id             VARCHAR(255) PRIMARY KEY,
    user_id             VARCHAR(255) NOT NULL,
    profile_id          VARCHAR(255) NOT NULL,
    chat_name           VARCHAR(512) NOT NULL,
    chat_type           VARCHAR(64)  NOT NULL,
    is_favorite         BOOLEAN      NOT NULL DEFAULT FALSE,
    context_type        VARCHAR(255),
    context_id          VARCHAR(255),
    context_label       VARCHAR(512),
    context_entity_type VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ai_chat_user ON ai_chat (user_id);
CREATE UNIQUE INDEX uq_ai_chat_context ON ai_chat (user_id, context_type, context_id);

-- Turns (ai_conversation_turn + profile_id)
CREATE TABLE ai_chat_turn (
    turn_id     VARCHAR(255)  PRIMARY KEY,
    chat_id     VARCHAR(255)  NOT NULL,
    profile_id  VARCHAR(255)  NOT NULL,
    role        VARCHAR(32)   NOT NULL,
    text        TEXT,
    position    INT           NOT NULL,
    urn         VARCHAR(1024) NOT NULL,
    created_at  TIMESTAMP     NOT NULL
);

CREATE INDEX idx_ai_chat_turn_chat ON ai_chat_turn (chat_id, position);
```

FK on `ai_chat_turn.chat_id` is added **after** data copy (Phase 4) so orphan turns can be detected or skipped.

### Phase 2 — copy parent rows into `ai_chat`

```sql
-- 2a: metadata (+ optional conversation header for timestamps/profile)
INSERT INTO ai_chat (
    chat_id, user_id, profile_id, chat_name, chat_type, is_favorite,
    context_type, context_id, context_label, context_entity_type,
    created_at, updated_at
)
SELECT
    m.chat_id,
    m.user_id,
    COALESCE(m.profile_id, c.profile_id),
    m.chat_name,
    m.chat_type,
    m.is_favorite,
    m.context_type,
    m.context_id,
    m.context_label,
    m.context_entity_type,
    COALESCE(m.created_at, c.created_at),
    GREATEST(m.updated_at, COALESCE(c.updated_at, m.updated_at))
FROM ai_chat_metadata m
LEFT JOIN ai_conversation c ON c.conversation_id = m.chat_id;

-- 2b: orphan conversations (no metadata row)
INSERT INTO ai_chat (
    chat_id, user_id, profile_id, chat_name, chat_type, is_favorite,
    created_at, updated_at
)
SELECT
    c.conversation_id,
    'default',
    c.profile_id,
    'Recovered Chat',
    'general',
    FALSE,
    c.created_at,
    c.updated_at
FROM ai_conversation c
WHERE NOT EXISTS (
    SELECT 1 FROM ai_chat_metadata m WHERE m.chat_id = c.conversation_id
);
```

(`GREATEST` works on H2 PostgreSQL mode and Postgres; if a edge-case NULL slips through, use `COALESCE`/`CASE` instead in implementation.)

### Phase 3 — copy turns into `ai_chat_turn`

```sql
INSERT INTO ai_chat_turn (
    turn_id, chat_id, profile_id, role, text, position, urn, created_at
)
SELECT
    t.turn_id,
    t.conversation_id,
    ch.profile_id,
    t.role,
    t.text,
    t.position,
    t.urn,
    t.created_at
FROM ai_conversation_turn t
INNER JOIN ai_chat ch ON ch.chat_id = t.conversation_id;
```

Turns whose `conversation_id` has **no** `ai_chat` row (should not happen after 2b) are **omitted** — log/count via migration IT if any exist in test fixtures.

Then add FK:

```sql
ALTER TABLE ai_chat_turn
    ADD CONSTRAINT fk_ai_chat_turn_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat (chat_id) ON DELETE CASCADE;
```

### Phase 4 — rename `conversation_id` → `chat_id` on satellite tables

Order matters because of FKs on `chat_memory`.

```sql
-- 4a: chat_memory — drop child FK, rename PK column, rename child column, re-FK
ALTER TABLE chat_memory_message DROP CONSTRAINT IF EXISTS CONSTRAINT_INDEX_3; -- use Flyway/H2-safe name lookup in impl
-- Implementation: discover constraint name per DB or use portable rebuild pattern (see below)

ALTER TABLE chat_memory RENAME COLUMN conversation_id TO chat_id;
ALTER TABLE chat_memory_message RENAME COLUMN conversation_id TO chat_id;
-- Re-create FK chat_memory_message(chat_id) -> chat_memory(chat_id) ON DELETE CASCADE

-- 4b: no FK today — simple renames
ALTER TABLE ai_artifact RENAME COLUMN conversation_id TO chat_id;
ALTER TABLE ai_active_artifact_pointer RENAME COLUMN conversation_id TO chat_id;
ALTER TABLE ai_run_event RENAME COLUMN conversation_id TO chat_id;

-- Recreate indexes with new column names (drop old, create new)
DROP INDEX IF EXISTS idx_ai_artifact_conv;
CREATE INDEX idx_ai_artifact_chat ON ai_artifact (chat_id, created_at);
DROP INDEX IF EXISTS idx_ai_run_event_conv;
CREATE INDEX idx_ai_run_event_chat ON ai_run_event (chat_id, created_at);
DROP INDEX IF EXISTS idx_chat_memory_message_conv;
CREATE INDEX idx_chat_memory_message_chat ON chat_memory_message (chat_id, position);
```

**Portable FK rename (H2 + Postgres):** if `DROP CONSTRAINT` names differ across engines, use the **rebuild pattern** already common in Flyway migrations:

1. Create `chat_memory_new` / `chat_memory_message_new` with `chat_id`
2. `INSERT … SELECT` with column map
3. `DROP` old tables
4. `ALTER TABLE … RENAME TO chat_memory`

Prefer rebuild for `chat_memory` pair if constraint names are not stable in H2 testIT.

**Optional FK** (WI-317 acceptance does not require all satellites FK’d to `ai_chat`):

```sql
ALTER TABLE chat_memory
    ADD CONSTRAINT fk_chat_memory_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat (chat_id) ON DELETE CASCADE;
```

Defer strict artifact/pointer FKs to A-76/A-77 if delete semantics are still service-layer.

### Phase 5 — drop legacy transcript tables

```sql
DROP TABLE ai_conversation_turn;  -- empty after copy; FK drops with parent
DROP TABLE ai_conversation;
DROP TABLE ai_chat_metadata;
```

### Phase 6 — verification hooks (test / ops)

Migration IT ([`FlywayMigrationIT`](../../../../persistence/mill-persistence/src/testIT/kotlin/io/qpointz/mill/persistence/FlywayMigrationIT.kt)) after V10:

- Assert tables **`ai_chat`**, **`ai_chat_turn`** exist; **`ai_chat_metadata`**, **`ai_conversation`** absent
- Row counts: `COUNT(ai_chat)` ≥ former `COUNT(ai_chat_metadata)`; `COUNT(ai_chat_turn)` = former `COUNT(ai_conversation_turn)`
- Spot-check: every `ai_chat_turn.chat_id` exists in `ai_chat`

Optional one-shot SQL comment in migration file for ops manual check:

```sql
-- SELECT COUNT(*) FROM ai_chat;
-- SELECT COUNT(*) FROM ai_chat_turn WHERE profile_id IS NULL;  -- expect 0
```

### What the SQL script deliberately does **not** do

| Item | Reason |
|------|--------|
| Rewrite historical turn `profile_id` per exchange | Impossible pre-migration; single backfill from header |
| Purge `ai_artifact` rows | Out of scope (WI-319 stops new ephemeral rows only) |
| Zero-downtime dual-write | Single maintenance migration; app deploy after Flyway |
| Data migration in Kotlin | Keeps WI-317 one SQL artifact; reviewable diff |

### Deploy order

1. Run Flyway V10 (mill-service startup or migrate job)
2. Deploy application build with `ChatEntity` / `JpaChatRegistry` (same release)

Old JPA code against old table names **must not** run against post-V10 schema.

---

## Code changes

### Domain (`mill-ai`)

- Add **`profileId`** to [`ConversationTurn`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ConversationStore.kt).
- **Lock at send:** [`UnifiedChatService.sendMessage`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt) / runtime session carries locked `profileId` for the run; toolbar PATCH updates header only for **subsequent** sends.
- **User turn:** set from locked profile at append ([`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt), [`SchemaExplorationAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/SchemaExplorationAgent.kt)).
- **Assistant turn:** same locked id in [`StandardPersistenceProjector.persistTranscriptTurn`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjector.kt) (`event.profileId` from routing input, not re-read from `ai_chat`).

### API (same WI)

- Add **`profileId`** to [`TurnResponse`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) for GET replay (mill-ui can show per-message agent later; no UI change required in this WI).

### Persistence (`mill-ai-persistence`)

- Replace `ChatMetadataEntity` + `ConversationEntity` with **`ChatEntity`** (`ai_chat`).
- Replace `ConversationTurnEntity` → **`ChatTurnEntity`** (`ai_chat_turn`, column `chat_id`).
- **`JpaChatRegistry`** → maps to `ChatEntity` / `ChatRepository`.
- **`JpaConversationStore`** → turn operations only; `ensureExists` becomes validate-or-noop (chat row must exist from `createChat`); `updateProfileId` updates `ai_chat.profile_id`.
- Rename JPA repos / column mappings on memory, artifact, pointer, run-event entities.

### Autoconfigure

- Register **`jpaChatRegistry`** bean in [`AiV3JpaConfiguration`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3JpaConfiguration.kt).
- Extend [`AiV3JpaAutoConfigurationIT`](../../../../ai/mill-ai-autoconfigure/src/testIT/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3JpaAutoConfigurationIT.kt).

### Service

- [`UnifiedChatService.createChat`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt): single durable parent row on create (already via registry; remove reliance on lazy `ensureExists` for new chats).
- [`UnifiedChatService.sendMessage`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt): `ensureExists` only guards legacy/migrated rows or updates `updated_at` on `ai_chat`.

### Domain / port naming (same WI unless too large)

- DB columns: **`chat_id`** everywhere.
- Kotlin ports may keep `conversationId` parameters temporarily as aliases to `chatId` **or** rename in the same WI — prefer **`chatId`** in [`ConversationStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ConversationStore.kt) / records if touch surface is bounded; runtime `AgentEvent.conversationId` can follow in a follow-up commit within the WI.

## Tests

- Migration IT: metadata + turns survive rename; FK cascade on delete (turns at minimum).
- **Profile snapshot IT:** create chat with profile A → send → PATCH profile B → send → reload → turn 1 has A, turn 2 has B (user + assistant rows for each exchange).
- Autoconfigure IT: `ChatRegistry` is `JpaChatRegistry`.
- Service IT: create chat → row in `ai_chat` before first message; restart simulation → list returns chat.
- Update all `mill-ai-persistence` testIT entities/repos for new table names.

## Out of scope

- User identity resolver (WI-318).
- Ephemeral artifacts (WI-319).
- Per-message profile UI in mill-ui (API only; optional UI follow-up).
- Full artifact/run-event delete policy (A-77) — note FK choices in migration comment.

## Acceptance

- Consistent table names: **`ai_chat`**, **`ai_chat_turn`** (no mixed conversation/chat table names).
- Each turn row carries **`profile_id`**; mid-chat profile switch preserves historical attribution on reload.
- `POST create chat` inserts **`ai_chat`** row immediately.
- After mill-service restart (same DB): **`GET /api/v1/ai/chats`** lists chats; **`GET …/{id}`** loads turns.
- `./gradlew :ai:mill-ai-persistence:testIT :ai:mill-ai-autoconfigure:testIT :ai:mill-ai-service:testIT` pass.

## Modules

- `persistence/mill-persistence` (Flyway **V10** — V9 reserved for pgvector extension WI-322)
- `ai/mill-ai-persistence`
- `ai/mill-ai-autoconfigure`
- `ai/mill-ai-service`
- `ai/mill-ai` (port/record renames if included)
