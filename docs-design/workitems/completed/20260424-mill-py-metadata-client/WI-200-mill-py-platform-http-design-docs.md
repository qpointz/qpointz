# WI-200 — mill-py: platform HTTP design documentation

Status: `planned`  
Type: `📝 docs`  
Area: `client`, `metadata`, `data`  
Story: [`STORY.md`](STORY.md)

## Goal

Add or update **design docs** under [`docs/design`](../../../../docs/design) so the **Python platform HTTP clients** (metadata `/api/v1/metadata`, schema explorer `/api/v1/schema`) are discoverable and aligned with server modules:

- **New or extended note** under [`docs/design/client`](../../../../docs/design/client) (e.g. `mill-py-platform-http.md`): architecture diagram or short narrative — shared `mill/_http_common` vs Jet `HttpTransport`, auth (`mill.auth`), error envelope (`MillStatusDetails`), co-location on mill-service HTTP port, link to [`clients/mill-py/README.md`](../../../../clients/mill-py/README.md).
- If **WI-202** lands: document **full canonical export** (MetadataScope + **FacetTypeDefinition only for catalog-defined / known-schema types** + MetadataEntity docs with **all facet instances** that match `scope`, including types with no definition doc), **JSON array** shape, **`scope` / `format`** query semantics, and **import** as **`kind:`** multi-doc only (no `entities:` list); cross-link [`MetadataYamlSerializer`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt).
- **Cross-links** from existing metadata / data design pages where appropriate, for example:
  - [`docs/design/metadata/metadata-service-design.md`](../../../../docs/design/metadata/metadata-service-design.md) or [`metadata-documentation.md`](../../../../docs/design/metadata/metadata-documentation.md) — “Python client” subsection or bullet pointing at the client design note.
  - [`docs/design/data/schema-facet-service.md`](../../../../docs/design/data/schema-facet-service.md) — reference schema explorer REST and Python read client if that doc lists API surfaces.

Do **not** duplicate full OpenAPI; link to controllers and DTO sources instead.

## Dependencies

- **WI-192**–**WI-198** (design should reflect implemented API; can land in same MR wave as **WI-199** or immediately after sync client API is stable).

## Acceptance

- At least one **client** design doc committed; **README** in [`docs/design/client`](../../../../docs/design/client) lists it if that index exists.
- Cross-links from metadata and/or data design docs are valid (relative paths, no broken anchors).
