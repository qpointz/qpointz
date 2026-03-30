# WI-112 — Metadata URN platform design doc

Status: `planned`  
Type: `docs` / `feature` (architecture)  
Area: `metadata`, `persistence`, `data`  
Story: [`STORY.md`](./STORY.md)

## Goal

Author **`docs/design/metadata/`** content that locks: **URN grammar**, **binding registry** (JDBC today; extension points for non-relational bindings), **`EntityPath`** + **`TypedEntityLocator`**, **entity-kind** def vs runtime tables (parallel to facet def/inst), **relation / cross-entity references** as **opaque URNs** in core (typed DTOs only in **`mill-data-schema-core`**), **row-level audit** vs **append-only event audit**, and **squashed Flyway** shape (single investigation-audit table, listeners).

## URN grammar (locked — duplicate in STORY)

This grammar describes **Mill metadata identifiers** only — **not** JDBC, SQL, or physical catalog syntax. Bindings (e.g. JDBC in **WI-113**) **encode** their world into `<id>` (or agreed substructure); they do not replace this pattern.

- **Form:** `urn:mill/<group>/<class>:<id>`
  - **`<group>`** — topic namespace grouping related classes (e.g. `metadata`).
  - **`<class>`** — kind of resource (e.g. `facet-type`, `entity-type`, `entity`, …); may use path segments before `:` per product convention (e.g. `metadata/facet-type`).
  - **`<id>`** — unique id for that class; **opaque** to metadata-core for instance and reference strings; binding-specific encoding allowed.
- **Examples:** `urn:mill/metadata/facet-type:descriptive`, `urn:mill/metadata/facet-type:relation`, … — all platform facet **`typeKey`** values must match **[`platform-facet-types.json`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json)** (single source of truth).
- **REST URL mapping:** document use of [`UrnSlug`](../../../core/mill-core/src/main/java/io/qpointz/mill/UrnSlug.java) (full slug vs namespace-prefixed slug) for path variables that carry full URNs.

## In scope

- Binding-neutral invariants: metadata-core must not interpret catalog tuples; **instance** vs **entity-type** URNs.
- **Module dependency rule** (duplicate STORY): **`metadata/*`** → **`metadata/*`** + **`core/*`** only; **`data/*`** may depend on **`metadata/*`**; metadata **never** depends on **`data/*`**.
- **Append-only audit vs row audit:** row quad on each `metadata_*` table = **state**; append-only tables (`metadata_operation_audit`, **single** facet-investigation table) = **events** with optional **user id**, correlation id, before/after payload — **one event per logical mutation**, FK/reference to subject row where useful (complementary to `last_modified_*`, not a redundant full row copy).
- **Investigation audit:** **one** table, **JPA listeners** (design in this doc; implement **WI-115**).
- Cross-reference **WI-115:** Flyway seeds must stay in sync with `platform-facet-types.json` (parity test or codegen).
- Optional **offline** appendix for operators rewriting old YAML — **not** a runtime API contract.

## Out of scope

- Implementation code (other WIs).
- **As-built** refresh after code lands — **[WI-118](WI-118-metadata-design-doc-as-built-sync.md)**.
- **Backward compatibility** — greenfield story; see [`STORY.md`](./STORY.md).

## Code documentation (this WI)

- **Deliverable is markdown** under `docs/design/metadata/`. Any **embedded code samples** (Kotlin/Java/SQL snippets) in those docs must be **annotated or preceded by prose** so intent is clear.
- **No production module code** in this WI — full **KDoc/JavaDoc** rules apply starting **WI-113**.

## Acceptance criteria

- New or updated markdown under `docs/design/metadata/` checked into the branch.
- Grammar, `UrnSlug`, JSON SoT, audit model, and file-repo removal intent are reflected consistently with [`STORY.md`](./STORY.md).

## Commit

One logical commit for this WI, prefix `[docs]`, per `docs/workitems/RULES.md`.
