# WI-322 — Optional pgvector Flyway Java migration

## Cold start

| Field | Value |
|-------|--------|
| **Story** | [pgvector-flyway-extension/STORY.md](STORY.md) — seq **1** in combined delivery |
| **Also listed in** | [ai-chat-persistence/STORY.md](../../in-progress/ai-chat-persistence/STORY.md) tracking list |
| **Depends on** | None |
| **Flyway** | **`V9__EnsurePgvectorExtension`** (Java class, not `.sql`) |
| **Commit** | `[feat] WI-322: optional pgvector Flyway migration V9` |

### Verify when done

```bash
./gradlew :persistence:mill-persistence:testIT
./gradlew :persistence:mill-persistence:test
```

Optional manual: fresh Postgres with `pgvector/pgvector` image + `ai-pgvector` profile → no manual `CREATE EXTENSION`.

### Primary files to create / touch

| Area | Path |
|------|------|
| Migration class | `persistence/mill-persistence/src/main/java/io/qpointz/mill/persistence/flyway/V9__EnsurePgvectorExtension.java` |
| Bean registration | `persistence/mill-persistence-autoconfigure/.../PersistenceAutoConfiguration.kt` (or new `MillFlywayConfiguration.kt`) — `@Bean` `JavaMigration` |
| Gradle | `persistence/mill-persistence/build.gradle.kts` — `implementation(libs.flyway.core)` |
| Unit test | `persistence/mill-persistence/src/test/java/.../PgvectorExtensionMigrationTest.java` — classify SQLException messages |
| Docs comment | `apps/mill-service/application.yml` pgvector profile block |
| IT | `FlywayMigrationIT.kt` — still passes on H2 |

### Implementation checklist

1. Add Flyway core compile dependency to `mill-persistence`.
2. Implement Java migration: **H2-safe** (see H2 section); Postgres `CREATE EXTENSION`; soft-fail if extension unavailable; **rethrow** permission errors on Postgres only.
3. Register migration bean for Spring Boot Flyway discovery.
4. Unit-test error classification helpers.
5. Confirm H2 `testIT` green.
6. Mark `[x]` in [STORY.md](STORY.md) and [ai-chat-persistence/STORY.md](../../in-progress/ai-chat-persistence/STORY.md) WI-322 line. **Done** — story archived **2026-06-18**.

### Locked behaviour (recap)

| Case | Outcome |
|------|---------|
| **H2** (testIT) | **Never fail Flyway** — skip extension SQL; any unexpected error → WARN/DEBUG + success |
| Postgres + pgvector | Extension created, success |
| Postgres, extension not installed | WARN, success |
| Postgres, permission denied | **Flyway fails** |

**Why Java (not SQL):** a `.sql` `CREATE EXTENSION` fails hard on H2 and vanilla Postgres. Java can branch on `DatabaseMetaData` and swallow/environment-skip errors H2 would throw.

---

## Goal

Add Flyway migration **`V9__EnsurePgvectorExtension`** that runs `CREATE EXTENSION IF NOT EXISTS vector` on PostgreSQL when pgvector is available, and **completes successfully** when pgvector is not installed (plain vanilla PostgreSQL) or when the database is not PostgreSQL (H2 tests).

Fixes fresh-Postgres startup for mill-service with **`pgvector`** profile when the server has pgvector packages installed.

## Problem

With `--spring.profiles.active=ai-pgvector` (or `ai,pgvector`) against a new PostgreSQL database:

1. Flyway applies SQL migrations (V1–V8) — none create the `vector` extension.
2. Spring context starts `VectorStoreAutoConfiguration`, which calls `assertPostgreSqlWithVectorExtension()`.
3. Startup fails:

   > mill.ai.data.embedding vector-store backend=pgvector requires the PostgreSQL 'vector' extension (pgvector). Create it with CREATE EXTENSION IF NOT EXISTS vector; …

Manual `CREATE EXTENSION` works but should be automated for deployments that **have** pgvector. Deployments **without** pgvector must not break Flyway for everyone else.

## Solution

**Java-based Flyway migration** (not SQL — need conditional error handling).

### Behaviour matrix

| Database | pgvector installed | Migration outcome |
|----------|-------------------|-------------------|
| H2 (testIT) | N/A | **Never fail** — no extension SQL (or catch-all skip) |
| PostgreSQL | Yes | `CREATE EXTENSION IF NOT EXISTS vector`, success |
| PostgreSQL | No (vanilla) | Catch extension-unavailable error, **log WARN**, success |
| PostgreSQL | No `CREATE` privilege | **Hard-fail Flyway** — surfaces misconfigured production DB (user decision) |

After migration on vanilla Postgres: Flyway OK; **`backend=pgvector` still fails at startup** (unchanged) — use `chroma` / `in-memory` or install pgvector.

### H2 and non-PostgreSQL (must not break testIT)

H2 cannot run `CREATE EXTENSION vector`. A SQL migration would **always fail** Flyway on `./gradlew :persistence:mill-persistence:testIT`.

**Two-layer guard (both required):**

1. **Early exit** — before any extension SQL, detect non-Postgres:
   - `databaseProductName` contains `"h2"` (case-insensitive), **or**
   - product is not PostgreSQL  
   → `log.debug("Skipping pgvector extension migration on {}")` and **return** (Flyway success).

2. **Catch-all on non-Postgres** — outer `try/catch` around the whole `migrate()` body:
   - If `isH2OrNonPostgres(conn)` **or** `isNonPostgresSqlError(e)` (syntax/feature errors typical of H2)  
   → log WARN/DEBUG, **return** (do **not** rethrow).  
   This covers mis-detection, wrapped connections, or future test DBs.

**Only PostgreSQL** reaches the `CREATE EXTENSION` path and the permission-denied hard-fail logic.

### Implementation sketch

**Class:** `io.qpointz.mill.persistence.flyway.V9__EnsurePgvectorExtension`  
**Module:** [`persistence/mill-persistence`](../../../persistence/mill-persistence/)  
**Language:** Java (Flyway `BaseJavaMigration` / `JavaMigration` — stable Flyway API)

```java
@Override
public void migrate(Context context) throws Exception {
    Connection conn = context.getConnection();
    if (isH2OrNonPostgres(conn)) {
        log.debug("Skipping pgvector extension migration on non-PostgreSQL database");
        return;
    }
    try (Statement st = conn.createStatement()) {
        st.execute("CREATE EXTENSION IF NOT EXISTS vector");
    } catch (SQLException e) {
        if (isPgvectorExtensionUnavailable(e)) {
            log.warn("Skipping pgvector extension: {}", e.getMessage());
            return;
        }
        if (isH2OrNonPostgres(conn) || isNonPostgresSqlError(e)) {
            log.debug("Skipping pgvector extension after non-PostgreSQL error: {}", e.getMessage());
            return;
        }
        throw e; // Postgres permission denied and other production errors fail Flyway
    }
}

private static boolean isH2OrNonPostgres(Connection conn) throws SQLException {
    String product = conn.getMetaData().getDatabaseProductName();
    if (product == null) return true;
    String p = product.toLowerCase(Locale.ROOT);
    return p.contains("h2") || !p.contains("postgresql");
}

private static boolean isNonPostgresSqlError(SQLException e) {
    // H2: syntax error / unsupported feature on CREATE EXTENSION
    String msg = e.getMessage();
    if (msg != null && (msg.contains("H2") || msg.contains("Syntax error")
            || msg.contains("CREATE EXTENSION"))) {
        return true;
    }
    return false; // narrow — only use with isH2OrNonPostgres double-check when unsure
}
```

**Detect “pgvector not available”** (non-exhaustive; match message / SQLState) — **soft-fail only for these:**

- `extension "vector" is not available`
- `could not open extension control file` + `vector.control`
- SQLState `0A000` (feature_not_supported) where applicable

**Hard-fail (rethrow):** permission / privilege errors (e.g. `permission denied to create extension`, insufficient role) — Flyway aborts so ops fix grants or run extension as superuser.

**Registration:** Spring Boot Flyway picks up `JavaMigration` beans — register `@Bean` in [`PersistenceAutoConfiguration`](../../../persistence/mill-persistence-autoconfigure/src/main/kotlin/io/qpointz/mill/persistence/configuration/PersistenceAutoConfiguration.kt) (or dedicated `MillFlywayConfiguration` in `mill-persistence-autoconfigure`).

**Dependencies:** add `implementation(libs.flyway.core)` to [`mill-persistence/build.gradle.kts`](../../../persistence/mill-persistence/build.gradle.kts) for compile-time Flyway API.

### Do not

- Put `CREATE EXTENSION` in a `.sql` migration without handling failure — Flyway would abort on vanilla Postgres.
- Fail Flyway when pgvector is missing — breaks H2 testIT, vanilla Postgres dev, and non-pgvector profiles on shared DB.

## Tests

1. **`FlywayMigrationIT`**: H2 — all migrations including V9 apply cleanly (**primary H2 gate**).
2. **Unit tests**:
   - `isH2OrNonPostgres` true for H2 product name → migrate returns without executing SQL (mock connection).
   - Extension-unavailable vs permission-denied classification on Postgres-style errors.
   - Optional: if `execute` mocked to throw H2 syntax error, migrate still completes (catch-all).
3. **Optional testIT** (Testcontainers, if already used elsewhere):
   - `postgres:16` — V9 succeeds, extension still absent, no Flyway failure.
   - `pgvector/pgvector:pg16` — V9 succeeds, `SELECT 1 FROM pg_extension WHERE extname='vector'` returns a row.

## Docs

- One-line comment in [`apps/mill-service/application.yml`](../../../apps/mill-service/application.yml) pgvector profile: Flyway V9 attempts extension creation on PostgreSQL.
- Optional note in [`deploy/local-dev/docker-compose.yml`](../../../deploy/local-dev/docker-compose.yml) comment block.

## Acceptance

- Fresh PostgreSQL **with** pgvector (e.g. `pgvector/pgvector` image): mill-service `ai-pgvector` starts without manual `CREATE EXTENSION`.
- Fresh PostgreSQL **without** pgvector: Flyway completes; mill-service without pgvector backend starts; `ai-pgvector` still fails with existing clear message (extension missing).
- Fresh PostgreSQL **with** pgvector but app user **without** `CREATE` on extension: Flyway **fails** (permission denied not swallowed).
- `./gradlew :persistence:mill-persistence:testIT` passes on H2 (**V9 must never fail Flyway on H2**).

## Modules

- `persistence/mill-persistence`
- `persistence/mill-persistence-autoconfigure`
- `apps/mill-service` (profile comment only)

## Flyway ordering note

- This WI: **`V9__EnsurePgvectorExtension`**
- ai-chat-persistence WI-317 (unified `ai_chat`): use **`V10__`** (V8 is [`V8__saved_queries.sql`](../../../persistence/mill-persistence/src/main/resources/db/migration/V8__saved_queries.sql))
