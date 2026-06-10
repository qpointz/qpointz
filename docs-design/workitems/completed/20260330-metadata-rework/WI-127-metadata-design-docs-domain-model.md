# WI-127 — Metadata design documentation & domain model reference

**Story:** Metadata Rework  
**Spec sections:** [`SPEC.md`](./SPEC.md) (normative).  
**Depends on:** WI-124 (implementation and config keys stable); may draft earlier but **must finish** after code matches SPEC.

## Objective

1. **Review and update** all **metadata-relevant** documents under **`docs/design/`**, aligned to the greenfield implementation (URNs, facet rows, merge semantics, audit, seeds, `mill.metadata.repository.*`, REST).
2. **Author the detailed design reference** [`docs/design/metadata/mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md) (replace the stub).
3. **Update Spring / `mill.metadata.*` documentation** that lives under **design and workitem tracking** (not under `docs/public/` — that is **WI-128**).

## Scope

### 1. Design documentation review (`docs/design/`)

**Primary:** everything under [`docs/design/metadata/`](../../../design/metadata/) — including [`README.md`](../../../design/metadata/README.md) and all `.md` files in that folder.

**Secondary sweep:** the rest of `docs/design/` for metadata subsystem references (`ui/`, `platform/`, `data/`, `agentic/`, etc.). Update or cross-link so nothing contradicts SPEC (entity URN opacity, `FacetInstance`, `metadata_audit`, no `metadata_promotion`, seed ledger, etc.).

**Deliverable:** each reviewed file either updated to as-built truth or explicitly marked **historical / superseded** with a pointer to `mill-metadata-domain-model.md` and SPEC.

### 2. Domain model reference document

**File:** [`docs/design/metadata/mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md)

**Minimum contents (diagrams welcome):**

- **Entity** — `MetadataEntity`; full `urn:mill/...` id; opaque to metadata-core; physical schema / codec (high level, link `metadata-urn-platform.md`).
- **Facet type** — definition vs runtime (`DEFINED` / `OBSERVED`); facet type URN.
- **Facet assignment (`FacetInstance`)** — uid, entity, type, scope, payload; **SINGLE** vs **MULTIPLE** as **policy** only; **flat** effective list.
- **Scopes and context** — `MetadataScope`, **`MetadataContext`** caller-ordered; last-wins; RBAC vs global+chat examples.
- **`merge_action`** — `SET`, `TOMBSTONE`, `CLEAR`; not in `payload_json`; merge in core vs persistence.
- **Persistence mapping** — short `metadata_*` table ↔ concept table (link SPEC §8 for DDL).
- **Import / YAML / seeds** — canonical YAML; `mill.metadata.seed` + ledger (high level); link SPEC §14–§15 and `metadata-canonical-yaml-spec.md`.

**Cross-link** from `metadata-service-design.md`, `metadata-documentation.md`, `metadata-urn-platform.md`.

### 3. Spring configuration — design & inventory docs only

**Inventory** under **`docs/design/`** and **`docs/workitems/`** (excluding `docs/public/`):

- `mill.metadata`, `metadata.repository`, `metadata.storage`, `metadata.seed`

**Update** every normative hit, including at minimum:

- [`docs/design/refactoring/05-configuration-keys.md`](../../../design/refactoring/05-configuration-keys.md)
- [`docs/design/platform/CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md)
- [`docs/workitems/MILESTONE.md`](../../MILESTONE.md) — if it still states obsolete metadata config

**Optional:** example YAML under `docs/` or `samples/` that is maintained as documentation — include here if it lives outside `docs/public/`.

## Done Criteria

- [x] `mill-metadata-domain-model.md` is **complete** (not stub) and linked from related design docs.
- [x] All `docs/design/metadata/*.md` reviewed; contradictions resolved or marked historical.
- [x] Design-side **`mill.metadata.*`** docs use **`repository.*`** / **`seed.*`**; no stale normative **`storage.*`** in **WI-127** paths.
- [x] One commit for **WI-127** per [`RULES.md`](../../RULES.md).

## Relation to other WIs

- **WI-128** — public site (`docs/public/`) and user-facing config prose.  
- **WI-125** — story closure after **WI-127** and **WI-128**.
