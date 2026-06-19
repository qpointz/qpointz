# pgvector-flyway-extension

**Status:** closed **2026-06-18** (branch `qpointz-ai-chat-persistence-incomplete`, MR !397).

## Goal

On a **fresh PostgreSQL** database, Flyway should attempt `CREATE EXTENSION IF NOT EXISTS vector` so mill-service starts with the **`pgvector`** / **`ai-pgvector`** profile when the server has pgvector installed. When pgvector is **not installed** (vanilla PostgreSQL), the migration **succeeds** (WARN, continue). When the DB user **lacks privilege**, Flyway **fails** (surfaces misconfiguration).

---

## Cold start (from empty context)

### Branch and workspace

| Item | Value |
|------|--------|
| **Branch** | Delivered on [ai-chat-persistence](../in-progress/ai-chat-persistence/STORY.md) branch `qpointz-ai-chat-persistence-incomplete`, or standalone `feat/pgvector-flyway-extension` from `origin/dev` |
| **Working directory** | Repository root |
| **Story folder** | `docs/workitems/completed/20260618-pgvector-flyway-extension/` (archived **2026-06-18**) |

### Preconditions

| Check | Detail |
|--------|--------|
| **JDK** | Java 21 |
| **Flyway** | Latest SQL migration: [`V8__saved_queries.sql`](../../../persistence/mill-persistence/src/main/resources/db/migration/V8__saved_queries.sql) |
| **This WI** | Adds **`V9__EnsurePgvectorExtension`** (Java, not SQL) |
| **H2 tests** | [`FlywayMigrationIT`](../../../persistence/mill-persistence/src/testIT/kotlin/io/qpointz/mill/persistence/FlywayMigrationIT.kt) must still pass (migration no-op on H2) |

### Problem (symptom)

Profile: `--spring.profiles.active=ai-pgvector` (group: `ai` + `pgvector`) against fresh Postgres.

Startup fails after Flyway with:

> vector-store backend=pgvector requires the PostgreSQL 'vector' extension (pgvector). Create it with CREATE EXTENSION IF NOT EXISTS vector;

Source: [`VectorStoreAutoConfiguration.assertPostgreSqlWithVectorExtension`](../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/vectorstore/VectorStoreAutoConfiguration.kt) (~line 137).

Config: [`apps/mill-service/application.yml`](../../../apps/mill-service/application.yml) — `spring.profiles.group.ai-pgvector`, `mill.ai.data.embedding.default.vector-store.backend=pgvector`.

### Locked decisions

| Topic | Decision |
|--------|-----------|
| **Migration type** | **Java** Flyway migration (conditional catch) — not `.sql` |
| **H2 / non-Postgres** | **Never fail Flyway** — skip before SQL; if anything still throws, catch and succeed (see below) |
| **Postgres + pgvector installed** | `CREATE EXTENSION IF NOT EXISTS vector`, success |
| **Postgres, extension not installed** | WARN + **success** (Flyway continues) |
| **Postgres, permission denied** | **Fail Flyway** (rethrow) |
| **After soft-skip** | `backend=pgvector` startup check **unchanged** — still errors if extension missing |
| **Flyway version** | **V9** — [WI-317](../ai-chat-persistence/WI-317-jpa-chat-registry.md) uses **V10** |

### Work items

| WI | File |
|----|------|
| WI-322 | [`WI-322-pgvector-extension-flyway-migration.md`](WI-322-pgvector-extension-flyway-migration.md) |

### Verification

```bash
./gradlew :persistence:mill-persistence:testIT
./gradlew :persistence:mill-persistence:test

# Manual (Postgres with pgvector image, e.g. deploy/local-dev docker-compose)
# Fresh DB + ai-pgvector → service starts without manual CREATE EXTENSION
```

### Commit example

`[feat] WI-322: optional pgvector Flyway migration V9`

### Relation to ai-chat-persistence

Independent — recommended **first** in combined delivery ([ai-chat-persistence STORY](../in-progress/ai-chat-persistence/STORY.md) seq 1). No code dependency on WI-317–321.

---

## Out of scope

- Auto-degrade pgvector backend to in-memory/chroma when extension missing
- Installing pgvector on OS/container (use `pgvector/pgvector` in [`deploy/local-dev/docker-compose.yml`](../../../deploy/local-dev/docker-compose.yml))
- LangChain4j embedding table DDL (`create-table: true` stays separate)

## Design references

- [`apps/mill-service/application.yml`](../../../apps/mill-service/application.yml)
- [`persistence/mill-persistence/`](../../../persistence/mill-persistence/)

## Work Items (tracking list)

- [x] WI-322 — Optional pgvector Flyway Java migration ([`WI-322-pgvector-extension-flyway-migration.md`](WI-322-pgvector-extension-flyway-migration.md))
