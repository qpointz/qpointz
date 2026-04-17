# Schema facet payloads ↔ AI schema tools (`SchemaCatalogPort`)

**Normative source:** [`FacetTypeDefinition`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/FacetTypeDefinition.kt) content in [`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) and [`platform-flow-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml) where applicable.

## Descriptive (`urn:mill/metadata/facet-type:descriptive`)

| Seed / `contentSchema` field | Domain (`DescriptiveFacet`) | `list_schemas` / `list_tables` / `list_columns` |
|-----------------------------|------------------------------|------------------------------------------------|
| `displayName` (alias `title` in JSON) | `displayName` | `displayName` |
| `description` | `description` | `description` |

## Relation (`urn:mill/metadata/facet-type:relation`)

| Seed / `contentSchema` field | Domain (`RelationFacet.Relation`) | `list_relations` |
|-----------------------------|-----------------------------------|------------------|
| Nested `source` / `target` / `columns` | Mapped to `sourceTable`, `sourceAttributes`, `targetTable`, `targetAttributes` in [`RelationPayloadNormalization`](../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/RelationPayloadNormalization.kt) before `SchemaFacets` merge | Same join semantics via `RelationFacet` |
| `joinSql` | `joinSql` | `joinSql` |
| `cardinality` | `cardinality` | `cardinality` |

**Model-entity relations:** Seeds may attach many relation facets to the **model** entity (not per-table). [`SchemaFacetCatalogAdapter`](../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) merges [`ModelRootWithFacets`](../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/ModelRootWithFacets.kt) relations with table-level relations so `list_relations` sees canonical Skymill edges.

## `ai-column-value-mapping*` facet types

**Decision (this story):** not exposed on `list_*` tools; they remain for embeddings / value-mapping flows. Revisit if product requires column-level AI mapping in schema exploration.
