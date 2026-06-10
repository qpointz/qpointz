# WI-122 — Persistence: Greenfield Flyway, JPA, Row Audit, `metadata_audit`, Seed Ledger

**Story:** Metadata Rework
**Spec sections:** §8, §9, §6.6, §14.1
**Depends on:** WI-120 (domain types needed for JPA mapping)

## Objective

Delete legacy metadata Flyway migrations (**V4–V10** — SQL and Kotlin) and replace them with a **single squashed** greenfield script (**`V4__metadata_greenfield.sql`** — name per SPEC §8.1 after V1–V3).

Update JPA entities and adapter implementations to match the new domain model: **row-audit quad** and **`uuid`** column on all **`metadata_*` business tables** (except **`metadata_audit`**), **`merge_action`** on **`metadata_entity_facet`** (mandatory table rename from **`metadata_facet`**), **`metadata_seed`** ledger, unified **`metadata_audit`** with **listener-only** writers (services never call `MetadataAuditRepository`).

**Remove** **`metadata_promotion`** entirely (no greenfield table; drop legacy code in implementation).

## Scope

### 1. Delete V4–V10 migrations

Delete all existing metadata migrations — SQL files and Kotlin classes (paths as in prior WI revisions under `mill-persistence` / shared migration layout).

V1–V3 (AI/auth) are unrelated — do not touch.

Dev databases must be recreated. No upgrade path.

### 2. Squashed migration `V4__metadata_greenfield.sql` (SPEC §8.3–8.5, §8.7)

Create a **plain SQL** file with a file-level comment describing purpose and scope.

**Normative DDL** — follow **SPEC §8.3** (surrogate `BIGINT` FKs, `type_res` / `entity_res` / `scope_res` unique keys, **`uuid`** on business tables, **`merge_action`** on **`metadata_entity_facet`**, **`metadata_audit`** shape, **`metadata_seed`** with row-audit + **`uuid`**). **Hard drop** legacy metadata tables — no backward compatibility.

**Do not** create **`metadata_promotion`**.

**Platform facet seeds** — SPEC §8.5; **`platform-facet-types.json`** is source of truth — generate **`INSERT`**s **once** when authoring the migration, then maintain JSON and SQL **manually** (use **`type_res`**, not legacy `type_key`).

### 3. Update `platform-facet-types.json` (SPEC §8.5)

Contains only metadata-owned types (**`descriptive`**, **`concept`**). Data-layer types are registered at startup by `SchemaFacetAutoConfiguration`.

### 4. JPA entity updates (SPEC §9)

- Surrogate PKs and **integer FKs** per SPEC; **`MetadataEntityFacetEntity`** on **`metadata_entity_facet`**: **`uuid`**, **`mergeAction`** → **`merge_action`**.
- **`uuid`** column on all auditable entities per SPEC §8.2.
- **`metadata_seed`** JPA entity + Spring Data repository for the ledger (**WI-126** consumes from autoconfigure).

### 5. JPA adapter implementations

**Case-insensitivity (SPEC §4.3):** normalise URN inputs to lowercase before persistence/query.

**`JpaFacetRepository`:** map **`merge_action`** ↔ `FacetInstance.mergeAction`.

### 6. `metadata_audit` listeners (SPEC §8.4, §9.8)

Append one row per lifecycle event on entity / facet / facet-type records. **Fail-open.** Subject vocabulary per SPEC §8.4.

### 7. KDoc coverage

All new and modified JPA entities, adapters, listeners, and repositories in `mill-metadata-persistence` must carry **KDoc to parameter level** on touched production code.

## Done Criteria

- V4–V10 deleted (SQL + Kotlin migration classes).
- `V4__metadata_greenfield.sql` applies cleanly on a fresh H2 PostgreSQL-compatibility DB.
- Flyway migrate succeeds in `testIT` context.
- JPA entities match the greenfield DDL.
- **`metadata_audit`** rows written on facet insert / update / delete (integration test).
- **`metadata_promotion`** absent from DDL and codebase.
- **`metadata_seed`** table present; ledger repository usable by **WI-126**.
- `platform-facet-types.json` aligned with SQL seeds.
- `./gradlew :metadata:mill-metadata-persistence:testIT` passes.
