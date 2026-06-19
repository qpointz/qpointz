# WI-323 — Cosmetic table renames (`V11`)

## Goal

Rename remaining AI chat persistence tables to the **`ai_chat_*`** prefix (WI-317 follow-up) and align JPA `@Table` names, Flyway assertions, and design docs.

## Delivered

| Before | After |
|--------|-------|
| `ai_artifact` | `ai_chat_artifact` |
| `ai_active_artifact_pointer` | `ai_chat_artifact_pointer` |
| `ai_run_event` | `ai_chat_run_event` |
| `chat_memory` | `ai_chat_memory` |
| `chat_memory_message` | `ai_chat_memory_message` |

**Out of scope:** `mill_langchain_embedding_store` → `ai_value_mapping_vector` (config default only; no Flyway rename — operator re-index acceptable).

## Changes

- Flyway **`V11__ai_chat_table_naming.sql`**
- JPA entities: `ArtifactEntity`, `ActiveArtifactPointerEntity`, `RunEventEntity`, `ChatMemoryEntity`, `ChatMemoryMessageEntity`
- `FlywayMigrationIT` expected table names
- Design docs: `v3-conversation-persistence.md`, `artifact-foundation.md`, `v3-capability-manifest.md`
- Canonical naming rules: [`docs/design/persistence/db-naming-convention.md`](../../../design/persistence/db-naming-convention.md)
- Vector store default table: `VectorStoreSettings`, `application.yml`, GCP config template

## Verify

```bash
./gradlew :persistence:mill-persistence:testIT
./gradlew :ai:mill-ai-persistence:testIT
./gradlew :ai:mill-ai-autoconfigure:testIT
```

## Status

**Done** — 2026-06-19
