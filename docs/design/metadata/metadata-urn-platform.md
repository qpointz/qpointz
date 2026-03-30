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

**Case rule:** URNs are case-insensitive; canonical form is **lowercase**. At service boundaries, inputs MUST be normalised before persistence and equality checks. After **WI-120**, `MetadataEntityUrn.canonicalize` is the generic normaliser for any `urn:mill/…` entity id; until then, relational entity URNs use `urn:mill/metadata/entity:<local>` as today.

| URN | Group | Class | Owner module |
|-----|-------|-------|----------------|
| `urn:mill/metadata/facet-type:<name>` | `metadata` | `facet-type` | `mill-metadata-core` |
| `urn:mill/metadata/scope:<name>` | `metadata` | `scope` | `mill-metadata-core` |
| `urn:mill/model/schema:<id>` | `model` | `schema` | `mill-data-schema-*` |
| `urn:mill/model/table:<schema>.<table>` | `model` | `table` | `mill-data-schema-*` |
| `urn:mill/model/attribute:<schema>.<table>.<col>` | `model` | `attribute` | `mill-data-schema-*` |

**`metadata_entity.entity_res`:** stores **any** valid `urn:mill/…` instance URN the caller provides — not restricted to the `metadata` group.

### Relational metadata entity URNs (current JDBC binding)

- **Form:** `urn:mill/metadata/entity:<path>`  
- `<path>` is dot-separated normalised segments, e.g. `sales.customers` (table) or `sales` (schema).

**REST path variables:** URNs containing `/` cannot be raw path segments. Controllers accept the full `urn:…` when safe, the local part after `urn:mill/metadata/entity:` when it contains no `/`, or **UrnSlug** encoding (`io.qpointz.mill.UrnSlug` in `mill-core`).

## `MetadataEntityUrnCodec` (`mill-data-schema-core`)

Maps catalog-facing names from a physical schema provider snapshot to **canonical** `urn:mill/metadata/entity:…` URNs and decodes back to `CatalogPath`. Types: `io.qpointz.mill.data.schema.MetadataEntityUrnCodec`, `DefaultMetadataEntityUrnCodec`, `CatalogPath` in `mill-data-schema-core`.

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
