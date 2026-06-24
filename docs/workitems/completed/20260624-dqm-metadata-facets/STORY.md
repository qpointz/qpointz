# Metadata — Data Quality facet types (L1/L2)

Define **Data Quality** metadata facet types for all **L1 and L2** rules from the DQ rule catalog
that can be expressed as a **single relational plan**. Document per-type relational-plan sketches
(pseudo-language) and add platform **`FacetTypeDefinition`** seed YAML (separate L1 and L2 files).

**Scope:** facet type design + seed definitions + internal design doc only.  
**Out of scope:** rule compiler, execution service, scheduling, L3 rules.  
**Delivered adjunct:** mill-ui schema-default fix for facet payload editors (BOOLEAN/ENUM); seed unit tests; Skymill profile wiring.

**Branch:** `feat/dqm-metadata-initial`

**Sources:**

- Rule catalog: [`docs/design/data/dq-rules.md`](../../design/data/dq-rules.md)
- Metadata facet model: [`docs/design/metadata/`](../../design/metadata/) (descriptor formats, payload schema, platform seeds)
- Relation facet precedent: [`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) (`relation`, `relation-source`, `relation-target`)
- Backlog follow-on: [**M-16**](../../BACKLOG.md) — execution engine remains a **future** story

## Architectural decisions (locked)

| Decision | Choice |
|----------|--------|
| Facet granularity | **15** facet types total — one per catalog rule **except** referential integrity (**3** types, relation triple) and predicate-backed L2 rules (**1** `dq-predicate` covers **4** catalog rows) |
| Category | **`data-quality`** on every type |
| Entity target | Fixed per type via `applicableTo` — not a payload field (`attribute`, `table`, `schema`, or unrestricted) |
| Cardinality | **`MULTIPLE`** — many rule instances per entity |
| Payload model | Manifest-driven `contentSchema` only — no Kotlin facet lifecycle classes ([`facet-class-elimination.md`](../../design/metadata/facet-class-elimination.md)) |
| Common payload | `name` (required), `description`, `severity`, optional **`profile`** (violation-rate bands), `enabled`, optional **`tags`** (`stereotype: tags` — same as `descriptive` facet) |
| Physical binding | **Assignment target** (`FacetAssignment.entityId`) — host column/table is the entity the facet is assigned to; partner entities from payload only |
| Referential integrity | **Mirror relation facet triple** — `dq-referential-integrity` / `dq-referential-source` / `dq-referential-target` |
| Predicate rules (L2) | **Single** `dq-predicate` on **table** — *Cross-Column Consistency*, *Derived Value Validation*, *Conditional Completeness*, and *Semantic Validation* as instances with different `predicate` values |
| Execution | **Documented only** — relational plan sketch per facet type; no runtime in this story |
| Seed files | [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) (**10** types), [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) (**5** types) — operator-grade descriptions per WI-344 |

## Facet type inventory (15)

### L1 — `platform-dq-l1-facet-types.yaml` (10)

| Slug | Catalog rule(s) | `applicableTo` | Notes |
|------|-----------------|----------------|-------|
| `dq-null-check` | Null Check | attribute | |
| `dq-empty-value-check` | Empty Value Check | attribute | |
| `dq-unique-value-check` | Unique Value Check | attribute | |
| `dq-allowed-values-check` | Allowed Values Check | attribute | |
| `dq-pattern-check` | Pattern Check | attribute | |
| `dq-min-max-check` | Min/Max Check | attribute | |
| `dq-referential-integrity` | Foreign Key Check (full) | schema | Like `relation` — full `source` + `target` in payload |
| `dq-referential-source` | Foreign Key Check (outbound) | table | Like `relation-source` — host = assignment target |
| `dq-referential-target` | Foreign Key Check (inbound) | table | Like `relation-target` — host = assignment target |
| `dq-data-age-check` | Data Age Check | table | |

### L2 — `platform-dq-l2-facet-types.yaml` (5)

| Slug | Catalog rule(s) | `applicableTo` | Notes |
|------|-----------------|----------------|-------|
| `dq-predicate` | Cross-Column Consistency; Derived Value Validation; Conditional Completeness; Semantic Validation | table | One `predicate` field; catalog examples in design doc |
| `dq-composite-uniqueness` | Composite Uniqueness | table | |
| `dq-parent-child-reconciliation` | Parent-Child Reconciliation | table | |
| `dq-cross-table-reconciliation` | Cross-Table Reconciliation | table | |
| `dq-sla-compliance-check` | SLA Compliance Check | table | |

## Catalog → facet mapping (non-1:1 rows)

| Catalog rule (L2) | Facet type |
|-------------------|------------|
| Cross-Column Consistency | `dq-predicate` |
| Derived Value Validation | `dq-predicate` |
| Conditional Completeness | `dq-predicate` |
| Semantic Validation | `dq-predicate` |

All other catalog L1/L2 rows map 1:1 to a facet slug in the inventory above.

## Work item order

| Seq | WI | Rationale |
|-----|-----|-----------|
| 1 | WI-342 | Shared contract, pseudo-language, L1 types (10) + plan sketches |
| 2 | WI-343 | L2 types (5), cross-table patterns, scope assessment, catalog mapping |
| 3 | WI-344 | L1 + L2 seed YAML, descriptions, inventory cross-links |

## Work Items

- [x] WI-342 — DQ facet design contract and L1 plan sketches (`WI-342-dq-facet-design-l1.md`)
- [x] WI-343 — L2 plan sketches and scope assessment (`WI-343-dq-facet-design-l2.md`)
- [x] WI-344 — Platform seed facet types YAML — L1 + L2 (`WI-344-dq-facet-seed-types.md`)

## Story acceptance

- [x] [`docs/design/metadata/dq-rule-facet-types.md`](../../design/metadata/dq-rule-facet-types.md) — contract, threshold profile grammar, catalog→type mapping.
- [x] [`docs/design/metadata/dq-rule-relplan-sketches.md`](../../design/metadata/dq-rule-relplan-sketches.md) — pseudo SQL + post-processing per **15** types.
- [x] [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) — **10** `FacetTypeDefinition` rows.
- [x] [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) — **5** `FacetTypeDefinition` rows.
- [x] Seed descriptions meet WI-344 **Description quality** bar; L1/L2 seed unit tests green.
- [x] [`platform-standard-facet-types.md`](../../design/metadata/platform-standard-facet-types.md) — both seed files listed.
- [x] [`dq-rules.md`](../../design/data/dq-rules.md) — metadata facets section; L3 unchanged.
- [x] Skymill loads L1 + L2 DQ seeds; mill-ui applies payload schema defaults on edit.

## Related

- Value-mapping facets: [`completed/20260417-value-mapping-facets-vector-lifecycle/`](../../completed/20260417-value-mapping-facets-vector-lifecycle/)
- Future execution: BACKLOG **M-16**
