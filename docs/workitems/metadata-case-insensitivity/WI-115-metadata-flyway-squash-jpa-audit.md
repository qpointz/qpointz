# WI-115 — Metadata Flyway squashed migration, JPA, row audit, facet investigation audit

Status: `planned`  
Type: `feature`  
Area: `persistence`, `metadata` — Flyway in `mill-persistence`, JPA in `mill-metadata-persistence`, adapters  
Story: [`STORY.md`](./STORY.md)

## Goal

Replace **V4–V8** metadata migrations with **one** script (greenfield / big-bang): final **`metadata_*` DDL** including **facet type def/inst**, **`metadata_facet`** with **`facet_uid`**, **`metadata_entity`** with **URN-only** identity + **`metadata_entity_type`** FK, **`metadata_entity_type_def`**. Apply **logical column order** and **`created_at`, `created_by`, `last_modified_at`, `last_modified_by`** on **every** `metadata_*` table (`*_by` nullable).

**Facet investigation audit:** **single** append-only table; **JPA entity listeners** on facet type def, facet type inst, and facet instance entities to append rows (design **WI-112**). **Fail-open** on audit insert if agreed (log + continue).

**Facet type seeds:** **`platform-facet-types.json`** ([`metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json)) is the **source of truth**. Produce **`INSERT` SQL** into the squashed Flyway script under **`persistence/mill-persistence/src/main/resources/db/migration/`** (maintain SQL to match JSON, or check in SQL generated from JSON — same outcome: one canonical SQL body in repo).

**Dialect:** **PostgreSQL** is **primary**. **H2** in **PostgreSQL compatibility mode** is **required for local / standalone** test runs where applicable — avoid PG-only features without H2-safe alternatives, or document explicit H2 exclusions.

## Preconditions

- Confirm **no environment** has relied on applied V4–V8 history (or document repair steps for dev DBs).
- **WI-111** — **superseded**; no coordinate merge migration on this branch ([`STORY.md`](./STORY.md)).

## In scope

- **Physically delete** superseded **`V4__metadata.sql` through `V8__…`** files; single new metadata migration after V3.
- JPA entities, repositories, **`JpaMetadataRepository`** (`resolveOrCreateEntityTypeInstance`, remove coordinate repair).
- Row audit column renames (`last_modified_*`) aligned with JPA `@Column`.

## Out of scope

- REST/UI (**WI-116**).
- Removing **file** repository (**WI-117**) — may land same branch but separate WI.
- **`apps/mill-regression-cli`** — ignore unless trivial.

## Code documentation (this WI)

- **Kotlin/Java** (JPA entities, repositories, adapters, listeners): **KDoc/JavaDoc** through **parameter level** on all **new or updated** non-test code in **`mill-metadata-persistence`**, **`mill-persistence`** (if touched), **`mill-metadata-autoconfigure`** (if touched).
- **SQL:** squashed migration file(s) begin with a **comment block** (purpose, tables, greenfield scope).

## Acceptance criteria

- Repo contains **no** orphaned metadata migration files beyond the one squashed script (plus prior `V1`–`V3` as applicable).
- Fresh migrate: schema matches JPA; tests pass on **PostgreSQL** and on **H2 (PostgreSQL compatibility mode)** where the project uses H2 for metadata IT.
- All `metadata_*` tables have the standard audit quad; investigation audit table exists; listeners verified (IT or manual).
- Facet type **`INSERT`s** in Flyway **match** **`platform-facet-types.json`** (reviewer diff or optional codegen — no silent drift).
- **KDoc/JavaDoc** (and SQL header) as above for every **non-test** file touched.

## Commit

One logical commit for this WI, prefix `[feat]`, per `docs/workitems/RULES.md`.
