# WI-193 — mill-py: metadata REST DTO models

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Introduce **dataclasses** (or equivalent typed structures) in `clients/mill-py` for metadata API JSON, with parsers that honour **server aliases** (e.g. `id` → `entityUrn`, `facetType` / `scope` on facet rows, `scopeId` on scopes, facet manifest `typeKey` / `displayName` / `payload` vs `facetTypeUrn` / `title` / `contentSchema`).

Mirror DTOs under [`metadata/mill-metadata-service/.../api/dto`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/dto) and merge-trace / history types required by [`MetadataEntityController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt).

Use `dict[str, Any]` (or similar) for nested facet payloads and JSON-schema blobs where the server returns open objects.

## Dependencies

- **WI-192** (shared HTTP helpers optional for this WI if models are pure parse-only; prefer landing WI-192 first for integration smoke).

## Acceptance

- Types cover entities, facet instances, scopes, import result, **facet type manifests**, and **all** REST DTOs required for **WI-194**–**WI-196**, including **merge-trace** and **audit/history** types returned by [`MetadataEntityController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt) (`merge-trace`, `history` routes).
- Unit tests parse representative JSON snippets (including alias keys) for **each** of the above, not only core CRUD DTOs.
