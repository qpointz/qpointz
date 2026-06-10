# WI-203 — mill-py: canonical mass import and export helpers

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

High-level helpers on top of **`MetadataClient`** (from **WI-194** / **WI-198**) for workflows that mirror [`test/datasets/skymill`](../../../../test/datasets/skymill) **canonical** seeds:

1. **Mass import** — Accept an ordered sequence of **paths or binary streams** (e.g. `skymill-canonical.yaml` then `skymill-extras-seed.yaml`). **Concatenate** file contents in that order, separating with `\n---\n` as needed, then call the existing **single** `POST /api/v1/metadata/import` (no server bundle API).  
   - Parameters: `mode` (`MERGE` / `REPLACE`), `actor`; document that **caller order is import order**.

2. **Export canonical** — Call **`GET /export`** with **`scope`** (omit, comma-separated list, or `all` / `*` per **WI-202**) and **`format="yaml" | "json"`**.  
   - **Return type (locked):** both formats return **`str`** (raw response body).  
   - Provide **`parse_metadata_export_json(body: str) -> list[dict[str, Any]]`** (stdlib **`json.loads`**) in the same module for callers who want parsed document maps; document that the JSON body is a **top-level array** matching **WI-202**.

3. **Tests** — Unit tests using **small trimmed fixtures** checked into `clients/mill-py/tests` (or read-only references to `test/datasets/skymill` if CI path is stable); mock httpx; assert merged payload is one multipart upload; assert JSON export round-trips through **`parse_metadata_export_json`**. Optional integration test in **WI-201** with concat import + JSON export.

## Dependencies

- **WI-192**–**WI-194** minimum; **WI-202** for JSON export.  
- **WI-198** if async variants of helpers are required in the same WI (or defer async wrappers).

## Acceptance

- Public API documented in README (**WI-199**) with Skymill-style example.  
- No **PyYAML** required for **import** if helpers only upload file bytes; **JSON** export parsing uses stdlib `json` only.
