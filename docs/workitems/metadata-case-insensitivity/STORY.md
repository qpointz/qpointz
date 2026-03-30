# Story: Metadata identity, URN platform, and persistence

**Goal:** Deliver a **binding-neutral** metadata model: **opaque instance URNs** and pluggable JDBC (and future) bindings; **no catalog coordinates** (`schema` / `table` / `attribute`) in `metadata/` core APIs or JPA. Fold **Flyway V4–V8** into a **single greenfield migration** with **standard row audit columns** on all `metadata_*` tables and **append-only investigation audit** for **facet type** and **facet instance** changes. **URN canonicalisation** at the edge replaces separate coordinate case-merge work (**WI-111** superseded for this story — see **Locked decisions**).

**Non-goal:** **No backward compatibility** for this story — API, import, UI, OpenAPI, and persistence shape change in one cut; **no** legacy id acceptance, **no** deprecation period, **no** dual DTO fields or runtime shims. Operators fix YAML or dev DBs **offline** if needed.

**Cleanup:** **Delete** superseded code and leftovers (**WI-117** + plan Phase 5) — no hoarding removed endpoints, dead helpers, or duplicate migration files.

**Planning reference (Cursor):** `.cursor/plans/Generic metadata entity IDs-acbc5404.plan.md` — optional local planning aid (`.cursor/` is gitignored); **authoritative** decisions are in this file + **WI-112** + `docs/design/metadata/`.

**Branch:** Continue from **`fix/ui-fixes`** (or its descendant) for this work — same components overlap with UI fixes; linear history is acceptable. Optionally rename/split to a feature branch name when convenient; **do not** require a clean branch-from-`origin/dev` solely for isolation.

## Code documentation (all touched production code)

Matches repository rules (**`CLAUDE.md`** / **`AGENTS.md`**): anything **new or modified** in this story must carry **JavaDoc** or **KDoc** down to **type, method, and parameter** level (including configuration classes, controllers, DTOs, repositories, entities, listeners, services).

- **Exempt:** test classes and test methods; generated code (Protobuf, OpenAPI clients).
- **`ui/mill-ui`:** document to **function / exported API** level only — JSDoc or TSDoc on significant **functions**, hooks, and service methods; describe component **behaviour** and key handlers where non-obvious (**not** parameter-level on every React prop).
- **SQL migrations:** file-level / section header comment describing purpose and scope of the change.

Each implementation WI repeats this in **Acceptance criteria** for reviewers.

## Locked decisions (product / architecture)

| # | Topic | Decision |
|---|--------|----------|
| 1 | **WI-111** | **(b) Greenfield:** no coordinate duplicate-merge migration. **Supersede WI-111** for this story; identity is **URN-only** after cutover. |
| 2 | **Branch** | Work may branch from / continue **`fix/ui-fixes`** (see **Branch** paragraph above). |
| 3 | **URN grammar (metadata domain)** | Pattern: **`urn:mill/<group>/<class>:<id>`** describes **Mill metadata identifiers** (facet types, entity types, scopes, **instance ids**, etc.) — **not** a JDBC or catalog syntax. **group** = topic namespace; **class** = kind of resource; **id** = unique id (opaque to metadata-core except grammar). Example: `urn:mill/metadata/facet-type:relation` (SoT [`platform-facet-types.json`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json)). **REST path segments:** [`UrnSlug`](../../../core/mill-core/src/main/java/io/qpointz/mill/UrnSlug.java). JDBC bindings **encode** physical locations **into** instance URNs at the **data** layer (**WI-113**); they do not redefine this grammar. |
| 4 | **Cursor plan file** | Optional / local only — **OK** if not in git. |
| 5 | **Relation / JDBC-shaped facets** | **Opaque URN strings** in stored JSON where entity references appear; **grammar** is the only enforcement in metadata-core. **Typed** relation / JDBC interpretation → **`mill-data-schema-core`** (move **`RelationFacet`** and similar there). |
| 6 | **Facet investigation audit** | **Single** append-only table; **JPA entity listeners** (or equivalent) to record facet type def / inst / facet instance mutations. |
| 7 | **Row audit vs append-only audit** | **Row-level** quad (`created_*`, `last_modified_*`) = **current state** on each `metadata_*` row. **Append-only** audit (e.g. `metadata_operation_audit`, facet investigation) = **events** with richer context (**user id**, request/correlation id, payload snapshots). They are **complementary**: typically **one append-only event per logical mutation** referencing the subject row (entity / facet) — treat as **1:1 with the change event**, not a duplicate copy of every row column. Document exact FK/column shape in **WI-112** / **WI-115**. |
| 8 | **Design / user docs** | Tracked in **Design & inventory doc checklist** below — **WI-116** / **WI-118** check every box. |
| 9 | **File-backed metadata** | **Drop `FileMetadataRepository` / `mill.metadata.storage.type=file` for this story** (leaning confirmed). Reintroduce a file adapter **later** when the model is stable. **`MetadataRepository` wiring** = **JPA** (and **NoOp** where appropriate only); any future **composite** aggregates **compatible** implementations only (no file+JPA merge until file mode returns). |
| 10 | **Facet type seeds → SQL** | **Source of truth:** [`platform-facet-types.json`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json). Maintain **`INSERT` SQL** in **`persistence/mill-persistence/.../db/migration/`** (or generated `.sql` checked in from JSON). JSON and SQL must not diverge. |
| 11 | **Module dependencies** | **`metadata/*`** may depend only on **`metadata/*`** + **`core/*`** (e.g. `mill-core`). **No** dependencies on **`data/*`**, **`ai/*`**, **`apps/*`**, **`clients/*`**. **`data/*`** may depend on **`metadata/*`**. Typed JDBC facet DTOs (**`RelationFacet`**, …) in **`mill-data-schema-core`**; **metadata-core** does **not** import them. |
| 12 | **Spring config metadata** | **Java** `@ConfigurationProperties` for changed `mill.metadata.*` so **`spring-boot-configuration-processor`** emits IDE metadata (**WI-117**). |
| 13 | **Database dialect** | **PostgreSQL** primary; **H2** in **PostgreSQL compatibility mode** for local runs (**WI-115**). |
| 14 | **mill-regression-cli** | **Out of scope** (AI v1); fix only if trivial. |

## Design & inventory doc checklist (track in PR)

Check off during **WI-116** and verify in **WI-118**.

- [ ] [`docs/public/src/grinder-ui.md`](../../public/src/grinder-ui.md)
- [ ] [`docs/design/metadata/metadata-service-design.md`](../../design/metadata/metadata-service-design.md)
- [ ] [`docs/design/metadata/metadata-documentation.md`](../../design/metadata/metadata-documentation.md)
- [ ] [`docs/design/metadata/metadata-implementation-roadmap.md`](../../design/metadata/metadata-implementation-roadmap.md)
- [ ] [`docs/design/metadata/model-view-facet-boxes.md`](../../design/metadata/model-view-facet-boxes.md)
- [ ] [`docs/design/refactoring/05-configuration-keys.md`](../../design/refactoring/05-configuration-keys.md)
- [ ] [`docs/design/platform/CONFIGURATION_INVENTORY.md`](../../design/platform/CONFIGURATION_INVENTORY.md)
- [ ] [`docs/design/refactoring/02-file-inventory.md`](../../design/refactoring/02-file-inventory.md)
- [ ] [`docs/design/platform/webflux-migration-plan.md`](../../design/platform/webflux-migration-plan.md)

## Work Items

- [ ] ~~WI-111~~ — **Superseded** for this story (greenfield); file retained for history — see [`WI-111-metadata-case-insensitive-identity.md`](./WI-111-metadata-case-insensitive-identity.md).
- [ ] WI-112 — URN platform design doc (`WI-112-metadata-urn-platform-design-doc.md`)
- [ ] WI-113 — JDBC binding: instance URN codec + schema facet join (`WI-113-jdbc-metadata-entity-urn-codec.md`)
- [ ] WI-114 — metadata-core: URN domain + strip JDBC from entity/repository (`WI-114-metadata-core-urn-domain-repository.md`)
- [ ] WI-115 — Flyway squashed metadata DDL + JPA + row audit + facet investigation audit (`WI-115-metadata-flyway-squash-jpa-audit.md`)
- [ ] WI-116 — REST, import/YAML, schema explorer, mill-ui, **public + design docs** (`WI-116-metadata-rest-import-ui-urn.md`)
- [ ] WI-117 — Tests, remove file repo, dead code cleanup (`WI-117-metadata-tests-file-repo-cleanup.md`)
- [ ] WI-118 — Sync `docs/design/metadata` with as-built model (`WI-118-metadata-design-doc-as-built-sync.md`)

## Suggested implementation order

1. **WI-112** — Locks URN grammar, `EntityPath` / `TypedEntityLocator`, binding registry, audit/event vs row-audit notes; unblocks 113/114.
2. **WI-113** and **WI-114** — In parallel after **WI-112** where possible; **114** must not assume DB migration is applied until **115** lands.
3. **WI-115** — Blocks **WI-116**; squashed migration; facet **`INSERT`s from `platform-facet-types.json`** as SQL under **`mill-persistence`**; PostgreSQL + H2 PG mode; row audit quad; **single** facet investigation table + **listeners**.
4. **WI-116** — REST, UI, import, OpenAPI, **`docs/public`** + **`docs/design/metadata`** updates for new contracts.
5. **WI-117** — Remove **file** metadata repository + autoconfigure paths; tests; delete dead code.
6. **WI-118** — Reconcile **`docs/design/metadata/`** with **final** DDL, JPA, APIs, audit (**as-built**); closes gap vs **WI-112** draft.
