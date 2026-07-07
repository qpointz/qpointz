# Platform standard facet types (seed inventory)

**Status:** Reference — classpath seeds for greenfield bootstrap  
**Last updated:** 2026-06-24  
**Related:** [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md), [`metadata-facet-type-catalog-defined-and-observed.md`](metadata-facet-type-catalog-defined-and-observed.md) (DEFINED vs OBSERVED in admin UI)

---

## Scope

**“Standard”** here means **`FacetTypeDefinition`** documents shipped under:

| Seed file | Role |
|-----------|------|
| [`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) | Global platform types (descriptive, structural, relation, qsynth, links, AI value-mapping). Loaded via `mill.metadata.seed.resources` with global scope. |
| [`metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml) | Flow backend bindings; loaded **after** `platform-bootstrap.yaml` when listed in seed resources. |
| [`metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml) | Data-quality L1 rule facet types; loaded **after** `platform-bootstrap.yaml` (and flow seeds when present). See [`dq-rule-facet-types.md`](dq-rule-facet-types.md). |
| [`metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml) | Data-quality L2 rule facet types; loaded **after** L1 DQ seeds. See [`dq-rule-facet-types.md`](dq-rule-facet-types.md). |

Custom types **defined only in the database**, or **OBSERVED** keys without a definition, are **not** listed here — see [`metadata-facet-type-catalog-defined-and-observed.md`](metadata-facet-type-catalog-defined-and-observed.md).

---

## Inventory

`applicableTo: []` means **no restriction** in the seed (any entity type unless constrained elsewhere). Slugs are the short form of `urn:mill/metadata/facet-type:<slug>`.

### `platform-bootstrap.yaml` (15 types)

| Slug | Title | Category | Cardinality | Applicable entity types (seed) | Purpose (short) |
|------|-------|----------|-------------|-------------------------------|-----------------|
| `descriptive` | Description | general | SINGLE | (none — any) | Display name, description, tags. |
| `ai-annotation` | AI Annotation | ai | MULTIPLE | `schema`, `table`, `attribute` | Entity-scoped procedural instructions for agents (SQL habits, projection rules). |
| `schema` | Schema | general | SINGLE | `schema` | Schema name for a schema-type entity. |
| `table` | Table | general | SINGLE | `table` | Schema and table names for a table entity. |
| `relation` | Relation | relation | MULTIPLE | (none — any) | Relation between source and target tables (join metadata). |
| `relation-target` | RelationTarget | relation | MULTIPLE | `table` | Inbound relation where this table is the target. |
| `column` | Column | general | SINGLE | `attribute` | Column identity (schema, table, column names). |
| `relation-source` | RelationSource | relation | MULTIPLE | `table` | Outbound relation where this table is the source. |
| `qsynth-model` | Qsynth model | data | SINGLE | `schema` | Qsynth model settings on a schema entity. |
| `qsynth-table` | Qsynth table | data | SINGLE | `table` | Qsynth dataset settings on a table entity. |
| `qsynth-column` | Qsynth column | data | SINGLE | `attribute` | Qsynth generator settings on a column entity. |
| `qsynth-run` | Qsynth run | data | SINGLE | `schema` | Qsynth generation run record on a schema entity. |
| `links` | Links | general | MULTIPLE | (none — any) | Named groups of hyperlinks. |
| `ai-column-value-mapping` | Value Mapping | ai | SINGLE | `attribute` | Column value-mapping: context, threshold, NULL handling, column-backed indexing flags. |
| `ai-column-value-mapping-values` | Value Mapping Values | ai | MULTIPLE | `attribute` | Static `content` / `value` rows; only meaningful with the primary AI mapping facet on the same attribute. |

### `platform-flow-facet-types.yaml` (3 types)

| Slug | Title | Category | Cardinality | Applicable entity types (seed) | Purpose (short) |
|------|-------|----------|-------------|-------------------------------|-----------------|
| `flow-schema` | Flow schema binding | flow | SINGLE | `schema` | Flow source name and storage (type + params). |
| `flow-table` | Flow table binding | flow | SINGLE | `table` | Per-reader table inputs (format, mapping, params). |
| `flow-column` | Flow column binding | flow | SINGLE | `attribute` | Flow-derived column provenance (`binding` type + params). |

### `platform-dq-l1-facet-types.yaml` (10 types)

| Slug | Title | Category | Cardinality | Applicable entity types (seed) | Purpose (short) |
|------|-------|----------|-------------|-------------------------------|-----------------|
| `dq-null-check` | Null Check | data-quality | MULTIPLE | `attribute` | Column must not be NULL. |
| `dq-empty-value-check` | Empty Value Check | data-quality | MULTIPLE | `attribute` | String column must not be empty/blank. |
| `dq-unique-value-check` | Unique Value Check | data-quality | MULTIPLE | `attribute` | Column values must be unique. |
| `dq-allowed-values-check` | Allowed Values Check | data-quality | MULTIPLE | `attribute` | Value must be in allowed domain list. |
| `dq-pattern-check` | Pattern Check | data-quality | MULTIPLE | `attribute` | Value must match SQL LIKE or dialect regex pattern. |
| `dq-min-max-check` | Min/Max Check | data-quality | MULTIPLE | `attribute` | Numeric or date value within inclusive bounds. |
| `dq-data-age-check` | Data Age Check | data-quality | MULTIPLE | `table` | Latest timestamp within maxAge of evaluation anchor; optional cron + IANA timezone for schedule-relative freshness. |
| `dq-referential-integrity` | Referential Integrity (Full) | data-quality | MULTIPLE | `schema` | Full FK rule with source + target in payload. |
| `dq-referential-source` | Referential Integrity (Source) | data-quality | MULTIPLE | `table` | Outbound FK from host table to target. |
| `dq-referential-target` | Referential Integrity (Target) | data-quality | MULTIPLE | `table` | Inbound FK references to host table. |

**Total:** **33** standard facet type definitions across bootstrap, flow, and DQ L1/L2 seed files.

### `platform-dq-l2-facet-types.yaml` (5 types)

| Slug | Title | Category | Cardinality | Applicable entity types | Purpose (short) |
|------|-------|----------|-------------|-------------------------|-----------------|
| `dq-predicate` | Predicate Rule | data-quality | MULTIPLE | `table` | Row-level boolean predicate (3 catalog consistency rules). |
| `dq-composite-uniqueness` | Composite Uniqueness | data-quality | MULTIPLE | `table` | Column combination must be unique. |
| `dq-parent-child-reconciliation` | Parent-Child Reconciliation | data-quality | MULTIPLE | `table` | Parent measure matches child aggregate. |
| `dq-cross-table-reconciliation` | Cross-Table Reconciliation | data-quality | MULTIPLE | `table` | Scalar aggregates match across two tables. |
| `dq-sla-compliance-check` | SLA Compliance Check | data-quality | MULTIPLE | `table` | Data arrival before daily deadline. |

**Note:** Catalog **Semantic Validation** maps to **`dq-predicate`** (not a separate facet type).

---

## See also

- Payload schema dialect and **array-of-objects** example: [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md)
- Value-mapping facet semantics: [`value-mapping-indexing-facet-types.md`](value-mapping-indexing-facet-types.md)
- DQ rule facet types (L1/L2): [`dq-rule-facet-types.md`](dq-rule-facet-types.md) · [Relplan sketches (pseudo SQL)](dq-rule-relplan-sketches.md)
- Descriptor envelope vs wire JSON: [`facet-type-descriptor-formats.md`](facet-type-descriptor-formats.md)
