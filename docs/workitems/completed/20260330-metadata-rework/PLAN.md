# Metadata rework — handoff plan (agents)

**Story branch:** `feat/metadata-rework-final`. Rebase from `origin/dev` before push ([`docs/workitems/RULES.md`](../../RULES.md), root [`CLAUDE.md`](../../../../CLAUDE.md)).

This file is the **narrative backlog**: rationale, open design questions, and verification hints. **Normative detail** lives in **[`SPEC.md`](./SPEC.md)**; **execution order and checkboxes** in **[`STORY.md`](./STORY.md)**. Keep this plan in sync when decisions change.

---

## For the receiving agent (read first)

1. If you branch from **`origin/dev`**, read **[`HANDOFF-FROM-DEV.md`](./HANDOFF-FROM-DEV.md)** — rebasing the full feature branch vs cherry-picking only docs; use **[`reference/`](./reference/)** when `test/datasets/` is missing.
2. Open **[`STORY.md`](./STORY.md)** — implementation order, locked decisions, WI checklist.
3. Skim **[`SPEC.md`](./SPEC.md)** for the areas your WI touches (§§ domain, 8 DDL, 10 REST, 14–15 config/YAML).
4. Open the **WI file** for the item you are implementing (scope + done criteria).
5. Use **this PLAN** for background sections below (canonical YAML tension, merge semantics, seeds, config rename) and **Verification**.

**Git:** One commit per WI with **all** related files and **`STORY.md`** checkbox updated; clean working tree before the next WI ([`RULES.md`](../../RULES.md) — *Complete working copy per WI*).

**Implementation doc standards:** KDoc/JavaDoc to **parameter** level on touched Kotlin/Java; TS to **function** level; new **`@ConfigurationProperties`** in **Java** (see end of this file).

---

## Folder contents (sufficient for handoff)

Everything needed to run the story from docs lives **in this folder**:

| File | Purpose |
|------|---------|
| [`SPEC.md`](./SPEC.md) | Greenfield specification (Draft). |
| [`STORY.md`](./STORY.md) | Goal, non-goals, locked table, **WI 119–128** checklist, doc ownership table. |
| [`PLAN.md`](./PLAN.md) | This document. |
| [`HANDOFF-FROM-DEV.md`](./HANDOFF-FROM-DEV.md) | Rebasing vs `origin/dev`, commit topology, when **`reference/`** snapshots are needed. |
| [`reference/`](./reference/) | Mirrors of `test/datasets/*` canonical YAML + key design docs **not on `origin/dev`** (see [`reference/README.md`](./reference/README.md)). |
| [`WI-119-design-lock.md`](./WI-119-design-lock.md) | Design lock: URNs, `FacetInstance` + `merge_action`, `metadata_audit`, REST. |
| [`WI-120-domain-service-rework.md`](./WI-120-domain-service-rework.md) | Core domain + services + `MetadataReader` / merge. |
| [`WI-121-jdbc-binding-urn-codec.md`](./WI-121-jdbc-binding-urn-codec.md) | `MetadataEntityUrnCodec` + schema layer. |
| [`WI-122-persistence-flyway-jpa-audit.md`](./WI-122-persistence-flyway-jpa-audit.md) | Flyway greenfield, JPA, `metadata_seed`, no `metadata_promotion`. |
| [`WI-123-rest-import-ui.md`](./WI-123-rest-import-ui.md) | REST, OpenAPI, YAML, **`ui/mill-ui`**. |
| [`WI-124-cleanup.md`](./WI-124-cleanup.md) | Legacy removal, **`mill.metadata.repository.*`**, Java properties. |
| [`WI-126-metadata-startup-seed.md`](./WI-126-metadata-startup-seed.md) | **`mill.metadata.seed`**, ledger, runner. |
| [`WI-127-metadata-design-docs-domain-model.md`](./WI-127-metadata-design-docs-domain-model.md) | Design docs + [`mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md) + design-side config docs. |
| [`WI-128-metadata-public-user-docs.md`](./WI-128-metadata-public-user-docs.md) | Public MkDocs metadata section + `docs/public` config prose. |
| [`WI-125-design-doc-sync.md`](./WI-125-design-doc-sync.md) | **Last:** MILESTONE/BACKLOG, final `rg` spot-check; story **archived** to this folder per [`RULES.md`](../../RULES.md). |

**Outside this folder (durable after closure):** [`docs/design/metadata/`](../../../design/metadata/) (including domain model stub → completed in WI-127), [`docs/public/`](../../../public/), [`docs/workitems/MILESTONE.md`](../../MILESTONE.md), [`docs/workitems/BACKLOG.md`](../../BACKLOG.md).

---

## Work item index and order

Execute in this order (details in each WI file):

| Step | WI | Depends on (typical) |
|------|-----|----------------------|
| 1 | **119** | — |
| 2 | **120**, **121** (parallel) | 119 |
| 3 | **122** | 120 (domain for JPA) |
| 4 | **123** | 122 |
| 5 | **126** | 122 + 120 (overlap late 122 / early 123 OK) |
| 6 | **124** | 123 |
| 7 | **127** | 124 |
| 8 | **128** | 124; **recommended after 127** (link to domain model doc) |
| 9 | **125** | 127, 128, 124 |

---

## Primary code and UI paths (repo root)

| Area | Path |
|------|------|
| Metadata core | `metadata/mill-metadata-core/` |
| JPA / Flyway | `metadata/mill-metadata-persistence/` (confirm shared migration layout with `mill-persistence` per SPEC) |
| Autoconfigure | `metadata/mill-metadata-autoconfigure/` |
| REST | `metadata/mill-metadata-service/` |
| Main app / samples | `apps/mill-service/`, sample `application*.yml` |
| UI (current) | `ui/mill-ui/` (not legacy grinder UI) |

Build/test: `./gradlew :metadata:...` from repo root ([`CLAUDE.md`](../../../../CLAUDE.md)).

---

## Resolved product decisions (sync with SPEC / STORY)

- **Canonical YAML:** **§11.0** — **§15 multi-document** is normative interchange; **`metadataFormat: CANONICAL`** files are migration/fixture shapes (see **Reference — canonical YAML** below).
- **Platform facet SQL:** **`platform-facet-types.json`** is source of truth; **one-time** `INSERT` authoring for **`V4__metadata_greenfield.sql`**, then **manual** JSON/SQL sync.
- **Audit:** **JPA listeners only** — services and REST do not use `MetadataAuditRepository`.
- **`uuid` column** on all auditable `metadata_*` tables; assignment table **`metadata_entity_facet`** (mandatory rename).
- **`FacetInstanceDto`:** no **`mergeAction`**; **`GET .../facets/merge-trace`** for UI merge chain.
- **Unassign:** **DELETE** row only for **`SET`** in **non-overlay** scopes; else **TOMBSTONE**.
- **`MetadataView`:** **`data/*`** read paths use it instead of raw metadata repositories.
- **Facet payload field `stereotype` (UI-only):** Each field in a facet type’s **payload object schema** may carry an optional string **`stereotype`**, default **null** / omitted. It has **no** server-side semantics (metadata does not validate or branch on it). It exists so the **UI** can choose controls — e.g. **`stereotype: table`** → show a **tables dropdown** instead of a plain text box for a string field. The **facet types admin** UI (`ui/mill-ui`, facet type editor) must allow **viewing and editing** `stereotype` alongside field name / required / schema. Entity/facet value editors may later honor known stereotypes. Persist on **`FacetPayloadField`** / manifest JSON as today’s payload schema tree; align **SPEC** (facet manifest / §15 if needed) when locking prose.

---

## Reference — “canonical YAML” as exercised in repo test data

Concrete **legacy → canonical** behaviour is documented by:

- **[`test/datasets/convert_to_canonical_yaml.py`](../../../../test/datasets/convert_to_canonical_yaml.py)** — one-off helper: adds envelope, rewrites ids and facet/scope keys to URNs, maps `structural` by entity `type`.

**Input (legacy repository YAML)** — see e.g. [`skymill-meta-repository.yaml`](../../../../test/datasets/skymill/skymill-meta-repository.yaml), [`moneta-meta-repository.yaml`](../../../../test/datasets/moneta/moneta-meta-repository.yaml):

- Root `entities:` list; entity `id` is a **dot-path** (`skymill`, `skymill.cities`).
- `type`: `SCHEMA` | `TABLE` | `ATTRIBUTE`; **coordinate fields** `schemaName` / `tableName` / `attributeName` on entities.
- `facets`: **short keys** (`descriptive`, `structural`, `relation`, …) with scope shorthand `global:` (or maps keyed by scope).

**Output (canonical YAML)** — paired files [`skymill-meta-canonical.yaml`](../../../../test/datasets/skymill/skymill-meta-canonical.yaml), [`moneta-meta-canonical.yaml`](../../../../test/datasets/moneta/moneta-meta-canonical.yaml):

**Output (SPEC §15.2 multi-document)** — [`build_multidoc_metadata_fixtures.py`](../../../../test/datasets/build_multidoc_metadata_fixtures.py) produces [`skymill-meta-multidoc-v1.yaml`](../../../../test/datasets/skymill/skymill-meta-multidoc-v1.yaml) and [`moneta-meta-multidoc-v1.yaml`](../../../../test/datasets/moneta/moneta-meta-multidoc-v1.yaml) (facet defs from `platform-facet-types.json` + `MetadataScope` + `MetadataEntity` docs).

- Root envelope: **`metadataFormat: CANONICAL`**, **`formatVersion: 1`**.
- Entity **`id`** → **`urn:mill/metadata/entity:<legacy-dot-id>`** (e.g. `urn:mill/metadata/entity:skymill.cities`).
- **Facet type keys** → full URNs (`urn:mill/metadata/facet-type:descriptive`, `relation`, …).
- **Scope keys** → `urn:mill/metadata/scope:global` (and script maps `user:` / `team:` / `role:` prefixes into `urn:mill/metadata/scope:...`).
- **`structural` split** (script-specific): for `type: TABLE` → `urn:mill/metadata/facet-type:source-table` + slim payload; for `type: ATTRIBUTE` → `urn:mill/metadata/facet-type:source-column`; else remains `urn:mill/metadata/facet-type:structural`.
- Coordinate columns are **dropped** from the canonical entity rows in the sample output; timestamps stay as `createdAt` / `updatedAt` (not yet aligned with SPEC row-audit naming).

**Resolution (SPEC §11.0):** **§15 multi-document `kind:`** is the **normative** interchange for import, seeds, and file backend. The **`metadataFormat: CANONICAL`** + **`entities:`** files are **migration/fixture** shapes; optional explicit parser or **offline** conversion to §15 — not an undocumented second normative format.

## New requirement — startup metadata seeding (user direction)

**Abandon** the current pattern of scattering metadata work across **ad-hoc on-startup** hooks (e.g. classpath loads, facet registration, or implicit autoconfig side effects without a single idempotency story).

**Replace with** explicit **repository seeding**:

| Aspect | Specification |
|--------|----------------|
| **Configuration** | `mill.metadata.seed.*` — primarily a **ordered list of Spring resource locations** (e.g. `classpath:metadata/seeds/01-platform.yml`, `file:...`). Order in the list is execution order. |
| **Independence** | Each configured resource is a **separate seed unit**; failure policy (fail-fast vs continue) should be stated in SPEC (recommend **fail-fast** after recording attempt or **skip-if-already-done** only). |
| **Exactly once** | Each seed executes **at most once per environment** (survives restarts). Persistence is required for JPA mode: a **seed ledger** row (or equivalent) written when a seed completes successfully. |
| **Contract** | Expose through **`mill-metadata-core`**: `MetadataSeedLedgerRepository` with `findBySeedKey`, `recordCompletion(seedKey, metadata)` — **no Spring** in the interface; JPA impl in `mill-metadata-persistence`. `seedKey` is location-based; `metadata.fingerprint` holds `md5:` hex for change detection. |
| **Import path** | Reuse the same **canonical YAML / import semantics** as `MetadataImportService` (SPEC §7.5 / §11) so seeds are not a second format. |

**Clarify vs Flyway (SPEC §8.5):** Decide and document one of:

- **A)** Flyway keeps **only** structural DDL + minimal bootstrap (e.g. global scope row); **all** facet-type definitions and sample data move to `mill.metadata.seed` resources; or  
- **B)** Flyway retains **SQL INSERT** seeds for a minimal platform set; **additional** seeds only via `mill.metadata.seed` (two sources — document precedence).

Recommendation: **A** for facet definitions that are “data” — single source of truth in versioned YAML under classpath + ordered seed list; Flyway stays schema + empty tables (except unavoidable bootstrap like `metadata_scope` global if not seeded).

**Autoconfigure:** A startup **runner** (Spring, lives in `mill-metadata-autoconfigure`) iterates the list, checks ledger, runs import for pending entries. **`@ConfigurationProperties`** for `mill.metadata.seed` in **Java** (per CLAUDE.md / SPEC §14).

## Metadata promotion — out of scope for this story (user direction)

**Excluded for now:** No **`metadata_promotion`** table in the **greenfield** DDL, no persistence or API surface for promotion in this rework.

**Drop existing:** Remove **`metadata_promotion`** and **all related code** created for the old workflow (e.g. **`MetadataPromotionEntity`**, **`MetadataPromotionJpaRepository`**, any services/controllers/config that reference promotion). Today’s table originates from **`V4__metadata.sql`** and is altered in later migrations — the squashed greenfield migration must **omit** it entirely.

**Docs:** Delete **§8** / **§8a** references to “keep **`metadata_promotion`** shape”; align **STORY** non-goals (promotion workflow **M-23** remains deferred). **WI-122** must not list promotion DDL.

## Scope merging + read model (user direction)

**Canonical rule (only this is fixed):** Merge uses an **explicit ordered list of scope URNs** (`MetadataContext`) and **last-wins** per facet type (plus **SINGLE** / **MULTIPLE** rules). **Precedence is not hard-coded** inside metadata-core as “always global < team < user < role” — the **caller supplies the order** for each resolution (HTTP query, UI session, agent runtime, etc.).

**Example default (docs only):** **user > team > role > global** in [`metadata-documentation.md`](../../../design/metadata/metadata-documentation.md) is an **illustrative** ordering for org/user-centric UIs. It is **equivalent** to application order **global → … → role → team → user** with **last-wins** — same logic, two phrasings. Do **not** treat that stack as the only valid stack.

**Custom / agent context (user example — AI v3):** Metadata may be layered as **global** (where the chat started) **then** a **conversation- or chat-scoped** URN (e.g. `chat:xyz` / `urn:mill/.../scope:...` — exact grammar TBD). **`MetadataContext` = `[global, chat]`** → **chat wins** for overrides, enabling **tools and prompts** to see **chat-effective** metadata while still inheriting global defaults. Any number of **custom scope types** can appear in the list as long as they are **valid registered scope URNs** (or ephemeral scopes per SPEC rules).

**SPEC obligation:** State clearly that **`MetadataContext.scopes` is caller-defined**; document **examples** (RBAC stack, chat stack) and require **validation** (known scopes, ordering policy per product surface — e.g. REST always appends user scope last vs agent supplies full list).

**Persisting merge / overlay semantics on the assignment row (user direction):** Operational semantics for how a row participates in merge (e.g. **plain overlay**, **tombstone / suppress inherited**, **inherit-only**, **INSERT/UPDATE-only** policy in narrow scopes) should live on the **facet assignment** table, **not** inside **`payload_json`** — keeps **domain facet content** portable and avoids clients accidentally copying merge control fields.

**Table name:** Prefer a clearer name than **`metadata_facet`** for “entity + facet type + scope assignment”, e.g. **`metadata_entity_facet`** (breaking rename — list in **§16** and Flyway greenfield). If rename cost is too high short-term, keep `metadata_facet` but document the same column model.

**How to store merge control (options):**

| Approach | Pros | Cons |
|----------|------|------|
| **Dedicated columns** (e.g. `merge_action` / `overlay_mode` ENUM, optional `suppresses_inherited` boolean, nullable **small fixed set**) | Queryable, indexable, obvious in SQL; validators easy | Schema migration when vocabulary grows |
| **Sidecar JSON** e.g. **`apply_spec_json`** / **`merge_meta_json`** (not `payload_json`) | Extensible merge DSL without wide nullable columns | Harder to index; second JSON to validate/version |
| **Inside `payload_json`** | None recommended | Pollutes domain payload; client export/import leaks infra |

**Recommendation for SPEC:** Use a **single `merge_action` (or `overlay_mode`) column** — **ENUM / VARCHAR with check constraint** — on the assignment row (**`metadata_entity_facet`**). No merge control inside **`payload_json`**. Add **`apply_spec_json`** only if v1 enum is insufficient.

**Preferred v1 enum — meanings (for SPEC prose and `MetadataReader`):**

| Value | Meaning |
|--------|--------|
| **`SET`** (default) | **Normal overlay.** `payload_json` is the facet payload at this scope. When building the effective view, **last-wins** across `MetadataContext` applies: a **later** scope’s **`SET`** replaces an earlier scope’s **`SET`** for **SINGLE** cardinality; **MULTIPLE** rules unchanged. This is the usual “user annotation over global” behaviour. |
| **`TOMBSTONE`** | **Suppress inherited facet for this type at this entity** for merge purposes. While this scope is **present** in the active context, the merged result behaves as if this facet type is **absent** (or empty) **for that entity**, **even if** a **lower-priority** scope (e.g. global) has a **`SET`**. Does **not** delete rows in other scopes — it only affects the **read/merge** outcome. A **later** scope in context can still **`SET`** again and restore visibility. Use case: “hide this descriptive facet for me in this chat / team context without wiping global.” |
| **`CLEAR`** | **This scope contributes nothing** — same **effective** result as **having no assignment row** at this `(entity, facetType, scope)` for **SINGLE** (reader skips this row for merge). Retains a **persisted row** for audit / “I withdrew my overlay” without using SQL **DELETE** if policy forbids DELETE on overlays. `payload_json` may be ignored or `{}`. |

**Optional later values** (document as reserved / phase 2 if needed):

| Value | Meaning |
|--------|--------|
| **`MERGE_DEEP`** | **Structural merge** of `payload_json` with the inherited payload (field-by-field) instead of full replace — only if product needs JSON patch semantics; requires strict schema rules. |
| **`FORK`** | **Copy-on-write hint** — first edit branches from inherited snapshot (mostly product/workflow; merge engine may treat like **`SET`** initially). |

**`MetadataReader`:** Interprets **`merge_action`** when folding scopes in order; repositories **only** persist the column.

**DELETE / overlay policy linkage:** If HTTP **DELETE** is disallowed on overlay scopes, **`CLEAR`** or row **unassign** (if allowed) replaces “remove my overlay”; **`TOMBSTONE`** replaces “hide inherited without deleting global data.”

**`CLEAR` vs physical “delete override” (unassign):** Both yield the **same merged effective view** for that `(entity, facetType, scope)` — no contribution from that scope, weaker scopes in `MetadataContext` show through. **`CLEAR`** keeps a **row** (audit / no-delete policy). **Unassign (delete row)** removes the row; trace moves to **`metadata_audit`** (or absence of row). SPEC should state this equivalence for **effective** metadata and the **operational** difference for compliance and debugging.

**Repositories = persistence only:** **`MetadataEntityRepository`**, **`FacetRepository`**, etc. perform **store/load/delete by keys** — **no** scope-merge or “effective facet” logic inside adapters.

**Merge / aggregation in `mill-metadata-core`:** Introduce a **repository-agnostic** component (name TBD — e.g. **`MetadataReader`**, **`ScopedFacetMerger`**, or fold into **`DefaultFacetService.resolve`** behind a dedicated collaborator) that:

- Takes **read ports** (repositories or narrower query interfaces) **plus** optional **non-repository inputs** (e.g. **physical schema / connection-describing metadata** from the Mill backend, supplied by `data/*` via an interface implemented outside metadata-persistence).
- Implements **centralised** rules: **caller-supplied** ordered scopes, **SINGLE** vs **MULTIPLE** behaviour, interaction between **layers** (stored scopes + **ephemeral backend** / **agent-generated** facet payloads if applicable).

**Rationale (composite / backend capture):** When **structural** (or similar) metadata is **synthesised from the live backend** while **annotations** live in **JPA/file**, the **effective** view is a **merge of sources**, not a single table scan. That merge belongs in **metadata-core** orchestration, **not** in JPA repositories, so it stays **independent of storage implementation** and testable without Spring.

**DELETE vs INSERT/UPDATE in overlay scopes (consideration):**

- **Risk:** Allowing **DELETE** in a **narrow** scope to be interpreted as “remove the merged facet everywhere” blurs layers and can **destroy global** data by mistake.
- **Direction (user):** In **overriding** scopes (team/user), only **INSERT** and **UPDATE** — **no DELETE** (needs precise definition).

**Clarifications to lock in SPEC:**

1. **Row-level:** **Unassign** by **`uid`** always removes **only that assignment row**. If that row is in **user** scope, the merged view **falls back** to team/global — this is usually still implemented as a **DELETE** on `metadata_facet`. If that is **forbidden by policy**, specify the alternative (**UPDATE** to empty payload, **`inherit: true`**, or **tombstone** record).
2. **API-level:** Forbid endpoints that accept **“delete effective facet”** without naming **scope + uid** (no **merged-view DELETE** that targets inherited global rows through a user/team route).
3. **Backend layer:** Physical connection metadata is typically **read-only synthetic** — **no DELETE** in that layer; overlays remain INSERT/UPDATE only if you want symmetry.

**Deliverables:** Extend **SPEC §5.6 / §7.2** (and a short **§7.x** if needed) with: merge **precedence table**, **where logic lives** (class + package), **multi-source** diagram, and **DELETE/UNASSIGN** rules per scope class. Update **WI-120** / **WI-119** if the **FacetService** split (resolve vs persist) changes.

## SINGLE vs MULTIPLE — uniform persistence (user direction)

**No separate persistence shape:** **`FacetTargetCardinality` (SINGLE | MULTIPLE)** MUST **not** imply different tables, columns, or row “kinds”. **Every** facet assignment is **one row** in **`metadata_entity_facet`** with the **same** column set; **every** assignment has a **stable UUID** (DDL column **`uuid`**, domain **`uid`**, REST **`{facetUid}`**) as its **unique handle**.

**Cardinality affects write / validate / resolve rules only:**

- **MULTIPLE:** Many rows may share the same `(entity, facetType, scope)` triple; each row has its **own** uuid; **assign** adds a new row.
- **SINGLE:** At most **one** effective row per triple is a **policy** enforced in **`FacetService` / repository save path** (upsert: update payload **in place** on the **same** uuid, or replace row — still **one row shape**), **not** a different storage layout.

**API / domain mental model — flat list (not nested):** Effective facets for an entity MUST be representable as a **single flat list** of assignments, e.g. `[{…, uuid}, {…, uuid}, {…, uuid}, …]`, mixing “single-type” and “multiple-type” facet assignments with **no** grouping like `[single, single, [multi, multi]]`. **MULTIPLE** does **not** introduce a nested array container in persistence or in the canonical DTO list — only **more rows** (or more list elements), each **individually** identified by uuid.

**SPEC cleanup:** Remove any wording that suggests **MULTIPLE** facets live in a **separate** structure from **SINGLE**; align **§5.4**, **§6.2**, **§7.2**, and REST **`FacetInstanceDto`** list responses with this **one-row-one-assignment** model.

## Row audit + `uuid` on all `metadata_*` business tables (user direction)

**Row-audit quad (uniform):** Every `metadata_*` table **except `metadata_audit`** carries:

```text
created_at        TIMESTAMP NOT NULL,
created_by        VARCHAR(255),
last_modified_at  TIMESTAMP NOT NULL,
last_modified_by  VARCHAR(255)
```

Apply **wherever it is missing** in the greenfield DDL (e.g. ensure **`metadata_scope`** matches other tables; today’s JPA `MetadataScopeEntity` may only have `created_at` — align entity + migration).

**`uuid` column:** Each auditable metadata business row has **`uuid`** per SPEC §8.2. **Exception:** **`metadata_audit`** — no row-audit quad and no **`uuid`** column on that table.

**`metadata_entity_facet`:** Assignment stable id is the **`uuid`** column (**`merge_action`** lives here — **not** in **`payload_json`**).

**Scope:** Includes future **`metadata_seed`** (and any other `metadata_*` operational tables). **`metadata_promotion`** is **out of scope** — see section above.

**Follow-through in SPEC:** §8 DDL, §8a ERD, §9 JPA — **`uuid`** column naming; H2/PostgreSQL type choice (`UUID` vs `VARCHAR(36)`).

## Table names (`metadata_*`) — review for SPEC glossary / registry

**Prefix:** All authoritative metadata persistence tables use the **`metadata_`** prefix — consistent.

| Table | Role (one line) | Domain / JPA note |
|-------|-----------------|-------------------|
| **`metadata_scope`** | Scope registry (URN + type + `reference_id`) | `MetadataScope`; `scope_id` surrogate |
| **`metadata_entity`** | Entity identity (URN + optional `kind`) | `MetadataEntity`; `entity_id` surrogate |
| **`metadata_facet_type_def`** | Declared facet type contracts (`manifest_json`) | `MetadataFacetTypeEntity` in §9.3 — class name does not say `Def` |
| **`metadata_facet_type`** | Runtime facet type row (DEFINED \| OBSERVED, FK `def_id`) | **`MetadataFacetTypeInstEntity`** — **misleading:** `Inst` reads like “facet instance” but table is **type catalog**, not **`metadata_entity_facet`** |
| **`metadata_entity_facet`** | Facet **assignments** (entity + type + scope; **`uuid`**, `payload_json`, **`merge_action`**) | `FacetInstance` / `MetadataEntityFacetEntity`; not “facet type” |
| **`metadata_audit`** | Append-only **operation** audit log | `MetadataAuditRecord`; exclude row-audit quad / **`uuid`** per policy |
| **`metadata_promotion`** | **Removed from this story** — no greenfield table; drop legacy table + code (reintroduce only in a future promotion story) |
| **`metadata_seed`** | Seed ledger for **`mill.metadata.seed`** (exactly-once) | SPEC §8 / §14.1; JPA + **WI-122** / **WI-126** |

**Naming patterns:** singular nouns (`entity`, `scope`, `facet`); `_def` suffix for definition table; **`metadata_entity_facet`** = assignment rows; column **`uuid`** for cross-system ids.

**Legacy names to purge from prose:** `metadata_operation_audit`, `metadata_facet_investigation_audit` — replaced by **`metadata_audit`** only.

**Index / constraint names:** `uq_metadata_*`, `idx_metadata_*` — consistent; keep in SPEC examples.

**Terminology coupling (from prior review + tables):** distinguish **row audit** (columns on business tables) vs **`metadata_audit`** (table name = operation log). **§4.3** must use **`type_res`**, not `type_key`, to match DDL.

## Spot-check list (implementation vs SPEC)

| Topic | Note |
|--------|------|
| **File storage** | **Retained** with canonical YAML (**SPEC** G7, §15, **STORY** #9). |
| **Entity URNs** | Any **`urn:mill/...`**; **§11.0** explains normative YAML vs **`CANONICAL`** fixture shape. |
| **Audit** | **`metadata_audit`**; **JPA listeners only** — services do not call `MetadataAuditRepository`. |
| **REST** | Facet delete path + **unassign** rules **§10.2**; **`merge-trace`** **§10.5**. |
| **DDL** | **`metadata_entity_facet`**; **`uuid`** columns; platform seeds from **`platform-facet-types.json`**. |

## Configuration prefix rename: `mill.metadata.storage` → `mill.metadata.repository` (user direction)

**Intent:** The property namespace **`mill.metadata.storage.*`** is misleading — it selects the **metadata repository implementation** (JPA, file, noop), not generic “storage”. Rename to **`mill.metadata.repository.*`** (correct spelling **repository**, not `repositiry`).

**Examples of new keys (illustrative — exact nesting in Java properties TBD):**

- **`mill.metadata.repository.type`** — `jpa` \| `file` \| `noop` (same semantics as today’s `storage.type`)
- **`mill.metadata.repository.file.path`**, **`writable`**, **`watch`** — replace **`mill.metadata.storage.file.*`**
- Any **`@ConditionalOnProperty(prefix = "mill.metadata.storage", …)`** → **`mill.metadata.repository`**
- **`@ConfigurationProperties`**: rename class e.g. **`MetadataStorageProperties`** → **`MetadataRepositoryProperties`** (or keep inner structure with new prefix); regenerate **`spring-configuration-metadata.json`**

**Scope of code/docs to touch:** `mill-metadata-autoconfigure` (all metadata `*AutoConfiguration.kt`), **`mill-metadata-core`** KDoc mentioning the old prefix, **`apps/mill-service`** / sample **`application*.yml`**, **`docs/design/refactoring/05-configuration-keys.md`**, **`docs/design/platform/CONFIGURATION_INVENTORY.md`**, **`docs/design/metadata/*`**, **SPEC §14 / §15.7**, **WI-124**, **MILESTONE** if it cites the old key, tests.

**Breaking:** No silent alias unless product requires a **deprecation release** — greenfield story can cut **`mill.metadata.storage.*`** entirely.

## Recommended product decision (unless you object)

**Keep file-backed storage** (`jpa` | `file` | `noop`) with the canonical multi-document YAML from **§15**, and treat “non-goal” as **no reintroduction of the legacy ad-hoc file format** (not “no file at all”). That matches **G7**, **WI-124**, and minimizes rework.

## Concrete doc edits (tracking checklist)

Many items below were **already applied** to [`SPEC.md`](./SPEC.md) / [`STORY.md`](./STORY.md) during planning. Use this list to **hunt gaps** between spec, WIs, and code while implementing — not as a mandate to re-edit SPEC from scratch.

1. **`SPEC.md`**
   - §3 **Non-Goals**: replace or narrow “File metadata adapter reintroduction” to **legacy file format / old keys**; align with §15.
   - §14 **Configuration**: use **`mill.metadata.repository.*`** prefix (renamed from **`mill.metadata.storage.*`**); align **`repository.type`** with **§15.7** / **WI-124** (`jpa` \| `file` \| `noop`); document breaking rename.
   - **New § (e.g. §14a or §17a)** — **Startup seeding**: `mill.metadata.seed` properties; ledger table DDL; `MetadataSeedLedgerRepository` (core) + semantics (ordered, exactly-once); relationship to `MetadataImportService`; abandonment of ad-hoc startup loading.
   - §8 **DDL**: **remove `metadata_promotion`** entirely; add **`metadata_seed`** (or agreed name) with columns at minimum: stable `seed_key` (PK or unique), **row-audit quad + global UUID** (same pattern as other `metadata_*`), `completed_at` / fingerprint / `last_error` as needed; **`metadata_audit`** excluded from quad + global UUID per user rule.
   - §8 / §9: **Uniform row-audit quad** on all `metadata_*` except **`metadata_audit`**; **`uuid`** column on business tables; **`metadata_entity_facet`** assignment table.
   - §7.1: fix audit repository name to match §6.5.
   - §10: fix duplicate **10.7** numbering; ensure **§10.3** example `id` matches §5.2 (e.g. model table URN).
   - §8.5: reconcile platform facet seeds with seed-runner approach (per decision A/B above).
   - **§11 / §15 + canonical YAML**: Link **`convert_to_canonical_yaml.py`** and **skymill / moneta** `*-meta-canonical.yaml` as worked examples; document **`metadataFormat` / `formatVersion`**; reconcile with multi-document `kind:` spec and with **entity URN** convention (`metadata/entity:` vs `model/table:`).
   - **Glossary + table registry**: appendix with **table → domain type → JPA entity**; **row audit** vs **`metadata_audit`**; rename or document **`MetadataFacetTypeInstEntity`** vs **`metadata_facet_type`** confusion; fix **§4.3** / **FacetTypeManifest** / **investigation audit** wording (see plan sections above).
   - **Scope merge + `MetadataReader`**: **`MetadataContext` order is caller-defined** (RBAC example in docs is not the only stack); document **global + chat** (AI v3) last-wins; priority ↔ application-order equivalence for examples only; repositories persistence-only; merge in **core**; **`metadata_entity_facet`** + merge columns / **`apply_spec_json`**; overlay **DELETE** rules (see plan section).
   - **SINGLE / MULTIPLE:** uniform assignment row + **uuid**; flat list in API; cardinality = **write/resolve policy only** (see plan section).
   - Optional: add a short **“Normative vs exploratory”** or **Revision** note at top when moving from Draft → Reviewed.

2. **`STORY.md`**
   - **Non-goals:** confirm **metadata promotion (M-23)** remains deferred and **`metadata_promotion`** is **not** part of this delivery (table dropped, code removed).
   - **Locked decision #2**: entity id = **full Mill URN** (`urn:mill/...`), opaque to metadata; **not** limited to `metadata/entity`.
   - **Locked decision #9**: replace “delete file” with **rewrite file adapter + canonical YAML; remove legacy format** (or the opposite if you choose to drop file).
   - **New locked row**: **Startup metadata** = **`mill.metadata.seed` ordered resources + ledger-backed exactly-once**; no reliance on implicit autoconfig-only loading.
   - **Locked row / checklist**: **`mill.metadata.repository.*`** replaces **`mill.metadata.storage.*`** (implementation + docs).
   - **Goal / WI-122 cross-links**: replace “V9” wording with the actual squashed migration filename you commit to.
   - **Work items**: add WI or expand **WI-122** (DDL + JPA for ledger), **WI-120** or autoconfigure (runner), **WI-124** (Java `@ConfigurationProperties` for `mill.metadata.seed`).
   - **Locked row / checklist**: production **Kotlin/Java** doc **to parameter level**; **TypeScript** **to function level**; property-bound config classes in **Java** for Spring metadata (see implementation standards section).
   - Optional: one paragraph **success criteria** (e.g. `mill-metadata-core` dependency graph clean; H2 testIT green; UI uses URNs end-to-end; second process start does not re-apply seeds).

3. **`WI-119-design-lock.md`**
   - Swap investigation DDL for **`metadata_audit`** (columns + operation vocabulary from SPEC §8.4).
   - Fix REST change-list: **DELETE** uses **`{facetUid}` in path** per SPEC §10.2; adjust “MULTIPLE delete” wording accordingly.
   - Add **seed ledger** + **config key shape** to design-lock checklist once SPEC section exists.
   - Add explicit **sign-off line** (owner/date) when design is locked.

4. **`WI-122`**: remove **`metadata_promotion`** from scope/body; title (first line) e.g. “Persistence: squashed greenfield migration, JPA, row audit, unified audit listeners” — avoid “→ V9” if the file is `V4__...`.

5. **WI implementation (when coding):** Enforce **KDoc/JavaDoc to parameter level** and **TS to function level**; **`@ConfigurationProperties` in Java** — see **“Implementation documentation standards”** below.

## Implementation documentation standards (user direction)

Applies to **all code touched, changed, or newly added** during this story’s implementation (not only autoconfigure).

**Kotlin and Java (backend / libraries):** **KDoc** (Kotlin) and **JavaDoc** (Java) on **every** new or modified **production** type and member, **down to parameter level** — classes, constructors, methods, public properties; describe behaviour, constraints, and `null` expectations where relevant. Matches **`CLAUDE.md`** / repo conventions. **Test** types and test methods remain exempt unless adding shared test utilities (then document those).

**TypeScript (UI):** Document **to function level** — each **touched or new** exported **function**, hook, or service method gets a short **JSDoc/TSDoc** block (purpose, side effects, non-obvious args/return). **Parameter-level** TS doc is optional unless the signature is non-obvious.

**Spring configuration bound to properties:** Any **`@ConfigurationProperties`** (or equivalent property-binding types) introduced or substantially changed for this work MUST be implemented in **Java** so **`spring-boot-configuration-processor`** generates **`META-INF/spring-configuration-metadata.json`** automatically. **Do not** use Kotlin for new properties classes unless the module already ships **`META-INF/additional-spring-configuration-metadata.json`** by policy — default is **Java** for metadata story keys (`mill.metadata.repository.*`, `mill.metadata.seed.*`, etc.).

## Out of scope for this documentation pass

- Implementing migrations, code, or UI (separate WIs).
- Updating `docs/design/metadata/*` and design-side config inventory — **WI-127**; public metadata docs and `docs/public/` config — **WI-128**; story closure — **WI-125** (except if WI-119 deliverables explicitly require a stub update to `metadata-urn-platform.md`).

## Verification

### Spec / story consistency (documentation pass)

Skim **STORY** locked table, **SPEC §3 / §14 / §15**, seeding (**§14.1**), **WI-119** (audit + REST), **WI-122** (DDL). There should be **no** conflicting statements on: file adapter (**canonical YAML** + **`repository.type=file`**), **`metadata_audit`** (not legacy investigation table names), entity URN = **any** `urn:mill/...`, facet **DELETE** path **`{facetUid}`**, **no `metadata_promotion`**, **`mill.metadata.repository.*`** / **`mill.metadata.seed.*`** (no normative **`mill.metadata.storage.*`**).

### Build and search (implementation pass)

From repo root (adjust modules if your WI is narrower):

```bash
./gradlew :metadata:build
./gradlew :metadata:mill-metadata-persistence:testIT   # when persistence changed
```

```bash
rg 'mill\.metadata\.storage' --glob '!**/build/**' --glob '!**/.venv/**'
rg 'metadata_promotion' metadata/
```

UI: `cd ui/mill-ui && npm run build` (when **WI-123** touches the app).

Public docs: `mkdocs build` from `docs/public` (after **WI-128**).

### Story closure (**WI-125** only)

- Update [`MILESTONE.md`](../../MILESTONE.md) and [`BACKLOG.md`](../../BACKLOG.md).
- **Move** (do not delete) this story folder to `docs/workitems/completed/YYYYMMDD-metadata-rework/` per [`RULES.md`](../../RULES.md) — **done** as `completed/20260330-metadata-rework/`.

---

## Handoff sufficiency

A new agent has enough **in this folder** to execute the story if these exist and are read together:

- [`SPEC.md`](./SPEC.md), [`STORY.md`](./STORY.md), this [`PLAN.md`](./PLAN.md), [`HANDOFF-FROM-DEV.md`](./HANDOFF-FROM-DEV.md), and **every** [`WI-*.md`](./) file listed in the folder table above.
- **[`reference/`](./reference/)** — optional if your Git history already includes `9017952c5`-era paths; **required** if you only cherry-pick the story commit onto bare `origin/dev` and need canonical YAML examples + design copies without pulling the whole prior commit.

No separate “handoff doc” is required beyond keeping **SPEC** and **STORY** truthful as code lands. Repository-wide rules: [`docs/workitems/RULES.md`](../../RULES.md), [`CLAUDE.md`](../../../../CLAUDE.md).
