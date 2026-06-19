# Database Naming Convention

**Status:** Active  
**Scope:** All relational objects owned by Mill Flyway migrations and JPA mappings  
**Owner:** `persistence/mill-persistence` (schema); domain `*-persistence` modules (entity `@Table` names)

This document is the canonical reference for **table, column, index, and constraint naming**
across Mill components. Module ownership, Flyway placement, and adapter rules remain in
[persistence-bootstrap.md](./persistence-bootstrap.md).

---

## 1. Principles

| Rule | Detail |
|------|--------|
| **Case** | Lowercase only (`DATABASE_TO_LOWER=TRUE` in H2 test mode). |
| **Words** | `snake_case`; ASCII letters and digits; no quoted identifiers. |
| **Singular entity tables** | One row ≈ one domain object (`ai_chat`, `metadata_entity`, not `ai_chats`). |
| **Domain prefix** | Every application table starts with a **component prefix** (see §2). |
| **Portable SQL** | Names must be valid on H2 (PostgreSQL mode) and PostgreSQL. |
| **JPA parity** | `@Table(name = "...")` and `@Column(name = "...")` must match Flyway exactly. |
| **Renames** | Cosmetic table/column renames use a dedicated Flyway migration; never edit shipped migrations in place. |

Shared cross-domain primitives may omit a component prefix only when documented as **platform-wide**
(see §2.4).

---

## 2. Component prefixes

Use a **two-level** pattern where the feature is a bounded subdomain:

```text
{component}_{feature}_{entity}     -- feature-specific tables
{component}_{entity}               -- when the component has one clear aggregate area
```

### 2.1 AI (`ai_`)

| Area | Prefix / pattern | Examples |
|------|------------------|----------|
| Chat persistence | `ai_chat` aggregate + `ai_chat_*` satellites | `ai_chat`, `ai_chat_turn`, `ai_chat_memory`, `ai_chat_artifact` |
| Value mappings (relational) | `ai_` + feature noun | `ai_embedding_model`, `ai_value_mapping`, `ai_value_mapping_state` |
| Vector store (pgvector) | `ai_` + feature noun; **not** Flyway-managed | `ai_value_mapping_vector` (config default in `VectorStoreSettings`) |

**Chat aggregate rule:** `ai_chat` is the parent row for a conversation. Satellite tables that
hold rows keyed by `chat_id` use the `ai_chat_{role}` prefix (`ai_chat_turn`, `ai_chat_run_event`,
`ai_chat_artifact_pointer`, …).

**Foreign key column:** use `chat_id` (not `conversation_id`) for all chat-scoped tables created or
renamed after V10.

### 2.2 Metadata (`metadata_`)

All metadata relational tables use the `metadata_` prefix:

- `metadata_scope`, `metadata_entity`, `metadata_facet_type`, `metadata_entity_facet`, …

Child tables name the parent in the FK column (`entity_id`, `scope_id`, `facet_type_id`).

### 2.3 Security / identity (`auth_` target, legacy mixed)

**Target convention** for new security tables: `auth_{entity}` (e.g. `auth_user`, `auth_credential`).

**Legacy (V3):** existing tables use mixed naming without a uniform prefix:

| Table | Status |
|-------|--------|
| `users`, `user_credentials`, `user_identities`, `user_profiles` | Legacy — rename deferred |
| `groups`, `group_memberships` | Legacy — rename deferred |
| `auth_events` | Partially aligned (`auth_` prefix) |

New security persistence should follow `auth_*` unless extending a legacy table in place.

### 2.4 Platform / shared

| Table | Prefix rule |
|-------|-------------|
| `relation_record` | **Platform shared** — cross-domain relation graph; no component prefix |
| `saved_query` | Legacy — target `platform_saved_query` when a rename migration is scheduled |

### 2.5 External / vendor-managed tables

Tables created by third-party libraries (e.g. LangChain4j pgvector) are **not** in Flyway unless
we take ownership. Configure the physical name via application properties and align with the
`ai_{feature}_{role}` pattern where we control the default:

```yaml
mill.ai.vector-stores.pgvector.table: ai_value_mapping_vector
```

Operator note: changing the pgvector table name requires re-index/re-ingest; no in-place rename.

---

## 3. Table naming patterns

### 3.1 Aggregate + satellites

```text
{prefix}_{aggregate}              -- parent / root row
{prefix}_{aggregate}_{part}       -- owned child (1:N from parent)
{prefix}_{aggregate}_{role}       -- sibling satellite (also keyed by parent id)
```

Examples (AI chat):

```text
ai_chat                    -- header (user, profile, title, …)
ai_chat_turn               -- transcript lines
ai_chat_memory             -- model memory header
ai_chat_memory_message     -- memory lines (child of ai_chat_memory)
ai_chat_artifact           -- durable artifacts
ai_chat_artifact_pointer   -- latest pointer per key
ai_chat_run_event          -- routed runtime telemetry
```

### 3.2 Junction / link tables

Name both sides when short: `metadata_entity_facet`.  
Prefer `{parent}_{child}` ordering from the **owning** side (the side with `ON DELETE CASCADE`).

### 3.3 Audit / ledger / state

| Suffix | Use |
|--------|-----|
| `_audit` | Append-only audit trail (`metadata_audit`) |
| `_seed` | Bootstrap / migration ledger (`metadata_seed`) |
| `_state` | Operational state snapshot (`ai_value_mapping_state`) |

---

## 4. Column naming

| Kind | Pattern | Examples |
|------|---------|----------|
| Surrogate PK | `{entity}_id` or domain id | `entity_id`, `facet_id`, `scope_id` |
| Natural / string PK | stable domain name | `chat_id`, `artifact_id`, `turn_id`, `event_id` |
| FK to parent | `{parent}_id` | `chat_id`, `embedding_model_id`, `user_id` |
| URN | `urn` or `{scope}_urn` | `urn`, `attribute_urn`, `source_urn` |
| Timestamps | `_at` suffix | `created_at`, `updated_at`, `occurred_at`, `last_refresh_at` |
| Actor | `_by` suffix | `created_by`, `last_modified_by` |
| JSON payload | `_json` suffix | `payload_json`, `metadata_json`, `content_json`, `tags_json` |
| Display / enum | plain noun | `status`, `visibility`, `role`, `kind` |
| Booleans | `is_` prefix or plain adjective | `is_favorite`, `enabled`, `mandatory` |

Avoid redundant table name in column (`ai_chat.chat_id` not `ai_chat.ai_chat_id`).

**Legacy columns** renamed in Flyway when the owning feature migration runs (e.g.
`conversation_id` → `chat_id` in V10).

---

## 5. Constraints and indexes

### 5.1 Primary keys

- Inline on create: `{column} PRIMARY KEY` or `PRIMARY KEY (col_a, col_b)` for composite keys.
- Name explicit PK constraints only when required for portable `ALTER TABLE` (`pk_{table}`).

### 5.2 Foreign keys

```text
fk_{child_table}_{parent_entity}
```

Examples:

```sql
CONSTRAINT fk_ai_chat_turn_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat (chat_id) ON DELETE CASCADE

CONSTRAINT fk_ai_chat_memory_message_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat_memory (chat_id) ON DELETE CASCADE
```

Abbreviated aliases (`fk_mef_entity`) are acceptable for long table names when the meaning stays
obvious in migration context.

### 5.3 Unique constraints

```text
uq_{table}_{column(s)}
```

Examples: `uq_metadata_entity_uuid`, `uq_ai_embedding_model_fingerprint`.

### 5.4 Indexes

```text
idx_{table}_{column(s)}
```

- List columns in index order.
- Drop and recreate indexes when renaming tables (see V11 pattern).
- Partial indexes: suffix purpose if needed (`idx_{table}_{cols}_active`).

### 5.5 Delete semantics

| Relationship | Default |
|--------------|---------|
| Parent aggregate → owned children | `ON DELETE CASCADE` |
| Optional reference (nullable FK) | `ON DELETE SET NULL` or no FK |
| Catalog / golden source | `ON DELETE RESTRICT` (e.g. embedding model ← value mapping) |
| Cross-domain `relation_record` | No FK to domain tables; application-level lifecycle |

Document exceptions in the domain design doc when CASCADE is intentionally omitted.

---

## 6. Flyway migration naming

Script files (unchanged from bootstrap):

```text
V{n}__{snake_case_description}.sql
```

Description should name the **component + action**:

- `V11__ai_chat_table_naming.sql`
- `V12__ai_chat_cascade_delete.sql`
- `V4__metadata_greenfield.sql`

One logical change per version where possible. Table renames and FK additions may be split
(WI-323 / WI-324) when rename must land before constraint enforcement.

---

## 7. JPA entity mapping

```kotlin
@Entity
@Table(name = "ai_chat_artifact")
class ArtifactEntity(
    @Column(name = "chat_id")
    var chatId: String,
    // ...
)
```

Rules:

- Entity **class** names stay domain-oriented (`ArtifactEntity`); **table** names follow this doc.
- Do not rely on Hibernate implicit naming strategies for production tables.
- Repository method names use domain language; SQL/table names appear only in entities and Flyway.

---

## 8. Current table inventory

Status key: **✓** conforms · **~** legacy (acceptable until rename WI) · **cfg** config-only

| Table | Component | Status |
|-------|-----------|--------|
| `ai_chat` | AI chat | ✓ |
| `ai_chat_turn` | AI chat | ✓ |
| `ai_chat_memory` | AI chat | ✓ |
| `ai_chat_memory_message` | AI chat | ✓ |
| `ai_chat_artifact` | AI chat | ✓ |
| `ai_chat_artifact_pointer` | AI chat | ✓ |
| `ai_chat_run_event` | AI chat | ✓ |
| `ai_embedding_model` | AI value mapping | ✓ |
| `ai_value_mapping` | AI value mapping | ✓ |
| `ai_value_mapping_state` | AI value mapping | ✓ |
| `ai_value_mapping_vector` | AI pgvector | cfg |
| `metadata_scope` | Metadata | ✓ |
| `metadata_entity` | Metadata | ✓ |
| `metadata_facet_type_def` | Metadata | ✓ |
| `metadata_facet_type` | Metadata | ✓ |
| `metadata_entity_facet` | Metadata | ✓ |
| `metadata_audit` | Metadata | ✓ |
| `metadata_seed` | Metadata | ✓ |
| `relation_record` | Platform | ✓ (shared) |
| `saved_query` | Platform | ~ |
| `users` | Security | ~ |
| `user_credentials` | Security | ~ |
| `user_identities` | Security | ~ |
| `user_profiles` | Security | ~ |
| `groups` | Security | ~ |
| `group_memberships` | Security | ~ |
| `auth_events` | Security | ~ |

Dropped / historical (do not reuse without intent): `ai_chat_metadata`, `ai_conversation`,
`ai_conversation_turn`, `chat_memory`, `chat_memory_message`, `ai_artifact`, `ai_active_artifact_pointer`,
`ai_run_event` (all superseded by V10–V11).

---

## 9. Checklist for new tables

1. Choose component prefix (§2) and aggregate/satellite pattern (§3).
2. Add migration in `persistence/mill-persistence/src/main/resources/db/migration/`.
3. Name columns per §4; add `created_at` / audit columns consistent with sibling tables.
4. Add `fk_*`, `uq_*`, `idx_*` per §5.
5. Define `ON DELETE` behavior explicitly.
6. Map entity `@Table` / `@Column` to identical names (§7).
7. Extend `FlywayMigrationIT` (or domain IT) to assert the table exists.
8. Update the inventory table in this document when the migration merges.

---

## 10. Related documents

| Document | Role |
|----------|------|
| [persistence-bootstrap.md](./persistence-bootstrap.md) | Module ownership, Flyway placement, contract purity |
| [persistence-overview.md](./persistence-overview.md) | Domain map and persistence lanes |
| [../agentic/v3-conversation-persistence.md](../agentic/v3-conversation-persistence.md) | AI chat table semantics |
| [../metadata/metadata-service-design.md](../metadata/metadata-service-design.md) | Metadata domain model |
