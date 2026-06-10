# WI-197 — mill-py: schema explorer client

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Implement **`SchemaExplorerClient`** for read-only [`SchemaExplorerController`](../../../../data/mill-data-schema-service/src/main/kotlin/io/qpointz/mill/data/schema/api/SchemaExplorerController.kt) under `/api/v1/schema`: `context`, list schemas (`/` and legacy `/schemas`), tree, model root, get schema/table/column (including legacy path aliases).

**Locked public layout:**

- Python package: **`mill.schema_explorer`** (directory `mill/schema_explorer/`).
- Factory: **`mill.schema_explorer.connect(base_url, *, prefix="/api/v1/schema", auth=..., tls_...)`** → **`SchemaExplorerClient`**.
- Default path prefix constant documented next to **`mill.metadata`**.

DTOs mirror [`SchemaDtos.kt`](../../../../data/mill-data-schema-service/src/main/kotlin/io/qpointz/mill/data/schema/api/dto/SchemaDtos.kt). Support query params `scope`, `context` (deprecated), `origin`, `facetMode`.

Error handling uses the same **`MillStatusDetails`** mapping as metadata (**WI-192**).

Document in public docstrings the **workflow** with [`data/mill-data-metadata`](../../../../data/mill-data-metadata): use explorer **`metadataEntityId`** when invoking metadata writes.

## Dependencies

- **WI-192**; **WI-193** may share a small **`FacetResolvedRow`**-shaped type if identical wire shape to metadata facet DTOs.

## Acceptance

- Unit tests with mocks for main GET routes and error envelope parsing.
