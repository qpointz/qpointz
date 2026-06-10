# Story: Metadata Rework (Greenfield)

**Branch:** `feat/metadata-rework-final`
**Spec:** [`SPEC.md`](./SPEC.md)
**Handoff / backlog:** [`PLAN.md`](./PLAN.md) · **Branch from `origin/dev`:** [`HANDOFF-FROM-DEV.md`](./HANDOFF-FROM-DEV.md) · **Portable copies of SPEC-linked assets:** [`reference/`](./reference/)

**Git:** After **each** WI, commit **all** files for that WI in **one** commit and leave a **clean** working tree before the next WI (`STORY.md` checkbox in the same commit). See [`docs/workitems/RULES.md`](../../RULES.md) → *Commits → Complete working copy per WI*.

---

## Goal

Greenfield redesign of the metadata subsystem:

- **URN-based entity identity** — any valid `urn:mill/...` entity id (opaque to metadata-core); no JDBC coordinate columns in `metadata/*`.
- **Facets as first-class rows** — `FacetInstance` replaces the denormalized map-on-entity; **uniform** row shape for **SINGLE** and **MULTIPLE**; **`merge_action`** on assignments for overlay semantics.
- **Pure `mill-metadata-core`** — no Spring, no JPA, no direct Jackson; only `mill-core`. Merge / effective-view logic in a **repository-agnostic** reader (e.g. `MetadataReader`), not in JPA repositories.
- **Squashed Flyway migration** — delete legacy metadata migrations **V4–V10**; single greenfield SQL (**`V4__metadata_greenfield.sql`** after V1–V3); row-audit quad on all **`metadata_*` except `metadata_audit`**; **global UUID** per business row; unified **`metadata_audit`** with JPA listeners; **`metadata_seed`** ledger; **no `metadata_promotion`**.
- **Startup seeding** — ordered **`mill.metadata.seed`** Spring resources, ledger-backed **exactly-once** (see **WI-126**); abandon ad-hoc autoconfig-only loads without idempotency.
- **Configuration** — **`mill.metadata.repository.*`** replaces **`mill.metadata.storage.*`** (breaking); file backend retained with **canonical YAML** only (§15).
- **Breaking changes in one cut** — domain, persistence, REST, YAML import, UI.

Full design in [`SPEC.md`](./SPEC.md).

## Success criteria (short)

- `mill-metadata-core` dependency graph clean (no `data/*`).
- H2 `testIT` green after greenfield migration.
- Second process start does **not** re-apply completed seeds.
- UI uses full URNs; facet **DELETE** uses **path** `{facetUid}` (SPEC §10.2).

## Non-Goals

- Backward compatibility of any kind.
- Value mapping bridge — separate story.
- **Metadata promotion workflow (M-23)** — deferred; **`metadata_promotion`** table and related code are **removed**, not carried into greenfield.
- Complex type metadata (M-27) — separate story.
- Reintroduction of the **legacy** file **format** or **`mill.metadata.storage.*`** keys.
- `apps/mill-regression-cli` (AI v1) — out of scope.

---

## Locked Decisions (sync with SPEC §2, §4, §5, §8, §14)

| # | Decision |
|---|----------|
| 1 | `mill-metadata-core` depends **only** on `mill-core`. No Spring, JPA, or direct Jackson. |
| 2 | Entity identity = **full Mill URN** (`urn:mill/...`). Group/class/id are **opaque** to metadata-core (e.g. `urn:mill/model/table:…`, `urn:mill/metadata/entity:…`). |
| 3 | Metadata has **no notion of schema, table, or attribute**. Those are `data/mill-data-schema-*` concerns. |
| 4 | Entities are **flat and independent** — no hierarchy, no parent-child. |
| 5 | `FacetInstance` is a first-class row. `entity.facets` map eliminated. **SINGLE** vs **MULTIPLE** affects **write/resolve policy only** — same persistence shape; **flat list** in API. |
| 6 | Service implementations are plain constructor-injected classes; Spring `@Bean` wiring in autoconfigure only. **Effective facet merge** lives in **core** (`MetadataReader` or equivalent), not in repositories. |
| 7 | Legacy metadata Flyway **V4–V10** deleted; single greenfield migration **`V4__metadata_greenfield.sql`**; dev databases must be recreated. |
| 8 | Platform facet types: **Flyway SQL** from **`platform-facet-types.json`** (one-time generate, then manual sync); extra data via **`mill.metadata.seed`**. Data-owned types registered at startup by schema autoconfig remain. |
| 9 | **File adapter kept**: canonical multi-document YAML (§15); **legacy** file format and **`mill.metadata.storage.*`** removed; use **`mill.metadata.repository.type=file`**. |
| 10 | All `mill.metadata.*` configuration in **Java** `@ConfigurationProperties` (Spring metadata generation). |
| 11 | PostgreSQL primary; H2 PostgreSQL-compatibility mode for `testIT`. |
| 12 | **Metadata is a permissive store.** Any entity URN, facet type key, and payload is accepted. Unknown types auto-create `OBSERVED` records. `applicableTo` and `contentSchema` are advisory — never enforced by metadata-core. |
| 13 | **`MetadataContext` scope order is caller-defined** (not only a fixed RBAC stack); last-wins; examples include global + chat (AI v3). |
| 14 | **Startup metadata** = **`mill.metadata.seed`** ordered resources + **`metadata_seed`** ledger + core **`MetadataSeedLedgerRepository`** (**WI-126**). |
| 15 | **Documentation standards for implementation:** production **Kotlin/Java** — KDoc/JavaDoc to **parameter** level on touched code; **TypeScript UI** — JSDoc/TSDoc to **function** level on touched exports. |
| 16 | **`metadata_audit`:** written **only** by **JPA entity listeners** in `mill-metadata-persistence`. Services and REST **do not** inject or call `MetadataAuditRepository`. |
| 17 | **Cross-system ids:** DDL column name **`uuid`** on every auditable `metadata_*` table (except `metadata_audit`). Facet assignment **`uuid`** = domain `FacetInstance.uid` / REST `{facetUid}`. |
| 18 | **Assignments table:** mandatory rename **`metadata_facet` → `metadata_entity_facet`** in greenfield Flyway; no backward compatibility. |
| 19 | **Platform facet types:** source of truth **`platform-facet-types.json`**; **one-time** `INSERT` generation into **`V4__metadata_greenfield.sql`**, then **manual** JSON/SQL sync. |
| 20 | **`merge_action`:** **not** on **`FacetInstanceDto`** / **`GET .../facets`**. **`GET .../entities/{id}/facets/merge-trace`** exposes merge chain for multi-scope UI. |
| 21 | **Unassign:** physical **DELETE** only for **`merge_action == SET`** in **non-overlay** scopes (v1: **`GLOBAL`** + whitelist). Otherwise **TOMBSTONE**. |
| 22 | **`MetadataView`** in **`mill-metadata-core`:** **`data/*`** uses it (repos + `MetadataContext`) for **read** metadata; no direct metadata repository use on those read paths. |
| 23 | **Canonical YAML (§11.0):** **§15 multi-document `kind:`** is the **normative** interchange; **`metadataFormat: CANONICAL`** samples are **migration/fixture** shapes unless an explicit mapper is added. |

---

## Work item execution order

Run work items in this order. Steps marked **parallel** may be done concurrently on the same story branch; steps marked **overlap** share a time window but still have finish dependencies.

| # | Work item | Notes |
|---|-----------|--------|
| 1 | **WI-119** | Design lock; unblocks domain and JDBC work. |
| 2 | **WI-120** and **WI-121** | **Parallel** after WI-119 (same milestone; no sub-branches unless one depends on the other’s merge). |
| 3 | **WI-122** | Greenfield persistence; **must complete before** WI-123. |
| 4 | **WI-123** | REST, import/export, UI. |
| 5 | **WI-126** | **Overlap** late WI-122 / early WI-123; finish before the story is considered **seed-complete** (ledger + `mill.metadata.seed`). |
| 6 | **WI-124** | Cleanup and config rename; after WI-123 and WI-126 are far enough along that sweep is meaningful. |
| 7 | **WI-127** | Design docs + domain model; **after** WI-124. |
| 8 | **WI-128** | Public docs; **after** WI-124; **recommended after** WI-127 so public pages can link to the domain model doc. |
| 9 | **WI-125** | Story closure — **last**; MILESTONE, BACKLOG, final verify (**assumes WI-127 and WI-128 done**). Story folder **archived** to `docs/workitems/completed/20260330-metadata-rework/` per [`RULES.md`](../../RULES.md). |

**Linear shorthand (dependencies only):**  
`119 → (120 ∥ 121) → 122 → 123` with `126` overlapping `122`/`123` → `124` → `127` → `128` → `125`.

---

## Work Items

Ordered delivery: see **[Work item execution order](#work-item-execution-order)** above.

- [x] WI-119 — Design lock: URN grammar, `FacetInstance` + `merge_action`, `MetadataEntityUrnCodec`, **`metadata_audit`**, REST surface ([`WI-119-design-lock.md`](./WI-119-design-lock.md))
- [x] WI-120 — Domain + service rework: coordinates stripped, `FacetInstance`, **`MetadataReader`** / merge, services ([`WI-120-domain-service-rework.md`](./WI-120-domain-service-rework.md))
- [x] WI-121 — JDBC binding: `MetadataEntityUrnCodec` + `SchemaFacetService` URN integration ([`WI-121-jdbc-binding-urn-codec.md`](./WI-121-jdbc-binding-urn-codec.md))
- [x] WI-122 — Persistence: greenfield Flyway, JPA, row audit, **global UUID**, **`merge_action`**, **`metadata_seed`**, **`metadata_audit`**, drop **`metadata_promotion`** ([`WI-122-persistence-flyway-jpa-audit.md`](./WI-122-persistence-flyway-jpa-audit.md))
- [x] WI-123 — REST + import/export + mill-ui: endpoints, OpenAPI, YAML, UI URN construction ([`WI-123-rest-import-ui.md`](./WI-123-rest-import-ui.md))
- [x] WI-124 — Cleanup: legacy format removal, dead code sweep, **`mill.metadata.repository.*`**, Java `@ConfigurationProperties` ([`WI-124-cleanup.md`](./WI-124-cleanup.md))
- [x] WI-125 — Story closure: MILESTONE, BACKLOG, final verify; story folder archived ([`WI-125-design-doc-sync.md`](./WI-125-design-doc-sync.md))
- [x] WI-126 — Startup metadata seed: `mill.metadata.seed`, ledger, runner, Java properties ([`WI-126-metadata-startup-seed.md`](./WI-126-metadata-startup-seed.md))
- [x] WI-127 — Design docs review + **`mill-metadata-domain-model.md`** + design/inventory Spring config ([`WI-127-metadata-design-docs-domain-model.md`](./WI-127-metadata-design-docs-domain-model.md))
- [x] WI-128 — Public user docs (`docs/public/`) + Metadata `nav` + public Spring config ([`WI-128-metadata-public-user-docs.md`](./WI-128-metadata-public-user-docs.md))

## Documentation checklist

| Area | Owner |
|------|--------|
| **`mill-metadata-domain-model.md`**, full `docs/design/metadata/` review, `docs/design/**` metadata refs, `05-configuration-keys.md`, `CONFIGURATION_INVENTORY.md`, `MILESTONE.md` (if needed) | **WI-127** |
| **`docs/public/mkdocs.yml`** Metadata `nav`, `metadata/system.md`, refresh `metadata/*`, `mill-ui.md`, `grinder-ui.md`, all `mill.metadata*` under `docs/public/src` | **WI-128** |
| Final `rg` under entire **`docs/`** for stale normative **`storage.*`** | **WI-125** spot-check (or last of 127/128 to finish runs a final pass) |

**WI-125** spot-checks and closes the story (tracking + verify). This story lives under **`docs/workitems/completed/20260330-metadata-rework/`** so **SPEC.md** and WI files remain the historical planning record ([`RULES.md`](../../RULES.md) story closure).

### Post–WI-128 docs — known facet stereotypes (`feat/metadata-rework-final`)

- **Design:** [`docs/design/metadata/mill-ui-facet-stereotypes.md`](../../../design/metadata/mill-ui-facet-stereotypes.md) — tags Mill UI implements (`hyperlink`, `email`, `tags`), precedence, wire shapes, maintenance hooks.  
- **Public:** [`docs/public/src/metadata/facet-stereotypes.md`](../../../public/src/metadata/facet-stereotypes.md) — operator/analyst summary; linked from **Concepts** and MkDocs **Metadata** nav.  
- **Tracking:** `docs/workitems/BACKLOG.md` **M-30** (done); `docs/workitems/MILESTONE.md` 0.7.0 — Mill UI facet polish on this branch (list filters, delete confirm, MULTIPLE captions, hyperlink read layout, expert YAML save parity).

These items complete the documentation slice for **story closure** alongside merge-ready code review.
