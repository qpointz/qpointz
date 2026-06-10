# Metadata URN platform (binding-neutral identity)

**Status:** Design lock (WI-119, March 2026)  
**Normative story:** `docs/workitems/completed/20260330-metadata-rework/SPEC.md` — this page is a concise binding summary.  
**Domain model (entities, facets, merge, persistence):** [`mill-metadata-domain-model.md`](mill-metadata-domain-model.md)

## Purpose

Metadata **instances** are identified by **opaque URNs**. Physical schema/table/column names live in the data layer and in facet payloads; the metadata service persists **URNs only** on entity and facet rows.

## Grammar (Mill identifiers)

Universal Mill URN form:

```
urn:mill/<group>/<class>:<id>
```

**Case rule:** URNs are case-insensitive; canonical form is **lowercase**. At service boundaries, inputs MUST be normalised before persistence and equality checks. **`MetadataEntityUrn.canonicalize`** (WI-120+) is the generic normaliser for any `urn:mill/…` entity id.

| URN | Group | Class | Owner module |
|-----|-------|-------|----------------|
| `urn:mill/metadata/facet-type:<name>` | `metadata` | `facet-type` | `mill-metadata-core` |
| `urn:mill/metadata/scope:<name>` | `metadata` | `scope` | `mill-metadata-core` |
| `urn:mill/metadata/entity-type:<name>` | `metadata` | `entity-type` | `mill-metadata-core` (facet manifest **applicability**, not instance ids) |
| `urn:mill/model/schema:<schema>` | `model` | `schema` | `mill-data-schema-*` |
| `urn:mill/model/table:<schema>.<table>` | `model` | `table` | `mill-data-schema-*` |
| `urn:mill/model/attribute:<schema>.<table>.<col>` | `model` | `attribute` | `mill-data-schema-*` |
| `urn:mill/model/model:model-entity` | `model` | `model` | `mill-data-schema-*` (logical catalog root entity) |
| `urn:mill/model/concept:<id>` | `model` | `concept` | taxonomy / concepts |

**`metadata_entity.entity_res`:** stores **any** valid `urn:mill/…` instance URN the caller provides — not restricted to the `metadata` group.

### Relational & logical-model entity URNs (current JDBC / catalog binding)

The **flat** legacy form `urn:mill/metadata/entity:<local>` is **retired** for relational and first-class model entities. The **authoritative** shapes are **typed** under the `model` group:

| Entity | Form | Example |
|--------|------|---------|
| Schema | `urn:mill/model/schema:<schema>` | `urn:mill/model/schema:sales` |
| Table | `urn:mill/model/table:<schema>.<table>` | `urn:mill/model/table:sales.customers` |
| Column / attribute | `urn:mill/model/attribute:<schema>.<table>.<col>` | `urn:mill/model/attribute:sales.customers.id` |
| Model root | `urn:mill/model/model:model-entity` | (stable id for the logical model node) |
| Concept | `urn:mill/model/concept:<id>` | (taxonomy concept instance) |

`<schema>`, `<table>`, and `<col>` segments are **dot-separated path suffixes** after the final `:`; each segment is normalised to **lowercase** when building or canonicalising URNs.

**Entity class vs `kind`:** the **class** (`schema`, `table`, `attribute`, `model`, `concept`) is part of the URN path. The optional domain field **`MetadataEntity.kind`** (and JPA **`entity_kind`**) duplicates that information for some deployments; work item **WI-144** removes that redundancy.

**REST path variables:** URNs contain `/` in the namespace. They MUST NOT be passed as raw extra path segments. Clients encode the full instance id with **`UrnSlug`** (`io.qpointz.mill.UrnSlug` in `mill-core`); Mill UI uses the TypeScript twin for `/api/v1/metadata/entities/{id}/…`.

## `MetadataEntityUrnCodec` (`mill-data-schema-core`)

Maps catalog-facing names from a physical schema provider snapshot to **canonical typed** `urn:mill/model/…` instance URNs and decodes back to `CatalogPath`. Implementation uses **`RelationalMetadataEntityUrns`** in `mill-data-metadata`. Types: `io.qpointz.mill.data.schema.MetadataEntityUrnCodec`, `DefaultMetadataEntityUrnCodec`, `CatalogPath` in `mill-data-schema-core`.

## `FacetInstance` + `merge_action` (`mill-metadata-core`)

Primary facet row type: `io.qpointz.mill.metadata.domain.facet.FacetInstance` with `MergeAction` (`SET` \| `TOMBSTONE` \| `CLEAR`). Persisted column: `metadata_entity_facet.merge_action` (greenfield DDL).

## Unified audit: `metadata_audit`

Append-only operational audit (SPEC §8.4). **Sole writers:** JPA entity listeners calling `MetadataAuditRepository` — **not** domain services or REST. Replaces legacy `metadata_operation_audit` and draft investigation-audit tables in the greenfield migration.

## Module boundaries

- **Metadata modules** (`metadata/*`, `core/*`) do not import `data/*`.
- Typed JDBC-oriented facet DTOs (relation, structural, …) live in **`mill-data-schema-core`**; metadata-core treats cross-entity references in JSON as opaque strings.

## Persistence shape (greenfield target)

- **`metadata_entity`:** `entity_res` holds the instance URN; no coordinate columns on the entity row.
- **Business tables:** row-audit quad + **`uuid`** on auditable metadata tables (except `metadata_audit`).
- **`metadata_entity_facet`:** assignment rows with **`uuid`**, **`merge_action`**, payload JSON.
- **`metadata_seed`:** startup seed ledger; **`MetadataSeedLedgerRepository`** in core, JPA impl in persistence.

## UI (`mill-ui`)

Schema explorer uses **`metadataEntityId`** (instance URN) alongside explorer ids. Facet APIs target that URN; controls disabled when absent.

## Related documents

- [`metadata-service-design.md`](./metadata-service-design.md)
- [`metadata-canonical-yaml-spec.md`](./metadata-canonical-yaml-spec.md)
