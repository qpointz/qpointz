# Findings: Schema capability tools vs platform facet types

**Status:** Findings report (analysis)  
**Date:** 2026-04-17  
**Scope:** Reconcile [`ai/mill-ai-v3`](../../../../ai/mill-ai-v3) schema exploration (`SchemaCapability`, `SchemaCatalogPort`, `capabilities/schema.yaml`) with platform facet definitions in [`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) and [`docs/design/metadata/platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md).

---

## 1. Scope and method

**Code path reviewed**

- Tools: [`SchemaCapability`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCapability.kt) → [`SchemaCatalogPort`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCatalogPort.kt) + [`capabilities/schema.yaml`](../../../../ai/mill-ai-v3/src/main/resources/capabilities/schema.yaml).
- Data adapter: [`SchemaFacetCatalogAdapter`](../../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) over [`SchemaFacetService`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacetService.kt) / [`SchemaFacets`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt).

**References**

- [`platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md) — inventory of standard facet types.
- [`platform-bootstrap.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) — authoritative seed `contentSchema` shapes.
- [`schema-facet-service.md`](../../../design/data/schema-facet-service.md) — physical schema vs metadata enrichment.

---

## 2. Findings

### Finding 1 — `SchemaFacets` materializes only a subset of seeded facet types

**Observation:** [`SchemaFacets.fromResolved`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt) converts resolved facet rows into typed Kotlin facets only for: `descriptive`, `structural`, `relation`, `concept`, and `value-mapping`. Other `FacetTypeDefinition` entries in `platform-bootstrap.yaml` (for example `schema`, `table`, `column`, `relation-source`, `relation-target`, `links`, `qsynth-*`, `ai-column-value-mapping`, `ai-column-value-mapping-values`) are not mapped into this typed convenience layer.

**Impact:** Metadata stored primarily under those types is not surfaced through the typed properties the adapter relies on (`descriptive`, `relation`, etc.), unless generic `facetByType` and raw payload handling are introduced.

**Severity:** High for parity with the documented “standard” platform inventory; lower if deployments only use JDBC-style `relation` + `descriptive`.

---

### Finding 2 — Descriptive facet: seed field names vs domain type vs tool output

**Observation:**

- `platform-bootstrap` descriptive `contentSchema` defines a top-level field named **`title`** (see seed YAML `descriptive` block).
- [`DescriptiveFacet`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/core/DescriptiveFacet.kt) uses **`displayName`**, not `title`, and the data class does not declare `@JsonAlias("title")`.
- [`SchemaFacetCatalogAdapter`](../../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) passes only **`description`** from `descriptive` into tool results (`description(item)`), not display name, tags, or other descriptive fields.

**Impact:** Possible **silent mismatch** between seed-documented payload keys and Jackson binding to `DescriptiveFacet`; tools **under-report** enrichment (no title/tags in `list_*` output) even when metadata exists.

**Severity:** Medium–high for correctness of descriptive enrichment; medium for LLM-facing UX.

---

### Finding 3 — Value mapping: legacy `value-mapping` vs `ai-column-value-mapping*`

**Observation:** [`MetadataUrns`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/MetadataUrns.kt) normalizes the short key **`value-mapping`**, not **`ai-column-value-mapping`** / **`ai-column-value-mapping-values`**. The standard inventory document lists the **`ai-column-*`** types as platform seeds. [`SchemaFacets`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt) routes **`value-mapping`** to [`ValueMappingFacet`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/core/ValueMappingFacet.kt). Schema list tools do not expose value mapping in any form today.

**Impact:** Deployments that store only the new AI facet type keys may not get the same typed handling as legacy `value-mapping`; alignment work is needed across Urns, merge routing, and product choice of canonical type(s).

**Severity:** High if value-mapping is expected in schema exploration; low if explicitly out of scope for `list_*` tools.

---

### Finding 4 — Relation payloads: platform nested shape vs `RelationFacet` flat shape

**Observation:**

- **`platform-bootstrap`** `relation` facet: nested **`source`** / **`target`** objects with **`columns`** arrays, plus **`cardinality`**, **`joinSql`** (required in seed).
- [`RelationFacet.Relation`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/facet/RelationFacet.kt): **`sourceTable` / `targetTable`**, **`sourceAttributes` / `targetAttributes`**, optional **`joinSql`**, plus **`name`**, **`description`**, **`type`**, **`businessMeaning`**.

**Impact:** If persisted JSON follows the **bootstrap** nested layout literally, conversion to `RelationFacet` may **fail or omit fields** unless a normalization or mapping layer exists end-to-end.

**Severity:** High until wire format is verified identical or an explicit mapping is documented and tested.

---

### Finding 5 — Relation coverage: adapter only reads aggregate `relation` on tables

**Observation:** [`SchemaFacetCatalogAdapter.listRelations`](../../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) collects from **`it.facets.relation?.relations`**. It does not assemble paths from **`relation-source`** / **`relation-target`** facet types, which the standard inventory associates with table entities.

**Impact:** Metadata authored only as per-table **`relation-source`** / **`relation-target`** facets may be **absent** from `list_relations` results.

**Severity:** High if those types are preferred storage; medium if the aggregate `relation` facet remains the only write path.

---

### Finding 6 — `list_relations` output omits platform-required / model fields

**Observation:** [`ListRelationsItem`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCatalogPort.kt) does not expose **`joinSql`** (required on the platform `relation` facet in the seed), nor **`type`** / **`businessMeaning`** from `RelationFacet`.

**Impact:** Consumers cannot see join hints and extra semantics described by the platform schema and domain model.

**Severity:** Medium for join discovery and admin/UI parity.

---

### Finding 7 — Columns: physical types vs `column` facet vs `structural` facet

**Observation:** `list_columns` returns **`nullable`** and **`type`** from the **physical** column model (protobuf enums via the adapter). The **`column`** facet in `platform-bootstrap` describes identity plus optional nested **type** details; [`StructuralFacet`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/facet/StructuralFacet.kt) captures JDBC-oriented flags. The adapter does not merge **`column`** or **`structural`** into tool rows.

**Impact:** Output is physically grounded but may **diverge** from metadata-only overrides; there is no explicit **provenance** indicating physical vs facet-sourced fields.

**Severity:** Medium in mixed environments (synced catalog + edited metadata).

---

### Finding 8 — Terminology drift (tools vs facet field names)

**Observation:** Tools use **`columnName`**, **`sourceAttributes`** / **`targetAttributes`**; the **`column`** facet uses **`column`**; relation payloads in the seed use nested **`columns`**. Semantics align; names are not identical to facet JSON.

**Impact:** Extra mapping burden for prompts, tests, and cross-system documentation.

**Severity:** Low–medium.

---

## 3. Summary table

| Area | Issue | Severity |
|------|--------|----------|
| Facet coverage | Many seeded types not on typed `SchemaFacets` path | High |
| Descriptive | `title` vs `displayName`; tools only expose `description` | Medium–high |
| Value mapping | `value-mapping` vs `ai-column-*`; not in schema tools | High (if in scope) |
| Relation JSON | Bootstrap nested vs `RelationFacet` flat | High |
| Relation sources | `relation-source` / `relation-target` not used by adapter | High (if used) |
| Relation tools | Missing `joinSql`, extra fields | Medium |
| Columns | Physical vs facet/structural; provenance unclear | Medium |
| Naming | Tool vs facet field names differ | Low–medium |

---

## 4. Suggested follow-up (implementation story)

1. **Normative decision:** Canonical on-wire relation shape (or explicit bidirectional mapping) aligned with `platform-bootstrap` and `RelationFacet`.
2. **Descriptive alignment:** Serde + seed + tool contract for title/displayName/tags as required.
3. **Tool scope:** Whether `list_*` should expose AI value-mapping, links, qsynth (likely separate tools or flags).
4. **Tests:** Golden paths from representative facet JSON through `SchemaFacetService` to `SchemaCatalogPort` tool output.

---

## 5. Related documents

- [`platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md)
- [`facet-payload-schema-reference.md`](../../../design/metadata/facet-payload-schema-reference.md)
- [`mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md)
