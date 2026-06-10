# WI-198 — mill-py: async platform HTTP clients

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Provide **`AsyncMetadataClient`** and **`AsyncSchemaExplorerClient`** using `httpx.AsyncClient`, mirroring the sync method surfaces from **WI-194**–**WI-197**.

Extend **WI-192** shared module if needed for async (thin wrapper around `AsyncClient` construction and request error handling).

**Locked layout (parallel to [`mill.aio`](../../../../clients/mill-py/mill/aio/client.py)):**

- **`mill.metadata.aio.connect`** → **`AsyncMetadataClient`** (package `mill/metadata/aio/`).
- **`mill.schema_explorer.aio.connect`** → **`AsyncSchemaExplorerClient`** (package `mill/schema_explorer/aio/`).

Same TLS/auth/env patterns as sync factories; **`async with`** / **`aclose`** consistent with **`AsyncMillClient`**.

## Dependencies

- **WI-192**–**WI-197** complete for sync counterparts.

## Acceptance

- Async unit tests (pytest-asyncio) mirroring key sync tests.
- Context manager / `aclose` pattern documented and consistent with `AsyncMillClient`.
