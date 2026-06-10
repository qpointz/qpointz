# Metadata URN platform (binding-neutral identity)

**Status:** As-built (March 2026)  
**Delivery record:** `docs/workitems/MILESTONE.md` — WI-111 superseded; WI-112–WI-121 completed (URN platform, persistence, REST/UI alignment, canonical YAML, file snapshot + bootstrap).

## Purpose

Metadata **instances** in Mill are identified by **opaque instance URNs**, not by catalog coordinates stored on `MetadataEntity`. Physical schema/table/column names live in the **data / schema** layer and in facet payloads; the metadata service persists and REST-exposes **URNs only**.

## Grammar (Mill metadata identifiers)

- **Form:** `urn:mill/<group>/<class>:<local>`  
  - Describes **Mill** identifiers (facet types, entity types, scopes, entity instances, etc.), not SQL or JDBC syntax.
- **Entity instances:** `urn:mill/metadata/entity:<path>`  
  - `<path>` is a dot-separated sequence of normalised (trimmed, lowercased) segments, e.g. `sales.customers` for a table or `sales` for a schema-level entity row.
- **REST path variables:** URNs that contain `/` cannot be used raw in a path segment. Controllers accept:
  - the full `urn:…` string when safe,
  - the **prefixed local** part after `urn:mill/metadata/entity:` when it contains no `/`,
  - or a full **UrnSlug** encoding (see `io.qpointz.mill.UrnSlug` in `mill-core`).

## Module boundaries

- **`metadata/*` + `core/*` only** for metadata modules — no `data/*` imports from metadata.
- **Typed** JDBC-oriented facet DTOs (e.g. relation structure) live in **`mill-data-schema-core`**; **metadata-core** treats cross-entity references in JSON as opaque strings and does not depend on those DTOs.
- **Bindings** (relational catalog → instance URN) are implemented in **`mill-data-schema-core`** (e.g. codec matching entities by URN + case-insensitive catalog names).

## Persistence shape (summary)

- **`metadata_entity`:** `entity_res` holds the instance URN; **no** `schema_name` / `table_name` / `attribute_name` columns on the JPA row.
- **Row audit:** `created_at`, `last_modified_at`, `created_by`, `last_modified_by` on metadata tables (exact column names per Flyway squashed script).
- **Append-only investigation audit:** separate table for facet / facet-type investigation events (see Flyway + JPA entities in `mill-metadata-persistence`).

## UI (`mill-ui`)

- Schema explorer DTOs expose **`metadataEntityId`** (instance URN) alongside explorer **`id`** (`schema`, `schema.table`, …).
- Facet fetch/update uses **`metadataEntityId`**; if absent, the client does not call facet endpoints and facet mutation controls are disabled.

## Related documents

- [`metadata-service-design.md`](./metadata-service-design.md) — broader service design and implementation notes.
- [`metadata-canonical-yaml-spec.md`](./metadata-canonical-yaml-spec.md) — import/export YAML handoff.
