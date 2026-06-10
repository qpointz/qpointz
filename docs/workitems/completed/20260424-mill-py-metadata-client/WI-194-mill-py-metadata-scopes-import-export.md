# WI-194 — mill-py: metadata client — scopes, import, export

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Implement **`MetadataClient`** methods for:

- [`MetadataScopeController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataScopeController.kt): list scopes, create scope, delete scope by slug.
- [`MetadataImportExportController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataImportExportController.kt): `POST /api/v1/metadata/import` (multipart YAML, `mode`, `actor`), `GET /api/v1/metadata/export` (`scope` per **WI-202**: omit → global; comma-separated → union of facet rows; `all` / optional `*` → all scopes; **`format`** `yaml`|`json`). Multi-seed import stays **one request** with **client-merged** YAML (**WI-203**).

Use httpx from **WI-192** with per-request headers; import uses multipart (`file` part per server).

Expose **`mill.metadata.connect(base_url=..., prefix="/api/v1/metadata", auth=..., tls_...)`** → **`MetadataClient`** (package **`mill.metadata`** — locked in **STORY.md**).

## Dependencies

- **WI-192**, **WI-193**

## Acceptance

- Happy-path unit tests with mocked httpx responses for list/create/delete/import/export.
- README touch deferred to **WI-199** unless a minimal docstring example is needed earlier.
