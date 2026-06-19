# ai-chat-table-naming

## Goal

Align remaining AI chat persistence tables with the **`ai_chat_*`** naming convention (WI-317 follow-up) and add **`ON DELETE CASCADE`** FKs from chat satellites to `ai_chat` to prevent stale rows on hard-delete.

## Work Items

- [x] WI-323 — Cosmetic table renames (`V11`)
- [x] WI-324 — `ai_chat` CASCADE delete FKs (`V12`)

## Depends on

- [WI-317](../completed/20260619-ai-chat-persistence/WI-317-jpa-chat-registry.md) — unified `ai_chat` / `ai_chat_turn` (V10)

## Status

**Closed** — 2026-06-19.

## Verify

```bash
./gradlew :persistence:mill-persistence:testIT
./gradlew :ai:mill-ai-autoconfigure:testIT
./gradlew :ai:mill-ai-persistence:testIT
./gradlew :ai:mill-ai-service:testIT
```
