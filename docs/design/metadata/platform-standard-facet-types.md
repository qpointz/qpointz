# Platform standard facet types (seed inventory)

**Status:** Reference — classpath seeds for greenfield bootstrap  
**Last updated:** 2026-04-16  
**Related:** [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md), [`metadata-facet-type-catalog-defined-and-observed.md`](metadata-facet-type-catalog-defined-and-observed.md) (DEFINED vs OBSERVED in admin UI)

---

## Scope

**“Standard”** here means **`FacetTypeDefinition`** documents shipped under:

| Seed file | Role |
|-----------|------|
| [`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) | Global platform types (descriptive, structural, relation, qsynth, links, AI value-mapping). Loaded via `mill.metadata.seed.resources` with global scope. |
| [`metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml) | Flow backend bindings; loaded **after** `platform-bootstrap.yaml` when listed in seed resources. |

Custom types **defined only in the database**, or **OBSERVED** keys without a definition, are **not** listed here — see [`metadata-facet-type-catalog-defined-and-observed.md`](metadata-facet-type-catalog-defined-and-observed.md).

---

## Inventory

`applicableTo: []` means **no restriction** in the seed (any entity type unless constrained elsewhere). Slugs are the short form of `urn:mill/metadata/facet-type:<slug>`.

### `platform-bootstrap.yaml` (14 types)

| Slug | Title | Category | Cardinality | Applicable entity types (seed) | Purpose (short) |
|------|-------|----------|-------------|-------------------------------|-----------------|
| `descriptive` | Description | general | SINGLE | (none — any) | Display name, description, tags. |
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

**Total:** **17** standard facet type definitions across both files.

---

## See also

- Payload schema dialect and **array-of-objects** example: [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md)
- Value-mapping facet semantics: [`value-mapping-indexing-facet-types.md`](value-mapping-indexing-facet-types.md)
- Descriptor envelope vs wire JSON: [`facet-type-descriptor-formats.md`](facet-type-descriptor-formats.md)
