# Mill metadata — domain model reference

**Status:** As-built reference (metadata rework, March 2026)  
**Normative specification:** [`docs/workitems/completed/20260330-metadata-rework/SPEC.md`](../../workitems/completed/20260330-metadata-rework/SPEC.md)  
**URN binding summary:** [`metadata-urn-platform.md`](metadata-urn-platform.md)  
**Canonical YAML:** [`metadata-canonical-yaml-spec.md`](metadata-canonical-yaml-spec.md)

This document describes how Mill models **metadata** in code and persistence: entities, facet types, facet assignments, scopes, merge semantics, and how that maps to relational tables. It is the design companion to the SPEC; where they differ, the SPEC wins until the story updates it.

---

## 1. Entity (`MetadataEntity`)

An **entity** is a single row in the metadata store identified by a **full Mill URN** (`urn:mill/...`). The identifier is **opaque to `mill-metadata-core`**: the core module does not parse schema/table/column out of the id.

- **Relational catalog binding** (typical): the data layer uses [`MetadataEntityUrnCodec`](metadata-urn-platform.md#metadataentityurncodec-mill-data-schema-core) in `mill-data-schema-core` to map physical names to canonical `urn:mill/metadata/entity:<local>` URNs (dot-separated, lowercase).
- **Other bindings** may use other `urn:mill/...` groups/classes; metadata accepts any valid instance URN.

Domain fields (see `metadata/mill-metadata-core`): `id`, optional `kind`, optional `uuid`, audit timestamps and actors. There is **no** nested `facets` map on the entity aggregate in the greenfield model — facets live as **separate assignment rows** (see below).

---

## 2. Facet type — definition vs runtime

A **facet type** describes *what kind* of payload can be attached (descriptor, JSON Schema, cardinality policy, etc.).

- **`FacetTypeDefinition`** — authored manifest (strict descriptor contract).  
- **`FacetType`** — runtime row: either **`DEFINED`** (linked to a definition) or **`OBSERVED`** (created when an assignment arrives with an unknown type key — metadata remains permissive; see SPEC §5).

Facet type keys are normalised to **URNs** (e.g. `urn:mill/metadata/facet-type:descriptive`) at the API boundary where required.

**JPA persistence (`metadata_facet_type_def`):** the row stores **`manifest_json`** (JSON) built from the definition. It must round-trip optional **`category`** and the payload / **`contentSchema`** tree, including per-field **`stereotype`** lists, so values from seed YAML (and admin edits) reload correctly. Row-level **`mandatory`** / **`enabled`** columns are updated alongside `manifest_json` on save.

---

## 3. Facet assignment (`FacetInstance`)

Each attachment of a facet type to an entity is a **`FacetInstance`** (conceptually; persisted in `metadata_entity_facet` / JPA equivalent).

| Concept | Role |
|--------|------|
| **`uid`** | Stable id for this assignment (column `uuid` in DDL; REST `{facetUid}`). |
| **Entity** | The entity URN this row belongs to. |
| **Facet type key** | Which facet type this row instantiates. |
| **Scope** | Which scope this row applies under (e.g. global, team, user). |
| **Payload** | JSON document for the facet body — **does not** carry merge op codes. |

**SINGLE vs MULTIPLE** is **policy** on the facet type descriptor (`targetCardinality`), not a different Java type. The effective view is always a **flat list** of instances per `(entity, type, context)` after merge.

Persistence and API details: SPEC §5.4, §8, §10.

---

## 4. Scopes and `MetadataContext`

A **`MetadataScope`** is a first-class record (global, team, custom, etc.) with its own URN.

**`MetadataContext`** is a **caller-ordered list of scope keys** used when resolving facets. Resolution applies scopes in order; **last wins** for overlapping contributions (SPEC §5 / service layer).

Examples:

- **RBAC-oriented** deployments may pass user/team scopes first and global last — or the reverse, depending on product rules — as long as the order is explicit in the API.
- **Global + chat** contexts are expressed as multiple scope entries; merge rules use the same ordered list.

---

## 5. `merge_action` — `SET`, `TOMBSTONE`, `CLEAR`

Operational overlay semantics for a facet row are stored in **`merge_action`**, **not** inside `payload_json`:

| Value | Meaning (high level) |
|-------|----------------------|
| **SET** | Normal active payload. |
| **TOMBSTONE** | Row retained for audit; effective merge treats it as removed or suppressed per rules. |
| **CLEAR** | Explicit clear overlay. |

Merge and REST delete rules (hard delete vs tombstone) depend on scope class (overlay vs non-overlay); see SPEC §8.3 and §10.2. **Merge logic lives in `mill-metadata-core`** (repository-agnostic); JPA adapters load and save rows only.

---

## 6. Persistence mapping (relational)

Greenfield metadata uses a **single squashed migration** and tables prefixed `metadata_*`. Row-level audit columns apply to business tables; **`metadata_audit`** is a separate append-only **operation** log (do not confuse with row audit).

| Table (concept) | Purpose |
|-----------------|--------|
| `metadata_entity` | Entities (`MetadataEntity`) |
| `metadata_entity_facet` | Facet assignments (`FacetInstance` + `merge_action`, `payload_json`, `uuid`) |
| `metadata_facet_type` | Facet type runtime + manifest |
| `metadata_scope` | Scopes |
| `metadata_audit` | Operation audit |
| `metadata_seed` | Startup seed ledger (SPEC §14.1) |

Full DDL and column lists: **SPEC §8**. Legacy table names from older migrations are **not** recreated (`metadata_promotion`, old investigation audit names, etc.).

**Data rows:** greenfield Flyway scripts create **tables and indexes only**. They do **not** insert `metadata_scope`, facet type definition, or facet type rows — that content is loaded **only** through **`mill.metadata.seed.resources`** (for example `classpath:metadata/platform-bootstrap.yaml`).

---

## 7. Import, YAML, and startup seeds

- **Canonical import/export** uses multi-document YAML per [`metadata-canonical-yaml-spec.md`](metadata-canonical-yaml-spec.md) (facet-types and/or entities).
- **Startup seeds (sole control plane for platform metadata):** ordered list under **`mill.metadata.seed.resources`**, imported in **MERGE** mode with actor `system` when configured. This is the **only** supported way to install the global scope, standard facet types, and any other seed documents at startup — there are no Flyway data inserts, no separate file-bootstrap auto-configuration, and no other `ApplicationRunner` seeders for that content. Completion is recorded in **`metadata_seed`** when a seed ledger repository is present (SPEC §14–§15), including an **`md5:`** content fingerprint; unchanged files are skipped, changed files are re-imported.
- **File repository:** when **`mill.metadata.repository.type=file`**, **`mill.metadata.repository.file.path`** may list YAML snapshots the app reads/writes as the file-backed store. **At least one** of non-blank **`repository.file.path`** or non-empty **`mill.metadata.seed.resources`** is required (validator); seeds and path are independent — seeds do not replace configuring `path` when you rely on a persistent file location.

**Removed:** legacy **`mill.metadata.storage.*`** keys and the old ad-hoc file format — use **`mill.metadata.repository.*`** and canonical YAML only.

---

## 8. Spring configuration (summary)

| Prefix / keys | Purpose |
|---------------|--------|
| **`mill.metadata.repository.type`** | `file` \| `jpa` \| `noop` |
| **`mill.metadata.repository.file.path`** | Comma-separated resources; with `type=file`, set **either** a non-blank path **or** non-empty **`mill.metadata.seed.resources`** |
| **`mill.metadata.repository.file.writable`**, **`watch`** | Reserved / future file behaviour |
| **`mill.metadata.facet-type-registry.type`** | `inMemory` \| `local` \| `portal` (reserved) |
| **`mill.metadata.seed.resources`** | Ordered list of seed YAML locations |
| **`mill.metadata.seed.on-failure`** | `fail-fast` (default) \| `continue` |

User-facing prose lives under **`docs/public/`**; design-side inventories: [`05-configuration-keys.md`](../refactoring/05-configuration-keys.md), [`CONFIGURATION_INVENTORY.md`](../platform/CONFIGURATION_INVENTORY.md).

---

## 9. Related documents

| Document | Topic |
|----------|--------|
| [`metadata-service-design.md`](metadata-service-design.md) | Service architecture, REST surface, registry |
| [`metadata-documentation.md`](metadata-documentation.md) | Broader documentation index (update config examples to `repository.*`) |
| [`metadata-urn-platform.md`](metadata-urn-platform.md) | URN grammar and JDBC binding |
| [`SPEC.md`](../../workitems/completed/20260330-metadata-rework/SPEC.md) | Authoritative greenfield contract |
