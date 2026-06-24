# WI-344 — Platform seed facet types YAML (L1 + L2)

| Field | Value |
|--------|--------|
| **Story** | [`dqm-metadata-facets`](STORY.md) |
| **Status** | `done` |
| **Type** | `docs` / `feature` (seed YAML only) |
| **Area** | `metadata`, `data-quality` |
| **Depends on** | [**WI-342**](WI-342-dq-facet-design-l1.md), [**WI-343**](WI-343-dq-facet-design-l2.md) — stable `contentSchema` per type in [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md) |
| **Enables** | Future execution story (**M-16**); metadata capture / import of DQ rules |

## Problem

Facet types defined only in design prose are not loaded into the **`FacetCatalog`** until
**`FacetTypeDefinition`** seed documents exist. Operators and import tooling need registered types
with validated `contentSchema` and **clear, self-explanatory descriptions** before attaching DQ rule
instances to entities.

## Goal

Add **two** platform seed YAML files (L1 and L2) under
[`metadata/mill-metadata-core/src/main/resources/metadata/`](../../../metadata/mill-metadata-core/src/main/resources/metadata/)
for all **16** DQ facet types, and update the standard facet inventory docs.

## Deliver

### 1. Seed files (L1 and L2 separate)

**Directory:** [`metadata/mill-metadata-core/src/main/resources/metadata/`](../../../metadata/mill-metadata-core/src/main/resources/metadata/)

| File | Level | Type count | Slugs |
|------|-------|------------|-------|
| [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) | L1 | **10** | `dq-null-check`, `dq-empty-value-check`, `dq-unique-value-check`, `dq-allowed-values-check`, `dq-pattern-check`, `dq-min-max-check`, `dq-referential-integrity`, `dq-referential-source`, `dq-referential-target`, `dq-data-age-check` |
| [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) | L2 | **5** | `dq-predicate`, `dq-composite-uniqueness`, `dq-parent-child-reconciliation`, `dq-cross-table-reconciliation`, `dq-sla-compliance-check` |

Mirror structure of [`platform-flow-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml) and description depth of [`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) (`relation`, `descriptive`).

Each file:

- One `FacetTypeDefinition` document per type (YAML `---` separated).
- Per-row fields:
  - `facetTypeUrn`: `urn:mill/metadata/facet-type:dq-<slug>`
  - `title`, `description` — see **Description quality** below
  - `category`: **`data-quality`**
  - `enabled`: true
  - `mandatory`: false
  - `targetCardinality`: **`MULTIPLE`**
  - `applicableTo`: per design doc (attribute, table, or schema)
  - `contentSchema`: OBJECT with common + rule-specific fields from [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md)
  - `schemaVersion`: `"1.0"`

**Payload conventions:**

- Common fields on every type: `name`, `description`, `severity`, `enabled`, `tags` (`stereotype: tags` — copy from `descriptive` in `platform-bootstrap.yaml`).
- Referential triple: reuse `source` / `target` / `sourceColumns` / `targetColumns` shapes from relation facets; **column lists optional**; optional **`joinSql`** on all three types with shared join semantics.
- `dq-predicate`: required `predicate` (STRING).

### 2. Description quality (normative)

Descriptions must be **operator-grade**: understandable in the facet-type admin UI and Data Model
without reading the design doc. Match the tone and completeness of existing platform seeds.

**Facet type level (`title` + `description` on each `FacetTypeDefinition`):**

- **`title`** — short product label (e.g. “Null Check”, “Predicate Rule”, “Referential Integrity (Source)”).
- **`description`** — **2–4 sentences** covering:
  1. **What** is validated (business rule in plain language).
  2. **Where** it applies — assignment target entity kind (attribute, table, schema) and binding rule.
  3. **How** pass/fail is interpreted at execution time (e.g. zero violating rows).
  4. For consolidated types (`dq-predicate`, referential triple), mention relationship to catalog rule names or relation facets.

**`contentSchema` level:**

- Root OBJECT: `title` + `description` summarizing the payload.
- **Every** field: `title` + `description` (required fields marked `required: true`).
- **ENUM** fields: each `values[].description` explains when to use that severity / dialect / kind.
- **ARRAY** / nested **OBJECT** fields: same as `relation` seeds — describe list semantics and child fields.
- Include **brief examples** in descriptions where it reduces ambiguity (e.g. predicate examples, ISO-8601 duration for `maxAge`, allowed-values list).

**Anti-patterns (reject in review):**

- One-word or tautological descriptions (“Name of the rule”, “The predicate”).
- Duplicating only the field name in `title` with no semantics.
- Missing assignment-target guidance on table/column types.

### 3. Seed registration note

Document in [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md) (§ Platform seeds):

- Load **L1 then L2** after `platform-bootstrap.yaml` (and after `platform-flow-facet-types.yaml` if present).
- **Do not** change `apps/mill-service/application.yml` in this WI unless the user explicitly requests runtime wiring — document the required entry only.

Example entry (documentation):

```yaml
mill.metadata.seed.resources: >-
  classpath:metadata/platform-bootstrap.yaml,
  classpath:metadata/platform-flow-facet-types.yaml,
  classpath:metadata/platform-dq-l1-facet-types.yaml,
  classpath:metadata/platform-dq-l2-facet-types.yaml
```

### 4. Inventory and catalog docs

Update [`docs/design/metadata/platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md):

- New subsections **`platform-dq-l1-facet-types.yaml` (10 types)** and **`platform-dq-l2-facet-types.yaml` (6 types)** — table per file: slug, title, target entity, one-line purpose.
- Update total standard-type count accordingly.

Update [`docs/design/data/dq-rules.md`](../../../design/data/dq-rules.md):

- **Metadata facets** section linking L1/L2 catalog rows → facet type URN(s) and design doc.

Optional: index entry in [`docs/design/metadata/README.md`](../../../design/metadata/README.md).

### 5. Canonical example (optional)

Minimal facet assignment example in the design doc (e.g. `dq-null-check` on an attribute entity).

## Out of scope

- `application.yml` seed wiring (unless user requests)
- Flyway / DB migrations
- Facet assignment seed data on Skymill entities
- Backend validation beyond existing `FacetTypeManifestNormalizer`
- Execution engine

## Acceptance criteria

- [x] `platform-dq-l1-facet-types.yaml` contains **10** valid `FacetTypeDefinition` documents
- [x] `platform-dq-l2-facet-types.yaml` contains **5** valid `FacetTypeDefinition` documents
- [x] Every type meets **Description quality** bar (type-level + field-level; ENUM value descriptions where applicable)
- [x] Referential triple reuses relation facet payload shapes from `platform-bootstrap.yaml`
- [x] `dq-predicate` includes required `predicate` plus common DQ fields
- [x] Every `contentSchema` matches [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md)
- [x] Every type includes optional **`tags`** with `stereotype: tags`
- [x] `platform-standard-facet-types.md` lists both seed files
- [x] `dq-rules.md` links to facet design doc
- [x] Seed registration documented (config note, L1 before L2)
- [ ] No Kotlin/Java production code changes except the two new YAML resource files

## Suggested commit

`[docs] WI-344: platform DQ L1/L2 facet type seeds and inventory`
