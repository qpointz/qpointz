# WI-324 — `ai_chat` CASCADE delete FKs (`V12`)

## Goal

Add **`ON DELETE CASCADE`** foreign keys from chat satellite tables to `ai_chat` so hard-delete removes turns, memory, artifacts, pointers, and run events without stale rows.

## Delivered

- Flyway **`V12__ai_chat_cascade_delete.sql`**
  - Orphan cleanup for rows missing parent `ai_chat`
  - FKs: `ai_chat_memory`, `ai_chat_artifact`, `ai_chat_run_event` → `ai_chat` CASCADE
  - `ai_chat_turn` already CASCADE from V10; `ai_chat_memory_message` CASCADE via memory header; pointer CASCADE via artifact FK
- `JpaConversationStore.delete()` — explicit turn removal via `ChatTurnRepository.deleteByChatId`
- `RunEventRepository.countByChatId` for cascade IT assertions
- `JpaChatTestSupport.seedChat()` — parent row seeding for ITs after FK enforcement
- **`JpaChatDeleteCascadeIT`** — delete cascades to turns, memory, artifacts, pointers, run events

**Out of scope:** `relation_record` orphan cleanup on chat delete (separate future WI).

## Verify

```bash
./gradlew :ai:mill-ai-persistence:testIT
./gradlew :ai:mill-ai-service:testIT
```

Resolves backlog **A-76**, **A-77**.

## Status

**Done** — 2026-06-19
