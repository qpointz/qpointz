# WI-199 — mill-py: platform HTTP tests and documentation

Status: `planned`  
Type: `📝 docs` / `🧪 test`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

- **README** ([`clients/mill-py/README.md`](../../../../clients/mill-py/README.md)): document platform HTTP vs Jet/gRPC (`connect`), base URL (`origin` + `/api/v1/metadata` and `/api/v1/schema`), **locked package names** (`mill.metadata`, `mill.schema_explorer`, **`mill.metadata.aio`**, **`mill.schema_explorer.aio`**), auth, `scope`/`facetMode`, **export `scope` + `format`** (**WI-202**), canonical import (multi-doc `kind:`), integration **`MILL_IT_PLATFORM_ORIGIN`**, and URN primer linking to [`ModelEntityUrn`](../../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt) / [`SchemaModelRoot`](../../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/SchemaModelRoot.kt).
- **Unit tests**: error paths, multipart import edge cases, mocked HTTP (integration against a live server is **WI-201**).
- **Exports**: ensure `__init__.py` / `__all__` expose the new public entry points consistently.

## Dependencies

- **WI-192**–**WI-198** (parallel: **WI-201** may add README lines for `pytest -m integration` — coordinate to avoid duplicate edits)

## Acceptance

- `pytest -m unit` green for `clients/mill-py`.
- README section reviewed for accuracy against live controller paths.
