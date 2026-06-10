# mill-py platform HTTP clients

Python support for Mill **platform REST** (Spring Boot) complements the existing **data-plane**
client (`MillClient` over gRPC or Jet HTTP under `/services/jet`).

**Package README:** [`clients/mill-py/README.md`](../../../clients/mill-py/README.md).

## Modules

| Area | Kotlin services | mill-py package |
|------|-----------------|-----------------|
| Metadata CRUD, import/export | `metadata/mill-metadata-service` (`/api/v1/metadata`) | `mill.metadata` / `mill.metadata.aio` |
| Schema explorer (read-only) | `data/mill-data-schema-service` (`/api/v1/schema`) | `mill.schema_explorer` / `mill.schema_explorer.aio` |

## Shared HTTP stack

- **`mill._http_common`**: `httpx` construction (TLS, auth headers), `raise_for_status` /
  Problem Details-aware parsing (RFC 9457) alongside legacy Spring and MillStatusDetails-shaped bodies,
  `encode_metadata_entity_path_segment` for path variables.
- See **[`client-error-transparency.md`](./client-error-transparency.md)** for the shared error contract
  on the **data lane** (`/services/jet`) and how platform HTTP benefits from the same `mill-py` parsers.
- Platform clients use **per-request** `Accept` and `Content-Type` (JSON, YAML export,
  multipart import), not Jet proto defaults.

## Cross-service workflow

URN semantics for model entities are defined in **`data/mill-data-metadata`**
(`ModelEntityUrn`, `SchemaModelRoot`). Schema explorer DTOs expose **`metadataEntityId`**;
Python callers should pass that value into metadata write APIs instead of guessing URNs.

## Canonical metadata I/O

Normative serializer (Kotlin): [`MetadataYamlSerializer`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt).

Import is a **single** `POST /api/v1/metadata/import` with **`kind:`** multi-document YAML only
(legacy `entities:` list envelopes are rejected).

Export (`GET /api/v1/metadata/export`):

- **Order:** all persisted `MetadataScope` documents, then `FacetTypeDefinition` documents **only**
  for types registered in the facet catalog (persisted definitions), then `MetadataEntity` documents.
- **Facet rows:** under each entity, every facet assignment matching the **`scope`** query is included
  (including types with no definition document); **`scope`** filters facet instances only, not scope or
  definition documents.
- **`scope`:** omit → global facet rows only; comma-separated → union; `all` / `*` → no facet scope filter.
- **`format`:** `yaml` (multi-doc) or `json` — a **JSON array** of document objects, **same order** as YAML.

Client-side **bundle import** (concatenate seeds, one upload): `mill.metadata.bulk.import_metadata_bundle`
(WI-203).
